var PageManager = (function() {
  var handles = new Array();
  var handleMap = {};
  var readOnly = false;
  var alwaysAvailable = [];
  return {
    setAlwaysAvailable : function(fields) {
      alwaysAvailable = fields;
    },
    isAlwaysAvailable : function(field) {
      return alwaysAvailable != null ? alwaysAvailable.indexOf(field) >= 0 : false;
    },
    setReadOnly : function(pageReadOnly) {
      readOnly = pageReadOnly;
    },
    isReadOnly : function() {
      return readOnly;
    },
    init : function(data) {
      if (data && data['fieldMap']) {
        config = data['fieldMap'];
      }
    },
    addHandler : function(handle, name) {
      handles.push(handle);
      handleMap[name] = handle;
    },
    clearHandles : function() {
      for ( var i = 0; i < handles.length; i++) {
        dojo.disconnect(handles[i]);
      }
      handles = new Array();
      handleMap = {};
    },
    removeHandlers : function(field) {
      for ( var i = 0; i < 5; i++) {
        try {
          var handle = handleMap[field + '_handle' + i];
          if (handle) {
            dojo.disconnect(handle);
          }
        } catch (e) {

        }
      }
    },
    getHandles : function() {
      return handles;
    }
  };
})();
