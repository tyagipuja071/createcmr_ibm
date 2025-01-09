/*
  This file contains the specific replacement for all clientTierCodeValidator functions. 
  ISU and CTC combination is a global function and in the future only this validator should
  be updated and maintained. all instances of clientTierCodeValidator on the sepsrated files
  are overridden by this function
  
 */

/**
 * Override of the clientTierCodeValidator that will affect all JS files that
 * implemented it
 * 
 * @returns
 */
function clientTierCodeValidator() {
  console.log('Executing global clientTierCodeValidator function..');
  var isuCode = FormManager.getActualValue('isuCd');
  var clientTierCode = FormManager.getActualValue('clientTier');
  var reqType = FormManager.getActualValue('reqType');

  if (((isuCode == '21' || isuCode == '8B' || isuCode == '5K') && reqType == 'C')
      || ((isuCode != '33' && isuCode != '34' && isuCode != '27' && isuCode != '36') && reqType == 'U')) {
    if (clientTierCode == '') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else if (isuCode == '34') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Q') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\'\'.');
    }
  } else if (isuCode == '27') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'E') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'E\'\'.');
    }
  } else if (isuCode == '36') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'Y') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Y\'\'.');
    }
  } else if (isuCode == '33') {
    if (clientTierCode == '') {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier code is Mandatory.');
    } else if (clientTierCode == 'W') {
      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'W\'\'.');
    }
  } else if (isuCode != '36' || isuCode != '34' || isuCode != '27' || isuCode != '33') {
    if (clientTierCode == '') {

      return new ValidationResult(null, true);
    } else {
      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept blank.');
    }
  } else {
    if (clientTierCode == 'Q' || clientTierCode == 'Y' || clientTierCode == 'E' || clientTierCode == '' || clientTierCode == 'W') {
      $("#clientTierSpan").html('');

      return new ValidationResult(null, true);
    } else {
      $("#clientTierSpan").html('');
      $("#clientTierSpan").append('<span style="color:red" class="cmr-ast" id="ast-clientTier">* </span>');

      return new ValidationResult({
        id : 'clientTier',
        type : 'text',
        name : 'clientTier'
      }, false, 'Client Tier can only accept \'Q\', \'Y\', \'E\', \'W\' or blank.');
    }
  }
}

function validatorISUCTCES(){
  console.log('Adding global ctc validator for ES');
  FormManager.addFormValidator((function() {
    return {
      validate : clientTierCodeValidator
    };
  })(), 'MAIN_IBM_TAB', 'frmCMR');
};

function validatorISUCTCEntGR() {
  return clientTierCodeValidator();
}

function validateIsuClientTier() {
  return clientTierCodeValidator();
}

function validatorISUCTCEntMT() {
  return clientTierCodeValidator();
}