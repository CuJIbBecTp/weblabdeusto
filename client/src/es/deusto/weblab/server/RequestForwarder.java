/*
* Copyright (C) 2005 onwards University of Deusto
* All rights reserved.
*
* This software is licensed as described in the file COPYING, which
* you should have received as part of this distribution.
*
* This software consists of contributions made by many individuals, 
* listed below:
*
* Author: Pablo Orduña <pablo@ordunya.com>
*
*/

package es.deusto.weblab.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class RequestForwarder extends HttpServlet{

	private final String host = "localhost";
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
		final URLConnection serverConnection = new URL("http", this.host, req.getRequestURI()).openConnection();
		serverConnection.setDoOutput(true);
		
		this.forwardHeadersFromBrowser(req, serverConnection);
		serverConnection.addRequestProperty("Connection", "close");
		serverConnection.addRequestProperty("X-Faa", "ber");
		
		this.forwardStreamToEnd(req.getInputStream(), serverConnection.getOutputStream());
		
		String[] postHeaders = new String[]{ "Server", "Date", "Content-type", "Content-length", "Set-Cookie", "Content-encoding", "Vary", "Last-Modified"};
		this.forwardHeadersToBrowser(resp, serverConnection, postHeaders);
		resp.addHeader("Connection", "close");
		resp.addHeader("X-Foo", "bar");
		
		this.forwardStreamToSize(serverConnection.getInputStream(), resp.getOutputStream(), serverConnection.getContentLength());
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final String queryString;
		if(req.getQueryString() != null)
			queryString = "?"+req.getQueryString();
		else
			queryString = "";
		final HttpURLConnection serverConnection = (HttpURLConnection)new URL("http", this.host, req.getRequestURI()+queryString).openConnection();
		serverConnection.setDoOutput(true);
		serverConnection.setRequestMethod(req.getMethod());

		this.forwardHeadersFromBrowser(req, serverConnection);
		serverConnection.addRequestProperty("Connection", "close");
		serverConnection.addRequestProperty("X-Faa", "ber");
		
		String[] getHeaders = new String[]{ "Server", "Date", "Set-Cookie", "Content-encoding", "Vary", "Last-Modified"};
		this.forwardHeadersToBrowser(resp, serverConnection, getHeaders); // This makes the request!
		resp.addHeader("Connection", "close");
		resp.addHeader("X-Foo", "bar");
		
		this.forwardStreamToSize(serverConnection.getInputStream(), resp.getOutputStream(), serverConnection.getContentLength());
	}

	private void forwardHeadersFromBrowser(HttpServletRequest req, final URLConnection serverConnection) {
	    final Enumeration<?> headersIn = req.getHeaderNames();
	    while(headersIn.hasMoreElements()){
	    	final String header = (String)headersIn.nextElement();
	    	if(this.isValidHeader(header))
	    	    serverConnection.addRequestProperty(header, req.getHeader(header));
	    }
	}

	private void forwardHeadersToBrowser(HttpServletResponse resp, final URLConnection serverConnection, String[] headers) {
	    for(final String header : headers){
	        final String value = serverConnection.getHeaderField(header);
	        if(this.isValidHeader(header) && value != null){
	    		resp.addHeader(header, value);
	        }
	    }
	}

	private final static Collection<String> badHeaders = Arrays.asList("host", "connection", "keep-alive");
	
	private boolean isValidHeader(String header) {
	    return header != null && !RequestForwarder.badHeaders.contains(header.toLowerCase());
	}

	private void forwardStreamToEnd(final InputStream is, final OutputStream os) throws IOException {
    	final byte [] buffer = new byte [4096];
    	int bytesRead;
    	do{
    		bytesRead = is.read(buffer);
    		
    		if(bytesRead > 0)
    		    os.write(buffer, 0, bytesRead);
    	}while(bytesRead != -1);
    	is.close();
    	os.flush();
    	os.close();
    }

	private void forwardStreamToSize(final InputStream is, final OutputStream os, int size) throws IOException {
    	final byte [] buffer = new byte [4096];
    	int bytesRead;
    	int totalRead = size;
    	do{
    		bytesRead = is.read(buffer);
    		if(bytesRead > 0){
    		    os.write(buffer, 0, bytesRead);
    		    totalRead -= bytesRead;
    		}
    	}while(bytesRead != -1 && totalRead > 0);
    	is.close();
    	os.flush();
    	os.close();
    }
}
