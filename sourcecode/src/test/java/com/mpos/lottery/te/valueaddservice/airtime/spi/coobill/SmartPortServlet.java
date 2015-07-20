package com.mpos.lottery.te.valueaddservice.airtime.spi.coobill;

import com.mpos.lottery.te.common.http.HttpRequestUtil;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is only port exposed by TE. All requests will received by <code>TEPortServlet</code>, and \ then the servlet
 * will dispatch request to <code>DispatchController</code>.
 */
public class SmartPortServlet extends HttpServlet {
    private static final long serialVersionUID = 7269637810411964297L;
    Logger logger = LoggerFactory.getLogger(SmartPortServlet.class);
    private int queryIndex = 1;

    /**
     * init servlet.
     */
    public void init(ServletConfig config) throws ServletException {
    }

    /**
     * post method implementation.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info(request.getRequestURL().toString());

        byte[] bodyBytes = HttpRequestUtil.getContentAsByte(request);
        String reqXml = new String(bodyBytes, Charset.forName("UTF-8"));
        logger.info("[Request XML]" + reqXml);
        Document doc = this.buildDoc(reqXml);

        String msisdn = this.lookupParameter(doc,
                "//soapenv:Envelope/soapenv:Body/com:SDF_Data/com:parameters/com:parameter", "MSISDN");
        if ("13800138000".equals(msisdn)) {
            logger.info("Successful topup transaction");
            this.topupOk(request, response, doc);
        } else if ("13800138999".equals(msisdn)) {
            logger.info("Query transaction[queryIndex=" + queryIndex + "].");
            this.topupFail(request, response, doc);
        } else {
            throw new ServletException("Unsupported MSISDN.");
        }
    }

    private void topupFail(HttpServletRequest request, HttpServletResponse response, Document doc) throws IOException {
        Document respDoc = this.buildResponse(doc, 1, 1, "Duplicated Transaction 000001", "00001");

        response.getWriter().println(respDoc.asXML());
        response.getWriter().close();
    }

    private void topupOk(HttpServletRequest request, HttpServletResponse response, Document doc) throws IOException {
        Document respDoc = this.buildResponse(doc, 0, 0, "Successful Transaction 0508155235053561", "0508155235053561");

        response.getWriter().println(respDoc.asXML());
        response.getWriter().close();
    }

    protected Document buildResponse(Document doc, int statusCode, int errorCode, String descMsg, String instanceId) {
        XPath xpath = doc.createXPath("//soapenv:Envelope/soapenv:Body/com:SDF_Data");
        Node sdfNode = xpath.selectSingleNode(doc);
        Element resultEle = ((Element) sdfNode).addElement("com:result");
        resultEle.addElement("com:statusCode").setText(statusCode + "");
        resultEle.addElement("com:errorCode").setText(errorCode + "");
        resultEle.addElement("com:errorDescription").setText(descMsg);
        resultEle.addElement("com:instanceId").setText(instanceId);
        return doc;
    }

    protected Document buildDoc(String xml) throws IOException {
        try {
            SAXReader reader = new SAXReader();
            return reader.read(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    protected String lookupParameter(Document doc, String xpathStr, String parameter) {
        XPath xpath = doc.createXPath(xpathStr);
        // Map<String, String> nsb = new HashMap<String, String>();
        // nsb.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        // nsb.put("ns1", "http://tempuri.org/");
        // xpath.setNamespaceURIs(nsb);
        List<Node> nodes = xpath.selectNodes(doc);
        for (Node node : nodes) {
            String paraName = ((Element) node).attributeValue("name");
            String paraValue = node.getText();
            if (paraName.equals(parameter)) {
                return paraValue;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        String reqXml = "";
        reqXml += "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://sdf.cellc.net/commonDataModel\">";
        reqXml += "   <soapenv:Header/>";
        reqXml += "   <soapenv:Body>";
        reqXml += "      <com:SDF_Data>";
        reqXml += "         <com:header>";
        reqXml += "            <com:processTypeID>8798</com:processTypeID>";
        reqXml += "            <com:externalReference>23423523623423423</com:externalReference>";
        reqXml += "            <com:sourceID>33613195</com:sourceID>";
        reqXml += "            <com:username>CM6 Lottery</com:username>";
        reqXml += "            <com:password>Y202U01AcA==</com:password>";
        reqXml += "            <com:processFlag>1</com:processFlag>";
        reqXml += "         </com:header>";
        reqXml += "         <com:parameters name=\"\">";
        reqXml += "            <com:parameter name=\"RechargeType\">1010</com:parameter>";
        reqXml += "            <com:parameter name=\"MSISDN\">70204008</com:parameter>";
        reqXml += "            <com:parameter name=\"Amount\">1</com:parameter>";
        reqXml += "            <com:parameter name=\"Channel_ID\">35</com:parameter>";
        reqXml += "         </com:parameters>";
        reqXml += "      </com:SDF_Data>";
        reqXml += "   </soapenv:Body>";
        reqXml += "</soapenv:Envelope>";

        SmartPortServlet servlet = new SmartPortServlet();
        Document doc = servlet.buildDoc(reqXml);
        String msisdn = servlet.lookupParameter(doc,
                "//soapenv:Envelope/soapenv:Body/com:SDF_Data/com:parameters/com:parameter", "MSISDN");
        System.out.println(msisdn);

        servlet.buildResponse(doc, 0, 0, "hello", "world");
        System.out.println(doc.asXML());
    }
}
