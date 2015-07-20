n#!/bin/sh

#--------------------------------------------------------------------------
# Analyze IGPE log, and find out all non-test batch of upload data. There 
# must be a manifest file named 'export_batchofupload.mf.txt' to list name
# of all log files. 
# * The shell script, manifest file, log files should be placed at same 
#   folder.
#--------------------------------------------------------------------------

# 
# Format of export_batchofupload.mf.txt:
# #2009/12
# jim.log.2009-12-18
# jim.log.2009-12-19
# jim.log.2009-12-20
#
mf_file="export_batchofupload.mf.txt"
# check if there is file 'export_batchofupload.mf.txt'
if [ ! -e ${mf_file} ]; then
	echo "can NOT find manifest file: $mf_file"
	exit 0
fi

# make a temporary directory
tmp_dir="./batch_of_upload"
if [ ! -d "$tmp_dir" ]; then
	echo "make a temporary directory: $tmp_dir";
	mkdir $tmp_dir
fi

begin=`date '+%Y-%m-%d %r'`
echo "#Begin time: ${begin}"

for logfile in `cat export_batchofupload.mf.txt`
do
	# \#* is a pattern to match each input line. If a line starts with #, ignore it.
	if [[ $logfile != \#* ]]; then
		for i in `cat $logfile |grep -n '<BatchOfUpload>'|awk -F : '{print $1}'`
		do
			bou=`head -n $i ${logfile} |tail -n 1`
			# check if the batchofupload is test data
			if [[ $bou == *TEST* ]]; then
				echo "[${logfile}:$i] a test batch of upload, ignore it. "
			else
				#get thread number
				t=`head -n $((i-1)) $logfile |tail -n 1|awk '{print $1}'`
				#echo thread=$t
				target_file=${tmp_dir}/${logfile}.${t}
				# write a thread temporary file
				# must use '${t} ' rather than '${t}' to grep, think about there are two thread 'Thread-1' and 'Thread-11'...
				cat $logfile|grep "${t} " >${target_file}.tmp
				# get terminalId, use sort|uniq to filter dulplicated line		
				dev=`cat ${target_file}.tmp|grep 'Terminal ID '|awk '{print $7 } '|sort|uniq`
				#echo dev=$dev
				operator=`cat ${target_file}.tmp|grep 'Operator ID '|awk '{print $7 }'|sort|uniq`
				#echo operator=$operator
				batch=`cat ${target_file}.tmp|grep 'Batch number '|awk '{print $7 }'|sort|uniq`
				#echo batch=$batch
				
				data_file=${target_file}.${dev}-${operator}-${batch}
				echo ${dev} >$data_file
				echo ${operator} >>$data_file
				echo ${batch} >>$data_file 
				echo $bou >$data_file
				echo "[${logfile}:$i] Finish exporting thread: ${t}"
			fi
		done	
		echo "Finish exporting log:${logfile}"	
		echo "-----------------------------------------------"	
	fi	
done

end=`date '+%Y-%m-%d %r'`
echo "#End time: ${end}"


