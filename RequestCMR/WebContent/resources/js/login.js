dojo.require("dijit.form.Form");
dojo.require("dojo.string");
dojo.ready(function() {
	if ((window.location.href).includes("c360-prod")) {
		alert("This urls will be sunset in 18th of June 2024. Please bookmark and start using the new url: https://createcmr.epm2-prod.not-for-users.ibm.com/CreateCMR/login. \n \n Click 'Ok' and you will be redirect automatically "); 
		window.location.href = "https://createcmr.epm2-prod.not-for-users.ibm.com/CreateCMR/login";
	};
});
var login = (function() {

  dojo.ready(function() {
    cmr.hideIBMLoader();
    cmr.NOSESSIONCHECK = true;
    if (FormManager) {
      FormManager.addValidator('username', Validators.REQUIRED, [ 'User Name' ]);
      FormManager.addValidator('username', Validators.EMAIL, [ 'User Name' ]);
      FormManager.addValidator('password', Validators.REQUIRED, [ 'Password' ]);
      FormManager.ready();
      cmr.showNode('cmr-login-btn');
      cmr.showNode('supportal-link');

      document.onkeypress = login.handleEnter;
    }
    dojo.cookie('lastTab', 'x');
  });

  return {
    validateAndSubmit : function() {
      var result = FormManager.validate('frmCMR');
      if (result) {
        cmr.showProgress('Logging in. Please wait..');
        document.forms['frmCMR'].submit();
      }
    },
    handleEnter : function(e) {
      var charCode = null;
      if (window.event) {
        e = window.event;
        charCode = e.keyCode;
      } else if (e && e.which) {
        charCode = e.which;
      }
      if (charCode === 13) {
        login.validateAndSubmit();
      }
    },
    openSupportal : function() {
      CISupportal.open({
        issueType : 'A',
        appIssue : 'A',
        userId : FormManager.getActualValue('username')
      });
    }
  };
})();
