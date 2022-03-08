package com.ibm.cio.cmr.request.util.legacy;

import java.util.List;
import java.util.Random;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ibm.cio.cmr.request.entity.Kna1;
import com.ibm.cio.cmr.request.query.ExternalizedQuery;
import com.ibm.cio.cmr.request.query.PreparedQuery;

/**
 * @author PriyRanjan
 * 
 */

public class CloningRDCDirectUtil {

  private static final Logger LOG = Logger.getLogger(CloningRDCDirectUtil.class);

  private static final char[] symbols;

  static {
    StringBuilder tmp = new StringBuilder();
    for (char ch = '0'; ch <= '9'; ++ch)
      tmp.append(ch);
    for (char ch = 'A'; ch <= 'Z'; ++ch)
      tmp.append(ch);
    symbols = tmp.toString().toCharArray();
  }

  private final Random random = new Random();

  private final char[] buf;

  public CloningRDCDirectUtil(int length) {
    if (length < 1)
      throw new IllegalArgumentException("length < 1: " + length);
    buf = new char[length];
  }

  public static String genNumericNumberSeries(int len, String kukla) {
    final String numerics = "0123456789";
    final int N = numerics.length();
    String ret = "";
    Random r = new Random();

    if ("81".equals(kukla)) {
      ret = "99";
      do {

        for (int i = 0; len != ret.length(); i++) {
          ret = ret.concat(String.valueOf(numerics.charAt(r.nextInt(N))));
        }

        if (Integer.parseInt(ret) >= 990000 && Integer.parseInt(ret) <= 999999 && !"99".equals(ret)) {
          break;
        } else {
          ret = "99";
        }

      } while (Integer.parseInt(ret) >= 990000 && Integer.parseInt(ret) <= 999999 && !"99".equals(ret));
    } else {

      do {
        for (int i = 0; len != ret.length(); i++) {
          if ("0".equals(ret)) {
            ret = String.valueOf(numerics.charAt(r.nextInt(N)));
          } else {
            ret = ret.concat(String.valueOf(numerics.charAt(r.nextInt(N))));
          }
        }

        if (!"".equals(ret) && Integer.parseInt(ret) >= 000001 && Integer.parseInt(ret) <= 989999) {
          break;
        } else {
          ret = "";
        }

      } while (StringUtils.isBlank(ret) || (Integer.parseInt(ret) >= 000001 && Integer.parseInt(ret) <= 989999 && !"0".equals(ret)));
    }

    return ret;
  }

  public static boolean checkCustNoForDuplicateRecord(EntityManager rdcMgr, String zzkvCusNo, String mandt, String cmrIssuingCntry) {

    boolean flage = false;
    LOG.info("checkig duplicate CMR No." + zzkvCusNo + " in RDC.");
    String sql = ExternalizedQuery.getSql("GET.KNA1.ZZKV_CUSNO.DUPLICATE_CHECK");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setParameter("ZZKV_CUSNO", zzkvCusNo);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", cmrIssuingCntry);

    List<Kna1> records = query.getResults(Kna1.class);
    if (records != null && records.size() > 0) {
      flage = true;
    }
    return flage;
  }

  public String getKuklaFromCMR(EntityManager entityManager, String cmrIssuingCntry, String cmrNo, String mandt) {
    String sql = ExternalizedQuery.getSql("GET.KNA1.KUKLA_VAL");
    PreparedQuery query = new PreparedQuery(entityManager, sql);
    query.setParameter("ZZKV_CUSNO", cmrNo);
    query.setParameter("MANDT", mandt);
    query.setParameter("KATR6", cmrIssuingCntry);
    query.setForReadOnly(true);
    String kukla = query.getSingleResult(String.class);
    return kukla;
  }

  public static String genSingleRandomCharExcludeP() {
    final String alphabet = "0123456789ABCDEFGHIJKLMNOQRSTUVWXYZ";
    final int N = alphabet.length();
    String ret = "";
    Random r = new Random();

    ret = String.valueOf(alphabet.charAt(r.nextInt(N)));
    return ret;
  }

  public String nextString() {
    for (int idx = 0; idx < buf.length; ++idx)
      buf[idx] = symbols[random.nextInt(symbols.length)];
    return new String(buf);
  }

  public static String generateCMRNo(EntityManager rdcMgr, String loc1, String loc2, String mandt, int min, int max) throws Exception {

    String sql = ExternalizedQuery.getSql("CMR_NO.GENERATE_MA");
    sql = StringUtils.replaceOnce(sql, "$MIN", min + "");
    sql = StringUtils.replaceOnce(sql, "$MAX", max + "");
    PreparedQuery query = new PreparedQuery(rdcMgr, sql);
    query.setForReadOnly(true);
    query.setParameter("LOC1", loc1);
    query.setParameter("LOC2", loc2);
    query.setParameter("MANDT", mandt);

    List<String> cmrNos = query.getResults(1000, String.class);
    if (cmrNos != null) {
      return cmrNos.size() > 0 ? cmrNos.get(0) : null;
    } else {
      return null;
    }
  }

}
