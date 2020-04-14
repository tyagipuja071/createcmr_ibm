var app = angular.module('GDApp', []);
app.controller('GDController', [ '$scope', '$document', '$http', '$timeout', function($scope, $document, $http, $timeout) {
  $scope.deptList = [];
  $scope.oldItems = [];

  $scope.findDept = function(deptName) {
  var deptName = FormManager.getActualValue('deptName');
  console.log($scope.deptList.length);
  debugger;
  if(deptName == "" || deptName.length < 4){
  alert("Please specify atleast 4 characters in Department No.");
  return;

  }
  
  if(deptName.length > 6)
  {
  alert("Exceeds length (Max length = 6)");
  return;
  }
  
  if(deptName.length == 6){
    
   var result = cmr.query('COUNT_GERMANY_DEPT', {
   DEPT : deptName
  
});
  /* alert("Add Department");*/
if (result != null && result.ret1 == 0){
  alert("No record found")
  var res = confirm( "Do you want to add the Department " + deptName +" to the valid department list?" );

  if ( res ) {
    {
      cmr.showProgress('Adding');
      $http({
        url : cmr.CONTEXT_ROOT + '/code/dept/process.json',
        method : 'POST',
        params : {
          "deptName" : deptName,
          "Action" : "ADD_DEPT"

        }
      }).then(function(response) {
        cmr.hideProgress();
        if (response.data.result.success) {
          debugger;
      /*    $scope.deptList.push({
            "deptName" : deptName
            
          });*/
          
       /*   var url = cmr.CONTEXT_ROOT + '/code/germanyDept';
          window.location = url;*/
          console.log(response);
        } else {
          cmr.showAlert('An error occurred while adding.');
        }
      }, function(response) {
        cmr.hideProgress();
        console.log('error.');
      });
    }
  } else {
      // the user clicked cancel or closed the confirm dialog.
  }
}

  }
  
  $scope.deptList = [];
  $http.get(cmr.CONTEXT_ROOT + '/code/germanyDeptList.json?deptName='+deptName)
  .then(function(response) {
    console.log(response.data);
    if (response.data.items.length>0) {
      response.data.items.forEach(function(item) {
        debugger;
        $scope.deptList.push({
          "deptName" : item.deptName,
          "status" : "D"
        });
      });
      
      $scope.oldItems = angular.copy($scope.deptList);
    }
  });
  
  
  
  
  $scope.removeValue = function(index){
    if(confirm("Remove Selected Entry?"))/*{
      $http.get(cmr.CONTEXT_ROOT + '/code/removeDept?deptName='+$scope.deptList[index].deptName)
        $scope.deptList[index].status = "D";
      }*/
    {
      cmr.showProgress('Deleting, please wait...');
      $http({
        url : cmr.CONTEXT_ROOT + '/code/dept/process.json',
        method : 'POST',
        params : {
          "deptName" : $scope.deptList[index].deptName,
          "action" : "REMOVE_DEPT"

        }
      }).then(function(response) {
        cmr.hideProgress();
        if (response.data.result.success) {
          debugger;
          var url = cmr.CONTEXT_ROOT + '/code/germanyDept';
          window.location = url;
          console.log(response);
        } else {
          cmr.showAlert('An error occurred while deleting.');
        }
      }, function(response) {
        cmr.hideProgress();
        console.log('error.');
      });
    }
      
    }  
  
  }
  
  $scope.backToCodeMaintHome = function() {
    window.location = cmr.CONTEXT_ROOT + '/code';
    }
  
} ]);
