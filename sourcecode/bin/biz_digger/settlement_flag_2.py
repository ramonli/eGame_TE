"""
First of all we should prepare a input file which follow below format:
${MERCHANT_ID},${OPERATOR_ID},${BATCH_NO},${REPORT_DATE}
Based on this file, this tool will query database to get all other information, including actual 
sale amount, settled sales amount, amount of settlement_flag_2.
And finally, we have to figure out what cause the settlement_flag_2 manually one by one :(
"""

import sys
from java.sql import *
from java.lang import *

# global settings
input_file = 'batch_of_upload-input.csv.sample'
output_file = 'batch_of_upload-input.output.csv'

dbdriver = 'oracle.jdbc.driver.OracleDriver'
dburl = 'jdbc:oracle:thin:@10.40.0.202:1521/DBL'
dbuser = 'ramon'
dbpasswd = 'ramon'

lotto_game_id = 'ff80808125a165340125a7c978ab01e5'

def main(input_file, output_file):
	# export the result file in format:
	# 	${MERCHANT_ID},${OPERATOR_ID},${BATCH_NO},${REPORT_DATE},${ACTUAL_SALE},${SETTLEMENT_SALE},${FLAG2_SALE}
	fo = open(output_file,'w')
	fo.write("\"MERCHANT_ID\",\"BATCH_NO\",\"OPERATOR_ID\",\"LOGIN_NAME\",\"REPORT_DATE\",\"ACTUAL_SALE\",\"SETTLEMENT_SALE\",\"FLAG2_SALE\"\n")
	f = open(input_file,'r')
	for l in f:
		# ignore the comment line
		if l.startswith("#"): continue
		if l.strip() == "" : continue
		
		eles = l.split(',')
		if len(eles) != 5:	
			print "WARN:illegal line:" + l.strip() + ", ignore it!"
			continue
		try:
			int(eles[0].strip())
		except ValueError, f:
			print "WARN: can't parse, ignore this line directly: " + l
			continue
		
		# parse line
		merchant_id = int(eles[0].strip())
		batch_no = remove_colon(eles[1].strip())
		operator_id = remove_colon(eles[2].strip())
		operator_loginname = remove_colon(eles[3].strip())
		report_date = remove_colon(eles[4].strip())
		result = query_amount(merchant_id, operator_id, batch_no)
		# if the value starts with ', the excel will treat it as a string.
		line = str(merchant_id) + ',\'' + batch_no + ',' + operator_id + ',\'' + operator_loginname+ ',' \
			+ report_date + ',' + str(result[0]) + ',' + str(result[1]) + ',' + str(result[2]) + "\n"
		fo.write(line)
		fo.flush()
		
		print('finish handling line:%s' % l)
	f.close()
	fo.close()

def remove_colon(ele):
	i = ele.find("\"")
	if i >= 0:
		ele = ele.replace("\"","")
	return ele

def query_amount(merchant_id, operator_id, batch_no):
	result = []
	conn = None
	try:
		conn = connect()
	
		# the actual amount no matter if settled or not
		actual_sale_amount = query_actual_sale(conn, merchant_id, operator_id, batch_no)
		# the amount from settlement report.
		settlement_amount = query_settlement_amount(conn, merchant_id, operator_id, batch_no)
		# the amount of settlement flag_2
		settlement_flag2_amount = query_settlementflag_2_amount(conn, merchant_id, operator_id, batch_no)
		
		result.append(actual_sale_amount);
		result.append(settlement_amount);
		result.append(settlement_flag2_amount);
	except SQLException, e:
		print('ERROR:',e)
	finally:
		if conn != None:
			conn.close()
	return result
	
def query_settlementflag_2_amount(conn, merchant_id, operator_id, batch_no):
	query = """
	select sum(i.total_amount) as total_amount
	from v_te_transaction@orcl t, v_te_ticket@orcl i where t.type=200 
    and t.ticket_serial_no=i.serial_no and i.status in (0,1,5) and t.settlement_flag=2
    and t.merchant_id=? and t.operator_id=? and t.batch_no=?
    """
	ps = conn.prepareStatement(query)
	ps.setInt(1,merchant_id)
	ps.setString(2,operator_id)
	ps.setString(3,batch_no)
	rs = ps.executeQuery()
	while rs.next():
		total_amount = rs.getDouble(1);
		return total_amount
	rs.close()
	ps.close()

def query_settlement_amount(conn, merchant_id, operator_id, batch_no):
	query = """
	select SALES_TOTAL from settlement_report@orcl t where
    t.merchant_id=? and t.operator_id=? and t.batch_no=? and t.game_id=?
    """
	ps = conn.prepareStatement(query)
	ps.setInt(1,merchant_id)
	ps.setString(2,operator_id)
	ps.setString(3,batch_no)
	ps.setString(4, lotto_game_id)
	rs = ps.executeQuery()
	while rs.next():
		total_amount = rs.getDouble(1);
		return total_amount
	rs.close()
	ps.close()

# test database connection
def query_actual_sale(conn, merchant_id, operator_id, batch_no):
	query = """
	select sum(i.total_amount) as total_amount
	from v_te_transaction@orcl t, v_te_ticket@orcl i where t.type=200 
    and t.ticket_serial_no=i.serial_no and i.status in (0,1,5) 
    and t.merchant_id=? and t.operator_id=? and t.batch_no=?
    """
	ps = conn.prepareStatement(query)
	ps.setInt(1,merchant_id)
	ps.setString(2,operator_id)
	ps.setString(3,batch_no)
	rs = ps.executeQuery()
	while rs.next():
		total_amount = rs.getDouble(1);
		return total_amount
	rs.close()
	ps.close()		
	
def connect():
	Class.forName(dbdriver)
	conn = DriverManager.getConnection(dburl,dbuser,dbpasswd)
	return conn
	
if __name__ == "__main__":
	#args = sys.argv
	#if len(args) != 3:
	#	print "[Usage] jython " + args[0] + " [input file] [output file]"
	#	sys.exit(0)
		
    main(input_file, output_file)
	
	