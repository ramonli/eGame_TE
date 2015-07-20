#!/bin/sh

log=activity.log
t=`date '+%Y-%m-%d %r'`
echo "# begin time: ${t}" >$log

i=0
for f in `ls -lt jim.log.*|awk '{print $8}'`
do
	dev=`head -n 1 $f|tail -n 1`
	# disable trailing newline
	echo -n "${dev}-" >>$log
	op=`head -n 2 $f|tail -n 1`
	echo -n "${op}-" >>$log
	batch=`head -n 3 $f|tail -n 1`
	echo "${batch}" >>$log
	
	i=$((i+1))
	echo "$i:Finish ${f}."
done

t=`date '+%Y-%m-%d %r'`
echo "# end time: ${t}" >>$log
