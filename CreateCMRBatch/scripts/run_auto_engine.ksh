####################################################################

result=$(ps -ef|grep -v grep|grep AutomationServiceEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms128M -Xmx1024M -DBATCH_APP=LegacyDirect com.ibm.cmr.create.batch.entry.AutomationServiceEntryPoint

else
echo Automation Engine Service currently running
fi