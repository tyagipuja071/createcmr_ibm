/*global dojo, dijit, dojox, ibmweb, ibmOverlays, messageObject*/
/*
 * JS for handling Help Page, Displaying it in separate window and brings it
 * to foreground it is already open
 */

var popupWindow = null;
var okCallbackHandle = null;
var cancelCallbackHandle = null;
var personFullId = null;

var cmr = {
  CONTEXT_ROOT : '',
  PageTypeEnum : {
    LOGIN : {
      pageName : "LOGIN"
    },
    QUOTA_HOME_OVERVIEW : {
      pageName : "QUOTA_HOME_OVERVIEW"
    }
  },
  alertShown : false,
  progressShown : false,
  confirmShown : false,
  superUser : false,
  clearAndHideElements : function(elementIdArray) {
    for ( var idx in elementIdArray) {
      var elId = elementIdArray[idx];
      var el = dojo.byId(elId);
      if (el) {
        el.innerHTML = "";
        el.style.display = "none";
      }
    }
  },
  isDetailsTotalUnbalanced : function(groupPeriods, listOfBooleans) {
    for ( var periodIdx in groupPeriods) {
      var period = groupPeriods[periodIdx];
      if (listOfBooleans[period] && listOfBooleans[period] == true) {
        return true;
      }
    }
    return false;
  },
  showNode : function(id) {
    if (dojo.byId(id)) {
      dojo.byId(id).style.display = '';
    }
  },
  hideNode : function(id) {
    if (dojo.byId(id)) {
      dojo.byId(id).style.display = 'none';
    }
  },
  hideWidget : function(id) {
    dijit.byId(id).domNode.style.display = 'none';
  },
  showWidget : function(id) {
    var el = dijit.byId(id);
    el.domNode.style.display = '';
    return el;
  },
  onNewMessage : function(messages) {
    var messageIconElementData = "<span style='float:right !important;'><a style='position:absolute; top:5px; margin-left:5px; padding-right:0px; background-repeat:no-repeat !important;  background-position: 0 -401px !important; background-color: transparent;' id='messageBoxNewMessagesIcon' title='You have new messages. Click on the Messages tab to see them.' class='ibm-email-link' href='#' alt='' onclick='return false;'></a></span>";
    var messageIconElement = dojo.byId("messageBoxNewMessagesIcon");
    if (messageIconElement) {
      cmr.showNode(messageIconElement.id);
    } else {
      // create icon
      var holder = dojo.query("li#MESSAGES > a");
      dojo.place(messageIconElementData, holder[0]);
    }
  },
  onAllReadMessages : function() {
    var messageIconElement = dojo.byId("messageBoxNewMessagesIcon");
    if (messageIconElement) {
      cmr.hideNode(messageIconElement.id);
    }
  },
  createAndStartPollMechanism : function(pollFunction, intervalTime) {
    if (typeof (pollFunction) != "function") {
      throw "The poll function in the argument is not a function object.";
    }
    if (intervalTime == null) {
      intervalTime = 120000;
    }
    var MessagePoll = function(pollFunction, intervalTime) {
      var intervalId = null;
      this.start = function(newPollFunction, newIntervalTime) {
        pollFunction = newPollFunction || pollFunction;
        intervalTime = newIntervalTime || intervalTime;
        if (intervalId) {
          this.stop();
        }
        intervalId = setInterval(pollFunction, intervalTime);
      };
      this.stop = function() {
        clearInterval(intervalId);
      };
    };
    var poll = new MessagePoll(pollFunction, intervalTime);
    poll.start();
    dojo.hitch(poll, poll.stop);
  },
  showProgress : function(progressTitle) {
    ibmweb.overlay.show("progressOverlay", this);
    var header = dojo.byId("progressOverlayTitle");
    if (header) {
      if (progressTitle) {
        header.innerHTML = progressTitle;
      } else {
        // use default
        header.innerHTML = "Loading, please wait...";
      }
    }
    if (dojo.byId('dialog_progressOverlay')) {
      dojo.byId('dialog_progressOverlay').setAttribute('role', 'alert');
    }
    try {
      ibmweb.queue.push(function() {
        return dojo.query("div#dialog_progressOverlay .dijitDialogCloseIcon").length == 1;
      }, function() {
        dojo.query("div#dialog_progressOverlay .dijitDialogCloseIcon").style("visibility", "hidden");
      });
    } catch (e) {

    }
    progressShown = true;
  },
  hideProgress : function() {
    if (!progressShown) {
      return;
    }
    var me = this;

    try {
      ibmweb.overlay.hide("progressOverlay");
    } catch (e) {
      ibmweb.queue.push(function() {
        return dojo.query("div#dialog_progressOverlay").length == 1;
      }, function() {
        ibmweb.overlay.hide("progressOverlay", me);
      });
    }
    progressShown = false;
  },
  hideIBMLoader : function() {
    dojo.fadeOut({
      node : "ibm-loader-screen",
      onEnd : function() {
        dojo.style("ibm-loader-screen", "display", "none");
      }
    }).play();
  },
  hideLoader : function() {
    dojo.fadeOut({
      node : "ibm-loader-screen",
      onEnd : function() {
        dojo.style("ibm-loader-screen", "display", "none");
      }
    }).play();
  },
  setBluePagesSearch : function(fieldId, inetFieldId, useUID, notesId, both) {
    if (!dojo.byId(fieldId)) {
      return;
    }
    var handler = (function(fieldId, inetFieldId, useUID, notesId, both) {
      var id = fieldId;
      var contId = fieldId + '_bpcont';
      var roId = fieldId + '_readonly';
      var inetId = inetFieldId;
      var useUid = useUID;

      return {
        faces : function(person) {
          personFullId = person.uid;
          localStorage.setItem("pID", personFullId);
          person.uid = person.uid && person.uid.length > 3 ? person.uid.substring(0, person.uid.length - 3) : person.uid;
          var value = person.name + ':' + (useUid ? person.uid : person.email);
          dojo.byId(contId).value = value;
          dojo.byId(id).value = person.name;
          if (dojo.byId(inetId)) {
            dojo.byId(inetId).value = (useUid ? person.uid : person.email);

          }
          if (dojo.byId(roId)) {
            dojo.byId(roId).innerHTML = (useUid ? person.uid : person.email);

          }
          if (notesId && dojo.byId(notesId)) {
            dojo.byId(notesId).value = person['notes-id'];
          }
          console.log('both ' + both + ' elem: ' + dojo.byId(fieldId + '_uid'));
          console.log(person);
          if (both && dojo.byId(fieldId + '_uid')) {
            dojo.byId(fieldId + '_uid').innerHTML = ' (' + person.uid + ')';
          }
        }
      };
    })(fieldId, inetFieldId, useUID, notesId, both);
    FacesTypeAhead.init(dojo.byId(fieldId), {
      key : "requestcmr;clacombe@us.ibm.com",
      faces : {
        headerLabel : "BluePages Results",
        onclick : handler.faces,
        moreResultsLabel : '${count} matches.',
        moreResultsLabel2 : 'More than ${count} matches.',
        moreResultsUrl : '#'
      },
      resultsAlign : "left"
    });
  },
  selectTab : function(tab, executeFunc) {
    // Defect 1240961 :FVT - Create Request - Non-leasing type should be
    // restrict add Install At (US) address type.
    // UPDATE (NSE) : Check first if request is already save before accessing
    // other fields.
    // Check if CmrGrid.GRIDS.ADDRESS_GRID_GRID exists.
    // Other background function doesn't run properly because of TypeErrors
    // during fresh create for BR(631).
    if (FormManager.getActualValue('reqId') && Number(FormManager.getActualValue('reqId')) > 0) {
      // Story :1202244 by Mukesh Kumar
      if (FormManager.getActualValue('cmrIssuingCntry') == '631' && FormManager.getActualValue('reqType') == 'C' && FormManager.getActualValue('email1') != 'bcibmnfe@br.ibm.com') {
        var _custType = FormManager.getActualValue('custType');
        // var isDisabled = dojo.byId('custSubGrp').readOnly;
        var role = FormManager.getActualValue('userRole').toUpperCase();

        if (_custType != 'undefined' && _custType != '') {
          if (_custType == 'CC3CC') {
            FormManager.setValue('abbrevNm', "CC3 USE ONLY");
            if (role == 'REQUESTER') {
              FormManager.readOnly('abbrevNm');
              FormManager.removeValidator('abbrevNm', Validators.REQUIRED);
            } else if (role == "PROCESSOR") {
              FormManager.enable('abbrevNm');
              FormManager.addValidator('abbrevNm', Validators.REQUIRED, [ 'Abbreviated Name (TELX1)' ], 'MAIN_CUST_TAB');
            }
          }
          if (_custType == 'SOFTL') {
            FormManager.setValue('abbrevNm', "SOFTLAYER USE ONLY");
            FormManager.readOnly('abbrevNm');
          }

          /*
           * DTN - #1318356 - Abbreviated Name is not saved when it is edited
           * for the first time Commenting as we are now handling the setting of
           * the abbrevNm on la_validations.js on an onChange event.
           */
          // if (_custType != 'CC3CC' && _custType != 'SOFTL' && !isDisabled)
          // {
          // var custNm1 = FormManager.getActualValue('mainCustNm1');
          // if (custNm1 != '') {
          // FormManager.setValue('abbrevNm', custNm1.length <= 30 ? custNm1 :
          // custNm1.substr(0, 30).trim());
          // FormManager.enable('abbrevNm');
          // }
          // }
        }
      }
    }

    var className = tab.getAttribute('class');
    if (className && className.indexOf('active') < 0) {
      dojo.query('.cmr-search-tab-crit').removeClass('active');
      dojo.query(tab).addClass('active');
      eval(executeFunc);
    }
  },
  showElement : function(id) {
    if (dojo.byId(id)) {
      dojo.byId(id).style.display = '';
    }
  },
  hideElement : function(id) {
    if (dojo.byId(id)) {
      dojo.byId(id).style.display = 'none';
    }
  },
  showAlert : function(message, title, executeFunc, info, buttonLabel) {
    if (!message) {
      return;
    }
    ibmweb.overlay.show("messagesOverlay", this);
    if (dojo.byId('dialog_messagesOverlay')) {
      dojo.byId('dialog_messagesOverlay').setAttribute('role', 'alert');
    }
    var header = dojo.byId("messagesOverlayContent");
    if (header) {
      header.innerHTML = message;
    }
    var mheader = dojo.byId("messagesOverlayTitle");
    if (mheader && title) {
      mheader.innerHTML = title;
    }

    if (info) {
      cmr.hideNode('alertTitleContainer');
    } else {
      cmr.showNode('alertTitleContainer');
    }
    var btn = dojo.byId("messagesOverlayButtonOK");
    if (btn) {
      if (buttonLabel && buttonLabel.OK) {
        btn.setAttribute('value', buttonLabel.OK);
      } else {
        btn.setAttribute('value', 'OK');
      }
    }
    ibmweb.queue.push(function() {
      return dojo.query("div#dialog_messagesOverlay .dijitDialogCloseIcon").length == 1;
    }, function() {
      dojo.query("div#dialog_messagesOverlay .dijitDialogCloseIcon").style("visibility", "hidden");
    });
    cmr.hideFunc = executeFunc;
    alertShown = true;
  },
  hideAlert : function() {
    if (!alertShown) {
      return;
    }
    var me = this;

    try {
      ibmweb.overlay.hide("messagesOverlay");
    } catch (e) {
      ibmweb.queue.push(function() {
        return dojo.query("div#dialog_messagesOverlay").length == 1;
      }, function() {
        ibmweb.overlay.hide("messagesOverlay", me);
      });
    }
    if (cmr.hideFunc) {
      eval(cmr.hideFunc);
      cmr.hideFunc = null;
    }
    alertShown = false;
  },
  showConfirm : function(executeFunc, message, title, closeFunc, buttonLabels) {
    if (!message) {
      return;
    }
    ibmweb.overlay.show("dialogOverlay", this);
    var btn = dojo.byId("dialogOverlayButtonOK");
    if (btn) {
      if (buttonLabels && buttonLabels.OK) {
        btn.setAttribute('value', buttonLabels.OK);
      } else {
        btn.setAttribute('value', 'OK');
      }
      btn.setAttribute('onclick', 'cmr.executeConfirm(\'' + executeFunc + '\')');
    }
    var cancelbtn = dojo.byId("dialogOverlayButtonCancel");
    if (cancelbtn) {
      if (buttonLabels && buttonLabels.CANCEL) {
        cancelbtn.setAttribute('value', buttonLabels.CANCEL);
      } else {
        cancelbtn.setAttribute('value', 'Cancel');
      }
    }
    if (cancelbtn && closeFunc) {
      cancelbtn.setAttribute('onclick', 'cmr.hideConfirm(\'' + closeFunc + '\')');
    } else if (cancelbtn) {
      cancelbtn.setAttribute('onclick', 'cmr.hideConfirm()');
    }

    var header = dojo.byId("dialogOverlayContent");
    if (header) {
      header.innerHTML = message;
    }
    var mheader = dojo.byId("dialogOverlayTitle");
    if (mheader && title) {
      mheader.innerHTML = title;
    }
    if (dojo.byId('dialog_dialogOverlay')) {
      dojo.byId('dialog_dialogOverlay').setAttribute('role', 'alert');
    }
    ibmweb.queue.push(function() {
      return dojo.query("div#dialog_dialogOverlay .dijitDialogCloseIcon").length == 1;
    }, function() {
      dojo.query("div#dialog_dialogOverlay .dijitDialogCloseIcon").style("visibility", "hidden");
    });
    confirmShown = true;
  },
  executeConfirm : function(executeFunc) {
    cmr.hideConfirm();
    window.setTimeout(executeFunc, 500);
  },
  hideConfirm : function(closeFunc) {
    if (!confirmShown) {
      return;
    }
    var me = this;

    try {
      ibmweb.overlay.hide("dialogOverlay");
    } catch (e) {
      ibmweb.queue.push(function() {
        return dojo.query("div#dialog_dialogOverlay").length == 1;
      }, function() {
        ibmweb.overlay.hide("dialogOverlay", me);
      });
    }
    if (closeFunc) {
      window.setTimeout(closeFunc, 500);
    }
    confirmShown = false;
  },
  showModal : function(id) {
    ibmweb.overlay.show(id, this);
    // check if an onload function is defined for the modal
    try {
      var o = eval(id + '_onLoad');
      if (o && typeof (o) == 'function') {
        o();
      }
    } catch (e) {

    }
    if (dojo.byId('dialog_' + id)) {
      dojo.byId('dialog_' + id).setAttribute('role', 'alert');
    }
    ibmweb.queue.push(function() {
      return dojo.query("div#dialog_" + id + " .dijitDialogCloseIcon").length == 1;
    }, function() {
      dojo.query("div#dialog_" + id + " .dijitDialogCloseIcon").style("visibility", "hidden");
    });
  },
  hideModal : function(id) {
    ibmweb.overlay.hide(id, this);
    try {
      var o = eval(id + '_onClose');
      if (o && typeof (o) == 'function') {
        o();
      }
    } catch (e) {

    }
  },
  setModalTitle : function(id, title) {
    var titleElem = dojo.byId(id + 'Title');
    if (titleElem) {
      titleElem.innerHTML = title;
    }
  },
  extractUrl : function(removeParams) {
    var loc = window.location.toString();
    var index = loc.indexOf(cmr.CONTEXT_ROOT);
    if (index > 0) {
      loc = loc.substring(index + cmr.CONTEXT_ROOT.length);
    }
    if (removeParams && loc.indexOf('?') > 0) {
      loc = loc.substring(0, loc.indexOf('?'));
    }
    return loc;
  },
  toTop : function() {
    window.scrollTo(0, 0);
  },
  limitTextArea : function(txtAreaId, maxLength) {
    var txtArea = dojo.byId(txtAreaId);
    if (!txtArea || txtArea.tagName != 'textarea') {
      var memos = dojo.query('textarea[id="' + txtAreaId + '"]');
      if (memos && memos.length > 0) {
        txtArea = memos[0];
      }
      if (!txtArea) {
        return;
      }
    }
    var chars = txtArea.value.length;
    document.getElementById(txtAreaId + '_charind').innerHTML = maxLength - chars;
    if (chars > maxLength) {
      txtArea.value = txtArea.value.substring(0, maxLength);
    }
  },
  searchCode : function(id, typeaheadId, title, textId) {
    $("#typeaheadField").autocomplete({
      source : cmr.CONTEXT_ROOT + '/typeahead/' + typeaheadId,
      select : function(event, ui) {
        cmr.hideModal('typeaheadModal');
        $("#typeaheadField").autocomplete('destroy');
        dojo.byId('typeaheadField').value = '';
        if (ui.item) {
          dojo.byId(id).value = ui.item.value;
          if (textId) {
            dojo.byId(textId).innerHTML = ui.item.label;
          }
          return false;
        }
      }
    });
    dojo.byId('typeaheadModalTitle').innerHTML = 'Search ' + title;
    cmr.showModal('typeaheadModal');
  },
  closeSearchCode : function() {
    cmr.hideModal('typeaheadModal');
    dojo.byId('typeaheadField').value = '';
  },
  clearSearchValue : function(id, textId) {
    if (dojo.byId(id)) {
      dojo.byId(id).value = '';
    }
    if (dojo.byId(textId)) {
      dojo.byId(textId).innerHTML = '';
    }
  },
  query : function(id, params) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/query/' + id + '.json',
      handleAs : 'json',
      method : 'GET',
      content : params,
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
  },
  extquery : function(id, params, returnFields) {
    var result = {};
    var qParams = params;
    qParams.returnFields = returnFields;
    console.log(qParams);
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/extquery/' + id + '.json',
      handleAs : 'json',
      method : 'GET',
      content : qParams,
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
  },
  exists : function(queryId, params) {
    var results = cmr.query(queryId, params);
    return results != null && results.ret1 != null && results.ret1 != '';
  },
  getRecord : function(id, model, params) {
    var result = {};
    var qParams = params;
    qParams.model = 'com.ibm.cio.cmr.request.entity.' + model;
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/record/' + id,
      handleAs : 'json',
      method : 'GET',
      content : qParams,
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data) {
          result = data;
        }
      },
      error : function(error, ioargs) {
        result = {};
      }
    });
    return result;
  },
  getAllRecords : function(id, model, params) {
    var result = {};
    var qParams = params;
    qParams.model = 'com.ibm.cio.cmr.request.entity.' + model;
    qParams['_qall'] = 'Y';
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/record/' + id,
      handleAs : 'json',
      method : 'GET',
      content : qParams,
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data) {
          result = data;
        }
      },
      error : function(error, ioargs) {
        result = {};
      }
    });
    return result;
  },
  checkAddrStd : function(formName, returnFunc) {
    var results = null;
    cmr.showProgress('Connecting to the service. Please wait..');
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/tgme.json',
      handleAs : 'json',
      content : dojo.formToObject(formName),
      method : 'GET',
      timeout : 50000,
      load : function(data, ioargs) {
        cmr.hideProgress();
        results = data;
        returnFunc(results);
      },
      error : function(error, ioargs) {
        cmr.hideProgress();
        results = {
          result : {
            stdResultCode : 'X'
          }
        };
        returnFunc(results);
      }
    });
    // return results;
  },
  userSessionCheck : function() {
    if (cmr.NOSESSIONCHECK) {
      return;
    }
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/sessioncheck.json',
      handleAs : 'json',
      method : 'GET',
      timeout : 50000,
      load : function(data, ioargs) {
        // check session every minute
        window.setTimeout('cmr.userSessionCheck()', 1000 * 60);
      },
      error : function(error, ioargs) {
        cmr.showAlert('Your session has expired due to inactivity. <br>Please click OK to go back to the login page.', 'Session Expired', 'dummyRefreshThePage()');
        // window.setTimeout('cmr.userSessionCheck()', 1000);
      }
    });
  },
  formatDate : function(time, type) {
    var result = '';
    var params = {
      'time' : time
    };
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/convert/' + type,
      handleAs : 'text',
      method : 'GET',
      content : params,
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data) {
          result = data;
        }
      },
      error : function(error, ioargs) {
        result = '';
      }
    });
    return result;
  },
  chooseNewEntry : function() {
    cmr.showModal('newRequestModal');
  },
  validateNewEntry : function() {
    var type = FormManager.getActualValue('newReqType');
    var cntry = FormManager.getActualValue('newReqCntry');
    if (cntry == '') {
      cmr.showAlert('Please choose the CMR Issuing Country.');
      return false;
    }
    if (type == '') {
      cmr.showAlert('Please choose the request type.');
      return false;
    }
    return true;
  },
  validateVAT : function(country, vat) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/vat.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        country : country,
        vat : vat
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
  },
  validateVATUsingVies : function(country, vat) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/vat/vies.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        country : country,
        vat : vat
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
  },
  validateGST : function(country, vat, name, address, postal, city) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/in/gst.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        country : country,
        vat : vat,
        name : name,
        address : address,
        postal : postal,
        city : city
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
  },
  validateABN : function(businessNumber, reqId, formerAbn) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/au/abn.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        abn : businessNumber,
        reqId : reqId,
        formerAbn : formerAbn
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
  },
  validateCustNmFromVat: function(businessNumber, reqId, formerCustNm, custNm) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/au/custNm.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        abn : businessNumber,
        reqId : reqId,
        formerCustNm : formerCustNm,
        custNm : custNm
      },
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data) {
          result = data;
        }
      },
      error : function(error, ioargs) {
        result = {};
      }
    });
    return result;
  },
  validateNZBNFromAPI: function(businessNumber, reqId, custNm) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/nz/nzbnFromAPI.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        businessNumber : businessNumber,
        reqId : reqId,
        custNm : custNm
      },
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        result = data;
      },
      error : function(error, ioargs) {
        result = {};
      }
    });
    return result;
  },
  validateCustNmFromAPI: function(reqId, formerCustNm, custNm) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/au/custNmFromAPI.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        reqId : reqId,
        formerCustNm : formerCustNm,
        custNm : custNm
      },
      timeout : 50000,
      sync : true,
      load : function(data, ioargs) {
        if (data) {
          result = data;
        }
      },
      error : function(error, ioargs) {
        result = {};
      }
    });
    return result;
  },
  validateZIP : function(country, zip, loc) {
    var result = {};
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/zip.json',
      handleAs : 'json',
      method : 'GET',
      content : {
        country : country,
        postalCode : zip,
        sysLoc : loc
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
  },
  openTranslateWindow : function(fromLang, toLang, arrayOfFields) {
    var url = typeof (_translateUrl) == 'undefined' ? null : _translateUrl;
    if (!url) {
      cmr.showAlert('URL for translation not defined.');
      return;
    }
    url = url.replace(/FROM/, fromLang);
    url = url.replace(/TO/, toLang);
    var params = '';
    var val = null;
    for (var i = 0; i < arrayOfFields.length; i++) {
      val = FormManager.getActualValue(arrayOfFields[i]);
      if (val != '') {
        params += params == '' ? '' : '%0A';
        params += encodeURIComponent(val);
      }
    }
    console.log(params);
    url += '/' + params;

    var win = window.open(url, 'TRANSLATE', 'height=430,width=770,scrollbars=no,resizable=no');
    if (win) {
      win.focus();
    }
  },
  handleOpenById : function(e) {
    var charCode = null;
    if (window.event) {
      e = window.event;
      charCode = e.keyCode;
    } else if (e && e.which) {
      charCode = e.which;
    }
    if (e.ctrlKey && charCode === 81) {
      openRequestById();
    }
  },
  getUID : function(id, spanId) {
    window.setTimeout(function() {
      var cmrIssuingCntry = FormManager.getActualValue('cmrIssuingCntry');
      var reqType = FormManager.getActualValue('reqType');
      if (cmrIssuingCntry == '760' && (reqType != 'C' && reqType != 'U')) {
        dojo.byId(spanId).innerHTML = "(" + id + ")";
      } else {
        // do nothing
      }
    }, 1000);
  },
  superUserMode : function(){
    if (this.superUser){
      cmr.showAlert('Already in Super User Mode');
      return;
    }
    var msg = 'Super User Mode unlocks all fields and enables a processor to specify values manually on the request form.';
    msg += '<strong>This function should only be used with caution and the immediate next action should be <span style="color:red">';
    msg += 'Create/Upd CMR</span></strong>. Executing other actions (apart from Check Request) will cause the page to reload and the normal locking, computed values, and dependencies to be executed.<br>Proceed?';
    cmr.showConfirm('cmr.executeSuperUserMode()', msg, 'Warning', null);
  },
  executeSuperUserMode() {
    if (typeof(_pagemodel) != 'undefined'){
      for (var nm in _pagemodel){
        if (nm != 'cmrIssuingCntry' && nm != 'reqType'){
          FormManager.enable(nm);
        }
      }
    }
    cmr.showNode('superUserModeText');
    this.superUser = true;
  },
  isSuperUserMode : function(){
   return this.superUser; 
  },
  reprocessRdc : function(){
    var msg = 'This will directly reprocess the request in RDC, do you want to proceed?';
    cmr.showConfirm('cmr.executeReprocessRdc()', msg, 'Warning', null);
  },
  executeReprocessRdc() {
    FormManager.doAction('frmCMR', 'RER', true);
  }
};

function createNewEntry() {
  if (cmr.validateNewEntry()) {
    var type = FormManager.getActualValue('newReqType');
    var cntry = FormManager.getActualValue('newReqCntry');
    if (type == 'C' || type == 'U' || type == 'X') {
      goToUrl(cmr.CONTEXT_ROOT + '/request?create=Y&newReqType=' + type + '&newReqCntry=' + cntry);
    } else {
      goToUrl(cmr.CONTEXT_ROOT + '/massrequest?create=Y&newReqType=' + type + '&newReqCntry=' + cntry);
    }
  }
}

function openRequestById() {
  if (dojo.byId('openRequestByIdModal')) {
    cmr.showModal('openRequestByIdModal');
  }
}

function doOpenRequestById() {
  var reqId = FormManager.getActualValue('requestById');
  if (reqId == '') {
    cmr.showAlert('Please enter the Request ID.');
    return;
  }
  var ret = cmr.query('CHECKREQUESTBYID', {
    REQID : reqId
  });
  if (ret && ret.ret1 == 'Y') {
    cmr.showProgress('Opening the request, please wait...');
    window.setTimeout('_openRequest(' + reqId + ')', 200);
  } else {
    cmr.showAlert('Request ' + reqId + ' does not exist.');
    return;
  }
}

function _openRequest(reqId) {
  window.location = cmr.CONTEXT_ROOT + '/request/' + reqId;
}
function dummyRefreshThePage() {
  window.location = '' + window.location.href;// cmr.CONTEXT_ROOT + '/timeout';
}
/**
 * Object.freeze - From FF 4, IE 9, CH 7 Freezes an object: that is, prevents
 * new properties from being added to it; prevents existing properties from
 * being removed; and prevents existing properties, or their enumerability,
 * configurability, or writability, from being changed. In essence the object is
 * made effectively immutable. The method returns the object being frozen.
 */
if (typeof Object.freeze === 'function') {
  Object.freeze(cmr.PageTypeEnum);
}

/**
 * Global export function.
 * 
 * @param formId
 */
function exportData(formId) {
  var form = dojo.byId(formId);
  var oldTarget = form.target;
  var oldAction = form.action;
  var oldMethod = form.method;
  form.action = 'exportData';
  form.target = 'exportFrame';
  form.method = 'POST';
  var token = new Date().getTime();
  dojo.byId('downloadToken').value = token;
  cmr.showProgress('Please wait while data is being exported...');
  form.submit();
  window.setTimeout('checkDownloadToken("' + token + '")', 3000);
  dojo.byId('downloadToken').value = '';
  form.action = oldAction;
  form.target = oldTarget;
  form.method = oldMethod;
}

/**
 * Checks the download token cookie. If cookie is set to 'Y', the export has
 * completed
 * 
 * @param token
 */
function checkDownloadToken(token) {
  dojo.xhrPost({
    url : 'checkToken.json',
    handleAs : 'json',
    content : {
      downloadToken : token
    },
    method : 'POST',
    timeout : 50000,
    load : function(data, ioargs) {
      if ('Y' == data.tokenStatus) {
        cmr.hideProgress();
      } else if ('X' == data.tokenStatus) {
        cmr.hideProgress();
        messageManager.displayIBMOverlay({
          title : "System Error",
          messageObject : [ {
            "key" : "EMPTY_CUST_NAME",
            "valueObject" : {
              "messageValue" : "An error has occurred while exporting the data. Please refresh your page once. If problem persists, please contact your System Administrator.",
              messageKey : "EMPTY_CUST_NAME",
              "messageTitle" : "Error",
              "messageType" : "error"
            }
          } ],
          messageKey : "EMPTY_CUST_NAME",
          type : "error"
        });
      } else {
        window.setTimeout('checkDownloadToken("' + token + '")', 1000);
      }
    },
    error : function(error, ioargs) {
      cmr.hideProgress();
      messageManager.displayIBMOverlay({
        title : "System Error",
        messageObject : [ {
          "key" : "EMPTY_CUST_NAME",
          "valueObject" : {
            "messageValue" : "An error has occurred while exporting the data. Please refresh your page once. If problem persists, please contact your System Administrator.",
            messageKey : "EMPTY_CUST_NAME",
            "messageTitle" : "Error",
            "messageType" : "error"
          }
        } ],
        messageKey : "EMPTY_CUST_NAME",
        type : "error"
      });
    }
  });
}

/**
 * Checks the login status of the user. Executes the passed executeFunc after
 * interval milliseconds if login is ok
 * 
 * @param executeFunc
 * @param noLoginFunc
 * @param interval
 */
function checkLoginStatus(executeFunc, noLoginFunc, interval) {
  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/checkLoginStatus.json',
    handleAs : 'json',
    content : null,
    method : 'POST',
    timeout : 50000,
    load : function(data, ioargs) {
      if ('N' == data.loginStatus) {
        noLoginFunc();
      } else {
        window.setTimeout(executeFunc, interval);
      }
    },
    error : function(error, ioargs) {
      noLoginFunc();
    }
  });
}

/**
 * OnChange function of blue pages
 * 
 * @param elem
 */
function bpOnChange(elem) {
  var id = elem.id;
  if (id) {
    var val = FormManager.getActualValue(id);
    if (val == '') {
      var roId = id + '_readonly';
      if (dojo.byId(roId)) {
        dojo.byId(roId).innerHTML = '(none selected)';
      }
      var bpId = elem.getAttribute('bpId');
      if (bpId && dojo.byId(bpId)) {
        dojo.byId(bpId).value = '';
      }
      if (dojo.byId(id + '_uid')) {
        dojo.byId(id + '_uid').innerHTML = '';
      }
    }
  }
}

/**
 * Global function to use for changing URLs
 * 
 * @param url
 */
function goToUrl(url) {
  if (FormManager && FormManager.shouldCheckBeforeLeave()) {
    FormManager.executeCheckFunction(url);
  } else {
    window.location = url;
  }
}

function checkToken(token) {
  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/token.json',
    handleAs : 'json',
    content : {
      tokenId : token
    },
    method : 'POST',
    timeout : 50000,
    load : function(data, ioargs) {
      if (!data) {
        window.setTimeout('checkToken("' + token + '")', 1000);
      } else {
        if (data.ok) {
          cmr.hideProgress();
          if (data.success) {
            MessageMgr.showInfoMessage(data.message);
            if (cmr.aftertoken) {
              cmr.aftertoken();
              cmr.aftertoken = null;
            }
          } else {
            MessageMgr.showErrorMessage(data.message, cmr.modalmode);
            cmr.modalmode = null;
          }
        } else {
          window.setTimeout('checkToken("' + token + '")', 1000);
        }
      }
    },
    error : function(error, ioargs) {
      cmr.hideProgress();
      MessageMgr.showErrorMessage('An error has occured while attaching the file. Please try again. If error persists, please contact your system administrator.');
    }
  });
};
dojo.ready(function() {
  window.setTimeout('cmr.userSessionCheck()', 20000);
});

var _LOGWINDOW = null;
function showAppLog() {
  if (_LOGWINDOW && !_LOGWINDOW.closed) {
    _LOGWINDOW.focus();
  } else {

    var dualScreenLeft = window.screenLeft != undefined ? window.screenLeft : screen.left;
    var dualScreenTop = window.screenTop != undefined ? window.screenTop : screen.top;

    var width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
    var height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

    var left = ((width / 2) - (353)) + dualScreenLeft;
    var top = ((height / 2) - (115)) + dualScreenTop;

    var specs = 'location=no,menubar=no,resizable=no,scrollbars=yes,status=no,toolbar=no,height=330px,width=706px,left=' + left + ',top=' + top;
    var win = window.open('log', 'logwindow', specs, true);
    _LOGWINDOW = win;
    win.focus();

    dojo.byId('LOG').className = '';

  }
}

var BrowserCheck = (function() {
  var opera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;

  // Firefox 1.0+
  var firefox = typeof InstallTrigger !== 'undefined';

  // Safari 3.0+ "[object HTMLElementConstructor]"
  var isafari = /constructor/i.test(window.HTMLElement) || (function(p) {
    return p.toString() === "[object SafariRemoteNotification]";
  })(!window['safari'] || safari.pushNotification);

  // Internet Explorer 6-11
  var IE = /* @cc_on!@ */false || !!document.documentMode;

  // Edge 20+
  var edge = !IE && !!window.StyleMedia;

  // Chrome 1+
  var chrome = !!window.chrome;

  // Blink engine detection
  var blink = (chrome || opera) && !!window.CSS;

  return {
    isFirefox : function() {
      return firefox;
    },
    isChrome : function() {
      return chrome;
    },
    isIE : function() {
      return IE;
    },
    isEdge : function() {
      return edge;
    },
    isBlink : function() {
      return blink;
    },
    isSafari : function() {
      return isafari;
    },
    isOpera : function() {
      return opera;
    },
    getFirefoxVersion : function() {
      var agent = navigator.userAgent;
      if (agent.indexOf('Firefox') > 0) {
        var version = agent.substring(agent.indexOf('rv') + 3);
        version = version.substring(0, version.indexOf(')')).trim();
        return parseFloat(version);
      }
      return 0;
    }
  };
})();

function openCISupportal() {
  CISupportal.open();
}

function openQuickSearchDirect() {
  var subRegion = FormManager.getActualValue('countryUse');
  var ctry = subRegion && subRegion.length > 3 ? subRegion : FormManager.getActualValue('cmrIssuingCntry');
  window.location = cmr.CONTEXT_ROOT + '/quick_search?issuingCntry=' + ctry;
}

function openQuickSearch() {
  goToUrl(cmr.CONTEXT_ROOT + '/quick_search');
}

dojo.addOnLoad(function() {
  document.onkeydown = cmr.handleOpenById;

  if (typeof (cmUtils) == 'undefined' || !cmUtils) {
    cmrUtils = {
      error : function() {

      }
    };
  } else if (cmUtils && !cmUtils.error) {
    cmUtils.error = function() {

    };
  }

});