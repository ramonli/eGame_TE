package com.mpos.lottery.te.merchant;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

public class MerchantTestMain {

    /**
     * The main test method.
     */
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("spring-test-dao.xml");
        DataSource ds = (DataSource) appContext.getBean("dataSource");
        Connection conn = ds.getConnection();
        conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        try {
            conn.setAutoCommit(false);
            System.out.println(getStatus(conn));

            System.out.println("Wait other transaction to change it.");

            System.out.println(getStatus(conn));
            updateStatus(conn);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.close();
        }
    }

    public static int getStatus(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select status from merchant where merchant_id=111");
        while (rs.next()) {
            return rs.getInt(1);
        }
        throw new SQLException("no record found");

    }

    public static void updateStatus(Connection conn) throws SQLException {
        Statement statement = conn.createStatement();
        statement.execute("update merchant set status=-99 where merchant_id=111");
    }
}
