# -*- coding: UTF-8 -*-

__author__="ramonli"
__date__ ="$Dec 11, 2008 4:31:16 PM$"

"""
I think use grinder to do acceptance/integration test is a good idea. Let's use mlotter.te as 
a example. TE exposes some http services to client(POS), after finishing a server, I will write
a integration test to test the server(remote invocation), which is implemented by java. And then
I also need to do performance test on some critical services, It means I must re-implement the 
test code in python(Let's say, performance test is just invoking integratin test many times :)).

It is troublesome! So I think why am i implementing the integration in python directly. Grinder
is also a good test platform, it also can generate test report. 
"""

# Running multiple scripts sequentially??
# There is a sample script from the grinder website here:
# http://grinder.sourceforge.net/g3/script-gallery.html#sequence

# TreeMap is the simplest way to sort a Java map.
# scripts = grinder.properties.getProperty("grinder.mix.scripts")
# if scripts: print("FAIL!!!!!")

class TestRunner:
    def __init__(self):
        # add sequence test scripts here, MUST gurantee that each test has a unique test number.
        self.testRunners = [
            lottosale_test.TestRunner(),
         ]

    def __call__(self):
        # This method is called for every run.
        for testRunner in self.testRunners: testRunner()

if "__name__" == "__main__":
    pass
