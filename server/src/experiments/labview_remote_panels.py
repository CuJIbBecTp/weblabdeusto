#!/usr/bin/python
# -*- coding: utf-8 -*-
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
# Author: Pablo Orduña <pablo.orduna@deusto.es>
#
import base64
import socket
import time

import random
import traceback
import urllib2
import json
import threading
import weakref
from voodoo import log

from voodoo.lock import locked
from voodoo.log import logged
from voodoo.override import Override
from weblab.experiment.experiment import Experiment
import weblab.core.coordinator.coordinator as Coordinator

DEFAULT_DEBUG_MESSAGE = True
DEFAULT_DEBUG_COMMAND = True

class LabviewRemotePanels(Experiment):
    def __init__(self, coord_address, locator, config, *args, **kwargs):
        super(LabviewRemotePanels, self).__init__(*args, **kwargs)

        self.host = config.get('labview_host', 'localhost')
        self.port = config.get('labview_port', 20000)
        self.shared_secret = config.get('labview_shared_secret', "12345@&")
        self.debug_message = config.get('labview_debug_message', DEFAULT_DEBUG_MESSAGE)
        self.debug_command = config.get('labview_debug_command', DEFAULT_DEBUG_COMMAND)

        if self.debug_message or self.debug_command:
            print("LabVIEW Configuration: {}:{}  key: {}".format(self.host, self.port, self.shared_secret))

    def _dbg_message(self, msg):
        if self.debug_message:
            print(msg)

    def _dbg_command(self, msg):
        if if self.debug_command:
            print(msg)

    def _send_message(self, message):
        message = message + '\r\n'
        self._dbg_message("Creating socket")
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self._dbg_message("Connecting...")
        s.connect((self.host, self.port))
        self._dbg_message("Connected. Sending message: %s" % message)
        s.send(message)
        self._dbg_message("Message sent")
        self._dbg_message("Waiting for response")
        response = s.recv(1024)
        self._dbg_message("Response received: %r" % response)
        self._dbg_message("Closing socket")
        s.close()
        self._dbg_message("Socket closed")
        return response

    @Override(Experiment)
    @logged("info")
    def do_start_experiment(self, client_initial_data, server_initial_data):
        raw_back_url = json.loads(serialized_client_initial_data).get('back','')
        back_url = urllib2.quote(raw_back_url,'')

        data = json.loads(server_initial_data)
        username = data['request.username']
        time_slot = int(server_data['priority.queue.slot.length'])

        self.current_client_key = str(random.random())[2:7]
        message = '@@@'.join(('start', self.shared_secret, self.current_client_key, username, back_url, str(time_slot)))
        json_response = self._send_message(message)
        response = json.loads(json_response)

        current_config = {
            'url' : response['url'],
        }

        return json.dumps({"initial_configuration": json.dumps(current_config), "batch": False})

    @Override(Experiment.Experiment)
    def do_should_finish(self):
        message = '@@@'.join(('status', WEBLAB_SECRET))

        json_response = _send_message(message)
        if not json_response:
            return 10
        try:
            response = int(json_response)
        except ValueError:
            return 10
        else:
            return response

    @Override(Experiment.Experiment)
    def do_dispose(self):
        message = '@@@'.join(('end', WEBLAB_SECRET))
        _send_message(message)
        return 'ok'

