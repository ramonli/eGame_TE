package com.mpos.lottery.te.ticket;

import com.mpos.lottery.te.gameimpl.lotto.LottoDomainMocker;
import com.mpos.lottery.te.gameimpl.lotto.sale.domain.LottoTicket;
import com.mpos.lottery.te.port.Context;
import com.mpos.lottery.te.test.accptancetest.BaseAcceptanceTest;
import com.mpos.lottery.te.trans.domain.TransactionType;
import com.mpos.lottery.te.workingkey.domain.WorkingKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This test class just check if TE behaves normally in concurrent environment.
 */
public class SellTicketPerfTest extends BaseAcceptanceTest {
    private WorkingKey key;

    private int totalThread = 10;
    private int transPerThread = 1;

    public SellTicketPerfTest() throws Exception {
        key = this.getDefaultWorkingKey();
    }

    public void sell() throws Exception {

        List calls = new ArrayList(0);
        for (int i = 0; i < totalThread; i++) {
            Callable c = new SellCallable(transPerThread);
            calls.add(c);
        }
        ExecutorService pool = Executors.newFixedThreadPool(totalThread);
        // this method will block main thread until 'invokeAll' returns.
        List<Future<Result>> results = pool.invokeAll(calls);
        Result statistics = new Result();
        for (Future<Result> result : results) {
            Result r = result.get();
            statistics.addSuccess(r.getSuccess());
            statistics.addTotalTime(r.getTotalTime());
            statistics.addTotalTrans(r.getTotalTrans());
        }

        pool.shutdown();
        System.out.println("Average Time:" + statistics.getTotalTime() / totalThread * transPerThread / 1000);
        System.out.println("Expected Total Trans:" + totalThread * transPerThread + ", Actual Total Trans:"
                + statistics.getTotalTrans());
        System.out.println("Total Success:" + statistics.getSuccess());
    }

    @Override
    protected void customizeRequestContext(Context ctx) throws Exception {
        // set sell ticket transaction
        ctx.setTransType(TransactionType.SELL_TICKET.getRequestType());
        // sellTicketReq.setWorkingKey(key);
        LottoTicket ticket = LottoDomainMocker.mockTicket();
        ctx.setModel(ticket);
    }

    class SellCallable implements Callable {
        private int transPerThread;

        public SellCallable(int transPerThread) {
            this.transPerThread = transPerThread;
        }

        public Object call() throws Exception {
            Result result = new Result();
            for (int i = 0; i < transPerThread; i++) {
                // assemble request context
                Context request = mockRequestContext();
                request.setWorkingKey(key);

                Context response = null;
                try {
                    long start = System.currentTimeMillis();
                    result.addTotalTrans(1);
                    response = post(request);
                    long end = System.currentTimeMillis();
                    result.addTotalTime(end - start);
                    if (200 == response.getResponseCode()) {
                        result.addSuccess(1);
                    }
                    result.setTicket((LottoTicket) response.getModel());
                    System.out.println(i + ":" + response.getResponseCode() + "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

    }

    public static void main(String args[]) {
        try {
            SellTicketPerfTest perf = new SellTicketPerfTest();
            perf.sell();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class Result {
        private transient int successfulTrans;
        private transient long totalTime;
        private transient int totalTrans;
        private LottoTicket ticket;

        public void addSuccess(int i) {
            this.successfulTrans += i;
        }

        public void addTotalTime(long exeTime) {
            this.totalTime += exeTime;
        }

        public int getSuccess() {
            return this.successfulTrans;
        }

        public long getTotalTime() {
            return this.totalTime;
        }

        public void addTotalTrans(int i) {
            this.totalTrans += i;
        }

        public int getTotalTrans() {
            return this.totalTrans;
        }

        public LottoTicket getTicket() {
            return ticket;
        }

        public void setTicket(LottoTicket ticket) {
            this.ticket = ticket;
        }

    }
}
