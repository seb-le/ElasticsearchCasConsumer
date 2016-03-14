package org.apache.uima.components.escc;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.apache.uima.util.Logger;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;

public class ElasticsearchCasConsumer extends JCasAnnotator_ImplBase {

	public static final String PARAM_ES_CLUSTER = "es_cluster";
	public static final String PARAM_ES_TRANSPORT_ADDRESSES = "es_transport_addresses";
	public static final String PARAM_ES_PORT = "es_port";

	private Logger logger;
	private Client client;

	public void initialize(UimaContext context) throws ResourceInitializationException {
		String esCluster = (String) context.getConfigParameterValue(PARAM_ES_CLUSTER);
		int esPort = (Integer) context.getConfigParameterValue(PARAM_ES_PORT);
		String[] esTransportAddressStrings = (String[]) context.getConfigParameterValue(PARAM_ES_TRANSPORT_ADDRESSES);
		TransportAddress[] esTransportAddresses = new InetSocketTransportAddress[esTransportAddressStrings.length];
		for (int i=0; i < esTransportAddresses.length; i++) {
			try {
				esTransportAddresses[i] = new InetSocketTransportAddress(InetAddress.getByName(esTransportAddressStrings[i]), esPort);
			} catch (UnknownHostException e) {
				throw new ResourceInitializationException(e);
			}
		}

		Settings settings = Settings.settingsBuilder().put("cluster.name", esCluster).build();
		client = TransportClient.builder().settings(settings).build().addTransportAddresses(esTransportAddresses);
		logger = context.getLogger();
		logger.log(Level.INFO, "Initialized ");
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		Map<String, Object> document = new HashMap<String, Object>();
		document.put("content", aJCas.getDocumentText());
		document.put("language", aJCas.getDocumentLanguage());
		IndexResponse response = client.prepareIndex("ccse", "site").setSource(document).get();
	}

}