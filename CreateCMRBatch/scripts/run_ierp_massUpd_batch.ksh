####################################################################

result=$(ps -ef|grep -v grep|grep IERPMassProcessEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /ci/shared/data/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M $JAVA_BATCH_ARGS -DBATCH_APP=IERPMassProcess com.ibm.cmr.create.batch.entry.IERPMassProcessEntryPoint

else
echo IERP Mass Update Service currently running
fi