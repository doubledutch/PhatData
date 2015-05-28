package me.doubledutch.phat.servlet;

import java.io.IOException;
import java.util.*;
import java.io.*;
import org.json.*;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileServlet extends HttpServlet{
	private long appStartTime=System.currentTimeMillis();
	public static String sourceFolder=null;

	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		String uriPath=request.getRequestURI();
		String fileName=uriPath;
		if(sourceFolder==null){
			fileName="/site"+fileName;
		}else{
			fileName=sourceFolder+fileName;
		}
		if(fileName.indexOf("..")>-1){
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
		}
		// TODO: timestamps on jar resources?
		if(request.getDateHeader("If-Modified-Since")!=-1){
			System.out.println(" + If-Modified-Since "+request.getDateHeader("If-Modified-Since"));
			System.out.println(" + Last modified "+appStartTime);
			if(request.getDateHeader("If-Modified-Since")>=appStartTime){
				System.out.println(" + Not modified since");
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}

		InputStream in =null;
		if(sourceFolder==null){
			in=this.getClass().getResourceAsStream(fileName);
		}else{
			in=new FileInputStream(fileName);
		}
		if(in==null){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		try{
			response.setContentType(getContentTypeForExtension(uriPath));
			OutputStream out=response.getOutputStream();
	        byte[] buf=new byte[32768];
	        int read=0;
	        int total=0;
	        while(read>-1){
	            read=in.read(buf,0,32768);
	            if(read>0){
	            	out.write(buf,0,read);
	            	total+=read;
	            }
	        }
	        in.close();
	        out.flush();
	    }catch(Exception e){
	    	response.sendError(HttpServletResponse.SC_NOT_FOUND);
	    }
	}

	protected static String getContentTypeForExtension(String path){
		if(path.indexOf(".")>-1){
			String suffix=path.substring(path.lastIndexOf(".")).toLowerCase();
			if(suffix.equals(".htm"))return "text/html";
			if(suffix.equals(".html"))return "text/html";
			if(suffix.equals(".txt"))return "text/plain";
			if(suffix.equals(".css"))return "text/css";
			if(suffix.equals(".js"))return "text/javascript";
			if(suffix.equals(".pdf"))return "application/pdf";
			if(suffix.equals(".gz"))return "application/x-gtar";
			if(suffix.equals(".tar"))return "application/x-tar";
			if(suffix.equals(".class"))return "application/x-java-vm";
			if(suffix.equals(".jar"))return "application/x-java-archive";
			if(suffix.equals(".zip"))return "application/zip";
			if(suffix.equals(".dmg"))return "application/octet-stream";
			if(suffix.equals(".rss"))return "application/xml";
			if(suffix.equals(".xml"))return "application/xml";
			if(suffix.equals(".gif"))return "image/gif";
			if(suffix.equals(".jpg"))return "image/jpeg";
			if(suffix.equals(".jpeg"))return "image/jpeg";
			if(suffix.equals(".jpe"))return "image/jpeg";
			if(suffix.equals(".png"))return "image/png";
		}
		return "application/octet-stream";
	}
}