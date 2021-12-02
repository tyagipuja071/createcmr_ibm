####################################################################

resultSwissM=$(ps -ef|grep -v grep|grep SWISSMassProcessEntryPoint|awk '{print $2}')
if [ "$resultSwissM" = "" ]
then

. /cmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.SWISSMassProcessEntryPoint MULTI

else
echo SWISSMassProcessEntryPoint MULTI running
fi

