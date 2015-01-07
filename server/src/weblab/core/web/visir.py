#!/usr/bin/env python
#-*-*- encoding: utf-8 -*-*-
#
# Copyright (C) 2005-2009 University of Deusto
# All rights reserved.
#
# This software is licensed as described in the file COPYING, which
# you should have received as part of this distribution.
#
# This software consists of contributions made by many individuals,
# listed below:
#
# Author: Pablo Orduña <pablo@ordunya.com>
#         Luis Rodriguez <luis.rodriguez@opendeusto.es>
#

import StringIO
from flask import Response, make_response, request
from weblab.core.web import weblab_api, get_argument
from weblab.util import data_filename
import tempfile
import traceback
from weblab.data.command import Command
# To convert from HTTP date to standard time
import email.utils as eut
import time

import os
import mimetypes

import hashlib

VISIR_RELATIVE_PATH = data_filename(os.path.join('war','weblabclientlab','visir')) + os.sep

VISIR_LOCATION = VISIR_RELATIVE_PATH
VISIR_TEMP_FILES = os.sep.join((VISIR_LOCATION, 'temp')) + os.sep

BASE_HTML_TEMPLATE="""<?xml version="1.0"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <title>WebLab visir</title>
    </head>
    <body>%(MESSAGE)s</body>
</html>
"""

SUCCESS_HTML_TEMPLATE = BASE_HTML_TEMPLATE % {
		"MESSAGE" : "SUCCESS@%(RESULT)s"
	}

FAULT_HTML_TEMPLATE = BASE_HTML_TEMPLATE % {
		"MESSAGE" : "ERROR@%(THE_FAULT_CODE)s@%(THE_FAULT_MESSAGE)s"
	}

DEBUG = False
MIMETYPES_LOADED = False

@weblab_api.route_web('/visir/<path:fileonly>', methods = ['GET', 'POST'])
def visir(fileonly = None):
    """
    This will redirect every request to serve the VISIR files.
    """

    # Just deny any request with an URL containing .. to prevent security issues
    if ".." in fileonly:
        if DEBUG: print "Forbidden"
        return Response("403 Forbidden: The URI should not contain ..", 403)

    # Find out the location of the file.
    fname = VISIR_LOCATION + fileonly

    if DEBUG: print "Loading %s..." % fname,

    if not os.path.abspath(fname).startswith(VISIR_LOCATION):
        if DEBUG: print "Forbidden"
        return Response("403 Forbidden: The URI tried to go outside the scope of VISIR", 403)

    # Intercept the save request
    if fileonly == 'save':
        content = intercept_save()
        if DEBUG: print "Intercepted %s" % fileonly
        return content

    if fileonly == 'store_temporary.php':
        content = intercept_store()
        if DEBUG: print "Intercepted %s" % fileonly
        return content

    if fileonly.startswith('temp/'):
        content = intercept_temp(fileonly[len('temp/'):])
        if DEBUG: print "Intercepted %s" % fileonly
        return content

    # We did not intercept the request, we will just serve the file.

    # We will need to report the Last-Modified date. Otherwise the browser
    # won't send if-modified-since.
    # Getmtime returns a localtime, so we also convert it to gmt. Also, we want
    # a timestamp and not a tuple.
    response = make_response()
    if os.path.exists(fname):
        mod_time = time.mktime(time.gmtime(os.path.getmtime(fname)))
        if fileonly != 'breadboard/library.xml':
            response.headers['Last-Modified'] = time_to_http_date(mod_time)
        else:
            mod_time = None
    else:
        mod_time = None

    # Client already has a version of the file. Check whether
    # ours is newer.
    if_modified_since = request.headers.get('If-Modified-Since', None)
    if if_modified_since is not None:
        since_time = http_date_to_time(if_modified_since)

        # The file was not modified. Report as such.
        if mod_time is not None and mod_time <= since_time:
            if DEBUG: print "Not modified"
            return Response("304 Not Modified", 304)

    try:
        with open(fname, "rb") as f:
            content = f.read()
    except:
        if DEBUG: print "Not found"
        return Response("404 Not found", 404)
    
    global MIMETYPES_LOADED
    if not MIMETYPES_LOADED:
        mimetypes.init()
        MIMETYPES_LOADED = True

    # Use the file path to guess the mimetype
    mimetype = mimetypes.guess_type(fname)[0]
    if mimetype is None:
        mimetype = "application/octet-stream"

    response.content_type = mimetype

    if fileonly == "breadboard/library.xml":
        content = intercept_library(content, mimetype)
        if DEBUG: print "Intercepted %s; md5: %s" % (len(content), hashlib.new("md5", content).hexdigest())
        return content

    response.response = content
    if DEBUG: print "Returning %s bytes" % len(content)
    return response


def http_date_to_time(datestr, want_gmt = True):
    """
    http_date_to_time(datestr)
    Converts an HTTP date string to a localtime timestamp.

    @param datestr HTTP date string, generally GMT, which is specified in the
    string itself

    @param want_gmt If True (the default) then the timestamp returned will be GMT.
    Otherwise, it will be localtime.

    @return Timestamp which corresponds to the specified date. It will be the GMT
    timestamp if want_gmt is set to true (the default), false otherwise.
    """
    t = time.mktime(eut.parsedate(datestr))
    if want_gmt: return time.gmtime(t)
    else: return t

def time_to_http_date(tm):
    """
    time_to_http_date(tm)
    Converts a timestamp to an http date string.
    @param tm The timestamp to convert to an http date. The timestamp should
    represent the GMT time.
    """
    return eut.formatdate(timeval = tm, localtime = False, usegmt = True)

def intercept_save():
    save = get_argument("save", "")
    response = make_response(save)
    response.content_type = 'application/download'
    response.headers['Content-Disposition'] = 'attachment; filename=circuit.cir'
    return response

def intercept_library(content, mimetype):
    session_id = request.cookies.get('weblabsessionid')
    if session_id:
        weblab_api.ctx.session_id = session_id

    reservation_id = request.cookies.get('weblab_reservation_id')
    if reservation_id:
        weblab_api.ctx.reservation_id = reservation_id

    try:
        response = weblab_api.api.send_command(Command("GIVE_ME_LIBRARY"))
    except:
        pass
    else:
        if response.commandstring is not None and response.commandstring != 'failed':
            return response.commandstring

    if reservation_id is None and session_id is not None:
        try:
            reservation_id_str = weblab_api.api.get_reservation_id_by_session_id()
            if reservation_id_str is not None:
                weblab_api.ctx.reservation_id = reservation_id_str
        except:
            traceback.print_exc()

    if reservation_id is not None:
        try:
            response = weblab_api.api.send_command( Command("GIVE_ME_LIBRARY") )
        except:
            failed = True
            traceback.print_exc()
        else:
            failed = response.commandstring is None or response.commandstring == 'failed'
    else:
        print "Can not request library since reservation_id is None"
        failed = True

    if failed:
        return content
    else:
        return response.commandstring

def intercept_store():
    # filename = request.files.get('Filename')
    filedata_fh = request.files.get('Filedata')
    sio = StringIO.StringIO()
    filedata_fh.save(sio)
    filedata = sio.getvalue()

    if not os.path.exists(VISIR_TEMP_FILES):
        os.makedirs(VISIR_TEMP_FILES)

    fd, name = tempfile.mkstemp(suffix='.cir.tmp', prefix='weblab_visir_', dir=VISIR_TEMP_FILES)
    os.close(fd)

    with file(name, "wb") as fo:
        fo.write(filedata)

    response = make_response("<result><filename>%s</filename></result>" % os.path.basename(name))
    response.content_type = 'text/xml'
    return response

def intercept_temp(fileonly):
    # Avoid weird characters, .., etc.
    filename = os.path.basename(fileonly)
    full_filename = os.sep.join((VISIR_TEMP_FILES, filename))
    if not os.path.exists(full_filename):
        if DEBUG: print "Not found"
        return make_response("404: Temporal file not found", 404)

    response = make_response(open(full_filename, 'rb').read())
    response.content_type = 'text/xml'
    return response


class VisirException(Exception):
    pass


## TODO: Make this a real test.
#f = file("c:/tmp/out.txt", "r")
#f.readline()
#data = f.read()
#boundary = """------------KM7gL6cH2KM7Ij5GI3ae0ei4ei4gL6"""
#u = UploadExtractor(data, boundary)
#
#print "DATA:  \n", u.extract_file()
#print "DATA:  \n", u.extract_file()
#print "DATA:  \n", u.extract_file()
#print "DATA:  \n", u.extract_file()

