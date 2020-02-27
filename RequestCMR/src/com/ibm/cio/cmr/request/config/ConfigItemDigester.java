package com.ibm.cio.cmr.request.config;

import java.util.ArrayList;

import org.apache.commons.digester.Digester;

/**
 * Digester for cmrconfig.xml
 * 
 * @author Jeffrey Zamora
 * 
 */
public class ConfigItemDigester extends Digester {

  public ConfigItemDigester() {
    setValidating(false);
    addObjectCreate("config", ArrayList.class);

    addObjectCreate("config/item", ConfigItem.class);

    addBeanPropertySetter("config/item/id", "id");
    addBeanPropertySetter("config/item/order", "order");
    addBeanPropertySetter("config/item/name", "name");
    addBeanPropertySetter("config/item/hint", "hint");
    addBeanPropertySetter("config/item/description", "description");
    addBeanPropertySetter("config/item/type", "type");
    addBeanPropertySetter("config/item/editable", "editable");
    addBeanPropertySetter("config/item/required", "required");
    addBeanPropertySetter("config/item/editType", "editType");
    addBeanPropertySetter("config/item/value", "value");
    addSetNext("config/item", "add");
  }
}
