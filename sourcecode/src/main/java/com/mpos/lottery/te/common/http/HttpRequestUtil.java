package com.mpos.lottery.te.common.http;

import com.mpos.lottery.te.common.util.HexCoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public class HttpRequestUtil {
    private static Log logger = LogFactory.getLog(HttpRequestUtil.class);
    public static final String ATT_MESSAGE_BODY = "http.message.body";

    /**
     * Constructor.
     */
    public static String getContentAsString(HttpServletRequest request) throws IOException {
        // check if the message body has been parsed
        String content = (String) request.getAttribute(ATT_MESSAGE_BODY);
        if (content == null) { // retrieve content from message body
            StringBuffer buffer = new StringBuffer();
            BufferedReader br = request.getReader();
            if (br == null) {
                return null;
            }
            for (String tmp = br.readLine(); tmp != null;) {
                buffer.append(tmp);
                tmp = br.readLine();
            }
            content = buffer.toString();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieve Message Body: " + content);
        }

        return content;
    }

    public static byte[] getContentAsByte(HttpServletRequest request) throws IOException {
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(request.getInputStream());
        int size = 0;
        while ((size = bis.read(buffer)) != -1) {
            baos.write(buffer, 0, size);
        }
        byte[] contentBytes = baos.toByteArray();
        if (logger.isDebugEnabled()) {
            logger.debug("The content of request:" + HexCoder.bufferToHex(contentBytes));
        }

        bis.close();
        baos.close();
        return contentBytes;
    }
}
