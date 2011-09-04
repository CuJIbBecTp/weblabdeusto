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

package es.deusto.weblab.client.admin.comm;

import es.deusto.weblab.client.admin.comm.callbacks.IPermissionsCallback;
import es.deusto.weblab.client.comm.FakeCommonCommunication;
import es.deusto.weblab.client.comm.ICommonSerializer;
import es.deusto.weblab.client.dto.SessionID;

public class FakeAdminCommunication extends FakeCommonCommunication implements IAdminCommunication {
	
	public static final String GET_USER_PERMISSIONS = "FakeWebAdminCommunication::getUserPermissions";
	
	@Override
	protected ICommonSerializer createSerializer() {
		return new FakeAdminSerializer();
	}

	@Override
	public void getUserPermissions(SessionID sessionId, IPermissionsCallback callback) {
		this.append(FakeAdminCommunication.GET_USER_PERMISSIONS, new Object[]{
				sessionId,
				callback
		});
	}
}
