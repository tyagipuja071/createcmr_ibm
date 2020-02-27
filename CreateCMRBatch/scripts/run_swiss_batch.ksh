####################################################################

result=$(ps -ef|grep -v grep|grep SWISSServiceEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M com.ibm.cmr.create.batch.entry.SWISSServiceEntryPoint

else
echo SWISS currently running
fi

