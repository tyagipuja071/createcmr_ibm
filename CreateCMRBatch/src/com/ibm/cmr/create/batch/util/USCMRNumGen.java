package com.ibm.cmr.create.batch.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.config.SystemConfiguration;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;
import com.ibm.cio.cmr.request.util.SystemLocation;

public class USCMRNumGen {
  private static final Logger LOG = Logger.getLogger(USCMRNumGen.class);
  public static HashMap<String, ArrayList<String>> cmrNumMap = null;
  public static HashMap<String, ArrayList<String>> cmrNumMapMassCrt = null;
  private static String reGenCMRFlag = "N";

  public static synchronized String genCMRNum(EntityManager entityManager, String type) {
    String cmrNum = "";
    if (cmrNumMap == null || cmrNumMap.isEmpty() || "Y".equals(reGenCMRFlag)) {
      LOG.info("there is no CMR number stored in cache, so init...");
      init(entityManager);
    }

    if ("POA".equals(type)) {
      ArrayList<String> poaList = cmrNumMap.get("POA");
      if (poaList == null || poaList.isEmpty()) {
        LOG.info("no POA CMR number stored in cache, so generate...");
        poaList = getPOANumList(entityManager);
      }
      cmrNum = poaList.remove(0);
      LOG.info("return CMR number:" + cmrNum + ", there are " + poaList.size() + " poa CMR Number left in cache...");
    } else if ("COMM".equals(type)) {
      ArrayList<String> commList = cmrNumMap.get("COMM");
      if (commList == null || commList.isEmpty()) {
        commList = getCommonNumList(entityManager);
      }
      cmrNum = commList.remove(0);
      LOG.info("return CMR number:" + cmrNum + ", there are " + commList.size() + " common CMR Number left in cache...");
    } else if ("MAIN".equals(type)) {
      ArrayList<String> mainList = cmrNumMap.get("MAIN");
      if (mainList == null || mainList.isEmpty()) {
        mainList = getMainNmNumList(entityManager);
      }
      cmrNum = mainList.remove(0);
      LOG.info("return CMR number:" + cmrNum + ", there are " + mainList.size() + " Main Name CMR Number left in cache...");
    }

    String querySql = ExternalizedQuery.getSql("BATCH.GET.KNA1_MANDT_CMRNO");
    PreparedQuery query = new PreparedQuery(entityManager, querySql);
    query.setParameter("KATR6", SystemLocation.UNITED_STATES);
    query.setParameter("CMR_NO", cmrNum);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setForReadOnly(true);
    boolean nonExisted = query.getResults().isEmpty();
    while (!nonExisted) {
      LOG.info(" CMR number:" + cmrNum + " already existed, re-generate CMR number again.");
      reGenCMRFlag = "Y";
      cmrNum = genCMRNum(entityManager, type);
    }
    reGenCMRFlag = "N";

    return cmrNum;
  }
  
  public static synchronized String genCMRNumMassCrt(EntityManager entityManager, String type) {
	    String cmrNum = "";
	    if (cmrNumMapMassCrt == null || cmrNumMapMassCrt.isEmpty()) {
	      LOG.info("there is no CMR number stored in cache, so init...");
	      initMassCrt(entityManager);
	    }

	    if ("POA".equals(type)) {
	      ArrayList<String> poaList = cmrNumMapMassCrt.get("POA");
	      if (poaList == null || poaList.isEmpty()) {
	        LOG.info("no POA CMR number stored in cache, so generate...");
	        poaList = getPOANumListMassCrt(entityManager);
	      }
	      cmrNum = poaList.remove(0);
	      LOG.info("return CMR number for Mass Create:" + cmrNum + ", there are " + poaList.size() + " poa CMR Number left in cache...");
	    } else if ("COMM".equals(type)) {
	      ArrayList<String> commList = cmrNumMapMassCrt.get("COMM");
	      if (commList == null || commList.isEmpty()) {
	        commList = getCommonNumListMassCrt(entityManager);
	      }
	      cmrNum = commList.remove(0);
	      LOG.info("return CMR number for Mass Create:" + cmrNum + ", there are " + commList.size() + " common CMR Number left in cache...");
	    } else if ("MAIN".equals(type)) {
	      ArrayList<String> mainList = cmrNumMapMassCrt.get("MAIN");
	      if (mainList == null || mainList.isEmpty()) {
	        mainList = getMainNmNumListMassCrt(entityManager);
	      }
	      cmrNum = mainList.remove(0);
	      LOG.info("return CMR number for Mass Create:" + cmrNum + ", there are " + mainList.size() + " Main Name CMR Number left in cache...");
	    }

	    String querySql = ExternalizedQuery.getSql("BATCH.GET.KNA1_MANDT_CMRNO");
	    PreparedQuery query = new PreparedQuery(entityManager, querySql);
	    query.setParameter("KATR6", SystemLocation.UNITED_STATES);
	    query.setParameter("CMR_NO", cmrNum);
	    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
	    query.setForReadOnly(true);
	    boolean nonExisted = query.getResults().isEmpty();
	    while (!nonExisted) {
	      LOG.info(" CMR number:" + cmrNum + " already existed, re-generate CMR number again.");
	      cmrNum = genCMRNumMassCrt(entityManager, type);
	    }

	    return cmrNum;
	  }

  public static synchronized void init(EntityManager entityManager) {
	  if (cmrNumMap == null || cmrNumMap.isEmpty() || "Y".equals(reGenCMRFlag)) {
	      LOG.info("there is no CMR number stored in cache, so init...");
	     
    cmrNumMap = new HashMap<String, ArrayList<String>>();
    ArrayList<String> poaList = getPOANumList(entityManager);
    ArrayList<String> commonList = getCommonNumList(entityManager);
    ArrayList<String> mainList = getMainNmNumList(entityManager);
    cmrNumMap.put("POA", poaList);
    cmrNumMap.put("COMM", commonList);
    cmrNumMap.put("MAIN", mainList);
	  }
  }
  
  public static synchronized void initMassCrt(EntityManager entityManager) {
	  
	  if (cmrNumMapMassCrt == null || cmrNumMapMassCrt.isEmpty()) {
	      LOG.info("there is no CMR number stored for mass create in cache, so init...");
	      	  
	    cmrNumMapMassCrt = new HashMap<String, ArrayList<String>>();
	    ArrayList<String> poaListMassCrt = getPOANumListMassCrt(entityManager);
	    ArrayList<String> commonListMassCrt = getCommonNumListMassCrt(entityManager);
	    ArrayList<String> mainListMassCrt = getMainNmNumListMassCrt(entityManager);
	    cmrNumMapMassCrt.put("POA", poaListMassCrt);
	    cmrNumMapMassCrt.put("COMM", commonListMassCrt);
	    cmrNumMapMassCrt.put("MAIN", mainListMassCrt);
	  }
  }

  private static ArrayList<String> getPOANumList(EntityManager entityManager) {

    String cndCMR = "";
    ArrayList<String> cndCMRList = new ArrayList<String>();

    // String sql =
    // ExternalizedQuery.getSql("BATCH.GET.KNA1_ZZKV_CUSNO.US_FIND_MISSINGCMRNO");
    // query.setParameter("MANDT1", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR61", SystemLocation.UNITED_STATES);
    // query.setParameter("MANDT2", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR62", SystemLocation.UNITED_STATES);
    // query.setParameter("ZZKV_CUSNO1", 9 + "%");
    // query.setParameter("ZZKV_CUSNO2", 9 + "%");
    // List<String> records = query.getResults(String.class);

    String sql = ExternalizedQuery.getSql("BATCH.US_CMR_NO.GENERATE");
    int min = 9000001;
    int max = 9999999;

    sql = StringUtils.replaceOnce(sql, "$MIN", min + "");
    sql = StringUtils.replaceOnce(sql, "$MAX", max + "");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("LOC", SystemLocation.UNITED_STATES);
    List<String> records = query.getResults(300, String.class);

    if (records != null && records.size() > 0) {
      for (String missCmrNo : records) {
        if (missCmrNo != null) {
          // cndCMR = String.format("%07d", Integer.valueOf(missCmrNo) + 1);
          cndCMR = String.format("%07d", Integer.valueOf(missCmrNo));
          cndCMRList.add(cndCMR);
        }
      }
      cndCMRList.sort(Comparator.naturalOrder());
    }
    return cndCMRList;

  }

  private static ArrayList<String> getPOANumListMassCrt(EntityManager entityManager) {

    String cndCMR = "";
    ArrayList<String> cndCMRList = new ArrayList<String>();

    int counts = 0;
    String sqlc = "SELECT count(*) FROM creqcmr.MASS_CREATE WHERE PAR_REQ_ID IN (SELECT REQ_ID FROM creqcmr.ADMIN WHERE REQ_TYPE = 'N' AND RDC_PROCESSING_STATUS = 'A')";
    Query q = entityManager.createNativeQuery(sqlc);
    counts = (int) q.getSingleResult() + 50;

    String sql = ExternalizedQuery.getSql("BATCH.US_CMR_NO.GENERATE");
    int min = 9600001;
    int max = 9699999;
    LOG.info(" NEW Query for Mass Crt is " + sql + "the counts = " + counts);
    sql = StringUtils.replaceOnce(sql, "$MIN", min + "");
    sql = StringUtils.replaceOnce(sql, "$MAX", max + "");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("LOC", SystemLocation.UNITED_STATES);
    List<String> records = query.getResults(counts, String.class);

    // String sql =
    // ExternalizedQuery.getSql("BATCH.GET.KNA1_ZZKV_CUSNO.US_FIND_MISSINGCMRNO_PART");
    // String sql1 = sql + " FETCH FIRST " + counts + " ROWS ONLY WITH UR";
    // LOG.info(" NEW Query for Mass Crt is " + sql1 + "the counts = " +
    // counts);
    // LOG.info(" The counts = " + counts);
    //
    // PreparedQuery query = new PreparedQuery(entityManager, sql1);
    //
    // query.setParameter("MANDT1", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR61", SystemLocation.UNITED_STATES);
    // query.setParameter("MANDT2", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR62", SystemLocation.UNITED_STATES);
    //
    // query.setParameter("ZZKV_CUSNO1", 96 + "%");
    // query.setParameter("ZZKV_CUSNO2", 96 + "%");
    // List<String> records = query.getResults(String.class);
    if (records != null && records.size() > 0) {
      for (String missCmrNo : records) {
        if (missCmrNo != null) {
          // cndCMR = String.format("%07d", Integer.valueOf(missCmrNo) + 1);
          cndCMR = String.format("%07d", Integer.valueOf(missCmrNo));
          cndCMRList.add(cndCMR);
        }
      }
      cndCMRList.sort(Comparator.naturalOrder());
    }
    return cndCMRList;

  }

  private static ArrayList<String> getCommonNumList(EntityManager entityManager) {

    String cndCMR = "";
    ArrayList<String> cndCMRList = new ArrayList<String>();

    // String sql =
    // ExternalizedQuery.getSql("BATCH.GET.KNA1_ZZKV_CUSNO.US_FIND_MISSINGCMRNO");
    // PreparedQuery query = new PreparedQuery(entityManager, sql);
    //
    // query.setParameter("MANDT1", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR61", SystemLocation.UNITED_STATES);
    // query.setParameter("MANDT2", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR62", SystemLocation.UNITED_STATES);

    String sql = ExternalizedQuery.getSql("BATCH.US_CMR_NO.GENERATE");
    int min = 1000001;
    int max = 8999999;

    sql = StringUtils.replaceOnce(sql, "$MIN", min + "");
    sql = StringUtils.replaceOnce(sql, "$MAX", max + "");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("LOC", SystemLocation.UNITED_STATES);
    List<String> records = query.getResults(300, String.class);

    // for (int i = 1; i < 9; i++) {
    // query.setParameter("ZZKV_CUSNO1", i + "%");
    // query.setParameter("ZZKV_CUSNO2", i + "%");
    // List<String> records = query.getResults(String.class);
    if (records != null && records.size() > 0) {
      for (String missCmrNo : records) {
        if (missCmrNo != null) {
          // cndCMR = String.valueOf(Integer.valueOf(missCmrNo) + 1);
          cndCMR = String.valueOf(Integer.valueOf(missCmrNo));
          cndCMRList.add(cndCMR);
        }
      }
      // TODO
      // if (cndCMRList.size() > 1) {
      // break;
      // }
    }
    // }
    cndCMRList.sort(Comparator.naturalOrder());

    return cndCMRList;
  }

  private static ArrayList<String> getCommonNumListMassCrt(EntityManager entityManager) {

    String cndCMR = "";
    ArrayList<String> cndCMRList = new ArrayList<String>();

    int counts = 0;
    String sqlc = "SELECT count(*) FROM creqcmr.MASS_CREATE WHERE PAR_REQ_ID IN (SELECT REQ_ID FROM creqcmr.ADMIN WHERE REQ_TYPE = 'N' AND RDC_PROCESSING_STATUS = 'A')";
    Query q = entityManager.createNativeQuery(sqlc);
    counts = (int) q.getSingleResult() + 50;

    // String sql =
    // ExternalizedQuery.getSql("BATCH.GET.KNA1_ZZKV_CUSNO.US_FIND_MISSINGCMRNO_PART");
    // String sql1 = sql + " FETCH FIRST " + counts + " ROWS ONLY WITH UR";
    // LOG.info(" NEW Query for Mass Crt is " + sql1 + "the counts = " +
    // counts);
    // LOG.info(" The counts = " + counts);

    String sql = ExternalizedQuery.getSql("BATCH.US_CMR_NO.GENERATE");
    int min = 6000001;
    int max = 6999999;

    LOG.info(" NEW Query for Mass Crt is " + sql + "the counts = " + counts);

    sql = StringUtils.replaceOnce(sql, "$MIN", min + "");
    sql = StringUtils.replaceOnce(sql, "$MAX", max + "");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("LOC", SystemLocation.UNITED_STATES);
    List<String> records = query.getResults(counts, String.class);

    // PreparedQuery query = new PreparedQuery(entityManager, sql1);
    //
    // query.setParameter("MANDT1", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR61", SystemLocation.UNITED_STATES);
    // query.setParameter("MANDT2", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR62", SystemLocation.UNITED_STATES);
    //
    // query.setParameter("ZZKV_CUSNO1", 6 + "%");
    // query.setParameter("ZZKV_CUSNO2", 6 + "%");
    // List<String> records = query.getResults(String.class);
    if (records != null && records.size() > 0) {
      for (String missCmrNo : records) {
        if (missCmrNo != null) {
          // cndCMR = String.format("%07d", Integer.valueOf(missCmrNo) + 1);
          cndCMR = String.format("%07d", Integer.valueOf(missCmrNo));
          cndCMRList.add(cndCMR);
        }
      }
      cndCMRList.sort(Comparator.naturalOrder());
    }
    return cndCMRList;
  }

  private static ArrayList<String> getMainNmNumList(EntityManager entityManager) {

    String cndCMR = "";
    ArrayList<String> cndCMRList = new ArrayList<String>();

    // String sql =
    // ExternalizedQuery.getSql("BATCH.GET.KNA1_ZZKV_CUSNO.US_FIND_MISSINGCMRNO");
    // PreparedQuery query = new PreparedQuery(entityManager, sql);
    //
    // query.setParameter("MANDT1", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR61", SystemLocation.UNITED_STATES);
    // query.setParameter("MANDT2", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR62", SystemLocation.UNITED_STATES);
    //
    // query.setParameter("ZZKV_CUSNO1", 0 + "%");
    // query.setParameter("ZZKV_CUSNO2", 0 + "%");
    String sql = ExternalizedQuery.getSql("BATCH.US_CMR_NO.GENERATE");
    int min = 1;
    int max = 999999;

    sql = StringUtils.replaceOnce(sql, "$MIN", min + "");
    sql = StringUtils.replaceOnce(sql, "$MAX", max + "");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("LOC", SystemLocation.UNITED_STATES);
    List<String> records = query.getResults(300, String.class);

    // List<String> records = query.getResults(String.class);
    if (records != null && records.size() > 0) {
      for (String missCmrNo : records) {
        if (missCmrNo != null) {
          // cndCMR = String.format("%07d", Integer.valueOf(missCmrNo) + 1);
          cndCMR = String.format("%07d", Integer.valueOf(missCmrNo));
          cndCMRList.add(cndCMR);
        }
      }
      cndCMRList.sort(Comparator.naturalOrder());
    }

    return cndCMRList;

  }

  private static ArrayList<String> getMainNmNumListMassCrt(EntityManager entityManager) {

    String cndCMR = "";
    ArrayList<String> cndCMRList = new ArrayList<String>();

    int counts = 0;
    String sqlc = "SELECT count(*) FROM creqcmr.MASS_CREATE WHERE PAR_REQ_ID IN (SELECT REQ_ID FROM creqcmr.ADMIN WHERE REQ_TYPE = 'N' AND RDC_PROCESSING_STATUS = 'A')";
    Query q = entityManager.createNativeQuery(sqlc);
    counts = (int) q.getSingleResult() + 50;

    // String sql =
    // ExternalizedQuery.getSql("BATCH.GET.KNA1_ZZKV_CUSNO.US_FIND_MISSINGCMRNO_PART");
    // String sql1 = sql + " FETCH FIRST " + counts + " ROWS ONLY WITH UR";
    // LOG.info(" NEW Query for Mass Crt is " + sql1 + "the counts = " +
    // counts);
    // LOG.info(" The counts = " + counts);

    String sql = ExternalizedQuery.getSql("BATCH.US_CMR_NO.GENERATE");
    int min = 600000;
    int max = 699999;
    LOG.info(" NEW Query for Mass Crt is " + sql + "the counts = " + counts);
    sql = StringUtils.replaceOnce(sql, "$MIN", min + "");
    sql = StringUtils.replaceOnce(sql, "$MAX", max + "");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("MANDT", SystemConfiguration.getValue("MANDT"));
    query.setParameter("LOC", SystemLocation.UNITED_STATES);
    List<String> records = query.getResults(counts, String.class);

    // PreparedQuery query = new PreparedQuery(entityManager, sql1);
    //
    // query.setParameter("MANDT1", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR61", SystemLocation.UNITED_STATES);
    // query.setParameter("MANDT2", SystemConfiguration.getValue("MANDT"));
    // query.setParameter("KATR62", SystemLocation.UNITED_STATES);
    //
    // query.setParameter("ZZKV_CUSNO1", "06" + "%");
    // query.setParameter("ZZKV_CUSNO2", "06" + "%");
    // List<String> records = query.getResults(String.class);
    if (records != null && records.size() > 0) {
      for (String missCmrNo : records) {
        if (missCmrNo != null) {
          // cndCMR = String.format("%07d", Integer.valueOf(missCmrNo) + 1);
          cndCMR = String.format("%07d", Integer.valueOf(missCmrNo));
          cndCMRList.add(cndCMR);
        }
      }
      cndCMRList.sort(Comparator.naturalOrder());
    }

    return cndCMRList;

  }
}