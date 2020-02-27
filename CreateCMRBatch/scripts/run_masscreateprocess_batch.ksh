####################################################################

result=$(ps -ef|grep -v grep|grep MassCreateProcessEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M com.ibm.cmr.create.batch.entry.MassCreateProcessEntryPoint

else
echo Mass Create Processor currently running
fi
