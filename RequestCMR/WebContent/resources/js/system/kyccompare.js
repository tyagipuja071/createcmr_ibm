var app = angular.module('KYCApp', [ 'ngSanitize' ]);
app.controller('KYCController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', '$filter', function($scope, $document, $http, $timeout, $sanitize, $filter) {

  $scope.names = [];
  $scope.priv = 'N';
  $scope.sample = 'N';
  $scope.test = function(){
     if ($scope.checkType == 'N' && !$scope.inputName){
       alert('Please input the name to screen.');
       return;
     }
     if ($scope.checkType == 'F' && !sessionData){
       alert('Please input names on the file input.');
       return;
     }
     $scope.names = [];
     var lines = [];
     if ($scope.checkType == 'N'){
       lines.push($scope.inputName);
     }
     if ($scope.checkType == 'F'){
       lines = sessionData ? sessionData.split('\r\n') : [];
     }

     if (lines.length > 51){
       alert('Only a maximum of 50 names at a time can be checked.');
       return;
     }
     lines.forEach(function(name,i){
       if (name && name.trim() != ''){
         $scope.names.push({name : name, index : i});
       }
     });
     for (var i = 0; i < $scope.names.length; i++){
       $timeout($scope.dplCheck, 500 * i, false, $scope.names[i]);
     }
  };
  $scope.dplCheck = function(item){
    var name = item.name;
    var i = item.index;
    dojo.xhrGet({
      url : cmr.CONTEXT_ROOT + '/kycevs.json?name='+name+'&priv='+$scope.priv,
      handleAs : 'json',
      timeout : 1000 * 30,
      sync : false,
      load : function(data, ioargs) {
        if (data && data.success){
          if (!data.kyc_call){
            dojo.byId('kyc_'+i).innerHTML = 'Error';
          } else {
            dojo.byId('kyc_'+i).innerHTML = data.kyc ? '<span style="color:green;font-weight:bold">Passed</span>' : '<span style="color:red;font-weight:bold">Failed</span>';
            if ($scope.sample == 'Y'){
              var rec = data.kyc_raw.result.result[0];
              if (rec){
                dojo.byId('kyc_'+i).innerHTML = dojo.byId('kyc_'+i).innerHTML +'<br>'+rec.name;
              }
            }
          }
          if (!data.evs_call){
            dojo.byId('evs_'+i).innerHTML = 'Error';
          } else {
            dojo.byId('evs_'+i).innerHTML = data.evs ? '<span style="color:green;font-weight:bold">Passed</span>' : '<span style="color:red;font-weight:bold">Failed</span>';
          }
        } else {
          dojo.byId('evs_'+i).innerHTML = 'Error';
          dojo.byId('kyc_'+i).innerHTML = 'Error';
        }
      },
      error : function(error, ioargs) {
        dojo.byId('evs_'+i).innerHTML = 'Error';
        dojo.byId('kyc_'+i).innerHTML = 'Error';
      }
    });
  }
}]);

