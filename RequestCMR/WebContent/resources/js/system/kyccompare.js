var app = angular.module('KYCApp', [ 'ngSanitize' ]);
app.controller('KYCController', [ '$scope', '$document', '$http', '$timeout', '$sanitize', '$filter', function($scope, $document, $http, $timeout, $sanitize, $filter) {

  $scope.names = [];

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
         
         dojo.xhrGet({
           url : cmr.CONTEXT_ROOT + '/kycevs.json?name='+name,
           handleAs : 'json',
           timeout : 1000 * 30,
           sync : false,
           load : function(data, ioargs) {
             if (data && data.success){
               dojo.byId('evs_'+i).innerHTML = data.evs ? '<span style="color:green;font-weight:bold">Passed</span>' : '<span style="color:red;font-weight:bold">Failed</span>';
               dojo.byId('kyc_'+i).innerHTML = data.kyc ? '<span style="color:green;font-weight:bold">Passed</span>' : '<span style="color:red;font-weight:bold">Failed</span>';
             } else {
               dojo.byId('evs_'+i).innerHTML = 'Error';
               dojo.byId('kyc_'+i).innerHTML = 'Error';
             }
           },
           error : function(error, ioargs) {
           }
         });
         
       }
     });
  };
}]);

