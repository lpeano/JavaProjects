package jmxmonitor;


import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

public class JMXMain {
	public static void main(String[] args)  {
		/* COLLECTORE */
		Properties ConfigFileProperties=System.getProperties();
		OutputStream out=System.out;  
		Collector collector = new Collector(ConfigFileProperties.getProperty("ConfigFile"));
		collector.ConnectCollectors();
	    List<String> RetValues=collector.startCollect();
	    System.out.println("Collect Ended\n");
	    for(String ret: RetValues ){
	    	//System.out.println(ret);
	    	byte[] o=ret.getBytes();
	    	try {
				out.write(o);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    System.out.println("Collect Ended");
	    /*
		pusher p=new pusher("D:/10.49.41.133.json");
		System.out.println("1."+p.ParseFile());
		p.cleanBuffer();
		System.out.println("1."+p.ParseFile());
		p.cleanBuffer();
		System.out.println("1."+p.ParseFile());
		p.cleanBuffer();
		System.out.println("1."+p.ParseFile());
		p.cleanBuffer();
		System.out.println("1."+p.ParseFile());
		p.cleanBuffer();
		*/
	}

}
