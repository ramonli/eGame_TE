#!/bin/sh

#----------------------------------------------------
# Export log data of a given transaction type.
#----------------------------------------------------

if [ $# != 2 ]; then
    echo "Usage: ./operator_activity.sh [full path log file] [transaction type]"
    exit 0
fi

logfile=$1
transtype=$2

# check if the $logfile exists

# export a threads list temporary file.
keyword=" Value = ${transtype}"
tmp_file="transaction_activity.tmp"
for i in `cat $logfile | grep " Tag = 0x0004, " |grep "${keyword}" | awk '{print $1}'`; do  
    echo $i >> $tmp_file
done

# prepare operator activity log file
trans_activity_file=${logfile}.transtype_$transtype

# looks like nest loop won't work, or we can find the XML data.
# write a temporary file
# remove duplicated lines and keep the original running order of threads
for t in `cat ${tmp_file} | uniq`; do   
    cat $logfile | grep -n "$t " >>${trans_activity_file}
    echo "-----------------------------------" >>${trans_activity_file}
    echo "Exported $t"
done

echo "Finish export transactions of $transtype to file:${trans_activity_file}"