var app = angular.module('APCLUSTERApp', [ 'ngRoute' ]);

app.controller('APCLUSTERController', [
  '$scope',
  '$document',
  '$http',
  '$timeout',
  function($scope, $document, $http, $timeout) {
    $scope.current = {};
    $scope.oldItems = [];
    $scope.apClusterItems = [];
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

    $http.get(cmr.CONTEXT_ROOT + '/code/apClusterList.json?issuingCntry='+country)
    .then(function(response) {
      console.log(response.data);
      if (response.data.items.length>0) {
        response.data.items.forEach(function(item) {
          $scope.apClusterItems.push({
             "apCustClusterId" : item.apCustClusterId,
             "defaultIndc" : item.defaultIndc,
            "clusterDesc" : item.clusterDesc,
            "isuCode" : item.isuCode,
            "clientTierCd" : item.clientTierCd,
            "edit" : false,
            "status" : "E"
          });
        });
        $scope.oldItems = angular.copy($scope.apClusterItems);
      }
    });

    $scope.backToList = function() {
      window.location = cmr.CONTEXT_ROOT + '/code/apCluster';

    };

    $scope.addIsu = function() {
      let isuCd = $scope.UI.isuCd;
      const {isuCdList} = $scope.current;
      isuCd = isuCd.trim();
      if(isuCd!=''){
        if(!isuCdList.includes(isuCd)){
          isuCdList.push(isuCd);
          if(isuCdList.length >1){
            alert('Only one ISU Code can be mapped per row.');    
          }
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
      
      for(let i=0;i<$scope.apClusterItems.length;i++){
        const curr = $scope.apClusterItems[i];
        if(curr.apCustClusterId==''  && curr.clusterDesc=='' && curr.isuCode=='' && curr.clientTierCd=='' && curr.defaultIndc==''){
          $scope.apClusterItems.splice(i,1);
          $scope.oldItems.splice(i,1);
          i--;
          continue;
        }
        
//        if((curr.apClusterId == null || curr.repTeamCd == '') && (curr.salesBoCd == null || curr.salesBoCd == '')){
//          cmr.showAlert('Please provide a value to Rep Team Code or Sales BO Code or both, before saving.');
//          return;
//        }

//        if (curr.repTeamCd !=null && curr.repTeamCd!='' && !curr.repTeamCd.match(/^[0-9a-zA-Z ]+$/)){
//          cmr.showAlert('Rep Team Code can only accept alphanumeric characters. Found value: '+curr.repTeamCd);
//          return;
//        }
//
//        if (curr.salesBoCd !=null && curr.salesBoCd!='' && !curr.salesBoCd.match(/^[0-9a-zA-Z ]+$/)){
//          cmr.showAlert('Sales BO Code can only accept alphanumeric characters. Found value: '+curr.salesBoCd);
//          return;
//        }
//
//        if (curr.mrcCd !=null && curr.mrcCd!='' && !curr.mrcCd.match(/^[0-9a-zA-Z ]+$/)){
//          cmr.showAlert('MRC Code can only accept a single alphanumeric character. Found value: '+curr.mrcCd);
//          return;
//        }

        let found = false;
        let foundDupDefIndc = false;
        pk.forEach((item,id)=>{
          if(item.apCustClusterId == curr.apCustClusterId && item.isuCode==curr.isuCode && item.clientTierCd==curr.clientTierCd){
            found = true;
          }
          if(item.defaultIndc == 'Y' && curr.defaultIndc == 'Y'){
            foundDupDefIndc = true;
          }
        });
        
        if(found){
          cmr.showAlert("Duplicate entry exist for same AP Cluster ID: "+curr.apCustClusterId+". Please specify a unique AP Cluster ID , ISU Code and Client Tier combination.");
          return;
        } else {
          if(foundDupDefIndc){
            cmr.showAlert("Only one entry's Default Indicator can be set as Y.");
            return;
          }
          pk.push({
            apCustClusterId:curr.apCustClusterId,
            isuCode:curr.isuCode,
            clientTierCd:curr.clientTierCd,
            defaultIndc:curr.defaultIndc
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

      var temp = JSON.stringify($scope.apClusterItems.map((item)=>{
        if(item.status!='D'){
          return JSON.stringify({
            apCustClusterId : item.apCustClusterId,
            defaultIndc : item.defaultIndc,
            clusterDesc : item.clusterDesc,
            isuCode : item.isuCode,
            clientTierCd : item.clientTierCd,
            defaultIndc : item.defaultIndc
          });
        }
      }));
      var toSend = JSON.parse(temp);
      if ($scope.apClusterItems.length == 1) {
        toSend.push({
          apCustClusterId : "DUMMY",
          defaultIndc : 'DUMMY',
          clusterDesc : "DUMMY",
          isuCode : "DUMMY",
          clientTierCd : "DUMMY"
        });
      }
      
      cmr.showProgress('Saving, please wait...');
      $http({
        url : cmr.CONTEXT_ROOT + '/code/apClusterMaint/process.json',
        method : 'POST',
        params : {
          issuingCntry : $scope.issuingCntry,
          items : toSend,
          massAction : "MASS_SAVE"
        }
      }).then(function(response) {
        cmr.hideProgress();
        if (response.data.result.success) {
          var url = cmr.CONTEXT_ROOT + '/code/apClusterMaint?issuingCntry='+$scope.issuingCntry+'&infoMessage=Records saved successfully.';
          window.location = url;
        } else {
          cmr.showAlert('An error occurred while saving the values.');
        }
      }, function(response) {
        cmr.hideProgress();
        console.log('error.');
      });

    };

    $scope.deleteApCluster = function(){
      var result = cmr.query('CHECK_AP_CLUSTER_RECORDS', {
        ISSUING_CNTRY : $scope.issuingCntry
      });

      if (result == null || result.ret1!='1' ) {
        cmr.showAlert("No mappings found for Issuing Country - "+$scope.issuingCntry+". Cannot perform delete operation.");
        return;
      }
      
      if (confirm('Deleting AP Tier Cluster Mapping. This action is irreversible.\nContinue with delete?')) {
        cmr.showProgress('Deleting, please wait...');
        $http({
          url : cmr.CONTEXT_ROOT + '/code/apClusterMaint/process.json',
          method : 'POST',
          params : {
            "issuingCntry" : $scope.issuingCntry,
            "massAction" : "DELETE"

          }
        }).then(function(response) {
          cmr.hideProgress();
          if (response.data.result.success) {
            var url = cmr.CONTEXT_ROOT + '/code/apCluster?infoMessage=AP Cluster Records for issuing country - '+country+' deleted successfully.';
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
        if($scope.apClusterItems[index].edit){
          const curr = $scope.apClusterItems[index];
          if(curr.status=="N"){
            $scope.apClusterItems.splice(index,1);
            $scope.oldItems.splice(index,1);
          } else {
            if($scope.oldItems[index]){
              $scope.apClusterItems[index] = angular.copy($scope.oldItems[index]);
            }
            $scope.apClusterItems[index].status="D";
          }
        } 
        else {
          $scope.apClusterItems[index].status = "D";
        }
      } 
    }

    $scope.unRemoveValue = function(index){
      if($scope.apClusterItems[index].status=="D"){
        $scope.apClusterItems[index].status=angular.copy($scope.oldItems[index].status);
      }
    };

    $scope.addNew = function(index){
      $scope.apClusterItems.splice(index,0,{
        "apCustClusterId" : "",
        "clusterDesc" : "",
        "isuCode" : "",
        "clientTierCd" : "",
        "edit" : true,
        "status" : "N"
      });
      
      $scope.oldItems[index] = angular.copy($scope.apClusterItems[index]);
      console.log($scope.oldItems,$scope.apClusterItems);
    };

    $scope.editValue = function(index){
      $scope.apClusterItems[index].edit = true;
      $scope.apClusterItems[index].status = 'U';
    };

    $scope.editISUCTC = function(index,type){
      $scope.current = {};
      $scope.current.index=-1;
      $scope.current.isuCdList = [];
      $scope.current.clientTierList = [];
      if(index>=0){
        $scope.current.index = index;
        const curr = $scope.apClusterItems[index];
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
      if(isuCdList.length > 1) {
        alert("Only one ISU code can be mapped per row. Please remove additional ones.");
      }
      else{
        if(index!=-1){
          $scope.apClusterItems[index].isuCode=isuCdList[isuCdList.length-1];
        }
        console.log($scope.apClusterItems[index]);
        $scope.cancelISUCTC('isu');
      }     
    };


    $scope.saveCTC = function(){
      const {index,clientTierList} = $scope.current;
      if(index!=-1){
        $scope.apClusterItems[index].clientTierCd=clientTierList.join(",");
      }
      console.log($scope.apClusterItems[index]);
      $scope.cancelISUCTC('ctc');
    };
    
    $scope.updateSelection = function(position, apClusterItems,title) {
      apClusterItems.forEach(function(item,index) {
          if (position != index)
            item.defaultIndc = '';
              $scope.selected = title;
          }
      );
  };
  } ]);