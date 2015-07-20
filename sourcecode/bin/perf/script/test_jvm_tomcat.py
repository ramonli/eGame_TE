"""
Test the throughput of Tomcat running on different jvm(sun,ibm,jrocket).
"""

from net.grinder.script import Test
from net.grinder.plugin.http import HTTPRequest
#from HTTPClient import NVPair
from net.grinder.script.Grinder import grinder
from net.grinder.common import Logger

# assemble Test object
jvm_test = Test(1, "Test tomcat based on different JVM.")
# assemble http proxy object
request = jvm_test.wrap(HTTPRequest())

# initialize a grinder log.
log = grinder.logger.output

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
    
        response = request.GET("http://localhost:8080/examples/")
        
        code = response.getStatusCode() 
        # verify response status 
        if 200 != code:
            # log("ERROR: %d" % code, Logger.TERMINAL)
            # Set success = 0 to mark the test as a failure.
            grinder.statistics.forLastTest.setSuccess(0)   
        else:
            # Write log to terminal
            #log("PASS!", Logger.TERMINAL)
            pass


