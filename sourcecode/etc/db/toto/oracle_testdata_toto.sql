delete from GAME_TYPE where GAME_TYPE_ID=5;
delete from GAME where GAME_ID like 'GAME-TOTO-%';
delete from GAME_MERCHANT where ID like 'GM-TOTO-%'; 
delete from TT_OPERATION_PARAMETERS;
delete from TOTO_GAME_INSTANCE;
delete from SPORT_MATCH_DETAIL;
delete from SPORT_BET_OPTION;
delete from TE_TT_TICKET;
delete from TE_TOTO_ENTRY;
delete from TE_TRANSACTION where ID like 'TRANS-TOTO-%';
delete from TT_WINNING;
delete from TT_WINNING_STATISTICS;
delete from BD_PRIZE_GROUP_ITEM where GAME_TYPE=5;
delete from MERCHANT_GAME_PROPERTIES where MRID like '%-TOTO-%'; 

-- GAME_TYPE
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(5, 'ToTo');

-- GAME
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'GAME-TOTO-1', 5, 'FTI-111','OP-111','TP-1',2,'TOTO',1,1
);

-- TT_OPERATION_PARAMETERS
insert into TT_OPERATION_PARAMETERS(ID, SEQ_NUMBER,TRIPLE,MIN_DOUBLE,MAX_DOUBLE)
    values('OP-111',1,0,0,3);
insert into TT_OPERATION_PARAMETERS(ID, SEQ_NUMBER,TRIPLE,MIN_DOUBLE,MAX_DOUBLE)
    values('OP-111',2,1,0,3);
insert into TT_OPERATION_PARAMETERS(ID, SEQ_NUMBER,TRIPLE,MIN_DOUBLE,MAX_DOUBLE)
    values('OP-111',3,2,0,3);        

-- GAME_MERCHANT 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-TOTO-111', 111, 'GAME-TOTO-1',0.1,0.1
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-TOTO-114', 222, 'GAME-TOTO-1',0.1,0.1
);

-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-TOTO-111', 111, 'GAME-TOTO-1','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-TOTO-113', 111, 'GAME-TOTO-1','OPERATOR-111',0.0,0.0
);

-- GAME INSTANCE
insert into TOTO_GAME_INSTANCE(
    GAME_INSTANCE_ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    NUM_MATCH,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,BASE_AMOUNT,OMR_GAME_SET,GAME_ANNOUNCE,IS_SUSPEND_PAYOUT,
    CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-111','OPL-1','GAME-TOTO-1','payout started','20090401',
    3,sysdate-5, sysdate-3,0,7,3650,1,sysdate-3+30/(24*60),100.0,'G1',sysdate-5,0,2, 0.4, 15000
);
insert into TOTO_GAME_INSTANCE(
    GAME_INSTANCE_ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    NUM_MATCH,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,BASE_AMOUNT,OMR_GAME_SET,GAME_ANNOUNCE,IS_SUSPEND_PAYOUT,
    CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-112','OPL-1','GAME-TOTO-1','active','20090402',
    3,sysdate-1,sysdate+1,0,2,3650,1,sysdate+1+30/(24*60),100.0,'G2',sysdate-1,0,2, 0.4, 15000
);
update TOTO_GAME_INSTANCE set IS_SUSPEND_SALE=0;
update TOTO_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=0;

-- Match
insert into SPORT_MATCH_DETAIL(
    MATCH_DETAIL_ID,GAME_INSTANCE_ID,MATCH_TYPE_ID,HOME_ID,AWAY_ID,BET_OPTION_ID,BET_TYPE_ID,
    BET_OPTION_VALUE,GAME_DATETIME,COUNT_IN_POOL,RESULT,WIN_OPTION,QUARTER_TYPE,QUARTER_VALUE,MATCH_SEQ
) values(
    'M1','GII-112','MT-1','T1','T6','BO1','BT1',20.0,sysdate+1-30/(24*60),1,null,1,1,1,1
);
insert into SPORT_MATCH_DETAIL(
    MATCH_DETAIL_ID,GAME_INSTANCE_ID,MATCH_TYPE_ID,HOME_ID,AWAY_ID,BET_OPTION_ID,BET_TYPE_ID,
    BET_OPTION_VALUE,GAME_DATETIME,COUNT_IN_POOL,RESULT,WIN_OPTION,QUARTER_TYPE,QUARTER_VALUE,MATCH_SEQ
) values(
    'M2','GII-112','MT-1','T2','T5','BO1','BT1',20.0,sysdate+1-30/(24*60),1,null,1,1,1,1
);
insert into SPORT_MATCH_DETAIL(
    MATCH_DETAIL_ID,GAME_INSTANCE_ID,MATCH_TYPE_ID,HOME_ID,AWAY_ID,BET_OPTION_ID,BET_TYPE_ID,
    BET_OPTION_VALUE,GAME_DATETIME,COUNT_IN_POOL,RESULT,WIN_OPTION,QUARTER_TYPE,QUARTER_VALUE,MATCH_SEQ
) values(
    'M3','GII-112','MT-1','T3','T4','BO1','BT1',20.0,sysdate+1-30/(24*60),1,null,1,1,1,1
);

-- SPORT_BET_OPTION
insert into SPORT_BET_OPTION(BET_OPTION_ID,TYPE,NAME) values('BO1', 0, 'option[0/1/3]');
insert into SPORT_BET_OPTION(BET_OPTION_ID,TYPE,NAME) values('BO2', 1, 'option handicap[0/1/3]');
insert into SPORT_BET_OPTION(BET_OPTION_ID,TYPE,NAME) values('BO3', 2, 'option[0/1]');
insert into SPORT_BET_OPTION(BET_OPTION_ID,TYPE,NAME) values('BO4', 3, 'option handicap[0/1]');
insert into SPORT_BET_OPTION(BET_OPTION_ID,TYPE,NAME) values('BO5', 4, 'Any value');

-- TE_TRANSACTION
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID
) values(
    'TRANS-TOTO-1','OPERATOR-111','GPE-111',111,111,sysdate-4,200,sysdate-4,200,'T-123456',
    'TMI-TT-1','20092009','GAME-TOTO-1'
); 

-- TE_TT_TICKET
insert into TE_TT_TICKET(
    ID, GAME_INSTANCE_ID,TRANSACTION_ID,CREATE_TIME,UPDATE_TIME,SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,
    STATUS,PIN,IS_COUNT_IN_POOL,IS_BLOCK_PAYOUT,OPERATOR_ID,DEV_ID,MERCHANT_ID,MULTI_DRAW,IS_OFFLINE,
    VERSION,TRANS_TYPE,TICKET_TYPE,TICKET_FROM,VALIDATION_CODE,IS_WINING_lUCKY_DRAW,TOTAL_BETS
) values(
    'T1','GII-111','TRANS-TOTO-1',sysdate-4,sysdate-4,'T-123456',600.0,1,1,'f5e09f731f7dffc2a603a7b9b977b2ca',
    1,0,'OPERATOR-111',111,111,1,0,1,200,1,1,'111111',1,1
);
update TE_TT_TICKET set LD_WINING_TOTAL_BETS=1;  

-- TE_TOTO_ENTRY
insert into TE_TOTO_ENTRY(
    ID,TICKET_SERIAL_NO,SELECT_TEAM,CREATE_TIME,UPDATE_TIME,TOTAL_BET,ENTRY_NO,IS_QUIDPICK,ENTRY_AMOUNT,
    VERSION,BET_OPTION
) values(
    'E1','T-123456','0|1|2,1,2|0',sysdate-4,sysdate-4,6,1,1,600.0,1,0
);

--TT_WINNING
insert into TT_WINNING(
    ID, VERSION, TICKET_SERIALNO,GAME_INSTANCE_ID,PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,ENTRY_ID,CREATE_TIME
) values(
    'W1', 1, 'T-123456','GII-111',3,2,1,'E1',sysdate
);
insert into TT_WINNING(
    ID, VERSION, TICKET_SERIALNO,GAME_INSTANCE_ID,PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,ENTRY_ID,CREATE_TIME
) values(
    'W2', 1, 'T-123456','GII-111',4,4,1,'E1',sysdate
);

--TT_WINNING_STATISTICS
insert into TT_WINNING_STATISTICS(
    ID,GAME_INSTANCE_ID, VERSION,PRIZE_LEVEL,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_NUMBER,CREATE_TIME
) values(
    'WS1','GII-111',1,3,3000.0,300.0,2701.0,22,sysdate
);
insert into TT_WINNING_STATISTICS(
    ID,GAME_INSTANCE_ID, VERSION,PRIZE_LEVEL,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_NUMBER,CREATE_TIME
) values(
    'WS2','GII-111',1,4,4000.0,400.0,3601.0,231,sysdate
);

-- ============================================================== --
-- BD_PRIZE_GROUP_ITEM                                            --
-- ============================================================== --
-- prize type definition: 1,cash; 2,object, 3:cash+object
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-1', 'BPG-1', '1', 5, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-2', 'BPG-1', '2', 5, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-3', 'BPG-1', '3', 5, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-4', 'BPG-1', '4',5, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-5', 'BPG-1', '5',5, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-6', 'BPG-1', '6',5, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-11', 'BPG-1', '1', 5, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-12', 'BPG-1', '2', 5, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-13', 'BPG-1', '3', 5, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-14', 'BPG-1', '4',5, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-15', 'BPG-1', '5',5, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'TOTO-16', 'BPG-1', '6',5, 2
);


-------------------------------------------------------------------
-- ENCRYPTION
------------------------------------------------------------------- 
update TE_TT_TICKET set SERIAL_NO='nAmMGjJjE2Fpdde2k1iZRw==' where SERIAL_NO='T-123456';
update TE_TOTO_ENTRY set TICKET_SERIAL_NO='nAmMGjJjE2Fpdde2k1iZRw==' where TICKET_SERIAL_NO='T-123456';
update PAYOUT set TICKET_SERIALNO='nAmMGjJjE2Fpdde2k1iZRw==' where TICKET_SERIALNO='T-123456';
update TE_TRANSACTION set TICKET_SERIAL_NO='nAmMGjJjE2Fpdde2k1iZRw==' where TICKET_SERIAL_NO='T-123456';
update TT_WINNING set TICKET_SERIALNO='nAmMGjJjE2Fpdde2k1iZRw==' where TICKET_SERIALNO='T-123456';

commit;
