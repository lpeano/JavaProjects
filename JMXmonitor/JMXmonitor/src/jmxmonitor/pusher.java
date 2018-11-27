package jmxmonitor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class pusher {
private String fileName="";
private static final byte buffer[] = new byte[8192];
private static BufferedInputStream bis;
private static int par=0,parc=0;
private static boolean instr=false;
private static char bout[]= new char[8192];
private static char bret[]= new char[8192*10];
private static int d=0;
private char OpenPar='{';
private char ClosePar='}';
private static File fd;
private static InputStream  fis;
private static boolean fopened=false;
private static boolean injson=true;
private static int pending=0;

	public pusher(String FileName) {
		this.fileName=FileName;
	}
	
	public void cleanBuffer() {
		for(int c=0;c<bret.length;c++) {
			bret[c]='\0';
		}
	}
	public String ParseFile() {
		if(!fopened) {
			fd=new File(this.fileName);
			fis= null;
		}
		try {
			if(!fopened) {
				
			 fis=new FileInputStream(fd);
			 bis=new BufferedInputStream(fis);
			 fopened=true;
			}
			 int bytesRead;
			 try {
				
				 
				while ((bytesRead = bis.read(buffer)) != -1) {
					
					 String V=	new String(buffer,0,bytesRead);
					 if(pending!=0) {
						 char[] tmp =new char [bout.length-pending];
						 for (int x=0;x<bout.length-pending;x++) {
							 tmp[x]=bout[pending+x];
						 }
						 char[] tmp2 =new char[bout.length];
						 tmp2=V.toCharArray();
						 for(int x=0;x<tmp.length;x++) {
							 bout[x]=tmp[x];
						 }
						 for(int x=tmp.length;x<(tmp2.length);x++) {
							 bout[x]=tmp2[x];
						 }
					 } else {
						 bout=V.toCharArray();
					 }
					 
					 for(int c=0;c<bout.length;c++) {
						 if(injson) { 
							 bret[d*bout.length+c]=bout[c];
							 System.out.println(bret[c]);
						} 
						 System.out.println(bret[c]);
						 if(bout[c]==OpenPar && !instr && injson) {
							 par++;
							 injson=true;
							 bret[d*bout.length+c]=bout[c];
						 }
						 if(bout[c]==ClosePar && !instr && injson) {
							 parc++;

						 }
						 if(bout[c]=='"' && instr==true  && injson) {
							 instr=!instr;
						 }
						 if((par-parc)==0 && injson) {
							
							d=0;
							parc=0;
							par=0;
							injson=!injson;
							pending=c+1;
							return new String(bret);
							
						 }
						 
					 }
					 d++;
				 }
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ("");
	}
	
}
