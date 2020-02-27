/*
 * File: cmr-scenarios.js
 * Description:
 * Contains the code for the TemplateService which handles CMR Scenarios
 */

var TemplateService = (function() {
  console.log('Using generic scenarios framework');
  var currentTemplate = null;

  var initialLoad = false;

  var ENABLED_MANDATORY = '*';
  var READONLY_CLEARVALUE = '$';
  var EDITABLE_CLEARVALUE = '@';
  var ADDRESS_TYPES = [ 'ZS01', 'ZI01', 'ZP01', 'ZD01', 'ZS02', 'ZP02', 'CTYA', 'CTYB', 'CTYC', 'CTYD', 'CTYE', 'CTYF', 'CTYG', 'CTYH', 'EDUC', 'MAIL', 'PUBB', 'PUBS', 'STAT', 'ZF01', 'ZH01' ];

  var getUserRole = function() {
    var role = null;
    if (typeof (_pagemodel) != 'undefined') {
      role = _pagemodel.userRole;
    }
    return role;
  };

  var currentChosenScenario = '';

  var limitDropdownValues = function(field, values) {
    if (values && values.length == 0) {
      var model = {
        identifier : "id",
        label : "name",
        items : []
      };
      var tempStore = new dojo.data.ItemFileReadStore({
        data : model,
        clearOnClose : true
      });
      field.store = tempStore;
      return;
    }
    var loadedStore = field.loadedStore;
    if (!loadedStore) {
      return;
    }

    var currentValue = field.get('value');
    if (currentValue != '' && values.indexOf(currentValue) < 0) {
      field.set('value', '');
    }
    var model = {
      identifier : "id",
      label : "name",
      items : []
    };

    var item = null;
    for (var i = 0; i < loadedStore._arrayOfAllItems.length; i++) {
      item = loadedStore._arrayOfAllItems[i];
      if (values.indexOf(item.id[0]) >= 0) {
        model.items.push({
          id : item.id[0],
          name : item.name[0]
        });
      }
    }
    var tempStore = new dojo.data.ItemFileReadStore({
      data : model,
      clearOnClose : true
    });
    field.store = tempStore;
  };
  return {
    initLoad : function() {
      initialLoad = true;
    },
    initLoaded : function() {
      return initialLoad;
    },
    init : function() {
      // add the onchange for address types
      console.log('Template Service init...');
      for (var i = 0; i < ADDRESS_TYPES.length; i++) {
        if (dijit.byId('addrType_' + ADDRESS_TYPES[i])) {
          dojo.connect(dijit.byId('addrType_' + ADDRESS_TYPES[i]), 'onClick', function(evt) {
            var currentTemplate = TemplateService.getCurrentTemplate();
            if (currentTemplate && evt.target.checked) {
              TemplateService.loadAddressTemplate(currentTemplate, 'reqentry', evt.target.value);
            }
            if (evt.target.checked) {
              if (typeof (executeOnChangeOfAddrType) != 'undefined') {
                executeOnChangeOfAddrType();
              }
            }
          });
        }
      }
    },
    fill : function(formId) {
      // get the template from TemplatesController
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
      // template is of type Template, with a list of TemplatedFields
      if (PageManager.isReadOnly()) {
        console.log('Page is read only, skip template load.');
        return;
      }
      console.log('loading scenario..');
      initialLoad = true;
      currentTemplate = template;
      var fields = template.fields;
      var values = null;
      var name = '';
      var type = '';
      var retainValue = false; // this will follow old framework, not touched
      var field = {};
      var id = '';
      var onParentForm = false;
      var create = FormManager.getActualValue('reqType') != 'U';
      var lockInd = 'Y';
      var required = '';
      var label = '';
      var scenarioChanged = false;

      if (fields && fields.length > 0) {
        // reset behavior first if there are actual fields on the template

        if (typeof (GEOHandler) != 'undefined' && GEOHandler.isRevertIsicBehavior()) {
          console.log('reverting isic behavior..');
          revertISICBehavior();
        }

        // determine if the scenario changed
        if (null != FormManager.getActualValue('cmrIssuingCntry') && FormManager.getActualValue('cmrIssuingCntry') == '897') {
          var scenario = FormManager.getActualValue('custSubGrp');
          scenarioChanged = false;
          if (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != scenario) {
            scenarioChanged = true;
          }
          scenarioChanged = scenarioChanged || (currentChosenScenario != '' && currentChosenScenario != scenario);
          currentChosenScenario = scenario;
        } else {
          var driver = template.driver;
          var driverFieldName = driver.fieldName;
          var scenario = FormManager.getActualValue(driverFieldName);
          if ((typeof (_pagemodel) != 'undefined' && scenario && _pagemodel[driverFieldName] != scenario)) {
            console.log('scenario changed: ' + _pagemodel[driverFieldName] + ' to ' + scenario);
            scenarioChanged = true;
          }
          scenarioChanged = scenarioChanged || (currentChosenScenario != '' && scenario && currentChosenScenario != scenario);
          if (scenarioChanged) {
            console.log('scenario changed from current: ' + currentChosenScenario + ' to ' + scenario);
          }
          currentChosenScenario = scenario;
        }

        console.log('Scenario changed? ' + scenarioChanged);

        // iterate through the template fields
        for (var i = 0; i < fields.length; i++) {
          field = fields[i];
          name = field.fieldName;
          id = field.fieldId;
          label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
          values = field.values;
          // RETAIN_VAL_IND (Y/N)
          retainValue = field.retainValue;
          // LOCKED_INDC (N,R,P,Y)
          lockInd = field.lockInd;
          // REQ_IND (R/Y, O/N, D)
          required = field.requiredInd;

          // legacy handling, for N/Y inputs
          if ('Y' == required) {
            required = 'R';
          }
          if ('N' == required) {
            required = 'O';
          }
          onParentForm = !field.addressField;

          // jz - commented out, will use this logic now,all instances before
          // removed before current logic
          // retainValue = true; // no retention for updates now

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
          if (FormManager.getField(name) != null && FormManager.getField(name).store != null) {
            type = 'dropdown';
          }

          // comment out if you need to debug
          // console.log('Field: ' + label + ' | Type: ' + type + ' | Values: '
          // + values + ' | Retain: ' + retainValue + ' | Locked: ' + lockInd +
          // ' | Required: ' + required);

          // remove any current validations
          if (field.fieldId != 'LocalTax2') {
            FormManager.resetValidations(name);
            if (type == 'dropdown') {
              var fieldTemp = FormManager.getField(name);
              if (fieldTemp && fieldTemp.loadedStore) {
                fieldTemp.store = fieldTemp.loadedStore;
              }
            }
          }

          if (values && values.length == 1) {
            if (values[0] == ENABLED_MANDATORY) {
              // case when the value has *, meaning enable, make required, allow
              // input

              // special case for allow any. do not set to readOnly and set to
              // mandatory
              FormManager.enable(name);
              if (create) {
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

              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);

            } else if (name == 'isicCd' && (values[0].indexOf('%') >= 0 || values[0].indexOf('LOV') == 0 || values[0].indexOf('^') >= 0 || values[0].indexOf('-') > 0)) {

              // has wildcard, LOV, exclusions, or range, dirty dirty fix for
              // ISIC/subindustry :( grr

              if (typeof (GEOHandler) != 'undefined' && GEOHandler.isRevertIsicBehavior()) {
                // 1. disconnect the subIndustryCd handler
                if (_DROPDOWNCONNECTS && _DROPDOWNCONNECTS.subIndustryCd) {
                  dojo.disconnect(_DROPDOWNCONNECTS.subIndustryCd[0]);
                  _DROPDOWNCONNECTS.subIndustryCd = null;
                }
              }

              // 2. load isic,
              FormManager.setValue(name, '');
              FilteringDropdown['val_isicCd'] = null;
              FilteringDropdown.loadItems('isicCd', 'isicCd_spinner', 'bds', 'fieldId=ISIC&cmrIssuingCntry=_cmrIssuingCntry&isicCd=' + values[0] + '&nocache=y');

              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
              FormManager.enable(name);

              // 3. load subindustry

              limitDropdownValues(FormManager.getField('subIndustryCd'), []);
              FormManager.setValue('subIndustryCd', '');
              FilteringDropdown['val_subIndustryCd'] = null;
              FilteringDropdown.loadOnChange('subIndustryCd', 'subIndustryCd_spinner', 'bds', 'fieldId=Subindustry&cmrIssuingCntry=_cmrIssuingCntry&nocache=y&isicCd=_isicCd', 'isicCd');

              subIndLabel = dojo.byId('cmr-fld-lbl-Subindustry') ? dojo.byId('cmr-fld-lbl-Subindustry').innerHTML : 'Subindustry';
              FormManager.addValidator('subIndustryCd', Validators.REQUIRED, [ subIndLabel ], field.parentTab);
              FormManager.readOnly('subIndustryCd');
            } else if (values[0].startsWith("COPY:")) {
              // COPY: format copies the value of the specified field to this
              // field
              var fname2 = values[0];
              fname2 = fname2.substring(5);
              fname2 = fname2.replace("##", "");
              var max = '';
              if (null != fname2 && fname2 != '') {
                var val = FormManager.getActualValue(fname2);
                if (type == 'text') {
                  max = FormManager.getField(name).maxLength;
                  if (!max && field.get) {
                    max = field.get('maxlength');
                  }
                  val = val.substring(0, Number(max));
                }
                if (create) {
                  if (typeof (_pagemodel) != 'undefined') {
                    FormManager.setValue(name, val);
                    FormManager.readOnly(name);
                  }
                }
                if (max && !isNaN(max) && Number(max) > 0) {
                  FormManager.addValidator(name, Validators.MAXLENGTH, [ label, Number(max) ], field.parentTab);
                }
              }
            } else if (values[0] == READONLY_CLEARVALUE) {
              // $ value means clear the field and not allow input
              if (create) {
                // if (typeof (_pagemodel) != 'undefined' && ((_pagemodel[name]
                // == null) || (_pagemodel[name] == ''))) {
                // no saved value, clear this field
                FormManager.setValue(name, '');
                FormManager.readOnly(name);
                // }
              }
            } else if (values[0] == EDITABLE_CLEARVALUE) {
              // @ value means clear the field and allow input
              FormManager.enable(name);
              if (create) {
                if (typeof (_pagemodel) != 'undefined' && ((_pagemodel[name] == null) || (_pagemodel[name] == ''))) {
                  FormManager.setValue(name, '');
                }
              }
            } else {

              // jz: for single values, only RETAIN_VAL_IND and LOCK_INDC have
              // effects; by default, if there is one value specified, the field
              // is MANDATORY

              // RETAIN_VAL_IND
              // Y - do not overwrite the current value if not empty; exception
              // is when the scenario changed
              // N - overwrite the value of the field when it has any value
              // Y/N applies only to text fields and dropdowns

              // LOCK_INC
              // Y - Locked for all roles
              // R - Locked for requester
              // P - Locked for processor
              // N - not locked for all roles

              // if there is a single value, assign and disable/set readOnly
              if (type == 'checkbox') {
                if (create) {
                  if (FormManager.getField(name).set) {
                    FormManager.getField(name).set('checked', 'Y' == values[0]);
                  } else if (FormManager.getField(name)) {
                    FormManager.getField(name).checked = 'Y' == values[0];
                  }
                  if (lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor')) {
                    FormManager.disable(name);
                  }
                }
              } else if (type == 'radio') {
                if (create) {
                  FormManager.setValue(name, values[0]);
                  if (lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor')) {
                    FormManager.disable(name);
                  }
                }
              } else {
                if (create) {
                  var useCountryRules = false;
                  if (typeof (useCountryScenarioRules) != 'undefined') {
                    useCountryRules = useCountryScenarioRules(currentChosenScenario, name, values[0]);
                  }
                  if (!useCountryRules) {
                    if (scenarioChanged) {
                      FormManager.setValue(name, values[0]);
                    }
                    if (!retainValue || (retainValue && FormManager.getActualValue(name) == '')) {
                      FormManager.setValue(name, values[0]);
                    }
                  }

                  if (lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor')) {
                    FormManager.readOnly(name);
                  } else {
                    // this is editable, add the mandatory validator
                    FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
                    FormManager.enable(name);
                  }

                  if (FormManager.getField(name).store != null) {
                    if (typeof (_pagemodel) != 'undefined') {
                      _pagemodel[name] = null;
                    }
                    // also set the filtering dropdown value
                    FilteringDropdown['val_' + name] = values[0];
                  }
                }
              }
              // check the REQ_IND from scenarios and implement
              switch (required) {
              case 'R':
                if (!(lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor'))) {
                  FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
                  FormManager.enable(name);
                }
                break;
              case 'O':
                if (!(lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor'))) {
                  FormManager.enable(name);
                }
                break;
              case 'D':
                FormManager.readOnly(name);
                break;
              case 'G':
                FormManager.disable(name);
                break;
              }
            }
          } else if (values && values.length > 1) {
            // if there is more than 1 value, provide a button to allow multiple
            // inputs
            // for text fields
            if (type == 'checkbox') {
              if (create) {
                var checked = 'Y' == values[0] || (_pagemodel[name] && _pagemodel[name] == 'Y');
                if (FormManager.getField(name).set) {
                  FormManager.getField(name).set('checked', checked);
                } else if (FormManager.getField(name)) {
                  FormManager.getField(name).checked = checked;
                }
              }
            } else if (type == 'radio') {
              if (create) {
                FormManager.setValue(name, values[0]);
                FormManager.disable(name);
              }
            } else if (type == 'dropdown') {
              if (create) {
                console.log(">>>>>> DROPDOWN VALUES BEING ADDED >>> VALIDATORS");
                var fieldTemp = FormManager.getField(name);
                limitDropdownValues(fieldTemp, values);
                FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
                FormManager.enable(name);
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

              if (create) {
                var currValue = FormManager.getActualValue(name);
                if (currValue != null && values.indexOf(currValue) < 0) {
                  // current value is not on the list, set value to first on the
                  // list
                  FormManager.setValue(name, values[0]);
                }
                FormManager.readOnly(name);
              }

              // add the mandatory validation
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
            }

          } else {
            // if there is no actual value, enable the field
            if (scenarioChanged && !retainValue) {
              // clear the value all the time if the scenario has indeed been
              // changed
              FormManager.setValue(name, '');
            } /*
               * else if (!retainValue) { // scenario has changed, field has no
               * value from scenarios but // wants to retain
               * FormManager.setValue(name, ''); }
               */

            // check the REQ_IND from scenarios and implement
            switch (required) {
            case 'R':
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
              FormManager.enable(name);
              break;
            case 'O':
              FormManager.enable(name);
              break;
            case 'D':
              FormManager.readOnly(name);
              break;
            case 'G':
              FormManager.disable(name);
              break;
            }

            if (type == 'checkbox') {
              if (create) {
                var checked = (_pagemodel[name] && _pagemodel[name] == 'Y');
                if (FormManager.getField(name).set) {
                  FormManager.getField(name).set('checked', checked);
                } else if (FormManager.getField(name)) {
                  FormManager.getField(name).checked = checked;
                }
              }
            }

          }
          // add the invalid value validator for dropdowns
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

          if (name == 'enterprise' || name == 'affiliate' || name == 'company') {
            FormManager.addValidator(name, Validators.DIGIT, [ label ], field.parentTab);
          }

        }
        if (scenarioChanged) {
          cmr.showAlert("Default values for the scenario have been loaded. Any existing value from a previous template has been cleared/overwritten.", "Warning");
        }

        if ((typeof GEOHandler) != 'undefined') {
          GEOHandler.executeAfterTemplateLoad(false, currentChosenScenario, scenarioChanged);
        }
      }
    },
    loadAddressTemplate : function(template, formId, addrType) {
      if (PageManager.isReadOnly()) {
        return;
      }
      console.log('loading address scenario..');
      var fields = template.fields;
      var values = null;
      var name = '';
      var type = '';
      var field = null;
      var id = '';
      var onParentForm = false;
      var label = '';
      var lockInd = 'Y';
      var required = '';
      var scenario = FormManager.getActualValue(template.driver.fieldName);
      var retainValue = '';
      var scenarioChanged = false;
      if (fields && fields.length > 0) {
        for (var i = 0; i < fields.length; i++) {
          field = fields[i];
          name = field.fieldName;
          id = field.fieldId;
          label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
          values = field.valueMap && field.valueMap[addrType] ? field.valueMap[addrType] : [];
          // RETAIN_VAL_IND (Y/N)
          retainValue = field.retainValue;
          // LOCKED_INDC (N,R,P,Y)
          lockInd = field.lockInd;
          // REQ_IND (R/Y, O/N, D)
          required = field.requiredInd;
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
          if (FormManager.getField(name) != null && FormManager.getField(name).store != null) {
            type = 'dropdown';
          }

          // comment out if you need to debug
          // console.log('Field: ' + label + ' | Address Type: ' + addrType + '|
          // Type: ' + type + ' | Values: ' + values + ' | Retain: ' +
          // retainValue + ' | Locked: ' + lockInd + ' | Required: '
          // + required);

          // reset current validations
          FormManager.resetValidations(name);

          // revert dropdowns to previous value
          if (type == 'dropdown') {
            var fieldTemp = FormManager.getField(name);
            if (fieldTemp && fieldTemp.loadedStore) {
              fieldTemp.store = fieldTemp.loadedStore;
            }
          }

          var driver = template.driver;
          var driverFieldName = driver.fieldName;
          scenario = FormManager.getActualValue(driverFieldName);
          if ((typeof (_pagemodel) != 'undefined' && _pagemodel[driverFieldName] != scenario)) {
            scenarioChanged = true;
          }
          scenarioChanged = scenarioChanged || (currentChosenScenario != '' && currentChosenScenario != scenario);
          currentChosenScenario = scenario;

          console.log('Scenario changed? ' + scenarioChanged);

          if (values && values.length == 1) {
            if (values[0] == ENABLED_MANDATORY) {
              // case when the value has *, meaning enable, make required, allow
              // input

              // special case for allow any. do not set to readOnly and set to
              // mandatory
              FormManager.enable(name);
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);

            } else if (values[0] == READONLY_CLEARVALUE) {
              // $ value means clear the field and not allow input
              if (create) {
                if (typeof (_pagemodel) != 'undefined' && ((_pagemodel[name] == null) || (_pagemodel[name] == ''))) {
                  // no saved value, clear this field
                  FormManager.setValue(name, '');
                  FormManager.readOnly(name);
                }
              }
            } else if (values[0] == EDITABLE_CLEARVALUE) {
              // $ value means clear the field and allow input
              FormManager.enable(name);
              if (create) {
                if (typeof (_pagemodel) != 'undefined' && ((_pagemodel[name] == null) || (_pagemodel[name] == ''))) {
                  FormManager.setValue(name, '');
                }
              }
            } else {

              // jz: for single values, only RETAIN_VAL_IND and LOCK_INDC have
              // effects; by default, if there is one value specified, the field
              // is MANDATORY

              // RETAIN_VAL_IND
              // Y - do not overwrite the current value if not empty; exception
              // is when the scenario changed
              // N - overwrite the value of the field when it has any value
              // Y/N applies only to text fields and dropdowns

              // LOCK_INC
              // Y - Locked for all roles
              // R - Locked for requester
              // P - Locked for processor
              // N - not locked for all roles

              // if there is a single value, assign
              if (type == 'checkbox') {
                if (FormManager.getField(name).set) {
                  FormManager.getField(name).set('checked', 'Y' == values[0]);
                } else if (FormManager.getField(name)) {
                  FormManager.getField(name).checked = 'Y' == values[0];
                }
                if (lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor')) {
                  FormManager.disable(name);
                }
              } else if (type == 'radio') {
                FormManager.setValue(name, values[0]);
                if (lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor')) {
                  FormManager.disable(name);
                }
              } else {
                if (!retainValue || (retainValue && FormManager.getActualValue(name) == '')) {
                  FormManager.setValue(name, values[0]);
                }
                if (lockInd == 'Y' || (lockInd == 'R' && getUserRole() == 'Requester') || (lockInd == 'P' && getUserRole() == 'Processor')) {
                  FormManager.readOnly(name);
                } else {
                  // this is editable, add the mandatory validator
                  FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
                  FormManager.enable(name);
                }
                if (FormManager.getField(name).store != null) {
                  if (typeof (_pagemodel) != 'undefined') {
                    _pagemodel[name] = null;
                    // also set the filtering dropdown value
                    FilteringDropdown['val_' + name] = values[0];
                  }
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
            } else if (type == 'dropdown') {
              if (create) {
                var fieldTemp = FormManager.getField(name);
                limitDropdownValues(fieldTemp, values);
                FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
                FormManager.enable(name);
              }
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

              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
            }

          } else {

            // if there is no actual value, enable the field and do nothing
            FormManager.resetValidations(name);
            FormManager.show(name);
            FormManager.enable(name);

            if (scenarioChanged && !retainValue) {
              FormManager.setValue(name, '');
            }

            // check the REQ_IND from scenarios and implement
            switch (required) {
            case 'R':
              FormManager.addValidator(name, Validators.REQUIRED, [ label ], field.parentTab);
              FormManager.enable(name);
              break;
            case 'O':
              FormManager.enable(name);
              break;
            case 'D':
              FormManager.readOnly(name);
              break;
            case 'G':
              FormManager.setValue(name, '');
              FormManager.disable(name);
              break;
            }

            if (type == 'checkbox') {
              if (FormManager.getField(name).set) {
                FormManager.getField(name).set('checked', false);
              } else if (FormManager.getField(name)) {
                FormManager.getField(name).checked = false;
              }
            }

            if (type == 'text' && FormManager.getField(name).store == null && 'R' == required) {
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
        var scenarioChanged = false;
        if (typeof (_pagemodel) != 'undefined' && _pagemodel['custSubGrp'] != scenario) {
          scenarioChanged = true;
        }

        if ((typeof GEOHandler) != 'undefined') {
          GEOHandler.executeAfterTemplateLoad(true, currentChosenScenario, scenarioChanged);
        }
      }
    },
    isHandled : function(fieldName) {
      if (currentTemplate == null) {
        return false;
      }
      var fields = currentTemplate.fields;
      for (var i = 0; i < fields.length; i++) {
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
        for (var i = 0; i < fields.length; i++) {
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
  for (var i = 0; i < tabs.length; i++) {
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
    for (var i = 0; i < list.length; i++) {
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
    // FormManager.setValue(name, '');
    FormManager.setValue('subIndustryCd', '');
    FilteringDropdown['val_subIndustryCd'] = null;
    FilteringDropdown['val_isicCd'] = null;
    FilteringDropdown.loadItems('subIndustryCd', 'subIndustryCd_spinner', 'bds', 'fieldId=Subindustry&cmrIssuingCntry=_cmrIssuingCntry');
    FilteringDropdown.loadOnChange('isicCd', 'isicCd_spinner', 'bds', 'fieldId=ISIC&cmrIssuingCntry=_cmrIssuingCntry&subIndustryCd=_subIndustryCd', 'subIndustryCd');
  }
}