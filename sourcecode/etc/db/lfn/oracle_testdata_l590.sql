delete from GAME_TYPE where GAME_TYPE_ID=15;
delete from GAME where GAME_TYPE_ID=15;
delete from GAME_MERCHANT where ID like 'GM-LFN-%';
delete from TE_TRANSACTION where ID like 'TRANS-LFN-%';
delete from LFN_OPERATION_PARAMETERS;
delete from LFN_FUN_TYPE;
delete from LFN_GAME_INSTANCE;   
delete from LFN_TE_ENTRY;
delete from LFN_TE_TICKET;
delete from LFN_WINNING;
delete from BD_PRIZE_GROUP_ITEM where ID like 'BPGI-LFN-%';
delete from BD_PRIZE_LEVEL_ITEM where ID like 'PL-LFN-%';
delete from BD_PRIZE_LEVEL where ID like 'PL-LFN-%';
delete from BD_RISK_BETTING where ID like 'LFN-%';
delete from MERCHANT_GAME_PROPERTIES where MRID like '%-LFN-%';  

-- GAME_TYPE
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(15, 'L590');

-- OPERATION_PARAMETERS
insert into LFN_OPERATION_PARAMETERS(
    ID, BASE_AMOUNT,PAYOUT_MODEL,BANKER, MULTIPLE, ALLOW_CANCELLATION,MIN_MULTI_DRAW,MAX_MULTI_DRAW
) values(
    'LFN-OP-1', 100.0,1,1,1,1,1,10
);

-- OPERATION_PARAMETERS
insert into LFN_FUN_TYPE(LFT_ID, KKK,NNN,XXX,YYY) values('FUN-1', 5, 90,0,0);

-- GAME
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'LFN-1', 15, 'FUN-1', 'LFN-OP-1','TP-1',1,'LFN-1',1,1
);
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,GAME_NAME,TAX_CALCULATION_METHOD,
    STATUS,WINNER_TAX_POLICY_ID,TAX_CALCULATION_BASED
) values(
    'LFN-2', 15, 'FUN-1', 'LFN-OP-1','LFN-2',1,1,'TP-1',1
);

-- GAME_MERCHANT 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LFN-111', 111, 'LFN-1',0.2,0.1
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LFN-113', 111, 'LFN-2',0.2,0.1
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LFN-114', 222, 'LFN-1',0.3,0.2
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LFN-116', 222, 'LFN-2',0.3,0.2
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LFN-117', 112, 'LFN-2',0.3,0.2
);

-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LFN-111', 111, 'LFN-1','OPERATOR-111',0.2,0.1
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LFN-113', 111, 'LFN-2','OPERATOR-111',0.2,0.1
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LFN-114', 222, 'LFN-1','OPERATOR-112',0.3,0.25
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LFN-116', 222, 'LFN-2','OPERATOR-112',0.3,0.25
);


-- GAME INSTANCE
insert into LFN_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,PAYOUT_START_TIME,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-111','PL-LFN-111','LFN-1','payout-started game instance','11001',
    sysdate-1, sysdate-3, sysdate-1,0,7,3650,1,sysdate-1+30/(24*60),0,sysdate-1+1/24,
    1, 0.4, 80000
);
insert into LFN_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-112','PL-LFN-111','LFN-1','active game instance','11002',
    sysdate+1, sysdate-1, sysdate+1,0,2,3650,0,sysdate+1+30/(24*60),0,
    1, 0.4, 80000
);
insert into LFN_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-113','PL-LFN-111','LFN-1','new game instance','11003',
    sysdate+3, sysdate+1, sysdate+3,0,1,3650,0,sysdate+3+30/(24*60),0,
    1, 0.4, 80000
);
insert into LFN_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-114','PL-LFN-111','LFN-1','new game instance','11004',
    sysdate+5, sysdate+3, sysdate+5,0,1,3650,0,sysdate+5+30/(24*60),0,
    1, 0.4, 80000
);
insert into LFN_GAME_INSTANCE(
    ID,BD_PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-115','PL-LFN-111','LFN-1','new game instance','11005',
    sysdate+7, sysdate+5, sysdate+7,0,1,3650,0,sysdate+7+30/(24*60),0,
    1, 0.4, 80000
);
update LFN_GAME_INSTANCE set IS_SUSPEND_SALE=0;
update LFN_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=0;

-- TE_TRANSACTION
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID
) values(
    'TRANS-LFN-1','OPERATOR-111','GPE-111',111,111,sysdate-3+4/24,
    200,sysdate-3+4/24,200,'S-123456','TMI-091','20092009','LFN-1'
);  
update TE_TRANSACTION set VERSION=0 where ID like 'TRANS-LFN-%';

-- LFN_TE_TICKET  
insert into LFN_TE_TICKET( 
    ID,LFN_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,EXTEND_TEXT,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '1','GII-111','TRANS-LFN-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,1,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,3,'2b514a0d4a859c5d6c5898f66e12f78a',1,200,
    '111111',1
);
insert into LFN_TE_TICKET(
    ID,LFN_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,EXTEND_TEXT,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '2','GII-112','TRANS-LFN-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,0,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,0,'2b514a0d4a859c5d6c5898f66e12f78a',1,200,
    '111111',0
); 
insert into LFN_TE_TICKET(
    ID,LFN_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,EXTEND_TEXT,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '3','GII-113','TRANS-LFN-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,0,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,0,'2b514a0d4a859c5d6c5898f66e12f78a',1,200,
    '111111',0
); 
update LFN_TE_TICKET set IS_BLOCK_PAYOUT=0;
update LFN_TE_TICKET set dev_id=111;
update LFN_TE_TICKET set merchant_id=111;
update LFN_TE_TICKET set operator_id='OPERATOR-111';
update LFN_TE_TICKET set ticket_type=1;
update LFN_TE_TICKET set TOTAL_BETS=1;  
update LFN_TE_TICKET set LD_WINING_TOTAL_BETS=1;  

-- LFN ENTRY
insert into LFN_TE_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT
) values(
    'E-1',0,sysdate-3+4/24,1,'S-123456',1,'1,2,3,4,5',2,1,100
);
insert into LFN_TE_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT
) values(
    'E-2',0,sysdate-3+4/24,2,'S-123456',55,'1,2,3,4,5,6',2,6,100
);   

-- WINNING DEFINITION
insert into LFN_WINNING(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,LFN_GAME_INSTANCE_ID,ENTRY_ID,
    PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'W-1',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-1','2',1,1,
    20000.0,2000.0,18000.0
);
insert into LFN_WINNING(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,LFN_GAME_INSTANCE_ID,ENTRY_ID,
    PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'W-2',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-2','5',2,1,2000.0,100.0,1900.0
);
insert into LFN_WINNING(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,LFN_GAME_INSTANCE_ID,ENTRY_ID,
    PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'W-3',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-2','7',4,1,100.0,0.0,100.0
); 

-- ============================================================== --
-- BD_RISK_BETTING                                                --
-- ============================================================== --
--insert into BD_RISK_BETTING(ID,BETTING_NUMBER,GAME_INSTANCE_ID,TOTAL_AMOUNT) values('LFN-1', '5', 'GII-112', 100);

-- ============================================================== --
-- BD_PRIZE_GROUP_ITEM                                            --
-- ============================================================== --
-- prize type definition: 1,cash; 2,object
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-1', 'BPG-1', '1', 15, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-2', 'BPG-1', '2', 15, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-3', 'BPG-1', '3', 15, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-4', 'BPG-1', '4', 15, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-5', 'BPG-1', '5', 15, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-6', 'BPG-1', '6', 15, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-7', 'BPG-1', '7', 15, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-LFN-8', 'BPG-1', '8', 15, 1
);

-- ============================================================== --
-- BD_PRIZE_LEVEL                                                 --
-- ============================================================== --
insert into bd_prize_level(
    ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME
) values(
    'PL-LFN-1', 'PL-LFN-111', 1, 'N1'
);
insert into bd_prize_level(
    ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME
) values(
    'PL-LFN-2', 'PL-LFN-111', 2, 'N2'
);
insert into bd_prize_level(
    ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME
) values(
    'PL-LFN-3', 'PL-LFN-111', 3, 'N3'
);
insert into bd_prize_level(
    ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME
) values(
    'PL-LFN-4', 'PL-LFN-111', 4, 'N4'
);
insert into bd_prize_level(
    ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME
) values(
    'PL-LFN-5', 'PL-LFN-111', 5, 'N5'
);

insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT) values('PL-LFN-1', 'PL-LFN-1', 50);
insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT) values('PL-LFN-2', 'PL-LFN-2', 40);
insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT) values('PL-LFN-3', 'PL-LFN-3', 30);
insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT) values('PL-LFN-4', 'PL-LFN-4', 20);
insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT) values('PL-LFN-5', 'PL-LFN-5', 10);

-------------------------------------------------------------------
-- ENCRYPTION
------------------------------------------------------------------- 
update lfn_te_ticket set serial_no='9pkxn/npytVqVbOF5fPlsg==' where serial_no='S-123456';

update lfn_te_entry set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';

update te_transaction set TICKET_SERIAL_NO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIAL_NO='S-123456';

update LFN_WINNING set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';    

commit;
