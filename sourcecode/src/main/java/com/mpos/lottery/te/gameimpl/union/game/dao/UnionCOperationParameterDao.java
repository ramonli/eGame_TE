package com.mpos.lottery.te.gameimpl.union.game.dao;

import com.mpos.lottery.te.gameimpl.union.game.UnionCOperationParameter;

import java.util.List;

public interface UnionCOperationParameterDao {

    List<UnionCOperationParameter> findByOperationParameter(String operationParameterId);
}
