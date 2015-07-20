package com.mpos.lottery.te.valueaddservice.airtime.support;

import com.mpos.lottery.te.config.exception.SystemException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class AirtimeProviderFactory {
    private Log logger = LogFactory.getLog(AirtimeProviderFactory.class);
    private Map<Integer, AirtimeProvider> providerFactory = new HashMap<Integer, AirtimeProvider>();

    /**
     * Lookup a <code>AirtimeProvider</code> by given provider id.
     */
    public AirtimeProvider lookupProvider(int airtimeProviderId) {
        for (Integer providerId : providerFactory.keySet()) {
            if (providerId == airtimeProviderId) {
                AirtimeProvider provider = this.providerFactory.get(providerId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Found " + AirtimeProvider.class.getSimpleName() + "(" + provider
                            + ") for airtime provider " + airtimeProviderId);
                }
                return provider;
            }
        }
        return null;
    }

    /**
     * Register a <code>AirtimeProvider</code> instance with given provider Id. Only a single instance can be bound to a
     * given provider ID.
     */
    public void register(AirtimeProvider provider) {
        AirtimeProvider exited = this.lookupProvider(provider.supportProvider());
        if (exited != null) {
            throw new SystemException("THe provider ID(" + provider.supportProvider() + ") has been bounded by "
                    + exited);
        }
        this.providerFactory.put(provider.supportProvider(), provider);
    }
}
