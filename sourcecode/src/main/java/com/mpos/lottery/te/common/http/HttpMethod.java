package com.mpos.lottery.te.common.http;

import com.mpos.lottery.te.config.exception.SystemException;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;

import java.net.URI;

public enum HttpMethod {
    GET,
    POST,
    DELETE,
    PUT;

    public HttpUriRequest getRequest(URI uri) {
        switch (this) {
            case GET :
                return new HttpGet(uri);
            case POST :
                return new HttpPost(uri);
            case DELETE :
                return new HttpDelete(uri);
            case PUT :
                return new HttpPut(uri);
            default :
                throw new SystemException("Unsupported http method:" + this);
        }
    }
}
