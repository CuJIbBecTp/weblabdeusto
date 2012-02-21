#!/usr/bin/env python
#-*-*- encoding: utf-8 -*-*-
#
# Copyright (C) 2005 onwards University of Deusto
# All rights reserved.
#
# This software is licensed as described in the file COPYING, which
# you should have received as part of this distribution.
#
# This software consists of contributions made by many individuals,
# listed below:
#
# Author: Pablo Orduña <pablo@ordunya.com>
#

import weblab.core.exc as coreExc

class CoordinatorException(coreExc.WebLabCoreException):
    pass

class ExperimentNotFoundException(CoordinatorException):
    pass

class ExpiredSessionException(CoordinatorException):
    pass

class InvalidExperimentConfigException(CoordinatorException):
    pass

class UnregisteredSchedulingSystemException(CoordinatorException):
    pass

class NoSchedulerFoundException(CoordinatorException):
    pass

