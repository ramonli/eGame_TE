delete from GAME_TYPE where GAME_TYPE_ID=14;
delete from GAME where GAME_TYPE_ID=14;
delete from GAME_MERCHANT where ID like 'GM-RA-%';
delete from TE_TRANSACTION where ID like 'TRANS-RA-%';
delete from RA_OPERATION_PARAMETERS;
delete from RA_GAME_INSTANCE;   
delete from RA_TE_TICKET;
delete from RA_WINNING_OBJECT;
delete from BD_PRIZE_GROUP_ITEM where ID like 'BPGI-RA-%';
delete from MERCHANT_GAME_PROPERTIES where MRID like '%-RA-%'; 

-- GAME_TYPE
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(14, 'Raffle');

-- GAME
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'RA-1', 14, null, 'RA-OP-1','TP-1',2,'RA-1',1,1
);
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,GAME_NAME,TAX_CALCULATION_METHOD,
    STATUS,WINNER_TAX_POLICY_ID,TAX_CALCULATION_BASED
) values(
    'RA-2', 14, null, 'RA-OP-1','RA-2',1,1,'TP-1',1
);

-- OPERATION_PARAMETERS
insert into RA_OPERATION_PARAMETERS(
    ID, BASE_AMOUNT,PAYOUT_MODEL,BANKER, MULTIPLE, ALLOW_CANCELLATION,MIN_MULTI_DRAW,MAX_MULTI_DRAW
) values(
    'RA-OP-1', 100.0, 1,1,1,1,1,10
);

-- GAME_MERCHANT 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-RA-111', 111, 'RA-1',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-RA-113', 111, 'RA-2',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-RA-114', 222, 'RA-1',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-RA-116', 222, 'RA-2',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-RA-117', 112, 'RA-2',0.0,0.0
);

-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-RA-111', 111, 'RA-1','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-RA-113', 111, 'RA-1','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-RA-114', 222, 'RA-2','OPERATOR-112',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-RA-116', 222, 'RA-2','OPERATOR-112',0.0,0.0
);


-- GAME INSTANCE
insert into RA_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-111','OPL-1','RA-1','draw-closed','11001',
    sysdate-1, sysdate-3, sysdate-1,0,7,3650,1,sysdate-1+30/(24*60),0,2, 0.4, 15000
);
insert into RA_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-112','OPL-1','RA-1','draw-active','11002',
    sysdate+1, sysdate-1, sysdate+1,0,2,3650,1,sysdate+1+30/(24*60),0,2, 0.4, 15000
);
insert into RA_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-113','OPL-1','RA-1','draw-new','11003',
    sysdate+3, sysdate+1, sysdate+3,0,1,3650,1,sysdate+3+30/(24*60),0,2, 0.4, 15000
);
insert into RA_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-512','OPL-1','RA-2','draw-active','51002',
    sysdate+7, sysdate+1, sysdate+7,0,2,3650,1,sysdate+7+30/(24*60),0,2, 0.4, 15000
);
update RA_GAME_INSTANCE set IS_SUSPEND_SALE=0;
update RA_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=0;

-- TE_TRANSACTION
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID,TOTAL_AMOUNT,VERSION
) values(
    'TRANS-RA-1','OPERATOR-111','GPE-111',111,111,sysdate-3+4/24,
    200,sysdate-3+4/24,200,'S-123456','TMI-091','20092009','RA-1',1400,0
);  

-- LFN_TE_TICKET  
insert into RA_TE_TICKET( 
    ID,RA_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '1','GII-111','TRANS-RA-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,1,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,2,1,200,'111111',1
);
insert into RA_TE_TICKET(
    ID,RA_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '2','GII-112','TRANS-RA-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,0,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,0,1,200,'111111',0
); 
 
update RA_TE_TICKET set IS_BLOCK_PAYOUT=0;
update RA_TE_TICKET set dev_id=111;
update RA_TE_TICKET set merchant_id=111;
update RA_TE_TICKET set operator_id='OPERATOR-111';
update RA_TE_TICKET set ticket_type=1;
update RA_TE_TICKET set TOTAL_BETS=1;  
update RA_TE_TICKET set LD_WINING_TOTAL_BETS=1;  

-------------------------------------------------------------------
-- WINNING
------------------------------------------------------------------- 
insert into RA_WINNING_OBJECT(
    ID, VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,GAME_INSTANCE_ID,PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID
) values (
    'W-1',1,sysdate,sysdate, 'S-123456', 'GII-111', 1,2,1
);
insert into RA_WINNING_OBJECT(
    ID, VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,GAME_INSTANCE_ID,PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID
) values (
    'W-2',1,sysdate,sysdate, 'S-123456', 'GII-111', 4,2,1
);

-- ============================================================== --
-- BD_PRIZE_GROUP_ITEM                                            --
-- ============================================================== --
-- prize type definition: 1,cash; 2,object
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-1', 'BPG-1', '1', 14, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-2', 'BPG-1', '2', 14, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-3', 'BPG-1', '3', 14, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-4', 'BPG-1', '4', 14, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-5', 'BPG-1', '5', 14, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-6', 'BPG-1', '6', 14, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-7', 'BPG-1', '7', 14, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-RA-8', 'BPG-1', '8', 14, 1
);

-------------------------------------------------------------------
-- ENCRYPTION
------------------------------------------------------------------- 
update RA_TE_TICKET set serial_no='9pkxn/npytVqVbOF5fPlsg==' where serial_no='S-123456';

update RA_WINNING_OBJECT set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';  

commit;
