package com.mpos.lottery.te.common.http;

import com.google.protobuf.Message;

import com.mpos.lottery.te.common.encrypt.TriperDESCipher;
import com.mpos.lottery.te.common.util.Base64Coder;
import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.thirdpartyservice.HttpHeader;
import com.mpos.lottery.te.thirdpartyservice.PaymentTransactionType;
import com.mpos.lottery.te.thirdpartyservice.playeraccount.web.PlayerAccountHttpHeader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class HttpClientProtoBuffChannel {
    private static Log logger = LogFactory.getLog(HttpClientProtoBuffChannel.class);
    public static final String CTX_MESSAGE = "CTX_MESSAGE_BODY";
    private DefaultHttpClient httpClient;
    private URI uri;

    /**
     * Constructor.
     */
    public HttpClientProtoBuffChannel() {
        httpClient = new DefaultHttpClient();
        // configure default connection parameters
        // timeout of waiting for data
        this.httpClient.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT,
                MLotteryContext.getInstance().getInt("remoteservice.read.timeout", 30) * 1000);
        // timeout of establishing connection
        this.httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                MLotteryContext.getInstance().getInt("remoteservice.connection.timeout", 30) * 1000);
    }

    public HttpClientProtoBuffChannel(URI uri) {
        this();
        this.uri = uri;
    }

    public HttpClientProtoBuffChannel(String uri) throws URISyntaxException {
        this();
        this.uri = new URI(uri);
    }

    /**
     * Construct channel with given HTTP parameters. Refer to {@link DefaultHttpClient} for supported parameter key.
     */
    public HttpClientProtoBuffChannel(Map<String, Object> httpParams) {
        this();
        if (httpParams != null) {
            for (String key : httpParams.keySet()) {
                this.httpClient.getParams().setParameter(key, httpParams.get(key));
            }
        }

        this.httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler());
    }

    public HttpClientProtoBuffChannel(URI uri, Map<String, Object> httpParams) {
        this(httpParams);
        this.uri = uri;
    }

    // ---------------------------------------------------------
    // PUBLIC METHODS
    // ---------------------------------------------------------

    /**
     * All requests issued by a single instance of <code>HttpClient</code> share the same URI.
     * 
     * @param transType
     *            THe transaction type.
     * @param httpMethod
     *            Http request method.
     * @param httpHeaders
     *            The headers definition of HTTP.
     * @param entity
     *            THe entity which will be attached to HTTP message.
     * @param responseHandler
     *            THe handler which is responsible of handling response.
     * @return a response determined by handler.
     * @throws IOException
     *             if connection failed.
     */
    public <T> T send(PaymentTransactionType transType, HttpMethod httpMethod, PlayerAccountHttpHeader httpHeaders,
            Message entity, ResponseHandler<T> responseHandler, ReversalHandler<T> reversalHandler) throws IOException {
        if (this.uri == null) {
            throw new SystemException("No host definition found");
        }
        return this.send(transType, this.uri, httpMethod, httpHeaders, entity, responseHandler, reversalHandler);
    }

    /**
     * Executes a request to the target using the given context and processes the response using the given response
     * handler.
     * 
     * @param transType
     *            The transaction type defined by {@link PaymentTransactionType} .
     * @param uri
     *            The target URI.
     * @param httpMethod
     *            THe http method defined by {@link HttpMethod}
     * @param httpHeaders
     *            THe customized HTTP headers.
     * @param entity
     *            THe HTTP entity.
     * @param responseHandler
     *            A handler which is responsible of handling response.
     * @param reversalHandler
     *            A handler which trigger reversal once fail to read response data(read timeout).
     * @return A appropriate response instance which is determined by <code>ResponseHandler</code>
     * @throws ClientProtocolException
     *             in case of an http protocol error
     * @throws IOException
     *             in case of a problem or the connection was aborted
     */
    public <T> T send(PaymentTransactionType transType, URI uri, HttpMethod httpMethod,
            PlayerAccountHttpHeader httpHeaders, Message entity, ResponseHandler<T> responseHandler,
            ReversalHandler<T> reversalHandler) throws IOException, ClientProtocolException {
        HttpUriRequest request = httpMethod.getRequest(uri);
        // set HTTP headers
        assembleHttpHeaders(httpHeaders, request);

        this.assembleRequestEntity(transType, request, entity);

        if (logger.isDebugEnabled()) {
            logger.debug("CoreConnectionPNames.SO_TIMEOUT:"
                    + this.httpClient.getParams().getParameter(CoreConnectionPNames.SO_TIMEOUT));
            logger.debug("CoreConnectionPNames.CONNECTION_TIMEOUT:"
                    + this.httpClient.getParams().getParameter(CoreConnectionPNames.CONNECTION_TIMEOUT));
            logger.debug("Request URL:" + this.uri);
        }

        try {
            // configure HTTP execution context
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(HttpHeader.TRANS_TYPE.getHeader(), transType);
            localContext.setAttribute(CTX_MESSAGE, entity);

            T result = this.httpClient.execute(request, responseHandler, localContext);
            if (logger.isDebugEnabled()) {
                logger.debug("Finish sending request to " + this.uri + ".");
            }
            return result;
        } catch (IOException e) {
            if ((e instanceof InterruptedIOException) && reversalHandler != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Read timeout or fail to get response, the reqeust(" + transType
                            + ") will be reversed automatically.");
                }
                // read response timeout, need to reverse.
                reversalHandler.reverse(uri, entity);
                // Even reverse the timeout message successfully, the original
                // exception must be thrown out exception to let client know
                // that this request is reversed.
                if (logger.isInfoEnabled()) {
                    logger.info("Reverse successfully.");
                }
            }
            throw e;
        }
    }

    protected void assembleHttpHeaders(PlayerAccountHttpHeader httpHeaders, HttpUriRequest request) {
        if (httpHeaders == null) {
            return;
        }
        request.setHeader(PlayerAccountHttpHeader.HEADER_PROTOCOL_VERSION,
                this.encryptHeader(httpHeaders.getProtocolVersion(), true));
        request.setHeader(PlayerAccountHttpHeader.HEADER_REQ_MSGID,
                this.encryptHeader(httpHeaders.getRequestMsgId(), true));
        request.setHeader(PlayerAccountHttpHeader.HEADER_SYSTEM_ID, this.encryptHeader(httpHeaders.getSystemId(), true));
        request.setHeader(PlayerAccountHttpHeader.HEADER_TIME_STAMP,
                this.encryptHeader(httpHeaders.getTimestamp(), true));
        if (httpHeaders.getUserId() != null) {
            request.setHeader(PlayerAccountHttpHeader.HEADER_USER_ID, this.encryptHeader(httpHeaders.getUserId(), true));
        }
        request.setHeader(PlayerAccountHttpHeader.HEADER_TRANS_TYPE,
                this.encryptHeader(httpHeaders.getTransType() + "", true));

        if (logger.isDebugEnabled()) {
            logger.debug("[Prepare Http request Headers]:");
            logger.debug(httpHeaders);
            // print all http headers
            Header[] headers = request.getAllHeaders();
            for (Header header : headers) {
                logger.debug(header.getName() + ": " + header.getValue());
            }
        }
    }

    // ---------------------------------------------------------
    // HELPER METHODS
    // ---------------------------------------------------------

    protected HttpEntity assembleRequestEntity(PaymentTransactionType transType, HttpRequest request, Message body) {
        if (body != null) {
            if (!(request instanceof HttpEntityEnclosingRequestBase)) {
                throw new SystemException("Can NOT set entity to a none " + HttpEntityEnclosingRequestBase.class
                        + " request.");
            }

            HttpEntity entity = new ByteArrayEntity(this.encryptMessage(body));
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
            if (logger.isDebugEnabled()) {
                logger.debug("Assemble entity for transaction(" + transType + "):" + ProtoMessageUtil.toString(body));
            }
            return entity;
        } else {
            return null;
        }
    }

    protected byte[] encryptMessage(Message body) {
        try {
            MLotteryContext mContext = MLotteryContext.getInstance();
            return TriperDESCipher.encrypt(Base64Coder.decode(mContext.get("3rdpart.key")), body.toByteArray(),
                    TriperDESCipher.IV);
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    protected static String encryptHeader(String headerValue, boolean isEncrypt) {
        try {
            MLotteryContext mContext = MLotteryContext.getInstance();
            if (isEncrypt) {
                return TriperDESCipher.encrypt(mContext.get("3rdpart.key"), headerValue, TriperDESCipher.STR_IV);
            } else {
                return TriperDESCipher.decrypt(mContext.get("3rdpart.key"), headerValue, TriperDESCipher.STR_IV);
            }
        } catch (Exception e) {
            throw new SystemException(e);
        }
    }

    // ---------------------------------------------------------
    // HELPER CLASSES
    // ---------------------------------------------------------

    public static class MessageResponseHandler implements ResponseHandler<MessageResponse> {
        private Message responsePrototype;

        public MessageResponseHandler(Message responsePrototype) {
            this.responsePrototype = responsePrototype;
        }

        @Override
        public MessageResponse handleResponse(HttpResponse httpResponse) throws ClientProtocolException, IOException {
            if (httpResponse.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
                logger.warn("Response StatusLine: " + httpResponse.getStatusLine());
            }

            MessageResponse response = new MessageResponse();
            response.setHttpResponse(httpResponse);

            // print header information
            if (logger.isDebugEnabled()) {
                logger.debug("[The HTTP headers of response]:");
                Header[] headers = httpResponse.getAllHeaders();
                for (Header header : headers) {
                    String headerValue = header.getValue();
                    try {
                        String plainValue = encryptHeader(headerValue, false);
                        headerValue = plainValue;
                    } catch (Exception e) {
                        // simply ignore this exception, as there are also many
                        // HTTP predefined headers.
                    }
                    logger.debug(header.getName() + ": " + headerValue);
                }
            }

            // whether the remote service has handled request successfully
            Header respCodeHeader = response.getHttpResponse().getFirstHeader(
                    PlayerAccountHttpHeader.HEADER_RESPONSE_CODE);
            if (respCodeHeader == null) {
                throw new IOException("No response code header found ");
            } else {
                PlayerAccountHttpHeader respHeader = new PlayerAccountHttpHeader();
                respHeader.setResponseCode(Integer.parseInt(HttpClientProtoBuffChannel.encryptHeader(
                        respCodeHeader.getValue().trim(), false).trim()));
                Header respDescHeader = response.getHttpResponse().getFirstHeader(
                        PlayerAccountHttpHeader.HEADER_RESPONSE_DESC);
                respHeader.setResponseDesc(respDescHeader != null ? HttpClientProtoBuffChannel.encryptHeader(
                        respDescHeader.getValue().trim(), false) : "");
                response.setRespHeader(respHeader);
            }
            // if carrying entity
            if (httpResponse.getEntity() != null) {
                byte[] entityByte = EntityUtils.toByteArray(httpResponse.getEntity());
                if (entityByte.length != 0) {
                    // decrypt message body
                    Message messageBody = responsePrototype.newBuilderForType()
                            .mergeFrom(this.decryptMessage(entityByte)).build();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Reponsed message body: " + messageBody);
                    }
                    response.setMessageBody(messageBody);
                } else {
                    // fix bug#6961
                    if (logger.isInfoEnabled()) {
                        logger.info("No any data carried in response body(content length: "
                                + httpResponse.getEntity().getContentLength() + ")");
                    }
                }
            }
            return response;
        }

        protected byte[] decryptMessage(byte[] messageBody) {
            try {
                MLotteryContext mContext = MLotteryContext.getInstance();
                return TriperDESCipher.decrypt(Base64Coder.decode(mContext.get("3rdpart.key")), messageBody,
                        TriperDESCipher.IV);
            } catch (Exception e) {
                throw new SystemException(e);
            }
        }
    }
}
