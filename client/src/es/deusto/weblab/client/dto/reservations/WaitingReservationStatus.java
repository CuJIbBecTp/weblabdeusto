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
package es.deusto.weblab.client.dto.reservations;

public class WaitingReservationStatus extends ReservationStatus {
	private int position;
	
	public WaitingReservationStatus(){}
	
	public WaitingReservationStatus(int position){
		this.position = position;
	}

	public int getPosition() {
		return this.position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
	
}
