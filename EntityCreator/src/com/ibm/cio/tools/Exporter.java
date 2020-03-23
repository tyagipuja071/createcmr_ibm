/**
 * 
 */
package com.ibm.cio.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * Export the table to Entity and EntityPK files
 * 
 * @author Jeffrey Zamora
 * 
 */
public class Exporter {

  private VelocityEngine engine;

  public Exporter() throws Exception {
    initEngine();
  }

  /**
   * Initializes the engine
   * 
   * @throws Exception
   */
  private void initEngine() throws Exception {
    this.engine = new VelocityEngine();
    this.engine.addProperty(Velocity.RESOURCE_LOADER, "classpath");
    this.engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
    this.engine.init();
  }

  /**
   * Exports the table to Entity and EntityPK files to the output directory
   * 
   * @param outDir
   * @param table
   * @throws ResourceNotFoundException
   * @throws ParseErrorException
   * @throws Exception
   */
  public void export(String outDir, Table table) throws ResourceNotFoundException, ParseErrorException, Exception {

    exportTable(outDir, table);

    exportPK(outDir, table);
  }

  /**
   * Exports the table to a file
   * 
   * @param outDir
   * @param table
   * @throws Exception
   */
  private void exportTable(String outDir, Table table) throws Exception {
    // process table
    VelocityContext ctx = new VelocityContext();
    ctx.put("table", table);
    ctx.put("user", System.getProperty("user.name"));
    ctx.put("currdate", new Date());
    ctx.put("tableName", table.getName());
    ctx.put("tableNameJava", toJava(table.getName()));
    ctx.put("schema", table.getSchema());
    ctx.put("columns", getColumns(table));
    Template template = this.engine.getTemplate("entity.vm");
    StringWriter sw = new StringWriter();
    try {
      template.merge(ctx, sw);
    } finally {
      sw.close();
    }
    toFile(outDir, toJava(table.getName()) + ".java", sw.toString());
  }

  /**
   * Exports the PK to a file
   * 
   * @param outDir
   * @param table
   * @throws Exception
   */
  private void exportPK(String outDir, Table table) throws Exception {
    VelocityContext ctx = new VelocityContext();
    ctx.put("table", table);
    ctx.put("currdate", new Date());
    ctx.put("user", System.getProperty("user.name"));
    ctx.put("tableName", table.getName());
    ctx.put("tableNameJava", toJava(table.getName()));
    ctx.put("schema", table.getSchema());
    ctx.put("columns", getColumns(table, true));
    ctx.put("columnEquals", getColumnEquals(table));
    ctx.put("columnHash", getColumnHash(table));
    ctx.put("columnAllKeys", getColumnAllKeys(table));

    Template template = this.engine.getTemplate("entitypk.vm");
    StringWriter sw = new StringWriter();
    try {
      template.merge(ctx, sw);
    } finally {
      sw.close();
    }
    toFile(outDir, toJava(table.getName()) + "PK.java", sw.toString());

  }

  /**
   * Gets the non-PK columns
   * 
   * @param table
   * @return
   */
  private String getColumns(Table table) {
    return getColumns(table, false);
  }

  /**
   * Gets all columns
   * 
   * @param table
   * @param pk
   * @return
   */
  private String getColumns(Table table, boolean pk) {
    StringBuilder sb = new StringBuilder();

    // create first the field declarations
    for (Column col : table.getColumns()) {
      if ((!pk && !col.isPrimaryKey()) || (pk && col.isPrimaryKey())) {
        if (col.getRemarks() != null && col.getRemarks().length() < 30) {
          sb.append("  /** \n");
          sb.append("   * " + col.getRemarks() + "\n");
          sb.append("   */ \n");
        }
        if (col.getName().contains("_")) {
          sb.append("  @Column(name = \"" + col.getName() + "\")\n");
        }
        if ("VARCHAR".equals(col.getType()) || "CHAR".equals(col.getType()) || "CHARACTER".equals(col.getType())) {
          sb.append("  private String " + toField(col.getName() + ";\n"));
        }
        if ("INTEGER".equals(col.getType()) || "DECIMAL".equals(col.getType())) {
          sb.append("  private int " + toField(col.getName() + ";\n"));
        }
        if ("BIGINT".equals(col.getType())) {
          sb.append("  private long " + toField(col.getName() + ";\n"));
        }
        if ("TIMESTAMP".equals(col.getType()) || "TIMESTMP".equals(col.getType())) {
          sb.append("  @Temporal(TemporalType.TIMESTAMP)\n");
          sb.append("  private Date " + toField(col.getName() + ";\n"));
        }
        if ("DATE".equals(col.getType())) {
          sb.append("  @Temporal(TemporalType.DATE)\n");
          sb.append("  private Date " + toField(col.getName() + ";\n"));
        }
        if ("TIME".equals(col.getType())) {
          sb.append("  @Temporal(TemporalType.TIME)\n");
          sb.append("  private Date " + toField(col.getName() + ";\n"));
        }
        sb.append("\n");
      }
    }

    // create the getters and setters
    for (Column col : table.getColumns()) {
      if ((!pk && !col.isPrimaryKey()) || (pk && col.isPrimaryKey())) {
        if ("VARCHAR".equals(col.getType()) || "CHAR".equals(col.getType()) || "CHARACTER".equals(col.getType())) {
          sb.append("  public String get" + toJava(col.getName()) + "(){\n");
          sb.append("    return this." + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
          sb.append("  public void set" + toJava(col.getName()) + "(String " + toField(col.getName()) + "){\n");
          sb.append("    this." + toField(col.getName()) + " = " + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
        }
        if ("INTEGER".equals(col.getType()) || "DECIMAL".equals(col.getType())) {
          sb.append("  public int get" + toJava(col.getName()) + "(){\n");
          sb.append("    return this." + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
          sb.append("  public void set" + toJava(col.getName()) + "(int " + toField(col.getName()) + "){\n");
          sb.append("    this." + toField(col.getName()) + " = " + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
        }
        if ("BIGINT".equals(col.getType())) {
          sb.append("  public long get" + toJava(col.getName()) + "(){\n");
          sb.append("    return this." + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
          sb.append("  public void set" + toJava(col.getName()) + "(long " + toField(col.getName()) + "){\n");
          sb.append("    this." + toField(col.getName()) + " = " + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
        }
        if ("TIMESTMP".equals(col.getType()) || "TIMESTAMP".equals(col.getType()) || "DATE".equals(col.getType()) || "TIME".equals(col.getType())) {
          sb.append("  public Date get" + toJava(col.getName()) + "(){\n");
          sb.append("    return this." + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
          sb.append("  public void set" + toJava(col.getName()) + "(Date " + toField(col.getName()) + "){\n");
          sb.append("    this." + toField(col.getName()) + " = " + toField(col.getName()) + ";\n");
          sb.append("  }\n");
          sb.append("\n");
        }
        sb.append("\n");
      }
    }

    return sb.toString();
  }

  /**
   * Gets the equals part of the PK
   * 
   * @param table
   * @return
   */
  private String getColumnEquals(Table table) {
    StringBuilder sb = new StringBuilder();
    sb.append("    return ");
    int cnt = 0;
    for (Column col : table.getColumns()) {

      if (col.isPrimaryKey()) {
        sb.append(cnt > 0 ? " && " : "");
        if ("INTEGER".equals(col.getType()) || "BIGINT".equals(col.getType()) || "DECIMAL".equals(col.getType())) {
          sb.append("this." + toField(col.getName()) + " == o." + toField(col.getName()) + "");
        } else {
          sb.append("this." + toField(col.getName()) + ".equals(o." + toField(col.getName()) + ")");
        }
        cnt++;
      }
    }
    return sb.append(";\n").toString();
  }

  /**
   * Gets the allKeysAssigned part of the PK
   * 
   * @param table
   * @return
   */
  private String getColumnAllKeys(Table table) {
    StringBuilder sb = new StringBuilder();
    sb.append("    return ");
    int cnt = 0;
    for (Column col : table.getColumns()) {

      if (col.isPrimaryKey()) {
        sb.append(cnt > 0 ? " && " : "");
        if ("INTEGER".equals(col.getType()) || "BIGINT".equals(col.getType()) || "DECIMAL".equals(col.getType())) {
          sb.append("this." + toField(col.getName()) + " > 0");
        } else {
          sb.append("!StringUtils.isEmpty(this." + toField(col.getName()) + ")");
        }

        cnt++;
      }
    }
    return sb.append(";\n").toString();
  }

  /**
   * Gets the hashCode part of the PK
   * 
   * @param table
   * @return
   */
  private String getColumnHash(Table table) {
    StringBuilder sb = new StringBuilder();
    for (Column col : table.getColumns()) {

      if (col.isPrimaryKey()) {
        if ("INTEGER".equals(col.getType()) || "DECIMAL".equals(col.getType())) {
          sb.append("    hash = hash * prime + this." + toField(col.getName()) + ";\n");
        } else if ("BIGINT".equals(col.getType())) {
          sb.append("    hash = hash * prime + (this." + toField(col.getName()) + " > 0 ? new java.lang.Long(this." + toField(col.getName())
              + ").hashCode() : 0);\n");
        } else {
          sb.append("    hash = hash * prime + (this." + toField(col.getName()) + " != null ? this." + toField(col.getName()) + ".hashCode() : 0);\n");
        }
      }
    }
    return sb.toString();
  }

  /**
   * Converts the name to a java field equivalent
   * 
   * @param name
   * @return
   */
  private String toField(String name) {
    int cnt = 0;
    StringBuilder sb = new StringBuilder();
    for (String part : name.split("_")) {
      if (cnt == 0) {
        sb.append(part.toLowerCase());
      } else {
        sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
      }
      cnt++;
    }
    return sb.toString();
  }

  /**
   * Converts the name to Title Case (java) format
   * 
   * @param name
   * @return
   */
  private String toJava(String name) {
    StringBuilder sb = new StringBuilder();
    for (String part : name.split("_")) {
      sb.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase());
    }
    return sb.toString();
  }

  /**
   * Exports the contents to a file
   * 
   * @param outDir
   * @param fileName
   * @param contents
   * @throws FileNotFoundException
   */
  private void toFile(String outDir, String fileName, String contents) throws FileNotFoundException {
    File f = new File(outDir + "/" + fileName);
    System.out.println(f.getAbsolutePath());
    PrintWriter pw = new PrintWriter(f);
    try {
      pw.print(contents);
    } finally {
      pw.close();
    }
    System.out.println(f.getName() + " created.");
  }
}
