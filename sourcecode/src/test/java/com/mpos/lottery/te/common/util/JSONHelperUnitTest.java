package com.mpos.lottery.te.common.util;

import static org.junit.Assert.assertEquals;

import com.google.gson.Gson;

import com.mpos.lottery.te.merchant.web.CreditTransferDto;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JSONHelperUnitTest {

    @Test
    public void testDecode() {
        String input = "{\"balance\":1000.21,\"num\":100,\"nickname\":null,\"is_vip\":true,\"name\":\"foo\"}";
        Object o = JSONHelper.decode(input);
        JSONObject jo = (JSONObject) o;
        assertEquals(1000.21, (Double) jo.get("balance"), 0);
    }

    @Test
    public void testEncodeObjectArray() {
        String[] input = { "11", "33sdf9234112", "09123459s7123" };
        String output = JSONHelper.encode(input);
        assertEquals("[\"11\",\"33sdf9234112\",\"09123459s7123\"]", output);
        Object o = JSONHelper.decode(output);
        JSONArray jo = (JSONArray) o;
        assertEquals(3, jo.size());
        assertEquals(input[0], jo.get(0));
        assertEquals(input[1], jo.get(1));
        assertEquals(input[2], jo.get(2));
    }

    @Test
    public void testEncodeList() {
        List input = new ArrayList();
        input.add("11");
        input.add("33sdf9234112");
        input.add("09123459s7123");
        String output = JSONHelper.encode(input);
        assertEquals("[\"11\",\"33sdf9234112\",\"09123459s7123\"]", output);
        Object o = JSONHelper.decode(output);
        JSONArray jo = (JSONArray) o;
        assertEquals(3, jo.size());
        assertEquals(input.get(0), jo.get(0));
        assertEquals(input.get(1), jo.get(1));
        assertEquals(input.get(2), jo.get(2));
    }

    @Test
    public void testEncodeMap() {
        JSONObject header = new JSONObject();
        header.put("type", 1);
        JSONObject input = new JSONObject();
        input.put("name", true);
        input.put("phone", "33sdf9234112");
        input.put("mobile", "09123459s7123");
        header.put("value", input);
        String output = JSONHelper.encode(header);
        System.out.println(output);
        assertEquals("{\"value\":{\"phone\":\"33sdf9234112\",\"name\":true,\"mobile\":\"09123459s7123\"},\"type\":1}",
                output);
        Object o = JSONHelper.decode(output);
        JSONObject jo = (JSONObject) o;
        assertEquals(true, ((JSONObject) jo.get("value")).get("name"));
        assertEquals("33sdf9234112", ((JSONObject) jo.get("value")).get("phone"));
        assertEquals("09123459s7123", ((JSONObject) jo.get("value")).get("mobile"));
    }

    @Test
    public void testGson() {
        CreditTransferDto dto = new CreditTransferDto();
        dto.setFromOperatorLoginName("OPERATOR-LOGIN");
        dto.setToOperatorLoginName("OPERATOR-LOGIN-2");
        dto.setCreditType(CreditTransferDto.CREDITTYPE_SALE);
        dto.setAmount(new BigDecimal("500"));

        Gson gson = new Gson();
        String json = gson.toJson(dto);
        System.out.println(json);

        CreditTransferDto jsonDto = gson.fromJson(json, CreditTransferDto.class);
        assertEquals(dto.getFromOperatorLoginName(), jsonDto.getFromOperatorLoginName());
        assertEquals(dto.getToOperatorLoginName(), jsonDto.getToOperatorLoginName());
        assertEquals(dto.getCreditType(), jsonDto.getCreditType());
        assertEquals(dto.getAmount().doubleValue(), jsonDto.getAmount().doubleValue(), 0);
    }

}
