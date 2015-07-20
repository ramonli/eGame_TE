package com.mpos.lottery.te.workingkey.domain;

import com.mpos.lottery.te.config.MLotteryContext;
import com.mpos.lottery.te.workingkey.dao.WorkingKeyDao;

import java.util.HashMap;
import java.util.Map;

/**
 * The daily working key for a given GPE is unique and won't change once generated, to it is suitable to cache.
 * 
 * @author Ramon
 */
public class WorkingKeyCache {
    private static WorkingKeyCache cache;
    // key of outer map is gpeId, and key of inner map is dailyStr.
    private Map<String, Map<String, WorkingKey>> gpeWorkingKeyMap = new HashMap<String, Map<String, WorkingKey>>();

    private WorkingKeyCache() {
    }

    /**
     * Get the singleton instance.
     */
    public static synchronized WorkingKeyCache getInstance() {
        if (cache == null) {
            cache = new WorkingKeyCache();
        }
        return cache;
    }

    /**
     * Get a daily working key by GPE and date string.
     */
    public synchronized WorkingKey getDailyWorkingKey(String gpeId, String dailyStr) {
        WorkingKey workingKey = null;
        Map<String, WorkingKey> dailyWorkingKeyMap = this.gpeWorkingKeyMap.get(gpeId);
        if (dailyWorkingKeyMap == null) {
            dailyWorkingKeyMap = new HashMap<String, WorkingKey>();
            this.gpeWorkingKeyMap.put(gpeId, dailyWorkingKeyMap);

            // create working key for this GPE
            workingKey = this.fetchWorkingKey(gpeId, dailyStr);
            if (workingKey != null) {
                dailyWorkingKeyMap.put(dailyStr, workingKey);
            }
        } else {
            workingKey = dailyWorkingKeyMap.get(dailyStr);
            if (workingKey == null) {
                workingKey = this.fetchWorkingKey(gpeId, dailyStr);
                if (workingKey != null) {
                    // clear all other workingKey of this GPE to avoid
                    // OutOfMemoryException of jvm heap
                    dailyWorkingKeyMap.clear();
                    // put new daily working key into cache
                    dailyWorkingKeyMap.put(dailyStr, workingKey);
                }
            }
        }
        return workingKey;
    }

    private WorkingKey fetchWorkingKey(String gpeId, String dailyStr) {
        MLotteryContext prop = MLotteryContext.getInstance();
        String beanName = prop.get(MLotteryContext.ENTRY_BEAN_WORKINGKEYDAO);
        WorkingKeyDao dao = (WorkingKeyDao) prop.getBeanFactory().getBean(beanName);
        WorkingKey key = dao.getWorkingKey(dailyStr, gpeId);
        return key;
    }
}
