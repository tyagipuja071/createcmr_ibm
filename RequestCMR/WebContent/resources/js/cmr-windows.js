var WindowMgr = (function() {
  var _CMRWINDOWS = new Object();

  return {
    open : function(windowId, recordId, location, width, height, external) {
      var id = windowId + '-' + recordId;
      if (_CMRWINDOWS[id] && !_CMRWINDOWS[id].closed) {
        _CMRWINDOWS[id].focus();
        try {
          _CMRWINDOWS[id].doFocus();
        } catch (e) {
          // noop
        }
        return _CMRWINDOWS[id];
      } else {
        var dHeight = height ? height + 'px' : '560px';
        var dWidth = width ? width + 'px' : '1000px';
        var fullLoc = external ? location : cmr.CONTEXT_ROOT + '/window/' + location;
        var specs = 'location=no,menubar=no,resizable=no,scrollbars=yes,status=no,toolbar=no,height=' + dHeight + ',width=' + dWidth;
        console.log(fullLoc + ' = ' + id);
        var win = window.open(fullLoc, id, specs, true);
        if (!win) {
          cmr.showAlert("The window cannot be opened at this time. Please contact your system administrator");
          return null;
        } else {
          _CMRWINDOWS[id] = win;
          win.focus();
          return win;
        }
      }
    },
    checkLoginAndOpener : function() {
      if (!window.opener || window.opener.closed) {
        window.close();
      } else {
        checkLoginStatus('WindowMgr.checkLoginAndOpener()', window.close, 1000);
      }
    },
    trackMe : function() {
      console.log('Started tracking of window ' + window.name);
      window.setTimeout('WindowMgr.checkLoginAndOpener()', 1000);
    },
    closeMe : function() {
      window.close();
    },
    openFromMain : function(url) {
      if (window.opener) {
        window.opener.location.href = cmr.CONTEXT_ROOT + url;
      }
    }
  };
})();