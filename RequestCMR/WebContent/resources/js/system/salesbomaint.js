var app = angular.module('SBOApp', [ 'ngRoute' ]);

app.controller('SBOController', [
  '$scope',
  '$document',
  '$http',
  '$timeout',
  function($scope, $document, $http, $timeout) {
    $scope.current = {};
    $scope.oldItems = [];
    $scope.sboItems = [];
    $scope.issuingCntry = country;
    $scope.existing = false;
    $scope.isuCodes = cmr.query('SYSTEM.ISUCD', {
      _qall : 'Y',
      MANDT:cmr.MANDT
    });

    $scope.UI = {
        isuCd:'',
        clientTier:''
    };

    $scope.clientTierCodes = cmr.query('SYSTEM.CTC', {
      _qall : 'Y',
      MANDT:cmr.MANDT
    });

    $http.get(cmr.CONTEXT_ROOT + '/code/salesBoList.json?issuingCntry='+country)
    .then(function(response) {
      console.log(response.data);
      if (response.data.items.length>0) {
        response.data.items.forEach(function(item) {
          $scope.sboItems.push({
            "repTeamCd" : item.repTeamCd,
            "salesBoCd" : item.salesBoCd,
            "salesBoDesc" : item.salesBoDesc,
            "mrcCd" : item.mrcCd,
            "isuCd" : item.isuCd,
            "clientTier" : item.clientTier,
            "edit" : false,
            "status" : "E"
          });
        });
        $scope.oldItems = angular.copy($scope.sboItems);
      }
    });

    $scope.backToList = function() {
      window.location = cmr.CONTEXT_ROOT + '/code/salesBo';

    };

    $scope.addIsu = function() {
      let isuCd = $scope.UI.isuCd;
      const {isuCdList} = $scope.current;
      isuCd = isuCd.trim();
      if(isuCd!=''){
        if(!isuCdList.includes(isuCd)){
          isuCdList.push(isuCd);
        }else {
          alert('ISU Code value already exists!');
        }
      } else {
        alert('Please choose a value from dropdown before adding ISU.');
      }
    };

    $scope.removeIsu = function(isu){
      const {isuCdList} = $scope.current;
      if(isu!=null && isu !='' && isuCdList.includes(isu)){
        const index = isuCdList.indexOf(isu);
        if (index > -1) {
          isuCdList.splice(index, 1);
        }
      }
    };

    $scope.getIsuDesc = function(isu){
      if(isu!=''){
        if(isu.length>2){
          isu = isu.slice(0,2);
        }
        let desc='';
        $scope.isuCodes.forEach(item=>{
          var val = item.ret1.trim();
          if(val==isu){
            desc = item.ret2;
          }
        });
        return desc;
      } else {
        return "No Description Found";
      }
    };

    $scope.addClientTier = function() {
      let clientTier = $scope.UI.clientTier;
      const {clientTierList} = $scope.current;
      clientTier = clientTier.trim();
      if(clientTier!=''){
        if(!clientTierList.includes(clientTier)){
          clientTierList.push(clientTier);
        }else {
          alert('Client Tier value already exists!');
        }
      } else {
        alert('Please choose a value from dropdown before adding Client Tier.');
      }
    };

    $scope.removeClientTier = function(ctc){
      const {clientTierList} = $scope.current;
      if(ctc!=null && ctc !='' && clientTierList.includes(ctc)){
        const index = clientTierList.indexOf(ctc);
        if (index > -1) {
          clientTierList.splice(index, 1);
        }
      }
    };

    $scope.getClientTierDesc = function(ctc){
      if(ctc!=''){
        if(ctc.length>1){
          ctc = ctc.slice(0,1);
        }
        let desc='';
        $scope.clientTierCodes.forEach(item=>{
          var val =item.ret1.trim();
          if(val==ctc){
            desc= item.ret2;
          }
        });
        return desc;
      } else {
        return "No Description Found";
      }
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
        cmr.showAlert('Cannot save values. Issuing Country not found');
        return;
      }
      let edit = false;
      let update = false;
      const pk = [];
      
      for(let i=0;i<$scope.sboItems.length;i++){
        const curr = $scope.sboItems[i];
        if(curr.repTeamCd=='' && curr.salesBoCd=='' && curr.salesBoDesc=='' && curr.mrcCd=='' && curr.isuCd=='' && curr.clientTier==''){
          $scope.sboItems.splice(i,1);
          $scope.oldItems.splice(i,1);
          i--;
          continue;
        }
        
        if((curr.repTeamCd == null || curr.repTeamCd == '') && (curr.salesBoCd == null || curr.salesBoCd == '')){
          cmr.showAlert('Please provide a value to Rep Team Code or Sales BO Code or both, before saving.');
          return;
        }

        if (curr.repTeamCd !=null && curr.repTeamCd!='' && !curr.repTeamCd.match(/^[0-9a-zA-Z ]+$/)){
          cmr.showAlert('Rep Team Code can only accept alphanumeric characters. Found value: '+curr.repTeamCd);
          return;
        }

        if (curr.salesBoCd !=null && curr.salesBoCd!='' && !curr.salesBoCd.match(/^[0-9a-zA-Z ]+$/)){
          cmr.showAlert('Sales BO Code can only accept alphanumeric characters. Found value: '+curr.salesBoCd);
          return;
        }

        if (curr.mrcCd !=null && curr.mrcCd!='' && !curr.mrcCd.match(/^[0-9a-zA-Z ]+$/)){
          cmr.showAlert('MRC Code can only accept a single alphanumeric character. Found value: '+curr.mrcCd);
          return;
        }
        
        let found = false;
        pk.forEach((item,id)=>{
          if(item.repTeamCd == curr.repTeamCd && item.salesBoCd==curr.salesBoCd){
            found = true;
          }
        });
        
        if(found){
          cmr.showAlert("Duplicate entry exist for same Rep Team Code: "+curr.repTeamCd+" and Sales BO Code: "+curr.salesBoCd+". Please specify a unique Rep Team Code and Sales BO combination.");
          return;
        } else {
          pk.push({
            repTeamCd:curr.repTeamCd,
            salesBoCd:curr.salesBoCd
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

      var temp = JSON.stringify($scope.sboItems.map((item)=>{
        if(item.status!='D'){
          return JSON.stringify({
            repTeamCd : item.repTeamCd,
            salesBoCd : item.salesBoCd,
            salesBoDesc : item.salesBoDesc,
            mrcCd : item.mrcCd,
            isuCd : item.isuCd,
            clientTier : item.clientTier
          });
        }
      }));
      var toSend = JSON.parse(temp);
      if ($scope.sboItems.length == 1) {
        toSend.push({
          repTeamCd : "DUMMY",
          salesBoCd : "DUMMY",
          salesBoDesc : "DUMMY",
          mrcCd : "D",
          isuCd : "DUMMY",
          clientTier : "DUMMY"
        });
      }
      
      cmr.showProgress('Saving, please wait...');
      $http({
        url : cmr.CONTEXT_ROOT + '/code/salesBoMaint/process.json',
        method : 'POST',
        params : {
          issuingCntry : $scope.issuingCntry,
          items : toSend,
          massAction : "MASS_SAVE"
        }
      }).then(function(response) {
        cmr.hideProgress();
        if (response.data.result.success) {
          var url = cmr.CONTEXT_ROOT + '/code/salesBoMaint?issuingCntry='+$scope.issuingCntry+'&infoMessage=Records saved successfully.';
          window.location = url;
        } else {
          cmr.showAlert('An error occurred while saving the values.');
        }
      }, function(response) {
        cmr.hideProgress();
        console.log('error.');
      });

    };

    $scope.deleteSBO = function(){
      var result = cmr.query('CHECK_SALES_BRANCH_OFF_RECORDS', {
        ISSUING_CNTRY : $scope.issuingCntry
      });

      if (result == null || result.ret1!='1' ) {
        cmr.showAlert("No mappings found for Issuing Country - "+$scope.issuingCntry+". Cannot perform delete operation.");
        return;
      }
      
      if (confirm('Deleting Sales BO Mapping. This action is irreversible.\nContinue with delete?')) {
        cmr.showProgress('Deleting, please wait...');
        $http({
          url : cmr.CONTEXT_ROOT + '/code/salesBoMaint/process.json',
          method : 'POST',
          params : {
            "issuingCntry" : $scope.issuingCntry,
            "massAction" : "DELETE"

          }
        }).then(function(response) {
          cmr.hideProgress();
          if (response.data.result.success) {
            var url = cmr.CONTEXT_ROOT + '/code/salesBo?infoMessage=SBO Records for issuing country - '+country+' deleted successfully.';
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
        if($scope.sboItems[index].edit){
          const curr = $scope.sboItems[index];
          if(curr.status=="N"){
            $scope.sboItems.splice(index,1);
            $scope.oldItems.splice(index,1);
          } else {
            if($scope.oldItems[index]){
              $scope.sboItems[index] = angular.copy($scope.oldItems[index]);
            }
            $scope.sboItems[index].status="D";
          }
        } 
        else {
          $scope.sboItems[index].status = "D";
        }
      } 
    }

    $scope.unRemoveValue = function(index){
      if($scope.sboItems[index].status=="D"){
        $scope.sboItems[index].status=angular.copy($scope.oldItems[index].status);
      }
    };

    $scope.addNew = function(index){
      $scope.sboItems.splice(index,0,{
        "repTeamCd" : "",
        "salesBoCd" : "",
        "salesBoDesc" : "",
        "mrcCd" : "",
        "isuCd" : "",
        "clientTier" : "",
        "edit" : true,
        "status" : "N"
      });
      
      $scope.oldItems[index] = angular.copy($scope.sboItems[index]);
      console.log($scope.oldItems,$scope.sboItems);
    };

    $scope.editValue = function(index){
      $scope.sboItems[index].edit = true;
      $scope.sboItems[index].status = 'U';
    };

    $scope.editISUCTC = function(index,type){
      $scope.current = {};
      $scope.current.index=-1;
      $scope.current.isuCdList = [];
      $scope.current.clientTierList = [];
      if(index>=0){
        $scope.current.index = index;
        const curr = $scope.sboItems[index];
        var {isuCd,clientTier} = curr;
        if(isuCd != null && isuCd !=''){
          isuCd.split(',').forEach(result=>{
            $scope.current.isuCdList.push(result);
          });
        }
        if(clientTier!=null && clientTier!=''){
          clientTier.split(',').forEach(result=>{
            $scope.current.clientTierList.push(result);
          });
        }
        if(type=="isu"){
          cmr.showModal("isuCodeModal");
        } else if(type=="ctc"){
          cmr.showModal("clientTierModal");
        }
      }
    };

    $scope.cancelISUCTC = function(type){
      if(type == "isu"){
        cmr.hideModal("isuCodeModal");
      } else {
        cmr.hideModal("clientTierModal");
      }
      $scope.current = {};
      $scope.current.isuCdList = [];
      $scope.current.clientTierList = [];
      $scope.current.index=-1;
    };

    $scope.saveISU = function(){
      const {index,isuCdList} = $scope.current;
      if(index!=-1){
        $scope.sboItems[index].isuCd=isuCdList.join(",");
      }
      console.log($scope.sboItems[index]);
      $scope.cancelISUCTC('isu');
    };


    $scope.saveCTC = function(){
      const {index,clientTierList} = $scope.current;
      if(index!=-1){
        $scope.sboItems[index].clientTier=clientTierList.join(",");
      }
      console.log($scope.sboItems[index]);
      $scope.cancelISUCTC('ctc');
    };
  } ]);