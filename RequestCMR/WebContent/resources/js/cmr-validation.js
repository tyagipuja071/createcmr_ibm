/**
 * Handles the validations of the page
 */

var Validators = (function() {
  return {
    REQUIRED : function(input) {
      var value = FormManager.getActualValue(input);
      if (input.type == 'radio') {
        value = FormManager.getActualValue(input.name);
      } else if (input.type == 'checkbox') {
        if (input.checked != undefined) {
          value = input.checked;
        }
      }
      if (!value || value == '' || value.length == 0) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.REQUIRED);
      } else {
        return new ValidationResult(input, true);
      }
    },
    RANGE : function(min, max) {
      this.min = min;
      this.max = max;
      return function(input) {
        var value = FormManager.getActualValue(input);
        if (value && value.length > 0 && !isNaN(value)) {
          var intValue = parseInt(value, 10);
          if (intValue >= this.min && intValue <= this.max) {
            return new ValidationResult(input, true);
          } else {
            return new ValidationResult(input, false, MessageMgr.MESSAGES.OUTOFRANGE_VALUE);
          }
        }
        return new ValidationResult(input, true);
      };
    },
    DATE : function(format) {
      return {
        dateFormat : format,
        validate : function(input) {
          var value = FormManager.getActualValue(input);
          if (value && value.length > 0) {
            var valid = moment(value, this.dateFormat, true).isValid();
            if (!valid) {
              return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_VALUE);
            } else {
              input.value = moment(value, this.dateFormat, true).format(this.dateFormat);
              return new ValidationResult(input, true);
            }
          }
          return new ValidationResult(input, true);
        }
      };
    },
    RANGE : function(min, max) {
      return {
        min : min,
        max : max,
        validate : function(input) {
          var value = FormManager.getActualValue(input);
          if (value && value.length > 0 && !isNaN(value)) {
            var intValue = parseInt(value, 10);
            if (intValue >= this.min && intValue <= this.max) {
              return new ValidationResult(input, true);
            } else {
              return new ValidationResult(input, false, MessageMgr.MESSAGES.OUTOFRANGE_VALUE);
            }
          }
        }
      };
    },
    DB : function(queryId, params) {
      return {
        queryId : queryId,
        params : params,
        name : queryId,
        type : 'db',
        validate : function(input) {
          var qParams = params;
          var value = FormManager.getActualValue(input);
          if (value == '') {
            return new ValidationResult(input, true);
          }
          qParams.value = value;
          var result = cmr.query('db.' + queryId, qParams);
          if (!result.ret1) {
            return new ValidationResult(input, false, MessageMgr.MESSAGES.NOT_FOUND_IN_DB);
          } else {
            return new ValidationResult(input, true);
          }
        }
      };
    },
    NUMBER : function(input) {
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && isNaN(value)) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_NUMERIC_VALUE);
      } else {
        return new ValidationResult(input, true);
      }
    },
    DIGIT : function(input) {
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && !value.match("^[0-9]+$")) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_PHONE);
      } else {
        return new ValidationResult(input, true);
      }
    },
    NO_SPECIAL_CHAR : function(input) {
      var checkstr = FormManager.getActualValue(input);
      var allValid = true;
      allvalid = (/[^A-Za-z\d ]/.test(checkstr) == false);
      if (allvalid) {
        return new ValidationResult(input, true);
      } else {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.NO_SPECIAL_CHARACTERS);
      }
    },
    ALPHA : function(input) {
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && !value.match("^[a-zA-Z]+$")) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_ALPHABET);
      } else {
        return new ValidationResult(input, true);
      }
    },

    ALPHANUM : function(input) {
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && !value.match("^[0-9a-zA-Z]*$")) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_ALPHANUMERIC);
      } else {
        return new ValidationResult(input, true);
      }
    },
    INVALID_VALUE : function(input) {
      var id = null;
      if (typeof (input) == 'string') {
        id = input;
      } else {
        id = input.id;
      }
      var field = FormManager.getField(id);
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && field && field.state == 'Error') {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_VALUE);
      } else {
        return new ValidationResult(input, true);
      }
    },
    MAXLENGTH : function(input) {
      var id = null;
      if (typeof (input) == 'string') {
        id = input;
      } else {
        id = input.id;
      }
      if (id && id.trim == '') {
        return new ValidationResult(input, true);
      }
      var field = FormManager.getField(id);
      if (!field) {
        return new ValidationResult(input, true);
      }
      if (field.type != 'text') {
        return new ValidationResult(input, true);
      }
      var max = field.maxLength;
      if (!max && field.get) {
        max = field.get('maxlength');
      }
      if (max && !isNaN(max)) {
        var val = FormManager.getActualValue(id);

        var valLengthText = val.length;
        var valLength = val.length;
        if (typeof (TextEncoder) != 'undefined') {
          valLength = new TextEncoder('UTF-8').encode(val).length / 2;
        } else if (typeof (Blob) != 'undefined') {
          valLength = new Blob([ val ]).size / 2;
        }
        var checkBytes = FormManager.checkBytes(id);
        if (!checkBytes) {
          console.log('field ' + id + ' skipped in bytes checks..');
          valLength = valLengthText;
        }
        if (valLengthText > Number(max) || valLength > Number(max)) {
          var result = new ValidationResult(input, false, MessageMgr.MESSAGES.MAXLENGTH);
          result.params = new Array();
          var lbl = FormManager.getLabel(id);
          try {
            lbl = dojo.query('label[for="' + id + '"]').text();
            if (lbl) {
              lbl = lbl.replace('*', '').replace(':', '');
              lbl = lbl.trim();
            }
          } catch (e) {

          }
          result.params.push(lbl);
          result.params.push(max);
          return result;
        }
      }
      return new ValidationResult(input, true);
    },
    EMAIL : function(input) {
      var value = FormManager.getActualValue(input);
      if (value.length == 0) {
        return true;
      }
      var reg = /^[_A-Za-z0-9\-\.]+(\.[_A-Za-z0-9]+)*@[A-Za-z0-9]+(\.[A-Za-z0-9]+)*(\.[A-Za-z]{2,})$/;
      if (!reg.test(value)) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_VALUE);
      } else {
        return new ValidationResult(input, true);
      }
    },
    BLUEPAGES : function(input) {
      var value = FormManager.getActualValue(input);
      if (value == '') {
        var actualId = input.id + '_bpcont';
        if (dojo.byId(actualId)) {
          dojo.byId(actualId).value = '';
        }
        var bpId = dojo.byId(input.id).getAttribute('bpId');
        if (bpId && dojo.byId(bpId)) {
          dojo.byId(bpId).value = '';
        }
        return new ValidationResult(input, true);
      }
      if (input.id && dojo.byId(input.id + '_bpcont')) {
        var actualId = input.id + '_bpcont';
        var pair = FormManager.getActualValue(actualId);
        if (pair && pair.indexOf(':') > 0) {
          var name = pair.split(':')[0];
          name = dojo.string.trim(name);
          if (name != value) {
            return new ValidationResult(input, false, MessageMgr.MESSAGES.BLUEPAGES_ERROR);
          }
          var bpId = dojo.byId(input.id).getAttribute('bpId');
          if (bpId && dojo.byId(bpId)) {
            var actualpId = FormManager.getActualValue(bpId);
            var pId = pair.split(':')[1];
            actualpId = dojo.string.trim(actualpId);
            if (pId != actualpId) {
              return new ValidationResult(input, false, MessageMgr.MESSAGES.BLUEPAGES_ERROR);
            }
          }
        } else if (pair.length == 0 && value.length > 0) {
          return new ValidationResult(input, false, MessageMgr.MESSAGES.BLUEPAGES_ERROR);
        }
      } else {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.BLUEPAGES_ERROR);
      }
      return new ValidationResult(input, true);
    },
    LATIN : function(input) {
      var value = FormManager.getActualValue(input);
      if (!value || value == '' || value.length == 0) {
        return true;
      }
      var reg = /[^\u0000-\u007f]/;
      if (reg.test(value)) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.LATIN);
      } else {
        return new ValidationResult(input, true);
      }
    },
    NON_LATIN : function(input) {
      var value = FormManager.getActualValue(input);
      if (!value || value == '' || value.length == 0) {
        return true;
      }
      var reg = /[A-Za-z]/g;
      if (value.match(reg)) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.NON_LATIN);
      } else {
        return new ValidationResult(input, true);
      }
    },
    CHECKALPHANUMDASH : function(input) {
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && !value.match("^[ 0-9a-zA-Z\-,:]+$")) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_NUMAZ);
      } else {
        return new ValidationResult(input, true);
      }
    },
    CHECKALPHANUMDASHDOT : function(input) {
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && !value.match("^[ 0-9a-zA-Z\-,.:]+$")) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_NUMAZ);
      } else {
        return new ValidationResult(input, true);
      }
    },
    CHECKSPACEPLACE4DASH : function(input) {
      var str = FormManager.getActualValue(input);
      if (str.indexOf(" -") > 0 || str.indexOf("- ") > 0) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_DASH);
      } else {
        return new ValidationResult(input, true);
      }
    },
    CHECKDASHSPACE : function(input) {
      var value = FormManager.getActualValue(input);
      if (value && value.length > 0 && (value.match("[?<!-]+$") || value.substr(0, 1) == ("-"))) {
        return new ValidationResult(input, false, MessageMgr.MESSAGES.INVALID_DASH_SPACE);
      } else {
        return new ValidationResult(input, true);
      }
    },
    DIGIT_OR_DOT : function(fieldOrId) {
      var inputVal = FormManager.getActualValue(fieldOrId);
      if (inputVal && inputVal.length > 0 && !inputVal.match("^[0-9.]+$")) {
        return new ValidationResult(fieldOrId, false, MessageMgr.MESSAGES.DIGIT_OR_DOT);
      } else {
        return new ValidationResult(fieldOrId, true);
      }
    },
    NO_SINGLE_BYTE : function(fieldOrId) {
      var inputVal = FormManager.getActualValue(fieldOrId);
      var reg = /[0-9a-zA-Z\-(). \\\/]/;
      if (inputVal && inputVal.length > 0 && reg.test(inputVal)) {
        return new ValidationResult(fieldOrId, false, MessageMgr.MESSAGES.NO_SINGLE_BYTE);
      } else {
        return new ValidationResult(fieldOrId, true);
      }
    },
    NO_HALF_ANGLE : function(fieldOrId) {
      var value = FormManager.getActualValue(fieldOrId);
      if (!value || value == '' || value.length == 0) {
        return true;
      }
      var reg = /[\uff66-\uff9f]/;
      if (reg.test(value)) {
        return new ValidationResult(fieldOrId, false, MessageMgr.MESSAGES.NO_HALF_ANGLE);
      } else {
        return new ValidationResult(fieldOrId, true);
      }
    },
    TWO_DASHES_FORMAT : function(fieldOrId) {
      var inputVal = FormManager.getActualValue(fieldOrId);
      var reg = /^[^-]{1,}[-]{1}[^-]{1,}[-]{1}[^-]{1,}$/;
      if (inputVal && inputVal.length > 0) {
        if (!reg.test(inputVal)) {
          return new ValidationResult(fieldOrId, false, MessageMgr.MESSAGES.TWO_DASHES_FORMAT);
        } else {
          return new ValidationResult(fieldOrId, true);
        }
      } else {
        return new ValidationResult(fieldOrId, true);
      }
    }
  };
}());

var FormManager = (function() {
  var formValidations = new Array();
  var formFields = {};
  var formCondFields = {};
  var formParams = {};
  var formFieldTabs = {};
  var formReady = false;
  var skipByteCheckFields = [];
  var checkFunction = null;
  var error = new Array();
  var STATE_NEW = 0;
  var STATE_EXISTING = 1;
  var addError = function(validationResult) {
    if (validationResult.formType) {
      error.push(validationResult);
    } else {
      if (!validationResult.field || !validationResult.field.id || validationResult.field.id.length == 0) {
        return;
      }
      var fieldID = validationResult.field.id;
      if (validationResult.field.type == 'radio') {
        fieldID = validationResult.field.name;
      }
      if (!validationResult.params) {
        validationResult.params = formParams[fieldID];
      }
      if (!validationResult.tabId) {
        validationResult.tabId = formFieldTabs[fieldID];
      }
      error.push(validationResult);
    }
  };

  var waitForPage = function() {
    cmr.showAlert('Please wait for page to completely load first.');
  };

  return {
    addFormValidator : function(validator, tabId, formName) {
      validator.tabId = tabId;
      if (formValidations[formName] == null) {
        formValidations[formName] = new Array();
      }
      formValidations[formName].push(validator);
    },
    addValidator : function(fieldIdOrName, validator, params, tabId) {
      if (!formFields[fieldIdOrName]) {
        formFields[fieldIdOrName] = new Array();
      }
      formFields[fieldIdOrName].push(validator);
      formParams[fieldIdOrName] = params;
      formFieldTabs[fieldIdOrName] = tabId;

      if (validator == Validators.REQUIRED && dojo.byId('ast-' + fieldIdOrName) == null) {
        var label = dojo.query('label[for="' + fieldIdOrName + '"]');
        var mand = '<span style="color:red" class="cmr-ast" id="ast-' + fieldIdOrName + '">* </span>';
        if (label && label[0]) {
          var change = dojo.query(label[0]).query('img.cmr-delta-icon');
          if (change && change[0]) {
            dojo.place(mand, change[0], 'before');
          } else {
            var info = dojo.query(label[0]).query('img.cmr-info-bubble');
            if (info && info[0]) {
              dojo.place(mand, info[0], 'before');
            } else {
              dojo.place(mand, label[0], 'last');
            }
          }
        }
      }
    },
    removeValidator : function(fieldIdOrName, validator) {
      var validators = formFields[fieldIdOrName];
      if (validators != null) {
        var index = validators.indexOf(validator);
        if (index > -1) {
          validators.splice(index, 1);
          if (validator == Validators.REQUIRED && dojo.byId('ast-' + fieldIdOrName) != null) {
            dojo.query('#ast-' + fieldIdOrName).remove();
          }
        }
      }
      if (validator.name && validators != null) {
        var index = -1;
        for (var i = 0; i < validators.length; i++) {
          if (validator.name == validators[i].name) {
            index = i;
            break;
          }
        }
        if (index > -1) {
          validators.splice(index, 1);
        }
      }
    },
    addConditionalValidator : function(fieldIdOrName, params, sourceFieldId, sourceValue, tabId) {
      if (!formCondFields[fieldIdOrName]) {
        formCondFields[fieldIdOrName] = new Array();
      }
      var condValidator = new ConditionalValidator(fieldIdOrName, params, sourceFieldId, sourceValue);
      formCondFields[fieldIdOrName].push(condValidator);
      formFieldTabs[fieldIdOrName] = tabId;
    },
    validate : function(formName, modal, noMessage, noFormValidators) {
      if (!formReady) {
        waitForPage();
        return false;
      }
      MessageMgr.clearMessages(modal);
      error = new Array();
      var form = document.forms[formName];
      if (!form) {
        // no form found, set validation to true
        return true;
      }
      var inputs = form.elements;
      var idOrName = null;
      var field = null;
      var hasError = false;

      var validatedRadios = {};
      for (var i = 0; i < inputs.length; i++) {
        field = inputs[i];
        idOrName = field.id;
        if (!formFields[idOrName]) {
          idOrName = field.name;
        }
        if (validatedRadios[idOrName] == 'Y') {
          continue;
        }
        if (field.type == 'radio') {
          validatedRadios[idOrName] = 'Y';
        }
        if (formFields[idOrName]) {
          var validator = null;
          var result = null;
          for (var j = 0; j < formFields[idOrName].length; j++) {
            validator = formFields[idOrName][j];
            if (validator) {
              var type = typeof (validator);
              if (type == 'object') {
                if (validator.type == 'db' && dojo.byId(idOrName + '-validated') != null) {
                  cmr.hideNode(idOrName + '-validated');
                }
                result = validator.validate(field);
              } else {
                result = validator(field);
              }
            } else {
              result = new ValidationResult(null, true);
            }

            if (!result.success) {
              addError(result);
              hasError = true;
              break;
            } else {
              var type = typeof (validator);
              if (type == 'object') {
                if (validator.type == 'db' && dojo.byId(idOrName + '-validated') != null) {
                  if (dojo.byId(idOrName + '-validated')) {
                    dojo.byId(idOrName + '-validated').style.display = 'inline';
                  }
                }
              }
            }
          }
        }
        if (formCondFields[idOrName]) {
          // conditional validation
          for (var j = 0; j < formCondFields[idOrName].length; j++) {
            validator = formCondFields[idOrName][j];
            if (validator) {
              result = validator.validate(field);
            } else {
              result = new ValidationResult(null, true);
            }

            if (!result.success) {
              addError(result);
              hasError = true;
              break;
            }
          }
        }
      }
      if (!noFormValidators) {
        if (formValidations[formName]) {
          for (var f = 0; f < formValidations[formName].length; f++) {
            result = formValidations[formName][f].validate();
            if (result != undefined && !result.success) {
              if (result.field == null) {
                result.formType = true;
              }
              result.tabId = formValidations[formName][f].tabId;
              addError(result);
              hasError = true;
            }
          }
        }
      }
      if (hasError && error.length > 0) {
        if (!noMessage) {
          MessageMgr.showValidationErrors(error, modal);
        }
        return false;
      } else {
        return true;
      }
    },
    save : function(formName, message) {
      if (!formReady) {
        waitForPage();
        return;
      }
      if (!FormManager.validate(formName)) {
        return;
      }
      if (STATE_NEW == parseInt(dojo.byId(formName + '_modelState').value)) {
        dojo.byId(formName + '_modelAction').value = 'I';
      } else if (STATE_EXISTING == parseInt(dojo.byId(formName + '_modelState').value)) {
        dojo.byId(formName + '_modelAction').value = 'U';
      }
      if (!message) {
        message = 'Processing. Please wait...';
      }
      cmr.showProgress(message);
      document.forms[formName].submit();
    },
    remove : function(formName) {
      if (!formReady) {
        waitForPage();
        return;
      }
      if (!confirm('Delete record?')) {
        return;
      }
      if (STATE_EXISTING == parseInt(dojo.byId(formName + '_modelState').value)) {
        dojo.byId(formName + '_modelAction').value = 'D';
      }
      cmr.showProgress('Processing. Please wait...');
      document.forms[formName].submit();
    },
    doAction : function(formName, actionName, noValidate, progressMsg) {
      if (!formReady) {
        waitForPage();
        return;
      }
      if (!noValidate && !FormManager.validate(formName)) {
        return;
      }
      dojo.byId(formName + '_modelAction').value = actionName;
      cmr.showProgress(progressMsg ? progressMsg : 'Processing. Please wait...');
      document.forms[formName].submit();
    },
    doHiddenAction : function(formName, actionName, urlName, noValidate, afterActionFunc, useModal, timeout) {
      if (!formReady) {
        waitForPage();
        return;
      }
      if (!noValidate && !FormManager.validate(formName, useModal)) {
        return;
      }
      var result = null;
      dojo.byId(formName + '_modelAction').value = actionName;
      cmr.showProgress('Processing. Please wait...');
      dojo.xhrPost({
        url : urlName,
        handleAs : 'json',
        content : dojo.formToObject(formName),
        method : 'POST',
        timeout : timeout ? timeout : 50000,
        // sync : true,
        load : function(data, ioargs) {
          cmr.hideProgress();
          result = data ? data.result : {
            success : false,
            message : ''
          };
          if (afterActionFunc) {
            afterActionFunc(result);
          }
          if (result.success) {
            MessageMgr.showInfoMessage(result.message, false);
          } else {
            MessageMgr.showErrorMessage(result.message, useModal);
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          console.log(typeof (error));
          result = {
            success : false,
            message : error
          };
          // dirty fix
          if (!error.columnNumber) {
            if (afterActionFunc) {
              afterActionFunc(result);
            }
          }
        }
      });
      return result;
    },
    doHiddenCMRAction : function(formName, actionName, urlName, noValidate, afterActionFunc, useModal) {
      if (!formReady) {
        waitForPage();
        return;
      }
      if (!noValidate && !FormManager.validate(formName, useModal)) {
        return;
      }
      var result = null;
      dojo.byId(formName + '_modelAction').value = actionName;
      cmr.showProgress('Processing. Please wait...');
      dojo.xhrPost({
        url : urlName,
        handleAs : 'json',
        content : dojo.formToObject(formName),
        method : 'POST',
        timeout : 50000,
        // sync : true,
        load : function(data, ioargs) {
          cmr.hideProgress();
          result = data ? data.result : {
            success : false,
            message : ''
          };
          if (afterActionFunc) {
            afterActionFunc(result);
          }
          if (result.displayMsg) {
            if (result.success) {
              MessageMgr.showInfoMessage(result.message, false);
            } else {
              MessageMgr.showErrorMessage(result.message, useModal);
            }
          }

        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          console.log(typeof (error));
          result = {
            success : false,
            message : error
          };
          // dirty fix
          if (!error.columnNumber) {
            if (afterActionFunc) {
              afterActionFunc(result);
            }
          }
        }
      });
      return result;
    },
    doHiddenFileAction : function(formName, actionName, urlName, noValidate, tokenId, useModal) {
      if (!formReady) {
        waitForPage();
        return;
      }
      if (!noValidate && !FormManager.validate(formName, useModal)) {
        return;
      }
      var token = new Date().getTime();
      if (dojo.byId(tokenId)) {
        dojo.byId(tokenId).value = token;
      }
      dojo.byId(formName + '_modelAction').value = actionName;
      cmr.showProgress('Processing. Please wait...');
      dojo.io.iframe.send({
        url : urlName,
        form : formName,
        handleAs : 'json',
        method : 'POST',
        timeout : 50000,
        load : function(data, ioargs) {
        },
        error : function(error, ioargs) {
        }
      });
      window.setTimeout('checkToken("' + token + '")', 1000);
    },
    gridAction : function(formName, action, confirmMessage) {
      if (!formReady) {
        waitForPage();
        return;
      }
      if (!CmrGrid.hasSelected()) {
        alert('No record(s) selected.');
        return;
      }
      if (confirmMessage) {
        if (!confirm(confirmMessage)) {
          return;
        }
      }
      dojo.byId(formName + '_modelState').value = 1;
      dojo.byId(formName + '_modelAction').value = 'S';
      dojo.byId(formName + '_modelMassAction').value = action;
      cmr.showProgress('Processing. Please wait...');
      document.forms[formName].submit();
    },
    gridHiddenAction : function(formName, actionName, urlName, noValidate, afterActionFunc, useModal, confirmMessage, timeout) {

      if (!formReady) {
        waitForPage();
        return;
      }
      if (!CmrGrid.hasSelected()) {
        alert('No record(s) selected.');
        return;
      }
      if (confirmMessage) {
        if (!confirm(confirmMessage)) {
          return;
        }
      }

      if (!noValidate && !FormManager.validate(formName, useModal)) {
        return;
      }
      dojo.byId(formName + '_modelState').value = 1;

      // dojo.byId(formName + '_modelMassAction').value = actionName;

      var formId = formName;
      if (!dojo.byId(formId)) {
        formId = document.forms[formName] ? document.forms[formName].id : formName;
      }
      var result = null;
      dojo.byId(formName + '_modelAction').value = actionName;
      cmr.showProgress('Processing. Please wait...');
      dojo.xhrPost({
        url : urlName,
        handleAs : 'json',
        content : dojo.formToObject(formId),
        method : 'POST',
        timeout : timeout ? timeout : 50000,
        // sync : true,
        load : function(data, ioargs) {
          cmr.hideProgress();
          result = data ? data.result : {
            success : false,
            message : ''
          };
          if (afterActionFunc) {
            afterActionFunc(result);
          }
          if (result.success) {
            MessageMgr.showInfoMessage(result.message, false);
          } else {
            MessageMgr.showErrorMessage(result.message, useModal);
          }
        },
        error : function(error, ioargs) {
          cmr.hideProgress();
          console.log(typeof (error));
          result = {
            success : false,
            message : error
          };
          // dirty fix
          if (!error.columnNumber) {
            if (afterActionFunc) {
              afterActionFunc(result);
            }
          }
        }
      });
      return result;
    },
    ready : function() {
      formReady = true;
    },
    isReady : function() {
      return formReady;
    },
    setCheckFunction : function(func) {
      checkFunction = func;
    },
    shouldCheckBeforeLeave : function() {
      return checkFunction != null;
    },
    executeCheckFunction : function(url) {
      checkFunction(url);
    },
    getActualValue : function(input) {
      var id = null;
      if (typeof (input) == 'string') {
        id = input;
      } else {
        id = input.id;
      }
      var value = null;
      try {
        value = dijit.byId(id).get('value');
      } catch (e) {

      }
      if (!value) {
        try {
          value = dojo.byId(id).value;
        } catch (e) {

        }
      }
      // try radio or checkboxes
      if (!value) {
        var inputs = document.getElementsByName(id);
        if (inputs && inputs.length) {
          for (var i = 0; i < inputs.length; i++) {
            if (inputs[i].tagName == 'INPUT' && (inputs[i].type == 'radio' || inputs[i].type == 'checkbox')) {
              if (inputs[i].checked) {
                value = inputs[i].value;
                break;
              }
            }
          }
        }
      }
      if (value) {
        value = dojo.string.trim(value);
      } else {
        value = '';
      }
      return value;
    },
    resetValidations : function(fieldId) {
      if (fieldId) {
        formFields[fieldId] = null;
        formCondFields[fieldId] == null;
        formParams[fieldId] == null;
        dojo.query('#ast-' + fieldId).remove();
      } else {
        // no field passed, clear all
        formFields = {};
        formCondFields = {};
        formParams = {};
        dojo.query('span.cmr-ast').remove();
      }
    },
    getField : function(fieldId) {
      var field = dijit.byId(fieldId);
      if (field) {
        return field;
      } else {
        field = dojo.byId(fieldId);
        if (field) {
          return field;
        } else {
          return null;
        }
      }
      return null;
    },
    disable : function(fieldId) {
      var field = dijit.byId(fieldId);
      if (field) {
        field.set('disabled', true);
        field.set('readOnly', true);
        if (field.type == 'text') {
          dojo.addClass(dojo.byId(fieldId), 'cmr-disabled');
        }
      } else {
        field = dojo.byId(fieldId);
        if (field) {
          field.setAttribute('disabled', 'true');
          field.setAttribute('readOnly', 'true');
          if (field.type == 'text') {
            dojo.addClass(dojo.byId(fieldId), 'cmr-disabled');
          }
        } else {
          var groups = document.getElementsByName(fieldId);
          if (groups) {
            for (var i = 0; i < groups.length; i++) {
              if (groups[i].tagName == 'INPUT' && (groups[i].type == 'radio' || groups[i].type == 'checkbox')) {
                if (groups[i].id) {
                  FormManager.disable(groups[i].id);
                } else {
                  groups[i].setAttribute('disabled', 'true');
                }
              }
            }
          }
        }
      }
    },
    enable : function(fieldId) {
      var field = dijit.byId(fieldId);
      if (field) {
        field.set('disabled', false);
        field.set('readOnly', false);
        if (field.type == 'text') {
          dojo.removeClass(dojo.byId(fieldId), 'cmr-disabled');
          dojo.removeClass(dojo.byId(fieldId), 'cmr-readOnly');
        }
      } else {
        field = dojo.byId(fieldId);
        if (field) {
          field.removeAttribute('disabled');
          field.removeAttribute('readonly');
          if (field.type == 'text') {
            dojo.removeClass(dojo.byId(fieldId), 'cmr-disabled');
            dojo.removeClass(dojo.byId(fieldId), 'cmr-readOnly');
          }
        } else {
          var groups = document.getElementsByName(fieldId);
          if (groups) {
            for (var i = 0; i < groups.length; i++) {
              if (groups[i].tagName == 'INPUT' && (groups[i].type == 'radio' || groups[i].type == 'checkbox')) {
                if (groups[i].id) {
                  FormManager.enable(groups[i].id);
                } else {
                  groups[i].removeAttribute('disabled');
                }
              }
            }
          }
        }
      }
    },
    readOnly : function(fieldId) {
      var field = dijit.byId(fieldId);
      if (field) {
        field.set('readOnly', true);
        if (field.type == 'text') {
          dojo.addClass(dojo.byId(fieldId), 'cmr-readOnly');
        } else if (field.type == 'checkbox') {
          field.set('disabled', true);
        }
      } else {
        field = dojo.byId(fieldId);
        if (field) {
          field.setAttribute('readonly', 'true');
          if (field.type == 'text') {
            dojo.addClass(dojo.byId(fieldId), 'cmr-readOnly');
          } else if (field.type == 'checkbox') {
            field.setAttribute('disabled', 'true');
          }
        } else {
          var groups = document.getElementsByName(fieldId);
          if (groups) {
            for (var i = 0; i < groups.length; i++) {
              if (groups[i].tagName == 'INPUT' && (groups[i].type == 'radio' || groups[i].type == 'checkbox')) {
                if (groups[i].id) {
                  FormManager.disable(groups[i].id);
                } else {
                  groups[i].setAttribute('readonly', 'true');
                }
              }
            }
          }
        }
      }
    },
    mandatory : function(name, id, parentTab) {
      var label = dojo.byId('cmr-fld-lbl-' + id) ? dojo.byId('cmr-fld-lbl-' + id).innerHTML : id;
      FormManager.addValidator(name, Validators.REQUIRED, [ label ], parentTab);
    },
    hide : function(fieldId, fieldNm) {
      FormManager.disable(fieldNm);
      var container = dojo.byId('container-' + fieldId);
      if (container) {
        container.style.display = 'none';
      }
    },
    show : function(fieldId, fieldNm) {
      var container = dojo.byId('container-' + fieldId);
      if (container) {
        container.style.display = 'inline-block';
      }
      FormManager.enable(fieldNm);
    },
    changeLabel : function(fieldId, label) {
      var lbl = dojo.byId('cmr-fld-lbl-' + fieldId);
      if (lbl) {
        lbl.innerHTML = label;
      }
      var lbls = dojo.query('span.lbl-' + fieldId);
      if (lbls && lbls.length > 0) {
        for (var i = 0; i < lbls.length; i++) {
          lbls[i].innerHTML = label;
        }
      }
    },
    setValue : function(fieldId, value) {
      var field = dijit.byId(fieldId);
      if (field) {
        field.set('value', value);
      } else {
        field = dojo.byId(fieldId);
        if (field) {
          if (field.type == 'checkbox' || field.type == 'radio') {
            if (field.value == value) {
              field.checked = true;
            }
          } else {
            field.value = value;
          }
        } else {
          var groups = document.getElementsByName(fieldId);
          if (groups) {
            for (var i = 0; i < groups.length; i++) {
              if (groups[i].tagName == 'INPUT' && (groups[i].type == 'radio' || groups[i].type == 'checkbox')) {
                try {
                  if (groups[i].id && groups[i].value == value) {
                    dijit.byId(groups[i].id).set('checked', true);
                  } else {
                    groups[i].checked = groups[i].value == value;
                  }
                } catch (e) {
                  groups[i].checked = groups[i].value == value;
                }
              }
            }
          }
        }
      }
    },
    clearValue : function(fieldId) {
      var field = dijit.byId(fieldId);
      if (field) {
        field.set('value', '');
      } else {
        field = dojo.byId(fieldId);
        if (field) {
          if (field.type == 'checkbox' || field.type == 'radio') {
            if (field.value == value) {
              field.checked = false;
            }
          } else {
            field.value = '';
          }
        } else {
          var groups = document.getElementsByName(fieldId);
          if (groups) {
            for (var i = 0; i < groups.length; i++) {
              if (groups[i].tagName == 'INPUT' && (groups[i].type == 'radio' || groups[i].type == 'checkbox')) {
                try {
                  dijit.byId(groups[i].id).set('checked', false);
                } catch (e) {
                  groups[i].checked = false;
                }
              }
            }
          }
        }
      }
    },
    getLabel : function(fieldName) {
      var label = dojo.byId('cmr-fld-lbl-' + fieldName) ? dojo.byId('cmr-fld-lbl-' + fieldName).innerHTML : fieldName;
      return label;
    },
    // #1285796 : special case for BR(631) but can be reused to other countries
    // formFields : storage of validations for each fields
    GETFIELD_VALIDATIONS : formFields,
    resetDropdownValues : function(field) {
      if (field && field.loadedStore) {
        field.store = field.loadedStore;
      }
    },
    limitDropdownValues : function(field, values) {
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
    },
    skipByteChecks : function(arrOfFieldNames) {
      if (arrOfFieldNames) {
        for (var i = 0; i < arrOfFieldNames.length; i++) {
          console.log('skipping byte check for field ' + arrOfFieldNames[i]);
          skipByteCheckFields.push(arrOfFieldNames[i]);
        }
      }
    },
    checkBytes : function(fieldId) {
      return skipByteCheckFields.indexOf(fieldId) < 0;
    }
  };
}());

function ValidationResult(field, success, message) {
  this.field = field;
  this.message = message;
  this.success = success;
  return this;
};

function ConditionalValidator(fieldId, params, sourceFieldId, sourceValue) {
  this.fieldId = fieldId;
  this.params = params;
  this.sourceField = sourceFieldId;
  this.sourceValue = sourceValue;

  this.validate = function(input) {

    var src = dojo.byId(this.sourceField);
    if (src) {
      var value = FormManager.getActualValue(src);
      var trgValue = FormManager.getActualValue(input);
      if ((this.sourceValue && value == this.sourceValue) && trgValue.length == 0) {
        var result = new ValidationResult(input, false, MessageMgr.MESSAGES.CONDITIONAL_REQ_VAL);
        result.params = this.params;
        result.params.push(value);
        return result;
      }
      if ((!this.sourceValue && dojo.string.trim(src.value).length > 0) && trgValue.length == 0) {
        var result = new ValidationResult(input, false, MessageMgr.MESSAGES.CONDITIONAL_REQ);
        result.params = this.params;
        return result;
      }
    }

    return new ValidationResult(input, true);
  };
  return this;
}

function ActionResult(status, message) {
  this.status = status;
  this.message = message;

  this.getMessage = function() {
    return this.message;
  };
  this.success = function() {
    return this.status;
  };
}

/**
 * Formatter for Comment to warp the word
 * 
 * @param value
 * @param rowIndex
 * @returns {String}
 */
function errorTxtFormatter(value, rowIndex) {
  rowData = this.grid.getItem(rowIndex);
  var cmtdata = rowData.errorTxt;
  if (cmtdata != null && cmtdata[0] != null) {
    cmtdata = cmtdata[0].replace(/\n/g, '<br>');
  }
  return '<span style="word-wrap: break-word">' + cmtdata + '</span>';
}