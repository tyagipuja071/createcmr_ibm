/**
 * CI Supportal Script Supports the integration with the CI Supportal UI
 * 
 */
var CISupportal = (function() {

  var _siteURL = '';
  var appId = '';
  var userId = '';
  return {
    init : function(supportalUrl, applicationId, user) {
      _siteURL = supportalUrl;
      appId = applicationId;
      userId = user;
      console.log('CI Supportal Init: ' + appId + ' - ' + _siteURL);
    },
    open : function(params, direct) {
      if (!_siteURL) {
        alert('Cannot open CI Supportal. init() not yet called.');
        return;
      }
      var url = _siteURL + '?appId=' + appId;
      var param = null;
      var urlParams = '';
      for ( var elem in params) {
        if (params.hasOwnProperty(elem)) {
          if (elem == 'userId' && userId == '') {
            if (userId == '') {
              param = params[elem];
            } else {
              param == userId;
            }
          } else {
            param = params[elem];
          }
          if (param) {
            var type = typeof (param);
            if (type != 'object') {
              urlParams += '&' + elem + '=' + encodeURIComponent(param);
            } else if (type == 'object' && param.length) {
              for ( var i = 0; i < param.length; i++) {
                urlParams += '&' + elem + '=' + encodeURIComponent(param[i]);
              }
            }
          }
        }
      }
      if (!urlParams.includes('userId') && userId != '') {
        urlParams += '&userId=' + encodeURIComponent(userId);
      }
      if (direct) {
        window.location = url + urlParams;
      } else {

        var w = 670;
        var h = 550;
        var title = 'CI Supportal - ' + appId;
        var dualScreenLeft = window.screenLeft != undefined ? window.screenLeft : window.screenX;
        var dualScreenTop = window.screenTop != undefined ? window.screenTop : window.screenY;

        var width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
        var height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

        var left = ((width / 2) - (w / 2)) + dualScreenLeft;
        var top = ((height / 2) - (h / 2)) + dualScreenTop + 30;
        var newWindow = window.open(url + urlParams, title, 'scrollbars=yes, width=' + w + ', height=' + h + ', top=' + top + ', left=' + left);

        // Puts focus on the newWindow
        if (newWindow.focus) {
          newWindow.focus();
        }
      }

    },

  };
})();