####################################################################

resultSWM=$(ps -ef|grep -v grep|grep SwissMultiServiceEntryPoint|awk '{print $2}')
if [ "$resultSWM" = "" ]
then

. /cmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.SwissMultiServiceEntryPoint

else
echo SwissMulti currently running
fi

