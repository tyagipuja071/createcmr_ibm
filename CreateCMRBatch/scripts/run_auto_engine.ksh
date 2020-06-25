####################################################################

result=$(ps -ef|grep -v grep|grep AutomationServiceEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms128M -Xmx1024M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.AutomationServiceEntryPoint

else
echo Automation Engine Service currently running
fi