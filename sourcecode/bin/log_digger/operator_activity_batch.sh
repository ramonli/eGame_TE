#!/bin/sh
#-------------------------------------------------------
# Exports all logs of all operators from a index file.
#-------------------------------------------------------

if [ $# != 1 ]; then
	echo "Usage: ./operator_activity.sh [full path index file]"
	exit 0
fi

# The content of index file must follow below format:
# ${LOG_FILE_NAME_1},${OPERATOR_LOGIN_NAME_1}
# ${LOG_FILE_NAME_1},${OPERATOR_LOGIN_NAME_2}
# ${LOG_FILE_NAME_2},${OPERATOR_LOGIN_NAME_1}
index=$1
# IFS: Internal Field Separator, the default value is "<tab><space><newline>". Here we specify new line as 
# the field seperator(IFS=$'\n'), or maybe some commands, sucs as 'cat', will seperate field by <space>.
IFS=$'\n'

begin_time=`date '+%Y-%m-%d %r'`

for l in `cat ${index}`; do
	logfile=`echo $l|awk -F ',' '{print $1}'`
	op=`echo $l|awk -F ',' '{print $2}'`
	
	# check if the log file exist
	#if [ ! -e ${logfile} ]; then
	#	echo "can NOT find log file: $logfile"
	#	# cygwin doesn't support this command?
	#	continute
	#fi
	
	# prepare operator activity log file
	op_activity_file=operator_activity/${logfile}.op_$op
	echo "# Activity of operator:$op" >${op_activity_file}

	# looks like nest loop won't work, or we can find the XML data.
	# write a temporary file
	keyword=" Value = $op"
	#echo "cat ${logfile} |grep "Tag = 0x0005, "|grep "${keyword}" | awk '{print \$1}'"
	for t in `cat ${logfile} |grep "Tag = 0x0005, "|grep "${keyword}" | awk '{print $1}'`; do
		cat $logfile | grep -n "$t" >>${op_activity_file}
		echo "-----------------------------------" >>${op_activity_file}
		echo "Exported $t"
	done;	#Don't forget this ';'

	echo "Finish export operator(${op}) activity to file:${op_activity_file}"	
done	

end_time=`date '+%Y-%m-%d %r'`
echo "Elapsed Time:${begin_time} - ${end_time}"
