delete from VAT_CUSTOMER;
delete from VAT;
delete from VAT_OPERATOR_MERCHANT_TYPE;
delete from VAT_GAME;
delete from VAT_MERCHANT;
delete from VAT_OPERATOR_BALANCE;
delete from VAT_CUSTOMER;
delete from vat_transaction_records;

-- VAT
insert into VAT(id, vat_code, status, round_is_up_down) values('VAT-1', 'foodA', 1, 0);
insert into VAT(id, vat_code, status, round_is_up_down) values('VAT-2', 'foodB', 1, 0);

-- VAT_OPERATOR_MERCHANT_TYPE
insert into VAT_OPERATOR_MERCHANT_TYPE(
    id, operator_id, VAT_MERCHANT_TYPE_ID, status
) values(
    '1', 'OPERATOR-111', '1', 1
);

-- VAT_GAME
insert into VAT_GAME(
    id, game_id, vat_id, VAT_MERCHANT_TYPE_ID, status,vat_rate,MINIMUM_AMOUNT
) values(
    'VG-1', 'RA-1', 'VAT-1', '1', 1,0.1,10
);
insert into VAT_GAME(
    id, game_id, vat_id, VAT_MERCHANT_TYPE_ID, status,vat_rate,MINIMUM_AMOUNT
) values(
    'VG-2', 'LK-1', 'VAT-1', '2', 1,0.1,10
);
insert into VAT_GAME(
    id, game_id, vat_id, VAT_MERCHANT_TYPE_ID, status,vat_rate,MINIMUM_AMOUNT
) values(
    'VG-3', 'RA-1', 'VAT-2', '2', 1,0.1,10
);
insert into VAT_GAME(
    id, game_id, vat_id, VAT_MERCHANT_TYPE_ID, status,vat_rate,MINIMUM_AMOUNT
) values(
    'VG-4', 'LK-1', 'VAT-2', '1', 1,0.1,10
);

-- VAT_MERCHANT
insert into VAT_MERCHANT(id, merchant_id, vat_id, status) values('VM-1', 111, 'VAT-1', 1);
insert into VAT_MERCHANT(id, merchant_id, vat_id, status) values('VM-2', 111, 'VAT-2', 1);

-- VAT_OPERATOR_BALANCE;
insert into VAT_OPERATOR_BALANCE(
    id,create_time,update_time, operator_id, operator_sale_balance
) values(
    'VOB-1', sysdate, sysdate, 'OPERATOR-111', 100.0
);

-- VAT_CUSTOMER
insert into VAT_CUSTOMER(id, create_time, merchant_id) values('COMPANY-1', sysdate, 111);
insert into VAT_CUSTOMER(id, create_time, merchant_id) values('COMPANY-2', sysdate, 112);

commit;
