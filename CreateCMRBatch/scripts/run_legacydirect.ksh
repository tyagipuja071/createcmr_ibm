####################################################################

result=$(ps -ef|grep -v grep|grep LegacyDirectEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M -DBATCH_APP=LegacyDirect com.ibm.cmr.create.batch.entry.LegacyDirectEntryPoint

else
echo Legacy Direct Service currently running
fi