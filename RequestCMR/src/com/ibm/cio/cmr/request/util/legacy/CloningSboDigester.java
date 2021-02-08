package com.ibm.cio.cmr.request.util.legacy;

import java.util.ArrayList;

import org.apache.commons.digester.Digester;

public class CloningSboDigester extends Digester {

  public CloningSboDigester() {

    addObjectCreate("mappings", ArrayList.class);
    addObjectCreate("mappings/mapping", CloningSboMapping.class);
    addBeanPropertySetter("mappings/mapping/country", "country");
    addBeanPropertySetter("mappings/mapping/sbo", "sbo");
    addSetNext("mappings/mapping", "add");

  }

}
