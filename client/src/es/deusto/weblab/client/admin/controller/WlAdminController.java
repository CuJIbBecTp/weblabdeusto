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
*         Jaime Irurzun <jaime.irurzun@gmail.com>
*
*/

package es.deusto.weblab.client.admin.controller;

import java.util.ArrayList;
import java.util.Date;

import es.deusto.weblab.client.admin.comm.IWlAdminCommunication;
import es.deusto.weblab.client.admin.comm.callbacks.IExperimentUsesCallback;
import es.deusto.weblab.client.admin.comm.callbacks.IExperimentsCallback;
import es.deusto.weblab.client.admin.comm.callbacks.IGroupsCallback;
import es.deusto.weblab.client.admin.comm.callbacks.IUsersCallback;
import es.deusto.weblab.client.admin.ui.IUIManager;
import es.deusto.weblab.client.comm.callbacks.ISessionIdCallback;
import es.deusto.weblab.client.comm.callbacks.IUserInformationCallback;
import es.deusto.weblab.client.comm.callbacks.IVoidCallback;
import es.deusto.weblab.client.comm.exceptions.WlCommException;
import es.deusto.weblab.client.comm.exceptions.login.LoginException;
import es.deusto.weblab.client.configuration.IConfigurationManager;
import es.deusto.weblab.client.dto.SessionID;
import es.deusto.weblab.client.dto.experiments.Experiment;
import es.deusto.weblab.client.dto.experiments.ExperimentUse;
import es.deusto.weblab.client.dto.users.Group;
import es.deusto.weblab.client.dto.users.User;

public class WlAdminController implements IWlAdminController {

	private IConfigurationManager configurationManager;
	private IWlAdminCommunication communications;
	private IUIManager uimanager;
	private SessionID currentSession;
	
	public WlAdminController(IConfigurationManager configurationManager, IWlAdminCommunication communications) {
		this.configurationManager = configurationManager;
		this.communications = communications;
	}

	@Override
	public void setUIManager(IUIManager uimanager) {
		this.uimanager = uimanager;
	}

	@Override
	public void login(String username, String password) {
		this.communications.login(username, password, new ISessionIdCallback(){
			public void onSuccess(SessionID sessionId) {
				WlAdminController.this.startSession(sessionId);
			}

			public void onFailure(WlCommException e) {
				if(e instanceof LoginException){
					WlAdminController.this.uimanager.onWrongLoginOrPasswordGiven();
				}else{
					WlAdminController.this.uimanager.onErrorAndFinishSession(e.getMessage());
				}
			}
		});
	}

	@Override
	public void startLoggedIn(SessionID sessionID) {
		this.startSession(sessionID);
	}

	@Override
	public void logout() {
		this.communications.logout(this.currentSession, new IVoidCallback(){
			public void onSuccess() {
				WlAdminController.this.uimanager.onLoggedOut();
			}
			
			public void onFailure(WlCommException e) {
				WlAdminController.this.uimanager.onErrorAndFinishSession(e.getMessage());
			}
		});
	}

	private void startSession(SessionID sessionID) {
		this.currentSession = sessionID;
		
		this.communications.getUserInformation(this.currentSession, new IUserInformationCallback(){
			public void onSuccess(final User userInformation) {
				WlAdminController.this.uimanager.onLoggedIn(userInformation);
			}
			
			public void onFailure(WlCommException e) {
				WlAdminController.this.uimanager.onError(e.getMessage());
			}
		});			
	}
	
	@Override
	public void getUsers() {
		this.communications.getUsers(this.currentSession, new IUsersCallback(){
			public void onSuccess(final ArrayList<User> users) {
				WlAdminController.this.uimanager.onUsersRetrieved(users);
			}
			
			public void onFailure(WlCommException e) {
				WlAdminController.this.uimanager.onError(e.getMessage());
			}
		});
	}

	@Override
	public void getExperiments() {
		this.communications.getExperiments(this.currentSession, new IExperimentsCallback(){
			public void onSuccess(final ArrayList<Experiment> experiments) {
				WlAdminController.this.uimanager.onExperimentsRetrieved(experiments);
			}
			
			public void onFailure(WlCommException e) {
				WlAdminController.this.uimanager.onError(e.getMessage());
			}
		});
	}

	@Override
	public void getGroups() {
		this.communications.getGroups(this.currentSession, new IGroupsCallback(){
			public void onSuccess(final ArrayList<Group> groups) {
				WlAdminController.this.uimanager.onGroupsRetrieved(groups);
			}
			
			public void onFailure(WlCommException e) {
				WlAdminController.this.uimanager.onError(e.getMessage());
			}
		});
	}

	@Override
	public void getExperimentUses(Date fromDate, Date toDate, Group group, Experiment experiment) {

		IExperimentUsesCallback callback = new IExperimentUsesCallback() {
			public void onSuccess(final ArrayList<ExperimentUse> experimentUses) {
				WlAdminController.this.uimanager.onExperimentUsesRetrieved(experimentUses);
			}
			
			public void onFailure(WlCommException e) {
				WlAdminController.this.uimanager.onError(e.getMessage());
			}
		};
		
		this.communications.getExperimentUses(
				this.currentSession,
				fromDate,
				toDate,
				group != null ? group.getId(): -1,
				experiment != null ? experiment.getId(): -1,
				callback
		);		
		/*
		ArrayList<ExperimentUse> experimentUses = new ArrayList<ExperimentUse>();
		
		for ( ExperimentUse eu: this.temporalFakeData.allExperimentUses ) {
			boolean valid = true;
			
			if ( fromDate != null && toDate != null ) {
				if ( ! ( eu.getStartTimestamp().after(fromDate) && eu.getStartTimestamp().before(toDate) ) ) {
					valid = false;
				}
			} else if ( fromDate == null && toDate != null ) {
				if ( ! eu.getStartTimestamp().before(toDate) ) {
					valid = false;
				}
			} else if ( toDate == null && fromDate != null ) {
				if ( ! eu.getStartTimestamp().after(fromDate) ) {
					valid = false;
				}
			}
			
			if ( group != null ) {
				if ( ! eu.getUser().isMemberOf(group) ) {
					valid = false;
				}
			}
			
			if ( experiment != null ) {
				if ( ! eu.getExperiment().equals(experiment) ) {
					valid = false;
				}
			}
			
			if ( valid ) {
				experimentUses.add(eu);
			}
		}
		
		Collections.sort(experimentUses, new Comparator<ExperimentUse>() {
			@Override
			public int compare(ExperimentUse o1, ExperimentUse o2) {
				return o2.getStartTimestamp().compareTo(o1.getStartTimestamp());
			}
		});
		
		return experimentUses;
		*/
	}
}