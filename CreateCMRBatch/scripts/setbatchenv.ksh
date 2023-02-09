#!/bin/ksh

export BATCH_HOME=/cmr/batch
export JAVA_HOME=/opt/ibm/ibm-java-x86_64-80/jre

export CMR_LIB=$BATCH_HOME/cmr_lib
export DB2_LIB=$BATCH_HOME/db2_lib
export WLP_LIB=$BATCH_HOME/wlp_lib
export CONFIG_DIR=/ci/shared/data/webconfig/createcmr
export COV_PROPS=/cmr/coverage/props


export ciwebconfig=$CONFIG_DIR
export CLASSPATH=$BATCH_HOME/application
export CLASSPATH=$CLASSPATH:$COV_PROPS

export CLASSPATH=$CLASSPATH:$BATCH_HOME/templates

export CLASSPATH=$CLASSPATH:$BATCH_HOME/CreateCMRBatch.jar

export CLASSPATH=$CLASSPATH:$DB2_LIB/*
export CLASSPATH=$CLASSPATH:$WLP_LIB/*
export CLASSPATH=$CLASSPATH:$CMR_LIB/*

export CLASSPATH=$CLASSPATH:$CONFIG_DIR

export JAVA_BATCH_ARGS="-Dciwebconfig=/ci/shared/data/webconfig/createcmr -Dcom.ibm.jsse2.overrideDefaultTLS=true -Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2 -Dcmr.home=/ci/shared/data -Dcom.ibm.ed.bpjtk.http.api.locator.url=https://bluepages.ibm.com/BpHttpApisv3/apilocator -Dservice.id=CreateCMR -Dservice.password=k03wQ@lX -Devs.appID=CreateCMR -Dfcmr.pwd=z19pH8rT -DGCARS_FTP_HOST=$GCARS_FTP_HOST -DGCARS_FTP_USER=$GCARS_FTP_USER -DGCARS_FTP_PASS=$GCARS_FTP_PASS -DGCARS_FTP_KNOWN_HOSTS=$GCARS_FTP_KNOWN_HOSTS"

echo JAVA Arguments: $JAVA_BATCH_ARGS

export PATH=$JAVA_HOME/bin:$PATH



