package com.mpos.lottery.te.port;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RequestHeadersUnitTest {

    @Test
    public void test() {
        String baseRequiredHeaders[] = RequestHeaders.HEADERS_BASE_MANDATORY;
        assertEquals(8, baseRequiredHeaders.length);
        String baseGameRequiredHeaders[] = RequestHeaders.HEADERS_GAME_MANDATORY;
        assertEquals(9, baseGameRequiredHeaders.length);
        assertEquals(RequestHeaders.HEADER_GAME_TYPE_ID, baseGameRequiredHeaders[8]);
    }

}
