package com.mpos.lottery.te.gameimpl.magic100.sale;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.List;

public class RequeuedNumbersUnitTest {

    @Test
    public void testLookupValidItems() {
        RequeuedNumbers range = new RequeuedNumbers();

        RequeuedNumbersItem i1 = new RequeuedNumbersItem();
        i1.setSequenceOfNumber(8);
        i1.setState(RequeuedNumbersItem.STATE_VALID);
        range.getRequeuedNumbersItemList().add(i1);

        RequeuedNumbersItem i2 = new RequeuedNumbersItem();
        i2.setSequenceOfNumber(2);
        i2.setState(RequeuedNumbersItem.STATE_INVALID);
        range.getRequeuedNumbersItemList().add(i2);

        RequeuedNumbersItem i3 = new RequeuedNumbersItem();
        i3.setSequenceOfNumber(4);
        i3.setState(RequeuedNumbersItem.STATE_INVALID);
        range.getRequeuedNumbersItemList().add(i3);

        RequeuedNumbersItem i4 = new RequeuedNumbersItem();
        i4.setSequenceOfNumber(3);
        i4.setState(RequeuedNumbersItem.STATE_VALID);
        range.getRequeuedNumbersItemList().add(i4);

        RequeuedNumbersItem i5 = new RequeuedNumbersItem();
        i5.setSequenceOfNumber(7);
        i5.setState(RequeuedNumbersItem.STATE_VALID);
        range.getRequeuedNumbersItemList().add(i5);

        List<RequeuedNumbersItem> list = range.lookupValidItems(5);
        assertEquals(0, list.size());

        list = range.lookupValidItems(3);
        assertEquals(3, list.size());
        assertEquals(3, list.get(0).getSequenceOfNumber());
        assertEquals(7, list.get(1).getSequenceOfNumber());
        assertEquals(8, list.get(2).getSequenceOfNumber());

        list = range.lookupValidItems(2);
        assertEquals(2, list.size());
        assertEquals(3, list.get(0).getSequenceOfNumber());
        assertEquals(7, list.get(1).getSequenceOfNumber());
    }

}
