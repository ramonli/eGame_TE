package com.mpos.lottery.te.common;

import java.util.concurrent.locks.ReentrantLock;

public class Java5TransactionLockerTester {

    public static void main(String args[]) {
        Thread t1 = new Thread(new RI("X"), "T-1");
        Thread t2 = new Thread(new RI("X"), "T-2");
        Thread t3 = new Thread(new RI("X"), "T-3");
        Thread t4 = new Thread(new RI("X"), "T-4");
        Thread t5 = new Thread(new RI("Y"), "T-5");
        Thread t6 = new Thread(new RI("Y"), "T-6");
        Thread t7 = new Thread(new RI("Y"), "T-7");
        Thread t8 = new Thread(new RI("Y"), "T-8");
        Thread t9 = new Thread(new RI(null), "T-9");
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();
        t9.start();
    }

}

class RI implements Runnable {
    private String key;

    public RI(String key) {
        this.key = key;
    }

    public void run() {
        KeyValuePair<String, ReentrantLock> lockMap = Java5TransactionLocker.getInstance().acquire(key);
        ReentrantLock lock = lockMap.getValue();
        // lock.lock();
        try {
            Thread.sleep(5 * 1000);
            System.out.println(Thread.currentThread().getName() + ": finished.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Java5TransactionLocker.getInstance().release(lockMap);
        }
    }

}
