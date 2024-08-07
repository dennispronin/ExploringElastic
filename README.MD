## About Elasticsearch

**Elasticsearch** is an application optimized for performant search and scalable storage of large datasets. At its core, it relies on Apache Lucene for its powerful search capabilities. Elasticsearch enhances this foundation by providing an easy-to-use and rich REST API for CRUD and utility functions, as well as facilitating easy horizontal scaling.

#### Use cases:
1. Utility purposes for log aggregation and analysis
2. Big data and machine learning fields for storing and analyzing large datasets
3. Search-Heavy applications, when traditional SQL solutions are not enough

#### Notable features:
1. Full-text search
2. Fuzzy search
3. Easy horizontal scaling
4. Rest API with rich Query DSL

#### Primary components:
- Cluster - set of nodes
- Node - Elasticsearch instance located on separate servers
- Shard - Apache Lucene instance. The application that stores data and performs all primary computations. Number of shards is configured for each index individually
- Replica - copy of shard located on separate node. Number of replicas is configured for each index individually
- Index - collection of documents of single type
- Document - a record in index

## Configuration and known issues with solutions
> Next section is a summary of my experience of using Elasticsearch in production

### Index Topology

Index topology encompasses:
- Data grouping within an index
- Sharding within an index
- Mapping fields within an index used for search queries

Topology should be chosen based on read queries to minimize search query processing time. Unlike RDBMS, which prioritizes data normalization and avoiding duplication, NoSQL solutions focus on how data will be used (read or write). Inevitably, data duplication and redundancy are seen as a reasonable trade-off for transparency and query speed.

### Grouping into Indexes

Data should be grouped into indexes by relevance, ensuring that the most frequently accessed data is in one index. The size of the index is limited by the size of one shard and the number of shards in an index. For my current project, monthly indexing was chosen, with each index representing data for one calendar month.

### Choosing the Number of Shards and Sharding Key

From ES developers' recommendations:

"A good rule-of-thumb is to ensure you keep the number of shards per node below 20 per GB heap it has configured. A node with a 30GB heap should therefore have a maximum of 600 shards. Aim to keep the average shard size between at least a few GB and a few tens of GB."

600 shards per node - from all indexes including all replicas.

Increasing the number of shards while decreasing shard size (less than a few GB) generally leads to significant overhead in search queries.

In the ES cluster for my current project, there are three data nodes with 30GB heap each, 60+ indexes with 5 shards, and one additional replica. The average index size is 50GB, so the average shard size is 10GB, with 200 shards per node.

This configuration wasn't achieved immediately. Initially, the number of shards reached three hundred, using a round-robin sharding algorithm. As load increased, the cluster couldn't keep up with search queries (the internal ES queue filled up, clients either timed out or the query was rejected immediately).

The first data model in ES was designed based on relational storage principles: data first - queries later. During UI development, key client query parameters became clear, specifically that each query includes a date range and account number. Thus, it became apparent that with round-robin sharding, all shards must be searched to find all transactions for an account. Using the account number as the sharding key was more efficient, building the data model based on queries.

### Adding a Mapping Template to the Cluster

Fields in ES can have various types. By default, if a field is not specified in the mapping, its value will be stored as both text for full-text search and keyword for exact matching.

To avoid overhead in data storage in indexes and inverted indexes, each field in the mapping should be specified according to how it will be used in search queries.

Sorting data during document indexing plays a crucial role. It should match the sorting in the search query (most search queries) and consider search query filters. This avoids programmatic sorting and allows bulk reading from the disk.

For example, the sort order when indexing looked like this:

- Bank transaction ID
- Bank transaction date/time
- Bank transaction account number

Since in my project read scenarios always include an account number as a filter, it is optimal to store transactions for one account sequentially on the disk, allowing maximum data blocks (list of bank transactions) for one account to be read per disk access, rather than one operation per IOPS (as was the case when transactions were sorted and stored by ID).

The issue became clear when ES couldn't handle increased load, and monitoring revealed a high IOPS count, with only tens of MB/s of data read from the disk.

Changing the sort order to:

- Bank transaction account number
- Bank transaction date/time
- Bank transaction ID

accelerated read scenarios by up to five times.

The index.unassigned.node_left.delayed_timeout parameter significantly affects performance since network blips can trigger resource-intensive reallocation. This parameter should be added to the index mapping template or each index individually.

To search by part of an entered text, use one-sided edgeNGramm.

### Cluster Configuration

ES heavily relies on file system caching, so the more RAM available to the ES process on the node, the better (the higher the cluster load capacity).

For my project, for example, Load testing showed that doubling the memory allowed handling four times the load profile.

Adding RAM to the cluster is currently the most rational solution to increase ES cluster performance (compared to adding new nodes or switching to SSDs).

Another issue can arise from allocating too much heap memory on one node. Values above approximately 30GB (varying for each JVM implementation) disable pointer compression (oops compression), leading to significant memory overhead, potentially rendering the cluster non-functional even under relatively low loads.

In summary, the ES process heap should be no more than 30GB, and native - as much as possible (as much as the OS can handle for file system caching).

An obvious configuration is to separate data nodes from master nodes.

### Running in a Docker Container

When running an ES node in a Docker container, ES may determine the number of available cores as 1, causing all node processes (search, write, merging, etc.) to occur in a single thread. Set the number of available cores explicitly via elasticsearch.yml parameter node.processors. Pool sizes for each group (search, write, management) will be calculated based on this number.

A corresponding memory limit must be set (see point 5).

### Constructing Search Queries

When constructing search queries, use the filter context as much as possible (all parameters not requiring scoring should be passed in the search query via filter).

ES also recommends using search_after navigation rather than pageNumber.

Use spring-data cautiously, as early versions, for example, always generated a read request for a write request (to check record existence).

### Slow queries on Elasticsearch v6.8.13

If some of your queries take too long to execute, for example, more than 30 seconds, consider setting the soKeepAlive parameter to true. This configuration will force Elasticsearch to reuse connections, thus reducing latency.
Example:

<code>

 	@Bean
    public RestClientBuilderCustomizer clientBuilderCustomizer() {

        return new RestClientBuilderCustomizer() {

            @Override
            public void customize(RestClientBuilder builder) {
            }

            @Override
            public void customize(HttpAsyncClientBuilder builder) {
                builder.setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(true).build());
                builder.setKeepAliveStrategy((response, context) -> 300000);
            }

            @Override
            public void customize(RequestConfig.Builder builder) {
            }
        };
    }
</code>

## How to run this example project
1. Have docker installed
2. Run <code>docker-compose up</code> while being in the root directory of the project. It will start a single node of Elasticsearch with Kibana and an instance of PostgreSQL
3. Compile and start the application
   - Once started, it will fill Elasticsearch and PostgreSQL with data taken from [spotify_millsongdata.csv](spotify_millsongdata.csv)
   - The main method will make several queries to Elasticsearch and PostgreSQL and will print the execution speed

#### Very simple Benchmark:
The following is a comparison of query execution speed when searching songs which contain specific words using Elasticsearch and PostgreSQL with several optimisations

|                        | ElasticSearch | PostgreSQL | PostgreSQL index | PostgreSQL full-text |
|:----------------------:|:-------------:|:----------:|:----------------:|:--------------------:|
|  peace (1811 records)  |    115 ms     |   565 ms   |      147 ms      |        65 ms         |
| science (1186 records) |     16 ms     |   519 ms   |      29 ms       |         5 ms         |
|    dog (338 records)   |     13 ms     |   536 ms   |      64 ms       |        45 ms         |
|  space (1714 records)  |     13 ms     |   534 ms   |      50 ms       |        26 ms         |

Things to consider:
1. Note that due to the Elasticsearch's tokenizations approach, the regular "match" query is not equal to PostgreSQL's "%ILIKE%"
   - Example: Elastic will not find "loveable" word with "love" as search string, but PostgreSQL will
   - To make the results count equal, I am using Elastic's wildcard match query
2. Modifying operations are not considered for this example, but they should be in the real world scenarios

Conclusion: Although ElasticSearch offers obvious performance benefits over RDBMS, several possible optimizations should be considered before choosing to use ElasticSearch, as it may complicate the system.

## Sources:
- [Elasticsearch in 3 Days](https://www.youtube.com/playlist?list=PL_mJOmq4zsHaF175B7IxMCc4gNP5wcNub)
- [Elasticsearch in action repository](https://github.com/madhusudhankonda/elasticsearch-in-action)
- [Java Elasticsearch client docs](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/introduction.html)
- https://www.elastic.co/blog/how-many-shards-should-i-have-in-my-elasticsearch-cluster
- https://www.elastic.co/guide/en/elasticsearch/reference/current/max-number-of-threads.html
- https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-types.html
- https://www.baeldung.com/jvm-compressed-oops
- https://www.elastic.co/guide/en/elasticsearch/reference/current/delayed-allocation.html
- https://octoperf.com/blog/2018/09/21/optimizing-elasticsearch/
- https://www.elastic.co/guide/en/elasticsearch/reference/current/modules-threadpool.html
- https://www.elastic.co/guide/en/elasticsearch/reference/current/query-filter-context.html
- https://www.elastic.co/guide/en/elasticsearch/reference/7.9/paginate-search-results.html
- https://www.elastic.co/guide/en/elasticsearch/reference/current/analysis-edgengram-tokenfilter.html

## Self test questions:
1. What is Elasticsearch?
2. Elasticsearch use cases
3. Elasticsearch features
4. Why not using SQL for search? 
5. What are primary components of Elasticsearch?
6. How to scale Elasticsearch?
7. How to make basic Elasticsearch configuration?
8. Explain how ELK works
9. What if the data type of a field changes in the index?
10. What is the structure of index?
11. Cluster health statuses
12. match vs match_all
13. Explain Relevancy score
14. What is term query?
15. Explain fuzziness setting
16. What is the difference between fuzziness as a parameter in match and fuzzy with fuzziness instead of match?
17. How to make full-text search in Elasticsearch?
18. Explain text analyzers and text tokenization process
