####################################################################

resultTCR=$(ps -ef|grep -v grep|grep TCRServiceEntryPoint|awk '{print $2}')
if [ "$resultTCR" = "" ]
then

. /cmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.TCRServiceEntryPoint

else
echo TCR currently running
fi
