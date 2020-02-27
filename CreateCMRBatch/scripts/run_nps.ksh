####################################################################

result=$(ps -ef|grep -v grep|grep NPSEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M -DBATCH_APP=NPSSurvey com.ibm.cmr.create.batch.entry.NPSEntryPoint

else
echo Legacy Direct Service currently running
fi