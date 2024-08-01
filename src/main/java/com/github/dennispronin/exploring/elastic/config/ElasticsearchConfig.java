package com.github.dennispronin.exploring.elastic.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

public class ElasticsearchConfig {

    public static ElasticsearchClient createClient() {
        var httpClient = RestClient.builder(new HttpHost("localhost", 9200)).build();
        var transport = new RestClientTransport(httpClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
