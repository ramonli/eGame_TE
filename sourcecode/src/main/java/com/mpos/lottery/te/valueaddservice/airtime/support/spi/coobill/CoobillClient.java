package com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class CoobillClient {

    protected RechargeMsisdnService service;

    private long connectionTimeout;
    private long receiveTimeout;
    private URL url;
    private QName serviceName;

    public CoobillClient(String wsdl, long connectionTimeout, long receiveTimeout) throws MalformedURLException {
        this.connectionTimeout = connectionTimeout;
        this.receiveTimeout = receiveTimeout;
        URL baseUrl = com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill.RechargeMsisdnService.class.getResource(".");
        url = new URL(baseUrl, wsdl);
        serviceName = new QName("http://tempuri.org/", "RechargeMsisdnService");

        service = new RechargeMsisdnService(url, serviceName);
    }

    /**
     * Coolbill topup Msisdn transaction.
     * 
     * @param topupMsisdnReq
     *            Topup Msisdn request parameter object
     * @return TopupMsisdnResult
     * @throws SocketTimeoutException
     *             Response Exception
     * @throws XMLStreamException
     *             Connect Exception
     */
    public TopupMsisdnResult topupMsisdn(TopupMsisdnReq topupMsisdnReq) throws SocketTimeoutException,
            XMLStreamException {
        try {
            RechargeMsisdn rechargeMsisdn = service.getRechargeMsisdnPort();

            // set transaction timeout
            this.setTimeout(rechargeMsisdn);
            // String agent = MLotteryContext.getInstance().get(MLotteryContext.COOBILL_AGENT);
            // String password = MLotteryContext.getInstance().get(MLotteryContext.COOBILL_PASSWORD);

            TopupMsisdnResult result = rechargeMsisdn.topupMsisdn(topupMsisdnReq.getAgent(),
                    topupMsisdnReq.getPassword(), topupMsisdnReq.getMsisdn(), topupMsisdnReq.getAmout(),
                    topupMsisdnReq.getRefTrx());
            return result;
        } catch (RuntimeException e) {
            Throwable ta = e.getCause();
            if (ta instanceof SocketTimeoutException) {
                throw (SocketTimeoutException) ta;
            } else if (ta instanceof XMLStreamException) {
                throw (XMLStreamException) ta;
            } else {
                throw e;
            }
        }

    }

    /**
     * Query top up transaction info by transaction id
     * 
     * @param queryMsisdnReq
     *            telNo : msisdn of TopupMsisdnReq ,reqTransid:refTrx of TopupMsisdnReq
     * @return OrderPaymentItem return transaction info
     * @throws SocketTimeoutException
     * @throws XMLStreamException
     */
    public OrderPaymentItem queryMsisdn(QueryMsisdnReq queryMsisdnReq) throws SocketTimeoutException,
            XMLStreamException {
        RechargeMsisdn rechargeMsisdn = service.getRechargeMsisdnPort();
        // set transaction timeout
        try {
            this.setTimeout(rechargeMsisdn);
            return rechargeMsisdn.queryMsisdn(queryMsisdnReq.getTelNo(), queryMsisdnReq.getReqTransid());
        } catch (RuntimeException e) {
            Throwable ta = e.getCause();
            if (ta instanceof SocketTimeoutException) {
                throw (SocketTimeoutException) ta;
            } else if (ta instanceof XMLStreamException) {
                throw (XMLStreamException) ta;
            } else {
                throw e;
            }
        }
    }

    private void setTimeout(RechargeMsisdn rechargeMsisdn) {

        Client cl = ClientProxy.getClient(rechargeMsisdn);
        HTTPConduit http = (HTTPConduit) cl.getConduit();
        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout(connectionTimeout);
        httpClientPolicy.setReceiveTimeout(receiveTimeout);
        http.setClient(httpClientPolicy);
    }

    /**
     * main method.
     * 
     * @param args
     *            agres
     * @throws XMLStreamException
     * @throws SocketTimeoutException
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws SocketTimeoutException, XMLStreamException, MalformedURLException {
        // CoobillClient client = new CoobillClient("http://onlinehall.cootel.com.kh/bankAgentRecharge/terminal?wsdl",
        // 1000, 1000);
        CoobillClient client = new CoobillClient("http://localhost:9090/3rdparty/cb/", 30 * 1000, 30 * 1000);
        // TopupMsisdnReq topupMsisdnReq = new TopupMsisdnReq();
        // topupMsisdnReq.setAgent("paygo24");
        // topupMsisdnReq.setPassword("20b7889cf6325b4644ab");
        // topupMsisdnReq.setAmout(1);
        // // topupMsisdnReq.setMsisdn("008613825207590");
        // topupMsisdnReq.setMsisdn("222");
        // topupMsisdnReq.setRefTrx("20144230101235");
        // TopupMsisdnResult result = client.topupMsisdn(topupMsisdnReq);
        // System.out.println("Description : " + result.getDescription());
        // System.out.println("Status : " + result.getStatus());
        // System.out.println("TransactionDate : " + result.getTransactionDate());
        // System.out.println("TransactionId : " + result.getTransactionId());

        QueryMsisdnReq req = new QueryMsisdnReq();
        req.setReqTransid("14225985390228460");
        req.setTelNo("008613590495473");
        OrderPaymentItem result = client.queryMsisdn(req);
        System.out.println("[Status]" + result.getPayStatus());
        System.out.println("[TransId]" + result.getTransactionId());
    }

}
