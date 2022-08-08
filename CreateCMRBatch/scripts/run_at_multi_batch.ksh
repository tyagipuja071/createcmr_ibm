####################################################################

resultATM=$(ps -ef|grep -v grep|grep ATServiceMultiEntryPoint|awk '{print $2}')
if [ "$resultATM" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.ATServiceMultiEntryPoint

else
echo ATMulti currently running
fi

