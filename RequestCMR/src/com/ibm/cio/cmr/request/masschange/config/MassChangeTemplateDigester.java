/**
 * 
 */
package com.ibm.cio.cmr.request.masschange.config;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.xml.sax.Attributes;

import com.ibm.cio.cmr.request.CmrConstants;
import com.ibm.cio.cmr.request.listener.CmrContextListener;
import com.ibm.cio.cmr.request.masschange.MassChangeTemplateManager;
import com.ibm.cio.cmr.request.masschange.obj.MassChangeTemplate;
import com.ibm.cio.cmr.request.masschange.obj.TemplateColumn;
import com.ibm.cio.cmr.request.masschange.obj.TemplateDataType;
import com.ibm.cio.cmr.request.masschange.obj.TemplateTab;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation;
import com.ibm.cio.cmr.request.masschange.obj.TemplateValidation.ValidationRow;
import com.ibm.cio.cmr.request.util.JpaManager;
import com.ibm.cio.cmr.request.util.SystemLocation;

/**
 * {@link Digester} implementation to parse the {@link MassChangeTemplate}
 * configurations
 * 
 * @author JeffZAMORA
 * 
 */
public class MassChangeTemplateDigester extends Digester {

  public static void main(String[] args) throws Exception {
    CmrContextListener.startCMRContext("cmr-log4j.properties", false);

    // to use this without starting the server, make sure to edit
    // persistence.xml
    // remove the jdbc/rdc line and convert to specify URL/U/P similar to the
    // batch persistence.xml

    MassChangeTemplateManager.initTemplatesAndValidators("866");

    // change to the ID of the config you are generating
    MassChangeTemplate template = MassChangeTemplateManager.getMassUpdateTemplate("EMEA");
    // MassChangeTemplate swissTemplate =
    // MassChangeTemplateManager.getMassUpdateTemplate("SWISS");

    // testGenerate(template);
    testParse(template);
    // testParse(swissTemplate);
    System.out.println("ok");
  }

  public static void testGenerate(MassChangeTemplate template) throws Exception {
    // change to your local test file
    try (FileOutputStream fos = new FileOutputStream("C:/createCMR/downloads/EMEA.xlsx")) {
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = SystemLocation.UNITED_KINGDOM;
        // modify the country for testing
        template.generate(fos, em, country, 2000);
      } finally {
        em.close();
      }
    }

    try (FileOutputStream fos = new FileOutputStream("C:/createCMR/downloads/SWISS.xlsx")) {
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = SystemLocation.UNITED_KINGDOM;
        // modify the country for testing
        template.generate(fos, em, country, 2000);
      } finally {
        em.close();
      }
    }
    System.out.println("ok");
  }

  public static void testParse(MassChangeTemplate template) throws Exception {
    // change to your local test file

    try (FileInputStream fis = new FileInputStream("C:/createCMR/downloads/MassUpdateTemplateAutoUK(14).xlsx")) {
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = SystemLocation.UNITED_KINGDOM;
        System.out.println("Validating..");

        byte[] bookBytes = template.cloneStream(fis);
        List<TemplateValidation> validations = null;
        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          validations = template.validate(em, is, country, 2000);
          System.out.println(new ObjectMapper().writeValueAsString(validations));
          for (TemplateValidation validation : validations) {
            for (ValidationRow row : validation.getRows()) {
              if (!row.isSuccess()) {
                System.err.println(row.getError());
              }
            }
          }
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream("C:/createCMR/downloads/MassUpdateTemplateAutoUK(14).xlsx")) {
            System.out.println("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } finally {
        em.close();
      }
    }

    try (FileInputStream fis = new FileInputStream("C:/createCMR/downloads/MassUpdateTemplateAutoSWISS.xlsx")) {
      EntityManager em = JpaManager.getEntityManager();
      try {
        String country = SystemLocation.UNITED_KINGDOM;
        System.out.println("Validating..");

        byte[] bookBytes = template.cloneStream(fis);
        List<TemplateValidation> validations = null;
        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          validations = template.validate(em, is, country, 2000);
          System.out.println(new ObjectMapper().writeValueAsString(validations));
          for (TemplateValidation validation : validations) {
            for (ValidationRow row : validation.getRows()) {
              if (!row.isSuccess()) {
                System.err.println(row.getError());
              }
            }
          }
        }

        try (InputStream is = new ByteArrayInputStream(bookBytes)) {
          try (FileOutputStream fos = new FileOutputStream("C:/createCMR/downloads/MassUpdateTemplateAutoSWISS.xlsx")) {
            System.out.println("Merging..");
            template.merge(validations, is, fos, 2000);
          }
        }
        // modify the country for testing
      } finally {
        em.close();
      }
    }
    System.out.println("ok");
  }

  public MassChangeTemplateDigester() {
    addFactoryCreate("template", new TemplateCreatorFactory(this));
    addObjectCreate("template/tabs", ArrayList.class);
    addFactoryCreate("template/tabs/tab", new TabCreatorFactory(this));

    addObjectCreate("template/tabs/tab/columns", ArrayList.class);
    addFactoryCreate("template/tabs/tab/columns/column", new ColumnCreatorFactory(this));
    addBeanPropertySetter("template/tabs/tab/columns/column/label", "label");
    addBeanPropertySetter("template/tabs/tab/columns/column/width", "width");
    addBeanPropertySetter("template/tabs/tab/columns/column/length", "length");
    addBeanPropertySetter("template/tabs/tab/columns/column/lovId", "lovId");
    addBeanPropertySetter("template/tabs/tab/columns/column/bdsId", "bdsId");
    addBeanPropertySetter("template/tabs/tab/columns/column/dbColumn", "dbColumn");

    addObjectCreate("template/tabs/tab/columns/column/values", LinkedHashSet.class);
    addCallMethod("template/tabs/tab/columns/column/values/value", "add", 1);
    addCallParam("template/tabs/tab/columns/column/values/value", 0);
    addSetNext("template/tabs/tab/columns/column/values", "setValues");

    addSetNext("template/tabs/tab/columns/column", "add");
    addSetNext("template/tabs/tab/columns", "addAll");

    addSetNext("template/tabs/tab", "add");
    addSetNext("template/tabs", "addAll");
  }

  public class TemplateCreatorFactory implements ObjectCreationFactory {

    private Digester digester;

    @Override
    public Object createObject(Attributes atts) throws Exception {
      MassChangeTemplate template = new MassChangeTemplate();
      template.setId(atts.getValue("id"));
      template.setType("UPDATE".equals(atts.getValue("for")) ? CmrConstants.REQ_TYPE_MASS_UPDATE : CmrConstants.REQ_TYPE_MASS_CREATE);
      return template;
    }

    public TemplateCreatorFactory(Digester digester) {
      setDigester(digester);
    }

    @Override
    public Digester getDigester() {
      return this.digester;
    }

    @Override
    public void setDigester(Digester arg0) {
      this.digester = arg0;
    }

  }

  public class TabCreatorFactory implements ObjectCreationFactory {

    private Digester digester;

    public TabCreatorFactory(Digester digester) {
      setDigester(digester);
    }

    @Override
    public Object createObject(Attributes atts) throws Exception {
      TemplateTab tab = new TemplateTab();
      tab.setName(atts.getValue("name"));
      if (atts.getValue("typeCode") != null) {
        tab.setTypeCode(atts.getValue("typeCode"));
      }
      if (atts.getValue("type") != null) {
        tab.setType(TemplateDataType.valueOf(atts.getValue("type")));
      }
      return tab;
    }

    @Override
    public Digester getDigester() {
      return this.digester;
    }

    @Override
    public void setDigester(Digester arg0) {
      this.digester = arg0;
    }

  }

  public class ColumnCreatorFactory implements ObjectCreationFactory {

    private Digester digester;

    public ColumnCreatorFactory(Digester digester) {
      setDigester(digester);
    }

    @Override
    public Object createObject(Attributes atts) throws Exception {
      TemplateColumn col = new TemplateColumn();
      if (atts.getValue("typeCode") != null) {
        // override tab level specifications
        col.setTypeCode(atts.getValue("typeCode"));
      }
      if (atts.getValue("type") != null) {
        // override tab level specifications
        col.setType(TemplateDataType.valueOf(atts.getValue("type")));
      }
      col.setExactLength("true".equals(atts.getValue("exactLength")));
      col.setNumber("true".equals(atts.getValue("number")));
      return col;
    }

    @Override
    public Digester getDigester() {
      return this.digester;
    }

    @Override
    public void setDigester(Digester arg0) {
      this.digester = arg0;
    }
  }

}
