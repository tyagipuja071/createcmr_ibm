####################################################################

resultTCRUPD=$(ps -ef|grep -v grep|grep TCRServiceEntryPoint|awk '{print $2}')
if [ "$resultTCRUPD" = "" ]
then

. /cmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.TCRServiceEntryPoint UPD

else
echo TCR currently running
fi
