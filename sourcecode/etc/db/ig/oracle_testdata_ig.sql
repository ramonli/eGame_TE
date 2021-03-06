delete from GAME_TYPE where GAME_TYPE_ID=4;
delete from GAME where GAME_ID in ('IG-112');
delete from GAME_MERCHANT where ID like 'GM-IG-%';    
delete from IG_OPERATION_PARAMETERS;
delete from IG_GAME_INSTANCE;
delete from TE_TRANSACTION where exists(select * from TE_TRANSACTION t,GAME g where t.game_id=g.game_id and g.game_type_id=4);     
delete from TE_TRANSACTION_MSG;
--delete from INSTANT_GAME_FUN_TYPE;
delete from IG_GAME_INSTANCE;
delete from INSTANT_TICKET;
delete from INSTANT_TICKET_VIRN;
delete from bd_prize_group_item where id like 'IG-%';
delete from TRANSACTION_RETRY_LOG;
delete from IG_BATCH_REPORT;    
delete from IG_FAILED_TICKETS_REPORT;
delete from IG_OPERATOR_BATCH;
delete from ig_batch_report where id like 'IG-br-%';
delete from ig_failed_tickets_report where id like 'IG-FTR-%';
delete from MERCHANT_GAME_PROPERTIES where MRID like '%-IG-%';  
delete from IG_PAYOUT_DETAIL_TEMP;
delete from IG_PAYOUT_TEMP;
delete from IG_FAILED_TICKETS_REPORT;

-- GAME_TYPE
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(4, 'IG');

--GAME
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,TAX_CALCULATION_METHOD,
    GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'IG-112', 4, 'FTI-111','1',1,'Running Stone',1,1
); 
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,TAX_CALCULATION_METHOD,
    GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'IG-113', 4, 'FTI-111','1',1,'Black Jack',1,1
);  
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,TAX_CALCULATION_METHOD,
    GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'IG-114', 4, 'FTI-111','1',1,'Golden Farmer',1,1
);    

--IG_OPERATION_PARAMETERS
insert into IG_OPERATION_PARAMETERS(
    ID,MAX_VALIDATE_TIMES,DESCRIPTION,START_SERIAL_NO,END_SERIAL_NO
) values(
    '1',50,'5 times/hour',0,199
);

-- GAME_MERCHANT 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-IG-112', 111, 'IG-112',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-IG-115', 222, 'IG-112',0.0,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-IG-116', 111, 'IG-113',0.0,0.1
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-IG-117', 222, 'IG-113',0.0,0.3
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-IG-118', 111, 'IG-114',0.0,0.1
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-IG-119', 222, 'IG-114',0.0,0.2
);

-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-IG-111', 111, 'IG-112','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-IG-112', 111, 'IG-113','OPERATOR-111',0.0,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-IG-113', 111, 'IG-114','OPERATOR-111',0.0,0.0
);

-- INSTANT GAME INSTANCE
insert into IG_GAME_INSTANCE(
    IG_GAME_INSTANCE_ID,GAME_INSTANCE_NAME,START_ACTIVATION_TIME,STOP_PAYOUT_TIME,STATUS,
    GAME_ID,VALIDATION_TYPE,FACE_VALUE,IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT
) values(
    'IGII-111','157',to_date('2009-04-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),
    to_date('2021-08-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),2,'IG-112',1,10.0,0,0
);
insert into IG_GAME_INSTANCE(
    IG_GAME_INSTANCE_ID,GAME_INSTANCE_NAME,START_ACTIVATION_TIME,STOP_PAYOUT_TIME,STATUS,
    GAME_ID,VALIDATION_TYPE,FACE_VALUE,IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT
) values(
    'IGII-112','198',to_date('2009-04-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),
    to_date('2021-08-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),2,'IG-112',1,10.0,0,0
);
insert into IG_GAME_INSTANCE(
    IG_GAME_INSTANCE_ID,GAME_INSTANCE_NAME,START_ACTIVATION_TIME,STOP_PAYOUT_TIME,STATUS,
    GAME_ID,VALIDATION_TYPE,FACE_VALUE,IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT
) values(
    'IGII-113','200',to_date('2009-04-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),
    to_date('2021-08-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),2,'IG-112',1,10.0,0,0
);
insert into IG_GAME_INSTANCE(
    IG_GAME_INSTANCE_ID,GAME_INSTANCE_NAME,START_ACTIVATION_TIME,STOP_PAYOUT_TIME,STATUS,
    GAME_ID,VALIDATION_TYPE,FACE_VALUE,IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT
) values(
    'IGII-114','333',to_date('2009-06-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),
    to_date('2021-08-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),2,'IG-112',1,10.0,0,0
);
update IG_GAME_INSTANCE set bd_prize_logic_id='OPL-1';

-- TRANSACTION    
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID
) values(
    'TRANS-109','OPERATOR-111','GPE-111',111,111,to_date('2009-07-20 16:37:41','YYYY-MM-DD HH24:MI:SS'),
    404,sysdate,200,'200415681001','TMI-090','20092009','IG-112'
);  
insert into TE_TRANSACTION(
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TRACE_MESSAGE_ID,TICKET_SERIAL_NO,BATCH_NO,GAME_ID
) values(
    'TRANS-118','OPERATOR-111','GPE-111',111,111,sysdate,404,sysdate,200,
    'TMI-116','198415681983','200901','IG-112'
);
insert into TE_TRANSACTION(
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TRACE_MESSAGE_ID,TICKET_SERIAL_NO,BATCH_NO,GAME_ID,TOTAL_AMOUNT
) values(
    'TRANS-119','OPERATOR-111','GPE-111',111,111,sysdate,402,sysdate,200,
    'TMI-117','598198195103','200901','IG-112',450.0
);
insert into TE_TRANSACTION(
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TRACE_MESSAGE_ID,TICKET_SERIAL_NO,BATCH_NO,GAME_ID
) values(
    'TRANS-120','OPERATOR-111','GPE-111',111,111,sysdate,407,sysdate,200,
    'TMI-118',null,'200901','IG-112'
);
--reversal
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TRACE_MESSAGE_ID,TICKET_SERIAL_NO,BATCH_NO,GAME_ID
) values(
    'TRANS-121','OPERATOR-111','GPE-111',111,111,sysdate,407,sysdate,321,
    'TMI-119',null,'200901','IG-112'
);
-- cancel by transaction
--reversal
insert into TE_TRANSACTION(
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TRACE_MESSAGE_ID,TICKET_SERIAL_NO,BATCH_NO,GAME_ID,TOTAL_AMOUNT
) values(
    'TRANS-130','OPERATOR-111','GPE-111',111,111,sysdate,402,sysdate,421,
    'TMI-130','201015681004','200901','IG-112',400.0
);
update TE_TRANSACTION set version=0;

insert into TE_TRANSACTION_MSG(
    TRANSACTION_ID, REQ_MESSAGE, RES_MESSAGE
) values(
    'TRANS-120','{"200415681001":200,"200415681002":401,"200415681003":200,"200415681004":200}',null
);
insert into TE_TRANSACTION_MSG(
    TRANSACTION_ID, REQ_MESSAGE, RES_MESSAGE
) values(
    'TRANS-121','{"200415681001":200,"200415681002":401,"200415681003":200,"200415681004":200}',null
);     

-- INSTANT TICKET
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS
) values(
    'IT-110','IGII-111','157823119','157823119020','333CF2725BDC6C54D37CE39580197800',
    '36338604','41684190',7,3,0,0,null,0,0,0,0
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-111','IGII-111','157823119','157823119021','A059A8CD6395DBCD5C85AF88F0D53795',
    '18675470237','12579',10,3,0,0,null,0,0,0,0,'42892ffdc942243cf117ab42623dda81'
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS
) values(
    'IT-112','IGII-111','598198195','598198195103','DDC65B1798283D04A1F1038F0FE976C9',
    '9','474802131240018',0,5,0,0,null,0,0,0,0
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-113','IGII-112','198415681','198415681983','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,3,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS
) values(
    'IT-114','IGII-111','984161896','984161896312','69A02DE5B12EF084A2CF287EF368A90E',
    '3','314173430586373',0,1,0,0,null,0,0,0,0
);

insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-115','IGII-112','198415681','198415681002','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,2,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-116','IGII-112','198415681','198415681003','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,3,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
-- for batch validation
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-117','IGII-112','200415681','200415681001','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,5,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-118','IGII-112','200415681','200415681002','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,3,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-119','IGII-112','200415681','200415681003','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,5,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-120','IGII-112','200415681','200415681004','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,5,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
-- for free ticket
insert into INSTANT_TICKET(
    ID,IG_GAME_INSTANCE_ID,BOOK_NUMBER,TICKET_SERIAL,TICKET_MAC,TICKET_XOR1,TICKET_XOR2,
    REFERENCE_INDEX,STATUS,IS_SOLD_TO_CUSTOMER,IS_IN_BLACKLIST,SOLD_TIME,
    IS_SUSPEND_ACTIVATION,IS_SUSPEND_PAYOUT,IS_VALIDATION_SETTLED,PHYSICAL_STATUS,XOR_MD5
) values(
    'IT-121','IGII-112','201015681','201015681004','B17835883C94BE331F4ECA6E89AC797F',
    '5810383066345330','',15,5,0,0,null,0,0,0,0,'59e8fd450b39ab311efda6f6ec5f6a4c'
);
-- for confirmation batch validation

-- INSTANT_VIRN_PRIZE
insert into INSTANT_TICKET_VIRN(
    ID,IG_GAME_INSTANCE_ID,VIRN,PRIZE_VALUE,IS_VALIDATED,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_TYPE
) values(
    'ITV-111', 'IGII-112', '37330218', 11111.0,0,1000.0,10000.0,'CC'
);
insert into INSTANT_TICKET_VIRN(
    ID,IG_GAME_INSTANCE_ID,VIRN,PRIZE_VALUE,IS_VALIDATED,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_TYPE
) values(
    'ITV-112', 'IGII-112', '37330200', 21111.0,0,2000.0,20000.0,'CC'
);
insert into INSTANT_TICKET_VIRN(
    ID,IG_GAME_INSTANCE_ID,VIRN,PRIZE_VALUE,IS_VALIDATED,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_TYPE
) values(
    'ITV-113', 'IGII-112', '27330200', 31111.0,0,3000.0,30000.0,'FO'
);
insert into INSTANT_TICKET_VIRN(
    ID,IG_GAME_INSTANCE_ID,VIRN,PRIZE_VALUE,IS_VALIDATED,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_TYPE
) values(
    'ITV-114', 'IGII-111', '47330200', 41111.0,0,4000.0,40000.0,'CC'
);
insert into INSTANT_TICKET_VIRN(
    ID,IG_GAME_INSTANCE_ID,VIRN,PRIZE_VALUE,IS_VALIDATED,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_TYPE
) values(
    'ITV-115', 'IGII-111', '57330200', 51111.0,0,5000.0,50000.0,'CC'
);
insert into INSTANT_TICKET_VIRN(
    ID,IG_GAME_INSTANCE_ID,VIRN,PRIZE_VALUE,IS_VALIDATED,TAX_AMOUNT,ACTUAL_PAYOUT,PRIZE_TYPE
) values(
    'ITV-116', 'IGII-111', '20100200', 1000.0,1,200.0,800.0,'FO'
);

-- PAYOUT
insert into PAYOUT(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-4',1,sysdate,sysdate,null,'TRANS-120','200415681001',50.0,1,1,1,0,'IG-112'
);
insert into PAYOUT(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-5',1,sysdate,sysdate,null,'TRANS-120','200415681003',150.0,1,1,1,0,'IG-112'
);
insert into PAYOUT(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-6',1,sysdate,sysdate,null,'TRANS-120','200415681004',250.0,1,1,1,0,'IG-112'
);
--reversal
insert into PAYOUT( 
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-7',1,sysdate,sysdate,null,'TRANS-121','200415681001',50.0,1,1,2,0,'IG-112'
);
insert into PAYOUT(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-8',1,sysdate,sysdate,null,'TRANS-121','200415681003',150.0,1,1,2,0,'IG-112'
);
insert into PAYOUT(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-9',1,sysdate,sysdate,null,'TRANS-121','200415681004',250.0,1,1,2,0,'IG-112'
);
insert into PAYOUT(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-10',1,sysdate,sysdate,null,'TRANS-130','201015681004',800.0,1,1,1,0,'IG-112'
);
insert into PAYOUT(
    ID,VERSION,CREATE_TIME,UPDATE_TIME,GAME_INSTANCE_ID,TRANSACTION_ID,TICKET_SERIALNO,
    TOTAL_AMOUNT,TYPE,IS_VALID,STATUS,IS_BY_MANUAL,GAME_ID
) values(
    'IG-P-3',1,sysdate,sysdate,null,'TRANS-119','598198195103',50.0,1,1,1,0,'IG-112'
);
update payout set dev_id=111, merchant_id=111,operator_id='OPERATOR-111' where ID like 'IG-%'; 
update payout set total_amount_b4_tax = total_amount + 10 where ID like 'IG-%';
update payout set object_num=1 where ID like 'IG-%';

-- PAYOUT_DETAIL
insert into PAYOUT_DETAIL(
    ID, PAYOUT_ID,TOTAL_AMOUNT,CASH_AMOUNT,PAYOUT_TYPE,BG_LUCKY_PRIZE_OBJECT_ID,
    BG_LUCKY_PRIZE_OBJECT_NAME,CREATE_TIME,UPDATE_TIME,OBJECT_NUM,OBJECT_TYPE
) values(
    'IG-PD-1','IG-P-3',50,40,1,null,null,sysdate,sysdate,1,1
);
insert into PAYOUT_DETAIL(
    ID, PAYOUT_ID,TOTAL_AMOUNT,CASH_AMOUNT,PAYOUT_TYPE,BG_LUCKY_PRIZE_OBJECT_ID,
    BG_LUCKY_PRIZE_OBJECT_NAME,CREATE_TIME,UPDATE_TIME,OBJECT_NUM,OBJECT_TYPE
) values(
    'IG-PD-2','IG-P-3',5,5,2,'BPO-8','Book',sysdate,sysdate,2,1
);
insert into PAYOUT_DETAIL(
    ID, PAYOUT_ID,TOTAL_AMOUNT,CASH_AMOUNT,PAYOUT_TYPE,BG_LUCKY_PRIZE_OBJECT_ID,
    BG_LUCKY_PRIZE_OBJECT_NAME,CREATE_TIME,UPDATE_TIME,OBJECT_NUM,OBJECT_TYPE
) values(
    'IG-PD-3','IG-P-3',500,500,2,'BPO-18','free entry 2',sysdate,sysdate,2,2
);
insert into PAYOUT_DETAIL(
    ID, PAYOUT_ID,TOTAL_AMOUNT,CASH_AMOUNT,PAYOUT_TYPE,BG_LUCKY_PRIZE_OBJECT_ID,
    BG_LUCKY_PRIZE_OBJECT_NAME,CREATE_TIME,UPDATE_TIME,OBJECT_NUM,OBJECT_TYPE
) values(
    'IG-PD-4','IG-P-3',500,500,2,'BPO-18','free entry 2',sysdate,sysdate,2,2
);  

-- ============================================================== --
-- BD_PRIZE_GROUP_ITEM                                            --
-- ============================================================== --
-- prize type definition: 1,cash; 2,object
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL0', 'BPG-1', '1', 4, 3
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL1', 'BPG-1', '2', 4, 3
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL2', 'BPG-1', '3', 4, 3
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL3', 'BPG-1', '4',4, 3
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL4', 'BPG-1', '5', 4, 3
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL5', 'BPG-1', '6', 4, 3
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL6', 'BPG-1', '7', 4, 3
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-LL7', 'BPG-1', '8', 4, 2
);

insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-8', 'BPG-1', '1', 4, 1
);

insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-9', 'BPG-1', '2', 4, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-10', 'BPG-1', '3', 4, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-11', 'BPG-1', '4',4, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-12', 'BPG-1', '5', 4, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-13', 'BPG-1', '6', 4, 1
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-14', 'BPG-1', '2', 4, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-15', 'BPG-1', '3', 4, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-16', 'BPG-1', '4',4, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-17', 'BPG-1', '5', 4, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'IG-18', 'BPG-1', '6', 4, 2
);

insert into IG_BATCH_REPORT (ID, OPERATOR_ID, BATCH_ID, FAILED_TICKETS_COUNT, SUCCEDED_TICKETS_COUNT, ACTUAL_AMOUNT, TAX_AMOUNT, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY)
values ('IG-br-1', 'OPERATOR-111', 200901, 2, 3, 100, 5, sysdate, null, sysdate, null);


insert into IG_FAILED_TICKETS_REPORT (ID, OPERATOR_ID, BATCH_ID, IG_SERIAL_NUMBER, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, STATUS)
values ('IG-FTR-3', 'OPERATOR-111', 200901, 'Ueo53lZi+W7Nq/jRme4+Hw==', sysdate, null, sysdate, null, 1);
insert into IG_FAILED_TICKETS_REPORT (ID, OPERATOR_ID, BATCH_ID, IG_SERIAL_NUMBER, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, STATUS)
values ('IG-FTR-2', 'OPERATOR-111', 200901, 'GMdmUFyojm1t9oEUjU7wlA==', sysdate, null, sysdate, null, 0);
insert into IG_FAILED_TICKETS_REPORT (ID, OPERATOR_ID, BATCH_ID, IG_SERIAL_NUMBER, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, STATUS)
values ('IG-FTR-1', 'OPERATOR-111', 200901, '78MPOsEOwlfiQ1nbbHNFCg==', sysdate, null, sysdate, null, 0);

-- IG_OPERATOR_BATCH
insert into IG_OPERATOR_BATCH (
    OPERATOR_ID, CURRENT_BATCH_NUMBER, CREATE_TIME, UPDATE_TIME, CREATE_BY, UPDATE_BY, ID
) values (
    'OPERATOR-111', 2, null, null, null, null, '1'
);



insert into IG_PAYOUT_DETAIL_TEMP (ID, PAYOUT_ID, TOTAL_AMOUNT, CASH_AMOUNT, TOPUP_AMOUNT, TOPUP_MODE, PAYOUT_TYPE, BG_LUCKY_PRIZE_OBJECT_ID, BG_LUCKY_PRIZE_OBJECT_NAME, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, OBJECT_NUM, OBJECT_TYPE, OBJECT_NUM_PER_LEVEL_ITEM, IG_BATCH_NUMBER, OPERATOR_ID)
values ('1', '1', 10000, 10000, 0, 0, 1, null, null, to_timestamp('21-05-2014 16:29:53.223000', 'dd-mm-yyyy hh24:mi:ss.ff'), '1', to_timestamp('21-05-2014 16:29:53.223000', 'dd-mm-yyyy hh24:mi:ss.ff'), '1', null, null, null, 2, 'OPERATOR-111');

insert into IG_PAYOUT_TEMP (ID, VERSION, CREATE_TIME, UPDATE_TIME, GAME_INSTANCE_ID, TRANSACTION_ID, TICKET_SERIALNO, TOTAL_AMOUNT, TYPE, IS_VALID, STATUS, TOTAL_AMOUNT_B4_TAX, IS_BY_MANUAL, WINNER_ID, WINNER_NAME, HAS_DETAIL, DEV_ID, MERCHANT_ID, OPERATOR_ID, IS_PAYOUT_CONFIRMATION, OBJECT_NUM, OBJECT_AMOUNT, BATCH_NO2, GAME_ID, UTC_CREATETIME, IG_BATCH_NUMBER)
values ('1', 1, to_timestamp('16-10-2014 14:36:24.241000', 'dd-mm-yyyy hh24:mi:ss.ff'), to_timestamp('16-10-2014 14:36:24.241000', 'dd-mm-yyyy hh24:mi:ss.ff'), 'IGII-113', 'IG-113', 'u0wLyFL6hZx0yNAHqJjvsA==', 10000, '1', 1, 1, 12000, 0, '1', '1', 1, 1, 111, 'OPERATOR-111', 1, 0, 0, '12354', 'test001', to_timestamp('16-10-2014 14:36:24.241000', 'dd-mm-yyyy hh24:mi:ss.ff'), 2);

insert into IG_FAILED_TICKETS_REPORT (ID, OPERATOR_ID, BATCH_ID, IG_SERIAL_NUMBER, CREATE_TIME, CREATE_BY, UPDATE_TIME, UPDATE_BY, STATUS, ERROR_CODE)
values ('IG-FTR-6', 'OPERATOR-111', 2, 'Ueo53lZi+W7Nq/jRme4+Hw==', to_timestamp('22-11-2014 10:00:52.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), null, to_timestamp('22-11-2014 10:00:52.000000', 'dd-mm-yyyy hh24:mi:ss.ff'), null, 1, 500);

-------------------------------------------------------------------
-- ENCRYPTION    
-------------------------------------------------------------------

-- Update payout
update payout set TICKET_SERIALNO='Ueo53lZi+W7Nq/jRme4+Hw==' where TICKET_SERIALNO='157823119021';
update payout set TICKET_SERIALNO='78MPOsEOwlfiQ1nbbHNFCg==' where TICKET_SERIALNO='198415681983';
update payout set TICKET_SERIALNO='GMdmUFyojm1t9oEUjU7wlA==' where TICKET_SERIALNO='598198195103';
update payout set TICKET_SERIALNO='p1vtfyuUXrgsQSpTwHAlXw==' where TICKET_SERIALNO='201015681004';
update payout set TICKET_SERIALNO='p1vtfyuUXrh5dn7tDlsndw==' where TICKET_SERIALNO='201015681001';
update payout set TICKET_SERIALNO='p1vtfyuUXrgxGvb/HiSHcg==' where TICKET_SERIALNO='201015681003';
update payout set TICKET_SERIALNO='fPmOE7i5WlAIU9IIrGlBIw==' where TICKET_SERIALNO='157823119020';
update payout set TICKET_SERIALNO='4dWvSQZcR7/b25zWxS+RwA==' where TICKET_SERIALNO='984161896312';
update payout set TICKET_SERIALNO='78MPOsEOwlfbMTWefC18Zw==' where TICKET_SERIALNO='198415681002';
update payout set TICKET_SERIALNO='u0wLyFL6hZx0yNAHqJjvsA==' where TICKET_SERIALNO='198415681003';
update payout set TICKET_SERIALNO='U/y2hXr0Qs0KTmYGGY7ZdQ==' where TICKET_SERIALNO='200415681001';
update payout set TICKET_SERIALNO='U/y2hXr0Qs1lX+7Th3iJTg==' where TICKET_SERIALNO='200415681002';
update payout set TICKET_SERIALNO='U/y2hXr0Qs29MMrwrN9F2Q==' where TICKET_SERIALNO='200415681003';
update payout set TICKET_SERIALNO='U/y2hXr0Qs1JHmGUy5JIlg==' where TICKET_SERIALNO='200415681004';

-- Update te_transaction
update te_transaction set TICKET_SERIAL_NO='Ueo53lZi+W7Nq/jRme4+Hw==' where TICKET_SERIAL_NO='157823119021';
update te_transaction set TICKET_SERIAL_NO='78MPOsEOwlfiQ1nbbHNFCg==' where TICKET_SERIAL_NO='198415681983';
update te_transaction set TICKET_SERIAL_NO='GMdmUFyojm1t9oEUjU7wlA==' where TICKET_SERIAL_NO='598198195103';
update te_transaction set TICKET_SERIAL_NO='p1vtfyuUXrgsQSpTwHAlXw==' where TICKET_SERIAL_NO='201015681004';
update te_transaction set TICKET_SERIAL_NO='p1vtfyuUXrh5dn7tDlsndw==' where TICKET_SERIAL_NO='201015681001';
update te_transaction set TICKET_SERIAL_NO='p1vtfyuUXrgxGvb/HiSHcg==' where TICKET_SERIAL_NO='201015681003';
update te_transaction set TICKET_SERIAL_NO='fPmOE7i5WlAIU9IIrGlBIw==' where TICKET_SERIAL_NO='157823119020';
update te_transaction set TICKET_SERIAL_NO='4dWvSQZcR7/b25zWxS+RwA==' where TICKET_SERIAL_NO='984161896312';
update te_transaction set TICKET_SERIAL_NO='78MPOsEOwlfbMTWefC18Zw==' where TICKET_SERIAL_NO='198415681002';
update te_transaction set TICKET_SERIAL_NO='u0wLyFL6hZx0yNAHqJjvsA==' where TICKET_SERIAL_NO='198415681003';
update te_transaction set TICKET_SERIAL_NO='U/y2hXr0Qs0KTmYGGY7ZdQ==' where TICKET_SERIAL_NO='200415681001';
update te_transaction set TICKET_SERIAL_NO='U/y2hXr0Qs1lX+7Th3iJTg==' where TICKET_SERIAL_NO='200415681002';
update te_transaction set TICKET_SERIAL_NO='U/y2hXr0Qs29MMrwrN9F2Q==' where TICKET_SERIAL_NO='200415681003';
update te_transaction set TICKET_SERIAL_NO='U/y2hXr0Qs1JHmGUy5JIlg==' where TICKET_SERIAL_NO='200415681004';

-- Update Intant_ticket
update instant_ticket set ticket_serial='Ueo53lZi+W7Nq/jRme4+Hw==' where ticket_serial='157823119021';
update instant_ticket set ticket_serial='78MPOsEOwlfiQ1nbbHNFCg==' where ticket_serial='198415681983';
update instant_ticket set ticket_serial='GMdmUFyojm1t9oEUjU7wlA==' where ticket_serial='598198195103';
update instant_ticket set ticket_serial='p1vtfyuUXrgsQSpTwHAlXw==' where ticket_serial='201015681004';
update instant_ticket set ticket_serial='p1vtfyuUXrh5dn7tDlsndw==' where ticket_serial='201015681001';
update instant_ticket set ticket_serial='p1vtfyuUXrgxGvb/HiSHcg==' where ticket_serial='201015681003';
update instant_ticket set ticket_serial='fPmOE7i5WlAIU9IIrGlBIw==' where ticket_serial='157823119020';
update instant_ticket set ticket_serial='4dWvSQZcR7/b25zWxS+RwA==' where ticket_serial='984161896312';
update instant_ticket set ticket_serial='78MPOsEOwlfbMTWefC18Zw==' where ticket_serial='198415681002';
update instant_ticket set ticket_serial='u0wLyFL6hZx0yNAHqJjvsA==' where ticket_serial='198415681003';
update instant_ticket set ticket_serial='U/y2hXr0Qs0KTmYGGY7ZdQ==' where ticket_serial='200415681001';
update instant_ticket set ticket_serial='U/y2hXr0Qs1lX+7Th3iJTg==' where ticket_serial='200415681002';
update instant_ticket set ticket_serial='U/y2hXr0Qs29MMrwrN9F2Q==' where ticket_serial='200415681003';
update instant_ticket set ticket_serial='U/y2hXr0Qs1JHmGUy5JIlg==' where ticket_serial='200415681004';

commit;
