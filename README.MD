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
- Shard - Apache Lucene instance. The application that stores data and performs all primary computations. Number of shardS is configured for each index individually
- Replica - copy of shard located on separate node. Number of replicas is configured for each index individually
- Index - collection of documents of single type
- Document - a record in index

## Hot to run project
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

#### Sources:
[Elasticsearch in 3 Days](https://www.youtube.com/playlist?list=PL_mJOmq4zsHaF175B7IxMCc4gNP5wcNub)
[Elasticsearch in action repository](https://github.com/madhusudhankonda/elasticsearch-in-action)
[Java Elasticsearch client docs](https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/introduction.html)

#### Self test questions:
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
