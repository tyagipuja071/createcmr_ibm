####################################################################

resultTCR=$(ps -ef|grep -v grep|grep TCRServiceEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /cmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.TCRServiceEntryPoint EXT

else
echo TCR currently running
fi
