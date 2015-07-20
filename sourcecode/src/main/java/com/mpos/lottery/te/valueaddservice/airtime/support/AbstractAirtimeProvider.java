package com.mpos.lottery.te.valueaddservice.airtime.support;

import com.mpos.lottery.te.common.util.SimpleToolkit;

import org.springframework.beans.factory.InitializingBean;

import java.util.Date;
import java.util.Random;

import javax.annotation.Resource;

/**
 * The auto-register <code>AirtimeProvider</code> implementation, all sub-implementations should inherit from this
 * class. The instance must be spring managed, then spring framework will register the instance of sub-implementation to
 * {@link AirtimeProviderFactory} instance.
 * 
 * @author Ramon
 */
public abstract class AbstractAirtimeProvider implements AirtimeProvider, InitializingBean {
    protected static final String JOB_GROUP = "airtime";
    @Resource(name = "airtimeProviderFactory")
    private AirtimeProviderFactory airtimeProviderFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.getAirtimeProviderFactory().register(this);
    }

    public AirtimeProviderFactory getAirtimeProviderFactory() {
        return airtimeProviderFactory;
    }

    public void setAirtimeProviderFactory(AirtimeProviderFactory airtimeProviderFactory) {
        this.airtimeProviderFactory = airtimeProviderFactory;
    }

    /**
     * Generate a unique reference No. whose length must be less than 19. It will be nanoseconds + (4 random number).
     */
    protected String generateRefNo() {
        StringBuffer sb = new StringBuffer();
        Random ran = new Random();
        int ranInt = ran.nextInt(9999);
        sb.append(new Date().getTime()).append(SimpleToolkit.fillLeft(4, '0', ranInt + ""));
        return sb.toString();
    }
}
