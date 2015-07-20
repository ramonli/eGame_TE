delete from GAME_TYPE where GAME_TYPE_ID=18;
delete from GAME where GAME_TYPE_ID=18;
delete from GAME_MERCHANT where ID like '%-LK-%';
delete from MERCHANT_GAME_PROPERTIES where MRID like '%-LK-%';    
delete from LK_OPERATION_PARAMETERS;
delete from LK_TE_ENTRY;  
delete from LK_TE_TICKET;  
delete from LK_GAME_INSTANCE;
delete from LK_PRIZE_PARAMETERS;
delete from LK_PRIZE_STATUS;
delete from TE_TRANSACTION where ID like '%-LUCKY8-%';
delete from LK_REQUEUE_NUMBERS;
delete from LK_REQUEUE_NUMBERS_ITEM;
delete from LK_TE_ENTRY;
delete from LK_TE_TICKET;
delete from LK_OFFLINE_CANCELLATION where ID like '%LOC-%';
delete from LK_OFFLINE_PRIZE_STATUS where ID like '%LOPS-%';


-- GAME_TYPE
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(18, 'Lucky8');

-- OPERATION_PARAMETERS
insert into LK_OPERATION_PARAMETERS(
    ID, BASE_AMOUNT,PAYOUT_MODEL,BANKER, MULTIPLE, ALLOW_CANCELLATION,MIN_MULTI_DRAW,MAX_MULTI_DRAW
) values(
    'LK-OP-1', 100.0,1,1,1,1,1,10
);

-- GAME
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED,NEED_PAYOUT
) values(
    'LK-1', 18, null, 'LK-OP-1','TP-1',1,'Lucky8',1,2,1
);
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED,NEED_PAYOUT
) values(
    'LK-2', 18, null, 'LK-OP-1','TP-1',1,'Lucky9',1,2,1
);

-- GAME_MERCHANT 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LK-111', 111, 'LK-1',0.3,0.4
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LK-112', 222, 'LK-1',0.3,0.4
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LK-1113', 111, 'LK-2',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-LK-114', 222, 'LK-2',0.0,0.0
);

-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LK-111', 111, 'LK-1','OPERATOR-111',0.1,0.2
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LK-113', 111, 'LK-2','OPERATOR-111',0.2,0.2
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LK-114', 222, 'LK-1','OPERATOR-112',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-LK-116', 222, 'LK-2','OPERATOR-112',0.0,0.0
);

-- GAME INSTANCE
insert into LK_GAME_INSTANCE(
    ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,
    IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,
    START_NUMBER_SEQ,END_NUMBER_SEQ,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-111','LK-1','active game instance','001',
    sysdate+100, sysdate-1, sysdate+100,0,2,3650,1,sysdate+100+30/(24*60),0,1,10,2, 0.4, 15000
);
insert into LK_GAME_INSTANCE(
    ID,GAME_ID,GAME_INSTANCE_NAME,DRAW_NO,DRAW_DATE,START_SELLING_TIME,STOP_SELLING_TIME,
    IS_SNOWBALL,STATUS,MAX_CLAIM_PERIOD,VERSION,GAME_FREEZING_TIME,IS_SUSPEND_PAYOUT,
    START_NUMBER_SEQ,END_NUMBER_SEQ,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values(
    'GII-112','LK-2','active game instance','001',
    sysdate+100, sysdate-1, sysdate+100,0,2,3650,1,sysdate+100+30/(24*60),0,1,10,2, 0.4, 15000
);
update LK_GAME_INSTANCE set IS_SUSPEND_SALE=0;
update LK_GAME_INSTANCE set IS_SUSPEND_MANUAL_CANCEL=0;

-- --------------------------------------------------------------------------- --
-- LK_PRIZE_STATUS                                                             --
-- --------------------------------------------------------------------------- --
insert into LK_PRIZE_STATUS(ID, GAME_ID, NEXT_SEQ, LAST_BUYER) values(1,'LK-1', 8, null);

-- --------------------------------------------------------------------------- --
-- LK_PRIZE_PARAMETERS                                                         --
-- --------------------------------------------------------------------------- --  
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '1', 1, '1', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '4', 2, '2', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '7', 3, '3', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '10', 4, '4', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '13', 5, '5', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '17', 6, '6', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '20', 7, '7', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '21', 8, '8', 1000.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '23', 9, '9', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '24', 10, '10', 0.0, 0, 'GII-111', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '117', 6, '6', 0.0, 0, 'GII-112', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '120', 7, '7', 0.0, 0, 'GII-112', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '121', 8, '8', 1000.0, 0, 'GII-112', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '123', 9, '9', 0.0, 0, 'GII-112', sysdate, sysdate
);
insert into LK_PRIZE_PARAMETERS(
    ID, NUMBER_SEQ, LUCKY_NUM, PRICE_AMOUNT, CANCEL_COUNTER, LK_GAME_INSTACE_ID,CREATE_TIME, UPDATE_TIME
) values(
    '124', 10, '10', 0.0, 0, 'GII-112', sysdate, sysdate
);

-- --------------------------------------------------------------------------- --
-- LK_REQUEUE_NUMBERS                                                          --
-- --------------------------------------------------------------------------- --
insert into LK_REQUEUE_NUMBERS(
    ID, TRANS_ID, LK_GAME_INSTACE_ID, NUMBER_SEQ, COUNT_OF_VALID_NUMBER, COUNT_OF_NUMBER, CREATE_TIME, UPDATE_TIME
) values(
    '1', 'nonexist-id-0', 'GII-111', 7, 2, 2, sysdate, sysdate
);
insert into LK_REQUEUE_NUMBERS(
    ID, TRANS_ID, LK_GAME_INSTACE_ID, NUMBER_SEQ, COUNT_OF_VALID_NUMBER, COUNT_OF_NUMBER, CREATE_TIME, UPDATE_TIME
) values(
    '2', 'nonexist-id-1', 'GII-111', 6, 5, 5, sysdate, sysdate
);

-- --------------------------------------------------------------------------- --
-- LK_REQUEUE_NUMBERS_ITEM                                                     --
-- --------------------------------------------------------------------------- --
insert into LK_REQUEUE_NUMBERS_ITEM(
    ID, LK_REQUEUE_NUMBERS_ID, NUMBER_SEQ, LUCKY_NUM, PRIZE_AMOUNT, TAX_AMOUNT, STATE, CREATE_TIME, UPDATE_TIME
) values(
    '1', '1', 7, '7', 0.0, 0.0, 1, sysdate, sysdate
);
insert into LK_REQUEUE_NUMBERS_ITEM(
    ID, LK_REQUEUE_NUMBERS_ID, NUMBER_SEQ, LUCKY_NUM, PRIZE_AMOUNT, TAX_AMOUNT, STATE, CREATE_TIME, UPDATE_TIME
) values(
    '2', '1', 8, '8', 1000.0, 0.0, 1, sysdate, sysdate
);
insert into LK_REQUEUE_NUMBERS_ITEM(
    ID, LK_REQUEUE_NUMBERS_ID, NUMBER_SEQ, LUCKY_NUM, PRIZE_AMOUNT, TAX_AMOUNT, STATE, CREATE_TIME, UPDATE_TIME
) values(
    '7', '2', 6, '6', 0.0, 0.0, 1, sysdate, sysdate
);
insert into LK_REQUEUE_NUMBERS_ITEM(
    ID, LK_REQUEUE_NUMBERS_ID, NUMBER_SEQ, LUCKY_NUM, PRIZE_AMOUNT, TAX_AMOUNT, STATE, CREATE_TIME, UPDATE_TIME
) values(
    '3', '2', 7, '7', 0.0, 0.0, 1, sysdate, sysdate
);
insert into LK_REQUEUE_NUMBERS_ITEM(
    ID, LK_REQUEUE_NUMBERS_ID, NUMBER_SEQ, LUCKY_NUM, PRIZE_AMOUNT, TAX_AMOUNT, STATE, CREATE_TIME, UPDATE_TIME
) values(
    '4', '2', 8, '8', 1000.0, 10.0, 1, sysdate, sysdate
);
insert into LK_REQUEUE_NUMBERS_ITEM(
    ID, LK_REQUEUE_NUMBERS_ID, NUMBER_SEQ, LUCKY_NUM, PRIZE_AMOUNT, TAX_AMOUNT, STATE, CREATE_TIME, UPDATE_TIME
) values(
    '5', '2', 9, '9', 0.0, 0.0, 1, sysdate, sysdate
);
insert into LK_REQUEUE_NUMBERS_ITEM(
    ID, LK_REQUEUE_NUMBERS_ID, NUMBER_SEQ, LUCKY_NUM, PRIZE_AMOUNT, TAX_AMOUNT, STATE, CREATE_TIME, UPDATE_TIME
) values(
    '6', '2', 10, '10', 0.0, 0.0, 1, sysdate, sysdate
);


-- TE_TRANSACTION
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID
) values(
    'TRANS-LK-1','OPERATOR-111','GPE-111',111,111,sysdate-3+4/24,
    200,sysdate-3+4/24,200,'S-123456','TMI-091','20092009','LK-1'
);  
update TE_TRANSACTION set VERSION=0 where ID like 'TRANS-LK-%';

-- LK_TE_TICKET  
insert into LK_TE_TICKET( 
    ID,LK_GAME_INSTANCE_ID,TRANSACTION_ID,VERSION,CREATE_TIME,UPDATE_TIME,
    SERIAL_NO,TOTAL_AMOUNT,IS_WINNING,STATUS,PIN,IS_OFFLINE,IS_COUNT_IN_POOL,
    MUTLI_DRAW,EXTEND_TEXT,TICKET_FROM,TRANS_TYPE,VALIDATION_CODE,IS_WINING_lUCKY_DRAW
) values(
    '1','GII-111','TRANS-LK-1',1,sysdate-3+4/24,sysdate-3+4/24,'S-123456',200.0,1,1,
    '98abe3a28383501f4bfd2d9077820f11',0,1,1,'2b514a0d4a859c5d6c5898f66e12f78a',1,200,
    '111111',1
);
update LK_TE_TICKET set IS_BLOCK_PAYOUT=0;
update LK_TE_TICKET set dev_id=111;
update LK_TE_TICKET set merchant_id=111;
update LK_TE_TICKET set operator_id='OPERATOR-111';
update LK_TE_TICKET set ticket_type=1;
update LK_TE_TICKET set TOTAL_BETS=1;  
update LK_TE_TICKET set LD_WINING_TOTAL_BETS=1;  

-- LFN ENTRY
insert into LK_TE_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT, NUMBER_SEQ, PRIZE_AMOUNT, TAX_AMOUNT, IS_WINNING
) values(
    'E-1',0,sysdate-3+4/24,1,'S-123456',1,'1,2,3,4,5',2,1,100,1,0,0,0
);
insert into LK_TE_ENTRY(
    ID,VERSION,CREATE_TIME,ENTRY_NO,TICKET_SERIALNO,BET_OPTION,SELECTED_NUMBER,IS_QUIDPICK,TOTAL_BETS,ENTRY_AMOUNT, NUMBER_SEQ, PRIZE_AMOUNT, TAX_AMOUNT, IS_WINNING
) values(
    'E-2',0,sysdate-3+4/24,2,'S-123456',55,'1,2,3,4,5,6',2,1,100,8,1000,50,1
); 

--LK_OFFLINE_CANCELLATION
insert into LK_OFFLINE_CANCELLATION (ID, START_NUMBER, END_NUMBER, CURRENT_NUMBER, IS_HANDLED, TE_TRANSACTION_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, GAME_ID)
values ('LOC-1', 2, 7, 5, 0, 'TRANS-LK-1', null, null, null, null, 'LK-1');
insert into LK_OFFLINE_CANCELLATION (ID, START_NUMBER, END_NUMBER, CURRENT_NUMBER, IS_HANDLED, TE_TRANSACTION_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, GAME_ID)
values ('LOC-2', 7, 4, 9, 0, 'TRANS-LK-1', null, null, null, null, 'LK-1');
insert into LK_OFFLINE_CANCELLATION (ID, START_NUMBER, END_NUMBER, CURRENT_NUMBER, IS_HANDLED, TE_TRANSACTION_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, GAME_ID)
values ('LOC-3', 7, 4, 2, 0, 'TRANS-LK-1', null, null, null, null, 'LK-1');
insert into LK_OFFLINE_CANCELLATION (ID, START_NUMBER, END_NUMBER, CURRENT_NUMBER, IS_HANDLED, TE_TRANSACTION_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, GAME_ID)
values ('LOC-4', 2, 7, 5, 0, 'TRANS-LK-1', null, null, null, null, 'LK-2');
insert into LK_OFFLINE_CANCELLATION (ID, START_NUMBER, END_NUMBER, CURRENT_NUMBER, IS_HANDLED, TE_TRANSACTION_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, GAME_ID)
values ('LOC-5', 7, 4, 9, 0, 'TRANS-LK-1', null, null, null, null, 'LK-2');
insert into LK_OFFLINE_CANCELLATION (ID, START_NUMBER, END_NUMBER, CURRENT_NUMBER, IS_HANDLED, TE_TRANSACTION_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, GAME_ID)
values ('LOC-6', 7, 4, 2, 0, 'TRANS-LK-1', null, null, null, null, 'LK-2');

--LK_OFFLINE_PRIZE_STATUS
insert into LK_OFFLINE_PRIZE_STATUS (ID, NEXT_SEQ, LAST_BUYER, GAME_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY)
values ('LOPS-1', 5, 'terry1', 'LK-1', null, null, null, null);
insert into LK_OFFLINE_PRIZE_STATUS (ID, NEXT_SEQ, LAST_BUYER, GAME_ID, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY)
values ('LOPS-2', 5, 'terry2', 'LK-2', null, null, null, null);

-------------------------------------------------------------------
-- ENCRYPTION
------------------------------------------------------------------- 
update lk_te_ticket set serial_no='9pkxn/npytVqVbOF5fPlsg==' where serial_no='S-123456';

update lk_te_entry set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';

update te_transaction set TICKET_SERIAL_NO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIAL_NO='S-123456';
 

commit;    