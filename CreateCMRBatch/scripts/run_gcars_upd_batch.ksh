####################################################################

resultGCARS=$(ps -ef|grep -v grep|grep GCARSServiceEntryPoint UPD|awk '{print $2}')
if [ "$resultGCARS" = "" ]
then

. /cmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.GCARSServiceEntryPoint UPD

else
echo GCARS Update currently running
fi
