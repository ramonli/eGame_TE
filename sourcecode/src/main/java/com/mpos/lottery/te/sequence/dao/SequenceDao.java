package com.mpos.lottery.te.sequence.dao;

import com.mpos.lottery.te.sequence.domain.Sequence;

import org.springframework.dao.DataAccessException;

public interface SequenceDao {

    Sequence getByName(String name) throws DataAccessException;

    void update(Sequence sequence) throws DataAccessException;
}
