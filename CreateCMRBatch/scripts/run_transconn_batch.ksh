####################################################################

result=$(ps -ef|grep -v grep|grep TransConnEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M com.ibm.cmr.create.batch.entry.TransConnEntryPoint

else
echo TransConn currently running
fi

