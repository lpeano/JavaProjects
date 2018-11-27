package jmxmonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;
import javax.management.openmbean.CompositeData;
import javax.xml.parsers.ParserConfigurationException;


import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;

public class Collector extends ConfigurationJMXTOOL {
	private static Map <String,Object> mapV;
	public Collector(String ConfigFile) {
		// TODO Auto-generated constructor stub
		super(ConfigFile);
	}
	public void ConnectCollectors(){
		ArrayList<Connection> con=(ArrayList<Connection>) super.getConn();
		for(Connection c: con){
			if(!c.isIsConnected()){
				c.connect(50);
			}
		}
	}

	public List<String> startCollect(){
		String JsonCollectOutput="";
		List<String> ReturnedValues=new ArrayList<String>();
		for(Connection cn : super.getConn()){
			JsonCollectOutput=DoCollect(cn).concat("\n");
			if(!cn.getSpoolFile().equals("-")) {
				SpoolToFile(JsonCollectOutput,cn);			
			}
			
			// If configured send data to elasticsearch
			if(!cn.getEs_URL().isEmpty() && (!JsonCollectOutput.isEmpty())) {
				SendDataToES(JsonCollectOutput,cn);
			}
			if(cn.isIsConnected() && ( cn.getSpoolFile().equals("-") || cn.getSpoolFile().isEmpty())){
				ReturnedValues.add(DoCollect(cn));
			}
		}
		return(ReturnedValues);
	}
	
	private void SendDataToES(String jsonCollectOutput,Connection cn) {
		// Basic Credentials
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
		        new UsernamePasswordCredentials(cn.getEs_user(), cn.getEs_pass()));
		
		// Connections
		@SuppressWarnings("unused")
		RestClientBuilder builder = RestClient.builder(new HttpHost(cn.getEs_URL(), cn.getEs_port()))
		        .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
		            @Override
		            public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
		                return httpClientBuilder.setDefaultIOReactorConfig(
		                        IOReactorConfig.custom().setIoThreadCount(1).build()).setDefaultCredentialsProvider(credentialsProvider);
		            }
		        });
		@SuppressWarnings("unused")
		RestClient restClinet= builder.build();
		
		
		HttpEntity entity = new NStringEntity(jsonCollectOutput, ContentType.APPLICATION_JSON);
		try {
			Map<String, String> params = Collections.singletonMap("", "");
			
			Response respClient=restClinet.performRequest("POST", "/collector/".concat(cn.getConnectionName()).concat("/"),params,entity);
			System.out.println("Response :"+respClient.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				restClinet.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//response.isDone();
		
	/*	@SuppressWarnings("resource")
		Settings settings= Settings.EMPTY;
		TransportClient client = new PreBuiltTransportClient(settings);
		try {
			client.addTransportAddress(new TransportAddress(InetAddress.getByName(cn.getEs_URL()), cn.getEs_port()));
			IndexResponse response = client.prepareIndex("HostCollection",cn.getConnectionName())
					.setSource(jsonCollectOutput, XContentType.JSON)
					.get();
			System.out.println("Elastic response is :"+response.status());
			
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			client.close();
		}*/
		
		
		
		
	}
	// Method to spool output to file.
	private void SpoolToFile(String JsonString,Connection cn) {
		FileOutputStream fos = null;
	
		File  file = new File(cn.getSpoolFile());

		try {
			if (!file.exists()) {
				file.createNewFile();		
				fos = new FileOutputStream(file, true);
				fos.write(JsonString.getBytes());
			}else {
				fos = new FileOutputStream(file, true);
				fos.write((JsonString).getBytes());
			}
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	

	private String DoCollect(Connection cn ){
		//-------------------
     
			MBeanServerConnection mBeanServerConnection;
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.createObjectNode();
		    final JsonNodeFactory factory = JsonNodeFactory.instance;
		    String retString="";
		    ArrayNode outerArray=null;
		    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    
			try {
		
			mBeanServerConnection = cn.getjmxConnection().getMBeanServerConnection();
			ObjectName query;
			for( Object q : cn.getQueryList()){
				
			
			ArrayList<String> queries=(ArrayList<String>) super.getTemplates().get(q.toString());
			for (Object qu11: queries){
			Thread.sleep(10L);
			if(super.getTemplateType(q.toString()).getyType().toString().equals("List")) {
				outerArray = mapper.createArrayNode(); 
				((ObjectNode)rootNode).set(super.getTemplateType(q.toString()).gettName(),outerArray);
			}
			try {
				query = new ObjectName((String) qu11);
				Set<ObjectName> mbeans;
				try {
					mbeans = mBeanServerConnection.queryNames(query, null);
					try {
					    ((ObjectNode)rootNode).put("@timestamp", timestamp.toLocalDateTime().toString());
					    ((ObjectNode)rootNode).put("HostName",cn.getConnectionName());
					for (Object mbean : mbeans)
					{
						
						final ObjectNode node = factory.objectNode();
						try {
									try {
										WriteAttributes( mBeanServerConnection, (ObjectName)mbean, (Object) mbean,node,factory);
									
									} catch ( ParserConfigurationException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
						} catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(super.getTemplateType(q.toString()).getyType().toString().equals("List")) {
							outerArray.add(node);
						} else {
							((ObjectNode)rootNode).set(node.get("Name").toString().replaceAll("\"", ""), node);
						}
					}
					if(cn.getPrettyJson()==true) {
						retString=(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(((ObjectNode)rootNode)));
					} else {
						retString=(mapper.writer().writeValueAsString(((ObjectNode)rootNode)));
					}
						
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
						

		
		
			} catch (MalformedObjectNameException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			}
			}
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			return (retString);
		//-------------------
	}
	
	private static void WriteAttributes(final MBeanServerConnection mBeanServer, final ObjectName http,Object mb ,ObjectNode node,JsonNodeFactory factory)
	        throws InstanceNotFoundException, IntrospectionException, ReflectionException, IOException, ParserConfigurationException
	{
		

		MBeanInfo info = null;
		try {
			info = mBeanServer.getMBeanInfo(http);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MBeanAttributeInfo[] attrInfo = info.getAttributes();
	    node.put("Name", http.getCanonicalName().toString());
	    for (MBeanAttributeInfo attr : attrInfo)
	    {
	    	try {
				Thread.sleep(1L);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	       try {

	    	if(!attr.getName().toString().equals("UsageThreshold")){
	    		if(attr.getType().toString().equals("java.lang.String")){
	    			node.put(attr.getName(), (String) mBeanServer.getAttribute(http,attr.getName()));
	    		}
	    		if(attr.getType().toString().equals("boolean")){
	    			node.put(attr.getName(),  (boolean)mBeanServer.getAttribute(http,attr.getName()));
	    		}
	    		if(attr.getType().toString().equals("java.lang.Boolean")){
	    			node.put(attr.getName(), (Boolean) mBeanServer.getAttribute(http,attr.getName()));
	    		}
	    		if(attr.getType().toString().equals("java.lang.Long")){
	    			if((mBeanServer.getAttribute(http,attr.getName()))!=null){
		    			node.put(attr.getName(), (Long) mBeanServer.getAttribute(http,attr.getName()));
	    			}
	    		}
	    		if(attr.getType().toString().equals("long")){
	    			if((mBeanServer.getAttribute(http,attr.getName()))!=null){
		    			node.put(attr.getName(), (long) mBeanServer.getAttribute(http,attr.getName()));
	    			}
	    		}
	    		if(attr.getType().toString().equals("java.lang.Integer")){
	    			if((mBeanServer.getAttribute(http,attr.getName()))!=null){
		    			node.put(attr.getName(),  (Integer) mBeanServer.getAttribute(http,attr.getName()));
	    			}
	    		}
	    		if(attr.getType().toString().equals("int")){
	    			if((mBeanServer.getAttribute(http,attr.getName()))!=null){
	    				node.put(attr.getName(), (int)  mBeanServer.getAttribute(http,attr.getName()));
	    			}
	    		}
	    		
	    		if(attr.getType().toString().equals("javax.management.openmbean.CompositeData") && (((CompositeData) mBeanServer.getAttribute(http,attr.getName()) != null))){
	    			final ObjectNode child = factory.objectNode();
	    			mapV = toMap((CompositeData) mBeanServer.getAttribute(http,attr.getName()));
	    			for( Map.Entry<String, Object> pair: mapV.entrySet()){
		    			putWithType(pair,child);
	    			}
	    			node.set(attr.getName(),child);
	    		}
	    		if(attr.getType().toString().equals("double")){
	    			node.put(attr.getName(),  (double) mBeanServer.getAttribute(http,attr.getName()));
	    		}
	    	}
		} catch (IOException e) {
			// TODO Auto-generated catch block
		} catch (AttributeNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (MBeanException e) {
			
		} catch (RuntimeMBeanException e){
		}
	    }
	}
	
	private static void putWithType(final Map.Entry<String, Object> pair, ObjectNode child){
		if(pair.getValue() instanceof Long){
			child.put(pair.getKey().toString(), (Long)pair.getValue());
		}
		if(pair.getValue() instanceof long[]){
			child.put(pair.getKey().toString(), (long)pair.getValue());
		}		
		if(pair.getValue() instanceof boolean[]){
			child.put(pair.getKey().toString(), (boolean)pair.getValue());
		}	
		if(pair.getValue() instanceof Boolean){
			child.put(pair.getKey().toString(), (Boolean)pair.getValue());
		}		
		if(pair.getValue() instanceof Integer){
			child.put(pair.getKey().toString(), (Integer)pair.getValue());
		}	
		if(pair.getValue() instanceof int[]){
			child.put(pair.getKey().toString(), (int)pair.getValue());
		}	
		if(pair.getValue() instanceof double[]){
			child.put(pair.getKey().toString(), (double)pair.getValue());
		}
	}
	private static Map<String, Object> toMap(CompositeData data)
	{
	    ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
	    // never trust JMX to do the right thing
	    Set<String> keySet = data.getCompositeType().keySet();
	    if (keySet != null) {
	        for (String key : keySet) {
	            if (key != null) {
	                Object value = data.get(key);
	                if (value != null) {
	                    builder.put(key, value);
	                }
	            }
	        }
	    }
	    return builder.build();
	}
}
