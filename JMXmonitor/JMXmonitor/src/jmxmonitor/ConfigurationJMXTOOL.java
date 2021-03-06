/**
 * 
 */
package jmxmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

/**
 * @author PeanoLuca
 *
 */
public class ConfigurationJMXTOOL {
	private String ConfigurationFile="";
	private Configuration config=null;
	private List<Connection> Conn=null;
	private HashMap<String,List<String>> Templates;
	private List<String> server_Queries=null;
	
	public ConfigurationJMXTOOL ( String FilePah ){
		this.ConfigurationFile=FilePah;
		this.Conn=new ArrayList<Connection>();
		this.Templates=new HashMap<String,List<String>>();
		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
			    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
			    .configure(params.properties()
			        .setFileName(ConfigurationFile)
			        .setListDelimiterHandler(new DefaultListDelimiterHandler(',')));
		FileBasedConfigurationBuilder<FileBasedConfiguration> buildertpl =
			    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
			    .configure(params.properties()
			        .setFileName(ConfigurationFile)
			        .setListDelimiterHandler(new DefaultListDelimiterHandler('|')));
		try {
			this.config = (Configuration) builder.getConfiguration();
			//Get Connection List
			List<Object> ConnectionList=null;
			ConnectionList =  config.getList("ConnectionNames");
			for (final Object conName: ConnectionList){
				Connection cn= new Connection();
				cn.setConnectionName(conName.toString());
				cn.setPassword(config.getString(conName.toString()+"_password"));
				cn.setUserName(config.getString(conName.toString()+"_username"));
				cn.setSpoolFile(config.getString(conName.toString()+"_output"));
				cn.setRemoteServerJMXUrl(config.getString(conName.toString()+"_connectionUrl"));
				this.Conn.add(cn);
				List<Object> Query_List = config.getList(conName.toString()+"_Queries");
				for (final Object qlist: Query_List){
					cn.getQueryList().add(qlist.toString());
				}
				
			}
			List<Object> TemplateList=config.getList("TemplateNames");
			
			
			Configuration cnf=(Configuration) buildertpl.getConfiguration();
			for (final Object TempName: TemplateList ){
				List<Object> tmplsl=cnf.getList(TempName.toString());
				List<String> tpls = new ArrayList<String>();
				for (final Object tmpl: tmplsl){
					tpls.add(tmpl.toString());
				}
				Templates.put(TempName.toString(), tpls);
			}
			
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}

	public HashMap<String,List<String>> getTemplates(){
		return this.Templates;
	}
	
	public String getConfigurationFile() {
		return ConfigurationFile;
	}

	public void setConfigurationFile(String configurationFile) {
		ConfigurationFile = configurationFile;
	}

	public List<Connection> getConn() {
		return Conn;
	}

	public void setConn(List<Connection> conn) {
		Conn = conn;
	}

	public List<String> getServer_Queries() {
		return server_Queries;
	}

	public void setServer_Queries(List<String> server_Queries) {
		this.server_Queries = server_Queries;
	}
	

}
