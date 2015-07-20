package com.mpos.lottery.te.valueaddservice.airtime.support.spi.smart;

import com.mpos.lottery.te.common.http.HttpMethod;
import com.mpos.lottery.te.common.util.SimpleToolkit;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.ApplicationException;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.trans.domain.Transaction;
import com.mpos.lottery.te.valueaddservice.airtime.AirtimeTopup;
import com.mpos.lottery.te.valueaddservice.airtime.support.AbstractAirtimeProvider;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;

@Component("smartAirtimeProvider")
public class SmartAirtimeProvider extends AbstractAirtimeProvider {
    private Log logger = LogFactory.getLog(SmartAirtimeProvider.class);
    private static final int SMART_PROVIDER_ID = 2;
    // all other value of COOBILL_QUERY_STATUS is failure
    public static final int SMART_TOPOUP_STATUS_OK = 0;
    private DefaultHttpClient httpClient;

    /**
     * General constructor.
     */
    public SmartAirtimeProvider() {
        httpClient = new DefaultHttpClient();
        // configure default connection parameters
        // timeout of waiting for data
        this.httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                MLotteryContext.getInstance().getInt("remoteservice.read.timeout", 30) * 1000);
        // timeout of establishing connection
        this.httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                MLotteryContext.getInstance().getInt("remoteservice.connection.timeout", 30) * 1000);
    }

    @Override
    public int supportProvider() {
        return SMART_PROVIDER_ID;
    }

    /**
     * Call topup service of Smart. As Smart doesn't provide any service for transaction enquiry or cancellation, TE
     * doesn't provide those services too.
     */
    @Override
    public AirtimeTopup topup(final Context respCtx, final AirtimeTopup topupReq) throws ApplicationException {
        topupReq.setSerialNo(this.generateRefNo());
        // each provider may request a different refNo algorithm.
        MLotteryContext context = MLotteryContext.getInstance();
        try {
            HttpUriRequest request = HttpMethod.POST.getRequest(new URI(context.get("smart.url")));
            request.setHeader("SOAPAction", "http://sdf.cellc.net/process");
            // convert the monetary unit from dollar to cent.
            BigDecimal amount = SimpleToolkit.mathMultiple(topupReq.getAmount(), new BigDecimal("100.0"));
            HttpEntity entity = new ByteArrayEntity(this.assembleXmlRequest(topupReq.getSerialNo(),
                    context.get("smart.sourceid"), context.get("smart.username"), context.get("smart.password"),
                    topupReq.getMobileNo(), amount.intValue()).getBytes());
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);

            AirtimeTopup result = this.httpClient.execute(request, new ResponseHandler<AirtimeTopup>() {

                @Override
                public AirtimeTopup handleResponse(HttpResponse httpResponse) throws ClientProtocolException,
                        IOException {
                    byte[] entityByte = EntityUtils.toByteArray(httpResponse.getEntity());
                    if (logger.isDebugEnabled()) {
                        logger.debug("[Smart Response] " + new String(entityByte));
                    }
                    SmartResponse respDto = assembleResponse(entityByte);
                    // assemble response
                    topupReq.setStatus(SMART_TOPOUP_STATUS_OK == respDto.getStatusCode()
                            ? AirtimeTopup.STATUS_SUCCESS
                            : AirtimeTopup.STATUS_FAIL);
                    topupReq.setRespMessageOfRemoteService(respDto.getErrorDesc());
                    topupReq.setTelcCommTransId(respDto.getRefId());
                    if (respDto.getStatusCode() != SMART_TOPOUP_STATUS_OK) {
                        logger.warn("Failed result of transaction(" + respCtx.getTransaction().getId()
                                + ") from SMART:" + respDto.getErrorDesc());
                    }
                    return topupReq;
                }
            }, null);
            return result;
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    protected SmartResponse assembleResponse(byte[] respXml) {
        SmartResponse respDto = new SmartResponse();
        try {
            SAXReader reader = new SAXReader();
            Document doc = reader.read(new ByteArrayInputStream(respXml));
            // XPath xpath = doc.createXPath("//soapenv:Envelope/soapenv:Body/com:SDF_Data/com:result/com:statusCode");
            // Node node = xpath.selectSingleNode(doc);
            // System.out.println(node.getText());
            String smartPrefix = doc.getRootElement().getNamespaceForURI("http://sdf.cellc.net/commonDataModel")
                    .getPrefix();
            String soapPrefix = doc.getRootElement().getNamespaceForURI("http://schemas.xmlsoap.org/soap/envelope/")
                    .getPrefix();

            String xpath = String.format("//%s:Envelope/%s:Body/%s:SDF_Data/%s:result/%s:statusCode", soapPrefix,
                    soapPrefix, smartPrefix, smartPrefix, smartPrefix);
            Node node = doc.createXPath(xpath).selectSingleNode(doc);
            respDto.setStatusCode(Integer.parseInt(node.getText()));

            xpath = String.format("//%s:Envelope/%s:Body/%s:SDF_Data/%s:result/%s:errorCode", soapPrefix, soapPrefix,
                    smartPrefix, smartPrefix, smartPrefix);
            node = doc.createXPath(xpath).selectSingleNode(doc);
            respDto.setErrorCode(Integer.parseInt(node.getText()));

            xpath = String.format("//%s:Envelope/%s:Body/%s:SDF_Data/%s:result/%s:errorDescription", soapPrefix,
                    soapPrefix, smartPrefix, smartPrefix, smartPrefix);
            node = doc.createXPath(xpath).selectSingleNode(doc);
            respDto.setErrorDesc(node.getText());

            xpath = String.format("//%s:Envelope/%s:Body/%s:SDF_Data/%s:result/%s:instanceId", soapPrefix, soapPrefix,
                    smartPrefix, smartPrefix, smartPrefix);
            node = doc.createXPath(xpath).selectSingleNode(doc);
            respDto.setRefId(node.getText());
        } catch (Exception e) {
            throw new SystemException(e);
        }
        return respDto;
    }

    protected String assembleXmlRequest(String transId, String sourceId, String userName, String password,
            String mobileNo, int amount) {
        String xmlTemplate = "";
        xmlTemplate += "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:com=\"http://sdf.cellc.net/commonDataModel\">";
        xmlTemplate += "   <soapenv:Header/>";
        xmlTemplate += "   <soapenv:Body>";
        xmlTemplate += "      <com:SDF_Data>";
        xmlTemplate += "         <com:header>";
        xmlTemplate += "            <com:processTypeID>8798</com:processTypeID>";
        xmlTemplate += "            <com:externalReference>%s</com:externalReference>";
        xmlTemplate += "            <com:sourceID>%s</com:sourceID>";
        xmlTemplate += "            <com:username>%s</com:username>";
        xmlTemplate += "            <com:password>%s</com:password>";
        xmlTemplate += "            <com:processFlag>1</com:processFlag>";
        xmlTemplate += "         </com:header>";
        xmlTemplate += "         <com:parameters name=\"\">";
        xmlTemplate += "            <com:parameter name=\"RechargeType\">1010</com:parameter>";
        xmlTemplate += "            <com:parameter name=\"MSISDN\">%s</com:parameter>";
        xmlTemplate += "            <com:parameter name=\"Amount\">%s</com:parameter>";
        xmlTemplate += "            <com:parameter name=\"Channel_ID\">35</com:parameter>";
        xmlTemplate += "         </com:parameters>";
        xmlTemplate += "      </com:SDF_Data>";
        xmlTemplate += "   </soapenv:Body>";
        xmlTemplate += "</soapenv:Envelope>";
        String reqXml = String.format(xmlTemplate, transId, sourceId, userName, password, mobileNo, amount);
        if (logger.isDebugEnabled()) {
            logger.debug("[SMART Request] " + reqXml);
        }
        return reqXml;
    }

    private class SmartResponse {
        private int statusCode;
        private int errorCode;
        private String errorDesc;
        private String refId;

        public SmartResponse() {
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public int getErrorCode() {
            return errorCode;
        }

        public void setErrorCode(int errorCode) {
            this.errorCode = errorCode;
        }

        public String getErrorDesc() {
            return errorDesc;
        }

        public void setErrorDesc(String errorDesc) {
            this.errorDesc = errorDesc;
        }

        public String getRefId() {
            return refId;
        }

        public void setRefId(String refId) {
            this.refId = refId;
        }

    }

    public static void main(String[] args) throws Exception {
        System.out.println(new SmartAirtimeProvider().assembleXmlRequest("23423523623423423", "33613195",
                "CM6 Lottery", "Y202U01AcA==", "70204008", 1));

        String soapResp = "";
        soapResp += "<soapenv:Envelope xmlns:com=\"http://sdf.cellc.net/commonDataModel\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        soapResp += "   <soapenv:Header/>";
        soapResp += "   <soapenv:Body>";
        soapResp += "      <com:SDF_Data processID=\"\">";
        soapResp += "         <com:header>";
        soapResp += "            <com:processTypeID>8798</com:processTypeID>";
        soapResp += "            <com:externalReference>911411110000012400035</com:externalReference>";
        soapResp += "            <com:sourceID>33613195</com:sourceID>";
        soapResp += "            <com:username>CM6 Lottery</com:username>";
        soapResp += "            <com:password>Y202U01AcA==</com:password>";
        soapResp += "            <com:processFlag>1</com:processFlag>";
        soapResp += "         </com:header>";
        soapResp += "         <com:parameters name=\"\">";
        soapResp += "            <com:parameter name=\"RechargeType\">101</com:parameter>";
        soapResp += "            <com:parameter name=\"MSISDN\">70204008</com:parameter>";
        soapResp += "            <com:parameter name=\"Amount\">1</com:parameter>";
        soapResp += "            <com:parameter name=\"Channel_ID\">35</com:parameter>";
        soapResp += "         </com:parameters>";
        soapResp += "         <com:result>";
        soapResp += "            <com:statusCode>0</com:statusCode>";
        soapResp += "            <com:errorCode>0</com:errorCode>";
        soapResp += "            <com:errorDescription>Successful Transaction 0508155235053561</com:errorDescription>";
        soapResp += "            <com:instanceId>0508155235053561</com:instanceId>";
        soapResp += "         </com:result>";
        soapResp += "      </com:SDF_Data>";
        soapResp += "   </soapenv:Body>";
        soapResp += "</soapenv:Envelope>";
        SAXReader reader = new SAXReader();
        Document doc = reader.read(new ByteArrayInputStream(soapResp.getBytes()));
        // ((DefaultElement) doc.getRootElement()).setNamespace(Namespace.NO_NAMESPACE);
        // doc.getRootElement().additionalNamespaces().clear();
        XPath xpath = doc.createXPath("//soapenv:Envelope/soapenv:Body/com:SDF_Data/com:result/com:statusCode");
        Node node = xpath.selectSingleNode(doc);
        System.out.println(node.getText());
        System.out.println(doc.getRootElement().getNamespaceForURI("http://sdf.cellc.net/commonDataModel").getPrefix());
        System.out.println(doc.getRootElement().getNamespaceForURI("http://schemas.xmlsoap.org/soap/envelope/")
                .getPrefix());
        System.out.println(doc.getRootElement().getNamespaceForPrefix("com"));

        SmartAirtimeProvider provider = new SmartAirtimeProvider();
        Context respCtx = new Context();
        Transaction trans = new Transaction();
        trans.setId("9000002");
        respCtx.setTransaction(trans);
        AirtimeTopup topupReq = new AirtimeTopup();
        topupReq.setMobileNo("70204008");
        topupReq.setAmount(new BigDecimal("1"));
        AirtimeTopup respDto = provider.topup(respCtx, topupReq);
        System.out.println(ToStringBuilder.reflectionToString(respDto));
    }
}
