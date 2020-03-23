package com.ibm.cio.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.ibm.db2.jcc.DB2Driver;

/**
 * 
 */

/**
 * Creates Entities and Entity PKs based based on DB definition.
 * 
 * @author Jeffrey Zamora
 * 
 */
public class EntityCreator {

  private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("db");
  private static final String SQL = "select trim(t.CREATOR) SCHEMA, t.NAME TABNAME, c.NAME COLNAME, case when c.KEYSEQ > 0 then 'Y' else null end PK, trim(c.COLTYPE) TYPENAME, c.COLNO ORDER, c.REMARKS "
      + " from SYSIBM.SYSTABLES t, SYSIBM.SYSCOLUMNS c"
      + " where t.CREATOR = ?"
      + " and t.NAME = ?"
      + " and t.CREATOR = c.TBCREATOR"
      + " and t.NAME = c.TBNAME";

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Please specify at least one table to export");
      System.exit(1);
    }
    System.setProperty("java.security.properties", "java.security");
    Class.forName(DB2Driver.class.getName());

    String url = BUNDLE.getString("URL");
    String user = BUNDLE.getString("USER");
    String password = BUNDLE.getString("PASSWORD");

    initSSL();

    // connect
    Connection conn = DriverManager.getConnection(url, user, password);
    try {
      System.out.println("Connected to database..");
      System.out.println();

      Exporter exporter = new Exporter();
      Table table = null;
      for (String tableName : args) {
        // get table information
        System.out.println("Retrieving information for table " + tableName);
        table = getInfo(conn, tableName);
        System.out.println(table.getColumns().size() + " columns in the definition.");

        // export one by one
        System.out.println("Exporting table to JPA entity file...");
        exporter.export(BUNDLE.getString("OUT_DIR"), table);
        System.out.println();
      }
    } finally {
      conn.close();
    }
  }

  /**
   * Gets the table information by connecting to the database
   * 
   * @param conn
   * @param tableName
   * @return
   * @throws Exception
   */
  private static Table getInfo(Connection conn, String tableName) throws Exception {
    PreparedStatement stmt = conn.prepareStatement(SQL);
    try {
      stmt.setString(1, BUNDLE.getString("SCHEMA"));
      stmt.setString(2, tableName);
      ResultSet rs = stmt.executeQuery();
      try {
        Table t = null;
        Column c = null;
        while (rs.next()) {
          if (t == null) {
            t = new Table();
            t.setName(rs.getString("TABNAME"));
            t.setSchema(rs.getString("SCHEMA"));
          }
          c = new Column();
          c.setName(rs.getString("COLNAME"));
          c.setPrimaryKey("Y".equals(rs.getString("PK")));
          c.setType(rs.getString("TYPENAME"));
          c.setOrder(Integer.parseInt(rs.getString("ORDER")));
          c.setRemarks(rs.getString("REMARKS"));
          t.add(c);
        }
        return t;
      } finally {
        rs.close();
      }
    } finally {
      stmt.close();
    }
  }

  protected static void initSSL() {
    System.setProperty("javax.net.ssl.keyStore", BUNDLE.getString("KEYSTORE_LOC"));
    System.setProperty("javax.net.ssl.trustStore", BUNDLE.getString("KEYSTORE_LOC"));
    System.setProperty("javax.net.ssl.keyStorePassword", BUNDLE.getString("KEYSTORE_PASS"));

    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {

      @Override
      public boolean verify(String arg0, SSLSession arg1) {
        return true;
      }
    });
  }

}
