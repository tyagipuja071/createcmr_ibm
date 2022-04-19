####################################################################

resultTCM=$(ps -ef|grep -v grep|grep TransConnMultiEntryPoint|awk '{print $2}')
if [ "$resultTCM" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.TransConnMultiEntryPoint

else
echo TransConnMulti currently running
fi

