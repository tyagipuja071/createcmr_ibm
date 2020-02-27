####################################################################

result=$(ps -ef|grep -v grep|grep ATServiceEntryPoint|awk '{print $2}')
if [ "$result" = "" ]
then

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M com.ibm.cmr.create.batch.entry.ATServiceEntryPoint MASS

else
echo AUSTRIA Mass Update currently running
fi

