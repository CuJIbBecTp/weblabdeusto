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

package es.deusto.weblab.client.lab.experiments.exceptions;

public class ExperimentCreatorInstanciationException extends WlExperimentException {
	private static final long serialVersionUID = -7058850598596749766L;

	public ExperimentCreatorInstanciationException() {}

	public ExperimentCreatorInstanciationException(String arg0) {
		super(arg0);
	}

	public ExperimentCreatorInstanciationException(Throwable arg0) {
		super(arg0);
	}

	public ExperimentCreatorInstanciationException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
