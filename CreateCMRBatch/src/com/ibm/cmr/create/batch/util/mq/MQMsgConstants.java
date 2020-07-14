package com.ibm.cmr.create.batch.util.mq;

import java.text.SimpleDateFormat;

public class MQMsgConstants {

  public static final String FLAG_NO = "N";
  public static final String FLAG_YES = "Y";

  public static final String REQ_TYPE_CREATE = "C";
  public static final String REQ_TYPE_UPDATE = "U";
  public static final String REQ_TYPE_DELETE = "D";

  public static final String REQ_TYPE_CU = "C,U";
  public static final String PROCESSED_FLAG_WX = "Wx";

  public static final String PROCESSED_FLAG_Y = "Y";
  public static final String PROCESSED_FLAG_E = "E";
  public static final String REQ_STATUS_PPN = "PPN";
  public static final String REQ_STATUS_PCP = "PCP";

  // published
  public static final String REQ_STATUS_PUB = "PUB";
  // new
  public static final String REQ_STATUS_NEW = "NEW";
  // completed
  public static final String REQ_STATUS_COM = "COM";
  // error
  public static final String REQ_STATUS_SER = "SER";
  // intermediate retry
  public static final String REQ_STATUS_RETRY = "RETRY";
  // full resend
  public static final String REQ_STATUS_RESEND = "RESEND";

  // waiting for final reply
  public static final String REQ_STATUS_WAIT = "WAIT";

  public static final String CUSTSUBGRP_BUSPR = "BUSPR";
  public static final String CUSTSUBGRP_BUSSM = "BUSSM";
  public static final String CUSTSUBGRP_BUSVA = "BUSVA";
  public static final String CUSTSUBGRP_PRICU = "PRICU";
  public static final String CUSTSUBGRP_CROSS = "CROSS";
  public static final String CUSTSUBGRP_INFSL = "INFSL";
  public static final String CUSTSUBGRP_COMLC = "COMLC";

  public static final String CUSTSUBGRP_COMME = "COMME";
  public static final String CUSTSUBGRP_BPIEU = "BPIEU";
  public static final String CUSTSUBGRP_BPUEU = "BPUEU";
  public static final String CUSTSUBGRP_GOVRN = "GOVRN";
  public static final String CUSTSUBGRP_INTSO = "INTSO";
  public static final String CUSTSUBGRP_LCIFF = "LCIFF";
  public static final String CUSTSUBGRP_LCIFL = "LCIFL";
  public static final String CUSTSUBGRP_LEASE = "LEASE";
  public static final String CUSTSUBGRP_OTFIN = "OTFIN";
  public static final String CUSTSUBGRP_LCOEM = "LCOEM";
  public static final String CUSTSUBGRP_FIBAB = "FIBAB";
  public static final String CUSTSUBGRP_HOSTC = "HOSTC";
  public static final String CUSTSUBGRP_THDPT = "THDPT";
  public static final String CUSTSUBGRP_IBMEM = "IBMEM";

  public static final String CUSTSUBGRP_CBMME = "CBMME";
  public static final String CUSTSUBGRP_CBIEU = "CBIEU";
  public static final String CUSTSUBGRP_CBUEU = "CBUEU";
  public static final String CUSTSUBGRP_CBICU = "CBICU";
  public static final String CUSTSUBGRP_CBVRN = "CBVRN";
  public static final String CUSTSUBGRP_CBTER = "CBTER";
  public static final String CUSTSUBGRP_CBTSO = "CBTSO";
  public static final String CUSTSUBGRP_CBIFF = "CBIFF";
  public static final String CUSTSUBGRP_CBIFL = "CBIFL";
  public static final String CUSTSUBGRP_CBFIN = "CBFIN";
  public static final String CUSTSUBGRP_CBASE = "CBASE";
  public static final String CUSTSUBGRP_CBOEM = "CBOEM";
  public static final String CUSTSUBGRP_CBBAB = "CBBAB";
  public static final String CUSTSUBGRP_CBSTC = "CBSTC";
  public static final String CUSTSUBGRP_CBDPT = "CBDPT";
  public static final String CUSTSUBGRP_CBIEM = "CBIEM";

  public static final String CUSTSUBGRP_INTER = "INTER";

  public static final String CUSTGRP_BLUEMIX = "BLUEMIX";
  public static final String CUSTGRP_MKPLA = "MARKETPLACE";

  public static final String ADDR_ZS01 = "ZS01";
  public static final String ADDR_ZP01 = "ZP01";
  public static final String ADDR_ZI01 = "ZI01";
  public static final String ADDR_ZD01 = "ZD01";
  public static final String ADDR_ZS02 = "ZS02";
  public static final String ADDR_ZP02 = "ZP02";
  public static final String ADDR_ZP03 = "ZP03";
  public static final String ADDR_ZD02 = "ZD02";
  public static final String ADDR_CTYA = "CTYA";
  public static final String ADDR_CTYB = "CTYB";
  public static final String ADDR_CTYC = "CTYC";

  public static final String MQ_APP_USER = "MQCreateCMR";
  public static final String MQ_TARGET_SYS = "MQ-Intf";

  public static final String MQ_OPERATION_C = "create";
  public static final String MQ_OPERATION_U = "update";
  public static final String MQ_OPERATION_D = "delete";

  public static final String XMLMSG_RDCTOSOF = "rdctosof";
  public static final String XMLMSG_DOCUMENT = "document";
  public static final String XMLMSG_ITEM = "item";
  public static final String XMLMSG_NAME = "name";
  public static final String XMLMSG_DATETIME = "datetime";
  public static final String XMLMSG_UPDATEDBY = "updatedby";
  public static final String XMLMSG_DST = "dst";
  public static final String XMLMSG_TEXT = "text";
  public static final String XMLMSG_UNIQUENUMBER = "UniqueNumber";
  public static final String XMLMSG_XML_DOCUMENTNUMBER = "XML_DocumentNumber";
  public static final String XMLMSG_GET = "get";
  public static final String XMLMSG_SET = "set";
  public static final String XMLMSG_FORM = "form";
  public static final String XMLMSG_XMLDOC = "xmlDoc";
  public static final String XML_DOCUMENTNUMBER_1 = "1";

  public static final String SOF_STATUS_SUCCESS = "CRP";
  public static final String SOF_STATUS_FAIL = "CRE";
  public static final String SOF_TRANSACTION_MODIFY = "M";
  public static final String SOF_TRANSACTION_NEW = "N";
  public static final String SOF_STATUS_ANA = "ANA";

  public static final String SOF_ADDRESS_USE_MAILING = "1";
  public static final String SOF_ADDRESS_USE_BILLING = "2";
  public static final String SOF_ADDRESS_USE_FR_BILLING = "2B";
  public static final String SOF_ADDRESS_USE_INSTALLING = "3";
  public static final String SOF_ADDRESS_USE_SHIPPING = "4";
  public static final String SOF_ADDRESS_USE_EPL = "5";
  public static final String SOF_ADDRESS_USE_COUNTRY_USE_A = "A";
  public static final String SOF_ADDRESS_USE_COUNTRY_USE_B = "B";
  public static final String SOF_ADDRESS_USE_COUNTRY_USE_C = "C";
  public static final String SOF_ADDRESS_USE_FISCAL = "F";
  public static final String SOF_ADDRESS_USE_COUNTRY_USE_G = "G";
  public static final String SOF_ADDRESS_USE_COUNTRY_USE_H = "H";

  public static final String MQ_QUEUE = "QUEUE";
  public static final String MQ_QUEUE_MANAGER = "QUEUE_MANAGER";
  public static final String MQ_HOST_NAME = "HOST_NAME";
  public static final String MQ_PORT = "PORT";
  public static final String MQ_CHANNEL = "CHANNEL";
  public static final String MQ_USER_ID = "USER_ID";
  public static final String MQ_PASSWORD = "PASSWORD";
  public static final String MQ_QUEUE_OUTPUT = "OUTPUT";
  public static final String MQ_QUEUE_INPUT = "INPUT";
  public static final String MQ_CIPHER = "CIPHER";

  public static final String WH_HIST_FORCED = "Forced Status Change";
  public static final String WH_REJECT = "Reject";
  public static final String WH_COMMENT_REJECT = "Error(s) occurred during processing. Please check the request's comment log for details.";
  public static final String WH_HIST_COM = "All Processing Complete";
  public static final String WH_HIST_CMT_COM = "Successfully completed all processing. Assigned CMR No: ";
  public static final String WH_HIST_CMT_COM_UPDATE = "Successfully completed all processing. Updated CMR No: ";

  public static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
  public static final SimpleDateFormat FILE_TIME_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
  public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd");
  public static final String[] MESSAGE_FRONT_ELEMENTS = new String[] { "created", "modified", "revised", "lastaccessed", "addedtofile", "updatedby",
      "revisions" };

  public static final String NATURE_CLIENT = "NatureClient";
  public static final String SIGLE_IDENTIF = "SigleIdentificateur";
  public static final String AUTH_REMARK = "ARemark";
  public static final String OVERSEAS_TERRITORY = "SOFOverSeasTerritory";
  public static final String DEPARTMENT_CODE = "DepartmentCode";
  public static final String SIRET = "Siret";
  public static final String CODE_APE = "CodeApe";
  public static final String TLSPE = "TopListeSpeciale";
  public static final String TAPAR = "TarifParticulier";
  public static final String AFFAC = "Affacturage";
  public static final String PENALTIESDERETARD = "PenaltiesDeRetard";
  public static final String TYPEDEFACTURATION = "TypeDeFacturation";
  public static final String SOFTYPEOFALLIANCE = "SOFTypeOfAlliance";
  public static final String DATEOFORIGINALAGREEMENT = "DateOfOriginalAgreement";

}
