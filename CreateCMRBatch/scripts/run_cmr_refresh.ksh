####################################################################

resultCMR=$(ps -ef|grep -v grep|grep CMRRefreshEntryPoint|awk '{print $2}')
if [ "$resultCMR" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.CMRRefreshEntryPoint $@

else
echo CMRRefreshPoint running
fi

