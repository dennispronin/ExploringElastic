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

#### Hot to run
1. Have docker installed
2. Run <code>docker-compose up</code> while being in the root directory of the project. It will start single node of Elasticsearch and Kibana

#### Sources:
[Elasticsearch in 3 Days](https://www.youtube.com/playlist?list=PL_mJOmq4zsHaF175B7IxMCc4gNP5wcNub)
[Elasticsearch in action repository](https://github.com/madhusudhankonda/elasticsearch-in-action)

#### Self test questions:
1. What is Elasticsearch?
2. Elasticsearch use cases
3. Elasticsearch features
4. Why not using SQL for search? 
5. What are primary components of Elasticsearch?
6. How to scale Elasticsearch?
7. How to make basic Elasticsearch configuration?
8. Explain ELK
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
