package org.apache.uima.components.escc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticsearchCasConsumerTest {

	private Logger log = LoggerFactory.getLogger(ElasticsearchCasConsumerTest.class);
	
	@Test
	public void test() throws UIMAException {
		JCas jCas = JCasFactory.createJCas();
		jCas.setDocumentText("This is an English document. It has two sentences.");
		  
		AnalysisEngine analysisEngine = AnalysisEngineFactory.createEngine(
		  ElasticsearchCasConsumer.class,
		  ElasticsearchCasConsumer.PARAM_ES_CLUSTER, "elasticsearch",
		  ElasticsearchCasConsumer.PARAM_ES_PORT, 9300,
		  ElasticsearchCasConsumer.PARAM_ES_TRANSPORT_ADDRESSES, new String[]{"127.0.0.1"});
		  
		analysisEngine.process(jCas);
	}

	@Test
	@Ignore
	public void testElasticsearchClient() throws UnknownHostException {
		Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").build();
		Client client = TransportClient.builder().settings(settings).build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));

		Map<String, Object> document = new HashMap<String, Object>();
		document.put("content", "some content");
		document.put("language", "en");
		IndexResponse response = client.prepareIndex("ccse", "site").setSource(document).get();
		log.info(response.toString());

		client.close();
	}
}