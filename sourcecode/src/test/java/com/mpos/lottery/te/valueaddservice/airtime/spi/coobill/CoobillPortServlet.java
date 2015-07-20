package com.mpos.lottery.te.valueaddservice.airtime.spi.coobill;

import com.mpos.lottery.te.common.http.HttpRequestUtil;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill.CooBillAirtimeProvider;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is only port exposed by TE. All requests will received by <code>TEPortServlet</code>, and \ then the servlet
 * will dispatch request to <code>DispatchController</code>.
 */
public class CoobillPortServlet extends HttpServlet {
    private static final long serialVersionUID = 7269637810411964297L;
    Logger logger = LoggerFactory.getLogger(CoobillPortServlet.class);
    private int queryIndex = 1;

    /**
     * init servlet.
     */
    public void init(ServletConfig config) throws ServletException {
    }

    /**
     * get method implementation.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println(this.coobillWsdl());
        response.getWriter().close();
    }

    /**
     * post method implementation.
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info(request.getRequestURL().toString());

        byte[] bodyBytes = HttpRequestUtil.getContentAsByte(request);
        String reqXml = new String(bodyBytes, Charset.forName("UTF-8"));
        logger.info("[Request XML]" + reqXml);
        Document doc = buildDoc(reqXml);

        Node transTypeNode = null;
        if (this.lookupNode(doc, "//soap:Envelope/soap:Body/ns1:TopupMsisdn") != null) {
            logger.info("Topup transaction");
            this.topup(request, response, doc);
        } else if (this.lookupNode(doc, "//soap:Envelope/soap:Body/ns1:queryMsisdn") != null) {
            logger.info("Query transaction[queryIndex=" + queryIndex + "].");
            this.query(request, response, doc);
        } else {
            throw new ServletException("Unsupported transaction type.");
        }
    }

    protected void query(HttpServletRequest request, HttpServletResponse response, Document doc) throws IOException {
        // fail
        int status = 99;
        int flag = queryIndex % 2;
        if (flag == 1) {
            status = CooBillAirtimeProvider.COOBILL_QUERY_STATUS_PENDING;
        } else if (flag == 0) {
            // try {
            // logger.info("Sleep 35 seconds...");
            // // pending transaction...
            // Thread.sleep(35 * 1000L);
            // } catch (Exception e) {
            // throw new IOException(e);
            // }
            // comment below to make failed response
            status = CooBillAirtimeProvider.COOBILL_QUERY_STATUS_SUCCESS;
        }
        queryIndex++;

        String xml = "";
        xml += "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        xml += "   <soap:Body>";
        xml += "      <ns1:queryMsisdnResponse xmlns:ns1=\"http://tempuri.org/\">";
        xml += "         <return>";
        xml += "            <chargeSource>0</chargeSource>";
        xml += "            <cmdType>2</cmdType>";
        xml += "            <coinType>2</coinType>";
        xml += "            <operResult>0</operResult>";
        xml += "            <payDate>20150130131800</payDate>";
        xml += "            <payDoneCode>3020150130131800000000134841</payDoneCode>";
        xml += "            <payMoney>0.01</payMoney>";
        xml += "            <payStatus>" + status + "</payStatus>";
        xml += "            <payerSubsId>1644560226</payerSubsId>";
        xml += "            <receiverSubsId>1644561202</receiverSubsId>";
        xml += "            <requestDate>0</requestDate>";
        xml += "            <signature/>";
        xml += "            <subGoodsMoney>0.01</subGoodsMoney>";
        xml += "            <totalMoney>0.0</totalMoney>";
        xml += "            <transactionId>14225985390228460</transactionId>";
        xml += "         </return>";
        xml += "      </ns1:queryMsisdnResponse>";
        xml += "   </soap:Body>";
        xml += "</soap:Envelope>";

        logger.info("[Response XML]" + xml);
        response.getWriter().println(xml);
        response.getWriter().close();
    }

    protected void topup(HttpServletRequest request, HttpServletResponse response, Document doc) throws IOException {
        String mobileNo = this.lookupNode(doc, "//soap:Envelope/soap:Body/ns1:TopupMsisdn/msisdn").getText();

        String respXml = "Timeout";
        if ("111".equalsIgnoreCase(mobileNo)) {
            // successful transaction
            respXml = topupResponse(CooBillAirtimeProvider.COOBILL_TOPOUP_STATUS_OK);
        } else if ("222".equalsIgnoreCase(mobileNo)) {
            try {
                logger.info("Sleep 35 seconds...");
                // pending transaction...
                Thread.sleep(35 * 1000L);
            } catch (Exception e) {
                throw new IOException(e);
            }
        } else if ("333".equalsIgnoreCase(mobileNo)) {
            // failed transaction
            respXml = topupResponse(CooBillAirtimeProvider.COOBILL_TOPOUP_STATUS_OK + 1);
        }
        logger.info("[Response XML]" + respXml);

        response.getWriter().println(respXml);
        response.getWriter().close();
    }

    private String topupResponse(int status) {
        String message = (CooBillAirtimeProvider.COOBILL_TOPOUP_STATUS_OK) == status ? "OK" : "Failuer";
        String transDate = SimpleToolkit.formatDate(new Date());
        String transId = "123456";
        String respXml = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        respXml += "   <soap:Body>";
        respXml += "      <ns1:TopupMsisdnResponse xmlns:ns1=\"http://tempuri.org/\">";
        respXml += "         <return>";
        respXml += "            <description>" + message + "</description>";
        respXml += "            <status>" + status + "</status>";
        respXml += "            <transactionDate>" + transDate + "</transactionDate>";
        respXml += "            <transactionId>" + transId + "</transactionId>";
        respXml += "         </return>";
        respXml += "      </ns1:TopupMsisdnResponse>";
        respXml += "   </soap:Body>";
        respXml += "</soap:Envelope>";
        return respXml;
    }

    protected Node lookupNode(Document doc, String xpathStr) {
        XPath xpath = doc.createXPath(xpathStr);
        Map<String, String> nsb = new HashMap<String, String>();
        nsb.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsb.put("ns1", "http://tempuri.org/");
        xpath.setNamespaceURIs(nsb);
        return xpath.selectSingleNode(doc);
    }

    protected Document buildDoc(String xml) throws IOException {
        try {
            SAXReader reader = new SAXReader();
            return reader.read(new ByteArrayInputStream(xml.getBytes()));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    protected String coobillWsdl() throws IOException {
        MLotteryContext mockContext = MLotteryContext.getInstance();

        String wsdl = "";
        wsdl += "<wsdl:definitions name=\"RechargeMsisdnService\" targetNamespace=\"http://tempuri.org/\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:tns=\"http://tempuri.org/\" xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";
        wsdl += "   <wsdl:types>";
        wsdl += "      <xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"unqualified\" targetNamespace=\"http://tempuri.org/\" xmlns=\"http://tempuri.org/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">";
        wsdl += "         <xs:complexType name=\"orderPaymentItem\">";
        wsdl += "            <xs:sequence>";
        wsdl += "               <xs:element name=\"chargeSource\" type=\"xs:int\"/>";
        wsdl += "               <xs:element name=\"cmdType\" type=\"xs:int\"/>";
        wsdl += "               <xs:element name=\"coinType\" type=\"xs:int\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"cooMallID\" type=\"xs:string\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"id\" type=\"xs:string\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"operMsg\" type=\"xs:string\"/>";
        wsdl += "               <xs:element name=\"operResult\" type=\"xs:int\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"orderId\" type=\"xs:string\"/>";
        wsdl += "               <xs:element name=\"payDate\" type=\"xs:long\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"payDoneCode\" type=\"xs:string\"/>";
        wsdl += "               <xs:element name=\"payMoney\" type=\"xs:double\"/>";
        wsdl += "               <xs:element name=\"payStatus\" type=\"xs:int\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"payStatusMessage\" type=\"xs:string\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"payerSubsId\" type=\"xs:string\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"receiverSubsId\" type=\"xs:string\"/>";
        wsdl += "               <xs:element name=\"requestDate\" type=\"xs:long\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"signature\" type=\"xs:base64Binary\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"subGoodsId\" type=\"xs:string\"/>";
        wsdl += "               <xs:element name=\"subGoodsMoney\" type=\"xs:double\"/>";
        wsdl += "               <xs:element name=\"totalMoney\" type=\"xs:double\"/>";
        wsdl += "               <xs:element name=\"transactionId\" type=\"xs:long\"/>";
        wsdl += "            </xs:sequence>";
        wsdl += "         </xs:complexType>";
        wsdl += "         <xs:complexType name=\"getMsisdnResult\">";
        wsdl += "            <xs:sequence>";
        wsdl += "               <xs:element name=\"status\" type=\"xs:int\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"statusMessage\" type=\"xs:string\"/>";
        wsdl += "               <xs:element name=\"type\" type=\"xs:int\"/>";
        wsdl += "            </xs:sequence>";
        wsdl += "         </xs:complexType>";
        wsdl += "         <xs:complexType name=\"topupMsisdnResult\">";
        wsdl += "            <xs:sequence>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"description\" type=\"xs:string\"/>";
        wsdl += "               <xs:element name=\"status\" type=\"xs:int\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"transactionDate\" type=\"xs:string\"/>";
        wsdl += "               <xs:element minOccurs=\"0\" name=\"transactionId\" type=\"xs:string\"/>";
        wsdl += "            </xs:sequence>";
        wsdl += "         </xs:complexType>";
        wsdl += "      </xs:schema>";
        wsdl += "   </wsdl:types>";
        wsdl += "   <wsdl:message name=\"queryMsisdn\">";
        wsdl += "      <wsdl:part name=\"telNo\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"reqTransid\" type=\"xsd:string\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:message name=\"TopupMsisdnResponse\">";
        wsdl += "      <wsdl:part name=\"return\" type=\"tns:topupMsisdnResult\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:message name=\"TopupMsisdn\">";
        wsdl += "      <wsdl:part name=\"agent\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"password\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"msisdn\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"amount\" type=\"xsd:int\"/>";
        wsdl += "      <wsdl:part name=\"refTrx\" type=\"xsd:string\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:message name=\"cancelMsisdn\">";
        wsdl += "      <wsdl:part name=\"cmdType\" type=\"xsd:int\"/>";
        wsdl += "      <wsdl:part name=\"transactionId\" type=\"xsd:long\"/>";
        wsdl += "      <wsdl:part name=\"payerSubsId\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"receiverSubsId\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"cooMallID\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"orderId\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"totalMoney\" type=\"xsd:double\"/>";
        wsdl += "      <wsdl:part name=\"coinType\" type=\"xsd:int\"/>";
        wsdl += "      <wsdl:part name=\"subGoodsId\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"subGoodsMoney\" type=\"xsd:double\"/>";
        wsdl += "      <wsdl:part name=\"requestDate\" type=\"xsd:long\"/>";
        wsdl += "      <wsdl:part name=\"payDoneCode\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"payMoney\" type=\"xsd:double\"/>";
        wsdl += "      <wsdl:part name=\"payStatus\" type=\"xsd:int\"/>";
        wsdl += "      <wsdl:part name=\"operResult\" type=\"xsd:int\"/>";
        wsdl += "      <wsdl:part name=\"signature\" type=\"xsd:base64Binary\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:message name=\"queryMsisdnResponse\">";
        wsdl += "      <wsdl:part name=\"return\" type=\"tns:orderPaymentItem\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:message name=\"GetMsisdn\">";
        wsdl += "      <wsdl:part name=\"agent\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"password\" type=\"xsd:string\"/>";
        wsdl += "      <wsdl:part name=\"msisdn\" type=\"xsd:string\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:message name=\"GetMsisdnResponse\">";
        wsdl += "      <wsdl:part name=\"return\" type=\"tns:getMsisdnResult\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:message name=\"cancelMsisdnResponse\">";
        wsdl += "      <wsdl:part name=\"return\" type=\"tns:orderPaymentItem\"/>";
        wsdl += "   </wsdl:message>";
        wsdl += "   <wsdl:portType name=\"RechargeMsisdn\">";
        wsdl += "      <wsdl:operation name=\"queryMsisdn\">";
        wsdl += "         <wsdl:input message=\"tns:queryMsisdn\" name=\"queryMsisdn\"/>";
        wsdl += "         <wsdl:output message=\"tns:queryMsisdnResponse\" name=\"queryMsisdnResponse\"/>";
        wsdl += "      </wsdl:operation>";
        wsdl += "      <wsdl:operation name=\"cancelMsisdn\">";
        wsdl += "         <wsdl:input message=\"tns:cancelMsisdn\" name=\"cancelMsisdn\"/>";
        wsdl += "         <wsdl:output message=\"tns:cancelMsisdnResponse\" name=\"cancelMsisdnResponse\"/>";
        wsdl += "      </wsdl:operation>";
        wsdl += "      <wsdl:operation name=\"GetMsisdn\">";
        wsdl += "         <wsdl:input message=\"tns:GetMsisdn\" name=\"GetMsisdn\"/>";
        wsdl += "         <wsdl:output message=\"tns:GetMsisdnResponse\" name=\"GetMsisdnResponse\"/>";
        wsdl += "      </wsdl:operation>";
        wsdl += "      <wsdl:operation name=\"TopupMsisdn\">";
        wsdl += "         <wsdl:input message=\"tns:TopupMsisdn\" name=\"TopupMsisdn\"/>";
        wsdl += "         <wsdl:output message=\"tns:TopupMsisdnResponse\" name=\"TopupMsisdnResponse\"/>";
        wsdl += "      </wsdl:operation>";
        wsdl += "   </wsdl:portType>";
        wsdl += "   <wsdl:binding name=\"RechargeMsisdnServiceSoapBinding\" type=\"tns:RechargeMsisdn\">";
        wsdl += "      <soap:binding style=\"rpc\" transport=\"http://schemas.xmlsoap.org/soap/http\"/>";
        wsdl += "      <wsdl:operation name=\"cancelMsisdn\">";
        wsdl += "         <soap:operation soapAction=\"\" style=\"rpc\"/>";
        wsdl += "         <wsdl:input name=\"cancelMsisdn\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:input>";
        wsdl += "         <wsdl:output name=\"cancelMsisdnResponse\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:output>";
        wsdl += "      </wsdl:operation>";
        wsdl += "      <wsdl:operation name=\"queryMsisdn\">";
        wsdl += "         <soap:operation soapAction=\"\" style=\"rpc\"/>";
        wsdl += "         <wsdl:input name=\"queryMsisdn\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:input>";
        wsdl += "         <wsdl:output name=\"queryMsisdnResponse\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:output>";
        wsdl += "      </wsdl:operation>";
        wsdl += "      <wsdl:operation name=\"GetMsisdn\">";
        wsdl += "         <soap:operation soapAction=\"\" style=\"rpc\"/>";
        wsdl += "         <wsdl:input name=\"GetMsisdn\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:input>";
        wsdl += "         <wsdl:output name=\"GetMsisdnResponse\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:output>";
        wsdl += "      </wsdl:operation>";
        wsdl += "      <wsdl:operation name=\"TopupMsisdn\">";
        wsdl += "         <soap:operation soapAction=\"\" style=\"rpc\"/>";
        wsdl += "         <wsdl:input name=\"TopupMsisdn\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:input>";
        wsdl += "         <wsdl:output name=\"TopupMsisdnResponse\">";
        wsdl += "            <soap:body namespace=\"http://tempuri.org/\" use=\"literal\"/>";
        wsdl += "         </wsdl:output>";
        wsdl += "      </wsdl:operation>";
        wsdl += "   </wsdl:binding>";
        wsdl += "   <wsdl:service name=\"RechargeMsisdnService\">";
        wsdl += "      <wsdl:port binding=\"tns:RechargeMsisdnServiceSoapBinding\" name=\"RechargeMsisdnPort\">";
        wsdl += "         <soap:address location=\"" + mockContext.get("coobill.wsdl") + "\"/>";
        wsdl += "      </wsdl:port>";
        wsdl += "   </wsdl:service>";
        wsdl += "</wsdl:definitions>";
        return wsdl;
    }

    /**
     * test method.
     */
    public static void main(String[] args) throws Exception {
        String xml = "";
        xml += "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        xml += "    <soap:Body>";
        xml += "        <ns1:TopupMsisdn xmlns:ns1=\"http://tempuri.org/\">";
        xml += "            <agent>paygo24</agent>";
        xml += "            <password>20b7889cf6325b4644ab</password>";
        xml += "            <msisdn>008613825207590</msisdn>";
        xml += "            <amount>1</amount>";
        xml += "            <refTrx>20144230101235</refTrx>";
        xml += "        </ns1:TopupMsisdn>";
        xml += "    </soap:Body>";
        xml += "</soap:Envelope>";
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new ByteArrayInputStream(xml.getBytes()));
        // System.out.println(doc.selectSingleNode("//soap:Envelope/soap:Body/ns1:TopupMsisdn/msisdn").getText());
        XPath xpath = doc.createXPath("//soap:Envelope/soap:Body/ns1:TopupMsisdn/msisdn");
        Map<String, String> nsb = new HashMap<String, String>();
        nsb.put("soap", "http://schemas.xmlsoap.org/soap/envelope/");
        nsb.put("ns1", "http://tempuri.org/");
        xpath.setNamespaceURIs(nsb);
        Node node = xpath.selectSingleNode(doc);
        System.out.println(node.getText());

        xpath = doc.createXPath("//soap:Envelope/soap:Body/ns1:queryMsisdn");
        xpath.setNamespaceURIs(nsb);
        node = xpath.selectSingleNode(doc);
        System.out.println(node);

        xpath = doc.createXPath("//soap:Envelope/soap:Body");
        xpath.setNamespaceURIs(nsb);
        node = xpath.selectSingleNode(doc);
        System.out.println(node);

        CoobillPortServlet servlet = new CoobillPortServlet();
        String wsdl = servlet.coobillWsdl();
        System.out.println(wsdl);

        System.out.println(1 % 3);
        System.out.println(2 % 3);
        System.out.println(3 % 3);
        System.out.println(4 % 3);
        System.out.println(5 % 3);
        System.out.println(6 % 3);
    }
}
