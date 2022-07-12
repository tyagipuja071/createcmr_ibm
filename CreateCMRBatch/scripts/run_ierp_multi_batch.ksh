####################################################################

resultIERPM=$(ps -ef|grep -v grep|grep IERPServiceEntryPoint|awk '{print $2}')
if [ "$resultIERPM" = "" ]
then

. /cmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.IERPServiceEntryPoint MULTI

else
echo IerpMulti currently running
fi

