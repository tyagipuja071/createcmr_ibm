/**
 * 
 */
/* Register JP Javascripts */

/**
 * No single byte characters for JP
 */
var isPageLoad = true;
var setFieldsRequiredCount = 0;
function addSingleByteValidatorJP(cntry, addressMode, details) {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'ZC01' || reqType == 'ZE01') {
    FormManager.removeValidator('office', Validators.NO_SINGLE_BYTE);
    FormManager.removeValidator('dept', Validators.NO_SINGLE_BYTE);
    FormManager.removeValidator('contact', Validators.NO_SINGLE_BYTE);

  } else {
    FormManager.addValidator('office', Validators.NO_SINGLE_BYTE, [ 'Branch/Office' ]);
    FormManager.addValidator('dept', Validators.NO_SINGLE_BYTE, [ 'Department' ]);
    FormManager.addValidator('contact', Validators.NO_SINGLE_BYTE, [ 'Contact' ]);
  }
  FormManager.addValidator('custNm1', Validators.NO_SINGLE_BYTE, [ 'Customer Name-KANJI' ]);
  FormManager.addValidator('custNm2', Validators.NO_SINGLE_BYTE, [ 'Name-KANJI Continue' ]);
  FormManager.addValidator('custNm4', Validators.NO_SINGLE_BYTE, [ 'Katakana' ]);
  FormManager.addValidator('addrTxt', Validators.NO_SINGLE_BYTE, [ 'Address' ]);
  FormManager.addValidator('postCd', Validators.NO_SINGLE_BYTE, [ 'Postal Code' ]);
  FormManager.addValidator('bldg', Validators.NO_SINGLE_BYTE, [ 'Building' ]);
  FormManager.addValidator('custPhone', Validators.LATIN, [ 'Phone #' ]);
  FormManager.addValidator('custNm3', Validators.LATIN, [ 'Full English Name' ]);
  FormManager.addValidator('office', Validators.NO_SINGLE_BYTE, [ 'Branch/Office' ]);
  FormManager.addValidator('office', Validators.NO_HALF_ANGLE, [ 'Branch/Office' ]);
  FormManager.addValidator('custNm1', Validators.NO_HALF_ANGLE, [ 'Customer Name-KANJI' ]);
  FormManager.addValidator('custNm2', Validators.NO_HALF_ANGLE, [ 'Name-KANJI Continue' ]);
  FormManager.addValidator('custNm4', Validators.NO_HALF_ANGLE, [ 'Katakana' ]);
  FormManager.addValidator('addrTxt', Validators.NO_HALF_ANGLE, [ 'Address' ]);
  FormManager.addValidator('bldg', Validators.NO_HALF_ANGLE, [ 'Building' ]);
  FormManager.addValidator('dept', Validators.NO_HALF_ANGLE, [ 'Department' ]);
  FormManager.addValidator('contact', Validators.NO_HALF_ANGLE, [ 'Contact' ]);
}
function afterConfigForJP() {
  console.log(">>> Process afterConfigForJP... >> ");
  // Story 1747794 The field of DUNS NO. _Company needs to added to
  // company-level address
  if (FormManager.getActualValue('reqType') == 'U' && FormManager.getActualValue('viewOnlyPage') != 'true') {
    FormManager.enable('dunsNo');
  }
  // Story 1747794
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    FormManager.readOnly('educAllowCd');
    FormManager.readOnly('outsourcingService');
    FormManager.readOnly('zseriesSw');
    FormManager.readOnly('crsCd');
    FormManager.readOnly('billingProcCd');
    FormManager.readOnly('invoiceSplitCd');

    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.readOnly('prodType_1');
    FormManager.readOnly('prodType_2');
    FormManager.readOnly('prodType_3');
    FormManager.readOnly('prodType_4');
    FormManager.readOnly('prodType_5');
    FormManager.readOnly('prodType_6');
    FormManager.readOnly('prodType_7');
    FormManager.readOnly('prodType_8');

    FormManager.readOnly('originatorNm');
    FormManager.readOnly('requestDueDateTemp');

  }

  FormManager.addValidator('email2', Validators.NO_SINGLE_BYTE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('email2', Validators.NO_HALF_ANGLE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('abbrevNm', Validators.LATIN, [ 'Account Abbreviated Name' ]);

  var _custSubGrpHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
    setCustNmDetailOnScenario();
    setFieldsRequired();
    setCmrNoCmrNo2Required();
    setAccntAbbNmOnScrnarioChange();
    setAccountAbbNmRequired();
    performDPLCheck4JP();
    setDefaultValueForChargeCode();
    setTier2Required();
    disableRequestFor();
    disableProductType();
    showHideJSIC();
    showHideSubindustry();
    setOutsourcingServiceRequired();
    setJSICSubIndustryCdOptional();

    disableFieldsForUpdateOnScenarios();
    setCSBORequired();
    setCSBOOnScenarioChange();
    setSalesBusOffCdRequired();
    setSalesTeamCdEditoble();
    // CREATCMR-6694
    setAdminDeptOptional();
    addScenarioDriven()
  });
  if (_custSubGrpHandler && _custSubGrpHandler[0]) {
    _custSubGrpHandler[0].onChange();
  }
  // CREATCMR-788
  addressQuotationValidator();
}
function addScenarioDriven() {
  var custType = FormManager.getActualValue('custType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var addrType = FormManager.getActualValue('addrType');
  var _role = null;
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }
  if (custSubGrp == 'EUCMR' || custSubGrp == 'WHCMR') {
    FormManager.enable('outsourcingService', Validators.REQUIRED)
    FormManager.removeValidator('zseriesSw', Validators.REQUIRED)
    FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED)
    FormManager.addValidator('email3', Validators.REQUIRED, [ 'BP Company Name' ])
    FormManager.removeValidator('salesTeamCd', Validators.REQUIRED)
    FormManager.removeValidator('jsicCd', Validators.REQUIRED)
    FormManager.removeValidator('subIndustryCd', Validators.REQUIRED)
    if (FormManager.getActualValue('reqType') != 'C') {
      FormManager.removeValidator('email3', Validators.REQUIRED)
    }
    if (_role == 'Processor') {
      FormManager.enable('cmrNo2', Validators.REQUIRED)
      FormManager.removeValidator('csBo', Validators.REQUIRED)
    }
    if (custType == 'A') {
      FormManager.enable('tier2', Validators.REQUIRED)
      FormManager.removeValidator('custClass', Validators.REQUIRED)
      if (addrType == 'ZC01') {
        FormManager.enable('city2', Validators.REQUIRED);
      } else if (addrType == 'ZE01') {
        FormManager.enable('divn', Validators.REQUIRED, [ 'Estab No.' ]);
      } else if (addrType != 'ZC01' && addrType != 'ZE01') {
        FormManager.removeValidator('postCd', Validators.REQUIRED);
        FormManager.removeValidator('custPhone', Validators.REQUIRED);
        FormManager.removeValidator('custNm4', Validators.REQUIRED);
      }
    }
  }
}
function setCustNmDetailOnScenario() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var chargeCode = 'アウトソーシング・サービス';
  var chargeCdValue = FormManager.getActualValue('chargeCd');
  chargeCdValue = convert2DBCS(chargeCdValue);
  var projectCode = FormManager.getActualValue('soProjectCd');
  projectCode = convert2DBCS(projectCode);
  var custNmDetail = '';
  var accountCustNMKANJI = '';

  var qParams = {
    _qall : 'Y',
    REQ_ID : FormManager.getActualValue('reqId'),
    ADDR_TYPE : 'ZS01',
  };
  var results = cmr.query('GET.ZS01_CUST_NM_KANJI', qParams);
  if (results != null && results.length > 0) {
    accountCustNMKANJI = results[0].ret1.trim() + results[0].ret2.trim();
  }

  if (custSubGrp == 'STOSB' || custSubGrp == 'STOSC') {
    if ((_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp)
        && (_pagemodel.soProjectCd != null && FormManager.getActualValue('soProjectCd') == _pagemodel.soProjectCd)) {
      return;
    }
    if (projectCode != null && projectCode.length >= 5) {
      custNmDetail = chargeCode + '（' + projectCode.substring(0, 5) + '）';
    } else {
      custNmDetail = chargeCode + '（' + projectCode + '）';
    }
  } else if (custSubGrp == 'STOSI') {
    if ((_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp)
        && (_pagemodel.chargeCd != null && FormManager.getActualValue('chargeCd') == _pagemodel.chargeCd)) {
      return;
    }
    if (projectCode != null && projectCode.length >= 5) {
      custNmDetail = accountCustNMKANJI + '（' + projectCode.substring(0, 5) + '）';
    } else {
      custNmDetail = accountCustNMKANJI + '（' + projectCode + '）';
    }
  } else if (custSubGrp == 'INTER' || custSubGrp == 'BIJSC') {
    if (_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp) {
      return;
    }
    custNmDetail = accountCustNMKANJI;
  } else if (custSubGrp == '') {
    if (FormManager.getActualValue('email2') != '') {
      return;
    }
  } else {
    if (_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp) {
      return;
    }
    custNmDetail = accountCustNMKANJI;
  }

  if (custNmDetail.length > 30) {
    custNmDetail = custNmDetail.substring(0, 30);
  }
  FormManager.setValue('email2', custNmDetail);
  FormManager.addValidator('email2', Validators.NO_SINGLE_BYTE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('email2', Validators.NO_HALF_ANGLE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
}
function convert2DBCS(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0) {
    modifiedVal = input.toUpperCase();
    // modifiedVal = modifiedVal.replace(/[^\d]/g, '');
    modifiedVal = modifiedVal.replace(/1/g, '１');
    modifiedVal = modifiedVal.replace(/2/g, '２');
    modifiedVal = modifiedVal.replace(/3/g, '３');
    modifiedVal = modifiedVal.replace(/4/g, '４');
    modifiedVal = modifiedVal.replace(/5/g, '５');
    modifiedVal = modifiedVal.replace(/6/g, '６');
    modifiedVal = modifiedVal.replace(/7/g, '７');
    modifiedVal = modifiedVal.replace(/8/g, '８');
    modifiedVal = modifiedVal.replace(/9/g, '９');
    modifiedVal = modifiedVal.replace(/0/g, '０');
    modifiedVal = modifiedVal.replace(/A/g, 'Ａ');
    modifiedVal = modifiedVal.replace(/B/g, 'Ｂ');
    modifiedVal = modifiedVal.replace(/C/g, 'Ｃ');
    modifiedVal = modifiedVal.replace(/D/g, 'Ｄ');
    modifiedVal = modifiedVal.replace(/E/g, 'Ｅ');
    modifiedVal = modifiedVal.replace(/F/g, 'Ｆ');
    modifiedVal = modifiedVal.replace(/G/g, 'Ｇ');
    modifiedVal = modifiedVal.replace(/H/g, 'Ｈ');
    modifiedVal = modifiedVal.replace(/I/g, 'Ｉ');
    modifiedVal = modifiedVal.replace(/J/g, 'Ｊ');
    modifiedVal = modifiedVal.replace(/K/g, 'Ｋ');
    modifiedVal = modifiedVal.replace(/L/g, 'Ｌ');
    modifiedVal = modifiedVal.replace(/M/g, 'Ｍ');
    modifiedVal = modifiedVal.replace(/N/g, 'Ｎ');
    modifiedVal = modifiedVal.replace(/O/g, 'Ｏ');
    modifiedVal = modifiedVal.replace(/P/g, 'Ｐ');
    modifiedVal = modifiedVal.replace(/Q/g, 'Ｑ');
    modifiedVal = modifiedVal.replace(/R/g, 'Ｒ');
    modifiedVal = modifiedVal.replace(/S/g, 'Ｓ');
    modifiedVal = modifiedVal.replace(/T/g, 'Ｔ');
    modifiedVal = modifiedVal.replace(/U/g, 'Ｕ');
    modifiedVal = modifiedVal.replace(/V/g, 'Ｖ');
    modifiedVal = modifiedVal.replace(/W/g, 'Ｗ');
    modifiedVal = modifiedVal.replace(/X/g, 'Ｘ');
    modifiedVal = modifiedVal.replace(/Y/g, 'Ｙ');
    modifiedVal = modifiedVal.replace(/Z/g, 'Ｚ');
    modifiedVal = modifiedVal.replace(/ /g, '　');
  }
  return modifiedVal;
}
function convert2DBCSIgnoreCase(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0) {
    modifiedVal = input;
    // modifiedVal = modifiedVal.replace(/[^\d]/g, '');
    modifiedVal = modifiedVal.replace(/1/g, '１');
    modifiedVal = modifiedVal.replace(/2/g, '２');
    modifiedVal = modifiedVal.replace(/3/g, '３');
    modifiedVal = modifiedVal.replace(/4/g, '４');
    modifiedVal = modifiedVal.replace(/5/g, '５');
    modifiedVal = modifiedVal.replace(/6/g, '６');
    modifiedVal = modifiedVal.replace(/7/g, '７');
    modifiedVal = modifiedVal.replace(/8/g, '８');
    modifiedVal = modifiedVal.replace(/9/g, '９');
    modifiedVal = modifiedVal.replace(/0/g, '０');
    modifiedVal = modifiedVal.replace(/a/g, 'ａ');
    modifiedVal = modifiedVal.replace(/b/g, 'ｂ');
    modifiedVal = modifiedVal.replace(/c/g, 'ｃ');
    modifiedVal = modifiedVal.replace(/d/g, 'ｄ');
    modifiedVal = modifiedVal.replace(/e/g, 'ｅ');
    modifiedVal = modifiedVal.replace(/f/g, 'ｆ');
    modifiedVal = modifiedVal.replace(/g/g, 'ｇ');
    modifiedVal = modifiedVal.replace(/h/g, 'ｈ');
    modifiedVal = modifiedVal.replace(/i/g, 'ｉ');
    modifiedVal = modifiedVal.replace(/j/g, 'ｊ');
    modifiedVal = modifiedVal.replace(/k/g, 'ｋ');
    modifiedVal = modifiedVal.replace(/l/g, 'ｌ');
    modifiedVal = modifiedVal.replace(/m/g, 'ｍ');
    modifiedVal = modifiedVal.replace(/n/g, 'ｎ');
    modifiedVal = modifiedVal.replace(/o/g, 'ｏ');
    modifiedVal = modifiedVal.replace(/p/g, 'ｐ');
    modifiedVal = modifiedVal.replace(/q/g, 'ｑ');
    modifiedVal = modifiedVal.replace(/r/g, 'ｒ');
    modifiedVal = modifiedVal.replace(/s/g, 'ｓ');
    modifiedVal = modifiedVal.replace(/t/g, 'ｔ');
    modifiedVal = modifiedVal.replace(/u/g, 'ｕ');
    modifiedVal = modifiedVal.replace(/v/g, 'ｖ');
    modifiedVal = modifiedVal.replace(/w/g, 'ｗ');
    modifiedVal = modifiedVal.replace(/x/g, 'ｘ');
    modifiedVal = modifiedVal.replace(/y/g, 'ｙ');
    modifiedVal = modifiedVal.replace(/z/g, 'ｚ');
    modifiedVal = modifiedVal.replace(/A/g, 'Ａ');
    modifiedVal = modifiedVal.replace(/B/g, 'Ｂ');
    modifiedVal = modifiedVal.replace(/C/g, 'Ｃ');
    modifiedVal = modifiedVal.replace(/D/g, 'Ｄ');
    modifiedVal = modifiedVal.replace(/E/g, 'Ｅ');
    modifiedVal = modifiedVal.replace(/F/g, 'Ｆ');
    modifiedVal = modifiedVal.replace(/G/g, 'Ｇ');
    modifiedVal = modifiedVal.replace(/H/g, 'Ｈ');
    modifiedVal = modifiedVal.replace(/I/g, 'Ｉ');
    modifiedVal = modifiedVal.replace(/J/g, 'Ｊ');
    modifiedVal = modifiedVal.replace(/K/g, 'Ｋ');
    modifiedVal = modifiedVal.replace(/L/g, 'Ｌ');
    modifiedVal = modifiedVal.replace(/M/g, 'Ｍ');
    modifiedVal = modifiedVal.replace(/N/g, 'Ｎ');
    modifiedVal = modifiedVal.replace(/O/g, 'Ｏ');
    modifiedVal = modifiedVal.replace(/P/g, 'Ｐ');
    modifiedVal = modifiedVal.replace(/Q/g, 'Ｑ');
    modifiedVal = modifiedVal.replace(/R/g, 'Ｒ');
    modifiedVal = modifiedVal.replace(/S/g, 'Ｓ');
    modifiedVal = modifiedVal.replace(/T/g, 'Ｔ');
    modifiedVal = modifiedVal.replace(/U/g, 'Ｕ');
    modifiedVal = modifiedVal.replace(/V/g, 'Ｖ');
    modifiedVal = modifiedVal.replace(/W/g, 'Ｗ');
    modifiedVal = modifiedVal.replace(/X/g, 'Ｘ');
    modifiedVal = modifiedVal.replace(/Y/g, 'Ｙ');
    modifiedVal = modifiedVal.replace(/Z/g, 'Ｚ');
    modifiedVal = modifiedVal.replace(/ /g, '　');
    modifiedVal = replaceAndSymbol(modifiedVal);
    modifiedVal = replaceCrossbarSymbol(modifiedVal);
  }
  return modifiedVal;
}
function convertHalfToFullKatakana(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0) {
    modifiedVal = input;
    modifiedVal = modifiedVal.replace(/ｱ/g, 'ア');
    modifiedVal = modifiedVal.replace(/ｲ/g, 'イ');
    modifiedVal = modifiedVal.replace(/ｳ/g, 'ウ');
    modifiedVal = modifiedVal.replace(/ｴ/g, 'エ');
    modifiedVal = modifiedVal.replace(/ｵ/g, 'オ');
    modifiedVal = modifiedVal.replace(/ｶ/g, 'カ');
    modifiedVal = modifiedVal.replace(/ｷ/g, 'キ');
    modifiedVal = modifiedVal.replace(/ｸ/g, 'ク');
    modifiedVal = modifiedVal.replace(/ｹ/g, 'ケ');
    modifiedVal = modifiedVal.replace(/ｺ/g, 'コ');
    modifiedVal = modifiedVal.replace(/ｻ/g, 'サ');
    modifiedVal = modifiedVal.replace(/ｼ/g, 'シ');
    modifiedVal = modifiedVal.replace(/ｽ/g, 'ス');
    modifiedVal = modifiedVal.replace(/ｾ/g, 'セ');
    modifiedVal = modifiedVal.replace(/ｿ/g, 'ソ');
    modifiedVal = modifiedVal.replace(/ﾀ/g, 'タ');
    modifiedVal = modifiedVal.replace(/ﾁ/g, 'チ');
    modifiedVal = modifiedVal.replace(/ﾂ/g, 'ツ');
    modifiedVal = modifiedVal.replace(/ﾃ/g, 'テ');
    modifiedVal = modifiedVal.replace(/ﾄ/g, 'ト');
    modifiedVal = modifiedVal.replace(/ﾅ/g, 'ナ');
    modifiedVal = modifiedVal.replace(/ﾆ/g, 'ニ');
    modifiedVal = modifiedVal.replace(/ﾇ/g, 'ヌ');
    modifiedVal = modifiedVal.replace(/ﾈ/g, 'ネ');
    modifiedVal = modifiedVal.replace(/ﾉ/g, 'ノ');
    modifiedVal = modifiedVal.replace(/ﾊ/g, 'ハ');
    modifiedVal = modifiedVal.replace(/ﾋ/g, 'ヒ');
    modifiedVal = modifiedVal.replace(/ﾌ/g, 'フ');
    modifiedVal = modifiedVal.replace(/ﾍ/g, 'ヘ');
    modifiedVal = modifiedVal.replace(/ﾎ/g, 'ホ');
    modifiedVal = modifiedVal.replace(/ﾏ/g, 'マ');
    modifiedVal = modifiedVal.replace(/ﾐ/g, 'ミ');
    modifiedVal = modifiedVal.replace(/ﾑ/g, 'ム');
    modifiedVal = modifiedVal.replace(/ﾒ/g, 'メ');
    modifiedVal = modifiedVal.replace(/ﾓ/g, 'モ');
    modifiedVal = modifiedVal.replace(/ﾔ/g, 'ヤ');
    modifiedVal = modifiedVal.replace(/ﾕ/g, 'ユ');
    modifiedVal = modifiedVal.replace(/ﾖ/g, 'ヨ');
    modifiedVal = modifiedVal.replace(/ﾗ/g, 'ラ');
    modifiedVal = modifiedVal.replace(/ﾘ/g, 'リ');
    modifiedVal = modifiedVal.replace(/ﾙ/g, 'ル');
    modifiedVal = modifiedVal.replace(/ﾚ/g, 'レ');
    modifiedVal = modifiedVal.replace(/ﾛ/g, 'ロ');
    modifiedVal = modifiedVal.replace(/ﾜ/g, 'ワ');
    modifiedVal = modifiedVal.replace(/ｦ/g, 'ヲ');
    modifiedVal = modifiedVal.replace(/ﾝ/g, 'ン');
    modifiedVal = modifiedVal.replace(/ｶﾞ/g, 'ガ');
    modifiedVal = modifiedVal.replace(/ｷﾞ/g, 'ギ');
    modifiedVal = modifiedVal.replace(/ｸﾞ/g, 'グ');
    modifiedVal = modifiedVal.replace(/ｹﾞ/g, 'ゲ');
    modifiedVal = modifiedVal.replace(/ｺﾞ/g, 'ゴ');
    modifiedVal = modifiedVal.replace(/ｻﾞ/g, 'ザ');
    modifiedVal = modifiedVal.replace(/ｼﾞ/g, 'ジ');
    modifiedVal = modifiedVal.replace(/ｽﾞ/g, 'ズ');
    modifiedVal = modifiedVal.replace(/ｾﾞ/g, 'ゼ');
    modifiedVal = modifiedVal.replace(/ｿﾞ/g, 'ゾ');
    modifiedVal = modifiedVal.replace(/ﾀﾞ/g, 'ダ');
    modifiedVal = modifiedVal.replace(/ﾁﾞ/g, 'ヂ');
    modifiedVal = modifiedVal.replace(/ﾂﾞ/g, 'ヅ');
    modifiedVal = modifiedVal.replace(/ﾃﾞ/g, 'デ');
    modifiedVal = modifiedVal.replace(/ﾄﾞ/g, 'ド');
    modifiedVal = modifiedVal.replace(/ﾊﾞ/g, 'バ');
    modifiedVal = modifiedVal.replace(/ﾋﾞ/g, 'ビ');
    modifiedVal = modifiedVal.replace(/ﾌﾞ/g, 'ブ');
    modifiedVal = modifiedVal.replace(/ﾍﾞ/g, 'ベ');
    modifiedVal = modifiedVal.replace(/ﾎﾞ/g, 'ボ');
    modifiedVal = modifiedVal.replace(/ﾊﾟ/g, 'パ');
    modifiedVal = modifiedVal.replace(/ﾋﾟ/g, 'ピ');
    modifiedVal = modifiedVal.replace(/ﾌﾟ/g, 'プ');
    modifiedVal = modifiedVal.replace(/ﾍﾟ/g, 'ペ');
    modifiedVal = modifiedVal.replace(/ﾎﾟ/g, 'ポ');
    modifiedVal = modifiedVal.replace(/ｷｬ/g, 'キャ');
    modifiedVal = modifiedVal.replace(/ｷｭ/g, 'キュ');
    modifiedVal = modifiedVal.replace(/ｷｮ/g, 'キョ');
    modifiedVal = modifiedVal.replace(/ｷﾞｬ/g, 'ギャ');
    modifiedVal = modifiedVal.replace(/ｷﾞｭ/g, 'ギュ');
    modifiedVal = modifiedVal.replace(/ｷﾞｮ/g, 'ギョ');
    modifiedVal = modifiedVal.replace(/ｼｬ/g, 'シャ');
    modifiedVal = modifiedVal.replace(/ｼｭ/g, 'シュ');
    modifiedVal = modifiedVal.replace(/ｼｮ/g, 'ショ');
    modifiedVal = modifiedVal.replace(/ｼﾞｬ/g, 'ジャ');
    modifiedVal = modifiedVal.replace(/ｼﾞｭ/g, 'ジュ');
    modifiedVal = modifiedVal.replace(/ｼﾞｮ/g, 'ジョ');
    modifiedVal = modifiedVal.replace(/ﾁｬ/g, 'チャ');
    modifiedVal = modifiedVal.replace(/ﾁｭ/g, 'チュ');
    modifiedVal = modifiedVal.replace(/ﾁｮ/g, 'チョ');
    modifiedVal = modifiedVal.replace(/ﾆｬ/g, 'ニャ');
    modifiedVal = modifiedVal.replace(/ﾆｭ/g, 'ニュ');
    modifiedVal = modifiedVal.replace(/ﾆｮ/g, 'ニョ');
    modifiedVal = modifiedVal.replace(/ﾋｬ/g, 'ヒャ');
    modifiedVal = modifiedVal.replace(/ﾋｭ/g, 'ヒュ');
    modifiedVal = modifiedVal.replace(/ﾋｮ/g, 'ヒョ');
    modifiedVal = modifiedVal.replace(/ﾋﾞｬ/g, 'ビャ');
    modifiedVal = modifiedVal.replace(/ﾋﾞｭ/g, 'ビュ');
    modifiedVal = modifiedVal.replace(/ﾋﾞｮ/g, 'ビョ');
    modifiedVal = modifiedVal.replace(/ﾋﾟｬ/g, 'ピャ');
    modifiedVal = modifiedVal.replace(/ﾋﾟｭ/g, 'ピュ');
    modifiedVal = modifiedVal.replace(/ﾋﾟｮ/g, 'ピョ');
    modifiedVal = modifiedVal.replace(/ﾐｬ/g, 'ミャ');
    modifiedVal = modifiedVal.replace(/ﾐｭ/g, 'ミュ');
    modifiedVal = modifiedVal.replace(/ﾐｮ/g, 'ミョ');
    modifiedVal = modifiedVal.replace(/ﾘｬ/g, 'リャ');
    modifiedVal = modifiedVal.replace(/ﾘｭ/g, 'リュ');
    modifiedVal = modifiedVal.replace(/ﾘｮ/g, 'リョ');
    modifiedVal = modifiedVal.replace(/ /g, '　');
  }
  return modifiedVal;
}
function setFieldsRequired() {
  setFieldsRequiredCount += 1;
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _role = null;
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }

  switch (custSubGrp) {
  case '':
    return;
    break;
  case 'NORML':
  case 'EUCMR':
  case 'WHCMR':
    if (!isPageLoad && setFieldsRequiredCount > 2) {
      // Unlock Outsourcing Service Show zSeries SW
      FormManager.hide('DirectBp', 'creditBp');
      FormManager.removeValidator('creditBp', Validators.REQUIRED);
      FormManager.disable('creditBp');

      FormManager.show('zSeriesSw', 'zseriesSw');
      FormManager.addValidator('zseriesSw', Validators.REQUIRED, [ 'zSeries SW' ], 'MAIN_CUST_TAB');
      FormManager.enable('zseriesSw');

      FormManager.enable('outsourcingService');
      if (FormManager.getField('outsourcingService').set) {
        FormManager.getField('outsourcingService').set('checked', false);
      } else if (FormManager.getField('outsourcingService')) {
        FormManager.getField('outsourcingService').checked = false;
      }
    }
    break;
  case 'OUTSC':
    if (!isPageLoad && setFieldsRequiredCount > 2) {
      // Unlock Outsourcing Service Show zSeries SW
      FormManager.hide('DirectBp', 'creditBp');
      FormManager.removeValidator('creditBp', Validators.REQUIRED);
      FormManager.disable('creditBp');

      FormManager.show('zSeriesSw', 'zseriesSw');
      FormManager.addValidator('zseriesSw', Validators.REQUIRED, [ 'zSeries SW' ], 'MAIN_CUST_TAB');
      FormManager.enable('zseriesSw');

      FormManager.enable('outsourcingService');
      if (FormManager.getField('outsourcingService').set) {
        FormManager.getField('outsourcingService').set('checked', false);
      } else if (FormManager.getField('outsourcingService')) {
        FormManager.getField('outsourcingService').checked = false;
      }
    }
    break;
  case 'ISOCU':
    // if (!isPageLoad && setFieldsRequiredCount > 2) {
    // Unlock Outsourcing Service Show zSeries SW
    // FormManager.hide('DirectBp', 'creditBp');
    // FormManager.removeValidator('creditBp', Validators.REQUIRED);
    // FormManager.disable('creditBp');
    //
    // FormManager.show('zSeriesSw', 'zseriesSw');
    // FormManager.addValidator('zseriesSw', Validators.REQUIRED, [ 'zSeries SW'
    // ], 'MAIN_CUST_TAB');
    // FormManager.enable('zseriesSw');
    //
    // FormManager.enable('outsourcingService');
    // if (FormManager.getField('outsourcingService').set) {
    // FormManager.getField('outsourcingService').set('checked', false);
    // } else if (FormManager.getField('outsourcingService')) {
    // FormManager.getField('outsourcingService').checked = false;
    // }
    // }
    break;
  case 'STOSB':
    // For Defect 1753780
    if (_role == 'Requester') {
      FormManager.removeValidator('jsicCd', Validators.REQUIRED);
      FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
    }
    break;
  case 'STOSC':
    // For Defect 1753780
    if (_role == 'Requester') {
      FormManager.removeValidator('jsicCd', Validators.REQUIRED);
      FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
    }
    break;
  case 'STOSI':
    break;
  case 'BCEXA':
  case 'BFKSC':
    FormManager.disable('outsourcingService');
    FormManager.removeValidator('zseriesSw', Validators.REQUIRED);
    break;
  case 'INTER':
    if (!isPageLoad && setFieldsRequiredCount > 2) {
      // Disable Outsourcing Service, zSeries SW
      FormManager.hide('DirectBp', 'creditBp');
      FormManager.removeValidator('creditBp', Validators.REQUIRED);
      FormManager.disable('creditBp');

      FormManager.show('zSeriesSw', 'zseriesSw');
      FormManager.addValidator('zseriesSw', Validators.REQUIRED, [ 'zSeries SW' ], 'MAIN_CUST_TAB');
      FormManager.enable('zseriesSw');

      if (FormManager.getField('outsourcingService').set) {
        FormManager.getField('outsourcingService').set('checked', false);
      } else if (FormManager.getField('outsourcingService')) {
        FormManager.getField('outsourcingService').checked = false;
      }
      FormManager.disable('outsourcingService');
    }
    break;
  // CREATCMR-6854
  case 'BPWPQ':
    FormManager.setValue('salesTeamCd', 'D0000');
    FormManager.readOnly('salesTeamCd');

    FormManager.setValue('tier2', '');
    FormManager.readOnly('tier2');

    FormManager.setValue('billToCustNo', '');
    FormManager.enable('billToCustNo');

    FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');
    break;
  // CREATCMR-6854
  default:
    // if (!isPageLoad && setFieldsRequiredCount > 2) {
    // // Disable Outsourcing Service, zSeries SW
    // FormManager.hide('DirectBp', 'creditBp');
    // FormManager.removeValidator('creditBp', Validators.REQUIRED);
    // FormManager.disable('creditBp');
    //
    // FormManager.show('zSeriesSw', 'zseriesSw');
    // FormManager.removeValidator('zseriesSw', Validators.REQUIRED);
    // FormManager.disable('zseriesSw');
    //
    // if (FormManager.getField('outsourcingService').set) {
    // FormManager.getField('outsourcingService').set('checked', false);
    // } else if (FormManager.getField('outsourcingService')) {
    // FormManager.getField('outsourcingService').checked = false;
    // }
    // FormManager.disable('outsourcingService');
    // }
    break;
  }
}
function setCmrNoCmrNo2Required() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _role = null;
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }

  switch (custSubGrp) {
  case '':
    return;
    break;
  case 'ISOCU':
    if (_role == 'Requester') {
      FormManager.readOnly('cmrNo');
      FormManager.readOnly('cmrNo2');
      FormManager.resetValidations('cmrNo');
      FormManager.resetValidations('cmrNo2');
    } else if (_role == 'Processor') {
      FormManager.enable('cmrNo');
      FormManager.enable('cmrNo2');
      FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('cmrNo2', Validators.REQUIRED, [ 'CMR Number 2' ], 'MAIN_IBM_TAB');
    }
    break;
  case 'STOSB':
    if (_role == 'Requester') {
      FormManager.readOnly('cmrNo');
      FormManager.resetValidations('cmrNo');
      FormManager.readOnly('cmrNo2');
      FormManager.resetValidations('cmrNo2');
    } else if (_role == 'Processor') {
      FormManager.readOnly('cmrNo');
      FormManager.resetValidations('cmrNo');
      FormManager.enable('cmrNo2');
      FormManager.addValidator('cmrNo2', Validators.REQUIRED, [ 'CMR Number 2' ], 'MAIN_IBM_TAB');
    }
    break;
  case 'STOSI':
    if (_role == 'Requester') {
      FormManager.readOnly('cmrNo');
      FormManager.resetValidations('cmrNo');
      FormManager.readOnly('cmrNo2');
      FormManager.resetValidations('cmrNo2');
    } else if (_role == 'Processor') {
      FormManager.enable('cmrNo');
      FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
      FormManager.readOnly('cmrNo2');
      FormManager.resetValidations('cmrNo2');
    }
    break;
  case 'BCEXA':
  case 'BFKSC':
    if (_role == 'Requester') {
      FormManager.setValue('cmrNo', '');
      FormManager.setValue('cmrNo2', '');
    }
    FormManager.readOnly('cmrNo');
    FormManager.readOnly('cmrNo2');
    FormManager.resetValidations('cmrNo');
    FormManager.resetValidations('cmrNo2');
    break;
  case 'INTER':
    FormManager.enable('cmrNo');
    if (_role == 'Processor') {
      FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
    }
    FormManager.setValue('cmrNo2', '');
    FormManager.readOnly('cmrNo2');
    FormManager.resetValidations('cmrNo2');
    break;
  default:
    if (_role == 'Requester') {
      FormManager.setValue('cmrNo', '');
      FormManager.setValue('cmrNo2', '');
    }
    FormManager.readOnly('cmrNo');
    FormManager.resetValidations('cmrNo');
    FormManager.readOnly('cmrNo2');
    FormManager.resetValidations('cmrNo2');
    break;
  }

  // 1785164 - processor specific logic
  if (typeof (_pagemodel) != 'undefined' && _pagemodel.userRole == 'Processor') {
    if (dojo.byId('disableAutoProc') && dojo.byId('disableAutoProc').checked) {
      if (FormManager.getActualValue('reqType') == 'C') {
        FormManager.enable('cmrNo');
        if (custSubGrp == "EUCMR" || "WHCMR") {
          FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');
        }
      }
    }
  }
}
function setAccntAbbNmOnScrnarioChange() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custTypeinDB = _pagemodel.custSubGrp;
  if (custSubGrp == '') {
    return;
  }
  if (custTypeinDB != null && custSubGrp == custTypeinDB) {
    // FormManager.setValue('abbrevNm', _pagemodel.abbrevNm);
    return;
  } else {
    setAccountAbbNm();
  }
}

// CREATCMR-6694
function setAdminDeptOptional() {
  var custGrp = FormManager.getActualValue('custGrp');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('reqType') == 'C') {
    if (custGrp == 'IBMTP' && custSubGrp == 'INTER') {
      FormManager.removeValidator('adminDeptCd', Validators.REQUIRED);
      FormManager.removeValidator('adminDeptLine', Validators.REQUIRED);
    }
  }
}

function setAccountAbbNm() {
  if (FormManager.getActualValue('reqType') == 'C') {
    setAccountAbbNmForCreate();
  } else if (FormManager.getActualValue('reqType') == 'U') {
    setAccountAbbNmForUpdate();
  }
}
function setAccountAbbNmForCreate() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == '') {
    return;
  }
  var oldAccountAbbNm = getZS01CustNm3();
  if (_pagemodel.abbrevNm == null) {
    oldAccountAbbNm = FormManager.getActualValue('abbrevNm');
  }
  var accountAbbNm = '';
  switch (custSubGrp) {
  case 'OUTSC':
    if (_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp) {
      return;
    }
    if (oldAccountAbbNm != null && oldAccountAbbNm != '' && oldAccountAbbNm != undefined) {
      if (oldAccountAbbNm.length > 17) {
        accountAbbNm = oldAccountAbbNm.substring(0, 17) + '   SO';
      } else {
        var blankSpaceLength = 22 - oldAccountAbbNm.length - 5;
        var blankSpace = '';
        for (var i = 0; i < blankSpaceLength; i++) {
          blankSpace += ' ';
        }
        accountAbbNm = oldAccountAbbNm + blankSpace + '   SO';
      }
    }
    break;
  case 'BPWPQ':
    // defect 1727965 - BP does not have address. Account Abb Name comes from
    // CRIS search via Credit Customer No. Logic added in JPHandler.
    accountAbbNm = FormManager.getActualValue('abbrevNm');
    break;
  case 'STOSB':
  case 'STOSC':
    var chargeCd = FormManager.getActualValue('chargeCd');
    if ((_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp) && (_pagemodel.chargeCd != null && chargeCd == _pagemodel.chargeCd)) {
      return;
    }
    accountAbbNm = chargeCd + ' ' + oldAccountAbbNm;
    break;
  case 'STOSI':
    var chargeCd = FormManager.getActualValue('chargeCd');
    if ((_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp) && (_pagemodel.chargeCd != null && chargeCd == _pagemodel.chargeCd)) {
      return;
    }
    if (chargeCd != null && chargeCd.length >= 5) {
      chargeCd = chargeCd.substring(0, 5);
    }
    accountAbbNm = 'I' + chargeCd + ' ' + oldAccountAbbNm;
    break;
  case 'BCEXA':
  case 'BFKSC':
    accountAbbNm = '';
    break;
  default:
    if (_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp) {
      return;
    }
    accountAbbNm = oldAccountAbbNm;
  }
  if (accountAbbNm && accountAbbNm.length > 22) {
    accountAbbNm = accountAbbNm.substring(0, 22);
  }
  FormManager.setValue('abbrevNm', accountAbbNm);

}
function setAccountAbbNmForUpdate() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  if (FormManager.getActualValue('reqType') != 'U') {
    return;
  }

  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == '') {
    return;
  }

  if (_pagemodel.custSubGrp != null && custSubGrp == _pagemodel.custSubGrp) {
    return;
  }

  var oldAccountAbbNm = getZS01CustNm3();
  if (_pagemodel.abbrevNm == null) {
    oldAccountAbbNm = FormManager.getActualValue('abbrevNm');
  }
  var accountAbbNm = '';
  accountAbbNm = oldAccountAbbNm;

  if (accountAbbNm && accountAbbNm.length > 22) {
    accountAbbNm = accountAbbNm.substring(0, 22);
  }
  FormManager.setValue('abbrevNm', accountAbbNm);
}

function getZS01CustNm3() {
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    _qall : 'Y',
    REQ_ID : reqId,
    ADDR_TYPE : 'ZS01',
  };
  var results = cmr.query('GET.ACCOUNT_CUST_NM3', qParams);
  if (results != null && results.length > 0) {
    var zs01CustNm3 = results[0].ret1;
    return zs01CustNm3;
  } else {
    return null;
  }
}

function resetAccountAbbNmOnFieldChange() {
  dojo.connect(FormManager.getField('salesTeamCd'), 'onChange', function(value) {
    if (value != _pagemodel.salesTeamCd) {
      setAccountAbbNm();
    }
    setTier2Required();
  });
  dojo.connect(FormManager.getField('tier2'), 'onChange', function(value) {
    if (value != _pagemodel.tier2) {
      setAccountAbbNm();
    }
  });
  dojo.connect(FormManager.getField('chargeCd'), 'onChange', function(value) {
    if (value != _pagemodel.chargeCd) {
      setAccountAbbNm();
      setCustNmDetailOnScenario();
    }
  });
}
function setAccountAbbNmRequired() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  switch (custSubGrp) {
  case 'BPWPQ':
    if (role == 'Requester') {
      FormManager.readOnly('abbrevNm');
      FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    } else if (role == 'Processor') {
      console.log('role = ' + role);
      FormManager.enable('abbrevNm');
      FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Account Abbreviated Name' ], 'MAIN_CUST_TAB');
    }
    break;
  case 'ISOCU':
    FormManager.readOnly('abbrevNm');
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    break;
  case 'BCEXA':
  case 'BFKSC':
    FormManager.clearValue('abbrevNm');
    FormManager.readOnly('abbrevNm');
    FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
    break;
  case '':
  default:
    FormManager.enable('abbrevNm');
    FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Account Abbreviated Name' ], 'MAIN_CUST_TAB');
  }
}

/**
 * Hidden H address when create request for FR
 */
function toggleAddrTypesForJP(cntry, addressMode) {

  // 1743139 - hide ZC01 ZE01 for Update in scenario STOSB/STOSC/STOSI
  if (FormManager.getActualValue('reqType') == 'U') {
    if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      switch (custSubGrp) {
      case 'STOSB':
      case 'STOSC':
      case 'STOSI':
        cmr.hideNode('radiocont_ZC01');
        cmr.hideNode('radiocont_ZE01');
        break;
      case '':
      default:
        cmr.showNode('radiocont_ZC01');
        cmr.showNode('radiocont_ZE01');
        break;
      }
    }
  }

  if (FormManager.getActualValue('reqType') == 'C') {
    if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
      // cmr.hideNode('radiocont_ZI02');
      // cmr.hideNode('radiocont_ZI01');
      cmr.hideNode('radiocont_ZI03');
      cmr.hideNode('radiocont_ZP02');
      cmr.hideNode('radiocont_ZP03');
      cmr.hideNode('radiocont_ZP04');
      cmr.hideNode('radiocont_ZP05');
      cmr.hideNode('radiocont_ZP06');
      cmr.hideNode('radiocont_ZP07');
      cmr.hideNode('radiocont_ZP08');
    }
  }
  return;
}
/**
 * Opens Find CMR search
 */
function doCompanySearch() {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var reqId = FormManager.getActualValue('reqId');
  var url = cmr.CONTEXT_ROOT + '/connect?system=cmr&cntry=' + cntry + '&reqId=' + reqId;
  var specs = 'location=no,menubar=no,resizable=no,scrollbars=yes,status=no,toolbar=no,height=' + screen.height + 'px,width=' + screen.width + 'px';
  _findCMRWin = window.open(url, 'win_findCMR', specs, true);
  _findCMROK = false;
  _findMode = 'cmr';
  window.setTimeout('waitForResult()', 1000);
  cmr.showProgress('Waiting for Find CMR Search results..');
}
/**
 * While Find CMR / D&B search is opened, poll the window and wait for a result
 * from Find CMR
 */
function waitForResult() {
  if ((_findCMRWin == null || _findCMRWin.closed) && !_findCMROK) {
    // if the window has been closed manually without returning any result, just
    // hide the progress modal
    cmr.hideProgress();
    return;
  }
  _findCMRWin.postMessage("cmrconnect", '*');
  if (!_findCMROK) {
    window.setTimeout('waitForResult()', 2000);
  }
}

function isValidDate(dateString) {
  if (dateString == '' || dateString == null)
    return true;
  if (dateString.length > 10)
    return false;

  if (!/^(\d{1,4})(-)(\d{1,2})\2(\d{1,2})$/.test(dateString)) {
    return false;
  }

  var year = parseInt(dateString.substr(0, 4), 10);
  var month = parseInt(dateString.substr(5, 2), 10);
  var day = parseInt(dateString.substr(8, 2), 10);

  // Check the ranges of month and year
  if (year == 0 || month == 0 || month > 12)
    return false;
  if (month == 2) {
    if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) {
      if (day == 0 || day > 29)
        return false;
    } else {
      if (day == 0 || day > 28)
        return false;
    }
  }
  if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
    if (day == 0 || day > 31)
      return false;
  }
  if (month == 4 || month == 6 || month == 9 || month == 11) {
    if (day == 0 || day > 30)
      return false;
  }
  return true;
}
function addDateValidatorForReqDueDate() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqDueDate = FormManager.getActualValue('requestDueDateTemp');
        if (!isValidDate(reqDueDate))
          return new ValidationResult(FormManager.getField('requestDueDateTemp'), false,
              'Request Due Date should be in date format, like YYYY-MM-DD.');
        else {
          return new ValidationResult(null, true, null);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

function canRemoveAddress(value, rowIndex, grid) {
  var reqType = FormManager.getActualValue('reqType');
  var rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType[0];
  if (reqType == 'U') {
    if (addrType == 'ZS01' || addrType == 'ZC01' || addrType == 'ZE01') {
      return false;
    }
    return true;
  }
  if (reqType == 'C' && FormManager.getActualValue('userRole') == 'Processor') {
    return true;
  }
  var parentNo = rowData.parCmrNo[0];
  if (parentNo != null && parentNo != '') {
    return false;
  }
  return true;
}

function canUpdateAddress(value, rowIndex, grid) {
  var custType = FormManager.getActualValue('custType');
  var rowData = grid.getItem(rowIndex);
  var addrType = rowData.addrType[0];
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  console.log(addrType + ' - ' + custType);
  if (addrType == 'ZC01' && custType.includes('C')) {
    if (reqType == 'U' && (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI' || custSubGrp == 'INTER')) {
      return false;
    } else {
      return true;
    }
  } else if (addrType == 'ZE01' && custType.includes('E')) {
    if (reqType == 'U' && (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI' || custSubGrp == 'INTER')) {
      return false;
    } else {
      return true;
    }
  } else if (addrType != 'ZC01' && addrType != 'ZE01') {
    return true;
  }
  return false;
}

function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  return canRemoveAddress(value, rowIndex, grid);
}

function addrSeqFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var addrType = rowData.addrType[0];

  if (addrType == 'ZC01' || addrType == 'ZE01') {
    return 'N/A';
  }
  return value;
}

function showOrHideAddrFields(cntry, addressMode, details) {
  var custType = FormManager.getActualValue('custType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var role = FormManager.getActualValue('userRole').toUpperCase();

  if (cmr.addressMode == 'updateAddress') {
    var addrType = FormManager.getActualValue('addrType');
    showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    showOrHideAddrFieldForBPWPQ(custSubGrp, custType, addrType, role);
    showOrHideAddrFieldForLocationCode(custSubGrp, custType, addrType, role);

  } else if (addressMode == 'newAddress' || addressMode == 'copyAddress') {
    var addrType = 'ZS01';
    checkAddrType(addrType);
    showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);

    dojo.connect(FormManager.getField('addrType_ZC01'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZC01').checked == true) {
        addrType = 'ZC01';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
      removeDefaultValueTelNo();
    });
    dojo.connect(FormManager.getField('addrType_ZE01'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZE01').checked == true) {
        addrType = 'ZE01';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
      removeDefaultValueTelNo();
    });
    dojo.connect(FormManager.getField('addrType_ZS02'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZS02').checked == true) {
        addrType = 'ZS02';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZS01'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZS01').checked == true) {
        addrType = 'ZS01';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP01'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP01').checked == true) {
        addrType = 'ZP01';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP09'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP09').checked == true) {
        addrType = 'ZP09';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZI02'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZI02').checked == true) {
        addrType = 'ZI02';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZI01'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZI01').checked == true) {
        addrType = 'ZI01';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZI03'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZI03').checked == true) {
        addrType = 'ZI03';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP02'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP02').checked == true) {
        addrType = 'ZP02';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP03'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP03').checked == true) {
        addrType = 'ZP03';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP04'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP04').checked == true) {
        addrType = 'ZP04';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP05'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP05').checked == true) {
        addrType = 'ZP05';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP06'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP06').checked == true) {
        addrType = 'ZP06';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP07'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP07').checked == true) {
        addrType = 'ZP07';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
    dojo.connect(FormManager.getField('addrType_ZP08'), 'onClick', function(value) {
      if (FormManager.getField('addrType_ZP08').checked == true) {
        addrType = 'ZP08';
      }
      showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role);
    });
  }
}
function checkAddrType(addrType) {
  if (FormManager.getField('addrType_ZC01').checked == true) {
    addrType = 'ZC01';
  } else if (FormManager.getField('addrType_ZE01').checked == true) {
    addrType = 'ZE01';
  } else if (FormManager.getField('addrType_ZS02').checked == true) {
    addrType = 'ZS02';
  } else if (FormManager.getField('addrType_ZS01').checked == true) {
    addrType = 'ZS01';
  } else if (FormManager.getField('addrType_ZP01').checked == true) {
    addrType = 'ZP01';
  } else if (FormManager.getField('addrType_ZP09').checked == true) {
    addrType = 'ZP09';
  } else if (FormManager.getField('addrType_ZI02').checked == true) {
    addrType = 'ZI02';
  } else if (FormManager.getField('addrType_ZI01').checked == true) {
    addrType = 'ZI01';
  } else if (FormManager.getField('addrType_ZI03').checked == true) {
    addrType = 'ZI03';
  } else if (FormManager.getField('addrType_ZP02').checked == true) {
    addrType = 'ZP02';
  } else if (FormManager.getField('addrType_ZP03').checked == true) {
    addrType = 'ZP03';
  } else if (FormManager.getField('addrType_ZP04').checked == true) {
    addrType = 'ZP04';
  } else if (FormManager.getField('addrType_ZP05').checked == true) {
    addrType = 'ZP05';
  } else if (FormManager.getField('addrType_ZP06').checked == true) {
    addrType = 'ZP06';
  } else if (FormManager.getField('addrType_ZP07').checked == true) {
    addrType = 'ZP07';
  } else if (FormManager.getField('addrType_ZP08').checked == true) {
    addrType = 'ZP08';
  }
  return addrType;
}
function showOrHideAddrFieldInDetails(custSubGrp, custType, addrType, role) {
  if (cmr.addressMode == 'updateAddress') {
    addrType = cmr.addrdetails.ret2;
  }
  switch (custSubGrp) {
  case 'NORML':
  case 'OUTSC':
    if (custType == 'CEA') {
      if (addrType == 'ZC01') {
        if (cmr.currentRequestType == 'C' || FormManager.getActualValue('reqType') == 'C') {
          setAddrFieldMandatory('rol', 'ROL', 'ROL Flag');
        }

        // setAddrFieldMandatory('city2', 'City2', 'Company No');
        if (changed) {
          if (cmr.addressMode == 'newAddress') {
            FormManager.setValue('companySize', '0');
          }
        }
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldMandatory('companySize', 'CompanySize', 'Company Size');
        setAddrFieldOptional('bldg', 'Building');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
      } else if (addrType == 'ZE01') {
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        // setAddrFieldMandatory('divn', 'Division', 'Estab No');
        setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
        setAddrFieldOptional('bldg', 'Building');

        setAddrFieldHide('rol', 'ROL');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
      } else {
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');

        setAddrFieldOptional('bldg', 'Building');
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
        setAddrFieldOptional('custFax', 'CustFax');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('rol', 'ROL');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
      }
      setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
      setAddrFieldOptional('custNm2', 'CustomerName2');
      setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
      setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
      setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
    } else if (custType == 'EA') {
      if (addrType == 'ZC01') {
        // setAddrFieldMandatory('city2', 'City2', 'Company No');
        setAddrFieldHide('custNm1', 'CustomerName1');
        setAddrFieldHide('custNm2', 'CustomerName2');
        setAddrFieldHide('custNm3', 'CustomerName3');
        setAddrFieldHide('custNm4', 'CustomerName4');
        setAddrFieldHide('postCd', 'PostalCode');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('locationCode', 'LocationCode');
        setAddrFieldHide('bldg', 'Building');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('rol', 'ROL');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
      } else if (addrType == 'ZE01') {
        // setAddrFieldMandatory('divn', 'Division', 'Estab No');
        setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
        setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
        setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
        setAddrFieldOptional('custNm2', 'CustomerName2');
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
        setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
        setAddrFieldOptional('bldg', 'Building');

        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
        setAddrFieldHide('rol', 'ROL');
      } else {
        setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
        setAddrFieldOptional('custNm2', 'CustomerName2');
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
        setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
        setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
        setAddrFieldOptional('bldg', 'Building');
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
        setAddrFieldOptional('custFax', 'CustFax');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('rol', 'ROL');
      }
    } else if (custType == 'A') {
      if (addrType == 'ZC01') {
        // setAddrFieldMandatory('city2', 'City2', 'Company No');
        setAddrFieldHide('custNm1', 'CustomerName1');
        setAddrFieldHide('custNm2', 'CustomerName2');
        setAddrFieldHide('custNm3', 'CustomerName3');
        setAddrFieldHide('custNm4', 'CustomerName4');
        setAddrFieldHide('postCd', 'PostalCode');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('locationCode', 'LocationCode');
        setAddrFieldHide('bldg', 'Building');
        setAddrFieldHide('companySize', 'CompanySize');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
        setAddrFieldHide('rol', 'ROL');
      } else if (addrType == 'ZE01') {
        // setAddrFieldMandatory('divn', 'Division', 'Estab No');
        setAddrFieldHide('custNm1', 'CustomerName1');
        setAddrFieldHide('custNm2', 'CustomerName2');
        setAddrFieldHide('custNm3', 'CustomerName3');
        setAddrFieldHide('custNm4', 'CustomerName4');
        setAddrFieldHide('postCd', 'PostalCode');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('custPhone', 'CustPhone');
        setAddrFieldHide('locationCode', 'LocationCode');
        setAddrFieldHide('bldg', 'Building');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('bldg', 'Building');

        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
        setAddrFieldHide('rol', 'ROL');
      } else {
        setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
        setAddrFieldOptional('custNm2', 'CustomerName2');
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
        setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
        setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
        setAddrFieldOptional('bldg', 'Building');
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
        setAddrFieldOptional('custFax', 'CustFax');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('rol', 'ROL');
      }
    }
    break;
  case 'EUCMR':
  case 'WHCMR':
    if (custType == 'CEA') {
      if (addrType == 'ZC01') {
        if (cmr.currentRequestType == 'C' || FormManager.getActualValue('reqType') == 'C') {
          setAddrFieldMandatory('rol', 'ROL', 'ROL Flag');
        }

        // setAddrFieldMandatory('city2', 'City2', 'Company No');
        if (changed) {
          if (cmr.addressMode == 'newAddress') {
            FormManager.setValue('companySize', '0');
          }
        }
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldMandatory('companySize', 'CompanySize', 'Company Size');
        setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code')

        setAddrFieldOptional('bldg', 'Building');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
      } else if (addrType == 'ZE01') {
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        // setAddrFieldMandatory('divn', 'Division', 'Estab No');
        setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
        setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code')

        setAddrFieldOptional('bldg', 'Building');

        setAddrFieldHide('rol', 'ROL');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
      } else {
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');

        setAddrFieldOptional('bldg', 'Building');
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
        setAddrFieldOptional('custFax', 'CustFax');
        setAddrFieldOptional('postCd', 'PostalCode', 'Postal Code');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('rol', 'ROL');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
      }
      setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
      setAddrFieldOptional('custNm2', 'CustomerName2');
      setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
      setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
    } else if (custType == 'EA') {
      if (addrType == 'ZC01') {
        // setAddrFieldMandatory('city2', 'City2', 'Company No');
        setAddrFieldHide('custNm1', 'CustomerName1');
        setAddrFieldHide('custNm2', 'CustomerName2');
        setAddrFieldHide('custNm3', 'CustomerName3');
        setAddrFieldHide('custNm4', 'CustomerName4');
        setAddrFieldHide('postCd', 'PostalCode');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('locationCode', 'LocationCode');
        setAddrFieldHide('bldg', 'Building');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('rol', 'ROL');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
      } else if (addrType == 'ZE01') {
        // setAddrFieldMandatory('divn', 'Division', 'Estab No');
        setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
        setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
        setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
        setAddrFieldOptional('custNm2', 'CustomerName2');
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
        setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
        setAddrFieldOptional('bldg', 'Building');

        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
        setAddrFieldHide('rol', 'ROL');
      } else {
        setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
        setAddrFieldOptional('custNm2', 'CustomerName2');
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
        setAddrFieldOptional('postCd', 'PostalCode', 'Postal Code');
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
        setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
        setAddrFieldOptional('bldg', 'Building');
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
        setAddrFieldOptional('custFax', 'CustFax');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('rol', 'ROL');
      }
    } else if (custType == 'A') {
      if (addrType == 'ZC01') {
        // setAddrFieldMandatory('city2', 'City2', 'Company No');
        setAddrFieldHide('custNm1', 'CustomerName1');
        setAddrFieldHide('custNm2', 'CustomerName2');
        setAddrFieldHide('custNm3', 'CustomerName3');
        setAddrFieldHide('custNm4', 'CustomerName4');
        setAddrFieldHide('postCd', 'PostalCode');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('locationCode', 'LocationCode');
        setAddrFieldHide('bldg', 'Building');
        setAddrFieldHide('companySize', 'CompanySize');

        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
        setAddrFieldHide('rol', 'ROL');
      } else if (addrType == 'ZE01') {
        // setAddrFieldMandatory('divn', 'Division', 'Estab No');
        setAddrFieldHide('custNm1', 'CustomerName1');
        setAddrFieldHide('custNm2', 'CustomerName2');
        setAddrFieldHide('custNm3', 'CustomerName3');
        setAddrFieldHide('custNm4', 'CustomerName4');
        setAddrFieldHide('postCd', 'PostalCode');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('custPhone', 'CustPhone');
        setAddrFieldHide('locationCode', 'LocationCode');
        setAddrFieldHide('bldg', 'Building');
        setAddrFieldHide('addrTxt', 'AddressTxt');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('bldg', 'Building');

        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');

        setAddrFieldHide('custFax', 'CustFAX');
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
        setAddrFieldHide('rol', 'ROL');
      } else {
        setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
        setAddrFieldOptional('custNm2', 'CustomerName2');
        setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
        setAddrFieldOptional('custNm4', 'CustomerName4', 'Katakana');
        setAddrFieldOptional('postCd', 'PostalCode', 'Postal Code');
        setAddrFieldOptional('divn', 'Division', 'Estab No');
        setAddrFieldOptional('city2', 'City2', 'Company No');
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
        setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
        setAddrFieldOptional('bldg', 'Building');
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
        setAddrFieldOptional('custFax', 'CustFax');

        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('companySize', 'CompanySize');
        setAddrFieldHide('rol', 'ROL');
      }
    }
    break;
  case 'BPWPQ':
    // For defect 1740581
    if (cmr.currentRequestType == 'U') {
      if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'updateAddress') {

        setAddrFieldOptional('custPhone', 'Tel No');
        setAddrFieldHide('estabFuncCd', 'Estab Function Code');
        setAddrFieldHide('divn', 'Estab No');
        setAddrFieldHide('city2', 'Company No');
        setAddrFieldHide('companySize', 'Company Size');
        setAddrFieldHide('rol', 'ROL Flag');
        setAddrFieldHide('locationCode', 'LocationCode');
      }
    }
    break;
  case 'ISOCU':
    if (cmr.currentRequestType == 'C') {
      setAddrFieldHide('custNm1', 'CustomerName1');
      setAddrFieldHide('custNm2', 'CustomerName2');
      setAddrFieldHide('custNm3', 'CustomerName3');
      setAddrFieldHide('custNm4', 'CustomerName4');
      setAddrFieldHide('postCd', 'PostalCode');
      setAddrFieldHide('locationCode', 'LocationCode');
      setAddrFieldHide('addrTxt', 'AddressTxt');
      setAddrFieldHide('bldg', 'Building');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('custFax', 'CustFax');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      setAddrFieldHide('custFax', 'CustFax');
      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('rol', 'ROL');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
    } else if (cmr.currentRequestType == 'U') {
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
        setAddrFieldOptional('custPhone', 'CustPhone');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
      if (addrType == 'ZC01' || addrType == 'ZE01') {
        setAddrFieldHide('office', 'Office');
        setAddrFieldHide('custFax', 'CustFax');
        setAddrFieldHide('dept', 'Department');
        setAddrFieldHide('contact', 'Contact');
        setAddrFieldHide('custFax', 'CustFax');
        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('rol', 'ROL');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
      } else {
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('custFax', 'CustFax');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
        setAddrFieldOptional('custFax', 'CustFax');
        setAddrFieldHide('divn', 'Division');
        setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
        setAddrFieldHide('rol', 'ROL');
        setAddrFieldHide('city2', 'City2');
        setAddrFieldHide('companySize', 'CompanySize');
      }
      setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
      setAddrFieldOptional('custNm2', 'CustomerName2');
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
      setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
      setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
      setAddrFieldOptional('bldg', 'Building');
    }
    break;
  case 'BCEXA':
  case 'BFKSC':
    if (addrType == 'ZC01') {
      FormManager.setValue('city2', getCompanyNo(addrType) != null ? getCompanyNo(addrType) : '');
      setAddrFieldOptional('city2', 'City2');
      setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
      setAddrFieldOptional('custNm2', 'CustomerName2');
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
      setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
      setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
      setAddrFieldOptional('bldg', 'Building');
      setAddrFieldHide('companySize', 'CompanySize');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('custFax', 'CustFax');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      setAddrFieldHide('custFax', 'CustFax');
      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('locationCode', 'LocationCode');
      setAddrFieldHide('rol', 'ROL');
      if (cmr.currentRequestType == 'C') {
        setAddrFieldHide('bldg', 'Building');
      }
    } else {
      setAddrFieldHide('custNm1', 'CustomerName1');
      setAddrFieldHide('custNm2', 'CustomerName2');
      setAddrFieldHide('custNm3', 'CustomerName3');
      setAddrFieldHide('custNm4', 'CustomerName4');
      setAddrFieldHide('postCd', 'PostalCode');
      setAddrFieldHide('locationCode', 'LocationCode');
      setAddrFieldHide('addrTxt', 'AddressTxt');
      // setAddrFieldHide('bldg', 'Building');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('custFax', 'CustFax');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      setAddrFieldHide('custFax', 'CustFax');
      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('rol', 'ROL');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
    }
    if (role == 'REQUESTER' && (cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U')) {
      setAddrFieldHide('bldg', 'Building');
    }
    if (role == 'PROCESSOR' && (cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U')) {
      setAddrFieldOptional('bldg', 'Building');
    }
    break;
  case 'STOSB':
  case 'STOSC':
  case 'STOSI':
  case 'INTER':
    if (addrType == 'ZC01') {
      if (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI') {
        setAddrFieldOptional('city2', 'City2');
      } else if (custSubGrp == 'INTER') {
        setAddrFieldHide('city2', 'City2');
      }
      setAddrFieldHide('custNm1', 'CustomerName1');
      setAddrFieldHide('custNm2', 'CustomerName2');
      setAddrFieldHide('custNm3', 'CustomerName3');
      setAddrFieldHide('custNm4', 'CustomerName4');
      setAddrFieldHide('postCd', 'PostalCode');
      setAddrFieldHide('addrTxt', 'AddressTxt');
      setAddrFieldHide('locationCode', 'LocationCode');
      setAddrFieldHide('bldg', 'Building');
      setAddrFieldHide('companySize', 'CompanySize');

      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      setAddrFieldHide('rol', 'ROL');
    } else if (addrType == 'ZE01') {
      if (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI') {
        setAddrFieldOptional('divn', 'Division');
      } else if (custSubGrp == 'INTER') {
        setAddrFieldHide('divn', 'Division');
      }
      setAddrFieldHide('custNm1', 'CustomerName1');
      setAddrFieldHide('custNm2', 'CustomerName2');
      setAddrFieldHide('custNm3', 'CustomerName3');
      setAddrFieldHide('custNm4', 'CustomerName4');
      setAddrFieldHide('postCd', 'PostalCode');
      setAddrFieldHide('addrTxt', 'AddressTxt');
      setAddrFieldHide('locationCode', 'LocationCode');
      setAddrFieldHide('bldg', 'Building');
      setAddrFieldHide('addrTxt', 'AddressTxt');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('bldg', 'Building');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      setAddrFieldHide('rol', 'ROL');
    } else {
      if (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI') {
        setAddrFieldOptional('office', 'Office');
        setAddrFieldOptional('dept', 'Department');
        setAddrFieldOptional('contact', 'Contact');
      } else if (custSubGrp == 'INTER') {
        setAddrFieldMandatory('office', 'Office', 'Branch/Office');
        setAddrFieldMandatory('dept', 'Department', 'Department');
        setAddrFieldMandatory('contact', 'Contact', 'Contact');
      }
      setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
      setAddrFieldOptional('custNm2', 'CustomerName2');
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
      setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
      setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
      setAddrFieldOptional('bldg', 'Building');
      setAddrFieldOptional('office', 'Office');
      setAddrFieldOptional('dept', 'Department');
      setAddrFieldOptional('contact', 'Contact');
      setAddrFieldOptional('custFax', 'CustFax');

      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('rol', 'ROL');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
    }
    break;
  case 'ABIJS':
  case 'AHIJE':
  case 'AUITS':
  case 'AWIGS':
  case 'BDRBS':
  case 'BVMDS':
  case 'BGICS':
  case 'BHISO':
  case 'BKRBS':
  case 'BLNIS':
  case 'BMISI':
  case 'BPIJB':
  case 'BRMSI':
    if (addrType == 'ZC01') {
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      // setAddrFieldMandatory('city2', 'City2', 'Company No');
      if (changed) {
        if (cmr.addressMode == 'newAddress') {
          FormManager.setValue('companySize', '0');
        }
      }
      setAddrFieldMandatory('companySize', 'CompanySize', 'Company Size');
      setAddrFieldOptional('bldg', 'Building');

      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
    } else if (addrType == 'ZE01') {
      if (custType == 'A') {
        setAddrFieldHide('locationCode', 'LocationCode');
      } else {
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
      }

      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      // setAddrFieldMandatory('divn', 'Division', 'Estab No');
      setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
      setAddrFieldOptional('bldg', 'Building');

      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
    } else {
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
      setAddrFieldOptional('bldg', 'Building');
      setAddrFieldOptional('office', 'Office');
      setAddrFieldOptional('dept', 'Department');
      setAddrFieldOptional('contact', 'Contact');
      setAddrFieldOptional('custFax', 'CustFax');

      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
    }
    setAddrFieldHide('rol', 'ROL');
    setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
    setAddrFieldOptional('custNm2', 'CustomerName2');
    setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
    setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
    setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
    break;
  case 'BQICL':
    if (addrType == 'ZC01') {
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      // setAddrFieldMandatory('city2', 'City2', 'Company No');
      if (changed) {
        if (cmr.addressMode == 'newAddress') {
          FormManager.setValue('companySize', '0');
        }
      }
      setAddrFieldMandatory('companySize', 'CompanySize', 'Company Size');
      setAddrFieldOptional('bldg', 'Building');

      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      setAddrFieldOptional('locationCode', 'LocationCode');
    } else if (addrType == 'ZE01') {

      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      // setAddrFieldMandatory('divn', 'Division', 'Estab No');
      setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
      setAddrFieldOptional('bldg', 'Building');

      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      setAddrFieldOptional('locationCode', 'LocationCode');
    } else {
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
      setAddrFieldOptional('bldg', 'Building');
      setAddrFieldOptional('office', 'Office');
      setAddrFieldOptional('dept', 'Department');
      setAddrFieldOptional('contact', 'Contact');
      setAddrFieldOptional('custFax', 'CustFax');

      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
    }
    setAddrFieldHide('rol', 'ROL');
    setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
    setAddrFieldOptional('custNm2', 'CustomerName2');
    setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
    setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
    setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
    break;
  case 'BIJSC':
    if (addrType == 'ZC01') {
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
      if (changed) {
        if (cmr.addressMode == 'newAddress') {
          FormManager.setValue('companySize', '0');
        }
      }
      setAddrFieldHide('companySize', 'CompanySize');
      setAddrFieldOptional('bldg', 'Building');

      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
    } else if (addrType == 'ZE01') {
      if (custType == 'A') {
        setAddrFieldHide('locationCode', 'LocationCode');
      } else {
        if (role == 'REQUESTER') {
          setAddrFieldOptional('locationCode', 'LocationCode');
        } else if (role == 'PROCESSOR') {
          setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
        }
      }
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
      setAddrFieldOptional('bldg', 'Building');
      if (cmr.addressMode == 'newAddress') {
        setAddrFieldHide('custNm4', 'CustomerName4');
      } else {
        setAddrFieldOptional('custNm4', 'CustomerName4');
      }
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
    } else {
      setAddrFieldOptional('bldg', 'Building');
      setAddrFieldOptional('office', 'Office');
      setAddrFieldOptional('dept', 'Department');
      setAddrFieldOptional('contact', 'Contact');
      setAddrFieldOptional('custFax', 'CustFax');
      if ('ZE01' != FormManager.getActualValue('addrType') || 'newAddress' != cmr.addressMode) {
        setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
      }
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
    }
    setAddrFieldHide('rol', 'ROL');
    setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
    setAddrFieldOptional('custNm2', 'CustomerName2');
    setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
    setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
    break;
  case '':
  default:
    if (addrType == 'ZC01') {
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      // setAddrFieldMandatory('city2', 'City2', 'Company No');
      if (changed && cmr.currentRequestType == 'C') {
        if (cmr.addressMode == 'newAddress') {
          FormManager.setValue('companySize', '0');
        }
      }
      setAddrFieldMandatory('companySize', 'CompanySize', 'Company Size');
      setAddrFieldOptional('bldg', 'Building');
      if (custType.includes('C') && cmr.currentRequestType == 'C') {
        // setAddrFieldMandatory('rol', 'ROL', 'ROL Flag');
        if (cmr.addressMode == 'newAddress') {
          FormManager.setValue('rol', 'Y');
        }
      }
      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
    } else if (addrType == 'ZE01') {
      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');
      // setAddrFieldMandatory('divn', 'Division', 'Estab No');
      setAddrFieldMandatory('estabFuncCd', 'EstabFuncCd', 'Estab Function Code');
      setAddrFieldOptional('bldg', 'Building');

      setAddrFieldHide('rol', 'ROL');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');

      setAddrFieldHide('custFax', 'CustFAX');
      setAddrFieldHide('office', 'Office');
      setAddrFieldHide('dept', 'Department');
      setAddrFieldHide('contact', 'Contact');
    } else {
      setAddrFieldOptional('bldg', 'Building');
      setAddrFieldOptional('office', 'Office');
      setAddrFieldOptional('dept', 'Department');
      setAddrFieldOptional('contact', 'Contact');
      setAddrFieldOptional('custFax', 'CustFax');

      setAddrFieldMandatory('custNm3', 'CustomerName3', 'Full English Name');

      setAddrFieldHide('divn', 'Division');
      setAddrFieldHide('estabFuncCd', 'EstabFuncCd');
      setAddrFieldHide('city2', 'City2');
      setAddrFieldHide('companySize', 'CompanySize');
    }
    setAddrFieldMandatory('custNm1', 'CustomerName1', 'Customer Name-KANJI');
    setAddrFieldOptional('custNm2', 'CustomerName2');
    setAddrFieldMandatory('custNm4', 'CustomerName4', 'Katakana');
    setAddrFieldMandatory('addrTxt', 'AddressTxt', 'Address');
    setAddrFieldMandatory('postCd', 'PostalCode', 'Postal Code');
    setAddrFieldHide('rol', 'ROL');
    if (role == 'REQUESTER') {
      setAddrFieldOptional('locationCode', 'LocationCode');
    } else if (role == 'PROCESSOR') {
      setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
    }
    break;
  }
}
function showOrHideAddrFieldForLocationCode(custSubGrp, custType, addrType, role) {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U') {
    if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'updateAddress') {
      if (role == 'REQUESTER') {
        setAddrFieldOptional('locationCode', 'LocationCode');
      } else if (role == 'PROCESSOR') {
        setAddrFieldMandatory('locationCode', 'LocationCode', 'Location');
      }
    }
  }
}
function setAddrFieldMandatory(id, fieldId, fieldLblDesc) {
  FormManager.show(fieldId, id);
  FormManager.addValidator(id, Validators.REQUIRED, [ fieldLblDesc ]);
}
function setAddrFieldHide(id, fieldId) {
  // FormManager.clearValue(id);
  // FormManager.hide(fieldId, id);
  // FormManager.resetValidations(id);
  FormManager.readOnly(id);
  FormManager.resetValidations(id);
}
function setAddrFieldOptional(id, fieldId) {
  FormManager.show(fieldId, id);
  FormManager.removeValidator(id, Validators.REQUIRED);
}
function changed() {
  if (scenarioChanged) {
    return true;
  } else {
    return false;
  }
}
function scenarioChanged() {
  if (FormManager.getActualValue('custSubGrp') != _pagemode.custSubGrp) {
    return true;
  } else {
    return false;
  }
}
function getCompanyNo(addrType) {
  var reqId = FormManager.getActualValue('reqId');
  if (reqId != null && addrType != null) {
    var qParams = {
      _qall : 'Y',
      REQ_ID : reqId,
      ADDR_TYPE : addrType,
    };
    var results = cmr.query('GET.COMP_NO_BY_REQID_ADDRTYPE', qParams);
    if (results != null && results.length > 0) {
      var companyNo = results[0].ret1;
      return companyNo;
    } else {
      return null;
    }
  } else {
    return null;
  }
}

function disableFieldsForUpdate() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {

    FormManager.resetValidations('enterCMRNo');

    var custType = FormManager.getActualValue('custType');
    if (custType == 'C' || custType == 'CE') {
      var accountFieldList = [ 'creditToCustNo', 'billToCustNo', 'tier2', 'salesTeamCd', 'custClass', 'custAcctType', 'outsourcingService',
          'abbrevNm', 'zseriesSw', 'email2', 'salesBusOffCd', 'salesTeamCd', 'func', 'oemInd', 'leasingCompanyIndc', 'searchTerm', 'repTeamMemberNo',
          'govType', 'iinInd', 'valueAddRem', 'channelCd', 'siInd', 'inacType', 'inacCd', 'creditCd', 'csDiv', 'rol' ];
      for (var i = 0; i < accountFieldList.length; i++) {
        disableFiled(accountFieldList[i]);
      }
    } else if (custType == 'CEA') {
      disableFiled('outsourcingService');
      disableFiled('rol');
    }
  } else if (reqType == 'C') {
    FormManager.hide('ICMSContribution', 'icmsInd');
  }
}
function disableFiled(fieldId) {
  FormManager.removeValidator(fieldId, Validators.REQUIRED);
  // FormManager.resetValidations(fieldId);
  FormManager.readOnly(fieldId);
}

function convertKANJICont(cntry, addressMode, details) {
  convertKANJIContInDetails();
  dojo.connect(FormManager.getField('custNm2'), 'onChange', function(value) {
    convertKANJIContInDetails();
  });
}
function convertKANJIContInDetails() {
  var KANJICont = FormManager.getActualValue('custNm2');
  FormManager.setValue('custNm2', convert2DBCSIgnoreCase(KANJICont));
  KANJICont = FormManager.getActualValue('custNm2');
  // FormManager.setValue('custNm2', convertHalfToFullKatakana(KANJICont));
}

function convertKATAKANA(cntry, addressMode, details) {
  convertKATAKANAInDetails();
  dojo.connect(FormManager.getField('custNm4'), 'onChange', function(value) {
    convertKATAKANAInDetails();
  });
}
function convertKATAKANAInDetails() {
  var kATAKANA = FormManager.getActualValue('custNm4');
  FormManager.setValue('custNm4', replaceKATAKANAChar(kATAKANA));
  FormManager.setValue('custNm4', convert2DBCS(FormManager.getActualValue('custNm4')));
  // FormManager.setValue('custNm4',
  // convertHalfToFullKatakana(FormManager.getActualValue('custNm4')));
}
function replaceKATAKANAChar(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/ィ/g, 'イ');
    modifiedVal = modifiedVal.replace(/ョ/g, 'ヨ');
    modifiedVal = modifiedVal.replace(/ュ/g, 'ユ');
    modifiedVal = modifiedVal.replace(/ヵ/g, 'カ');
    modifiedVal = modifiedVal.replace(/ャ/g, 'ヤ');
    modifiedVal = modifiedVal.replace(/ッ/g, 'ツ');
    modifiedVal = modifiedVal.replace(/ァ/g, 'ア');
    modifiedVal = replaceCrossbarSymbol(modifiedVal);
  }
  return modifiedVal;
}

function convertBuilding(cntry, addressMode, details) {
  // convertBuildingInDetails();
  dojo.connect(FormManager.getField('bldg'), 'onChange', function(value) {
    convertBuildingInDetails();
  });
}
function convertBuildingInDetails() {
  var building = FormManager.getActualValue('bldg');
  console.log('building = ' + building);
  FormManager.setValue('bldg', replaceBuildingChar(building));

  FormManager.setValue('bldg', convert2DBCS(FormManager.getActualValue('bldg')));
  // FormManager.setValue('bldg',
  // convertHalfToFullKatakana(FormManager.getActualValue('bldg')));
}
function replaceBuildingChar(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = replaceAndSymbol(modifiedVal);
    modifiedVal = replaceCrossbarSymbol(modifiedVal);
    modifiedVal = modifiedVal.replace(/㈱/g, '（株）');
  }
  return modifiedVal;
}

function convertBranch(cntry, addressMode, details) {
  var addrType = FormManager.getActualValue('addrType');
  if (addrType == 'ZC01' || addrType == 'ZE01') {
    return;
  } else {
    convertBranchInDetails();
  }
  dojo.connect(FormManager.getField('office'), 'onChange', function(value) {
    var _addrType = FormManager.getActualValue('addrType');
    if (_addrType == 'ZC01' || _addrType == 'ZE01') {
      return;
    } else {
      convertBranchInDetails();
    }
  });
}
function convertBranchInDetails() {
  var branch = FormManager.getActualValue('office');
  FormManager.setValue('office', replaceBranchChar(branch));
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'NORML' || custSubGrp == 'EUCMR' || custSubGrp == 'WHCMR' || custSubGrp == 'BIJSC' || custSubGrp == 'BQICL'
      || custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI') {
    FormManager.setValue('office', convert2DBCSIgnoreCase(FormManager.getActualValue('office')));
    // FormManager.setValue('office',
    // convertHalfToFullKatakana(FormManager.getActualValue('office')));
  } else {
    FormManager.setValue('office', convert2DBCS(FormManager.getActualValue('office')));
    // FormManager.setValue('office',
    // convertHalfToFullKatakana(FormManager.getActualValue('office')));
  }
}
function replaceBranchChar(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = replaceAndSymbol(modifiedVal);
    modifiedVal = replaceCrossbarSymbol(modifiedVal);
    modifiedVal = modifiedVal.replace(/㈱/g, '（株）');
  }
  return modifiedVal;
}

function convertDept(cntry, addressMode, details) {
  var addrType = FormManager.getActualValue('addrType');
  if (addrType == 'ZC01' || addrType == 'ZE01') {
    return;
  } else {
    convertDeptInDetails();
  }
  dojo.connect(FormManager.getField('dept'), 'onChange', function(value) {
    var _addrType = FormManager.getActualValue('addrType');
    if (_addrType == 'ZC01' || _addrType == 'ZE01') {
      return;
    } else {
      convertDeptInDetails();
    }
  });
}
function convertDeptInDetails() {
  var dept = FormManager.getActualValue('dept');
  // FormManager.setValue('dept', replaceDeptChar(dept));
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'NORML' || custSubGrp == 'EUCMR' || custSubGrp == 'WHCMR' || custSubGrp == 'BIJSC' || custSubGrp == 'BQICL'
      || custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI') {
    FormManager.setValue('dept', replaceDeptChar(dept));
    FormManager.setValue('dept', convert2DBCSIgnoreCase(FormManager.getActualValue('dept')));
    // FormManager.setValue('dept',
    // convertHalfToFullKatakana(FormManager.getActualValue('dept')));
  } else {
    FormManager.setValue('dept', replaceDeptChar(dept));
    FormManager.setValue('dept', convert2DBCS(FormManager.getActualValue('dept')));
    // FormManager.setValue('dept',
    // convertHalfToFullKatakana(FormManager.getActualValue('dept')));
  }
}
function replaceDeptChar(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = replaceAndSymbol(modifiedVal);
    modifiedVal = replaceCrossbarSymbol(modifiedVal);
    modifiedVal = modifiedVal.replace(/㈱/g, '（株）');
  }
  return modifiedVal;
}

function convertContact(cntry, addressMode, details) {
  convertContactInDetails();
  dojo.connect(FormManager.getField('contact'), 'onChange', function(value) {
    convertContactInDetails();
  });
}
function convertContactInDetails() {
  var contact = FormManager.getActualValue('contact');
  contact = replaceAndSymbol(contact);
  contact = replaceCrossbarSymbol(contact);
  FormManager.setValue('contact', contact);
  FormManager.setValue('contact', convert2DBCS(FormManager.getActualValue('contact')));
  // FormManager.setValue('contact',
  // convertHalfToFullKatakana(FormManager.getActualValue('contact')));
}

function abbNmUpperCase(cntry, addressMode, details) {
  abbNmUpperCaseInDetails();
  dojo.connect(FormManager.getField('custNm3'), 'onChange', function(value) {
    abbNmUpperCaseInDetails();
  });
}
function abbNmUpperCaseInDetails() {
  var abbNm = FormManager.getActualValue('custNm3');
  FormManager.setValue('custNm3', abbNm.toUpperCase());
}
function accountAbbNmUpperCase() {
  accountAbbNmUpperCaseInDetails();
  dojo.connect(FormManager.getField('abbrevNm'), 'onChange', function(value) {
    accountAbbNmUpperCaseInDetails();
  });
}
function accountAbbNmUpperCaseInDetails() {
  var accountAbbNmabbNm = FormManager.getActualValue('abbrevNm');
  FormManager.setValue('abbrevNm', accountAbbNmabbNm.toUpperCase());
}

function addPostlCdLocnCdLogic(cntry, addressMode, details) {
  if (addressMode == 'newAddress' || addressMode == 'updateAddress' || addressMode == 'copyAddress') {
    dojo.connect(FormManager.getField('postCd'), 'onChange', function(value) {
      convertPostalCode();
      autoSetLocnCdByPostCd();
    });
  }
}
function convertPostalCode() {
  var postCd = FormManager.getActualValue('postCd');
  var modifiedVal = '';
  if (postCd != null && postCd.length > 0) {
    modifiedVal = postCd;
    modifiedVal = modifiedVal.replace(/[^\d]/g, '');
    modifiedVal = modifiedVal.replace(/1/g, '１');
    modifiedVal = modifiedVal.replace(/2/g, '２');
    modifiedVal = modifiedVal.replace(/3/g, '３');
    modifiedVal = modifiedVal.replace(/4/g, '４');
    modifiedVal = modifiedVal.replace(/5/g, '５');
    modifiedVal = modifiedVal.replace(/6/g, '６');
    modifiedVal = modifiedVal.replace(/7/g, '７');
    modifiedVal = modifiedVal.replace(/8/g, '８');
    modifiedVal = modifiedVal.replace(/9/g, '９');
    modifiedVal = modifiedVal.replace(/0/g, '０');
  }
  if (modifiedVal == '' || modifiedVal.length == 0) {
    return;
  }
  if (modifiedVal != null && modifiedVal.length >= 6) {
    modifiedVal = modifiedVal.substring(0, 3) + '―' + modifiedVal.substring(modifiedVal.length - 4);
  }
  FormManager.setValue('postCd', modifiedVal);
}
function autoSetLocnCdByPostCd() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'updateAddress' || cmr.addressMode == 'copyAddress') {
    var reqId = FormManager.getActualValue('reqId');
    var addrType = FormManager.getActualValue('addrType');
    var addrSeq = FormManager.getActualValue('addrSeq');
    var locnCdInAddr = getLocnCdInAddr(reqId, addrType, addrSeq);

    var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
    var postCd = FormManager.getActualValue('postCd');
    var locnCdInPostCdMap = getLocnCdInPostCdMap(cmrIssuingCntry, postCd);
    if (locnCdInPostCdMap != null && locnCdInPostCdMap.length > 0) {
      FormManager.setValue('locationCode', locnCdInPostCdMap);
    } else {
      FormManager.setValue('locationCode', locnCdInAddr);
    }
  } else {
    return;
  }
}
function getLocnCdInAddr(reqId, addrType, addrSeq) {
  if (reqId != null && addrType != null && addrSeq != null) {
    var qParams = {
      _qall : 'Y',
      REQ_ID : reqId,
      ADDR_TYPE : addrType,
      ADDR_SEQ : addrSeq,
    };
    var results = cmr.query('GET.LOCNCD_ADDR', qParams);
    if (results != null && results.length > 0) {
      var locnCd = results[0].ret1;
      return locnCd;
    } else {
      return null;
    }
  } else {
    return null;
  }
}
function getLocnCdInPostCdMap(cmrIssuingCntry, postCd) {
  if (cmrIssuingCntry != null && postCd != null) {
    postCd = dbcs2ascii(postCd);
    var qParams = {
      _qall : 'Y',
      CMR_ISSUING_CNTRY : cmrIssuingCntry,
      POST_CD : postCd,
    };
    var results = cmr.query('GET.LOCNCD_BY_POSTLCD', qParams);
    if (results != null && results.length > 0) {
      var locnCd = results[0].ret1;
      return locnCd;
    } else {
      return null;
    }
  } else {
    return null;
  }
}
function dbcs2ascii(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/１/g, '1');
    modifiedVal = modifiedVal.replace(/２/g, '2');
    modifiedVal = modifiedVal.replace(/３/g, '3');
    modifiedVal = modifiedVal.replace(/４/g, '4');
    modifiedVal = modifiedVal.replace(/５/g, '5');
    modifiedVal = modifiedVal.replace(/６/g, '6');
    modifiedVal = modifiedVal.replace(/７/g, '7');
    modifiedVal = modifiedVal.replace(/８/g, '8');
    modifiedVal = modifiedVal.replace(/９/g, '9');
    modifiedVal = modifiedVal.replace(/０/g, '0');
    modifiedVal = modifiedVal.replace(/-/g, '');
    modifiedVal = modifiedVal.replace(/−/g, '');
    modifiedVal = modifiedVal.replace(/―/g, '');
    modifiedVal = modifiedVal.replace(/－/g, '');
  }
  return modifiedVal;
}

function showOfcdMessage() {
  if (FormManager.getField('icmsInd').checked == true) {
    FormManager.getField('ofcdMessage').style.display = "inline-block";
  } else {
    FormManager.getField('ofcdMessage').style.display = "none";
  }
  dojo.connect(FormManager.getField('icmsInd'), 'onClick', function(value) {
    if (FormManager.getField('icmsInd').checked == true) {
      FormManager.getField('ofcdMessage').style.display = "inline-block";
    } else {
      FormManager.getField('ofcdMessage').style.display = "none";
    }
  });
}
function showOrHideDirectBpZSeriesSw() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // if (custTypeinDB != null && custSubGrp == custTypeinDB) {
  // return;
  // }
  var outsourcingServiceInDb = null;
  if (typeof (_pagemodel) != 'undefined') {
    outsourcingServiceInDb = _pagemodel.outsourcingService;
  }
  if (outsourcingServiceInDb) {
    FormManager.show('DirectBp', 'creditBp');
    // FormManager.addValidator('creditBp', Validators.REQUIRED, [ 'Direct/BP'
    // ], 'MAIN_CUST_TAB');
    // FormManager.enable('creditBp');

    FormManager.hide('zSeriesSw', 'zseriesSw');
    FormManager.removeValidator('zseriesSw', Validators.REQUIRED);
    // FormManager.disable('zseriesSw');
    FormManager.enable('outsourcingService');
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', true);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = true;
    }
  } else {
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.removeValidator('creditBp', Validators.REQUIRED);
    // FormManager.disable('creditBp');

    FormManager.show('zSeriesSw', 'zseriesSw');
    // FormManager.addValidator('zseriesSw', Validators.REQUIRED, [ 'zSeries SW'
    // ], 'MAIN_CUST_TAB');
    // FormManager.enable('zseriesSw');
  }

  dojo.connect(FormManager.getField('outsourcingService'), 'onClick', function(value) {
    if (FormManager.getField('outsourcingService').checked == true) {
      FormManager.show('DirectBp', 'creditBp');
      FormManager.addValidator('creditBp', Validators.REQUIRED, [ 'Direct/BP' ], 'MAIN_CUST_TAB');
      FormManager.enable('creditBp');

      FormManager.hide('zSeriesSw', 'zseriesSw');
      FormManager.removeValidator('zseriesSw', Validators.REQUIRED);
      FormManager.disable('zseriesSw');
    } else if (FormManager.getField('outsourcingService').checked == false) {
      FormManager.hide('DirectBp', 'creditBp');
      FormManager.removeValidator('creditBp', Validators.REQUIRED);
      FormManager.disable('creditBp');

      FormManager.show('zSeriesSw', 'zseriesSw');
      var custSubGrp = FormManager.getActualValue('custSubGrp');
      if (custSubGrp != 'EUCMR' && custSubGrp != 'WHCMR') {
        FormManager.addValidator('zseriesSw', Validators.REQUIRED, [ 'zSeries SW' ], 'MAIN_CUST_TAB');
      }
      FormManager.enable('zseriesSw');
    }
  });
}
function setPageLoadDone() {
  isPageLoad = false;
  if (true == FormManager.getField('outsourcingService').checked) {
    FormManager.removeValidator('zseriesSw', Validators.REQUIRED);
    FormManager.enable('outsourcingService');
  }
}
function addChargeCodeLogic() {
  dojo.connect(FormManager.getField('orgNo'), 'onChange', function(value) {
    setDefaultValueForChargeCode();
  });
  dojo.connect(FormManager.getField('soProjectCd'), 'onChange', function(value) {
    setDefaultValueForChargeCode();
    setCustNmDetailOnScenario();
  });
}
function setDefaultValueForChargeCode() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if ('STOSB' == custSubGrp || 'STOSC' == custSubGrp || 'STOSI' == custSubGrp) {
    var referenceValue;
    if (null != FormManager.getActualValue('orgNo') && '' != FormManager.getActualValue('orgNo').trim()) {
      referenceValue = FormManager.getActualValue('orgNo');
    } else {
      referenceValue = FormManager.getActualValue('soProjectCd');
    }
    FormManager.setValue('chargeCd', referenceValue);
  } else {
    return;
  }
}
function addSpaceForCustNm1(cntry, addressMode) {
  dojo.connect(FormManager.getField('custNm1'), 'onChange', function(value) {
    var custNm1KanJi = FormManager.getActualValue('custNm1');
    custNm1KanJi = convert2DBCSIgnoreCase(custNm1KanJi);
    // custNm1KanJi = convertHalfToFullKatakana(custNm1KanJi);
    FormManager.setValue('custNm1', custNm1KanJi);
    // When address type is COMPANY -> ZC01 or ESTAB -> ZE01 - ignore.
    if ('ZE01' == FormManager.getActualValue('addrType') || 'ZC01' == FormManager.getActualValue('addrType')) {
      return;
    }
    // Defect 1676927 2 spaces between specific company type and legal name
    var legalEntityPrefixArray = null;
    if (custNm1KanJi.indexOf('医療法人社団') > -1 || custNm1KanJi.indexOf('医療法人財団') > -1) {
      legalEntityPrefixArray = [ '医療法人社団', '医療法人財団' ];
    } else if (custNm1KanJi.indexOf('医療法人') > -1) {
      legalEntityPrefixArray = [ '医療法人' ];
    } else {
      legalEntityPrefixArray = [ '株式会社', '有限会社', '合同会社', '合名会社', '合資会社', '社会医療法人', '財団法人', '一般社団法人', '公益財団法人', '社団法人', '一般社団法人', '公益社団法人', '宗教法人',
          '学校法人', '社会福祉法人', '更生保護法人', '相互会社', '特定非営利活動法人', '独立行政法人', '地方独立行政法人', '弁護士法人', '有限責任中間法人', '無限責任中間法人', '行政書士法人', '司法書士法人', '税理士法人',
          '国立大学法人', '公立大学法人', '農事組合法人', '管理組合法人', '社会保険労務士法人' ];
    }
    legalEntityPrefixArray.forEach(function(value, index, array) {
      var regExString = value + '[^　]';
      var custNm1Re = new RegExp(regExString);
      if (custNm1Re.test(custNm1KanJi)) {
        // Add a space behind 法人格
        var newstr = custNm1KanJi.replace(value, value + '　');
        FormManager.setValue('custNm1', newstr);
      }
    });
  });
}
function addSpaceForCustNmDetail(cntry, addressMode) {
  dojo.connect(FormManager.getField('email2'), 'onChange', function(value) {
    var custNmDetail = FormManager.getActualValue('email2');
    if (null == custNmDetail || '' == custNmDetail) {
      return;
    }
    custNmDetail = convert2DBCSIgnoreCase(custNmDetail);
    FormManager.setValue('email2', custNmDetail);
    // Defect 1676927 2 spaces between specific company type and legal name
    var legalEntityPrefixArray = null;
    if (custNmDetail.indexOf('医療法人社団') > -1 || custNmDetail.indexOf('医療法人財団') > -1) {
      legalEntityPrefixArray = [ '医療法人社団', '医療法人財団' ];
    } else if (custNmDetail.indexOf('医療法人') > -1) {
      legalEntityPrefixArray = [ '医療法人' ];
    } else {
      legalEntityPrefixArray = [ '株式会社', '有限会社', '合同会社', '合名会社', '合資会社', '社会医療法人', '財団法人', '一般社団法人', '公益財団法人', '社団法人', '一般社団法人', '公益社団法人', '宗教法人',
          '学校法人', '社会福祉法人', '更生保護法人', '相互会社', '特定非営利活動法人', '独立行政法人', '地方独立行政法人', '弁護士法人', '有限責任中間法人', '無限責任中間法人', '行政書士法人', '司法書士法人', '税理士法人',
          '国立大学法人', '公立大学法人', '農事組合法人', '管理組合法人', '社会保険労務士法人' ];
    }
    legalEntityPrefixArray.forEach(function(value, index, array) {
      var regExString = value + '[^　]';
      var custNmDetailRe = new RegExp(regExString);
      if (custNmDetailRe.test(custNmDetail)) {
        // Add a space behind 法人格
        var newstr = custNmDetail.replace(value, value + '　');
        FormManager.setValue('email2', newstr);
      }
    });
  });
}
function setFieldValueOnAddrSave(cntry, addressMode, saving, finalSave, force) {
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  var addrType = FormManager.getActualValue('addrType');
  if (finalSave || force || addrType == 'ZS01') {
    var copyTypes = document.getElementsByName('copyTypes');
    var copyingToA = false;
    if (copyTypes != null && copyTypes.length > 0) {
      copyTypes.forEach(function(input, i) {
        if (input.value == 'ZS01' && input.checked) {
          copyingToA = true;
        }
      });
    }
    if (addrType == 'ZS01' || copyingToA) {
      setCSBOOnAddrSave();
      if (role != 'Processor') {
        setCustNmDetailOnAddrSave();
        setAccountAbbNmOnAddrSave();
      }
    }
  }
}
function setCSBOOnAddrSave() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var addrType = FormManager.getActualValue('addrType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  if (!(cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U')) {
    switch (custSubGrp) {
    case 'NORML':
    case 'EUCMR':
    case 'WHCMR':
    case 'OUTSC':
    case 'STOSB':
    case 'STOSC':
    case 'STOSI':
    case 'INTER':
      var postCd = FormManager.getActualValue('postCd');
      addPostCdCSBOLogic(postCd);
      break;
    case 'BPWPQ':
    case 'ISOCU':
    case 'BCEXA':
    case 'BFKSC':
      FormManager.setValue('csBo', '');
      break;
    case 'ABIJS':
    case 'AHIJE':
    case 'AUITS':
    case 'AWIGS':
    case 'BDRBS':
    case 'BVMDS':
    case 'BGICS':
    case 'BHISO':
    case 'BIJSC':
    case 'BKRBS':
    case 'BLNIS':
    case 'BMISI':
    case 'BPIJB':
    case 'BQICL':
    case 'BRMSI':
      setCSBOSubsidiaryValue();
      break;
    case '':
    default:
      break;
    }
  } else if (cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U') {
    switch (custSubGrp) {
    case 'NORML':
    case 'EUCMR':
    case 'WHCMR':
    case 'OUTSC':
    case 'BPWPQ':
    case 'ISOCU':
    case 'STOSB':
    case 'STOSC':
    case 'STOSI':
    case 'INTER':
      var postCd = FormManager.getActualValue('postCd');
      addPostCdCSBOLogic(postCd);
      break;
    case 'BCEXA':
    case 'BFKSC':
      FormManager.setValue('csBo', '');
      FormManager.readOnly('csBo');
      break;
    case 'ABIJS':
    case 'AHIJE':
    case 'AUITS':
    case 'AWIGS':
    case 'BDRBS':
    case 'BVMDS':
    case 'BGICS':
    case 'BHISO':
    case 'BIJSC':
    case 'BKRBS':
    case 'BLNIS':
    case 'BMISI':
    case 'BPIJB':
    case 'BQICL':
    case 'BRMSI':
      FormManager.setValue('csBo', '0000');
      if (role == 'REQUESTER') {
        FormManager.readOnly('csBo');
        FormManager.removeValidator('csBo', Validators.REQUIRED);
      } else if (role == 'PROCESSOR') {
        FormManager.removeValidator('csBo', Validators.REQUIRED);
      }
      break;
    case '':
    default:
      break;
    }
  }
}
function addPostCdCSBOLogic(postCd) {
  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  postCd = dbcs2ascii(postCd);
  var qParams = {
    _qall : 'Y',
    CMR_ISSUING_CNTRY : cmrIssuingCntry,
    POST_CD : postCd,
  };
  var results = cmr.query('GET.CSBO_BY_POSTLCD', qParams);
  if (results != null && results.length > 0) {
    var csbo = results[0].ret1;
    FormManager.setValue('csBo', csbo);
  }
}
function setCSBOSubsidiaryValue() {
  FormManager.setValue('csBo', '0000');
  FormManager.readOnly('csBo');
}

function setCustNmDetailOnAddrSave() {
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var chargeCode = 'アウトソーシング・サービス';
  var chargeCdValue = FormManager.getActualValue('chargeCd');
  chargeCdValue = convert2DBCS(chargeCdValue);
  var projectCode = FormManager.getActualValue('soProjectCd');
  projectCode = convert2DBCS(projectCode);
  var custNmDetail = '';
  var custNm1 = FormManager.getActualValue('custNm1');
  var custNm2 = FormManager.getActualValue('custNm2');
  var accountCustNMKANJI = custNm1.trim() + custNm2.trim();

  if (custSubGrp == 'STOSB' || custSubGrp == 'STOSC') {
    if (projectCode != null && projectCode.length >= 5) {
      custNmDetail = chargeCode + '（' + projectCode.substring(0, 5) + '）';
    } else {
      custNmDetail = chargeCode + '（' + projectCode + '）';
    }
  } else if (custSubGrp == 'STOSI') {
    if (projectCode != null && projectCode.length >= 5) {
      custNmDetail = accountCustNMKANJI + '（' + projectCode.substring(0, 5) + '）';
    } else {
      custNmDetail = accountCustNMKANJI + '（' + projectCode + '）';
    }
  } else if (custSubGrp == 'INTER' || custSubGrp == 'BIJSC') {
    custNmDetail = accountCustNMKANJI;
  } else if (custSubGrp == '') {
    if (FormManager.getActualValue('email2') != '') {
      return;
    }
  } else {
    custNmDetail = accountCustNMKANJI;
  }

  if (custNmDetail.length > 30) {
    custNmDetail = custNmDetail.substring(0, 30);
  }
  FormManager.setValue('email2', custNmDetail);
  FormManager.addValidator('email2', Validators.NO_SINGLE_BYTE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('email2', Validators.NO_HALF_ANGLE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
}
function setAccountAbbNmOnAddrSave() {
  if (cmr.currentRequestType == 'C') {
    setAccountAbbNmOnAddrSaveCreate();
  } else if (cmr.currentRequestType == 'U') {
    setAccountAbbNmOnAddrSaveUpdate();
  }
}
function setAccountAbbNmOnAddrSaveCreate() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var fullEngNm = FormManager.getActualValue('custNm3');
  var accountAbbNm = '';
  switch (custSubGrp) {
  case 'OUTSC':
    if (fullEngNm != null && fullEngNm != '' && fullEngNm != undefined) {
      if (fullEngNm.length > 17) {
        accountAbbNm = fullEngNm.substring(0, 17) + '   SO';
      } else {
        var blankSpaceLength = 22 - fullEngNm.length - 5;
        var blankSpace = '';
        for (var i = 0; i < blankSpaceLength; i++) {
          blankSpace += ' ';
        }
        accountAbbNm = fullEngNm + blankSpace + '   SO';
      }
    }
    break;
  case 'BPWPQ':
    // defect 1727965 - BP does not have address. Account Abb Name comes from
    // CRIS search via Credit Customer No. Logic added in JPHandler.
    accountAbbNm = FormManager.getActualValue('abbrevNm');
    break;
  case 'STOSB':
  case 'STOSC':
    var chargeCd = FormManager.getActualValue('chargeCd');
    accountAbbNm = chargeCd + ' ' + fullEngNm;
    break;
  case 'STOSI':
    var chargeCd = FormManager.getActualValue('chargeCd');
    if (chargeCd != null && chargeCd.length >= 5) {
      chargeCd = chargeCd.substring(0, 5);
    }
    accountAbbNm = 'I' + chargeCd + ' ' + fullEngNm;
    break;
  case 'BCEXA':
  case 'BFKSC':
    accountAbbNm = '';
    break;
  case '':
  default:
    accountAbbNm = fullEngNm;
  }
  if (accountAbbNm && accountAbbNm.length > 22) {
    accountAbbNm = accountAbbNm.substring(0, 22);
  }
  FormManager.setValue('abbrevNm', accountAbbNm);
}
function setAccountAbbNmOnAddrSaveUpdate() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var fullEngNm = FormManager.getActualValue('custNm3');
  var accountAbbNm = '';
  switch (custSubGrp) {
  case 'BCEXA':
  case 'BFKSC':
    accountAbbNm = '';
    break;
  case '':
  default:
    accountAbbNm = fullEngNm;
  }
  if (accountAbbNm && accountAbbNm.length > 22) {
    accountAbbNm = accountAbbNm.substring(0, 22);
  }
  FormManager.setValue('abbrevNm', accountAbbNm);
}

function addLogicOnOfficeCdChange() {
  dojo.connect(FormManager.getField('salesBusOffCd'), 'onChange', function(value) {
    setINACCodeMandatory();
    // addJSICLogic();
    setClusterOnOfcdChange();
  });
}
function setINACCodeMandatory() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp != 'NORML' && custSubGrp != 'EUCMR' && custSubGrp != 'WHCMR' && custSubGrp != 'OUTSC' && custSubGrp != 'BQICL') {
    return;
  }

  var inacCd = [];
  var isRequired = false;

  var qParams = {
    _qall : 'Y',
    OFFICE_CD : FormManager.getActualValue('salesBusOffCd'),
  };
  var results = cmr.query('GET.INACCD_SECTOR_CLUSTER_BY_OFCD', qParams);
  if (results == undefined) {
    FormManager.resetValidations('inacCd');
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
  } else if (results != null && results.length > 0) {
    for (var i = 0; i < results.length; i++) {
      if (results[i].ret2 != ' ' && results[i].ret3 == ' ') {
        isRequired = true;
        inacCd.push(results[i].ret2);
      }
    }
    if (isRequired) {
      FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
      FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCd);
    } else {
      FormManager.resetValidations('inacCd');
      FormManager.resetDropdownValues(FormManager.getField('inacCd'));
    }
  } else {
    FormManager.resetValidations('inacCd');
    FormManager.resetDropdownValues(FormManager.getField('inacCd'));
  }
  
  var setInacRequiredForOfficeCd = "FT,GK, GN,GV, HD,JS,KL, KQ, LC, LG, LJ, PL, PR, QE";
  var salesBusOffCd = FormManager.getActualValue('salesBusOffCd');
  if(setInacRequiredForOfficeCd.includes(salesBusOffCd)){
	FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');	
  }
}
function addJSICLogic() {
  var officeCd = FormManager.getActualValue('salesBusOffCd');
  var sector = '';
  var jsic = '';
  var isSpecialOfficeCd = false;

  if (officeCd == '' || officeCd.length < 1) {
    return;
  }

  if (checkSpecialOfficeCdList(officeCd)) {
    isSpecialOfficeCd = true;
    FormManager.resetDropdownValues(FormManager.getField('jsicCd'));
    return;
  }

  if (!isSpecialOfficeCd) {
    sector = getSector(officeCd);
    if (sector != undefined && sector.length > 0) {
      jsic = getJSIC(sector);
      FormManager.limitDropdownValues(FormManager.getField('jsicCd'), jsic);
    } else {
      FormManager.resetDropdownValues(FormManager.getField('jsicCd'));
    }
  }
}
function checkSpecialOfficeCdList(officeCd) {
  var specialOfficeCdList = [ 'EN', 'OC', 'OM', 'OP', 'OR', 'O0', 'QS', 'QT', 'QY', 'QZ', 'Q9', 'VD', 'VE', 'VP', 'VR', 'VS', 'VT', 'VU', 'VW', 'VX',
      'VY', 'VZ', 'WE', 'WS', 'WT', 'WU', 'WX', 'WZ', 'W3', 'W6', 'XH', 'XY', 'XZ', 'X3', 'X4', 'X6', '0V', '2B', '2D', '2K', '2L', '2M', '2N', '2P',
      '2Q', '3A', '3M', '3N', '3P', '3R', '3X', '3Y', '4N', '4W', '6A', '72' ];
  for (var i = 0; i < specialOfficeCdList.length; i++) {
    if (officeCd == specialOfficeCdList[i]) {
      return true;
    }
  }
  return false;
}
function getSector(officeCd) {
  var sector = [];
  if (officeCd == '' || officeCd.length < 1) {
    return;
  }
  var qParams = {
    _qall : 'Y',
    OFFICE_CD : officeCd,
  };
  var results = cmr.query('GET.INACCD_SECTOR_CLUSTER_BY_OFCD', qParams);
  if (results != null && results.length > 0) {
    for (var i = 0; i < results.length; i++) {
      if (results[i].ret2 == ' ' && results[i].ret3 != ' ') {
        sector.push(results[i].ret3);
      }
    }
    return sector[0];
  }
}
function getJSIC(sector) {
  var jsicValueList = [];
  if (sector == '' || sector.length < 1) {
    return;
  }
  var qParams = {
    _qall : 'Y',
    SECTOR_CD : sector,
  };
  var results = cmr.query('GET.JSIC_BY_SECTOR', qParams);
  if (results != null && results.length > 0) {
    for (var i = 0; i < results.length; i++) {
      jsicValueList.push(results[i].ret1);
    }
    return jsicValueList;
  }
}
function addJsicIsicLogic() {
  setISICOnJSIC();
  dojo.connect(FormManager.getField('jsicCd'), 'onChange', function(value) {
    setISICOnJSIC();
  });
}
function setISICOnJSIC() {
  var jsic = FormManager.getActualValue('jsicCd');
  var isic = '';
  var qParams = {
    _qall : 'Y',
    JSIC_CD : jsic,
  };
  var results = cmr.query('GET.ISIC_BY_JSIC', qParams);
  if (results != null && results.length > 0) {
    isic = results[0].ret1;
    FormManager.setValue('isicCd', isic);
  }
}

function setClusterOnOfcdChange() {
  var custGrp = FormManager.getActualValue('custGrp');
  if (custGrp == 'IBMTP') {
    addClusterOfcdLogic();
  } else if (custGrp == 'SUBSI') {
    FormManager.setValue('searchTerm', '91454');
  }
}
function addClusterOfcdLogic() {
  var cluster = '';
  var qParams = {
    _qall : 'Y',
    OFFICE_CD : FormManager.getActualValue('salesBusOffCd'),
  };
  var results = cmr.query('GET.INACCD_SECTOR_CLUSTER_BY_OFCD', qParams);
  if (results != null && results.length > 0) {
    for (var i = 0; i < results.length; i++) {
      //if (results[i].ret2 == ' ' && results[i].ret4 != ' ') {
	  if ( results[i].ret4 != ' ') {
        cluster = results[i].ret4;
      }
    }
    FormManager.setValue('searchTerm', cluster);
  }
}

function showConfirmForTier2() {
  var _tier2Handler = dojo.connect(FormManager.getField('tier2'), 'onChange', function(value) {
    if (value == _pagemodel.tier2) {
      return;
    }
    var tier2 = FormManager.getActualValue('tier2');
    if (tier2.substring(0, 1) == 'R' || tier2.substring(0, 1) == 'H' || tier2.substring(0, 1) == 'T') {
      var message = 'Please provide the evidence of the validity of this Tier 2 No.';
      cmr.showConfirm(null, message, 'Warning', null, null);
    }
  });
  if (_tier2Handler && _tier2Handler[0]) {
    _tier2Handler[0].onChange();
  }
}
function updateINACType() {
  var _inacCdHandler = dojo.connect(FormManager.getField('inacCd'), 'onChange', function(value) {
    if (FormManager.getActualValue('viewOnlyPage') == 'true') {
      return;
    }
    var inacCd = FormManager.getActualValue('inacCd');
    if (inacCd && isDigital(inacCd)) {
      FormManager.setValue('inacType', 'I');
    } else if (inacCd && hasAlphabet(inacCd)) {
      FormManager.setValue('inacType', 'N');
    } else if (inacCd == '') {
      // For defect 1712123
      FormManager.setValue('inacType', '');
      return;
    } else {
      FormManager.setValue('inacType', '');
    }
    FormManager.readOnly('inacType');
  });
  if (_inacCdHandler && _inacCdHandler[0]) {
    _inacCdHandler[0].onChange();
  }
}
function isDigital(input) {
  if (input && input.length > 0 && !input.match("^[0-9]+$")) {
    return false;
  } else {
    return true;
  }
}
function hasAlphabet(input) {
  var reg = /[a-zA-Z]/i;
  if (input && input.length > 0 && reg.test(input)) {
    return true;
  } else {
    return false;
  }
}
function addINACCodeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var role = null;
        if (typeof (_pagemodel) != 'undefined') {
          role = _pagemodel.userRole;
        }
        var inacCd = FormManager.getActualValue('inacCd');
        if (inacCd == 'new' && role == 'Processor')
          return new ValidationResult(FormManager.getField('inacCd'), false, '"new" is invalid for INAC\NAC Code.');
        else {
          return new ValidationResult(null, true, null);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}
function performDPLCheck4JP() {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  var dplChkResult = FormManager.getActualValue('dplChkResult');
  if (reqType == 'C') {
    if (custSubGrp == 'INTER' || custSubGrp == 'BPWPQ') {
      FormManager.setValue('dplChkResult', 'NR');
    } else if (dplChkResult == 'NR') {
      FormManager.setValue('dplChkResult', 'Not Done');
    }
  }
}
function showHideCMRPrefix() {
  var role = null;
  if (typeof (_pagemodel) != 'undefined') {
    role = _pagemodel.userRole;
  }
  if (role != 'Processor') {
    // Hide CMR Prefix if user role is not Processor
    FormManager.hide('CmrNoPrefix', 'cmrNoPrefix');
  }
}
function showHideJSIC() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _role = '';
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }

  var hasZs01 = true;
  switch (custSubGrp) {
  case 'NORML':
  case 'EUCMR':
  case 'WHCMR':
  case 'OUTSC':
    if (_role == 'Requester' || _role == 'Processor') {
      FormManager.show('JSICCd', 'jsicCd');
      FormManager.addValidator('jsicCd', Validators.REQUIRED, [ 'JSIC' ], 'MAIN_CUST_TAB');
    }
    // 1712242 - clear JSIC value if there is no ZS01 address
    if (changed) {
      hasZS01 = checkZS01();
      if (!hasZS01) {
        FormManager.setValue('jsicCd', '');
        FormManager.setValue('isicCd', '');
        FormManager.setValue('subIndustryCd', '');
      }
    }
    break;
  case 'STOSB':
  case 'STOSC':
    if (_role == 'Requester') {
      FormManager.show('JSICCd', 'jsicCd');
      FormManager.removeValidator('jsicCd', Validators.REQUIRED);
      FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
      return;
    } else if (_role == 'Processor') {
      FormManager.show('JSICCd', 'jsicCd');
      FormManager.addValidator('jsicCd', Validators.REQUIRED, [ 'JSIC' ], 'MAIN_CUST_TAB');
    }
    // 1712242 - clear JSIC value if there is no ZS01 address
    if (changed) {
      hasZS01 = checkZS01();
      if (!hasZS01) {
        FormManager.setValue('jsicCd', '');
        FormManager.setValue('isicCd', '');
        FormManager.setValue('subIndustryCd', '');
      }
    }
    break;
  case 'BCEXA':
  case 'BFKSC':
  case 'ABIJS':
  case 'AHIJE':
  case 'AUITS':
  case 'AWIGS':
  case 'BDRBS':
  case 'BVMDS':
  case 'BGICS':
  case 'BHISO':
  case 'BIJSC':
  case 'BKRBS':
  case 'BLNIS':
  case 'BMISI':
  case 'BPIJB':
  case 'BQICL':
  case 'BRMSI':
    if (_role == 'Requester') {
      FormManager.show('JSICCd', 'jsicCd');
      FormManager.removeValidator('jsicCd', Validators.REQUIRED);
      return;
    } else if (_role == 'Processor') {
      FormManager.show('JSICCd', 'jsicCd');
      FormManager.addValidator('jsicCd', Validators.REQUIRED, [ 'JSIC' ], 'MAIN_CUST_TAB');
    }
    break;
  case 'STOSI':
    if (_role == 'Requester' || _role == 'Processor') {
      FormManager.setValue('jsicCd', '9999A');
      FormManager.readOnly('jsicCd');
    }
    break;
  case 'BPWPQ':
    if (_role == 'Requester' || _role == 'Processor') {
      FormManager.removeValidator('jsicCd', Validators.REQUIRED);
      FormManager.resetValidations('jsicCd');
      FormManager.resetDropdownValues(FormManager.getField('jsicCd'));
    }
    break;
  case 'ISOCU':
    FormManager.setValue('jsicCd', '');
    FormManager.readOnly('jsicCd');
    FormManager.resetValidations('jsicCd');
    break;
  default:
    if (_role == 'Requester' || _role == 'Processor') {
      FormManager.show('JSICCd', 'jsicCd');
      FormManager.addValidator('jsicCd', Validators.REQUIRED, [ 'JSIC' ], 'MAIN_CUST_TAB');
    }
    break;
  }
}
function checkZS01() {
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
  };
  var record = cmr.query('GETZS01VALRECORDS', qParams);
  var zs01Reccount = record.ret1;
  if (zs01Reccount > 0) {
    return true;
  } else {
    return false;
  }
}
function showHideSubindustry() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _role = '';
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }
  switch (custSubGrp) {
  case 'STOSB':
  case 'STOSC':
  case 'BCEXA':
  case 'BFKSC':
  case 'ABIJS':
  case 'AHIJE':
  case 'AUITS':
  case 'AWIGS':
  case 'BDRBS':
  case 'BVMDS':
  case 'BGICS':
  case 'BHISO':
  case 'BIJSC':
  case 'BKRBS':
  case 'BLNIS':
  case 'BMISI':
  case 'BPIJB':
  case 'BQICL':
  case 'BRMSI':
    if (_role == 'Requester') {
      FormManager.show('Subindustry', 'subIndustryCd');
      FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
      return;
    } else if (_role == 'Processor') {
      FormManager.show('Subindustry', 'subIndustryCd');
      FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    }
    break;
  case 'STOSI':
    if (_role == 'Requester' || _role == 'Processor') {
      FormManager.setValue('subIndustryCd', 'ZF');
      FormManager.readOnly('subIndustryCd');
    }
    break;
  case 'BPWPQ':
    if (_role == 'Requester' || _role == 'Processor') {
      FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
      FormManager.resetValidations('subIndustryCd');
      FormManager.resetDropdownValues(FormManager.getField('subIndustryCd'));
    }
    break;
  case 'ISOCU':
    FormManager.setValue('subIndustryCd', '');
    FormManager.readOnly('subIndustryCd');
    FormManager.resetValidations('subIndustryCd');
    break;
  default:
    if (_role == 'Requester' || _role == 'Processor') {
      FormManager.show('Subindustry', 'subIndustryCd');
      FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ 'Subindustry' ], 'MAIN_CUST_TAB');
    }
    break;
  }
}
function disableRequestFor() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI') {
    document.getElementById('ast-privIndc').innerHTML = '* ';
    document.getElementById('ast-privIndc').setAttribute('style', 'color:red');
    document.getElementById('ast-privIndc').setAttribute('class', 'cmr-ast');

    FormManager.enable('privIndc_1');
    FormManager.enable('privIndc_2');
    FormManager.enable('privIndc_3');
  } else if (custSubGrp == '') {
    return;
  } else {
    document.getElementById('ast-privIndc').innerHTML = '';
    try {
      FormManager.getField('privIndc_1').set('checked', false);
      FormManager.getField('privIndc_2').set('checked', false);
      FormManager.getField('privIndc_3').set('checked', false);
    } catch (e) {
      FormManager.getField('privIndc_1').checked = false;
      FormManager.getField('privIndc_2').checked = false;
      FormManager.getField('privIndc_3').checked = false;
    }
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');
  }
}
function disableProductType() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (custSubGrp == 'INTER' && reqType == 'C') {
    document.getElementById('ast-ProdType').innerHTML = '* ';
    document.getElementById('ast-ProdType').setAttribute('style', 'color:red');
    document.getElementById('ast-ProdType').setAttribute('class', 'cmr-ast');

    FormManager.enable('prodType_1');
    FormManager.enable('prodType_2');
    FormManager.enable('prodType_3');
    FormManager.enable('prodType_4');
    FormManager.enable('prodType_5');
    FormManager.enable('prodType_6');
    FormManager.enable('prodType_7');
    FormManager.enable('prodType_8');
  } else if (custSubGrp == '') {
    return;
  } else {
    document.getElementById('ast-ProdType').innerHTML = '';
    try {
      FormManager.getField('prodType_1').set('checked', false);
      FormManager.getField('prodType_2').set('checked', false);
      FormManager.getField('prodType_3').set('checked', false);
      FormManager.getField('prodType_4').set('checked', false);
      FormManager.getField('prodType_5').set('checked', false);
      FormManager.getField('prodType_6').set('checked', false);
      FormManager.getField('prodType_7').set('checked', false);
      FormManager.getField('prodType_8').set('checked', false);
    } catch (e) {
      FormManager.getField('prodType_1').checked = false;
      FormManager.getField('prodType_2').checked = false;
      FormManager.getField('prodType_3').checked = false;
      FormManager.getField('prodType_4').checked = false;
      FormManager.getField('prodType_5').checked = false;
      FormManager.getField('prodType_6').checked = false;
      FormManager.getField('prodType_7').checked = false;
      FormManager.getField('prodType_8').checked = false;
    }
    FormManager.readOnly('prodType_1');
    FormManager.readOnly('prodType_2');
    FormManager.readOnly('prodType_3');
    FormManager.readOnly('prodType_4');
    FormManager.readOnly('prodType_5');
    FormManager.readOnly('prodType_6');
    FormManager.readOnly('prodType_7');
    FormManager.readOnly('prodType_8');
  }
}
function updateBillToCustomerNo() {
  var _billToCustNoHandler = dojo.connect(FormManager.getField('salesTeamCd'), 'onChange', function(value) {
    if ('IBMTP' == FormManager.getActualValue('custGrp')) {
      if ('BPWPQ' == FormManager.getActualValue('custSubGrp')) {
        var D0074 = 'D0074';
        var dealerNo = FormManager.getActualValue('salesTeamCd');
        var billToCustNo = null;
        if (dealerNo.toUpperCase().startsWith("D")) {
          if (dealerNo > D0074) {
            billToCustNo = '930' + (dealerNo.substring(dealerNo.length - 3));
          } else if (dealerNo <= D0074) {
            var intDealerNo = parseInt(dealerNo.substring(dealerNo.length - 3)) - 1;
            if (intDealerNo >= 100) {
              billToCustNo = '930' + intDealerNo;
            } else if (intDealerNo >= 10) {
              billToCustNo = '9300' + intDealerNo;
            } else {
              billToCustNo = '93000' + intDealerNo;
            }
          }
        } else if (dealerNo.toUpperCase().startsWith("S")) {
          billToCustNo = '940S' + dealerNo.substring(dealerNo.length - 2);
        }
        FormManager.setValue('billToCustNo', billToCustNo);
      }
    }
  });
  if (_billToCustNoHandler && _billToCustNoHandler[0]) {
    _billToCustNoHandler[0].onChange();
  }
}

/**
 * Validator for only a single Sold-to record for the request
 */
function addSoldToValidatorJP() {
  FormManager.addFormValidator(
      (function() {
        return {
          validate : function() {
            var zs01ReqId = FormManager.getActualValue('reqId');
            var custSubGrp = FormManager.getActualValue('custSubGrp');
            var qParams = {
              REQ_ID : zs01ReqId,
            };
            var record = cmr.query('GETZS01VALRECORDS', qParams);
            var zs01Reccount = record.ret1;
            var custType = FormManager.getActualValue('custType');
            if (custType.includes('A')) {
              if (Number(zs01Reccount) > 1) {
                return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
              } else if (Number(zs01Reccount == 0)
                  && (custSubGrp != 'BCEXA' && custSubGrp != 'BFKSC' && custSubGrp != 'BPWPQ' && custSubGrp != 'ISOCU')) {
                return new ValidationResult(null, false, 'At least one Sold-To Address must be defined.');
              } else {
                return new ValidationResult(null, true);
              }
            } else {
              return new ValidationResult(null, true);
            }
            return new ValidationResult(null, true);
          }
        };
      })(), 'MAIN_NAME_TAB', 'frmCMR');
}
function setEnterCMRNoForupdate() {
  var reqType = FormManager.getActualValue('reqType');
  var reqId = FormManager.getActualValue('reqId');
  var custType = FormManager.getActualValue('custType');
  if (reqType == 'U' && FormManager.getActualValue('enterCMRNo') == '') {
    if (custType.includes('A')) {
      var params = {
        REQ_ID : reqId,
        ADDR_TYPE : "ZS01",
      };
      var result = cmr.query('GET.CMR_NO_BY_REQID_ADDRTYPE', params);
      var cmrNo = result.ret1;
      if (cmrNo != null && cmrNo.length > 0) {
        FormManager.setValue('enterCMRNo', cmrNo);
      }
    }
    window.setTimeout('ensureCMRNoValue()', 1000);
  }
}
function setCSBORequired() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    if (role == 'REQUESTER') {
      FormManager.removeValidator('csBo', Validators.REQUIRED);
    } else if (role == 'PROCESSOR') {
      if (custSubGrp == 'BPWPQ' || custSubGrp == 'ISOCU' || custSubGrp == 'BCEXA' || custSubGrp == 'BFKSC') {
        FormManager.removeValidator('csBo', Validators.REQUIRED);
      } else {
        FormManager.enable('csBo');
        FormManager.addValidator('csBo', Validators.REQUIRED, [ 'CS BO Code' ], 'MAIN_IBM_TAB');
      }
    }
  } else if (reqType == 'U') {
    if (role == 'REQUESTER') {
      if (custSubGrp == 'NORML' || custSubGrp == 'EUCMR' || custSubGrp == 'WHCMR' || custSubGrp == 'OUTSC' || custSubGrp == 'BPWPQ'
          || custSubGrp == 'ISOCU' || custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI' || custSubGrp == 'INTER') {
        FormManager.enable('csBo');
        FormManager.removeValidator('csBo', Validators.REQUIRED);
      } else {
        FormManager.readOnly('csBo');
        FormManager.removeValidator('csBo', Validators.REQUIRED);
      }
    } else if (role == 'PROCESSOR') {
      if (custSubGrp == 'NORML' || custSubGrp == 'EUCMR' || custSubGrp == 'WHCMR' || custSubGrp == 'OUTSC' || custSubGrp == 'BPWPQ'
          || custSubGrp == 'ISOCU' || custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI' || custSubGrp == 'INTER') {
        FormManager.addValidator('csBo', Validators.REQUIRED, [ 'CS BO Code' ], 'MAIN_IBM_TAB');
      } else if (custSubGrp == 'BCEXA' || custSubGrp == 'BFKSC') {
        FormManager.readOnly('csBo');
      } else if (custSubGrp == 'ABIJS' || custSubGrp == 'AHIJE' || custSubGrp == 'AUITS' || custSubGrp == 'AWIGS' || custSubGrp == 'BDRBS'
          || custSubGrp == 'BVMDS' || custSubGrp == 'BGICS' || custSubGrp == 'BHISO' || custSubGrp == 'BIJSC' || custSubGrp == 'BKRBS'
          || custSubGrp == 'BLNIS' || custSubGrp == 'BMISI' || custSubGrp == 'BPIJB' || custSubGrp == 'BQICL' || custSubGrp == 'BRMSI') {
        FormManager.enable('csBo');
      }
    }
  }
}
function setCSBOOnScenarioChange() {
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var custTypeinDB = _pagemodel.custSubGrp;
  if (custSubGrp == '') {
    return;
  }
  if (custTypeinDB != null && custSubGrp == custTypeinDB) {
    return;
  }
  if (!(cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U')) {
    switch (custSubGrp) {
    case 'NORML':
    case 'EUCMR':
    case 'WHCMR':
    case 'OUTSC':
    case 'STOSB':
    case 'STOSC':
    case 'STOSI':
    case 'INTER':
      var postCd = FormManager.getActualValue('postCd');
      addPostCdCSBOLogic(postCd);
      break;
    case 'BPWPQ':
    case 'ISOCU':
    case 'BCEXA':
    case 'BFKSC':
      FormManager.setValue('csBo', '');
      break;
    case 'ABIJS':
    case 'AHIJE':
    case 'AUITS':
    case 'AWIGS':
    case 'BDRBS':
    case 'BVMDS':
    case 'BGICS':
    case 'BHISO':
    case 'BIJSC':
    case 'BKRBS':
    case 'BLNIS':
    case 'BMISI':
    case 'BPIJB':
    case 'BQICL':
    case 'BRMSI':
      setCSBOSubsidiaryValue();
      break;
    case '':
    default:
      break;
    }
  } else if (cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U') {
    switch (custSubGrp) {
    case 'NORML':
    case 'EUCMR':
    case 'WHCMR':
    case 'OUTSC':
    case 'BPWPQ':
    case 'ISOCU':
    case 'STOSB':
    case 'STOSC':
    case 'STOSI':
    case 'INTER':
      var postCd = getZS01PostCd();
      addPostCdCSBOLogic(postCd);
      break;
    case 'BCEXA':
    case 'BFKSC':
      FormManager.setValue('csBo', '');
      break;
    case 'ABIJS':
    case 'AHIJE':
    case 'AUITS':
    case 'AWIGS':
    case 'BDRBS':
    case 'BVMDS':
    case 'BGICS':
    case 'BHISO':
    case 'BIJSC':
    case 'BKRBS':
    case 'BLNIS':
    case 'BMISI':
    case 'BPIJB':
    case 'BQICL':
    case 'BRMSI':
      FormManager.setValue('csBo', '0000');
      if (role == 'REQUESTER') {
        FormManager.readOnly('csBo');
        FormManager.removeValidator('csBo', Validators.REQUIRED);
      } else if (role == 'PROCESSOR') {
        FormManager.removeValidator('csBo', Validators.REQUIRED);
      }
      break;
    case '':
    default:
      break;
    }
  }
}
function getZS01PostCd() {
  var _reqId = FormManager.getActualValue('reqId');
  var postCdParams = {
    _qall : 'Y',
    REQ_ID : _reqId,
    ADDR_TYPE : "ZS01",
  };
  var postCdResult = cmr.query('ADDR.GET.POSTCD.BY_REQID_ADDRTYP', postCdParams);
  if (postCdResult != null && postCdResult.length > 0) {
    var ZS01PostCd = postCdResult[0].ret1;
    return ZS01PostCd;
  } else {
    return null;
  }
}
function setTier2Required() {
  var dealerNo = FormManager.getActualValue('salesTeamCd');
  var requiredDealerNoList = [ 'D0660', 'D0663', 'D0674', 'D0696', 'D0764', 'D0799', 'D0259', 'D0313', 'D0800', 'D0860', 'D0863', 'D0864', 'D0874',
      'D0883', 'D0884', 'D0885', 'D0886', 'D0887', 'D0919', 'D0920', 'D0921', 'D0922', 'D0923', 'D0924', 'D0925', 'D0926', 'D0939' ];
  var matched = false;
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (custSubGrp == 'BPWPQ' || custSubGrp == 'ISOCU') {
    for (var i = 0; i < requiredDealerNoList.length; i++) {
      if (dealerNo == requiredDealerNoList[i]) {
        matched = true;
      }
    }
    if (matched == true) {
      FormManager.addValidator('tier2', Validators.REQUIRED, [ 'TIER-2' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('tier2', Validators.REQUIRED);
    }
  }
}
function addLengthValidatorForWorkNo() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _workno = FormManager.getActualValue('orgNo');
        if (_workno && _workno.length > 0 && _workno.length != 6) {
          return new ValidationResult(FormManager.getField('orgNo'), false, 'The correct Work No length is exactly 6 characters.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}
function addLengthValidatorForPC() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _pc = FormManager.getActualValue('soProjectCd');
        if (_pc && _pc.length > 0 && _pc.length != 6) {
          return new ValidationResult(FormManager.getField('soProjectCd'), false, 'The correct Project code length is exactly 6 characters.');
        }
        return new ValidationResult(null, true, null);
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

/* Override Actions for Approvals */
/**
 * Formatter for the Action column of the Approval list grid
 * 
 */
function actionsFormatter(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var status = rowData.status;
  var requiredIndc = rowData.requiredIndc.toString();
  var actions = '';
  var approvalId = rowData.approvalId[0];
  var approverNm = rowData.approverNm[0];
  if (status == 'DRA') {
    _approvals.hasDraft = true;
  }
  if ('PAPR' == status) {
    actions = actions + '<input type="button" value="Send Reminder" class="cmr-grid-btn" onclick="sendApprovalReminder(\'' + approvalId + '\', \''
        + approverNm + '\', \'' + status + '\')">';
    actions = actions + '<input type="button" value="Override" class="cmr-grid-btn" onclick="overrideApproval(\'' + approvalId + '\', \'' + status
        + '\', \'' + requiredIndc + '\')">';
  }
  if ('DRA' == status || 'PMAIL' == status || 'PAPR' == status || 'OVERP' == status || 'PREM' == status) {
    if (!('Y' == requiredIndc.toUpperCase())) {
      actions = actions + '<input type="button" value="Cancel" class="cmr-grid-btn" onclick="cancelApproval(\'' + approvalId + '\', \'' + status
          + '\')">';
    } else if ('PROCESSOR' == role.toUpperCase()) {
      actions = actions + '<input type="button" value="Cancel" class="cmr-grid-btn" onclick="cancelApproval(\'' + approvalId + '\', \'' + status
          + '\')">';
    }
  }
  if ('REJ' == status) {
    actions = actions + '<input type="button" value="Override" class="cmr-grid-btn" onclick="overrideApproval(\'' + approvalId + '\', \'' + status
        + '\', \'' + requiredIndc + '\')">' + '<input type="button" value="Re-submit" class="cmr-grid-btn" onclick="resubmitApproval(\'' + approvalId
        + '\', \'' + status + '\')">';
  }
  if ('DRA' == status) {
    // actions = actions + '<input type="button" value="Send Request"
    // class="cmr-grid-btn" onclick="sendApprovalRequest(\'' + approvalId + '\',
    // \'' + approverNm + '\', \'' + status + '\')">';
  }

  return actions;
}

function actionsFormatterBlank(value, rowIndex) {
  var rowData = this.grid.getItem(rowIndex);
  var status = rowData.status[0];
  var approvalId = rowData.approvalId[0];
  var approverNm = rowData.approverNm[0];
  var actions = '';
  var viewer = true;
  if ((typeof (_pagemodel) != 'undefined') && _pagemodel.userRole != 'Viewer') {
    viewer = false;
  }
  if (!viewer && status == 'PAPR') {
    actions = actions + '<input type="button" value="Send Reminder" class="cmr-grid-btn" onclick="sendApprovalReminder(\'' + approvalId + '\', \''
        + approverNm + '\', \'' + status + '\')">';
    actions = actions + '<input type="button" value="Override" class="cmr-grid-btn" onclick="overrideApproval(\'' + approvalId + '\', \'' + status
        + '\', \'' + requiredIndc + '\')">';
  }
  return actions;
}

function addApprovalModal_onLoad() {
  var reqType = FormManager.getActualValue('reqType');
  cmr.currentModalId = 'addApprovalModal';
  // clear messages and values
  MessageMgr.clearMessages(true);
  if (reqType == 'C' || reqType == 'U') {
    FormManager.setValue('typId', '');
  }
  FormManager.setValue('displayName', '');
  FormManager.setValue('approval_cmt', '');
  FormManager.setValue('intranetId', '');
  FormManager.setValue('displayName_bpcont', '');
  FormManager.setValue('displayName_readonly', '(none selected)');

  // show default for add approval
  cmr.showNode('approvalTypeCont');
  cmr.showNode('approverCont');
  cmr.showNode('approvalBtnAdd');
  cmr.hideNode('approvalBtnSubmit');
  dojo.byId('displayName-lbl').innerHTML = 'Approver: <span style="color:red" class="cmr-ast" id="ast-approval_cmt">* </span>';
  cmr.setModalTitle('addApprovalModal', 'Add Approval');

  if (_approvals.actionMode == 'ADD_APPROVER') {
    // add approval, defaults
  } else if (_approvals.actionMode == 'CANCEL') {
    // cancel
    cmr.setModalTitle('addApprovalModal', 'Cancel Approval');
    cmr.hideNode('approvalTypeCont');
    cmr.hideNode('approverCont');
    cmr.hideNode('approvalBtnAdd');
    cmr.showNode('approvalBtnSubmit');
  } else if (_approvals.actionMode == 'RESUBMIT') {
    // cancel
    cmr.setModalTitle('addApprovalModal', 'Re-submit Approval');
    cmr.hideNode('approvalTypeCont');
    cmr.hideNode('approverCont');
    cmr.hideNode('approvalBtnAdd');
    cmr.showNode('approvalBtnSubmit');
  } else if (_approvals.actionMode == 'OVERRIDE') {
    // cancel
    FormManager.enable('displayName');
    cmr.setModalTitle('addApprovalModal', 'Override Approval');
    cmr.hideNode('approvalTypeCont');
    dojo.byId('displayName-lbl').innerHTML = 'New Approver: <span style="color:red" class="cmr-ast" id="ast-approval_cmt">* </span>';
    cmr.showNode('approverCont');
    cmr.hideNode('approvalBtnAdd');
    cmr.showNode('approvalBtnSubmit');
  }
}

function addTelFaxValidator(cntry, addressMode, details) {
  FormManager.addValidator('custPhone', Validators.TWO_DASHES_FORMAT, [ 'Tel No' ]);
  FormManager.addValidator('custFax', Validators.TWO_DASHES_FORMAT, [ 'Fax No' ]);
}

function checkEstabFuncCdMandatory() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '760') {
          return new ValidationResult(null, true);
        }
        var failInd = false;
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var estabFuncCd = null;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            type = record.addrType;
            estabFuncCd = record.estabFuncCd;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (estabFuncCd) == 'object') {
              estabFuncCd = estabFuncCd[0];
            }
            if (type == 'ZE01' && (estabFuncCd == '' || estabFuncCd == null)) {
              failInd = true;
            }
          }
        }
        if (failInd == true) {
          return new ValidationResult(null, false, 'Estab Function Code is missing for Establishment address. Please check again.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
function addRequestForValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var checked = false;
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (custSubGrp == 'STOSB' || custSubGrp == 'STOSC' || custSubGrp == 'STOSI') {
          if (FormManager.getField('privIndc_1').checked == true) {
            checked = true;
          } else if (FormManager.getField('privIndc_2').checked == true) {
            checked = true;
          } else if (FormManager.getField('privIndc_3').checked == true) {
            checked = true;
          }
          if (checked == false) {
            return new ValidationResult(null, false, 'Requset For is required.');
          } else {
            return new ValidationResult(null, true, null);
          }
        } else {
          return new ValidationResult(null, true, null);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}
function addProductTypeValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var checked = false;
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var reqType = FormManager.getActualValue('reqType');
        if (reqType == 'C' && custSubGrp == 'INTER') {
          if (FormManager.getField('prodType_1').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_1').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_2').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_3').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_4').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_5').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_6').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_7').checked == true) {
            checked = true;
          } else if (FormManager.getField('prodType_8').checked == true) {
            checked = true;
          }
          if (checked == false) {
            return new ValidationResult(null, false, 'Product Type is required.');
          } else {
            return new ValidationResult(null, true, null);
          }
        } else {
          return new ValidationResult(null, true, null);
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function addDPLCheckValidatorJP() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('dplChkResult');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        if (custSubGrp == 'BPWPQ' || custSubGrp == 'ISOCU' || custSubGrp == '' || custSubGrp == 'BQICL') {
          return new ValidationResult(null, true);
        } else {
          if (result == '' || result.toUpperCase() == 'NOT DONE') {
            return new ValidationResult(null, false, 'DPL Check has not been performed yet.');
          } else if (result == '' || result.toUpperCase() == 'ALL FAILED') {
            return new ValidationResult(null, false, 'DPL Check has failed. This record cannot be processed.');
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}
function addFailedDPLValidatorJP() {
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                if (typeof (_pagemodel) != 'undefined') {
                  var custSubGrp = FormManager.getActualValue('custSubGrp');
                  if (custSubGrp == 'BPWPQ' || custSubGrp == 'ISOCU' || custSubGrp == '' || custSubGrp == 'BQICL') {
                    return new ValidationResult(null, true);
                  } else {
                    if (_pagemodel.dplChkResult.trim() == 'SF' || _pagemodel.dplChkResult.trim() == 'AF') {
                      var id = FormManager.getActualValue('reqId');
                      var ret = cmr.query('CHECK_DPL_ATTACHMENT', {
                        ID : id
                      });

                      if (ret == null || ret.ret1 == null) {
                        return new ValidationResult(null, false,
                            'DPL Matching results has not been attached to the request. This is required since DPL checks failed for one or more addresses.');
                      } else {
                        return new ValidationResult(null, true);
                      }
                    } else {
                      return new ValidationResult(null, true);
                    }
                  }
                }
              }
            };
          })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}
function removeDefaultValueTelNo() {
  var custType = FormManager.getActualValue('custType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var addrType = FormManager.getActualValue('addrType');
  switch (custSubGrp) {
  case 'NORML':
  case 'OUTSC':
  case 'ABIJS':
  case 'AHIJE':
  case 'AUITS':
  case 'AWIGS':
  case 'BDRBS':
  case 'BVMDS':
  case 'BGICS':
  case 'BHISO':
  case 'BIJSC':
  case 'BKRBS':
  case 'BLNIS':
  case 'BMISI':
  case 'BPIJB':
  case 'BRMSI':
    if (custType == 'CEA') {
      if (changed) {
        if (cmr.addressMode == 'newAddress') {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    } else if (custType == 'EA' && cmr.addressMode == 'newAddress') {
      if (changed) {
        if (addrType == 'ZC01') {
          setAddrFieldHide('custPhone', 'CustPhone');
          FormManager.setValue('custPhone', '');
        } else {
          if (changed) {
            setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
            FormManager.setValue('custPhone', '00-0000-0000');
          }
        }
      }
    } else if (custType == 'A' && cmr.addressMode == 'newAddress') {
      if (changed) {
        if (addrType == 'ZC01' || addrType == 'ZE01') {
          setAddrFieldHide('custPhone', 'CustPhone');
          FormManager.setValue('custPhone', '');
        } else {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    }
    break;
  case 'EUCMR':
  case 'WHCMR':
    if (custType == 'CEA') {
      if (changed) {
        if (cmr.addressMode == 'newAddress') {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    } else if (custType == 'EA' && cmr.addressMode == 'newAddress') {
      if (changed) {
        if (addrType == 'ZC01') {
          setAddrFieldHide('custPhone', 'CustPhone');
          FormManager.setValue('custPhone', '');
        } else {
          if (changed) {
            setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
            FormManager.setValue('custPhone', '00-0000-0000');
          }
        }
      }
    } else if (custType == 'A' && cmr.addressMode == 'newAddress') {
      if (changed) {
        if (addrType == 'ZC01' || addrType == 'ZE01') {
          setAddrFieldHide('custPhone', 'CustPhone');
          FormManager.setValue('custPhone', '');
        } else {
          setAddrFieldOptional('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    }
    break;
  case 'BPWPQ':
    break;
  case 'ISOCU':
    if (changed) {
      var _reqType = FormManager.getActualValue('reqType');
      var _currentReqType = cmr.currentRequestType;
      if (!(_reqType == 'U' || _currentReqType == 'U')) {
        // Create request
        if (cmr.addressMode == 'newAddress') {
          setAddrFieldHide('custPhone', 'CustPhone');
          FormManager.setValue('custPhone', '');
        }
      } else if (_reqType == 'U' || _currentReqType == 'U') {
        // Update request
        setAddrFieldOptional('custPhone', 'CustPhone');
      }
    }
    break;
  case 'BCEXA':
  case 'BFKSC':
    if (changed) {
      if (cmr.addressMode == 'newAddress') {
        if (addrType == 'ZC01') {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        } else {
          setAddrFieldHide('custPhone', 'CustPhone');
          FormManager.setValue('custPhone', '');
        }
      }
    }
    break;
  case 'STOSB':
  case 'STOSC':
  case 'STOSI':
  case 'INTER':
  case 'BQICL':
    if (changed) {
      if (cmr.addressMode == 'newAddress') {
        if (addrType == 'ZC01' || addrType == 'ZE01') {
          setAddrFieldHide('custPhone', 'CustPhone');
          FormManager.setValue('custPhone', '');
        } else {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    }
    break;
  default:
    if (custType == 'CEA') {
      if (changed) {
        if (cmr.addressMode == 'newAddress') {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    } else if (custType == 'EA') {
      if (addrType == 'ZC01') {
        setAddrFieldHide('custPhone', 'CustPhone');
        FormManager.setValue('custPhone', '');
      } else {
        if (cmr.addressMode == 'newAddress') {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    } else if (custType == 'A') {
      if (addrType == 'ZC01' || addrType == 'ZE01') {
        setAddrFieldHide('custPhone', 'CustPhone');
        FormManager.setValue('custPhone', '');
      } else {
        if (cmr.addressMode == 'newAddress') {
          setAddrFieldMandatory('custPhone', 'CustPhone', 'Phone #');
          FormManager.setValue('custPhone', '00-0000-0000');
        }
      }
    }
    break;
  }
  function _doBeforeAction(action) {
    if (action == YourActions.Claim) {
      FormManager.enable('privIndc');
    }
  }
  ;
}

/* Override for init template handler - do not load template for updates */
function initGenericTemplateHandler() {
  // templates/scenarios initialization. connect onchange of the customer type
  // to load the template
  var reqType = FormManager.getActualValue('reqType');
  if (_templateHandler == null && FormManager.getField('custSubGrp')) {
    _templateHandler = dojo.connect(FormManager.getField('custSubGrp'), 'onChange', function(value) {
      if (_delayedLoadComplete) {
        var reqType = FormManager.getActualValue('reqType');
        var val = FormManager.getActualValue('custSubGrp');
        if (reqType == 'C') {
          if (val != '') {
            TemplateService.fill('reqentry');
          }
        } else {
          console.log('sub scenario changed to ' + val + ' for update.');
          // add here code to enable/disable fields when a scenario is selected
          // for updates
          disableFieldsForUpdateOnScenarios();
          clearFieldsForUpdateOnScenarios();
        }
      }
    });
  }
  if (_templateHandler && _templateHandler[0]) {
    _templateHandler[0].onChange();
  }
  if (reqType == 'C') {
    TemplateService.init();
  }
}

function clearFieldsForUpdateOnScenarios() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (reqType != "U") {
    return;
  }

  if (custSubGrp != 'BQICL') {
    FormManager.setValue('proxiLocnNo', '');
  }
}

function disableFieldsForUpdateOnScenarios() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  // console.log('reqType =' + reqType);
  if (reqType != "U") {
    return;
  }

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  FormManager.readOnly('inacType');

  switch (custSubGrp) {
  // case '':
  // return;
  // break;
  case 'NORML':
  case 'EUCMR':
  case 'WHCMR':
    FormManager.enable('icmsInd');
    FormManager.addValidator('icmsInd', Validators.REQUIRED, [ 'OFCD /Sales(Team) No/Rep Sales No Change' ], 'MAIN_GENERAL_TAB');

    FormManager.addValidator('leasingCompanyIndc', Validators.REQUIRED, [ 'Leasing' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('channelCd', Validators.REQUIRED, [ 'Channel' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('govType', Validators.REQUIRED, [ 'Government Entity' ], 'MAIN_CUST_TAB');

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.disable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    // FormManager.enable('inacType');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.removeValidator('billToCustNo', Validators.REQUIRED);
    break;
  case 'OUTSC':
    FormManager.enable('icmsInd');
    FormManager.addValidator('icmsInd', Validators.REQUIRED, [ 'OFCD /Sales(Team) No/Rep Sales No Change' ], 'MAIN_GENERAL_TAB');

    FormManager.addValidator('leasingCompanyIndc', Validators.REQUIRED, [ 'Leasing' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('channelCd', Validators.REQUIRED, [ 'Channel' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('govType', Validators.REQUIRED, [ 'Government Entity' ], 'MAIN_CUST_TAB');

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.disable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    // FormManager.enable('inacType');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');
    // FormManager.enable('zseriesSw');

    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    break;
  case 'BPWPQ':
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.enable('icmsInd');
    FormManager.addValidator('icmsInd', Validators.REQUIRED, [ 'OFCD /Sales(Team) No/Rep Sales No Change' ], 'MAIN_GENERAL_TAB');

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);
    // For defect 1740581
    FormManager.enable('abbrevNm');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    // FormManager.enable('inacType');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');
    // FormManager.enable('zseriesSw');

    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    break;
  case 'ISOCU':
    FormManager.enable('icmsInd');
    FormManager.addValidator('icmsInd', Validators.REQUIRED, [ 'OFCD /Sales(Team) No/Rep Sales No Change' ], 'MAIN_GENERAL_TAB');

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.disable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    // FormManager.enable('inacType');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');
    // FormManager.enable('zseriesSw');

    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    break;
  case 'STOSI':
    FormManager.setValue('custAcctType', 'OU');

    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    // For Defect 1704880 new comment
    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.addValidator('soProjectCd', Validators.REQUIRED, [ 'Project Code' ], 'MAIN_IBM_TAB');

    // For Defect 1714942 new comment
    FormManager.setValue('oemInd', '');
    FormManager.removeValidator('oemInd', Validators.REQUIRED);
    FormManager.setValue('siInd', 'N');
    FormManager.addValidator('siInd', Validators.REQUIRED, [ 'SI' ], 'MAIN_CUST_TAB');

    FormManager.disable('proxiLocnNo');

    // For defect 1746497
    FormManager.disable('creditToCustNo');
    FormManager.disable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.disable('oemInd');
    FormManager.disable('leasingCompanyIndc');
    FormManager.disable('searchTerm');
    FormManager.disable('covId');
    FormManager.disable('repTeamMemberNo');
    FormManager.disable('govType');
    FormManager.disable('educAllowCd');
    FormManager.disable('iinInd');
    FormManager.disable('valueAddRem');
    FormManager.disable('channelCd');
    FormManager.disable('siInd');
    FormManager.disable('inacType');
    FormManager.disable('inacCd');
    FormManager.disable('creditCd');
    FormManager.disable('billToCustNo');
    FormManager.removeValidator('billToCustNo', Validators.REQUIRED);
    // FormManager.enable('zseriesSw');

    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    break;
  case 'STOSB':
    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    // For Defect 1704880 latest comment
    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.addValidator('soProjectCd', Validators.REQUIRED, [ 'Project Code' ], 'MAIN_IBM_TAB');

    FormManager.disable('proxiLocnNo');

    // For defect 1746497
    FormManager.disable('creditToCustNo');
    FormManager.disable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.disable('oemInd');
    FormManager.disable('leasingCompanyIndc');
    FormManager.disable('searchTerm');
    FormManager.disable('covId');
    FormManager.disable('repTeamMemberNo');
    FormManager.disable('govType');
    FormManager.disable('educAllowCd');
    FormManager.disable('iinInd');
    FormManager.disable('valueAddRem');
    FormManager.disable('channelCd');
    FormManager.disable('siInd');
    FormManager.disable('inacType');
    FormManager.disable('inacCd');
    FormManager.disable('creditCd');
    FormManager.disable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');
    // FormManager.enable('zseriesSw');

    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    break;
  case 'STOSC':
    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    // For Defect 1704880 latest comment
    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.addValidator('soProjectCd', Validators.REQUIRED, [ 'Project Code' ], 'MAIN_IBM_TAB');

    FormManager.disable('proxiLocnNo');

    // For defect 1746497
    FormManager.disable('creditToCustNo');
    FormManager.disable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.disable('oemInd');
    FormManager.disable('leasingCompanyIndc');
    FormManager.disable('searchTerm');
    FormManager.disable('covId');
    FormManager.disable('repTeamMemberNo');
    FormManager.disable('govType');
    FormManager.disable('educAllowCd');
    FormManager.disable('iinInd');
    FormManager.disable('valueAddRem');
    FormManager.disable('channelCd');
    FormManager.disable('siInd');
    FormManager.disable('inacType');
    FormManager.disable('inacCd');
    FormManager.disable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');
    // FormManager.enable('zseriesSw');

    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    break;
  case 'INTER':
    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.disable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.removeValidator('billToCustNo', Validators.REQUIRED);
    // FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to
    // Customer No' ], 'MAIN_IBM_TAB');
    // FormManager.enable('zseriesSw');
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    break;

  // Below are Subsidiary Scenario
  case 'BCEXA':
    FormManager.removeValidator('cmrNo', Validators.REQUIRED);
    FormManager.readOnly('cmrNo');

    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.disable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.removeValidator('billToCustNo', Validators.REQUIRED);

    // For 1746497 disable 'zSeries SW' field
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.disable('zseriesSw');
    break;
  case 'BFKSC':
    FormManager.removeValidator('cmrNo', Validators.REQUIRED);
    FormManager.readOnly('cmrNo');

    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.removeValidator('leasingCompanyIndc', Validators.REQUIRED);
    FormManager.removeValidator('channelCd', Validators.REQUIRED);
    FormManager.removeValidator('govType', Validators.REQUIRED);

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.disable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.removeValidator('billToCustNo', Validators.REQUIRED);

    // For 1746497 disable 'zSeries SW' field
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.disable('zseriesSw');
    break;
  case 'ABIJS':
  case 'AHIJE':
  case 'AUITS':
  case 'AWIGS':
  case 'BDRBS':
  case 'BVMDS':
  case 'BGICS':
  case 'BHISO':
  case 'BKRBS':
  case 'BLNIS':
  case 'BMISI':
  case 'BPIJB':
  case 'BRMSI':
    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.addValidator('leasingCompanyIndc', Validators.REQUIRED, [ 'Leasing' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('channelCd', Validators.REQUIRED, [ 'Channel' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('govType', Validators.REQUIRED, [ 'Government Entity' ], 'MAIN_CUST_TAB');

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.disable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');
    FormManager.disable('inacType');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');

    // For 1746497 disable 'zSeries SW' field
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.disable('zseriesSw');
    break;
  case 'BIJSC':
  case 'BQICL':
    FormManager.setValue('icmsInd', '');
    FormManager.readOnly('icmsInd');
    FormManager.removeValidator('icmsInd', Validators.REQUIRED);

    FormManager.addValidator('leasingCompanyIndc', Validators.REQUIRED, [ 'Leasing' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('channelCd', Validators.REQUIRED, [ 'Channel' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('govType', Validators.REQUIRED, [ 'Government Entity' ], 'MAIN_CUST_TAB');

    FormManager.addValidator('cmrNo', Validators.REQUIRED, [ 'CMR Number' ], 'MAIN_IBM_TAB');

    FormManager.removeValidator('orgNo', Validators.REQUIRED);
    FormManager.removeValidator('chargeCd', Validators.REQUIRED);
    FormManager.removeValidator('soProjectCd', Validators.REQUIRED);

    FormManager.enable('proxiLocnNo');
    FormManager.disable('privIndc_1');
    FormManager.disable('privIndc_2');
    FormManager.disable('privIndc_3');
    FormManager.disable('inacType');

    FormManager.enable('creditToCustNo');
    FormManager.enable('tier2');
    // FormManager.disable('outsourcingService');
    FormManager.enable('oemInd');
    FormManager.enable('leasingCompanyIndc');
    FormManager.enable('searchTerm');
    FormManager.enable('covId');
    FormManager.enable('repTeamMemberNo');
    FormManager.enable('govType');
    FormManager.enable('educAllowCd');
    FormManager.enable('iinInd');
    FormManager.enable('valueAddRem');
    FormManager.enable('channelCd');
    FormManager.enable('siInd');
    FormManager.enable('inacCd');
    FormManager.enable('creditCd');
    FormManager.enable('billToCustNo');
    FormManager.removeValidator('billToCustNo', Validators.REQUIRED);
    // FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to
    // Customer No' ], 'MAIN_IBM_TAB');

    // For 1746497 disable 'zSeries SW' field
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.disable('zseriesSw');
    break;
  default:
    break;
  }

}

function addINACValidator() {
  console.log("skip addINACValidator for JP");
  // noop
}

/**
 * Story 1652105: Convert "丁目"/"番地",”番”,"号" into "－" or remove if it is the last
 * letter
 */
function replaceBanGaoForAddrTxt(cntry, addressMode, details) {
  var _addrTxtHandler = dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {

    if (value == undefined) {
      return;
    }

    var addrTxt = FormManager.getActualValue('addrTxt');
    addrTxt = replaceAndSymbol(addrTxt);
    addrTxt = replaceCrossbarSymbol(addrTxt);
    addrTxt = convert2DBCS(addrTxt);
    // addrTxt = convertHalfToFullKatakana(addrTxt);

    // 1652105 - do not replace 番町, i.e, "東京都千代田区霞ヶ関３番町２－１－３" is a legal value.
    var patternStrDingMuFanLast = /(\d|[０-９])(丁目|番地|番(?!町)|号)$/;

    var patternStrDingMuFanNonLast = /(\d|[０-９])(丁目|番地|番(?!町)|号)/g;

    // Remove if it is the last letter

    var newAddrTxt = addrTxt.replace(patternStrDingMuFanLast, '$1');

    // Convert "丁目"/"番地",”番”,"号" into "－"

    newAddrTxt = newAddrTxt.replace(patternStrDingMuFanNonLast, '$1－');

    FormManager.setValue('addrTxt', newAddrTxt);

  });

  if (_addrTxtHandler && _addrTxtHandler[0]) {

    _addrTxtHandler[0].onChange();

  }

}

/**
 * Handles the toggling of IBM Related CMR
 */
function handleIBMRelatedCMR(fromAddress, scenario, scenarioChanged) {
  // 1686132: Special requirement for Subscenario = BQ - IBM Japan Credit LLC
  var relatedCMRName = 'proxiLocnNo';
  var reqType = FormManager.getActualValue('reqType');
  var subGrp = FormManager.getActualValue('custSubGrp');
  if (reqType != 'C') {
    if (subGrp == 'BQICL') {
      FormManager.enable(relatedCMRName);
    } else {
      FormManager.setValue(relatedCMRName, '');
      FormManager.disable(relatedCMRName);
    }
    return;
  }
  FormManager.resetValidations(relatedCMRName);
  if (subGrp == 'BQICL') {
    FormManager.enable(relatedCMRName);
    FormManager.addValidator(relatedCMRName, Validators.REQUIRED, [ FormManager.getLabel('IBMRelatedCMR') ], 'MAIN_CUST_TAB');
  } else {
    FormManager.disable(relatedCMRName);
  }
}

/**
 * Checks the current address types on the japan record
 */
function addAddressRecordsValidatorJP() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var addresses = _allAddressData;
        if (!addresses) {
          return new ValidationResult(null, false, 'Company and/or Establishment details should be entered.');
        }
        var reqType = FormManager.getActualValue('reqType');
        var subType = FormManager.getActualValue('custSubGrp');

        var hasCompany = false;
        var hasEstab = false;
        for (var i = 0; i < addresses.length; i++) {
          var addrType = addresses[i].addrType ? addresses[i].addrType[0] : '';
          if (addrType == 'ZC01') {
            hasCompany = true;
            console.log('Company found');
          } else if (addrType == 'ZE01') {
            hasEstab = true;
            console.log('Establishment found');
          }
        }

        if (reqType == 'U' || (reqType == 'C' && subType != 'BCEXA' && subType != 'BFKSC')) {
          // validate both
          if (!hasCompany || !hasEstab) {
            return new ValidationResult(null, false, 'Both Company and Establishment details should be entered.');
          }
        } else if (reqType == 'C' && (subType == 'BCEXA' || subType == 'BFKSC') && !hasCompany) {
          // validate company
          return new ValidationResult(null, false, 'Company details should be entered.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function ofcdJsicMismatchValidatorJP() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var officeCd = FormManager.getActualValue('salesBusOffCd');
        var sector = '';
        var jsicList = null;
        var jsicCd = FormManager.getActualValue('jsicCd');
        var isSpecialOfficeCd = false;

        if (officeCd == '' || officeCd.length < 1) {
          return new ValidationResult(null, true, null);
        }

        if (FormManager.getActualValue('custSubGrp') != 'NORML' && FormManager.getActualValue('custSubGrp') != 'EUCMR'
            && FormManager.getActualValue('custSubGrp') != 'WHCMR' && FormManager.getActualValue('custSubGrp') != 'OUTSC') {
          return new ValidationResult(null, true, null);
        }

        if (checkSpecialOfficeCdList(officeCd)) {
          isSpecialOfficeCd = true;
          return new ValidationResult(null, true, null);
        }

        if (!isSpecialOfficeCd) {
          sector = getSector(officeCd);
          if (sector != undefined && sector.length > 0) {
            jsicList = getJSIC(sector);
          }
        }

        if (jsicList && !jsicList.includes(jsicCd))
          return new ValidationResult(FormManager.getField('jsicCd'), false, 'Office Code and JSIC mismatch,please confirm the JSIC');
        else {
          return new ValidationResult(null, true, null);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function setJSICSubIndustryCdOptional() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') != 'C') {
    return;
  }
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _role = null;
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }

  switch (custSubGrp) {
  case '':
    break;
  case 'STOSB':
  case 'STOSC':
    if (_role == 'Requester') {
      FormManager.removeValidator('jsicCd', Validators.REQUIRED);
      FormManager.removeValidator('subIndustryCd', Validators.REQUIRED);
    }
    break;
  }
}

function setOutsourcingServiceRequired() {
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  // console.log('reqType =' + reqType);
  if (reqType != "C") {
    return;
  }

  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  switch (custSubGrp) {
  // case '':
  // return;
  // break;
  case 'BPWPQ':
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.removeValidator('creditBp', Validators.REQUIRED);
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.disable('zseriesSw');
    break;
  case 'OUTSC':
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', true);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = true;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('zSeriesSw', 'zseriesSw');
    FormManager.removeValidator('zseriesSw', Validators.REQUIRED);
    FormManager.show('DirectBp', 'creditBp');
    FormManager.addValidator('creditBp', Validators.REQUIRED, [ 'Direct/BP' ], 'MAIN_CUST_TAB');
    break;
  case 'STOSB':
  case 'STOSC':
  case 'STOSI':
  case 'INTER':
  case 'ISOCU':
  case 'NORML':
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.removeValidator('creditBp', Validators.REQUIRED);
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.addValidator('zseriesSw', Validators.REQUIRED, [ 'zSeries SW' ], 'MAIN_CUST_TAB');
    FormManager.enable('zseriesSw');
    break;
  case 'EUCMR':
  case 'WHCMR':
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.enable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.removeValidator('creditBp', Validators.REQUIRED);
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.removeValidator('zseriesSw', Validators.REQUIRED);
    FormManager.enable('zseriesSw');
    break;
  // Below are Subsidiary Scenario
  case 'BCEXA':
  case 'BFKSC':
  case 'ABIJS':
  case 'AHIJE':
  case 'AUITS':
  case 'AWIGS':
  case 'BDRBS':
  case 'BVMDS':
  case 'BGICS':
  case 'BHISO':
  case 'BIJSC':
  case 'BKRBS':
  case 'BLNIS':
  case 'BMISI':
  case 'BPIJB':
  case 'BRMSI':
  case 'BQICL':
    if (FormManager.getField('outsourcingService').set) {
      FormManager.getField('outsourcingService').set('checked', false);
    } else if (FormManager.getField('outsourcingService')) {
      FormManager.getField('outsourcingService').checked = false;
    }
    FormManager.disable('outsourcingService');
    FormManager.hide('DirectBp', 'creditBp');
    FormManager.removeValidator('creditBp', Validators.REQUIRED);
    FormManager.show('zSeriesSw', 'zseriesSw');
    FormManager.disable('zseriesSw');
    break;
  default:
    break;
  }

}

// CMR-6915 INAC editable for requestor of Create request.
function setSalesTeamCdEditoble() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var _role = null;
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  console.log("---setSalesTeamCdEditoble---reqType==" + reqType + " custSubGrp==" + custSubGrp + " _role==" + _role);
  if (custSubGrp == 'BVMDS' && reqType == 'C' && _role == 'Requester') {
    FormManager.enable('inacType');
    FormManager.enable('inacCd');
  }

}

// 1841252 - set OFCD required for Processer in all scenarios both Create and
// Update
function setSalesBusOffCdRequired() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var _role = null;
  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }
  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  switch (custSubGrp) {
  case '':
    return;
    break;
  case 'ISOCU':
    if (_role == 'Processor' && reqType == 'C') {
      FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
    } else if (_role == 'Processor' && reqType == 'U') {
      FormManager.enable('salesBusOffCd');
      FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'Office Code' ], 'MAIN_IBM_TAB');
    }
    break;
  case 'BCEXA':
  case 'BFKSC':
    if (_role == 'Processor') {
      FormManager.removeValidator('salesBusOffCd', Validators.REQUIRED);
      FormManager.readOnly('salesBusOffCd');
    }
    break;
  case 'BVMDS':
    FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'Office Code' ], 'MAIN_IBM_TAB');
    break;
  default:
    if (_role == 'Processor') {
      FormManager.enable('salesBusOffCd');
      FormManager.addValidator('salesBusOffCd', Validators.REQUIRED, [ 'Office Code' ], 'MAIN_IBM_TAB');
    }
    break;
  }
}
function addressDuplicateValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '760') {
          return new ValidationResult(null, true);
        }

        var duplicateInd = false;
        var duplicateAddr = [];
        var duplicateAddrDesc = [];

        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var qParams = {
          _qall : 'Y',
          CMR_ISSUING_CNTRY : cmrIssuingCntry,
        };
        var addrTypesInLOV = cmr.query('GET.ADDR_TYPES', qParams);
        if (addrTypesInLOV != null && CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          for (var i = 0; i < addrTypesInLOV.length; i++) {
            var addrCount = 0;
            for (var j = 0; j < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; j++) {
              var record = null;
              var type = null;
              record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(j);
              type = record.addrType;
              if (typeof (type) == 'object') {
                type = type[0];
              }
              if (addrTypesInLOV[i].ret1 == type) {
                addrCount += 1;
              }
            }
            if (addrCount > 1) {
              duplicateInd = true;
              duplicateAddr.push(addrTypesInLOV[i].ret1);
              duplicateAddrDesc.push(addrTypesInLOV[i].ret2);
            }
          }
        }
        if (duplicateInd == true && duplicateAddr.length >= 1) {
          return new ValidationResult(null, false, 'Only one instance of each address can be added.Please remove additional ' + duplicateAddrDesc
              + ' addresses');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function showOrHideAddrFieldForBPWPQ(custSubGrp, custType, addrType, role) {
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // For 1740581
  if (custSubGrp == 'BPWPQ' && (cmr.currentRequestType == 'U' || FormManager.getActualValue('reqType') == 'U')) {

    if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'updateAddress') {
      setAddrFieldOptional('custPhone', 'Tel No');
      setAddrFieldHide('estabFuncCd', 'Estab Function Code');
      setAddrFieldHide('divn', 'Estab No');
      setAddrFieldHide('city2', 'Company No');
      setAddrFieldHide('companySize', 'Company Size');
      setAddrFieldHide('rol', 'ROL Flag');
    }
  }
}

function ROLValidatorForZC01() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        if (FormManager.getActualValue('cmrIssuingCntry') != '760') {
          return new ValidationResult(null, true);
        }
        var failInd = false;
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        var custType = FormManager.getActualValue('custType');
        var reqType = FormManager.getActualValue('reqType');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0) {
          var record = null;
          var type = null;
          var rol = null;

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            type = record.addrType;
            rol = record.rol;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (rol) == 'object') {
              rol = rol[0];
            }
            if (reqType == 'C' && type == 'ZC01' && (rol == '' || rol == null)
                && (custSubGrp == 'NORML' || custSubGrp == 'EUCMR' || custSubGrp == 'WHCMR' || custSubGrp == 'OUTSC') && custType.includes('C')) {
              failInd = true;
            }
          }
        }
        if (failInd == true) {
          return new ValidationResult(null, false, 'ROL Flag is missing for Company address. Please check again.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function convertCustNmDetail2DBCS() {
  dojo.connect(FormManager.getField('email2'), 'onChange', function(value) {
    FormManager.addValidator('email2', Validators.NO_SINGLE_BYTE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
    FormManager.addValidator('email2', Validators.NO_HALF_ANGLE, [ 'Customer Name_Detail' ], 'MAIN_CUST_TAB');
    var custNmDetail = FormManager.getActualValue('email2');
    if (null == custNmDetail || '' == custNmDetail) {
      return;
    }
    custNmDetail = convert2DBCSIgnoreCase(custNmDetail);
    FormManager.setValue('email2', custNmDetail);
  });
}

function replaceCrossbarSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/-/g, '－');
    modifiedVal = modifiedVal.replace(/･/g, '・');
    // Story CMR-1660
    modifiedVal = modifiedVal.replace(/,/g, '，');
    modifiedVal = modifiedVal.replace(/:/g, '：');
    modifiedVal = modifiedVal.replace(/_/g, '＿');
    modifiedVal = modifiedVal.replace(/\(/g, '（');
    modifiedVal = modifiedVal.replace(/\)/g, '）');
  }
  return modifiedVal;
};

function replaceAndSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0) {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/&/g, '＆');
  }
  return modifiedVal;
};

function resetBPWPQValue() {

  var reqType = FormManager.getActualValue('reqType');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var _role = null;

  if (typeof (_pagemodel) != 'undefined') {
    _role = _pagemodel.userRole;
  }

  if (custSubGrp == 'BPWPQ') {
    if (reqType == 'C' && (_role == 'Requester' || _role == 'Processor')) {
      FormManager.setValue('salesTeamCd', _pagemodel.salesTeamCd == '' ? '' : _pagemodel.salesTeamCd);
      FormManager.readOnly('salesTeamCd');

      FormManager.setValue('tier2', _pagemodel.clientTier == '' ? '' : _pagemodel.clientTier);
      FormManager.readOnly('tier2');

      FormManager.setValue('billToCustNo', _pagemodel.billToCustNo == '' ? '' : _pagemodel.billToCustNo);
      FormManager.enable('billToCustNo');

      FormManager.addValidator('billToCustNo', Validators.REQUIRED, [ 'Bill to Customer No' ], 'MAIN_IBM_TAB');
    }
  } else {
    // FormManager.setValue('salesTeamCd', '');
    // FormManager.enable('salesTeamCd');

    // FormManager.setValue('tier2', '');
    // FormManager.readOnly('tier2');

    // FormManager.setValue('billToCustNo', '');
    // FormManager.readOnly('billToCustNo');

    // FormManager.removeValidator('billToCustNo', Validators.REQUIRED);
  }
}
function addressQuotationValidator() {
  // CREATCMR-788
  FormManager.addValidator('abbrevNm', Validators.NO_QUOTATION, [ 'Account Abbreviated Name' ], 'MAIN_CUST_TAB');
  FormManager.addValidator('custNm1', Validators.NO_QUOTATION, [ 'Customer Name-KANJI' ]);
  FormManager.addValidator('custNm2', Validators.NO_QUOTATION, [ 'Name-KANJI Continue' ]);
  FormManager.addValidator('custNm4', Validators.NO_QUOTATION, [ 'Katakana' ]);
  FormManager.addValidator('custNm3', Validators.NO_QUOTATION, [ 'Full English Name' ]);
  FormManager.addValidator('addrTxt', Validators.NO_QUOTATION, [ 'Address' ]);

  FormManager.addValidator('postCd', Validators.NO_QUOTATION, [ 'Postal Code' ]);
  FormManager.addValidator('dept', Validators.NO_QUOTATION, [ 'Department' ]);
  FormManager.addValidator('office', Validators.NO_QUOTATION, [ 'Branch/Office' ]);
  FormManager.addValidator('bldg', Validators.NO_QUOTATION, [ 'Building' ]);
  FormManager.addValidator('custPhone', Validators.NO_QUOTATION, [ 'Tel No' ]);

  FormManager.addValidator('custFax', Validators.NO_QUOTATION, [ 'Fax No' ]);
  FormManager.addValidator('divn', Validators.NO_QUOTATION, [ 'Estab No' ]);
  FormManager.addValidator('city2', Validators.NO_QUOTATION, [ 'Company No' ]);
  FormManager.addValidator('companySize', Validators.NO_QUOTATION, [ 'Company Size' ]);
  FormManager.addValidator('contact', Validators.NO_QUOTATION, [ 'Contact' ]);

}
dojo.addOnLoad(function() {
  GEOHandler.JP = [ SysLoc.JAPAN ];
  console.log('adding JP functions...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.JP);
  GEOHandler.enableCopyAddress(GEOHandler.JP);
  GEOHandler.setRevertIsicBehavior(false);
  GEOHandler.addAfterConfig(afterConfigForJP, GEOHandler.JP);
  GEOHandler.addAfterConfig(showOfcdMessage, GEOHandler.JP);
  GEOHandler.addAfterConfig(addLogicOnOfficeCdChange, GEOHandler.JP);
  GEOHandler.addAfterConfig(resetAccountAbbNmOnFieldChange, GEOHandler.JP);
  GEOHandler.addAfterConfig(showConfirmForTier2, GEOHandler.JP);
  GEOHandler.addAfterConfig(addJsicIsicLogic, GEOHandler.JP);
  GEOHandler.addAfterConfig(addChargeCodeLogic, GEOHandler.JP);
  GEOHandler.addAfterConfig(updateINACType, GEOHandler.JP);
  GEOHandler.addAfterConfig(showHideCMRPrefix, GEOHandler.JP);
  GEOHandler.addAfterConfig(showHideJSIC, GEOHandler.JP);
  GEOHandler.addAfterConfig(showHideSubindustry, GEOHandler.JP);
  // CREATCMR-6854
  // GEOHandler.addAfterConfig(updateBillToCustomerNo, GEOHandler.JP);
  GEOHandler.addAfterConfig(accountAbbNmUpperCase, GEOHandler.JP);
  GEOHandler.addAfterConfig(disableFieldsForUpdate, GEOHandler.JP);
  GEOHandler.addAfterConfig(setEnterCMRNoForupdate, GEOHandler.JP);
  GEOHandler.addAfterConfig(setTier2Required, GEOHandler.JP);
  GEOHandler.addAfterConfig(showOrHideDirectBpZSeriesSw, GEOHandler.JP);
  GEOHandler.addAfterConfig(setJSICSubIndustryCdOptional, GEOHandler.JP);
  GEOHandler.addAfterConfig(setOutsourcingServiceRequired, GEOHandler.JP);
  GEOHandler.addAfterConfig(setSalesBusOffCdRequired, GEOHandler.JP);
  // GEOHandler.addAfterConfig(addSpaceForCustNmDetail, GEOHandler.JP);
  GEOHandler.addAfterConfig(setCSBORequired, GEOHandler.JP);
  GEOHandler.addAfterConfig(convertCustNmDetail2DBCS, GEOHandler.JP);
  GEOHandler.addAfterConfig(addScenarioDriven, GEOHandler.JP);

  GEOHandler.addAfterTemplateLoad(setCSBORequired, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(setPageLoadDone, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(removeDefaultValueTelNo, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(setJSICSubIndustryCdOptional, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(setOutsourcingServiceRequired, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(setSalesBusOffCdRequired, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(addScenarioDriven, GEOHandler.JP);

  // CREATCMR-6694
  GEOHandler.addAfterConfig(setAdminDeptOptional, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(setAdminDeptOptional, GEOHandler.JP);

  // CREATCMR-6854
  GEOHandler.addAfterConfig(resetBPWPQValue, GEOHandler.JP);
  GEOHandler.addAfterTemplateLoad(resetBPWPQValue, GEOHandler.JP);

  // 1686132: Special requirement for Subscenario = BQ - IBM Japan Credit LLC
  // under the scenario of Subsidiary
  GEOHandler.addAfterTemplateLoad(handleIBMRelatedCMR, GEOHandler.JP);

  GEOHandler.addToggleAddrTypeFunction(toggleAddrTypesForJP, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(addPostlCdLocnCdLogic, GEOHandler.JP);

  GEOHandler.addToggleAddrTypeFunction(addSingleByteValidatorJP, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(showOrHideAddrFields, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(convertKATAKANA, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(convertKANJICont, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(convertBuilding, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(convertBranch, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(convertContact, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(convertDept, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(abbNmUpperCase, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(addSpaceForCustNm1, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(addTelFaxValidator, GEOHandler.JP);
  GEOHandler.addToggleAddrTypeFunction(replaceBanGaoForAddrTxt, GEOHandler.JP);

  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.JP);
  GEOHandler.addAddrFunction(setFieldValueOnAddrSave, GEOHandler.JP);
  GEOHandler.addAddrFunction(showOrHideAddrFieldForBPWPQ, GEOHandler.JP);
  GEOHandler.addAddrFunction(showOrHideAddrFieldForLocationCode, GEOHandler.JP);

  GEOHandler.registerValidator(addDateValidatorForReqDueDate, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(addINACCodeValidator, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(addSoldToValidatorJP, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(addLengthValidatorForWorkNo, [ '760' ], null, true);
  GEOHandler.registerValidator(addLengthValidatorForPC, [ '760' ], null, true);
  GEOHandler.registerValidator(checkEstabFuncCdMandatory, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(addRequestForValidator, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(addProductTypeValidator, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(addDPLCheckValidatorJP, GEOHandler.JP, GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addFailedDPLValidatorJP, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(addINACValidator, GEOHandler.JP, null, true);
  // GEOHandler.registerValidator(ofcdJsicMismatchValidatorJP, GEOHandler.JP,
  // GEOHandler.ROLE_REQUESTER, true);
  GEOHandler.registerValidator(addressDuplicateValidator, GEOHandler.JP, null, true);
  GEOHandler.registerValidator(ROLValidatorForZC01, GEOHandler.JP, null, true);
  // GEOHandler.registerValidator(addAddressRecordsValidatorJP, GEOHandler.JP,
  // GEOHandler.ROLE_PROCESSOR, true);

  // skip byte checks
  FormManager.skipByteChecks([ 'dept', 'office', 'custNm1', 'custNm2', 'custNm4', 'addrTxt', 'bldg', 'contact', 'postCd', 'email2' ]);

});
