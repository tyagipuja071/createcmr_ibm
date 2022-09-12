####################################################################

resultREQM=$(ps -ef|grep -v grep|grep RequestMaintEntryPoint|awk '{print $2}')
if [ "$resultREQM" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.RequestMaintEntryPoint

else
echo Request Maint currently running
fi

