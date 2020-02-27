#!/bin/ksh

export WAS_HOME=/opt/IBM/WebSphere/AppServer
export JAVA_HOME=$WAS_HOME/java_1.7.1_64/jre

export BATCH_HOME=/home/createcmr/batch

export CREATE_CMR="$WAS_HOME/profiles/AppSrv01/installedApps/sbybz2029Cell01/Create CMR.ear/RequestCMR.war/WEB-INF"
export CONFIG_DIR=/home/createcmr/properties


export CLASSPATH=$BATCH_HOME/application
export CLASSPATH=$CLASSPATH:$BATCH_HOME/templates

export CLASSPATH=$CLASSPATH:$BATCH_HOME/CreateCMRBatch.jar

export CLASSPATH=$CLASSPATH:$WAS_HOME/plugins/com.ibm.ws.prereq.jackson.jar
export CLASSPATH=$CLASSPATH:$WAS_HOME/plugins/com.ibm.ws.prereq.jaxrs.jar
export CLASSPATH=$CLASSPATH:$WAS_HOME/installedConnectors/wmq.jmsra.rar/com.ibm.mqjms.jar

export CLASSPATH=$CLASSPATH:"$CREATE_CMR/lib/*"

export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/*
export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/ext/*
export CLASSPATH=$CLASSPATH:$JAVA_HOME/lib/ext/*
export CLASSPATH=$CLASSPATH:$WAS_HOME/plugins/*
export CLASSPATH=$CLASSPATH:$WAS_HOME/endorsed_apis/*
export CLASSPATH=$CLASSPATH:$WAS_HOME/dev/JavaEE/6.0/*

export CLASSPATH=$CLASSPATH:$CONFIG_DIR

export DB2UNIVDRIVER=$WAS_HOME/deploytool/itp/plugins/com.ibm.datatools.db2_2.1.110.v20121008_1514/driver

export CLASSPATH=$CLASSPATH:$DB2UNIVDRIVER/db2jcc.jar
export CLASSPATH=$CLASSPATH:$DB2UNIVDRIVER/db2jcc_license_cisuz.jar
export CLASSPATH=$CLASSPATH:$DB2UNIVDRIVER/db2jcc_license_cu.jar

export PATH=$JAVA_HOME/bin:$PATH



