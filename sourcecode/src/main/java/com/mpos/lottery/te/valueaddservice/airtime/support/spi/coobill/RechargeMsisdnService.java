package com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.3-hudson-390- Generated source version: 2.0
 * <p>
 * An example of how this class may be used:
 * 
 * <pre>
 * RechargeMsisdnService service = new RechargeMsisdnService();
 * RechargeMsisdn portType = service.getRechargeMsisdnPort();
 * portType.cancelMsisdn(...);
 * </pre>
 * 
 * </p>
 * 
 */
@WebServiceClient(name = "RechargeMsisdnService", targetNamespace = "http://tempuri.org/", wsdlLocation = "http://onlinehall.cootel.com.kh/bankAgentRecharge/terminal?wsdl")
public class RechargeMsisdnService extends Service {

    private final static URL RECHARGEMSISDNSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger
            .getLogger(com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill.RechargeMsisdnService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill.RechargeMsisdnService.class.getResource(".");
            url = new URL(baseUrl, "http://onlinehall.cootel.com.kh/bankAgentRecharge/terminal?wsdl");
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: 'http://onlinehall.cootel.com.kh/bankAgentRecharge/terminal?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        RECHARGEMSISDNSERVICE_WSDL_LOCATION = url;
    }

    public RechargeMsisdnService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public RechargeMsisdnService() {
        super(RECHARGEMSISDNSERVICE_WSDL_LOCATION, new QName("http://tempuri.org/", "RechargeMsisdnService"));
    }

    /**
     * 
     * @return returns RechargeMsisdn
     */
    @WebEndpoint(name = "RechargeMsisdnPort")
    public RechargeMsisdn getRechargeMsisdnPort() {
        return super.getPort(new QName("http://tempuri.org/", "RechargeMsisdnPort"), RechargeMsisdn.class);
    }

    public static void main(String[] args) throws MalformedURLException {
        URL baseUrl;
        baseUrl = com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill.RechargeMsisdnService.class.getResource(".");
        URL url = new URL(baseUrl, "http://onlinehall.cootel.com.kh/bankAgentRecharge/terminal?wsdl");
        RechargeMsisdnService service = new RechargeMsisdnService(url, new QName("http://tempuri.org/",
                "RechargeMsisdnService"));
        RechargeMsisdn rechargeMsisdn = service.getRechargeMsisdnPort();
        TopupMsisdnResult result = rechargeMsisdn.topupMsisdn("paygo24", "20b7889cf6325b4644ab", "008613825207590", 1,
                "201412301012319");
        System.out.println("Description : " + result.getDescription());
        System.out.println("Status : " + result.getStatus());
        System.out.println("TransactionDate : " + result.getTransactionDate());
        System.out.println("TransactionId : " + result.getTransactionId());
    }

}