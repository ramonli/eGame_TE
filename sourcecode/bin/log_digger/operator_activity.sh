#!/bin/sh
#-------------------------------------------------
# Exports all logs of a given operator.
#-------------------------------------------------

if [ $# != 2 ]; then
    echo "Usage: ./operator_activity.sh [full path log file] [operator login name]"
    exit 0
fi

logfile=$1
# remove the space from head and tail.
op=`echo $2 | awk 'gsub(/^ *| *$/,"")'`

# check if the $logfile exists?

# looks like nest loop won't work, or we can find the XML data.
# write a temporary file
keyword=" Value = $op"
threads_list="threads_list.tmp"
for i in `cat $logfile |grep "Tag = 0x0005, "|grep "${keyword}" | awk '{print $1}'`; do 
    echo $i >> $threads_list
done
echo "Exported threads list file:$threads_list"

# prepare operator activity log file
op_activity_file=${logfile}.op_$op
echo "# Activity of operator:$op" >${op_activity_file}
# remove duplicated lines and keep the original running order of threads
for t in `cat ${threads_list} | uniq`; do       
    # the grep expression must be "${t} ", not "${t}", otherwise, for example 'thread-27' and 
    # 'thread-270' will both be exported.
    cat $logfile | grep -n "${t} " >>${op_activity_file}
    echo "-----------------------------------" >>${op_activity_file}
    echo "Exported $t"
done

echo "Finish export operator(${op}) activity to file:${op_activity_file}"