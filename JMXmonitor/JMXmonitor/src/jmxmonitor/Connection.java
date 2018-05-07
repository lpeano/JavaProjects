package jmxmonitor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class Connection {
	private String ConnectionName="";
	private String UserName="";	
	private String Password="";
	private String remoteServerJMXUrl="";
	private JMXConnector jmxConnection;
	private JMXServiceURL url=null;
	private boolean IsConnected=false;
	private ArrayList<String> QueryList=null;
	private String SpoolFile="";
	private String es_URL="";
	private String es_user="";
	private String es_pass="";

	
	public Connection() {
		// TODO Auto-generated constructor stub
		QueryList=new ArrayList<String>();
	}




	public String getRemoteServerJMXUrl() {
		return remoteServerJMXUrl;
	}


	public void setRemoteServerJMXUrl(String remoteServerJMXUrl) {
		this.remoteServerJMXUrl = remoteServerJMXUrl;
	}


	public JMXConnector getjmxConnection() {
		return jmxConnection;
	}


	public void setJmxConnection(JMXConnector jmxConnection) {
		this.jmxConnection = jmxConnection;
	}


	public boolean isIsConnected() {
		return IsConnected;
	}


	public void setIsConnected(boolean isConnected) {
		IsConnected = isConnected;
	}

	public void connect(int timeout){
		int count=0;
		try {
			url = new JMXServiceURL(remoteServerJMXUrl);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Hashtable<String, String[]> environment = new Hashtable<String, String[]>();
        String[] credentials = new String[]{this.UserName, this.Password};
        environment.put(JMXConnector.CREDENTIALS, credentials);
        boolean connected = false; // tracking the connection status
        do {
            try {

            	this.jmxConnection =  JMXConnectorFactory.connect(url, environment);
                connected = true;
            } catch (IOException e) { //re-trying the connection while the remote server become available..o
            	e.printStackTrace();

                
                try {
					Thread.sleep(5*1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} // 5 sec sleep
            }
            count++;
	            if(count*5*1000>=timeout){
	            	break;
	            }
            } while (!connected );
        if( !connected ){

        	this.IsConnected=false;
        }else{

        	this.IsConnected=true;
        }
       
	}


	public String getUserName() {
		return UserName;
	}


	public void setUserName(String UserName) {
		this.UserName = UserName;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}




	public List<String> getQueryList() {
		return QueryList;
	}




	public void setQueryList(List<String> queryList) {
		QueryList = (ArrayList<String>) queryList;
	}




	public String getConnectionName() {
		return ConnectionName;
	}




	public void setConnectionName(String connectionName) {
		ConnectionName = connectionName;
	}




	public String getSpoolFile() {
		return SpoolFile;
	}




	public void setSpoolFile(String spoolFile) {
		SpoolFile = spoolFile;
	}




	public String getEs_URL() {
		return es_URL;
	}




	public void setEs_URL(String es_URL) {
		this.es_URL = es_URL;
	}




	public String getEs_user() {
		return es_user;
	}




	public void setEs_user(String es_user) {
		this.es_user = es_user;
	}




	public String getEs_pass() {
		return es_pass;
	}




	public void setEs_pass(String es_pass) {
		this.es_pass = es_pass;
	}
}
