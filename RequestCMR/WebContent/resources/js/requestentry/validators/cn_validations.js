/**
 * 
 */
/* Register IERP Javascript */
var _vatExemptHandler = null;
var _scenarioSubTypeHandler = null;
var _deClientTierHandler = null;
var _addrTypeCNHandler = [];
var _addrTypesForCN = [ 'ZS01', 'ZP01', 'ZI01', 'ZD01' ];
var _landCntryHandler = null;
var _isuHandler = null;
var _searchTermHandler = null;
// var _govTypeHandler = null;
var _isicHandlerCN = null;
var _inacCdHandler = null;
var CNHandler = {
  CN_NAME1_MAX_BYTE_LEN : 70,
  CN_NAME2_MAX_BYTE_LEN : 70,
  CN_ADDRTXT_MAX_BYTE_LEN : 70,
  CN_ADDRTXT2_MAX_BYTE_LEN : 70,

  lengthInUtf8Bytes : function(str) {
    var m = encodeURIComponent(str).match(/%[89ABab]/g);
    console.log(str + " >>> " + (str.length + (m ? m.length : 0)));
    return str.length + (m ? m.length : 0);
  },

  encode_utf8 : function(s) {
    return unescape(encodeURIComponent(s));
  },

  substr_utf8_bytes : function(str, startInBytes, lengthInBytes) {

    var resultStr = '';
    var startInChars = 0;

    for (bytePos = 0; bytePos < startInBytes; startInChars++) {
      ch = str.charCodeAt(startInChars);
      bytePos += (ch < 128) ? 1 : CNHandler.encode_utf8(str[startInChars]).length;
    }

    end = startInChars + lengthInBytes - 1;

    for (n = startInChars; startInChars <= end; n++) {
      // get numeric code of character (is >128 for multibyte character)
      // and decrease "end" for each byte of the character sequence
      ch = str.charCodeAt(n);
      end -= (ch < 128) ? 1 : CNHandler.encode_utf8(str[n]).length;

      resultStr += str[n];
    }

    return resultStr;
  },

  hasDoubleByte : function(str) {
    console.log(">> RUN hasDoubleByte");
    console.log(">> validate > " + str);
    for (var i = 0, n = str.length; i < n; i++) {
      if (str.charCodeAt(i) > 255) {
        return true;
      }
    }
    return false;
  }

};

function setSearchTermList(){
  var custSubType = FormManager.getActualValue('custSubGrp');
  if (custSubType == 'NRMLD') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00260','04749','04629','04630','10158','10159','10160','10161',
      '10162','10163','10164','10165','10166','10167','10168','10169','10170','10171' ]);
  } else if (custSubType == 'NRMLC') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00000','10216','10217','10218','10219','10220','10221','10222',
      '10223','10224','10225','10226','10227','10228','10229','10230','10231','10232','10233','10234','10235','10236','10237','10238',
      '10239','10240','10241','10242','10243','10244','10245','10246','10247','10248','10249','10250','10251','10252','10253','10254',
      '10255','10256','10257','10258','10259','10260' ]);
    $("#cnsearchterminfoSpan").show();
  //  FormManager.readOnly('searchTerm');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
  } else if (custSubType == 'CROSS') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00000','00260','04749','04629','04630','08036','09058','10216',
      '10217','10218','10219','10220','10221','10222','10223','10224','10225','10226','10227','10228','10229','10230','10231','10232',
      '10233','10234','10235','10236','10237','10238','10239','10240','10241','10242','10243','10244','10245','10246','10247','10248',
      '10249','10250','10251','10252','10253','10254','10255','10256','10257','10258','10259','10260','10158','10159','10160','10161',
      '10162','10163','10164','10165','10166','10167','10168','10169','10170','10171'
 ]);
    if(FormManager.getActualValue('searchTerm') != undefined && FormManager.getActualValue('searchTerm') == ''){
      FormManager.setValue('searchTerm', '00000');
    }
  } else if (custSubType == 'KYND') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '09058' ]);
    FormManager.setValue('searchTerm', '09058');
  } else if (custSubType == 'EMBSA') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '09058','00260','04749','04629','04630','08036','10216','10217',
      '10218','10219','10220','10221','10222','10223','10224','10225','10226','10227','10228','10229','10230','10231','10232','10233',
      '10234','10235','10236','10237','10238','10239','10240','10241','10242','10243','10244','10245','10246','10247','10248','10249',
      '10250','10251','10252','10253','10254','10255','10256','10257','10258','10259','10260','10545','10546','10547','10548','10549',
      '10550','10551','10552','10553','10554','10555','10556','10557','10558','10559','10560','10561','10562','10563','10564','10565',
      '10566','10567','10568','10569','10570','10571','10572','10573','10574','10575','10576','10577','10578','10579','10580','10581',
      '10582','10583','10584','10585','10586','10587','10588','10589','10158','10159','10160','10161','10162','10163','10164','10165',
      '10166','10167','10168','10169','10170','10171'
 ]);
  } else if (custSubType == 'AQSTN') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00000','10216','10217','10218','10219','10220','10221','10222',
      '10223','10224','10225','10226','10227','10228','10229','10230','10231','10232','10233','10234','10235','10236','10237','10238',
      '10239','10240','10241','10242','10243','10244','10245','10246','10247','10248','10249','10250','10251','10252','10253','10254',
      '10255','10256','10257','10258','10259','10260'
 ]);
    $("#cnsearchterminfoSpan").show();
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
  } else if (custSubType == 'ECOSY') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '08036','10545','10546','10547','10548','10549','10550',
      '10551','10552','10553','10554','10555','10556','10557','10558','10559','10560','10561','10562','10563','10564','10565','10566',
      '10567','10568','10569','10570','10571','10572','10573','10574','10575','10576','10577','10578','10579','10580','10581','10582',
      '10583','10584','10585','10586','10587','10588','10589' ]);
    $("#cnsearchterminfoSpan").show();
    FormManager.readOnly('searchTerm');
    FormManager.readOnly('clientTier');
    FormManager.readOnly('isuCd');
  } else if (custSubType == 'MRKT') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00000' ]);
    FormManager.setValue('searchTerm', '00000');
  }
  else if (custSubType == 'BLUMX') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00000' ]);
    FormManager.setValue('searchTerm', '00000');
  }
  else if (custSubType == 'BUSPR') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00075' ]);
    FormManager.setValue('searchTerm', '00075');
  }
  else if (custSubType == 'INTER') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00000' ]);
    FormManager.setValue('searchTerm', '00000');
  }
  else if (custSubType == 'PRIV') {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '00000' ]);
    FormManager.setValue('searchTerm', '00000');
  }
}

function setSearchTermByGBGId() {
  var _GBGId = FormManager.getActualValue('gbgId');
  var gbgFlag = false;
  if (FormManager.getActualValue('gbgId') != undefined && FormManager.getActualValue('gbgId') != '') {
  var ret = cmr.query('CHECK_CN_S1_GBG_ID_LIST', {
    ID : _GBGId
  });
  if (ret && ret.ret1 && ret.ret1 != 0) {
    FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ '09058','00260','04749','04629','04630','08036','10216','10217',
      '10218','10219','10220','10221','10222','10223','10224','10225','10226','10227','10228','10229','10230','10231','10232','10233',
      '10234','10235','10236','10237','10238','10239','10240','10241','10242','10243','10244','10245','10246','10247','10248','10249',
      '10250','10251','10252','10253','10254','10255','10256','10257','10258','10259','10260','10545','10546','10547','10548','10549',
      '10550','10551','10552','10553','10554','10555','10556','10557','10558','10559','10560','10561','10562','10563','10564','10565',
      '10566','10567','10568','10569','10570','10571','10572','10573','10574','10575','10576','10577','10578','10579','10580','10581',
      '10582','10583','10584','10585','10586','10587','10588','10589','10158','10159','10160','10161','10162','10163','10164','10165',
      '10166','10167','10168','10169','10170','10171'
    ]);
    if(_GBGId == 'GB000YEN'){
      FormManager.setValue('searchTerm', '00260');
    }else if(_GBGId == 'GB001A7X'){
      FormManager.setValue('searchTerm', '04629');
    }else if(_GBGId == 'GB001CQ3'){
      FormManager.setValue('searchTerm', '04630');
    }else if(_GBGId == 'GB001CPY'){
      FormManager.setValue('searchTerm', '10158');
    }else if(_GBGId == 'GB001CPW'){
      FormManager.setValue('searchTerm', '10159');
    }else if(_GBGId == 'GB001DR4'){
      FormManager.setValue('searchTerm', '10160');
    }else if(_GBGId == 'GB001B83'){
      FormManager.setValue('searchTerm', '10161');
    }else if(_GBGId == 'GB001CQ2'){
      FormManager.setValue('searchTerm', '10162');
    }else if(_GBGId == 'GB001J73'){
      FormManager.setValue('searchTerm', '10163');
    }else if(_GBGId == 'GB0018BN'){
      FormManager.setValue('searchTerm', '10164');
    }else if(_GBGId == 'GB001A89'){
      FormManager.setValue('searchTerm', '10165');
    }else if(_GBGId == 'GB001AUJ'){
      FormManager.setValue('searchTerm', '10166');
    }else if(_GBGId == 'GB0018BS'){
      FormManager.setValue('searchTerm', '10167');
    }else if(_GBGId == 'GB0018EZ'){
      FormManager.setValue('searchTerm', '10168');
    }else if(_GBGId == 'GB227QFM'){
      FormManager.setValue('searchTerm', '10169');
    }else if(_GBGId == 'GB0019BN'){
      FormManager.setValue('searchTerm', '10170');
    }else if(_GBGId == 'GB0018X2'){
      FormManager.setValue('searchTerm', '10171');
    }else if(_GBGId == 'GB300S7F'){
      var zs01ReqId = FormManager.getActualValue('reqId');
      if (zs01ReqId != undefined && zs01ReqId != '') {
        qParams = {
            REQ_ID : zs01ReqId,
          };
          var record = cmr.query('GETZS01STATEBYREQID', qParams);
          var zs01State = record.ret1;
          if (zs01State != '') {
            if(zs01State == 'GZ'||zs01State == 'YN'||zs01State == 'GD'||zs01State == 'SC'||zs01State == 'CQ'||zs01State == 'GX'||zs01State == 'HI'){
              FormManager.setValue('searchTerm', '04749');
              FormManager.setValue('bgId', 'DB002KDH');
              FormManager.setValue('bgDesc', 'RCCB SOUTH');
            }else if(zs01State == 'NM'||zs01State == 'SN'||zs01State == 'HE'||zs01State == 'LN'||zs01State == 'NX'||zs01State == 'BJ'||zs01State == 'GS'||
                zs01State == 'QH'||zs01State == 'HA'||zs01State == 'TJ'||zs01State == 'HL'||zs01State == 'XJ'||zs01State == 'JL'||zs01State == 'XZ'||zs01State == 'SX'){
              FormManager.setValue('searchTerm', '04749');
              FormManager.setValue('bgId', 'DB002CBD');
              FormManager.setValue('bgDesc', 'RCCB NORTH');
            }else if(zs01State == 'JS'||zs01State == 'JX'||zs01State == 'AH'){
              FormManager.setValue('searchTerm', '04749');
              FormManager.setValue('bgId', 'DB002C9T');
              FormManager.setValue('bgDesc', 'RCCB EAST1');
            }else if(zs01State == 'ZJ'||zs01State == 'HB'||zs01State == 'HN'||zs01State == 'SH'||zs01State == 'FJ'||zs01State == 'SD'){
              FormManager.setValue('searchTerm', '04749');
              FormManager.setValue('bgId', 'DB002CF1');
              FormManager.setValue('bgDesc', 'RCCB EAST2');
            }
          }
          // FormManager.readOnly('searchTerm');
      }
    }
    if(FormManager.getActualValue('searchTerm') != undefined && FormManager.getActualValue('searchTerm') != ''){
      FormManager.readOnly('searchTerm');
    }    
  }else{
    gbgFlag = true;
  }
  var mandt = FormManager.getActualValue('mandt');
  var ret = cmr.query('CHECK_CN_INAC_BY_GBG_ID', {
    MANDT : mandt,
    ID : _GBGId
  });
  if (ret && ret.ret1 && ret.ret1 != '') {
    var inacArr = ret.ret1;
    var inacResult = inacArr.split('_');
    if(inacResult && inacResult.length>2){
      if(inacResult[inacResult.length - 2] == 'INAC'){
        FormManager.setValue('inacType', 'I');
        FormManager.setValue('inacCd', inacResult[inacResult.length - 1] );
      }else if(inacResult[inacResult.length - 2] == 'NAC'){
        FormManager.setValue('inacType', 'N');
        FormManager.setValue('inacCd', inacResult[inacResult.length - 1] );
      }
      FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
      FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
    } 
  }

  }
  
  var glcCode = FormManager.getActualValue('geoLocationCd');
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  if(custSubGrp=='EMBSA' && glcCode != undefined && glcCode != '' && (gbgFlag || FormManager.getActualValue('gbgId') == undefined || FormManager.getActualValue('gbgId') == '')){
    var oldSearchTerm = FormManager.getActualValue('searchTerm');
    var indc = 'C';
    var result1 = cmr.query('GLC.CN.SEARCHTERM', {
      GLC_CD : '%' + glcCode + '%',
      DEFAULT_INDC : indc
    });
    indc = 'E';
    var result2 = cmr.query('GLC.CN.SEARCHTERM', {
      GLC_CD : '%' + glcCode + '%',
      DEFAULT_INDC : indc
    });
    if (result1 != null && result1.ret1 != undefined && result1.ret1 != '' || result2 != null && result2.ret1 != undefined && result2.ret1 != '') {
      var searchTerm1 = result1 != null ? result1.ret1 : '';
      var searchTerm2 = result2 != null ? result2.ret1 : '';
      var clientTier1 = result1.ret2;
      var clientTier2 = result2.ret2;
      var isuCd1 = result1.ret3;
      var isuCd2 = result2.ret3;
      FormManager.limitDropdownValues(FormManager.getField('searchTerm'), [ searchTerm1, searchTerm2 ]);
      if(oldSearchTerm == searchTerm2){
        FormManager.setValue('searchTerm', searchTerm2);
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ clientTier2 ]);
        FormManager.setValue('clientTier', clientTier2);
        FormManager.readOnly('clientTier');
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ isuCd2 ]);
        FormManager.setValue('isuCd', isuCd2);
        FormManager.readOnly('isuCd');
      }else{
        FormManager.setValue('searchTerm', searchTerm1);
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), [ clientTier1 ]);
        FormManager.setValue('clientTier', clientTier1);
        FormManager.readOnly('clientTier');
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), [ isuCd1 ]);
        FormManager.setValue('isuCd', isuCd1);
        FormManager.readOnly('isuCd');
      }
    }
  }

// else{
// FormManager.setValue('inacCd', '');
// FormManager.readOnly('inacCd');
// FormManager.setValue('inacType', '');
// FormManager.readOnly('inacType');
// }
}

function afterConfigForCN() {
  if (FormManager.getActualValue('isicCd') != undefined && FormManager.getActualValue('isicCd') != '') {
    FormManager.readOnly('isicCd');
    var custSubType = FormManager.getActualValue('custSubGrp');
    var isicCd = FormManager.getField('isicCd');
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER" && FormManager.getActualValue('reqType') == 'C') {
      if(custSubType == 'CROSS' || custSubType == 'NRMLC' || custSubType == 'KYND' || custSubType == 'NRMLD' ||custSubType == 'EMBSA' 
        || custSubType == 'AQSTN' || custSubType == 'ECOSY' || custSubType == 'MRKT' || custSubType == 'BLUMX') {
        if (isicCd == '0000' || isicCd == '8888' || isicCd == '9500') {
          FormManager.enable('isicCd');
        }
      }
    }
  }
  var custSubT = FormManager.getActualValue('custSubGrp');
  if (_pagemodel.userRole.toUpperCase() == "REQUESTER" && FormManager.getActualValue('reqType') == 'C') {
    setSearchTermList();
  }
  if (_pagemodel.userRole.toUpperCase() == "REQUESTER" && FormManager.getActualValue('reqType') == 'C' && (custSubT == 'CROSS' || custSubT == 'NRMLD' || custSubT == 'EMBSA')) {
    setSearchTermByGBGId();
  }
  
  if (_isicHandlerCN == null) {
    _isicHandlerCN = dojo.connect(FormManager.getField('isicCd'), 'onChange', function(value) {
      setIsuOnIsic();
    });
  }
  
  if (_searchTermHandler == null) {
    _searchTermHandler = dojo.connect(FormManager.getField('searchTerm'), 'onChange', function(value) {
      console.log(">>> RUNNING SORTL HANDLER!!!!");
      if (!value) {
        return;
      }
      filterISUOnChange();
      setIsuOnIsic();
      setInacBySearchTerm();
      // addValidationForParentCompanyNo();
    });
  }

  if (_vatExemptHandler == null) {
    _vatExemptHandler = dojo.connect(FormManager.getField('vatExempt'), 'onClick', function(value) {
      setSocialCreditCdValidator();
    });
  }

// if (_govTypeHandler == null) {
// _govTypeHandler = dojo.connect(FormManager.getField('govType'), 'onClick',
// function(value) {
// addValidationForParentCompanyNo();
// });
// }

  
  if (_isuHandler == null) {
    _isuHandler = dojo.connect(FormManager.getField('isuCd'), 'onChange', function(value) {
      setCtcOnIsuCdChangeCN();
    });
  }

  if (FormManager.getActualValue('reqType') == 'U') {
    FormManager.disable('dnbSearchBtn');
    dojo.removeClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-pri');
    dojo.addClass(dojo.byId('dnbSearchBtn'), 'ibm-btn-cancel-disabled');
    
    FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
      /*
       * Removing hiding custClass because it is setting it to null when saving
       * from requester view. For the time being we should set it to read only
       * for updates.
       */
      // FormManager.hide('ClassCode', 'custClass');
      FormManager.readOnly('custClass');
      FormManager.resetValidations('custClass');
      FormManager.hide('InterAddrKey', 'cnInterAddrKey');
      FormManager.resetValidations('cnInterAddrKey');
    } else {
      FormManager.readOnly('custClass');
    }
  }
  FormManager.show('DisableAutoProcessing', 'disableAutoProc');

  if (_landCntryHandler == null) {
    _landCntryHandler = dojo.connect(FormManager.getField('landCntry'), 'onChange', function(value) {
      showHideCityCN();
    });
  }

  if (_landCntryHandler && _landCntryHandler[0]) {
    _landCntryHandler[0].onChange();
  }

  if (_searchTermHandler && _searchTermHandler[0]) {
    _searchTermHandler[0].onChange();
  }
// resetISICCode();
  updateBPSearchTerm();
}

function setReadOnly4Update(){
  if (FormManager.getActualValue('reqType') != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (FormManager.getActualValue('reqType') == 'U') {
    window.setTimeout('setReadOnly4Update()', 1);
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
      FormManager.readOnly('isicCd');

      if(FormManager.getField('capInd').checked == true){
        FormManager.readOnly('subIndustryCd');
        FormManager.readOnly('searchTerm');
        FormManager.readOnly('isuCd');
        FormManager.readOnly('clientTier');
        FormManager.readOnly('inacType');
        FormManager.readOnly('inacCd');
        FormManager.readOnly('company');
      }else{
        FormManager.readOnly('searchTerm');
        FormManager.readOnly('isuCd');
        FormManager.readOnly('clientTier');
        FormManager.readOnly('inacType');
        FormManager.readOnly('inacCd');
      }
      FormManager.removeValidator('inacType', Validators.REQUIRED);
    }
    
  }
}

function updateBPSearchTerm() {
  var ppsceidBP = FormManager.getActualValue('ppsceid');
  var searchTerm = FormManager.getActualValue('searchTerm');
  var cmrNo = FormManager.getActualValue('cmrNo');
  var regString = /[^0-9]+/;
  if (FormManager.getActualValue('reqType') == 'U' && _pagemodel.userRole.toUpperCase() == 'REQUESTER' &&  ppsceidBP != undefined && ppsceidBP != null && ppsceidBP != ''){
    if (searchTerm == '00075') {
      FormManager.readOnly('searchTerm');
      FormManager.readOnly('isuCd');
      FormManager.readOnly('clientTier');
      FormManager.readOnly('inacType');
      FormManager.readOnly('inacCd');
    } else if (searchTerm == null || searchTerm == '' || searchTerm == '00000' || regString.test(searchTerm)) {
      if (cmrNo.startsWith('1') || cmrNo.startsWith('2')){
        FormManager.setValue('searchTerm', '00075');
        FormManager.setValue('clientTier', 'Z');
        FormManager.readOnly('searchTerm');
        FormManager.readOnly('clientTier');
        FormManager.readOnly('isuCd');
        FormManager.readOnly('inacType');
        FormManager.readOnly('inacCd');
      }
    }
  } else if (FormManager.getActualValue('reqType') == 'U' && _pagemodel.userRole.toUpperCase() == 'REQUESTER' && (ppsceidBP == undefined || ppsceidBP == null || ppsceidBP == '')){
    if (searchTerm == null || searchTerm.trim() == '' || searchTerm == '00000' || searchTerm == '000000' || regString.test(searchTerm)) {
      if (cmrNo.startsWith('1') || cmrNo.startsWith('2')){
        FormManager.setValue('searchTerm', '00075');
        FormManager.readOnly('searchTerm');
      }
    }
  }
}

function setCtcOnIsuCdChangeCN() {
  isuCd = FormManager.getActualValue('isuCd');
  if (isuCd == '5K') {
    FormManager.resetValidations('clientTier');
    FormManager.removeValidator('clientTier', Validators.REQUIRED);
  } else {
    if (FormManager.getActualValue('reqType') == 'U') {
      FormManager.removeValidator('clientTier', Validators.REQUIRED);
    } else {
      FormManager.addValidator('clientTier', Validators.REQUIRED);
    }
    FormManager.enable('clientTier');
  }
}

function setInacBySearchTerm() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var searchTerm = FormManager.getActualValue('searchTerm');
 
  if (FormManager.getActualValue('reqType') == 'C' && searchTerm != undefined && searchTerm != '') {
    FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ], 'MAIN_IBM_TAB');
    FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ], 'MAIN_IBM_TAB');
    var qParams = {
        _qall : 'Y',
        ISSUING_CNTRY : cntry,
        CMT : '%' + searchTerm + '%'
      };
      var inacList = cmr.query('GET.INAC_BY_CLUSTER', qParams);
      if (inacList != null && inacList.length > 0) {
        var inacTypeSelected ='';
        var arr =  inacList.map(inacList => inacList.ret1);
        inacTypeSelected  =  inacList.map(inacList => inacList.ret2);
        FormManager.limitDropdownValues(FormManager.getField('inacCd'), arr);
        if (inacList.length == 1) {
          FormManager.setValue('inacCd', arr[0]);
        }       
        if (inacType != '' && inacTypeSelected[0].includes(",I") && !inacTypeSelected[0].includes(',IN')) {
          FormManager.limitDropdownValues(FormManager.getField('inacType'), 'I');
          FormManager.setValue('inacType', 'I');
        } else if (inacType != '' && inacTypeSelected[0].includes(',N')) {
          FormManager.limitDropdownValues(FormManager.getField('inacType'), 'N');
          FormManager.setValue('inacType', 'N');
        } else if(inacType != '' && inacTypeSelected[0].includes(',IN')){
          FormManager.resetDropdownValues(FormManager.getField('inacType'));
          var value = FormManager.getField('inacType');
          var cmt = (value==''?'%':value) + ','+ searchTerm +'%';
          var value = FormManager.getActualValue('inacType');
          var cntry = FormManager.getActualValue('cmrIssuingCntry');
            console.log(value);
            if (value != null) {
              var inacCdValue = [];
              var qParams = {
                _qall : 'Y',
                ISSUING_CNTRY : cntry ,
                CMT : cmt ,
               };
              var results = cmr.query('GET.INAC_CD', qParams);
              if (results != null && results.length > 0) {
                for (var i = 0; i < results.length; i++) {
                  inacCdValue.push(results[i].ret1);
                }
                FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
                if (inacCdValue.length == 1) {
                  FormManager.setValue('inacCd', inacCdValue[0]);
                }
              }
              if (inacCdValue.length == 0) {
                FormManager.setValue('inacType', '');
                FormManager.resetDropdownValues(FormManager.getField('inacCd'));
                FormManager.resetDropdownValues(FormManager.getField('inacType'));
                FormManager.removeValidator('inacCd', Validators.REQUIRED);
                FormManager.removeValidator('inacType', Validators.REQUIRED);
              }
            }
        } else {
          FormManager.resetDropdownValues(FormManager.getField('inacType'));
        }
    }else{
      FormManager.readOnly('inacType');
      FormManager.readOnly('inacCd');
      FormManager.resetDropdownValues(FormManager.getField('inacCd'));
      FormManager.resetDropdownValues(FormManager.getField('inacType'));
      FormManager.removeValidator('inacCd', Validators.REQUIRED);
      FormManager.removeValidator('inacType', Validators.REQUIRED);
    }
    addSearchTerm04687Logic();
  } 
// else {
// if(_GBGId != 'undefined' && _GBGId != ''){
// var mandt = FormManager.getActualValue('mandt');
// var ret = cmr.query('CHECK_CN_INAC_BY_GBG_ID', {
// MANDT : mandt,
// ID : _GBGId
// });
// if (ret && ret.ret1 && ret.ret1 != '') {
// var inacArr = ret.ret1;
// var inacResult = inacArr.split('_');
// if(inacResult && inacResult.length>2){
// if(inacResult[inacResult.length - 2] == 'INAC'){
// FormManager.setValue('inacType', 'I');
// FormManager.setValue('inacCd', inacResult[inacResult.length - 1] );
// }else if(inacResult[inacResult.length - 2] == 'NAC'){
// FormManager.setValue('inacType', 'N');
// FormManager.setValue('inacCd', inacResult[inacResult.length - 1] );
// }
// FormManager.addValidator('inacCd', Validators.REQUIRED, [ 'INAC/NAC Code' ],
// 'MAIN_IBM_TAB');
// FormManager.addValidator('inacType', Validators.REQUIRED, [ 'INAC Type' ],
// 'MAIN_IBM_TAB');
// }
// }else{
// FormManager.resetDropdownValues(FormManager.getField('inacCd'));
// FormManager.resetDropdownValues(FormManager.getField('inacType'));
// FormManager.removeValidator('inacCd', Validators.REQUIRED);
// FormManager.removeValidator('inacType', Validators.REQUIRED);
// }
// }else{
// FormManager.resetDropdownValues(FormManager.getField('inacCd'));
// FormManager.resetDropdownValues(FormManager.getField('inacType'));
// FormManager.removeValidator('inacCd', Validators.REQUIRED);
// FormManager.removeValidator('inacType', Validators.REQUIRED);
// }
// return;
// }
}

function setIsuOnIsic() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }

  var searchTerm = FormManager.getActualValue('searchTerm');
  if (!(searchTerm == '04687' || searchTerm == '04488' || searchTerm == '04630' || searchTerm == '04472' || searchTerm == '00260' || searchTerm == '04480' || searchTerm == '04484'
      || searchTerm == '04486' || searchTerm == '04491' || searchTerm == '04493' || searchTerm == '04495' || searchTerm == '04497' || searchTerm == '04499' || searchTerm == '04502'
      || searchTerm == '04629' || searchTerm == '04689' || searchTerm == '04489' || searchTerm == '04747' || searchTerm == '04748' || searchTerm == '04749')) {
    return;
  }

  var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var isicCd = FormManager.getActualValue('isicCd');

  var ISU = [];
  if (isicCd != '') {
    var qParams = {
      _qall : 'Y',
      ISSUING_CNTRY : cmrIssuingCntry,
      REP_TEAM_CD : '%' + isicCd + '%'
    };
    var results = cmr.query('GET.ISULIST.BYISIC', qParams);
    if (results != null) {
      for (var i = 0; i < results.length; i++) {
        ISU.push(results[i].ret1);
      }
      if (ISU != null) {
        FormManager.limitDropdownValues(FormManager.getField('isuCd'), ISU);
        if (ISU.length >= 1) {
          FormManager.setValue('isuCd', ISU[0]);
        }
      }
    }
    FormManager.readOnly('isicCd');
    var custSubType = FormManager.getActualValue('custSubGrp');
    var isicCd = FormManager.getField('isicCd');
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER" && FormManager.getActualValue('reqType') == 'C') {
      if(custSubType == 'CROSS' || custSubType == 'NRMLC' || custSubType == 'NRMLD' || custSubType == 'KYND' || custSubType == 'EMBSA' 
        || custSubType == 'AQSTN' || custSubType == 'ECOSY' || custSubType == 'MRKT' || custSubType == 'BLUMX') {
        if (isicCd == '0000' || isicCd == '8888' || isicCd == '9500') {
          FormManager.enable('isicCd');
        }
      }
    }
  }
}

function onInacTypeChange() {
  var searchTerm = FormManager.getActualValue('searchTerm');
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType == 'C') {
    var custSubT = FormManager.getActualValue('custSubGrp');
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER" && _inacCdHandler == null && searchTerm != undefined && searchTerm != '') {
      _inacCdHandler = dojo.connect(FormManager.getField('inacType'), 'onChange', function(value) {
        var cmt = value + ','+ searchTerm +'%';
        var cntry = FormManager.getActualValue('cmrIssuingCntry');
          console.log(value);
          if (value != null) {
            var inacCdValue = [];
              var qParams = {
              _qall : 'Y',
              ISSUING_CNTRY : cntry ,
              CMT : cmt ,
              };

            if(qParams != undefined){
              var results = cmr.query('GET.INAC_CD', qParams);
              if (results != null) {
                for (var i = 0; i < results.length; i++) {
                  inacCdValue.push(results[i].ret1);
                }
                FormManager.limitDropdownValues(FormManager.getField('inacCd'), inacCdValue);
                if (inacCdValue.length == 1) {
                  FormManager.setValue('inacCd', inacCdValue[0]);
                }
              }
            }
          }
      });
    }
  }
}

function addSearchTerm04687Logic() {
  if (FormManager.getActualValue('searchTerm') == '04687'){
    if (FormManager.getActualValue('searchTerm') != _pagemodel.searchTerm) {
      FormManager.setValue('inacCd', 'XXXX');
      FormManager.setValue('inacType', 'I');
    }
  }
}

function autoSetIBMDeptCostCenter() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  var _custClass = FormManager.getActualValue('custClass');
  if (FormManager.getActualValue('cmrIssuingCntry') == '641' && FormManager.getActualValue('reqType') == 'C') {
    if (_custSubGrp != undefined && _custClass != undefined && _custClass != '') {
      if (_custSubGrp == 'INTAM' || _custSubGrp == 'INTSO' || _custSubGrp == 'INTIN') {
        FormManager.show('IbmDeptCostCenter', 'ibmDeptCostCenter');
        if (_custClass == '81') {
          FormManager.addValidator('ibmDeptCostCenter', Validators.REQUIRED, [ 'IbmDeptCostCenter' ], 'MAIN_IBM_TAB');
        } else if (_custClass == '85') {
          FormManager.resetValidations('ibmDeptCostCenter');
        }
      } else {
        FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
      }
    } else {
      FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
    }
  } else {
    FormManager.hide('IbmDeptCostCenter', 'ibmDeptCostCenter');
  }

}

function defaultCapIndicator() {
  if (FormManager.getActualValue('reqType') == 'C') {
    FormManager.getField('capInd').checked = true;
    FormManager.readOnly('capInd');
  }
}

function defaultGovernmentIndicator(){
  FormManager.getField('govType').checked = true;
  FormManager.readOnly('govType');
}

function disableVatExemptForScenarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  // setValuesForScenarios();
  if (_custSubGrp != undefined && _custSubGrp != '') {
    if (_custSubGrp == 'INTER'  ||  _custSubGrp == 'PRIV' || _custSubGrp == 'CROSS') {
      FormManager.disable('vatExempt');
      FormManager.removeValidator('busnType', Validators.REQUIRED);
    } else {
      FormManager.enable('vatExempt');
      setSocialCreditCdValidator();
    }
  }
}

function setSocialCreditCdValidator(){
  if (dojo.byId('vatExempt').checked ) {
    console.log(">>> process Social Credit Code remove * >> ");
    FormManager.resetValidations('busnType');
  } else {
    console.log(">>> process Social Credit Code add * >> ");
    FormManager.addValidator('busnType', Validators.REQUIRED, [ 'Social Credit Code' ], 'MAIN_CUST_TAB');
  }
}

function setPrivacyIndcReqdForProc() {
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR" && FormManager.getActualValue('reqType') == 'C') {
    FormManager.addValidator('privIndc', Validators.REQUIRED, [ 'PrivacyIndc' ], 'MAIN_CUST_TAB');
  }
}

function filterISUOnChange() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  console.log(">>> RUNNING filterISUOnChange!!");
  var searchTerm = FormManager.getActualValue('searchTerm');
  var searchTermParams = {
    SORTL : searchTerm,
    KATR6 : '641'
  };
  var isuCdResult = cmr.query('GET.MAPPED_ISU_BY_SORTL', searchTermParams);

  if (isuCdResult != null) {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
    FormManager.setValue('isuCd', isuCdResult.ret2);
  }

  searchTermParams['_qall'] = 'Y';
  // FormManager.readOnly('isuCd');
  // var isuCd = isuCdResult.ret2;
  var clientTier = null;

  var mappedCtc = cmr.query('GET.MAPPED_CTC_BY_ISU', searchTermParams);
  if (mappedCtc && mappedCtc.length > 0) {
    clientTier = [];
    mappedCtc.forEach(function(ctc, index) {
      clientTier.push(ctc.ret1);
    });
  }

  // if (isuCd == "04") {
  // // filter the drop down
  // clientTier = [ 'BL', '7' ];
  // } else if (isuCd == "21") {
  // // filter the drop down
  // clientTier = [ 'Z' ];
  // } else if (isuCd == "32") {
  // // filter the drop down
  // if (searchTerm == '06757' || searchTerm == '06978' || searchTerm == '06979'
  // || searchTerm == '06981' || searchTerm == '06967' || searchTerm == '06968'
  // || searchTerm == '06980') {
  // clientTier = [ 'N' ];
  // } else {
  // clientTier = [ 'T', 'S' ];
  // }
  // } else if (isuCd == "34") {
  // // filter the drop down
  // if (searchTerm == '04617' || searchTerm == '04615' || searchTerm == '04613'
  // || searchTerm == '05485' || searchTerm == '05486' || searchTerm == '05487'
  // || searchTerm == '04603'
  // || searchTerm == '04605' || searchTerm == '04608' || searchTerm == '04609'
  // || searchTerm == '05483' || searchTerm == '05484') {
  // clientTier = [ 'A' ];
  // } else if (searchTerm == '06968') {
  // clientTier = [ 'V' ];
  // } else {
  // clientTier = [ 'V' ];
  // }
  // } else if (isuCd == "3T") {
  // // filter the drop down
  // clientTier = [ 'BL', '7' ];
  // } else if (isuCd == "5E") {
  // // filter the drop down
  // clientTier = [ 'BL', '7' ];
  // } else if (isuCd == "8B") {
  // // filter the drop down
  // clientTier = [ 'Z' ];
  // }

  if (clientTier != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTier);
    FormManager.enable('clientTier');
    if (clientTier.length == 1) {
      FormManager.setValue('clientTier', clientTier[0]);
      if (isuCd == "8B" && FormManager.getActualValue('custSubGrp') == 'BUSPR') {
        FormManager.readOnly('clientTier');
      } else if (FormManager.getActualValue('custSubGrp') == 'ECOSY'){
        FormManager.readOnly('clientTier');
      }
      // FormManager.readOnly('clientTier');
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
  }

  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
    console.log("Enabling isuCd for PROCESSOR...");
    FormManager.enable('isuCd');
  }

  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    console.log("Disabling isuCd for REQUESTER...");
    FormManager.readOnly('isuCd');
  }

}

function filterClientTierOnChange() {
  console.log(">>> RUNNING filterClientTierOnChange!!");
  var isuCd = FormManager.getActualValue('isuCd');
  var searchTerm = FormManager.getActualValue('searchTerm');
  var clientTier = null;

  if (isuCd == "04") {
    // filter the drop down
    clientTier = [ 'BLANK' ];
  } else if (isuCd == "21") {
    // filter the drop down
    clientTier = [ 'Z' ];
  } else if (isuCd == "32") {
    // filter the drop down
    if (searchTerm == '06757' || searchTerm == '06978' || searchTerm == '06979' || searchTerm == '06981' || searchTerm == '06967' || searchTerm == '06968' || searchTerm == '06980') {
      clientTier = [ 'N' ];
    } else {
      clientTier = [ 'T', 'S' ];
    }
  } else if (isuCd == "34") {
    // filter the drop down
    if (searchTerm == '04617' || searchTerm == '04615' || searchTerm == '04613' || searchTerm == '05485' || searchTerm == '05486' || searchTerm == '05487') {
      clientTier = [ 'A' ];
    } else if (searchTerm == '06968') {
      clientTier = [ 'V' ];
    } else {
      clientTier = [ 'V', '6' ];
    }
  } else if (isuCd == "3T") {
    // filter the drop down
    clientTier = [ 'BLANK' ];
  } else if (isuCd == "5E") {
    // filter the drop down
    clientTier = [ 'BLANK' ];
  } else if (isuCd == "8B") {
    // filter the drop down
    clientTier = [ 'Z' ];
  }

  if (clientTier != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTier);
    FormManager.enable('clientTier');
    if (clientTier.length == 1) {
      FormManager.setValue('clientTier', clientTier[0]);
      // FormManager.readOnly('clientTier');
    }
  } else {
    FormManager.resetDropdownValues(FormManager.getField('isuCd'));
  }

  if (FormManager.getField('searchTerm') != undefined && FormManager.getField('searchTerm') != '') {
    FormManager.readOnly('isuCd');
  } else {
    if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
      console.log("Enabling isuCd for PROCESSOR...");
      FormManager.enable('isuCd');
    }

    if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
      console.log("Disabling isuCd for REQUESTER...");
      FormManager.readOnly('isuCd');
    }
  }
}

function limitClientTierValuesOnCreate() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'C') {
    return;
  }
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (_custSubGrp != undefined && _custSubGrp != '') {
    if (_custSubGrp == 'COMME' || _custSubGrp == 'BROKR' || _custSubGrp == 'GOVMT' || _custSubGrp == 'SENSI') {
      var clientTierValues = [ 'A', 'B', 'V', 'Z', '6', 'T', 'S', 'C', 'N' ];
      if (clientTierValues != null) {
        FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTierValues);
      } else {
        FormManager.resetDropdownValues(FormManager.getField('clientTier'));
      }
    }
  }
}

function limitClientTierValuesOnUpdate() {
  var reqType = null;
  reqType = FormManager.getActualValue('reqType');
  if (reqType != 'U') {
    return;
  }
  var clientTierValues = [ 'A', 'B', 'V', 'Z', '6', 'T', 'S', 'C', 'N' ];
  if (clientTierValues != null) {
    FormManager.limitDropdownValues(FormManager.getField('clientTier'), clientTierValues);
  } else {
    FormManager.resetDropdownValues(FormManager.getField('clientTier'));
  }

}
// Defect 1370022: By Mukesh
function canRemoveAddress(value, rowIndex, grid) {
  console.log("Remove address button..");
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  var rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;

  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    if (importInd == 'Y') {
      return false;
    } else {
      return true;
    }
  } else {
    var addrType = rowData.addrType;
    if (addrType == 'ZS01') {
      return false;
    }
    return true;
  }
}
function canUpdateAddress(value, rowIndex, grid) {
  return true;
}
function canCopyAddress(value, rowIndex, grid) {
  return true;
}
function ADDRESS_GRID_showCheck(value, rowIndex, grid) {
  console.log(value + ' - ' + rowIndex);
  var rowData = grid.getItem(0);
  if (rowData == null) {
    return '';
  }
  rowData = grid.getItem(rowIndex);
  var importInd = rowData.importInd;
  var reqType = FormManager.getActualValue('reqType');
  if (reqType == 'U') {
    if (importInd == 'Y') {
      return false;
    } else {
      return true;
    }
  } else {
    var addrType = rowData.addrType;
    if (addrType == 'ZS01') {
      return false;
    }
    return true;
  }
}

function setValuesForScenarios() {
  var _custSubGrp = FormManager.getActualValue('custSubGrp');

  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    FormManager.hide('InterAddrKey', 'cnInterAddrKey');
    FormManager.resetValidations('cnInterAddrKey');
  }

  if (FormManager.getActualValue('reqType') == 'C' && _custSubGrp != undefined && _custSubGrp != '') {
    if (FormManager.getActualValue('isicCd') != undefined && FormManager.getActualValue('isicCd') != '') {
      FormManager.readOnly('isicCd');
      var custSubType = FormManager.getActualValue('custSubGrp');
      var isicCd = FormManager.getField('isicCd');
      if (_pagemodel.userRole.toUpperCase() == "REQUESTER" && FormManager.getActualValue('reqType') == 'C') {
        if(custSubType == 'CROSS' || custSubType == 'NRMLC' || custSubType == 'NRMLD'  || custSubType == 'KYND' || custSubType == 'EMBSA' 
          || custSubType == 'AQSTN' || custSubType == 'ECOSY' || custSubType == 'MRKT' || custSubType == 'BLUMX') {
          if (isicCd == '0000' || isicCd == '8888' || isicCd == '9500') {
            FormManager.enable('isicCd');
          }
        }
      }
    }
    if (_custSubGrp == 'INTER') {
      FormManager.show('ClassCode', 'custClass');
      var field = FormManager.getField('custClass');
      FormManager.limitDropdownValues(field, [ '81', '85' ]);
    }
    if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
      // if (_custSubGrp == 'BUSPR') {
      // FormManager.hide('SearchTerm', 'searchTerm');
      // FormManager.setValue('searchTerm', '');

      // FormManager.setValue('clientTier', '');
      // FormManager.readOnly('clientTier');
      // FormManager.resetValidations('clientTier');
      // } else {
      // FormManager.show('SearchTerm', 'searchTerm');
      // }
      if (_custSubGrp == 'INTER') {
        FormManager.resetValidations('searchTerm');
        // FormManager.enable('clientTier');
        FormManager.readOnly('searchTerm');
        FormManager.resetValidations('isicCd');
      }
      if (_custSubGrp == 'NRMLC' || _custSubGrp == 'NRMLD'  || _custSubGrp == 'KYND' || _custSubGrp == 'ECOSY' || _custSubGrp == 'INTER' || _custSubGrp == 'AQSTN'|| _custSubGrp == 'PRIV' || _custSubGrp == 'EMBSA' || _custSubGrp == 'CROSS') {
        FormManager.hide('PPSCEID', 'ppsceid');
        FormManager.hide('MembLevel', 'memLvl');
        FormManager.hide('BPRelationType', 'bpRelType');
      } else {
        FormManager.show('PPSCEID', 'ppsceid');
        FormManager.show('MembLevel', 'memLvl');
        FormManager.show('BPRelationType', 'bpRelType');
      }

      if (_custSubGrp == 'AQSTN'|| _custSubGrp == 'PRIV' || _custSubGrp == 'CROSS' || _custSubGrp == 'INTER') {
        FormManager.resetValidations('cnCustName1');
        FormManager.resetValidations('cnAddrTxt');
        FormManager.resetValidations('cnCity');
      }
      if (_custSubGrp == 'ECOSY'){
        var _GBGId = FormManager.getActualValue('gbgId');
        if(_GBGId != undefined && _GBGId != ''){
          var ret = cmr.query('CHECK_CN_S1_GBG_ID_LIST', {
            ID : _GBGId
          });
          if (ret && ret.ret1 && ret.ret1 != 0) {
            cmr.showAlert("Please select Scenario Sub Type -'Normal - Signature / Strategic / Dedicated', as this CMR belongs to China Signature/Strategic/Dedicate account.", "Warning");
          }
        }
      }
    }

    if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
      if (_custSubGrp == 'NRMLC' || _custSubGrp == 'NRMLD'  || _custSubGrp == 'KYND' || _custSubGrp == 'ECOSY' || _custSubGrp == 'BUSPR' || _custSubGrp == 'INTER' || _custSubGrp == 'EMBSA' || _custSubGrp == 'BLUMX' || _custSubGrp == 'MRKT') {
        FormManager.setValue('cnInterAddrKey', '6');
        FormManager.addValidator('cnInterAddrKey', Validators.REQUIRED, [ 'InterAddrKey' ], '');
        FormManager.readOnly('cnInterAddrKey');
      } else {
        FormManager.resetValidations('cnInterAddrKey');
      }
    }
  }
}

function hideContactInfoFields() {
  
  var addrType = FormManager.getActualValue('addrType');
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (addrType != '' && addrType == 'ZS01' && _custSubGrp != undefined && _custSubGrp != null && _custSubGrp != '' && (_custSubGrp == 'NRMLC' || _custSubGrp == 'NRMLD'  || _custSubGrp == 'KYND' || _custSubGrp == 'ECOSY' || _custSubGrp == 'AQSTN' || _custSubGrp == 'EMBSA' )){
    // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
    FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
    FormManager.show('ChinaCustomerCntName', 'cnCustContNm');
    FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
    FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
    FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
  } else {
    // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
    FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
    FormManager.show('ChinaCustomerCntName', 'cnCustContNm');

    FormManager.resetValidations('cnCustContJobTitle');
    FormManager.resetValidations('cnCustContNm');
    FormManager.resetValidations('custPhone');
  }
}

function autoSetAddrFieldsForCN() {
  if (cmr.addressMode == 'newAddress' || cmr.addressMode == 'copyAddress' || cmr.addressMode =='updateAddress' ) {
    for (var i = 0; i < _addrTypesForCN.length; i++) {
      _addrTypeCNHandler[i] = null;
      if (_addrTypeCNHandler[i] == null) {
        _addrTypeCNHandler[i] = dojo.connect(FormManager.getField('addrType_' + _addrTypesForCN[i]), 'onClick', function(value) {
          hideContactInfoFields();
        });
      }
    }

  }

  if (cmr.addressMode == 'updateAddress') {
      var addrType = FormManager.getActualValue('addrType');
      var _custSubGrp = FormManager.getActualValue('custSubGrp');
      if (addrType != '' && addrType == 'ZS01' && _custSubGrp != undefined && _custSubGrp != null && _custSubGrp != '' && (_custSubGrp == 'NRMLC' || _custSubGrp == 'NRMLD'  || _custSubGrp == 'KYND' || _custSubGrp == 'ECOSY' || _custSubGrp == 'AQSTN' || _custSubGrp == 'EMBSA' )){
      // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
      FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
      FormManager.show('ChinaCustomerCntName', 'cnCustContNm');
      FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
      FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
      FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
    } else {
      // FormManager.hide('CustomerCntPhone2', 'cnCustContPhone2');
      FormManager.show('CustomerCntJobTitle', 'cnCustContJobTitle');
      FormManager.show('ChinaCustomerCntName', 'cnCustContNm');

      FormManager.resetValidations('cnCustContJobTitle');
      FormManager.resetValidations('cnCustContNm');
      FormManager.resetValidations('custPhone');
    }
  }
}

function setChinaChecklistStatus() {
  console.log('validating china checklist..');
  var checklist = dojo.query('table.checklist');
  document.getElementById("checklistStatus").innerHTML = "Not Done";
  var reqId = FormManager.getActualValue('reqId');
  var questions = checklist.query('input[type="radio"]');

  if (reqId != null && reqId.length > 0 && reqId != 0) {
    if (questions.length > 0) {
      var noOfQuestions = questions.length / 2;
      var checkCount = 0;
      for (var i = 0; i < questions.length; i++) {
        if (questions[i].checked) {
          checkCount++;
        }
      }
      if (noOfQuestions != checkCount) {
        document.getElementById("checklistStatus").innerHTML = "Incomplete";
        FormManager.setValue('checklistStatus', "Incomplete");
      } else {
        document.getElementById("checklistStatus").innerHTML = "Complete";
        FormManager.setValue('checklistStatus', "Complete");
      }
    } else {
      document.getElementById("checklistStatus").innerHTML = "Complete";
      FormManager.setValue('checklistStatus', "Complete");
    }
  }
}

function showHideCityCN() {
  console.log(">>> showHideCityCN for CN");
  var landCntryVal = FormManager.getActualValue('landCntry');
  var _custSubGrp = FormManager.getActualValue('custSubGrp');

  if (landCntryVal != 'CN' && landCntryVal != "") {
    FormManager.hide('DropDownCity', 'dropdowncity1');
    FormManager.resetValidations('dropdowncity1');
    FormManager.show('City1', 'city1');
    FormManager.addValidator('city1', Validators.REQUIRED, [ 'City' ], null);
    FormManager.resetValidations('cnCity');
    FormManager.resetValidations('cnAddrTxt');
    FormManager.resetValidations('cnCustName1');
    FormManager.resetValidations('stateProv');
    //
    FormManager.resetValidations('custPhone');
    FormManager.resetValidations('cnCustContJobTitle');
    FormManager.resetValidations('cnCustContNm');
  } else {
    FormManager.show('DropDownCity', 'dropdowncity1');
    FormManager.addValidator('dropdowncity1', Validators.REQUIRED, [ 'City' ], null);
    FormManager.hide('City1', 'city1');
    FormManager.resetValidations('city1');
    // if (_custSubGrp != 'undefined' && _custSubGrp != '' && (_custSubGrp ==
    // 'AQSTN' || _custSubGrp == 'CROSS') &&
    // FormManager.getActualValue('reqType') == 'C') {
    if (_custSubGrp != undefined && _custSubGrp != '' && _custSubGrp == 'CROSS' && FormManager.getActualValue('reqType') == 'C') {
      FormManager.resetValidations('cnCustName1');
      FormManager.resetValidations('cnAddrTxt');
      FormManager.resetValidations('cnCity');
      FormManager.resetValidations('stateProv');
    } else {
      if (_custSubGrp != undefined && _custSubGrp != '' && (_custSubGrp != 'PRIV' && _custSubGrp != 'INTER' && _custSubGrp != 'AQSTN')) {
        FormManager.addValidator('cnCity', Validators.REQUIRED, [ 'City Chinese' ], null);
        FormManager.addValidator('cnAddrTxt', Validators.REQUIRED, [ 'Street Address Chinese' ], null);
        FormManager.addValidator('cnCustName1', Validators.REQUIRED, [ 'Customer Name Chinese' ], null);
      }
      FormManager.addValidator('stateProv', Validators.REQUIRED, [ 'State/Province' ], null);
      //
      var addrType = FormManager.getActualValue('addrType');
      var _custSubGrp = FormManager.getActualValue('custSubGrp');
      if (addrType != '' && addrType == 'ZS01' && _custSubGrp != undefined && _custSubGrp != null && _custSubGrp != '' && (_custSubGrp == 'NRMLC' || _custSubGrp == 'NRMLD'  || _custSubGrp == 'KYND' || _custSubGrp == 'ECOSY' || _custSubGrp == 'AQSTN' || _custSubGrp == 'EMBSA' )){
        FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
        FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
        FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
      } else {
        FormManager.resetValidations('custPhone');
        FormManager.resetValidations('cnCustContJobTitle');
        FormManager.resetValidations('cnCustContNm');
      }
    }
  }
}

function addMandatoryOnlyForZS01CN(){
  var addrType = FormManager.getActualValue('addrType');
  var _custSubGrp = FormManager.getActualValue('custSubGrp');
  if (addrType != '' && addrType == 'ZS01' && _custSubGrp != undefined && _custSubGrp != null && _custSubGrp != '' && (_custSubGrp == 'NRMLC' || _custSubGrp == 'NRMLD'  || _custSubGrp == 'KYND' || _custSubGrp == 'ECOSY' || _custSubGrp == 'AQSTN' || _custSubGrp == 'EMBSA' )){
    FormManager.addValidator('custPhone', Validators.REQUIRED, [ "Phone#" ], null);
    FormManager.addValidator('cnCustContJobTitle', Validators.REQUIRED, [ "Customer Contact's Job Title" ], null);
    FormManager.addValidator('cnCustContNm', Validators.REQUIRED, [ "Customer Contact's Name (include salutation)" ], null);
  } else {
    FormManager.removeValidator('custPhone', Validators.REQUIRED);
    FormManager.removeValidator('cnCustContJobTitle', Validators.REQUIRED);
    FormManager.removeValidator('cnCustContNm', Validators.REQUIRED);
  }
}
// DENNIS: COMMENTED BECAUSE OF SCRIPT RUN TIME ISSUES
// To Validate Date
// function dateValidator() {
//
// if (!isValidDate())
// /*
// * FormManager.addValidator('dateValidator', Validators.REQUIRED, [ 'Invalid
// * Date Format' ], null);
// */
// return new ValidationResult(input, false, 'Invalid Date Format');
// else {
// return new ValidationResult(input, true);
// }
// }
//
// function isValidDate() {
//
// var dateString = FormManager.getActualValue('exportCodesTDODate');
// if (dateString.length > 8)
// return false;
//
// if (!/^\d{8}$/.test(dateString))
// return false;
//
// var year = parseInt(dateString.substr(0, 4), 10);
// var month = parseInt(dateString.substr(4, 2), 10);
// var day = parseInt(dateString.substr(6, 2), 10);
//
// var currentTime = new Date();
// var cyear = currentTime.getFullYear();
// // Check the ranges of month and year
// if (year > cyear || year == 0 || month == 0 || month > 12)
// return false;
// if (month == 2) {
// if (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0)) {
// if (day == 0 || day > 29)
// return false;
// } else {
// if (day == 0 || day > 28)
// return false;
// }
// }
// if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 ||
// month == 10 || month == 12) {
// if (day == 0 || day > 31)
// return false;
// }
// if (month == 4 || month == 6 || month == 9 || month == 11) {
// if (day == 0 || day > 30)
// return false;
// }
// return true;
// };
function isValidDate() {
  console.log('>>> Validating date');
  var dateString = FormManager.getActualValue('bioChemMissleMfg');
  if (dateString == '' || dateString == null)
    return true;
  if (dateString.length > 8)
    return false;

  if (!/^\d{8}$/.test(dateString))
    return false;

  var year = parseInt(dateString.substr(0, 4), 10);
  var month = parseInt(dateString.substr(4, 2), 10);
  var day = parseInt(dateString.substr(6, 2), 10);

  var currentTime = new Date();
  var cyear = currentTime.getFullYear();
  // Check the ranges of month and year
  if (year > cyear || year == 0 || month == 0 || month > 12)
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
function addDateValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        if (!isValidDate())
          return new ValidationResult(null, false, 'Invalid Date Format');
        else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');

}

function addFastPassAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (typeof (_pagemodel) != undefined) {
          if (custSubType == 'EMBSA') {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_FASTPASS_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null) {
              return new ValidationResult(null, false, 'Fastpass screenshot has not been attached to the request. This is required since this is an Embedded Solution Agreement (ESA) request.');
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function setTDOFlagToYesValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var tdoFlag = FormManager.getActualValue('icmsInd');
        var overallStatus = FormManager.getActualValue('overallStatus');
        if (typeof (_pagemodel) != undefined) {
          var id = FormManager.getActualValue('reqId');
          var ret = cmr.query('GET_REQUEST_APPROVAL', {
            REQ_ID : id
          });

          if ((ret == null || ret.ret1 == null)) {
            return new ValidationResult(null, true);
          } else {
            if (tdoFlag != 'Y' && overallStatus == 'Processing Validation') {
              return new ValidationResult(null, false, 'TDO Indicator is not set to \'Yes\'. This request is conditionally approved and requires that TDO Indicator is set to \'Yes\'.');
            } else {
              return new ValidationResult(null, true);
            }
          }
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}
function hideTDOFields() {
  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    FormManager.hide('ExportCodesCountry', 'custAcctType');
    FormManager.hide('ExportCodesTDOdate', 'bioChemMissleMfg');
    FormManager.hide('ExportCodesTDOIndicator', 'icmsInd');
  }
}

function setReadOnlyFields() {
  if (_pagemodel.userRole.toUpperCase() == "PROCESSOR") {
    console.log("Enabling isuCd for PROCESSOR...");
    FormManager.enable('isuCd');
  }

  if (_pagemodel.userRole.toUpperCase() == "REQUESTER") {
    console.log("Disabling isuCd for REQUESTER...");
    FormManager.readOnly('isuCd');
  }

}

function addSoltToAddressValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var zs01ReqId = FormManager.getActualValue('reqId');
        var addrType = FormManager.getActualValue('addrType');
        qParams = {
          REQ_ID : zs01ReqId,
        };
        var record = cmr.query('GETZS01VALRECORDS', qParams);
        var zs01Reccount = record.ret1;
        if (addrType == 'ZS01' && Number(zs01Reccount) == 1 && cmr.addressMode != 'updateAddress') {
          return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addPRIVCustNameValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
         var _custSubGrp = FormManager.getActualValue('custSubGrp');
         var englishName1 = FormManager.getActualValue('custNm1');
         var englishName2 = FormManager.getActualValue('custNm2');
         if (_custSubGrp != undefined && _custSubGrp != '' && _custSubGrp == 'PRIV') {
           if (englishName1 != undefined && englishName1 != ''){
             englishName1 = englishName1.toUpperCase();
             if (englishName1.indexOf("PRIVATE LIMITED") < 0 && englishName1.indexOf("COMPANY") < 0 && englishName1.indexOf("CORPORATION") < 0  && englishName1.indexOf("INCORPORATE") < 0 && englishName1.indexOf("ORGANIZATION") < 0 && englishName1.indexOf("LIMITED") < 0 && englishName1.indexOf("PVT LTD") < 0 && englishName1.indexOf("CO., LTD.") < 0 && englishName1.indexOf("LTD") < 0 && englishName1.indexOf("LTD.") < 0 && englishName1.indexOf("COM LTD") < 0){
               console.log("Customer Name English for Private Person validate is successful...");
             } else {
               return new ValidationResult(null, false, "Customer Name English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
             }
           }
           if (englishName2 != undefined && englishName2 != ''){
             englishName2 = englishName2.toUpperCase();
             if (englishName2.indexOf("PRIVATE LIMITED") < 0 && englishName2.indexOf("COMPANY") < 0 && englishName2.indexOf("CORPORATION") < 0  && englishName2.indexOf("INCORPORATE") < 0 && englishName2.indexOf("ORGANIZATION") < 0 && englishName2.indexOf("LIMITED") < 0 && englishName2.indexOf("PVT LTD") < 0 && englishName2.indexOf("CO.,LTD.") < 0 && englishName2.indexOf("LTD") < 0 && englishName2.indexOf("LTD.") < 0 && englishName2.indexOf("COM LTD") < 0 ){
               console.log("Customer Name Con't English for Private Person validate is successful...");
               return new ValidationResult(null, true);
             } else {
               return new ValidationResult(null, false, "Customer Name Con't English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
           }
         }
       }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

function addPRIVCustNameSFPValidator(){
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var action = FormManager.getActualValue('yourAction');
        if (action == 'SFP' && CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount) {
          var record = null;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
             record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
             var _custSubGrp = FormManager.getActualValue('custSubGrp');
             var englishName1 = record.custNm1[0];
             var englishName2 = record.custNm2[0];
             if (_custSubGrp != undefined && _custSubGrp != '' && _custSubGrp == 'PRIV') {
               if (englishName1 != undefined && englishName1 != null && englishName1 != ''){
                 englishName1 = englishName1.toUpperCase();
                 if (englishName1.indexOf("PRIVATE LIMITED") < 0 && englishName1.indexOf("COMPANY") < 0 && englishName1.indexOf("CORPORATION") < 0  && englishName1.indexOf("INCORPORATE") < 0 && englishName1.indexOf("ORGANIZATION") < 0 && englishName1.indexOf("LIMITED") < 0 && englishName1.indexOf("PVT LTD") < 0 && englishName1.indexOf("CO., LTD.") < 0 && englishName1.indexOf("LTD") < 0 && englishName1.indexOf("LTD.") < 0 && englishName1.indexOf("COM LTD") < 0){
                   console.log("Customer Name English for Private Person validate is successful...");
                 } else {
                   return new ValidationResult(null, false, "Customer Name English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
                 }
               }
               if (englishName2 != undefined && englishName2 != null && englishName2 != ''){
                 englishName2 = englishName2.toUpperCase();
                 if (englishName2.indexOf("PRIVATE LIMITED") < 0 && englishName2.indexOf("COMPANY") < 0 && englishName2.indexOf("CORPORATION") < 0  && englishName2.indexOf("INCORPORATE") < 0 && englishName2.indexOf("ORGANIZATION") < 0 && englishName2.indexOf("LIMITED") < 0 && englishName2.indexOf("PVT LTD") < 0 && englishName2.indexOf("CO.,LTD.") < 0 && englishName2.indexOf("LTD") < 0 && englishName2.indexOf("LTD.") < 0 && englishName2.indexOf("COM LTD") < 0 ){
                   console.log("Customer Name Con't English for Private Person validate is successful...");
                   return new ValidationResult(null, true);
                 } else {
                   return new ValidationResult(null, false, "Customer Name Con't English can't contain 'Private Limited', 'Company', 'Corporation', 'incorporate', 'organization', 'Pvt Ltd','Limited','Co., Ltd.', 'ltd', 'com ltd' for Scenario Privte Person");
               }
             }
           }
          }
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addContactInfoValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (CmrGrid.GRIDS.ADDRESS_GRID_GRID && CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount > 0 && FormManager.getActualValue('reqType') == 'C' && (custSubType == 'EMBSA' || custSubType == 'NRMLC' || custSubType == 'NRMLD'  || custSubType == 'KYND' || custSubType == 'ECOSY')) {
          var record = null;
          var type = null;

          var custPhone = null;
          var custContJobTitle = null;
          var custContNm = null;
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            if (record == null && _allAddressData != null && _allAddressData[i] != null) {
              record = _allAddressData[i];
            }
            type = record.addrType;
            custPhone = record.custPhone;
            custContJobTitle = record.cnCustContJobTitle;
            custContNm = record.cnCustContNm;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (typeof (custPhone) == 'object') {
              custPhone = custPhone[0];
            }
            if (typeof (custContJobTitle) == 'object') {
              custContJobTitle = custContJobTitle[0];
            }
            if (typeof (custContNm) == 'object') {
              custContNm = custContNm[0];
            }

            if (type == 'ZS01' && (custPhone == null || custContJobTitle == null || custContNm == null)) {
              return new ValidationResult(null, false, "Phone # is required, Customer Contact's job title is required, Customer Contact's Name(include salutation )is required.");
            }
          }
        }
        return new ValidationResult(null, true);

      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');

}

function addCityRequiredOnUpdateValidatorAddrList() {
  console.log("running addCityRequiredOnUpdateValidatorAddrList . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                var reqType = FormManager.getActualValue('reqType');
                var reqReason = FormManager.getActualValue('reqReason');
                if (reqReason == 'DIV' && _pagemodel.userRole.toUpperCase() == 'REQUESTER') {
                  return new ValidationResult(null, true);
                }
                if (typeof (CmrGrid.GRIDS.ADDRESS_GRID_GRID) != undefined && CmrGrid.GRIDS.ADDRESS_GRID_GRID != null) {
                  var addressStore = CmrGrid.GRIDS.ADDRESS_GRID_GRID.store, addressItems = addressStore._arrayOfAllItems, addrGridRow = 0, rowString = '', errorCount = 0, genericMsg = 'City was changed to empty (see comments) and is required to be supplied a new value.';
                  if (addressItems != null && addressItems.length != 0) {
                    for ( var key in addressItems) {
                      addrGridRow++;
                      var currentAddr = addressItems[key], city = '';
                      city = currentAddr.city1[0];
                      if ((city == '' || city == null) && reqType == 'U') {
                        errorCount++;
                        rowString += addrGridRow + " ";
                      }
                    }
                    var beforeAnd = rowString.substring(0, rowString.length - 2), afterAnd = rowString.substring(rowString.length - 2, rowString.length), lastStr = rowString.substring(
                        rowString.length - 3, rowString.length);
                    if (Number(lastStr.trim().length) == 2) { // records >= 10
                      beforeAnd = rowString.substring(0, rowString.length - 3);
                      afterAnd = rowString.substring(rowString.length - 3, rowString.length);
                    }
                    if (Number(errorCount) > 0) {
                      if (beforeAnd != '' && beforeAnd != null) {
                        genericMsg += ' Please check address list rows ' + beforeAnd + ' and ' + afterAnd + '.';
                        return new ValidationResult(null, false, genericMsg);
                      }
                      genericMsg += ' Please check address list row ' + afterAnd + '.';
                      return new ValidationResult(null, false, genericMsg);
                    }
                  }
                } else {
                  console.log("CmrGrid.GRIDS.ADDRESS_GRID_GRID undefined/null");
                }
                return new ValidationResult(null, true);
              } // validate
            }; // return
          })(), 'MAIN_NAME_TAB', 'frmCMR'); // validator body
}// func end

function addValidationForParentCompanyNo() {
  if (FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  // var parentCompanyNo = FormManager.getActualValue('dealerNo');
  var custSubType = FormManager.getActualValue('custSubGrp');
  var isuCd = FormManager.getActualValue('isuCd');
  if (custSubType == 'NRMLC' || custSubType == 'NRMLD'  || custSubType == 'KYND' || custSubType == 'ECOSY') {
    if (isuCd != "32" && (false == FormManager.getField('govType').checked)) {
      FormManager.addValidator('dealerNo', Validators.REQUIRED, [ 'You can input"000000" when there is not existing one. Parent Company No' ], 'MAIN_IBM_TAB');
    } else {
      FormManager.removeValidator('dealerNo', Validators.REQUIRED);
    }
  }
}

function addEngNameFormatValidation() {
  // var format = /[!@#$%^&*()_+\-=\[\]{};':"\\|<>\/?]/;
  var name1, name2, genericMsg;
  name1 = FormManager.getActualValue('custNm1');
  name2 = FormManager.getAcrualValue('custNm2');

  if (CNHandler.hasDoubleByte(name1) || CNHandler.hasDoubleByte(name2)) {
    genericMsg = 'Customer Name 1 and Customer Name Con\'t must not contain non-Latin1 characters.';
    return new ValidationResult(null, false, genericMsg);
  } else {
    return new ValidationResult(null, true);
  }

}

function addSocialCreditCdLengthValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var _socialCreditCdLen = FormManager.getActualValue('busnType');
        if (_socialCreditCdLen && _socialCreditCdLen.length > 0 && _socialCreditCdLen.length != 18){
            return new ValidationResult({
              id : 'busnType',
              type : 'text',
              name : 'busnType'
            }, false, 'The length for Social Credit Code should be 18 characters.');
        }else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function addAddrUpdateValidator() {
  console.log("running addAddrUpdateValidator . . .");
  FormManager
      .addFormValidator(
          (function() {
            return {
              validate : function() {
                
                if (FormManager.getActualValue('reqType') != 'U') {
                  return new ValidationResult(null, true);
                }
                
                var addrList = [];
                var addrRdcList = [];
                var cnAddrList = [];
                var cnAddrRdcList = [];
                
                var failInd = false;
                var zs01AddressUpdated = false;
                var otherAddressUpdated = false;
                
                var zs01Count = 0;
                
                var addrTxtZS01 = null;
                var addrTxt2ZS01 = null;
                var cnAddrTxtZS01 = null;
                var cnAddrTxt2ZS01 = null;
                
                var addrTypeOther = null;
                var addrSeqOther = null;
                
                var addrTxtOther = null;
                var addrTxt2Other = null;
                var cnAddrTxtOther = null;
                var cnAddrTxt2Other = null;
                
                var reqReason = FormManager.getActualValue('reqReason');
                
                if (reqReason == 'TREC') {
                  return new ValidationResult(null, true);
                }
                
                // get addr, addr_rdc, intl_addr, intl_addr_rdc
                var reqId = FormManager.getActualValue('reqId');
                var qParams = {
                    _qall : 'Y',
                    REQ_ID : reqId ,
                   };
                var addrResults = cmr.query('GET.ADDR_BY_REQID', qParams);
                if (addrResults != null) {
                  for (var i = 0; i < addrResults.length; i++) {
                    var addr  = {
                        reqId : addrResults[i].ret1,
                        addrType : addrResults[i].ret2,
                        addrSeq : addrResults[i].ret3,
                        custNm1 : addrResults[i].ret4,
                        custNm2 : addrResults[i].ret5,
                        addrTxt : addrResults[i].ret6,
                        addrTxt2 : addrResults[i].ret7,
                        city1 : addrResults[i].ret8,
                    };
                    addrList.push(addr);
                  }
                }
                
                var addrRdcResults = cmr.query('GET.ADDRRDC_BY_REQID', qParams);
                if (addrRdcResults != null) {
                  for (var i = 0; i < addrRdcResults.length; i++) {
                    var addrRdc  = {
                        reqId : addrRdcResults[i].ret1,
                        addrType : addrRdcResults[i].ret2,
                        addrSeq : addrRdcResults[i].ret3,
                        custNm1 : addrRdcResults[i].ret4,
                        custNm2 : addrRdcResults[i].ret5,
                        addrTxt : addrRdcResults[i].ret6,
                        addrTxt2 : addrRdcResults[i].ret7,
                        city1 : addrRdcResults[i].ret8,
                    };
                    addrRdcList.push(addrRdc);
                  }
                }
                
                var intlAddrResults = cmr.query('GET.INTLADDR_BY_REQID', qParams);
                if (intlAddrResults != null) {
                  for (var i = 0; i < intlAddrResults.length; i++) {
                    var cnAddr  = {
                        reqId : intlAddrResults[i].ret1,
                        addrType : intlAddrResults[i].ret2,
                        addrSeq : intlAddrResults[i].ret3,
                        cnCustName1 : intlAddrResults[i].ret4,
                        cnCustName2 : intlAddrResults[i].ret5,
                        cnAddrTxt : intlAddrResults[i].ret6,
                        cnAddrTxt2 : intlAddrResults[i].ret7,
                        cnCity1 : intlAddrResults[i].ret8,
                    };
                    cnAddrList.push(cnAddr);
                  }
                }
                
                var intlAddrRdcResults = cmr.query('GET.INTLADDRRDC_BY_REQID', qParams);
                if (intlAddrRdcResults != null) {
                  for (var i = 0; i < intlAddrRdcResults.length; i++) {
                    var cnAddrRdc  = {
                        reqId : intlAddrRdcResults[i].ret1,
                        addrType : intlAddrRdcResults[i].ret2,
                        addrSeq : intlAddrRdcResults[i].ret3,
                        cnCustName1 : intlAddrRdcResults[i].ret4,
                        cnCustName2 : intlAddrRdcResults[i].ret5,
                        cnAddrTxt : intlAddrRdcResults[i].ret6,
                        cnAddrTxt2 : intlAddrRdcResults[i].ret7,
                        cnCity1 : intlAddrRdcResults[i].ret8,
                    };
                    cnAddrRdcList.push(cnAddrRdc);
                  }
                }
                
                if (addrList != null) {
                  for (var i=0; i< addrList.length; i++) {
                    if (addrList[i].addrType == 'ZS01') {
                      addrTxtZS01 = addrList[i].addrTxt;
                      addrTxt2ZS01 = addrList[i].addrTxt2;
                      
                      zs01Count +=1;
                      
                      if (isChangedAddress('ZS01', addrList[i].addrSeq, addrList, addrRdcList, cnAddrList, cnAddrRdcList)) {
                        zs01AddressUpdated = true;
                      }
                    }
                  }
                }
                
                if (cnAddrList != null) {
                  for (var i=0; i< cnAddrList.length; i++) {
                    if (cnAddrList[i].addrType == 'ZS01') {
                      cnAddrTxtZS01 = cnAddrList[i].cnAddrTxt;
                      cnAddrTxt2ZS01 = cnAddrList[i].cnAddrTxt2;
                    }
                  }
                }
                
                if (zs01Count > 1) {
                  return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
                }
                
                if (addrList.length > 1) {
                  for (var i=0; i< addrList.length; i++) {
                    if (addrList[i].addrType != 'ZS01') {
                      if (isChangedAddress(addrList[i].addrType, addrList[i].addrSeq, addrList, addrRdcList, cnAddrList, cnAddrRdcList)) {
                        otherAddressUpdated = true;
                        addrTxtOther = addrList[i].addrTxt;
                        addrTxt2Other = addrList[i].addrTxt2;
                        cnAddrTxtOther = getAddrValue('cnAddrTxt', addrList[i].addrType, addrList[i].addrSeq, cnAddrList);
                        cnAddrTxt2Other = getAddrValue('cnAddrTxt2', addrList[i].addrType, addrList[i].addrSeq, cnAddrList);
                      }
                      
                      if (otherAddressUpdated) {
                        if ((addrTxtZS01.toUpperCase() != addrTxtOther.toUpperCase()) 
                            || (addrTxt2ZS01.toUpperCase() != addrTxt2Other.toUpperCase()) 
                            || (cnAddrTxtZS01.toUpperCase() != cnAddrTxtOther.toUpperCase()) 
                            || (cnAddrTxt2ZS01.toUpperCase() != cnAddrTxt2Other.toUpperCase())) {
                          failInd = true;
                        }
                      }
                    }
                  }
                }
                
                if (failInd) {
                  var id = FormManager.getActualValue('reqId');
                  var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
                    ID : id
                  });

                  if (ret == null || ret.ret1 == null) {
                    return new ValidationResult(null, false, 'The additional address should be same with Sold to address (ZS01).'
                        + ' If you insist on using different address from the Sold to (ZS01), you need to attach the screenshot of Customer Official Website,'
                        + ' Business License, Government Website, Contract/Purchase order with signature, and file content selected must be "Name and Address Change(China Specific)". ');
                  } else {
                    return new ValidationResult(null, true);
                  }
                } else {
                  var matchTianYanCha = checkTianYanChaMatch();
                  console.log('matchTianYanCha = ' + matchTianYanCha);
                  if (matchTianYanCha && addrList.length > 1) {
                    var id = FormManager.getActualValue('reqId');
                    var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
                      ID : id
                    });

                    if (ret == null || ret.ret1 == null) {
                      return new ValidationResult(null, true);
                    } else {
                      return new ValidationResult(null, false, 'Your request is not allowed to be sent for processing if the Chinese Company Name '
                          + 'and Address match with D&B 100%, but you still added an attachment of type "Name and Address Change(China Specific)". Please remove this Attachment then try again.'
                          );
                    }
                  }
                }
                return new ValidationResult(null, true);
              }
            };
          })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function addCNDnBMatchingAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqId = FormManager.getActualValue('reqId');
        var reqType = FormManager.getActualValue('reqType');
        var reqStatus = FormManager.getActualValue('reqStatus');
        var matchOverrideIndc = FormManager.getActualValue('matchOverrideIndc');
        var findDnbResult = FormManager.getActualValue('findDnbResult');
        var userRole = FormManager.getActualValue('userRole');
        var ifReprocessAllowed = FormManager.getActualValue('autoEngineIndc');
        if (reqId > 0 && reqType == 'U' && reqStatus == 'DRA' && userRole == 'Requester' && (ifReprocessAllowed == 'R' || ifReprocessAllowed == 'P' || ifReprocessAllowed == 'B')
            && matchOverrideIndc == 'Y') {
          // FOR CN
          // var cntry = FormManager.getActualValue('landCntry');cntry != '' &&
          // cntry != 'CN' &&
          var loc = FormManager.getActualValue('cmrIssuingCntry');
          if( loc == '641' ) {
            // FOR US Temporary
           var id = FormManager.getActualValue('reqId');
             var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
               ID : id
             });
             if (ret == null || ret.ret1 == null) {
               return new ValidationResult(null, false, "By overriding the D&B matching, you\'re obliged to provide either one of the following documentation as backup - "
                    + "client\'s official website, Secretary of State business registration proof, client\'s confirmation email and signed PO, attach it under the file content "
                    + "of <strong>Company Proof</strong>. Please note that the sources from Wikipedia, Linked In and social medias are not acceptable.");
             } else {
               return new ValidationResult(null, true);
            }
          }         
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function isChangedAddress(addrType, addrSeq, addrList, addrRdcList, cnAddrList, cnAddrRdcList) {
  var result = false;
  if (isChangedField('addrTxt', addrType, addrSeq, addrList, addrRdcList) || isChangedField('addrTxt2', addrType, addrSeq, addrList, addrRdcList) || isChangedField('cnAddrTxt', addrType, addrSeq, cnAddrList, cnAddrRdcList) || isChangedField('cnAddrTxt2', addrType, addrSeq, cnAddrList, cnAddrRdcList)) {
    result = true;
  }
  return result;
}

function isChangedField(fieldName, addrType, addrSeq, list1, list2) {
  var result = false;
  var fieldValue1 = null;
  var fieldValue2 = null;
  
  // addr
  if (list1 != null && list1.length > 0) {
    for (var i = 0; i < list1.length; i ++) {
      if (list1[i].addrType == addrType && list1[i].addrSeq == addrSeq) {
        if (fieldName == 'addrTxt') {
          fieldValue1 = list1[i].addrTxt;
        } else if (fieldName == 'addrTxt2') {
          fieldValue1 = list1[i].addrTxt2;
        } else if (fieldName == 'cnAddrTxt') {
          fieldValue1 = list1[i].cnAddrTxt;
        } else if (fieldName == 'cnAddrTxt2') {
          fieldValue1 = list1[i].cnAddrTxt2;
        }
      }
    }
  }
  
  // addr_rdc
  if (list2 != null && list2.length > 0) {
    for (var j = 0; j < list2.length; j ++) {
      if (list2[j].addrType == addrType && list2[j].addrSeq == addrSeq) {
        if (fieldName == 'addrTxt') {
          fieldValue2 = list2[j].addrTxt;
        } else if (fieldName == 'addrTxt2') {
          fieldValue2 = list2[j].addrTxt2;
        } else if (fieldName == 'cnAddrTxt') {
          fieldValue2 = list2[j].cnAddrTxt;
        } else if (fieldName == 'cnAddrTxt2') {
          fieldValue2 = list2[j].cnAddrTxt2;
        }
      }
    }
  }
  
  if (convert2DBCSIgnoreCase(fieldValue1) != convert2DBCSIgnoreCase(fieldValue2)) {
    result = true;
  }
  return result;
}

function getAddrValue(fieldName, addrType, addrSeq, list) {
  var value = null;
  if (list!=null && list.length > 0) {
    for (var i = 0; i < list.length; i ++) {
      if (list[i].addrType == addrType && list[i].addrSeq == addrSeq) {
        if (fieldName == 'addrTxt') {
          value = list[i].addrTxt;
        } else if (fieldName == 'addrTxt2') {
          value = list[i].addrTxt2;
        } else if (fieldName == 'cnAddrTxt') {
          value = list[i].cnAddrTxt;
        } else if (fieldName == 'cnAddrTxt2') {
          value = list[i].cnAddrTxt2;
        }
      }
    }
  }
  return value;
}

function checkTianYanChaMatch() {
  var cnCustName1ZS01 = '';
  var cnCustName2ZS01 = '';
  var cnAddrTxtZS01 = '';
  var intlCustNm4ZS01 = '';
  var cnCityZS01 = '';
  var cnDistrictZS01 = '';

  for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
    record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
    type = record.addrType;

    if (typeof (type) == 'object') {
      type = type[0];
    }
    if (type == 'ZS01') {
      cnCustName1ZS01 = record.cnCustName1;
      cnCustName2ZS01 = record.cnCustName2;
      cnAddrTxtZS01 = record.cnAddrTxt;
      cnCityZS01 = record.cnCity;
      cnDistrictZS01 = record.cnDistrict;
    }
    if (typeof (cnCustName1ZS01) == 'object') {
      if (cnCustName1ZS01[0] != '' && cnCustName1ZS01[0] != null) {
        cnCustName1ZS01 = cnCustName1ZS01[0];
      }
    }
    if (typeof (cnCustName2ZS01) == 'object') {
      if (cnCustName2ZS01[0] != '' && cnCustName2ZS01[0] != null) {
        cnCustName2ZS01 = cnCustName2ZS01[0];
      }
    }
    if (typeof (cnAddrTxtZS01) == 'object') {
      if (cnAddrTxtZS01[0] != '' && cnAddrTxtZS01[0] != null) {
        cnAddrTxtZS01 = cnAddrTxtZS01[0];
      }
    }
    if (typeof (cnCityZS01) == 'object') {
      if (cnCityZS01[0] != '' && cnCityZS01[0] != null) {
        cnCityZS01 = cnCityZS01[0];
      }
    }
    if (typeof (cnDistrictZS01) == 'object') {
      if (cnDistrictZS01[0] != '' && cnDistrictZS01[0] != null) {
        cnDistrictZS01 = cnDistrictZS01[0];
      }
    }
  }
  
  var ret = cmr.query('ADDR.GET.INTLCUSTNM4.BY_REQID', {
    REQ_ID : FormManager.getActualValue('reqId')
  });
  if (ret && ret.ret1 && ret.ret1 != '') {
    intlCustNm4ZS01 = ret.ret1;
  }
  
  var busnType = FormManager.getActualValue('busnType');
  var cnName = convert2SBCS(cnCustName1ZS01 + cnCustName2ZS01);
  var result = {};
  var busnTypeResult = {};
  var nameResult = {};
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/cn/dnb.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      busnType : busnType,
      cnName : cnName,
      cnAddress : cnAddrTxtZS01,
      cnCity : cnCityZS01
    },
    timeout : 50000,
    sync : true,
    load : function(data, ioargs) {
      if (data && data.result) {
        busnTypeResult = data.result;
      }
    },
    error : function(error, ioargs) {
      busnTypeResult = {};
    }
  });
  result = busnTypeResult;
  
  if($.isEmptyObject(result)){
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/cn/dnb.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        cnName : cnName,
        cnAddress : cnAddrTxtZS01,
        cnCity : cnCityZS01
      },
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data && data.result) {
          nameResult = data.result;
        }
      },
      error : function(error, ioargs) {
        nameResult = {};
      }
    });
    result = nameResult;
  }

  if($.isEmptyObject(busnTypeResult) && !$.isEmptyObject(nameResult)) {
    return false;
  } else if ($.isEmptyObject(busnTypeResult) && $.isEmptyObject(nameResult)) {
    return false;
  } else {
    var cnAddress = convert2SBCS(cnAddrTxtZS01 + intlCustNm4ZS01);
    var cnAddressRev = convert2SBCS(intlCustNm4ZS01 + cnAddrTxtZS01);
    var name2SBCS = convert2SBCS(result.name);
    var address2SBCS = convert2SBCS(result.regLocation);
    var apiCity = '';
    var apiDistrict = '';
    var nameEqualFlag = true;
    var addressEqualFlag = true;
    if(result.city != null){
      apiCity = result.city;
    }
    if(result.district != null){
      apiDistrict = result.district;
    }
    
    if (name2SBCS != cnName) {
      nameEqualFlag = false;
    }
    if (address2SBCS != cnAddress) {
      if (address2SBCS.indexOf(cnAddress) >= 0 && apiCity.indexOf(cnCityZS01) >= 0 && apiDistrict.indexOf(cnDistrictZS01) >= 0){
        addressEqualFlag = true;
      } else if(apiCity.indexOf(cnCityZS01) >= 0 &&  (address2SBCS.indexOf(cnAddress) >= 0 || address2SBCS.indexOf(cnAddressRev) >= 0)){
        // this check is to add the D&B format of Street = District + Street
        addressEqualFlag = true;
      } else if(address2SBCS.indexOf(cnAddress) >= 0 && apiCity == '市辖区' && address2SBCS.indexOf(cnCityZS01) >= 0 && apiDistrict.indexOf(cnDistrictZS01) >= 0){
          addressEqualFlag = true;
      } else {
        addressEqualFlag = false;
      }
    }

    if(nameEqualFlag && addressEqualFlag){
      return true;
    }
    return false;
  }
}

function setCompanyOnInacCd() {
  if (FormManager.getActualValue('reqType') != 'C' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  if (_pagemodel.userRole.toUpperCase() != "REQUESTER") {
    return;
  }
  dojo.connect(FormManager.getField('inacCd'), 'onChange', function(value) {
    var inacCd = FormManager.getActualValue('inacCd');
    
    if (inacCd != _pagemodel.inacCd) {
      if (FormManager.getActualValue('company') !='') {
        FormManager.setValue('company', '');
      }
    }
    
    if (inacCd == 'XXXX') {
      FormManager.setValue('company', '856105');
    }
    
  });
}

function addDoubleByteValidatorCN(cntry, details) {

  /* Address */
  FormManager.addValidator('cnCustName1', Validators.NO_SINGLE_BYTE, [ 'Customer Name Chinese' ]);
  FormManager.addValidator('cnCustName2', Validators.NO_SINGLE_BYTE, [ 'Customer Name Con' + '\'' + 't Chinese' ]);
  FormManager.addValidator('cnCustName3', Validators.NO_SINGLE_BYTE, [ 'Customer Name Con' + '\'' + 't Chinese 2' ]);
  FormManager.addValidator('cnAddrTxt', Validators.NO_SINGLE_BYTE, [ 'Street Address Chinese' ]);
  FormManager.addValidator('cnAddrTxt2', Validators.NO_SINGLE_BYTE, [ 'Street Address Con' + '\'' + 't Chinese' ]);
  FormManager.addValidator('cnCity', Validators.NO_SINGLE_BYTE, [ 'City Chinese' ]);
  FormManager.addValidator('cnDistrict', Validators.NO_SINGLE_BYTE, [ 'District Chinese' ]);

  FormManager.addValidator('custNm1', Validators.LATIN, [ 'Customer Name English' ]);
  FormManager.addValidator('custNm2', Validators.LATIN, [ 'Customer Name Con' + '\'' + 't English' ]);
  FormManager.addValidator('custNm3', Validators.LATIN, [ 'Customer Name Con' + '\'' + 't 2 English' ]);
  FormManager.addValidator('addrTxt', Validators.LATIN, [ 'Customer Address English' ]);
  FormManager.addValidator('addrTxt2', Validators.LATIN, [ 'Customer Address Con' + '\'' + 't English' ]);
  FormManager.addValidator('city2', Validators.LATIN, [ 'District English' ]);
  FormManager.addValidator('dropdowncity1', Validators.LATIN, [ 'City English' ]);
  FormManager.addValidator('dept', Validators.LATIN, [ 'Department English' ]);
  FormManager.addValidator('bldg', Validators.LATIN, [ 'Building English' ]);
  FormManager.addValidator('office', Validators.LATIN, [ 'Office English' ]);
  FormManager.addValidator('poBox', Validators.LATIN, [ 'PostBox English' ]);
}

function convert2DBCSIgnoreCase(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0 && input != '') {
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
function replaceAndSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0 && value != '') {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/&/g, '＆');
  }
  return modifiedVal;
};
function replaceCrossbarSymbol(value) {
  var modifiedVal = '';
  if (value != null && value.length > 0 && value != '') {
    modifiedVal = value;
    modifiedVal = modifiedVal.replace(/-/g, '－');
    modifiedVal = modifiedVal.replace(/\./g, '．');
    modifiedVal = modifiedVal.replace(/,/g, '，');
    modifiedVal = modifiedVal.replace(/:/g, '：');
    modifiedVal = modifiedVal.replace(/_/g, '＿');
    modifiedVal = modifiedVal.replace(/\(/g, '（');
    modifiedVal = modifiedVal.replace(/\)/g, '）');
    
    modifiedVal = modifiedVal.replace(/\//g, '\／');
    modifiedVal = modifiedVal.replace(/\</g, '\＜');
    modifiedVal = modifiedVal.replace(/\>/g, '\＞');
    modifiedVal = modifiedVal.replace(/\?/g, '\？');
    modifiedVal = modifiedVal.replace(/\`/g, '\｀');
    modifiedVal = modifiedVal.replace(/\~/g, '\～');
    modifiedVal = modifiedVal.replace(/\!/g, '\！');
    modifiedVal = modifiedVal.replace(/\@/g, '\＠');
    modifiedVal = modifiedVal.replace(/\#/g, '\＃');
    modifiedVal = modifiedVal.replace(/\$/g, '\＄');
    modifiedVal = modifiedVal.replace(/\%/g, '\％');
    modifiedVal = modifiedVal.replace(/\^/g, '\＾');
    modifiedVal = modifiedVal.replace(/\*/g, '\＊');
    modifiedVal = modifiedVal.replace(/\+/g, '\＋');
    modifiedVal = modifiedVal.replace(/\=/g, '\＝');
    modifiedVal = modifiedVal.replace(/\[/g, '\［');
    modifiedVal = modifiedVal.replace(/\]/g, '\］');
    modifiedVal = modifiedVal.replace(/\{/g, '\｛');
    modifiedVal = modifiedVal.replace(/\}/g, '\｝');
    modifiedVal = modifiedVal.replace(/\|/g, '\｜');
    modifiedVal = modifiedVal.replace(/\;/g, '\；');
    modifiedVal = modifiedVal.replace(/\'/g, '\＇');
    modifiedVal = modifiedVal.replace(/\"/g, '\＂');
  }
  return modifiedVal;
};

function convert2SBCS(input) {
  var modifiedVal = '';
  if (input != null && input.length > 0 && input != '') {
    modifiedVal = input;
    // modifiedVal = modifiedVal.replace(/[^\d]/g, '');
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
    modifiedVal = modifiedVal.replace(/ａ/g, 'a');
    modifiedVal = modifiedVal.replace(/ｂ/g, 'b');
    modifiedVal = modifiedVal.replace(/ｃ/g, 'c');
    modifiedVal = modifiedVal.replace(/ｄ/g, 'd');
    modifiedVal = modifiedVal.replace(/ｅ/g, 'e');
    modifiedVal = modifiedVal.replace(/ｆ/g, 'f');
    modifiedVal = modifiedVal.replace(/ｇ/g, 'g');
    modifiedVal = modifiedVal.replace(/ｈ/g, 'f');
    modifiedVal = modifiedVal.replace(/ｉ/g, 'i');
    modifiedVal = modifiedVal.replace(/ｊ/g, 'j');
    modifiedVal = modifiedVal.replace(/ｋ/g, 'k');
    modifiedVal = modifiedVal.replace(/ｌ/g, 'l');
    modifiedVal = modifiedVal.replace(/ｍ/g, 'm');
    modifiedVal = modifiedVal.replace(/ｎ/g, 'n');
    modifiedVal = modifiedVal.replace(/ｏ/g, 'o');
    modifiedVal = modifiedVal.replace(/ｐ/g, 'p');
    modifiedVal = modifiedVal.replace(/ｑ/g, 'q');
    modifiedVal = modifiedVal.replace(/ｒ/g, 'r');
    modifiedVal = modifiedVal.replace(/ｓ/g, 's');
    modifiedVal = modifiedVal.replace(/ｔ/g, 't');
    modifiedVal = modifiedVal.replace(/ｕ/g, 'u');
    modifiedVal = modifiedVal.replace(/ｖ/g, 'v');
    modifiedVal = modifiedVal.replace(/ｗ/g, 'w');
    modifiedVal = modifiedVal.replace(/ｘ/g, 'x');
    modifiedVal = modifiedVal.replace(/ｙ/g, 'y');
    modifiedVal = modifiedVal.replace(/ｚ/g, 'z');
    modifiedVal = modifiedVal.replace(/Ａ/g, 'A');
    modifiedVal = modifiedVal.replace(/Ｂ/g, 'B');
    modifiedVal = modifiedVal.replace(/Ｃ/g, 'C');
    modifiedVal = modifiedVal.replace(/Ｄ/g, 'D');
    modifiedVal = modifiedVal.replace(/Ｅ/g, 'E');
    modifiedVal = modifiedVal.replace(/Ｆ/g, 'F');
    modifiedVal = modifiedVal.replace(/Ｇ/g, 'G');
    modifiedVal = modifiedVal.replace(/Ｈ/g, 'H');
    modifiedVal = modifiedVal.replace(/Ｉ/g, 'I');
    modifiedVal = modifiedVal.replace(/Ｊ/g, 'J');
    modifiedVal = modifiedVal.replace(/Ｋ/g, 'K');
    modifiedVal = modifiedVal.replace(/Ｌ/g, 'L');
    modifiedVal = modifiedVal.replace(/Ｍ/g, 'M');
    modifiedVal = modifiedVal.replace(/Ｎ/g, 'N');
    modifiedVal = modifiedVal.replace(/Ｏ/g, 'O');
    modifiedVal = modifiedVal.replace(/Ｐ/g, 'P');
    modifiedVal = modifiedVal.replace(/Ｑ/g, 'Q');
    modifiedVal = modifiedVal.replace(/Ｒ/g, 'R');
    modifiedVal = modifiedVal.replace(/Ｓ/g, 'S');
    modifiedVal = modifiedVal.replace(/Ｔ/g, 'T');
    modifiedVal = modifiedVal.replace(/Ｕ/g, 'U');
    modifiedVal = modifiedVal.replace(/Ｖ/g, 'V');
    modifiedVal = modifiedVal.replace(/Ｗ/g, 'W');
    modifiedVal = modifiedVal.replace(/Ｘ/g, 'X');
    modifiedVal = modifiedVal.replace(/Ｙ/g, 'Y');
    modifiedVal = modifiedVal.replace(/Ｚ/g, 'Z');
    modifiedVal = modifiedVal.replace(/　/g, ' ');
    modifiedVal = modifiedVal.replace(/＆/g, '&');
    modifiedVal = modifiedVal.replace(/－/g, '-');
    modifiedVal = modifiedVal.replace(/\．/g, '\.');
    modifiedVal = modifiedVal.replace(/，/g, ',');
    modifiedVal = modifiedVal.replace(/：/g, ':');
    modifiedVal = modifiedVal.replace(/＿/g, '_');
    
    modifiedVal = modifiedVal.replace(/（/g, '(');
    modifiedVal = modifiedVal.replace(/）/g, ')');
    modifiedVal = modifiedVal.replace(/／/g, '/');
    modifiedVal = modifiedVal.replace(/＜/g, '<');
    modifiedVal = modifiedVal.replace(/＞/g, '>');
    modifiedVal = modifiedVal.replace(/？/g, '?');
    modifiedVal = modifiedVal.replace(/｀/g, '`');
    modifiedVal = modifiedVal.replace(/～/g, '~');
    modifiedVal = modifiedVal.replace(/！/g, '!');
    modifiedVal = modifiedVal.replace(/＠/g, '@');
    modifiedVal = modifiedVal.replace(/＃/g, '#');
    modifiedVal = modifiedVal.replace(/＄/g, '$');
    modifiedVal = modifiedVal.replace(/％/g, '%');
    modifiedVal = modifiedVal.replace(/＾/g, '^');
    modifiedVal = modifiedVal.replace(/＊/g, '*');
    modifiedVal = modifiedVal.replace(/＋/g, '+');
    modifiedVal = modifiedVal.replace(/＝/g, '=');
    modifiedVal = modifiedVal.replace(/［/g, '[');
    modifiedVal = modifiedVal.replace(/］/g, ']');
    modifiedVal = modifiedVal.replace(/｛/g, '{');
    modifiedVal = modifiedVal.replace(/｝/g, '}');
    modifiedVal = modifiedVal.replace(/｜/g, '|');
    modifiedVal = modifiedVal.replace(/；/g, ';');
    modifiedVal = modifiedVal.replace(/＇/g, '\'');
    modifiedVal = modifiedVal.replace(/＂/g, '\"');
  }
  return modifiedVal;
}

function convertCnCustName1(cntry, addressMode, details) {
  convertCnCustName1InDetails();
  dojo.connect(FormManager.getField('cnCustName1'), 'onChange', function(value) {
    convertCnCustName1InDetails();
  });
}
function convertCnCustName1InDetails() {
  var cnCustName1 = FormManager.getActualValue('cnCustName1');
  FormManager.setValue('cnCustName1', convert2DBCSIgnoreCase(cnCustName1));
  cnCustName1 = FormManager.getActualValue('cnCustName1');
}

function convertCnCustName2(cntry, addressMode, details) {
  convertCnCustName2InDetails();
  dojo.connect(FormManager.getField('cnCustName2'), 'onChange', function(value) {
    convertCnCustName2InDetails();
  });
}
function convertCnCustName2InDetails() {
  var cnCustName2 = FormManager.getActualValue('cnCustName2');
  FormManager.setValue('cnCustName2', convert2DBCSIgnoreCase(cnCustName2));
  cnCustName2 = FormManager.getActualValue('cnCustName2');
}

function convertCnCustName3(cntry, addressMode, details) {
  convertCnCustName3InDetails();
  dojo.connect(FormManager.getField('cnCustName3'), 'onChange', function(value) {
    convertCnCustName3InDetails();
  });
}
function convertCnCustName3InDetails() {
  var cnCustName3 = FormManager.getActualValue('cnCustName3');
  FormManager.setValue('cnCustName3', convert2DBCSIgnoreCase(cnCustName3));
  cnCustName3 = FormManager.getActualValue('cnCustName3');
}

function convertCnAddrTxt(cntry, addressMode, details) {
  convertCnAddrTxtInDetails();
  dojo.connect(FormManager.getField('cnAddrTxt'), 'onChange', function(value) {
    convertCnAddrTxtInDetails();
  });
}
function convertCnAddrTxtInDetails() {
  var cnAddrTxt = FormManager.getActualValue('cnAddrTxt');
  FormManager.setValue('cnAddrTxt', convert2DBCSIgnoreCase(cnAddrTxt));
  cnAddrTxt = FormManager.getActualValue('cnAddrTxt');
}

function convertCnAddrTxt2(cntry, addressMode, details) {
  convertCnAddrTxt2InDetails();
  dojo.connect(FormManager.getField('cnAddrTxt2'), 'onChange', function(value) {
    convertCnAddrTxt2InDetails();
  });
}
function convertCnAddrTxt2InDetails() {
  var cnAddrTxt2 = FormManager.getActualValue('cnAddrTxt2');
  FormManager.setValue('cnAddrTxt2', convert2DBCSIgnoreCase(cnAddrTxt2));
  cnAddrTxt2 = FormManager.getActualValue('cnAddrTxt2');
}

function convertCnDistrict(cntry, addressMode, details) {
  convertCnDistrictInDetails();
  dojo.connect(FormManager.getField('cnDistrict'), 'onChange', function(value) {
    convertCnDistrictInDetails();
  });
}
function convertCnDistrictInDetails() {
  var cnDistrict = FormManager.getActualValue('cnDistrict');
  FormManager.setValue('cnDistrict', convert2DBCSIgnoreCase(cnDistrict));
  cnDistrict = FormManager.getActualValue('cnDistrict');
}
// TODO
function convertCustNm1(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('custNm1'), 'onChange', function(value) {
    var custNm1 = FormManager.getActualValue('custNm1');
    FormManager.setValue('custNm1', convert2SBCS(custNm1));
  });
}

function convertCustNm2(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('custNm2'), 'onChange', function(value) {
    var custNm2 = FormManager.getActualValue('custNm2');
    FormManager.setValue('custNm2', convert2SBCS(custNm2));
  });
}
function convertCustNm3(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('custNm3'), 'onChange', function(value) {
    var custNm3 = FormManager.getActualValue('custNm3');
    FormManager.setValue('custNm3', convert2SBCS(custNm3));
  });
}
function convertAddrTxt(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('addrTxt'), 'onChange', function(value) {
    var addrTxt = FormManager.getActualValue('addrTxt');
    FormManager.setValue('addrTxt', convert2SBCS(addrTxt));
  });
}
function convertAddrTxt2(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('addrTxt2'), 'onChange', function(value) {
    var addrTxt2 = FormManager.getActualValue('addrTxt2');
    FormManager.setValue('addrTxt2', convert2SBCS(addrTxt2));
  });
}
function convertCity2(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('city2'), 'onChange', function(value) {
    var city2 = FormManager.getActualValue('city2');
    FormManager.setValue('city2', convert2SBCS(city2));
  });
}
function convertDept(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('dept'), 'onChange', function(value) {
    var dept = FormManager.getActualValue('dept');
    FormManager.setValue('dept', convert2SBCS(dept));
  });
}
function convertBldg(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('bldg'), 'onChange', function(value) {
    var bldg = FormManager.getActualValue('bldg');
    FormManager.setValue('bldg', convert2SBCS(bldg));
  });
}
function convertOffice(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('office'), 'onChange', function(value) {
    var office = FormManager.getActualValue('office');
    FormManager.setValue('office', convert2SBCS(office));
  });
}
function convertPoBox(cntry, addressMode, details) {
  dojo.connect(FormManager.getField('poBox'), 'onChange', function(value) {
    var poBox = FormManager.getActualValue('poBox');
    FormManager.setValue('poBox', convert2SBCS(poBox));
  });
}

function validateEnNameForInter() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
      	console.log("running validateCnNameAndAddr . . .");
        var custSubType = FormManager.getActualValue('custSubGrp');
          if (custSubType == 'INTER') {
            var custNm1ZS01 = '';
            var custNm2ZS01 = '';
            for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
              record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
              type = record.addrType;

              if (typeof (type) == 'object') {
                type = type[0];
              }
              if (type == 'ZS01') {
                custNm1ZS01 = record.custNm1;
                custNm2ZS01 = record.custNm2 == null ? '' : record.custNm2;
              }
            }
            var enName = custNm1ZS01 + ' ' + custNm2ZS01;
            var custSubType = FormManager.getActualValue('custSubGrp');
            if (enName.toUpperCase().indexOf("IBM CHINA") == -1 && enName.toUpperCase().indexOf("IBM (CHINA)") == -1){
              return new ValidationResult(null, false, "Customer Name English should include 'IBM China' or 'IBM (CHINA)' for Internal Sub_scenario."); 
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
      }
    }
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function validateEnNameInAddrTab() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var custSubType = FormManager.getActualValue('custSubGrp');
          if (custSubType == 'INTER') {
            var custNm1 = FormManager.getActualValue('custNm1');
            var custNm2 = FormManager.getActualValue('custNm2');
            var enName = custNm1 + ' ' + custNm2;
            var custSubType = FormManager.getActualValue('custSubGrp');
            if (enName.toUpperCase().indexOf("IBM CHINA") == -1 && enName.toUpperCase().indexOf("IBM (CHINA)") == -1){
              return new ValidationResult(null, false, "Customer Name English should include 'IBM China' or 'IBM (CHINA)' for Internal Sub_scenario.");
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
      }
    };
  })(), null, 'frmCMR_addressModal');
}

// CREATCMR-7879
function checkCNCustomerName() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var errorMsg = '';
        var action = FormManager.getActualValue('yourAction');
        var custNm1 = FormManager.getActualValue('mainCustNm1').toUpperCase();
        
        var reqType = FormManager.getActualValue('reqType');
        var role = FormManager.getActualValue('userRole').toUpperCase();
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        
        if(reqType == 'C' && role == 'REQUESTER' && custSubGrp == 'KYND' && (action=='SFP' || action=='VAL')){
          // CREATCMR-7879
          if(custNm1.indexOf('KYNDRYL')==-1){
            errorMsg = 'Customer name must contain word \'Kyndryl\'';
          }
        }
        
        if (errorMsg != '') {
          return new ValidationResult({
            id : 'custNm1',
            type : 'text',
            name : 'custNm1'
          }, false, errorMsg);
        }
        
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}

function validateCnNameAndAddr4Create() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var action = FormManager.getActualValue('yourAction');
        if(action == 'SFP'){

          if (FormManager.getActualValue('reqType') != 'C') {
            return new ValidationResult(null, true);
          }
          console.log("running validateCnNameAndAddr4Create . . .");
          
          var cnCustName1ZS01 = '';
          var cnCustName2ZS01 = '';
          var cnAddrTxtZS01 = '';
          var intlCustNm4ZS01 = '';
          var cnCityZS01 = '';
          var cnDistrictZS01 = '';

          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            type = record.addrType;

            if (typeof (type) == 'object') {
              type = type[0];
            }

            if (type == 'ZS01') {
              cnCustName1ZS01 = record.cnCustName1;
              cnCustName2ZS01 = record.cnCustName2;
              cnAddrTxtZS01 = record.cnAddrTxt;
              cnCityZS01 = record.cnCity;
              cnDistrictZS01 = record.cnDistrict;
            }

            if (typeof (cnCustName1ZS01) == 'object') {
              if (cnCustName1ZS01[0] != '' && cnCustName1ZS01[0] != null) {
                cnCustName1ZS01 = cnCustName1ZS01[0];
              }
            }

            if (typeof (cnCustName2ZS01) == 'object') {
              if (cnCustName2ZS01[0] != '' && cnCustName2ZS01[0] != null) {
                cnCustName2ZS01 = cnCustName2ZS01[0];
              }
            }

            if (typeof (cnAddrTxtZS01) == 'object') {
              if (cnAddrTxtZS01[0] != '' && cnAddrTxtZS01[0] != null) {
                cnAddrTxtZS01 = cnAddrTxtZS01[0];
              }
            }
            
            if (typeof (cnCityZS01) == 'object') {
              if (cnCityZS01[0] != '' && cnCityZS01[0] != null) {
                cnCityZS01 = cnCityZS01[0];
              } else {
                cnCityZS01 = '';
              }
            }
            
            if (typeof (cnDistrictZS01) == 'object') {
              if (cnDistrictZS01[0] != '' && cnDistrictZS01[0] != null) {
                cnDistrictZS01 = cnDistrictZS01[0];
              } else {
                cnDistrictZS01 = '';
              }
            }
          }
          var ret = cmr.query('ADDR.GET.INTLCUSTNM4.BY_REQID', {
            REQ_ID : FormManager.getActualValue('reqId')
          });
          if (ret && ret.ret1 && ret.ret1 != '') {
            intlCustNm4ZS01 = ret.ret1;
          }

          var cnName = convert2SBCS(cnCustName1ZS01 + cnCustName2ZS01);
          var scenarioValidation = true;
          if (custSubType == 'INTER' || custSubType == 'CROSS' || custSubType == 'PRIV') {
            scenarioValidation = false;
          }else if(custSubType == 'AQSTN'){
            if(cnName == null || cnName == '' || cnName == '*'){
              scenarioValidation = false;
            }
          }
          if (scenarioValidation){
            var cnName = convert2SBCS(cnCustName1ZS01 + cnCustName2ZS01);
            var cnAddress = convert2SBCS(cnAddrTxtZS01 + intlCustNm4ZS01);
            var cnCity = convert2SBCS(cnCityZS01);
            var result = {};
            result = checkDnb(cnName, cnAddress, cnCity);

            console.log('Checking name and address info...');
            var cnAddress = convert2SBCS(cnAddrTxtZS01 + intlCustNm4ZS01);
            var name2SBCS = convert2SBCS(result.name);
            var address2SBCS = convert2SBCS(result.regLocation);
            var apiCity = '';
            var apiDistrict = '';
            var nameEqualFlag = true;
            var addressEqualFlag = true;
            if(result.city != null){
              apiCity = result.city;
            }
            if(result.district != null){
              apiDistrict = result.district;
            }

            var correctName = '';
            var correctAddress = '';
            if (name2SBCS != cnName) {
              console.log('Name mismatch: '+name2SBCS+' : '+cnName);
              nameEqualFlag = false;
              if(!$.isEmptyObject(result)){
                correctName = '<br/>Company Name: ' + result.name;
              } else {
                correctName = '<br/>Company Name: No Data';
              }
            }

            addressEqualFlag = addressValidation(address2SBCS, apiCity, cnAddress, cnCity, cnDistrictZS01);
            if(!addressEqualFlag){
              var rmProvince = cnAddress.replace(/.+?(省)/,'');
              var rmCity = rmProvince.replace(/.+?(市)/,'');
              addressEqualFlag = addressValidation(address2SBCS, apiCity, rmCity, cnCity, cnDistrictZS01);
            }
            if (!addressEqualFlag){
              console.log('Address mismatch: '+address2SBCS+' : '+cnAddress);
              if(!$.isEmptyObject(result)){
                correctAddress = '<br/>Company Address: ' + result.regLocation;
              } else {
                correctAddress = '<br/>Company Address: No Data';
              }
            }

            if(!nameEqualFlag || !addressEqualFlag){
              var id = FormManager.getActualValue('reqId');
              var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
                ID : id
              });
    
              if ((ret == null || ret.ret1 == null)) {
                return new ValidationResult(null, false, 'Your request is not allowed to be sent for processing if the Chinese Company Name '
                    + 'and Address do not match with D&B 100%. If you insist on using mismatched '
                    + 'Company Name or Address, you need to attach the screenshot of Customer Official Website, '
                    + 'Business License, Government Website, and/or Contract/Purchase Order with signature, and the '
                    + 'file content selected must be "Name and Address Change(China Specific)". The correct information based on the checks:'
                    + correctName + correctAddress);
              } else {
                return new ValidationResult(null, true);
              }
            } else if(nameEqualFlag && addressEqualFlag){
              var rowCount = CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount;
              if (rowCount == 1) {
                var id = FormManager.getActualValue('reqId');
                var ret = cmr.query('CHECK_CN_API_ATTACHMENT', {
                  ID : id
                });
      
                if (ret && ret.ret1 && ret.ret1 != '') {
                  return new ValidationResult(null, false, 'Your request is not allowed to be sent for processing if the Chinese Company Name '
                      + 'and Address match with D&B 100%, but you still added an attachment of type  "Name and Address Change(China Specific)". <br>Please remove this Attachment, then try again.'
                      );
                }else{
                  return new ValidationResult(null, true);
                }
              } else if (rowCount > 1) {
                // var addrDiffIndc = checkAddrDiffIndc();
                return new ValidationResult(null, true);
              }
            }else {
              return new ValidationResult(null, true);
            }
          } else {
          return new ValidationResult(null, true);
          }
        }
      }
    }
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function addressValidation(address2SBCS, apiCity, cnAddress, cnCity, cnDistrictZS01){

  var cityReg = /.+?(市)/g;
  var cityDNB = address2SBCS.match(cityReg) ? address2SBCS.match(cityReg)[0] : '';
  var rmCityDnb = address2SBCS.replace(cityDNB,'');
  var rmCityAddr = cnAddress.replace(cityDNB,'');

  var district = cnDistrictZS01.substr(-1) == '区' ? cnDistrictZS01 : cnDistrictZS01 + '区';
  var districtReg = /.+?(区)/g;
  var districtDNB = rmCityDnb.match(districtReg) ? rmCityDnb.match(districtReg)[0] : '';
  var rmDistrictDnb = rmCityDnb.replace(districtDNB,'');
  var rmDistrictAddr = rmCityAddr.replace(districtDNB,'');
  var rmParenthesis = rmDistrictDnb.replace(/\([^\)]*\)/g,'');
  var addressEqualFlag = false;
  if (address2SBCS == cnAddress && (cnDistrictZS01 == '' || districtDNB == district)) {
    addressEqualFlag = true;
  } else if(rmDistrictDnb == rmDistrictAddr){
    // this check is to add the D&B format of Street = District + Street
    if(district == '区' || districtDNB == district) {
      addressEqualFlag = true;
    } else {
      addressEqualFlag = false;
    }
  } else if(rmParenthesis == rmDistrictAddr) {
    // to validate after removing dnb () if dnb address contains parenthesis
    if(district == '区' || districtDNB == district) {
      addressEqualFlag = true;
    } else {
      addressEqualFlag = false;
    }
  } else if(address2SBCS == cnAddress && apiCity == '市辖区' && address2SBCS.indexOf(cnCityZS01) >= 0) {
      addressEqualFlag = true;
  } else {
    addressEqualFlag = false;  
  }
  return addressEqualFlag;
}

function validateCnNameAndAddr4Update() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var action = FormManager.getActualValue('yourAction');
        if(action == 'SFP'){

          if (FormManager.getActualValue('reqType') != 'U') {
            return new ValidationResult(null, true);
          }
          console.log("running validateCnNameAndAddr4Update . . .");
          
          // 1, no more than 1 ZS01.
          // 2, if ZS01 only, then check TianYanCha to ask for attached doc if
          // not 100% match.
          // 3, if more than 1 addr records, then need Chinese special
          // attachment if other addr is not same with zs01.
          // 4, if more than 1 addr records, remove Chinese special attachment
          // if zs01 CN name and addr 100% match TianYanCha.
          
          var addrList = [];
          var addrRdcList = [];
          var cnAddrList = [];
          var cnAddrRdcList = [];
          
          // 'failInd = true' means otherAddressUpdated and not sameWithZs01
          var failInd = false;
          var zs01AddressUpdated = false;
          var otherAddressUpdated = false;
          
          var zs01Count = 0;
          
          var addrTxtZS01 = '';
          var addrTxt2ZS01 = '';
          
          var cnCustName1ZS01 = '';
          var cnCustName2ZS01 = '';
          var cnCustName4ZS01 = '';
          var cnAddrTxtZS01 = '';
          var cnAddrTxt2ZS01 = '';
          
          var addrTypeOther = '';
          var addrSeqOther = '';
          
          var addrTxtOther = '';
          var addrTxt2Other = '';
          var cnAddrTxtOther = '';
          var cnAddrTxt2Other = '';
          var cnCityZS01 = '';
          var cnDistrictZS01 = '';
          
          var reqReason = FormManager.getActualValue('reqReason');
          
          if (reqReason == 'TREC') {
            return new ValidationResult(null, true);
          }
          
          addrList = getAddrList();
          addrRdcList = getAddrRdcList();
          cnAddrList = getCnAddrList();
          cnAddrRdcList = getCnAddrRdcList();
          
          for (var i=0; i< addrList.length; i++) {
            if (addrList[i].addrType == 'ZS01') {
              addrTxtZS01 = addrList[i].addrTxt;
              addrTxt2ZS01 = addrList[i].addrTxt2;
            }
          }
          
          for (var i=0; i< cnAddrList.length; i++) {
            if (cnAddrList[i].addrType == 'ZS01') {
              cnCustName1ZS01 = cnAddrList[i].cnCustNm1;
              cnCustName2ZS01 = cnAddrList[i].cnCustNm2;
              cnCustName4ZS01 = cnAddrList[i].cnAddrTxt2;
              cnAddrTxtZS01 = cnAddrList[i].cnAddrTxt;
              cnAddrTxt2ZS01 = cnAddrList[i].cnAddrTxt2;
              cnCityZS01 = cnAddrList[i].cnCity;
            }
          }
          
          // get cnDistrictZS01
          for (var i = 0; i < CmrGrid.GRIDS.ADDRESS_GRID_GRID.rowCount; i++) {
            var record = CmrGrid.GRIDS.ADDRESS_GRID_GRID.getItem(i);
            var type = record.addrType;
            if (typeof (type) == 'object') {
              type = type[0];
            }
            if (type == 'ZS01') {
              cnDistrictZS01 = record.cnDistrict;
            }
            if (typeof (cnDistrictZS01) == 'object') {
              if (cnDistrictZS01[0] != '' && cnDistrictZS01[0] != null) {
                cnDistrictZS01 = cnDistrictZS01[0];
              } else {
                cnDistrictZS01 = '';
              }
            }
          }
          
          if (addrList.length > 1) {
            for (var i=0; i< addrList.length; i++) {
              if (addrList[i].addrType != 'ZS01') {
                if (isChangedAddress(addrList[i].addrType, addrList[i].addrSeq, addrList, addrRdcList, cnAddrList, cnAddrRdcList)) {
                  otherAddressUpdated = true;
                  addrTxtOther = addrList[i].addrTxt;
                  addrTxt2Other = addrList[i].addrTxt2;
                  cnAddrTxtOther = getAddrValue('cnAddrTxt', addrList[i].addrType, addrList[i].addrSeq, cnAddrList);
                  cnAddrTxt2Other = getAddrValue('cnAddrTxt2', addrList[i].addrType, addrList[i].addrSeq, cnAddrList);
                }
                
                if (otherAddressUpdated) {
                  if ((addrTxtZS01.toUpperCase() != addrTxtOther.toUpperCase()) 
                      || (addrTxt2ZS01.toUpperCase() != addrTxt2Other.toUpperCase()) 
                      || (cnAddrTxtZS01.toUpperCase() != cnAddrTxtOther.toUpperCase()) 
                      || (cnAddrTxt2ZS01.toUpperCase() != cnAddrTxt2Other.toUpperCase())) {
                    failInd = true;
                  }
                }
              }
            }
          }

          var dnbResult = {};
          
          if (zs01Count > 1) {
            return new ValidationResult(null, false, 'Only one Sold-To Address can be defined.');
          }
          
          var cnAttachFlag = checkCnAttach();
          
          // check TianYanCha via Social Credit Code
          var cnName = convert2SBCS(cnCustName1ZS01 + cnCustName2ZS01);
          var cnAddress = convert2SBCS(cnAddrTxtZS01 + cnAddrTxt2ZS01);
          var cnCity = convert2SBCS(cnCityZS01);
          dnbResult = checkDnb(cnName, cnAddress, cnCity);
          // if ($.isEmptyObject(tycResultByBusnType)) {
          // // check TianYanCha via cn name
          // tycResultByCnNm = checkTycViaCnNm(cnName);
          // tycResult = tycResultByCnNm;
          // }
          
          // **** handle validation here ****
          if (addrList != null && addrList.length == 1) {
            // case 1, single address logic
            var cnAddress = convert2SBCS(cnAddrTxtZS01 + cnAddrTxt2ZS01);
            var apiName2SBCS = convert2SBCS(dnbResult.name);
            var apiAddress2SBCS = convert2SBCS(dnbResult.regLocation);
            var apiCity = '';
            var apiDistrict = '';
            
            var nameEqualFlag = true;
            var addressEqualFlag = true;
            
            if(dnbResult.city != null){
              apiCity = dnbResult.city;
            }
            if(dnbResult.district != null){
              apiDistrict = dnbResult.district;
            }

            var correctName = '';
            var correctAddress = '';
            
            if (apiName2SBCS != cnName) {
              nameEqualFlag = false;
              if(!$.isEmptyObject(dnbResult)){
                correctName = '<br/>Company Name: ' + dnbResult.name;
              } else {
                correctName = '<br/>Company Name: No Data';
              }
            }
            addressEqualFlag = addressValidation(apiAddress2SBCS, apiCity, cnAddress, cnCity, cnDistrictZS01);
            if(!addressEqualFlag){
              var rmProvince = cnAddress.replace(/.+?(省)/,'');
              var rmCity = rmProvince.replace(/.+?(市)/,'');
              addressEqualFlag = addressValidation(apiAddress2SBCS, apiCity, rmCity, cnCity, cnDistrictZS01);
            }

            if (!addressEqualFlag){
              console.log('Address mismatch: '+apiAddress2SBCS+' : '+cnAddress);
              if(!$.isEmptyObject(dnbResult)){
                correctAddress = '<br/>Company Address: ' + dnbResult.regLocation;
              } else {
                correctAddress = '<br/>Company Address: No Data';
              }
            }

            if(!nameEqualFlag || !addressEqualFlag){
              if(!cnAttachFlag){
                return new ValidationResult(null, false, 'Your request is not allowed to be sent for processing if the Chinese Company Name '
                    + 'and Address do not match with D&B 100%. If you insist on using mismatched '
                    + 'Company Name or Address, you need to attach the screenshot of Customer Official Website, '
                    + 'Business License, Government Website, Contract/Purchase order with signature, and '
                    + 'the file content selected must be "Name and Address Change(China Specific)". The correct information based on checks: '
                    + correctName + correctAddress);
              } else {
                return new ValidationResult(null, true);
              }
            } else if(nameEqualFlag && addressEqualFlag){
              if(cnAttachFlag){
                return new ValidationResult(null, false, 'Your request is not allowed to be sent for processing if the Chinese Company Name '
                    + 'and Address match with D&B 100%, but you still added an attachment of type "Name and Address Change(China Specific)". Please remove this Attachment then try again.'
                    );
              } else {
                return new ValidationResult(null, true);
              }
            } else {
              return new ValidationResult(null, true);
            }
          } else if (addrList != null && addrList.length > 1) {
            // case 2, multiple addresses logic
            var cnAddress = convert2SBCS(cnAddrTxtZS01 + cnAddrTxt2ZS01);
            var apiName2SBCS = convert2SBCS(dnbResult.name);
            var apiAddress2SBCS = convert2SBCS(dnbResult.regLocation);
            var apiCity = '';
            var apiDistrict = '';
            
            var nameEqualFlag = true;
            var addressEqualFlag = true;
            
            if(dnbResult.city != null){
              apiCity = dnbResult.city;
            }
            if(dnbResult.district != null){
              apiDistrict = dnbResult.district;
            }

            var correctName = '';
            var correctAddress = '';
            
            if (apiName2SBCS != cnName) {
              nameEqualFlag = false;
              if(!$.isEmptyObject(dnbResult)){
                correctName = '<br/>Company Name: ' + dnbResult.name;
              } else {
                correctName = '<br/>Company Name: No Data';
              }
            }
            addressEqualFlag = addressValidation(apiAddress2SBCS, apiCity, cnAddress, cnCity, cnDistrictZS01);
            if(!addressEqualFlag){
              var rmProvince = cnAddress.replace(/.+?(省)/,'');
              var rmCity = rmProvince.replace(/.+?(市)/,'');
              addressEqualFlag = addressValidation(apiAddress2SBCS, apiCity, rmCity, cnCity, cnDistrictZS01);
            }

            if (!addressEqualFlag){
              console.log('Address mismatch: '+apiAddress2SBCS+' : '+cnAddress);
              if(!$.isEmptyObject(dnbResult)){
                correctAddress = '<br/>Company Address: ' + dnbResult.regLocation;
              } else {
                correctAddress = '<br/>Company Address: No Data';
              }
            }

            if(!nameEqualFlag || !addressEqualFlag){
              if(!cnAttachFlag){
                return new ValidationResult(null, false, 'Your request is not allowed to be sent for processing if the Chinese Company Name '
                    + 'and Address do not match with D&B 100%. If you insist on using mismatched '
                    + 'Company Name or Address, you need to attach the screenshot of Customer Official Website, '
                    + 'Business License, Government Website, Contract/Purchase order with signature, and the '
                    + 'file content selected must be "Name and Address Change(China Specific)". The correct information based on checks: '
                    + 'should be:'
                    + correctName + correctAddress);
              } else {
                return new ValidationResult(null, true);
              }
            } else if(nameEqualFlag && addressEqualFlag){
              // 100% match TianYanCha logic
              // 1, should add CN attachment -
              // 1.1, when 'failInd' is true, that means other address is not
              // same with ZS01.
              // 2, should remove CN attachment -
              // 2.1, when other address is same with ZS01
              // 2.2, when English/Chinese address is not changed
              // and address match TianYanCha
              if (cnAttachFlag) {
                if (!failInd) {
                  return new ValidationResult(null, false, 'Your request is not allowed to be sent for processing if the Chinese Company Name '
                      + 'and Address match with D&B 100%, but you still added an attachment of type "Name and Address Change(China Specific)". Please remove this Attachment then try again.'
                      );
                }
                return new ValidationResult(null, true);
              } else {
                if (failInd) {
                  return new ValidationResult(null, false, 'The additional address should be same with Sold to address (ZS01).'
                      + ' If you insist on using a different address from the Sold to (ZS01), you need to attach the screenshot of Customer Official Website,'
                      + ' Business License, Government Website, Contract/Purchase order with signature, and the file content selected must be "Name and Address Change(China Specific)". ');
                }
                return new ValidationResult(null, true);
              }
              return new ValidationResult(null, true);
            } else {
              return new ValidationResult(null, true);
            }
          }
          return new ValidationResult(null, true);
        }
      }
    }
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function checkAddrDiffIndc() {
  var diffIndc = false;
  
  var addrList = [];
  var cnAddrList = [];
  
  var failInd = false;
  var zs01AddressUpdated = false;
  var otherAddressUpdated = false;
  
  var zs01Count = 0;
  
  var addrTxtZS01 = null;
  var addrTxt2ZS01 = null;
  var cnAddrTxtZS01 = null;
  var cnAddrTxt2ZS01 = null;
  
  var addrTypeOther = null;
  var addrSeqOther = null;
  
  var addrTxtOther = null;
  var addrTxt2Other = null;
  var cnAddrTxtOther = null;
  var cnAddrTxt2Other = null;
  
  // get addr
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
      _qall : 'Y',
      REQ_ID : reqId ,
     };
  var addrResults = cmr.query('GET.ADDR_BY_REQID', qParams);
  if (addrResults != null) {
    for (var i = 0; i < addrResults.length; i++) {
      var addr  = {
          reqId : addrResults[i].ret1,
          addrType : addrResults[i].ret2,
          addrSeq : addrResults[i].ret3,
          custNm1 : addrResults[i].ret4,
          custNm2 : addrResults[i].ret5,
          addrTxt : addrResults[i].ret6,
          addrTxt2 : addrResults[i].ret7,
          city1 : addrResults[i].ret8,
      };
      addrList.push(addr);
    }
  }
  
  // get intl_addr
  var intlAddrResults = cmr.query('GET.INTLADDR_BY_REQID', qParams);
  if (intlAddrResults != null) {
    for (var i = 0; i < intlAddrResults.length; i++) {
      var cnAddr  = {
          reqId : intlAddrResults[i].ret1,
          addrType : intlAddrResults[i].ret2,
          addrSeq : intlAddrResults[i].ret3,
          cnCustName1 : intlAddrResults[i].ret4,
          cnCustName2 : intlAddrResults[i].ret5,
          cnAddrTxt : intlAddrResults[i].ret6,
          cnAddrTxt2 : intlAddrResults[i].ret7,
          cnCity1 : intlAddrResults[i].ret8,
      };
      cnAddrList.push(cnAddr);
    }
  }
  
  // check if other addr is diffrent with ZS01 in addr
  if (addrList != null) {
    for (var i=0; i< addrList.length; i++) {
      if (addrList[i].addrType == 'ZS01') {
        addrTxtZS01 = addrList[i].addrTxt;
        addrTxt2ZS01 = addrList[i].addrTxt2;
      }
    }
    for (var i=0; i< addrList.length; i++) {
      if (addrList[i].addrType != 'ZS01') {
        addrTxtOther = addrList[i].addrTxt;
        addrTxt2Other = addrList[i].addrTxt2;
      }
      if ((addrTxtZS01.toUpperCase() != addrTxtOther.toUpperCase()) 
          || (addrTxt2ZS01.toUpperCase() != addrTxt2Other.toUpperCase())) {
        diffIndc = true;
      }
    }
  }
  
  // check if other addr is diffrent with ZS01 in intl_addr
  if (cnAddrList != null) {
    for (var i=0; i< cnAddrList.length; i++) {
      if (cnAddrList[i].addrType == 'ZS01') {
        cnAddrTxtZS01 = cnAddrList[i].cnAddrTxt;
        cnAddrTxt2ZS01 = cnAddrList[i].cnAddrTxt2;
      }
    }
    for (var i=0; i< cnAddrList.length; i++) {
      if (cnAddrList[i].addrType != 'ZS01') {
        cnAddrTxtOther = cnAddrList[i].cnAddrTxt;
        cnAddrTxt2Other = cnAddrList[i].cnAddrTxt2;
      }
      if ((cnAddrTxtZS01.toUpperCase() != cnAddrTxtOther.toUpperCase()) 
          || (cnAddrTxt2ZS01.toUpperCase() != cnAddrTxt2Other.toUpperCase())) {
        diffIndc = true;
      }
    }
  }
  
  // output
  if (diffIndc == true) {
    return true;
  } else {
    return false;
  }
}

function getAddrList() {
  var addrList = [];
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
      _qall : 'Y',
      REQ_ID : reqId ,
     };
  var results = cmr.query('GET.ADDR_BY_REQID', qParams);
  if (results != null) {
    for (var i = 0; i < results.length; i++) {
      var addr  = {
          reqId : results[i].ret1,
          addrType : results[i].ret2,
          addrSeq : results[i].ret3,
          custNm1 : results[i].ret4,
          custNm2 : results[i].ret5,
          addrTxt : results[i].ret6,
          addrTxt2 : results[i].ret7,
          city1 : results[i].ret8,
      };
      addrList.push(addr);
    }
  }
  return addrList;
}

function getAddrRdcList() {
  var addrRdcList = [];
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
      _qall : 'Y',
      REQ_ID : reqId ,
     };
  var results = cmr.query('GET.ADDRRDC_BY_REQID', qParams);
  if (results != null) {
    for (var i = 0; i < results.length; i++) {
      var addr  = {
          reqId : results[i].ret1,
          addrType : results[i].ret2,
          addrSeq : results[i].ret3,
          custNm1 : results[i].ret4,
          custNm2 : results[i].ret5,
          addrTxt : results[i].ret6,
          addrTxt2 : results[i].ret7,
          city1 : results[i].ret8,
      };
      addrRdcList.push(addr);
    }
  }
  return addrRdcList;
}

function getCnAddrList() {
  var cnAddrList = [];
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
      _qall : 'Y',
      REQ_ID : reqId ,
     };
  var results = cmr.query('GET.INTLADDR_BY_REQID', qParams);
  if (results != null) {
    for (var i = 0; i < results.length; i++) {
      var addr  = {
          reqId : results[i].ret1,
          addrType : results[i].ret2,
          addrSeq : results[i].ret3,
          cnCustNm1 : results[i].ret4,
          cnCustNm2 : results[i].ret5,
          cnAddrTxt : results[i].ret6,
          cnAddrTxt2 : results[i].ret7, // custNm4
          cnCity1 : results[i].ret8,
      };
      cnAddrList.push(addr);
    }
  }
  return cnAddrList;
}

function getCnAddrRdcList() {
  var cnAddrRdcList = [];
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
      _qall : 'Y',
      REQ_ID : reqId ,
     };
  var results = cmr.query('GET.INTLADDRRDC_BY_REQID', qParams);
  if (results != null) {
    for (var i = 0; i < results.length; i++) {
      var addr  = {
          reqId : results[i].ret1,
          addrType : results[i].ret2,
          addrSeq : results[i].ret3,
          cnCustNm1 : results[i].ret4,
          cnCustNm2 : results[i].ret5,
          cnAddrTxt : results[i].ret6,
          cnAddrTxt2 : results[i].ret7, // custNm4
          cnCity1 : results[i].ret8,
      };
      cnAddrRdcList.push(addr);
    }
  }
  return cnAddrRdcList;
}

function checkCnAttach() {
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
      ID : reqId
     };
  var result = cmr.query('CHECK_CN_API_ATTACHMENT', qParams);
  if (result != null && result.ret1 != null) {
    return true;
  }
  return false;
}

function checkDnb(cnName, cnAddress, cnCity) {
  var busnType = FormManager.getActualValue('busnType');
  var result = {};
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/cn/dnb.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      busnType : busnType,
      cnName : cnName,
      cnAddress : cnAddress,
      cnCity : cnCity
    },
    timeout : 50000,
    sync : true,
    load : function(data, ioargs) {
      if (data && data.result) {
        result = data.result;
      }
    },
    error : function(error, ioargs) {
      result = {};
    }
  });
  return result;
}

function checkTycViaCnNm(cnName) {
  var busnType = FormManager.getActualValue('busnType');
  var result = {};
  dojo.xhrGet({
    url : cmr.CONTEXT_ROOT + '/cn/dnb.json',
    handleAs : 'json',
    method : 'GET',
    content : {
      busnType : busnType,
      cnName : cnName
    },
    timeout : 50000,
    sync : true,
    load : function(data, ioargs) {
      if (data && data.result) {
        result = data.result;
      }
    },
    error : function(error, ioargs) {
      result = {};
    }
  });
  return result;
}

// function validateSearchTermForCROSS() {
// FormManager.addFormValidator((function() {
// return {
// validate : function() {
// var custSubType = FormManager.getActualValue('custSubGrp');
// var subType = '';
// if (FormManager.getActualValue('reqType') == 'C' && (custSubType == 'CROSS'
// || custSubType == 'NRMLD' || custSubType == 'KYND' ||custSubType ==
// 'EMBSA' ||custSubType == 'AQSTN')) {
// if (custSubType == 'CROSS'){
// subType = 'Foreign';
// } else if(custSubType == 'NRMLC') {
// subType = 'Normal - Select Core';
// } else if(custSubType == 'NRMLD') {
// subType = 'Normal - Signature / Strategic / Dedicated';
// } else if(custSubType == 'KYND') {
// subType = 'Kyndryl';
// } else if(custSubType == 'EMBSA') {
// subType = 'Embedded Solution Agreement (ESA)';
// } else if(custSubType == 'AQSTN') {
// subType = 'Acquisition';
// }
// var _GBGId = FormManager.getActualValue('gbgId');
// var searchTerm = FormManager.getActualValue('searchTerm');
// if (FormManager.getActualValue('gbgId') != 'undefined' &&
// FormManager.getActualValue('gbgId') != '') {
// var ret = cmr.query('CHECK_CN_S1_GBG_ID_LIST', {
// ID : _GBGId
// });
// if (ret == null || ret.ret1 == null || ret.ret1 == 0) {
// if(searchTerm == '04472' || searchTerm == '00260' || searchTerm == '04491' ||
// searchTerm == '04493' || searchTerm == '04687' || searchTerm ==
// '04497'
// || searchTerm == '04629' || searchTerm == '04495' || searchTerm == '04630' ||
// searchTerm == '04484' || searchTerm == '04480' || searchTerm ==
// '04488'
// || searchTerm == '04499' || searchTerm == '04486' || searchTerm == '04747' ||
// searchTerm == '04748' || searchTerm == '04749' || searchTerm ==
// '04502'){
// return new ValidationResult(null, false, 'It is not allowed to apply S1
// search term for none S1 GBGId under ' + subType + ' Sub_scenario.');
// }
// }
// }else{
// if(searchTerm == '04472' || searchTerm == '00260' || searchTerm == '04491' ||
// searchTerm == '04493' || searchTerm == '04687' || searchTerm ==
// '04497'
// || searchTerm == '04629' || searchTerm == '04495' || searchTerm == '04630' ||
// searchTerm == '04484' || searchTerm == '04480' || searchTerm ==
// '04488'
// || searchTerm == '04499' || searchTerm == '04486' || searchTerm == '04747' ||
// searchTerm == '04748' || searchTerm == '04749' || searchTerm ==
// '04502'){
// return new ValidationResult(null, false, 'It is not allowed to apply S1
// search term for none S1 GBGId under ' + subType + ' Sub_scenario.');
// }
// }
//
//
// // var searchTerm = FormManager.getActualValue('searchTerm');
// // var searchTermTxt = $('#searchTerm').val();
// // if (searchTerm == '00000' || searchTerm == '00075' || searchTerm ==
// '08036' || searchTerm == '71300') {
// // return new ValidationResult(null, false, 'It is not allowed to apply for
// default search term:' + searchTerm + ' by ' + subType + '
// Sub_scenario.');
// // } else if(searchTermTxt.indexOf('Expired') >= 0) {
// // return new ValidationResult(null, false, 'It is not allowed to apply for
// default or expired search term for ' + subType + ' Sub_scenario.');
// // }else {
// // return new ValidationResult(null, true);
// // }
// } else {
// return new ValidationResult(null, true);
// }
// }
// };
// })(), 'MAIN_IBM_TAB', 'frmCMR');
// }

function validateISICForCROSS() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var subType = '';
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (_pagemodel.userRole.toUpperCase() == "REQUESTER" && FormManager.getActualValue('reqType') == 'C' && (custSubType == 'CROSS' ||  custSubType == 'NRMLD' || custSubType == 'KYND' ||custSubType == 'EMBSA' 
          || custSubType == 'AQSTN' || custSubType == 'ECOSY' || custSubType == 'MRKT' || custSubType == 'BLUMX')) {
          if (custSubType == 'CROSS'){
            subType = 'Foreign';
          } else if(custSubType == 'NRMLC') {
            subType = 'Normal - Select Core';
          } else if(custSubType == 'NRMLD') {
            subType = 'Normal - Signature / Strategic / Dedicated';
          } else if(custSubType == 'KYND') {
            subType = 'Kyndryl';
          } else if(custSubType == 'EMBSA') {
            subType = 'Embedded Solution Agreement (ESA)';
          } else if(custSubType == 'AQSTN') {
            subType = 'Acquisition';
          } else if(custSubType == 'ECOSY') {
            subType = 'Ecosystem Partners';
          } else if(custSubType == 'MRKT') {
            subType = 'Marketplace';
          } else if(custSubType == 'BLUMX') {
            subType = 'Bluemix';
          }
          var isicCd = FormManager.getActualValue('isicCd');
          if (isicCd == '0000' || isicCd == '8888' || isicCd == '9500') {
            FormManager.enable('isicCd');
            return new ValidationResult(null, false, 'It is not allowed to apply for default ISIC for ' + subType + ' Sub_scenario.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function resetISICCode() {
  var custSubType = FormManager.getActualValue('custSubGrp');
  var field = FormManager.getField('isicCd');

  var isicCodeArray = [ '','0001', '0002', '0003', '0111', '0112', '0113', '0121', '0122', '0130', '0140', '0150', '0200', '0501', '0502', '1010',
      '1020', '1030', '1110', '1120', '1200', '1310', '1320', '1410', '1421', '1422', '1429', '1511', '1512', '1513', '1514', '1520', '1531', '1532',
      '1533', '1541', '1542', '1543', '1544', '1549', '1551', '1552', '1553', '1554', '1600', '1711', '1712', '1721', '1722', '1723', '1729', '1730',
      '1810', '1820', '1911', '1912', '1920', '2010', '2021', '2022', '2023', '2029', '2101', '2102', '2109', '2211', '2212', '2213', '2219', '2221',
      '2222', '2230', '2310', '2320', '2330', '2411', '2412', '2413', '2421', '2422', '2423', '2424', '2429', '2430', '2511', '2519', '2520', '2610',
      '2691', '2692', '2693', '2694', '2695', '2696', '2699', '2710', '2720', '2731', '2732', '2811', '2812', '2813', '2891', '2892', '2893', '2899',
      '2911', '2912', '2913', '2914', '2915', '2919', '2921', '2922', '2923', '2924', '2925', '2926', '2927', '2929', '2930', '3000', '3110', '3120',
      '3130', '3140', '3150', '3190', '3210', '3220', '3230', '3311', '3312', '3313', '3320', '3330', '3410', '3420', '3430', '3511', '3512', '3520',
      '3530', '3591', '3592', '3599', '3610', '3691', '3692', '3693', '3694', '3699', '3710', '3720', '4010', '4020', '4030', '4100', '4510', '4520',
      '4530', '4540', '4550', '4888', '5010', '5020', '5030', '5040', '5050', '5110', '5121', '5122', '5131', '5139', '5141', '5142', '5143', '5149',
      '5151', '5152', '5159', '5190', '5211', '5219', '5220', '5231', '5232', '5233', '5234', '5239', '5240', '5251', '5252', '5259', '5260', '5510',
      '5520', '6010', '6021', '6022', '6023', '6030', '6110', '6120', '6210', '6220', '6301', '6302', '6303', '6304', '6309', '6411', '6412', '6420',
      '6511', '6519', '6591', '6592', '6599', '6601', '6602', '6603', '6711', '6712', '6719', '6720', '7010', '7020', '7111', '7112', '7113', '7121',
      '7122', '7123', '7129', '7130', '7210', '7221', '7229', '7230', '7240', '7250', '7290', '7310', '7320', '7411', '7412', '7413', '7414', '7421',
      '7422', '7430', '7491', '7492', '7493', '7494', '7495', '7499', '7511', '7512', '7513', '7514', '751M', '751P', '7521', '7522', '7523', '752M',
      '752P', '7530', '7700', '7701', '7702', '7703', '7704', '7705', '7706', '7707', '7708', '7709', '7710', '7711', '7712', '7713', '7714', '7715',
      '7716', '7717', '7718', '7719', '7720', '7721', '8010', '8021', '8022', '8030', '8090', '8511', '8512', '8519', '8520', '8531', '8532', '8889',
      '8890', '9000', '9111', '9112', '9120', '9191', '9192', '9199', '9211', '9212', '9213', '9214', '9219', '9220', '9231', '9232', '9233', '9241',
      '9249', '9301', '9302', '9303', '9309', '9801', '9802', '9804', '9806', '9807', '9808', '9811', '9812', '9813', '9814', '9816', '9817', '9818',
      '9819', '9820', '9821', '9822', '9900', '9910', '9911', '9912', '9913', '9914', '9915', '9916', '9917', '9918', '9950', '9993', '9994', '999C',
      '999D', '999E', '999F', '999G', '999I', '999J', '999M', '999O', '999P', '999Q', '999R', '999S', '999X', '999Y' ];
  if (FormManager.getActualValue('reqType') == 'C' && (custSubType == 'CROSS' || custSubType == 'NRMLC' || custSubType == 'NRMLD' || custSubType == 'KYND' || custSubType == 'EMBSA' 
    || custSubType == 'AQSTN' || custSubType == 'ECOSY' || custSubType == 'MRKT' || custSubType == 'BLUMX')) {
    FormManager.limitDropdownValues(field, isicCodeArray);
// FormManager.enable(field);
  }
}

function retrievedValueValidator() {
  console.log("RetrievedValueValidator for REQUESTER...");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var reqType = FormManager.getActualValue('reqType');
        var hasRetrievedValue = FormManager.getActualValue('covBgRetrievedInd') == 'Y';
          if (reqType == 'C' && !hasRetrievedValue && (custSubType=='NRMLC' || custSubType=='AQSTN' || custSubType=='ECOSY')) {
            return new ValidationResult(null, false, 'Retrieve Values button is required action.');
          } else {
            return new ValidationResult(null, true);
          }
        }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function sSDGBGIdValidator() {
  console.log("sSDGBGIdValidator for REQUESTER...");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var reqType = FormManager.getActualValue('reqType');
        if (typeof (_pagemodel) != undefined) {
          var id = FormManager.getActualValue('gbgId');
          if (reqType == 'C'){
            if(id != undefined && id != ''){
              if(custSubType == 'NRMLD'){
                if(!(id == 'GB000YEN' || id == 'GB001A7X' || id == 'GB001CQ3' || id == 'GB300S7F' || id == 'GB001CPY' || id == 'GB001CPW' || id == 'GB001DR4' || id == 'GB001B83'
                  || id == 'GB001CQ2' || id == 'GB001J73' || id == 'GB0018BN' || id == 'GB001A89' || id == 'GB001AUJ' || id == 'GB0018BS' || id == 'GB0018EZ' || id == 'GB227QFM'
                    || id == 'GB0019BN' || id == 'GB0018X2')){
                  return new ValidationResult(null, false, 'Please Select Scenario Sub Type - "Normal - Select Core", as this CMR does not belong to China Signature/Strategic/Dedicate account.');
                }else {
                  return new ValidationResult(null, true);
                }
              }else{
                if(id == 'GB000YEN' || id == 'GB001A7X' || id == 'GB001CQ3' || id == 'GB300S7F' || id == 'GB001CPY' || id == 'GB001CPW' || id == 'GB001DR4' || id == 'GB001B83'
                  || id == 'GB001CQ2' || id == 'GB001J73' || id == 'GB0018BN' || id == 'GB001A89' || id == 'GB001AUJ' || id == 'GB0018BS' || id == 'GB0018EZ' || id == 'GB227QFM'
                    || id == 'GB0019BN' || id == 'GB0018X2' ){
                  if(custSubType != 'EMBSA'){
                    return new ValidationResult(null, false, 'Please select Scenario Sub Type -"Normal - Signature / Strategic / Dedicated", as this CMR belongs to China Signature/Strategic/Dedicate account.');
                  }else{
                    return new ValidationResult(null, true);
                  }
                }else {
                  return new ValidationResult(null, true);
                }
              }
            }else {
              if(custSubType == 'NRMLD'){
                  return new ValidationResult(null, false, 'Please do not select Scenario Sub Type - "Normal - Signature / Strategic / Dedicated"  as this CMR is not belong to China Signature/Strategic/Dedicate account, please select Scenario Sub Type -"Normal - Select Core".');
                }else{
                  return new ValidationResult(null, true);
                }
              }
           }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function s1GBGIdValidator() {
  console.log("s1GBGIdValidator for REQUESTER...");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (typeof (_pagemodel) != undefined) {
          var id = FormManager.getActualValue('gbgId');
          if (custSubType == 'ECOSY' && id != undefined && id != '') {
            var ret = cmr.query('CHECK_CN_S1_GBG_ID_LIST', {
              ID : id
            });

            if (ret && ret.ret1 && ret.ret1 != 0) {
              return new ValidationResult(null, false, 'S&S Account client and subsidiary is not allowed to apply for ecosystem CMR type, pls change other "Scenario Sub-type" in General Tab.');
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

function foreignValidator() {
  console.log("running foreignValidator...");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        if (typeof (_pagemodel) != undefined) {
          if (custSubType == 'CROSS') {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_DNB_MATCH_ATTACHMENT', {
              ID : id
            });

            if (ret == null || ret.ret1 == null || ret.ret1 == 0) {
              return new ValidationResult(null, false, 'Company Proof attachment is required for Foreign Sub_scenario.');
            } else {
              return new ValidationResult(null, true);
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function handleExpiredClusterCN() {
  var reqType = FormManager.getActualValue('reqType');
  if (reqType != 'U' || FormManager.getActualValue('viewOnlyPage') == 'true') {
    return;
  }
  
  var clusterDataRdc = getClusterDataRdc();
  if (clusterDataRdc != null && clusterDataRdc != undefined && clusterDataRdc != '') {
    var clusterExpired = checkClusterExpired(clusterDataRdc);
    if (clusterExpired) {
      FormManager.readOnly('clientTier');
    }
  }
}

function getClusterDataRdc() {
  var clusterDataRdc = '';
  var reqId = FormManager.getActualValue('reqId');
  var qParams = {
    REQ_ID : reqId,
  };
  var result = cmr.query('GET.SEARCH_TERM_DATA_RDC', qParams);
  if (result != null) {
    clusterDataRdc = result.ret1;
  }
  return clusterDataRdc;
}

function addEROAttachmentValidator() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        
        var reqType = FormManager.getActualValue('reqType');
        var custSubType = FormManager.getActualValue('custSubGrp');
        var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
        var reqReason = FormManager.getActualValue('reqReason');
        
        if (typeof (_pagemodel) != 'undefined') {
          if (reqType == 'U' && reqReason == 'TREC' ) {
            var id = FormManager.getActualValue('reqId');
            var ret = cmr.query('CHECK_ERO_ATTACHMENT', {
              ID : id
            });
            if(ret == null || ret.ret1 == null){
              return new ValidationResult(null, false, 'ERO Approval Attachment is required for Temporary Reactivate Reqeusts.');
            } else {
              return new ValidationResult(null, true);
            }
          }
        }
        
      }
    };
  })(), 'MAIN_ATTACH_TAB', 'frmCMR');
}

function reqReasonHandler() {
  var _reqReasonHandler = dojo.connect(FormManager.getField('reqReason'), 'onChange', function(value) {
    var reqReason = FormManager.getActualValue('reqReason');
    checkEmbargoCd(reqReason);
  });
  if (_reqReasonHandler && _reqReasonHandler[0]) {
    _reqReasonHandler[0].onChange();
  }
}

function checkEmbargoCd(value) {
  releaseFieldsFromERO();
  if (value != 'TREC')
    return;
  var reqId = FormManager.getActualValue('reqId');
  var emabargoCd = FormManager.getActualValue('ordBlk');
  var qParams = {
    REQ_ID : reqId
  };
  
  lockFieldsForERO();

  var result = cmr.query('GET.DATA_RDC.EMBARGO_BY_REQID_SWISS', qParams);
  if (result.ret1 == '88') {

  } else {
    FormManager.clearValue('reqReason');
    cmr.showAlert('This Request reason can be chosen only if imported record has 88 embargo code.');
    return;
  }
}

function addChecklistValidatorCN() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        console.log('validating checklist..');
        var checklist = dojo.query('table.checklist');
        var reqReason = FormManager.getActualValue('reqReason');
        
        if (reqReason == 'TREC') {
          return new ValidationResult(null, true);
        }
        
        // local customer name if found
        var localNm = checklist.query('input[name="localCustNm"]');
        if (localNm.length > 0 && localNm[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

        // local customer name if found
        var localAddr = checklist.query('input[name="localAddr"]');
        if (localAddr.length > 0 && localAddr[0].value.trim() == '') {
          return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
        }

        var questions = checklist.query('input[type="radio"]');
        if (questions.length > 0) {
          var noOfQuestions = questions.length / 2;
          var checkCount = 0;
          for (var i = 0; i < questions.length; i++) {
            if (questions[i].checked) {
              checkCount++;
            }
          }
          if (noOfQuestions != checkCount) {
            return new ValidationResult(null, false, 'Checklist has not been fully accomplished. All items are required.');
          }
        }

        // add check for checklist on DB
        var reqId = FormManager.getActualValue('reqId');
        var record = cmr.getRecord('GBL_CHECKLIST', 'ProlifChecklist', {
          REQID : reqId
        });
        if (!record || !record.sectionA1) {
          return new ValidationResult(null, false, 'Checklist has not been registered yet. Please execute a \'Save\' action before sending for processing to avoid any data loss.');
        }
        return new ValidationResult(null, true);
      }
    };
  })(), 'MAIN_CHECKLIST_TAB', 'frmCMR');
}

function addDPLCheckValidatorCN() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var result = FormManager.getActualValue('dplChkResult');
        var reqReason = FormManager.getActualValue('reqReason');
        if (reqReason == 'TREC') {
          return new ValidationResult(null, true);
        }
        console.log('>>> RUNNING WW addDPLCheckValidator');
        if (result == '' || result.toUpperCase() == 'NOT DONE') {
          return new ValidationResult(null, false, 'DPL Check has not been performed yet.');
        } else if (result == '' || result.toUpperCase() == 'ALL FAILED') {
          return new ValidationResult(null, false, 'DPL Check has failed. This record cannot be processed.');
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_NAME_TAB', 'frmCMR');
}


//function setDropdownField2Values() {
//  var cmrIssuingCntry = FormManager.getActualValue('dupIssuingCntryCd');
//  FilteringDropdown.loadItems('reqReason', null, 'lov', 'fieldId=RequestReason&cmrIssuingCntry=_cmrIssuingCntry' + cmrIssuingCntry);
//}

function lockFieldsForERO(){
  console.log('>>>> Lock Fields for ERO Temporary Reactivate >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqReason = FormManager.getActualValue('reqReason');
  if (reqType == 'U' && reqReason == 'TREC') {
//    FormManager.readOnly('abbrevNm');
//    FormManager.readOnly('abbrevLocn');
    FormManager.readOnly('custPrefLang');
    FormManager.readOnly('subIndustryCd');
    FormManager.readOnly('isicCd');
    FormManager.readOnly('rdcComment');
    FormManager.readOnly('vatExempt');
    FormManager.removeValidator('busnType', Validators.REQUIRED);
    FormManager.readOnly('vat');
    FormManager.resetValidations('busnType');
    FormManager.readOnly('enterprise');
    FormManager.readOnly('company');
    FormManager.readOnly('ppsceid');
    FormManager.readOnly('memLvl');
    FormManager.readOnly('dealerNo');
//    FormManager.readOnly('taxCd1');
//    FormManager.readOnly('cmrNo');
//    FormManager.readOnly('cmrOwner');
    FormManager.readOnly('bpRelType');
//    FormManager.readOnly('bpName');
    FormManager.readOnly('busnType');  
    FormManager.readOnly('dunsNo');
  }
}

function releaseFieldsFromERO() {
  console.log('>>>> Releasing fields from ERO Temporary Reactivate >>>>');
  var reqType = FormManager.getActualValue('reqType');
  var role = FormManager.getActualValue('userRole').toUpperCase();
  var custSubGrp = FormManager.getActualValue('custSubGrp');
  var reqReason = FormManager.getActualValue('reqReason');
  if (reqType == 'U' && role == 'REQUESTER' && reqReason != 'TREC') {
    FormManager.enable('abbrevNm');
    // FormManager.enable('abbrevLocn');
      FormManager.enable('custPrefLang');
      FormManager.enable('subIndustryCd');
      FormManager.enable('isicCd');
      FormManager.enable('rdcComment');
      FormManager.enable('vatExempt');
      FormManager.enable('vat');
      FormManager.resetValidations('vat');
      FormManager.enable('enterprise');
      FormManager.enable('company');
      FormManager.enable('ppsceid');
      FormManager.enable('memLvl');
      FormManager.enable('dealerNo');
    // FormManager.enable('taxCd1');
    // FormManager.enable('cmrNo');
    // FormManager.enable('cmrOwner');
      FormManager.enable('bpRelType');
    // FormManager.enable('bpName');
      FormManager.enable('busnType');  
      FormManager.enable('dunsNo');
  }
}

function checkClusterExpired(clusterDataRdc) {
  var cntry = FormManager.getActualValue('cmrIssuingCntry');
  var qParams = {
      ISSUING_CNTRY : cntry,
      AP_CUST_CLUSTER_ID : clusterDataRdc,
  };
  var results = cmr.query('CHECK.CLUSTER', qParams);
  if (results != null && results.ret1 == '1') {
    return false;
  }
  return true;
}

// CREATCMR-7567
function setIsicCdFromDnb() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var custSubType = FormManager.getActualValue('custSubGrp');
        var action = FormManager.getActualValue('yourAction');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var busnType = FormManager.getActualValue('busnType');
        var isicCd = FormManager.getActualValue('isicCd');
        var result = {};
        if (action == 'SFP' && cmrNo && cmrNo.startsWith('P')) {
          if (busnType == null || busnType == '') {
            return;
          } else {
            result = findIsicViaDnb();
            if (!$.isEmptyObject(result)) {
              var isicCdFromDnb = result.ibmIsic;
              if (isicCd != isicCdFromDnb) {
                FormManager.setValue('isicCd', isicCdFromDnb);
                $("#cnisicinfoSpan").show();
              }
            }
          }
        }
      }
    }
  })(), 'MAIN_CUST_TAB', 'frmCMR');
}

function findIsicViaDnb() {
  var issuingCntry = FormManager.getActualValue('cmrIssuingCntry');
  var countryCd = FormManager.getActualValue('landCntry');
  var busnType = FormManager.getActualValue('busnType');
  var dnbResult = {};
  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/quick_search/find.json',
    handleAs : 'json',
    method : 'POST',
    content : {
      issuingCntry : issuingCntry,
      countryCd : 'CN', // countryCd,
      taxCd1 : busnType,
    },
    timeout : 2 * 60000,
    sync : true,
    load : function(data, ioargs) {
      if (data && data.items) {
        data.items.forEach(function(item, i) {
          if (item.recType == 'DNB' && (item.matchGrade == '10' || item.matchGrade == '09')) {
            dnbResult = item;
            return false;
          }
        });
      }
    },
    error : function(error, ioargs) {
      console.log('error');
      console.log(error);
    }
  });
  if(!$.isEmptyObject(dnbResult)) {
    var detailResult = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/quick_search/details.json?dunsNo=' + dnbResult.dunsNo,
      handleAs : 'json',
      method : 'GET',
      content : {},
      timeout : 60000,
      sync : true,
      load : function(data, ioargs) {
        if (data.success && data.data.results) {
          detailResult = data.data.results[0];
        }
      },
      error : function(error, ioargs) {
        console.log('error');
        console.log(error);
      }
    });
  }
  return detailResult;
}

// CREATCMR-7879

function retrievedForCNValidator() {
  console.log("running retrievedForCNValidator...");
  FormManager.addFormValidator((function() {
    return {
      validate : function() {
        var reqType = FormManager.getActualValue('reqType');
        var oldGlcCode = FormManager.getActualValue('geoLocationCd');
        var oldSearchTerm = FormManager.getActualValue('searchTerm');
        var custSubGrp = FormManager.getActualValue('custSubGrp');
        console.log( "old GLC code is ", oldGlcCode);
        if (typeof (_pagemodel) != undefined) {
          if(reqType == 'C' && (custSubGrp=='NRMLC' || custSubGrp=='AQSTN' || custSubGrp=='ECOSY' || custSubGrp == 'EMBSA')) {

            console.log("Checking the GLC match... retrieve value again...")
            var data = CmrServices.getAll('reqentry');
            // var progressShown = true;
            // cmr.hideProgress();
            if (data) {
              console.log(data);
              if (data.error && data.error == 'Y') {
                return new ValidationResult(null, false, 'An error was encountered when retrieving the values.\nPlease contact your system administrator. ');
              } else {
                if (data.glcError) {
                  return new ValidationResult(null, false, 'The following values cannot be retrieved at the moment.:GEO Location Code\nPlease contact your system administrator.');
                } else {
                  var indc = false;
                  if(custSubGrp == 'EMBSA'){
                     var _GBGId = FormManager.getActualValue('gbgId');
                     if (FormManager.getActualValue('gbgId') != undefined && FormManager.getActualValue('gbgId') != '') {
                       var ret = cmr.query('CHECK_CN_S1_GBG_ID_LIST', {
                         ID : _GBGId
                       });
                       if (ret && ret.ret1 && ret.ret1 != 0) {
                         indc = true;
                       }
                     }
                  }
                  if(oldGlcCode != undefined && oldGlcCode != '' && data.glcCode != undefined && data.glcCode != '' && data.glcCode != oldGlcCode && (custSubGrp != 'EMBSA' || !indc)){
                    return new ValidationResult(null, false, 'The GEO Location Code has been changed to ' + data.glcCode +', \nPlease click Retrieve Values button.');
                  }else{
                    return new ValidationResult(null, true);
                  }              
                }
              }
            }
          } else {
            return new ValidationResult(null, true);
          }
        }
      }
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
}

// CREATCMR-8581

function checkCmrUpdateBeforeImport() {
  FormManager.addFormValidator((function() {
    return {
      validate : function() {

        var cntry = FormManager.getActualValue('cmrIssuingCntry');
        var cmrNo = FormManager.getActualValue('cmrNo');
        var reqId = FormManager.getActualValue('reqId');
        var reqType = FormManager.getActualValue('reqType');
        var uptsrdc = '';
        var lastupts = '';

        if (reqType == 'C') {
          // console.log('reqType = ' + reqType);
          return new ValidationResult(null, true);
        }

        var resultsCC = cmr.query('GETUPTSRDC', {
          COUNTRY : cntry,
          CMRNO : cmrNo,
          MANDT : cmr.MANDT
        });

        if (resultsCC != null && resultsCC != undefined && resultsCC.ret1 != '') {
          uptsrdc = resultsCC.ret1;
          // console.log('lastupdatets in RDC = ' + uptsrdc);
        }

        var results11 = cmr.query('GETUPTSADDR', {
          REQ_ID : reqId
        });
        if (results11 != null && results11 != undefined && results11.ret1 != '') {
          lastupts = results11.ret1;
          // console.log('lastupdatets in CreateCMR = ' + lastupts);
        }

        if (lastupts != '' && uptsrdc != '') {
          if (uptsrdc > lastupts) {
            return new ValidationResult(null, false, 'This CMR has a new update , please re-import this CMR.');
          } else {
            return new ValidationResult(null, true);
          }
        } else {
          return new ValidationResult(null, true);
        }
      }
    };
  })(), 'MAIN_GENERAL_TAB', 'frmCMR');
}

dojo.addOnLoad(function() {
  GEOHandler.CN = [ SysLoc.CHINA ];
  console.log('adding CN validators...');
  GEOHandler.enableCustomerNamesOnAddress(GEOHandler.CN);
  GEOHandler.setRevertIsicBehavior(false);
  
  GEOHandler.addAfterConfig(afterConfigForCN, GEOHandler.CN);
  GEOHandler.addAfterConfig(setSocialCreditCdValidator, GEOHandler.CN);
  // GEOHandler.addAfterConfig(limitClientTierValuesOnUpdate, GEOHandler.CN);
  GEOHandler.addAfterConfig(defaultCapIndicator, SysLoc.CHINA);
  GEOHandler.addAfterConfig(defaultGovernmentIndicator, GEOHandler.CN);
  // Checklist
  GEOHandler.addAfterConfig(setChinaChecklistStatus, GEOHandler.CN);
  // GEOHandler.addAfterConfig(addValidationForParentCompanyNo, GEOHandler.CN);
  // DENNIS: COMMENTED BECAUSE OF SCRIPT RUN TIME ISSUES
  GEOHandler.addAfterConfig(addDateValidator, GEOHandler.CN);
  GEOHandler.addAfterConfig(hideTDOFields, GEOHandler.CN);
  GEOHandler.addAfterConfig(onInacTypeChange, GEOHandler.CN);
  GEOHandler.addAfterConfig(setCompanyOnInacCd, GEOHandler.CN);
  GEOHandler.addAfterConfig(updateBPSearchTerm, GEOHandler.CN);
  GEOHandler.addAfterConfig(setReadOnly4Update, GEOHandler.CN);
  GEOHandler.addAfterConfig(setCtcOnIsuCdChangeCN, GEOHandler.CN);
  GEOHandler.addAfterConfig(handleExpiredClusterCN, GEOHandler.CN);
  GEOHandler.addAfterConfig(reqReasonHandler, GEOHandler.CN);
//  GEOHandler.addAfterConfig(setDropdownField2Values, GEOHandler.CN);
  
  GEOHandler.addAfterTemplateLoad(autoSetIBMDeptCostCenter, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(afterConfigForCN, GEOHandler.CN);
  // GEOHandler.addAfterTemplateLoad(addValidationForParentCompanyNo,
  // GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(disableVatExemptForScenarios, GEOHandler.CN);
  // GEOHandler.addAfterTemplateLoad(setPrivacyIndcReqdForProc, GEOHandler.CN);
  // GEOHandler.addAfterTemplateLoad(limitClientTierValuesOnCreate,
  // GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setValuesForScenarios, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setReadOnlyFields, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(updateBPSearchTerm, GEOHandler.CN);
  GEOHandler.addAfterTemplateLoad(setCtcOnIsuCdChangeCN, GEOHandler.CN);
  
  GEOHandler.addAddrFunction(updateMainCustomerNames, GEOHandler.CN);
  GEOHandler.addAddrFunction(autoSetAddrFieldsForCN, GEOHandler.CN);
  GEOHandler.addAddrFunction(showHideCityCN, GEOHandler.CN);
  GEOHandler.addAddrFunction(addMandatoryOnlyForZS01CN, GEOHandler.CN);
  GEOHandler.addAddrFunction(addDoubleByteValidatorCN, GEOHandler.CN);
  
  GEOHandler.addToggleAddrTypeFunction(convertCnCustName1, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnCustName2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnCustName3, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnAddrTxt, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnAddrTxt2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCnDistrict, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCustNm1, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCustNm2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCustNm3, GEOHandler.CN); 
  GEOHandler.addToggleAddrTypeFunction(convertAddrTxt, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertAddrTxt2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertCity2, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertDept, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertBldg, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertOffice, GEOHandler.CN);
  GEOHandler.addToggleAddrTypeFunction(convertPoBox, GEOHandler.CN);
  
  // CREATCMR-8581
  GEOHandler.registerValidator(checkCmrUpdateBeforeImport, GEOHandler.CN,null,true);
  
  GEOHandler.registerValidator(addDPLCheckValidatorCN, GEOHandler.CN, GEOHandler.ROLE_REQUESTER, false, false);
  GEOHandler.registerValidator(addGenericVATValidator(SysLoc.CHINA, 'MAIN_CUST_TAB', 'frmCMR'), [ SysLoc.CHINA ], null, true);
  GEOHandler.registerValidator(addChecklistValidatorCN, GEOHandler.CN);
  GEOHandler.registerValidator(retrievedValueValidator, GEOHandler.CN);
// GEOHandler.registerValidator(isValidDate,GEOHandler.CN);
  GEOHandler.registerValidator(addFailedDPLValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addFastPassAttachmentValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addEROAttachmentValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(setTDOFlagToYesValidator, GEOHandler.CN, GEOHandler.PROCESSOR, false, false);
  GEOHandler.registerValidator(addSoltToAddressValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(addContactInfoValidator, GEOHandler.CN, GEOHandler.REQUESTER, false, false);
  GEOHandler.registerValidator(addCityRequiredOnUpdateValidatorAddrList, GEOHandler.CN, null, true);
  GEOHandler.registerValidator(addSocialCreditCdLengthValidator, GEOHandler.CN, GEOHandler.REQUESTER, true);
  // GEOHandler.registerValidator(addAddrUpdateValidator, GEOHandler.CN, null,
  // true);
  // GEOHandler.registerValidator(validateCnNameAndAddr, GEOHandler.CN, null,
  // false);
  GEOHandler.registerValidator(validateCnNameAndAddr4Create, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(validateCnNameAndAddr4Update, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(addCNDnBMatchingAttachmentValidator, GEOHandler.CN, null, false);
  // GEOHandler.registerValidator(foreignValidator, GEOHandler.CN, null,
  // false,false);
  GEOHandler.registerValidator(addPRIVCustNameValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(checkCNCustomerName, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(addPRIVCustNameSFPValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(validateEnNameForInter, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(validateEnNameInAddrTab, GEOHandler.CN, null, false, false);
  // GEOHandler.registerValidator(validateSearchTermForCROSS, GEOHandler.CN,
  // null, false);
  GEOHandler.registerValidator(validateISICForCROSS, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(s1GBGIdValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(sSDGBGIdValidator, GEOHandler.CN, null, false, false);
  GEOHandler.registerValidator(setIsicCdFromDnb, GEOHandler.CN, null, false);
  GEOHandler.registerValidator(retrievedForCNValidator, GEOHandler.CN, null, false, false);
});
