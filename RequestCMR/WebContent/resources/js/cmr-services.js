var CmrServices = (function() {
  return {
    getStandardCity : function(formId, cmrIssuingCntry) {
      var city = null;
      var param = dojo.formToObject(formId);
      param.cmrIssuingCntry = cmrIssuingCntry;
      console.log(param);
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/cmrservices/CITY.json',
        handleAs : 'json',
        content : param,
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          city = data;
        },
        error : function(error, ioargs) {
          console.log(error);
        }
      });
      return city;
    },
    getCoverage : function(formId) {
      var covId = '';
      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/cmrservices/COV.json',
        handleAs : 'json',
        content : dojo.formToObject(formId),
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          if (data && data.coverageID) {
            covId = data.coverageType + data.coverageID;
          }
        },
        error : function(error, ioargs) {
          console.log(error);
        }
      });
      return covId;
    },
    getBuyingGroup : function(formId) {
      var buyingGroup = '';
      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/cmrservices/GBG.json',
        handleAs : 'json',
        content : dojo.formToObject(formId),
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          if (data && data.buyingGroupID) {
            buyingGroup = data.buyingGroupID;
          }
        },
        error : function(error, ioargs) {
          console.log(error);
        }
      });
      return buyingGroup;
    },
    getGLC : function(formId) {
      var glc = '';
      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/cmrservices/GLC.json',
        handleAs : 'json',
        content : dojo.formToObject(formId),
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          if (data && data.glcCode) {
            glc = data.glcCode;
          }
        },
        error : function(error, ioargs) {
          console.log(error);
        }
      });
      return glc;
    },
    getDuns : function(formId) {
      var dunsNo = '';
      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/cmrservices/DUNS.json',
        handleAs : 'json',
        content : dojo.formToObject(formId),
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          if (data && data.dunsNo) {
            dunsNo = data.dunsNo;
          }
        },
        error : function(error, ioargs) {
          console.log(error);
        }
      });
      return dunsNo;
    },
    getAll : function(formId) {
      var intData = {};
      dojo.xhrPost({
        url : cmr.CONTEXT_ROOT + '/cmrservices/ALL.json',
        handleAs : 'json',
        content : dojo.formToObject(formId),
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          intData = data;
        },
        error : function(error, ioargs) {
          console.log(error);
          initData = {
            "error" : "Y"
          };
        }
      });
      return intData;
    },
    checkDnB : function(dunsNo) {
      var retdata = {};
      var param = {
        dunsNo : dunsNo
      };
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/dnb.json',
        handleAs : 'json',
        content : param,
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          retdata = data;
        },
        error : function(error, ioargs) {
          console.log(error);
          retdata.success = false;
        }
      });
      return retdata;
    },
    checkWTAAS : function(cmrNo,country) {
      var retdata = {};
      var param = {
        cmrNo : cmrNo,
        country : country
      };
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/wtaas.json',
        handleAs : 'json',
        content : param,
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          retdata = data;
        },
        error : function(error, ioargs) {
          console.log(error);
          retdata.success = false;
        }
      });
      return retdata;
    },
  };
})();
