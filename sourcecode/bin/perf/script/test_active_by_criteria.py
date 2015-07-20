from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest
from HTTPClient import NVPair
from net.grinder.script.Grinder import grinder

from java.lang import String

import te_message;
from te_conf import *
from te_testdata import *

# assemble Test object
bet_test = Test(2, "active: active multiple books.")
# assemble http proxy object
request = bet_test.wrap(HTTPRequest())

# initialize a grinder log.
log = grinder.logger.output
out = grinder.logger.TERMINAL

request_msgbody = activebycriteria_bylastticket()
#request_msgbody = activebycriteria_byfirstticket()
#request_msgbody = activebycriteria_byrangeticket()
#request_msgbody = activebycriteria_bybatchbook()
#request_msgbody = activebycriteria_bybatchrange()

#--------------------------------------------------#
# Grinder specified test runner                    #
#--------------------------------------------------#
class TestRunner:
    def __init__(self):
        pass

    def __call__(self):
        """
        A Python object is callable if it defines a __call__ method. Each worker thread performs a
        number of runs of the test script, as configured by the property grinder.runs. For each run,
        the worker thread calls its TestRunner; thus the __call__ method can be thought of as the
        definition of a run.
        """
        
        # Normally test results are reported automatically when the test returns. If you want to 
        # alter the statistics after a test has completed, you must set delayReports = 1 to delay 
        # the reporting before performing the test. This only affects the current worker thread.
        grinder.statistics.delayReports = 1
        
        msg = te_message.Message("1.0", "20090927113634", 401, None, "GPE-111", 
                                 111, "OPERATOR-111", "00000000001", None, request_msgbody)
        print("******hello" + str(type(msg)))
        print("msg.raw_body" + msg.raw_body)
        print("des:" + msg.encrypted_body)
        body = String(msg.encrypted_body)
        response = request.POST(CONF_url, body.getBytes(), msg.assemble_nvpairs())
        
        code = int(response.getHeader("X-Response-Code"))
        # verify response status 
        if 200 != code:
            print("------------------------------------ERROR: %d" % code)
            # Set success = 0 to mark the test as a failure.
            grinder.statistics.forLastTest.setSuccess(0)   
        else:
            print("------------------------------------PASS!")


