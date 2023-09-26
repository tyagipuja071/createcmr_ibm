####################################################################

resultKSC=$(ps -ef|grep -v grep|grep KscReportsEntryPoint|awk '{print $2}')
if [ "$resultKSC" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms128M -Xmx1024M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.KscReportsEntryPoint $1 $2

else
echo KSC Reports Service currently running
fi