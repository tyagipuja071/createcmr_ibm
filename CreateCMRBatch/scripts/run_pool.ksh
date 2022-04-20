####################################################################

resultPool=$(ps -ef|grep -v grep|grep PoolEntryPoint|awk '{print $2}')
if [ "$resultPool" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.PoolEntryPoint

else
echo Pool batch currently running
fi

