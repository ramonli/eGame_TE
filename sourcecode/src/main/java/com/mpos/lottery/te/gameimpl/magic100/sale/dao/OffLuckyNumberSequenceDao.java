/**************************************************************************/
/*                                                                        */
/* Copyright (c) 2010-2015  MPOS Company             　　　　　　　       */
/*                 汇宝交易系统（深圳）有限公司  版权所有 2010-2015       */
/*                                                                        */
/* PROPRIETARY RIGHTS of MPOS Company are involved in the  　　　　　　   */
/* subject matter of this material.  All manufacturing, reproduction, use,*/
/* and sales rights pertaining to this subject matter are governed by the */
/* license agreement.  The recipient of this software implicitly accepts  */
/* the terms of the license.                                              */
/* 本软件文档资料是汇宝交易系统（深圳）公司的资产,任何人士阅读和使用本资料*/
/* 必须获得相应的书面授权,承担保密责任和接受相应的法律约束.               */
/*                                                                        */
/**************************************************************************/

/*
 * System Abbrev ：
 * Ssystem Name  :
 * Component No  ：
 * Component Name：
 * File name     ：OfflinePrizeStatusDao.java
 * Author        ：terry
 * Date          ：2014-7-25
 * Description   :  <description>
 */

/* Updation record 1：
 * Updation date        :  2014-7-25
 * Updator          :  terry
 * Trace No:  <Trace No>
 * Updation No:  <Updation No>
 * Updation Content:  <List all contents of updation and all methods updated.>
 */
package com.mpos.lottery.te.gameimpl.magic100.sale.dao;

import com.mpos.lottery.te.common.dao.DAO;
import com.mpos.lottery.te.gameimpl.magic100.sale.OffLuckyNumberSequence;

/**
 * @author terry
 * @version [Version NO, 2014-7-25]
 */
public interface OffLuckyNumberSequenceDao extends DAO {

    OffLuckyNumberSequence findByGameId(String gameId);
}
