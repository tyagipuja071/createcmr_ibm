/*
 * File: geohandler.js
 * Description:
 * This contains the GEOHandler code to handle validations and special
 * execution flows that are specific to countries/GEOs
 */

var _forceExecuteAfterConfigs = false;
var SysLoc = (function() {

  return {
    ABU_DHABI : '680',
    ARGENTINA : '613',
    AUSTRALIA : '616',
    AUSTRIA : '618',
    BAHAMAS : '619',
    BANGLADESH : '615',
    BARBADOS : '621',
    BERMUDA : '627',
    BOLIVIA : '629',
    BRAZIL : '631',
    BRUNEI : '643',
    BULGARIA : '644',
    CAMBODIA : '720',
    CAYMAN_ISLANDS : '647',
    CHILE : '655',
    CHINA : '641',
    COLOMBIA : '661',
    COSTA_RICA : '663',
    CROATIA : '704',
    CYPRUS : '666',
    CZECH_REPUBLIC : '668',
    DENMARK : '678',
    DOMINICAN_REPUBLIC : '681',
    ECUADOR : '683',
    EGYPT : '865',
    EL_SALVADOR : '829',
    FINLAND : '702',
    GERMANY : '724',
    GREECE : '726',
    GUATEMALA : '731',
    GUYANA : '640',
    HONDURAS : '735',
    HONG_KONG : '738',
    HUNGARY : '740',
    INDIA : '744',
    INDONESIA : '749',
    IRELAND : '754',
    ISRAEL : '755',
    ITALY : '758',
    JAMAICA : '759',
    JAPAN : '760',
    KAZAKHSTAN : '694',
    LAOS : '714',
    MACAO : '736',
    MALTA : '780',
    MALASIA : '778',
    MEXICO : '781',
    MYANMAR : '646',
    NEPAL : '790',
    NETH_ANTILLES : '791',
    NEW_ZEALAND : '796',
    NICARAGUA : '799',
    NORWAY : '806',
    PAKISTAN : '808',
    PANAMA : '811',
    PARAGUAY : '813',
    PERU : '815',
    PHILIPPINES : '818',
    POLAND : '820',
    PORTUGAL : '822',
    SAINT_LUCIA : '839',
    SERBIA : '707',
    SINGAPORE : '834',
    SLOVENIA : '708',
    SPAIN : '838',
    SLOVAKIA : '693',
    SRI_LANKA : '652',
    SURINAME : '843',
    SWEDEN : '846',
    SWITZERLAND : '848',
    THAILAND : "856",
    TANZANIA : "851",
    MOROCCO : "642",
    TUNISIA : "729",
    TURKEY : '862',
    TRINIDAD_TOBAGO : '859',
    UK : '866',
    USA : '897',
    URUGUAY : '869',
    VENEZUELA : '871',
    VIETNAM : '852',
    ITALY : '758',
    SAUDI_ARABIA : '832',
    SOUTH_AFRICA : '864',
    MAURITIUS : '373',
    ANGOLA : '610',
    RUSSIA : '821',
    ROMANIA : '826',
    UKRAINE : '889',
    UNITED_ARAB_EMIRATES : '677',
    AZERBAIJAN : '358',
    TURKMENISTAN : '359',
    TAJIKISTAN : '363',
    ARMENIA : '607',
    ALBANIA : '603',
    BELARUS : '626',
    GEORGIA : '651',
    KYRGYZSTAN : '695',
    MACEDONIA : '705',
    UZBEKISTAN : '741',
    MOLDOVA : '787',
    BOSNIA_HERZEGOVINA : '699',
    MADAGASCAR : '700',
    CANADA : '649',
    KOREA : '766',
    KENYA : '764',
    TAIWAN : '858'
  };
})();

var GEOHandler = (function() {

  var afterConfigFunctions = {};
  afterConfigFunctions['WW'] = new Array();
  var beforeAddrLoadFunctions = {};
  beforeAddrLoadFunctions['WW'] = new Array();
  var toggleAddrTypeFunctions = {};
  toggleAddrTypeFunctions['WW'] = new Array();
  var noTGME = new Array();
  var copySupported = new Array();
  var copyValidator = null;
  var revertIsic = true;
  var addrNmSupported = new Array();

  var doNotCopyTypes = new Array();

  var afterTemplateLoadFunctions = {};
  afterTemplateLoadFunctions['WW'] = new Array();

  var nameAddressType = 'ZS01';

  var forceLockUnlock = function() {
  	console.log(">>>> forceLockUnlock");
    FormManager.readOnly('cmrIssuingCntry');
  }

  var getCMRIssuingCountry = function() {
   	console.log(">>>> getCMRIssuingCountry");
    var cntry = FormManager.getActualValue('cmrIssuingCntry');
    if (cntry == '' && typeof (_pagemodel) != 'undefined') {
      cntry = _pagemodel.cmrIssuingCntry;
    }
    forceLockUnlock();
    return cntry;
  };

  var getUserRole = function() {
    var role = null;
    if (typeof (_pagemodel) != 'undefined') {
      role = _pagemodel.userRole;
    }
    return role;
  };

  return {
    setRevertIsicBehavior : function(revert) {
      revertIsic = revert;
    },
    isRevertIsicBehavior : function() {
      return revertIsic;
    },
    setAddressTypeForName : function(addrType) {
      nameAddressType = addrType;
    },
    getAddressTypeForName : function() {
      return nameAddressType;
    },
    registerWWValidator : function(validator, roleCode, existingRequestOnly) {
      if (validator == null) {
        return;
      }
      if (existingRequestOnly && (dojo.byId("reqId").value == '0' || (typeof (_pagemodel) != 'undefined' && _pagemodel.reqId == 0))) {
        console.log('skipping WW validator for new entry: ' + validator.name);
        return;
      }
      if (roleCode != null) {
        var role = getUserRole();
        if (role == roleCode) {
          console.log('adding WW validator: ' + validator.name + ', role: ' + roleCode);
          validator();
        } else {
          console.log('skipping WW validator for ' + roleCode + ': ' + validator.name + ", current role: " + role);
        }
      } else {
        console.log('adding WW validator: ' + validator.name);
        validator();
      }
    },

    // register a country specific validator, can be except for that country
    registerValidator : function(validator, arrayOfCountryCodes, roleCode, existingRequestOnly, excludeCountries) {
      if (validator == null) {
        return;
      }
      if (existingRequestOnly && (dojo.byId("reqId").value == '0' || (typeof (_pagemodel) != 'undefined' && _pagemodel.reqId == 0))) {
        console.log('skipping validator for new entry: ' + validator.name + ", countries: " + getCMRIssuingCountry());
        return;
      }
      var cntry = getCMRIssuingCountry();
      if ((arrayOfCountryCodes.indexOf(cntry) < 0 && !excludeCountries) || (arrayOfCountryCodes.indexOf(cntry) >= 0 && excludeCountries)) {
        console.log('skipping validator for ' + getCMRIssuingCountry() + ': ' + validator.name);
        return;
      }
      var addValidator = false;
      var role = null;
      if (roleCode == null) {
        addValidator = true;
      } else {
        role = getUserRole();
        if (role == roleCode) {
          addValidator = true;
        }
      }
      if (!addValidator) {
        console.log('skipping validator for role ' + role + ': ' + validator.name);
        return;
      }
      console.log('adding validator for ' + (excludeCountries ? 'non ' : '') + getCMRIssuingCountry() + ': ' + validator.name);
      validator();
    },

    ROLE_PROCESSOR : 'Processor',
    ROLE_REQUESTER : 'Requester',

    addAfterConfig : function(afterConfigFunc, arrayOfCountryCodes, excludeCountries) {
      var cntry = getCMRIssuingCountry();
      if ((arrayOfCountryCodes.indexOf(cntry) < 0 && !excludeCountries) || (arrayOfCountryCodes.indexOf(cntry) >= 0 && excludeCountries)) {
        console.log('skipping after config function for ' + getCMRIssuingCountry() + ': ' + afterConfigFunc.name);
        return;
      }

      if (excludeCountries) {
        console.log('adding global after config: ' + afterConfigFunc.name);
        afterConfigFunctions['WW'].push(afterConfigFunc);
      } else {
        if (afterConfigFunctions[cntry] == null) {
          afterConfigFunctions[cntry] = new Array();
        }
        console.log('adding after config for ' + cntry + ': ' + afterConfigFunc.name);
        afterConfigFunctions[cntry].push(afterConfigFunc);
      }
    },

    executeAfterConfigs : function() {
      forceLockUnlock();
      if (dojo.byId("reqId").value == '0' && !_forceExecuteAfterConfigs) {
        console.log('skipping after configurations for new request..');
        return;
      }
      console.log('Executing after configuration load functions...');
      // execute globals
      for (var i = 0; i < afterConfigFunctions['WW'].length; i++) {
        afterConfigFunctions['WW'][i]();
      }

      var cntry = getCMRIssuingCountry();
      if (cntry != '' && afterConfigFunctions[cntry] != null) {
        for (var i = 0; i < afterConfigFunctions[cntry].length; i++) {
          afterConfigFunctions[cntry][i]();
        }
      }
    },

    addAfterTemplateLoad : function(afterTemplateLoadFunc, arrayOfCountryCodes, excludeCountries) {
      var cntry = getCMRIssuingCountry();
      if ((arrayOfCountryCodes.indexOf(cntry) < 0 && !excludeCountries) || (arrayOfCountryCodes.indexOf(cntry) >= 0 && excludeCountries)) {
        console.log('skipping after template load function for ' + arrayOfCountryCodes + ': ' + afterTemplateLoadFunc.name);
        return;
      }

      if (excludeCountries) {
        console.log('adding global after template load: ' + afterTemplateLoadFunc.name);
        afterTemplateLoadFunctions['WW'].push(afterTemplateLoadFunc);
      } else {
        if (afterTemplateLoadFunctions[cntry] == null) {
          afterTemplateLoadFunctions[cntry] = new Array();
        }
        console.log('adding after template load for ' + cntry + ': ' + afterTemplateLoadFunc.name);
        afterTemplateLoadFunctions[cntry].push(afterTemplateLoadFunc);
      }
    },

    executeAfterTemplateLoad : function(fromAddress, scenario, scenarioChanged) {
      if (dojo.byId("reqId").value == '0') {
        console.log('skipping after template load for new request..');
        return;
      }
      console.log('Executing after template load functions...');
      // execute globals
      for (var i = 0; i < afterTemplateLoadFunctions['WW'].length; i++) {
        afterTemplateLoadFunctions['WW'][i](fromAddress, scenario, scenarioChanged);
      }

      var cntry = getCMRIssuingCountry();
      if (cntry != '' && afterTemplateLoadFunctions[cntry] != null) {
        for (var i = 0; i < afterTemplateLoadFunctions[cntry].length; i++) {
          afterTemplateLoadFunctions[cntry][i](fromAddress, scenario, scenarioChanged);
        }
      }
    },

    addAddrFunction : function(beforeAddrLoadFunc, arrayOfCountryCodes, excludeCountries) {
      if (dojo.byId("reqId").value == '0') {
        console.log('skipping adding address function for not saved records..');
        return;
      }
      var cntry = getCMRIssuingCountry();
      if ((arrayOfCountryCodes.indexOf(cntry) < 0 && !excludeCountries) || (arrayOfCountryCodes.indexOf(cntry) >= 0 && excludeCountries)) {
        console.log('skipping address function for ' + arrayOfCountryCodes + ': ' + beforeAddrLoadFunc.name);
        return;
      }

      if (excludeCountries) {
        console.log('adding address function for WW: ' + beforeAddrLoadFunc.name);
        beforeAddrLoadFunctions['WW'].push(beforeAddrLoadFunc);
      } else {
        if (beforeAddrLoadFunctions[cntry] == null) {
          beforeAddrLoadFunctions[cntry] = new Array();
        }
        console.log('adding add address function for ' + cntry + ': ' + beforeAddrLoadFunc.name);
        beforeAddrLoadFunctions[cntry].push(beforeAddrLoadFunc);
      }
    },
    addToggleAddrTypeFunction : function(toggleFunc, arrayOfCountryCodes, excludeCountries) {
      if (dojo.byId("reqId").value == '0') {
        console.log('skipping adding toggle function for not saved records..');
        return;
      }
      var cntry = getCMRIssuingCountry();
      if ((arrayOfCountryCodes.indexOf(cntry) < 0 && !excludeCountries) || (arrayOfCountryCodes.indexOf(cntry) >= 0 && excludeCountries)) {
        console.log('skipping toggle function for ' + arrayOfCountryCodes + ': ' + toggleFunc.name);
        return;
      }

      if (excludeCountries) {
        console.log('adding toggle function for WW: ' + toggleFunc.name);
        toggleAddrTypeFunctions['WW'].push(toggleFunc);
      } else {
        if (toggleAddrTypeFunctions[cntry] == null) {
          toggleAddrTypeFunctions[cntry] = new Array();
        }
        console.log('adding toggle function for ' + cntry + ': ' + toggleFunc.name);
        toggleAddrTypeFunctions[cntry].push(toggleFunc);
      }
    },

    executeAddrFuncs : function(saving, afterValidate, copying) {
      console.log('Executing before address load functions...');
      // execute globals
      for (var i = 0; i < beforeAddrLoadFunctions['WW'].length; i++) {
        beforeAddrLoadFunctions['WW'][i]();
      }

      var cntry = getCMRIssuingCountry();
      var addressMode = cmr.addressMode;
      if (cntry != '' && beforeAddrLoadFunctions[cntry] != null) {
        for (var i = 0; i < beforeAddrLoadFunctions[cntry].length; i++) {
          beforeAddrLoadFunctions[cntry][i](cntry, copying ? 'COPY' : addressMode, saving, afterValidate);
        }
      }
    },
    executeToggleTypeFuncs : function(addressMode, details) {
      console.log('Executing toggle functions...');
      // execute globals
      for (var i = 0; i < toggleAddrTypeFunctions['WW'].length; i++) {
        toggleAddrTypeFunctions['WW'][i](cntry, addressMode, details);
      }

      var cntry = getCMRIssuingCountry();
      if (cntry != '' && toggleAddrTypeFunctions[cntry] != null) {
        for (var i = 0; i < toggleAddrTypeFunctions[cntry].length; i++) {
          toggleAddrTypeFunctions[cntry][i](cntry, addressMode, details);
        }
      }
    },
    skipTGMEForCountries : function(arrayOfCountries) {
      var cntry = getCMRIssuingCountry();
      if (arrayOfCountries && arrayOfCountries.indexOf(cntry) >= 0) {
        console.log('Disabling TGME for ' + cntry);
        noTGME.push(cntry);
      }
    },
    isTGMERequired : function(cntry) {
      // CREATCMR-5741 no TGME Addr Std
      return false;
      // for (var i = 0; i < noTGME.length; i++) {
      // if (noTGME[i] == cntry) {
      // return false;
      // }
      // }
      // return true;
    },
    enableCopyAddress : function(arrayOfCountries, validator, arrayOfTypesWithoutCopy) {
      var cntry = getCMRIssuingCountry();
      if (arrayOfCountries && arrayOfCountries.indexOf(cntry) >= 0) {
        console.log('Enabling Address copy for ' + cntry);
        copySupported.push(cntry);
      }
      if (validator) {
        copyValidator = validator;
      }
      if (arrayOfTypesWithoutCopy) {
        console.log('Do Not Copy Types: ' + arrayOfTypesWithoutCopy);
        doNotCopyTypes = arrayOfTypesWithoutCopy;
      }
    },
    canCopyAddressType : function(type) {
      return doNotCopyTypes == null ? true : doNotCopyTypes.indexOf(type) < 0;
    },
    getCreateOnlyAddresses : function() {
      var list = '';
      for (var i = 0; i < doNotCopyTypes.length; i++) {
        list += list.length > 0 ? '|' : '';
        list += doNotCopyTypes[i];
      }
      return list;
    },
    disableCopyAddress : function() {
      var cntry = getCMRIssuingCountry();
      var index = copySupported.indexOf(cntry);
      if (index > -1) {
        console.log('Disabling Address copy for ' + cntry);
        copySupported.splice(index, 1);
      }
    },
    validateCopy : function(addrType, arrayOfTargetTypes) {
      if (copyValidator) {
        return copyValidator(addrType, arrayOfTargetTypes);
      }
      return null;
    },
    isCopyAddressEnabled : function(cntry) {
      if (!cntry) {
        cntry = getCMRIssuingCountry();
      }
      return copySupported.indexOf(cntry) >= 0;
    },
    // #1285796 : special case for BR(631) but can be reused to other countries
    checkRoleBeforeAddAddrFunction : function(beforeAddrLoadFunc, arrayOfCountryCodes, excludeCountries, roleCode) {
      if ((typeof (_pagemodel) != 'undefined' && dojo.byId("reqId").value == '0') || (typeof (_pagemodel) != 'undefined' && _pagemodel.reqId == 0)) {
        console.log(beforeAddrLoadFunc.name + ' : skipping for not saved record. . .');
        return;
      }
      if (roleCode != null && roleCode != '') {
        var role = getUserRole();
        if (role == roleCode) {
          GEOHandler.addAddrFunction(beforeAddrLoadFunc, arrayOfCountryCodes, excludeCountries);
        } else {
          console.log(beforeAddrLoadFunc.name + ' : skipping for mismatch roles. . .');
          return;
        }
      } else {
        console.log(beforeAddrLoadFunc.name + ' : skipping for undefined role. . .');
        return;
      }
    },
    enableCustomerNamesOnAddress : function(arrayOfCountries) {
      var cntry = getCMRIssuingCountry();
      if (arrayOfCountries && arrayOfCountries.indexOf(cntry) >= 0) {
        addrNmSupported.push(cntry);
      }
    },
    customerNamesOnAddress : function(cntry) {
      if (!cntry) {
        cntry = getCMRIssuingCountry();
      }
      return addrNmSupported.indexOf(cntry) >= 0;
    },
  };
}());
