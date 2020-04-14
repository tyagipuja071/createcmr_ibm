var app = angular.module('RTApp', [ 'ngRoute' ]);

app.controller('RTController', [
    '$scope',
    '$document',
    '$http',
    '$timeout',
        function($scope, $document, $http, $timeout) {
          $scope.current = {};
          $scope.oldItems = [];
          $scope.rtItems = [];
          $scope.issuingCntry = country;
          $scope.existing = false;
          $http.get(cmr.CONTEXT_ROOT + '/code/repTeamList.json?issuingCntry='+country)
          .then(function(response) {
            console.log(response.data);
            if (response.data.items.length>0) {
              response.data.items.forEach(function(item) {
                $scope.rtItems.push({
                  "repTeamCd" : item.repTeamCd,
                  "repTeamMemberNo" : item.repTeamMemberNo,
                  "repTeamMemberName" : item.repTeamMemberName,
                  "edit" : false,
                  "status" : "E"
                });
              });
              $scope.oldItems = angular.copy($scope.rtItems);
            }
          });

          $scope.backToList = function() {
            window.location = cmr.CONTEXT_ROOT + '/code/repTeam';

          };

          $scope.getCountryDesc = function(country){
            if(country!=''){
              let desc='';
              $scope.countries.forEach(item=>{
                var val =item.ret1.trim();
                if(val==country){
                  desc= item.ret2;
                }
              });
              return desc;
            } else {
              return "No Description Found";
            }
          };

          $scope.saveAll = function(){
            if ($scope.issuingCntry == '' || $scope.issuingCntry==null) {
              cmr.showAlert('Can not save values. Issuing Country not found');
              return;
            }
            let edit = false;
            let update = false;
            const pk = [];
            
            for(let i=0;i<$scope.rtItems.length;i++){
              const curr = $scope.rtItems[i];
              if(curr.repTeamCd=='' && curr.repTeamMemberNo=='' && curr.repTeamMemberName==''){
                $scope.rtItems.splice(i,1);
                $scope.oldItems.splice(i,1);
                i--;
                continue;
              }
              

              if (curr.repTeamMemberName !=null && curr.repTeamMemberName!='' && curr.repTeamMemberName.match(/^[0-9a-zA-Z ]+$/)){
                if((curr.repTeamCd == null || curr.repTeamCd == '') && (curr.repTeamMemberNo == null || curr.repTeamMemberNo == '')){
                  cmr.showAlert('Please provide value to Rep Team Code and Rep Team Member No ,before saving.');
                  return;
                }
              }
              
              if(curr.repTeamCd == null || curr.repTeamCd == ''){
                cmr.showAlert('Please provide a value to Rep Team Code before saving.');
                return;
              }
              if(curr.repTeamMemberNo == null || curr.repTeamMemberNo == ''){
                cmr.showAlert('Please provide a value Rep Team Member No before saving.');
                return;
              }

              
              if (curr.repTeamCd !=null && curr.repTeamCd!='' && !curr.repTeamCd.match(/^[0-9a-zA-Z ]+$/)){
                cmr.showAlert('Rep Team Code can only accept alphanumeric characters.');
                return;
              }

              if (curr.repTeamMemberNo !=null && curr.repTeamMemberNo!='' && !curr.repTeamMemberNo.match(/^[0-9a-zA-Z ]+$/)){
                cmr.showAlert('Rep Team Member No Code only accept alphanumeric characters.');
                return;
              }

              if (curr.repTeamMemberName !=null && curr.repTeamMemberName!='' && !curr.repTeamMemberName.match(/^[0-9a-zA-Z ]+$/)){
                cmr.showAlert('Rep Team Member Name can only accept alphanumeric character.');
                return;
              }
              
          
              let found = false;
              pk.forEach((item,id)=>{
                if(item.repTeamCd == curr.repTeamCd && item.repTeamMemberNo==curr.repTeamMemberNo){
                  found = true;
                }
              });
              if(found){
                cmr.showAlert("Duplicate entry exist for same Rep Team Code: "+curr.repTeamCd+" and Rep Team Member No: "+curr.repTeamMemberNo+". Please specify a unique Rep Team Code and Rep Team Member No combination.");
                return;
              }else {
                pk.push({
                  repTeamCd:curr.repTeamCd,
                  repTeamMemberNo:curr.repTeamMemberNo
                });
              }    
              if(curr.status=='N' || curr.status == 'U' || curr.status == 'D'){
                update=true;
              }
            };

            if(!update){
              cmr.showAlert('No new or updated records found.');
              return
            }

            var temp = JSON.stringify($scope.rtItems.map((item)=>{
              if(item.status!='D'){
                return JSON.stringify({
                  repTeamCd : item.repTeamCd,
                  repTeamMemberNo : item.repTeamMemberNo,
                  repTeamMemberName : item.repTeamMemberName
                });
              }
            }));
            var toSend = JSON.parse(temp);
            if ($scope.rtItems.length == 1) {
              toSend.push({
                repTeamCd : "DUMMY",
                repTeamMemberNo : "DUMMY",
                repTeamMemberName : "DUMMY"
              });
            }
            
            cmr.showProgress('Saving, please wait...');
            $http({
              url : cmr.CONTEXT_ROOT + '/code/repTeamMaint/process.json',
              method : 'POST',
              params : {
                issuingCntry : $scope.issuingCntry,
                items : toSend,
                massAction : "MASS_SAVE"
              }
            }).then(function(response) {
              cmr.hideProgress();
              if (response.data.result.success) {
                var url = cmr.CONTEXT_ROOT + '/code/repTeamMaint?issuingCntry='+$scope.issuingCntry+'&infoMessage=Records saved successfully.';
                window.location = url;
              } else {
                cmr.showAlert('An error occurred while saving the values.');
              }
            }, function(response) {
              cmr.hideProgress();
              console.log('error.');
            });

          };

          $scope.deleteRT = function(){
            var result = cmr.query('CHECK_REP_TEAM_RECORDS', {
              CNTRY : $scope.issuingCntry
            });

            if (result == null || result.ret1!='1' ) {
              cmr.showAlert("No mappings found for Issuing Country - "+$scope.issuingCntry+". Cannot perform delete operation.");
              return;
            }
            
            if (confirm('Deleting Rep Team Mapping. This action is irreversible.\nContinue with delete?')) {
              cmr.showProgress('Deleting, please wait...');
              $http({
                url : cmr.CONTEXT_ROOT + '/code/repTeamMaint/process.json',
                method : 'POST',
                params : {
                  "issuingCntry" : $scope.issuingCntry,
                  "massAction" : "DELETE"

                }
              }).then(function(response) {
                cmr.hideProgress();
                if (response.data.result.success) {
                  var url = cmr.CONTEXT_ROOT + '/code/repTeam?infoMessage=Rep Team Records for issuing country - '+country+' deleted successfully.';
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
          };

          $scope.removeValue = function(index){
            if(confirm("Remove Selected Entry?")){
              if($scope.rtItems[index].edit){
                const curr = $scope.rtItems[index];
                if(curr.status=="N"){
                  $scope.rtItems.splice(index,1);
                  $scope.oldItems.splice(index,1);
                } else {
                  if($scope.oldItems[index]){
                    $scope.rtItems[index] = angular.copy($scope.oldItems[index]);
                  }
                  $scope.rtItems[index].status="D";
                }
              } 
              else {
                $scope.rtItems[index].status = "D";
              }
            } 
          }

    /*      $scope.unEdit = function(index){
            if(confirm("Undo changes to current record?")){
              $scope.rtItems[index] = angular.copy($scope.oldItems[index]);
            }
          }*/

          $scope.unRemoveValue = function(index){
            if($scope.rtItems[index].status=="D"){
              $scope.rtItems[index].status=angular.copy($scope.oldItems[index].status);
            }
          };

          $scope.addNew = function(index){
            $scope.rtItems.splice(index,0,{
              "repTeamCd" : "",
              "repTeamMemberNo" : "",
              "repTeamMemberName" : "",
              "edit" : true,
              "status" : "N"
            });
            $scope.oldItems[index] = angular.copy($scope.rtItems[index]);
            console.log($scope.oldItems,$scope.rtItems);
          };

          $scope.editValue = function(index){
            $scope.rtItems[index].edit = true;
            $scope.rtItems[index].status = 'U';
          };


        } ]);