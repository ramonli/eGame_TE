package com.mpos.lottery.te.gamespec.sale.support;

import com.mpos.lottery.te.gamespec.sale.BaseTicket;

import java.math.BigDecimal;
import java.util.List;

public class TicketHelper {
    /**
     * Generate a physical ticket based on ticket entities.
     */
    public static <T extends BaseTicket> T assemblePhysicalTicket(List<T> hostTickets, List newEntries) {
        BaseTicket physicalTicket = (BaseTicket) hostTickets.get(0).clone();
        physicalTicket.setTotalBets(hostTickets.get(0).getTotalBets() * hostTickets.get(0).getMultipleDraws());
        physicalTicket.setMultipleDraws(hostTickets.get(0).getMultipleDraws());
        physicalTicket.setValidationCode(hostTickets.get(0).getValidationCode());
        physicalTicket.setBarcode(hostTickets.get(0).getBarcode());
        if (newEntries != null && newEntries.size() > 0) {
            physicalTicket.setEntries(newEntries);
        }
        physicalTicket.setTotalAmount(hostTickets.get(0).getTotalAmount()
                .multiply(new BigDecimal(hostTickets.get(0).getMultipleDraws())));
        physicalTicket.setGameInstance(hostTickets.get(0).getGameInstance());
        physicalTicket.getGameInstance().setGameId(physicalTicket.getGameInstance().getGame().getId());
        physicalTicket.setLastDrawNo(hostTickets.get(physicalTicket.getMultipleDraws() - 1).getGameInstance()
                .getNumber());
        physicalTicket.setTicketType(hostTickets.get(0).getTicketType());
        physicalTicket.setRawSerialNo(false, BaseTicket.descryptSerialNo(hostTickets.get(0).getSerialNo()));
        return (T) physicalTicket;
    }
}
