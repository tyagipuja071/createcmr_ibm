// new functions for comments
function doAddComment() {
  var cmt = FormManager.getActualValue('addCommentTA');
  if (!cmt || cmt.trim().length == 0) {
    cmr.showAlert('Please specify the comment to add to the request.');
    return;
  }
  if (cmt && cmt.length > 2000) {
    cmr.showAlert('Please limit comment length to 2000 characters.');
    return;
  }

  cmr.showProgress('Saving comment, please wait...');

  dojo.xhrPost({
    url : cmr.CONTEXT_ROOT + '/addcomment.json',
    handleAs : 'json',
    content : {
      reqId : FormManager.getActualValue('reqId'),
      comment : cmt
    },
    method : 'POST',
    timeout : 50000,
    load : function(data, ioargs) {
      cmr.hideProgress();
      if (data && data.success) {
        FormManager.setValue('addCommentTA', '');
        dojo.byId('addCommentTA').innerHTML = '';
        cmr.hideModal('addCommentModal');
        CmrGrid.refresh('COMMENT_LIST_GRID');
      } else {
        cmr.showAlert(data.msg);
      }
    },
    error : function(error, ioargs) {
      console.log(error);
      cmr.hideProgress();
      cmr.showAlert('An error occurred during the process. Please contact your system admnistrator.');
    }
  });

}
