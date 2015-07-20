package com.mpos.lottery.te.valueaddservice.airtime.support.spi.coobill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for orderPaymentItem complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="orderPaymentItem">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="chargeSource" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="cmdType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="coinType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="cooMallID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="operMsg" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="operResult" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="orderId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="payDate" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="payDoneCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="payMoney" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="payStatus" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="payStatusMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="payerSubsId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="receiverSubsId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requestDate" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="signature" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="subGoodsId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="subGoodsMoney" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="totalMoney" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="transactionId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "orderPaymentItem", propOrder = { "chargeSource", "cmdType", "coinType", "cooMallID", "id", "operMsg",
        "operResult", "orderId", "payDate", "payDoneCode", "payMoney", "payStatus", "payStatusMessage", "payerSubsId",
        "receiverSubsId", "requestDate", "signature", "subGoodsId", "subGoodsMoney", "totalMoney", "transactionId" })
public class OrderPaymentItem {

    protected int chargeSource;
    protected int cmdType;
    protected int coinType;
    protected String cooMallID;
    protected String id;
    protected String operMsg;
    protected int operResult;
    protected String orderId;
    protected long payDate;
    protected String payDoneCode;
    protected double payMoney;
    protected int payStatus;
    protected String payStatusMessage;
    protected String payerSubsId;
    protected String receiverSubsId;
    protected long requestDate;
    protected byte[] signature;
    protected String subGoodsId;
    protected double subGoodsMoney;
    protected double totalMoney;
    protected long transactionId;

    /**
     * Gets the value of the chargeSource property.
     * 
     */
    public int getChargeSource() {
        return chargeSource;
    }

    /**
     * Sets the value of the chargeSource property.
     * 
     */
    public void setChargeSource(int value) {
        this.chargeSource = value;
    }

    /**
     * Gets the value of the cmdType property.
     * 
     */
    public int getCmdType() {
        return cmdType;
    }

    /**
     * Sets the value of the cmdType property.
     * 
     */
    public void setCmdType(int value) {
        this.cmdType = value;
    }

    /**
     * Gets the value of the coinType property.
     * 
     */
    public int getCoinType() {
        return coinType;
    }

    /**
     * Sets the value of the coinType property.
     * 
     */
    public void setCoinType(int value) {
        this.coinType = value;
    }

    /**
     * Gets the value of the cooMallID property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getCooMallID() {
        return cooMallID;
    }

    /**
     * Sets the value of the cooMallID property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setCooMallID(String value) {
        this.cooMallID = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the operMsg property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOperMsg() {
        return operMsg;
    }

    /**
     * Sets the value of the operMsg property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOperMsg(String value) {
        this.operMsg = value;
    }

    /**
     * Gets the value of the operResult property.
     * 
     */
    public int getOperResult() {
        return operResult;
    }

    /**
     * Sets the value of the operResult property.
     * 
     */
    public void setOperResult(int value) {
        this.operResult = value;
    }

    /**
     * Gets the value of the orderId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Sets the value of the orderId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setOrderId(String value) {
        this.orderId = value;
    }

    /**
     * Gets the value of the payDate property.
     * 
     */
    public long getPayDate() {
        return payDate;
    }

    /**
     * Sets the value of the payDate property.
     * 
     */
    public void setPayDate(long value) {
        this.payDate = value;
    }

    /**
     * Gets the value of the payDoneCode property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPayDoneCode() {
        return payDoneCode;
    }

    /**
     * Sets the value of the payDoneCode property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPayDoneCode(String value) {
        this.payDoneCode = value;
    }

    /**
     * Gets the value of the payMoney property.
     * 
     */
    public double getPayMoney() {
        return payMoney;
    }

    /**
     * Sets the value of the payMoney property.
     * 
     */
    public void setPayMoney(double value) {
        this.payMoney = value;
    }

    /**
     * Gets the value of the payStatus property.
     * 
     */
    public int getPayStatus() {
        return payStatus;
    }

    /**
     * Sets the value of the payStatus property.
     * 
     */
    public void setPayStatus(int value) {
        this.payStatus = value;
    }

    /**
     * Gets the value of the payStatusMessage property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPayStatusMessage() {
        return payStatusMessage;
    }

    /**
     * Sets the value of the payStatusMessage property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPayStatusMessage(String value) {
        this.payStatusMessage = value;
    }

    /**
     * Gets the value of the payerSubsId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getPayerSubsId() {
        return payerSubsId;
    }

    /**
     * Sets the value of the payerSubsId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPayerSubsId(String value) {
        this.payerSubsId = value;
    }

    /**
     * Gets the value of the receiverSubsId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getReceiverSubsId() {
        return receiverSubsId;
    }

    /**
     * Sets the value of the receiverSubsId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setReceiverSubsId(String value) {
        this.receiverSubsId = value;
    }

    /**
     * Gets the value of the requestDate property.
     * 
     */
    public long getRequestDate() {
        return requestDate;
    }

    /**
     * Sets the value of the requestDate property.
     * 
     */
    public void setRequestDate(long value) {
        this.requestDate = value;
    }

    /**
     * Gets the value of the signature property.
     * 
     * @return possible object is byte[]
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Sets the value of the signature property.
     * 
     * @param value
     *            allowed object is byte[]
     */
    public void setSignature(byte[] value) {
        this.signature = ((byte[]) value);
    }

    /**
     * Gets the value of the subGoodsId property.
     * 
     * @return possible object is {@link String }
     * 
     */
    public String getSubGoodsId() {
        return subGoodsId;
    }

    /**
     * Sets the value of the subGoodsId property.
     * 
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSubGoodsId(String value) {
        this.subGoodsId = value;
    }

    /**
     * Gets the value of the subGoodsMoney property.
     * 
     */
    public double getSubGoodsMoney() {
        return subGoodsMoney;
    }

    /**
     * Sets the value of the subGoodsMoney property.
     * 
     */
    public void setSubGoodsMoney(double value) {
        this.subGoodsMoney = value;
    }

    /**
     * Gets the value of the totalMoney property.
     * 
     */
    public double getTotalMoney() {
        return totalMoney;
    }

    /**
     * Sets the value of the totalMoney property.
     * 
     */
    public void setTotalMoney(double value) {
        this.totalMoney = value;
    }

    /**
     * Gets the value of the transactionId property.
     * 
     */
    public long getTransactionId() {
        return transactionId;
    }

    /**
     * Sets the value of the transactionId property.
     * 
     */
    public void setTransactionId(long value) {
        this.transactionId = value;
    }

}
