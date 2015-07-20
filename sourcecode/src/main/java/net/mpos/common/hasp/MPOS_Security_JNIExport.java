/**
 * All right reserves by MPost.net
 */
package net.mpos.common.hasp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Supports HASP running mode.
 */
public class MPOS_Security_JNIExport {
    private static Log logger = LogFactory.getLog(MPOS_Security_JNIExport.class);
    // "-DHASP" must be removed, otherwise bay guy can simply set "-DHASP=off" to disable HASP
    // checking.
    // /**
    // * You can specify "-DHASP=on" to enable HASP mode, "-DHASP=off" to disable
    // * HASP mode accordingly.
    // */
    // private static final String SYS_PROP_HASP = "HASP";
    public static boolean IS_TEST = true;

    public native byte[] Decrypt(byte[] pOriginalClass, int nMode);

    private static MPOS_Security_JNIExport mposJNIExport = new MPOS_Security_JNIExport();

    public static boolean isHaspEnabled() {
        // String hasp = System.getProperty(SYS_PROP_HASP);
        // if (hasp != null) {
        // if (hasp.equalsIgnoreCase("on"))
        // return true;
        // else if (hasp.equalsIgnoreCase("off"))
        // return false;
        // else
        // logger.info("Unrecognized HASP properties: " + hasp
        // + ", will be ignored(only on|off allowed).");
        // }
        return !IS_TEST;
    }

    public static byte[] decryptBinary(byte[] originalByte) {
        if (mposJNIExport == null) {
            mposJNIExport = new MPOS_Security_JNIExport();
        }
        return mposJNIExport.Decrypt(originalByte, 2);
    }

    static {
        if (isHaspEnabled())
            System.loadLibrary("MPOSSecurity112292x64");
    }
}
