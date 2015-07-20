package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;

import com.mpos.lottery.te.gamespec.prize.PrizeLevelItem;
import com.mpos.lottery.te.merchant.domain.Merchant;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GsonUnitTest {

    @Test
    public void testGson() {
        Map<String, Object> map = new HashMap<String, Object>();
        Merchant merchant = new Merchant();
        merchant.setCode("merchant_code");
        merchant.setId(11l);
        map.put("merchant", merchant);

        List<PrizeLevelItem> items = new LinkedList<PrizeLevelItem>();
        for (int i = 0; i < 3; i++) {
            PrizeLevelItem item = new PrizeLevelItem();
            item.setId(i + "");
            item.setObjectName("iphone#" + i);
            items.add(item);
        }
        map.put("prizeLevelItems", items);
        System.out.println(new Gson().toJson(items));

        String json = new Gson().toJson(map);
        System.out.println(json);

        // encode
        Map newMap = new Gson().fromJson(json, Map.class);
        Merchant m = (Merchant) map.get("merchant");
        assertEquals(merchant.getCode(), m.getCode());
        items = (List<PrizeLevelItem>) map.get("prizeLevelItems");
        assertEquals(3, items.size());
        PrizeLevelItem item0 = items.get(0);
        assertEquals("0", item0.getId());

        System.out.println(new Gson().toJson(new HashMap<String, Object>()));
    }

    @Test
    public void testGson_2() {
        String jsonInput = "{\"JsonRiskControlTrans\":[{\"id\":\"LFN-1\",\"amount\":100.0},{\"id\":\"0114111900000000010002\","
                + "\"amount\":300.00},{\"id\":\"0114111900000000010003\",\"amount\":300.00},{\"id\":\"0114111900000000010004\","
                + "\"amount\":300.00},{\"id\":\"0114111900000000010005\",\"amount\":300.00},{\"id\":\"0114111900000000010006\","
                + "\"amount\":100.0},{\"id\":\"0114111900000000010007\",\"amount\":300.00},{\"id\":\"0114111900000000010008\","
                + "\"amount\":300.00},{\"id\":\"0114111900000000010009\",\"amount\":300.00},{\"id\":\"0114111900000000010010\","
                + "\"amount\":300.00}]}";
        // Map respJsonMap = new Gson().fromJson(jsonInput, Map.class);
        Map map = new Gson().fromJson(jsonInput, Map.class);
        String riskJsonInput = new Gson().toJson(map.get("JsonRiskControlTrans"));
        System.out.println(riskJsonInput);
        // new Gson().fromJson(jsonEle, classOfT)
        // System.out.println(((List)respJsonMap.get("JsonRiskControlTrans")).get(0).getClass());
        // List<JsonRiskControlTransItem> riskLogs =
        // (List<JsonRiskControlTransItem>) respJsonMap
        // .get("JsonRiskControlTrans");
        // // cancel those logs one by one
        // assertEquals(100.0, riskLogs.get(0).getAmount().doubleValue(), 0);
    }

}
