####################################################################

result=$(ps -ef|grep -v grep|grep MQServiceEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS -DBATCH_APP=SOFPub com.ibm.cmr.create.batch.entry.MQServiceEntryPoint put

else
echo MQ Service currently running
fi