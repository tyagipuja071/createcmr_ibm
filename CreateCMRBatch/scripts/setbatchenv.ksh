#!/bin/ksh

export BATCH_HOME=/ci/shared/data/batch

export CMR_LIB=$BATCH_HOME/cmr_lib
export DB2_LIB=$BATCH_HOME/db2_lib
export WLP_LIB=$BATCH_HOME/wlp_lib
export CONFIG_DIR=/ci/shared/data/webconfig/createcmr


export CLASSPATH=$BATCH_HOME/application
export CLASSPATH=$CLASSPATH:$BATCH_HOME/templates

export CLASSPATH=$CLASSPATH:$BATCH_HOME/CreateCMRBatch.jar

export CLASSPATH=$CLASSPATH:$DB2_LIB/*
export CLASSPATH=$CLASSPATH:$WLP_LIB/*
export CLASSPATH=$CLASSPATH:$CMR_LIB/*

export CLASSPATH=$CLASSPATH:$CONFIG_DIR

export JAVA_BATCH_ARGS="-Dcom.ibm.jsse2.overrideDefaultTLS=true -Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2"

echo JAVA Arguments: $JAVA_BATCH_ARGS

# export PATH=$JAVA_HOME/bin:$PATH



