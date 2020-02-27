var app = angular.module('SBOApp', [ 'ngRoute' ]);

app.controller('SBOController', [
    '$scope',
    '$document',
    '$http',
    '$timeout',
    function($scope, $document, $http, $timeout) {
      $scope.sbo = sbo;
      $scope.existing = false;
      if ($scope.sbo == null) {
        $scope.sbo = {};
      }
      $scope.sbo.isuCdList = [];
      $scope.sbo.clientTierList =[];
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
      
      if ($scope.sbo.issuingCntry != null && $scope.sbo.repTeamCd != null && $scope.sbo.salesBoCd!=null) {
        $scope.existing = true;
        var {isuCd,clientTier} = $scope.sbo;
        if(isuCd != null && isuCd !=''){
          isuCd.split(',').forEach(result=>{
            $scope.sbo.isuCdList.push(result);
          });
        }
        if(clientTier!=null && clientTier!=''){
          clientTier.split(',').forEach(result=>{
            $scope.sbo.clientTierList.push(result);
          });
        }
      } else {
        var countries = cmr.query('SYSTEM.SUPPCNTRY', {
          _qall : 'Y'
        });
        $scope.countries = countries;
      }
      
      $scope.backToList = function() {
        window.location = cmr.CONTEXT_ROOT + '/code/salesBo';
         
      };
      
      $scope.addIsu = function() {
        let isuCd = $scope.UI.isuCd;
        const {isuCdList} = $scope.sbo;
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
        const {isuCdList} = $scope.sbo;
        if(isu!=null && isu !='' && isuCdList.includes(isu)){
          const index = isuCdList.indexOf(isu);
          if (index > -1) {
             isuCdList.splice(index, 1);
          }
        }
      }
      
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
        const {clientTierList} = $scope.sbo;
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
        const {clientTierList} = $scope.sbo;
        if(ctc!=null && ctc !='' && clientTierList.includes(ctc)){
          const index = clientTierList.indexOf(ctc);
          if (index > -1) {
             clientTierList.splice(index, 1);
          }
        }
      }
      
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
      }
      
      $scope.saveAll = function(){
        
        //validation
        const {issuingCntry, repTeamCd, salesBoCd, mrcCd, salesBoDesc, isuCdList, clientTierList} = $scope.sbo;
        if (issuingCntry == '' || issuingCntry==null || repTeamCd == null || salesBoCd == null || repTeamCd=='' || salesBoCd ==''  ) {
          cmr.showAlert('Please specify Issuing Country, Rep Team Code and Sales BO Code.');
          return;
        }
        if (!repTeamCd.match(/^[0-9a-zA-Z ]+$/)){
          cmr.showAlert('Rep Team Code can only accept alphanumeric characters.');
          return
        }
        
        if (!salesBoCd.match(/^[0-9a-zA-Z ]+$/)){
          cmr.showAlert('Sales BO Code can only accept alphanumeric characters.');
          return
        }
        
        if (salesBoDesc!=null && salesBoDesc!= '' && !salesBoDesc.match(/^[0-9a-zA-Z ]+$/)){
          cmr.showAlert('Sales BO Description can only accept alphanumeric characters.');
          return
        }
        
        if (mrcCd !=null && mrcCd!='' && !mrcCd.match(/^[0-9a-zA-Z ]+$/)){
          cmr.showAlert('MRC Code can only accept a single alphanumeric character.');
          return
        }
        
        var qResult = cmr.query("CHECK_SALES_BRANCH_OFF",{
          "ISSUING_CNTRY":issuingCntry,
          "REP_TEAM_CD" : repTeamCd,
          "SALES_BO_CD" : salesBoCd
        })
        if(qResult!=null && qResult.ret1=='1' && !$scope.existing){
          cmr.showAlert('This combination of Rep Team Code and Sales BO Code already exists for issuing country - '+issuingCntry+'. Please check your inputs and retry.');
          return;
        }
        //validation - end
        
        var action = '';
        if($scope.existing){
          action = "U";
        } else {
          action ="I";
        }
        cmr.showProgress('Saving, please wait...');
        $http({
          url : cmr.CONTEXT_ROOT + '/code/salesBo/process.json',
          method : 'POST',
          params : {
            "issuingCntry" : issuingCntry,
            "repTeamCd" : repTeamCd,
            "salesBoCd" : salesBoCd,
            "salesBoDesc" : salesBoDesc,
            "isuCd" : isuCdList.join(","),
            "clientTier" : clientTierList.join(","),
            "mrcCd" : mrcCd,
            "action" : action
            
          }
        }).then(function(response) {
          cmr.hideProgress();
          if (response.data.success) {
            $scope.existing = true;
            cmr.showAlert('List of values saved successfully', null, null, true);
          } else {
            cmr.showAlert('An error occurred while saving the values.');
          }
        }, function(response) {
          cmr.hideProgress();
          console.log('error: ');
          console.log(response);
        });
      
      }
      
      $scope.deleteSBO = function(){
        const {issuingCntry, repTeamCd, salesBoCd} = $scope.sbo;
        if (confirm('Deleting Sales BO Mapping. This action is irreversible.\nContinue with delete?')) {
          cmr.showProgress('Deleting, please wait...');
          $http({
            url : cmr.CONTEXT_ROOT + '/code/salesBo/process.json',
            method : 'POST',
            params : {
              "issuingCntry" : issuingCntry,
              "repTeamCd" : repTeamCd,
              "salesBoCd" : salesBoCd,
              "action" : "D"
              
            }
          }).then(function(response) {
            cmr.hideProgress();
            if (response.data.success) {
              var url = cmr.CONTEXT_ROOT + '/code/salesBo?infoMessage=SBO Mapping deleted successfully.';
              window.location = url;
              console.log(response);
            } else {
              cmr.showAlert('An error occurred while deleting.');
            }
          }, function(response) {
            cmr.hideProgress();
            console.log('error: ');
            console.log(response);
          });
        }
      }
    } ]);