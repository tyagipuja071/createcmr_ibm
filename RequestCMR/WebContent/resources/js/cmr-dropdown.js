dojo.require("dijit.form.FilteringSelect");
dojo.require("dojo.data.ItemFileReadStore");

var _NEWSEARCH = true;
var _DROPDOWNCONNECTS = {};
var FilteringDropdown = (function() {

  var _DROPDOWNS = {};
  var _PENDING_DD = [];
  var _DROPDOWNNAMES = new Array();
  loadFixedItems = function(fieldId, spinnerId, data) {
    if (!_DROPDOWNS[fieldId]) {
      _DROPDOWNS[fieldId] = fieldId;
      _DROPDOWNNAMES.push(fieldId);
    }
    var filteringField = dijit.byId(fieldId);
    if (!filteringField) {
      return;
    }
    // wildcard matching and no auto-complete.
    filteringField.queryExpr = '*${0}*';
    filteringField.autoComplete = false;
    var spinner = dojo.byId(spinnerId);
    if (spinner) {
      spinner.className = 'ibm-spinner-small';
      spinner.style.display = "";
      spinner.title = '';
    }

    if (spinner) {
      spinner.style.display = "none";
    }

    var theStore = null;
    theStore = new dojo.data.ItemFileReadStore({
      data : data.listItems,
      clearOnClose : true
    });
    filteringField.store = theStore;

    if ((typeof (_pagemodel) != 'undefined') && _pagemodel[fieldId]) {
      filteringField.set('value', _pagemodel[fieldId]);
    }
    if ((filteringField.get('value') == '' || filteringField.get('value') == null) && data.listItems.selectedItem != '') {
      filteringField.set('value', data.listItems.selectedItem);
    }

    // if at this point still no value, try assigning others
    if (filteringField.get('value') == '') {
      try {
        if (FilteringDropdown['val_' + fieldId]) {
          filteringField.set('value', FilteringDropdown['val_' + fieldId]);
        }
        if (filteringField.get('value') == '') {
          if (eval('dropdownval_' + fieldId)) {
            var val = eval('dropdownval_' + fieldId + '();');
            filteringField.set('value', val);
          }
        }
      } catch (e) {

      }
    }

    var readOnly = false;
    try {
      if (typeof (TemplateService) != "undefined") {
        if (TemplateService.isHandled(fieldId)) {
          if ((typeof TemplateService != "undefined") && (typeof PageManager != "undefined")) {
            readOnly = TemplateService.isFieldReadOnly(fieldId);
            if ((readOnly || PageManager.isReadOnly()) && !PageManager.isAlwaysAvailable(fieldId)) {
              FormManager.readOnly(fieldId);
            } else {
              FormManager.getField(fieldId).set('readOnly', false);
              dojo.removeClass(dojo.byId(fieldId), 'cmr-readOnly');
            }
          }
        }
      }
    } catch (e) {
      console.log(e);
    }

  }, loadItems = function(fieldId, spinnerId, queryId, params, hasDefault, noValueText) {
    if (FormManager.getField('MAIN_GENERAL_TAB')) {
      FormManager.readOnly('cmrIssuingCntry');
      FormManager.readOnly('reqType');
    }
    if (!fieldId) {
      var queryParams = {};
      queryParams.queryId = queryId;
      if (params) {
        var pairs = params.split('&');
        for (var i = 0; i < pairs.length; i++) {
          var pair = pairs[i].split('=');
          if (pair.length == 2) {
            if (pair[1].substring(0, 1) == '_') {
              var field = pair[1].substring(1);
              var dynamicValue = FormManager.getActualValue(field);
              // no other way to force CMR Issuing Country :(
              if (field == 'cmrIssuingCntry' && dynamicValue == '' && typeof (_pagemodel) != 'undefined') {
                dynamicValue = _pagemodel.cmrIssuingCntry;
              }
              eval('queryParams.' + pair[0] + ' = "' + dynamicValue + '";');
            } else {
              eval('queryParams.' + pair[0] + ' = "' + pair[1] + '";');
            }
          } else if (pair == 'nocache') {
            queryParams.nocache = 'Y';
          }
        }
      }
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/dropdown/' + queryId + "/list.json",
        handleAs : 'json',
        content : queryParams,
        method : 'GET',
        timeout : 50000,
        load : function(data, ioargs) {
        },
        error : function(error, ioargs) {
        }
      });
      return;
    }

    if (!_DROPDOWNS[fieldId]) {
      _DROPDOWNS[fieldId] = fieldId;
      _DROPDOWNNAMES.push(fieldId);
    }
    var filteringField = dijit.byId(fieldId);
    if (!filteringField) {
      return;
    }
    // wildcard matching and no auto-complete.
    filteringField.queryExpr = '*${0}*';
    filteringField.autoComplete = false;
    var spinner = dojo.byId(spinnerId);
    if (spinner) {
      spinner.className = 'ibm-spinner-small';
      spinner.style.display = "";
      spinner.title = '';
    }
    filteringField.setDisabled(true);
    var queryParams = {};
    queryParams.queryId = queryId;
    if (params) {
      var pairs = params.split('&');
      for (var i = 0; i < pairs.length; i++) {
        var pair = pairs[i].split('=');
        if (pair.length == 2) {
          if (pair[1].substring(0, 1) == '_') {
            var field = pair[1].substring(1);
            var dynamicValue = FormManager.getActualValue(field);
            // no other way to force CMR Issuing Country :(
            if (field == 'cmrIssuingCntry' && dynamicValue == '' && typeof (_pagemodel) != 'undefined') {
              dynamicValue = _pagemodel.cmrIssuingCntry;
            }
            eval('queryParams.' + pair[0] + ' = "' + dynamicValue + '";');
          } else {
            eval('queryParams.' + pair[0] + ' = "' + pair[1] + '";');
          }
        } else if (pair == 'nocache') {
          queryParams.nocache = 'Y';
        }
      }
      // To avoid fetching incorrect data from cached values
      if (queryParams.fieldId == 'RequestReason' && !queryParams.cmrIssuingCntry) {
        queryParams.cmrIssuingCntry = '*';
      }
    }
    _PENDING_DD.push(fieldId);
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/dropdown/' + queryId + "/list.json",
      handleAs : 'json',
      content : queryParams,
      method : 'GET',
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (spinner) {
          spinner.style.display = "none";
        }
        if (data && (!data.listItems || data.listItems.length == 0)) {
          if (noValueText) {
            var ph = dojo.query('div#widget_' + fieldId).query('span.dijitPlaceHolder');
            if (ph && ph[0]) {
              ph[0].innerHTML = noValueText;
            }
          }
          data.listItems = {
            identified : 'id',
            label : 'name',
            items : [],
            selectedItem : null
          };
        }

        filteringField.setDisabled(false);
        var theStore = null;
        theStore = new dojo.data.ItemFileReadStore({
          data : data.listItems,
          clearOnClose : true
        });
        filteringField.store = theStore;
        filteringField.loadedStore = theStore;

        if ((typeof (_pagemodel) != 'undefined') && _pagemodel[fieldId]) {
          filteringField.set('value', _pagemodel[fieldId]);
        }

        var itemName = filteringField.get('name');
        if (itemName && (typeof (_pagemodel) != 'undefined') && _pagemodel[itemName]) {
          filteringField.set('value', _pagemodel[itemName]);
        }
        if ((filteringField.get('value') == '' || filteringField.get('value') == null) && data.listItems.selectedItem != '') {
          filteringField.set('value', data.listItems.selectedItem);
        }

        // if at this point still no value, try assigning others
        if (filteringField.get('value') == '') {
          try {
            if (FilteringDropdown['val_' + fieldId]) {
              filteringField.set('value', FilteringDropdown['val_' + fieldId]);
            }
            if (filteringField.get('value') == '') {
              if (eval('dropdownval_' + fieldId)) {
                var val = eval('dropdownval_' + fieldId + '();');
                filteringField.set('value', val);
              }
            }
          } catch (e) {

          }
        }

        var readOnly = false;
        try {
          if (typeof (TemplateService) != "undefined") {
            if (TemplateService.isHandled(fieldId)) {
              if ((typeof TemplateService != "undefined") && (typeof PageManager != "undefined")) {
                readOnly = TemplateService.isFieldReadOnly(fieldId);
                if ((readOnly || PageManager.isReadOnly()) && !PageManager.isAlwaysAvailable(fieldId)) {
                  FormManager.readOnly(fieldId);
                } else {
                  FormManager.getField(fieldId).set('readOnly', false);
                  dojo.removeClass(dojo.byId(fieldId), 'cmr-readOnly');
                }
              }
            }
          }
        } catch (e) {
          console.log(e);
        }
        var fIndex = _PENDING_DD.indexOf(fieldId);
        if (fIndex >= 0) {
          _PENDING_DD.splice(fIndex, 1);
        }

      },
      error : function(error, ioargs) {
        if (spinner) {
          spinner.className = 'cmr-filter-err';
          spinner.style.display = "";
          spinner.title = 'Some error occurred while getting the values. Please refresh your page once.';
        }
        var fIndex = _PENDING_DD.indexOf(fieldId);
        if (fIndex >= 0) {
          _PENDING_DD.splice(fIndex, 1);
        }
      }
    });

  };

  loadOnChange = function(fieldId, spinnerId, queryId, params, sourceFieldId, extraFunction) {
    var srcField = dijit.byId(sourceFieldId);
    if (!srcField) {
      return;
    }
    dijit.byId(fieldId).loadedOnChange = true;
    dijit.byId(fieldId).setDisabled(true);
    var connect = dojo.connect(srcField, "onChange", createOnChangeHandler(fieldId, spinnerId, queryId, params, sourceFieldId, extraFunction).handler);
    if (!_DROPDOWNCONNECTS[sourceFieldId]) {
      _DROPDOWNCONNECTS[sourceFieldId] = new Array();
    }
    _DROPDOWNCONNECTS[sourceFieldId].push(connect);
  };

  createOnChangeHandler = function(fieldId, spinnerId, queryId, params, sourceFieldId, extraFunction) {
    var _field = fieldId;
    var _spinner = spinnerId;
    var _query = queryId;
    var _params = params;
    var _src = sourceFieldId;
    var _extra = extraFunction;
    return {
      handler : function() {
        dijit.byId(_field).reset();
        if (dijit.byId(_src).get('value') == '' || !dijit.byId(_src).get('value')) {
          dijit.byId(_field).setDisabled(true);
        } else {
          loadItems(_field, _spinner, _query, _params, false);
        }
        if (_extra) {
          _extra(_src, _field);
        }
      }
    };
  };

  pending = function() {
    return _PENDING_DD != null && _PENDING_DD.length != 0;
  };

  return this;
})();