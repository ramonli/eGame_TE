package com.mpos.lottery.te.common.util;

import org.perf4j.StopWatch;
import org.perf4j.commonslog.CommonsLogStopWatch;

/**
 * Refer to http://perf4j.codehaus.org/devguide.html
 */
public class Perf4jTestMain {

    public static void main(String args[]) throws Exception {
        for (int i = 0; i < 1000; i++) {
            StopWatch stopWatch = new CommonsLogStopWatch();

            try {
                // the code block being timed - this is just a dummy example
                long sleepTime = (long) (Math.random() * 1000L);
                Thread.sleep(sleepTime);
                if (sleepTime > 500L) {
                    throw new Exception("Throwing exception");
                }

                stopWatch.stop("codeBlock2.success", "Sleep time was < 500 ms");
            } catch (Exception e) {
                stopWatch.stop("codeBlock2.failure", "Exception was: " + e);
            }
        }
    }
}
