package com.mpos.lottery.te.thirdpartyservice.amqp;

import org.junit.Test;

/**
 * Must lauch RabbitMQ broker to pass test.
 * 
 * @author Ramon
 * 
 */
public class MessageConnectionPoolUnitTest {

    @Test
    public void testBorrow() throws Exception {
        // MessageConnectionPoolablObjectFactory factory = new MessageConnectionPoolablObjectFactory();
        // factory.setHost("localhost");
        // GenericObjectPool<Connection> connPool = new GenericObjectPool<Connection>(factory, 2,
        // GenericObjectPool.WHEN_EXHAUSTED_FAIL, 1000);
        //
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // connPool.borrowObject();
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // Connection conn = connPool.borrowObject();
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // connPool.returnObject(conn);
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // try {
        // conn = connPool.borrowObject();
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // connPool.invalidateObject(conn);
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // connPool.borrowObject();
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // assertEquals(2, connPool.getNumActive());
        // connPool.borrowObject();
        // fail("should be NoSuchElementException");
        // }
        // catch (NoSuchElementException e) {
        // e.printStackTrace();
        // }
        // connPool.close();
    }

    @Test
    public void testClose() throws Exception {
        // MessageConnectionPoolablObjectFactory factory = new MessageConnectionPoolablObjectFactory();
        // factory.setHost("localhost");
        // GenericObjectPool<Connection> connPool = new GenericObjectPool<Connection>(factory, 2,
        // GenericObjectPool.WHEN_EXHAUSTED_FAIL, 1000);
        //
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // connPool.borrowObject();
        // connPool.borrowObject();
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
        // connPool.close();
        // System.out.println("Active:" + connPool.getNumActive());
        // System.out.println("Idle:" + connPool.getNumIdle());
    }
}
