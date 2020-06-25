####################################################################

result=$(ps -ef|grep -v grep|grep MassCreateProcessEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.MassCreateProcessEntryPoint

else
echo Mass Create Processor currently running
fi
