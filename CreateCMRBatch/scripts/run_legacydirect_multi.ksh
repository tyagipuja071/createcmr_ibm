####################################################################

resultLDM=$(ps -ef|grep -v grep|grep LegacyDirectMultiEntryPoint|awk '{print $2}')
if [ "$resultLDM" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.LegacyDirectMultiEntryPoint

else
echo LegacyDirectMulti currently running
fi

