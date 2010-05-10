/*
* Copyright (C) 2005-2009 University of Deusto
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
package es.deusto.weblab.client.lab.ui.widgets;

import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

public class WlWebcam extends HorizontalPanel implements IWlWidget{
	
	public static final String DEFAULT_IMAGE_URL = "http://pld.weblab.deusto.es/webcam/pld0/image.jpg";
	public static final int DEFAULT_REFRESH_TIME = 400;
	
	private final Image image;
	
	private final int time;
	private String url;
	private Timer timer;
	private boolean running;

	public WlWebcam(){
		this(WlWebcam.DEFAULT_REFRESH_TIME, WlWebcam.DEFAULT_IMAGE_URL);
	}
	
	public WlWebcam(int time, String url){
		
		this.setWidth("100%");
		this.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		this.time = time;
		this.url = url;
		
		this.image = new Image(this.getDifferentUrl());
		
		this.add(this.image);
	}
	
	public void start(){
		this.running = true;
		this.timer = new Timer(){
			@Override
			public void run(){
				WlWebcam.this.reload();
			}
		};
		this.reload();
	}
	
	public void dispose(){
		this.running = false;
		
		if(this.timer != null)
			this.timer.cancel();
		
		this.timer = null;
	}
	
	@Override
	public void setVisible(boolean visible){
		super.setVisible(visible);
		this.image.setVisible(visible);
	}
	
	public void reload(){
		if(this.running){
			this.image.setUrl(this.getDifferentUrl());
			this.image.addErrorHandler(new ErrorHandler() {
			    
			    @Override
			    public void onError(ErrorEvent event) {
				WlWebcam.this.timer.schedule(WlWebcam.this.time);
			    }
			});
			this.image.addLoadHandler(new LoadHandler(){
					@Override
					public void onLoad(LoadEvent event) {
						WlWebcam.this.timer.schedule(WlWebcam.this.time);
					}
				}
			);
		}
	}
	
	private String randomStuff(){
		return "?" + Random.nextInt();
	}
	
	private String getDifferentUrl(){
		return this.url + this.randomStuff();
	}

	public String getUrl() {
		return this.url;
	}

	/**
	 * Sets the URL to obtain the webcam image from and reloads the webcam
	 * so as to display that new image.
	 * @param url URL of the image.
	 */
	public void setUrl(String url) {
		this.url = url;
		this.reload();
	}
	
	@Override
	public Widget getWidget(){
		return this.image;
	}
}
