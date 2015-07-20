from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest
from HTTPClient import NVPair
from net.grinder.script.Grinder import grinder

from java.lang import String

import te_message;
from te_conf import *

# assemble Test object
bet_test = Test(1, "bet:Commit bet request.")
# assemble http proxy object
request = bet_test.wrap(HTTPRequest())

# initialize a grinder log.
log = grinder.logger.output
out = grinder.logger.TERMINAL

# define the message body of request
request_msgbody= \
"""
<Ticket multipleDraws="1" totalAmount="20" PIN="123456-a">
    <GameDraw number="20090724" gameId="GAME-113"/>
    <Entry selectedNumber="2,3,5,6,13,1" betOption="1" isQuickPick="0"/>
    <Entry selectedNumber="2,3,5,6,13,7" betOption="1" isQuickPick="0"/>
</Ticket>
"""  

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
        
        msg = te_message.Message("1.0", "20111020153610", 200, None, "GPE-111", 
                                 111, "OPERATOR-111", "00000000001", None, request_msgbody)
        #print("******hello" + str(type(msg)))
        #print("msg.raw_body" + msg.raw_body)
        #print("des:" + msg.encrypted_body)
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



