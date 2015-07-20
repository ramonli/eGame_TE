package com.mpos.lottery.te.sequence.dao.jpa;

import com.mpos.lottery.te.config.exception.SystemException;
import com.mpos.lottery.te.sequence.dao.SequenceDao;
import com.mpos.lottery.te.sequence.domain.Sequence;

import org.springframework.jdbc.core.RowMapper;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * When maps java.math.BigInteger to a number(15,0) column, Exception 'ORA-03115 unsupported network datatype or
 * representation' will be thrown. This situation will occur when use jdbc access database directly. When use
 * JPA/Hibernate, no exception will be thrown. I don't know what cause this. So just alter number(15,0) column to
 * varchar(15), then java code will transfer string into java.math.BigInteger.
 */
public class SequenceDaoImpl implements SequenceDao {
    private Connection connection;

    public SequenceDaoImpl(Connection conn) {
        this.connection = conn;
    }

    public Sequence getByName(String name) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String sql = "select * from TE_SEQUENCE where SEQ_NAME=? for update";
            statement = this.connection.prepareStatement(sql);
            statement.setString(1, name);
            rs = statement.executeQuery();
            while (rs.next()) {
                RowMapper mapper = new SequenceRowMapper();
                return (Sequence) mapper.mapRow(rs, -1);
            }
            return null;
        } catch (SQLException e) {
            throw new SystemException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new SystemException(e);
            }
        }
    }

    public void update(Sequence sequence) {
        PreparedStatement statement = null;
        try {
            String sql = "update TE_SEQUENCE set SEQ_NAME=?,MINVALUE=?,MAXVALUE=?,INTERVAL=?,NEXTMIN=?,NEXTMAX=?,"
                    + "ISCYCLE=? where ID=?";
            statement = this.connection.prepareStatement(sql);
            statement.setString(1, sequence.getName());
            statement.setString(2, sequence.getMinValue().toString());
            statement.setString(3, sequence.getMaxValue().toString());
            statement.setString(4, sequence.getInterval().toString());
            statement.setString(5, sequence.getNextMin().toString());
            statement.setString(6, sequence.getNextMax().toString());
            statement.setInt(7, sequence.isCycle() ? 1 : 0);
            statement.setInt(8, sequence.getId());
            statement.execute();
        } catch (SQLException e) {
            throw new SystemException(e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new SystemException(e);
            }
        }
    }

    class SequenceRowMapper implements RowMapper {

        public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
            Sequence sequence = new Sequence();
            sequence.setId(rs.getInt("ID"));
            sequence.setName(rs.getString("SEQ_NAME"));
            sequence.setInterval(new BigInteger(rs.getString("INTERVAL")));
            sequence.setMaxValue(new BigInteger(rs.getString("MAXVALUE")));
            sequence.setMinValue(new BigInteger(rs.getString("MINVALUE")));
            sequence.setNextMax(new BigInteger(rs.getString("NEXTMAX")));
            sequence.setNextMin(new BigInteger(rs.getString("NEXTMIN")));
            sequence.setCycle(rs.getInt("ISCYCLE") == 1 ? true : false);
            return sequence;
        }

    }
}
