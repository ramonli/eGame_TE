delete from LD_WINNING;
delete from LD_GAME_INSTANCE;
delete from BD_PRIZE_GROUP_ITEM where id like 'LK-%';  	
delete from GAME where GAME_ID='LD-1';

-- ============================================================== --
-- GAME INSTANCE                                                  --
-- ============================================================== --
insert into GAME(
    GAME_ID,GAME_TYPE_ID,FUNDAMENTAL_TYPE_ID,OPERATION_PARAMETERS_ID,WINNER_TAX_POLICY_ID,
    TAX_CALCULATION_METHOD,GAME_NAME,STATUS,TAX_CALCULATION_BASED
) values(
    'LD-1', 19, 'FTI-111','OP-111','TP-1',2,'Lucky Draw',1,1
);
insert into LD_GAME_INSTANCE(
	id, GAME_INSTANCE_NAME, DRAW_NO, DRAW_DATE, START_SELLING_TIME, STOP_SELLING_TIME, STATUS, prize_logic_id,
	MAX_CLAIM_PERIOD, IS_SUSPEND_PAYOUT,VERSION, GAME_ID,CONTORL_METHOD,SALES_AMOUNT_PERCENT,LOSS_AMOUNT
) values (
	'GII-LD-1', 'lucky7', '001', sysdate+1, sysdate-1, sysdate+1, 2, 'OPL-1', 0,0,1,'LD-1',1,0,0
);

-- ============================================================== --
-- WINNING OF LUCKY DRAW                                          --
-- ============================================================== --
-- the prize amount doesn't matter, actual prize information will be retrieved from bd_prize_level.
insert into LD_WINNING(
	id, version, TICKET_SERIALNO, prize_level, is_valid, ld_game_instance_id,create_time, prize_amount, PRIZE_NUMBER,STATUS
) values (
	'w-1', 1, 'S-123456', 1, 1, 'GII-LD-1',sysdate, 0,2,1
);
insert into LD_WINNING(
	id, version, TICKET_SERIALNO, prize_level, is_valid, ld_game_instance_id,create_time, prize_amount,PRIZE_NUMBER,STATUS
) values (
	'w-2', 1, 'S-123456', 4, 1, 'GII-LD-1',sysdate, 0,2,1
);
insert into LD_WINNING(
	id, version, TICKET_SERIALNO, prize_level, is_valid, ld_game_instance_id,create_time, prize_amount, PRIZE_NUMBER,STATUS
) values (
	'w-3', 1, 'T-123456', 1, 1, 'GII-LD-1',sysdate, 0,2,1
);
insert into LD_WINNING(
	id, version, TICKET_SERIALNO, prize_level, is_valid, ld_game_instance_id,create_time, prize_amount,PRIZE_NUMBER,STATUS
) values (
	'w-4', 1, 'T-123456', 4, 1, 'GII-LD-1',sysdate, 0,2,1
);

-- ============================================================== --
-- BD_PRIZE_GROUP_ITEM                                            --
-- ============================================================== --
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-1', 'BPG-1', '1', 19, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-2', 'BPG-1', '2', 19, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-3', 'BPG-1', '3', 19, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-4', 'BPG-1', '4',19, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-5', 'BPG-1', '5', 19, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-6', 'BPG-1', '6', 19, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-7', 'BPG-1', '7', 19, 2
);
insert into bd_prize_group_item(
    id, bd_prize_group_id, prize_level,game_type,prize_type
) values(
    'LK-8', 'BPG-1', '8', 19, 2
);

update LD_WINNING set TICKET_SERIALNO='9pkxn/npytVqVbOF5fPlsg==' where TICKET_SERIALNO='S-123456';
update LD_WINNING set TICKET_SERIALNO='nAmMGjJjE2Fpdde2k1iZRw==' where TICKET_SERIALNO='T-123456';	

commit;