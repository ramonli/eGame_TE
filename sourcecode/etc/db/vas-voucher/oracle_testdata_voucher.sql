delete from GAME_TYPE where GAME_TYPE_ID=1001;
delete from GAME where GAME_TYPE_ID=1001;
delete from VAS_VOUCHER_OPERATOR_PARA;
delete from GAME_MERCHANT where ID like 'GM-VOUCHER-%';
delete from VAS_VOUCHER_PARAMETERS;
delete from MERCHANT_GAME_PROPERTIES where MRID like 'MG_VOUCHER_%';    
delete from VAS_VOUCHERS;
delete from TE_TRANSACTION where ID like 'TRANS-TELECO-VOUCHER%';
delete from VAS_VOUCHER_SELLING_RECORDS ;
delete from VAS_VOUCHER_PARAMETERS;


-- GAME_TYPE
insert into GAME_TYPE(GAME_TYPE_ID,TYPE_NAME) values(1001, 'VAS-VOUCHER');

-- GAME
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'VOUCHER-1', 1001, null, 'VOUCHER-OP-1','TP-1',2,'VOUCHER-1',1,1
);
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,GAME_NAME,TAX_CALCULATION_METHOD,
    STATUS,WINNER_TAX_POLICY_ID,TAX_CALCULATION_BASED
) values(
    'VOUCHER-2', 1001, null, 'VOUCHER-OP-1','VOUCHER-2',1,1,'TP-1',1
);

-- VAS_VOUCHER_OPERATOR_PARA
insert into VAS_VOUCHER_OPERATOR_PARA(ID, VOUCHER_TYPE,EXPIRED_DAY) values('VOUCHER-OP-1', 1, 1);

insert into VAS_VOUCHER_PARAMETERS(ID,DENOMINATION,REMAINING_VOUCHER_NUMBER,GAME_ID) values('1',5.0,100,'VOUCHER-1');  
insert into VAS_VOUCHER_PARAMETERS(ID,DENOMINATION,REMAINING_VOUCHER_NUMBER,GAME_ID) values('2',15.0,100,'VOUCHER-1');
insert into VAS_VOUCHER_PARAMETERS(ID,DENOMINATION,REMAINING_VOUCHER_NUMBER,GAME_ID) values('3',30.0,100,'VOUCHER-1');
insert into VAS_VOUCHER_PARAMETERS(ID,DENOMINATION,REMAINING_VOUCHER_NUMBER,GAME_ID) values('4',60.0,100,'VOUCHER-1');
insert into VAS_VOUCHER_PARAMETERS(ID,DENOMINATION,REMAINING_VOUCHER_NUMBER,GAME_ID) values('5',80.0,100,'VOUCHER-1');

-- GAME_MERCHANT 
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-VOUCHER-111', 111, 'VOUCHER-1',0.1,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-VOUCHER-113', 111, 'VOUCHER-2',0.1,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-VOUCHER-114', 222, 'VOUCHER-1',0.15,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-VOUCHER-116', 222, 'VOUCHER-2',0.15,0.0
);
insert into GAME_MERCHANT(
    ID, MERCHANT_ID, GAME_ID, COMMISSION_RATE_SALES, COMMISSION_RATE_PAYOUT
) values(
    'GM-VOUCHER-117', 112, 'VOUCHER-2',0.1,0.0
);

-- ============================================================== --
-- MERCHANT_GAME_PROPERTIES                                              --
-- ============================================================== -- 
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-VOUCHER-111', 111, 'VOUCHER-1','OPERATOR-111',0.05,0.0
);
insert into MERCHANT_GAME_PROPERTIES(
    MRID, MERCHANT_ID, GAME_ID,OPERATOR_ID, COMMISSION_RATE, COMMISSION_RATE_PAYOUT
) values(
    'MG-VOUCHER-113', 111, 'VOUCHER-2','OPERATOR-111',0.05,0.0
);

-- VAS_VOUCHERS
-- V-1-PIN : U+HBKjFw5pM=
insert into VAS_VOUCHERS(
    ID,GAME_ID,FACE_VALUE,EXPIRED_DATE,SERIAL_NUMBER,VOUCHER_NUMBER,STATUS,CREATE_TIME,UPDATE_TIME
) values (
    '1','VOUCHER-1',60,sysdate+2,'V-1','U+HBKjFw5pM=',1,sysdate,sysdate
);
-- V-2-PIN : uPWpJE977/E=
insert into VAS_VOUCHERS(
    ID,GAME_ID,FACE_VALUE,EXPIRED_DATE,SERIAL_NUMBER,VOUCHER_NUMBER,STATUS,CREATE_TIME,UPDATE_TIME
) values (
    '2','VOUCHER-1',60,sysdate+3+30/(24*60),'V-2','uPWpJE977/E=',1,sysdate,sysdate
);
-- V-3-PIN : vkfSC0ePm9U=
insert into VAS_VOUCHERS(
    ID,GAME_ID,FACE_VALUE,EXPIRED_DATE,SERIAL_NUMBER,VOUCHER_NUMBER,STATUS,CREATE_TIME,UPDATE_TIME
) values (
    '3','VOUCHER-1',60,sysdate+4,'V-3','vkfSC0ePm9U=',1,sysdate,sysdate
);
-- V-4-PIN : QPWb4/8akkQ=
insert into VAS_VOUCHERS(
    ID,GAME_ID,FACE_VALUE,EXPIRED_DATE,SERIAL_NUMBER,VOUCHER_NUMBER,STATUS,CREATE_TIME,UPDATE_TIME
) values (
    '4','VOUCHER-1',50,sysdate+2,'V-4','QPWb4/8akkQ=',1,sysdate,sysdate
);
-- V-101-PIN : upENMovTLB+b9JhJ95Q7hw==
insert into VAS_VOUCHERS(
    ID,GAME_ID,FACE_VALUE,EXPIRED_DATE,SERIAL_NUMBER,VOUCHER_NUMBER,STATUS,CREATE_TIME,UPDATE_TIME
) values (
    '101','VOUCHER-2',60,sysdate+2-30/(24*60),'V-101','upENMovTLB+b9JhJ95Q7hw==',1,sysdate,sysdate
);
--V-102-PIN : A9k48+/TnIGb9JhJ95Q7hw==
insert into VAS_VOUCHERS(
    ID,GAME_ID,FACE_VALUE,EXPIRED_DATE,SERIAL_NUMBER,VOUCHER_NUMBER,STATUS,CREATE_TIME,UPDATE_TIME
) values (
    '102','VOUCHER-2',80,sysdate+1,'V-102','VA9k48+/TnIGb9JhJ95Q7hw==',2,sysdate,sysdate
);

-- TE_TRANSACTION
insert into TE_TRANSACTION( 
    ID,OPERATOR_ID,GPE_ID,DEV_ID,MERCHANT_ID,CREATE_TIME,TYPE,TRANS_TIMESTAMP,RESPONSE_CODE,
    TICKET_SERIAL_NO,TRACE_MESSAGE_ID,BATCH_NO,GAME_ID,VERSION,TOTAL_AMOUNT,DESTINATION_OPEATOR,CANCEL_TE_TRANSACTION_ID,CANCEL_TRANSACTION_TYPE
) values(
    'TRANS-TELECO-VOUCHER-1111','OPERATOR-111','GPE-111',111,111,sysdate,
    456,sysdate,200,null,'telecovoucher_msg_1','20092009','VOUCHER-1',0,100.0,'OPERATOR-111',null,0
); 

-- VAS_VOUCHER_SELLING_RECORDS
insert into VAS_VOUCHER_SELLING_RECORDS (
    ID, VOUCHER_SERIAL, VOUCHER_ID, GAME_ID, STATUS, FACE_VALUE, DEV_ID, OPERATOR_ID, MERCHANT_ID, TE_TRANSACTION_ID, CREATE_TIME, 
    CREATE_BY, UPDATE_BY, UPDATE_TIME
) values(
    'voucher-record-111', 'V-1', '1', 'VOUCHER-1', 1, 50, 111, 'OPERATOR-111', 111, 'TRANS-TELECO-VOUCHER-1111', sysdate, 
    '111', '111', sysdate
);

insert into BALANCE_TRANSACTIONS (ID, TE_TRANSACTION_ID, MERCHANT_ID, DEVICE_ID, OPERATOR_ID, OWNER_ID, OWNER_TYPE, PAYMENT_TYPE, TRANSACTION_TYPE, BALANCE_AMOUNT, COMMISION_AMOUNT, UPDATE_TIME, COMMISION_RATE, CREATE_TIME, STATUS)
    values (1000005, 'TRANS-TELECO-VOUCHER-1111', 111, 111, 'OPERATOR-111', 'OPERATOR-111', 1, 1, 456, 100, 1, sysdate, .01, sysdate, '1');



commit;
