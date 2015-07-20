x#!/bin/sh

if [ $# != 4 ]; then
	echo "Usage: ./te_access_find_message.sh [full path log file] [batch_no] [operator_id] [trans_type]"
	exit 0
fi

log_file=$1
batch_no=$2
operator_id=$3
trans_type=$4
# IFS: Internal Field Separator, the default value is "<tab><space><newline>". Here we specify new line as 
# the field seperator(IFS=$'\n'), or maybe some commands, sucs as 'cat', will seperate field by <space>.
IFS=$'\n'

# check if the $logfile exists

for i in `cat ${log_file}|grep -n "Trans-BatchNumber:${batch_no}"|grep "Operator-Id:${operator_id}"|grep "Transaction-Type:${trans_type}"`; do	
	for t in `echo $i`; do
		#echo $t
		line_num=`echo $t|awk -F ':' '{print $1}'`
		line=$((line_num+1))
		head -n $line ${log_file}|tail -n 2
	done;
done
