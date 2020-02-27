####################################################################

. /home/createcmr/batch/setbatchenv.ksh

echo Classpath = $CLASSPATH

java -Xms64M -Xmx512M com.ibm.cmr.create.batch.entry.CheckCertificatesEntryPoint
