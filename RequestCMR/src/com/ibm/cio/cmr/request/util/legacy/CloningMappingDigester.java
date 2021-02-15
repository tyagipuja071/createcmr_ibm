package com.ibm.cio.cmr.request.util.legacy;

import java.util.ArrayList;

import org.apache.commons.digester.Digester;

public class CloningMappingDigester extends Digester {

  public CloningMappingDigester() {

    addObjectCreate("mappings", ArrayList.class);
    addObjectCreate("mappings/mapping", CloningMapping.class);
    addBeanPropertySetter("mappings/mapping/cmrNoMin", "cmrNoMin");
    addBeanPropertySetter("mappings/mapping/cmrNoMax", "cmrNoMax");
    addSetNext("mappings/mapping", "add");

  }

}
