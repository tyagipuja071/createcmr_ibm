package com.ibm.cio.cmr.request.util.legacy;

import java.util.ArrayList;

import org.apache.commons.digester.Digester;

public class CloningRDCDigester extends Digester {

  public CloningRDCDigester() {

    setValidating(false);
    addObjectCreate("application", CloningRDCConfiguration.class);

    addBeanPropertySetter("application/targetMandt", "targetMandt");
    addBeanPropertySetter("application/katr10", "katr10");

    addBeanPropertySetter("application/processKnb1", "processKnb1");
    addObjectCreate("application/createKnb1Countries", "countriesForKnb1Create", ArrayList.class);
    addCallMethod("application/createKnb1Countries/country", "add", 1);
    addCallParam("application/createKnb1Countries/country", 0);
    addSetNext("application/createKnb1Countries", "setCountriesForKnb1Create");

    addBeanPropertySetter("application/processKnvv", "processKnvv");
    addObjectCreate("application/createKnvvCountries", "countriesForKnvvCreate", ArrayList.class);
    addCallMethod("application/createKnvvCountries/country", "add", 1);
    addCallParam("application/createKnvvCountries/country", 0);
    addSetNext("application/createKnvvCountries", "setCountriesForKnvvCreate");

    addBeanPropertySetter("application/processKnvi", "processKnvi");
    addObjectCreate("application/createKnviCountries", "countriesForKnviCreate", ArrayList.class);
    addCallMethod("application/createKnviCountries/country", "add", 1);
    addCallParam("application/createKnviCountries/country", 0);
    addSetNext("application/createKnviCountries", "setCountriesForKnviCreate");

    addBeanPropertySetter("application/processKnvk", "processKnvk");
    addObjectCreate("application/createKnvkCountries", "countriesForKnvkCreate", ArrayList.class);
    addCallMethod("application/createKnvkCountries/country", "add", 1);
    addCallParam("application/createKnvkCountries/country", 0);
    addSetNext("application/createKnvkCountries", "setCountriesForKnvkCreate");

    addBeanPropertySetter("application/processKnvp", "processKnvp");
    addObjectCreate("application/createKnvpCountries", "countriesForKnvpCreate", ArrayList.class);
    addCallMethod("application/createKnvpCountries/country", "add", 1);
    addCallParam("application/createKnvpCountries/country", 0);
    addSetNext("application/createKnvpCountries", "setCountriesForKnvpCreate");

    addBeanPropertySetter("application/processKnex", "processKnex");
    addObjectCreate("application/createKnexCountries", "countriesForKnexCreate", ArrayList.class);
    addCallMethod("application/createKnexCountries/country", "add", 1);
    addCallParam("application/createKnexCountries/country", 0);
    addSetNext("application/createKnexCountries", "setCountriesForKnexCreate");

    addBeanPropertySetter("application/processAddlCtryData", "processAddlCtryData");
    addObjectCreate("application/createAddlCtryDataCountries", "countriesForAddlCtryDataCreate", ArrayList.class);
    addCallMethod("application/createAddlCtryDataCountries/country", "add", 1);
    addCallParam("application/createAddlCtryDataCountries/country", 0);
    addSetNext("application/createAddlCtryDataCountries", "setCountriesForAddlCtryDataCreate");

    addBeanPropertySetter("application/processKunnrExt", "processKunnrExt");
    addObjectCreate("application/createKunnrExtCountries", "countriesForKunnrExtCreate", ArrayList.class);
    addCallMethod("application/createKunnrExtCountries/country", "add", 1);
    addCallParam("application/createKunnrExtCountries/country", 0);
    addSetNext("application/createKunnrExtCountries", "setCountriesForKunnrExtCreate");

    addBeanPropertySetter("application/processKnbk", "processKnbk");
    addObjectCreate("application/createKnbkCountries", "countriesForKnbkCreate", ArrayList.class);
    addCallMethod("application/createKnbkCountries/country", "add", 1);
    addCallParam("application/createKnbkCountries/country", 0);
    addSetNext("application/createKnbkCountries", "setCountriesForKnbkCreate");

    addBeanPropertySetter("application/processKnva", "processKnva");
    addObjectCreate("application/createKnvaCountries", "countriesForKnvaCreate", ArrayList.class);
    addCallMethod("application/createKnvaCountries/country", "add", 1);
    addCallParam("application/createKnvaCountries/country", 0);
    addSetNext("application/createKnvaCountries", "setCountriesForKnvaCreate");

    addBeanPropertySetter("application/processKnvl", "processKnvl");
    addObjectCreate("application/createKnvlCountries", "countriesForKnvlCreate", ArrayList.class);
    addCallMethod("application/createKnvlCountries/country", "add", 1);
    addCallParam("application/createKnvlCountries/country", 0);
    addSetNext("application/createKnvlCountries", "setCountriesForKnvlCreate");

    addBeanPropertySetter("application/processSizeInfo", "processSizeInfo");
    addObjectCreate("application/createSizeInfoCountries", "countriesForSizeInfoCreate", ArrayList.class);
    addCallMethod("application/createSizeInfoCountries/country", "add", 1);
    addCallParam("application/createSizeInfoCountries/country", 0);
    addSetNext("application/createSizeInfoCountries", "setCountriesForSizeInfoCreate");
  }

}
