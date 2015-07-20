delete from GAME_TYPE where GAME_TYPE_ID=7;
delete from GAME where GAME_TYPE_ID=7;
delete from GAME_MERCHANT where ID like 'GM-FD-%';
delete from TE_TRANSACTION where ID like 'TRANS-FD-%';
delete from FD_OPERATION_PARAMETERS;
delete from FD_FUN_TYPE;
delete from FD_GAME_INSTANCE;   
delete from TE_FD_ENTRY;
delete from TE_FD_TICKET;
delete from FD_WINNING;
delete from FD_WINNING_STATISTICS;
delete from BD_PRIZE_GROUP_ITEM where ID like 'BPGI-FD-%';
delete from PRIZE_PARAMETERS where parameter_id like 'DIG-%';
delete from PRIZE_LOGIC where PRIZE_LOGIC_ID like 'PL-DIG-%';
delete from MERCHANT_GAME_PROPERTIES where MRID like '%-FD-%';  

-- GAME_TYPE
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(7, 'Digital');

-- OPERATION_PARAMETERS
insert into FD_OPERATION_PARAMETERS(ID, BASE_AMOUNT,PAYOUT_MODEL,IS_ODD_EVEN,MAX_BASE_AMOUNT,BANKER,
    MULTIPLE,ALLOW_CANCELLATION,IS_SUPPORT_SUM,MIN_MULTI_DRAW,MAX_MULTI_DRAW
) values(
    'FD-OP-1', 50.0,1,1,5000,1,1,1,1,1,10
);

-- OPERATION_PARAMETERS
insert into FD_FUN_TYPE(LFT_ID, KKK,NNN,XXX,YYY) values('FD-FUN-1', 1, 4, 0, 9);

-- GAME
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'FD-1', 7, 'FD-FUN-1', 'FD-OP-1','TP-1',1,'FD-1',1,1
);
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'FD-2', 7, 'FD-FUN-1', 'FD-OP-1','TP-1',1,'FD-1',1,1
);

-- GAME_MERCHANT 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-FD-111', 111, 'FD-1',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-FD-113', 111, 'FD-2',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-FD-114', 222, 'FD-1',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-FD-116', 222, 'FD-2',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-FD-117', 112, 'FD-2',0.0,0.0
);


-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-FD-111', 111, 'FD-1','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-FD-113', 111, 'FD-1','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-FD-114', 222, 'FD-2','OPERATOR-112',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-FD-116', 222, 'FD-2','OPERATOR-112',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-FD-117', 112, 'FD-3','OPERATOR-112',0.0,0.0
);

-- GAME INSTANCE
insert into FD_GAME_INSTANCE(
    GAME_INSTANCE_ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-111','PL-DIG-111','FD-1','payout-started game instance','11001',
    sysdate-1, sysdate-3, sysdate-1,0,7,3650,1,sysdate-1+30/(24*60),0,1, 0.4, 150000
);
insert into FD_GAME_INSTANCE(
    GAME_INSTANCE_ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-112','PL-DIG-111','FD-1','active game instance','11002',
    sysdate+1, sysdate-1, sysdate+1,0,2,3650,0,sysdate+1+30/(24*60),0,1, 0.4, 150000
);
insert into FD_GAME_INSTANCE(
    GAME_INSTANCE_ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-113','PL-DIG-111','FD-1','new game instance','11003',
    sysdate+3, sysdate+1, sysdate+3,0,1,3650,0,sysdate+3+30/(24*60),0,1, 0.4, 150000
);
insert into FD_GAME_INSTANCE(
    GAME_INSTANCE_ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-114','PL-DIG-111','FD-1','new game instance','11004',
    sysdate+5, sysdate+3, sysdate+5,0,1,3650,0,sysdate+5+30/(24*60),0,1, 0.4, 150000
);
insert into FD_GAME_INSTANCE(
    GAME_INSTANCE_ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-115','PL-DIG-111','FD-1','new game instance','11005',
    sysdate+7, sysdate+5, sysdate+7,0,1,3650,0,sysdate+7+30/(24*60),0,1, 0.4, 150000
);
update FD_GAME_INSTANCE set IS_SUSPEND_SALE=0;
update FD_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=0;

-- TE_TRANSACTION
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID
) values(
    'TRANS-FD-1','OPERATOR-111','GPE-111',111,111,sysdate-3+4/24,
    200,sysdate-3+4/24,200,'S-123456','TMI-091','20092009','FD-1'
);  
update TE_TRANSACTION set VERSION=0 where ID like 'TRANS-FD-%';

-- LFN_TE_TICKET  
-- setup a winning ticket 'S-123456'
insert into TE_FD_TICKET( 
    ID,GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,EXTEND_TEXT,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '1','GII-111','TRANS-FD-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,1,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,3,'2b514a0d4a859c5d6c5898f66e12f78a',1,200,
    '111111',0
);
insert into TE_FD_TICKET(
    ID,GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,EXTEND_TEXT,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '2','GII-112','TRANS-FD-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,0,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,0,'2b514a0d4a859c5d6c5898f66e12f78a',1,200,
    '111111',1
); 
insert into TE_FD_TICKET(
    ID,GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,EXTEND_TEXT,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '3','GII-113','TRANS-FD-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,0,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,0,'2b514a0d4a859c5d6c5898f66e12f78a',1,200,
    '111111',0
); 
update TE_FD_TICKET set IS_BLOCK_PAYOUT=0;
update TE_FD_TICKET set dev_id=111;
update TE_FD_TICKET set merchant_id=111;
update TE_FD_TICKET set operator_id='OPERATOR-111';
update TE_FD_TICKET set ticket_type=1;
update TE_FD_TICKET set TOTAL_BETS=1;
update TE_FD_TICKET set LD_WINING_TOTAL_BETS=1;

-- LFN ENTRY
insert into TE_FD_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT
) values(
    'E-1',0,sysdate-3+4/24,1,'S-123456',3,'3,0,0',2,1,100
);
insert into TE_FD_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT
) values(
    'E-2',0,sysdate-3+4/24,2,'S-123456',4,'3,8,0,4',2,1,100
);   

-- WINNING DEFINITION
insert into FD_WINNING(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,GAME_INSTANCE_ID,ENTRY_ID,
    PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'W-1',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-1',2,1,1,
    20000.0,2000.0,18000.0
);
insert into FD_WINNING(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,GAME_INSTANCE_ID,ENTRY_ID,
    PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'W-2',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-2',5,2,1,
    2000.0,100.0,1900.0
);
insert into FD_WINNING(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,TICKET_SERIALNO,GAME_INSTANCE_ID,ENTRY_ID,
    PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,PRIZE_AMOUNT,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'W-3',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-2',7,4,1,
    100.0,0.0,100.0
);

-- WINNING STATISTICS
insert into FD_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-1',1,sysdate,sysdate,1,1,5000000.0,'GII-111',1000000.0,4000001.0
);
insert into FD_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-2',1,sysdate,sysdate,2,2,20000.0,'GII-111',2000.0,18000.0
);
insert into FD_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-3',1,sysdate,sysdate,3,10,500000.0,'GII-111',4000.0,460001.0
);
insert into FD_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-4',1,sysdate,sysdate,4,5,50000.0,'GII-111',10000.0,40001.0
);
insert into FD_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-5',1,sysdate,sysdate,5,5,2000.0,'GII-111',100.0,1900.0
);
insert into FD_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-6',1,sysdate,sysdate,6,5,50000.0,'GII-111',10000.0,40001.0
);
insert into FD_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-7',1,sysdate,sysdate,7,5,100.0,'GII-111',0.0,100.0
);

-- ============================================================== --
-- BD_PRIZE_GROUP_ITEM                                            --
-- ============================================================== --
-- prize type definition: 1,cash; 2,object
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-1', 'BPG-1', '1', 7, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-2', 'BPG-1', '2', 7, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-3', 'BPG-1', '3', 7, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-4', 'BPG-1', '4', 7, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-5', 'BPG-1', '5', 7, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-6', 'BPG-1', '6', 7, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-7', 'BPG-1', '7', 7, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'BPGI-FD-8', 'BPG-1', '8', 7, 1
);

-- ============================================================== --
-- PRIZE_LOGIC                                                    --
-- ============================================================== --
-- Definition of Algorithm_ID:
--  21 DigitGame(Type C)
--  22 DigitGame(Sum)
--  20 DigitGame(Type B)
--  26 DigitGame(Type E)
--  7 DigitGame(Type A)
insert into PRIZE_LOGIC(
    PRIZE_LOGIC_ID, ALGORITHM_ID,SUM_PRIZE_LOGIC_ID,prize_logic_name
) values(
    'PL-DIG-111', '21', 'PL-DIG-SUM-111','Digital Prize Logic'
);
insert into PRIZE_LOGIC(
    PRIZE_LOGIC_ID, ALGORITHM_ID,SUM_PRIZE_LOGIC_ID,prize_logic_name
) values(
    'PL-DIG-E-111', '26', 'PL-DIG-SUM-111','Digital Prize Logic(TypeE)'
);
insert into PRIZE_LOGIC(PRIZE_LOGIC_ID,prize_logic_name) values('PL-DIG-SUM-111','Digital Sum PrizeLogic');

-- ============================================================== --
-- PRIZE_PARAMETERS                                               --
-- ============================================================== --
-- bet_option means 'from left to right'/'from right to left'
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-1','PL-DIG-111', '41',50,4,1, 'First 4D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-2','PL-DIG-111', '31',30,3,1, 'First 3D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-3','PL-DIG-111', '32',30,3,1, 'Last 3D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-4','PL-DIG-111', '21',20,2,1, 'First 2D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-5','PL-DIG-111', '22',20,2,1, 'Last 2D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-6','PL-DIG-111', '11',10,1,1, 'First 1D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-7','PL-DIG-111', '12',10,1,1, 'Last 1D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-8','PL-DIG-111', '-1',15,-1,1, 'ODD'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-9','PL-DIG-111', '-2',25,-2,1, 'EVEN'
);
-- for TYPE E
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-E1','PL-DIG-E-111', '6',50,6,1, '6D'
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable,DESCRIPTION
) values(
    'DIG-E2','PL-DIG-E-111', '5',30,6,1, '2D and Above till 6D'
);
-- for sum betopion
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-11','PL-DIG-SUM-111', '0 or 36',50,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-12','PL-DIG-SUM-111', '1 or 35',40,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-13','PL-DIG-SUM-111', '2 or 34',30,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-14','PL-DIG-SUM-111', '3 or 33',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-15','PL-DIG-SUM-111', '4 or 32',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-16','PL-DIG-SUM-111', '5 or 31',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-17','PL-DIG-SUM-111', '6 or 30',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-18','PL-DIG-SUM-111', '7 or 29',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-19','PL-DIG-SUM-111', '8 or 28',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-20','PL-DIG-SUM-111', '9 or 27',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-21','PL-DIG-SUM-111', '10 or 26',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-22','PL-DIG-SUM-111', '11 or 25',50,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-23','PL-DIG-SUM-111', '12 or 24',20,0,1
);
insert into PRIZE_PARAMETERS(
    parameter_id,prize_logic_id,parameter_name,parameter_value, bet_option,is_enable
) values(
    'DIG-24','PL-DIG-SUM-111', '18',20,0,1
);

-------------------------------------------------------------------
-- ENCRYPTION
------------------------------------------------------------------- 
update TE_FD_TICKET set serial_no='9pkxn/npytVqVbOF5fPlsg==' where serial_no='S-123456';

update TE_FD_ENTRY set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';

update TE_TRANSACTION set TICKET_SERIAL_NO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIAL_NO='S-123456';

update FD_WINNING set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';    

commit;
