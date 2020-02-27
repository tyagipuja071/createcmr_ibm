dojo.require("dojo.NodeList-traverse");
dojo.require("dojo.fx.Toggler");
/**
 * Scripts to handle retrieval of messages from the backend, via AJAX
 * 
 */
var MessageMgr = (function() {
  var clientMessages = {};

  var clientMsgUrl = cmr.CONTEXT_ROOT + '/messages/client.json';

  var getClientMessages = function() {
    dojo.xhrGet({
      url : clientMsgUrl,
      handleAs : 'json',
      method : 'GET',
      timeout : 50000,
      load : function(data, ioargs) {
        clientMessages.REQUIRED = data.m5001;
        clientMessages.INVALID_VALUE = data.m5002;
        clientMessages.BLUEPAGES_ERROR = data.m5003;
        clientMessages.OUTOFRANGE_VALUE = data.m5004;
        clientMessages.CONDITIONAL_REQ = data.m5005;
        clientMessages.CONDITIONAL_REQ_VAL = data.m5006;
        clientMessages.SEARCH_CRIT_ONLY_ONE = data.m5007;
        clientMessages.SEARCH_CRIT_AT_LEAST_ONE = data.m5008;
        clientMessages.SEARCH_CMR_NUM_CNTRY = data.m5009;
        clientMessages.NOT_FOUND_IN_DB = data.m5010;
        clientMessages.INVALID_NUMERIC_VALUE = data.m5011;
        clientMessages.INVALID_VALUE = data.m5012;
        clientMessages.MAXLENGTH = data.m5013;
        clientMessages.INVALID_PHONE = data.m5014;
        clientMessages.NO_SPECIAL_CHARACTERS = data.m5015;
        clientMessages.INVALID_ALPHANUMERIC = data.m5016;
        clientMessages.INVALID_ALPHABET = data.m5017;
        clientMessages.LATIN = data.m5018;
        clientMessages.NON_LATIN = data.m5019;
        clientMessages.DIGIT_OR_DOT = data.m5020;
        clientMessages.INVALID_NUMAZ = data.m5021;
        clientMessages.INVALID_DASH = data.m5022;
        clientMessages.INVALID_DASH_SPACE = data.m5023;
        clientMessages.NO_SINGLE_BYTE = data.m5024;
        clientMessages.TWO_DASHES_FORMAT = data.m5025;
        clientMessages.NO_HALF_ANGLE = data.m5026;
      },
      error : function(error, ioargs) {
      }
    });
  };

  var clearMessagesInternal = function(modal) {
    dojo.byId('cmr-info-box').style.display = 'none';
    dojo.byId('cmr-error-box').style.display = 'none';
    dojo.byId('cmr-validation-box').style.display = 'none';
    dojo.query("img[class='cmr-input-error-icon']").remove();
    if (modal) {
      try {
        dojo.byId(cmr.currentModalId + '-cmr-info-box-modal').style.display = 'none';
        dojo.byId(cmr.currentModalId + '-cmr-error-box-modal').style.display = 'none';
        dojo.byId(cmr.currentModalId + '-cmr-validation-box-modal').style.display = 'none';
      } catch (e) {
        // noop
      }
    }
  };

  dojo.ready(function() {
    getClientMessages();
  });

  return {
    get : function(msgCode, msgParams) {
      var returnedMsg = '';
      dojo.xhrGet({
        url : 'messages/' + msgCode,
        content : {
          params : msgParams
        },
        handleAs : 'text',
        method : 'GET',
        timeout : 50000,
        sync : true,
        load : function(data, ioargs) {
          returnedMsg = data;
        },
        error : function(error, ioargs) {
          returnedMsg = 'Message could not be retrieved at this time.';
        }
      });
      return returnedMsg;
    },
    showInfoMessage : function(message, modal, alwaysShow) {
      var suff = modal ? '-modal' : '';
      clearMessagesInternal();
      if (message) {
        dojo.byId('cmr-info-box-msg' + suff).innerHTML = message;
      }
      dojo.byId('cmr-info-box' + suff).style.display = '';
      dojo.byId('cmr-info-box' + suff).style.opacity = '1';
      if (!alwaysShow) {
        var toggle = new dojo.fx.Toggler({
          node : 'cmr-info-box' + suff,
          hideDuration : 1000
        });
        cmr.removeInfo = function() {
          dojo.byId('cmr-info-box' + suff).style.display = 'none';
          cmr.toggle = null;
          document.onclick = null;
        };
        cmr.toggle = function() {
          toggle.hide();
          window.setTimeout('cmr.removeInfo()', 1000);
        };
      }
      document.onclick = function() {
        if (cmr.toggle) {
          cmr.toggle();
        }
      };
      if (!modal) {
        window.setTimeout('cmr.toTop()', 500);
      } else {
      }
    },
    showErrorMessage : function(message, modal) {
      var suff = modal ? '-modal' : '';
      var pref = modal ? cmr.currentModalId + '-' : '';
      clearMessagesInternal();
      if (message) {
        dojo.byId(pref + 'cmr-error-box-msg' + suff).innerHTML = message;
      }
      dojo.byId(pref + 'cmr-error-box' + suff).style.display = '';
      if (!modal) {
        window.setTimeout('cmr.toTop()', 500);
      } else {
      }
    },
    clearMessages : clearMessagesInternal,
    showValidationErrors : function(errors, modal) {
      clearMessagesInternal(modal);
      var html = '';
      var msg = '';
      var error = null;
      for (var i = 1; i <= errors.length; i++) {
        error = errors[i - 1];
        if (!error.formType && (!error.field.id || error.field.id.length == 0)) {
          continue;
        }
        msg = error.message;
        if (error.params) {
          var j = 0;
          for (j = 0; j < error.params.length; j++) {
            msg = msg.replace('{' + j + '}', error.params[j]);
          }
          if (error.field) {
            msg = msg.replace('{' + j + '}', error.field.value);
          }
        }

        // construct error message
        var fieldId = error.formType ? 'xxx' : error.field.id;
        if (error.field && error.field.type == 'radio') {
          fieldId = error.field.name;
        }

        var focus = error.tabId ? 'focusActualField(\'' + fieldId + '\', \'' + error.tabId + '\', ' + modal + ')' : 'focusActualField(\'' + fieldId + '\', null, ' + modal + ')';
        html += '<li onclick="' + focus + '">' + i + '. ' + msg + '</li>';

        // add the error icon to label
        var icon = '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/error-icon.png" class="cmr-input-error-icon" title="' + msg + '">';
        var label = error.formType ? null : dojo.query('label[for="' + fieldId + '"]');
        if (label && label[0]) {
          dojo.place(icon, label[0], 'last');
        }

        if (error.tabId) {
          // add the error icon to tab
          var icon2 = '<img src="' + cmr.CONTEXT_ROOT + '/resources/images/error-icon.png" class="cmr-input-error-icon" title="Some fields have errors.">';
          var tab = dojo.query('#' + error.tabId);
          if (tab && tab[0].innerHTML.indexOf('cmr-input-error-icon') < 0) {
            tab[0].innerHTML = tab[0].innerHTML + icon2;
          }
        }
      }
      var suff = modal ? '-modal' : '';
      var pref = modal ? cmr.currentModalId + '-' : '';
      dojo.byId(pref + 'cmr-validation-box-list' + suff).innerHTML = html;
      dojo.byId(pref + 'cmr-validation-box' + suff).style.display = '';
      if (!modal) {
        window.location.href = '#_MESSAGES';
      } else {
        if (cmr.currentModalId) {
          dojo.query('#dialog_' + cmr.currentModalId + ' div.dijitDialogPaneContent')[0].scrollTo(0, 0);
        }
      }
    },
    MESSAGES : clientMessages
  };
})();

function focusActualField(id, tabId, modal) {
  if (!modal) {
    var parentTab = null;
    if (tabId) {
      parentTab = dojo.byId(tabId);
    }

    // a parent tab has been defined, try to open it first
    if (parentTab && cmr && (id != 'xxx')) {
      cmr.selectTab(parentTab);
    }

    if (dojo.query('#' + id).length > 0) {
      var parentSection = dojo.query('#' + id).parents('.cmr-sub');
      if (parentSection) {
        parentSection = parentSection[0];
        var sections = dojo.query('.cmr-sub');
        for (var i = 0; i < sections.length; i++) {
          if (sections[i].getAttribute('class').indexOf('cmr-nohide') < 0) {
            sections[i].style.display = 'none';
          }
        }
        parentSection.style.display = '';
      }
    }

    if (parentTab && id == 'xxx') {
      if (typeof (cmr.sectionMap) != 'undefined' && cmr.sectionMap[tabId] && typeof (switchTabs) == 'function') {
        cmr.selectTab(parentTab);
        switchTabs(cmr.sectionMap[tabId]);
      }
    }

  }
  if (dojo.byId(id)) {
    dojo.byId(id).focus();
  }
}