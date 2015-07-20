package com.mpos.lottery.te.common.dao;

import com.mpos.lottery.te.test.integration.BaseTransactionalIntegrationTest;

import oracle.jdbc.OracleTypes;
import oracle.sql.STRUCT;

import org.junit.Test;

import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.sql.DataSource;

public class StoredProcedureCaller extends BaseTransactionalIntegrationTest {

    @Test
    public void test() throws Exception {
        DataSource ds = (DataSource) this.applicationContext.getBean("dataSource");
        Connection conn = null;
        try {
            conn = ds.getConnection();
            String storedProc = "{call enquiry_activity_report(?)}";
            CallableStatement cs = conn.prepareCall(storedProc);
            // register output parameter
            cs.registerOutParameter(1, OracleTypes.ARRAY, "ACTIVITY_REPORT_ITEMS_TYPE");
            cs.execute();
            Array array = cs.getArray(1);
            ResultSet rs = array.getResultSet();
            while (rs.next()) {
                // why getObject(2) instead of getObject(1)?
                Object elements[] = ((STRUCT) rs.getObject(2)).getAttributes();
                System.out.println(elements[0]);
                System.out.println(elements[1]);
                System.out.println(elements[2]);
            }

            cs.close();
        } finally {
            if (conn != null)
                conn.close();
        }
    }

}
