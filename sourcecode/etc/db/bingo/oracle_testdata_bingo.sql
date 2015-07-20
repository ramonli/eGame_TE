delete from GAME_TYPE where GAME_TYPE_ID=6;
delete from GAME where GAME_TYPE_ID=6;
delete from GAME_MERCHANT where ID like 'GM-BINGO-%';
delete from TE_TRANSACTION where ID like 'TRANS-BINGO-%';
delete from BG_OPERATION_PARAMETERS;
delete from BG_FUN_TYPE;
delete from BG_GAME_INSTANCE;   
delete from BG_ENTRY_REF;
delete from BG_TICKET_REF;
delete from TE_BG_ENTRY;
delete from TE_BG_TICKET;
delete from BG_WINNING;
delete from BG_WINNING_STATISTICS;
delete from BD_PRIZE_GROUP_ITEM where ID like 'BPGI-BINGO-%';
delete from BD_PRIZE_LEVEL_ITEM where ID like 'PL-BINGO-%';
delete from BD_PRIZE_LEVEL where ID like 'PL-BINGO-%';
delete from BG_WINNING where ID like 'W-%';
delete from BG_WINNING_STATISTICS where ID like 'WS-%';
delete from BG_WINNING_LUCKY where ID like 'BWL-%';
delete from BG_LUCKY_PRIZE_RESULT where ID like 'BLPR-%';
delete from MERCHANT_GAME_PROPERTIES where MRID like '%-BINGO-%';  



-- ============================================================== --
-- GAME_TYPE                                                      --
-- ============================================================== -- 
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(6, 'BINGO');

-- ============================================================== --
-- BG_OPERATION_PARAMETER                                         --
-- ============================================================== -- 
insert into BG_OPERATION_PARAMETERS(
    PARAMETERS_ID, BASE_AMOUNT,PAYOUT_MODEL,BANKER, MULTIPLE, ALLOW_CANCELLATION,MIN_MULTI_DRAW,MAX_MULTI_DRAW
) values(
    'BINGO-OP-1', 100.0,1,1,1,1,1,10
);

-- ============================================================== --
-- BG_FUN_TYPE                                                    --
-- ============================================================== -- 
insert into BG_FUN_TYPE(LFT_ID, KKK,NNN,XXX,YYY, ENTRY_COUNT) values('FUN-1', 15, 90,0,0, 3);

-- ============================================================== --
-- GAME                                                           --
-- ============================================================== -- 
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'BINGO-1', 6, 'FUN-1', 'BINGO-OP-1','TP-1',1,'BINGO-1',1,1
);
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,GAME_NAME,TAX_CALCULATION_METHOD,
    STATUS,WINNER_TAX_POLICY_ID,TAX_CALCULATION_BASED
) values(
    'BINGO-2', 6, 'FUN-1', 'BINGO-OP-1','BINGO-2',1,1,'TP-1',1
);

-- ============================================================== --
-- GAME_MERCHANT                                                  --
-- ============================================================== -- 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-BINGO-111', 111, 'BINGO-1',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-BINGO-113', 111, 'BINGO-2',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-BINGO-114', 222, 'BINGO-1',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-BINGO-116', 222, 'BINGO-2',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-BINGO-117', 112, 'BINGO-2',0.0,0.0
);

-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-BINGO-111', 111, 'BINGO-1','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-BINGO-113', 111, 'BINGO-2','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-BINGO-114', 222, 'BINGO-1','OPERATOR-112',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-BINGO-116', 222, 'BINGO-2','OPERATOR-112',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-BINGO-117', 112, 'BINGO-2','OPERATOR-112',0.0,0.0
);

-- ============================================================== --
-- BG_GAME_INSTANCE                                               --
-- ============================================================== -- 
insert into BG_GAME_INSTANCE(
    ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT,BG_LUCKY_PRIZE_LOGIC_ID,START_NUMBER_SEQ, END_NUMBER_SEQ,CURRENT_SEQUENCE
) values(
    'GII-111','PL-BINGO-111','BINGO-1','payout-started game instance','11001',
    sysdate-1, sysdate-3, sysdate-1,0,7,3650,1,sysdate-1+30/(24*60),0, 2, 0.4, 15000,'PL-BINGO-111',1,5,3
);
insert into BG_GAME_INSTANCE(
    ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT,START_NUMBER_SEQ, END_NUMBER_SEQ,CURRENT_SEQUENCE
) values(
    'GII-112','PL-BINGO-111','BINGO-1','active game instance','11002',
    sysdate+1, sysdate-1, sysdate+1,0,2,3650,0,sysdate+1+30/(24*60),0,
    2, 0.4, 15000,1,5,3
);
insert into BG_GAME_INSTANCE(
    ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT,START_NUMBER_SEQ, END_NUMBER_SEQ,CURRENT_SEQUENCE
) values(
    'GII-113','PL-BINGO-111','BINGO-1','new game instance-1','11003',
    sysdate+3, sysdate+1, sysdate+3,0,1,3650,0,sysdate+3+30/(24*60),0,
    2, 0.4, 15000,1,5,3
);
insert into BG_GAME_INSTANCE(
    ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT,START_NUMBER_SEQ, END_NUMBER_SEQ,CURRENT_SEQUENCE
) values(
    'GII-114','PL-BINGO-111','BINGO-1','new game instance-2','11004',
    sysdate+5, sysdate+3, sysdate+5,0,1,3650,0,sysdate+5+30/(24*60),0,
    2, 0.4, 15000,1,5,3
);
insert into BG_GAME_INSTANCE(
    ID,PRIZE_LOGIC_ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,
    DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,
    VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,CONTORL_METHOD,
    SALES_AMOUNT_PERCENT,LOSS_AMOUNT,START_NUMBER_SEQ, END_NUMBER_SEQ,CURRENT_SEQUENCE
) values(
    'GII-115','PL-BINGO-111','BINGO-1','new game instance-3','11005',
    sysdate+7, sysdate+5, sysdate+7,0,1,3650,0,sysdate+7+30/(24*60),0,
    2, 0.4, 15000,1,5,3
);
update BG_GAME_INSTANCE set IS_SUSPEND_SALE=0;
update BG_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=0;

-- ============================================================== --
-- BG_TICKET_REF                                                  --
-- ============================================================== --     
insert into BG_TICKET_REF(
    ID, BG_GAME_INSTANCE_ID, SERIAL_NO, TOTAL_AMOUNT, STATUS,PIN, BOOK, SERIAL,CREATE_TIME, UPDATE_TIME,SEQUENCE_NUMBER
) values(
    'T-1','GII-112','01110394646910962176',300, 2, '98abe3a28383501f4bfd2d9077820f11','B1','S11',sysdate,sysdate,1
);
insert into BG_TICKET_REF(
    ID, BG_GAME_INSTANCE_ID, SERIAL_NO, TOTAL_AMOUNT, STATUS,PIN, BOOK, SERIAL,CREATE_TIME, UPDATE_TIME,SEQUENCE_NUMBER
) values(
    'T-2','GII-112','01110394646910962214',300, 3, '98abe3a28383501f4bfd2d9077820f11','B1','S12',sysdate,sysdate,2
);
insert into BG_TICKET_REF(
    ID, BG_GAME_INSTANCE_ID, SERIAL_NO, TOTAL_AMOUNT, STATUS,PIN, BOOK, SERIAL,CREATE_TIME, UPDATE_TIME,SEQUENCE_NUMBER
) values(
    'T-3','GII-112','01110394646910962353',300, 1, '98abe3a28383501f4bfd2d9077820f11','B1','S13',sysdate,sysdate,3
);
insert into BG_TICKET_REF(
    ID, BG_GAME_INSTANCE_ID, SERIAL_NO, TOTAL_AMOUNT, STATUS,PIN, BOOK, SERIAL,CREATE_TIME, UPDATE_TIME,SEQUENCE_NUMBER
) values(
    'T-4','GII-112','01110394646910962438',300, 1, '98abe3a28383501f4bfd2d9077820f11','B1','S14',sysdate,sysdate,4
);
insert into BG_TICKET_REF(
    ID, BG_GAME_INSTANCE_ID, SERIAL_NO, TOTAL_AMOUNT, STATUS,PIN, BOOK, SERIAL,CREATE_TIME, UPDATE_TIME,SEQUENCE_NUMBER
) values(
    'T-5','GII-112','01110394646910962572',300, 1, '98abe3a28383501f4bfd2d9077820f11','B1','S15',sysdate,sysdate,5
);

-- ============================================================== --
-- BG_ENTRY_REF                                                   --
-- ============================================================== --   
insert into BG_ENTRY_REF(
    ID,BG_GAME_INSTANCE_ID, SELECTED_NUMBER, STATUS, CREATE_TIME,UPDATE_TIME
) values(
    'E-1','GII-112','1,2,3,4,5,6,7,8,9,10,11,12,13,14,15',2,sysdate,sysdate
);
insert into BG_ENTRY_REF(
    ID,BG_GAME_INSTANCE_ID, SELECTED_NUMBER, STATUS, CREATE_TIME,UPDATE_TIME
) values(
    'E-2','GII-112','11,12,13,14,15,16,17,18,19,20,21,22,23,24,25',3,sysdate,sysdate
);

insert into BG_ENTRY_REF(
    ID,BG_GAME_INSTANCE_ID, SELECTED_NUMBER, STATUS, CREATE_TIME,UPDATE_TIME
) values(
    'E-3','GII-112','21,22,23,24,25,26,27,28,29,30,31,32,33,34,35',1,sysdate,sysdate
);

insert into BG_ENTRY_REF(
    ID,BG_GAME_INSTANCE_ID, SELECTED_NUMBER, STATUS, CREATE_TIME,UPDATE_TIME
) values(
    'E-4','GII-112','31,32,33,34,35,36,37,38,39,40,41,42,43,44,45',1,sysdate,sysdate
);

insert into BG_ENTRY_REF(
    ID,BG_GAME_INSTANCE_ID, SELECTED_NUMBER, STATUS, CREATE_TIME,UPDATE_TIME
) values(
    'E-5','GII-112','41,42,43,44,45,46,47,48,49,50,51,52,53,54,55',1,sysdate,sysdate
);

insert into BG_ENTRY_REF(
    ID,BG_GAME_INSTANCE_ID, SELECTED_NUMBER, STATUS, CREATE_TIME,UPDATE_TIME
) values(
    'E-6','GII-112','51,52,53,54,55,56,57,58,59,60,61,62,63,64,65',1,sysdate,sysdate
);
insert into BG_ENTRY_REF(
    ID,BG_GAME_INSTANCE_ID, SELECTED_NUMBER, STATUS, CREATE_TIME,UPDATE_TIME
) values(
    'E-7','GII-112','61,62,63,64,65,66,67,68,69,70,71,72,73,74,75',1,sysdate,sysdate
);

-- ============================================================== --
-- TE_TRANSACTION                                                 --
-- ============================================================== -- 
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID
) values(
    'TRANS-BINGO-1','OPERATOR-111','GPE-111',111,111,sysdate-3+4/24,
    200,sysdate-3+4/24,200,'S-123456','TMI-091','20092009','BINGO-1'
);  
update TE_TRANSACTION set VERSION=0 where ID like 'TRANS-BINGO-%';

-- ============================================================== --
-- TE_BG_TICKET                                                   --
-- ============================================================== -- 
insert into TE_BG_TICKET( 
    ID,BG_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_COUNT_IN_POOL,
    MUTLI_DRAW,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_LUCKY_DRAW,BARCODE
) values(
    '1','GII-111','TRANS-BINGO-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,1,1,
    'f5e09f731f7dffc2a603a7b9b977b2ca',1,3,1,200,'111111',1,'01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1'
);
insert into TE_BG_TICKET(
    ID,BG_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_COUNT_IN_POOL,
    MUTLI_DRAW,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_LUCKY_DRAW,BARCODE
) values(
    '2','GII-112','TRANS-BINGO-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,0,1,
    'f5e09f731f7dffc2a603a7b9b977b2ca',1,0,1,200,'111111',0,'01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1'
); 
insert into TE_BG_TICKET(
    ID,BG_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_COUNT_IN_POOL,
    MUTLI_DRAW,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_LUCKY_DRAW,BARCODE
) values(
    '3','GII-113','TRANS-BINGO-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',700.0,0,1,
    'f5e09f731f7dffc2a603a7b9b977b2ca',1,0,1,200, '111111',0,'01cK/u9hwZY2Rogq4k24Oeg4G3HSlAl9EbjLdf+UJJlt/ITKo3ngns+4pjWl52Uuv1'
); 
update TE_BG_TICKET set IS_BLOCK_PAYOUT=0;
update TE_BG_TICKET set dev_id=111;
update TE_BG_TICKET set merchant_id=111;
update TE_BG_TICKET set operator_id='OPERATOR-111';
update TE_BG_TICKET set ticket_type=1;
update TE_BG_TICKET set TOTAL_BETS=1;  
update TE_BG_TICKET set LD_WINING_TOTAL_BETS=1;  
update TE_BG_TICKET set IS_OFFLINE=0;

-- ============================================================== --
-- TE_BG_ENTRY                                                    --
-- ============================================================== --
insert into TE_BG_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT
) values(
    'E-1',0,sysdate-3+4/24,1,'S-123456',1,'1,2,3,4,5,6,7,8,9,10,11,12,13,14,15',2,1,100
);
insert into TE_BG_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT
) values(
    'E-2',0,sysdate-3+4/24,2,'S-123456',1,'11,12,13,14,15,16,17,18,19,20,21,22,23,24,25',1,1,100
);   

-- ============================================================== --
-- BG_WINNING                                                     --
-- ============================================================== --
 insert into BG_WINNING(
     ID,VERSION,CREATE_TIME,UPDATE_TIME,SERIAL_NO,BG_GAME_INSTANCE_ID,TE_BG_ENTRY_ID,
     PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,TE_BG_TICKET_ID
 ) values(
     'W-1',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-1',1,1,1,1
 );
 insert into BG_WINNING(
     ID,VERSION,CREATE_TIME,UPDATE_TIME,SERIAL_NO,BG_GAME_INSTANCE_ID,TE_BG_ENTRY_ID,
     PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,TE_BG_TICKET_ID
 ) values(
     'W-2',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-2',5,2,1,1
 );
 insert into BG_WINNING(
     ID,VERSION,CREATE_TIME,UPDATE_TIME,SERIAL_NO,BG_GAME_INSTANCE_ID,TE_BG_ENTRY_ID,
     PRIZE_LEVEL,PRIZE_NUMBER,IS_VALID,TE_BG_TICKET_ID
 ) values(
     'W-3',1,sysdate+3+1/24,sysdate+3+1/24,'S-123456','GII-111','E-2',7,4,1,1
 ); 
 
-- WINNING STATISTICS
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-1',1,sysdate,sysdate,1,1,5000000.0,'GII-111',1000000.0,4000001.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-2',1,sysdate,sysdate,2,2,1000000.0,'GII-112',5000.0,350001.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-3',1,sysdate,sysdate,3,10,500000.0,'GII-111',4000.0,460001.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-4',1,sysdate,sysdate,4,5,50000.0,'GII-112',10000.0,40001.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-5',1,sysdate,sysdate,5,22,10000.0,'GII-111',2000.0,8001.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-6',1,sysdate,sysdate,6,112,7000.0,'GII-111',10.0,6991.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-7',1,sysdate,sysdate,7,433,3000.0,'GII-111',10.0,2991.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-8',1,sysdate,sysdate,7,122,3000.0,'GII-112',10.0,2991.0
);
insert into BG_WINNING_STATISTICS(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,PRIZE_LEVEL,PRIZE_NUMBER,PRIZE_AMOUNT,
    BG_GAME_INSTANCE_ID,TAX_AMOUNT,ACTUAL_PAYOUT
) values(
    'WS-9',1,sysdate,sysdate,8,122,0.0,'GII-112',0.0,0.0
);

-- ============================================================== --
-- BD_PRIZE_GROUP_ITEM                                            --
-- ============================================================== --
-- prize type definition: 1,cash; 2,object
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-1', 'BPG-1', '1', 6, 1
 );
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-2', 'BPG-1', '2', 6, 1
 );
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-3', 'BPG-1', '3', 6, 1
 );
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-4', 'BPG-1', '4', 6, 1
 );
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-5', 'BPG-1', '5', 6, 1
 );
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-6', 'BPG-1', '6', 6, 1
 );
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-7', 'BPG-1', '7', 6, 1
 );
 insert into bd_prize_group_item(
     id, bd_prize_group_id, prize_level,game_type,prize_type
 ) values(
     'BPGI-BINGO-8', 'BPG-1', '8', 6, 1
 );
 
-- ============================================================== --
-- BD_PRIZE_LEVEL                                                 --
-- ============================================================== --
 insert into bd_prize_level(
     ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME,PRIZE_LEVEL_TYPE
 ) values(
     'PL-BINGO-1', 'PL-BINGO-111', 1, 'N1',1
 );
 insert into bd_prize_level(
     ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME,PRIZE_LEVEL_TYPE
 ) values(
     'PL-BINGO-2', 'PL-BINGO-111', 2, 'N2',1
 );
 insert into bd_prize_level(
     ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME,PRIZE_LEVEL_TYPE
 ) values(
     'PL-BINGO-3', 'PL-BINGO-111', 3, 'N3',1
 );
 insert into bd_prize_level(
     ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME,PRIZE_LEVEL_TYPE
 ) values(
     'PL-BINGO-4', 'PL-BINGO-111', 4, 'N4',1
 );
 insert into bd_prize_level(
     ID, BD_PRIZE_LOGIC_ID, PRIZE_LEVEL,PRIZE_NAME,PRIZE_LEVEL_TYPE
 ) values(
     'PL-BINGO-5', 'PL-BINGO-111', 5, 'N5',2
 );
 
 insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT,ACTUAL_PAYOUT,TAX_AMOUNT,ITEM_TYPE,PRIZE_LEVEL_NUM) values('PL-BINGO-1', 'PL-BINGO-1', 50,10,40,1,0);
 insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT,ACTUAL_PAYOUT,TAX_AMOUNT,ITEM_TYPE,PRIZE_LEVEL_NUM) values('PL-BINGO-2', 'PL-BINGO-2', 40,10,30,1,0);
 insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT,ACTUAL_PAYOUT,TAX_AMOUNT,ITEM_TYPE,PRIZE_LEVEL_NUM) values('PL-BINGO-3', 'PL-BINGO-3', 30,10,20,1,0);
 insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT,ACTUAL_PAYOUT,TAX_AMOUNT,ITEM_TYPE,PRIZE_LEVEL_NUM) values('PL-BINGO-4', 'PL-BINGO-4', 20,5,15,1,0);
 insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT,ACTUAL_PAYOUT,TAX_AMOUNT,BD_PRIZE_OBJECT_ID,ITEM_TYPE,PRIZE_LEVEL_NUM,OBJECT_NAME) values('PL-BINGO-5', 'PL-BINGO-5', 10,5,5,'BPO-20',2,1,'Bingo Prize Ticket');
 insert into bd_prize_level_item(ID, BD_PRIZE_LEVEL_ID, PRIZE_AMOUNT,ACTUAL_PAYOUT,TAX_AMOUNT,BD_PRIZE_OBJECT_ID,ITEM_TYPE,PRIZE_LEVEL_NUM,OBJECT_NAME) values('PL-BINGO-6', 'PL-BINGO-5', 10,5,5,'BPO-21',2,1,'Bingo Prize free entry');
 
-------------------------------------------------------------------
-- SECOND PRIZE
------------------------------------------------------------------- 
 insert into BG_WINNING_LUCKY (ID, VERSION, BG_GAME_INSTANCE_ID, LUCKYNO, SERIAL_NO, TE_BG_TICKET_ID, CREATE_TIME, UPDATE_TIME, BOOK, SERIAL, TYPE)
 values ('BWL-1', 1, 'GII-111', '1', 'S-123456', '1', sysdate, sysdate, null, null, 1);
 
 insert into BG_LUCKY_PRIZE_RESULT (ID, BG_GAME_INSTANCE_ID, PRIZE_LEVEL, LUCKYNO, VERSION)
 values ('BLPR-1', 'GII-111', 3, '1', 1);
 insert into BG_LUCKY_PRIZE_RESULT (ID, BG_GAME_INSTANCE_ID, PRIZE_LEVEL, LUCKYNO, VERSION)
 values ('BLPR-2', 'GII-111', 4, '1', 1);
 insert into BG_LUCKY_PRIZE_RESULT (ID, BG_GAME_INSTANCE_ID, PRIZE_LEVEL, LUCKYNO, VERSION)
 values ('BLPR-3', 'GII-111', 5, '1', 1);
-------------------------------------------------------------------
-- ENCRYPTION
------------------------------------------------------------------- 
update TE_BG_TICKET set SERIAL_NO='9pkxn/npytVqVbOF5fPlsg==' where SERIAL_NO='S-123456';

update TE_BG_ENTRY set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';

update TE_TRANSACTION set TICKET_SERIAL_NO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIAL_NO='S-123456';

update BG_WINNING set SERIAL_NO='9pkxn/npytVqVbOF5fPlsg==' where SERIAL_NO='S-123456';  

update BG_WINNING_LUCKY set SERIAL_NO='9pkxn/npytVqVbOF5fPlsg==' where SERIAL_NO='S-123456';    

commit;
