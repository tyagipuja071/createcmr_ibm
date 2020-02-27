####################################################################

result=$(ps -ef|grep -v grep|grep MQServiceEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M -DBATCH_APP=SOFSub com.ibm.cmr.create.batch.entry.MQServiceEntryPoint get

else
echo MQ Service currently running
fi