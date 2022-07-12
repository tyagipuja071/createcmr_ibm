####################################################################

resultUSM=$(ps -ef|grep -v grep|grep USMultiServiceEntryPoint|awk '{print $2}')
if [ "$resultUSM" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.USMultiServiceEntryPoint

else
echo USMulti currently running
fi

