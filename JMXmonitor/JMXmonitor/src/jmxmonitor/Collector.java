package jmxmonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
		FileOutputStream fos = null;
		List<String> ReturnedValues=new ArrayList<String>();
		for(Connection cn : super.getConn()){
			if(!cn.getSpoolFile().equals("-")) {
				try {
					// Open File report
					File  file = new File(cn.getSpoolFile());
					fos = new FileOutputStream(file, true);
					if (!file.exists()) {
						file.createNewFile();
					}
					if(cn.isIsConnected()) {
						fos.write(DoCollect(cn).concat("\n").getBytes());
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}finally{
					try {
						fos.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
			if(cn.isIsConnected() && ( cn.getSpoolFile().equals("-") || cn.getSpoolFile().isEmpty())){
				ReturnedValues.add(DoCollect(cn));
			}
		}
		return(ReturnedValues);
	}
	
	private String DoCollect(Connection cn ){
		//-------------------
     
			MBeanServerConnection mBeanServerConnection;
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.createObjectNode();
		    final JsonNodeFactory factory = JsonNodeFactory.instance;
		    String retString="";
		    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		    
			try {
		
			mBeanServerConnection = cn.getjmxConnection().getMBeanServerConnection();
			ObjectName query;
			for( Object q : cn.getQueryList()){
				

			ArrayList<String> queries=(ArrayList<String>) super.getTemplates().get(q.toString());
			for (Object qu11: queries){

			try {
				query = new ObjectName((String) qu11);
				Set<ObjectName> mbeans;
				try {
					mbeans = mBeanServerConnection.queryNames(query, null);
					try {
					DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();	
					DocumentBuilder documentBuilder;
					try {
						// XML implementation
						documentBuilder = documentBuilderFactory.newDocumentBuilder();
						Document document=documentBuilder.newDocument();
						Element rootElement = (Element) ((Document) document).createElement("report");
					    rootElement.setAttribute("Name", "report");
					    document.appendChild((Node) rootElement);
					    ((ObjectNode)rootNode).put("Timestamp", timestamp.toString());
					    ((ObjectNode)rootNode).put("HostName",cn.getConnectionName());
					for (Object mbean : mbeans)
					{
						final ObjectNode node = factory.objectNode();
						try {
									try {
										WriteAttributes( mBeanServerConnection, (ObjectName)mbean, (Object) mbean,document,node,factory);
									
									} catch ( ParserConfigurationException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
						} catch (InstanceNotFoundException | IntrospectionException | ReflectionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						((ObjectNode)rootNode).set(node.get("Name").toString().replaceAll("\"", ""), node);
					}
					retString=(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(((ObjectNode)rootNode)));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					} catch (ParserConfigurationException e1) {
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
			}
			
			return (retString);
		//-------------------
	}
	
	/*public void getAllObject(){
	
		for(Connection cn : super.getConn()){
		try {
			MBeanServerConnection mBeanServerConnection=cn.getjmxConnection().getMBeanServerConnection();
			
			Set<ObjectName> mbeans = mBeanServerConnection.queryNames(null, null);
			for (Object mbean : mbeans)
			{

			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}*/
	
	private static void WriteAttributes(final MBeanServerConnection mBeanServer, final ObjectName http,Object mb ,Document document,ObjectNode node,JsonNodeFactory factory)
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
	    Element rElement=document.getDocumentElement();	
	    Element rootElement = (Element) ((Document) document).createElement("item");
	    node.put("Name", http.getCanonicalName().toString());
	    rootElement.setAttribute("Name", http.getCanonicalName().toString());
	    for (MBeanAttributeInfo attr : attrInfo)
	    {
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
	    			
					Element el = (Element) ((Document) document).createElement(attr.getName());
	    			for( Map.Entry<String, Object> pair: mapV.entrySet()){
		    			putWithType(pair,child);
	    			}
	    			node.set(attr.getName(),child);
	    			rootElement.appendChild(el);
					
	    			

	    		}
	    		if(attr.getType().toString().equals("double")){
	    			node.put(attr.getName(),  (double) mBeanServer.getAttribute(http,attr.getName()));
	    		}
	    	}
	    	rElement.appendChild(rootElement);
			
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
