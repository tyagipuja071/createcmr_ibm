/*
 * File: cmr-scenarios_us_la.js
 * Description:
 * Refactor of the scenarios framework based on the original to not affect LA and US
 */

var TemplateService = (function() {
  console.log('Using Legacy LA/US scenarios framework');
  var currentTemplate = null;
  return {
    init : function() {

    },
    initLoaded : function() {
      return false;
    },
    fill : function(formId) {
      dojo.xhrGet({
        url : cmr.CONTEXT_ROOT + '/templates.json',
        handleAs : 'json',
        method : 'GET',
        content : dojo.formToObject(formId),
        timeout : 50000,
        load : function(data, ioargs) {
          if (data && data.template) {
            TemplateService.loadTemplate(data.template, formId);
          }
        },
        error : function(error, ioargs) {
          console.log(error);
          // NOOP
        }
      });
    },

    loadTemplate : function(template, formId) {
      if (PageManager.isReadOnly()) {
        return;
      }
      console.log('loading template..');
      currentTemplate = template;
      var fields = template.fields;
      var values = null;
      var name = '';
      var type = '';
      var retain = false;
      var field = null;
      var id = '';
      var onParentForm = false;
      var create = FormManager.getActualValue('reqType') != 'U';
      if (fields && fields.length > 0) {
        // reset behavior first
        revertISICBehavior();

        for ( var i = 0; i < fields.length; i++) {
          field = fields[i];
          name = field.fieldName;
          id = field.fieldId;
          values = field.values;
          retain = field.retainValue;
          onParentForm = !field.addressField;

          retain = true; // no retention for updates now
          if (!onParentForm) {
            // skip non-main page fields
            continue;
          }

          if (!FormManager.getField(name)) {
            continue;
          }
          // remove first any shown button
          if (dojo.byId('templatevalue-' + name) != null) {
            dojo.query('#templatevalue-' + name).remove();
          }

          // determine field type
          type = FormManager.getField(name).type;
          if (!type) {
            type = FormManager.getField(name).get('type');
          }

          // remove any current validations
          FormManager.resetValidations(name);

          if (values && values.length == 1) {
            if (values[0] == '*') {
              // case when the value has *, meaning enable, make required, allow
              // input

              // special case for allow any. do not set to readOnly and set to
              // mandatory
              FormManager.enable(name);
              if (create || !retain) {
                if (typeof (_pagemodel) != 'undefined' && ((_pagemodel[name] == null) || (_pagemodel[name] == ''))) {
                  // no saved value, clear this field
                  FormManager.setValue(name, '');
                }
                if (type == 'checkbox') {
                  if (FormManager.getField(name).set) {
                    FormManager.getField(name).set('checked', false);
                  } else if (FormManager.getField(name)) {
                    FormManager.getField(name).checked = false;
                  }
                }
              }

              var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);

            } else if (name == 'isicCd' && values[0].indexOf('%') >= 0 || values[0].indexOf('LOV') == 0 || values[0].indexOf('^') >= 0 || values[0].indexOf('-') > 0) {

              // has wildcard, LOV, exclusions, or range, dirty dirty fix for
              // ISIC/subindustry :( grr

              // 1. disconnect the subIndustryCd handler
              if (_DROPDOWNCONNECTS && _DROPDOWNCONNECTS.subIndustryCd) {
                dojo.disconnect(_DROPDOWNCONNECTS.subIndustryCd[0]);
                _DROPDOWNCONNECTS.subIndustryCd = null;
              }

              // 2. load isic,
              FormManager.setValue(name, '');
              FilteringDropdown['val_isicCd'] = null;
              FilteringDropdown.loadItems('isicCd', 'isicCd_spinner', 'bds', 'fieldId=ISIC&cmrIssuingCntry=_cmrIssuingCntry&isicCd=' + values[0] + '&nocache=y');

              var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
              console.log('enable: ' + name);
              FormManager.enable(name);

              // 3. load subindustry

              FormManager.setValue('subIndustryCd', '');
              FilteringDropdown['val_subIndustryCd'] = null;
              FilteringDropdown.loadOnChange('subIndustryCd', 'subIndustryCd_spinner', 'bds', 'fieldId=Subindustry&cmrIssuingCntry=_cmrIssuingCntry&nocache=y&isicCd=_isicCd', 'isicCd');

              label = dojo.byId('cmr-fld-lbl-Subindustry') ? dojo.byId('cmr-fld-lbl-Subindustry').innerHTML : 'Subindustry';
              FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ label ], field.parentTab);
              FormManager.enable('subIndustryCd');
            } else if (values[0].startsWith("COPY:")) {
              var fname2 = values[0];
              fname2 = fname2.substring(5);
              fname2 = fname2.replace("##", "");
              if (null != fname2 && fname2 != '') {
                var val = FormManager.getActualValue(fname2);
                if (type == 'text') {
                  var max = FormManager.getField(name).maxLength;
                  if (!max && field.get) {
                    max = field.get('maxlength');
                  }
                  val = val.substring(0, Number(max));
                }
                if (create || !retain) {
                  if (typeof (_pagemodel) != 'undefined') {
                    FormManager.setValue(name, val);
                    FormManager.readOnly(name);
                  }
                }
                if (max && !isNaN(max) && Number(max) > 0) {
                  FormManager.addValidator(name, Validators.MAXLENGTH, [ label, Number(max) ], field.parentTab);
                }
              }
            } else if (values[0] == '$') {
              if (create || !retain) {
                if (typeof (_pagemodel) != 'undefined' && ((_pagemodel[name] == null) || (_pagemodel[name] == ''))) {
                  // no saved value, clear this field
                  FormManager.setValue(name, '');
                  FormManager.readOnly(name);
                }
              }
            } else if (values[0] == '@') {
              FormManager.enable(name);
              if (create || !retain) {
                if (typeof (_pagemodel) != 'undefined' && ((_pagemodel[name] == null) || (_pagemodel[name] == ''))) {
                  FormManager.setValue(name, '');
                }
              }
            } else {

              // if there is a single value, assign and disable/set readOnly
              if (type == 'checkbox') {
                if (create || !retain) {
                  if (FormManager.getField(name).set) {
                    FormManager.getField(name).set('checked', 'Y' == values[0]);
                  } else if (FormManager.getField(name)) {
                    FormManager.getField(name).checked = 'Y' == values[0];
                  }
                  FormManager.disable(name);
                }
              } else if (type == 'radio') {
                if (create || !retain) {
                  FormManager.setValue(name, values[0]);
                  FormManager.disable(name);
                }
              } else {
                if (create || !retain) {
                  FormManager.setValue(name, values[0]);
                  FormManager.readOnly(name);
                  if (FormManager.getField(name).store != null) {
                    if (_pagemodel) {
                      _pagemodel[name] = null;
                    }
                    // also set the filtering dropdown value
                    FilteringDropdown['val_' + name] = values[0];
                  }
                }
              }
            }
          } else if (values && values.length > 1) {
            // if there is more than 1 value, provide a button to allow multiple
            // inputs
            // for text fields
            if (type == 'checkbox') {
              if (create || !retain) {
                var checked = 'Y' == values[0] || (_pagemodel[name] && _pagemodel[name] == 'Y');
                if (FormManager.getField(name).set) {
                  FormManager.getField(name).set('checked', checked);
                } else if (FormManager.getField(name)) {
                  FormManager.getField(name).checked = checked;
                }
              }
            } else if (type == 'radio') {
              if (create || !retain) {
                FormManager.setValue(name, values[0]);
                FormManager.disable(name);
              }
            } else {
              if (dojo.byId('templatevalue-' + name) == null) {
                var button = '<input title="Select a value from the list" type="button" id="templatevalue-' + name + '" class="templateButton" fieldId="' + name + '" field="' + id + '" values="'
                    + values + '"onclick="TemplateService.openChoices(this)" value="..." />';
                var widget = dojo.byId('widget_' + name);
                if (!widget) {
                  widget = dojo.byId(name);
                }
                // add the choose values button
                dojo.place(button, widget, 'after');
              }

              if (create || !retain) {
                var currValue = FormManager.getActualValue(name);
                if (currValue != null && values.indexOf(currValue) < 0) {
                  // current value is not on the list, set value to first on
                  // then
                  // list
                  FormManager.setValue(name, values[0]);
                }
                FormManager.readOnly(name);
              }

              // add the mandatory validation
              var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
            }

          } else {
            // if there is no actual value, enable the field
            if (null != FormManager.getActualValue('cmrIssuingCntry') && FormManager.getActualValue('cmrIssuingCntry') == '897') {
              var scenario = FormManager.getActualValue('custSubGrp');
              if (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != scenario) {
                // only clear the value if the scenario has indeed been changed.
                FormManager.setValue(name, '');
              }
            } else {
              var driver = template.driver;
              var fname = driver.fieldName;
              var scnario = FormManager.getActualValue(fname);
              if (typeof (_pagemodel) != 'undefined' && _pagemodel[fname] != scnario) {
                // only clear the value if the scenario has indeed been changed.
                FormManager.setValue(name, '');
              }
            }

            FormManager.enable(name);
            if (type == 'checkbox') {
              if (create || !retain) {
                var checked = (_pagemodel[name] && _pagemodel[name] == 'Y');
                if (FormManager.getField(name).set) {
                  FormManager.getField(name).set('checked', checked);
                } else if (FormManager.getField(name)) {
                  FormManager.getField(name).checked = checked;
                }
              }
            } else if (type == 'radio') {
              // noop
            } else {
            }

            if (type == 'text' && FormManager.getField(name).store == null && field.requiredInd == 'Y') {
              var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
            }

          }
          // add the invalid value validator for dropdowns
          var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
          if (FormManager.getField(name).store != null) {
            FormManager.addValidator(name, Validators.INVALID_VALUE, [ label ], field.parentTab);
          }

          // add the maxlength validator for text fields
          if (type == 'text') {
            var max = FormManager.getField(name).maxLength;
            if (!max && field.get) {
              max = field.get('maxlength');
            }
            if (max && !isNaN(max) && Number(max) > 0) {
              FormManager.addValidator(name, Validators.MAXLENGTH, [ label, Number(max) ], field.parentTab);
            }
          }

          if (name == 'enterprise' || name == 'company') {
            FormManager.addValidator(name, Validators.DIGIT, [ label ], field.parentTab);
          }

          if (name == 'affiliate') {
            FormManager.addValidator(name, Validators.ALPHANUM, [ label ], field.parentTab);
          }

          var showWarning = false;

          if (null != FormManager.getActualValue('cmrIssuingCntry') && FormManager.getActualValue('cmrIssuingCntry') == '897') {
            var scenario = FormManager.getActualValue('custSubGrp');
            showWarning = false;
            if (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != scenario) {
              showWarning = true;
            }
          } else {
            var driver = template.driver;
            var fname = driver.fieldName;
            var scnario = FormManager.getActualValue(fname);
            if ((typeof (_pagemodel) != 'undefined' && _pagemodel[fname] != scnario)) {
              showWarning = true;
            }
          }
          console.log('template loaded: ' + showWarning);

          if (showWarning) {
            cmr.showAlert("Default values for the scenario has been loaded. Any existing value from a previous template has been cleared/overwritten.", "Warning");
          }

          if ((typeof GEOHandler) != 'undefined') {
            var scenario = FormManager.getActualValue('custSubGrp');
            GEOHandler.executeAfterTemplateLoad(false, scenario, showWarning);
          }
        }
      }
    },
    loadAddressTemplate : function(template, formId, addrType) {
      if (PageManager.isReadOnly()) {
        return;
      }
      var fields = template.fields;
      var values = null;
      var name = '';
      var type = '';
      var field = null;
      var id = '';
      var onParentForm = false;
      if (fields && fields.length > 0) {
        for ( var i = 0; i < fields.length; i++) {
          field = fields[i];
          name = field.fieldName;
          id = field.fieldId;
          values = field.valueMap && field.valueMap[addrType] ? field.valueMap[addrType] : [];
          retain = field.retainValue;
          onParentForm = !field.addressField;

          if (onParentForm) {
            // skip main page fields
            continue;
          }

          // remove first any shown button
          if (dojo.byId('templatevalue-' + name) != null) {
            dojo.query('#templatevalue-' + name).remove();
          }

          // determine the type
          type = FormManager.getField(name).type;
          if (!type) {
            type = FormManager.getField(name).get('type');
          }

          // reset current validations
          FormManager.resetValidations(name);

          if (values && values.length == 1) {
            if (values[0] == '*') {
              // case when the value has *, meaning enable, make required, allow
              // input

              // special case for allow any. do not set to readOnly and set to
              // mandatory
              FormManager.enable(name);
              var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);

            } else {
              // if there is a single value, assign
              if (type == 'checkbox') {
                if (FormManager.getField(name).set) {
                  FormManager.getField(name).set('checked', 'Y' == values[0]);
                } else if (FormManager.getField(name)) {
                  FormManager.getField(name).checked = 'Y' == values[0];
                }
                FormManager.disable(name);
              } else if (type == 'radio') {
                FormManager.setValue(name, values[0]);
                FormManager.disable(name);
              } else {
                FormManager.setValue(name, values[0]);
                FormManager.readOnly(name);
                if (FormManager.getField(name).store != null) {
                  // also set the filtering dropdown value
                  FilteringDropdown['val_' + name] = values[0];
                }
              }
            }
          } else if (values && values.length > 1) {
            if (type == 'checkbox') {
              if (FormManager.getField(name).set) {
                FormManager.getField(name).set('checked', true);
              } else if (FormManager.getField(name)) {
                FormManager.getField(name).checked = true;
              }
              FormManager.disable(name);
            } else if (type == 'radio') {
              FormManager.setValue(name, values[0]);
              FormManager.disable(name);
            } else {
              if (dojo.byId('templatevalue-' + name) == null) {
                var button = '<input title="Select a value from the list" type="button" id="templatevalue-' + name + '" class="templateButton" fieldId="' + name + '" field="' + id + '" values="'
                    + values + '"onclick="TemplateService.openChoices(this)" value="..." />';
                var widget = dojo.byId('widget_' + name);
                if (!widget) {
                  widget = dojo.byId(name);
                }
                dojo.place(button, widget, 'after');
              }

              var currValue = FormManager.getActualValue(name);
              if (currValue != null && values.indexOf(currValue) < 0) {
                // current value is not on the list
                FormManager.setValue(name, '');
              }
              FormManager.readOnly(name);

              var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
            }

          } else {
            // if there is no actual value, enable the field and do nothing
            FormManager.resetValidations(name);
            FormManager.show(name);
            FormManager.enable(name);
            // var scenario = FormManager.getActualValue('custSubGrp');
            // if (typeof(_pagemodel) != 'undefined' && _pagemodel['custSubGrp']
            // != scenario){
            // FormManager.setValue(name, '');
            // }

            if (type == 'checkbox') {
              if (FormManager.getField(name).set) {
                FormManager.getField(name).set('checked', false);
              } else if (FormManager.getField(name)) {
                FormManager.getField(name).checked = false;
              }
            }

            if (type == 'text' && FormManager.getField(name).store == null && field.requiredInd == 'Y') {
              var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
            }
          }
          var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
          if (FormManager.getField(name).store != null) {
            FormManager.addValidator(name, Validators.INVALID_VALUE, [ label ], field.parentTab);
          }
          // add the maxlength validator for text fields
          if (type == 'text') {
            var max = FormManager.getField(name).maxLength;
            if (!max && field.get) {
              max = field.get('maxlength');
            }
            if (max && !isNaN(max) && Number(max) > 0) {
              FormManager.addValidator(name, Validators.MAXLENGTH, [ label, Number(max) ], field.parentTab);
            }
          }
        }
        if ((typeof GEOHandler) != 'undefined') {
          GEOHandler.executeAfterTemplateLoad(true);
        }
      }
    },
    isHandled : function(fieldName) {
      if (currentTemplate == null) {
        return false;
      }
      var fields = currentTemplate.fields;
      for ( var i = 0; i < fields.length; i++) {
        if (fields[i].fieldName == fieldName) {
          return true;
        }
      }
    },
    isFieldReadOnly : function(fieldName) {
      if (currentTemplate == null) {
        return false;
      }
      var fields = currentTemplate.fields;
      if (fields && fields.length > 0) {
        for ( var i = 0; i < fields.length; i++) {
          if (fields[i].fieldName == fieldName) {
            var retain = FormManager.getActualValue('reqType') != 'C' && fields[i].retainValue;
            if (fields[i].addressField) {
              if (!fields[i].valueMap) {
                return false;
              }
              var addrType = FormManager.getActualValue('addrType');
              if (addrType == '' || fields[i].valueMap == null || fields[i].valueMap[addrType] == null) {
                return false;
              }
              return fields[i].valueMap[addrType].length > 0 && fields[i].valueMap[addrType][0] != '*' && !retain;
            } else {
              var isicFilter = false;
              if (fields[i].fieldName == 'isicCd') {
                if (fields[i].values && fields[i].values.length > 0) {
                  isicFilter = fields[i].values[0].indexOf('%') >= 0 || fields[i].values[0].indexOf('LOV') == 0 || fields[i].values[0].indexOf('-') > 0 || fields[i].values[0].indexOf('^') >= 0;
                }
              }
              return fields[i].values && fields[i].values.length > 0 && fields[i].values[0] != '*' && fields[i].values[0] != '@' && fields[i].values[0].indexOf('%') < 0 && !isicFilter && !retain;
            }
          }
        }
      }
      return false;
    },
    getCurrentTemplate : function() {
      return currentTemplate;
    },
    chooseTemplateValue : function() {
      if (cmr.templateOptions && cmr.templateOptions.field) {
        FormManager.setValue(cmr.templateOptions.field, FormManager.getActualValue('templatevalues'));
      }
      cmr.hideModal('templateValueModal');
      cmr.templateOptions = null;
    },
    openChoices : function(button) {
      cmr.templateOptions = {
        field : button.getAttribute('fieldId'),
        values : button.getAttribute('values'),
        id : button.getAttribute('field')
      };
      cmr.showModal('templateValueModal');
    }
  };
}());

function findTab(id) {
  var tabs = [ 'GENERAL_REQ_TAB', 'TYPE_REQ_TAB', 'NAME_REQ_TAB', 'CUST_REQ_TAB', 'IBM_REQ_TAB', 'TAXINFO_REQ_TAB' ];
  for ( var i = 0; i < tabs.length; i++) {
    if (dojo.query('#' + tabs[i]).query('#' + id).length > 0) {
      return tabs[i];
    }
  }
  return null;
}
function templateValueModal_onLoad() {
  var values = cmr.templateOptions ? cmr.templateOptions.values : null;
  if (values) {
    var options = '';
    var list = values.split(',');
    for ( var i = 0; i < list.length; i++) {
      options += '<option value="' + list[i].trim() + '">' + list[i].trim() + '</option>';
    }
    dojo.byId('templatevalues').innerHTML = options;
    dojo.byId('templatevaluename').innerHTML = dojo.byId('cmr-fld-lbl-' + cmr.templateOptions.id).innerHTML;
  }
}

function revertISICBehavior() {
  // dirty dirty dirty fix for isic
  if (_DROPDOWNCONNECTS && _DROPDOWNCONNECTS.isicCd) {
    // means at one point a like search for isic was done, disconnect dependency
    // of subind with isic
    dojo.disconnect(_DROPDOWNCONNECTS.isicCd[0]);
    _DROPDOWNCONNECTS.isicCd = null;
  }
  if (_DROPDOWNCONNECTS && !_DROPDOWNCONNECTS.subIndustryCd) {
    // reconnect ISIC dependency to subindustry
    FormManager.setValue(name, '');
    FormManager.setValue('subIndustryCd', '');
    FilteringDropdown['val_subIndustryCd'] = null;
    FilteringDropdown['val_isicCd'] = null;
    FilteringDropdown.loadItems('subIndustryCd', 'subIndustryCd_spinner', 'bds', 'fieldId=Subindustry&cmrIssuingCntry=_cmrIssuingCntry');
    FilteringDropdown.loadOnChange('isicCd', 'isicCd_spinner', 'bds', 'fieldId=ISIC&cmrIssuingCntry=_cmrIssuingCntry&subIndustryCd=_subIndustryCd', 'subIndustryCd');
  }
}