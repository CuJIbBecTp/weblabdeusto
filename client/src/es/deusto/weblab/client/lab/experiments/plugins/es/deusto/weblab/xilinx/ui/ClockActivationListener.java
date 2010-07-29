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
package es.deusto.weblab.client.lab.experiments.plugins.es.deusto.weblab.xilinx.ui;

import es.deusto.weblab.client.dto.experiments.Command;
import es.deusto.weblab.client.lab.comm.callbacks.IResponseCommandCallback;
import es.deusto.weblab.client.lab.experiments.plugins.es.deusto.weblab.xilinx.commands.ClockActivationCommand;
import es.deusto.weblab.client.lab.experiments.plugins.es.deusto.weblab.xilinx.commands.ClockDeactivationCommand;
import es.deusto.weblab.client.lab.ui.BoardBase.IBoardBaseController;
import es.deusto.weblab.client.ui.widgets.WlClockActivator.IWlClockActivationListener;

class ClockActivationListener implements IWlClockActivationListener{
	private final IBoardBaseController commandSender;
	private final IResponseCommandCallback commandCallback;
	
	public ClockActivationListener(IBoardBaseController commandSender, IResponseCommandCallback commandCallback){
		this.commandSender = commandSender;
		this.commandCallback = commandCallback;
	}

	@Override
	public void onActivate(int value) {
		final Command command = new ClockActivationCommand(value);
		this.commandSender.sendCommand(command, this.commandCallback);
	}

	@Override
	public void onDeactivate() {
		final Command command = new ClockDeactivationCommand();
		this.commandSender.sendCommand(command, this.commandCallback);
	}
}
