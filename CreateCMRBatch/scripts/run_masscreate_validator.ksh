####################################################################

result=$(ps -ef|grep -v grep|grep MassCreateValidatorEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M com.ibm.cmr.create.batch.entry.MassCreateValidatorEntryPoint

else
echo Mass Create Validator currently running
fi

