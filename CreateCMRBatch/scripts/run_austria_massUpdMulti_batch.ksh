####################################################################

resultATMASS=$(ps -ef|grep -v grep|grep ATMassProcessEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS com.ibm.cmr.create.batch.entry.ATMassProcessEntryPoint MULTI

else
echo Austria Mass Update Multi Threaded currently running
fi