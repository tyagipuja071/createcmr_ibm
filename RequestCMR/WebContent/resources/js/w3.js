/* 20170517 1431 */
/*
	Copyright (c) 2004-2011, The Dojo Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

/*
	This is an optimized version of Dojo, built for deployment and not for
	development. To get sources and documentation, please visit:

		http://dojotoolkit.org
*/
(function() {
  eval("var djConfig = {locale:\"en-us\",ibmForceConfig:\"production\",isDebug:false,parseOnLoad:false,baseUrl:\"./\",modulePaths:{\"ibmweb\":\"//1.w3.s81c.com/common/js/dojo/1.6/ibmweb\",\"ext\":\"//1.w3.s81c.com/common/js/dojo/1.6/ext\"},useXDomain:true,xdWaitSeconds:15,xdDojoPath:\"//1.w3.s81c.com/common/js/dojo/1.6\"};");
  var _1 = null;
  if ((_1 || (typeof djConfig != "undefined" && djConfig.scopeMap)) && (typeof window != "undefined")) {
    var _2 = "",
      _3 = "",
      _4 = "",
      _5 = {},
      _6 = {};
    _1 = _1 || djConfig.scopeMap;
    for (var i = 0; i < _1.length; i++) {
      var _7 = _1[i];
      _2 += "var " + _7[0] + " = {}; " + _7[1] + " = " + _7[0] + ";" + _7[1] + "._scopeName = '" + _7[1] + "';";
      _3 += (i == 0 ? "" : ",") + _7[0];
      _4 += (i == 0 ? "" : ",") + _7[1];
      _5[_7[0]] = _7[1];
      _6[_7[1]] = _7[0];
    }
    eval(_2 + "dojo._scopeArgs = [" + _4 + "];");
    dojo._scopePrefixArgs = _3;
    dojo._scopePrefix = "(function(" + _3 + "){";
    dojo._scopeSuffix = "})(" + _4 + ")";
    dojo._scopeMap = _5;
    dojo._scopeMapRev = _6;
  }(function() {
    if (typeof this["loadFirebugConsole"] == "function") {
      this["loadFirebugConsole"]();
    } else {
      this.console = this.console || {};
      var cn = ["assert", "count", "debug", "dir", "dirxml", "error", "group", "groupEnd", "info", "profile", "profileEnd", "time", "timeEnd", "trace", "warn", "log"];
      var i = 0,
        tn;
      while ((tn = cn[i++])) {
        if (!console[tn]) {
          (function() {
            var _8 = tn + "";
            console[_8] = ("log" in console) ? function() {
              var a = Array.apply({}, arguments);
              a.unshift(_8 + ":");
              console["log"](a.join(" "));
            } : function() {};
            console[_8]._fake = true;
          })();
        }
      }
    }
    if (typeof dojo == "undefined") {
      dojo = {
        _scopeName: "dojo",
        _scopePrefix: "",
        _scopePrefixArgs: "",
        _scopeSuffix: "",
        _scopeMap: {},
        _scopeMapRev: {}
      };
    }
    var d = dojo;
    if (typeof dijit == "undefined") {
      dijit = {
        _scopeName: "dijit"
      };
    }
    if (typeof dojox == "undefined") {
      dojox = {
        _scopeName: "dojox"
      };
    }
    if (!d._scopeArgs) {
      d._scopeArgs = [dojo, dijit, dojox];
    }
    d.global = this;
    d.config = {
      isDebug: false,
      debugAtAllCosts: false
    };
    var _9 = typeof djConfig != "undefined" ? djConfig : typeof dojoConfig != "undefined" ? dojoConfig : null;
    if (_9) {
      for (var c in _9) {
        d.config[c] = _9[c];
      }
    }
    dojo.locale = d.config.locale;
    var _a = "$Rev: 23843 $".match(/\d+/);
    dojo.version = {
      major: 1,
      minor: 6,
      patch: 0,
      flag: "-ibm",
      revision: _a ? +_a[0] : NaN,
      toString: function() {
        with(d.version) {
          return major + "." + minor + "." + patch + flag + " (" + revision + ")";
        }
      }
    };
    if (typeof OpenAjax != "undefined") {
      OpenAjax.hub.registerLibrary(dojo._scopeName, "http://dojotoolkit.org", d.version.toString());
    }
    var _b, _c, _d = {};
    for (var i in {
        toString: 1
      }) {
      _b = [];
      break;
    }
    dojo._extraNames = _b = _b || ["hasOwnProperty", "valueOf", "isPrototypeOf", "propertyIsEnumerable", "toLocaleString", "toString", "constructor"];
    _c = _b.length;
    dojo._mixin = function(_e, _f) {
      var _10, s, i;
      for (_10 in _f) {
        s = _f[_10];
        if (!(_10 in _e) || (_e[_10] !== s && (!(_10 in _d) || _d[_10] !== s))) {
          _e[_10] = s;
        }
      }
      if (_c && _f) {
        for (i = 0; i < _c; ++i) {
          _10 = _b[i];
          s = _f[_10];
          if (!(_10 in _e) || (_e[_10] !== s && (!(_10 in _d) || _d[_10] !== s))) {
            _e[_10] = s;
          }
        }
      }
      return _e;
    };
    dojo.mixin = function(obj, _11) {
      if (!obj) {
        obj = {};
      }
      for (var i = 1, l = arguments.length; i < l; i++) {
        d._mixin(obj, arguments[i]);
      }
      return obj;
    };
    dojo._getProp = function(_12, _13, _14) {
      var obj = _14 || d.global;
      for (var i = 0, p; obj && (p = _12[i]); i++) {
        if (i == 0 && d._scopeMap[p]) {
          p = d._scopeMap[p];
        }
        obj = (p in obj ? obj[p] : (_13 ? obj[p] = {} : undefined));
      }
      return obj;
    };
    dojo.setObject = function(_15, _16, _17) {
      var _18 = _15.split("."),
        p = _18.pop(),
        obj = d._getProp(_18, true, _17);
      return obj && p ? (obj[p] = _16) : undefined;
    };
    dojo.getObject = function(_19, _1a, _1b) {
      return d._getProp(_19.split("."), _1a, _1b);
    };
    dojo.exists = function(_1c, obj) {
      return d.getObject(_1c, false, obj) !== undefined;
    };
    dojo["eval"] = function(_1d) {
      return d.global.eval ? d.global.eval(_1d) : eval(_1d);
    };
    d.deprecated = d.experimental = function() {};
  })();
  (function() {
    var d = dojo,
      _1e;
    d.mixin(d, {
      _loadedModules: {},
      _inFlightCount: 0,
      _hasResource: {},
      _modulePrefixes: {
        dojo: {
          name: "dojo",
          value: "."
        },
        doh: {
          name: "doh",
          value: "../util/doh"
        },
        tests: {
          name: "tests",
          value: "tests"
        }
      },
      _moduleHasPrefix: function(_1f) {
        var mp = d._modulePrefixes;
        return !!(mp[_1f] && mp[_1f].value);
      },
      _getModulePrefix: function(_20) {
        var mp = d._modulePrefixes;
        if (d._moduleHasPrefix(_20)) {
          return mp[_20].value;
        }
        return _20;
      },
      _loadedUrls: [],
      _postLoad: false,
      _loaders: [],
      _unloaders: [],
      _loadNotifying: false
    });
    dojo._loadUriAndCheck = function(uri, _21, cb) {
      var ok = false;
      try {
        ok = d._loadUri(uri, cb);
      } catch (e) {}
      return !!(ok && d._loadedModules[_21]);
    };
    dojo.loaded = function() {
      d._loadNotifying = true;
      d._postLoad = true;
      var mll = d._loaders;
      d._loaders = [];
      for (var x = 0; x < mll.length; x++) {
        mll[x]();
      }
      d._loadNotifying = false;
      if (d._postLoad && d._inFlightCount == 0 && mll.length) {
        d._callLoaded();
      }
    };
    dojo.unloaded = function() {
      var mll = d._unloaders;
      while (mll.length) {
        (mll.pop())();
      }
    };
    d._onto = function(arr, obj, fn) {
      if (!fn) {
        arr.push(obj);
      } else {
        if (fn) {
          var _22 = (typeof fn == "string") ? obj[fn] : fn;
          arr.push(function() {
            _22.call(obj);
          });
        }
      }
    };
    dojo.ready = dojo.addOnLoad = function(obj, _23) {
      d._onto(d._loaders, obj, _23);
      if (d._postLoad && d._inFlightCount == 0 && !d._loadNotifying) {
        d._callLoaded();
      }
    };
    var dca = d.config.addOnLoad;
    if (dca) {
      d.addOnLoad[(dca instanceof Array ? "apply" : "call")](d, dca);
    }
    dojo._modulesLoaded = function() {
      if (d._postLoad) {
        return;
      }
      if (d._inFlightCount > 0) {
        return;
      }
      d._callLoaded();
    };
    dojo._callLoaded = function() {
      if (typeof setTimeout == "object" || (d.config.useXDomain && d.isOpera)) {
        setTimeout(d.isAIR ? function() {
          d.loaded();
        } : d._scopeName + ".loaded();", 0);
      } else {
        d.loaded();
      }
    };
    dojo._getModuleSymbols = function(_24) {
      var _25 = _24.split(".");
      for (var i = _25.length; i > 0; i--) {
        var _26 = _25.slice(0, i).join(".");
        if (i == 1 && !d._moduleHasPrefix(_26)) {
          _25[0] = "../" + _25[0];
        } else {
          var _27 = d._getModulePrefix(_26);
          if (_27 != _26) {
            _25.splice(0, i, _27);
            break;
          }
        }
      }
      return _25;
    };
    dojo._global_omit_module_check = false;
    dojo.loadInit = function(_28) {
      _28();
    };
    dojo._loadModule = dojo.require = function(_29, _2a) {
      _2a = d._global_omit_module_check || _2a;
      var _2b = d._loadedModules[_29];
      if (_2b) {
        return _2b;
      }
      var _2c = d._getModuleSymbols(_29).join("/") + ".js";
      var _2d = !_2a ? _29 : null;
      var ok = d._loadPath(_2c, _2d);
      if (!ok && !_2a) {
        throw new Error("Could not load '" + _29 + "'; last tried '" + _2c + "'");
      }
      if (!_2a && !d._isXDomain) {
        _2b = d._loadedModules[_29];
        if (!_2b) {
          throw new Error("symbol '" + _29 + "' is not defined after loading '" + _2c + "'");
        }
      }
      return _2b;
    };
    dojo.provide = function(_2e) {
      _2e = _2e + "";
      return (d._loadedModules[_2e] = d.getObject(_2e, true));
    };
    dojo.platformRequire = function(_2f) {
      var _30 = _2f.common || [];
      var _31 = _30.concat(_2f[d._name] || _2f["default"] || []);
      for (var x = 0; x < _31.length; x++) {
        var _32 = _31[x];
        if (_32.constructor == Array) {
          d._loadModule.apply(d, _32);
        } else {
          d._loadModule(_32);
        }
      }
    };
    dojo.requireIf = function(_33, _34) {
      if (_33 === true) {
        var _35 = [];
        for (var i = 1; i < arguments.length; i++) {
          _35.push(arguments[i]);
        }
        d.require.apply(d, _35);
      }
    };
    dojo.requireAfterIf = d.requireIf;
    dojo.registerModulePath = function(_36, _37) {
      d._modulePrefixes[_36] = {
        name: _36,
        value: _37
      };
    };
    if (typeof dojo.config["useXDomain"] == "undefined") {
      dojo.config.useXDomain = true;
    }
    dojo.registerModulePath("dojo", "//1.w3.s81c.com/common/js/dojo/1.6/dojo");
    dojo.registerModulePath("dijit", "//1.w3.s81c.com/common/js/dojo/1.6/dijit");
    dojo.registerModulePath("dojox", "//1.w3.s81c.com/common/js/dojo/1.6/dojox");
    dojo.requireLocalization = function(_38, _39, _3a, _3b) {
      d.require("dojo.i18n");
      d.i18n._requireLocalization.apply(d.hostenv, arguments);
    };
    var ore = new RegExp("^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?$"),
      ire = new RegExp("^((([^\\[:]+):)?([^@]+)@)?(\\[([^\\]]+)\\]|([^\\[:]*))(:([0-9]+))?$");
    dojo._Url = function() {
      var n = null,
        _3c = arguments,
        uri = [_3c[0]];
      for (var i = 1; i < _3c.length; i++) {
        if (!_3c[i]) {
          continue;
        }
        var _3d = new d._Url(_3c[i] + ""),
          _3e = new d._Url(uri[0] + "");
        if (_3d.path == "" && !_3d.scheme && !_3d.authority && !_3d.query) {
          if (_3d.fragment != n) {
            _3e.fragment = _3d.fragment;
          }
          _3d = _3e;
        } else {
          if (!_3d.scheme) {
            _3d.scheme = _3e.scheme;
            if (!_3d.authority) {
              _3d.authority = _3e.authority;
              if (_3d.path.charAt(0) != "/") {
                var _3f = _3e.path.substring(0, _3e.path.lastIndexOf("/") + 1) + _3d.path;
                var _40 = _3f.split("/");
                for (var j = 0; j < _40.length; j++) {
                  if (_40[j] == ".") {
                    if (j == _40.length - 1) {
                      _40[j] = "";
                    } else {
                      _40.splice(j, 1);
                      j--;
                    }
                  } else {
                    if (j > 0 && !(j == 1 && _40[0] == "") && _40[j] == ".." && _40[j - 1] != "..") {
                      if (j == (_40.length - 1)) {
                        _40.splice(j, 1);
                        _40[j - 1] = "";
                      } else {
                        _40.splice(j - 1, 2);
                        j -= 2;
                      }
                    }
                  }
                }
                _3d.path = _40.join("/");
              }
            }
          }
        }
        uri = [];
        if (_3d.scheme) {
          uri.push(_3d.scheme, ":");
        }
        if (_3d.authority) {
          uri.push("//", _3d.authority);
        }
        uri.push(_3d.path);
        if (_3d.query) {
          uri.push("?", _3d.query);
        }
        if (_3d.fragment) {
          uri.push("#", _3d.fragment);
        }
      }
      this.uri = uri.join("");
      var r = this.uri.match(ore);
      this.scheme = r[2] || (r[1] ? "" : n);
      this.authority = r[4] || (r[3] ? "" : n);
      this.path = r[5];
      this.query = r[7] || (r[6] ? "" : n);
      this.fragment = r[9] || (r[8] ? "" : n);
      if (this.authority != n) {
        r = this.authority.match(ire);
        this.user = r[3] || n;
        this.password = r[4] || n;
        this.host = r[6] || r[7];
        this.port = r[9] || n;
      }
    };
    dojo._Url.prototype.toString = function() {
      return this.uri;
    };
    dojo.moduleUrl = function(_41, url) {
      var loc = d._getModuleSymbols(_41).join("/");
      if (!loc) {
        return null;
      }
      if (loc.lastIndexOf("/") != loc.length - 1) {
        loc += "/";
      }
      var _42 = loc.indexOf(":");
      if (loc.charAt(0) != "/" && (_42 == -1 || _42 > loc.indexOf("/"))) {
        loc = d.baseUrl + loc;
      }
      return new d._Url(loc, url);
    };
  })();
  dojo.provide("dojo._base._loader.loader_xd");
  dojo._xdReset = function() {
    dojo._isXDomain = dojo.config.useXDomain || false;
    dojo._xdClearInterval();
    dojo._xdInFlight = {};
    dojo._xdOrderedReqs = [];
    dojo._xdDepMap = {};
    dojo._xdContents = [];
    dojo._xdDefList = [];
  };
  dojo._xdClearInterval = function() {
    if (dojo._xdTimer) {
      clearInterval(dojo._xdTimer);
      dojo._xdTimer = 0;
    }
  };
  dojo._xdReset();
  dojo._xdCreateResource = function(_43, _44, _45) {
    var _46 = _43.replace(/(\/\*([\s\S]*?)\*\/|\/\/(.*)$)/mg, "");
    var _47 = [];
    var _48 = /dojo.(require|requireIf|provide|requireAfterIf|platformRequire|requireLocalization)\s*\(([\w\W]*?)\)/mg;
    var _49;
    while ((_49 = _48.exec(_46)) != null) {
      if (_49[1] == "requireLocalization") {
        eval(_49[0]);
      } else {
        _47.push("\"" + _49[1] + "\", " + _49[2]);
      }
    }
    var _4a = [];
    _4a.push(dojo._scopeName + "._xdResourceLoaded(function(" + dojo._scopePrefixArgs + "){\n");
    var _4b = dojo._xdExtractLoadInits(_43);
    if (_4b) {
      _43 = _4b[0];
      for (var i = 1; i < _4b.length; i++) {
        _4a.push(_4b[i] + ";\n");
      }
    }
    _4a.push("return {");
    if (_47.length > 0) {
      _4a.push("depends: [");
      for (i = 0; i < _47.length; i++) {
        if (i > 0) {
          _4a.push(",\n");
        }
        _4a.push("[" + _47[i] + "]");
      }
      _4a.push("],");
    }
    _4a.push("\ndefineResource: function(" + dojo._scopePrefixArgs + "){");
    if (!dojo.config["debugAtAllCosts"] || _44 == "dojo._base._loader.loader_debug") {
      _4a.push(_43);
    }
    _4a.push("\n}, resourceName: '" + _44 + "', resourcePath: '" + _45 + "'};});");
    return _4a.join("");
  };
  dojo._xdExtractLoadInits = function(_4c) {
    var _4d = /dojo.loadInit\s*\(/g;
    _4d.lastIndex = 0;
    var _4e = /[\(\)]/g;
    _4e.lastIndex = 0;
    var _4f = [];
    var _50;
    while ((_50 = _4d.exec(_4c))) {
      _4e.lastIndex = _4d.lastIndex;
      var _51 = 1;
      var _52;
      while ((_52 = _4e.exec(_4c))) {
        if (_52[0] == ")") {
          _51 -= 1;
        } else {
          _51 += 1;
        }
        if (_51 == 0) {
          break;
        }
      }
      if (_51 != 0) {
        throw "unmatched paren around character " + _4e.lastIndex + " in: " + _4c;
      }
      var _53 = _4d.lastIndex - _50[0].length;
      _4f.push(_4c.substring(_53, _4e.lastIndex));
      var _54 = _4e.lastIndex - _53;
      _4c = _4c.substring(0, _53) + _4c.substring(_4e.lastIndex, _4c.length);
      _4d.lastIndex = _4e.lastIndex - _54;
      _4d.lastIndex = _4e.lastIndex;
    }
    if (_4f.length > 0) {
      _4f.unshift(_4c);
    }
    return (_4f.length ? _4f : null);
  };
  dojo._xdIsXDomainPath = function(_55) {
    var _56 = _55.indexOf(":");
    var _57 = _55.indexOf("/");
    if (_56 > 0 && _56 < _57 || _55.indexOf("//") === 0) {
      return true;
    } else {
      var url = dojo.baseUrl;
      _56 = url.indexOf(":");
      _57 = url.indexOf("/");
      if (url.indexOf("//") === 0 || (_56 > 0 && _56 < _57 && (!location.host || url.indexOf("http://" + location.host) != 0))) {
        return true;
      }
    }
    return false;
  };
  dojo._loadPath = function(_58, _59, cb) {
    var _5a = dojo._xdIsXDomainPath(_58);
    dojo._isXDomain |= _5a;
    var uri = ((_58.charAt(0) == "/" || _58.match(/^\w+:/)) ? "" : dojo.baseUrl) + _58;
    try {
      return ((!_59 || dojo._isXDomain) ? dojo._loadUri(uri, cb, _5a, _59) : dojo._loadUriAndCheck(uri, _59, cb));
    } catch (e) {
      return false;
    }
  };
  dojo._xdCharSet = "utf-8";
  dojo._loadUri = function(uri, cb, _5b, _5c) {
    if (dojo._loadedUrls[uri]) {
      return 1;
    }
    if (dojo._isXDomain && _5c && _5c != "dojo.i18n") {
      dojo._xdOrderedReqs.push(_5c);
      if (_5b || uri.indexOf("/nls/") == -1) {
        dojo._xdInFlight[_5c] = true;
        dojo._inFlightCount++;
      }
      if (!dojo._xdTimer) {
        if (dojo.isAIR) {
          dojo._xdTimer = setInterval(function() {
            dojo._xdWatchInFlight();
          }, 100);
        } else {
          dojo._xdTimer = setInterval(dojo._scopeName + "._xdWatchInFlight();", 100);
        }
      }
      dojo._xdStartTime = (new Date()).getTime();
    }
    if (_5b) {
      var _5d = uri.lastIndexOf(".");
      if (_5d <= 0) {
        _5d = uri.length - 1;
      }
      var _5e = uri.substring(0, _5d) + ".xd";
      if (_5d != uri.length - 1) {
        _5e += uri.substring(_5d, uri.length);
      }
      if (dojo.isAIR) {
        _5e = _5e.replace("app:/", "/");
      }
      var _5f = document.createElement("script");
      _5f.type = "text/javascript";
      if (dojo._xdCharSet) {
        _5f.charset = dojo._xdCharSet;
      }
      _5f.src = _5e;
      if (!dojo.headElement) {
        dojo._headElement = document.getElementsByTagName("head")[0];
        if (!dojo._headElement) {
          dojo._headElement = document.getElementsByTagName("html")[0];
        }
      }
      dojo._headElement.appendChild(_5f);
    } else {
      var _60 = dojo._getText(uri, null, true);
      if (_60 == null) {
        return 0;
      }
      if (dojo._isXDomain && uri.indexOf("/nls/") == -1 && _5c != "dojo.i18n") {
        var res = dojo._xdCreateResource(_60, _5c, uri);
        dojo.eval(res);
      } else {
        if (cb) {
          _60 = "(" + _60 + ")";
        } else {
          _60 = dojo._scopePrefix + _60 + dojo._scopeSuffix;
        }
        if (!dojo.isIE) {
          _60 += "\r\n//@ sourceURL=" + uri;
        }
        var _61 = dojo["eval"](_60);
        if (cb) {
          cb(_61);
        }
      }
    }
    dojo._loadedUrls[uri] = true;
    dojo._loadedUrls.push(uri);
    return true;
  };
  dojo._xdResourceLoaded = function(res) {
    res = res.apply(dojo.global, dojo._scopeArgs);
    var _62 = res.depends;
    var _63 = null;
    var _64 = null;
    var _65 = [];
    if (_62 && _62.length > 0) {
      var dep = null;
      var _66 = 0;
      var _67 = false;
      for (var i = 0; i < _62.length; i++) {
        dep = _62[i];
        if (dep[0] == "provide") {
          _65.push(dep[1]);
        } else {
          if (!_63) {
            _63 = [];
          }
          if (!_64) {
            _64 = [];
          }
          var _68 = dojo._xdUnpackDependency(dep);
          if (_68.requires) {
            _63 = _63.concat(_68.requires);
          }
          if (_68.requiresAfter) {
            _64 = _64.concat(_68.requiresAfter);
          }
        }
        var _69 = dep[0];
        var _6a = _69.split(".");
        if (_6a.length == 2) {
          dojo[_6a[0]][_6a[1]].apply(dojo[_6a[0]], dep.slice(1));
        } else {
          dojo[_69].apply(dojo, dep.slice(1));
        }
      }
      if (_65.length == 1 && _65[0] == "dojo._base._loader.loader_debug") {
        res.defineResource(dojo);
      } else {
        var _6b = dojo._xdContents.push({
          content: res.defineResource,
          resourceName: res["resourceName"],
          resourcePath: res["resourcePath"],
          isDefined: false
        }) - 1;
        for (i = 0; i < _65.length; i++) {
          dojo._xdDepMap[_65[i]] = {
            requires: _63,
            requiresAfter: _64,
            contentIndex: _6b
          };
        }
      }
      for (i = 0; i < _65.length; i++) {
        dojo._xdInFlight[_65[i]] = false;
      }
    }
  };
  dojo._xdLoadFlattenedBundle = function(_6c, _6d, _6e, _6f) {
    _6e = _6e || "root";
    var _70 = dojo.i18n.normalizeLocale(_6e).replace("-", "_");
    var _71 = [_6c, "nls", _6d].join(".");
    var _72 = dojo["provide"](_71);
    _72[_70] = _6f;
    var _73 = [_6c, _70, _6d].join(".");
    var _74 = dojo._xdBundleMap[_73];
    if (_74) {
      for (var _75 in _74) {
        _72[_75] = _6f;
      }
    }
  };
  dojo._xdInitExtraLocales = function() {
    var _76 = dojo.config.extraLocale;
    if (_76) {
      if (!_76 instanceof Array) {
        _76 = [_76];
      }
      dojo._xdReqLoc = dojo.xdRequireLocalization;
      dojo.xdRequireLocalization = function(m, b, _77, _78) {
        dojo._xdReqLoc(m, b, _77, _78);
        if (_77) {
          return;
        }
        for (var i = 0; i < _76.length; i++) {
          dojo._xdReqLoc(m, b, _76[i], _78);
        }
      };
    }
  };
  dojo._xdBundleMap = {};
  dojo.xdRequireLocalization = function(_79, _7a, _7b, _7c) {
    if (dojo._xdInitExtraLocales) {
      dojo._xdInitExtraLocales();
      dojo._xdInitExtraLocales = null;
      dojo.xdRequireLocalization.apply(dojo, arguments);
      return;
    }
    var _7d = _7c.split(",");
    var _7e = dojo.i18n.normalizeLocale(_7b);
    var _7f = "";
    for (var i = 0; i < _7d.length; i++) {
      if (_7e.indexOf(_7d[i]) == 0) {
        if (_7d[i].length > _7f.length) {
          _7f = _7d[i];
        }
      }
    }
    var _80 = _7f.replace("-", "_");
    var _81 = dojo.getObject([_79, "nls", _7a].join("."));
    if (!_81 || !_81[_80]) {
      var _82 = [_79, (_80 || "root"), _7a].join(".");
      var _83 = dojo._xdBundleMap[_82];
      if (!_83) {
        _83 = dojo._xdBundleMap[_82] = {};
      }
      _83[_7e.replace("-", "_")] = true;
      dojo.require(_79 + ".nls" + (_7f ? "." + _7f : "") + "." + _7a);
    }
  };
  dojo._xdRealRequireLocalization = dojo.requireLocalization;
  dojo.requireLocalization = function(_84, _85, _86, _87) {
    var _88 = dojo.moduleUrl(_84).toString();
    if (dojo._xdIsXDomainPath(_88)) {
      return dojo.xdRequireLocalization.apply(dojo, arguments);
    } else {
      return dojo._xdRealRequireLocalization.apply(dojo, arguments);
    }
  };
  dojo._xdUnpackDependency = function(dep) {
    var _89 = null;
    var _8a = null;
    switch (dep[0]) {
      case "requireIf":
      case "requireAfterIf":
        if (dep[1] === true) {
          _89 = [{
            name: dep[2],
            content: null
          }];
        }
        break;
      case "platformRequire":
        var _8b = dep[1];
        var _8c = _8b["common"] || [];
        _89 = (_8b[dojo.hostenv.name_]) ? _8c.concat(_8b[dojo.hostenv.name_] || []) : _8c.concat(_8b["default"] || []);
        if (_89) {
          for (var i = 0; i < _89.length; i++) {
            if (_89[i] instanceof Array) {
              _89[i] = {
                name: _89[i][0],
                content: null
              };
            } else {
              _89[i] = {
                name: _89[i],
                content: null
              };
            }
          }
        }
        break;
      case "require":
        _89 = [{
          name: dep[1],
          content: null
        }];
        break;
      case "i18n._preloadLocalizations":
        dojo.i18n._preloadLocalizations.apply(dojo.i18n._preloadLocalizations, dep.slice(1));
        break;
    }
    if (dep[0] == "requireAfterIf" || dep[0] == "requireIf") {
      _8a = _89;
      _89 = null;
    }
    return {
      requires: _89,
      requiresAfter: _8a
    };
  };
  dojo._xdWalkReqs = function() {
    var _8d = null;
    var req;
    for (var i = 0; i < dojo._xdOrderedReqs.length; i++) {
      req = dojo._xdOrderedReqs[i];
      if (dojo._xdDepMap[req]) {
        _8d = [req];
        _8d[req] = true;
        dojo._xdEvalReqs(_8d);
      }
    }
  };
  dojo._xdEvalReqs = function(_8e) {
    while (_8e.length > 0) {
      var req = _8e[_8e.length - 1];
      var res = dojo._xdDepMap[req];
      var i, _8f, _90;
      if (res) {
        _8f = res.requires;
        if (_8f && _8f.length > 0) {
          for (i = 0; i < _8f.length; i++) {
            _90 = _8f[i].name;
            if (_90 && !_8e[_90]) {
              _8e.push(_90);
              _8e[_90] = true;
              dojo._xdEvalReqs(_8e);
            }
          }
        }
        var _91 = dojo._xdContents[res.contentIndex];
        if (!_91.isDefined) {
          var _92 = _91.content;
          _92["resourceName"] = _91["resourceName"];
          _92["resourcePath"] = _91["resourcePath"];
          dojo._xdDefList.push(_92);
          _91.isDefined = true;
        }
        dojo._xdDepMap[req] = null;
        _8f = res.requiresAfter;
        if (_8f && _8f.length > 0) {
          for (i = 0; i < _8f.length; i++) {
            _90 = _8f[i].name;
            if (_90 && !_8e[_90]) {
              _8e.push(_90);
              _8e[_90] = true;
              dojo._xdEvalReqs(_8e);
            }
          }
        }
      }
      _8e.pop();
    }
  };
  dojo._xdWatchInFlight = function() {
    var _93 = "";
    var _94 = (dojo.config.xdWaitSeconds || 15) * 1000;
    var _95 = (dojo._xdStartTime + _94) < (new Date()).getTime();
    for (var _96 in dojo._xdInFlight) {
      if (dojo._xdInFlight[_96] === true) {
        if (_95) {
          _93 += _96 + " ";
        } else {
          return;
        }
      }
    }
    dojo._xdClearInterval();
    if (_95) {
      throw "Could not load cross-domain resources: " + _93;
    }
    dojo._xdWalkReqs();
    var _97 = dojo._xdDefList.length;
    for (var i = 0; i < _97; i++) {
      var _98 = dojo._xdDefList[i];
      if (dojo.config["debugAtAllCosts"] && _98["resourceName"]) {
        if (!dojo["_xdDebugQueue"]) {
          dojo._xdDebugQueue = [];
        }
        dojo._xdDebugQueue.push({
          resourceName: _98.resourceName,
          resourcePath: _98.resourcePath
        });
      } else {
        _98.apply(dojo.global, dojo._scopeArgs);
      }
    }
    for (i = 0; i < dojo._xdContents.length; i++) {
      var _99 = dojo._xdContents[i];
      if (_99.content && !_99.isDefined) {
        _99.content.apply(dojo.global, dojo._scopeArgs);
      }
    }
    dojo._xdReset();
    if (dojo["_xdDebugQueue"] && dojo._xdDebugQueue.length > 0) {
      dojo._xdDebugFileLoaded();
    } else {
      dojo._xdNotifyLoaded();
    }
  };
  dojo._xdNotifyLoaded = function() {
    for (var _9a in dojo._xdInFlight) {
      if (typeof dojo._xdInFlight[_9a] == "boolean") {
        return;
      }
    }
    dojo._inFlightCount = 0;
    if (dojo._initFired && !dojo._loadNotifying) {
      dojo._callLoaded();
    }
  };
  if (typeof window != "undefined") {
    dojo.isBrowser = true;
    dojo._name = "browser";
    (function() {
      var d = dojo;
      if (document && document.getElementsByTagName) {
        var _9b = document.getElementsByTagName("script");
        var _9c = /dojo(\.xd)?\.js(\W|$)/i;
        for (var i = 0; i < _9b.length; i++) {
          var src = _9b[i].getAttribute("src");
          if (!src) {
            continue;
          }
          var m = src.match(_9c);
          if (m) {
            if (!d.config.baseUrl) {
              d.config.baseUrl = src.substring(0, m.index);
            }
            var cfg = (_9b[i].getAttribute("djConfig") || _9b[i].getAttribute("data-dojo-config"));
            if (cfg) {
              var _9d = eval("({ " + cfg + " })");
              for (var x in _9d) {
                dojo.config[x] = _9d[x];
              }
            }
            break;
          }
        }
      }
      d.baseUrl = d.config.baseUrl;
      var n = navigator;
      var dua = n.userAgent,
        dav = n.appVersion,
        tv = parseFloat(dav);
      if (dua.indexOf("Opera") >= 0) {
        d.isOpera = tv;
      }
      if (dua.indexOf("AdobeAIR") >= 0) {
        d.isAIR = 1;
      }
      d.isKhtml = (dav.indexOf("Konqueror") >= 0) ? tv : 0;
      d.isWebKit = parseFloat(dua.split("WebKit/")[1]) || undefined;
      d.isChrome = parseFloat(dua.split("Chrome/")[1]) || undefined;
      d.isMac = dav.indexOf("Macintosh") >= 0;
      var _9e = Math.max(dav.indexOf("WebKit"), dav.indexOf("Safari"), 0);
      if (_9e && !dojo.isChrome) {
        d.isSafari = parseFloat(dav.split("Version/")[1]);
        if (!d.isSafari || parseFloat(dav.substr(_9e + 7)) <= 419.3) {
          d.isSafari = 2;
        }
      }
      if (dua.indexOf("Gecko") >= 0 && !d.isKhtml && !d.isWebKit) {
        d.isMozilla = d.isMoz = tv;
      }
      if (d.isMoz) {
        d.isFF = parseFloat(dua.split("Firefox/")[1] || dua.split("Minefield/")[1]) || undefined;
      }
      if (document.all && !d.isOpera) {
        d.isIE = parseFloat(dav.split("MSIE ")[1]) || undefined;
        var _9f = document.documentMode;
        if (_9f && _9f != 5 && Math.floor(d.isIE) != _9f) {
          d.isIE = _9f;
        }
      }
      if (dojo.isIE && window.location.protocol === "file:") {
        dojo.config.ieForceActiveXXhr = true;
      }
      d.isQuirks = document.compatMode == "BackCompat";
      d.locale = dojo.config.locale || (d.isIE ? n.userLanguage : n.language).toLowerCase();
      d._XMLHTTP_PROGIDS = ["Msxml2.XMLHTTP", "Microsoft.XMLHTTP", "Msxml2.XMLHTTP.4.0"];
      d._xhrObj = function() {
        var _a0, _a1;
        if (!dojo.isIE || !dojo.config.ieForceActiveXXhr) {
          try {
            _a0 = new XMLHttpRequest();
          } catch (e) {}
        }
        if (!_a0) {
          for (var i = 0; i < 3; ++i) {
            var _a2 = d._XMLHTTP_PROGIDS[i];
            try {
              _a0 = new ActiveXObject(_a2);
            } catch (e) {
              _a1 = e;
            }
            if (_a0) {
              d._XMLHTTP_PROGIDS = [_a2];
              break;
            }
          }
        }
        if (!_a0) {
          throw new Error("XMLHTTP not available: " + _a1);
        }
        return _a0;
      };
      d._isDocumentOk = function(_a3) {
        var _a4 = _a3.status || 0;
        return (_a4 >= 200 && _a4 < 300) || _a4 == 304 || _a4 == 1223 || !_a4;
      };
      var _a5 = window.location + "";
      var _a6 = document.getElementsByTagName("base");
      var _a7 = (_a6 && _a6.length > 0);
      d._getText = function(uri, _a8) {
        var _a9 = d._xhrObj();
        if (!_a7 && dojo._Url) {
          uri = (new dojo._Url(_a5, uri)).toString();
        }
        if (d.config.cacheBust) {
          uri += "";
          uri += (uri.indexOf("?") == -1 ? "?" : "&") + String(d.config.cacheBust).replace(/\W+/g, "");
        }
        _a9.open("GET", uri, false);
        try {
          _a9.send(null);
          if (!d._isDocumentOk(_a9)) {
            var err = Error("Unable to load " + uri + " status:" + _a9.status);
            err.status = _a9.status;
            err.responseText = _a9.responseText;
            throw err;
          }
        } catch (e) {
          if (_a8) {
            return null;
          }
          throw e;
        }
        return _a9.responseText;
      };
      var _aa = window;
      var _ab = function(_ac, fp) {
        var _ad = _aa.attachEvent || _aa.addEventListener;
        _ac = _aa.attachEvent ? _ac : _ac.substring(2);
        _ad(_ac, function() {
          fp.apply(_aa, arguments);
        }, false);
      };
      d._windowUnloaders = [];
      d.windowUnloaded = function() {
        var mll = d._windowUnloaders;
        while (mll.length) {
          (mll.pop())();
        }
        d = null;
      };
      var _ae = 0;
      d.addOnWindowUnload = function(obj, _af) {
        d._onto(d._windowUnloaders, obj, _af);
        if (!_ae) {
          _ae = 1;
          _ab("onunload", d.windowUnloaded);
        }
      };
      var _b0 = 0;
      d.addOnUnload = function(obj, _b1) {
        d._onto(d._unloaders, obj, _b1);
        if (!_b0) {
          _b0 = 1;
          _ab("onbeforeunload", dojo.unloaded);
        }
      };
    })();
    dojo._initFired = false;
    dojo._loadInit = function(e) {
      if (dojo._scrollIntervalId) {
        clearInterval(dojo._scrollIntervalId);
        dojo._scrollIntervalId = 0;
      }
      if (!dojo._initFired) {
        dojo._initFired = true;
        if (!dojo.config.afterOnLoad && window.detachEvent) {
          window.detachEvent("onload", dojo._loadInit);
        }
        if (dojo._inFlightCount == 0) {
          dojo._modulesLoaded();
        }
      }
    };
    if (!dojo.config.afterOnLoad) {
      if (document.addEventListener) {
        document.addEventListener("DOMContentLoaded", dojo._loadInit, false);
        window.addEventListener("load", dojo._loadInit, false);
      } else {
        if (window.attachEvent) {
          window.attachEvent("onload", dojo._loadInit);
          if (!dojo.config.skipIeDomLoaded && self === self.top) {
            dojo._scrollIntervalId = setInterval(function() {
              try {
                if (document.body) {
                  document.documentElement.doScroll("left");
                  dojo._loadInit();
                }
              } catch (e) {}
            }, 30);
          }
        }
      }
    }
    if (dojo.isIE) {
      try {
        (function() {
          document.namespaces.add("v", "urn:schemas-microsoft-com:vml");
          var _b2 = ["*", "group", "roundrect", "oval", "shape", "rect", "imagedata", "path", "textpath", "text"],
            i = 0,
            l = 1,
            s = document.createStyleSheet();
          if (dojo.isIE >= 8) {
            i = 1;
            l = _b2.length;
          }
          for (; i < l; ++i) {
            s.addRule("v\\:" + _b2[i], "behavior:url(#default#VML); display:inline-block");
          }
        })();
      } catch (e) {}
    }
  }(function() {
    var mp = dojo.config["modulePaths"];
    if (mp) {
      for (var _b3 in mp) {
        dojo.registerModulePath(_b3, mp[_b3]);
      }
    }
  })();
  if (dojo.config.isDebug) {
    dojo.require("dojo._firebug.firebug");
  }
  if (dojo.config.debugAtAllCosts) {
    dojo.require("dojo._base._loader.loader_debug");
  }
  if (!dojo._hasResource["dojo._base.lang"]) {
    dojo._hasResource["dojo._base.lang"] = true;
    dojo.provide("dojo._base.lang");
    (function() {
      var d = dojo,
        _b4 = Object.prototype.toString;
      dojo.isString = function(it) {
        return (typeof it == "string" || it instanceof String);
      };
      dojo.isArray = function(it) {
        return it && (it instanceof Array || typeof it == "array");
      };
      dojo.isFunction = function(it) {
        return _b4.call(it) === "[object Function]";
      };
      dojo.isObject = function(it) {
        return it !== undefined && (it === null || typeof it == "object" || d.isArray(it) || d.isFunction(it));
      };
      dojo.isArrayLike = function(it) {
        return it && it !== undefined && !d.isString(it) && !d.isFunction(it) && !(it.tagName && it.tagName.toLowerCase() == "form") && (d.isArray(it) || isFinite(it.length));
      };
      dojo.isAlien = function(it) {
        return it && !d.isFunction(it) && /\{\s*\[native code\]\s*\}/.test(String(it));
      };
      dojo.extend = function(_b5, _b6) {
        for (var i = 1, l = arguments.length; i < l; i++) {
          d._mixin(_b5.prototype, arguments[i]);
        }
        return _b5;
      };
      dojo._hitchArgs = function(_b7, _b8) {
        var pre = d._toArray(arguments, 2);
        var _b9 = d.isString(_b8);
        return function() {
          var _ba = d._toArray(arguments);
          var f = _b9 ? (_b7 || d.global)[_b8] : _b8;
          return f && f.apply(_b7 || this, pre.concat(_ba));
        };
      };
      dojo.hitch = function(_bb, _bc) {
        if (arguments.length > 2) {
          return d._hitchArgs.apply(d, arguments);
        }
        if (!_bc) {
          _bc = _bb;
          _bb = null;
        }
        if (d.isString(_bc)) {
          _bb = _bb || d.global;
          if (!_bb[_bc]) {
            throw (["dojo.hitch: scope[\"", _bc, "\"] is null (scope=\"", _bb, "\")"].join(""));
          }
          return function() {
            return _bb[_bc].apply(_bb, arguments || []);
          };
        }
        return !_bb ? _bc : function() {
          return _bc.apply(_bb, arguments || []);
        };
      };
      dojo.delegate = dojo._delegate = (function() {
        function TMP() {};
        return function(obj, _bd) {
          TMP.prototype = obj;
          var tmp = new TMP();
          TMP.prototype = null;
          if (_bd) {
            d._mixin(tmp, _bd);
          }
          return tmp;
        };
      })();
      var _be = function(obj, _bf, _c0) {
        return (_c0 || []).concat(Array.prototype.slice.call(obj, _bf || 0));
      };
      var _c1 = function(obj, _c2, _c3) {
        var arr = _c3 || [];
        for (var x = _c2 || 0; x < obj.length; x++) {
          arr.push(obj[x]);
        }
        return arr;
      };
      dojo._toArray = d.isIE ? function(obj) {
        return ((obj.item) ? _c1 : _be).apply(this, arguments);
      } : _be;
      dojo.partial = function(_c4) {
        var arr = [null];
        return d.hitch.apply(d, arr.concat(d._toArray(arguments)));
      };
      var _c5 = d._extraNames,
        _c6 = _c5.length,
        _c7 = {};
      dojo.clone = function(o) {
        if (!o || typeof o != "object" || d.isFunction(o)) {
          return o;
        }
        if (o.nodeType && "cloneNode" in o) {
          return o.cloneNode(true);
        }
        if (o instanceof Date) {
          return new Date(o.getTime());
        }
        if (o instanceof RegExp) {
          return new RegExp(o);
        }
        var r, i, l, s, _c8;
        if (d.isArray(o)) {
          r = [];
          for (i = 0, l = o.length; i < l; ++i) {
            if (i in o) {
              r.push(d.clone(o[i]));
            }
          }
        } else {
          r = o.constructor ? new o.constructor() : {};
        }
        for (_c8 in o) {
          s = o[_c8];
          if (!(_c8 in r) || (r[_c8] !== s && (!(_c8 in _c7) || _c7[_c8] !== s))) {
            r[_c8] = d.clone(s);
          }
        }
        if (_c6) {
          for (i = 0; i < _c6; ++i) {
            _c8 = _c5[i];
            s = o[_c8];
            if (!(_c8 in r) || (r[_c8] !== s && (!(_c8 in _c7) || _c7[_c8] !== s))) {
              r[_c8] = s;
            }
          }
        }
        return r;
      };
      dojo.trim = String.prototype.trim ? function(str) {
        return str.trim();
      } : function(str) {
        return str.replace(/^\s\s*/, "").replace(/\s\s*$/, "");
      };
      var _c9 = /\{([^\}]+)\}/g;
      dojo.replace = function(_ca, map, _cb) {
        return _ca.replace(_cb || _c9, d.isFunction(map) ? map : function(_cc, k) {
          return d.getObject(k, false, map);
        });
      };
    })();
  }
  if (!dojo._hasResource["dojo._base.array"]) {
    dojo._hasResource["dojo._base.array"] = true;
    dojo.provide("dojo._base.array");
    (function() {
      var _cd = function(arr, obj, cb) {
        return [(typeof arr == "string") ? arr.split("") : arr, obj || dojo.global, (typeof cb == "string") ? new Function("item", "index", "array", cb) : cb];
      };
      var _ce = function(_cf, arr, _d0, _d1) {
        var _d2 = _cd(arr, _d1, _d0);
        arr = _d2[0];
        for (var i = 0, l = arr.length; i < l; ++i) {
          var _d3 = !!_d2[2].call(_d2[1], arr[i], i, arr);
          if (_cf ^ _d3) {
            return _d3;
          }
        }
        return _cf;
      };
      dojo.mixin(dojo, {
        indexOf: function(_d4, _d5, _d6, _d7) {
          var _d8 = 1,
            end = _d4.length || 0,
            i = 0;
          if (_d7) {
            i = end - 1;
            _d8 = end = -1;
          }
          if (_d6 != undefined) {
            i = _d6;
          }
          if ((_d7 && i > end) || i < end) {
            for (; i != end; i += _d8) {
              if (_d4[i] == _d5) {
                return i;
              }
            }
          }
          return -1;
        },
        lastIndexOf: function(_d9, _da, _db) {
          return dojo.indexOf(_d9, _da, _db, true);
        },
        forEach: function(arr, _dc, _dd) {
          if (!arr || !arr.length) {
            return;
          }
          var _de = _cd(arr, _dd, _dc);
          arr = _de[0];
          for (var i = 0, l = arr.length; i < l; ++i) {
            _de[2].call(_de[1], arr[i], i, arr);
          }
        },
        every: function(arr, _df, _e0) {
          return _ce(true, arr, _df, _e0);
        },
        some: function(arr, _e1, _e2) {
          return _ce(false, arr, _e1, _e2);
        },
        map: function(arr, _e3, _e4) {
          var _e5 = _cd(arr, _e4, _e3);
          arr = _e5[0];
          var _e6 = (arguments[3] ? (new arguments[3]()) : []);
          for (var i = 0, l = arr.length; i < l; ++i) {
            _e6.push(_e5[2].call(_e5[1], arr[i], i, arr));
          }
          return _e6;
        },
        filter: function(arr, _e7, _e8) {
          var _e9 = _cd(arr, _e8, _e7);
          arr = _e9[0];
          var _ea = [];
          for (var i = 0, l = arr.length; i < l; ++i) {
            if (_e9[2].call(_e9[1], arr[i], i, arr)) {
              _ea.push(arr[i]);
            }
          }
          return _ea;
        }
      });
    })();
  }
  if (!dojo._hasResource["dojo._base.declare"]) {
    dojo._hasResource["dojo._base.declare"] = true;
    dojo.provide("dojo._base.declare");
    (function() {
      var d = dojo,
        mix = d._mixin,
        op = Object.prototype,
        _eb = op.toString,
        _ec = new Function,
        _ed = 0,
        _ee = "constructor";

      function err(msg, cls) {
        throw new Error("declare" + (cls ? " " + cls : "") + ": " + msg);
      };

      function _ef(_f0, _f1) {
        var _f2 = [],
          _f3 = [{
            cls: 0,
            refs: []
          }],
          _f4 = {},
          _f5 = 1,
          l = _f0.length,
          i = 0,
          j, lin, _f6, top, _f7, rec, _f8, _f9;
        for (; i < l; ++i) {
          _f6 = _f0[i];
          if (!_f6) {
            err("mixin #" + i + " is unknown. Did you use dojo.require to pull it in?", _f1);
          } else {
            if (_eb.call(_f6) != "[object Function]") {
              err("mixin #" + i + " is not a callable constructor.", _f1);
            }
          }
          lin = _f6._meta ? _f6._meta.bases : [_f6];
          top = 0;
          for (j = lin.length - 1; j >= 0; --j) {
            _f7 = lin[j].prototype;
            if (!_f7.hasOwnProperty("declaredClass")) {
              _f7.declaredClass = "uniqName_" + (_ed++);
            }
            _f8 = _f7.declaredClass;
            if (!_f4.hasOwnProperty(_f8)) {
              _f4[_f8] = {
                count: 0,
                refs: [],
                cls: lin[j]
              };
              ++_f5;
            }
            rec = _f4[_f8];
            if (top && top !== rec) {
              rec.refs.push(top);
              ++top.count;
            }
            top = rec;
          }++top.count;
          _f3[0].refs.push(top);
        }
        while (_f3.length) {
          top = _f3.pop();
          _f2.push(top.cls);
          --_f5;
          while (_f9 = top.refs, _f9.length == 1) {
            top = _f9[0];
            if (!top || --top.count) {
              top = 0;
              break;
            }
            _f2.push(top.cls);
            --_f5;
          }
          if (top) {
            for (i = 0, l = _f9.length; i < l; ++i) {
              top = _f9[i];
              if (!--top.count) {
                _f3.push(top);
              }
            }
          }
        }
        if (_f5) {
          err("can't build consistent linearization", _f1);
        }
        _f6 = _f0[0];
        _f2[0] = _f6 ? _f6._meta && _f6 === _f2[_f2.length - _f6._meta.bases.length] ? _f6._meta.bases.length : 1 : 0;
        return _f2;
      };

      function _fa(_fb, a, f) {
        var _fc, _fd, _fe, _ff, meta, base, _100, opf, pos, _101 = this._inherited = this._inherited || {};
        if (typeof _fb == "string") {
          _fc = _fb;
          _fb = a;
          a = f;
        }
        f = 0;
        _ff = _fb.callee;
        _fc = _fc || _ff.nom;
        if (!_fc) {
          err("can't deduce a name to call inherited()", this.declaredClass);
        }
        meta = this.constructor._meta;
        _fe = meta.bases;
        pos = _101.p;
        if (_fc != _ee) {
          if (_101.c !== _ff) {
            pos = 0;
            base = _fe[0];
            meta = base._meta;
            if (meta.hidden[_fc] !== _ff) {
              _fd = meta.chains;
              if (_fd && typeof _fd[_fc] == "string") {
                err("calling chained method with inherited: " + _fc, this.declaredClass);
              }
              do {
                meta = base._meta;
                _100 = base.prototype;
                if (meta && (_100[_fc] === _ff && _100.hasOwnProperty(_fc) || meta.hidden[_fc] === _ff)) {
                  break;
                }
              } while (base = _fe[++pos]);
              pos = base ? pos : -1;
            }
          }
          base = _fe[++pos];
          if (base) {
            _100 = base.prototype;
            if (base._meta && _100.hasOwnProperty(_fc)) {
              f = _100[_fc];
            } else {
              opf = op[_fc];
              do {
                _100 = base.prototype;
                f = _100[_fc];
                if (f && (base._meta ? _100.hasOwnProperty(_fc) : f !== opf)) {
                  break;
                }
              } while (base = _fe[++pos]);
            }
          }
          f = base && f || op[_fc];
        } else {
          if (_101.c !== _ff) {
            pos = 0;
            meta = _fe[0]._meta;
            if (meta && meta.ctor !== _ff) {
              _fd = meta.chains;
              if (!_fd || _fd.constructor !== "manual") {
                err("calling chained constructor with inherited", this.declaredClass);
              }
              while (base = _fe[++pos]) {
                meta = base._meta;
                if (meta && meta.ctor === _ff) {
                  break;
                }
              }
              pos = base ? pos : -1;
            }
          }
          while (base = _fe[++pos]) {
            meta = base._meta;
            f = meta ? meta.ctor : base;
            if (f) {
              break;
            }
          }
          f = base && f;
        }
        _101.c = f;
        _101.p = pos;
        if (f) {
          return a === true ? f : f.apply(this, a || _fb);
        }
      };

      function _102(name, args) {
        if (typeof name == "string") {
          return this.inherited(name, args, true);
        }
        return this.inherited(name, true);
      };

      function _103(cls) {
        var _104 = this.constructor._meta.bases;
        for (var i = 0, l = _104.length; i < l; ++i) {
          if (_104[i] === cls) {
            return true;
          }
        }
        return this instanceof cls;
      };

      function _105(_106, _107) {
        var name, i = 0,
          l = d._extraNames.length;
        for (name in _107) {
          if (name != _ee && _107.hasOwnProperty(name)) {
            _106[name] = _107[name];
          }
        }
        for (; i < l; ++i) {
          name = d._extraNames[i];
          if (name != _ee && _107.hasOwnProperty(name)) {
            _106[name] = _107[name];
          }
        }
      };

      function _108(_109, _10a) {
        var name, t, i = 0,
          l = d._extraNames.length;
        for (name in _10a) {
          t = _10a[name];
          if ((t !== op[name] || !(name in op)) && name != _ee) {
            if (_eb.call(t) == "[object Function]") {
              t.nom = name;
            }
            _109[name] = t;
          }
        }
        for (; i < l; ++i) {
          name = d._extraNames[i];
          t = _10a[name];
          if ((t !== op[name] || !(name in op)) && name != _ee) {
            if (_eb.call(t) == "[object Function]") {
              t.nom = name;
            }
            _109[name] = t;
          }
        }
        return _109;
      };

      function _10b(_10c) {
        _108(this.prototype, _10c);
        return this;
      };

      function _10d(_10e, _10f) {
        return function() {
          var a = arguments,
            args = a,
            a0 = a[0],
            f, i, m, l = _10e.length,
            _110;
          if (!(this instanceof a.callee)) {
            return _111(a);
          }
          if (_10f && (a0 && a0.preamble || this.preamble)) {
            _110 = new Array(_10e.length);
            _110[0] = a;
            for (i = 0;;) {
              a0 = a[0];
              if (a0) {
                f = a0.preamble;
                if (f) {
                  a = f.apply(this, a) || a;
                }
              }
              f = _10e[i].prototype;
              f = f.hasOwnProperty("preamble") && f.preamble;
              if (f) {
                a = f.apply(this, a) || a;
              }
              if (++i == l) {
                break;
              }
              _110[i] = a;
            }
          }
          for (i = l - 1; i >= 0; --i) {
            f = _10e[i];
            m = f._meta;
            f = m ? m.ctor : f;
            if (f) {
              f.apply(this, _110 ? _110[i] : a);
            }
          }
          f = this.postscript;
          if (f) {
            f.apply(this, args);
          }
        };
      };

      function _112(ctor, _113) {
        return function() {
          var a = arguments,
            t = a,
            a0 = a[0],
            f;
          if (!(this instanceof a.callee)) {
            return _111(a);
          }
          if (_113) {
            if (a0) {
              f = a0.preamble;
              if (f) {
                t = f.apply(this, t) || t;
              }
            }
            f = this.preamble;
            if (f) {
              f.apply(this, t);
            }
          }
          if (ctor) {
            ctor.apply(this, a);
          }
          f = this.postscript;
          if (f) {
            f.apply(this, a);
          }
        };
      };

      function _114(_115) {
        return function() {
          var a = arguments,
            i = 0,
            f, m;
          if (!(this instanceof a.callee)) {
            return _111(a);
          }
          for (; f = _115[i]; ++i) {
            m = f._meta;
            f = m ? m.ctor : f;
            if (f) {
              f.apply(this, a);
              break;
            }
          }
          f = this.postscript;
          if (f) {
            f.apply(this, a);
          }
        };
      };

      function _116(name, _117, _118) {
        return function() {
          var b, m, f, i = 0,
            step = 1;
          if (_118) {
            i = _117.length - 1;
            step = -1;
          }
          for (; b = _117[i]; i += step) {
            m = b._meta;
            f = (m ? m.hidden : b.prototype)[name];
            if (f) {
              f.apply(this, arguments);
            }
          }
        };
      };

      function _119(ctor) {
        _ec.prototype = ctor.prototype;
        var t = new _ec;
        _ec.prototype = null;
        return t;
      };

      function _111(args) {
        var ctor = args.callee,
          t = _119(ctor);
        ctor.apply(t, args);
        return t;
      };
      d.declare = function(_11a, _11b, _11c) {
        if (typeof _11a != "string") {
          _11c = _11b;
          _11b = _11a;
          _11a = "";
        }
        _11c = _11c || {};
        var _11d, i, t, ctor, name, _11e, _11f, _120 = 1,
          _121 = _11b;
        if (_eb.call(_11b) == "[object Array]") {
          _11e = _ef(_11b, _11a);
          t = _11e[0];
          _120 = _11e.length - t;
          _11b = _11e[_120];
        } else {
          _11e = [0];
          if (_11b) {
            if (_eb.call(_11b) == "[object Function]") {
              t = _11b._meta;
              _11e = _11e.concat(t ? t.bases : _11b);
            } else {
              err("base class is not a callable constructor.", _11a);
            }
          } else {
            if (_11b !== null) {
              err("unknown base class. Did you use dojo.require to pull it in?", _11a);
            }
          }
        }
        if (_11b) {
          for (i = _120 - 1;; --i) {
            _11d = _119(_11b);
            if (!i) {
              break;
            }
            t = _11e[i];
            (t._meta ? _105 : mix)(_11d, t.prototype);
            ctor = new Function;
            ctor.superclass = _11b;
            ctor.prototype = _11d;
            _11b = _11d.constructor = ctor;
          }
        } else {
          _11d = {};
        }
        _108(_11d, _11c);
        t = _11c.constructor;
        if (t !== op.constructor) {
          t.nom = _ee;
          _11d.constructor = t;
        }
        for (i = _120 - 1; i; --i) {
          t = _11e[i]._meta;
          if (t && t.chains) {
            _11f = mix(_11f || {}, t.chains);
          }
        }
        if (_11d["-chains-"]) {
          _11f = mix(_11f || {}, _11d["-chains-"]);
        }
        t = !_11f || !_11f.hasOwnProperty(_ee);
        _11e[0] = ctor = (_11f && _11f.constructor === "manual") ? _114(_11e) : (_11e.length == 1 ? _112(_11c.constructor, t) : _10d(_11e, t));
        ctor._meta = {
          bases: _11e,
          hidden: _11c,
          chains: _11f,
          parents: _121,
          ctor: _11c.constructor
        };
        ctor.superclass = _11b && _11b.prototype;
        ctor.extend = _10b;
        ctor.prototype = _11d;
        _11d.constructor = ctor;
        _11d.getInherited = _102;
        _11d.inherited = _fa;
        _11d.isInstanceOf = _103;
        if (_11a) {
          _11d.declaredClass = _11a;
          d.setObject(_11a, ctor);
        }
        if (_11f) {
          for (name in _11f) {
            if (_11d[name] && typeof _11f[name] == "string" && name != _ee) {
              t = _11d[name] = _116(name, _11e, _11f[name] === "after");
              t.nom = name;
            }
          }
        }
        return ctor;
      };
      d.safeMixin = _108;
    })();
  }
  if (!dojo._hasResource["dojo._base.connect"]) {
    dojo._hasResource["dojo._base.connect"] = true;
    dojo.provide("dojo._base.connect");
    dojo._listener = {
      getDispatcher: function() {
        return function() {
          var ap = Array.prototype,
            c = arguments.callee,
            ls = c._listeners,
            t = c.target,
            r = t && t.apply(this, arguments),
            i, lls = [].concat(ls);
          for (i in lls) {
            if (!(i in ap)) {
              lls[i].apply(this, arguments);
            }
          }
          return r;
        };
      },
      add: function(_122, _123, _124) {
        _122 = _122 || dojo.global;
        var f = _122[_123];
        if (!f || !f._listeners) {
          var d = dojo._listener.getDispatcher();
          d.target = f;
          d._listeners = [];
          f = _122[_123] = d;
        }
        return f._listeners.push(_124);
      },
      remove: function(_125, _126, _127) {
        var f = (_125 || dojo.global)[_126];
        if (f && f._listeners && _127--) {
          delete f._listeners[_127];
        }
      }
    };
    dojo.connect = function(obj, _128, _129, _12a, _12b) {
      var a = arguments,
        args = [],
        i = 0;
      args.push(dojo.isString(a[0]) ? null : a[i++], a[i++]);
      var a1 = a[i + 1];
      args.push(dojo.isString(a1) || dojo.isFunction(a1) ? a[i++] : null, a[i++]);
      for (var l = a.length; i < l; i++) {
        args.push(a[i]);
      }
      return dojo._connect.apply(this, args);
    };
    dojo._connect = function(obj, _12c, _12d, _12e) {
      var l = dojo._listener,
        h = l.add(obj, _12c, dojo.hitch(_12d, _12e));
      return [obj, _12c, h, l];
    };
    dojo.disconnect = function(_12f) {
      if (_12f && _12f[0] !== undefined) {
        dojo._disconnect.apply(this, _12f);
        delete _12f[0];
      }
    };
    dojo._disconnect = function(obj, _130, _131, _132) {
      _132.remove(obj, _130, _131);
    };
    dojo._topics = {};
    dojo.subscribe = function(_133, _134, _135) {
      return [_133, dojo._listener.add(dojo._topics, _133, dojo.hitch(_134, _135))];
    };
    dojo.unsubscribe = function(_136) {
      if (_136) {
        dojo._listener.remove(dojo._topics, _136[0], _136[1]);
      }
    };
    dojo.publish = function(_137, args) {
      var f = dojo._topics[_137];
      if (f) {
        f.apply(this, args || []);
      }
    };
    dojo.connectPublisher = function(_138, obj, _139) {
      var pf = function() {
        dojo.publish(_138, arguments);
      };
      return _139 ? dojo.connect(obj, _139, pf) : dojo.connect(obj, pf);
    };
  }
  if (!dojo._hasResource["dojo._base.Deferred"]) {
    dojo._hasResource["dojo._base.Deferred"] = true;
    dojo.provide("dojo._base.Deferred");
    (function() {
      var _13a = function() {};
      var _13b = Object.freeze || function() {};
      dojo.Deferred = function(_13c) {
        var _13d, _13e, _13f, head, _140;
        var _141 = (this.promise = {});

        function _142(_143) {
          if (_13e) {
            throw new Error("This deferred has already been resolved");
          }
          _13d = _143;
          _13e = true;
          _144();
        };

        function _144() {
          var _145;
          while (!_145 && _140) {
            var _146 = _140;
            _140 = _140.next;
            if ((_145 = (_146.progress == _13a))) {
              _13e = false;
            }
            var func = (_13f ? _146.error : _146.resolved);
            if (func) {
              try {
                var _147 = func(_13d);
                if (_147 && typeof _147.then === "function") {
                  _147.then(dojo.hitch(_146.deferred, "resolve"), dojo.hitch(_146.deferred, "reject"));
                  continue;
                }
                var _148 = _145 && _147 === undefined;
                if (_145 && !_148) {
                  _13f = _147 instanceof Error;
                }
                _146.deferred[_148 && _13f ? "reject" : "resolve"](_148 ? _13d : _147);
              } catch (e) {
                _146.deferred.reject(e);
              }
            } else {
              if (_13f) {
                _146.deferred.reject(_13d);
              } else {
                _146.deferred.resolve(_13d);
              }
            }
          }
        };
        this.resolve = this.callback = function(_149) {
          this.fired = 0;
          this.results = [_149, null];
          _142(_149);
        };
        this.reject = this.errback = function(_14a) {
          _13f = true;
          this.fired = 1;
          _142(_14a);
          this.results = [null, _14a];
          if (!_14a || _14a.log !== false) {
            (dojo.config.deferredOnError || function(x) {})(_14a);
          }
        };
        this.progress = function(_14b) {
          var _14c = _140;
          while (_14c) {
            var _14d = _14c.progress;
            _14d && _14d(_14b);
            _14c = _14c.next;
          }
        };
        this.addCallbacks = function(_14e, _14f) {
          this.then(_14e, _14f, _13a);
          return this;
        };
        this.then = _141.then = function(_150, _151, _152) {
          var _153 = _152 == _13a ? this : new dojo.Deferred(_141.cancel);
          var _154 = {
            resolved: _150,
            error: _151,
            progress: _152,
            deferred: _153
          };
          if (_140) {
            head = head.next = _154;
          } else {
            _140 = head = _154;
          }
          if (_13e) {
            _144();
          }
          return _153.promise;
        };
        var _155 = this;
        this.cancel = _141.cancel = function() {
          if (!_13e) {
            var _156 = _13c && _13c(_155);
            if (!_13e) {
              if (!(_156 instanceof Error)) {
                _156 = new Error(_156);
              }
              _156.log = false;
              _155.reject(_156);
            }
          }
        };
        _13b(_141);
      };
      dojo.extend(dojo.Deferred, {
        addCallback: function(_157) {
          return this.addCallbacks(dojo.hitch.apply(dojo, arguments));
        },
        addErrback: function(_158) {
          return this.addCallbacks(null, dojo.hitch.apply(dojo, arguments));
        },
        addBoth: function(_159) {
          var _15a = dojo.hitch.apply(dojo, arguments);
          return this.addCallbacks(_15a, _15a);
        },
        fired: -1
      });
    })();
    dojo.when = function(_15b, _15c, _15d, _15e) {
      if (_15b && typeof _15b.then === "function") {
        return _15b.then(_15c, _15d, _15e);
      }
      return _15c(_15b);
    };
  }
  if (!dojo._hasResource["dojo._base.json"]) {
    dojo._hasResource["dojo._base.json"] = true;
    dojo.provide("dojo._base.json");
    dojo.fromJson = function(json) {
      return eval("(" + json + ")");
    };
    dojo._escapeString = function(str) {
      return ("\"" + str.replace(/(["\\])/g, "\\$1") + "\"").replace(/[\f]/g, "\\f").replace(/[\b]/g, "\\b").replace(/[\n]/g, "\\n").replace(/[\t]/g, "\\t").replace(/[\r]/g, "\\r");
    };
    dojo.toJsonIndentStr = "\t";
    dojo.toJson = function(it, _15f, _160) {
      if (it === undefined) {
        return "undefined";
      }
      var _161 = typeof it;
      if (_161 == "number" || _161 == "boolean") {
        return it + "";
      }
      if (it === null) {
        return "null";
      }
      if (dojo.isString(it)) {
        return dojo._escapeString(it);
      }
      var _162 = arguments.callee;
      var _163;
      _160 = _160 || "";
      var _164 = _15f ? _160 + dojo.toJsonIndentStr : "";
      var tf = it.__json__ || it.json;
      if (dojo.isFunction(tf)) {
        _163 = tf.call(it);
        if (it !== _163) {
          return _162(_163, _15f, _164);
        }
      }
      if (it.nodeType && it.cloneNode) {
        throw new Error("Can't serialize DOM nodes");
      }
      var sep = _15f ? " " : "";
      var _165 = _15f ? "\n" : "";
      if (dojo.isArray(it)) {
        var res = dojo.map(it, function(obj) {
          var val = _162(obj, _15f, _164);
          if (typeof val != "string") {
            val = "undefined";
          }
          return _165 + _164 + val;
        });
        return "[" + res.join("," + sep) + _165 + _160 + "]";
      }
      if (_161 == "function") {
        return null;
      }
      var _166 = [],
        key;
      for (key in it) {
        var _167, val;
        if (typeof key == "number") {
          _167 = "\"" + key + "\"";
        } else {
          if (typeof key == "string") {
            _167 = dojo._escapeString(key);
          } else {
            continue;
          }
        }
        val = _162(it[key], _15f, _164);
        if (typeof val != "string") {
          continue;
        }
        _166.push(_165 + _164 + _167 + ":" + sep + val);
      }
      return "{" + _166.join("," + sep) + _165 + _160 + "}";
    };
  }
  if (!dojo._hasResource["dojo._base.Color"]) {
    dojo._hasResource["dojo._base.Color"] = true;
    dojo.provide("dojo._base.Color");
    (function() {
      var d = dojo;
      dojo.Color = function(_168) {
        if (_168) {
          this.setColor(_168);
        }
      };
      dojo.Color.named = {
        black: [0, 0, 0],
        silver: [192, 192, 192],
        gray: [128, 128, 128],
        white: [255, 255, 255],
        maroon: [128, 0, 0],
        red: [255, 0, 0],
        purple: [128, 0, 128],
        fuchsia: [255, 0, 255],
        green: [0, 128, 0],
        lime: [0, 255, 0],
        olive: [128, 128, 0],
        yellow: [255, 255, 0],
        navy: [0, 0, 128],
        blue: [0, 0, 255],
        teal: [0, 128, 128],
        aqua: [0, 255, 255],
        transparent: d.config.transparentColor || [255, 255, 255]
      };
      dojo.extend(dojo.Color, {
        r: 255,
        g: 255,
        b: 255,
        a: 1,
        _set: function(r, g, b, a) {
          var t = this;
          t.r = r;
          t.g = g;
          t.b = b;
          t.a = a;
        },
        setColor: function(_169) {
          if (d.isString(_169)) {
            d.colorFromString(_169, this);
          } else {
            if (d.isArray(_169)) {
              d.colorFromArray(_169, this);
            } else {
              this._set(_169.r, _169.g, _169.b, _169.a);
              if (!(_169 instanceof d.Color)) {
                this.sanitize();
              }
            }
          }
          return this;
        },
        sanitize: function() {
          return this;
        },
        toRgb: function() {
          var t = this;
          return [t.r, t.g, t.b];
        },
        toRgba: function() {
          var t = this;
          return [t.r, t.g, t.b, t.a];
        },
        toHex: function() {
          var arr = d.map(["r", "g", "b"], function(x) {
            var s = this[x].toString(16);
            return s.length < 2 ? "0" + s : s;
          }, this);
          return "#" + arr.join("");
        },
        toCss: function(_16a) {
          var t = this,
            rgb = t.r + ", " + t.g + ", " + t.b;
          return (_16a ? "rgba(" + rgb + ", " + t.a : "rgb(" + rgb) + ")";
        },
        toString: function() {
          return this.toCss(true);
        }
      });
      dojo.blendColors = function(_16b, end, _16c, obj) {
        var t = obj || new d.Color();
        d.forEach(["r", "g", "b", "a"], function(x) {
          t[x] = _16b[x] + (end[x] - _16b[x]) * _16c;
          if (x != "a") {
            t[x] = Math.round(t[x]);
          }
        });
        return t.sanitize();
      };
      dojo.colorFromRgb = function(_16d, obj) {
        var m = _16d.toLowerCase().match(/^rgba?\(([\s\.,0-9]+)\)/);
        return m && dojo.colorFromArray(m[1].split(/\s*,\s*/), obj);
      };
      dojo.colorFromHex = function(_16e, obj) {
        var t = obj || new d.Color(),
          bits = (_16e.length == 4) ? 4 : 8,
          mask = (1 << bits) - 1;
        _16e = Number("0x" + _16e.substr(1));
        if (isNaN(_16e)) {
          return null;
        }
        d.forEach(["b", "g", "r"], function(x) {
          var c = _16e & mask;
          _16e >>= bits;
          t[x] = bits == 4 ? 17 * c : c;
        });
        t.a = 1;
        return t;
      };
      dojo.colorFromArray = function(a, obj) {
        var t = obj || new d.Color();
        t._set(Number(a[0]), Number(a[1]), Number(a[2]), Number(a[3]));
        if (isNaN(t.a)) {
          t.a = 1;
        }
        return t.sanitize();
      };
      dojo.colorFromString = function(str, obj) {
        var a = d.Color.named[str];
        return a && d.colorFromArray(a, obj) || d.colorFromRgb(str, obj) || d.colorFromHex(str, obj);
      };
    })();
  }
  if (!dojo._hasResource["dojo._base.window"]) {
    dojo._hasResource["dojo._base.window"] = true;
    dojo.provide("dojo._base.window");
    dojo.doc = window["document"] || null;
    dojo.body = function() {
      return dojo.doc.body || dojo.doc.getElementsByTagName("body")[0];
    };
    dojo.setContext = function(_16f, _170) {
      dojo.global = _16f;
      dojo.doc = _170;
    };
    dojo.withGlobal = function(_171, _172, _173, _174) {
      var _175 = dojo.global;
      try {
        dojo.global = _171;
        return dojo.withDoc.call(null, _171.document, _172, _173, _174);
      } finally {
        dojo.global = _175;
      }
    };
    dojo.withDoc = function(_176, _177, _178, _179) {
      var _17a = dojo.doc,
        _17b = dojo._bodyLtr,
        oldQ = dojo.isQuirks;
      try {
        dojo.doc = _176;
        delete dojo._bodyLtr;
        dojo.isQuirks = dojo.doc.compatMode == "BackCompat";
        if (_178 && typeof _177 == "string") {
          _177 = _178[_177];
        }
        return _177.apply(_178, _179 || []);
      } finally {
        dojo.doc = _17a;
        delete dojo._bodyLtr;
        if (_17b !== undefined) {
          dojo._bodyLtr = _17b;
        }
        dojo.isQuirks = oldQ;
      }
    };
  }
  if (!dojo._hasResource["dojo._base.event"]) {
    dojo._hasResource["dojo._base.event"] = true;
    dojo.provide("dojo._base.event");
    (function() {
      var del = (dojo._event_listener = {
        add: function(node, name, fp) {
          if (!node) {
            return;
          }
          name = del._normalizeEventName(name);
          fp = del._fixCallback(name, fp);
          if (!dojo.isIE && (name == "mouseenter" || name == "mouseleave")) {
            var ofp = fp;
            name = (name == "mouseenter") ? "mouseover" : "mouseout";
            fp = function(e) {
              if (!dojo.isDescendant(e.relatedTarget, node)) {
                return ofp.call(this, e);
              }
            };
          }
          node.addEventListener(name, fp, false);
          return fp;
        },
        remove: function(node, _17c, _17d) {
          if (node) {
            _17c = del._normalizeEventName(_17c);
            if (!dojo.isIE && (_17c == "mouseenter" || _17c == "mouseleave")) {
              _17c = (_17c == "mouseenter") ? "mouseover" : "mouseout";
            }
            node.removeEventListener(_17c, _17d, false);
          }
        },
        _normalizeEventName: function(name) {
          return name.slice(0, 2) == "on" ? name.slice(2) : name;
        },
        _fixCallback: function(name, fp) {
          return name != "keypress" ? fp : function(e) {
            return fp.call(this, del._fixEvent(e, this));
          };
        },
        _fixEvent: function(evt, _17e) {
          switch (evt.type) {
            case "keypress":
              del._setKeyChar(evt);
              break;
          }
          return evt;
        },
        _setKeyChar: function(evt) {
          evt.keyChar = evt.charCode >= 32 ? String.fromCharCode(evt.charCode) : "";
          evt.charOrCode = evt.keyChar || evt.keyCode;
        },
        _punctMap: {
          106: 42,
          111: 47,
          186: 59,
          187: 43,
          188: 44,
          189: 45,
          190: 46,
          191: 47,
          192: 96,
          219: 91,
          220: 92,
          221: 93,
          222: 39
        }
      });
      dojo.fixEvent = function(evt, _17f) {
        return del._fixEvent(evt, _17f);
      };
      dojo.stopEvent = function(evt) {
        evt.preventDefault();
        evt.stopPropagation();
      };
      var _180 = dojo._listener;
      dojo._connect = function(obj, _181, _182, _183, _184) {
        var _185 = obj && (obj.nodeType || obj.attachEvent || obj.addEventListener);
        var lid = _185 ? (_184 ? 2 : 1) : 0,
          l = [dojo._listener, del, _180][lid];
        var h = l.add(obj, _181, dojo.hitch(_182, _183));
        return [obj, _181, h, lid];
      };
      dojo._disconnect = function(obj, _186, _187, _188) {
        ([dojo._listener, del, _180][_188]).remove(obj, _186, _187);
      };
      dojo.keys = {
        BACKSPACE: 8,
        TAB: 9,
        CLEAR: 12,
        ENTER: 13,
        SHIFT: 16,
        CTRL: 17,
        ALT: 18,
        META: dojo.isSafari ? 91 : 224,
        PAUSE: 19,
        CAPS_LOCK: 20,
        ESCAPE: 27,
        SPACE: 32,
        PAGE_UP: 33,
        PAGE_DOWN: 34,
        END: 35,
        HOME: 36,
        LEFT_ARROW: 37,
        UP_ARROW: 38,
        RIGHT_ARROW: 39,
        DOWN_ARROW: 40,
        INSERT: 45,
        DELETE: 46,
        HELP: 47,
        LEFT_WINDOW: 91,
        RIGHT_WINDOW: 92,
        SELECT: 93,
        NUMPAD_0: 96,
        NUMPAD_1: 97,
        NUMPAD_2: 98,
        NUMPAD_3: 99,
        NUMPAD_4: 100,
        NUMPAD_5: 101,
        NUMPAD_6: 102,
        NUMPAD_7: 103,
        NUMPAD_8: 104,
        NUMPAD_9: 105,
        NUMPAD_MULTIPLY: 106,
        NUMPAD_PLUS: 107,
        NUMPAD_ENTER: 108,
        NUMPAD_MINUS: 109,
        NUMPAD_PERIOD: 110,
        NUMPAD_DIVIDE: 111,
        F1: 112,
        F2: 113,
        F3: 114,
        F4: 115,
        F5: 116,
        F6: 117,
        F7: 118,
        F8: 119,
        F9: 120,
        F10: 121,
        F11: 122,
        F12: 123,
        F13: 124,
        F14: 125,
        F15: 126,
        NUM_LOCK: 144,
        SCROLL_LOCK: 145,
        copyKey: dojo.isMac && !dojo.isAIR ? (dojo.isSafari ? 91 : 224) : 17
      };
      var _189 = dojo.isMac ? "metaKey" : "ctrlKey";
      dojo.isCopyKey = function(e) {
        return e[_189];
      };
      if (dojo.isIE < 9 || (dojo.isIE && dojo.isQuirks)) {
        dojo.mouseButtons = {
          LEFT: 1,
          MIDDLE: 4,
          RIGHT: 2,
          isButton: function(e, _18a) {
            return e.button & _18a;
          },
          isLeft: function(e) {
            return e.button & 1;
          },
          isMiddle: function(e) {
            return e.button & 4;
          },
          isRight: function(e) {
            return e.button & 2;
          }
        };
      } else {
        dojo.mouseButtons = {
          LEFT: 0,
          MIDDLE: 1,
          RIGHT: 2,
          isButton: function(e, _18b) {
            return e.button == _18b;
          },
          isLeft: function(e) {
            return e.button == 0;
          },
          isMiddle: function(e) {
            return e.button == 1;
          },
          isRight: function(e) {
            return e.button == 2;
          }
        };
      }
      if (dojo.isIE) {
        var _18c = function(e, code) {
          try {
            return (e.keyCode = code);
          } catch (e) {
            return 0;
          }
        };
        var iel = dojo._listener;
        var _18d = (dojo._ieListenersName = "_" + dojo._scopeName + "_listeners");
        if (!dojo.config._allow_leaks) {
          _180 = iel = dojo._ie_listener = {
            handlers: [],
            add: function(_18e, _18f, _190) {
              _18e = _18e || dojo.global;
              var f = _18e[_18f];
              if (!f || !f[_18d]) {
                var d = dojo._getIeDispatcher();
                d.target = f && (ieh.push(f) - 1);
                d[_18d] = [];
                f = _18e[_18f] = d;
              }
              return f[_18d].push(ieh.push(_190) - 1);
            },
            remove: function(_191, _192, _193) {
              var f = (_191 || dojo.global)[_192],
                l = f && f[_18d];
              if (f && l && _193--) {
                delete ieh[l[_193]];
                delete l[_193];
              }
            }
          };
          var ieh = iel.handlers;
        }
        dojo.mixin(del, {
          add: function(node, _194, fp) {
            if (!node) {
              return;
            }
            _194 = del._normalizeEventName(_194);
            if (_194 == "onkeypress") {
              var kd = node.onkeydown;
              if (!kd || !kd[_18d] || !kd._stealthKeydownHandle) {
                var h = del.add(node, "onkeydown", del._stealthKeyDown);
                kd = node.onkeydown;
                kd._stealthKeydownHandle = h;
                kd._stealthKeydownRefs = 1;
              } else {
                kd._stealthKeydownRefs++;
              }
            }
            return iel.add(node, _194, del._fixCallback(fp));
          },
          remove: function(node, _195, _196) {
            _195 = del._normalizeEventName(_195);
            iel.remove(node, _195, _196);
            if (_195 == "onkeypress") {
              var kd = node.onkeydown;
              if (--kd._stealthKeydownRefs <= 0) {
                iel.remove(node, "onkeydown", kd._stealthKeydownHandle);
                delete kd._stealthKeydownHandle;
              }
            }
          },
          _normalizeEventName: function(_197) {
            return _197.slice(0, 2) != "on" ? "on" + _197 : _197;
          },
          _nop: function() {},
          _fixEvent: function(evt, _198) {
            if (!evt) {
              var w = _198 && (_198.ownerDocument || _198.document || _198).parentWindow || window;
              evt = w.event;
            }
            if (!evt) {
              return (evt);
            }
            evt.target = evt.srcElement;
            evt.currentTarget = (_198 || evt.srcElement);
            evt.layerX = evt.offsetX;
            evt.layerY = evt.offsetY;
            var se = evt.srcElement,
              doc = (se && se.ownerDocument) || document;
            var _199 = ((dojo.isIE < 6) || (doc["compatMode"] == "BackCompat")) ? doc.body : doc.documentElement;
            var _19a = dojo._getIeDocumentElementOffset();
            evt.pageX = evt.clientX + dojo._fixIeBiDiScrollLeft(_199.scrollLeft || 0) - _19a.x;
            evt.pageY = evt.clientY + (_199.scrollTop || 0) - _19a.y;
            if (evt.type == "mouseover") {
              evt.relatedTarget = evt.fromElement;
            }
            if (evt.type == "mouseout") {
              evt.relatedTarget = evt.toElement;
            }
            if (dojo.isIE < 9 || dojo.isQuirks) {
              evt.stopPropagation = del._stopPropagation;
              evt.preventDefault = del._preventDefault;
            }
            return del._fixKeys(evt);
          },
          _fixKeys: function(evt) {
            switch (evt.type) {
              case "keypress":
                var c = ("charCode" in evt ? evt.charCode : evt.keyCode);
                if (c == 10) {
                  c = 0;
                  evt.keyCode = 13;
                } else {
                  if (c == 13 || c == 27) {
                    c = 0;
                  } else {
                    if (c == 3) {
                      c = 99;
                    }
                  }
                }
                evt.charCode = c;
                del._setKeyChar(evt);
                break;
            }
            return evt;
          },
          _stealthKeyDown: function(evt) {
            var kp = evt.currentTarget.onkeypress;
            if (!kp || !kp[_18d]) {
              return;
            }
            var k = evt.keyCode;
            var _19b = (k != 13 || (dojo.isIE >= 9 && !dojo.isQuirks)) && k != 32 && k != 27 && (k < 48 || k > 90) && (k < 96 || k > 111) && (k < 186 || k > 192) && (k < 219 || k > 222);
            if (_19b || evt.ctrlKey) {
              var c = _19b ? 0 : k;
              if (evt.ctrlKey) {
                if (k == 3 || k == 13) {
                  return;
                } else {
                  if (c > 95 && c < 106) {
                    c -= 48;
                  } else {
                    if ((!evt.shiftKey) && (c >= 65 && c <= 90)) {
                      c += 32;
                    } else {
                      c = del._punctMap[c] || c;
                    }
                  }
                }
              }
              var faux = del._synthesizeEvent(evt, {
                type: "keypress",
                faux: true,
                charCode: c
              });
              kp.call(evt.currentTarget, faux);
              if (dojo.isIE < 9 || (dojo.isIE && dojo.isQuirks)) {
                evt.cancelBubble = faux.cancelBubble;
              }
              evt.returnValue = faux.returnValue;
              _18c(evt, faux.keyCode);
            }
          },
          _stopPropagation: function() {
            this.cancelBubble = true;
          },
          _preventDefault: function() {
            this.bubbledKeyCode = this.keyCode;
            if (this.ctrlKey) {
              _18c(this, 0);
            }
            this.returnValue = false;
          }
        });
        dojo.stopEvent = (dojo.isIE < 9 || dojo.isQuirks) ? function(evt) {
          evt = evt || window.event;
          del._stopPropagation.call(evt);
          del._preventDefault.call(evt);
        } : dojo.stopEvent;
      }
      del._synthesizeEvent = function(evt, _19c) {
        var faux = dojo.mixin({}, evt, _19c);
        del._setKeyChar(faux);
        faux.preventDefault = function() {
          evt.preventDefault();
        };
        faux.stopPropagation = function() {
          evt.stopPropagation();
        };
        return faux;
      };
      if (dojo.isOpera) {
        dojo.mixin(del, {
          _fixEvent: function(evt, _19d) {
            switch (evt.type) {
              case "keypress":
                var c = evt.which;
                if (c == 3) {
                  c = 99;
                }
                c = c < 41 && !evt.shiftKey ? 0 : c;
                if (evt.ctrlKey && !evt.shiftKey && c >= 65 && c <= 90) {
                  c += 32;
                }
                return del._synthesizeEvent(evt, {
                  charCode: c
                });
            }
            return evt;
          }
        });
      }
      if (dojo.isWebKit) {
        del._add = del.add;
        del._remove = del.remove;
        dojo.mixin(del, {
          add: function(node, _19e, fp) {
            if (!node) {
              return;
            }
            var _19f = del._add(node, _19e, fp);
            if (del._normalizeEventName(_19e) == "keypress") {
              _19f._stealthKeyDownHandle = del._add(node, "keydown", function(evt) {
                var k = evt.keyCode;
                var _1a0 = k != 13 && k != 32 && (k < 48 || k > 90) && (k < 96 || k > 111) && (k < 186 || k > 192) && (k < 219 || k > 222);
                if (_1a0 || evt.ctrlKey) {
                  var c = _1a0 ? 0 : k;
                  if (evt.ctrlKey) {
                    if (k == 3 || k == 13) {
                      return;
                    } else {
                      if (c > 95 && c < 106) {
                        c -= 48;
                      } else {
                        if (!evt.shiftKey && c >= 65 && c <= 90) {
                          c += 32;
                        } else {
                          c = del._punctMap[c] || c;
                        }
                      }
                    }
                  }
                  var faux = del._synthesizeEvent(evt, {
                    type: "keypress",
                    faux: true,
                    charCode: c
                  });
                  fp.call(evt.currentTarget, faux);
                }
              });
            }
            return _19f;
          },
          remove: function(node, _1a1, _1a2) {
            if (node) {
              if (_1a2._stealthKeyDownHandle) {
                del._remove(node, "keydown", _1a2._stealthKeyDownHandle);
              }
              del._remove(node, _1a1, _1a2);
            }
          },
          _fixEvent: function(evt, _1a3) {
            switch (evt.type) {
              case "keypress":
                if (evt.faux) {
                  return evt;
                }
                var c = evt.charCode;
                c = c >= 32 ? c : 0;
                return del._synthesizeEvent(evt, {
                  charCode: c,
                  faux: true
                });
            }
            return evt;
          }
        });
      }
    })();
    if (dojo.isIE) {
      dojo._ieDispatcher = function(args, _1a4) {
        var ap = Array.prototype,
          h = dojo._ie_listener.handlers,
          c = args.callee,
          ls = c[dojo._ieListenersName],
          t = h[c.target];
        var r = t && t.apply(_1a4, args);
        var lls = [].concat(ls);
        for (var i in lls) {
          var f = h[lls[i]];
          if (!(i in ap) && f) {
            f.apply(_1a4, args);
          }
        }
        return r;
      };
      dojo._getIeDispatcher = function() {
        return new Function(dojo._scopeName + "._ieDispatcher(arguments, this)");
      };
      dojo._event_listener._fixCallback = function(fp) {
        var f = dojo._event_listener._fixEvent;
        return function(e) {
          return fp.call(this, f(e, this));
        };
      };
    }
  }
  if (!dojo._hasResource["dojo._base.html"]) {
    dojo._hasResource["dojo._base.html"] = true;
    dojo.provide("dojo._base.html");
    try {
      document.execCommand("BackgroundImageCache", false, true);
    } catch (e) {}
    if (dojo.isIE) {
      dojo.byId = function(id, doc) {
        if (typeof id != "string") {
          return id;
        }
        var _1a5 = doc || dojo.doc,
          te = _1a5.getElementById(id);
        if (te && (te.attributes.id.value == id || te.id == id)) {
          return te;
        } else {
          var eles = _1a5.all[id];
          if (!eles || eles.nodeName) {
            eles = [eles];
          }
          var i = 0;
          while ((te = eles[i++])) {
            if ((te.attributes && te.attributes.id && te.attributes.id.value == id) || te.id == id) {
              return te;
            }
          }
        }
      };
    } else {
      dojo.byId = function(id, doc) {
        return ((typeof id == "string") ? (doc || dojo.doc).getElementById(id) : id) || null;
      };
    }(function() {
      var d = dojo;
      var byId = d.byId;
      var _1a6 = null,
        _1a7;
      d.addOnWindowUnload(function() {
        _1a6 = null;
      });
      dojo._destroyElement = dojo.destroy = function(node) {
        node = byId(node);
        try {
          var doc = node.ownerDocument;
          if (!_1a6 || _1a7 != doc) {
            _1a6 = doc.createElement("div");
            _1a7 = doc;
          }
          _1a6.appendChild(node.parentNode ? node.parentNode.removeChild(node) : node);
          _1a6.innerHTML = "";
        } catch (e) {}
      };
      dojo.isDescendant = function(node, _1a8) {
        try {
          node = byId(node);
          _1a8 = byId(_1a8);
          while (node) {
            if (node == _1a8) {
              return true;
            }
            node = node.parentNode;
          }
        } catch (e) {}
        return false;
      };
      dojo.setSelectable = function(node, _1a9) {
        node = byId(node);
        if (d.isMozilla) {
          node.style.MozUserSelect = _1a9 ? "" : "none";
        } else {
          if (d.isKhtml || d.isWebKit) {
            node.style.KhtmlUserSelect = _1a9 ? "auto" : "none";
          } else {
            if (d.isIE) {
              var v = (node.unselectable = _1a9 ? "" : "on");
              d.query("*", node).forEach("item.unselectable = '" + v + "'");
            }
          }
        }
      };
      var _1aa = function(node, ref) {
        var _1ab = ref.parentNode;
        if (_1ab) {
          _1ab.insertBefore(node, ref);
        }
      };
      var _1ac = function(node, ref) {
        var _1ad = ref.parentNode;
        if (_1ad) {
          if (_1ad.lastChild == ref) {
            _1ad.appendChild(node);
          } else {
            _1ad.insertBefore(node, ref.nextSibling);
          }
        }
      };
      dojo.place = function(node, _1ae, _1af) {
        _1ae = byId(_1ae);
        if (typeof node == "string") {
          node = /^\s*</.test(node) ? d._toDom(node, _1ae.ownerDocument) : byId(node);
        }
        if (typeof _1af == "number") {
          var cn = _1ae.childNodes;
          if (!cn.length || cn.length <= _1af) {
            _1ae.appendChild(node);
          } else {
            _1aa(node, cn[_1af < 0 ? 0 : _1af]);
          }
        } else {
          switch (_1af) {
            case "before":
              _1aa(node, _1ae);
              break;
            case "after":
              _1ac(node, _1ae);
              break;
            case "replace":
              _1ae.parentNode.replaceChild(node, _1ae);
              break;
            case "only":
              d.empty(_1ae);
              _1ae.appendChild(node);
              break;
            case "first":
              if (_1ae.firstChild) {
                _1aa(node, _1ae.firstChild);
                break;
              }
              default:
                _1ae.appendChild(node);
          }
        }
        return node;
      };
      dojo.boxModel = "content-box";
      if (d.isIE) {
        d.boxModel = document.compatMode == "BackCompat" ? "border-box" : "content-box";
      }
      var gcs;
      if (d.isWebKit) {
        gcs = function(node) {
          var s;
          if (node.nodeType == 1) {
            var dv = node.ownerDocument.defaultView;
            s = dv.getComputedStyle(node, null);
            if (!s && node.style) {
              node.style.display = "";
              s = dv.getComputedStyle(node, null);
            }
          }
          return s || {};
        };
      } else {
        if (d.isIE) {
          gcs = function(node) {
            return node.nodeType == 1 ? node.currentStyle : {};
          };
        } else {
          gcs = function(node) {
            return node.nodeType == 1 ? node.ownerDocument.defaultView.getComputedStyle(node, null) : {};
          };
        }
      }
      dojo.getComputedStyle = gcs;
      if (!d.isIE) {
        d._toPixelValue = function(_1b0, _1b1) {
          return parseFloat(_1b1) || 0;
        };
      } else {
        d._toPixelValue = function(_1b2, _1b3) {
          if (!_1b3) {
            return 0;
          }
          if (_1b3 == "medium") {
            return 4;
          }
          if (_1b3.slice && _1b3.slice(-2) == "px") {
            return parseFloat(_1b3);
          }
          with(_1b2) {
            var _1b4 = style.left;
            var _1b5 = runtimeStyle.left;
            runtimeStyle.left = currentStyle.left;
            try {
              style.left = _1b3;
              _1b3 = style.pixelLeft;
            } catch (e) {
              _1b3 = 0;
            }
            style.left = _1b4;
            runtimeStyle.left = _1b5;
          }
          return _1b3;
        };
      }
      var px = d._toPixelValue;
      var astr = "DXImageTransform.Microsoft.Alpha";
      var af = function(n, f) {
        try {
          return n.filters.item(astr);
        } catch (e) {
          return f ? {} : null;
        }
      };
      dojo._getOpacity = d.isIE < 9 ? function(node) {
        try {
          return af(node).Opacity / 100;
        } catch (e) {
          return 1;
        }
      } : function(node) {
        return gcs(node).opacity;
      };
      dojo._setOpacity = d.isIE < 9 ? function(node, _1b6) {
        var ov = _1b6 * 100,
          _1b7 = _1b6 == 1;
        node.style.zoom = _1b7 ? "" : 1;
        if (!af(node)) {
          if (_1b7) {
            return _1b6;
          }
          node.style.filter += " progid:" + astr + "(Opacity=" + ov + ")";
        } else {
          af(node, 1).Opacity = ov;
        }
        af(node, 1).Enabled = !_1b7;
        if (node.nodeName.toLowerCase() == "tr") {
          d.query("> td", node).forEach(function(i) {
            d._setOpacity(i, _1b6);
          });
        }
        return _1b6;
      } : function(node, _1b8) {
        return node.style.opacity = _1b8;
      };
      var _1b9 = {
        left: true,
        top: true
      };
      var _1ba = /margin|padding|width|height|max|min|offset/;
      var _1bb = function(node, type, _1bc) {
        type = type.toLowerCase();
        if (d.isIE) {
          if (_1bc == "auto") {
            if (type == "height") {
              return node.offsetHeight;
            }
            if (type == "width") {
              return node.offsetWidth;
            }
          }
          if (type == "fontweight") {
            switch (_1bc) {
              case 700:
                return "bold";
              case 400:
              default:
                return "normal";
            }
          }
        }
        if (!(type in _1b9)) {
          _1b9[type] = _1ba.test(type);
        }
        return _1b9[type] ? px(node, _1bc) : _1bc;
      };
      var _1bd = d.isIE ? "styleFloat" : "cssFloat",
        _1be = {
          "cssFloat": _1bd,
          "styleFloat": _1bd,
          "float": _1bd
        };
      dojo.style = function(node, _1bf, _1c0) {
        var n = byId(node),
          args = arguments.length,
          op = (_1bf == "opacity");
        _1bf = _1be[_1bf] || _1bf;
        if (args == 3) {
          return op ? d._setOpacity(n, _1c0) : n.style[_1bf] = _1c0;
        }
        if (args == 2 && op) {
          return d._getOpacity(n);
        }
        var s = gcs(n);
        if (args == 2 && typeof _1bf != "string") {
          for (var x in _1bf) {
            d.style(node, x, _1bf[x]);
          }
          return s;
        }
        return (args == 1) ? s : _1bb(n, _1bf, s[_1bf] || n.style[_1bf]);
      };
      dojo._getPadExtents = function(n, _1c1) {
        var s = _1c1 || gcs(n),
          l = px(n, s.paddingLeft),
          t = px(n, s.paddingTop);
        return {
          l: l,
          t: t,
          w: l + px(n, s.paddingRight),
          h: t + px(n, s.paddingBottom)
        };
      };
      dojo._getBorderExtents = function(n, _1c2) {
        var ne = "none",
          s = _1c2 || gcs(n),
          bl = (s.borderLeftStyle != ne ? px(n, s.borderLeftWidth) : 0),
          bt = (s.borderTopStyle != ne ? px(n, s.borderTopWidth) : 0);
        return {
          l: bl,
          t: bt,
          w: bl + (s.borderRightStyle != ne ? px(n, s.borderRightWidth) : 0),
          h: bt + (s.borderBottomStyle != ne ? px(n, s.borderBottomWidth) : 0)
        };
      };
      dojo._getPadBorderExtents = function(n, _1c3) {
        var s = _1c3 || gcs(n),
          p = d._getPadExtents(n, s),
          b = d._getBorderExtents(n, s);
        return {
          l: p.l + b.l,
          t: p.t + b.t,
          w: p.w + b.w,
          h: p.h + b.h
        };
      };
      dojo._getMarginExtents = function(n, _1c4) {
        var s = _1c4 || gcs(n),
          l = px(n, s.marginLeft),
          t = px(n, s.marginTop),
          r = px(n, s.marginRight),
          b = px(n, s.marginBottom);
        if (d.isWebKit && (s.position != "absolute")) {
          r = l;
        }
        return {
          l: l,
          t: t,
          w: l + r,
          h: t + b
        };
      };
      dojo._getMarginBox = function(node, _1c5) {
        var s = _1c5 || gcs(node),
          me = d._getMarginExtents(node, s);
        var l = node.offsetLeft - me.l,
          t = node.offsetTop - me.t,
          p = node.parentNode;
        if (d.isMoz) {
          var sl = parseFloat(s.left),
            st = parseFloat(s.top);
          if (!isNaN(sl) && !isNaN(st)) {
            l = sl, t = st;
          } else {
            if (p && p.style) {
              var pcs = gcs(p);
              if (pcs.overflow != "visible") {
                var be = d._getBorderExtents(p, pcs);
                l += be.l, t += be.t;
              }
            }
          }
        } else {
          if (d.isOpera || (d.isIE > 7 && !d.isQuirks)) {
            if (p) {
              be = d._getBorderExtents(p);
              l -= be.l;
              t -= be.t;
            }
          }
        }
        return {
          l: l,
          t: t,
          w: node.offsetWidth + me.w,
          h: node.offsetHeight + me.h
        };
      };
      dojo._getMarginSize = function(node, _1c6) {
        node = byId(node);
        var me = d._getMarginExtents(node, _1c6 || gcs(node));
        var size = node.getBoundingClientRect();
        return {
          w: (size.right - size.left) + me.w,
          h: (size.bottom - size.top) + me.h
        };
      };
      dojo._getContentBox = function(node, _1c7) {
        var s = _1c7 || gcs(node),
          pe = d._getPadExtents(node, s),
          be = d._getBorderExtents(node, s),
          w = node.clientWidth,
          h;
        if (!w) {
          w = node.offsetWidth, h = node.offsetHeight;
        } else {
          h = node.clientHeight, be.w = be.h = 0;
        }
        if (d.isOpera) {
          pe.l += be.l;
          pe.t += be.t;
        }
        return {
          l: pe.l,
          t: pe.t,
          w: w - pe.w - be.w,
          h: h - pe.h - be.h
        };
      };
      dojo._getBorderBox = function(node, _1c8) {
        var s = _1c8 || gcs(node),
          pe = d._getPadExtents(node, s),
          cb = d._getContentBox(node, s);
        return {
          l: cb.l - pe.l,
          t: cb.t - pe.t,
          w: cb.w + pe.w,
          h: cb.h + pe.h
        };
      };
      dojo._setBox = function(node, l, t, w, h, u) {
        u = u || "px";
        var s = node.style;
        if (!isNaN(l)) {
          s.left = l + u;
        }
        if (!isNaN(t)) {
          s.top = t + u;
        }
        if (w >= 0) {
          s.width = w + u;
        }
        if (h >= 0) {
          s.height = h + u;
        }
      };
      dojo._isButtonTag = function(node) {
        return node.tagName == "BUTTON" || node.tagName == "INPUT" && (node.getAttribute("type") || "").toUpperCase() == "BUTTON";
      };
      dojo._usesBorderBox = function(node) {
        var n = node.tagName;
        return d.boxModel == "border-box" || n == "TABLE" || d._isButtonTag(node);
      };
      dojo._setContentSize = function(node, _1c9, _1ca, _1cb) {
        if (d._usesBorderBox(node)) {
          var pb = d._getPadBorderExtents(node, _1cb);
          if (_1c9 >= 0) {
            _1c9 += pb.w;
          }
          if (_1ca >= 0) {
            _1ca += pb.h;
          }
        }
        d._setBox(node, NaN, NaN, _1c9, _1ca);
      };
      dojo._setMarginBox = function(node, _1cc, _1cd, _1ce, _1cf, _1d0) {
        var s = _1d0 || gcs(node),
          bb = d._usesBorderBox(node),
          pb = bb ? _1d1 : d._getPadBorderExtents(node, s);
        if (d.isWebKit) {
          if (d._isButtonTag(node)) {
            var ns = node.style;
            if (_1ce >= 0 && !ns.width) {
              ns.width = "4px";
            }
            if (_1cf >= 0 && !ns.height) {
              ns.height = "4px";
            }
          }
        }
        var mb = d._getMarginExtents(node, s);
        if (_1ce >= 0) {
          _1ce = Math.max(_1ce - pb.w - mb.w, 0);
        }
        if (_1cf >= 0) {
          _1cf = Math.max(_1cf - pb.h - mb.h, 0);
        }
        d._setBox(node, _1cc, _1cd, _1ce, _1cf);
      };
      var _1d1 = {
        l: 0,
        t: 0,
        w: 0,
        h: 0
      };
      dojo.marginBox = function(node, box) {
        var n = byId(node),
          s = gcs(n),
          b = box;
        return !b ? d._getMarginBox(n, s) : d._setMarginBox(n, b.l, b.t, b.w, b.h, s);
      };
      dojo.contentBox = function(node, box) {
        var n = byId(node),
          s = gcs(n),
          b = box;
        return !b ? d._getContentBox(n, s) : d._setContentSize(n, b.w, b.h, s);
      };
      var _1d2 = function(node, prop) {
        if (!(node = (node || 0).parentNode)) {
          return 0;
        }
        var val, _1d3 = 0,
          _1d4 = d.body();
        while (node && node.style) {
          if (gcs(node).position == "fixed") {
            return 0;
          }
          val = node[prop];
          if (val) {
            _1d3 += val - 0;
            if (node == _1d4) {
              break;
            }
          }
          node = node.parentNode;
        }
        return _1d3;
      };
      dojo._docScroll = function() {
        var n = d.global;
        return "pageXOffset" in n ? {
          x: n.pageXOffset,
          y: n.pageYOffset
        } : (n = d.isQuirks ? d.doc.body : d.doc.documentElement, {
          x: d._fixIeBiDiScrollLeft(n.scrollLeft || 0),
          y: n.scrollTop || 0
        });
      };
      dojo._isBodyLtr = function() {
        return "_bodyLtr" in d ? d._bodyLtr : d._bodyLtr = (d.body().dir || d.doc.documentElement.dir || "ltr").toLowerCase() == "ltr";
      };
      dojo._getIeDocumentElementOffset = function() {
        var de = d.doc.documentElement;
        if (d.isIE < 8) {
          var r = de.getBoundingClientRect();
          var l = r.left,
            t = r.top;
          if (d.isIE < 7) {
            l += de.clientLeft;
            t += de.clientTop;
          }
          return {
            x: l < 0 ? 0 : l,
            y: t < 0 ? 0 : t
          };
        } else {
          return {
            x: 0,
            y: 0
          };
        }
      };
      dojo._fixIeBiDiScrollLeft = function(_1d5) {
        var ie = d.isIE;
        if (ie && !d._isBodyLtr()) {
          var qk = d.isQuirks,
            de = qk ? d.doc.body : d.doc.documentElement;
          if (ie == 6 && !qk && d.global.frameElement && de.scrollHeight > de.clientHeight) {
            _1d5 += de.clientLeft;
          }
          return (ie < 8 || qk) ? (_1d5 + de.clientWidth - de.scrollWidth) : -_1d5;
        }
        return _1d5;
      };
      dojo._abs = dojo.position = function(node, _1d6) {
        node = byId(node);
        var db = d.body(),
          dh = db.parentNode,
          ret = node.getBoundingClientRect();
        ret = {
          x: ret.left,
          y: ret.top,
          w: ret.right - ret.left,
          h: ret.bottom - ret.top
        };
        if (d.isIE) {
          var _1d7 = d._getIeDocumentElementOffset();
          ret.x -= _1d7.x + (d.isQuirks ? db.clientLeft + db.offsetLeft : 0);
          ret.y -= _1d7.y + (d.isQuirks ? db.clientTop + db.offsetTop : 0);
        } else {
          if (d.isFF == 3) {
            var cs = gcs(dh);
            ret.x -= px(dh, cs.marginLeft) + px(dh, cs.borderLeftWidth);
            ret.y -= px(dh, cs.marginTop) + px(dh, cs.borderTopWidth);
          }
        }
        if (_1d6) {
          var _1d8 = d._docScroll();
          ret.x += _1d8.x;
          ret.y += _1d8.y;
        }
        return ret;
      };
      dojo.coords = function(node, _1d9) {
        var n = byId(node),
          s = gcs(n),
          mb = d._getMarginBox(n, s);
        var abs = d.position(n, _1d9);
        mb.x = abs.x;
        mb.y = abs.y;
        return mb;
      };
      var _1da = {
          "class": "className",
          "for": "htmlFor",
          tabindex: "tabIndex",
          readonly: "readOnly",
          colspan: "colSpan",
          frameborder: "frameBorder",
          rowspan: "rowSpan",
          valuetype: "valueType"
        },
        _1db = {
          classname: "class",
          htmlfor: "for",
          tabindex: "tabIndex",
          readonly: "readOnly"
        },
        _1dc = {
          innerHTML: 1,
          className: 1,
          htmlFor: d.isIE,
          value: 1
        };
      var _1dd = function(name) {
        return _1db[name.toLowerCase()] || name;
      };
      var _1de = function(node, name) {
        var attr = node.getAttributeNode && node.getAttributeNode(name);
        return attr && attr.specified;
      };
      dojo.hasAttr = function(node, name) {
        var lc = name.toLowerCase();
        return _1dc[_1da[lc] || name] || _1de(byId(node), _1db[lc] || name);
      };
      var _1df = {},
        _1e0 = 0,
        _1e1 = dojo._scopeName + "attrid",
        _1e2 = {
          col: 1,
          colgroup: 1,
          table: 1,
          tbody: 1,
          tfoot: 1,
          thead: 1,
          tr: 1,
          title: 1
        };
      dojo.attr = function(node, name, _1e3) {
        node = byId(node);
        var args = arguments.length,
          prop;
        if (args == 2 && typeof name != "string") {
          for (var x in name) {
            d.attr(node, x, name[x]);
          }
          return node;
        }
        var lc = name.toLowerCase(),
          _1e4 = _1da[lc] || name,
          _1e5 = _1dc[_1e4],
          _1e6 = _1db[lc] || name;
        if (args == 3) {
          do {
            if (_1e4 == "style" && typeof _1e3 != "string") {
              d.style(node, _1e3);
              break;
            }
            if (_1e4 == "innerHTML") {
              if (d.isIE && node.tagName.toLowerCase() in _1e2) {
                d.empty(node);
                node.appendChild(d._toDom(_1e3, node.ownerDocument));
              } else {
                node[_1e4] = _1e3;
              }
              break;
            }
            if (d.isFunction(_1e3)) {
              var _1e7 = d.attr(node, _1e1);
              if (!_1e7) {
                _1e7 = _1e0++;
                d.attr(node, _1e1, _1e7);
              }
              if (!_1df[_1e7]) {
                _1df[_1e7] = {};
              }
              var h = _1df[_1e7][_1e4];
              if (h) {
                d.disconnect(h);
              } else {
                try {
                  delete node[_1e4];
                } catch (e) {}
              }
              _1df[_1e7][_1e4] = d.connect(node, _1e4, _1e3);
              break;
            }
            if (_1e5 || typeof _1e3 == "boolean") {
              node[_1e4] = _1e3;
              break;
            }
            node.setAttribute(_1e6, _1e3);
          } while (false);
          return node;
        }
        _1e3 = node[_1e4];
        if (_1e5 && typeof _1e3 != "undefined") {
          return _1e3;
        }
        if (_1e4 != "href" && (typeof _1e3 == "boolean" || d.isFunction(_1e3))) {
          return _1e3;
        }
        return _1de(node, _1e6) ? node.getAttribute(_1e6) : null;
      };
      dojo.removeAttr = function(node, name) {
        byId(node).removeAttribute(_1dd(name));
      };
      dojo.getNodeProp = function(node, name) {
        node = byId(node);
        var lc = name.toLowerCase(),
          _1e8 = _1da[lc] || name;
        if ((_1e8 in node) && _1e8 != "href") {
          return node[_1e8];
        }
        var _1e9 = _1db[lc] || name;
        return _1de(node, _1e9) ? node.getAttribute(_1e9) : null;
      };
      dojo.create = function(tag, _1ea, _1eb, pos) {
        var doc = d.doc;
        if (_1eb) {
          _1eb = byId(_1eb);
          doc = _1eb.ownerDocument;
        }
        if (typeof tag == "string") {
          tag = doc.createElement(tag);
        }
        if (_1ea) {
          d.attr(tag, _1ea);
        }
        if (_1eb) {
          d.place(tag, _1eb, pos);
        }
        return tag;
      };
      d.empty = d.isIE ? function(node) {
        node = byId(node);
        for (var c; c = node.lastChild;) {
          d.destroy(c);
        }
      } : function(node) {
        byId(node).innerHTML = "";
      };
      var _1ec = {
          option: ["select"],
          tbody: ["table"],
          thead: ["table"],
          tfoot: ["table"],
          tr: ["table", "tbody"],
          td: ["table", "tbody", "tr"],
          th: ["table", "thead", "tr"],
          legend: ["fieldset"],
          caption: ["table"],
          colgroup: ["table"],
          col: ["table", "colgroup"],
          li: ["ul"]
        },
        _1ed = /<\s*([\w\:]+)/,
        _1ee = {},
        _1ef = 0,
        _1f0 = "__" + d._scopeName + "ToDomId";
      for (var _1f1 in _1ec) {
        if (_1ec.hasOwnProperty(_1f1)) {
          var tw = _1ec[_1f1];
          tw.pre = _1f1 == "option" ? "<select multiple=\"multiple\">" : "<" + tw.join("><") + ">";
          tw.post = "</" + tw.reverse().join("></") + ">";
        }
      }
      d._toDom = function(frag, doc) {
        doc = doc || d.doc;
        var _1f2 = doc[_1f0];
        if (!_1f2) {
          doc[_1f0] = _1f2 = ++_1ef + "";
          _1ee[_1f2] = doc.createElement("div");
        }
        frag += "";
        var _1f3 = frag.match(_1ed),
          tag = _1f3 ? _1f3[1].toLowerCase() : "",
          _1f4 = _1ee[_1f2],
          wrap, i, fc, df;
        if (_1f3 && _1ec[tag]) {
          wrap = _1ec[tag];
          _1f4.innerHTML = wrap.pre + frag + wrap.post;
          for (i = wrap.length; i; --i) {
            _1f4 = _1f4.firstChild;
          }
        } else {
          _1f4.innerHTML = frag;
        }
        if (_1f4.childNodes.length == 1) {
          return _1f4.removeChild(_1f4.firstChild);
        }
        df = doc.createDocumentFragment();
        while (fc = _1f4.firstChild) {
          df.appendChild(fc);
        }
        return df;
      };
      var _1f5 = "className";
      dojo.hasClass = function(node, _1f6) {
        return ((" " + byId(node)[_1f5] + " ").indexOf(" " + _1f6 + " ") >= 0);
      };
      var _1f7 = /\s+/,
        a1 = [""],
        _1f8 = {},
        _1f9 = function(s) {
          if (typeof s == "string" || s instanceof String) {
            if (s.indexOf(" ") < 0) {
              a1[0] = s;
              return a1;
            } else {
              return s.split(_1f7);
            }
          }
          return s || "";
        };
      dojo.addClass = function(node, _1fa) {
        node = byId(node);
        _1fa = _1f9(_1fa);
        var cls = node[_1f5],
          _1fb;
        cls = cls ? " " + cls + " " : " ";
        _1fb = cls.length;
        for (var i = 0, len = _1fa.length, c; i < len; ++i) {
          c = _1fa[i];
          if (c && cls.indexOf(" " + c + " ") < 0) {
            cls += c + " ";
          }
        }
        if (_1fb < cls.length) {
          node[_1f5] = cls.substr(1, cls.length - 2);
        }
      };
      dojo.removeClass = function(node, _1fc) {
        node = byId(node);
        var cls;
        if (_1fc !== undefined) {
          _1fc = _1f9(_1fc);
          cls = " " + node[_1f5] + " ";
          for (var i = 0, len = _1fc.length; i < len; ++i) {
            cls = cls.replace(" " + _1fc[i] + " ", " ");
          }
          cls = d.trim(cls);
        } else {
          cls = "";
        }
        if (node[_1f5] != cls) {
          node[_1f5] = cls;
        }
      };
      dojo.replaceClass = function(node, _1fd, _1fe) {
        node = byId(node);
        _1f8.className = node.className;
        dojo.removeClass(_1f8, _1fe);
        dojo.addClass(_1f8, _1fd);
        if (node.className !== _1f8.className) {
          node.className = _1f8.className;
        }
      };
      dojo.toggleClass = function(node, _1ff, _200) {
        if (_200 === undefined) {
          _200 = !d.hasClass(node, _1ff);
        }
        d[_200 ? "addClass" : "removeClass"](node, _1ff);
      };
    })();
  }
  if (!dojo._hasResource["dojo._base.NodeList"]) {
    dojo._hasResource["dojo._base.NodeList"] = true;
    dojo.provide("dojo._base.NodeList");
    (function() {
      var d = dojo;
      var ap = Array.prototype,
        aps = ap.slice,
        apc = ap.concat;
      var tnl = function(a, _201, _202) {
        if (!a.sort) {
          a = aps.call(a, 0);
        }
        var ctor = _202 || this._NodeListCtor || d._NodeListCtor;
        a.constructor = ctor;
        dojo._mixin(a, ctor.prototype);
        a._NodeListCtor = ctor;
        return _201 ? a._stash(_201) : a;
      };
      var _203 = function(f, a, o) {
        a = [0].concat(aps.call(a, 0));
        o = o || d.global;
        return function(node) {
          a[0] = node;
          return f.apply(o, a);
        };
      };
      var _204 = function(f, o) {
        return function() {
          this.forEach(_203(f, arguments, o));
          return this;
        };
      };
      var _205 = function(f, o) {
        return function() {
          return this.map(_203(f, arguments, o));
        };
      };
      var _206 = function(f, o) {
        return function() {
          return this.filter(_203(f, arguments, o));
        };
      };
      var _207 = function(f, g, o) {
        return function() {
          var a = arguments,
            body = _203(f, a, o);
          if (g.call(o || d.global, a)) {
            return this.map(body);
          }
          this.forEach(body);
          return this;
        };
      };
      var _208 = function(a) {
        return a.length == 1 && (typeof a[0] == "string");
      };
      var _209 = function(node) {
        var p = node.parentNode;
        if (p) {
          p.removeChild(node);
        }
      };
      dojo.NodeList = function() {
        return tnl(Array.apply(null, arguments));
      };
      d._NodeListCtor = d.NodeList;
      var nl = d.NodeList,
        nlp = nl.prototype;
      nl._wrap = nlp._wrap = tnl;
      nl._adaptAsMap = _205;
      nl._adaptAsForEach = _204;
      nl._adaptAsFilter = _206;
      nl._adaptWithCondition = _207;
      d.forEach(["slice", "splice"], function(name) {
        var f = ap[name];
        nlp[name] = function() {
          return this._wrap(f.apply(this, arguments), name == "slice" ? this : null);
        };
      });
      d.forEach(["indexOf", "lastIndexOf", "every", "some"], function(name) {
        var f = d[name];
        nlp[name] = function() {
          return f.apply(d, [this].concat(aps.call(arguments, 0)));
        };
      });
      d.forEach(["attr", "style"], function(name) {
        nlp[name] = _207(d[name], _208);
      });
      d.forEach(["connect", "addClass", "removeClass", "replaceClass", "toggleClass", "empty", "removeAttr"], function(name) {
        nlp[name] = _204(d[name]);
      });
      dojo.extend(dojo.NodeList, {
        _normalize: function(_20a, _20b) {
          var _20c = _20a.parse === true ? true : false;
          if (typeof _20a.template == "string") {
            var _20d = _20a.templateFunc || (dojo.string && dojo.string.substitute);
            _20a = _20d ? _20d(_20a.template, _20a) : _20a;
          }
          var type = (typeof _20a);
          if (type == "string" || type == "number") {
            _20a = dojo._toDom(_20a, (_20b && _20b.ownerDocument));
            if (_20a.nodeType == 11) {
              _20a = dojo._toArray(_20a.childNodes);
            } else {
              _20a = [_20a];
            }
          } else {
            if (!dojo.isArrayLike(_20a)) {
              _20a = [_20a];
            } else {
              if (!dojo.isArray(_20a)) {
                _20a = dojo._toArray(_20a);
              }
            }
          }
          if (_20c) {
            _20a._runParse = true;
          }
          return _20a;
        },
        _cloneNode: function(node) {
          return node.cloneNode(true);
        },
        _place: function(ary, _20e, _20f, _210) {
          if (_20e.nodeType != 1 && _20f == "only") {
            return;
          }
          var _211 = _20e,
            _212;
          var _213 = ary.length;
          for (var i = _213 - 1; i >= 0; i--) {
            var node = (_210 ? this._cloneNode(ary[i]) : ary[i]);
            if (ary._runParse && dojo.parser && dojo.parser.parse) {
              if (!_212) {
                _212 = _211.ownerDocument.createElement("div");
              }
              _212.appendChild(node);
              dojo.parser.parse(_212);
              node = _212.firstChild;
              while (_212.firstChild) {
                _212.removeChild(_212.firstChild);
              }
            }
            if (i == _213 - 1) {
              dojo.place(node, _211, _20f);
            } else {
              _211.parentNode.insertBefore(node, _211);
            }
            _211 = node;
          }
        },
        _stash: function(_214) {
          this._parent = _214;
          return this;
        },
        end: function() {
          if (this._parent) {
            return this._parent;
          } else {
            return new this._NodeListCtor();
          }
        },
        concat: function(item) {
          var t = d.isArray(this) ? this : aps.call(this, 0),
            m = d.map(arguments, function(a) {
              return a && !d.isArray(a) && (typeof NodeList != "undefined" && a.constructor === NodeList || a.constructor === this._NodeListCtor) ? aps.call(a, 0) : a;
            });
          return this._wrap(apc.apply(t, m), this);
        },
        map: function(func, obj) {
          return this._wrap(d.map(this, func, obj), this);
        },
        forEach: function(_215, _216) {
          d.forEach(this, _215, _216);
          return this;
        },
        coords: _205(d.coords),
        position: _205(d.position),
        place: function(_217, _218) {
          var item = d.query(_217)[0];
          return this.forEach(function(node) {
            d.place(node, item, _218);
          });
        },
        orphan: function(_219) {
          return (_219 ? d._filterQueryResult(this, _219) : this).forEach(_209);
        },
        adopt: function(_21a, _21b) {
          return d.query(_21a).place(this[0], _21b)._stash(this);
        },
        query: function(_21c) {
          if (!_21c) {
            return this;
          }
          var ret = this.map(function(node) {
            return d.query(_21c, node).filter(function(_21d) {
              return _21d !== undefined;
            });
          });
          return this._wrap(apc.apply([], ret), this);
        },
        filter: function(_21e) {
          var a = arguments,
            _21f = this,
            _220 = 0;
          if (typeof _21e == "string") {
            _21f = d._filterQueryResult(this, a[0]);
            if (a.length == 1) {
              return _21f._stash(this);
            }
            _220 = 1;
          }
          return this._wrap(d.filter(_21f, a[_220], a[_220 + 1]), this);
        },
        addContent: function(_221, _222) {
          _221 = this._normalize(_221, this[0]);
          for (var i = 0, node;
            (node = this[i]); i++) {
            this._place(_221, node, _222, i > 0);
          }
          return this;
        },
        instantiate: function(_223, _224) {
          var c = d.isFunction(_223) ? _223 : d.getObject(_223);
          _224 = _224 || {};
          return this.forEach(function(node) {
            new c(_224, node);
          });
        },
        at: function() {
          var t = new this._NodeListCtor();
          d.forEach(arguments, function(i) {
            if (i < 0) {
              i = this.length + i;
            }
            if (this[i]) {
              t.push(this[i]);
            }
          }, this);
          return t._stash(this);
        }
      });
      nl.events = ["blur", "focus", "change", "click", "error", "keydown", "keypress", "keyup", "load", "mousedown", "mouseenter", "mouseleave", "mousemove", "mouseout", "mouseover", "mouseup", "submit"];
      d.forEach(nl.events, function(evt) {
        var _225 = "on" + evt;
        nlp[_225] = function(a, b) {
          return this.connect(_225, a, b);
        };
      });
    })();
  }
  if (!dojo._hasResource["dojo._base.query"]) {
    dojo._hasResource["dojo._base.query"] = true;
    (function() {
      var _226 = function(d) {
        var trim = d.trim;
        var each = d.forEach;
        var qlc = (d._NodeListCtor = d.NodeList);
        var _227 = function() {
          return d.doc;
        };
        var _228 = ((d.isWebKit || d.isMozilla) && ((_227().compatMode) == "BackCompat"));
        var _229 = ">~+";
        var _22a = false;
        var _22b = function() {
          return true;
        };
        var _22c = function(_22d) {
          if (_229.indexOf(_22d.slice(-1)) >= 0) {
            _22d += " * ";
          } else {
            _22d += " ";
          }
          var ts = function(s, e) {
            return trim(_22d.slice(s, e));
          };
          var _22e = [];
          var _22f = -1,
            _230 = -1,
            _231 = -1,
            _232 = -1,
            _233 = -1,
            inId = -1,
            _234 = -1,
            lc = "",
            cc = "",
            _235;
          var x = 0,
            ql = _22d.length,
            _236 = null,
            _237 = null;
          var _238 = function() {
            if (_234 >= 0) {
              var tv = (_234 == x) ? null : ts(_234, x);
              _236[(_229.indexOf(tv) < 0) ? "tag" : "oper"] = tv;
              _234 = -1;
            }
          };
          var _239 = function() {
            if (inId >= 0) {
              _236.id = ts(inId, x).replace(/\\/g, "");
              inId = -1;
            }
          };
          var _23a = function() {
            if (_233 >= 0) {
              _236.classes.push(ts(_233 + 1, x).replace(/\\/g, ""));
              _233 = -1;
            }
          };
          var _23b = function() {
            _239();
            _238();
            _23a();
          };
          var _23c = function() {
            _23b();
            if (_232 >= 0) {
              _236.pseudos.push({
                name: ts(_232 + 1, x)
              });
            }
            _236.loops = (_236.pseudos.length || _236.attrs.length || _236.classes.length);
            _236.oquery = _236.query = ts(_235, x);
            _236.otag = _236.tag = (_236["oper"]) ? null : (_236.tag || "*");
            if (_236.tag) {
              _236.tag = _236.tag.toUpperCase();
            }
            if (_22e.length && (_22e[_22e.length - 1].oper)) {
              _236.infixOper = _22e.pop();
              _236.query = _236.infixOper.query + " " + _236.query;
            }
            _22e.push(_236);
            _236 = null;
          };
          for (; lc = cc, cc = _22d.charAt(x), x < ql; x++) {
            if (lc == "\\") {
              continue;
            }
            if (!_236) {
              _235 = x;
              _236 = {
                query: null,
                pseudos: [],
                attrs: [],
                classes: [],
                tag: null,
                oper: null,
                id: null,
                getTag: function() {
                  return (_22a) ? this.otag : this.tag;
                }
              };
              _234 = x;
            }
            if (_22f >= 0) {
              if (cc == "]") {
                if (!_237.attr) {
                  _237.attr = ts(_22f + 1, x);
                } else {
                  _237.matchFor = ts((_231 || _22f + 1), x);
                }
                var cmf = _237.matchFor;
                if (cmf) {
                  if ((cmf.charAt(0) == "\"") || (cmf.charAt(0) == "'")) {
                    _237.matchFor = cmf.slice(1, -1);
                  }
                }
                _236.attrs.push(_237);
                _237 = null;
                _22f = _231 = -1;
              } else {
                if (cc == "=") {
                  var _23d = ("|~^$*".indexOf(lc) >= 0) ? lc : "";
                  _237.type = _23d + cc;
                  _237.attr = ts(_22f + 1, x - _23d.length);
                  _231 = x + 1;
                }
              }
            } else {
              if (_230 >= 0) {
                if (cc == ")") {
                  if (_232 >= 0) {
                    _237.value = ts(_230 + 1, x);
                  }
                  _232 = _230 = -1;
                }
              } else {
                if (cc == "#") {
                  _23b();
                  inId = x + 1;
                } else {
                  if (cc == ".") {
                    _23b();
                    _233 = x;
                  } else {
                    if (cc == ":") {
                      _23b();
                      _232 = x;
                    } else {
                      if (cc == "[") {
                        _23b();
                        _22f = x;
                        _237 = {};
                      } else {
                        if (cc == "(") {
                          if (_232 >= 0) {
                            _237 = {
                              name: ts(_232 + 1, x),
                              value: null
                            };
                            _236.pseudos.push(_237);
                          }
                          _230 = x;
                        } else {
                          if ((cc == " ") && (lc != cc)) {
                            _23c();
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
          return _22e;
        };
        var _23e = function(_23f, _240) {
          if (!_23f) {
            return _240;
          }
          if (!_240) {
            return _23f;
          }
          return function() {
            return _23f.apply(window, arguments) && _240.apply(window, arguments);
          };
        };
        var _241 = function(i, arr) {
          var r = arr || [];
          if (i) {
            r.push(i);
          }
          return r;
        };
        var _242 = function(n) {
          return (1 == n.nodeType);
        };
        var _243 = "";
        var _244 = function(elem, attr) {
          if (!elem) {
            return _243;
          }
          if (attr == "class") {
            return elem.className || _243;
          }
          if (attr == "for") {
            return elem.htmlFor || _243;
          }
          if (attr == "style") {
            return elem.style.cssText || _243;
          }
          return (_22a ? elem.getAttribute(attr) : elem.getAttribute(attr, 2)) || _243;
        };
        var _245 = {
          "*=": function(attr, _246) {
            return function(elem) {
              return (_244(elem, attr).indexOf(_246) >= 0);
            };
          },
          "^=": function(attr, _247) {
            return function(elem) {
              return (_244(elem, attr).indexOf(_247) == 0);
            };
          },
          "$=": function(attr, _248) {
            var tval = " " + _248;
            return function(elem) {
              var ea = " " + _244(elem, attr);
              return (ea.lastIndexOf(_248) == (ea.length - _248.length));
            };
          },
          "~=": function(attr, _249) {
            var tval = " " + _249 + " ";
            return function(elem) {
              var ea = " " + _244(elem, attr) + " ";
              return (ea.indexOf(tval) >= 0);
            };
          },
          "|=": function(attr, _24a) {
            var _24b = " " + _24a + "-";
            return function(elem) {
              var ea = " " + _244(elem, attr);
              return ((ea == _24a) || (ea.indexOf(_24b) == 0));
            };
          },
          "=": function(attr, _24c) {
            return function(elem) {
              return (_244(elem, attr) == _24c);
            };
          }
        };
        var _24d = (typeof _227().firstChild.nextElementSibling == "undefined");
        var _24e = !_24d ? "nextElementSibling" : "nextSibling";
        var _24f = !_24d ? "previousElementSibling" : "previousSibling";
        var _250 = (_24d ? _242 : _22b);
        var _251 = function(node) {
          while (node = node[_24f]) {
            if (_250(node)) {
              return false;
            }
          }
          return true;
        };
        var _252 = function(node) {
          while (node = node[_24e]) {
            if (_250(node)) {
              return false;
            }
          }
          return true;
        };
        var _253 = function(node) {
          var root = node.parentNode;
          var i = 0,
            tret = root.children || root.childNodes,
            ci = (node["_i"] || -1),
            cl = (root["_l"] || -1);
          if (!tret) {
            return -1;
          }
          var l = tret.length;
          if (cl == l && ci >= 0 && cl >= 0) {
            return ci;
          }
          root["_l"] = l;
          ci = -1;
          for (var te = root["firstElementChild"] || root["firstChild"]; te; te = te[_24e]) {
            if (_250(te)) {
              te["_i"] = ++i;
              if (node === te) {
                ci = i;
              }
            }
          }
          return ci;
        };
        var _254 = function(elem) {
          return !((_253(elem)) % 2);
        };
        var _255 = function(elem) {
          return ((_253(elem)) % 2);
        };
        var _256 = {
          "checked": function(name, _257) {
            return function(elem) {
              return !!("checked" in elem ? elem.checked : elem.selected);
            };
          },
          "first-child": function() {
            return _251;
          },
          "last-child": function() {
            return _252;
          },
          "only-child": function(name, _258) {
            return function(node) {
              if (!_251(node)) {
                return false;
              }
              if (!_252(node)) {
                return false;
              }
              return true;
            };
          },
          "empty": function(name, _259) {
            return function(elem) {
              var cn = elem.childNodes;
              var cnl = elem.childNodes.length;
              for (var x = cnl - 1; x >= 0; x--) {
                var nt = cn[x].nodeType;
                if ((nt === 1) || (nt == 3)) {
                  return false;
                }
              }
              return true;
            };
          },
          "contains": function(name, _25a) {
            var cz = _25a.charAt(0);
            if (cz == "\"" || cz == "'") {
              _25a = _25a.slice(1, -1);
            }
            return function(elem) {
              return (elem.innerHTML.indexOf(_25a) >= 0);
            };
          },
          "not": function(name, _25b) {
            var p = _22c(_25b)[0];
            var _25c = {
              el: 1
            };
            if (p.tag != "*") {
              _25c.tag = 1;
            }
            if (!p.classes.length) {
              _25c.classes = 1;
            }
            var ntf = _25d(p, _25c);
            return function(elem) {
              return (!ntf(elem));
            };
          },
          "nth-child": function(name, _25e) {
            var pi = parseInt;
            if (_25e == "odd") {
              return _255;
            } else {
              if (_25e == "even") {
                return _254;
              }
            }
            if (_25e.indexOf("n") != -1) {
              var _25f = _25e.split("n", 2);
              var pred = _25f[0] ? ((_25f[0] == "-") ? -1 : pi(_25f[0])) : 1;
              var idx = _25f[1] ? pi(_25f[1]) : 0;
              var lb = 0,
                ub = -1;
              if (pred > 0) {
                if (idx < 0) {
                  idx = (idx % pred) && (pred + (idx % pred));
                } else {
                  if (idx > 0) {
                    if (idx >= pred) {
                      lb = idx - idx % pred;
                    }
                    idx = idx % pred;
                  }
                }
              } else {
                if (pred < 0) {
                  pred *= -1;
                  if (idx > 0) {
                    ub = idx;
                    idx = idx % pred;
                  }
                }
              }
              if (pred > 0) {
                return function(elem) {
                  var i = _253(elem);
                  return (i >= lb) && (ub < 0 || i <= ub) && ((i % pred) == idx);
                };
              } else {
                _25e = idx;
              }
            }
            var _260 = pi(_25e);
            return function(elem) {
              return (_253(elem) == _260);
            };
          }
        };
        var _261 = (d.isIE < 9 || (dojo.isIE && dojo.isQuirks)) ? function(cond) {
          var clc = cond.toLowerCase();
          if (clc == "class") {
            cond = "className";
          }
          return function(elem) {
            return (_22a ? elem.getAttribute(cond) : elem[cond] || elem[clc]);
          };
        } : function(cond) {
          return function(elem) {
            return (elem && elem.getAttribute && elem.hasAttribute(cond));
          };
        };
        var _25d = function(_262, _263) {
          if (!_262) {
            return _22b;
          }
          _263 = _263 || {};
          var ff = null;
          if (!("el" in _263)) {
            ff = _23e(ff, _242);
          }
          if (!("tag" in _263)) {
            if (_262.tag != "*") {
              ff = _23e(ff, function(elem) {
                return (elem && (elem.tagName == _262.getTag()));
              });
            }
          }
          if (!("classes" in _263)) {
            each(_262.classes, function(_264, idx, arr) {
              var re = new RegExp("(?:^|\\s)" + _264 + "(?:\\s|$)");
              ff = _23e(ff, function(elem) {
                return re.test(elem.className);
              });
              ff.count = idx;
            });
          }
          if (!("pseudos" in _263)) {
            each(_262.pseudos, function(_265) {
              var pn = _265.name;
              if (_256[pn]) {
                ff = _23e(ff, _256[pn](pn, _265.value));
              }
            });
          }
          if (!("attrs" in _263)) {
            each(_262.attrs, function(attr) {
              var _266;
              var a = attr.attr;
              if (attr.type && _245[attr.type]) {
                _266 = _245[attr.type](a, attr.matchFor);
              } else {
                if (a.length) {
                  _266 = _261(a);
                }
              }
              if (_266) {
                ff = _23e(ff, _266);
              }
            });
          }
          if (!("id" in _263)) {
            if (_262.id) {
              ff = _23e(ff, function(elem) {
                return (!!elem && (elem.id == _262.id));
              });
            }
          }
          if (!ff) {
            if (!("default" in _263)) {
              ff = _22b;
            }
          }
          return ff;
        };
        var _267 = function(_268) {
          return function(node, ret, bag) {
            while (node = node[_24e]) {
              if (_24d && (!_242(node))) {
                continue;
              }
              if ((!bag || _269(node, bag)) && _268(node)) {
                ret.push(node);
              }
              break;
            }
            return ret;
          };
        };
        var _26a = function(_26b) {
          return function(root, ret, bag) {
            var te = root[_24e];
            while (te) {
              if (_250(te)) {
                if (bag && !_269(te, bag)) {
                  break;
                }
                if (_26b(te)) {
                  ret.push(te);
                }
              }
              te = te[_24e];
            }
            return ret;
          };
        };
        var _26c = function(_26d) {
          _26d = _26d || _22b;
          return function(root, ret, bag) {
            var te, x = 0,
              tret = root.children || root.childNodes;
            while (te = tret[x++]) {
              if (_250(te) && (!bag || _269(te, bag)) && (_26d(te, x))) {
                ret.push(te);
              }
            }
            return ret;
          };
        };
        var _26e = function(node, root) {
          var pn = node.parentNode;
          while (pn) {
            if (pn == root) {
              break;
            }
            pn = pn.parentNode;
          }
          return !!pn;
        };
        var _26f = {};
        var _270 = function(_271) {
          var _272 = _26f[_271.query];
          if (_272) {
            return _272;
          }
          var io = _271.infixOper;
          var oper = (io ? io.oper : "");
          var _273 = _25d(_271, {
            el: 1
          });
          var qt = _271.tag;
          var _274 = ("*" == qt);
          var ecs = _227()["getElementsByClassName"];
          if (!oper) {
            if (_271.id) {
              _273 = (!_271.loops && _274) ? _22b : _25d(_271, {
                el: 1,
                id: 1
              });
              _272 = function(root, arr) {
                var te = d.byId(_271.id, (root.ownerDocument || root));
                if (!te || !_273(te)) {
                  return;
                }
                if (9 == root.nodeType) {
                  return _241(te, arr);
                } else {
                  if (_26e(te, root)) {
                    return _241(te, arr);
                  }
                }
              };
            } else {
              if (ecs && /\{\s*\[native code\]\s*\}/.test(String(ecs)) && _271.classes.length && !_228) {
                _273 = _25d(_271, {
                  el: 1,
                  classes: 1,
                  id: 1
                });
                var _275 = _271.classes.join(" ");
                _272 = function(root, arr, bag) {
                  var ret = _241(0, arr),
                    te, x = 0;
                  var tret = root.getElementsByClassName(_275);
                  while ((te = tret[x++])) {
                    if (_273(te, root) && _269(te, bag)) {
                      ret.push(te);
                    }
                  }
                  return ret;
                };
              } else {
                if (!_274 && !_271.loops) {
                  _272 = function(root, arr, bag) {
                    var ret = _241(0, arr),
                      te, x = 0;
                    var tret = root.getElementsByTagName(_271.getTag());
                    while ((te = tret[x++])) {
                      if (_269(te, bag)) {
                        ret.push(te);
                      }
                    }
                    return ret;
                  };
                } else {
                  _273 = _25d(_271, {
                    el: 1,
                    tag: 1,
                    id: 1
                  });
                  _272 = function(root, arr, bag) {
                    var ret = _241(0, arr),
                      te, x = 0;
                    var tret = root.getElementsByTagName(_271.getTag());
                    while ((te = tret[x++])) {
                      if (_273(te, root) && _269(te, bag)) {
                        ret.push(te);
                      }
                    }
                    return ret;
                  };
                }
              }
            }
          } else {
            var _276 = {
              el: 1
            };
            if (_274) {
              _276.tag = 1;
            }
            _273 = _25d(_271, _276);
            if ("+" == oper) {
              _272 = _267(_273);
            } else {
              if ("~" == oper) {
                _272 = _26a(_273);
              } else {
                if (">" == oper) {
                  _272 = _26c(_273);
                }
              }
            }
          }
          return _26f[_271.query] = _272;
        };
        var _277 = function(root, _278) {
          var _279 = _241(root),
            qp, x, te, qpl = _278.length,
            bag, ret;
          for (var i = 0; i < qpl; i++) {
            ret = [];
            qp = _278[i];
            x = _279.length - 1;
            if (x > 0) {
              bag = {};
              ret.nozip = true;
            }
            var gef = _270(qp);
            for (var j = 0;
              (te = _279[j]); j++) {
              gef(te, ret, bag);
            }
            if (!ret.length) {
              break;
            }
            _279 = ret;
          }
          return ret;
        };
        var _27a = {},
          _27b = {};
        var _27c = function(_27d) {
          var _27e = _22c(trim(_27d));
          if (_27e.length == 1) {
            var tef = _270(_27e[0]);
            return function(root) {
              var r = tef(root, new qlc());
              if (r) {
                r.nozip = true;
              }
              return r;
            };
          }
          return function(root) {
            return _277(root, _27e);
          };
        };
        var nua = navigator.userAgent;
        var wk = "WebKit/";
        var _27f = (d.isWebKit && (nua.indexOf(wk) > 0) && (parseFloat(nua.split(wk)[1]) > 528));
        var _280 = d.isIE ? "commentStrip" : "nozip";
        var qsa = "querySelectorAll";
        var _281 = (!!_227()[qsa] && (!d.isSafari || (d.isSafari > 3.1) || _27f));
        var _282 = /n\+\d|([^ ])?([>~+])([^ =])?/g;
        var _283 = function(_284, pre, ch, post) {
          return ch ? (pre ? pre + " " : "") + ch + (post ? " " + post : "") : _284;
        };
        var _285 = function(_286, _287) {
          _286 = _286.replace(_282, _283);
          if (_281) {
            var _288 = _27b[_286];
            if (_288 && !_287) {
              return _288;
            }
          }
          var _289 = _27a[_286];
          if (_289) {
            return _289;
          }
          var qcz = _286.charAt(0);
          var _28a = (-1 == _286.indexOf(" "));
          if ((_286.indexOf("#") >= 0) && (_28a)) {
            _287 = true;
          }
          var _28b = (_281 && (!_287) && (_229.indexOf(qcz) == -1) && (!d.isIE || (_286.indexOf(":") == -1)) && (!(_228 && (_286.indexOf(".") >= 0))) && (_286.indexOf(":contains") == -1) && (_286.indexOf(":checked") == -1) && (_286.indexOf("|=") == -1));
          if (_28b) {
            var tq = (_229.indexOf(_286.charAt(_286.length - 1)) >= 0) ? (_286 + " *") : _286;
            return _27b[_286] = function(root) {
              try {
                if (!((9 == root.nodeType) || _28a)) {
                  throw "";
                }
                var r = root[qsa](tq);
                r[_280] = true;
                return r;
              } catch (e) {
                return _285(_286, true)(root);
              }
            };
          } else {
            var _28c = _286.split(/\s*,\s*/);
            return _27a[_286] = ((_28c.length < 2) ? _27c(_286) : function(root) {
              var _28d = 0,
                ret = [],
                tp;
              while ((tp = _28c[_28d++])) {
                ret = ret.concat(_27c(tp)(root));
              }
              return ret;
            });
          }
        };
        var _28e = 0;
        var _28f = d.isIE ? function(node) {
          if (_22a) {
            return (node.getAttribute("_uid") || node.setAttribute("_uid", ++_28e) || _28e);
          } else {
            return node.uniqueID;
          }
        } : function(node) {
          return (node._uid || (node._uid = ++_28e));
        };
        var _269 = function(node, bag) {
          if (!bag) {
            return 1;
          }
          var id = _28f(node);
          if (!bag[id]) {
            return bag[id] = 1;
          }
          return 0;
        };
        var _290 = "_zipIdx";
        var _291 = function(arr) {
          if (arr && arr.nozip) {
            return (qlc._wrap) ? qlc._wrap(arr) : arr;
          }
          var ret = new qlc();
          if (!arr || !arr.length) {
            return ret;
          }
          if (arr[0]) {
            ret.push(arr[0]);
          }
          if (arr.length < 2) {
            return ret;
          }
          _28e++;
          if (d.isIE && _22a) {
            var _292 = _28e + "";
            arr[0].setAttribute(_290, _292);
            for (var x = 1, te; te = arr[x]; x++) {
              if (arr[x].getAttribute(_290) != _292) {
                ret.push(te);
              }
              te.setAttribute(_290, _292);
            }
          } else {
            if (d.isIE && arr.commentStrip) {
              try {
                for (var x = 1, te; te = arr[x]; x++) {
                  if (_242(te)) {
                    ret.push(te);
                  }
                }
              } catch (e) {}
            } else {
              if (arr[0]) {
                arr[0][_290] = _28e;
              }
              for (var x = 1, te; te = arr[x]; x++) {
                if (arr[x][_290] != _28e) {
                  ret.push(te);
                }
                te[_290] = _28e;
              }
            }
          }
          return ret;
        };
        d.query = function(_293, root) {
          qlc = d._NodeListCtor;
          if (!_293) {
            return new qlc();
          }
          if (_293.constructor == qlc) {
            return _293;
          }
          if (typeof _293 != "string") {
            return new qlc(_293);
          }
          if (typeof root == "string") {
            root = d.byId(root);
            if (!root) {
              return new qlc();
            }
          }
          root = root || _227();
          var od = root.ownerDocument || root.documentElement;
          _22a = (root.contentType && root.contentType == "application/xml") || (d.isOpera && (root.doctype || od.toString() == "[object XMLDocument]")) || (!!od) && (d.isIE ? od.xml : (root.xmlVersion || od.xmlVersion));
          var r = _285(_293)(root);
          if (r && r.nozip && !qlc._wrap) {
            return r;
          }
          return _291(r);
        };
        d.query.pseudos = _256;
        d._filterQueryResult = function(_294, _295, root) {
          var _296 = new d._NodeListCtor(),
            _297 = _22c(_295),
            _298 = (_297.length == 1 && !/[^\w#\.]/.test(_295)) ? _25d(_297[0]) : function(node) {
              return dojo.query(_295, root).indexOf(node) != -1;
            };
          for (var x = 0, te; te = _294[x]; x++) {
            if (_298(te)) {
              _296.push(te);
            }
          }
          return _296;
        };
      };
      var _299 = function() {
        acme = {
          trim: function(str) {
            str = str.replace(/^\s+/, "");
            for (var i = str.length - 1; i >= 0; i--) {
              if (/\S/.test(str.charAt(i))) {
                str = str.substring(0, i + 1);
                break;
              }
            }
            return str;
          },
          forEach: function(arr, _29a, _29b) {
            if (!arr || !arr.length) {
              return;
            }
            for (var i = 0, l = arr.length; i < l; ++i) {
              _29a.call(_29b || window, arr[i], i, arr);
            }
          },
          byId: function(id, doc) {
            if (typeof id == "string") {
              return (doc || document).getElementById(id);
            } else {
              return id;
            }
          },
          doc: document,
          NodeList: Array
        };
        var n = navigator;
        var dua = n.userAgent;
        var dav = n.appVersion;
        var tv = parseFloat(dav);
        acme.isOpera = (dua.indexOf("Opera") >= 0) ? tv : undefined;
        acme.isKhtml = (dav.indexOf("Konqueror") >= 0) ? tv : undefined;
        acme.isWebKit = parseFloat(dua.split("WebKit/")[1]) || undefined;
        acme.isChrome = parseFloat(dua.split("Chrome/")[1]) || undefined;
        var _29c = Math.max(dav.indexOf("WebKit"), dav.indexOf("Safari"), 0);
        if (_29c && !acme.isChrome) {
          acme.isSafari = parseFloat(dav.split("Version/")[1]);
          if (!acme.isSafari || parseFloat(dav.substr(_29c + 7)) <= 419.3) {
            acme.isSafari = 2;
          }
        }
        if (document.all && !acme.isOpera) {
          acme.isIE = parseFloat(dav.split("MSIE ")[1]) || undefined;
        }
        Array._wrap = function(arr) {
          return arr;
        };
        return acme;
      };
      if (this["dojo"]) {
        dojo.provide("dojo._base.query");
        _226(this["queryPortability"] || this["acme"] || dojo);
      } else {
        _226(this["queryPortability"] || this["acme"] || _299());
      }
    })();
  }
  if (!dojo._hasResource["dojo._base.xhr"]) {
    dojo._hasResource["dojo._base.xhr"] = true;
    dojo.provide("dojo._base.xhr");
    (function() {
      var _29d = dojo,
        cfg = _29d.config;

      function _29e(obj, name, _29f) {
        if (_29f === null) {
          return;
        }
        var val = obj[name];
        if (typeof val == "string") {
          obj[name] = [val, _29f];
        } else {
          if (_29d.isArray(val)) {
            val.push(_29f);
          } else {
            obj[name] = _29f;
          }
        }
      };
      dojo.fieldToObject = function(_2a0) {
        var ret = null;
        var item = _29d.byId(_2a0);
        if (item) {
          var _2a1 = item.name;
          var type = (item.type || "").toLowerCase();
          if (_2a1 && type && !item.disabled) {
            if (type == "radio" || type == "checkbox") {
              if (item.checked) {
                ret = item.value;
              }
            } else {
              if (item.multiple) {
                ret = [];
                _29d.query("option", item).forEach(function(opt) {
                  if (opt.selected) {
                    ret.push(opt.value);
                  }
                });
              } else {
                ret = item.value;
              }
            }
          }
        }
        return ret;
      };
      dojo.formToObject = function(_2a2) {
        var ret = {};
        var _2a3 = "file|submit|image|reset|button|";
        _29d.forEach(dojo.byId(_2a2).elements, function(item) {
          var _2a4 = item.name;
          var type = (item.type || "").toLowerCase();
          if (_2a4 && type && _2a3.indexOf(type) == -1 && !item.disabled) {
            _29e(ret, _2a4, _29d.fieldToObject(item));
            if (type == "image") {
              ret[_2a4 + ".x"] = ret[_2a4 + ".y"] = ret[_2a4].x = ret[_2a4].y = 0;
            }
          }
        });
        return ret;
      };
      dojo.objectToQuery = function(map) {
        var enc = encodeURIComponent;
        var _2a5 = [];
        var _2a6 = {};
        for (var name in map) {
          var _2a7 = map[name];
          if (_2a7 != _2a6[name]) {
            var _2a8 = enc(name) + "=";
            if (_29d.isArray(_2a7)) {
              for (var i = 0; i < _2a7.length; i++) {
                _2a5.push(_2a8 + enc(_2a7[i]));
              }
            } else {
              _2a5.push(_2a8 + enc(_2a7));
            }
          }
        }
        return _2a5.join("&");
      };
      dojo.formToQuery = function(_2a9) {
        return _29d.objectToQuery(_29d.formToObject(_2a9));
      };
      dojo.formToJson = function(_2aa, _2ab) {
        return _29d.toJson(_29d.formToObject(_2aa), _2ab);
      };
      dojo.queryToObject = function(str) {
        var ret = {};
        var qp = str.split("&");
        var dec = decodeURIComponent;
        _29d.forEach(qp, function(item) {
          if (item.length) {
            var _2ac = item.split("=");
            var name = dec(_2ac.shift());
            var val = dec(_2ac.join("="));
            if (typeof ret[name] == "string") {
              ret[name] = [ret[name]];
            }
            if (_29d.isArray(ret[name])) {
              ret[name].push(val);
            } else {
              ret[name] = val;
            }
          }
        });
        return ret;
      };
      dojo._blockAsync = false;
      var _2ad = _29d._contentHandlers = dojo.contentHandlers = {
        text: function(xhr) {
          return xhr.responseText;
        },
        json: function(xhr) {
          return _29d.fromJson(xhr.responseText || null);
        },
        "json-comment-filtered": function(xhr) {
          if (!dojo.config.useCommentedJson) {}
          var _2ae = xhr.responseText;
          var _2af = _2ae.indexOf("/*");
          var _2b0 = _2ae.lastIndexOf("*/");
          if (_2af == -1 || _2b0 == -1) {
            throw new Error("JSON was not comment filtered");
          }
          return _29d.fromJson(_2ae.substring(_2af + 2, _2b0));
        },
        javascript: function(xhr) {
          return _29d.eval(xhr.responseText);
        },
        xml: function(xhr) {
          var _2b1 = xhr.responseXML;
          if (_29d.isIE && (!_2b1 || !_2b1.documentElement)) {
            var ms = function(n) {
              return "MSXML" + n + ".DOMDocument";
            };
            var dp = ["Microsoft.XMLDOM", ms(6), ms(4), ms(3), ms(2)];
            _29d.some(dp, function(p) {
              try {
                var dom = new ActiveXObject(p);
                dom.async = false;
                dom.loadXML(xhr.responseText);
                _2b1 = dom;
              } catch (e) {
                return false;
              }
              return true;
            });
          }
          return _2b1;
        },
        "json-comment-optional": function(xhr) {
          if (xhr.responseText && /^[^{\[]*\/\*/.test(xhr.responseText)) {
            return _2ad["json-comment-filtered"](xhr);
          } else {
            return _2ad["json"](xhr);
          }
        }
      };
      dojo._ioSetArgs = function(args, _2b2, _2b3, _2b4) {
        var _2b5 = {
          args: args,
          url: args.url
        };
        var _2b6 = null;
        if (args.form) {
          var form = _29d.byId(args.form);
          var _2b7 = form.getAttributeNode("action");
          _2b5.url = _2b5.url || (_2b7 ? _2b7.value : null);
          _2b6 = _29d.formToObject(form);
        }
        var _2b8 = [{}];
        if (_2b6) {
          _2b8.push(_2b6);
        }
        if (args.content) {
          _2b8.push(args.content);
        }
        if (args.preventCache) {
          _2b8.push({
            "dojo.preventCache": new Date().valueOf()
          });
        }
        _2b5.query = _29d.objectToQuery(_29d.mixin.apply(null, _2b8));
        _2b5.handleAs = args.handleAs || "text";
        var d = new _29d.Deferred(_2b2);
        d.addCallbacks(_2b3, function(_2b9) {
          return _2b4(_2b9, d);
        });
        var ld = args.load;
        if (ld && _29d.isFunction(ld)) {
          d.addCallback(function(_2ba) {
            return ld.call(args, _2ba, _2b5);
          });
        }
        var err = args.error;
        if (err && _29d.isFunction(err)) {
          d.addErrback(function(_2bb) {
            return err.call(args, _2bb, _2b5);
          });
        }
        var _2bc = args.handle;
        if (_2bc && _29d.isFunction(_2bc)) {
          d.addBoth(function(_2bd) {
            return _2bc.call(args, _2bd, _2b5);
          });
        }
        if (cfg.ioPublish && _29d.publish && _2b5.args.ioPublish !== false) {
          d.addCallbacks(function(res) {
            _29d.publish("/dojo/io/load", [d, res]);
            return res;
          }, function(res) {
            _29d.publish("/dojo/io/error", [d, res]);
            return res;
          });
          d.addBoth(function(res) {
            _29d.publish("/dojo/io/done", [d, res]);
            return res;
          });
        }
        d.ioArgs = _2b5;
        return d;
      };
      var _2be = function(dfd) {
        dfd.canceled = true;
        var xhr = dfd.ioArgs.xhr;
        var _2bf = typeof xhr.abort;
        if (_2bf == "function" || _2bf == "object" || _2bf == "unknown") {
          xhr.abort();
        }
        var err = dfd.ioArgs.error;
        if (!err) {
          err = new Error("xhr cancelled");
          err.dojoType = "cancel";
        }
        return err;
      };
      var _2c0 = function(dfd) {
        var ret = _2ad[dfd.ioArgs.handleAs](dfd.ioArgs.xhr);
        return ret === undefined ? null : ret;
      };
      var _2c1 = function(_2c2, dfd) {
        if (!dfd.ioArgs.args.failOk) {}
        return _2c2;
      };
      var _2c3 = null;
      var _2c4 = [];
      var _2c5 = 0;
      var _2c6 = function(dfd) {
        if (_2c5 <= 0) {
          _2c5 = 0;
          if (cfg.ioPublish && _29d.publish && (!dfd || dfd && dfd.ioArgs.args.ioPublish !== false)) {
            _29d.publish("/dojo/io/stop");
          }
        }
      };
      var _2c7 = function() {
        var now = (new Date()).getTime();
        if (!_29d._blockAsync) {
          for (var i = 0, tif; i < _2c4.length && (tif = _2c4[i]); i++) {
            var dfd = tif.dfd;
            var func = function() {
              if (!dfd || dfd.canceled || !tif.validCheck(dfd)) {
                _2c4.splice(i--, 1);
                _2c5 -= 1;
              } else {
                if (tif.ioCheck(dfd)) {
                  _2c4.splice(i--, 1);
                  tif.resHandle(dfd);
                  _2c5 -= 1;
                } else {
                  if (dfd.startTime) {
                    if (dfd.startTime + (dfd.ioArgs.args.timeout || 0) < now) {
                      _2c4.splice(i--, 1);
                      var err = new Error("timeout exceeded");
                      err.dojoType = "timeout";
                      dfd.errback(err);
                      dfd.cancel();
                      _2c5 -= 1;
                    }
                  }
                }
              }
            };
            if (dojo.config.debugAtAllCosts) {
              func.call(this);
            } else {
              try {
                func.call(this);
              } catch (e) {
                dfd.errback(e);
              }
            }
          }
        }
        _2c6(dfd);
        if (!_2c4.length) {
          clearInterval(_2c3);
          _2c3 = null;
          return;
        }
      };
      dojo._ioCancelAll = function() {
        try {
          _29d.forEach(_2c4, function(i) {
            try {
              i.dfd.cancel();
            } catch (e) {}
          });
        } catch (e) {}
      };
      if (_29d.isIE) {
        _29d.addOnWindowUnload(_29d._ioCancelAll);
      }
      _29d._ioNotifyStart = function(dfd) {
        if (cfg.ioPublish && _29d.publish && dfd.ioArgs.args.ioPublish !== false) {
          if (!_2c5) {
            _29d.publish("/dojo/io/start");
          }
          _2c5 += 1;
          _29d.publish("/dojo/io/send", [dfd]);
        }
      };
      _29d._ioWatch = function(dfd, _2c8, _2c9, _2ca) {
        var args = dfd.ioArgs.args;
        if (args.timeout) {
          dfd.startTime = (new Date()).getTime();
        }
        _2c4.push({
          dfd: dfd,
          validCheck: _2c8,
          ioCheck: _2c9,
          resHandle: _2ca
        });
        if (!_2c3) {
          _2c3 = setInterval(_2c7, 50);
        }
        if (args.sync) {
          _2c7();
        }
      };
      var _2cb = "application/x-www-form-urlencoded";
      var _2cc = function(dfd) {
        return dfd.ioArgs.xhr.readyState;
      };
      var _2cd = function(dfd) {
        return 4 == dfd.ioArgs.xhr.readyState;
      };
      var _2ce = function(dfd) {
        var xhr = dfd.ioArgs.xhr;
        if (_29d._isDocumentOk(xhr)) {
          dfd.callback(dfd);
        } else {
          var err = new Error("Unable to load " + dfd.ioArgs.url + " status:" + xhr.status);
          err.status = xhr.status;
          err.responseText = xhr.responseText;
          dfd.errback(err);
        }
      };
      dojo._ioAddQueryToUrl = function(_2cf) {
        if (_2cf.query.length) {
          _2cf.url += (_2cf.url.indexOf("?") == -1 ? "?" : "&") + _2cf.query;
          _2cf.query = null;
        }
      };
      dojo.xhr = function(_2d0, args, _2d1) {
        var dfd = _29d._ioSetArgs(args, _2be, _2c0, _2c1);
        var _2d2 = dfd.ioArgs;
        var xhr = _2d2.xhr = _29d._xhrObj(_2d2.args);
        if (!xhr) {
          dfd.cancel();
          return dfd;
        }
        if ("postData" in args) {
          _2d2.query = args.postData;
        } else {
          if ("putData" in args) {
            _2d2.query = args.putData;
          } else {
            if ("rawBody" in args) {
              _2d2.query = args.rawBody;
            } else {
              if ((arguments.length > 2 && !_2d1) || "POST|PUT".indexOf(_2d0.toUpperCase()) == -1) {
                _29d._ioAddQueryToUrl(_2d2);
              }
            }
          }
        }
        xhr.open(_2d0, _2d2.url, args.sync !== true, args.user || undefined, args.password || undefined);
        if (args.headers) {
          for (var hdr in args.headers) {
            if (hdr.toLowerCase() === "content-type" && !args.contentType) {
              args.contentType = args.headers[hdr];
            } else {
              if (args.headers[hdr]) {
                xhr.setRequestHeader(hdr, args.headers[hdr]);
              }
            }
          }
        }
        xhr.setRequestHeader("Content-Type", args.contentType || _2cb);
        if (!args.headers || !("X-Requested-With" in args.headers)) {
          xhr.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        }
        _29d._ioNotifyStart(dfd);
        if (dojo.config.debugAtAllCosts) {
          xhr.send(_2d2.query);
        } else {
          try {
            xhr.send(_2d2.query);
          } catch (e) {
            _2d2.error = e;
            dfd.cancel();
          }
        }
        _29d._ioWatch(dfd, _2cc, _2cd, _2ce);
        xhr = null;
        return dfd;
      };
      dojo.xhrGet = function(args) {
        return _29d.xhr("GET", args);
      };
      dojo.rawXhrPost = dojo.xhrPost = function(args) {
        return _29d.xhr("POST", args, true);
      };
      dojo.rawXhrPut = dojo.xhrPut = function(args) {
        return _29d.xhr("PUT", args, true);
      };
      dojo.xhrDelete = function(args) {
        return _29d.xhr("DELETE", args);
      };
    })();
  }
  if (!dojo._hasResource["dojo._base.fx"]) {
    dojo._hasResource["dojo._base.fx"] = true;
    dojo.provide("dojo._base.fx");
    (function() {
      var d = dojo;
      var _2d3 = d._mixin;
      dojo._Line = function(_2d4, end) {
        this.start = _2d4;
        this.end = end;
      };
      dojo._Line.prototype.getValue = function(n) {
        return ((this.end - this.start) * n) + this.start;
      };
      dojo.Animation = function(args) {
        _2d3(this, args);
        if (d.isArray(this.curve)) {
          this.curve = new d._Line(this.curve[0], this.curve[1]);
        }
      };
      d._Animation = d.Animation;
      d.extend(dojo.Animation, {
        duration: 350,
        repeat: 0,
        rate: 20,
        _percent: 0,
        _startRepeatCount: 0,
        _getStep: function() {
          var _2d5 = this._percent,
            _2d6 = this.easing;
          return _2d6 ? _2d6(_2d5) : _2d5;
        },
        _fire: function(evt, args) {
          var a = args || [];
          if (this[evt]) {
            if (d.config.debugAtAllCosts) {
              this[evt].apply(this, a);
            } else {
              try {
                this[evt].apply(this, a);
              } catch (e) {}
            }
          }
          return this;
        },
        play: function(_2d7, _2d8) {
          var _2d9 = this;
          if (_2d9._delayTimer) {
            _2d9._clearTimer();
          }
          if (_2d8) {
            _2d9._stopTimer();
            _2d9._active = _2d9._paused = false;
            _2d9._percent = 0;
          } else {
            if (_2d9._active && !_2d9._paused) {
              return _2d9;
            }
          }
          _2d9._fire("beforeBegin", [_2d9.node]);
          var de = _2d7 || _2d9.delay,
            _2da = dojo.hitch(_2d9, "_play", _2d8);
          if (de > 0) {
            _2d9._delayTimer = setTimeout(_2da, de);
            return _2d9;
          }
          _2da();
          return _2d9;
        },
        _play: function(_2db) {
          var _2dc = this;
          if (_2dc._delayTimer) {
            _2dc._clearTimer();
          }
          _2dc._startTime = new Date().valueOf();
          if (_2dc._paused) {
            _2dc._startTime -= _2dc.duration * _2dc._percent;
          }
          _2dc._active = true;
          _2dc._paused = false;
          var _2dd = _2dc.curve.getValue(_2dc._getStep());
          if (!_2dc._percent) {
            if (!_2dc._startRepeatCount) {
              _2dc._startRepeatCount = _2dc.repeat;
            }
            _2dc._fire("onBegin", [_2dd]);
          }
          _2dc._fire("onPlay", [_2dd]);
          _2dc._cycle();
          return _2dc;
        },
        pause: function() {
          var _2de = this;
          if (_2de._delayTimer) {
            _2de._clearTimer();
          }
          _2de._stopTimer();
          if (!_2de._active) {
            return _2de;
          }
          _2de._paused = true;
          _2de._fire("onPause", [_2de.curve.getValue(_2de._getStep())]);
          return _2de;
        },
        gotoPercent: function(_2df, _2e0) {
          var _2e1 = this;
          _2e1._stopTimer();
          _2e1._active = _2e1._paused = true;
          _2e1._percent = _2df;
          if (_2e0) {
            _2e1.play();
          }
          return _2e1;
        },
        stop: function(_2e2) {
          var _2e3 = this;
          if (_2e3._delayTimer) {
            _2e3._clearTimer();
          }
          if (!_2e3._timer) {
            return _2e3;
          }
          _2e3._stopTimer();
          if (_2e2) {
            _2e3._percent = 1;
          }
          _2e3._fire("onStop", [_2e3.curve.getValue(_2e3._getStep())]);
          _2e3._active = _2e3._paused = false;
          return _2e3;
        },
        status: function() {
          if (this._active) {
            return this._paused ? "paused" : "playing";
          }
          return "stopped";
        },
        _cycle: function() {
          var _2e4 = this;
          if (_2e4._active) {
            var curr = new Date().valueOf();
            var step = (curr - _2e4._startTime) / (_2e4.duration);
            if (step >= 1) {
              step = 1;
            }
            _2e4._percent = step;
            if (_2e4.easing) {
              step = _2e4.easing(step);
            }
            _2e4._fire("onAnimate", [_2e4.curve.getValue(step)]);
            if (_2e4._percent < 1) {
              _2e4._startTimer();
            } else {
              _2e4._active = false;
              if (_2e4.repeat > 0) {
                _2e4.repeat--;
                _2e4.play(null, true);
              } else {
                if (_2e4.repeat == -1) {
                  _2e4.play(null, true);
                } else {
                  if (_2e4._startRepeatCount) {
                    _2e4.repeat = _2e4._startRepeatCount;
                    _2e4._startRepeatCount = 0;
                  }
                }
              }
              _2e4._percent = 0;
              _2e4._fire("onEnd", [_2e4.node]);
              !_2e4.repeat && _2e4._stopTimer();
            }
          }
          return _2e4;
        },
        _clearTimer: function() {
          clearTimeout(this._delayTimer);
          delete this._delayTimer;
        }
      });
      var ctr = 0,
        _2e5 = null,
        _2e6 = {
          run: function() {}
        };
      d.extend(d.Animation, {
        _startTimer: function() {
          if (!this._timer) {
            this._timer = d.connect(_2e6, "run", this, "_cycle");
            ctr++;
          }
          if (!_2e5) {
            _2e5 = setInterval(d.hitch(_2e6, "run"), this.rate);
          }
        },
        _stopTimer: function() {
          if (this._timer) {
            d.disconnect(this._timer);
            this._timer = null;
            ctr--;
          }
          if (ctr <= 0) {
            clearInterval(_2e5);
            _2e5 = null;
            ctr = 0;
          }
        }
      });
      var _2e7 = d.isIE ? function(node) {
        var ns = node.style;
        if (!ns.width.length && d.style(node, "width") == "auto") {
          ns.width = "auto";
        }
      } : function() {};
      dojo._fade = function(args) {
        args.node = d.byId(args.node);
        var _2e8 = _2d3({
            properties: {}
          }, args),
          _2e9 = (_2e8.properties.opacity = {});
        _2e9.start = !("start" in _2e8) ? function() {
          return +d.style(_2e8.node, "opacity") || 0;
        } : _2e8.start;
        _2e9.end = _2e8.end;
        var anim = d.animateProperty(_2e8);
        d.connect(anim, "beforeBegin", d.partial(_2e7, _2e8.node));
        return anim;
      };
      dojo.fadeIn = function(args) {
        return d._fade(_2d3({
          end: 1
        }, args));
      };
      dojo.fadeOut = function(args) {
        return d._fade(_2d3({
          end: 0
        }, args));
      };
      dojo._defaultEasing = function(n) {
        return 0.5 + ((Math.sin((n + 1.5) * Math.PI)) / 2);
      };
      var _2ea = function(_2eb) {
        this._properties = _2eb;
        for (var p in _2eb) {
          var prop = _2eb[p];
          if (prop.start instanceof d.Color) {
            prop.tempColor = new d.Color();
          }
        }
      };
      _2ea.prototype.getValue = function(r) {
        var ret = {};
        for (var p in this._properties) {
          var prop = this._properties[p],
            _2ec = prop.start;
          if (_2ec instanceof d.Color) {
            ret[p] = d.blendColors(_2ec, prop.end, r, prop.tempColor).toCss();
          } else {
            if (!d.isArray(_2ec)) {
              ret[p] = ((prop.end - _2ec) * r) + _2ec + (p != "opacity" ? prop.units || "px" : 0);
            }
          }
        }
        return ret;
      };
      dojo.animateProperty = function(args) {
        var n = args.node = d.byId(args.node);
        if (!args.easing) {
          args.easing = d._defaultEasing;
        }
        var anim = new d.Animation(args);
        d.connect(anim, "beforeBegin", anim, function() {
          var pm = {};
          for (var p in this.properties) {
            if (p == "width" || p == "height") {
              this.node.display = "block";
            }
            var prop = this.properties[p];
            if (d.isFunction(prop)) {
              prop = prop(n);
            }
            prop = pm[p] = _2d3({}, (d.isObject(prop) ? prop : {
              end: prop
            }));
            if (d.isFunction(prop.start)) {
              prop.start = prop.start(n);
            }
            if (d.isFunction(prop.end)) {
              prop.end = prop.end(n);
            }
            var _2ed = (p.toLowerCase().indexOf("color") >= 0);

            function _2ee(node, p) {
              var v = {
                height: node.offsetHeight,
                width: node.offsetWidth
              } [p];
              if (v !== undefined) {
                return v;
              }
              v = d.style(node, p);
              return (p == "opacity") ? +v : (_2ed ? v : parseFloat(v));
            };
            if (!("end" in prop)) {
              prop.end = _2ee(n, p);
            } else {
              if (!("start" in prop)) {
                prop.start = _2ee(n, p);
              }
            }
            if (_2ed) {
              prop.start = new d.Color(prop.start);
              prop.end = new d.Color(prop.end);
            } else {
              prop.start = (p == "opacity") ? +prop.start : parseFloat(prop.start);
            }
          }
          this.curve = new _2ea(pm);
        });
        d.connect(anim, "onAnimate", d.hitch(d, "style", anim.node));
        return anim;
      };
      dojo.anim = function(node, _2ef, _2f0, _2f1, _2f2, _2f3) {
        return d.animateProperty({
          node: node,
          duration: _2f0 || d.Animation.prototype.duration,
          properties: _2ef,
          easing: _2f1,
          onEnd: _2f2
        }).play(_2f3 || 0);
      };
    })();
  }
  if (!dojo._hasResource["dojo.i18n"]) {
    dojo._hasResource["dojo.i18n"] = true;
    dojo.provide("dojo.i18n");
    dojo.getObject("i18n", true, dojo);
    dojo.i18n.getLocalization = dojo.i18n.getLocalization || function(_2f4, _2f5, _2f6) {
      _2f6 = dojo.i18n.normalizeLocale(_2f6);
      var _2f7 = _2f6.split("-");
      var _2f8 = [_2f4, "nls", _2f5].join(".");
      var _2f9 = dojo._loadedModules[_2f8];
      if (_2f9) {
        var _2fa;
        for (var i = _2f7.length; i > 0; i--) {
          var loc = _2f7.slice(0, i).join("_");
          if (_2f9[loc]) {
            _2fa = _2f9[loc];
            break;
          }
        }
        if (!_2fa) {
          _2fa = _2f9.ROOT;
        }
        if (_2fa) {
          var _2fb = function() {};
          _2fb.prototype = _2fa;
          return new _2fb();
        }
      }
      throw new Error("Bundle not found: " + _2f5 + " in " + _2f4 + " , locale=" + _2f6);
    };
    dojo.i18n.normalizeLocale = function(_2fc) {
      var _2fd = _2fc ? _2fc.toLowerCase() : dojo.locale;
      if (_2fd == "root") {
        _2fd = "ROOT";
      }
      return _2fd;
    };
    dojo.i18n._requireLocalization = function(_2fe, _2ff, _300, _301) {
      var _302 = dojo.i18n.normalizeLocale(_300);
      var _303 = [_2fe, "nls", _2ff].join(".");
      var _304 = "";
      if (_301) {
        var _305 = _301.split(",");
        for (var i = 0; i < _305.length; i++) {
          if (_302["indexOf"](_305[i]) == 0) {
            if (_305[i].length > _304.length) {
              _304 = _305[i];
            }
          }
        }
        if (!_304) {
          _304 = "ROOT";
        }
      }
      var _306 = _301 ? _304 : _302;
      var _307 = dojo._loadedModules[_303];
      var _308 = null;
      if (_307) {
        if (dojo.config.localizationComplete && _307._built) {
          return;
        }
        var _309 = _306.replace(/-/g, "_");
        var _30a = _303 + "." + _309;
        _308 = dojo._loadedModules[_30a];
      }
      if (!_308) {
        _307 = dojo["provide"](_303);
        var syms = dojo._getModuleSymbols(_2fe);
        var _30b = syms.concat("nls").join("/");
        var _30c;
        dojo.i18n._searchLocalePath(_306, _301, function(loc) {
          var _30d = loc.replace(/-/g, "_");
          var _30e = _303 + "." + _30d;
          var _30f = false;
          if (!dojo._loadedModules[_30e]) {
            dojo["provide"](_30e);
            var _310 = [_30b];
            if (loc != "ROOT") {
              _310.push(loc);
            }
            _310.push(_2ff);
            var _311 = _310.join("/") + ".js";
            _30f = dojo._loadPath(_311, null, function(hash) {
              hash = hash.root || hash;
              var _312 = function() {};
              _312.prototype = _30c;
              _307[_30d] = new _312();
              for (var j in hash) {
                _307[_30d][j] = hash[j];
              }
            });
          } else {
            _30f = true;
          }
          if (_30f && _307[_30d]) {
            _30c = _307[_30d];
          } else {
            _307[_30d] = _30c;
          }
          if (_301) {
            return true;
          }
        });
      }
      if (_301 && _302 != _304) {
        _307[_302.replace(/-/g, "_")] = _307[_304.replace(/-/g, "_")];
      }
    };
    (function() {
      var _313 = dojo.config.extraLocale;
      if (_313) {
        if (!_313 instanceof Array) {
          _313 = [_313];
        }
        var req = dojo.i18n._requireLocalization;
        dojo.i18n._requireLocalization = function(m, b, _314, _315) {
          req(m, b, _314, _315);
          if (_314) {
            return;
          }
          for (var i = 0; i < _313.length; i++) {
            req(m, b, _313[i], _315);
          }
        };
      }
    })();
    dojo.i18n._searchLocalePath = function(_316, down, _317) {
      _316 = dojo.i18n.normalizeLocale(_316);
      var _318 = _316.split("-");
      var _319 = [];
      for (var i = _318.length; i > 0; i--) {
        _319.push(_318.slice(0, i).join("-"));
      }
      _319.push(false);
      if (down) {
        _319.reverse();
      }
      for (var j = _319.length - 1; j >= 0; j--) {
        var loc = _319[j] || "ROOT";
        var stop = _317(loc);
        if (stop) {
          break;
        }
      }
    };
    dojo.i18n._preloadLocalizations = function(_31a, _31b) {
      function _31c(_31d) {
        _31d = dojo.i18n.normalizeLocale(_31d);
        dojo.i18n._searchLocalePath(_31d, true, function(loc) {
          for (var i = 0; i < _31b.length; i++) {
            if (_31b[i] == loc) {
              dojo["require"](_31a + "_" + loc);
              return true;
            }
          }
          return false;
        });
      };
      _31c();
      var _31e = dojo.config.extraLocale || [];
      for (var i = 0; i < _31e.length; i++) {
        _31c(_31e[i]);
      }
    };
  }
  if (!dojo._hasResource["dojo._base.browser"]) {
    dojo._hasResource["dojo._base.browser"] = true;
    dojo.provide("dojo._base.browser");
    dojo.forEach(dojo.config.require, function(i) {
      dojo["require"](i);
    });
  }
  if (!dojo._hasResource["dojo._base"]) {
    dojo._hasResource["dojo._base"] = true;
    dojo.provide("dojo._base");
  }
  if (dojo.isBrowser && (document.readyState === "complete" || dojo.config.afterOnLoad)) {
    window.setTimeout(dojo._loadInit, 100);
  }
})();
/*
 * $Id$ Copyright (c) 2011 IBM Corporation Owner: Corporate Webmaster
 * (NUS_N_NIWWW)
 */
if (!dojo._hasResource["ibmweb.config"]) {
  dojo._hasResource["ibmweb.config"] = true;
  dojo.provide("ibmweb.config");
  ibmweb.config.set = function(_1, _2) {
    if (dojo.isString(_1)) {
      dojo.setObject(_1, _2, ibmweb.config);
    } else {
      if (dojo.isArray(_1)) {
        dojo.setObject(_1.join("."), _2, ibmweb.config);
      } else {
        if (dojo.isObject(_1)) {
          for (var _3 in _1) {
            if (dojo.isObject(_1[_3]) && !dojo.isArray(_1[_3])) {
              ibmweb.config[_3] = dojo.mixin(ibmweb.config[_3], _1[_3]);
            } else {
              ibmweb.config.set([_3], _1[_3]);
            }
          }
        }
      }
    }
  };
  ibmweb.config.get = function(_4) {
    if (dojo.isArray(_4)) {
      _4 = _4.join(".");
    }
    return dojo.getObject(_4, false, ibmweb.config);
  };
  ibmweb.config.parseMetaTags = function() {
    if (!!window.ibmwebConfig && dojo.isObject(window.ibmwebConfig)) {
      ibmweb.config.set(window.ibmwebConfig);
    }
    if (dojo.byId("ibm-top") && dojo.hasClass("ibm-top", "ibm-popup")) {
      ibmweb.config.dynnav.enabled = false;
      if (!!ibmweb.config.pagetitle) {
        if (!dojo.byId("ibm-title")) {
          dojo.create("li", {
            id: "ibm-title"
          }, "ibm-unav-links");
        }
        dojo.byId("ibm-title").innerHTML = ibmweb.config.pagetitle.htmlspecialchars();
      }
    }
    var _5 = function(_6) {
      if (_6 == "true") {
        return true;
      }
      if (_6 == "false") {
        return false;
      }
      if (parseFloat(_6) == _6) {
        return parseFloat(_6);
      }
      return _6;
    };
    for (var _7 in ibmweb.meta) {
      if (_7.indexOf("ibmweb_config_") === 0) {
        var _8 = _7.substring(11).replace("_", ".");
        var _9 = ibmweb.meta[_7];
        var _a = _9.split("||");
        for (var i = 0, j = _a.length; i < j; i++) {
          var _b = _a[i].split(":");
          if (_b.length == 1) {
            ibmweb.config.set(_8, _5(_b[0]));
          } else {
            var _c = _b.shift();
            if (_b.length == 1) {
              ibmweb.config.set([_8, _c], _5(_b[0]));
            } else {
              ibmweb.config.set([_8, _c], _b);
            }
          }
        }
      }
    }
  };
}
if (!dojo._hasResource["ibmweb.w3.config"]) {
  dojo._hasResource["ibmweb.w3.config"] = true;
  dojo.provide("ibmweb.w3.config");
  dojo.mixin(ibmweb.config, {
    config: "w3",
    noDojoLocaleOverride: false,
    usedSettings: "production",
    forced: false,
    dataUrl: "//1.w3.s81c.com/common/js/dynamicnav/w3/",
    dynNavBaseUrl: "//w3.the.ibm.com/ibmweb/swwif/?",
    w3UserAppId: "6127839ee7627460c4189e36fc6b1b01",
    w3UserServiceUrl: "//w3.ibm.com/w3restsvc/user/1.0.0/6127839ee7627460c4189e36fc6b1b01/json/hkey/",
    w3logonUrl: "https://w3.ibm.com/w3login.html?returnUrl=",
    w3StaticLogonUrl: "https://w3.ibm.com/w3login.html",
    w3UserThumbnailUrl: "https://w3-connections.ibm.com/profiles/photo.do?email=",
    imageUrl: "//1.w3.s81c.com/i/v17/",
    timeout: 10000,
    w3_eluminate: {
//      enabled: false,
//      path: "/common/js/coremetrics/",
//      CID: "51040000",
//      cmSetClientID: {
//        id: null,
//        managedFirstParty: true,
//        dataCollectionDomain: "data.coremetrics.com",
//        cookieDomain: (document.domain.indexOf("ibm.com") !== -1) ? "ibm.com" : document.domain
//      }
    },
    dynnav: {
      enabled: true
    },
    w3UserServiceTimeout: 8000,
    signin: {
      enabled: false,
      overlay: true
    },
    footer: {
      enabled: true,
      type: "overlay",
      feedbackLink: ""
    },
    footermenu: {
      enabled: true
    },
    megamenu: {
      enabled: true,
      linksonly: false,
      disabledLocales: ["ilhe"],
      maxColumns: 6,
      maxRows: 8,
      noScroll: false,
      minimizeByDefault: false,
      expandOnClick: false,
      delay: 0,
      icons: "none",
      disableUniversalNav: false
    },
    easyaccess: {
      enabled: false
    },
    greeting: {
      enabled: false,
      signinWidget: "ibmweb.dynnav.w3signin"
    },
    localeselector: {
      enabled: true,
      maxitems: 12
    },
    video: {
      enabled: true
    },
    facestypeahead: {
      enabled: true
    },
    search: {
      before: [],
      after: [],
      w3SearchUrl: "//w3.ibm.com/search/do/search?qt=${query}&langopt=en&la=all&w3scope=w3General&queryType=simple&pageType=results&lk=&sortby=&pageNumber=1&tabName=w3&q=${query}&hkey=&op0=%2B&ty0=w&op1=%2B&ty1=p&op2=&ty2=w&op3=-&ty3=w&fl0=&tx0=&tx1=&tx2=&tx3=&ftselect=ao&filetype=any&w3scope=w3General&function=masthead&url=",
      profileSearchUrl: "//w3.ibm.com/connections/profiles/html/simpleSearch.do?searchBy=name&searchFor=${query}&lang=en&function=masthead",
      wwwSearchUrl: "//www.ibm.com/search/csass/search?sn=mh&q=${query}&lang=en&cc=us&en=utf&function=masthead",
      connectionSearchUrl: "//w3.ibm.com/connections/search/web/search?query=${query}&function=masthead",
      bluepageSearchUrl: "//w3.ibm.com/newbp/?s=${query}",
      learnSearchUrl: "https://w3-01.sso.ibm.com/hr/careersmart/html/find.html#tabId=FS&searchString=${query}&function=masthead",
      qaSearchUrl: "//w3-950.ibm.com/wwqa/wwqa.nsf/wwqalogon?OpenForm&corp&w3s=${query}&function=masthead"
    },
    sbs: {
      enabled: true,
      email: false,
      print: true
    },
    bitly: {
      login: "ibmdigital",
      key: "R_1ed9169df98d3b072731d88ec80d18a7",
      timeout: 3000
    },
    backtocountry: {
      enabled: false,
      id: "ibm-backtocountry",
      keepfilename: true
    },
    appmast: {
      enabled: false
    },
    subtabs: {
      viewtext: {
        all: "View all...",
        less: "View less..."
      }
    }
  });
  (function(_d) {
    _d = _d || "";
    if (_d) {
      ibmweb.config.forced = true;
    }
    var _e = document.cookie.match(/__DOJO=(\w*)/);
    if (_e) {
      _d = _e[1];
    }
    if (!_d && !location.hostname.match(/\.ibm\.com$/)) {
      _d = "development";
    }
    _d = _d || "production";
    ibmweb.config.usedSettings = _d;
    if (_d == "staging") {
      ibmweb.config.set({
        dataUrl: "//1.wwwstage.s81c.com/common/js/dynamicnav/w3/",
        dynNavBaseUrl: "https://stage.the.ibm.com/ibmweb/swwif/?",
        imageUrl: "//1.wwwstage.s81c.com/i/v17/"
      });
    }
    if (_d == "w3b") {
      ibmweb.config.set({
        dataUrl: "//w3b.ibm.com/common/js/dynamicnav/w3/",
        dynNavBaseUrl: "//w3.the.ibm.com/ibmweb/swwif/?",
        imageUrl: "//w3b.ibm.com/i/v17/"
      });
    }
    if (_d == "wwwpreview") {
      ibmweb.config.set({
        dataUrl: "//1.wwwpreview.s81c.com/common/js/dynamicnav/w3/",
        imageUrl: "//1.wwwpreview.s81c.com/i/v17/",
        dynNavBaseUrl: "https://stage.the.ibm.com/ibmweb/swwif/?",
        search: {
          w3SearchUrl: "//w3.ibm.com/search/do/search?qt=${query}&langopt=en&la=all&w3scope=w3General&queryType=simple&pageType=results&lk=&sortby=&pageNumber=1&tabName=w3&q=${query}&hkey=&op0=%2B&ty0=w&op1=%2B&ty1=p&op2=&ty2=w&op3=-&ty3=w&fl0=&tx0=&tx1=&tx2=&tx3=&ftselect=ao&filetype=any&w3scope=w3General&function=masthead&url="
        }
      });
    }
    if (_d == "development") {
      ibmweb.config.set({
        dataUrl: "//1.w3.s81c.com/common/js/dynamicnav/w3/",
        dynNavBaseUrl: "https://stage.the.ibm.com/ibmweb/swwif/?",
        imageUrl: "/i/v17/",
        annotatorServUrl: "https://iebalpha.cdtdp.toronto.ca.ibm.com:3443/transform/public/service/common_profile2/profile/shared/",
        search: {
          w3SearchUrl: "//w3.ibm.com/search/do/search?qt=${query}&langopt=en&la=all&w3scope=w3General&queryType=simple&pageType=results&lk=&sortby=&pageNumber=1&tabName=w3&q=${query}&hkey=&op0=%2B&ty0=w&op1=%2B&ty1=p&op2=&ty2=w&op3=-&ty3=w&fl0=&tx0=&tx1=&tx2=&tx3=&ftselect=ao&filetype=any&w3scope=w3General&function=masthead&url="
        }
      });
    }
  })(dojo.config.ibmForceConfig);
}
if (!dojo._hasResource["ibmweb.dojoReadyForLegacyDojo"]) {
  dojo._hasResource["ibmweb.dojoReadyForLegacyDojo"] = true;
  dojo.provide("ibmweb.dojoReadyForLegacyDojo");
  (function() {
    dojo.ready = dojo.ready || dojo.addOnLoad;
  })();
}
if (!dojo._hasResource["ibmweb.moduleDomain"]) {
  dojo._hasResource["ibmweb.moduleDomain"] = true;
  dojo.provide("ibmweb.moduleDomain");
  ibmweb.moduleDomain.get = function() {
    var _f = getModuleUrl().match(/\/\/([^\/]*)\//);
    if (_f) {
      return _f[0];
    } else {
      if (getModuleUrl().substr(0, 1) === "/") {
        return "//" + window.location.host + "/";
      }
    }
    return "//1.www.s81c.com/";
  };

  function getModuleUrl() {
    if (dojo && dojo.moduleUrl) {
      var _10 = dojo.moduleUrl("ibmweb");
      if (typeof(_10) === "string") {
        return _10;
      } else {
        if (_10.uri && typeof(_10.uri) === "string") {
          return _10.uri;
        }
      }
    }
    return "";
  };
}
if (!dojo._hasResource["ibmweb.coremetricsGhostFunctions"]) {
  dojo._hasResource["ibmweb.coremetricsGhostFunctions"] = true;
  dojo.provide("ibmweb.coremetricsGhostFunctions");
  (function() {
    var _11 = ["cmCreatePageviewTag", "cmCreateProductviewTag", "cmCreateShopAction5Tag", "cmDisplayShops", "cmCreateShopAction9Tag", "cmCreateOrderTag", "cmCreateRegistrationTag", "cmCreateElementTag", "cmCreateConversionEventTag", "cmCreateManualPageviewTag", "cmCreateManualLinkClickTag", "cmCreateManualImpressionTag", "cmCreateCustomTag", "cmSetupOther"],
      _12 = [];
    (function init() {
      if (!_13()) {
        for (var i = 0; i < _11.length; i++) {
          _14(_11[i]);
        }
        _15();
      }
    })();

    function _13() {
      for (var i = 0; i < _11.length; i++) {
        if (typeof(window[_11[i]]) !== "function" || window[_11[i]].isGhost) {
          return false;
        }
      }
      return true;
    };

    function _14(_16) {
      window[_16] = function() {
        _12.push({
          functionName: _16,
          args: arguments
        });
      };
      window[_16].isGhost = true;
    };

    function _15() {
      setTimeout(function() {
        if (_13()) {
          _17();
        } else {
          _15();
        }
      }, 50);
    };

    function _17() {
      for (var i = 0; i < _12.length; i++) {
        window[_12[i].functionName].apply(this, _12[i].args);
      }
    };
  })();
}
if (!dojo._hasResource["ibmweb.cookieCheck"]) {
  dojo._hasResource["ibmweb.cookieCheck"] = true;
  dojo.provide("ibmweb.cookieCheck");
  var x, str;
  var h = false;
  var cookies = document.cookie.split(";");
  for (i = 0; i < cookies.length; i++) {
    x = cookies[i].substr(0, cookies[i].indexOf("="));
    str = cookies[i].substr(cookies[i].indexOf("=") + 1);
    x = x.replace(/^\s+|\s+$/g, "");
    var cookieCheck = x.match("CookieChecker");
    if (cookieCheck) {
      h = true;
      break;
    }
  }
  if (h == false) {
    document.cookie = "CookieChecker=set;path=/;expires=Tue, 31 Dec 19 12:00:00 UTC";
    document.cookie = "CoreID6=;path=/;expires=Thu, 01-Jan-1970 00:00:01 GMT";
  }
}
if (!dojo._hasResource["dijit._base.manager"]) {
  dojo._hasResource["dijit._base.manager"] = true;
  dojo.provide("dijit._base.manager");
  dojo.declare("dijit.WidgetSet", null, {
    constructor: function() {
      this._hash = {};
      this.length = 0;
    },
    add: function(_18) {
      if (this._hash[_18.id]) {
        throw new Error("Tried to register widget with id==" + _18.id + " but that id is already registered");
      }
      this._hash[_18.id] = _18;
      this.length++;
    },
    remove: function(id) {
      if (this._hash[id]) {
        delete this._hash[id];
        this.length--;
      }
    },
    forEach: function(_19, _1a) {
      _1a = _1a || dojo.global;
      var i = 0,
        id;
      for (id in this._hash) {
        _19.call(_1a, this._hash[id], i++, this._hash);
      }
      return this;
    },
    filter: function(_1b, _1c) {
      _1c = _1c || dojo.global;
      var res = new dijit.WidgetSet(),
        i = 0,
        id;
      for (id in this._hash) {
        var w = this._hash[id];
        if (_1b.call(_1c, w, i++, this._hash)) {
          res.add(w);
        }
      }
      return res;
    },
    byId: function(id) {
      return this._hash[id];
    },
    byClass: function(cls) {
      var res = new dijit.WidgetSet(),
        id, _1d;
      for (id in this._hash) {
        _1d = this._hash[id];
        if (_1d.declaredClass == cls) {
          res.add(_1d);
        }
      }
      return res;
    },
    toArray: function() {
      var ar = [];
      for (var id in this._hash) {
        ar.push(this._hash[id]);
      }
      return ar;
    },
    map: function(_1e, _1f) {
      return dojo.map(this.toArray(), _1e, _1f);
    },
    every: function(_20, _21) {
      _21 = _21 || dojo.global;
      var x = 0,
        i;
      for (i in this._hash) {
        if (!_20.call(_21, this._hash[i], x++, this._hash)) {
          return false;
        }
      }
      return true;
    },
    some: function(_22, _23) {
      _23 = _23 || dojo.global;
      var x = 0,
        i;
      for (i in this._hash) {
        if (_22.call(_23, this._hash[i], x++, this._hash)) {
          return true;
        }
      }
      return false;
    }
  });
  (function() {
    dijit.registry = new dijit.WidgetSet();
    var _24 = dijit.registry._hash,
      _25 = dojo.attr,
      _26 = dojo.hasAttr,
      _27 = dojo.style;
    dijit.byId = function(id) {
      return typeof id == "string" ? _24[id] : id;
    };
    var _28 = {};
    dijit.getUniqueId = function(_29) {
      var id;
      do {
        id = _29 + "_" + (_29 in _28 ? ++_28[_29] : _28[_29] = 0);
      } while (_24[id]);
      return dijit._scopeName == "dijit" ? id : dijit._scopeName + "_" + id;
    };
    dijit.findWidgets = function(_2a) {
      var _2b = [];

      function _2c(_2d) {
        for (var _2e = _2d.firstChild; _2e; _2e = _2e.nextSibling) {
          if (_2e.nodeType == 1) {
            var _2f = _2e.getAttribute("widgetId");
            if (_2f) {
              var _30 = _24[_2f];
              if (_30) {
                _2b.push(_30);
              }
            } else {
              _2c(_2e);
            }
          }
        }
      };
      _2c(_2a);
      return _2b;
    };
    dijit._destroyAll = function() {
      dijit._curFocus = null;
      dijit._prevFocus = null;
      dijit._activeStack = [];
      dojo.forEach(dijit.findWidgets(dojo.body()), function(_31) {
        if (!_31._destroyed) {
          if (_31.destroyRecursive) {
            _31.destroyRecursive();
          } else {
            if (_31.destroy) {
              _31.destroy();
            }
          }
        }
      });
    };
    if (dojo.isIE) {
      dojo.addOnWindowUnload(function() {
        dijit._destroyAll();
      });
    }
    dijit.byNode = function(_32) {
      return _24[_32.getAttribute("widgetId")];
    };
    dijit.getEnclosingWidget = function(_33) {
      while (_33) {
        var id = _33.getAttribute && _33.getAttribute("widgetId");
        if (id) {
          return _24[id];
        }
        _33 = _33.parentNode;
      }
      return null;
    };
    var _34 = (dijit._isElementShown = function(_35) {
      var s = _27(_35);
      return (s.visibility != "hidden") && (s.visibility != "collapsed") && (s.display != "none") && (_25(_35, "type") != "hidden");
    });
    dijit.hasDefaultTabStop = function(_36) {
      switch (_36.nodeName.toLowerCase()) {
        case "a":
          return _26(_36, "href");
        case "area":
        case "button":
        case "input":
        case "object":
        case "select":
        case "textarea":
          return true;
        case "iframe":
          var _37;
          try {
            var _38 = _36.contentDocument;
            if ("designMode" in _38 && _38.designMode == "on") {
              return true;
            }
            _37 = _38.body;
          } catch (e1) {
            try {
              _37 = _36.contentWindow.document.body;
            } catch (e2) {
              return false;
            }
          }
          return _37.contentEditable == "true" || (_37.firstChild && _37.firstChild.contentEditable == "true");
        default:
          return _36.contentEditable == "true";
      }
    };
    var _39 = (dijit.isTabNavigable = function(_3a) {
      if (_25(_3a, "disabled")) {
        return false;
      } else {
        if (_26(_3a, "tabIndex")) {
          return _25(_3a, "tabIndex") >= 0;
        } else {
          return dijit.hasDefaultTabStop(_3a);
        }
      }
    });
    dijit._getTabNavigable = function(_3b) {
      var _3c, _3d, _3e, _3f, _40, _41, _42 = {};

      function _43(_44) {
        return _44 && _44.tagName.toLowerCase() == "input" && _44.type && _44.type.toLowerCase() == "radio" && _44.name && _44.name.toLowerCase();
      };
      var _45 = function(_46) {
        dojo.query("> *", _46).forEach(function(_47) {
          if ((dojo.isIE && _47.scopeName !== "HTML") || !_34(_47)) {
            return;
          }
          if (_39(_47)) {
            var _48 = _25(_47, "tabIndex");
            if (!_26(_47, "tabIndex") || _48 == 0) {
              if (!_3c) {
                _3c = _47;
              }
              _3d = _47;
            } else {
              if (_48 > 0) {
                if (!_3e || _48 < _3f) {
                  _3f = _48;
                  _3e = _47;
                }
                if (!_40 || _48 >= _41) {
                  _41 = _48;
                  _40 = _47;
                }
              }
            }
            var rn = _43(_47);
            if (dojo.attr(_47, "checked") && rn) {
              _42[rn] = _47;
            }
          }
          if (_47.nodeName.toUpperCase() != "SELECT") {
            _45(_47);
          }
        });
      };
      if (_34(_3b)) {
        _45(_3b);
      }

      function rs(_49) {
        return _42[_43(_49)] || _49;
      };
      return {
        first: rs(_3c),
        last: rs(_3d),
        lowest: rs(_3e),
        highest: rs(_40)
      };
    };
    dijit.getFirstInTabbingOrder = function(_4a) {
      var _4b = dijit._getTabNavigable(dojo.byId(_4a));
      return _4b.lowest ? _4b.lowest : _4b.first;
    };
    dijit.getLastInTabbingOrder = function(_4c) {
      var _4d = dijit._getTabNavigable(dojo.byId(_4c));
      return _4d.last ? _4d.last : _4d.highest;
    };
    dijit.defaultDuration = dojo.config["defaultDuration"] || 200;
  })();
}
if (!dojo._hasResource["dojo.Stateful"]) {
  dojo._hasResource["dojo.Stateful"] = true;
  dojo.provide("dojo.Stateful");
  dojo.declare("dojo.Stateful", null, {
    postscript: function(_4e) {
      if (_4e) {
        dojo.mixin(this, _4e);
      }
    },
    get: function(_4f) {
      return this[_4f];
    },
    set: function(_50, _51) {
      if (typeof _50 === "object") {
        for (var x in _50) {
          this.set(x, _50[x]);
        }
        return this;
      }
      var _52 = this[_50];
      this[_50] = _51;
      if (this._watchCallbacks) {
        this._watchCallbacks(_50, _52, _51);
      }
      return this;
    },
    watch: function(_53, _54) {
      var _55 = this._watchCallbacks;
      if (!_55) {
        var _56 = this;
        _55 = this._watchCallbacks = function(_57, _58, _59, _5a) {
          var _5b = function(_5c) {
            if (_5c) {
              _5c = _5c.slice();
              for (var i = 0, l = _5c.length; i < l; i++) {
                try {
                  _5c[i].call(_56, _57, _58, _59);
                } catch (e) {}
              }
            }
          };
          _5b(_55["_" + _57]);
          if (!_5a) {
            _5b(_55["*"]);
          }
        };
      }
      if (!_54 && typeof _53 === "function") {
        _54 = _53;
        _53 = "*";
      } else {
        _53 = "_" + _53;
      }
      var _5d = _55[_53];
      if (typeof _5d !== "object") {
        _5d = _55[_53] = [];
      }
      _5d.push(_54);
      return {
        unwatch: function() {
          _5d.splice(dojo.indexOf(_5d, _54), 1);
        }
      };
    }
  });
}
if (!dojo._hasResource["dijit._WidgetBase"]) {
  dojo._hasResource["dijit._WidgetBase"] = true;
  dojo.provide("dijit._WidgetBase");
  (function() {
    dojo.declare("dijit._WidgetBase", dojo.Stateful, {
      id: "",
      lang: "",
      dir: "",
      "class": "",
      style: "",
      title: "",
      tooltip: "",
      baseClass: "",
      srcNodeRef: null,
      domNode: null,
      containerNode: null,
      attributeMap: {
        id: "",
        dir: "",
        lang: "",
        "class": "",
        style: "",
        title: ""
      },
      _blankGif: (dojo.config.blankGif || dojo.moduleUrl("dojo", "resources/blank.gif")).toString(),
      postscript: function(_5e, _5f) {
        this.create(_5e, _5f);
      },
      create: function(_60, _61) {
        this.srcNodeRef = dojo.byId(_61);
        this._connects = [];
        this._subscribes = [];
        if (this.srcNodeRef && (typeof this.srcNodeRef.id == "string")) {
          this.id = this.srcNodeRef.id;
        }
        if (_60) {
          this.params = _60;
          dojo._mixin(this, _60);
        }
        this.postMixInProperties();
        if (!this.id) {
          this.id = dijit.getUniqueId(this.declaredClass.replace(/\./g, "_"));
        }
        dijit.registry.add(this);
        this.buildRendering();
        if (this.domNode) {
          this._applyAttributes();
          var _62 = this.srcNodeRef;
          if (_62 && _62.parentNode && this.domNode !== _62) {
            _62.parentNode.replaceChild(this.domNode, _62);
          }
        }
        if (this.domNode) {
          this.domNode.setAttribute("widgetId", this.id);
        }
        this.postCreate();
        if (this.srcNodeRef && !this.srcNodeRef.parentNode) {
          delete this.srcNodeRef;
        }
        this._created = true;
      },
      _applyAttributes: function() {
        var _63 = function(_64, _65) {
          if ((_65.params && _64 in _65.params) || _65[_64]) {
            _65.set(_64, _65[_64]);
          }
        };
        for (var _66 in this.attributeMap) {
          _63(_66, this);
        }
        dojo.forEach(this._getSetterAttributes(), function(a) {
          if (!(a in this.attributeMap)) {
            _63(a, this);
          }
        }, this);
      },
      _getSetterAttributes: function() {
        var _67 = this.constructor;
        if (!_67._setterAttrs) {
          var r = (_67._setterAttrs = []),
            _68, _69 = _67.prototype;
          for (var _6a in _69) {
            if (dojo.isFunction(_69[_6a]) && (_68 = _6a.match(/^_set([a-zA-Z]*)Attr$/)) && _68[1]) {
              r.push(_68[1].charAt(0).toLowerCase() + _68[1].substr(1));
            }
          }
        }
        return _67._setterAttrs;
      },
      postMixInProperties: function() {},
      buildRendering: function() {
        if (!this.domNode) {
          this.domNode = this.srcNodeRef || dojo.create("div");
        }
        if (this.baseClass) {
          var _6b = this.baseClass.split(" ");
          if (!this.isLeftToRight()) {
            _6b = _6b.concat(dojo.map(_6b, function(_6c) {
              return _6c + "Rtl";
            }));
          }
          dojo.addClass(this.domNode, _6b);
        }
      },
      postCreate: function() {},
      startup: function() {
        this._started = true;
      },
      destroyRecursive: function(_6d) {
        this._beingDestroyed = true;
        this.destroyDescendants(_6d);
        this.destroy(_6d);
      },
      destroy: function(_6e) {
        this._beingDestroyed = true;
        this.uninitialize();
        var d = dojo,
          dfe = d.forEach,
          dun = d.unsubscribe;
        dfe(this._connects, function(_6f) {
          dfe(_6f, d.disconnect);
        });
        dfe(this._subscribes, function(_70) {
          dun(_70);
        });
        dfe(this._supportingWidgets || [], function(w) {
          if (w.destroyRecursive) {
            w.destroyRecursive();
          } else {
            if (w.destroy) {
              w.destroy();
            }
          }
        });
        this.destroyRendering(_6e);
        dijit.registry.remove(this.id);
        this._destroyed = true;
      },
      destroyRendering: function(_71) {
        if (this.bgIframe) {
          this.bgIframe.destroy(_71);
          delete this.bgIframe;
        }
        if (this.domNode) {
          if (_71) {
            dojo.removeAttr(this.domNode, "widgetId");
          } else {
            dojo.destroy(this.domNode);
          }
          delete this.domNode;
        }
        if (this.srcNodeRef) {
          if (!_71) {
            dojo.destroy(this.srcNodeRef);
          }
          delete this.srcNodeRef;
        }
      },
      destroyDescendants: function(_72) {
        dojo.forEach(this.getChildren(), function(_73) {
          if (_73.destroyRecursive) {
            _73.destroyRecursive(_72);
          }
        });
      },
      uninitialize: function() {
        return false;
      },
      _setClassAttr: function(_74) {
        var _75 = this[this.attributeMap["class"] || "domNode"];
        dojo.replaceClass(_75, _74, this["class"]);
        this._set("class", _74);
      },
      _setStyleAttr: function(_76) {
        var _77 = this[this.attributeMap.style || "domNode"];
        if (dojo.isObject(_76)) {
          dojo.style(_77, _76);
        } else {
          if (_77.style.cssText) {
            _77.style.cssText += "; " + _76;
          } else {
            _77.style.cssText = _76;
          }
        }
        this._set("style", _76);
      },
      _attrToDom: function(_78, _79) {
        var _7a = this.attributeMap[_78];
        dojo.forEach(dojo.isArray(_7a) ? _7a : [_7a], function(_7b) {
          var _7c = this[_7b.node || _7b || "domNode"];
          var _7d = _7b.type || "attribute";
          switch (_7d) {
            case "attribute":
              if (dojo.isFunction(_79)) {
                _79 = dojo.hitch(this, _79);
              }
              var _7e = _7b.attribute ? _7b.attribute : (/^on[A-Z][a-zA-Z]*$/.test(_78) ? _78.toLowerCase() : _78);
              dojo.attr(_7c, _7e, _79);
              break;
            case "innerText":
              _7c.innerHTML = "";
              _7c.appendChild(dojo.doc.createTextNode(_79));
              break;
            case "innerHTML":
              _7c.innerHTML = _79;
              break;
            case "class":
              dojo.replaceClass(_7c, _79, this[_78]);
              break;
          }
        }, this);
      },
      get: function(_7f) {
        var _80 = this._getAttrNames(_7f);
        return this[_80.g] ? this[_80.g]() : this[_7f];
      },
      set: function(_81, _82) {
        if (typeof _81 === "object") {
          for (var x in _81) {
            this.set(x, _81[x]);
          }
          return this;
        }
        var _83 = this._getAttrNames(_81);
        if (this[_83.s]) {
          var _84 = this[_83.s].apply(this, Array.prototype.slice.call(arguments, 1));
        } else {
          if (_81 in this.attributeMap) {
            this._attrToDom(_81, _82);
          }
          this._set(_81, _82);
        }
        return _84 || this;
      },
      _attrPairNames: {},
      _getAttrNames: function(_85) {
        var apn = this._attrPairNames;
        if (apn[_85]) {
          return apn[_85];
        }
        var uc = _85.charAt(0).toUpperCase() + _85.substr(1);
        return (apn[_85] = {
          n: _85 + "Node",
          s: "_set" + uc + "Attr",
          g: "_get" + uc + "Attr"
        });
      },
      _set: function(_86, _87) {
        var _88 = this[_86];
        this[_86] = _87;
        if (this._watchCallbacks && this._created && _87 !== _88) {
          this._watchCallbacks(_86, _88, _87);
        }
      },
      toString: function() {
        return "[Widget " + this.declaredClass + ", " + (this.id || "NO ID") + "]";
      },
      getDescendants: function() {
        return this.containerNode ? dojo.query("[widgetId]", this.containerNode).map(dijit.byNode) : [];
      },
      getChildren: function() {
        return this.containerNode ? dijit.findWidgets(this.containerNode) : [];
      },
      connect: function(obj, _89, _8a) {
        var _8b = [dojo._connect(obj, _89, this, _8a)];
        this._connects.push(_8b);
        return _8b;
      },
      disconnect: function(_8c) {
        for (var i = 0; i < this._connects.length; i++) {
          if (this._connects[i] == _8c) {
            dojo.forEach(_8c, dojo.disconnect);
            this._connects.splice(i, 1);
            return;
          }
        }
      },
      subscribe: function(_8d, _8e) {
        var _8f = dojo.subscribe(_8d, this, _8e);
        this._subscribes.push(_8f);
        return _8f;
      },
      unsubscribe: function(_90) {
        for (var i = 0; i < this._subscribes.length; i++) {
          if (this._subscribes[i] == _90) {
            dojo.unsubscribe(_90);
            this._subscribes.splice(i, 1);
            return;
          }
        }
      },
      isLeftToRight: function() {
        return this.dir ? (this.dir == "ltr") : dojo._isBodyLtr();
      },
      placeAt: function(_91, _92) {
        if (_91.declaredClass && _91.addChild) {
          _91.addChild(this, _92);
        } else {
          dojo.place(this.domNode, _91, _92);
        }
        return this;
      }
    });
  })();
}
if (!dojo._hasResource["dojo.window"]) {
  dojo._hasResource["dojo.window"] = true;
  dojo.provide("dojo.window");
  dojo.getObject("window", true, dojo);
  dojo.window.getBox = function() {
    var _93 = (dojo.doc.compatMode == "BackCompat") ? dojo.body() : dojo.doc.documentElement;
    var _94 = dojo._docScroll();
    return {
      w: _93.clientWidth,
      h: _93.clientHeight,
      l: _94.x,
      t: _94.y
    };
  };
  dojo.window.get = function(doc) {
    if (dojo.isIE && window !== document.parentWindow) {
      doc.parentWindow.execScript("document._parentWindow = window;", "Javascript");
      var win = doc._parentWindow;
      doc._parentWindow = null;
      return win;
    }
    return doc.parentWindow || doc.defaultView;
  };
  dojo.window.scrollIntoView = function(_95, pos) {
    try {
      _95 = dojo.byId(_95);
      var doc = _95.ownerDocument || dojo.doc,
        _96 = doc.body || dojo.body(),
        _97 = doc.documentElement || _96.parentNode,
        _98 = dojo.isIE,
        _99 = dojo.isWebKit;
      if ((!(dojo.isMoz || _98 || _99 || dojo.isOpera) || _95 == _96 || _95 == _97) && (typeof _95.scrollIntoView != "undefined")) {
        _95.scrollIntoView(false);
        return;
      }
      var _9a = doc.compatMode == "BackCompat",
        _9b = (_98 >= 9 && _95.ownerDocument.parentWindow.frameElement) ? ((_97.clientHeight > 0 && _97.clientWidth > 0 && (_96.clientHeight == 0 || _96.clientWidth == 0 || _96.clientHeight > _97.clientHeight || _96.clientWidth > _97.clientWidth)) ? _97 : _96) : (_9a ? _96 : _97),
        _9c = _99 ? _96 : _9b,
        _9d = _9b.clientWidth,
        _9e = _9b.clientHeight,
        rtl = !dojo._isBodyLtr(),
        _9f = pos || dojo.position(_95),
        el = _95.parentNode,
        _a0 = function(el) {
          return ((_98 <= 6 || (_98 && _9a)) ? false : (dojo.style(el, "position").toLowerCase() == "fixed"));
        };
      if (_a0(_95)) {
        return;
      }
      while (el) {
        if (el == _96) {
          el = _9c;
        }
        var _a1 = dojo.position(el),
          _a2 = _a0(el);
        if (el == _9c) {
          _a1.w = _9d;
          _a1.h = _9e;
          if (_9c == _97 && _98 && rtl) {
            _a1.x += _9c.offsetWidth - _a1.w;
          }
          if (_a1.x < 0 || !_98) {
            _a1.x = 0;
          }
          if (_a1.y < 0 || !_98) {
            _a1.y = 0;
          }
        } else {
          var pb = dojo._getPadBorderExtents(el);
          _a1.w -= pb.w;
          _a1.h -= pb.h;
          _a1.x += pb.l;
          _a1.y += pb.t;
          var _a3 = el.clientWidth,
            _a4 = _a1.w - _a3;
          if (_a3 > 0 && _a4 > 0) {
            _a1.w = _a3;
            _a1.x += (rtl && (_98 || el.clientLeft > pb.l)) ? _a4 : 0;
          }
          _a3 = el.clientHeight;
          _a4 = _a1.h - _a3;
          if (_a3 > 0 && _a4 > 0) {
            _a1.h = _a3;
          }
        }
        if (_a2) {
          if (_a1.y < 0) {
            _a1.h += _a1.y;
            _a1.y = 0;
          }
          if (_a1.x < 0) {
            _a1.w += _a1.x;
            _a1.x = 0;
          }
          if (_a1.y + _a1.h > _9e) {
            _a1.h = _9e - _a1.y;
          }
          if (_a1.x + _a1.w > _9d) {
            _a1.w = _9d - _a1.x;
          }
        }
        var l = _9f.x - _a1.x,
          t = _9f.y - Math.max(_a1.y, 0),
          r = l + _9f.w - _a1.w,
          bot = t + _9f.h - _a1.h;
        if (r * l > 0) {
          var s = Math[l < 0 ? "max" : "min"](l, r);
          if (rtl && ((_98 == 8 && !_9a) || _98 >= 9)) {
            s = -s;
          }
          _9f.x += el.scrollLeft;
          el.scrollLeft += s;
          _9f.x -= el.scrollLeft;
        }
        if (bot * t > 0) {
          _9f.y += el.scrollTop;
          el.scrollTop += Math[t < 0 ? "max" : "min"](t, bot);
          _9f.y -= el.scrollTop;
        }
        el = (el != _9c) && !_a2 && el.parentNode;
      }
    } catch (error) {
      _95.scrollIntoView(false);
    }
  };
}
if (!dojo._hasResource["dijit._base.focus"]) {
  dojo._hasResource["dijit._base.focus"] = true;
  dojo.provide("dijit._base.focus");
  dojo.mixin(dijit, {
    _curFocus: null,
    _prevFocus: null,
    isCollapsed: function() {
      return dijit.getBookmark().isCollapsed;
    },
    getBookmark: function() {
      var bm, rg, tg, sel = dojo.doc.selection,
        cf = dijit._curFocus;
      if (dojo.global.getSelection) {
        sel = dojo.global.getSelection();
        if (sel) {
          if (sel.isCollapsed) {
            tg = cf ? cf.tagName : "";
            if (tg) {
              tg = tg.toLowerCase();
              if (tg == "textarea" || (tg == "input" && (!cf.type || cf.type.toLowerCase() == "text"))) {
                sel = {
                  start: cf.selectionStart,
                  end: cf.selectionEnd,
                  node: cf,
                  pRange: true
                };
                return {
                  isCollapsed: (sel.end <= sel.start),
                  mark: sel
                };
              }
            }
            bm = {
              isCollapsed: true
            };
            if (sel.rangeCount) {
              bm.mark = sel.getRangeAt(0).cloneRange();
            }
          } else {
            rg = sel.getRangeAt(0);
            bm = {
              isCollapsed: false,
              mark: rg.cloneRange()
            };
          }
        }
      } else {
        if (sel) {
          tg = cf ? cf.tagName : "";
          tg = tg.toLowerCase();
          if (cf && tg && (tg == "button" || tg == "textarea" || tg == "input")) {
            if (sel.type && sel.type.toLowerCase() == "none") {
              return {
                isCollapsed: true,
                mark: null
              };
            } else {
              rg = sel.createRange();
              return {
                isCollapsed: rg.text && rg.text.length ? false : true,
                mark: {
                  range: rg,
                  pRange: true
                }
              };
            }
          }
          bm = {};
          try {
            rg = sel.createRange();
            bm.isCollapsed = !(sel.type == "Text" ? rg.htmlText.length : rg.length);
          } catch (e) {
            bm.isCollapsed = true;
            return bm;
          }
          if (sel.type.toUpperCase() == "CONTROL") {
            if (rg.length) {
              bm.mark = [];
              var i = 0,
                len = rg.length;
              while (i < len) {
                bm.mark.push(rg.item(i++));
              }
            } else {
              bm.isCollapsed = true;
              bm.mark = null;
            }
          } else {
            bm.mark = rg.getBookmark();
          }
        } else {}
      }
      return bm;
    },
    moveToBookmark: function(_a5) {
      var _a6 = dojo.doc,
        _a7 = _a5.mark;
      if (_a7) {
        if (dojo.global.getSelection) {
          var sel = dojo.global.getSelection();
          if (sel && sel.removeAllRanges) {
            if (_a7.pRange) {
              var r = _a7;
              var n = r.node;
              n.selectionStart = r.start;
              n.selectionEnd = r.end;
            } else {
              sel.removeAllRanges();
              sel.addRange(_a7);
            }
          } else {}
        } else {
          if (_a6.selection && _a7) {
            var rg;
            if (_a7.pRange) {
              rg = _a7.range;
            } else {
              if (dojo.isArray(_a7)) {
                rg = _a6.body.createControlRange();
                dojo.forEach(_a7, function(n) {
                  rg.addElement(n);
                });
              } else {
                rg = _a6.body.createTextRange();
                rg.moveToBookmark(_a7);
              }
            }
            rg.select();
          }
        }
      }
    },
    getFocus: function(_a8, _a9) {
      var _aa = !dijit._curFocus || (_a8 && dojo.isDescendant(dijit._curFocus, _a8.domNode)) ? dijit._prevFocus : dijit._curFocus;
      return {
        node: _aa,
        bookmark: (_aa == dijit._curFocus) && dojo.withGlobal(_a9 || dojo.global, dijit.getBookmark),
        openedForWindow: _a9
      };
    },
    focus: function(_ab) {
      if (!_ab) {
        return;
      }
      var _ac = "node" in _ab ? _ab.node : _ab,
        _ad = _ab.bookmark,
        _ae = _ab.openedForWindow,
        _af = _ad ? _ad.isCollapsed : false;
      if (_ac) {
        var _b0 = (_ac.tagName.toLowerCase() == "iframe") ? _ac.contentWindow : _ac;
        if (_b0 && _b0.focus) {
          try {
            _b0.focus();
          } catch (e) {}
        }
        dijit._onFocusNode(_ac);
      }
      if (_ad && dojo.withGlobal(_ae || dojo.global, dijit.isCollapsed) && !_af) {
        if (_ae) {
          _ae.focus();
        }
        try {
          dojo.withGlobal(_ae || dojo.global, dijit.moveToBookmark, null, [_ad]);
        } catch (e2) {}
      }
    },
    _activeStack: [],
    registerIframe: function(_b1) {
      return dijit.registerWin(_b1.contentWindow, _b1);
    },
    unregisterIframe: function(_b2) {
      dijit.unregisterWin(_b2);
    },
    registerWin: function(_b3, _b4) {
      var _b5 = function(evt) {
        dijit._justMouseDowned = true;
        setTimeout(function() {
          dijit._justMouseDowned = false;
        }, 0);
        if (dojo.isIE && evt && evt.srcElement && evt.srcElement.parentNode == null) {
          return;
        }
        dijit._onTouchNode(_b4 || evt.target || evt.srcElement, "mouse");
      };
      var doc = dojo.isIE ? _b3.document.documentElement : _b3.document;
      if (doc) {
        if (dojo.isIE) {
          _b3.document.body.attachEvent("onmousedown", _b5);
          var _b6 = function(evt) {
            if (evt.srcElement.tagName.toLowerCase() != "#document" && dijit.isTabNavigable(evt.srcElement)) {
              dijit._onFocusNode(_b4 || evt.srcElement);
            } else {
              dijit._onTouchNode(_b4 || evt.srcElement);
            }
          };
          doc.attachEvent("onactivate", _b6);
          var _b7 = function(evt) {
            dijit._onBlurNode(_b4 || evt.srcElement);
          };
          doc.attachEvent("ondeactivate", _b7);
          return function() {
            _b3.document.detachEvent("onmousedown", _b5);
            doc.detachEvent("onactivate", _b6);
            doc.detachEvent("ondeactivate", _b7);
            doc = null;
          };
        } else {
          doc.body.addEventListener("mousedown", _b5, true);
          var _b8 = function(evt) {
            dijit._onFocusNode(_b4 || evt.target);
          };
          doc.addEventListener("focus", _b8, true);
          var _b9 = function(evt) {
            dijit._onBlurNode(_b4 || evt.target);
          };
          doc.addEventListener("blur", _b9, true);
          return function() {
            doc.body.removeEventListener("mousedown", _b5, true);
            doc.removeEventListener("focus", _b8, true);
            doc.removeEventListener("blur", _b9, true);
            doc = null;
          };
        }
      }
    },
    unregisterWin: function(_ba) {
      _ba && _ba();
    },
    _onBlurNode: function(_bb) {
      dijit._prevFocus = dijit._curFocus;
      dijit._curFocus = null;
      if (dijit._justMouseDowned) {
        return;
      }
      if (dijit._clearActiveWidgetsTimer) {
        clearTimeout(dijit._clearActiveWidgetsTimer);
      }
      dijit._clearActiveWidgetsTimer = setTimeout(function() {
        delete dijit._clearActiveWidgetsTimer;
        dijit._setStack([]);
        dijit._prevFocus = null;
      }, 100);
    },
    _onTouchNode: function(_bc, by) {
      if (dijit._clearActiveWidgetsTimer) {
        clearTimeout(dijit._clearActiveWidgetsTimer);
        delete dijit._clearActiveWidgetsTimer;
      }
      var _bd = [];
      try {
        while (_bc) {
          var _be = dojo.attr(_bc, "dijitPopupParent");
          if (_be) {
            _bc = dijit.byId(_be).domNode;
          } else {
            if (_bc.tagName && _bc.tagName.toLowerCase() == "body") {
              if (_bc === dojo.body()) {
                break;
              }
              _bc = dojo.window.get(_bc.ownerDocument).frameElement;
            } else {
              var id = _bc.getAttribute && _bc.getAttribute("widgetId"),
                _bf = id && dijit.byId(id);
              if (_bf && !(by == "mouse" && _bf.get("disabled"))) {
                _bd.unshift(id);
              }
              _bc = _bc.parentNode;
            }
          }
        }
      } catch (e) {}
      dijit._setStack(_bd, by);
    },
    _onFocusNode: function(_c0) {
      if (!_c0) {
        return;
      }
      if (_c0.nodeType == 9) {
        return;
      }
      dijit._onTouchNode(_c0);
      if (_c0 == dijit._curFocus) {
        return;
      }
      if (dijit._curFocus) {
        dijit._prevFocus = dijit._curFocus;
      }
      dijit._curFocus = _c0;
      dojo.publish("focusNode", [_c0]);
    },
    _setStack: function(_c1, by) {
      var _c2 = dijit._activeStack;
      dijit._activeStack = _c1;
      for (var _c3 = 0; _c3 < Math.min(_c2.length, _c1.length); _c3++) {
        if (_c2[_c3] != _c1[_c3]) {
          break;
        }
      }
      var _c4;
      for (var i = _c2.length - 1; i >= _c3; i--) {
        _c4 = dijit.byId(_c2[i]);
        if (_c4) {
          _c4._focused = false;
          _c4.set("focused", false);
          _c4._hasBeenBlurred = true;
          if (_c4._onBlur) {
            _c4._onBlur(by);
          }
          dojo.publish("widgetBlur", [_c4, by]);
        }
      }
      for (i = _c3; i < _c1.length; i++) {
        _c4 = dijit.byId(_c1[i]);
        if (_c4) {
          _c4._focused = true;
          _c4.set("focused", true);
          if (_c4._onFocus) {
            _c4._onFocus(by);
          }
          dojo.publish("widgetFocus", [_c4, by]);
        }
      }
    }
  });
  dojo.addOnLoad(function() {
    var _c5 = dijit.registerWin(window);
    if (dojo.isIE) {
      dojo.addOnWindowUnload(function() {
        dijit.unregisterWin(_c5);
        _c5 = null;
      });
    }
  });
}
if (!dojo._hasResource["dojo.AdapterRegistry"]) {
  dojo._hasResource["dojo.AdapterRegistry"] = true;
  dojo.provide("dojo.AdapterRegistry");
  dojo.AdapterRegistry = function(_c6) {
    this.pairs = [];
    this.returnWrappers = _c6 || false;
  };
  dojo.extend(dojo.AdapterRegistry, {
    register: function(_c7, _c8, _c9, _ca, _cb) {
      this.pairs[((_cb) ? "unshift" : "push")]([_c7, _c8, _c9, _ca]);
    },
    match: function() {
      for (var i = 0; i < this.pairs.length; i++) {
        var _cc = this.pairs[i];
        if (_cc[1].apply(this, arguments)) {
          if ((_cc[3]) || (this.returnWrappers)) {
            return _cc[2];
          } else {
            return _cc[2].apply(this, arguments);
          }
        }
      }
      throw new Error("No match found");
    },
    unregister: function(_cd) {
      for (var i = 0; i < this.pairs.length; i++) {
        var _ce = this.pairs[i];
        if (_ce[0] == _cd) {
          this.pairs.splice(i, 1);
          return true;
        }
      }
      return false;
    }
  });
}
if (!dojo._hasResource["dijit._base.place"]) {
  dojo._hasResource["dijit._base.place"] = true;
  dojo.provide("dijit._base.place");
  dijit.getViewport = function() {
    return dojo.window.getBox();
  };
  dijit.placeOnScreen = function(_cf, pos, _d0, _d1) {
    var _d2 = dojo.map(_d0, function(_d3) {
      var c = {
        corner: _d3,
        pos: {
          x: pos.x,
          y: pos.y
        }
      };
      if (_d1) {
        c.pos.x += _d3.charAt(1) == "L" ? _d1.x : -_d1.x;
        c.pos.y += _d3.charAt(0) == "T" ? _d1.y : -_d1.y;
      }
      return c;
    });
    return dijit._place(_cf, _d2);
  };
  dijit._place = function(_d4, _d5, _d6, _d7) {
    var _d8 = dojo.window.getBox();
    if (!_d4.parentNode || String(_d4.parentNode.tagName).toLowerCase() != "body") {
      dojo.body().appendChild(_d4);
    }
    var _d9 = null;
    dojo.some(_d5, function(_da) {
      var _db = _da.corner;
      var pos = _da.pos;
      var _dc = 0;
      var _dd = {
        w: _db.charAt(1) == "L" ? (_d8.l + _d8.w) - pos.x : pos.x - _d8.l,
        h: _db.charAt(1) == "T" ? (_d8.t + _d8.h) - pos.y : pos.y - _d8.t
      };
      if (_d6) {
        var res = _d6(_d4, _da.aroundCorner, _db, _dd, _d7);
        _dc = typeof res == "undefined" ? 0 : res;
      }
      var _de = _d4.style;
      var _df = _de.display;
      var _e0 = _de.visibility;
      _de.visibility = "hidden";
      _de.display = "";
      var mb = dojo.marginBox(_d4);
      _de.display = _df;
      _de.visibility = _e0;
      var _e1 = Math.max(_d8.l, _db.charAt(1) == "L" ? pos.x : (pos.x - mb.w)),
        _e2 = Math.max(_d8.t, _db.charAt(0) == "T" ? pos.y : (pos.y - mb.h)),
        _e3 = Math.min(_d8.l + _d8.w, _db.charAt(1) == "L" ? (_e1 + mb.w) : pos.x),
        _e4 = Math.min(_d8.t + _d8.h, _db.charAt(0) == "T" ? (_e2 + mb.h) : pos.y),
        _e5 = _e3 - _e1,
        _e6 = _e4 - _e2;
      _dc += (mb.w - _e5) + (mb.h - _e6);
      if (_d9 == null || _dc < _d9.overflow) {
        _d9 = {
          corner: _db,
          aroundCorner: _da.aroundCorner,
          x: _e1,
          y: _e2,
          w: _e5,
          h: _e6,
          overflow: _dc,
          spaceAvailable: _dd
        };
      }
      return !_dc;
    });
    if (_d9.overflow && _d6) {
      _d6(_d4, _d9.aroundCorner, _d9.corner, _d9.spaceAvailable, _d7);
    }
    var l = dojo._isBodyLtr(),
      s = _d4.style;
    s.top = _d9.y + "px";
    s[l ? "left" : "right"] = (l ? _d9.x : _d8.w - _d9.x - _d9.w) + "px";
    return _d9;
  };
  dijit.placeOnScreenAroundNode = function(_e7, _e8, _e9, _ea) {
    _e8 = dojo.byId(_e8);
    var _eb = dojo.position(_e8, true);
    return dijit._placeOnScreenAroundRect(_e7, _eb.x, _eb.y, _eb.w, _eb.h, _e9, _ea);
  };
  dijit.placeOnScreenAroundRectangle = function(_ec, _ed, _ee, _ef) {
    return dijit._placeOnScreenAroundRect(_ec, _ed.x, _ed.y, _ed.width, _ed.height, _ee, _ef);
  };
  dijit._placeOnScreenAroundRect = function(_f0, x, y, _f1, _f2, _f3, _f4) {
    var _f5 = [];
    for (var _f6 in _f3) {
      _f5.push({
        aroundCorner: _f6,
        corner: _f3[_f6],
        pos: {
          x: x + (_f6.charAt(1) == "L" ? 0 : _f1),
          y: y + (_f6.charAt(0) == "T" ? 0 : _f2)
        }
      });
    }
    return dijit._place(_f0, _f5, _f4, {
      w: _f1,
      h: _f2
    });
  };
  dijit.placementRegistry = new dojo.AdapterRegistry();
  dijit.placementRegistry.register("node", function(n, x) {
    return typeof x == "object" && typeof x.offsetWidth != "undefined" && typeof x.offsetHeight != "undefined";
  }, dijit.placeOnScreenAroundNode);
  dijit.placementRegistry.register("rect", function(n, x) {
    return typeof x == "object" && "x" in x && "y" in x && "width" in x && "height" in x;
  }, dijit.placeOnScreenAroundRectangle);
  dijit.placeOnScreenAroundElement = function(_f7, _f8, _f9, _fa) {
    return dijit.placementRegistry.match.apply(dijit.placementRegistry, arguments);
  };
  dijit.getPopupAroundAlignment = function(_fb, _fc) {
    var _fd = {};
    dojo.forEach(_fb, function(pos) {
      switch (pos) {
        case "after":
          _fd[_fc ? "BR" : "BL"] = _fc ? "BL" : "BR";
          break;
        case "before":
          _fd[_fc ? "BL" : "BR"] = _fc ? "BR" : "BL";
          break;
        case "below-alt":
          _fc = !_fc;
        case "below":
          _fd[_fc ? "BL" : "BR"] = _fc ? "TL" : "TR";
          _fd[_fc ? "BR" : "BL"] = _fc ? "TR" : "TL";
          break;
        case "above-alt":
          _fc = !_fc;
        case "above":
        default:
          _fd[_fc ? "TL" : "TR"] = _fc ? "BL" : "BR";
          _fd[_fc ? "TR" : "TL"] = _fc ? "BR" : "BL";
          break;
      }
    });
    return _fd;
  };
}
if (!dojo._hasResource["dijit._base.window"]) {
  dojo._hasResource["dijit._base.window"] = true;
  dojo.provide("dijit._base.window");
  dijit.getDocumentWindow = function(doc) {
    return dojo.window.get(doc);
  };
}
if (!dojo._hasResource["dijit._base.popup"]) {
  dojo._hasResource["dijit._base.popup"] = true;
  dojo.provide("dijit._base.popup");
  dijit.popup = {
    _stack: [],
    _beginZIndex: 2000,
    _idGen: 1,
    _createWrapper: function(_fe) {
      var _ff = _fe.declaredClass ? _fe._popupWrapper : (_fe.parentNode && dojo.hasClass(_fe.parentNode, "dijitPopup")),
        node = _fe.domNode || _fe;
      if (!_ff) {
        _ff = dojo.create("div", {
          "class": "dijitPopup",
          style: {
            display: "none"
          },
          role: "presentation"
        }, dojo.body());
        _ff.appendChild(node);
        var s = node.style;
        s.display = "";
        s.visibility = "";
        s.position = "";
        s.top = "0px";
        if (_fe.declaredClass) {
          _fe._popupWrapper = _ff;
          dojo.connect(_fe, "destroy", function() {
            dojo.destroy(_ff);
            delete _fe._popupWrapper;
          });
        }
      }
      return _ff;
    },
    moveOffScreen: function(_100) {
      var _101 = this._createWrapper(_100);
      dojo.style(_101, {
        visibility: "hidden",
        top: "-9999px",
        display: ""
      });
    },
    hide: function(_102) {
      var _103 = this._createWrapper(_102);
      dojo.style(_103, "display", "none");
    },
    getTopPopup: function() {
      var _104 = this._stack;
      for (var pi = _104.length - 1; pi > 0 && _104[pi].parent === _104[pi - 1].widget; pi--) {}
      return _104[pi];
    },
    open: function(args) {
      var _105 = this._stack,
        _106 = args.popup,
        _107 = args.orient || ((args.parent ? args.parent.isLeftToRight() : dojo._isBodyLtr()) ? {
          "BL": "TL",
          "BR": "TR",
          "TL": "BL",
          "TR": "BR"
        } : {
          "BR": "TR",
          "BL": "TL",
          "TR": "BR",
          "TL": "BL"
        }),
        _108 = args.around,
        id = (args.around && args.around.id) ? (args.around.id + "_dropdown") : ("popup_" + this._idGen++);
      while (_105.length && (!args.parent || !dojo.isDescendant(args.parent.domNode, _105[_105.length - 1].widget.domNode))) {
        dijit.popup.close(_105[_105.length - 1].widget);
      }
      var _109 = this._createWrapper(_106);
      dojo.attr(_109, {
        id: id,
        style: {
          zIndex: this._beginZIndex + _105.length
        },
        "class": "dijitPopup " + (_106.baseClass || _106["class"] || "").split(" ")[0] + "Popup",
        dijitPopupParent: args.parent ? args.parent.id : ""
      });
      if (dojo.isIE || dojo.isMoz) {
        if (!_106.bgIframe) {
          _106.bgIframe = new dijit.BackgroundIframe(_109);
        }
      }
      var best = _108 ? dijit.placeOnScreenAroundElement(_109, _108, _107, _106.orient ? dojo.hitch(_106, "orient") : null) : dijit.placeOnScreen(_109, args, _107 == "R" ? ["TR", "BR", "TL", "BL"] : ["TL", "BL", "TR", "BR"], args.padding);
      _109.style.display = "";
      _109.style.visibility = "visible";
      _106.domNode.style.visibility = "visible";
      var _10a = [];
      _10a.push(dojo.connect(_109, "onkeypress", this, function(evt) {
        if (evt.charOrCode == dojo.keys.ESCAPE && args.onCancel) {
          dojo.stopEvent(evt);
          args.onCancel();
        } else {
          if (evt.charOrCode === dojo.keys.TAB) {
            dojo.stopEvent(evt);
            var _10b = this.getTopPopup();
            if (_10b && _10b.onCancel) {
              _10b.onCancel();
            }
          }
        }
      }));
      if (_106.onCancel) {
        _10a.push(dojo.connect(_106, "onCancel", args.onCancel));
      }
      _10a.push(dojo.connect(_106, _106.onExecute ? "onExecute" : "onChange", this, function() {
        var _10c = this.getTopPopup();
        if (_10c && _10c.onExecute) {
          _10c.onExecute();
        }
      }));
      _105.push({
        widget: _106,
        parent: args.parent,
        onExecute: args.onExecute,
        onCancel: args.onCancel,
        onClose: args.onClose,
        handlers: _10a
      });
      if (_106.onOpen) {
        _106.onOpen(best);
      }
      return best;
    },
    close: function(_10d) {
      var _10e = this._stack;
      while ((_10d && dojo.some(_10e, function(elem) {
          return elem.widget == _10d;
        })) || (!_10d && _10e.length)) {
        var top = _10e.pop(),
          _10f = top.widget,
          _110 = top.onClose;
        if (_10f.onClose) {
          _10f.onClose();
        }
        dojo.forEach(top.handlers, dojo.disconnect);
        if (_10f && _10f.domNode) {
          this.hide(_10f);
        }
        if (_110) {
          _110();
        }
      }
    }
  };
  dijit._frames = new function() {
    var _111 = [];
    this.pop = function() {
      var _112;
      if (_111.length) {
        _112 = _111.pop();
        _112.style.display = "";
      } else {
        if (dojo.isIE < 9) {
          var burl = dojo.config["dojoBlankHtmlUrl"] || (dojo.moduleUrl("dojo", "resources/blank.html") + "") || "javascript:\"\"";
          var html = "<iframe src='" + burl + "'" + " style='position: absolute; left: 0px; top: 0px;" + "z-index: -1; filter:Alpha(Opacity=\"0\");'>";
          _112 = dojo.doc.createElement(html);
        } else {
          _112 = dojo.create("iframe");
          _112.src = "javascript:\"\"";
          _112.className = "dijitBackgroundIframe";
          dojo.style(_112, "opacity", 0.1);
        }
        _112.tabIndex = -1;
        dijit.setWaiRole(_112, "presentation");
      }
      return _112;
    };
    this.push = function(_113) {
      _113.style.display = "none";
      _111.push(_113);
    };
  }();
  dijit.BackgroundIframe = function(node) {
    if (!node.id) {
      throw new Error("no id");
    }
    if (dojo.isIE || dojo.isMoz) {
      var _114 = (this.iframe = dijit._frames.pop());
      node.appendChild(_114);
      if (dojo.isIE < 7 || dojo.isQuirks) {
        this.resize(node);
        this._conn = dojo.connect(node, "onresize", this, function() {
          this.resize(node);
        });
      } else {
        dojo.style(_114, {
          width: "100%",
          height: "100%"
        });
      }
    }
  };
  dojo.extend(dijit.BackgroundIframe, {
    resize: function(node) {
      if (this.iframe) {
        dojo.style(this.iframe, {
          width: node.offsetWidth + "px",
          height: node.offsetHeight + "px"
        });
      }
    },
    destroy: function() {
      if (this._conn) {
        dojo.disconnect(this._conn);
        this._conn = null;
      }
      if (this.iframe) {
        dijit._frames.push(this.iframe);
        delete this.iframe;
      }
    }
  });
}
if (!dojo._hasResource["dijit._base.scroll"]) {
  dojo._hasResource["dijit._base.scroll"] = true;
  dojo.provide("dijit._base.scroll");
  dijit.scrollIntoView = function(node, pos) {
    dojo.window.scrollIntoView(node, pos);
  };
}
if (!dojo._hasResource["dojo.uacss"]) {
  dojo._hasResource["dojo.uacss"] = true;
  dojo.provide("dojo.uacss");
  (function() {
    var d = dojo,
      html = d.doc.documentElement,
      ie = d.isIE,
      _115 = d.isOpera,
      maj = Math.floor,
      ff = d.isFF,
      _116 = d.boxModel.replace(/-/, ""),
      _117 = {
        dj_ie: ie,
        dj_ie6: maj(ie) == 6,
        dj_ie7: maj(ie) == 7,
        dj_ie8: maj(ie) == 8,
        dj_ie9: maj(ie) == 9,
        dj_quirks: d.isQuirks,
        dj_iequirks: ie && d.isQuirks,
        dj_opera: _115,
        dj_khtml: d.isKhtml,
        dj_webkit: d.isWebKit,
        dj_safari: d.isSafari,
        dj_chrome: d.isChrome,
        dj_gecko: d.isMozilla,
        dj_ff3: maj(ff) == 3
      };
    _117["dj_" + _116] = true;
    var _118 = "";
    for (var clz in _117) {
      if (_117[clz]) {
        _118 += clz + " ";
      }
    }
    html.className = d.trim(html.className + " " + _118);
    dojo._loaders.unshift(function() {
      if (!dojo._isBodyLtr()) {
        var _119 = "dj_rtl dijitRtl " + _118.replace(/ /g, "-rtl ");
        html.className = d.trim(html.className + " " + _119);
      }
    });
  })();
}
if (!dojo._hasResource["dijit._base.sniff"]) {
  dojo._hasResource["dijit._base.sniff"] = true;
  dojo.provide("dijit._base.sniff");
}
if (!dojo._hasResource["dijit._base.typematic"]) {
  dojo._hasResource["dijit._base.typematic"] = true;
  dojo.provide("dijit._base.typematic");
  dijit.typematic = {
    _fireEventAndReload: function() {
      this._timer = null;
      this._callback(++this._count, this._node, this._evt);
      this._currentTimeout = Math.max(this._currentTimeout < 0 ? this._initialDelay : (this._subsequentDelay > 1 ? this._subsequentDelay : Math.round(this._currentTimeout * this._subsequentDelay)), this._minDelay);
      this._timer = setTimeout(dojo.hitch(this, "_fireEventAndReload"), this._currentTimeout);
    },
    trigger: function(evt, _11a, node, _11b, obj, _11c, _11d, _11e) {
      if (obj != this._obj) {
        this.stop();
        this._initialDelay = _11d || 500;
        this._subsequentDelay = _11c || 0.9;
        this._minDelay = _11e || 10;
        this._obj = obj;
        this._evt = evt;
        this._node = node;
        this._currentTimeout = -1;
        this._count = -1;
        this._callback = dojo.hitch(_11a, _11b);
        this._fireEventAndReload();
        this._evt = dojo.mixin({
          faux: true
        }, evt);
      }
    },
    stop: function() {
      if (this._timer) {
        clearTimeout(this._timer);
        this._timer = null;
      }
      if (this._obj) {
        this._callback(-1, this._node, this._evt);
        this._obj = null;
      }
    },
    addKeyListener: function(node, _11f, _120, _121, _122, _123, _124) {
      if (_11f.keyCode) {
        _11f.charOrCode = _11f.keyCode;
        dojo.deprecated("keyCode attribute parameter for dijit.typematic.addKeyListener is deprecated. Use charOrCode instead.", "", "2.0");
      } else {
        if (_11f.charCode) {
          _11f.charOrCode = String.fromCharCode(_11f.charCode);
          dojo.deprecated("charCode attribute parameter for dijit.typematic.addKeyListener is deprecated. Use charOrCode instead.", "", "2.0");
        }
      }
      return [dojo.connect(node, "onkeypress", this, function(evt) {
        if (evt.charOrCode == _11f.charOrCode && (_11f.ctrlKey === undefined || _11f.ctrlKey == evt.ctrlKey) && (_11f.altKey === undefined || _11f.altKey == evt.altKey) && (_11f.metaKey === undefined || _11f.metaKey == (evt.metaKey || false)) && (_11f.shiftKey === undefined || _11f.shiftKey == evt.shiftKey)) {
          dojo.stopEvent(evt);
          dijit.typematic.trigger(evt, _120, node, _121, _11f, _122, _123, _124);
        } else {
          if (dijit.typematic._obj == _11f) {
            dijit.typematic.stop();
          }
        }
      }), dojo.connect(node, "onkeyup", this, function(evt) {
        if (dijit.typematic._obj == _11f) {
          dijit.typematic.stop();
        }
      })];
    },
    addMouseListener: function(node, _125, _126, _127, _128, _129) {
      var dc = dojo.connect;
      return [dc(node, "mousedown", this, function(evt) {
        dojo.stopEvent(evt);
        dijit.typematic.trigger(evt, _125, node, _126, node, _127, _128, _129);
      }), dc(node, "mouseup", this, function(evt) {
        dojo.stopEvent(evt);
        dijit.typematic.stop();
      }), dc(node, "mouseout", this, function(evt) {
        dojo.stopEvent(evt);
        dijit.typematic.stop();
      }), dc(node, "mousemove", this, function(evt) {
        evt.preventDefault();
      }), dc(node, "dblclick", this, function(evt) {
        dojo.stopEvent(evt);
        if (dojo.isIE) {
          dijit.typematic.trigger(evt, _125, node, _126, node, _127, _128, _129);
          setTimeout(dojo.hitch(this, dijit.typematic.stop), 50);
        }
      })];
    },
    addListener: function(_12a, _12b, _12c, _12d, _12e, _12f, _130, _131) {
      return this.addKeyListener(_12b, _12c, _12d, _12e, _12f, _130, _131).concat(this.addMouseListener(_12a, _12d, _12e, _12f, _130, _131));
    }
  };
}
if (!dojo._hasResource["dijit._base.wai"]) {
  dojo._hasResource["dijit._base.wai"] = true;
  dojo.provide("dijit._base.wai");
  dijit.wai = {
    onload: function() {
      var div = dojo.create("div", {
        id: "a11yTestNode",
        style: {
          cssText: "border: 1px solid;" + "border-color:red green;" + "position: absolute;" + "height: 5px;" + "top: -999px;" + "background-image: url(\"" + (dojo.config.blankGif || dojo.moduleUrl("dojo", "resources/blank.gif")) + "\");"
        }
      }, dojo.body());
      var cs = dojo.getComputedStyle(div);
      if (cs) {
        var _132 = cs.backgroundImage;
        var _133 = (cs.borderTopColor == cs.borderRightColor) || (_132 != null && (_132 == "none" || _132 == "url(invalid-url:)"));
        dojo[_133 ? "addClass" : "removeClass"](dojo.body(), "dijit_a11y");
        if (dojo.isIE) {
          div.outerHTML = "";
        } else {
          dojo.body().removeChild(div);
        }
      }
    }
  };
  if (dojo.isIE || dojo.isMoz) {
    dojo._loaders.unshift(dijit.wai.onload);
  }
  dojo.mixin(dijit, {
    hasWaiRole: function(elem, role) {
      var _134 = this.getWaiRole(elem);
      return role ? (_134.indexOf(role) > -1) : (_134.length > 0);
    },
    getWaiRole: function(elem) {
      return dojo.trim((dojo.attr(elem, "role") || "").replace("wairole:", ""));
    },
    setWaiRole: function(elem, role) {
      dojo.attr(elem, "role", role);
    },
    removeWaiRole: function(elem, role) {
      var _135 = dojo.attr(elem, "role");
      if (!_135) {
        return;
      }
      if (role) {
        var t = dojo.trim((" " + _135 + " ").replace(" " + role + " ", " "));
        dojo.attr(elem, "role", t);
      } else {
        elem.removeAttribute("role");
      }
    },
    hasWaiState: function(elem, _136) {
      return elem.hasAttribute ? elem.hasAttribute("aria-" + _136) : !!elem.getAttribute("aria-" + _136);
    },
    getWaiState: function(elem, _137) {
      return elem.getAttribute("aria-" + _137) || "";
    },
    setWaiState: function(elem, _138, _139) {
      elem.setAttribute("aria-" + _138, _139);
    },
    removeWaiState: function(elem, _13a) {
      elem.removeAttribute("aria-" + _13a);
    }
  });
}
if (!dojo._hasResource["dijit._base"]) {
  dojo._hasResource["dijit._base"] = true;
  dojo.provide("dijit._base");
}
if (!dojo._hasResource["dijit._Widget"]) {
  dojo._hasResource["dijit._Widget"] = true;
  dojo.provide("dijit._Widget");
  dojo.connect(dojo, "_connect", function(_13b, _13c) {
    if (_13b && dojo.isFunction(_13b._onConnect)) {
      _13b._onConnect(_13c);
    }
  });
  dijit._connectOnUseEventHandler = function(_13d) {};
  dijit._lastKeyDownNode = null;
  if (dojo.isIE) {
    (function() {
      var _13e = function(evt) {
        dijit._lastKeyDownNode = evt.srcElement;
      };
      dojo.doc.attachEvent("onkeydown", _13e);
      dojo.addOnWindowUnload(function() {
        dojo.doc.detachEvent("onkeydown", _13e);
      });
    })();
  } else {
    dojo.doc.addEventListener("keydown", function(evt) {
      dijit._lastKeyDownNode = evt.target;
    }, true);
  }(function() {
    dojo.declare("dijit._Widget", dijit._WidgetBase, {
      _deferredConnects: {
        onClick: "",
        onDblClick: "",
        onKeyDown: "",
        onKeyPress: "",
        onKeyUp: "",
        onMouseMove: "",
        onMouseDown: "",
        onMouseOut: "",
        onMouseOver: "",
        onMouseLeave: "",
        onMouseEnter: "",
        onMouseUp: ""
      },
      onClick: dijit._connectOnUseEventHandler,
      onDblClick: dijit._connectOnUseEventHandler,
      onKeyDown: dijit._connectOnUseEventHandler,
      onKeyPress: dijit._connectOnUseEventHandler,
      onKeyUp: dijit._connectOnUseEventHandler,
      onMouseDown: dijit._connectOnUseEventHandler,
      onMouseMove: dijit._connectOnUseEventHandler,
      onMouseOut: dijit._connectOnUseEventHandler,
      onMouseOver: dijit._connectOnUseEventHandler,
      onMouseLeave: dijit._connectOnUseEventHandler,
      onMouseEnter: dijit._connectOnUseEventHandler,
      onMouseUp: dijit._connectOnUseEventHandler,
      create: function(_13f, _140) {
        this._deferredConnects = dojo.clone(this._deferredConnects);
        for (var attr in this.attributeMap) {
          delete this._deferredConnects[attr];
        }
        for (attr in this._deferredConnects) {
          if (this[attr] !== dijit._connectOnUseEventHandler) {
            delete this._deferredConnects[attr];
          }
        }
        this.inherited(arguments);
        if (this.domNode) {
          for (attr in this.params) {
            this._onConnect(attr);
          }
        }
      },
      _onConnect: function(_141) {
        if (_141 in this._deferredConnects) {
          var _142 = this[this._deferredConnects[_141] || "domNode"];
          this.connect(_142, _141.toLowerCase(), _141);
          delete this._deferredConnects[_141];
        }
      },
      focused: false,
      isFocusable: function() {
        return this.focus && (dojo.style(this.domNode, "display") != "none");
      },
      onFocus: function() {},
      onBlur: function() {},
      _onFocus: function(e) {
        this.onFocus();
      },
      _onBlur: function() {
        this.onBlur();
      },
      setAttribute: function(attr, _143) {
        dojo.deprecated(this.declaredClass + "::setAttribute(attr, value) is deprecated. Use set() instead.", "", "2.0");
        this.set(attr, _143);
      },
      attr: function(name, _144) {
        if (dojo.config.isDebug) {
          var _145 = arguments.callee._ach || (arguments.callee._ach = {}),
            _146 = (arguments.callee.caller || "unknown caller").toString();
          if (!_145[_146]) {
            dojo.deprecated(this.declaredClass + "::attr() is deprecated. Use get() or set() instead, called from " + _146, "", "2.0");
            _145[_146] = true;
          }
        }
        var args = arguments.length;
        if (args >= 2 || typeof name === "object") {
          return this.set.apply(this, arguments);
        } else {
          return this.get(name);
        }
      },
      nodesWithKeyClick: ["input", "button"],
      connect: function(obj, _147, _148) {
        var d = dojo,
          dc = d._connect,
          _149 = this.inherited(arguments, [obj, _147 == "ondijitclick" ? "onclick" : _147, _148]);
        if (_147 == "ondijitclick") {
          if (d.indexOf(this.nodesWithKeyClick, obj.nodeName.toLowerCase()) == -1) {
            var m = d.hitch(this, _148);
            _149.push(dc(obj, "onkeydown", this, function(e) {
              if ((e.keyCode == d.keys.ENTER || e.keyCode == d.keys.SPACE) && !e.ctrlKey && !e.shiftKey && !e.altKey && !e.metaKey) {
                dijit._lastKeyDownNode = e.target;
                if (!("openDropDown" in this && obj == this._buttonNode)) {
                  e.preventDefault();
                }
              }
            }), dc(obj, "onkeyup", this, function(e) {
              if ((e.keyCode == d.keys.ENTER || e.keyCode == d.keys.SPACE) && e.target == dijit._lastKeyDownNode && !e.ctrlKey && !e.shiftKey && !e.altKey && !e.metaKey) {
                dijit._lastKeyDownNode = null;
                return m(e);
              }
            }));
          }
        }
        return _149;
      },
      _onShow: function() {
        this.onShow();
      },
      onShow: function() {},
      onHide: function() {},
      onClose: function() {
        return true;
      }
    });
  })();
}
if (!dojo._hasResource["dojo.string"]) {
  dojo._hasResource["dojo.string"] = true;
  dojo.provide("dojo.string");
  dojo.getObject("string", true, dojo);
  dojo.string.rep = function(str, num) {
    if (num <= 0 || !str) {
      return "";
    }
    var buf = [];
    for (;;) {
      if (num & 1) {
        buf.push(str);
      }
      if (!(num >>= 1)) {
        break;
      }
      str += str;
    }
    return buf.join("");
  };
  dojo.string.pad = function(text, size, ch, end) {
    if (!ch) {
      ch = "0";
    }
    var out = String(text),
      pad = dojo.string.rep(ch, Math.ceil((size - out.length) / ch.length));
    return end ? out + pad : pad + out;
  };
  dojo.string.substitute = function(_14a, map, _14b, _14c) {
    _14c = _14c || dojo.global;
    _14b = _14b ? dojo.hitch(_14c, _14b) : function(v) {
      return v;
    };
    return _14a.replace(/\$\{([^\s\:\}]+)(?:\:([^\s\:\}]+))?\}/g, function(_14d, key, _14e) {
      var _14f = dojo.getObject(key, false, map);
      if (_14e) {
        _14f = dojo.getObject(_14e, false, _14c).call(_14c, _14f, key);
      }
      return _14b(_14f, key).toString();
    });
  };
  dojo.string.trim = String.prototype.trim ? dojo.trim : function(str) {
    str = str.replace(/^\s+/, "");
    for (var i = str.length - 1; i >= 0; i--) {
      if (/\S/.test(str.charAt(i))) {
        str = str.substring(0, i + 1);
        break;
      }
    }
    return str;
  };
}
if (!dojo._hasResource["dojo.date.stamp"]) {
  dojo._hasResource["dojo.date.stamp"] = true;
  dojo.provide("dojo.date.stamp");
  dojo.getObject("date.stamp", true, dojo);
  dojo.date.stamp.fromISOString = function(_150, _151) {
    if (!dojo.date.stamp._isoRegExp) {
      dojo.date.stamp._isoRegExp = /^(?:(\d{4})(?:-(\d{2})(?:-(\d{2}))?)?)?(?:T(\d{2}):(\d{2})(?::(\d{2})(.\d+)?)?((?:[+-](\d{2}):(\d{2}))|Z)?)?$/;
    }
    var _152 = dojo.date.stamp._isoRegExp.exec(_150),
      _153 = null;
    if (_152) {
      _152.shift();
      if (_152[1]) {
        _152[1]--;
      }
      if (_152[6]) {
        _152[6] *= 1000;
      }
      if (_151) {
        _151 = new Date(_151);
        dojo.forEach(dojo.map(["FullYear", "Month", "Date", "Hours", "Minutes", "Seconds", "Milliseconds"], function(prop) {
          return _151["get" + prop]();
        }), function(_154, _155) {
          _152[_155] = _152[_155] || _154;
        });
      }
      _153 = new Date(_152[0] || 1970, _152[1] || 0, _152[2] || 1, _152[3] || 0, _152[4] || 0, _152[5] || 0, _152[6] || 0);
      if (_152[0] < 100) {
        _153.setFullYear(_152[0] || 1970);
      }
      var _156 = 0,
        _157 = _152[7] && _152[7].charAt(0);
      if (_157 != "Z") {
        _156 = ((_152[8] || 0) * 60) + (Number(_152[9]) || 0);
        if (_157 != "-") {
          _156 *= -1;
        }
      }
      if (_157) {
        _156 -= _153.getTimezoneOffset();
      }
      if (_156) {
        _153.setTime(_153.getTime() + _156 * 60000);
      }
    }
    return _153;
  };
  dojo.date.stamp.toISOString = function(_158, _159) {
    var _15a = function(n) {
      return (n < 10) ? "0" + n : n;
    };
    _159 = _159 || {};
    var _15b = [],
      _15c = _159.zulu ? "getUTC" : "get",
      date = "";
    if (_159.selector != "time") {
      var year = _158[_15c + "FullYear"]();
      date = ["0000".substr((year + "").length) + year, _15a(_158[_15c + "Month"]() + 1), _15a(_158[_15c + "Date"]())].join("-");
    }
    _15b.push(date);
    if (_159.selector != "date") {
      var time = [_15a(_158[_15c + "Hours"]()), _15a(_158[_15c + "Minutes"]()), _15a(_158[_15c + "Seconds"]())].join(":");
      var _15d = _158[_15c + "Milliseconds"]();
      if (_159.milliseconds) {
        time += "." + (_15d < 100 ? "0" : "") + _15a(_15d);
      }
      if (_159.zulu) {
        time += "Z";
      } else {
        if (_159.selector != "time") {
          var _15e = _158.getTimezoneOffset();
          var _15f = Math.abs(_15e);
          time += (_15e > 0 ? "-" : "+") + _15a(Math.floor(_15f / 60)) + ":" + _15a(_15f % 60);
        }
      }
      _15b.push(time);
    }
    return _15b.join("T");
  };
}
if (!dojo._hasResource["dojo.parser"]) {
  dojo._hasResource["dojo.parser"] = true;
  dojo.provide("dojo.parser");
  new Date("X");
  dojo.parser = new function() {
    var d = dojo;

    function _160(_161) {
      if (d.isString(_161)) {
        return "string";
      }
      if (typeof _161 == "number") {
        return "number";
      }
      if (typeof _161 == "boolean") {
        return "boolean";
      }
      if (d.isFunction(_161)) {
        return "function";
      }
      if (d.isArray(_161)) {
        return "array";
      }
      if (_161 instanceof Date) {
        return "date";
      }
      if (_161 instanceof d._Url) {
        return "url";
      }
      return "object";
    };

    function _162(_163, type) {
      switch (type) {
        case "string":
          return _163;
        case "number":
          return _163.length ? Number(_163) : NaN;
        case "boolean":
          return typeof _163 == "boolean" ? _163 : !(_163.toLowerCase() == "false");
        case "function":
          if (d.isFunction(_163)) {
            _163 = _163.toString();
            _163 = d.trim(_163.substring(_163.indexOf("{") + 1, _163.length - 1));
          }
          try {
            if (_163 === "" || _163.search(/[^\w\.]+/i) != -1) {
              return new Function(_163);
            } else {
              return d.getObject(_163, false) || new Function(_163);
            }
          } catch (e) {
            return new Function();
          }
          case "array":
            return _163 ? _163.split(/\s*,\s*/) : [];
          case "date":
            switch (_163) {
              case "":
                return new Date("");
              case "now":
                return new Date();
              default:
                return d.date.stamp.fromISOString(_163);
            }
            case "url":
              return d.baseUrl + _163;
            default:
              return d.fromJson(_163);
      }
    };
    var _164 = {},
      _165 = {};
    d.connect(d, "extend", function() {
      _165 = {};
    });

    function _166(cls, _167) {
      for (var name in cls) {
        if (name.charAt(0) == "_") {
          continue;
        }
        if (name in _164) {
          continue;
        }
        _167[name] = _160(cls[name]);
      }
      return _167;
    };

    function _168(_169, _16a) {
      var c = _165[_169];
      if (!c) {
        var cls = d.getObject(_169),
          _16b = null;
        if (!cls) {
          return null;
        }
        if (!_16a) {
          _16b = _166(cls.prototype, {});
        }
        c = {
          cls: cls,
          params: _16b
        };
      } else {
        if (!_16a && !c.params) {
          c.params = _166(c.cls.prototype, {});
        }
      }
      return c;
    };
    this._functionFromScript = function(_16c, _16d) {
      var _16e = "";
      var _16f = "";
      var _170 = (_16c.getAttribute(_16d + "args") || _16c.getAttribute("args"));
      if (_170) {
        d.forEach(_170.split(/\s*,\s*/), function(part, idx) {
          _16e += "var " + part + " = arguments[" + idx + "]; ";
        });
      }
      var _171 = _16c.getAttribute("with");
      if (_171 && _171.length) {
        d.forEach(_171.split(/\s*,\s*/), function(part) {
          _16e += "with(" + part + "){";
          _16f += "}";
        });
      }
      return new Function(_16e + _16c.innerHTML + _16f);
    };
    this.instantiate = function(_172, _173, args) {
      var _174 = [],
        _173 = _173 || {};
      args = args || {};
      var _175 = (args.scope || d._scopeName) + "Type",
        _176 = "data-" + (args.scope || d._scopeName) + "-";
      d.forEach(_172, function(obj) {
        if (!obj) {
          return;
        }
        var node, type, _177, _178, _179, _17a;
        if (obj.node) {
          node = obj.node;
          type = obj.type;
          _17a = obj.fastpath;
          _177 = obj.clsInfo || (type && _168(type, _17a));
          _178 = _177 && _177.cls;
          _179 = obj.scripts;
        } else {
          node = obj;
          type = _175 in _173 ? _173[_175] : node.getAttribute(_175);
          _177 = type && _168(type);
          _178 = _177 && _177.cls;
          _179 = (_178 && (_178._noScript || _178.prototype._noScript) ? [] : d.query("> script[type^='dojo/']", node));
        }
        if (!_177) {
          throw new Error("Could not load class '" + type);
        }
        var _17b = {};
        if (args.defaults) {
          d._mixin(_17b, args.defaults);
        }
        if (obj.inherited) {
          d._mixin(_17b, obj.inherited);
        }
        if (_17a) {
          var _17c = node.getAttribute(_176 + "props");
          if (_17c && _17c.length) {
            try {
              _17c = d.fromJson.call(args.propsThis, "{" + _17c + "}");
              d._mixin(_17b, _17c);
            } catch (e) {
              throw new Error(e.toString() + " in data-dojo-props='" + _17c + "'");
            }
          }
          var _17d = node.getAttribute(_176 + "attach-point");
          if (_17d) {
            _17b.dojoAttachPoint = _17d;
          }
          var _17e = node.getAttribute(_176 + "attach-event");
          if (_17e) {
            _17b.dojoAttachEvent = _17e;
          }
          dojo.mixin(_17b, _173);
        } else {
          var _17f = node.attributes;
          for (var name in _177.params) {
            var item = name in _173 ? {
              value: _173[name],
              specified: true
            } : _17f.getNamedItem(name);
            if (!item || (!item.specified && (!dojo.isIE || name.toLowerCase() != "value"))) {
              continue;
            }
            var _180 = item.value;
            switch (name) {
              case "class":
                _180 = "className" in _173 ? _173.className : node.className;
                break;
              case "style":
                _180 = "style" in _173 ? _173.style : (node.style && node.style.cssText);
            }
            var _181 = _177.params[name];
            if (typeof _180 == "string") {
              _17b[name] = _162(_180, _181);
            } else {
              _17b[name] = _180;
            }
          }
        }
        var _182 = [],
          _183 = [];
        d.forEach(_179, function(_184) {
          node.removeChild(_184);
          var _185 = (_184.getAttribute(_176 + "event") || _184.getAttribute("event")),
            type = _184.getAttribute("type"),
            nf = d.parser._functionFromScript(_184, _176);
          if (_185) {
            if (type == "dojo/connect") {
              _182.push({
                event: _185,
                func: nf
              });
            } else {
              _17b[_185] = nf;
            }
          } else {
            _183.push(nf);
          }
        });
        var _186 = _178.markupFactory || _178.prototype && _178.prototype.markupFactory;
        var _187 = _186 ? _186(_17b, node, _178) : new _178(_17b, node);
        _174.push(_187);
        var _188 = (node.getAttribute(_176 + "id") || node.getAttribute("jsId"));
        if (_188) {
          d.setObject(_188, _187);
        }
        d.forEach(_182, function(_189) {
          d.connect(_187, _189.event, null, _189.func);
        });
        d.forEach(_183, function(func) {
          func.call(_187);
        });
      });
      if (!_173._started) {
        d.forEach(_174, function(_18a) {
          if (!args.noStart && _18a && dojo.isFunction(_18a.startup) && !_18a._started && (!_18a.getParent || !_18a.getParent())) {
            _18a.startup();
          }
        });
      }
      return _174;
    };
    this.parse = function(_18b, args) {
      var root;
      if (!args && _18b && _18b.rootNode) {
        args = _18b;
        root = args.rootNode;
      } else {
        root = _18b;
      }
      root = root ? dojo.byId(root) : dojo.body();
      args = args || {};
      var _18c = (args.scope || d._scopeName) + "Type",
        _18d = "data-" + (args.scope || d._scopeName) + "-";

      function scan(_18e, list) {
        var _18f = dojo.clone(_18e.inherited);
        dojo.forEach(["dir", "lang"], function(name) {
          var val = _18e.node.getAttribute(name);
          if (val) {
            _18f[name] = val;
          }
        });
        var _190 = _18e.clsInfo && !_18e.clsInfo.cls.prototype._noScript ? _18e.scripts : null;
        var _191 = (!_18e.clsInfo || !_18e.clsInfo.cls.prototype.stopParser) || (args && args.template);
        for (var _192 = _18e.node.firstChild; _192; _192 = _192.nextSibling) {
          if (_192.nodeType == 1) {
            var type, _193 = _191 && _192.getAttribute(_18d + "type");
            if (_193) {
              type = _193;
            } else {
              type = _191 && _192.getAttribute(_18c);
            }
            var _194 = _193 == type;
            if (type) {
              var _195 = {
                "type": type,
                fastpath: _194,
                clsInfo: _168(type, _194),
                node: _192,
                scripts: [],
                inherited: _18f
              };
              list.push(_195);
              scan(_195, list);
            } else {
              if (_190 && _192.nodeName.toLowerCase() == "script") {
                type = _192.getAttribute("type");
                if (type && /^dojo\/\w/i.test(type)) {
                  _190.push(_192);
                }
              } else {
                if (_191) {
                  scan({
                    node: _192,
                    inherited: _18f
                  }, list);
                }
              }
            }
          }
        }
      };
      var _196 = {};
      if (args && args.inherited) {
        for (var key in args.inherited) {
          if (args.inherited[key]) {
            _196[key] = args.inherited[key];
          }
        }
      }
      var list = [];
      scan({
        node: root,
        inherited: _196
      }, list);
      var _197 = args && args.template ? {
        template: true
      } : null;
      return this.instantiate(list, _197, args);
    };
  }();
  (function() {
    var _198 = function() {
      if (dojo.config.parseOnLoad) {
        dojo.parser.parse();
      }
    };
    if (dojo.getObject("dijit.wai.onload") === dojo._loaders[0]) {
      dojo._loaders.splice(1, 0, _198);
    } else {
      dojo._loaders.unshift(_198);
    }
  })();
}
if (!dojo._hasResource["dojo.cache"]) {
  dojo._hasResource["dojo.cache"] = true;
  dojo.provide("dojo.cache");
  var cache = {};
  dojo.cache = function(_199, url, _19a) {
    if (typeof _199 == "string") {
      var _19b = dojo.moduleUrl(_199, url);
    } else {
      _19b = _199;
      _19a = url;
    }
    var key = _19b.toString();
    var val = _19a;
    if (_19a != undefined && !dojo.isString(_19a)) {
      val = ("value" in _19a ? _19a.value : undefined);
    }
    var _19c = _19a && _19a.sanitize ? true : false;
    if (typeof val == "string") {
      val = cache[key] = _19c ? dojo.cache._sanitize(val) : val;
    } else {
      if (val === null) {
        delete cache[key];
      } else {
        if (!(key in cache)) {
          val = dojo._getText(key);
          cache[key] = _19c ? dojo.cache._sanitize(val) : val;
        }
        val = cache[key];
      }
    }
    return val;
  };
  dojo.cache._sanitize = function(val) {
    if (val) {
      val = val.replace(/^\s*<\?xml(\s)+version=[\'\"](\d)*.(\d)*[\'\"](\s)*\?>/im, "");
      var _19d = val.match(/<body[^>]*>\s*([\s\S]+)\s*<\/body>/im);
      if (_19d) {
        val = _19d[1];
      }
    } else {
      val = "";
    }
    return val;
  };
}
if (!dojo._hasResource["dijit._Templated"]) {
  dojo._hasResource["dijit._Templated"] = true;
  dojo.provide("dijit._Templated");
  dojo.declare("dijit._Templated", null, {
    templateString: null,
    templatePath: null,
    widgetsInTemplate: false,
    _skipNodeCache: false,
    _earlyTemplatedStartup: false,
    constructor: function() {
      this._attachPoints = [];
      this._attachEvents = [];
    },
    _stringRepl: function(tmpl) {
      var _19e = this.declaredClass,
        _19f = this;
      return dojo.string.substitute(tmpl, this, function(_1a0, key) {
        if (key.charAt(0) == "!") {
          _1a0 = dojo.getObject(key.substr(1), false, _19f);
        }
        if (typeof _1a0 == "undefined") {
          throw new Error(_19e + " template:" + key);
        }
        if (_1a0 == null) {
          return "";
        }
        return key.charAt(0) == "!" ? _1a0 : _1a0.toString().replace(/"/g, "&quot;");
      }, this);
    },
    buildRendering: function() {
      var _1a1 = dijit._Templated.getCachedTemplate(this.templatePath, this.templateString, this._skipNodeCache);
      var node;
      if (dojo.isString(_1a1)) {
        node = dojo._toDom(this._stringRepl(_1a1));
        if (node.nodeType != 1) {
          throw new Error("Invalid template: " + _1a1);
        }
      } else {
        node = _1a1.cloneNode(true);
      }
      this.domNode = node;
      this.inherited(arguments);
      this._attachTemplateNodes(node);
      if (this.widgetsInTemplate) {
        var cw = (this._startupWidgets = dojo.parser.parse(node, {
          noStart: !this._earlyTemplatedStartup,
          template: true,
          inherited: {
            dir: this.dir,
            lang: this.lang
          },
          propsThis: this,
          scope: "dojo"
        }));
        this._supportingWidgets = dijit.findWidgets(node);
        this._attachTemplateNodes(cw, function(n, p) {
          return n[p];
        });
      }
      this._fillContent(this.srcNodeRef);
    },
    _fillContent: function(_1a2) {
      var dest = this.containerNode;
      if (_1a2 && dest) {
        while (_1a2.hasChildNodes()) {
          dest.appendChild(_1a2.firstChild);
        }
      }
    },
    _attachTemplateNodes: function(_1a3, _1a4) {
      _1a4 = _1a4 || function(n, p) {
        return n.getAttribute(p);
      };
      var _1a5 = dojo.isArray(_1a3) ? _1a3 : (_1a3.all || _1a3.getElementsByTagName("*"));
      var x = dojo.isArray(_1a3) ? 0 : -1;
      for (; x < _1a5.length; x++) {
        var _1a6 = (x == -1) ? _1a3 : _1a5[x];
        if (this.widgetsInTemplate && (_1a4(_1a6, "dojoType") || _1a4(_1a6, "data-dojo-type"))) {
          continue;
        }
        var _1a7 = _1a4(_1a6, "dojoAttachPoint") || _1a4(_1a6, "data-dojo-attach-point");
        if (_1a7) {
          var _1a8, _1a9 = _1a7.split(/\s*,\s*/);
          while ((_1a8 = _1a9.shift())) {
            if (dojo.isArray(this[_1a8])) {
              this[_1a8].push(_1a6);
            } else {
              this[_1a8] = _1a6;
            }
            this._attachPoints.push(_1a8);
          }
        }
        var _1aa = _1a4(_1a6, "dojoAttachEvent") || _1a4(_1a6, "data-dojo-attach-event");
        if (_1aa) {
          var _1ab, _1ac = _1aa.split(/\s*,\s*/);
          var trim = dojo.trim;
          while ((_1ab = _1ac.shift())) {
            if (_1ab) {
              var _1ad = null;
              if (_1ab.indexOf(":") != -1) {
                var _1ae = _1ab.split(":");
                _1ab = trim(_1ae[0]);
                _1ad = trim(_1ae[1]);
              } else {
                _1ab = trim(_1ab);
              }
              if (!_1ad) {
                _1ad = _1ab;
              }
              this._attachEvents.push(this.connect(_1a6, _1ab, _1ad));
            }
          }
        }
        var role = _1a4(_1a6, "waiRole");
        if (role) {
          dijit.setWaiRole(_1a6, role);
        }
        var _1af = _1a4(_1a6, "waiState");
        if (_1af) {
          dojo.forEach(_1af.split(/\s*,\s*/), function(_1b0) {
            if (_1b0.indexOf("-") != -1) {
              var pair = _1b0.split("-");
              dijit.setWaiState(_1a6, pair[0], pair[1]);
            }
          });
        }
      }
    },
    startup: function() {
      dojo.forEach(this._startupWidgets, function(w) {
        if (w && !w._started && w.startup) {
          w.startup();
        }
      });
      this.inherited(arguments);
    },
    destroyRendering: function() {
      dojo.forEach(this._attachPoints, function(_1b1) {
        delete this[_1b1];
      }, this);
      this._attachPoints = [];
      dojo.forEach(this._attachEvents, this.disconnect, this);
      this._attachEvents = [];
      this.inherited(arguments);
    }
  });
  dijit._Templated._templateCache = {};
  dijit._Templated.getCachedTemplate = function(_1b2, _1b3, _1b4) {
    var _1b5 = dijit._Templated._templateCache;
    var key = _1b3 || _1b2;
    var _1b6 = _1b5[key];
    if (_1b6) {
      try {
        if (!_1b6.ownerDocument || _1b6.ownerDocument == dojo.doc) {
          return _1b6;
        }
      } catch (e) {}
      dojo.destroy(_1b6);
    }
    if (!_1b3) {
      _1b3 = dojo.cache(_1b2, {
        sanitize: true
      });
    }
    _1b3 = dojo.string.trim(_1b3);
    if (_1b4 || _1b3.match(/\$\{([^\}]+)\}/g)) {
      return (_1b5[key] = _1b3);
    } else {
      var node = dojo._toDom(_1b3);
      if (node.nodeType != 1) {
        throw new Error("Invalid template: " + _1b3);
      }
      return (_1b5[key] = node);
    }
  };
  if (dojo.isIE) {
    dojo.addOnWindowUnload(function() {
      var _1b7 = dijit._Templated._templateCache;
      for (var key in _1b7) {
        var _1b8 = _1b7[key];
        if (typeof _1b8 == "object") {
          dojo.destroy(_1b8);
        }
        delete _1b7[key];
      }
    });
  }
  dojo.extend(dijit._Widget, {
    dojoAttachEvent: "",
    dojoAttachPoint: "",
    waiRole: "",
    waiState: ""
  });
}
if (!dojo._hasResource["dijit._Container"]) {
  dojo._hasResource["dijit._Container"] = true;
  dojo.provide("dijit._Container");
  dojo.declare("dijit._Container", null, {
    isContainer: true,
    buildRendering: function() {
      this.inherited(arguments);
      if (!this.containerNode) {
        this.containerNode = this.domNode;
      }
    },
    addChild: function(_1b9, _1ba) {
      var _1bb = this.containerNode;
      if (_1ba && typeof _1ba == "number") {
        var _1bc = this.getChildren();
        if (_1bc && _1bc.length >= _1ba) {
          _1bb = _1bc[_1ba - 1].domNode;
          _1ba = "after";
        }
      }
      dojo.place(_1b9.domNode, _1bb, _1ba);
      if (this._started && !_1b9._started) {
        _1b9.startup();
      }
    },
    removeChild: function(_1bd) {
      if (typeof _1bd == "number") {
        _1bd = this.getChildren()[_1bd];
      }
      if (_1bd) {
        var node = _1bd.domNode;
        if (node && node.parentNode) {
          node.parentNode.removeChild(node);
        }
      }
    },
    hasChildren: function() {
      return this.getChildren().length > 0;
    },
    destroyDescendants: function(_1be) {
      dojo.forEach(this.getChildren(), function(_1bf) {
        _1bf.destroyRecursive(_1be);
      });
    },
    _getSiblingOfChild: function(_1c0, dir) {
      var node = _1c0.domNode,
        _1c1 = (dir > 0 ? "nextSibling" : "previousSibling");
      do {
        node = node[_1c1];
      } while (node && (node.nodeType != 1 || !dijit.byNode(node)));
      return node && dijit.byNode(node);
    },
    getIndexOfChild: function(_1c2) {
      return dojo.indexOf(this.getChildren(), _1c2);
    },
    startup: function() {
      if (this._started) {
        return;
      }
      dojo.forEach(this.getChildren(), function(_1c3) {
        _1c3.startup();
      });
      this.inherited(arguments);
    }
  });
}
if (!dojo._hasResource["dijit._Contained"]) {
  dojo._hasResource["dijit._Contained"] = true;
  dojo.provide("dijit._Contained");
  dojo.declare("dijit._Contained", null, {
    getParent: function() {
      var _1c4 = dijit.getEnclosingWidget(this.domNode.parentNode);
      return _1c4 && _1c4.isContainer ? _1c4 : null;
    },
    _getSibling: function(_1c5) {
      var node = this.domNode;
      do {
        node = node[_1c5 + "Sibling"];
      } while (node && node.nodeType != 1);
      return node && dijit.byNode(node);
    },
    getPreviousSibling: function() {
      return this._getSibling("previous");
    },
    getNextSibling: function() {
      return this._getSibling("next");
    },
    getIndexInParent: function() {
      var p = this.getParent();
      if (!p || !p.getIndexOfChild) {
        return -1;
      }
      return p.getIndexOfChild(this);
    }
  });
}
if (!dojo._hasResource["dojo.NodeList-traverse"]) {
  dojo._hasResource["dojo.NodeList-traverse"] = true;
  dojo.provide("dojo.NodeList-traverse");
  dojo.extend(dojo.NodeList, {
    _buildArrayFromCallback: function(_1c6) {
      var ary = [];
      for (var i = 0; i < this.length; i++) {
        var _1c7 = _1c6.call(this[i], this[i], ary);
        if (_1c7) {
          ary = ary.concat(_1c7);
        }
      }
      return ary;
    },
    _getUniqueAsNodeList: function(_1c8) {
      var ary = [];
      for (var i = 0, node; node = _1c8[i]; i++) {
        if (node.nodeType == 1 && dojo.indexOf(ary, node) == -1) {
          ary.push(node);
        }
      }
      return this._wrap(ary, null, this._NodeListCtor);
    },
    _getUniqueNodeListWithParent: function(_1c9, _1ca) {
      var ary = this._getUniqueAsNodeList(_1c9);
      ary = (_1ca ? dojo._filterQueryResult(ary, _1ca) : ary);
      return ary._stash(this);
    },
    _getRelatedUniqueNodes: function(_1cb, _1cc) {
      return this._getUniqueNodeListWithParent(this._buildArrayFromCallback(_1cc), _1cb);
    },
    children: function(_1cd) {
      return this._getRelatedUniqueNodes(_1cd, function(node, ary) {
        return dojo._toArray(node.childNodes);
      });
    },
    closest: function(_1ce, root) {
      return this._getRelatedUniqueNodes(null, function(node, ary) {
        do {
          if (dojo._filterQueryResult([node], _1ce, root).length) {
            return node;
          }
        } while (node != root && (node = node.parentNode) && node.nodeType == 1);
        return null;
      });
    },
    parent: function(_1cf) {
      return this._getRelatedUniqueNodes(_1cf, function(node, ary) {
        return node.parentNode;
      });
    },
    parents: function(_1d0) {
      return this._getRelatedUniqueNodes(_1d0, function(node, ary) {
        var pary = [];
        while (node.parentNode) {
          node = node.parentNode;
          pary.push(node);
        }
        return pary;
      });
    },
    siblings: function(_1d1) {
      return this._getRelatedUniqueNodes(_1d1, function(node, ary) {
        var pary = [];
        var _1d2 = (node.parentNode && node.parentNode.childNodes);
        for (var i = 0; i < _1d2.length; i++) {
          if (_1d2[i] != node) {
            pary.push(_1d2[i]);
          }
        }
        return pary;
      });
    },
    next: function(_1d3) {
      return this._getRelatedUniqueNodes(_1d3, function(node, ary) {
        var next = node.nextSibling;
        while (next && next.nodeType != 1) {
          next = next.nextSibling;
        }
        return next;
      });
    },
    nextAll: function(_1d4) {
      return this._getRelatedUniqueNodes(_1d4, function(node, ary) {
        var pary = [];
        var next = node;
        while ((next = next.nextSibling)) {
          if (next.nodeType == 1) {
            pary.push(next);
          }
        }
        return pary;
      });
    },
    prev: function(_1d5) {
      return this._getRelatedUniqueNodes(_1d5, function(node, ary) {
        var prev = node.previousSibling;
        while (prev && prev.nodeType != 1) {
          prev = prev.previousSibling;
        }
        return prev;
      });
    },
    prevAll: function(_1d6) {
      return this._getRelatedUniqueNodes(_1d6, function(node, ary) {
        var pary = [];
        var prev = node;
        while ((prev = prev.previousSibling)) {
          if (prev.nodeType == 1) {
            pary.push(prev);
          }
        }
        return pary;
      });
    },
    andSelf: function() {
      return this.concat(this._parent);
    },
    first: function() {
      return this._wrap(((this[0] && [this[0]]) || []), this);
    },
    last: function() {
      return this._wrap((this.length ? [this[this.length - 1]] : []), this);
    },
    even: function() {
      return this.filter(function(item, i) {
        return i % 2 != 0;
      });
    },
    odd: function() {
      return this.filter(function(item, i) {
        return i % 2 == 0;
      });
    }
  });
}
if (!dojo._hasResource["dojo.NodeList-manipulate"]) {
  dojo._hasResource["dojo.NodeList-manipulate"] = true;
  dojo.provide("dojo.NodeList-manipulate");
  (function() {
    function _1d7(node) {
      var text = "",
        ch = node.childNodes;
      for (var i = 0, n; n = ch[i]; i++) {
        if (n.nodeType != 8) {
          if (n.nodeType == 1) {
            text += _1d7(n);
          } else {
            text += n.nodeValue;
          }
        }
      }
      return text;
    };

    function _1d8(node) {
      while (node.childNodes[0] && node.childNodes[0].nodeType == 1) {
        node = node.childNodes[0];
      }
      return node;
    };

    function _1d9(html, _1da) {
      if (typeof html == "string") {
        html = dojo._toDom(html, (_1da && _1da.ownerDocument));
        if (html.nodeType == 11) {
          html = html.childNodes[0];
        }
      } else {
        if (html.nodeType == 1 && html.parentNode) {
          html = html.cloneNode(false);
        }
      }
      return html;
    };
    dojo.extend(dojo.NodeList, {
      _placeMultiple: function(_1db, _1dc) {
        var nl2 = typeof _1db == "string" || _1db.nodeType ? dojo.query(_1db) : _1db;
        var _1dd = [];
        for (var i = 0; i < nl2.length; i++) {
          var _1de = nl2[i];
          var _1df = this.length;
          for (var j = _1df - 1, item; item = this[j]; j--) {
            if (i > 0) {
              item = this._cloneNode(item);
              _1dd.unshift(item);
            }
            if (j == _1df - 1) {
              dojo.place(item, _1de, _1dc);
            } else {
              _1de.parentNode.insertBefore(item, _1de);
            }
            _1de = item;
          }
        }
        if (_1dd.length) {
          _1dd.unshift(0);
          _1dd.unshift(this.length - 1);
          Array.prototype.splice.apply(this, _1dd);
        }
        return this;
      },
      innerHTML: function(_1e0) {
        if (arguments.length) {
          return this.addContent(_1e0, "only");
        } else {
          return this[0].innerHTML;
        }
      },
      text: function(_1e1) {
        if (arguments.length) {
          for (var i = 0, node; node = this[i]; i++) {
            if (node.nodeType == 1) {
              dojo.empty(node);
              node.appendChild(node.ownerDocument.createTextNode(_1e1));
            }
          }
          return this;
        } else {
          var _1e2 = "";
          for (i = 0; node = this[i]; i++) {
            _1e2 += _1d7(node);
          }
          return _1e2;
        }
      },
      val: function(_1e3) {
        if (arguments.length) {
          var _1e4 = dojo.isArray(_1e3);
          for (var _1e5 = 0, node; node = this[_1e5]; _1e5++) {
            var name = node.nodeName.toUpperCase();
            var type = node.type;
            var _1e6 = _1e4 ? _1e3[_1e5] : _1e3;
            if (name == "SELECT") {
              var opts = node.options;
              for (var i = 0; i < opts.length; i++) {
                var opt = opts[i];
                if (node.multiple) {
                  opt.selected = (dojo.indexOf(_1e3, opt.value) != -1);
                } else {
                  opt.selected = (opt.value == _1e6);
                }
              }
            } else {
              if (type == "checkbox" || type == "radio") {
                node.checked = (node.value == _1e6);
              } else {
                node.value = _1e6;
              }
            }
          }
          return this;
        } else {
          node = this[0];
          if (!node || node.nodeType != 1) {
            return undefined;
          }
          _1e3 = node.value || "";
          if (node.nodeName.toUpperCase() == "SELECT" && node.multiple) {
            _1e3 = [];
            opts = node.options;
            for (i = 0; i < opts.length; i++) {
              opt = opts[i];
              if (opt.selected) {
                _1e3.push(opt.value);
              }
            }
            if (!_1e3.length) {
              _1e3 = null;
            }
          }
          return _1e3;
        }
      },
      append: function(_1e7) {
        return this.addContent(_1e7, "last");
      },
      appendTo: function(_1e8) {
        return this._placeMultiple(_1e8, "last");
      },
      prepend: function(_1e9) {
        return this.addContent(_1e9, "first");
      },
      prependTo: function(_1ea) {
        return this._placeMultiple(_1ea, "first");
      },
      after: function(_1eb) {
        return this.addContent(_1eb, "after");
      },
      insertAfter: function(_1ec) {
        return this._placeMultiple(_1ec, "after");
      },
      before: function(_1ed) {
        return this.addContent(_1ed, "before");
      },
      insertBefore: function(_1ee) {
        return this._placeMultiple(_1ee, "before");
      },
      remove: dojo.NodeList.prototype.orphan,
      wrap: function(html) {
        if (this[0]) {
          html = _1d9(html, this[0]);
          for (var i = 0, node; node = this[i]; i++) {
            var _1ef = this._cloneNode(html);
            if (node.parentNode) {
              node.parentNode.replaceChild(_1ef, node);
            }
            var _1f0 = _1d8(_1ef);
            _1f0.appendChild(node);
          }
        }
        return this;
      },
      wrapAll: function(html) {
        if (this[0]) {
          html = _1d9(html, this[0]);
          this[0].parentNode.replaceChild(html, this[0]);
          var _1f1 = _1d8(html);
          for (var i = 0, node; node = this[i]; i++) {
            _1f1.appendChild(node);
          }
        }
        return this;
      },
      wrapInner: function(html) {
        if (this[0]) {
          html = _1d9(html, this[0]);
          for (var i = 0; i < this.length; i++) {
            var _1f2 = this._cloneNode(html);
            this._wrap(dojo._toArray(this[i].childNodes), null, this._NodeListCtor).wrapAll(_1f2);
          }
        }
        return this;
      },
      replaceWith: function(_1f3) {
        _1f3 = this._normalize(_1f3, this[0]);
        for (var i = 0, node; node = this[i]; i++) {
          this._place(_1f3, node, "before", i > 0);
          node.parentNode.removeChild(node);
        }
        return this;
      },
      replaceAll: function(_1f4) {
        var nl = dojo.query(_1f4);
        var _1f5 = this._normalize(this, this[0]);
        for (var i = 0, node; node = nl[i]; i++) {
          this._place(_1f5, node, "before", i > 0);
          node.parentNode.removeChild(node);
        }
        return this;
      },
      clone: function() {
        var ary = [];
        for (var i = 0; i < this.length; i++) {
          ary.push(this._cloneNode(this[i]));
        }
        return this._wrap(ary, this, this._NodeListCtor);
      }
    });
    if (!dojo.NodeList.prototype.html) {
      dojo.NodeList.prototype.html = dojo.NodeList.prototype.innerHTML;
    }
  })();
}
if (!dojo._hasResource["ibmweb.string"]) {
  dojo._hasResource["ibmweb.string"] = true;
  dojo.provide("ibmweb.string");
  ibmweb.string.normalizeSpace = function(str) {
    return str.replace(/\s+/g, " ");
  };
  String.prototype.normalizeSpace = function() {
    return ibmweb.string.normalizeSpace(this);
  };
  ibmweb.string.trim = function(str) {
    return str.replace(/^\s+/, "").replace(/\s+$/, "");
  };
  String.prototype.trim = function() {
    return ibmweb.string.trim(this);
  };
  ibmweb.string.htmlspecialchars = function(str) {
    return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/'/g, "&#039;").replace(/"/g, "&quot;");
  };
  String.prototype.htmlspecialchars = function() {
    return ibmweb.string.htmlspecialchars(this);
  };
  ibmweb.string.StringBuffer = function() {
    this.buffer = [];
  };
  ibmweb.string.StringBuffer.prototype.append = function append(_1f6) {
    this.buffer.push(_1f6);
    return this;
  };
  ibmweb.string.StringBuffer.prototype.toString = function toString(_1f7) {
    if (!this.buffer) {
      return "";
    }
    _1f7 = _1f7 || "";
    return this.buffer.join(_1f7);
  };
}
if (!dojo._hasResource["ibmweb.util"]) {
  dojo._hasResource["ibmweb.util"] = true;
  dojo.provide("ibmweb.util");
  ibmweb.util.__idcount = 0;
  ibmweb.util.generateId = function() {
    var nid;
    do {
      ibmweb.util.__idcount++;
      nid = "gi-" + ibmweb.util.__idcount;
    } while (document.getElementById(nid) != null);
    return nid;
  };
  ibmweb.util.getUrl = function() {
    var url = dojo.query("link[rel=canonical]").attr("href");
    if (url.length != 0) {
      return url[0];
    }
    url = location.href;
    if (url.indexOf("?") != -1) {
      var _1f8 = url.substr(0, url.indexOf("?"));
      var _1f9 = url.substr(url.indexOf("?") + 1).split("&");
      var _1fa = /^(cm_re|ca|me|met|re|lnk)=/;
      var _1fb = [];
      for (var i = 0, j = _1f9.length; i < j; i++) {
        if (!_1fa.test(_1f9[i])) {
          _1fb.push(_1f9[i]);
        }
      }
      if (_1fb.length > 0) {
        url = _1f8 + "?" + _1fb.join("&");
      } else {
        url = _1f8;
      }
    }
    return url;
  };
  ibmweb.util.statsHelper = function(e) {
    if (typeof(ibmStats) !== "undefined" && typeof(ibmStats.event) !== "undefined") {
      if (typeof(ibmweb) !== "undefined" && typeof(ibmweb.eluminate) !== "undefined" && typeof(ibmweb.eluminate.utilstatsHelper) === "function") {
        ibmweb.eluminate.utilstatsHelper(e);
      }
    }
  };
  ibmweb.util.preloadImages = function(_1fc, _1fd) {
    var _1fe = dojo.create("div", {
      style: {
        position: "absolute",
        top: "-9999px",
        height: "1px",
        overflow: "hidden"
      }
    }, dojo.body());
    var _1ff = 0;
    dojo.forEach(_1fc, function(url) {
      var img = dojo.create("img", {
        src: url
      }, _1fe);
      dojo.connect(img, "onload", function() {
        _1ff += 1;
        if (_1ff == _1fc.length) {
          if (typeof(_1fd) == "string") {
            dojo.publish(_1fd);
          } else {
            _1fd();
          }
        }
      });
    });
  };
  ibmweb.util.xhrGetHtml = function(_200) {
    if (_200.handleAs != "html") {
      return dojo.xhrGet(_200);
    }
    _200.handleAs = "text";
    var _201 = _200.load;
    _200.load = function(_202) {
      _202 = _202.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, "");
      var html = dojo.create("div", {
        innerHTML: _202
      }, null);
      _201(html);
    };
    return dojo.xhrGet(_200);
  };
}
if (!dojo._hasResource["ibmweb.queue"]) {
  dojo._hasResource["ibmweb.queue"] = true;
  dojo.provide("ibmweb.queue");
  ibmweb.queue._queue = [];
  ibmweb.queue._timer = null;
  ibmweb.queue._count = 0;
  ibmweb.queue._lock = false;
  ibmweb.queue.push = function(_203, _204) {
    ibmweb.queue._count++;
    ibmweb.queue._queue.push([ibmweb.queue._count, _203, _204]);
    ibmweb.queue._work();
    return ibmweb.queue._count;
  };
  ibmweb.queue.unshift = function(_205, _206) {
    ibmweb.queue._count++;
    ibmweb.queue._queue.unshift([ibmweb.queue._count, _205, _206]);
    ibmweb.queue._work();
    return ibmweb.queue._count;
  };
  ibmweb.queue.remove = function(_207) {
    for (var i = 0, j = ibmweb.queue._queue.length; i < j; i++) {
      if (ibmweb.queue._queue[i][0] == _207) {
        ibmweb.queue._queue.splice(i, 1);
        return true;
      }
    }
    return false;
  };
  ibmweb.queue._work = function() {
    if (ibmweb.queue._lock) {
      return;
    }
    if (ibmweb.queue._queue.length == 0) {
      return;
    }
    ibmweb.queue._lock = true;
    for (var i = 0, j = ibmweb.queue._queue.length; i < j; i++) {
      if (ibmweb.queue._queue[i][1]()) {
        ibmweb.queue._queue[i][2]();
        ibmweb.queue._queue.splice(i, 1);
        i--;
        j = ibmweb.queue._queue.length;
      }
    }
    ibmweb.queue._lock = false;
    ibmweb.queue._timer = setTimeout(ibmweb.queue._work, 50);
  };
  ibmweb.queue.waitForElement = function(_208, _209, _20a) {
    if (dojo._postLoad && typeof(_20a) != "undefined" && _20a == true) {
      _209();
      return;
    }
    var done = false;
    var eid = ibmweb.queue.push(function() {
      return document.getElementById(_208) != null;
    }, function() {
      if (done) {
        return;
      }
      done = true;
      _209();
    });
    if (typeof(_20a) != "undefined" && _20a == true) {
      dojo.addOnLoad(function() {
        if (done) {
          return;
        }
        done = true;
        ibmweb.queue.remove(eid);
        _209();
      });
    }
  };
}
if (!dojo._hasResource["ibmweb.meta"]) {
  dojo._hasResource["ibmweb.meta"] = true;
  dojo.provide("ibmweb.meta");
  ibmweb.meta.isLoaded = false;
  ibmweb.meta._init = function() {
    var tags = dojo.query("head meta").forEach(function(tag, _20b, _20c) {
      var name = tag.httpEquiv || tag.name;
      name = name.toLowerCase().replace(/[^\w]/g, "_");
      ibmweb.meta[name] = dojo.attr(tag, "content");
    });
    this.wwCase = "";
    this.cc = "";
    this.lc = "";
    if (!!this.ibm_country) {
      this.cc = this.ibm_country.toLowerCase().trim();
      if (this.cc.indexOf(",") > -1) {
        this.cc = this.cc.substring(0, this.cc.indexOf(",")).trim();
      }
      if (this.cc == "gb") {
        this.cc = "uk";
      }
      if (this.cc == "zz") {
        this.cc = "us";
        this.wwCase = "Y";
      }
    }
    if (!!this.dc_language) {
      this.lc = this.dc_language.substring(0, 2).toLowerCase();
    }
    var _20d = document.getElementsByTagName("html")[0].lang;
    if (!!_20d) {
      this.lc = this.lc || _20d.substring(0, 2).toLowerCase();
      this.cc = this.cc || _20d.substring(3, 5).toLowerCase();
    }
    this.lc = this.lc || "en";
    this.cc = this.cc || "us";
    this.cc = dojo.getObject("ibmweb.config.meta.cc") || this.cc;
    this.lc = dojo.getObject("ibmweb.config.meta.lc") || this.lc;
    this.cpi = this.cc + this.lc;
    if (this.cc === "us" && this.lc !== "en") {
      var _20e = {
        pt: "br",
        zh: "cn",
        de: "de",
        es: "es",
        fr: "fr",
        it: "it",
        ja: "jp",
        ko: "kr"
      };
      if (_20e[this.lc]) {
        this.cpi = _20e[this.lc] + this.lc;
      }
    }
    if (!this.dc_subject) {
      this.dc_subject = "ZZ999";
    }
    if (this.ibm_pageattributes == null) {
      this.ibm_pageattributes = "";
    }
    this.encoding = "utf8";
    this.encodingRaw = "UTF-8";
    if (this.content_type != null) {
      this.encodingRaw = this.content_type.substring(this.content_type.indexOf("=") + 1);
      this.encoding = this.encodingRaw.replace("-", "").replace("_", "").toLowerCase();
    }
    this.title = "";
    var tmp = dojo.query("title");
    if (tmp.length > 0) {
      this.title = tmp[0].innerHTML;
    }
    if (typeof(ibmwebConfigPortalFix) == "undefined") {
      if (!!window.ibmwebConfig && !!window.ibmwebConfig.noDojoLocaleOverride) {
        ibmweb.config.noDojoLocaleOverride = window.ibmwebConfig.noDojoLocaleOverride;
      }
      if (!ibmweb.config.noDojoLocaleOverride) {
        var cc = (this.cc == "uk") ? "gb" : this.cc;
        dojo.locale = dojo.config.locale = this.lc + "-" + cc;
      }
      if (ibmweb.meta._preloads.length > 0 && dojo.locale != "en-us") {
        for (var i = 0, j = ibmweb.meta._preloads.length; i < j; i++) {
          ibmweb.meta._preloadHack(ibmweb.meta._preloads[i], ibmweb.meta._preloadLocales);
        }
      }
    }
    ibmweb.meta.isLoaded = true;
  };
  if (typeof(ibmwebConfigPortalFix) == "undefined") {
    ibmweb.meta._preloads = [];
    if (dojo._hasResource["dojo.i18n"]) {
      ibmweb.meta._preloadHack = dojo.i18n._preloadLocalizations;
      dojo.i18n._preloadLocalizations = function(_20f, _210) {
        ibmweb.meta._preloads.push(_20f);
        ibmweb.meta._preloadLocales = _210;
        ibmweb.meta._preloadHack.apply(dojo.i18n, arguments);
      };
    }
  }
  ibmweb.queue.waitForElement("ibm-top", function() {
    ibmweb.meta._init();
  }, true);
}
if (!dojo._hasResource["ibmweb.info"]) {
  dojo._hasResource["ibmweb.info"] = true;
  dojo.provide("ibmweb.info");
  ibmweb.info.isLoaded = false;
  ibmweb.info._init = function() {
    this.v11 = this.v14 = this.v15 = this.v16 = this.v17 = this.www = this.w3 = false;
    dojo.query("link[rel^=\"style\"]").forEach(function(node) {
      if (node.href.indexOf("/common/v17/") != -1 || node.href.indexOf("/common/v17e/") != -1 || node.href.indexOf("/resources/ecm/") != -1) {
        ibmweb.info.v17 = true;
      } else {
        if (node.href.indexOf("/common/v16/css/screen") != -1) {
          ibmweb.info.v16 = true;
        } else {
          if (node.href.indexOf("/common/v15/") != -1) {
            ibmweb.info.v15 = true;
          } else {
            if (node.href.indexOf("/common/v14/") != -1) {
              ibmweb.info.v14 = true;
            } else {
              if (node.href.indexOf("/data/css/v11/") != -1) {
                ibmweb.info.v11 = true;
              }
            }
          }
        }
      }
    });
    if (ibmweb.info.v16 && ibmweb.info.v17) {
      ibmweb.info.v17 = false;
    }
    if (ibmweb.config.config == "www") {
      this.www = true;
    }
    if (ibmweb.config.config == "w3") {
      this.w3 = true;
    }
    var m = "msie",
      ua = navigator.userAgent.toLowerCase(),
      v = 0;
    var mi = ua.indexOf(m);
    if (ua.indexOf("opera") == -1 && mi != -1) {
      this.isIE = true;
      v = parseFloat(ua.substring(mi + m.length).replace(/^[^0-9]+/, ""));
    } else {
      this.isIE = false;
    }
    this.ieVersion = isNaN(v) ? 0 : v;
    var ts = [];
    this.isGecko = ua.indexOf("gecko") != -1 && ua.indexOf("khtml") == -1;
    this.geckoVersion = 0;
    if (this.isGecko) {
      ts = ua.split("rv:");
      if (ts.length > 1) {
        v = parseFloat(ts[1]);
      }
      this.geckoVersion = isNaN(v) ? 0 : v;
    }
    var str = "opera";
    var si = ua.indexOf(str);
    if (si == -1) {
      this.isOldOpera = false;
    } else {
      var ver = parseFloat(ua.substring(si + str.length));
      if (ver < 9) {
        this.isOldOpera = true;
      } else {
        this.isOldOpera = false;
      }
      this.OperaVersion = parseFloat(ua.substring(ua.indexOf("version") + 8));
    }
    this.isLinux = (navigator.userAgent.indexOf("Linux") > -1);
    this.isDOM = (!!document.createElement && !!document.createTextNode && !!document.getElementById && !!document.getElementsByTagName);
    this.iDevice = /iPad|iPhone|iPod/i.test(navigator.userAgent);
    this.mobileAndTabletcheck = function() {
      var _211 = false;
      (function(a) {
        if (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino|android|ipad|playbook|silk/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0, 4))) {
          _211 = true;
        }
      })(navigator.userAgent || navigator.vendor || window.opera);
      return _211;
    };
    this.checkHTML5support = function() {
      var _212 = document.createElement("video"),
        _213, h264, ogg, webm;
      var _214 = {};
      _214.html5supported = false;
      if (_212.canPlayType) {
        _214.html5supported = true;
        _214.mpeg4 = "" !== _212.canPlayType("video/mp4; codecs=\"mp4v.20.8\"");
        _214.h264 = "" !== (_212.canPlayType("video/mp4; codecs=\"avc1.42E01E\"") || _212.canPlayType("video/mp4; codecs=\"avc1.42E01E, mp4a.40.2\""));
        _214.ogg = "" !== _212.canPlayType("video/ogg; codecs=\"theora\"");
        _214.webm = "" !== _212.canPlayType("video/webm; codecs=\"vp8, vorbis\"");
      }
      return _214;
    };
    ibmweb.info.html5VideoSupport = this.checkHTML5support();
    ibmweb.info.mobileDevice = this.mobileAndTabletcheck();
    ibmweb.info.isLoaded = true;
  };
  ibmweb.queue.waitForElement("ibm-top", function() {
    ibmweb.info._init();
  }, true);
}
if (!dojo._hasResource["ibmweb.ibmcodesample"]) {
  dojo._hasResource["ibmweb.ibmcodesample"] = true;
  dojo.provide("ibmweb.ibmcodesample");
  ibmweb.ibmcodesample._init = function() {
    var _215 = [
      [/<br[\s|\W]*\/?>/gi, "<br />"],
      [/<(?!br \/)/gi, "&lt;"],
      [/&lt;(\/?)(\w*)/gi, function() {
        var _216 = (arguments[1] == "") ? "" : "/",
          _217 = arguments[2].toLowerCase(),
          _218 = (_217 == "hr") ? "/" : "";
        return "&lt;" + _216 + _217 + _218;
      }],
      [/&(?!amp;|nbsp;|lt;|gt;)/gi, "&amp;"],
      [/(&lt;select[^&>]*(&gt;|>)|&lt;\/option[^&>]*(&gt;|>))/gi, function() {
        if (!dojo.isIE) {
          return arguments[0];
        }
        return arguments[0] + "<br />";
      }],
      [/\n\s*|\r\s*/gi, "<br />"],
      [/<br \/><br \/>/gi, "<br />"]
    ];
    dojo.query(".ibm-alternate-code-sample pre, .ibm-alternate-code-sample code, .ibm-code-sample pre, .ibm-code-sample code").forEach(function(_219) {
      var _21a = _219.innerHTML;
      dojo.forEach(_215, function(_21b) {
        _21a = _21a.replace(_21b[0], _21b[1]);
      });
      _219.innerHTML = _21a;
    });
  };
  ibmweb.queue.waitForElement("ibm-top", function() {
    ibmweb.ibmcodesample._init();
  }, true);
}
if (!dojo._hasResource["ibmweb.callback"]) {
  dojo._hasResource["ibmweb.callback"] = true;
  dojo.provide("ibmweb.callback");
  ibmweb.callback.__callbacks = {};
  ibmweb.callback.register = function(_21c, _21d) {
    dojo.subscribe("/ibmweb/callback/" + _21c, _21d);
  };
  ibmweb.callback.call = function(_21e, data) {
    dojo.publish("/ibmweb/callback/" + _21e, data);
  };
  ibmweb.callback.createJSONPWrapper = function() {
    var _21f = null;
    if (typeof(arguments[0]) == "function") {
      _21f = dojo.partial.apply(dojo.partial, arguments);
    } else {
      _21f = dojo.hitch.apply(dojo.hitch, arguments);
    }
    var _220 = "__tcb" + Math.round(Math.random() * 100000);
    window[_220] = _21f;
    dojo.connect(null, _220, function() {
      window[_220] = null;
    });
    return _220;
  };
}
if (!dojo._hasResource["dojo.io.script"]) {
  dojo._hasResource["dojo.io.script"] = true;
  dojo.provide("dojo.io.script");
  dojo.getObject("io", true, dojo);
  (function() {
    var _221 = dojo.isIE ? "onreadystatechange" : "load",
      _222 = /complete|loaded/;
    dojo.io.script = {
      get: function(args) {
        var dfd = this._makeScriptDeferred(args);
        var _223 = dfd.ioArgs;
        dojo._ioAddQueryToUrl(_223);
        dojo._ioNotifyStart(dfd);
        if (this._canAttach(_223)) {
          var node = this.attach(_223.id, _223.url, args.frameDoc);
          if (!_223.jsonp && !_223.args.checkString) {
            var _224 = dojo.connect(node, _221, function(evt) {
              if (evt.type == "load" || _222.test(node.readyState)) {
                dojo.disconnect(_224);
                _223.scriptLoaded = evt;
              }
            });
          }
        }
        dojo._ioWatch(dfd, this._validCheck, this._ioCheck, this._resHandle);
        return dfd;
      },
      attach: function(id, url, _225) {
        var doc = (_225 || dojo.doc);
        var _226 = doc.createElement("script");
        _226.type = "text/javascript";
        _226.src = url;
        _226.id = id;
        _226.charset = "utf-8";
        if (url != null && url.indexOf('faces.tap') >=0){
          return null;
        }
        return doc.getElementsByTagName("head")[0].appendChild(_226);
      },
      remove: function(id, _227) {
        dojo.destroy(dojo.byId(id, _227));
        if (this["jsonp_" + id]) {
          delete this["jsonp_" + id];
        }
      },
      _makeScriptDeferred: function(args) {
        var dfd = dojo._ioSetArgs(args, this._deferredCancel, this._deferredOk, this._deferredError);
        var _228 = dfd.ioArgs;
        _228.id = dojo._scopeName + "IoScript" + (this._counter++);
        _228.canDelete = false;
        _228.jsonp = args.callbackParamName || args.jsonp;
        if (_228.jsonp) {
          _228.query = _228.query || "";
          if (_228.query.length > 0) {
            _228.query += "&";
          }
          _228.query += _228.jsonp + "=" + (args.frameDoc ? "parent." : "") + dojo._scopeName + ".io.script.jsonp_" + _228.id + "._jsonpCallback";
          _228.frameDoc = args.frameDoc;
          _228.canDelete = true;
          dfd._jsonpCallback = this._jsonpCallback;
          this["jsonp_" + _228.id] = dfd;
        }
        return dfd;
      },
      _deferredCancel: function(dfd) {
        dfd.canceled = true;
        if (dfd.ioArgs.canDelete) {
          dojo.io.script._addDeadScript(dfd.ioArgs);
        }
      },
      _deferredOk: function(dfd) {
        var _229 = dfd.ioArgs;
        if (_229.canDelete) {
          dojo.io.script._addDeadScript(_229);
        }
        return _229.json || _229.scriptLoaded || _229;
      },
      _deferredError: function(_22a, dfd) {
        if (dfd.ioArgs.canDelete) {
          if (_22a.dojoType == "timeout") {
            dojo.io.script.remove(dfd.ioArgs.id, dfd.ioArgs.frameDoc);
          } else {
            dojo.io.script._addDeadScript(dfd.ioArgs);
          }
        }
        return _22a;
      },
      _deadScripts: [],
      _counter: 1,
      _addDeadScript: function(_22b) {
        dojo.io.script._deadScripts.push({
          id: _22b.id,
          frameDoc: _22b.frameDoc
        });
        _22b.frameDoc = null;
      },
      _validCheck: function(dfd) {
        var _22c = dojo.io.script;
        var _22d = _22c._deadScripts;
        if (_22d && _22d.length > 0) {
          for (var i = 0; i < _22d.length; i++) {
            _22c.remove(_22d[i].id, _22d[i].frameDoc);
            _22d[i].frameDoc = null;
          }
          dojo.io.script._deadScripts = [];
        }
        return true;
      },
      _ioCheck: function(dfd) {
        var _22e = dfd.ioArgs;
        if (_22e.json || (_22e.scriptLoaded && !_22e.args.checkString)) {
          return true;
        }
        var _22f = _22e.args.checkString;
        if (_22f && eval("typeof(" + _22f + ") != 'undefined'")) {
          return true;
        }
        return false;
      },
      _resHandle: function(dfd) {
        if (dojo.io.script._ioCheck(dfd)) {
          dfd.callback(dfd);
        } else {
          dfd.errback(new Error("inconceivable dojo.io.script._resHandle error"));
        }
      },
      _canAttach: function(_230) {
        return true;
      },
      _jsonpCallback: function(json) {
        this.ioArgs.json = json;
      }
    };
  })();
}
if (!dojo._hasResource["ibmweb.data"]) {
  dojo._hasResource["ibmweb.data"] = true;
  dojo.provide("ibmweb.data");
  ibmweb.data.settings = {
    "url": "//www.ibm.com/common/translations/{TOKEN}/{CC}/{LC}/{ENC}.js",
    "cc": null,
    "lc": null,
    "enc": null
  };
  ibmweb.data.require = function(_231, _232, _233) {
    if (!_231) {
      return;
    }
    if (!dojo.isArray(_231)) {
      _231 = [_231];
    }
    var _234 = true;
    for (var i = 0, j = _231.length; i < j; i++) {
      _234 = _234 && ibmweb.data.exists(_231[i]);
    }
    if (!_234) {
      var _235 = dojo.mixin(_235, ibmweb.data.settings, _233);
      ibmweb.queue.push(function() {
        return ibmweb.meta.isLoaded;
      }, function() {
        var _236 = function(_237) {
          var url = _235.url.replace("{CC}", _235.cc ? _235.cc : ibmweb.meta.cc).replace("{LC}", _235.lc ? _235.lc : ibmweb.meta.lc).replace("{TOKEN}", _237).replace("{ENC}", _235.enc ? _235.enc : ibmweb.meta.encoding);
          dojo.io.script.attach(null, url, null);
        };
        for (var i = 0, j = _231.length; i < j; i++) {
          if (!ibmweb.data.exists(_231[i])) {
            _236(_231[i]);
          }
        }
        ibmweb.queue.push(function() {
          var _238 = true;
          for (var i = 0, j = _231.length; i < j; i++) {
            _238 = _238 && ibmweb.data.exists(_231[i]);
          }
          return _238;
        }, function() {
          if (typeof(_232) == "function") {
            _232(ibmweb.data);
          }
        });
      });
    } else {
      if (typeof(_232) == "function") {
        _232(ibmweb.data);
      }
    }
  };
  ibmweb.data.provide = function(_239, _23a) {
    ibmweb.data[_239] = _23a;
  };
  ibmweb.data.exists = function(_23b) {
    return !!ibmweb.data[_23b];
  };
}
if (!dojo._hasResource["dojo.fx.Toggler"]) {
  dojo._hasResource["dojo.fx.Toggler"] = true;
  dojo.provide("dojo.fx.Toggler");
  dojo.declare("dojo.fx.Toggler", null, {
    node: null,
    showFunc: dojo.fadeIn,
    hideFunc: dojo.fadeOut,
    showDuration: 200,
    hideDuration: 200,
    constructor: function(args) {
      var _23c = this;
      dojo.mixin(_23c, args);
      _23c.node = args.node;
      _23c._showArgs = dojo.mixin({}, args);
      _23c._showArgs.node = _23c.node;
      _23c._showArgs.duration = _23c.showDuration;
      _23c.showAnim = _23c.showFunc(_23c._showArgs);
      _23c._hideArgs = dojo.mixin({}, args);
      _23c._hideArgs.node = _23c.node;
      _23c._hideArgs.duration = _23c.hideDuration;
      _23c.hideAnim = _23c.hideFunc(_23c._hideArgs);
      dojo.connect(_23c.showAnim, "beforeBegin", dojo.hitch(_23c.hideAnim, "stop", true));
      dojo.connect(_23c.hideAnim, "beforeBegin", dojo.hitch(_23c.showAnim, "stop", true));
    },
    show: function(_23d) {
      return this.showAnim.play(_23d || 0);
    },
    hide: function(_23e) {
      return this.hideAnim.play(_23e || 0);
    }
  });
}
if (!dojo._hasResource["dojo.fx"]) {
  dojo._hasResource["dojo.fx"] = true;
  dojo.provide("dojo.fx");
  (function() {
    var d = dojo,
      _23f = {
        _fire: function(evt, args) {
          if (this[evt]) {
            this[evt].apply(this, args || []);
          }
          return this;
        }
      };
    var _240 = function(_241) {
      this._index = -1;
      this._animations = _241 || [];
      this._current = this._onAnimateCtx = this._onEndCtx = null;
      this.duration = 0;
      d.forEach(this._animations, function(a) {
        this.duration += a.duration;
        if (a.delay) {
          this.duration += a.delay;
        }
      }, this);
    };
    d.extend(_240, {
      _onAnimate: function() {
        this._fire("onAnimate", arguments);
      },
      _onEnd: function() {
        d.disconnect(this._onAnimateCtx);
        d.disconnect(this._onEndCtx);
        this._onAnimateCtx = this._onEndCtx = null;
        if (this._index + 1 == this._animations.length) {
          this._fire("onEnd");
        } else {
          this._current = this._animations[++this._index];
          this._onAnimateCtx = d.connect(this._current, "onAnimate", this, "_onAnimate");
          this._onEndCtx = d.connect(this._current, "onEnd", this, "_onEnd");
          this._current.play(0, true);
        }
      },
      play: function(_242, _243) {
        if (!this._current) {
          this._current = this._animations[this._index = 0];
        }
        if (!_243 && this._current.status() == "playing") {
          return this;
        }
        var _244 = d.connect(this._current, "beforeBegin", this, function() {
            this._fire("beforeBegin");
          }),
          _245 = d.connect(this._current, "onBegin", this, function(arg) {
            this._fire("onBegin", arguments);
          }),
          _246 = d.connect(this._current, "onPlay", this, function(arg) {
            this._fire("onPlay", arguments);
            d.disconnect(_244);
            d.disconnect(_245);
            d.disconnect(_246);
          });
        if (this._onAnimateCtx) {
          d.disconnect(this._onAnimateCtx);
        }
        this._onAnimateCtx = d.connect(this._current, "onAnimate", this, "_onAnimate");
        if (this._onEndCtx) {
          d.disconnect(this._onEndCtx);
        }
        this._onEndCtx = d.connect(this._current, "onEnd", this, "_onEnd");
        this._current.play.apply(this._current, arguments);
        return this;
      },
      pause: function() {
        if (this._current) {
          var e = d.connect(this._current, "onPause", this, function(arg) {
            this._fire("onPause", arguments);
            d.disconnect(e);
          });
          this._current.pause();
        }
        return this;
      },
      gotoPercent: function(_247, _248) {
        this.pause();
        var _249 = this.duration * _247;
        this._current = null;
        d.some(this._animations, function(a) {
          if (a.duration <= _249) {
            this._current = a;
            return true;
          }
          _249 -= a.duration;
          return false;
        });
        if (this._current) {
          this._current.gotoPercent(_249 / this._current.duration, _248);
        }
        return this;
      },
      stop: function(_24a) {
        if (this._current) {
          if (_24a) {
            for (; this._index + 1 < this._animations.length; ++this._index) {
              this._animations[this._index].stop(true);
            }
            this._current = this._animations[this._index];
          }
          var e = d.connect(this._current, "onStop", this, function(arg) {
            this._fire("onStop", arguments);
            d.disconnect(e);
          });
          this._current.stop();
        }
        return this;
      },
      status: function() {
        return this._current ? this._current.status() : "stopped";
      },
      destroy: function() {
        if (this._onAnimateCtx) {
          d.disconnect(this._onAnimateCtx);
        }
        if (this._onEndCtx) {
          d.disconnect(this._onEndCtx);
        }
      }
    });
    d.extend(_240, _23f);
    dojo.fx.chain = function(_24b) {
      return new _240(_24b);
    };
    var _24c = function(_24d) {
      this._animations = _24d || [];
      this._connects = [];
      this._finished = 0;
      this.duration = 0;
      d.forEach(_24d, function(a) {
        var _24e = a.duration;
        if (a.delay) {
          _24e += a.delay;
        }
        if (this.duration < _24e) {
          this.duration = _24e;
        }
        this._connects.push(d.connect(a, "onEnd", this, "_onEnd"));
      }, this);
      this._pseudoAnimation = new d.Animation({
        curve: [0, 1],
        duration: this.duration
      });
      var self = this;
      d.forEach(["beforeBegin", "onBegin", "onPlay", "onAnimate", "onPause", "onStop", "onEnd"], function(evt) {
        self._connects.push(d.connect(self._pseudoAnimation, evt, function() {
          self._fire(evt, arguments);
        }));
      });
    };
    d.extend(_24c, {
      _doAction: function(_24f, args) {
        d.forEach(this._animations, function(a) {
          a[_24f].apply(a, args);
        });
        return this;
      },
      _onEnd: function() {
        if (++this._finished > this._animations.length) {
          this._fire("onEnd");
        }
      },
      _call: function(_250, args) {
        var t = this._pseudoAnimation;
        t[_250].apply(t, args);
      },
      play: function(_251, _252) {
        this._finished = 0;
        this._doAction("play", arguments);
        this._call("play", arguments);
        return this;
      },
      pause: function() {
        this._doAction("pause", arguments);
        this._call("pause", arguments);
        return this;
      },
      gotoPercent: function(_253, _254) {
        var ms = this.duration * _253;
        d.forEach(this._animations, function(a) {
          a.gotoPercent(a.duration < ms ? 1 : (ms / a.duration), _254);
        });
        this._call("gotoPercent", arguments);
        return this;
      },
      stop: function(_255) {
        this._doAction("stop", arguments);
        this._call("stop", arguments);
        return this;
      },
      status: function() {
        return this._pseudoAnimation.status();
      },
      destroy: function() {
        d.forEach(this._connects, dojo.disconnect);
      }
    });
    d.extend(_24c, _23f);
    dojo.fx.combine = function(_256) {
      return new _24c(_256);
    };
    dojo.fx.wipeIn = function(args) {
      var node = args.node = d.byId(args.node),
        s = node.style,
        o;
      var anim = d.animateProperty(d.mixin({
        properties: {
          height: {
            start: function() {
              o = s.overflow;
              s.overflow = "hidden";
              if (s.visibility == "hidden" || s.display == "none") {
                s.height = "1px";
                s.display = "";
                s.visibility = "";
                return 1;
              } else {
                var _257 = d.style(node, "height");
                return Math.max(_257, 1);
              }
            },
            end: function() {
              return node.scrollHeight;
            }
          }
        }
      }, args));
      d.connect(anim, "onEnd", function() {
        s.height = "auto";
        s.overflow = o;
      });
      return anim;
    };
    dojo.fx.wipeOut = function(args) {
      var node = args.node = d.byId(args.node),
        s = node.style,
        o;
      var anim = d.animateProperty(d.mixin({
        properties: {
          height: {
            end: 1
          }
        }
      }, args));
      d.connect(anim, "beforeBegin", function() {
        o = s.overflow;
        s.overflow = "hidden";
        s.display = "";
      });
      d.connect(anim, "onEnd", function() {
        s.overflow = o;
        s.height = "auto";
        s.display = "none";
      });
      return anim;
    };
    dojo.fx.slideTo = function(args) {
      var node = args.node = d.byId(args.node),
        top = null,
        left = null;
      var init = (function(n) {
        return function() {
          var cs = d.getComputedStyle(n);
          var pos = cs.position;
          top = (pos == "absolute" ? n.offsetTop : parseInt(cs.top) || 0);
          left = (pos == "absolute" ? n.offsetLeft : parseInt(cs.left) || 0);
          if (pos != "absolute" && pos != "relative") {
            var ret = d.position(n, true);
            top = ret.y;
            left = ret.x;
            n.style.position = "absolute";
            n.style.top = top + "px";
            n.style.left = left + "px";
          }
        };
      })(node);
      init();
      var anim = d.animateProperty(d.mixin({
        properties: {
          top: args.top || 0,
          left: args.left || 0
        }
      }, args));
      d.connect(anim, "beforeBegin", anim, init);
      return anim;
    };
  })();
}
if (!dojo._hasResource["dojo.NodeList-fx"]) {
  dojo._hasResource["dojo.NodeList-fx"] = true;
  dojo.provide("dojo.NodeList-fx");
  dojo.extend(dojo.NodeList, {
    _anim: function(obj, _258, args) {
      args = args || {};
      var a = dojo.fx.combine(this.map(function(item) {
        var _259 = {
          node: item
        };
        dojo.mixin(_259, args);
        return obj[_258](_259);
      }));
      return args.auto ? a.play() && this : a;
    },
    wipeIn: function(args) {
      return this._anim(dojo.fx, "wipeIn", args);
    },
    wipeOut: function(args) {
      return this._anim(dojo.fx, "wipeOut", args);
    },
    slideTo: function(args) {
      return this._anim(dojo.fx, "slideTo", args);
    },
    fadeIn: function(args) {
      return this._anim(dojo, "fadeIn", args);
    },
    fadeOut: function(args) {
      return this._anim(dojo, "fadeOut", args);
    },
    animateProperty: function(args) {
      return this._anim(dojo, "animateProperty", args);
    },
    anim: function(_25a, _25b, _25c, _25d, _25e) {
      var _25f = dojo.fx.combine(this.map(function(item) {
        return dojo.animateProperty({
          node: item,
          properties: _25a,
          duration: _25b || 350,
          easing: _25c
        });
      }));
      if (_25d) {
        dojo.connect(_25f, "onEnd", _25d);
      }
      return _25f.play(_25e || 0);
    }
  });
}
if (!dojo._hasResource["ibmweb.twisty"]) {
  dojo._hasResource["ibmweb.twisty"] = true;
  dojo.provide("ibmweb.twisty");
  ibmweb.twisty._duration = 200;
  ibmweb.twisty.cachetimeout = 3600 * 12 * 7;
  ibmweb.twisty.initTwisty = function(ul) {
    var li = dojo.query("> li", ul);
    if (!dojo.hasClass(ul, "ibm-twisty")) {
      return;
    }
    li.forEach(function(node) {
      if (!dojo.hasClass(node, "ibm-active")) {
        ibmweb.twisty.collapseTwisty(node);
      }
      var _260 = dojo.query("> a.ibm-twisty-trigger", node).onclick(ibmweb.twisty.toggleTwisty);
      var _261 = dojo.query("> span.ibm-twisty-head", node).onclick(ibmweb.twisty.toggleTwisty);
    });
  };
  ibmweb.twisty.expandTwisty = function(li) {
    dojo.addClass(li, "ibm-active");
    dojo.query("> a.ibm-twisty-trigger", li).removeClass("ibm-twisty-trigger-closed").query("img").attr("alt", "collapse");
    dojo.query("> div.ibm-twisty-body", li).style("display", "block");
  };
  ibmweb.twisty.collapseTwisty = function(li) {
    dojo.removeClass(li, "ibm-active");
    dojo.query("> a.ibm-twisty-trigger", li).addClass("ibm-twisty-trigger-closed").query("img").attr("alt", "expand");
    dojo.query("> div.ibm-twisty-body", li).style("display", "none");
  };
  ibmweb.twisty.toggleTwisty = function(_262) {
    var li = this.parentNode;
    if (dojo.hasClass(li, "ibm-active")) {
      ibmweb.twisty.collapseTwisty(li);
    } else {
      ibmweb.twisty.expandTwisty(li);
    }
    dojo.stopEvent(_262);
    return false;
  };
  ibmweb.twisty.initSimpleShowHide = function(el) {
    if (!dojo.hasClass(el, "ibm-simple-show-hide")) {
      return;
    }
    var _263 = dojo.query("div.ibm-hideable", el);
    _263.wipeOut({
      duration: 1
    }).play();
    var _264 = true;
    var _265 = 0;
    var _266 = dojo.query("p.ibm-show-hide-controls", el).style("display", "block");
    if (_266.length) {
      dojo.place("<div class=\"ibm-rule\"><hr /></div>", _266[0], "last");
    }
    _266.query("a").onclick(function(_267) {
      dojo.query("a", this.parentNode).removeClass("ibm-active");
      dojo.addClass(_267.target, "ibm-active");
      var href = _267.target.href;
      if (href.indexOf("#show-hide") != -1) {
        if (_264) {
          _263.wipeIn().play();
          _264 = false;
        } else {
          _263.wipeOut().play();
          _264 = true;
          dojo.removeClass(_267.target, "ibm-active");
        }
      } else {
        if (href.indexOf("#show") != -1) {
          if (_265 != 1) {
            _263.wipeIn().play();
            _265 = 1;
          }
        } else {
          if (href.indexOf("#hide") != -1) {
            if (_265 != 2) {
              _263.wipeOut().play();
              _265 = 2;
            }
          }
        }
      }
      dojo.stopEvent(_267);
      return false;
    });
    return false;
  };
  ibmweb.twisty.initShowHide = function(el, _268) {
    if (!dojo.hasClass(el, "ibm-show-hide")) {
      return;
    }
    if (!_268) {
      _268 = "h2";
    }
    var _269 = dojo.query("> div.ibm-container-body, > div.ibm-columns", el);
    var _26a = dojo.query("> " + _268, el);
    _26a.forEach(function(_26b, i) {
      var body = _269[i];
      var a = dojo.query("a", _26b);
      a.attr("href", "#show-hide");
      dojo.style(body, "overflow", "hidden");
      var r = 0;
      if (el.id) {
        r = ibmweb.storage.getItem("ibmweb.twisty." + ibmweb.util.getUrl() + "." + el.id);
        if (r !== "h") {
          r = isNaN(Number(r)) ? 0 : Number(r);
        }
      }
      if (i === r) {
        a.addClass("ibm-show-active");
        if (dojo.hasClass(el, "ibm-alternate")) {
          a.removeClass("ibm-show-active");
          dojo.style(body, {
            "display": "none",
            "height": "1px"
          });
        }
      } else {
        a.removeClass("ibm-show-active");
        dojo.style(_26b, "border-top", "none");
        dojo.style(body, {
          "display": "none",
          "height": "1px"
        });
      }
      a.onclick(function(_26c) {
        dojo.stopEvent(_26c);
        if (dojo.hasClass(this, "ibm-show-active")) {
          if (_269.length == 1 || dojo.hasClass(el, "ibm-alternate")) {
            dojo.removeClass(this, "ibm-show-active");
            if (dojo.hasClass(el, "ibm-alternate")) {
              var _26d = dojo.query(this).parent().next();
              _26d.style("overflow", "hidden");
              _26d.anim({
                height: 1
              }, ibmweb.twisty._duration, null, function() {
                _26d.style("display", "none");
                if (el.id) {
                  ibmweb.storage.setItem("ibmweb.twisty." + ibmweb.util.getUrl() + "." + el.id, "h", ibmweb.twisty.cachetimeout);
                }
              });
            }
            var _26e = _269[0];
            _26e.style.overflow = "hidden";
            dojo.anim(_26e, {
              height: 1
            }, ibmweb.twisty._duration, null, function() {
              _26e.style.display = "none";
              if (el.id) {
                ibmweb.storage.setItem("ibmweb.twisty." + ibmweb.util.getUrl() + "." + el.id, "h", ibmweb.twisty.cachetimeout);
              }
            });
          }
          return false;
        }
        var _26f = this;
        _26a.forEach(function(_270, i) {
          var body = dojo.query(_269[i])[0];
          var a = dojo.query("a", _270);
          if (a[0] === _26f) {
            a.addClass("ibm-show-active");
            dojo.style(body, {
              "display": "block",
              "overflow": "visible",
              "height": "auto"
            });
            var _271 = body.clientHeight || body.scrollHeight;
            dojo.style(body, {
              "overflow": "hidden",
              "height": "1px"
            });
            dojo.anim(body, {
              height: _271
            }, ibmweb.twisty._duration, null, function() {
              body.style.height = "auto";
              var _272 = window.document.documentElement.scrollTop || window.document.body.scrollTop;
              var top = dojo.marginBox(_270).t;
              var mhh = dojo.position(dojo.byId("ibm-masthead")).h;
              if (top - mhh < _272) {
                window.scrollTo(0, dojo.marginBox(_270).t - dojo.position(dojo.byId("ibm-masthead")).h);
              }
              if (el.id) {
                ibmweb.storage.setItem("ibmweb.twisty." + ibmweb.util.getUrl() + "." + el.id, i, ibmweb.twisty.cachetimeout);
              }
            });
          } else {
            a.removeClass("ibm-show-active");
            dojo.anim(body, {
              height: 1
            }, ibmweb.twisty._duration, null, function() {
              body.style.display = "none";
            });
          }
        });
        return false;
      });
    });
  };
  dojo.addOnLoad(function() {
    if (!ibmweb.info.v17) {
      return;
    }
    dojo.query("div.ibm-simple-show-hide").forEach(function(node) {
      ibmweb.twisty.initSimpleShowHide(node);
    });
    dojo.query("div.ibm-container.ibm-show-hide").forEach(function(node) {
      ibmweb.twisty.initShowHide(node);
    });
    var _273 = false;
    dojo.query("ul.ibm-twisty").forEach(function(node) {
      if (!_273) {
        (new Image()).src = "//www.ibm.com/i/v16/icons/tw_closed.gif";
        _273 = true;
      }
      ibmweb.twisty.initTwisty(node);
    });
  });
}
if (!dojo._hasResource["ibmweb.overlay"]) {
  dojo._hasResource["ibmweb.overlay"] = true;
  dojo.provide("ibmweb.overlay");
  var handleTouch = function(e) {
    e.preventDefault();
  };
  ibmweb.overlay.show = function(_274, _275, _276) {
    dojo["require"]("dijit.Dialog");
    dojo.addOnLoad(function() {
      var _277 = dijit.byId("dialog_" + _274);
      if (!_277) {
        dojo.byId(_274).style.display = "block";
        _277 = new dijit.Dialog(dojo.mixin({
          "content": dojo.byId(_274),
          "id": "dialog_" + _274,
          draggable: !navigator.userAgent.match(/(iPad|iPhone|iPod|Android)/g)
        }, _276));
        dojo.connect(dijit.byId("dialog_" + _274), "onHide", function() {
          ibmweb.overlay.hide(_274);
        });
      }
      try {
        dojo.addClass(document.body, "stop-scrolling");
        if (document.body.addEventListener) {
          document.body.addEventListener("touchmove", handleTouch, false);
        } else {
          if (document.body.attachEvent) {
            document.body.attachEvent("on" + "touchmove", handleTouch);
          } else {
            document.body["on" + "touchmove"] = handleTouch;
          }
        }
        _277.show().then(function() {
          var _278 = dojo.query(".dijitDialogUnderlayWrapper");
          dojo.connect(_278[0], "onclick", function(e) {
            dojo.query("[role=\"dialog\"]").forEach(function(_279) {
              if (_279.className.match("dijit")) {
                if (_279.id) {
                  var _27a = dijit.byId(_279.id);
                  if (_27a._isShown() === true) {
                    dojo.query(_27a.closeButtonNode)[0].click();
                  }
                }
              }
            });
          });
          if (dojo.query("#" + _274 + " .ibm-body .ibm-main")[0].addEventListener) {
            dojo.query("#" + _274 + " .ibm-body .ibm-main")[0].addEventListener("touchmove", function(e) {
              e.stopPropagation();
            }, false);
          } else {
            if (dojo.query("#" + _274 + " .ibm-body .ibm-main")[0].attachEvent) {
              dojo.query("#" + _274 + " .ibm-body .ibm-main")[0].attachEvent("on" + "touchmove", function(e) {
                e.stopPropagation();
              });
            } else {
              dojo.query("#" + _274 + " .ibm-body .ibm-main")[0]["on" + "touchmove"] = function(e) {
                e.stopPropagation();
              };
            }
          }
          var _27b = dojo.query("#" + _274 + " .ibm-body .ibm-main .ibm-title").children()[0];
          if (typeof(_27b) != "undefined") {
            var _27c = dojo.attr(_27b, "id");
            if (_27c != null) {
              dojo.removeAttr(dojo.query("#dialog_" + _274 + "_title")[0], "id");
              dojo.attr(dojo.query("#dialog_" + _274)[0], "aria-labelledby", _27c);
            } else {
              dojo.removeAttr(dojo.query("#dialog_" + _274 + "_title")[0], "id");
              dojo.attr(_27b, "id", "dialog_" + _274 + "_title");
            }
          }
          var _27d = dojo.query("#" + _274 + " .ibm-body .ibm-main .ibm-container .ibm-container-body")[0];
          if (_27d != undefined) {
            var _27e = dojo.attr(_27d, "id");
            if (_27e != null) {
              dojo.attr(dojo.query("#dialog_" + _274)[0], "aria-describedby", _27e);
            } else {
              dojo.attr(dojo.query("#dialog_" + _274)[0], "aria-describedby", "dialog_" + _274 + "_description");
              dojo.attr(_27d, "id", "dialog_" + _274 + "_description");
            }
          }
          var _27f = dojo.query("#dialog_" + _274 + " .dijitDialogCloseIcon")[0];
          if (_27f != undefined) {
            dojo.attr(_27f, "aria-label", "Close");
            dojo.attr(_27f, "tabindex", "0");
            _27f.focus();
          }
        });
      } catch (e) {}
      ibmweb.util.statsHelper({
        "ibmEV": "overlay",
        "ibmEvAction": "show",
        "ibmEvGroup": "Opening overlay",
        "ibmEvModule": _274
      });
    });
  };
  ibmweb.overlay.hide = function(_280) {
    dojo.removeClass(document.body, "stop-scrolling");
    if (document.body.removeEventListener) {
      document.body.removeEventListener("touchmove", handleTouch, false);
    } else {
      if (document.body.detachEvent) {
        document.body.detachEvent("on" + "touchmove", handleTouch);
      } else {
        element["on" + "touchmove"] = null;
      }
    }
    dojo.query(".oo_feedback_float").style("visibility", "visible");
    var _281 = dijit.byId("dialog_" + _280);
    ibmweb.util.statsHelper({
      "ibmEV": "overlay",
      "ibmEvAction": "close",
      "ibmEvGroup": "Closing overlay",
      "ibmEvModule": _280
    });
    if (_281.open === true) {
      _281.hide();
    }
    if (dojo.hasClass(dojo.byId(_280), "ibm-rebuild-after-close")) {
      var _282 = dojo.query(".ibm-body", dojo.byId(_280))[0],
        _283 = _282.innerHTML;
      _282.innerHTML = "";
      setTimeout(function() {
        _282.innerHTML = _283;
      }, 0);
    }
  };
  ibmweb.overlay.init = function() {};
}
if (!dojo._hasResource["ibmweb.storage"]) {
  dojo._hasResource["ibmweb.storage"] = true;
  dojo.provide("ibmweb.storage");
  ibmweb.storage = {
    getItem: function(key, _284) {
      if (!_284) {
        var _284 = null;
      }
      if (!this.isSupported()) {
        return null;
      }
      if (localStorage.getItem(key + "_expire") !== null) {
        var _285 = localStorage.getItem(key + "_expire");
        var _286 = new Date();
        var _286 = _286.getTime();
        if (_285 < _286) {
          this.removeItem(key);
          return _284;
        }
      }
      return localStorage.getItem(key);
    },
    setItem: function(key, _287, _288) {
      if (!this.isSupported()) {
        return false;
      }
      this.removeItem(key);
      if (_288) {
        var time = new Date();
        var _289 = _288 * 1000;
        _289 += time.getTime();
        localStorage.setItem(key + "_expire", _289);
      }
      localStorage.setItem(key, _287);
      return localStorage[key];
    },
    removeItem: function(key) {
      if (!this.isSupported()) {
        return false;
      }
      localStorage.removeItem(key);
      localStorage.removeItem(key + "_expire");
    },
    clear: function() {
      if (!this.isSupported()) {
        return false;
      }
      localStorage.clear();
    },
    isSupported: function() {
      try {
        return window.localStorage && typeof(window.localStorage) == "object";
      } catch (e) {
        return false;
      }
    }
  };
}
if (!dojo._hasResource["ibmweb.form"]) {
  dojo._hasResource["ibmweb.form"] = true;
  dojo.provide("ibmweb.form");
  ibmweb.form = {
    _loaded: false,
    _init: function() {
      if (ibmweb.form._loaded) {
        return;
      }
      ibmweb.form._loaded = true;
      dojo["require"]("ibmweb.ibm-form");
    },
    initElement: function(elem) {
      if (elem.nodeName == "SELECT") {
        this.initSelect(elem);
      } else {
        if (elem.nodeName == "INPUT") {
          if (elem.type.toLowerCase() == "checkbox") {
            this.initCheckbox(elem);
          } else {
            if (elem.type.toLowerCase() == "radio") {
              this.initRadio(elem);
            }
          }
        }
      }
    },
    initRadio: function(elem) {
      var p = {
        srcNodeRef: elem,
        checked: elem.checked,
        disabled: elem.disabled,
        name: elem.name,
        value: elem.value
      };
      if (elem.id) {
        p.id = elem.id;
      }
      if (elem.title) {
        p.title = elem.title;
      }
      new dijit.form.RadioButton(p);
    },
    initSelect: function(elem) {
      if (dojo.hasClass(elem, "ibm-filteringselect")) {
        return;
      }
      var p = {
        srcNodeRef: elem,
        disabled: elem.disabled,
        name: elem.name,
        maxHeight: 200
      };
      if (elem.id) {
        p.id = elem.id;
      }
      if (elem.title) {
        p.title = elem.title;
      }
      if (dojo.version && !(dojo.version.major === 1 && dojo.version.minor < 8)) {
        dojo.query("> option", elem).forEach(function(elem) {
          if (dojo.attr(elem, "value") === "") {
            dojo.attr(elem, "value", " ");
          }
        });
      }
      if (elem.multiple) {
        p.size = elem.size;
        new dijit.form.MultiSelect(p);
      } else {
        new dijit.form.Select(p);
      }
    },
    initCheckbox: function(elem) {
      var p = {
        srcNodeRef: elem,
        checked: elem.checked,
        disabled: elem.disabled,
        name: elem.name,
        value: elem.value
      };
      if (elem.id) {
        p.id = elem.id;
      }
      if (elem.title) {
        p.title = elem.title;
      }
      new dijit.form.CheckBox(p);
    }
  };
  dojo.addOnLoad(function() {
    if (dojo.version.major <= 1 && dojo.version.minor <= 3) {
      return;
    }
    dojo.query("input[disabled], select[disabled], textarea[disabled]").forEach(function(i) {
      if (i.id) {
        var _28a = dojo.query("label[for=\"" + i.id + "\"]");
        if (_28a.length == 1) {
          dojo.addClass(_28a[0], "ibm-label-disabled");
        }
      }
    });
    var _28b = dojo.query("select.ibm-styled,select.ibm-styled-select, input[type=checkbox].ibm-styled, input[type=radio].ibm-styled, form.ibm-styled-form input[type=radio], form.ibm-styled-form input[type=checkbox], form.ibm-styled-form select");
    if (_28b.length > 0) {
      ibmweb.form._init();
      dojo.addOnLoad(function() {
        for (var i = 0, j = _28b.length; i < j; i++) {
          ibmweb.form.initElement(_28b[i]);
        }
        dojo.publish("/ibmweb/form/initialized");
      });
    }
    dojo.addOnLoad(function() {
      dojo.query("form.ibm-jump-form").forEach(function(item) {
        dojo.query("input.ibm-btn-go", item).remove();
        var _28c = dojo.query(".dijitSelect", item);
        if (!_28c || _28c.length != 1) {
          return;
        }
        var id = _28c[0].id;
        _28c = dijit.byId(id);
        _28c.onChange = function(_28d) {
          if (_28d) {
            if (dojo.hasClass(item, "ibm-new-window")) {
              window.open(_28d);
            } else {
              location.href = _28d;
            }
          }
        };
        dojo.style(id, "width", "100%");
      });
    });
    var tmp = dojo.query(".ibm-date-picker");
    if (tmp.length > 0) {
      dojo["require"]("dijit.form.DateTextBox");
      dojo.addOnLoad(function() {
        tmp.forEach(function(elem) {
          var _28e = {
              srcNodeRef: elem,
              disabled: elem.disabled,
              name: elem.name,
              id: elem.id,
              hasDownArrow: false,
              style: "width: 196px"
            },
            lbl;
          if (elem.name) {
            lbl = dojo.query("label[for=\"" + elem.name + "\"] span.ibm-date-format");
            if (lbl.length != 0) {
              _28e.constraints = {
                datePattern: lbl[0].innerHTML
              };
              _28e.serialize = function(_28f) {
                return dojo.date.locale.format(_28f, {
                  datePattern: "yyyy-MM-dd",
                  selector: "date"
                });
              };
            }
          }
          if (elem.value) {
            try {
              var val = elem.value,
                _290 = Date.parseString(val);
              if (lbl.length != 0 && !isNaN(_290) && _290 instanceof Date) {
                val = dojo.date.locale.format(_290, {
                  datePattern: lbl[0].innerHTML,
                  selector: "date"
                });
              }
              _28e.value = dojo.date.locale.parse(val, {
                datePattern: (lbl.length != 0) ? lbl[0].innerHTML : "y-M-d",
                selector: "date"
              });
            } catch (e) {}
          }
          var dtb = new dijit.form.DateTextBox(_28e);
        });
      });
    }
    dojo.query(".ibm-required").forEach(function(item) {
      var _291 = item.parentNode;
      var _292 = dojo.attr(_291, "for");
      var _293 = dojo.query("input#" + _292)[0];
      if (_293) {
        if (dojo.attr(_293, "type") == "text") {
          dojo.attr(_293, "aria-required", "true");
        }
        if (dojo.attr(_293, "type") == "checkbox" || dojo.attr(_293, "type") == "radio") {
          var _294 = _293.parentNode;
          dojo.query("input", _294).forEach(function(i) {
            dojo.attr(i, "aria-required", "true");
          });
        }
      }
    });
  });
  ibmweb.form.initFilterSelect = (function() {
    var $fs = dojo.query("form.ibm-styled-form .ibm-filteringselect");
    if ($fs.length > 0) {
      ibmweb.form._init();
      dojo.ready(function() {
        $fs.forEach(function(elem, _295) {
          var p = {
            id: elem.id,
            srcNodeRef: elem,
            disabled: elem.disabled,
            name: elem.name,
            maxHeight: 200,
            invalidMessage: "",
            missingMessage: "",
            forceWidth: true
          };
          var _296 = p.srcNodeRef.parentElement;
          var _297 = new dijit.form.Select(p);
          var _298 = dojo.create("input", {
            type: "text",
            "class": "searchinput filtering_select_readonly",
            value: "Select or click to filter"
          });
          dojo.connect(_297, "openDropDown", function() {
            _299();
            var _29a = dojo.query("#" + _297.id + "_dropdown table tbody")[0];
            if (dojo.query("#" + _297.id + "_searchText").length == 0) {
              var _29b;
              dojo.connect(_298, "onkeyup", function(e) {
                _29b = 1;
                for (var i = 1; i < _29a.children.length; i++) {
                  _29a.children[i].style.display = "";
                  var _29c = RegExp("^" + e.currentTarget.value + "", "i");
                  match = _29a.children[i].children[1].innerHTML.match(_29c);
                  if (match && match.index === 0) {
                    _29a.children[i].style.display = "";
                  } else {
                    _29a.children[i].style.display = "none";
                    _29b++;
                  }
                }
                if (_29b == _29a.children.length) {
                  dojo.addClass(e.currentTarget, "filtering_select_erroricon");
                } else {
                  dojo.removeClass(e.currentTarget, "filtering_select_erroricon");
                }
                if (e.currentTarget.value.length > 0) {
                  dojo.query("#" + _297.id + "_dropdown div.dijitSelectMenu")[0].style.height = "auto";
                  dojo.query("#" + _297.id + "_dropdown div.dijitSelectMenu")[0].style.overflowY = "scroll";
                } else {
                  _2a2();
                }
              });
              dojo.connect(_298, "onfocus", function(e) {
                _298.value = "";
                dojo.removeClass(_298, "filtering_select_readonly");
                dojo.addClass(_298, "filtering_select_typing");
              });
              var td1 = dojo.create("td", {
                id: "test",
                "class": "dijitReset dijitMenuItemIconCell",
                role: "presentation"
              });
              var td2 = dojo.create("td", {
                id: _297.id + "_text",
                "class": "dijitReset dijitMenuItemLabel, ibm-filter-sel",
                role: "presentation",
                dojoattachpoint: "containerNode",
                innerHTML: "",
                style: "width:100%; display: block;",
                colspan: "2"
              });
              var td3 = dojo.create("td", {
                id: _297.id + "_accel",
                "class": "dijitReset dijitMenuItemAccelKey",
                style: "display: none"
              });
              var td4 = dojo.create("td", {
                "class": "dijitReset dijitMenuArrowCell",
                role: "presentation"
              });
              var _29d = dojo.create("img", {
                "class": "dijitIcon dijitMenuItemIcon",
                alt: "",
                src: "//1.w3.s81c.com/common/js/dojo/1.6/dojo/resources/blank.gif",
                role: "presentation"
              });
              var _29e = dojo.create("div", {
                style: "visibility: hidden"
              });
              var _29f = dojo.create("img", {
                "class": "dijitMenuExpand",
                alt: "",
                src: "//1.w3.s81c.com/common/js/dojo/1.6/dojo/resources/blank.gif"
              });
              var _2a0 = dojo.create("span", {
                "class": "dijitMenuExpandA11y",
                innerHTML: "+"
              });
              dojo.place(_29d, td1);
              dojo.place(_29e, td4);
              dojo.place(_29f, _29e);
              dojo.place(_2a0, _29e);
              var tr = dojo.create("tr", {
                id: _297.id + "_searchText",
                "class": "dijitReset dijitMenuItem",
                role: "listitem",
                "aria-labelledby": "test"
              });
              dojo.place(td1, tr);
              dojo.place(td2, tr);
              dojo.place(td3, tr);
              dojo.place(td4, tr);
              dojo.place(_298, td2);
              dojo.place(tr, _29a, "first");
            }
          });
          return _297;

          function _299() {
            _298.value = "Select or click to filter";
            if (dojo.hasClass(_298, "filtering_select_typing")) {
              dojo.removeClass(_298, "filtering_select_typing");
            }
            if (dojo.hasClass(_298, "filtering_select_erroricon")) {
              dojo.removeClass(_298, "filtering_select_erroricon");
            }
            if (!dojo.hasClass(_298, "filtering_select_readonly")) {
              dojo.addClass(_298, "filtering_select_readonly");
            }
            var _2a1 = dojo.query("#" + _297.id + "_dropdown table tbody")[0];
            for (var i = 1; i < _2a1.children.length; i++) {
              _2a1.children[i].style.display = "";
            }
            _2a2();
          };

          function _2a2() {
            dojo.query("#" + _297.id + "_dropdown div.dijitSelectMenu")[0].style.height = "206px";
            dojo.query("#" + _297.id + "_dropdown div.dijitSelectMenu")[0].style.overflowY = "scroll";
          };
        });
      });
    }
  });
  dojo.ready(function() {
    ibmweb.form.initFilterSelect();
  });
  Date.LZ = function(x) {
    return (x < 0 || x > 9 ? "" : "0") + x;
  };
  Date.monthNames = new Array("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
  Date.monthAbbreviations = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
  Date.dayNames = new Array("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
  Date.dayAbbreviations = new Array("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
  Date.preferAmericanFormat = true;
  if (!Date.prototype.getFullYear) {
    Date.prototype.getFullYear = function() {
      var yy = this.getYear();
      return (yy < 1900 ? yy + 1900 : yy);
    };
  }
  Date.parseString = function(val, _2a3) {
    if (typeof(_2a3) == "undefined" || _2a3 == null || _2a3 == "") {
      var _2a4 = new Array("y-M-d", "MMM d, y", "MMM d,y", "y-MMM-d", "d-MMM-y", "MMM d", "MMM-d", "d-MMM"),
        _2a5 = new Array("M/d/y", "M-d-y", "M.d.y", "M/d", "M-d"),
        _2a6 = new Array("d/M/y", "d-M-y", "d.M.y", "d/M", "d-M"),
        _2a7 = new Array(_2a4, Date.preferAmericanFormat ? _2a5 : _2a6, Date.preferAmericanFormat ? _2a6 : _2a5);
      for (var i = 0; i < _2a7.length; i++) {
        var l = _2a7[i];
        for (var j = 0; j < l.length; j++) {
          var d = Date.parseString(val, l[j]);
          if (d != null) {
            return d;
          }
        }
      }
      return null;
    }
    this.isInteger = function(val) {
      for (var i = 0; i < val.length; i++) {
        if ("1234567890".indexOf(val.charAt(i)) == -1) {
          return false;
        }
      }
      return true;
    };
    this.getInt = function(str, i, _2a8, _2a9) {
      for (var x = _2a9; x >= _2a8; x--) {
        var _2aa = str.substring(i, i + x);
        if (_2aa.length < _2a8) {
          return null;
        }
        if (this.isInteger(_2aa)) {
          return _2aa;
        }
      }
      return null;
    };
    val = val + "";
    _2a3 = _2a3 + "";
    var _2ab = 0,
      _2ac = 0,
      c = "",
      _2ad = "",
      _2ae = "",
      x, y, year = new Date().getFullYear(),
      _2af = 1,
      date = 1,
      hh = 0,
      mm = 0,
      ss = 0,
      ampm = "";
    while (_2ac < _2a3.length) {
      c = _2a3.charAt(_2ac);
      _2ad = "";
      while ((_2a3.charAt(_2ac) == c) && (_2ac < _2a3.length)) {
        _2ad += _2a3.charAt(_2ac++);
      }
      switch (_2ad) {
        case "yyyy":
        case "yy":
        case "y":
          if (_2ad == "yyyy") {
            x = 4;
            y = 4;
          }
          if (_2ad == "yy") {
            x = 2;
            y = 2;
          }
          if (_2ad == "y") {
            x = 2;
            y = 4;
          }
          year = this.getInt(val, _2ab, x, y);
          if (year == null) {
            return null;
          }
          _2ab += year.length;
          if (year.length == 2) {
            if (year > 70) {
              year = 1900 + (year - 0);
            } else {
              year = 2000 + (year - 0);
            }
          }
          break;
        case "MMM":
        case "NNN":
          _2af = 0;
          var _2b0 = (_2ad == "MMM" ? (Date.monthNames.concat(Date.monthAbbreviations)) : Date.monthAbbreviations);
          for (var i = 0; i < _2b0.length; i++) {
            var _2b1 = _2b0[i];
            if (val.substring(_2ab, _2ab + _2b1.length).toLowerCase() == _2b1.toLowerCase()) {
              _2af = (i % 12) + 1;
              _2ab += _2b1.length;
              break;
            }
          }
          if ((_2af < 1) || (_2af > 12)) {
            return null;
          }
          break;
        case "EE":
        case "E":
          var _2b0 = (_2ad == "EE" ? Date.dayNames : Date.dayAbbreviations);
          for (var i = 0; i < _2b0.length; i++) {
            var _2b2 = _2b0[i];
            if (val.substring(_2ab, _2ab + _2b2.length).toLowerCase() == _2b2.toLowerCase()) {
              _2ab += _2b2.length;
              break;
            }
          }
          break;
        case "MM":
        case "M":
          _2af = this.getInt(val, _2ab, _2ad.length, 2);
          if (_2af == null || (_2af < 1) || (_2af > 12)) {
            return null;
          }
          _2ab += _2af.length;
          break;
        case "dd":
        case "d":
          date = this.getInt(val, _2ab, _2ad.length, 2);
          if (date == null || (date < 1) || (date > 31)) {
            return null;
          }
          _2ab += date.length;
          break;
        case "hh":
        case "h":
          hh = this.getInt(val, _2ab, _2ad.length, 2);
          if (hh == null || (hh < 1) || (hh > 12)) {
            return null;
          }
          _2ab += hh.length;
          break;
        case "HH":
        case "H":
          hh = this.getInt(val, _2ab, _2ad.length, 2);
          if (hh == null || (hh < 0) || (hh > 23)) {
            return null;
          }
          _2ab += hh.length;
          break;
        case "KK":
        case "K":
          hh = this.getInt(val, _2ab, _2ad.length, 2);
          if (hh == null || (hh < 0) || (hh > 11)) {
            return null;
          }
          _2ab += hh.length;
          hh++;
          break;
        case "kk":
        case "k":
          hh = this.getInt(val, _2ab, _2ad.length, 2);
          if (hh == null || (hh < 1) || (hh > 24)) {
            return null;
          }
          _2ab += hh.length;
          hh--;
          break;
        case "mm":
        case "m":
          mm = this.getInt(val, _2ab, _2ad.length, 2);
          if (mm == null || (mm < 0) || (mm > 59)) {
            return null;
          }
          _2ab += mm.length;
          break;
        case "ss":
        case "s":
          ss = this.getInt(val, _2ab, _2ad.length, 2);
          if (ss == null || (ss < 0) || (ss > 59)) {
            return null;
          }
          _2ab += ss.length;
          break;
        case "a":
          if (val.substring(_2ab, _2ab + 2).toLowerCase() == "am") {
            ampm = "AM";
          } else {
            if (val.substring(_2ab, _2ab + 2).toLowerCase() == "pm") {
              ampm = "PM";
            } else {
              return null;
            }
          }
          _2ab += 2;
          break;
        default:
          if (val.substring(_2ab, _2ab + _2ad.length) != _2ad) {
            return null;
          } else {
            _2ab += _2ad.length;
          }
      }
    }
    if (_2ab != val.length) {
      return null;
    }
    if (_2af == 2) {
      if (((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0)) {
        if (date > 29) {
          return null;
        }
      } else {
        if (date > 28) {
          return null;
        }
      }
    }
    if ((_2af == 4) || (_2af == 6) || (_2af == 9) || (_2af == 11)) {
      if (date > 30) {
        return null;
      }
    }
    if (hh < 12 && ampm == "PM") {
      hh = hh - 0 + 12;
    } else {
      if (hh > 11 && ampm == "AM") {
        hh -= 12;
      }
    }
    return new Date(year, _2af - 1, date, hh, mm, ss);
  };
  Date.isValid = function(val, _2b3) {
    return (Date.parseString(val, _2b3) != null);
  };
  Date.prototype.isBefore = function(_2b4) {
    if (_2b4 == null) {
      return false;
    }
    return (this.getTime() < _2b4.getTime());
  };
  Date.prototype.isAfter = function(_2b5) {
    if (_2b5 == null) {
      return false;
    }
    return (this.getTime() > _2b5.getTime());
  };
  Date.prototype.equals = function(_2b6) {
    if (_2b6 == null) {
      return false;
    }
    return (this.getTime() == _2b6.getTime());
  };
  Date.prototype.equalsIgnoreTime = function(_2b7) {
    if (_2b7 == null) {
      return false;
    }
    var d1 = new Date(this.getTime()).clearTime(),
      d2 = new Date(_2b7.getTime()).clearTime();
    return (d1.getTime() == d2.getTime());
  };
  Date.prototype.format = function(_2b8) {
    _2b8 = _2b8 + "";
    var _2b9 = "",
      _2ba = 0,
      c = "",
      _2bb = "",
      y = this.getYear() + "",
      M = this.getMonth() + 1,
      d = this.getDate(),
      E = this.getDay(),
      H = this.getHours(),
      m = this.getMinutes(),
      s = this.getSeconds(),
      yyyy, yy, MMM, MM, dd, hh, h, mm, ss, ampm, HH, H, KK, K, kk, k, _2bc = new Object();
    if (y.length < 4) {
      y = "" + (+y + 1900);
    }
    _2bc["y"] = "" + y;
    _2bc["yyyy"] = y;
    _2bc["yy"] = y.substring(2, 4);
    _2bc["M"] = M;
    _2bc["MM"] = Date.LZ(M);
    _2bc["MMM"] = Date.monthNames[M - 1];
    _2bc["NNN"] = Date.monthAbbreviations[M - 1];
    _2bc["d"] = d;
    _2bc["dd"] = Date.LZ(d);
    _2bc["E"] = Date.dayAbbreviations[E];
    _2bc["EE"] = Date.dayNames[E];
    _2bc["H"] = H;
    _2bc["HH"] = Date.LZ(H);
    if (H == 0) {
      _2bc["h"] = 12;
    } else {
      if (H > 12) {
        _2bc["h"] = H - 12;
      } else {
        _2bc["h"] = H;
      }
    }
    _2bc["hh"] = Date.LZ(_2bc["h"]);
    _2bc["K"] = _2bc["h"] - 1;
    _2bc["k"] = _2bc["H"] + 1;
    _2bc["KK"] = Date.LZ(_2bc["K"]);
    _2bc["kk"] = Date.LZ(_2bc["k"]);
    if (H > 11) {
      _2bc["a"] = "PM";
    } else {
      _2bc["a"] = "AM";
    }
    _2bc["m"] = m;
    _2bc["mm"] = Date.LZ(m);
    _2bc["s"] = s;
    _2bc["ss"] = Date.LZ(s);
    while (_2ba < _2b8.length) {
      c = _2b8.charAt(_2ba);
      _2bb = "";
      while ((_2b8.charAt(_2ba) == c) && (_2ba < _2b8.length)) {
        _2bb += _2b8.charAt(_2ba++);
      }
      if (typeof(_2bc[_2bb]) != "undefined") {
        _2b9 = _2b9 + _2bc[_2bb];
      } else {
        _2b9 = _2b9 + _2bb;
      }
    }
    return _2b9;
  };
  Date.prototype.getDayName = function() {
    return Date.dayNames[this.getDay()];
  };
  Date.prototype.getDayAbbreviation = function() {
    return Date.dayAbbreviations[this.getDay()];
  };
  Date.prototype.getMonthName = function() {
    return Date.monthNames[this.getMonth()];
  };
  Date.prototype.getMonthAbbreviation = function() {
    return Date.monthAbbreviations[this.getMonth()];
  };
  Date.prototype.clearTime = function() {
    this.setHours(0);
    this.setMinutes(0);
    this.setSeconds(0);
    this.setMilliseconds(0);
    return this;
  };
  Date.prototype.add = function(_2bd, _2be) {
    if (typeof(_2bd) == "undefined" || _2bd == null || typeof(_2be) == "undefined" || _2be == null) {
      return this;
    }
    _2be = +_2be;
    switch (_2bd) {
      case "y":
        this.setFullYear(this.getFullYear() + _2be);
        break;
      case "M":
        this.setMonth(this.getMonth() + _2be);
        break;
      case "d":
        this.setDate(this.getDate() + _2be);
        break;
      case "w":
        var step = (_2be > 0) ? 1 : -1;
        while (_2be != 0) {
          this.add("d", step);
          while (this.getDay() == 0 || this.getDay() == 6) {
            this.add("d", step);
          }
          _2be -= step;
        }
        break;
      case "h":
        this.setHours(this.getHours() + _2be);
        break;
      case "m":
        this.setMinutes(this.getMinutes() + _2be);
        break;
      case "s":
        this.setSeconds(this.getSeconds() + _2be);
        break;
    }
    return this;
  };
}
if (!dojo._hasResource["ibmweb.stepindicator"]) {
  dojo._hasResource["ibmweb.stepindicator"] = true;
  dojo.provide("ibmweb.stepindicator");
  ibmweb.stepindicator = {
    indicators: {},
    id_inc: 0,
    classnames: {
      indicator: "ibm-step-indicator",
      container: "ibm-step-container",
      past_step: "ibm-active-completed",
      present_step: "ibm-active-current",
      future_step: "ibm-disabled",
      inactive_content: "ibm-hidden-content",
      disable_previous: "ibm-disable-previous"
    },
    init: function(_2bf, _2c0) {
      _2bf = _2bf || dojo.query("body")[0];
      dojo.query("div." + this.classnames.indicator, _2bf).forEach(function(node, i) {
        if (!new ibmweb.stepindicator.StepIndicator(node, _2c0)) {}
      });
    },
    StepIndicator: function(node, _2c1) {
      var that = this;
      if (typeof(node) == "string") {
        node = dojo.byId(node);
      }
      if (!node) {
        throw "Cannot build StepIndicator, node does not exist";
      }
      if (dojo.attr(node, "data-step-indicator-init")) {
        return false;
      }
      dojo.attr(node, "data-step-indicator-init", true);
      var me = this,
        id = dojo.attr(node, "id"),
        _2c2 = {
          future_clickable: false,
          disable_previous: ~node.className.indexOf(ibmweb.stepindicator.classnames.disable_previous) ? true : false,
          content: dojo.NodeList(node).next()[0],
          focus_index: -1
        };
      if (id) {
        indicators[id] = me;
      }
      dojo.query(".ibm-hide-if-no-js", _2c2.content).forEach(function(_2c3) {
        dojo.removeClass(_2c3, "ibm-hide-if-no-js");
      });
      _2c1 = _2c1 || {};
      for (var key in _2c1) {
        _2c2[key] = _2c1[key];
      }
      node.setAttribute("role", "navigation");
      node.setAttribute("aria-level", "2");
      node.setAttribute("aria-label", "Step Indicator");
      var _2c4 = dojo.query("h2.ibm-access", node)[0];
      if (_2c4) {
        node.removeChild(_2c4);
      }
      var _2c5 = function() {
        return dojo.query("li", node);
      };
      var _2c6 = function() {
        return dojo.query("." + ibmweb.stepindicator.classnames.container, _2c2.content);
      };
      this.currentStep = function() {
        var cs = -1;
        _2c5().forEach(function(node, i) {
          if (dojo.query("." + ibmweb.stepindicator.classnames.present_step, node).length > 0) {
            cs = i;
          }
        });
        return cs;
      };
      this.totalSteps = function() {
        return _2c5().length;
      };
      this.isOnFirst = function() {
        return me.currentStep() === 0;
      };
      this.isOnLast = function() {
        return me.currentStep() === me.totalSteps() - 1;
      };
      this.goToStep = function(_2c7) {
        var $a;
        _2c5().forEach(function(_2c8, i) {
          $a = dojo.query("a", _2c8)[0];
          if (_2c2.disable_previous) {
            dojo[(i !== _2c7) ? "addClass" : "removeClass"]($a, ibmweb.stepindicator.classnames.future_step);
            dojo[(i === _2c7) ? "addClass" : "removeClass"]($a, ibmweb.stepindicator.classnames.present_step);
          } else {
            dojo[(i < _2c7) ? "addClass" : "removeClass"]($a, ibmweb.stepindicator.classnames.past_step);
            dojo[(i === _2c7) ? "addClass" : "removeClass"]($a, ibmweb.stepindicator.classnames.present_step);
            dojo[(i > _2c7) ? "addClass" : "removeClass"]($a, ibmweb.stepindicator.classnames.future_step);
          }
          if (i === _2c7) {
            $a.setAttribute("aria-selected", true);
            $a.tabIndex = 0;
          } else {
            $a.setAttribute("aria-selected", false);
            $a.tabIndex = -1;
          }
        });
        _2c6().forEach(function(_2c9, i) {
          dojo.style(_2c9, "display", (i === _2c7) ? "block" : "none");
          dojo.attr(_2c9, "role", (i === _2c7) ? "tabpanel" : "");
        });
        _2c2.focus_index = _2c7;
      };
      _2c5().forEach(function(_2ca, i) {
        $a = dojo.query("a", _2ca)[0];
        dojo.connect($a, "onclick", function(e) {
          var _2cb = me.currentStep() < i;
          if ((!_2cb || (_2cb && _2c2.future_clickable)) && !_2c2.disable_previous) {
            me.goToStep(i);
          }
          e.preventDefault();
        });
        dojo.connect($a, "onkeypress", function(e) {
          var key = e.keyCode || e.charCode;
          if (key === dojo.keys.RIGHT_ARROW || key === dojo.keys.LEFT_ARROW) {
            _2c2.focus_index = (_2c2.focus_index === -1) ? me.currentStep() : _2c2.focus_index;
            _2c2.focus_index += (key === dojo.keys.RIGHT_ARROW) ? 1 : -1;
            if (_2c2.focus_index === me.totalSteps()) {
              _2c2.focus_index = me.totalSteps() - 1;
            } else {
              if (_2c2.focus_index < 0) {
                _2c2.focus_index = 0;
              }
            }
            _2c5().forEach(function(_2cc, i) {
              $a_focus = dojo.query("a", _2cc)[0];
              if (_2c2.focus_index === i) {
                dijit.focus($a_focus);
              }
            });
          } else {
            if (key === dojo.keys.SPACE && !_2c2.disable_previous) {
              me.goToStep(_2c2.focus_index);
              e.preventDefault();
            }
          }
        });
      });
      this.next = function() {
        if (!me.isOnLast()) {
          me.goToStep(me.currentStep() + 1);
        }
      };
      this.prev = function(_2cd) {
        if (!me.isOnFirst()) {
          me.goToStep(me.currentStep() - 1);
        }
      };
      _2c6().forEach(function(_2ce, i) {
        var _2cf = dojo.query("[data-step=next]", _2ce);
        _2cf.forEach(function(_2d0) {
          dojo.attr(_2d0, "role", "button");
          dojo.connect(_2d0, "onclick", function() {
            me.next();
          });
        });
        var _2d1 = dojo.query("[data-step=prev]", _2ce);
        _2d1.forEach(function(_2d2) {
          dojo.attr(_2d2, "role", "button");
          dojo.connect(_2d2, "onclick", function() {
            me.prev();
          });
        });
      });
      var cs = me.currentStep();
      if (cs == -1) {
        cs = 0;
      }
      me.goToStep(cs);
    },
    byId: function(id) {
      return this.indicators[id] || null;
    }
  };
}
if (!dojo._hasResource["ibmweb.dynnav._module"]) {
  dojo._hasResource["ibmweb.dynnav._module"] = true;
  dojo.provide("ibmweb.dynnav._module");
  dojo.declare("ibmweb.dynnav._module", null, {
    isA_Module: true,
    init: function() {
      return false;
    },
    onLoad: function() {},
    onData: function(data) {},
    onError: function() {}
  });
}
if (!dojo._hasResource["ibmweb.dynnav.mediacontroller"]) {
  dojo._hasResource["ibmweb.dynnav.mediacontroller"] = true;
  dojo.provide("ibmweb.dynnav.mediacontroller");
  dojo.declare("ibmweb.dynnav.mediacontroller", [ibmweb.dynnav._module], {
    _widgets: [],
    init: function() {
      return ibmweb.dynnav.isServiceEnabled("video");
    },
    createWidgets: function(_2d3) {
      if (!_2d3) {
        _2d3 = dojo.query(".ibm-media");
      }
      var self = this;
      _2d3.forEach(function(item) {
        if (dojo.hasClass(item, "ibm-media-processed")) {
          return;
        }
        dojo.addClass(item, "ibm-media-processed");
        if (item.tagName == "A" || item.tagName == "AREA") {
          dojo.connect(item, "onclick", function(_2d4) {
            self._onLinkClick(_2d4, this);
          });
          return;
        }
        var link = dojo.query(".ibm-rss-link", item);
        if (link.length == 0) {
          return;
        }
        link = link[0];
        var _2d5 = dojo.queryToObject(link.rel);
        var _2d6, data = null;
        var _2d7 = dojo.query("img", link);
        var _2d8;
        if (_2d7.length != 0) {
          if (_2d5.format == "ribbon") {
            htmlBackground_arr = [];
            _2d7.forEach(function(i) {
              var src = dojo.attr(i, "src");
              htmlBackground_arr.push(src);
              dojo.destroy(i);
            });
            _2d7 = htmlBackground_arr;
          } else {
            var src = dojo.attr(_2d7[0], "src");
            _2d8 = dojo.attr(_2d7[0], "alt");
            _2d7.forEach(dojo.destroy);
            _2d7 = src;
          }
        } else {
          _2d7 = null;
        }
        var _2d9 = {
          container: item,
          url: link.href,
          options: dojo.queryToObject(link.rel),
          title: link.innerHTML,
          htmlBackground: _2d7,
          htmlBackgroundAlt: _2d8
        };
        if (_2d5.loadVideoUrl) {
          data = [{
            isVideo: true,
            url: {
              mp4: link.href
            }
          }];
          if (_2d5.height) {
            data[0].height = _2d5.height;
          }
          if (_2d5.width) {
            data[0].width = _2d5.width;
          }
        }
        switch (_2d5.format) {
          case "video_small":
            dojo.mixin(_2d9, {
              maxWidth: 300
            });
            _2d6 = "videowidget";
            data = [{
              height: "225",
              width: "300",
              isVideo: true,
              thumbnails: {},
              url: {
                mp4: link.href
              }
            }];
            break;
          case "inline_small":
            var _2da = 300;
            try {
              if (link.parentNode) {
                var _2db = link.parentNode;
                _2da = dojo.coords(_2db).w;
              }
            } catch (e) {}
            dojo.mixin(_2d9, {
              maxWidth: _2da,
              format: "inline_small"
            });
            _2d9.options.no_hide = true;
            _2d6 = "videowidget";
            break;
          case "inline_large":
            dojo.mixin(_2d9, {
              maxWidth: 940,
              maxHeight: 332,
              format: "inline_large"
            });
            _2d6 = "largewidget";
            break;
          case "inline_medium":
            dojo.mixin(_2d9, {
              maxWidth: 620,
              maxHeight: 332,
              format: "inline_medium"
            });
            _2d6 = "mediumwidget";
            break;
          case "playlist":
            dojo.mixin(_2d9, {
              maxWidth: 940,
              maxHeight: 332,
              format: "playlist"
            });
            _2d6 = "playlistwidget";
            break;
          case "inline_thumbnail":
            dojo.mixin(_2d9, {
              maxWidth: 940,
              maxHeight: 332,
              format: "inline_thumbnail"
            });
            _2d6 = "thumbnailwidget";
            break;
          case "ribbon":
          default:
            dojo.mixin(_2d9, {
              maxWidth: 940,
              maxHeight: 332,
              format: "ribbon"
            });
            _2d6 = "ribbonwidget";
            break;
        }
        _2d6 = new ext.media[_2d6](_2d9);
        dojo.empty(item);
        _2d6.placeAt(item);
        _2d6.startup(data);
        self._widgets.push(_2d6);
        dojo.publish("/ibmweb/dynnav/mediacontroller/created", _2d6);
      });
    },
    onData: function() {
      var _2dc = dojo.query(".ibm-media");
      var self = this;
      if (_2dc.length > 0) {
        dojo["require"]("ext.media._base");
        dojo.addOnLoad(dojo.hitch(this, function() {
          this.createWidgets();
        }));
      }
    },
    onError: function() {
      for (var i = 0, j = this._widgets.length; i < j; i++) {
        this._widgets[i].removeWaitingImage();
      }
    },
    useNewYTPlayer: function() {
      var _2dd = dojo.query("meta[name=__RMSBETA]");
      var _2de = false;
      if (_2dd && _2dd.length > 0 && dojo.attr(_2dd[0], "value") == "newYT") {
        _2de = true;
      }
      var _2df = /[&?]rmsbeta=newyt/.test(document.location.search);
      if (dojo.cookie("__RMSBETA") == "newYT" || _2de || _2df) {
        if (dojo.isIE) {
          return dojo.isIE > 8;
        }
        return true;
      }
      return false;
    },
    _onLinkClick: function(_2e0, link) {
      var _2e1 = {};
      var _2e2 = link.href && link.href.indexOf("youtube.com") > -1;
      if (link.rel) {
        _2e1 = dojo.queryToObject(link.rel);
      }
      if (ibmweb.info.mobileDevice && _2e2 && !(ibmweb.config.html5video && ibmweb.config.html5video.useYoutubeAppOnMobile == false)) {
        return;
      }
      if (!!_2e1.format && _2e1.format == "overlay") {
        dojo.stopEvent(_2e0);
        var _2e3 = {
          url: link.href,
          options: _2e1
        };
        var _2e4 = new ext.media.overlaywidget(_2e3);
        _2e4.startup();
        return;
      }
      if (!_2e2 && (!_2e1.height || !_2e1.width)) {
        var _2e5 = link.href.split(".").pop();
        var _2e6 = false;
        var _2e7 = document.createElement("video");
        var _2e8 = document.createElement("source");
        _2e8.type = "video/" + _2e5;
        _2e8.src = link.href;
        switch (_2e5) {
          case "mp4":
            if (ibmweb.info.html5VideoSupport.h264) {
              _2e6 = true;
            }
            break;
          case "ogg":
            if (ibmweb.info.html5VideoSupport.ogg) {
              _2e6 = true;
            }
            break;
          case "webm":
            if (ibmweb.info.html5VideoSupport.webm) {
              _2e6 = true;
            }
            break;
        }
        if (_2e6) {
          _2e7.appendChild(_2e8);
          _2e7.preload = "metadata";

          function _2e9(_2ea, _2eb) {
            var _2ec = _2ea / _2eb;
            return (Math.abs(_2ec - 4 / 3) < Math.abs(_2ec - 16 / 9)) ? "4:3" : "16:9";
          };
          _2e7.onloadedmetadata = function() {
            var _2ed = _2e9(_2e7.videoWidth, _2e7.videoHeight);
            switch (_2ed) {
              case "4:3":
                _2e1.width = "640";
                _2e1.height = "480";
                break;
              case "16:9":
                _2e1.width = "960";
                _2e1.height = "540";
                break;
            }
            _2e8.remove();
            _2e7.remove();
            dojo.stopEvent(_2e0);
            var _2ee = new ext.media.overlaywidget();
            _2ee.url = link.href;
            _2ee.options = _2e1;
            var obj = {
              isVideo: true,
              url: {
                mp4: link.href
              }
            };
            _2ee.startup([dojo.mixin({}, _2e1, obj)], true);
          };
          _2e0.preventDefault();
          return;
        } else {
          _2e8.remove();
          _2e7.remove();
          return;
        }
      }
      dojo.stopEvent(_2e0);
      var _2e4 = new ext.media.overlaywidget();
      _2e4.url = link.href;
      _2e4.options = _2e1;
      var obj = {
        isVideo: true,
        url: {
          mp4: link.href
        }
      };
      if (_2e2) {
        obj.type = "youtube";
        obj.url = {
          youtube: link.href
        };
        if (!_2e1.height) {
          obj.height = 360;
        }
        if (!_2e1.width) {
          obj.width = 640;
        }
      }
      _2e4.startup([dojo.mixin({}, _2e1, obj)], true);
    }
  });
}
if (!dojo._hasResource["ibmweb.external.expertise"]) {
  dojo._hasResource["ibmweb.external.expertise"] = true;
  dojo.provide("ibmweb.external.expertise");
  dojo.declare("ibmweb.external.expertise", ibmweb.dynnav._module, {
    init: function() {
      return ibmweb.dynnav.isServiceEnabled("expertise");
    },
    onData: function(data) {
      if (ibmweb.config.config == "www") {
        ibmweb.config.set({
          expv17css: "//1.www.s81c.com/common/v17/css/external/expertise.css"
        });
      } else {
        ibmweb.config.set({
          expv17css: "//1.w3.s81c.com/common/v17/css/external/expertise.css"
        });
      }
      var RE = /\,{1,}/g;
      if (ibmweb.config.siteid == "www" || ibmweb.config.siteid == "w3") {
        if (ibmweb.config.expertise.template == "col-5-1") {
          dojo["require"]("ext.expertise.AbstractExpertiseV17Widget");
          dojo["require"]("ext.expertise.Five1V17Widget");
          ibmweb.queue.push(function() {
            return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.Five1V17Widget) == "function";
          }, function() {
            if (!ibmweb.config.expertise.debug) {
              dojo.doc.getElementsByTagName("head")[0].appendChild(dojo.create("link", {
                type: "text/css",
                rel: "stylesheet",
                href: ibmweb.config.expv17css
              }));
            }
            ibmweb.data.require("expertlocator", function(_2ef) {
              if (RE.test(ibmweb.config.expertise.placeat)) {
                if (ibmweb.config.expertise.expertid && ibmweb.config.expertise.displaytype == "one") {
                  for (var i = 0; i < ibmweb.config.expertise.placeat.length; i++) {
                    var _2f0 = new ext.expertise.Five1V17Widget();
                    _2f0.startup(i);
                  }
                } else {
                  if (ibmweb.config.expertise.listid && ibmweb.config.expertise.displaytype == "multi") {
                    for (var i = 0; i < ibmweb.config.expertise.placeat.length; i++) {
                      var _2f0 = new ext.expertise.Five1V17Widget();
                      _2f0.startup(i);
                    }
                  } else {
                    return;
                  }
                }
              } else {
                var _2f0 = new ext.expertise.Five1V17Widget();
                _2f0.startup();
              }
            });
          });
        } else {
          if (ibmweb.config.expertise.template == "col-6-1") {
            dojo["require"]("ext.expertise.AbstractExpertiseV17Widget");
            dojo["require"]("ext.expertise.Six1V17Widget");
            ibmweb.queue.push(function() {
              return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.Six1V17Widget) == "function";
            }, function() {
              if (!ibmweb.config.expertise.debug) {
                dojo.doc.getElementsByTagName("head")[0].appendChild(dojo.create("link", {
                  type: "text/css",
                  rel: "stylesheet",
                  href: ibmweb.config.expv17css
                }));
              }
              ibmweb.data.require("expertlocator", function(_2f1) {
                if (RE.test(ibmweb.config.expertise.placeat)) {
                  if (ibmweb.config.expertise.expertid && ibmweb.config.expertise.displaytype == "one") {
                    for (var i = 0; i < ibmweb.config.expertise.placeat.length; i++) {
                      var _2f2 = new ext.expertise.Six1V17Widget();
                      _2f2.startup(i);
                    }
                  } else {
                    if (ibmweb.config.expertise.listid && ibmweb.config.expertise.displaytype == "multi") {
                      for (var i = 0; i < ibmweb.config.expertise.placeat.length; i++) {
                        var _2f2 = new ext.expertise.Six1V17Widget();
                        _2f2.startup(i);
                      }
                    } else {
                      return;
                    }
                  }
                } else {
                  var _2f2 = new ext.expertise.Six1V17Widget();
                  _2f2.startup();
                }
              });
            });
          } else {
            if (ibmweb.config.expertise.template == "col-6-2") {
              dojo["require"]("ext.expertise.AbstractExpertiseV17Widget");
              dojo["require"]("ext.expertise.Six2V17Widget");
              ibmweb.queue.push(function() {
                return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.Six2V17Widget) == "function";
              }, function() {
                if (!ibmweb.config.expertise.debug) {
                  dojo.doc.getElementsByTagName("head")[0].appendChild(dojo.create("link", {
                    type: "text/css",
                    rel: "stylesheet",
                    href: ibmweb.config.expv17css
                  }));
                }
                ibmweb.data.require("expertlocator", function(_2f3) {
                  if (RE.test(ibmweb.config.expertise.placeat)) {
                    if (ibmweb.config.expertise.expertid && ibmweb.config.expertise.displaytype == "one") {
                      for (var i = 0; i < ibmweb.config.expertise.placeat.length; i++) {
                        var _2f4 = new ext.expertise.Six2V17Widget();
                        _2f4.startup(i);
                      }
                    } else {
                      if (ibmweb.config.expertise.listid && ibmweb.config.expertise.displaytype == "multi") {
                        for (var i = 0; i < ibmweb.config.expertise.placeat.length; i++) {
                          var _2f4 = new ext.expertise.Six2V17Widget();
                          _2f4.startup(i);
                        }
                      } else {
                        return;
                      }
                    }
                  } else {
                    var _2f4 = new ext.expertise.Six2V17Widget();
                    _2f4.startup();
                  }
                });
              });
            } else {
              if (ibmweb.config.config == "www") {
                dojo["require"]("ext.expertise.AbstractExpertiseWidget");
                dojo["require"]("ext.expertise.WWWExpertiseWidget");
                ibmweb.queue.push(function() {
                  return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.WWWExpertiseWidget) == "function";
                }, function() {
                  var _2f5 = new ext.expertise.WWWExpertiseWidget();
                  _2f5.startup();
                });
              } else {
                if (ibmweb.config.config == "w3") {
                  dojo["require"]("ext.expertise.AbstractExpertiseWidget");
                  dojo["require"]("ext.expertise.W3ExpertiseWidget");
                  ibmweb.queue.push(function() {
                    return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.W3ExpertiseWidget) == "function";
                  }, function() {
                    var _2f6 = new ext.expertise.W3ExpertiseWidget();
                    _2f6.startup();
                  });
                }
              }
            }
          }
        }
      } else {
        if (ibmweb.config.siteid == "smarterplanet") {
          dojo["require"]("ext.expertise.AbstractExpertiseWidget");
          dojo["require"]("ext.expertise.ExternalExpertiseWidget");
          ibmweb.queue.push(function() {
            return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.ExternalExpertiseWidget) == "function";
          }, function() {
            var _2f7 = new ext.expertise.ExternalExpertiseWidget();
            _2f7.startup();
          });
        } else {
          if (ibmweb.config.siteid == "ibm100") {
            dojo["require"]("ext.expertise.AbstractExpertiseWidget");
            dojo["require"]("ext.expertise.IBM100ExpertiseWidget");
            ibmweb.queue.push(function() {
              return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.IBM100ExpertiseWidget) == "function";
            }, function() {
              var _2f8 = new ext.expertise.IBM100ExpertiseWidget();
              _2f8.startup();
            });
          } else {
            if (ibmweb.config.config == "www") {
              dojo["require"]("ext.expertise.AbstractExpertiseWidget");
              dojo["require"]("ext.expertise.WWWExpertiseWidget");
              ibmweb.queue.push(function() {
                return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.WWWExpertiseWidget) == "function";
              }, function() {
                var _2f9 = new ext.expertise.WWWExpertiseWidget();
                _2f9.startup();
              });
            } else {
              if (ibmweb.config.config == "w3") {
                dojo["require"]("ext.expertise.AbstractExpertiseWidget");
                dojo["require"]("ext.expertise.W3ExpertiseWidget");
                ibmweb.queue.push(function() {
                  return typeof(ext) == "object" && typeof(ext.expertise) == "object" && typeof(ext.expertise.W3ExpertiseWidget) == "function";
                }, function() {
                  var _2fa = new ext.expertise.W3ExpertiseWidget();
                  _2fa.startup();
                });
              }
            }
          }
        }
      }
    }
  });
}
if (!dojo._hasResource["ibmweb.external.noticechoice"]) {
  dojo._hasResource["ibmweb.external.noticechoice"] = true;
  dojo.provide("ibmweb.external.noticechoice");
  dojo.declare("ibmweb.external.noticechoice", [ibmweb.dynnav._module], {
    init: function() {
      if (!!ibmweb.meta.nc_pagedesc) {
        dojo.ready(function() {
          var _2fb = dojo.queryToObject(window.location.search.substr(1)).v17ConfigNoticeChoiceLocation,
            _2fc;
          if (!_2fb && window.v17ConfigNoticeChoiceLocation) {
            _2fb = window.v17ConfigNoticeChoiceLocation;
          }
          if (_2fb === "dev") {
            _2fc = "//webdev.webmaster.ibm.com/common/js/noticechoice/nc.js";
          } else {
            if (_2fb === "wwwpreview") {
              _2fc = "//1.wwwpreview.s81c.com/common/js/noticechoice/nc.js";
            } else {
              if (_2fb === "production" || !_2fb) {
                _2fc = "//1.www.s81c.com/common/js/noticechoice/nc.js";
              } else {
                _2fc = _2fb;
              }
            }
          }
          if (!window.ext) {
            window.ext = {};
          }
          if (!ext.noticechoice) {
            ext.noticechoice = {};
          }
          if (!ext.noticechoice.nc) {
            ext.noticechoice.nc = {};
          }
          dojo.io.script.get({
            url: _2fc
          });
          dojo.subscribe("/ext/noticechoice/nc/loaded", function() {
            try {
              ext.noticechoice.nc.init();
            } catch (e) {}
          });
        });
      }
      return !!ibmweb.meta.nc_questiontype && !!ibmweb.meta.nc_pagedesc;
    }
  });
}
if (!dojo._hasResource["ibmweb.dynnav._base-all"]) {
  dojo._hasResource["ibmweb.dynnav._base-all"] = true;
  dojo.provide("ibmweb.dynnav._base-all");
}
if (!dojo._hasResource["ibmweb.dynnav"]) {
  dojo._hasResource["ibmweb.dynnav"] = true;
  dojo.provide("ibmweb.dynnav");
  if (typeof(window.PMM) == "undefined" || window.PMM == null) {
    window.PMM = {
      isLoaded: false
    };
  }
  dojo.mixin(ibmweb.dynnav, {
    disabledCountries: ["xxilhe"],
    _modules: {},
    isDOMLoaded: false,
    PMM: {},
    isDataLoaded: false,
    isValid: true,
    _bundleCallbacks: [],
    _bundleParams: {},
    _bundleBlock: [],
    init: function() {
      if (!ibmweb.meta.lc || !ibmweb.meta.cc) {
        return;
      }
      if (dojo.indexOf(this.disabledCountries, ibmweb.meta.cpi) != -1) {
        return;
      }
      dojo.io.script.get({
        url: ibmweb.config.dataUrl + ibmweb.meta.cpi + "-" + ibmweb.meta.encoding + ".js",
        checkString: "ibmweb.dynnav.__checkString",
        timeout: ibmweb.config.timeout,
        error: function(_2fd, _2fe) {
          ibmweb.dynnav.dataCallback = function() {};
          ibmweb.dynnav._onError();
          ibmweb.dynnav.isDataLoaded = true;
        }
      });
      if (ibmweb.meta.cc == "us") {
        if (ibmweb.meta.ibm_pageattributes.indexOf("flashlead") > -1) {
          dojo.query("#ibm-esite-link").style("display", "none");
        }
      }
      ibmweb.config.parseMetaTags();
      var _2ff = false;
      for (var key in ibmweb.config.lazyload) {
        if (ibmweb.config.get([key, "enabled"])) {
          dojo["require"](ibmweb.config.lazyload[key]);
          _2ff = true;
        }
      }
      var _300 = function() {
        var tmp = dojo.mixin({}, ibmweb.external, ibmweb.dynnav);
        for (var key in tmp) {
          if (key.substring(0, 1) != "_" && typeof(tmp[key].prototype) != "undefined" && !!tmp[key].prototype.isA_Module) {
            var _301 = new tmp[key]();
            if (_301.init()) {
              this._modules[key] = _301;
            }
          }
        }
        this.executeBundleCall();
        dojo.addOnLoad(function() {
          ibmweb.dynnav._initWithDOM();
        });
      };
      if (_2ff) {
        dojo.addOnLoad(dojo.hitch(this, _300));
      } else {
        _300.call(this);
      }
    },
    registerModule: function(_302) {
      if (_302.init()) {
        this._modules[_302.declaredClass] = _302;
      }
    },
    isServiceEnabled: function(_303) {
      return ibmweb.config.dynnav.enabled && !!ibmweb.config.get([_303, "enabled"]);
    },
    getCallback: function(_304, _305) {
      return ibmweb.callback.createJSONPWrapper(null, function() {
        var args = arguments;
        ibmweb.queue.push(function() {
          return ibmweb.dynnav.isDataLoaded && ibmweb.dynnav.isDOMLoaded;
        }, function() {
          try {
            _304[_305].apply(_304, args);
          } catch (e) {}
        });
      });
    },
    fetchData: function(id, _306, _307, _308) {
      ibmweb.queue.push(function() {
        return ibmweb.dynnav._bundleCallbacks.length == 0;
      }, function() {
        ibmweb.dynnav.addBundleCallback(id, _306, _307, _308);
        ibmweb.dynnav.executeBundleCall();
      });
    },
    addBundleCallback: function(id, _309, _30a, _30b) {
      var func = this.getCallback(_309, _30a);
      this._bundleCallbacks.push(id + ":" + func);
      dojo.mixin(this._bundleParams, _30b);
      return func;
    },
    blockBundle: function(sid) {
      this._bundleBlock[sid] = 1;
    },
    releaseBundle: function(sid) {
      var i = dojo.indexOf(this._bundleBlock, sid);
      if (i != -1) {
        this._bundleBlock.splice(i, 1);
      }
    },
    isBundleBlocked: function() {
      return this._bundleBlock.length != 0;
    },
    executeBundleCall: function() {
      ibmweb.queue.push(function() {
        return !ibmweb.dynnav.isBundleBlocked();
      }, function() {
        var _30c = ibmweb.dynnav._bundleCallbacks.join("@");
        if (!_30c) {
          return;
        }
        var _30d = dojo.mixin({
          cc: ibmweb.meta.cc,
          lc: ibmweb.meta.lc,
          format: "json",
          ts: ((new Date).getTime())
        }, ibmweb.dynnav._bundleParams, {
          cb: _30c
        });
        var ip = /ip=([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3})/.exec(document.location.search);
        var _30e = /domain=([a-zA-Z\.\-0-9]+)/.exec(document.location.search);
        if (ip && _30e) {
          _30d.ip = ip[1];
          _30d.domain = _30e[1];
        }
        dojo.io.script.get({
          url: ibmweb.config.dynNavBaseUrl,
          content: _30d,
          timeout: ibmweb.config.timeout,
          error: function(_30f, _310) {
            var cb = dojo.getObject("args.content.cb", false, _310);
            if (cb) {
              var cbs = cb.split("@");
              for (var i = 0, j = cbs.length; i < j; i++) {
                var _311 = cbs[i].split(":");
                if (_311.length == 2 && typeof(window[_311[1]]) == "function") {
                  window[_311[1]].call(window);
                  window[_311[1]] = function() {};
                }
              }
            }
          }
        });
        ibmweb.dynnav._bundleParams = {};
        ibmweb.dynnav._bundleCallbacks = [];
      });
    },
    _initWithDOM: function() {
      this.isDOMLoaded = true;
      this.isValid = dojo.byId("ibm-top") && dojo.byId("ibm-masthead") && dojo.byId("ibm-footer") && (dojo.byId("ibm-content") || dojo.query("div.lotusFrame")[0]);
      if (!this.isValid && ibmweb.w3) {
        this.isValid = true;
      }
      if (!this.isValid) {
        return;
      }
      for (var _312 in this._modules) {
        if (typeof(this._modules[_312]) == "object") {
          try {
            this._modules[_312].onLoad();
          } catch (e) {}
        }
      }
    },
    _initWithData: function(data) {
      if (!this.isDOMLoaded) {
        dojo.addOnLoad(function() {
          ibmweb.dynnav._initWithData(data);
        });
        return;
      }
      if (!this.isValid) {
        return;
      }
      for (var _313 in this._modules) {
        if (typeof(this._modules[_313]) == "object") {
          try {
            this._modules[_313].onData(data);
          } catch (e) {}
        }
      }
    },
    _onError: function() {
      for (var _314 in this._modules) {
        if (typeof(this._modules[_314]) == "object") {
          try {
            this._modules[_314].onError();
          } catch (e) {}
        }
      }
    },
    dataCallback: function(data) {
      data.isLoaded = true;
      ibmweb.dynnav.__checkString = true;
      ibmweb.dynnav.PMM = data;
      if (!window.PMM.isLoaded && !ibmweb.info.v16) {
        window.PMM = data;
      }
      ibmweb.dynnav.isDataLoaded = true;
      dojo.addOnLoad(function() {
        ibmweb.dynnav._initWithData(data);
      });
    },
    isModuleLoaded: function(_315) {
      return !!ibmweb.dynnav._modules[_315];
    },
    getLoadedModule: function(_316) {
      return ibmweb.dynnav._modules[_316];
    },
    executeOnModule: function(_317, _318, _319) {
      if (!!ibmweb.dynnav._modules[_317] && typeof(ibmweb.dynnav._modules[_317][_318]) == "function") {
        if (!!_319) {
          ibmweb.dynnav._modules[_317][_318].apply(ibmweb.dynnav._modules[_317], _319);
        } else {
          ibmweb.dynnav._modules[_317][_318].apply(ibmweb.dynnav._modules[_317]);
        }
      }
    }
  });
  ibmCommonDynamicNavShowResults = function() {};
}
if (!dojo._hasResource["dojo.regexp"]) {
  dojo._hasResource["dojo.regexp"] = true;
  dojo.provide("dojo.regexp");
  dojo.getObject("regexp", true, dojo);
  dojo.regexp.escapeString = function(str, _31a) {
    return str.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, function(ch) {
      if (_31a && _31a.indexOf(ch) != -1) {
        return ch;
      }
      return "\\" + ch;
    });
  };
  dojo.regexp.buildGroupRE = function(arr, re, _31b) {
    if (!(arr instanceof Array)) {
      return re(arr);
    }
    var b = [];
    for (var i = 0; i < arr.length; i++) {
      b.push(re(arr[i]));
    }
    return dojo.regexp.group(b.join("|"), _31b);
  };
  dojo.regexp.group = function(_31c, _31d) {
    return "(" + (_31d ? "?:" : "") + _31c + ")";
  };
}
if (!dojo._hasResource["dojo.cookie"]) {
  dojo._hasResource["dojo.cookie"] = true;
  dojo.provide("dojo.cookie");
  dojo.cookie = function(name, _31e, _31f) {
    var c = document.cookie;
    if (arguments.length == 1) {
      var _320 = c.match(new RegExp("(?:^|; )" + dojo.regexp.escapeString(name) + "=([^;]*)"));
      return _320 ? decodeURIComponent(_320[1]) : undefined;
    } else {
      _31f = _31f || {};
      var exp = _31f.expires;
      if (typeof exp == "number") {
        var d = new Date();
        d.setTime(d.getTime() + exp * 24 * 60 * 60 * 1000);
        exp = _31f.expires = d;
      }
      if (exp && exp.toUTCString) {
        _31f.expires = exp.toUTCString();
      }
      _31e = encodeURIComponent(_31e);
      var _321 = name + "=" + _31e,
        _322;
      for (_322 in _31f) {
        _321 += "; " + _322;
        var _323 = _31f[_322];
        if (_323 !== true) {
          _321 += "=" + _323;
        }
      }
      document.cookie = _321;
    }
  };
  dojo.cookie.isSupported = function() {
    if (!("cookieEnabled" in navigator)) {
      this("__djCookieTest__", "CookiesAllowed");
      navigator.cookieEnabled = this("__djCookieTest__") == "CookiesAllowed";
      if (navigator.cookieEnabled) {
        this("__djCookieTest__", "", {
          expires: -1
        });
      }
    }
    return navigator.cookieEnabled;
  };
}
if (!dojo._hasResource["ibmweb.legacy"]) {
  dojo._hasResource["ibmweb.legacy"] = true;
  dojo.provide("ibmweb.legacy");
  if (typeof(ibmCommon) != "object") {
    ibmCommon = {};
  }
  if (typeof(ibmCommon.data) != "object") {
    ibmCommon.data = {};
  }
  ibmCommon.data.provide = ibmweb.data.provide;
  dojo.addOnLoad(function() {
    if (ibmweb.info.v17 && !document.getElementById("ibm-print-masthead") && !!dojo.byId("ibm-masthead")) {
      dojo.place("<img src=\"" + ibmweb.config.imageUrl + "t/ibm_logo_print.png\" width=\"43\" height=\"15\" id=\"ibm-print-masthead\" alt=\"IBM Print\" />", "ibm-top", "first");
    }
  });
  (function() {
    var dc = document.cookie;
    var _324 = "ibmSurvey=";
    var _325 = dc.indexOf(_324);
    var now = new Date();
    if (_325 == -1 || (_325 > 0 && dc.indexOf("; " + _324) == -1)) {
      if (window.location.href.indexOf(".ibm.com") > 0) {
        document.cookie = _324 + now.getTime().toString() + "; path=/; domain=.ibm.com";
      }
    }
  })();
  if (typeof(encodeURIComponent) != "function") {
    encodeURIComponent = escape;
  }
  if (typeof(decodeURIComponent) != "function") {
    decodeURIComponent = unescape;
  }
  dojo.addOnLoad(function() {
    if (ibmweb.meta.encoding != "utf8") {
      dojo.query("#ibm-search-form input[name=\"en\"][value=\"utf\"]").orphan();
    }
  });
  if (typeof ibmStats === "undefined") {
    var ibmStats = ibmStats || {};
    if (!!ibmStats.event) {
      ibmStats.event = function(_326) {
        ibmweb.queue.push(function() {
          return typeof(ibmStats.loaded) != "undefined";
        }, function() {
          ibmStats.event(_326);
        });
      };
    }
  }
  if (document.cookie.indexOf("ipcInfo=") == -1 && document.cookie.indexOf("lenovoPrefs=") != -1 && !!window && !!window.location && String(window.location.hostname).toLowerCase().indexOf("ibm.com") != -1) {
    document.cookie = "ipcInfo=" + escape("cc=;lc=") + "; path=/; domain=.ibm.com";
    if (document.cookie.indexOf("ipcInfo=") != -1) {
      if (typeof ibmCommonCookie == "undefined") {
        document.write("<scr", "ipt type=\"text/javascript\" src=\"//www.ibm.com/common/cookie/cookie.js\"></scr", "ipt>");
      }
      document.write("<scr", "ipt type=\"text/javascript\" src=\"//www.ibm.com/common/ocaiset.js\"></scr", "ipt>");
    }
  }
  dojo.addOnLoad(function() {
    dojo.cookie("cmTPSet", null, {
      expires: -1,
      domain: ".ibm.com",
      path: "/"
    });
    dojo.cookie("sauidp", null, {
      expires: -1,
      domain: ".ibm.com",
      path: "/"
    });
  });
  if (dojo.isOpera) {
    dojo.addOnLoad(function() {
      if (dojo.byId("ibm-leadspace-head")) {
        dojo.byId("ibm-leadspace-head").style.marginTop = "0px !important";
      }
    });
  }
  dojo.addOnLoad(function() {
    if (ibmweb.meta.cpi == "roro" || ibmweb.meta.cpi == "plpl" || ibmweb.meta.cpi == "rssr" || ibmweb.meta.cpi == "ilhe" || ibmweb.meta.cpi == "sksk" || ibmweb.meta.cpi == "czcs" || ibmweb.meta.cpi == "sisl") {
      var url = dojo.config.modulePaths ? dojo.config.modulePaths.ibmweb.substr(0, dojo.config.modulePaths.ibmweb.indexOf("js/dojo/")) + "v17/css/" : "/common/v17/css/";
      dojo.create("link", {
        type: "text/css",
        title: "www",
        rel: "stylesheet",
        href: url + "font-replacement.css"
      }, dojo.query("head")[0]);
    }
  });
  if (dojo.isIE < 7) {
    dojo.addOnLoad(function() {
      var node = dojo.byId("ibm-leadspace-body");
      if (node) {
        var _327 = dojo.style(node, "backgroundImage");
        if (_327.indexOf(".png") > -1) {
          var _327 = _327.substr(4);
          _327 = _327.substr(0, _327.length - 1);
          node.style.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=" + _327 + ", sizingMethod=crop);";
          dojo.style(node, "backgroundImage", "none");
        }
      }
    });
  }
  if (dojo.version.major <= 1 && dojo.version.minor < 4 && dojo.isIE) {
    var origAddOnLoad = dojo.addOnLoad;
    dojo.addOnLoad = function() {
      var args = arguments;
      if (document.readyState == "complete") {
        dojo.addOnLoad = origAddOnLoad;
        dojo.addOnLoad.apply(dojo, args);
      } else {
        setTimeout(function() {
          dojo.addOnLoad.apply(dojo, args);
        }, 10);
      }
    };
  }
}
if (!dojo._hasResource["ibmweb.truste"]) {
  dojo._hasResource["ibmweb.truste"] = true;
  dojo.provide("ibmweb.truste");
  if (ibmweb.config.config === "www") {
    ibmweb.truste.enabled = function() {};
    ibmweb.truste.decision = function() {};
    var id = ibmweb.queue.push(function() {
      return typeof(truste) != "undefined";
    }, function() {
      ibmweb.truste.enabled = function() {
        return true;
      };
      ibmweb.truste.decision = function(_328, dump) {
        if (!_328) {
          _328 = "ibm.com";
        }
        var json = truste.cma.callApi("getConsentDecision", _328);
        return dump ? json : json.consentDecision;
      };
      var json = "{\"PrivacyManagerAPI\":{\"action\":\"getConsent\",\"timestamp\":" + new Date().getTime() + "}}";
      window.top.postMessage(json, "*");
      var _329 = function() {
        dojo.publish("/ibm/truste-api/update");
      };
      if (window.addEventListener) {
        window.addEventListener("message", _329, false);
      } else {
        if (window.attachEvent) {
          window.attachEvent("onmessage", _329);
        }
      }
    });
    dojo.addOnLoad(function() {
      var _32a = ["gb", "fr", "de", "uk", "es", "nl", "gr", "at", "hu", "lv", "lt", "pl", "se", "bg", "dk", "fi", "cz", "ee", "pt", "be", "cy", "no", "sk", "ie", "it", "ro", "si", "ch", "ar", "bo", "cl", "co", "ec", "pe", "py", "uy", "ve"];
      if (dojo.indexOf(_32a, ibmweb.meta.cc) == -1 || typeof(ibmStats) == "undefined") {
        ibmweb.queue.remove(id);
        ibmweb.truste.enabled = function() {
          return false;
        };
        dojo.publish("/ibm/truste-api/disabling");
      }
    });
    dojo.subscribe("/ibmweb/dynnav/footermenu/finished", function() {
      ibmweb.queue.push(function() {
        return (typeof(truste) !== "undefined" && truste.eu && truste.eu.iconid && dojo.byId(truste.eu.iconid) && typeof(truste.eu.clickListener) === "function");
      }, function() {
        var _32b = dojo.query("#ibm-footer > ul > li:last-child"),
          _32c = dojo.query(dojo.create("a")),
          _32d = dojo.query(dojo.create("li")).append(_32c);
        var meta = document.getElementsByTagName("meta"),
          _32e = "Cookie Preferences",
          lc = "",
          name = "";
        for (var i = 0, j = meta.length; i < j; i += 1) {
          name = meta[i].httpEquiv || meta[i].name;
          name = name.toLowerCase();
          if (name == "dc.language") {
            lc = meta[i].getAttribute("content").toLowerCase();
          }
          switch (lc.substring(0, 2)) {
            case "en":
              _32e = "Cookie Preferences";
              break;
            case "es":
              _32e = "Configuración de Cookies";
              break;
            case "fr":
              _32e = "Préférences relatives aux témoins";
              break;
            default:
              _32e = "Cookie Preferences";
          }
        }
        _32c.attr("href", "#").innerHTML(_32e).onclick(function(e) {
          truste.eu.clickListener();
          e.preventDefault();
        }).onkeyup(function(e) {
          if (e.keyCode === 13) {
            truste.eu.clickListener();
            e.preventDefault();
          }
        });
        if (_32b[0]) {
          if (dojo.query("> a", _32b[0])[0] && dojo.query("> a", _32b[0]).innerHTML().indexOf("Feedback") !== -1) {
            _32b.before(_32d);
          } else {
            _32b.after(_32d);
          }
          dojo.query("#" + truste.eu.iconid).style("display", "none");
        }
      });
    });
  }
}
if (!dojo._hasResource["ibmweb.readmore"]) {
  dojo._hasResource["ibmweb.readmore"] = true;
  dojo.provide("ibmweb.readmore");
  ibmweb.readmore.content = "";
  ibmweb.readmore.defaultLimit = 200;
  ibmweb.readmore.limit = "";
  ibmweb.readmore.init = function() {
    dojo.query(".ibm-readmore").forEach(function(node, _32f) {
      if (node.tagName == "DIV") {
        var lrm = dojo.create("p", {
          innerHTML: "<a href='javascript:;' class='ibm-maximize-link' style='display:inline;' aria-label='Click to Expand'>Show&nbsp;more</a>",
          style: {
            margin: "0px"
          }
        });
        var lrl = dojo.create("p", {
          innerHTML: "<a href='javascript:;' class='ibm-minimize-link' style='display:inline;' aria-label='Click to Shrink'>Show&nbsp;less</a>",
          style: {
            margin: "0"
          }
        });
      } else {
        if (node.tagName == "UL" || node.tagName == "OL") {
          var lrm = dojo.create("li", {
            innerHTML: "<a href='javascript:;' class='ibm-maximize-link' style='display:inline;text-decoration:none;' aria-label='Click to Expand'>Show&nbsp;more</a>",
            style: {
              margin: "0",
              background: "none",
              listStyle: "none"
            }
          });
          var lrl = dojo.create("li", {
            innerHTML: "<a href='javascript:;' class='ibm-minimize-link' style='display:inline;text-decoration:none;' aria-label='Click to Shrink'>Show&nbsp;less</a>",
            style: {
              margin: "0",
              background: "none",
              listStyle: "none"
            }
          });
        } else {
          var lrm = dojo.create("span", {
            innerHTML: "<a href='javascript:;' class='ibm-maximize-link' style='display:inline;text-decoration:none;' aria-label='Click to Expand'>Show&nbsp;more</a>",
            style: {
              margin: "0px 0px 0px 11px"
            }
          });
          var lrl = dojo.create("span", {
            innerHTML: "<a href='javascript:;' class='ibm-minimize-link' style='display:inline;text-decoration:none;' aria-label='Click to Shrink'>Show&nbsp;less</a>",
            style: {
              margin: "0px 0px 0px 11px"
            }
          });
        }
      }
      ibmweb.readmore.readLength(node);
      var _330 = node.innerHTML.trim();
      dojo.connect(lrm, "onclick", function(e) {
        node.innerHTML = _330;
        lrl.innerHTML = "<a href='javascript:;' class='ibm-minimize-link' style='display:inline;text-decoration:none;' aria-label='click to Shrink'>Show&nbsp;less</a>";
        dojo.place(lrl, node);
      });
      _330 = _330.replace(new RegExp(">[\n\t ]+<", "g"), "><");
      _330 = _330.replace(/<!--[\s\S]*?-->/g, "");
      var _331 = ibmweb.readmore.htmlSubstring(_330, ibmweb.readmore.limit);
      var ind = _330.trim().indexOf(_331) + _331.length;
      node.innerHTML = _331;
      dojo.place(lrm, node);
      dojo.connect(lrl, "onclick", function(e) {
        ibmweb.readmore.readLength(node);
        node.innerHTML = ibmweb.readmore.htmlSubstring(_330, ibmweb.readmore.limit);
        lrm.innerHTML = "<a href='javascript:;' class='ibm-maximize-link' style='display:inline;text-decoration:none;' aria-label='click to Expand'>Show&nbsp;more</a>";
        dojo.place(lrm, node);
      });
    });
  };
  ibmweb.readmore.readLength = function(elem) {
    ibmweb.readmore.limit = ibmweb.readmore.defaultLimit;
    var _332 = dojo.query(".ibm-readmore-length", elem)[0];
    if (_332.parentElement.tagName == "LI") {
      dojo.style(_332.parentElement, "display", "none");
    } else {
      dojo.style(_332, "display", "none");
    }
    if (typeof _332 != "undefined") {
      if (/^\d+$/.test(_332.innerHTML)) {
        ibmweb.readmore.limit = _332.innerHTML;
      } else {}
    }
  };
  ibmweb.readmore.htmlSubstring = function(s, n) {
    var m, r = /<([^>\s]*)[^>]*>/g,
      _333 = [],
      _334 = 0,
      _335 = "",
      _336 = 0;
    n = (parseInt(n) + parseInt(n.length));
    while ((m = r.exec(s)) && n) {
      _336 = 0;
      var temp = s.substring(_334, m.index).substr(0, n);
      if (temp.match(/\s/g) != null) {
        _336 = temp.match(/\s/g).length;
      }
      temp = s.substring(_334, m.index).substr(0, n);
      _335 += temp;
      n -= temp.length;
      if (n == 0 && (s.substring(_334, m.index).substr(temp.length, 1) != " " || temp.length == s.substring(_334, m.index).length)) {
        _335 = _335.replace(new RegExp("><", "g"), "> <");
        if (temp.length != s.substring(_334, m.index).length) {
          _335 = _335.substr(0, _335.lastIndexOf(" "));
        } else {
          _335 = _335.substr(0, _335.length);
        }
      }
      _334 = r.lastIndex;
      if (n) {
        _335 += m[0];
        if (m[1].indexOf("/") === 0) {
          _333.pop();
        } else {
          if (m[1].lastIndexOf("/") !== m[1].length - 1) {
            _333.push(m[1]);
          }
        }
      }
    }
    if (n != 0) {
      if (s.substr(_334, n).match(/\s/g) != null) {
        _336 = s.substr(_334, n).match(/\s/g).length;
      }
      _335 += s.replace(/\s{2,}/g, "").substr(_334, (n));
      if (s.replace(/\s{2,}/g, "").substr(_335.length, 1) != " ") {
        _335 = _335.substr(0, _335.lastIndexOf(" "));
      }
    }
    _335 += "...";
    while (_333.length) {
      _335 += "</" + _333.pop() + ">";
    }
    return _335;
  };
  dojo.ready(function() {
    ibmweb.readmore.init();
  });
}
if (!dojo._hasResource["ibmweb.filebrowse"]) {
  dojo._hasResource["ibmweb.filebrowse"] = true;
  dojo.provide("ibmweb.filebrowse");
  dojo.ready(function() {
    var _337 = {
      id: "filebrowse",
      init: function() {
        dojo.query("form.ibm-styled-form input[type=file].ibm-multi-file-browser, form.ibm-styled-form input[type=file].ibm-single-file-browser").forEach(function(_338) {
          var form = _338.parentElement;
          while (form.tagName != "FORM") {
            form = form.parentElement;
          }
          if (form.className.indexOf("ibm-column-form") < 0 && form.className.indexOf("ibm-row-form") < 0) {
            return false;
          }
          var span = dojo.create("span", {});
          var _339 = dojo.create("span", {
            className: "ibm-filebrowser"
          }, span);
          dojo.place(span, _338.parentElement, "replace");
          var _33a = dojo.create("a", {
            className: "ibm-upload-link",
            role: "button",
            href: "javascript:;",
            innerHTML: "Browse",
            "aria-label": "Browse for a file..."
          }, _339);
          var _33b = dojo.query(_338);
          dojo.place(_33a, _339);
          dojo.place(_33b[0], _339);
          dojo.connect(_33a, "onclick", _337.onclick);
          dojo.connect(_338, "onchange", _337.onchange);
          dojo.query(_338).forEach(function(node, _33c) {
            var name = dojo.attr(node, "name");
            if (!name && dojo.hasClass(node, "ibm-single-file-browser")) {
              dojo.attr(node, "name", "uploadFile");
            } else {
              if (!name && dojo.hasClass(node, "ibm-multi-file-browser")) {
                dojo.attr(node, "name", "uploadFile[]");
              } else {
                if (name && dojo.hasClass(node, "ibm-multi-file-browser")) {
                  dojo.attr(node, "name", dojo.attr(node, "name") + "[]");
                }
              }
            }
          });
        });
      },
      onclick: function(e) {
        var _33d = e.currentTarget;
        if (dojo.hasClass(e.currentTarget, "ibm-delete-link")) {
          if (dojo.query(e.currentTarget.parentElement.parentElement).children(".ibm-filebrowser").length === 1) {
            dojo.destroy(e.currentTarget.nextSibling.nextSibling);
            dojo.removeClass(e.currentTarget, "ibm-delete-link");
            dojo.addClass(e.currentTarget, "ibm-upload-link");
            e.currentTarget.innerHTML = "Browse";
            dojo.attr(e.currentTarget, "aria-label", "Browse for a file...");
            var _33e = dojo.query(_33d.parentElement).children(".ibm-multi-file-browser")[0];
            if (!_33e) {
              return;
            }
            var _33f = dojo.query(e.currentTarget.parentElement.parentElement).children(".ibm-addlink-display")[0];
            if (_33f) {
              dojo.removeClass(_33f, "ibm-addlink-display");
              dojo.addClass(_33f, "ibm-addlink");
            }
          } else {
            var _340 = e.currentTarget.parentElement.nextSibling;
            while (_340.tagName != "SPAN") {
              _340 = _340.nextSibling;
            }
            var _341 = e.currentTarget.parentElement.parentElement;
            dojo.destroy(e.currentTarget.parentElement);
            var _342 = dojo.query(_341).children(".ibm-filebrowser").last();
            var _343 = "";
            if (_342) {
              var _344 = dojo.query(_342).children("input")[0];
              _343 = dojo.attr(_344, "value");
            }
            var _345 = dojo.query(_341).children(".ibm-addlink-display")[0];
            dojo.attr(dojo.query(".ibm-add1-link", _345)[0], "aria-label", "Browse for another file ... Already selected " + _343);
            dojo.query("a", _340)[0].focus();
            dojo.destroy(e.currentTarget.parentElement);
          }
        } else {
          dojo.query("input[type=file]", _33d.parentElement)[0].click();
        }
      },
      onchange: function(evt) {
        var form = evt.currentTarget.parentElement;
        while (form.tagName != "FORM") {
          form = form.parentElement;
        }
        var _346 = dojo.query(evt.currentTarget);
        var _347 = _346.val().split("\\");
        if (_347.length > 1) {
          name = _347[_347.length - 1];
        } else {
          name = _347[0];
        }
        if (form.className.indexOf("column-form") > 0 && name.length > 30) {
          name = name.substring(0, 14) + "..." + name.substring((name.length - 10), name.length);
        } else {
          if (form.className.indexOf("row-form") > 0 && name.length > 15) {
            name = name.substring(0, 6) + "..." + name.substring((name.length - 7), name.length);
          }
        }
        var _348 = dojo.create("span", {});
        dojo.place(_348, _346[0], "after");
        _346.next().text("(" + name + ")");
        _346.prev().text("Remove");
        _346.prev().attr("aria-label", "Remove " + _347);
        _346.prev()[0].className = "ibm-delete-link";
        if (_346[0].className.indexOf("ibm-multi-file-browser") == -1) {
          return;
        }
        var _349 = dojo.query(_346[0].parentElement.parentElement).children(".ibm-addlink-display, .ibm-addlink")[0];
        if (!_349) {
          var _34a = dojo.create("span", {
            className: "ibm-addlink-display"
          });
          var _34b = dojo.create("a", {
            className: "ibm-add1-link",
            role: "button",
            "aria-label": "Browse for another file ... Already selected " + _347,
            href: "javascript:;",
            innerHTML: "Add more"
          });
          dojo.connect(_34b, "onclick", _337.createBrowse);
          dojo.place(_34b, _34a);
          dojo.place(_34a, _346[0].parentElement.parentElement);
          _34b.focus();
        } else {
          dojo.removeClass(_349, "ibm-addlink");
          dojo.addClass(_349, "ibm-addlink-display");
          dojo.attr(dojo.query(".ibm-add1-link", _349)[0], "aria-label", "Browse for another file ... Already selected " + _347);
        }
      },
      createBrowse: function(e) {
        var form = e.currentTarget.parentElement;
        while (form.tagName != "FORM") {
          form = form.parentElement;
        }
        var _34c = dojo.create("span", {
          className: "ibm-filebrowser"
        });
        var _34d = dojo.create("a", {
          className: "ibm-upload-link",
          role: "button",
          href: "javascript:;",
          innerHTML: "Browse",
          "aria-label": "Browse for a file..."
        });
        var _34e = dojo.create("input", {
          className: "ibm-multi-file-browser",
          type: "file",
          name: "uploadFile[]"
        });
        dojo.place(_34d, _34c);
        dojo.place(_34e, _34c);
        var _34f = dojo.query(".ibm-filebrowser", form)[0].parentElement;
        dojo.place(_34c, dojo.query(".ibm-addlink-display", _34f)[0], "before");
        dojo.connect(_34d, "onclick", _337.onclick);
        dojo.connect(_34e, "onchange", _337.onchange);
        dojo.removeClass(e.currentTarget.parentElement, "ibm-addlink-display");
        dojo.addClass(e.currentTarget.parentElement, "ibm-addlink");
        _34e.click();
      }
    };
    _337.init();
  });
}
if (!dojo._hasResource["ibmweb.filesize"]) {
  dojo._hasResource["ibmweb.filesize"] = true;
  dojo.provide("ibmweb.filesize");
  dojo.ready(function() {
    var _350 = {
      youtubeLinks: [],
      youtubeVideoTimings: [],
      init: function() {
        var _351 = [".ibm-document-link", ".ibm-pdf-link", ".ibm-video-link", ".ibm-youtube-link"];
        for (var i = 0; i < _351.length; i++) {
          dojo.query(_351[i]).forEach(function(node, _352) {
            if (dojo.query(".ibm-item-note", node).length <= 0) {
              if (!node.getAttribute("href")) {
                var _353 = dojo.query("> a", node)[0];
                if (_353) {
                  var _354 = _353.getAttribute("href").substring(_353.getAttribute("href").indexOf("?") + 1, _353.getAttribute("href").length);
                  var _355 = dojo.queryToObject(_354);
                  if (typeof _355["v"] !== "undefined" && _355["v"] != "") {
                    if (/^[A-Za-z0-9_-]{8,11}$/.test(_355["v"])) {
                      _350.youtubeLinks.push(_353);
                    } else {}
                  } else {}
                }
              }
              if (node.getAttribute("href") && node.getAttribute("href").indexOf("youtube.com") > 0) {
                var _354 = node.getAttribute("href").substring(node.getAttribute("href").indexOf("?") + 1, node.getAttribute("href").length);
                var _355 = dojo.queryToObject(_354);
                if (typeof _355["v"] !== "undefined" && _355["v"] != "") {
                  if (/^[A-Za-z0-9_-]{8,11}$/.test(_355["v"])) {
                    _350.youtubeLinks.push(node);
                  } else {}
                } else {}
              } else {
                try {
                  if (node.getAttribute("href") && node.getAttribute("href").indexOf("youtube.com") < 0) {
                    dojo.xhr("HEAD", {
                      url: node.getAttribute("href"),
                      handle: function(_356, _357) {
                        var _358 = _350.formatSize(_357.xhr.getResponseHeader("Content-Length"));
                        _350.displayNote(node, _358);
                      }
                    });
                  }
                } catch (e) {}
              }
            }
          });
        }
        if (_350.youtubeLinks.length > 0) {
          _350.getYoutubeTiming();
        }
      },
      displayNote: function(node, _359) {
        if ((_359 != null && _359.indexOf("0.00") < 0) && !(node.getAttribute("href") == "#" || node.getAttribute("href") == "")) {
          var _35a = dojo.query(".ibm-item-note", node);
          if (_35a.length == 0) {
            dojo.create("span", {
              className: "ibm-item-note",
              innerHTML: " (" + _359 + ")"
            }, node);
          } else {
            if (_35a.innerHTML().indexOf(",") > 0) {
              var cont = _35a.innerHTML().split(",");
              if (cont[0].indexOf("(") < 0) {
                _359 = cont[0] + ", " + _359;
              } else {
                _359 = cont[0].replace("(", "") + ", " + _359;
              }
            }
            _35a.innerHTML(" (" + _359 + ")");
          }
        }
      },
      formatSize: function(_35b) {
        if (_35b != null) {
          if (_35b >= 1073741824) {
            _35b = (_35b / 1073741824).toFixed(0) + "GB";
          } else {
            if (_35b >= 1048576) {
              _35b = (_35b / 1048576).toFixed(0) + "MB";
            } else {
              _35b = (_35b / 1024).toFixed(0) + "KB";
            }
          }
        }
        return _35b;
      },
      getYoutubeTiming: function() {
        if (_350.youtubeLinks.length > 0) {
          for (var i = 0; i < _350.youtubeLinks.length; i++) {
            var node = _350.youtubeLinks[i];
            var _35c = node.getAttribute("href").substring(node.getAttribute("href").indexOf("?") + 1, node.getAttribute("href").length);
            var _35d = dojo.queryToObject(_35c);
            if (typeof _35d["v"] !== "undefined") {
              if (ibmweb.info.isIE && ibmweb.info.ieVersion < 10) {
                var _35e = window.location.href.split("/");
                var _35f = _35e[0];
                if (!(ibmweb.info.isIE && ibmweb.info.ieVersion == 6) && (_35f == "http:") && (window.location.href.indexOf("ibm.com") !== -1)) {
                  var url = "";
                  _35e[0] = "https:";
                  for (var i = 0, len = _35e.length; i < len; i++) {
                    url += _35e[i] + "/";
                  }
                  url = url.slice(0, -1);
                  window.location.href = url;
                }
              }
              dojo.io.script.get({
                url: "https://www.googleapis.com/youtube/v3/videos?id=" + _35d["v"] + "&key=AIzaSyDPXIUJCKjezNBfNrhgiub5GRd1CIQJrNI&part=contentDetails&callback=ibmweb.filesize.youtubeFeedCallback",
                error: function(_360, _361) {
                  for (var key in _350.youtubeLinks) {
                    var _362 = _361.url.substring(_361.url.indexOf("api/videos/") + 11, _361.url.indexOf("?v="));
                    if (_350.youtubeLinks[key].href.indexOf(_362) > 0) {
                      break;
                    }
                  }
                  _350.youtubeLinks.splice(key, 1);
                }
              });
            }
          }
          ibmweb.queue.push(function() {
            if (_350.youtubeVideoTimings.length == _350.youtubeLinks.length) {
              return true;
            } else {
              return false;
            }
          }, function() {
            for (var i = 0; i < _350.youtubeLinks.length; i++) {
              var node = _350.youtubeLinks[i];
              var _363 = node.getAttribute("href").substring(node.getAttribute("href").indexOf("?") + 1, node.getAttribute("href").length);
              var _364 = dojo.queryToObject(_363);
              var j = 0;
              while (_350.youtubeVideoTimings[j]) {
                if (_364["v"] == _350.youtubeVideoTimings[j].id) {
                  _350.displayNote(node, _350.youtubeVideoTimings[j].time);
                  break;
                }
                j++;
                if (j == _350.youtubeVideoTimings.length) {
                  j = 0;
                }
              }
            }
          });
        }
      },
      youtubeFeedCallback: function(json) {
        if (typeof json.items != "undefined") {
          var _365 = json.items[0].contentDetails.duration;
          var _366 = _365.match(/PT(\d+H)?(\d+M)?(\d+S)?/);
          var hr = (parseInt(_366[1]) || 0);
          var min = (parseInt(_366[2]) || 0);
          var sec = (parseInt(_366[3]) || 0);
          if (hr < 10) {
            hr = "0" + hr;
          }
          if (min < 10) {
            min = "0" + min;
          }
          if (sec < 10) {
            sec = "0" + sec;
          }
          _350.youtubeVideoTimings.push({
            id: json.items[0].id,
            time: hr + ":" + min + ":" + sec
          });
        }
      }
    };
    ibmweb.filesize.youtubeFeedCallback = _350.youtubeFeedCallback;
    _350.init();
  });
}
if (!dojo._hasResource["ibmweb._base"]) {
  dojo._hasResource["ibmweb._base"] = true;
  dojo.provide("ibmweb._base");
  (function() {
    var path = ibmweb.moduleDomain.get().slice(0, -1);
    if (ibmweb.config.config === "www") {
      path += ibmweb.config.eluminate.path + "eluminate.js";
    } else {
      if (ibmweb.config.config === "w3") {
        path += ibmweb.config.w3_eluminate.path + "w3_eluminate.js";
      }
    }
    script = document.createElement("script");
    script.setAttribute("type", "text/javascript");
    script.setAttribute("src", path);
    //document.getElementsByTagName("head")[0].appendChild(script);
  })();
}
if (!dojo._hasResource["ibmweb.bitly"]) {
  dojo._hasResource["ibmweb.bitly"] = true;
  dojo.provide("ibmweb.bitly");
  dojo.declare("ibmweb.bitly", [], {
    url: null,
    onSuccess: null,
    onError: null,
    shorturl: null,
    rawData: null,
    constructor: function(_367) {
      dojo.mixin(this, _367);
    },
    shorten: function() {
      if (!this.url) {
        this.url = location.href;
      }
      dojo.io.script.get({
        url: "http://api.bit.ly/v3/shorten?login=" + ibmweb.config.bitly.login + "&apiKey=" + ibmweb.config.bitly.key + "&longUrl=" + escape(this.url) + "&format=json&callback=" + ibmweb.callback.createJSONPWrapper(this, "_callback"),
        timeout: ibmweb.config.bitly.timeout,
        error: dojo.hitch(this, function(_368, _369) {
          this._error();
        })
      });
    },
    _callback: function(data) {
      if (!data || !data.status_code || !data.status_code == 200) {
        this._error();
      }
      this.shorturl = data.data.url;
      this.rawData = data;
      if (this.onSuccess) {
        this.onSuccess(data.data.url);
      }
    },
    _error: function() {
      this.shorturl = this.url;
      if (this.onError) {
        this.onError(this.url);
      } else {
        if (this.onSuccess) {
          this.onSuccess(this.url);
        }
      }
    }
  });
}
if (!dojo._hasResource["ibmweb.sbs"]) {
  dojo._hasResource["ibmweb.sbs"] = true;
  dojo.provide("ibmweb.sbs");
  (function() {
    var _36a = {
      "cc": "us",
      "lc": "en",
      "encoding": "utf8",
      "element": "ibm-sbs-icons",
      "loadManually": false,
      init: function(_36b) {
        if (document.getElementsByTagName("head").length != 1) {
          return;
        }
        if (!!_36b) {
          if (!!_36b.cc) {
            this.cc = _36b.cc;
          }
          if (!!_36b.lc) {
            this.lc = _36b.lc;
          }
          if (!!_36b.encoding) {
            this.encoding = _36b.encoding;
          }
          if (!!_36b.element) {
            this.element = _36b.element;
          }
          if (!!_36b.loadManually) {
            this.loadManually = _36b.loadManually;
          }
        }
        if (!this.loadManually) {
          var _36c = document.createElement("link");
          _36c.href = "//www.ibm.com/common/v16/css/sbs.css";
          _36c.rel = "stylesheet";
          _36c.type = "text/css";
          document.getElementsByTagName("head")[0].appendChild(_36c);
          var _36d = document.createElement("script");
          _36d.src = "//www.ibm.com/common/js/sbs/" + this.cc + "/" + this.lc + "/" + this.cc + this.lc + "-" + this.encoding + ".js";
          _36d.type = "text/javascript";
          document.getElementsByTagName("head")[0].appendChild(_36d);
        }
      },
      getUrl: function() {
        return location.href;
      },
      render: function(data, _36e) {
        if (!_36e) {
          _36e = this.element;
        }
        if (typeof(_36e) == "string") {
          _36e = document.getElementById(_36e);
        }
        if (_36e == null) {
          return;
        }
        if (_36e.childNodes.length != 0) {
          return;
        }
        var _36f = this.getUrl();
        if (document.getElementsByTagName("title").length == 0) {
          return;
        }
        var _370 = document.getElementsByTagName("title")[0].innerHTML;
        var ul = document.createElement("ul");
        for (var i = 0, j = data.length; i < j; i++) {
          var $i = data[i];
          if ($i.id == "print" || $i.id == "w3-print") {
            var li = this.getPTPLink($i);
            if (li) {
              ul.appendChild(li);
            }
            continue;
          }
          if ($i.id == "email" || $i.id == "w3-email") {
            var li = this.getETPLink($i);
            if (li) {
              ul.appendChild(li);
            }
            continue;
          }
          if (!$i.url) {
            continue;
          }
          $i.targeturl = $i.url.replace(/%\{URL\}/g, encodeURIComponent(_36f)).replace(/%\{TITLE\}/g, encodeURIComponent(_370));
          var a = this.createLink($i);
          var li = document.createElement("li");
          li.appendChild(a);
          ul.appendChild(li);
        }
        _36e.appendChild(ul);
        _36e.onmouseover = function() {
          _36e.className += " ibm-active";
        };
        _36e.onmouseout = function() {
          _36e.className = _36e.className.replace(/\s+ibm-active/, "");
        };
        _36e.style.display = "block";
      },
      createLink: function($i) {
        var a = dojo.create("a", {
          className: "ibm-share-" + $i.id,
          title: $i.title,
          innerHTML: $i.title,
          href: (!!$i.targeturl) ? $i.targeturl : "#",
          rel: $i.id,
          onmousedown: function() {
            this.className += " ibm-share-active-" + this.rel;
          },
          onmouseout: function() {
            this.className = this.className.replace(/\s+ibm-share-active-.*/, "");
          },
          onclick: function(e) {
            ibmweb.sbs.clickHandler(e, $i);
          }
        });
        if (document.getElementById("ibm-com").className.indexOf("dijit_a11y") != -1) {
          a.innerHTML = "";
          var img = document.createElement("img");
          img.className = "ibm-access";
          img.src = ibmweb.config.imageUrl + "icons/sprites-pt-all.png";
          img.alt = $i.title;
          a.appendChild(img);
        }
        return a;
      },
      clickHandler: function() {
        return true;
      },
      getPTPLink: function() {
        return null;
      },
      getETPLink: function() {
        return null;
      }
    };
    ibmweb.sbs = _36a;
  })();
  ibmweb.sbs.getUrl = ibmweb.util.getUrl;
  ibmweb.sbs.clickHandler = function(_371, data) {
    if (data.id == "ibm-dogear") {
      _371.preventDefault();
      window.open(data.targeturl, data.title, "width=600,height=550");
      return false;
    }
    if (!data.url) {
      return;
    }
    _371.preventDefault();
    var a = _371.target;
    ibmweb.util.statsHelper({
      "ibmEV": "external link",
      "ibmEvAction": a.href,
      "ibmEvGroup": "SBS icons",
      "ibmEvName": "sbs-" + a.rel
    });
    dojo.style(a, "cursor", "wait");
    if (dojo.hasClass(a, "clicked")) {
      return;
    }
    dojo.addClass(a, "clicked");
    if (data && data.id == "stumble") {
      var url = data.url.replace(/%\{URL\}/g, encodeURIComponent(document.location.href)).replace(/%\{TITLE\}/g, encodeURIComponent(ibmweb.meta.title));
      window.location = url;
      return true;
    }
    var b = new ibmweb.bitly({
      url: ibmweb.util.getUrl(),
      onSuccess: function(_372) {
        dojo.style(a, "cursor", "auto");
        dojo.removeClass(a, "clicked");
        var url = data.url.replace(/%\{URL\}/g, encodeURIComponent(_372)).replace(/%\{TITLE\}/g, encodeURIComponent(ibmweb.meta.title));
        setTimeout(function() {
          window.location = url;
        }, 0);
      }
    });
    b.shorten();
    return true;
  };
  ibmweb.sbs.getETPLink = function(data) {
    if (ibmweb.config.sbs.email != true) {
      return null;
    }
    data.url = "mailto:?subject=%{TITLE}&body=%{URL}".replace(/%\{URL\}/g, encodeURIComponent(ibmweb.util.getUrl())).replace(/%\{TITLE\}/g, encodeURIComponent(ibmweb.meta.title));
    var a = this.createLink(data);
    dojo.connect(a, "onclick", null, function(_373) {
      dojo.stopEvent(_373);
      var link = _373.target;
      ibmweb.util.statsHelper({
        "ibmEV": "external link",
        "ibmEvAction": link.href,
        "ibmEvGroup": "SBS icons",
        "ibmEvName": "sbs-email"
      });
      window.location = data.url;
      return false;
    });
    var li = document.createElement("li");
    li.appendChild(a);
    return li;
  };
  ibmweb.sbs.getPTPLink = function(data) {
    if (ibmweb.config.sbs.print != true) {
      return null;
    }
    var a = this.createLink(data);
    dojo.connect(a, "onclick", null, function(_374) {
      dojo.stopEvent(_374);
      var link = _374.target;
      ibmweb.util.statsHelper({
        "ibmEV": "external link",
        "ibmEvAction": link.href,
        "ibmEvGroup": "SBS icons",
        "ibmEvName": "sbs-print"
      });
      javascript: print();
      return false;
    });
    var li = document.createElement("li");
    li.appendChild(a);
    return li;
  };
  ibmweb.sbs.register = function(elem, all) {
    if (!(typeof(ibmweb.dynnav.PMM) == "object" && !!ibmweb.dynnav.PMM.footerToolData)) {
      window.setTimeout(function() {
        ibmweb.sbs.register(elem, all);
      }, 50);
      return;
    }
    if (ibmweb.dynnav.PMM.footerToolData.length == 0) {
      return false;
    }
    ibmweb.sbs.init({
      "cc": ibmweb.meta.cc,
      "lc": ibmweb.meta.lc,
      "encoding": ibmweb.meta.encoding,
      "loadManually": true,
      "element": null
    });
    if (!elem) {
      elem = ".ibm-share-this";
    }
    elem = dojo.query(elem);
    elem.forEach(function(node) {
      ibmweb.sbs.render(ibmweb.dynnav.PMM.footerToolData, node, all);
    });
  };
  dojo.addOnLoad(ibmweb.sbs.register);
}
if (!dojo._hasResource["ibmweb.dynnav.footer"]) {
  dojo._hasResource["ibmweb.dynnav.footer"] = true;
  dojo.provide("ibmweb.dynnav.footer");
  dojo.declare("ibmweb.dynnav.footer", ibmweb.dynnav._module, {
    init: function() {
      return ibmweb.dynnav.isServiceEnabled("footer");
    },
    onData: function(data) {
      var ul = dojo.query("#ibm-footer ul");
      if (ul.length == 0) {
        return;
      }
      ul = ul[0];
      dojo.empty(ul);
      dojo.forEach(data.footerLinkData, function(item) {
        if (ibmweb.config.config == "w3" && item.link == "http://w3.ibm.com/feedback/" && ibmweb.config.footer.feedbackLink != "") {
          item.link = ibmweb.config.footer.feedbackLink;
        }
        dojo.place("<li><a href=\"" + item.link + "\">" + item.name + "</li>", ul);
      });
      dojo.publish("/ibmweb/dynnav/footer/finished");
    }
  });
}
if (!dojo._hasResource["ibmweb.dynnav.footermenu"]) {
  dojo._hasResource["ibmweb.dynnav.footermenu"] = true;
  dojo.provide("ibmweb.dynnav.footermenu");
  dojo.declare("ibmweb.dynnav.footermenu", ibmweb.dynnav._module, {
    init: function() {
      return ibmweb.dynnav.isServiceEnabled("footermenu") && !ibmweb.config.appmast.enabled;
    },
    onLoad: function() {
      dojo.empty("ibm-footer-module");
      var div = dojo.create("div", {}, "ibm-footer-module");
      dojo.style(div, {
        textAlign: "center"
      });
      dojo.create("img", {
        src: ibmweb.config.imageUrl + "t/loadingAnimation.gif"
      }, div);
      dojo.style("ibm-footer-module", "minHeight", "0px");
    },
    onData: function(_375) {
      var data = _375.footerMenuData;
      if (!data || data.length == 0 || data[0].title == null) {
        this.onError();
        return;
      }
      if (!!ibmweb.dynnav.PMM.accessibilityData) {
        var h2 = dojo.create("h2", {
          id: "ibm-footer-res",
          className: "ibm-access",
          innerHTML: _375.accessibilityData.resources
        });
      }
      var div = dojo.create("div", {
        className: "ibm-columns"
      });
      var _376 = ibmweb.dynnav.PMM.accessibilityData.resources;
      for (var i = 0, j = data.length; i < j; i++) {
        if (data[i].title === null || data[i].child === null) {
          continue;
        }
        var col = dojo.create("div", {
          className: "ibm-col-6-1"
        }, div);
        var ul, h3;
        if (data[i].title.htmlspecialchars().trim() == "") {
          _376 = _376;
          h3 = dojo.create("h3", {
            className: "ibm-access",
            innerHTML: _376,
            style: {
              display: "none"
            }
          }, col);
          ul = dojo.create("ul", {
            style: {
              marginTop: (ibmweb.config.config == "www") ? "39px" : "41px"
            }
          }, col);
        } else {
          _376 = data[i].title.htmlspecialchars();
          h3 = dojo.create("h3", {
            innerHTML: _376
          }, col);
          ul = dojo.create("ul", {}, col);
        }
        dijit.setWaiState(col, "label", h3.innerHTML);
        for (var k = 0, l = data[i].child.length; k < l; k++) {
          var _377 = data[i].child[k];
          var li = dojo.create("li", {}, ul);
          var a = dojo.create("a", {
            innerHTML: _377.title.htmlspecialchars(),
            href: _377.url,
            tabIndex: "0"
          }, li);
          dojo.connect(a, "onfocus", this, function(_378) {
            dojo.addClass(_378.target, "ibm-active");
            dojo.addClass(_378.target.parentNode, "ibm-active");
          });
          dojo.connect(a, "onblur", this, function(_379) {
            dojo.removeClass(_379.target, "ibm-active");
            dojo.removeClass(_379.target.parentNode, "ibm-active");
          });
        }
        ul.firstChild.firstChild.tabIndex = 0;
      }
      dojo.empty("ibm-footer-module");
      dojo.style("ibm-footer-module", "minHeight", "150px");
      dojo.addClass("ibm-footer-module", "ibm-active");
      if (h2) {
        dojo.place(h2, "ibm-footer-module", "first");
        dijit.setWaiState(dojo.byId("ibm-footer-module"), "labelledby", h2.id);
      }
      dojo.place(div, "ibm-footer-module", "last");
      dojo.publish("/ibmweb/dynnav/footermenu/finished");
    },
    onError: function() {
      dojo.empty("ibm-footer-module");
    }
  });
}
if (!dojo._hasResource["ibmweb.dynnav.search"]) {
  dojo._hasResource["ibmweb.dynnav.search"] = true;
  dojo.provide("ibmweb.dynnav.search");
  dojo.declare("ibmweb.dynnav.search", [ibmweb.dynnav._module], {
    _original_search_text: "",
    q: null,
    hasFocus: false,
    valueBackup: "",
    hoverTrick: false,
    init: function() {
      if (dojo.byId("ibm-search-module")) {
        return true;
      }
      return false;
    },
    onLoad: function() {
      this.q = dojo.byId("q");
      if (this.q && ibmweb.config.config == "www") {
        this.q.value = dojo.query("label[for=q] span")[0].innerHTML;
        this._original_search_text = this.q.value;
        dojo.connect(this.q, "onfocus", dojo.hitch(this, function() {
          this.hasFocus = true;
          if (this.q.value == this._original_search_text) {
            this.q.value = "";
          }
          dojo.removeAttr(this.q, "title");
          dojo.style(this.q, "fontStyle", "normal");
        }));
        dojo.connect(this.q, "onblur", dojo.hitch(this, function() {
          this.hasFocus = false;
          this.q.value = this.q.value.trim() || this.valueBackup;
          dojo.style(this.q, "fontStyle", (this.q.value == this._original_search_text) ? "italic" : "normal");
        }));
        dojo.connect(this.q, "onmouseover", dojo.hitch(this, function() {
          if (this.hasFocus) {
            return;
          }
          if (this.q.value !== this._original_search_text) {
            return;
          }
          this.hoverTrick = true;
          this.valueBackup = this.q.value;
          dojo.attr(this.q, "title", this.valueBackup);
          this.q.value = "";
        }));
        dojo.connect(this.q, "onmouseout", dojo.hitch(this, function() {
          if (this.hasFocus || !this.hoverTrick) {
            return;
          }
          this.hoverTrick = false;
          dojo.removeAttr(this.q, "title");
          if (this.q.value == "") {
            this.q.value = this.valueBackup;
          }
        }));
      }
      if (ibmweb.config.config == "www") {
        this._initScopedSearch();
      }
    },
    onData: function(data) {
      if (ibmweb.config.config == "w3" && ibmweb.dynnav.isServiceEnabled("facestypeahead")) {
        dojo.attr(this.q, "autocomplete", "off");
        ibmweb.queue.push(function() {
          return !!window.ibmweb.w3.SearchBar;
        }, function() {
          new window.ibmweb.w3.SearchBar({
            form: "ibm-search-form"
          }, "q");
        });
      }
    },
    _initScopedSearch: function() {
      var sn = dojo.byId("sn");
      if (!sn) {
        return;
      }
      var _37a = null;
      var key = null;
      var _37b = dojo.byId("ibm-search");
      dojo.addClass("ibm-search", "ibm-with-scoped-search");
      dojo.addClass(sn, "ibm-access");
      dojo.attr(sn, "tabIndex", "-1");
      var ul = dojo.create("ul", {
        id: "ibm-scoped-search",
        onmouseover: function() {
          if (_37a) {
            clearTimeout(_37a);
          }
        },
        onmouseout: function() {
          clearTimeout(_37a);
          _37a = setTimeout(function() {
            dojo.style(ul, "display", "none");
          }, 500);
        }
      }, sn.parentNode);
      dojo.style(ul, "display", "none");
      dijit.setWaiRole(ul, "menu");
      var _37c = dojo.query("#sn > option");
      for (var i = 0, j = _37c.length; i < j; i++) {
        var li = dojo.create("li", {}, ul);
        var a = dojo.create("a", {
          className: _37c[i].selected ? "ibm-selected" : "",
          rel: _37c[i].value,
          innerHTML: _37c[i].innerHTML,
          href: "#"
        }, li);
        dijit.setWaiRole(a, "menuitem");
        _37c[i].selected ? dijit.setWaiState(a, "checked", "true") : dijit.setWaiState(a, "checked", "false");
        dojo.connect(a, "onclick", dojo.hitch(this, function(_37d) {
          dojo.stopEvent(_37d);
          var link = _37d.target;
          sn.value = link.rel;
          dojo.query(".ibm-selected", ul).forEach(function(item) {
            dijit.setWaiState(item, "checked", "false");
          });
          dojo.query(".ibm-selected", ul).removeClass("ibm-selected");
          dojo.query(".ibm-active", ul).removeClass("ibm-active");
          dojo.addClass(link, "ibm-selected");
          dijit.setWaiState(link, "checked", "true");
          if (this.q.value == this._original_search_text) {
            this.q.value = link.innerHTML;
          }
          this._original_search_text = link.innerHTML;
          dojo.style(ul, "display", "none");
          _37b.focus();
        }));
        dojo.connect(a, "onblur", dojo.hitch(this, function(_37e) {
          _37a = setTimeout(function() {
            dojo.removeClass(_37e.target, "ibm-active");
            dojo.style(ul, "display", "none");
          }, 500);
        }));
        dojo.connect(a, "onfocus", dojo.hitch(this, function(_37f) {
          clearTimeout(_37a);
          dojo.addClass(_37f.target, "ibm-active");
          dojo.style(ul, "display", "block");
        }));
        dojo.connect(a, "onmouseover", dojo.hitch(this, function(_380) {
          dojo.query("a.ibm-active", ul).removeClass("ibm-active");
          dojo.addClass(_380.target, "ibm-active");
        }));
        dojo.connect(a, "onmouseout", dojo.hitch(this, function(_381) {
          dojo.removeClass(_381.target, "ibm-active");
          _37a = setTimeout(function() {}, 100);
        }));
        if (_37c[i].selected) {
          this.q.value = this._original_search_text = _37c[i].innerHTML;
        }
      }
      dojo.connect(_37b, "onmouseover", dojo.hitch(this, function() {
        dojo.style(ul, "display", "block");
        if (_37a) {
          clearTimeout(_37a);
        }
      }));
      dojo.connect(_37b, "onmouseout", dojo.hitch(this, function() {
        _37a = setTimeout(function() {
          dojo.style(ul, "display", "none");
        }, 500);
      }));
      dojo.connect(_37b, "onfocus", dojo.hitch(this, function(_382) {
        if (key != "EE") {
          if (_37a) {
            clearTimeout(_37a);
          }
          dojo.style(ul, "display", "block");
          if (dojo.query(".ibm-selected", ul)[0]) {
            dojo.query(".ibm-selected", ul)[0].focus();
          }
        }
        key = null;
      }));
      dojo.connect(_37b, "onblur", dojo.hitch(this, function(_383) {
        _37a = setTimeout(function() {
          dojo.style(ul, "display", "none");
        }, 500);
      }));
      var _37c = dojo.query("a", ul);
      dojo.connect(ul, "onkeypress", dojo.hitch(this, function(_384) {
        if ((_384.keyCode == 0 || _384.charOrCode == " ") && _384.target.nodeName == "A") {
          dojo.stopEvent(_384);
          var link = _384.target;
          sn.value = link.rel;
          dojo.query(".ibm-selected", ul).forEach(function(item) {
            dijit.setWaiState(item, "checked", "false");
          });
          dojo.query(".ibm-selected", ul).removeClass("ibm-selected");
          dojo.query(".ibm-active", ul).removeClass("ibm-active");
          dojo.addClass(link, "ibm-selected");
          dijit.setWaiState(link, "checked", "true");
          if (this.q.value == this._original_search_text) {
            this.q.value = link.innerHTML;
          }
          this._original_search_text = link.innerHTML;
          dojo.style(ul, "display", "none");
          _37b.focus();
        }
        if (_384.keyCode == dojo.keys.ENTER || _384.keyCode == dojo.keys.ESCAPE || _384.keyCode == dojo.keys.TAB) {
          dojo.stopEvent(_384);
          key = "EE";
          _37b.focus();
          dojo.style(ul, "display", "none");
          if (_384.keyCode == dojo.keys.ENTER) {
            this.q.value = _384.target.innerHTML;
            dojo.query(".ibm-selected", ul).forEach(function(item) {
              dijit.setWaiState(item, "checked", "false");
            });
            dojo.query(".ibm-selected", ul).removeClass("ibm-selected");
            _384.target.className = "ibm-selected";
            dijit.setWaiState(_384.target, "checked", "true");
          }
        }
        if (_384.keyCode == dojo.keys.DOWN_ARROW) {
          dojo.stopEvent(_384);
          var curr = _384.target;
          for (i = 0; i < _37c.length; i++) {
            if (curr == _37c[i] && i < (_37c.length - 1)) {
              dojo.removeClass(curr, "ibm-active");
              dojo.addClass(_37c[i + 1], "ibm-active");
              sn.value = _37c[i + 1].rel;
              _37c[i + 1].focus();
            }
          }
        }
        if (_384.keyCode == dojo.keys.UP_ARROW) {
          dojo.stopEvent(_384);
          var curr = _384.target;
          for (i = 0; i < _37c.length; i++) {
            if (curr == _37c[i] && i > 0) {
              dojo.removeClass(curr, "ibm-active");
              dojo.addClass(_37c[i - 1], "ibm-active");
              sn.value = _37c[i - 1].rel;
              _37c[i - 1].focus();
            }
          }
        }
      }));
    }
  });
}
if (!dojo._hasResource["ibmweb.w3.facestypeahead"]) {
  dojo._hasResource["ibmweb.w3.facestypeahead"] = true;
  dojo.provide("ibmweb.w3.facestypeahead");
  if (window.dojo) {
    dojo.provide("dojo.NodeList-data");
    (function(d) {
      var _385 = {},
        x = 0,
        _386 = "data-dojo-dataid",
        nl = d.NodeList,
        _387 = function(node) {
          var pid = d.attr(node, _386);
          if (!pid) {
            pid = "pid" + (x++);
            d.attr(node, _386, pid);
          }
          return pid;
        };
      var _388 = d._nodeData = function(node, key, _389) {
        var pid = _387(node),
          r;
        if (!_385[pid]) {
          _385[pid] = {};
        }
        if (arguments.length == 1) {
          r = _385[pid];
        }
        if (typeof key == "string") {
          if (arguments.length > 2) {
            _385[pid][key] = _389;
          } else {
            r = _385[pid][key];
          }
        } else {
          r = d._mixin(_385[pid], key);
        }
        return r;
      };
      var _38a = d._removeNodeData = function(node, key) {
        var pid = _387(node);
        if (_385[pid]) {
          if (key) {
            delete _385[pid][key];
          } else {
            delete _385[pid];
          }
        }
      };
      d._gcNodeData = function() {
        var _38b = dojo.query("[" + _386 + "]").map(_387);
        for (var i in _385) {
          if (dojo.indexOf(_38b, i) < 0) {
            delete _385[i];
          }
        }
      };
      d.extend(nl, {
        data: nl._adaptWithCondition(_388, function(a) {
          return a.length === 0 || a.length == 1 && (typeof a[0] == "string");
        }),
        removeData: nl._adaptAsForEach(_38a)
      });
    })(dojo);
  }(function() {
    var h = window.QueryHelper = {};
    if (window.jQuery) {
      h.queryAll = jQuery;
      h.query = function(_38c, _38d) {
        return _38c.find(_38d);
      };
      h.extendObject = jQuery.extend;
      h.trimString = jQuery.trim;
      h.getValue = function(_38e) {
        return _38e.val();
      };
      h.setValue = function(_38f, str) {
        return _38f.val(str);
      };
      h.getAttr = function(_390, name) {
        return _390.attr(name);
      };
      h.setAttr = function(_391, name, _392) {
        _391.attr(name, _392);
      };
      h.fetchJSON = function(_393) {
        _393.dataType = "json";
        _393.url += "?callback=?";
        return jQuery.ajax(_393);
      };
      h.getData = function(_394, key) {
        return _394.data(key);
      };
      h.setData = function(_395, key, _396) {
        return _395.data(key, _396);
      };
      h.multiBind = function(_397, _398, func) {
        return _397.bind(_398, func);
      };
      h.isVisible = function(_399) {
        return _399.is(":visible");
      };
      h.isHidden = function(_39a) {
        return _39a.is(":hidden");
      };
      h.changeCSS = function(_39b, obj) {
        return _39b.css(obj);
      };
      h.show = function(_39c) {
        _39c.show();
      };
      h.hide = function(_39d) {
        _39d.hide();
      };
      h.setText = function(_39e, str) {
        _39e.text(str);
      };
      h.setHTML = function(_39f, str) {
        _39f.html(str);
      };
      h.hasAncestor = function(_3a0, _3a1) {
        return _3a0.parents().filter(_3a1).length > 0;
      };
      h.onDocumentReady = jQuery;
      h.map = jQuery.map;
      h.offset = function(_3a2) {
        return _3a2.offset();
      };
      h.hasClass = function(_3a3, clz) {
        return _3a3.hasClass(clz);
      };
    } else {
      if (window.dojo) {
        h.queryAll = dojo.query;
        h.query = function(_3a4, _3a5) {
          return _3a4.query(_3a5);
        };
        h.extendObject = dojo.mixin;
        h.trimString = dojo.trim;
        h.getValue = function(_3a6) {
          return _3a6.val();
        };
        h.setValue = function(_3a7, str) {
          return _3a7.val(str);
        };
        h.getAttr = function(_3a8, name) {
          return dojo.attr(_3a8[0], name);
        };
        h.setAttr = function(_3a9, name, _3aa) {
          dojo.attr(_3a9[0], name, _3aa);
        };
        h.fetchJSON = function(_3ab) {
          _3ab.url = _3ab.url + "?" + dojo.objectToQuery(_3ab.data);
          delete _3ab.data;
          _3ab.callbackParamName = "callback";
          var _3ac = _3ab.success;
          delete _3ab.success;
          _3ab.load = function(json, data) {
            return _3ac(json, undefined, data.xhr);
          };
          dojo.io.script.get(_3ab);
        };
        h.getData = function(_3ad, key) {
          return _3ad.data(key)[0];
        };
        h.setData = function(_3ae, key, _3af) {
          return _3ae.data(key, _3af);
        };
        h.multiBind = function(_3b0, _3b1, func) {
          var _3b2 = _3b1.split(" ");
          for (var i = 0, l = _3b2.length; i < l; i++) {
            dojo.connect(_3b0[0], "on" + _3b2[i], func);
          }
        };

        function _3b3(_3b4) {
          var w = _3b4.offsetWidth,
            h = _3b4.offsetHeight,
            _3b5 = (_3b4.tagName === "TR");
          return (w === 0 && h === 0 && !_3b5) ? true : (w !== 0 && h !== 0 && !_3b5) ? false : _3b4.getStyle("display") === "none";
        };
        h.isVisible = function(_3b6) {
          return !_3b3(_3b6[0]);
        };
        h.isHidden = function(_3b7) {
          return _3b3(_3b7[0]);
        };
        h.changeCSS = function(_3b8, obj) {
          return _3b8.style(obj);
        };
        var _3b9 = {};

        function _3ba(_3bb) {
          if (!_3b9[_3bb]) {
            var elem = document.createElement(_3bb),
              body = dojo.query("body");
            body.append(elem);
            var _3bc = dojo.style(elem, "display");
            body[0].removeChild(elem);
            if (_3bc === "none" || _3bc === "") {
              _3bc = "block";
            }
            _3b9[_3bb] = _3bc;
          }
          return _3b9[_3bb];
        };
        h.show = function(_3bd) {
          for (var i = 0, j = _3bd.length; i < j; i++) {
            elem = _3bd[i];
            elemInNodeList = dojo.query(elem);
            display = elem.style.display;
            if (!elemInNodeList.data("olddisplay")[0] && display === "none") {
              display = elem.style.display = "";
            }
            if (display === "" && dojo.style(elem, "display") === "none") {
              elemInNodeList.data("olddisplay", _3ba(elem.nodeName));
            }
          }
          for (i = 0; i < j; i++) {
            elem = _3bd[i];
            display = elem.style.display;
            if (display === "" || display === "none") {
              elem.style.display = elemInNodeList.data("olddisplay")[0] || "";
            }
          }
        };
        h.hide = function(_3be) {
          for (var i = 0, j = _3be.length; i < j; i++) {
            var _3bf = dojo.style(_3be[i], "display"),
              _3c0 = dojo.query(_3be[i]);
            if (_3bf !== "none") {
              _3c0.data("olddisplay", _3bf);
            }
          }
          for (i = 0; i < j; i++) {
            _3be[i].style.display = "none";
          }
        };
        h.setText = function(_3c1, str) {
          _3c1.text(str);
        };
        h.setHTML = function(_3c2, str) {
          _3c2.html(str);
        };
        h.hasAncestor = function(_3c3, _3c4) {
          return dojo.isDescendant(_3c3[0], _3c4[0]);
        };
        h.onDocumentReady = function(func) {
          dojo.addOnLoad(func);
        };
        h.map = function(arr, func) {
          var _3c5 = [],
            _3c6 = dojo.map(arr, func);
          for (var i = 0, l = _3c6.length; i < l; i++) {
            if (_3c6[i] !== undefined) {
              _3c5.push(_3c6[i]);
            }
          }
          return _3c5;
        };
        h.offset = function(_3c7) {
          var _3c8;
          if (dojo.position) {
            _3c8 = dojo.position(_3c7[0], true);
          } else {
            _3c8 = dojo.coords(_3c7[0], true);
          }
          return {
            top: _3c8.y,
            left: _3c8.x
          };
        };
        h.hasClass = function(_3c9, clz) {
          return _3c9.length > 0 && dojo.hasClass(_3c9[0], clz);
        };
      }
    }
  })();
  if (!Function.prototype.bind) {
    Function.prototype.bind = function() {
      if (arguments.length < 2 && (typeof arguments[0] == "undefined")) {
        return this;
      }
      var _3ca = this,
        args = Array.prototype.slice.call(arguments),
        _3cb = args.shift();
      return function() {
        return _3ca.apply(_3cb, args.concat(Array.prototype.slice.call(arguments)));
      };
    };
  }(function() {
    var h = window.QueryHelper,
      _3cc = "";
    if (location.protocol != "http:" && location.protocol != "https:") {
      _3cc = "http:";
    }
    var _3cd = {
      minQueryLength: 2,
      searchDelay: 100,
      xhrTimeout: 20000,
      filterQueries: function(_3ce) {
        return true;
      },
      showMoreResults: true,
      resultsAlign: "right",
      sizeToInput: false,
      topsearch: {
        enabled: false,
        host: "//topsearch.ciolab.ibm.com",
        minScore: 0.2,
        maxResults: 4,
        headerLabel: "",
        iconUrl: "//w3.ibm.com/jct03001pt/w3ODWThemeSkin/themes/html/w3ODWTheme/images/favicon.ico",
        moreResultsUrl: "//w3.ibm.com/search/do/search?qt=${query}",
        moreResultsLabel: "See more w3 results",
        url: function(host) {
          return API.buildURL("/find/", host);
        },
        extraParams: {
          "limit": 13,
          "format": "topsearch"
        },
        onclick: function(link) {
          location.href = link.url;
        }
      },
      faces: {
        enabled: true,
        ariaLiveLabel: "There are ${count} suggested search results.  Use down and up arrow keys to navigate through the results.",
        prefixDom: null,
        minScore: 0,
        maxResults: 4,
        headerLabel: "",
        moreResultsLabel: "See more Faces (${count})",
        moreResultsLabel2: "See more Faces (over ${count})",
        moreResultsUrl: "//faces.tap.ibm.com#${query}",
        host: _3cc + "//faces.tap.ibm.com",
        imageHost: _3cc + "//images.tap.ibm.com",
        largeImageSize: 50,
        smallImageSize: 32,
        url: function(host) {
          return API.buildURL("/find/", host);
        },
        extraParams: {
          "threshold": 0,
          "limit": 13,
          "format": "faces"
        },
        onclick: function(_3cf) {
          location.href = "//faces.tap.ibm.com/#uid:" + _3cf.uid + "/" + _3cf.name;
        }
      }
    };
    var _3d0 = 27;
    var me = window.FacesTypeAhead = {
      config: {},
      init: function() {
        var _3d1 = {},
          _3d2 = {},
          _3d3, a = arguments,
          _3d4 = _3d5("typeahead-results");
        if (a.length > 1) {
          _3d3 = a[0];
          _3d1 = a[1];
        } else {
          if (a.length == 1) {
            if ("innerHTML" in a[0]) {
              _3d3 = a[0];
            } else {
              _3d1 = a[0];
            }
          }
        }
        if (_3d3 && !_3d3.length) {
          _3d3 = [_3d3];
        }
        if (!_3d3) {
          throw Error("DOM node missing from init call.");
        }
        if (!_3d1.key) {
          throw Error("API Key missing from config options.");
        }
        var _3d6 = {};
        h.extendObject(_3d2, _3d1);
        for (var prop in _3cd) {
          if (prop in _3d1) {
            if (typeof _3cd[prop] == "object") {
              _3d2[prop] = h.extendObject({}, _3cd[prop], _3d1[prop]);
            }
          } else {
            _3d2[prop] = _3cd[prop];
          }
          if (typeof _3d2[prop] == "object" && _3d2[prop].enabled) {
            _3d6[prop] = true;
          }
        }
        _3d2.features = _3d6;
        var _3d7;
        var _3d8 = new Array();
        for (var i = 0; i < _3d3.length; i++) {
          _3d7 = h.queryAll(_3d3[i]).attr("autocomplete", "off");
          _3d8.push(new _3d9(_3d7, _3d2));
        }
        return _3d8;
      }
    };

    function _3da(num) {
      if (num <= 100) {
        return num + "";
      } else {
        if (num <= 150) {
          return "100+";
        } else {
          if (num <= 200) {
            return "150+";
          } else {
            var str = num + "";
            return _3db((str.charAt(0) * 1) * Math.pow(10, str.length - 1)) + "+";
          }
        }
      }
    };

    function _3db(str) {
      str = str + "";
      str = str.split("").reverse().join("");
      str = str.replace(/(\d\d\d)/g, "$1,");
      str = str.split("").reverse().join("");
      if (str.charAt(0) == ",") {
        return str.substring(1);
      } else {
        return str;
      }
    };

    function _3dc(obj) {
      var _3dd = new Array();
      for (key in obj) {
        if (typeof obj[key] != "function") {
          _3dd.push(obj[key]);
        }
      }
      return _3dd;
    };

    function _3d5(str) {
      var _3de = 0,
        id = str;
      while (document.getElementById(id)) {
        id = str + (_3de++);
      }
      return id;
    };
    var API = {
      NUM_IMAGE_SERVERS: 3,
      PAGE_SIZE: 21,
      PersonResult: function(_3df) {
        h.extendObject(this, _3df["person"]);
        this.score = _3df.score;
        for (var key in _3df) {
          if (_3df.hasOwnProperty(key) && key != "person") {
            this[key] = _3df[key];
          }
        }
      },
      fixReturnedPerson: function(_3e0) {
        return new this.PersonResult(_3e0);
      },
      hashUIDToServer: function(uid) {
        if (uid) {
          var sum = 0;
          for (var i = 0, l = uid.length; i < l; i++) {
            sum += uid.charCodeAt(i);
          }
          return sum % API.NUM_IMAGE_SERVERS;
        } else {
          return 0;
        }
      },
      buildURL: function(str, host) {
        if (typeof host != "undefined") {
          return host + "/api" + str;
        } else {
          if (/^\/faces/.test(window.location.pathname)) {
            return "/faces/api" + str;
          } else {
            return "/api" + str;
          }
        }
      },
      MIN_DELTA_PERCENT: 0.09,
      MIN_DELTA_VALUE: 75,
      MIN_SCORE: 500,
      calculateLargeDeltas: function(_3e1, _3e2, _3e3, _3e4) {
        var _3e5 = _3e1[0].score,
          _3e6 = [],
          _3e7 = [];
        _3e2 = _3e2 || API.MIN_DELTA_PERCENT;
        _3e3 = _3e3 || API.MIN_DELTA_VALUE;
        _3e4 = _3e4 || API.MIN_SCORE;
        for (var i = 0, l = _3e1.length; i < l; i++) {
          var _3e8 = _3e1[i],
            _3e9 = _3e8.score / _3e5;
          _3e6.push(_3e9);
          if (i > 0) {
            _3e7.push(_3e6[i - 1] - _3e9);
          }
        }
        var _3ea = h.map(_3e7, function(x, i) {
          if (x >= _3e2 && x * _3e5 > _3e3 && _3e1[i].score > _3e4) {
            return {
              delta: x,
              end: i + 1
            };
          }
        }.bind(this)).sort(function(a, b) {
          if (a.delta < b.delta) {
            return 1;
          } else {
            if (a.detla == b.delta) {
              return 0;
            } else {
              return -1;
            }
          }
        });
        return _3ea;
      }
    };
    var _3eb = function() {};
    var _3d9 = function(_3ec, _3ed) {
      var _3ee, _3ef, _3f0 = "selected",
        _3f1, _3f2, _3f3 = {},
        _3f4, _3f5 = 0,
        _3f6 = {},
        _3f7 = {},
        _3f8, _3f9 = {},
        _3fa = _3d5("typeahead-results"),
        KEY = {
          BACKSPACE: 8,
          RETURN: 13,
          SPACE: 32,
          LEFT: 37,
          UP: 38,
          RIGHT: 39,
          DOWN: 40,
          DELETE: 46,
          COMMA: 188
        },
        _3fb = {
          flushCache: function() {
            for (var c in _3f9) {
              if (_3f9.hasOwnProperty(c)) {
                _3f9[c].flush();
              }
            }
          },
          execute: function(_3fc, _3fd) {
            _3ed.extraOptions = _3fd || {};
            _3fe(_3ff(), _3fc);
          }
        };
      for (var f in _3ed.features) {
        _3f9[f] = new _3d9.Cache();
      }
      this.inputBox = _3ec.attr("role", "combobox").attr("aria-autocomplete", "list").attr("aria-expanded", "false").attr("aria-owns", _3fa);
      this.settings = _3ed;
      this.container = h.queryAll(document.createElement("div")).addClass("typeahead-results").attr("id", _3fa).attr("role", "listbox");
      h.changeCSS(this.container, {
        display: "none"
      });
      document.body.appendChild(this.container[0]);
      var _400 = document.createElement("div");
      _400.style.display = "none";
      _400.setAttribute("aria-live", "polite");
      this.container.append(_400);
      if (_3ed.features.faces && _3ed.faces.prefixDom) {
        this.container.append(_3ed.faces.prefixDom);
      }
      this.resultLists = {};
      this.resultHeaders = {};
      for (var f in _3ed.features) {
        if (_3ed[f].headerLabel) {
          var _401 = h.queryAll(document.createElement("h2"));
          h.setText(_401, _3ed[f].headerLabel);
          h.setAttr(_401, "id", "ibm_" + _3ed[f].headerLabel.toLowerCase().replace(" ", "_"));
          h.hide(_401);
          this.resultHeaders[f] = _401.appendTo(this.container);
        }
        var ul = document.createElement("ul");
        h.queryAll(document.createElement("li")).attr("role", "option").attr("aria-selected", "false").attr("tabIndex", "-1").appendTo(ul);
        ul = h.queryAll(ul).attr("role", "group").attr("aria-labelledby", "ibm_" + _3ed[f].headerLabel.toLowerCase().replace(" ", "_"));
        this.resultLists[f] = ul.addClass(f + "-typeahead-results").appendTo(this.container);
      }
      this.isPopupShowing = function() {
        return !h.isHidden(this.container);
      };
      var _402 = function(_403, _404, _405, _406, _407) {
        _405 = _405 || {};
        if (!_403) {
          this.hideResults();
          return;
        }
        if (_404 == latestQuery) {
          try {
            _408(_403, _405, _404, _406);
          } catch (e) {
            throw e;
          }
        } else {}
      }.bind(this);
      var _409 = function() {}.bind(this);
      var _40a = function(_40b) {
        latestQuery = _40b;
      }.bind(this);
      var _40c = function(_40d) {
        latestQuery = _40d;
      }.bind(this);
      var _40e = function() {
        if (!this.isPopupShowing()) {
          _40f();
        }
      }.bind(this);
      var _410 = function() {
        setTimeout(function() {
          var _411 = _3ff();
          if (_426(_411)) {
            clearTimeout(_3ee);
            _3ee = setTimeout(function() {
              _3fe(_411);
            }, _3ed.searchDelay);
          }
        }, 0);
      };
      h.setData(_3ec, "typeahead", _3fb);
      h.multiBind(_3ec, "keyup keydown", function(_412) {
        var _413, _414, _415, i;
        if (_412.type == "keyup" && (_412.keyCode == KEY.BACKSPACE || _412.keyCode == KEY.DELETE)) {
          _410();
        } else {
          if (_412.type == "keydown") {
            switch (_412.keyCode) {
              case KEY.BACKSPACE:
              case KEY.DELETE:
                break;
              case KEY.RETURN:
                if (this.isPopupShowing()) {
                  var curr = _416();
                  if (curr) {
                    _412.preventDefault();
                    curr[0].onclick();
                  }
                }
                break;
              case KEY.UP:
                if (this.isPopupShowing()) {
                  _412.preventDefault();
                }
                _417();
                break;
              case KEY.DOWN:
                if (this.isPopupShowing()) {
                  _418();
                } else {
                  if (_3ec.val().length) {
                    _410();
                  }
                }
                _412.preventDefault();
                break;
              default:
                if (_419(_412.keyCode) || _412.keyCode == KEY.SPACE) {
                  _410();
                }
                break;
            }
          }
        }
      }.bind(this));
      h.multiBind(this.container, "mouseover", function(_41a) {
        var t = _41a.target;
        while (t != null && t != this.container && t.className.indexOf("result") == -1) {
          t = t.parentNode;
        }
        if (t && t.nodeName.toLowerCase() == "li" && t.parentNode.className.indexOf("-typeahead-results") > -1) {
          var _41b;
          if (t.id && (_41b = h.getAttr(_3ec, "aria-activedescendant")) != t.id) {
            var old;
            if (_41b) {
              old = h.queryAll("#" + _41b);
            }
            _41c(old, h.queryAll(t));
          }
        }
      }.bind(this));

      function _416() {
        var attr = h.getAttr(_3ec, "aria-activedescendant"),
          curr;
        if (attr) {
          curr = h.queryAll("#" + attr);
          if (curr.length > 0) {
            return curr;
          }
        }
        return null;
      };

      function _41d(_41e) {
        return _41f(_41e, "previousSibling");
      };

      function _420(_421) {
        return _41f(_421, "nextSibling");
      };

      function _41f(_422, prop) {
        if (_422) {
          if (_422.length) {
            _422 = _422[0];
          }
          if (_422[prop]) {
            return h.queryAll(_422[prop]);
          }
          var _423, list = _422.parentNode;
          while ((list = list[prop]) && list.nodeName.toUpperCase() != "UL" && list.style.display != "none") {}
          if (list && list.nodeName.toUpperCase() == "UL" && list.style.display != "none") {
            _423 = list.childNodes;
            if (_423.length > 0) {
              return h.queryAll(_423[prop == "nextSibling" ? 0 : _423.length - 1]);
            }
          }
        }
        return null;
      };
      var _418 = function() {
        if (this.isPopupShowing()) {
          var curr = _416(),
            next;
          if (next = _420(curr)) {
            _41c(curr, next);
          } else {
            if (curr == null) {
              var $ul = h.query(this.container, "ul:not(ul:empty)");
              for (var i = 0; i < $ul.length; i++) {
                if ($ul[i].style.display != "none") {
                  _41c(null, h.query(h.queryAll($ul[i]), "li:first-child"));
                  break;
                }
              }
            }
          }
        }
      }.bind(this);
      var _417 = function() {
        if (this.isPopupShowing()) {
          var curr = _416(),
            prev;
          if (prev = _41d(curr)) {
            _41c(curr, prev);
          } else {
            if (curr) {
              this.hideResults();
            }
          }
        }
      }.bind(this);

      function _41c(_424, _425) {
        if (_424) {
          _424.removeClass(_3f0);
          h.setAttr(_424, "aria-selected", "false");
          h.setAttr(_424, "tabIndex", "-1");
        }
        if (_425 && _425.length > 0) {
          _425.addClass(_3f0);
          h.setAttr(_425, "aria-selected", "true");
          h.setAttr(_425, "tabIndex", "0");
          h.setAttr(_3ec, "aria-activedescendant", _425[0].id);
        } else {
          h.setAttr(_3ec, "aria-activedescendant", "");
        }
      };

      function _3ff() {
        return h.trimString(h.getValue(_3ec).toLowerCase()).replace(/\s+/g, " ");
      };

      function _426(_427) {
        return !_427 || _3ed.filterQueries(_427);
      };

      function _419(_428) {
        if ((_428 >= 48 && _428 <= 90) || (_428 >= 96 && _428 <= 111) || (_428 >= 186 && _428 <= 192) || (_428 >= 219 && _428 <= 222)) {
          return true;
        }
      };

      function _429(_42a, _42b, _42c, _42d) {
        var _42e = _402(_42a, _42b, _42c || [], _42d, _3ed);
        _3ed.extraOptions = null;
        return _42e;
      };

      function _42f() {
        for (var k in _3f6) {
          _3f7[k] = true;
          _3f6[k].abort();
        }
        _3f6 = {};
      };

      function _430(_431, _432) {
        if (_3f2 == _431 && _3f3.idOffset == _432.idOffset && _3f3.scoreOffset == _432.scoreOffset) {
          return true;
        } else {
          _3f2 = _431;
          _3f3 = _432;
          return false;
        }
      };
      var _3fe = function(_433, _434, _435) {
        if (this.disabled) {
          return;
        }
        _434 = _434 || {};
        if (!_430(_433, _434)) {
          _42f();
          if (_433 && _433.replace(/\s/g, "").length >= _3ed.minQueryLength) {
            setTimeout(function() {
              for (var f in _3ed.features) {
                var _436 = _3f9[f].get(_433),
                  data = {
                    "q": _433,
                    key: _3ed.key
                  },
                  _437 = false;
                if (_434 && _434.idOffset && _434.scoreOffset) {
                  data.idOffset = _434.idOffset;
                  data.scoreOffset = _434.scoreOffset;
                  _437 = true;
                  data = h.extendObject(data, _434);
                }
                if (_436 && !_437) {
                  _40c(_433);
                  _429(f, _433, _436, _434);
                } else {
                  _438(f, _433, _434, data, _437);
                }
              }
            }, (_435 || 0));
          } else {
            _40c(_433);
            _429(null, _433, [], _434);
          }
        } else {
          if (_433) {
            _40e();
          }
        }
      }.bind(this);

      function _438(_439, _43a, _43b, data, _43c) {
        _40a(_43a, _3f5);
        var xhr;
        var f = _439;
        xhr = h.fetchJSON({
          "url": _3ed[f].url(_3ed[f].host),
          "data": h.extendObject({}, _3ed[_439].extraParams, data),
          "timeout": _3ed.xhrTimeout,
          "success": function(_43d) {
            return function(_43e, _43f, xhr) {
              if ((!xhr || xhr.status == 200) && !_3f7[_43d]) {
                _3f1 = _43e;
                if (!_43c) {
                  _3f9[f].add(_43a, _43e);
                }
                _429(f, _43a, _43e, _43b);
              }
              delete _3f7[_43d];
              delete _3f6[_43d];
            };
          }(_3f5),
          "error": function(_440) {
            return function() {
              _3ed.extraOptions = null;
              if (_3ed.error) {
                _3ed.error.apply(this, arguments);
              }
              delete _3f7[_440];
              delete _3f6[_440];
            };
          }(_3f5)
        });
        if (xhr) {
          _3f6[_3f5++] = xhr;
        }
      };
      this.disable = function() {
        if (arguments.length > 0) {
          if (this.settings.features[arguments[0]]) {
            this.settings.features[arguments[0]] = false;
          }
        } else {
          this.disabled = true;
        }
      };
      this.enable = function() {
        if (arguments.length > 0) {
          this.settings.features[arguments[0]] = true;
        } else {
          this.disabled = false;
        }
      };
      var _408 = function(_441, _442, _443) {
        var _444 = [],
          _445, _446 = document.createDocumentFragment(),
          _447, c = _3ed;
        if (!_3ed.features.faces) {
          h.hide(this.resultLists.faces);
          if (this.resultHeaders.faces) {
            h.hide(this.resultHeaders.faces);
          }
        }
        if (!_3ed.features.topsearch) {
          h.hide(this.resultLists.topsearch);
          if (this.resultHeaders.topsearch) {
            h.hide(this.resultHeaders.topsearch);
          }
        }
        if (_3ed.features.faces && _441 == "faces") {
          _445 = _442.persons ? _442.persons.length : 0;
          for (var i = 0; i < _445; i++) {
            _444.push(API.fixReturnedPerson(_442.persons[i]));
          }
          _447 = _448(_444);
          if (_447 * 2 + _445 - _447 >= 12) {
            _444.splice(12 - _447);
          }
          if (c.faces.minScore && _444.length > 0 && _444[0].score < c.faces.minScore) {
            _444 = [];
          }
          if (c.faces.minScore && _447 > 0 && _444[_447] && _444[_447].score < c.faces.minScore) {
            _444 = _444.slice(0, _447);
          }
          if (c.faces.maxResults) {
            if (c.faces.maxResults % 2 == 0 && _447 > 0) {
              _444 = _444.slice(0, c.faces.maxResults - 1);
            } else {
              _444 = _444.slice(0, c.faces.maxResults);
            }
          }
          if (_444.length) {
            var _449;
            if (c.showMoreResults) {
              _449 = _44a(_442, _444.length, _443);
            }
            for (var i = 0, l = _444.length; i < l; i++) {
              var _44b = _444[i],
                _44c = document.createElement("li"),
                _44d;
              h.queryAll(_44c).attr("role", "option").attr("aria-selected", "false").attr("tabIndex", "-1");
              if (i < _447) {
                _44d = "large-result";
                _44c.className = "result large-result";
              } else {
                _44d = "small-result";
                _44c.className = "result small-result";
              }
              _44c.appendChild(_44e(_44b, _44d));
              _44c.onclick = (function(_44f, that) {
                return function(e) {
                  var _450 = c.faces.onclick(_44f);
                  if (typeof _450 == "string") {
                    h.setValue(_3ec, _450);
                    this.hideResults();
                    _3ec[0].focus();
                  }
                }.bind(that);
              })(_44b, this);
              _44c.id = _3d5("faces-result-" + i);
              _446.appendChild(_44c);
              if (i >= _444.length - 1 && (_447 + i) % 2 == 1) {
                _44c.className += " bottom-right";
              } else {
                if (i >= _444.length - 2 && (_447 + i) % 2 == 0) {
                  _44c.className += " bottom-left";
                }
              }
            }
            if (c.showMoreResults && _449) {
              _449.id = _3d5("faces-more-results");
              _446.appendChild(_449);
            }
            h.setHTML(this.resultLists.faces, _446);
            h.show(this.resultLists.faces);
            if (this.resultHeaders.faces) {
              h.show(this.resultHeaders.faces);
            }
          } else {
            h.hide(this.resultLists.faces);
            if (this.resultHeaders.faces) {
              h.hide(this.resultHeaders.faces);
            }
          }
        }
        var _451 = [];
        if (_3ed.features.topsearch && _441 == "topsearch") {
          _442 = _442.pages;
          _445 = _442.length;
          if (_445 > 0) {
            var _452 = document.createDocumentFragment();
            for (var i = 0; i < Math.min(_442.length, _3ed.topsearch.maxResults); i++) {
              var _453 = _442[i].page,
                link = document.createElement("a"),
                li = document.createElement("li"),
                img = document.createElement("img"),
                div = document.createElement("div");
              h.queryAll(li).attr("role", "option").attr("aria-selected", "false").attr("tabIndex", "-1");
              link.href = _453.url;
              img.src = _3ed.topsearch.iconUrl;
              img.className = "icon";
              link.appendChild(img);
              div.innerHTML = "<span class=\"query\">" + _453.title + "</span>" + "<br/><span class=\"url\">" + _453.url + "</span>";
              link.appendChild(div);
              li.appendChild(link);
              li.id = _3d5("topqueries-result-" + i);
              li.className = "result";
              _452.appendChild(li);
            }
            var _449 = document.createElement("li");
            h.queryAll(_449).attr("role", "option").attr("aria-selected", "false").attr("tabIndex", "-1");
            _449.className = "more-results";
            _449.innerHTML = "<a href=\"" + _3ed.topsearch.moreResultsUrl.replace("${query}", encodeURIComponent(_443)) + "\">" + _3ed.topsearch.moreResultsLabel + "</a>";
            _449.id = _3d5("topqueries-more-results");
            _449.onclick = function() {
              location.href = _3ed.topsearch.moreResultsUrl.replace("${query}", encodeURIComponent(_443));
            };
            _452.appendChild(_449);
            h.setHTML(this.resultLists.topsearch, _452);
            h.show(this.resultLists.topsearch);
            if (this.resultHeaders.topsearch) {
              h.show(this.resultHeaders.topsearch);
            }
          } else {
            h.hide(this.resultLists.topsearch);
            if (this.resultHeaders.topsearch) {
              h.hide(this.resultHeaders.topsearch);
            }
          }
        }
        if (_445 == 0) {
          this.hideResults(_441);
          var _454 = false;
          for (var f in this.resultLists) {
            if (this.resultLists[f].length > 0 && this.resultLists[f][0].style.display != "none") {
              _454 = true;
              break;
            }
          }
          if (!_454) {
            this.hideResults();
          }
        } else {
          _40f();
        }
      }.bind(this);
      var _44e = function(user, size) {
        var _455 = document.createDocumentFragment(),
          img = document.createElement("img"),
          info = document.createElement("div"),
          name = document.createElement("span"),
          meta = document.createElement("span"),
          _456 = document.createElement("span"),
          bio = document.createElement("span"),
          _457 = document.createElement("span"),
          _458;
        if (size == "large-result") {
          _458 = _3ed.faces.largeImageSize;
        } else {
          _458 = _3ed.faces.smallImageSize;
        }
        var port = 10000 + API.hashUIDToServer(user.uid);
        if (location.protocol == "https:" || _3ed.faces.imageHost.indexOf("https:") == 0) {
          port += 10000;
        }
        img.src = [_3ed.faces.imageHost, ":", port, "/image/", user.uid, ".jpg?s=", _458].join("");
        img.setAttribute("alt", user.name);
        _455.appendChild(img);
        name.appendChild(document.createTextNode(user.name));
        name.className = "name";
        info.appendChild(name);
        info.appendChild(document.createElement("br"));
        meta.className = "meta";
        info.appendChild(meta);
        if (user["office-phone"] || user["mobile-phone"]) {
          _457.className = "tel";
          if (user["office-phone"]) {
            _457.innerHTML += ["<span class=\"inner-label\" style=\"display: none;\"><span class=\"type\">Office</span>: </span>", "<span class=\"office-phone value\">", user["office-phone"], "</span>"].join("");
          }
          if (user["mobile-phone"]) {
            _457.innerHTML += ["<span class=\"inner-label\"><span class=\"type\" style=\"display: none;\">Mobile</span>M: </span>", "<span class=\"mobile-phone value\">", user["mobile-phone"], "</span>"].join("");
          }
          _457.appendChild(document.createElement("br"));
          meta.appendChild(_457);
        }
        _456.appendChild(document.createTextNode(user.email));
        _456.className = "email";
        meta.appendChild(_456);
        meta.appendChild(document.createElement("br"));
        if (user.bio) {
          bio.className = "bio";
          bio.appendChild(document.createTextNode(user.bio));
          meta.appendChild(bio);
        }
        _455.appendChild(info);
        return _455;
      }.bind(this);
      var _44a = function(_459, _45a, _45b) {
        var _45c = _459["matches"],
          _45d = _459["network-only"];
        if (_45c > _45a) {
          var _45e = _3da(_45c),
            _45f;
          if (_45e.charAt(_45e.length - 1) == "+") {
            _45f = _3ed.faces.moreResultsLabel2.replace("${count}", _45e.substring(0, _45e.length - 1));
          } else {
            _45f = _3ed.faces.moreResultsLabel.replace("${count}", _45e);
          }
          var li = document.createElement("li");
          h.queryAll(li).attr("role", "option").attr("aria-selected", "false").attr("tabIndex", "-1");
          li.className = "more-results";
          li.innerHTML = "<a href=\"" + _3ed.faces.moreResultsUrl.replace("${query}", encodeURIComponent(_45b)) + "\">" + _45f + "</a>";
          li.onclick = function() {
            location.href = _3ed.faces.moreResultsUrl.replace("${query}", encodeURIComponent(_45b));
          };
          return li;
        } else {
          return null;
        }
      }.bind(this);
      var _448 = function(_460) {
        if (_460.length > 3) {
          var _461 = API.calculateLargeDeltas(_460);
          for (var i = 0, l = _461.length; i < l; i++) {
            var d = _461[i];
            if (d.end == 1 || d.end == 2) {
              return d.end;
            }
          }
          return 0;
        } else {
          return _460.length;
        }
      }.bind(this);
      var _462 = function() {
        var _463 = h.offset(_3ec);
        if (window.jQuery) {
          var _464 = _3ec.outerHeight(),
            _465 = _3ec.outerWidth();
        } else {
          if (window.dojo) {
            var _466 = dojo.marginBox(_3ec[0]),
              _464 = _466.h,
              _465 = _466.w;
          }
        }
        var _467 = !this.isPopupShowing();
        if (_467) {
          h.changeCSS(this.container, {
            "visibility": "hidden"
          });
          h.show(this.container);
        }
        if (window.jQuery) {
          var _468 = this.container.innerWidth(),
            _469 = this.container.outerWidth();
        } else {
          var _469 = dojo.marginBox(this.container[0]).w;
          var _468 = dojo.contentBox(this.container[0]).w;
        }
        h.changeCSS(this.container, {
          "width": "0px"
        });
        if (window.jQuery) {
          var _46a = this.container.outerWidth();
        } else {
          var _46a = dojo.marginBox(this.container[0]).w;
        }
        h.changeCSS(this.container, {
          "width": ""
        });
        if (_467) {
          h.hide(this.container);
          h.changeCSS(this.container, {
            "visibility": ""
          });
        }
        var _46b = _469 - _468;
        var _46c = {
          top: _463.top + _464 + "px"
        };
        if (_3ed.resultsAlign == "left") {
          _46c.left = _463.left + "px";
        } else {
          if (_3ed.sizeToInput) {
            _46c.left = _463.left - Math.max(_46a, _465) + _465 + "px";
          } else {
            _46c.left = _463.left - _469 + _465 + "px";
          }
        }
        if (_3ed.sizeToInput) {
          _46c.width = _465 - _46b + "px";
        }
        return _46c;
      }.bind(this);
      var _40f = function() {
        _3ef = _462();
        if (!this.isPopupShowing()) {
          _46d();
        }
        h.changeCSS(this.container, _3ef);
        var _46e = h.query(this.container, ".result").length;
        _400.innerHTML = _3ed.faces.ariaLiveLabel.replace("${count}", _46e);
        h.show(this.container);
        this.inputBox.attr("aria-expanded", "true");
      }.bind(this);
      var _46d = function() {
        var body = h.queryAll("body");
        if (window.jQuery) {
          body.bind("click.faces-typeahead-hide", _46f.bind(this));
          body.bind("keydown.faces-typeahead-hide", _470.bind(this));
        } else {
          if (window.dojo) {
            this.hideOnOutsideClickHandle = dojo.connect(body[0], "onclick", _46f.bind(this));
            this.hideOnKeydownHandle = dojo.connect(body[0], "onkeydown", _470.bind(this));
          }
        }
      }.bind(this);
      var _46f = function(_471) {
        if (_471.target != _3ec[0] && !h.hasAncestor(h.queryAll(_471.target), this.container)) {
          this.hideResults();
        }
      }.bind(this);
      var _470 = function(_472) {
        if (_472.keyCode == _3d0) {
          this.hideResults();
        }
      }.bind(this);
      this.hideResults = function(_473) {
        var _474 = true;
        if (_473) {
          var _475 = h.queryAll(".typeahead-results > ul");
          var _476 = h.queryAll("." + _473 + "-typeahead-results");
          if (_476.length == 0) {
            return;
          }
          if (_475.length > 1) {
            _474 = false;
          }
        }
        if (_474) {
          if (window.jQuery) {
            jQuery("body").unbind(".faces-typeahead-hide");
          } else {
            if (window.dojo) {
              dojo.disconnect(this.hideOnOutsideClickHandle);
              dojo.disconnect(this.hideOnKeydownHandle);
              delete this.hideOnOutsideClickHandle;
              delete this.hideOnKeydownHandle;
            }
          }
        }
        var attr = h.getAttr(_3ec, "aria-activedescendant");
        if (attr) {
          var elms = h.queryAll("#" + attr);
          h.setAttr(elms, "aria-selected", "false");
          h.setAttr(elms, "tabIndex", "-1");
          elms.removeClass("selected");
          h.setAttr(_3ec, "aria-activedescendant", "");
        }
        if (_474) {
          h.hide(this.container);
          this.inputBox.attr("aria-expanded", "false");
        }
      }.bind(this);
    };
    _3d9.Cache = function(_477) {
      var data = {},
        size = 0,
        _478 = h.extendObject({
          MAX_SIZE: 10
        }, _477);
      this.flush = function() {
        data = {};
        size = 0;
      };
      this.add = function(_479, _47a) {
        if (size > _478.MAX_SIZE) {
          this.flush();
        }
        if (!data[_479]) {
          size++;
        }
        data[_479] = _47a;
      };
      this.get = function(_47b) {
        return data[_47b];
      };
    };
  })();
}
if (!dojo._hasResource["dijit._CssStateMixin"]) {
  dojo._hasResource["dijit._CssStateMixin"] = true;
  dojo.provide("dijit._CssStateMixin");
  dojo.declare("dijit._CssStateMixin", [], {
    cssStateNodes: {},
    hovering: false,
    active: false,
    _applyAttributes: function() {
      this.inherited(arguments);
      dojo.forEach(["onmouseenter", "onmouseleave", "onmousedown"], function(e) {
        this.connect(this.domNode, e, "_cssMouseEvent");
      }, this);
      dojo.forEach(["disabled", "readOnly", "checked", "selected", "focused", "state", "hovering", "active"], function(attr) {
        this.watch(attr, dojo.hitch(this, "_setStateClass"));
      }, this);
      for (var ap in this.cssStateNodes) {
        this._trackMouseState(this[ap], this.cssStateNodes[ap]);
      }
      this._setStateClass();
    },
    _cssMouseEvent: function(_47c) {
      if (!this.disabled) {
        switch (_47c.type) {
          case "mouseenter":
          case "mouseover":
            this._set("hovering", true);
            this._set("active", this._mouseDown);
            break;
          case "mouseleave":
          case "mouseout":
            this._set("hovering", false);
            this._set("active", false);
            break;
          case "mousedown":
            this._set("active", true);
            this._mouseDown = true;
            var _47d = this.connect(dojo.body(), "onmouseup", function() {
              this._mouseDown = false;
              this._set("active", false);
              this.disconnect(_47d);
            });
            break;
        }
      }
    },
    _setStateClass: function() {
      var _47e = this.baseClass.split(" ");

      function _47f(_480) {
        _47e = _47e.concat(dojo.map(_47e, function(c) {
          return c + _480;
        }), "dijit" + _480);
      };
      if (!this.isLeftToRight()) {
        _47f("Rtl");
      }
      if (this.checked) {
        _47f("Checked");
      }
      if (this.state) {
        _47f(this.state);
      }
      if (this.selected) {
        _47f("Selected");
      }
      if (this.disabled) {
        _47f("Disabled");
      } else {
        if (this.readOnly) {
          _47f("ReadOnly");
        } else {
          if (this.active) {
            _47f("Active");
          } else {
            if (this.hovering) {
              _47f("Hover");
            }
          }
        }
      }
      if (this._focused) {
        _47f("Focused");
      }
      var tn = this.stateNode || this.domNode,
        _481 = {};
      dojo.forEach(tn.className.split(" "), function(c) {
        _481[c] = true;
      });
      if ("_stateClasses" in this) {
        dojo.forEach(this._stateClasses, function(c) {
          delete _481[c];
        });
      }
      dojo.forEach(_47e, function(c) {
        _481[c] = true;
      });
      var _482 = [];
      for (var c in _481) {
        _482.push(c);
      }
      tn.className = _482.join(" ");
      this._stateClasses = _47e;
    },
    _trackMouseState: function(node, _483) {
      var _484 = false,
        _485 = false,
        _486 = false;
      var self = this,
        cn = dojo.hitch(this, "connect", node);

      function _487() {
        var _488 = ("disabled" in self && self.disabled) || ("readonly" in self && self.readonly);
        dojo.toggleClass(node, _483 + "Hover", _484 && !_485 && !_488);
        dojo.toggleClass(node, _483 + "Active", _485 && !_488);
        dojo.toggleClass(node, _483 + "Focused", _486 && !_488);
      };
      cn("onmouseenter", function() {
        _484 = true;
        _487();
      });
      cn("onmouseleave", function() {
        _484 = false;
        _485 = false;
        _487();
      });
      cn("onmousedown", function() {
        _485 = true;
        _487();
      });
      cn("onmouseup", function() {
        _485 = false;
        _487();
      });
      cn("onfocus", function() {
        _486 = true;
        _487();
      });
      cn("onblur", function() {
        _486 = false;
        _487();
      });
      this.watch("disabled", _487);
      this.watch("readOnly", _487);
    }
  });
}
if (!dojo._hasResource["dijit.form._FormWidget"]) {
  dojo._hasResource["dijit.form._FormWidget"] = true;
  dojo.provide("dijit.form._FormWidget");
  dojo.declare("dijit.form._FormWidget", [dijit._Widget, dijit._Templated, dijit._CssStateMixin], {
    name: "",
    alt: "",
    value: "",
    type: "text",
    tabIndex: "0",
    disabled: false,
    intermediateChanges: false,
    scrollOnFocus: true,
    attributeMap: dojo.delegate(dijit._Widget.prototype.attributeMap, {
      value: "focusNode",
      id: "focusNode",
      tabIndex: "focusNode",
      alt: "focusNode",
      title: "focusNode"
    }),
    postMixInProperties: function() {
      this.nameAttrSetting = this.name ? ("name=\"" + this.name.replace(/'/g, "&quot;") + "\"") : "";
      this.inherited(arguments);
    },
    postCreate: function() {
      this.inherited(arguments);
      this.connect(this.domNode, "onmousedown", "_onMouseDown");
    },
    _setDisabledAttr: function(_489) {
      this._set("disabled", _489);
      dojo.attr(this.focusNode, "disabled", _489);
      if (this.valueNode) {
        dojo.attr(this.valueNode, "disabled", _489);
      }
      dijit.setWaiState(this.focusNode, "disabled", _489);
      if (_489) {
        this._set("hovering", false);
        this._set("active", false);
        var _48a = "tabIndex" in this.attributeMap ? this.attributeMap.tabIndex : "focusNode";
        dojo.forEach(dojo.isArray(_48a) ? _48a : [_48a], function(_48b) {
          var node = this[_48b];
          if (dojo.isWebKit || dijit.hasDefaultTabStop(node)) {
            node.setAttribute("tabIndex", "-1");
          } else {
            node.removeAttribute("tabIndex");
          }
        }, this);
      } else {
        if (this.tabIndex != "") {
          this.focusNode.setAttribute("tabIndex", this.tabIndex);
        }
      }
    },
    setDisabled: function(_48c) {
      dojo.deprecated("setDisabled(" + _48c + ") is deprecated. Use set('disabled'," + _48c + ") instead.", "", "2.0");
      this.set("disabled", _48c);
    },
    _onFocus: function(e) {
      if (this.scrollOnFocus) {
        dojo.window.scrollIntoView(this.domNode);
      }
      this.inherited(arguments);
    },
    isFocusable: function() {
      return !this.disabled && this.focusNode && (dojo.style(this.domNode, "display") != "none");
    },
    focus: function() {
      if (!this.disabled) {
        dijit.focus(this.focusNode);
      }
    },
    compare: function(val1, val2) {
      if (typeof val1 == "number" && typeof val2 == "number") {
        return (isNaN(val1) && isNaN(val2)) ? 0 : val1 - val2;
      } else {
        if (val1 > val2) {
          return 1;
        } else {
          if (val1 < val2) {
            return -1;
          } else {
            return 0;
          }
        }
      }
    },
    onChange: function(_48d) {},
    _onChangeActive: false,
    _handleOnChange: function(_48e, _48f) {
      if (this._lastValueReported == undefined && (_48f === null || !this._onChangeActive)) {
        this._resetValue = this._lastValueReported = _48e;
      }
      this._pendingOnChange = this._pendingOnChange || (typeof _48e != typeof this._lastValueReported) || (this.compare(_48e, this._lastValueReported) != 0);
      if ((this.intermediateChanges || _48f || _48f === undefined) && this._pendingOnChange) {
        this._lastValueReported = _48e;
        this._pendingOnChange = false;
        if (this._onChangeActive) {
          if (this._onChangeHandle) {
            clearTimeout(this._onChangeHandle);
          }
          this._onChangeHandle = setTimeout(dojo.hitch(this, function() {
            this._onChangeHandle = null;
            this.onChange(_48e);
          }), 0);
        }
      }
    },
    create: function() {
      this.inherited(arguments);
      this._onChangeActive = true;
    },
    destroy: function() {
      if (this._onChangeHandle) {
        clearTimeout(this._onChangeHandle);
        this.onChange(this._lastValueReported);
      }
      this.inherited(arguments);
    },
    setValue: function(_490) {
      dojo.deprecated("dijit.form._FormWidget:setValue(" + _490 + ") is deprecated.  Use set('value'," + _490 + ") instead.", "", "2.0");
      this.set("value", _490);
    },
    getValue: function() {
      dojo.deprecated(this.declaredClass + "::getValue() is deprecated. Use get('value') instead.", "", "2.0");
      return this.get("value");
    },
    _onMouseDown: function(e) {
      if (!e.ctrlKey && dojo.mouseButtons.isLeft(e) && this.isFocusable()) {
        var _491 = this.connect(dojo.body(), "onmouseup", function() {
          if (this.isFocusable()) {
            this.focus();
          }
          this.disconnect(_491);
        });
      }
    }
  });
  dojo.declare("dijit.form._FormValueWidget", dijit.form._FormWidget, {
    readOnly: false,
    attributeMap: dojo.delegate(dijit.form._FormWidget.prototype.attributeMap, {
      value: "",
      readOnly: "focusNode"
    }),
    _setReadOnlyAttr: function(_492) {
      dojo.attr(this.focusNode, "readOnly", _492);
      dijit.setWaiState(this.focusNode, "readonly", _492);
      this._set("readOnly", _492);
    },
    postCreate: function() {
      this.inherited(arguments);
      if (dojo.isIE < 9 || (dojo.isIE && dojo.isQuirks)) {
        this.connect(this.focusNode || this.domNode, "onkeydown", this._onKeyDown);
      }
      if (this._resetValue === undefined) {
        this._lastValueReported = this._resetValue = this.value;
      }
    },
    _setValueAttr: function(_493, _494) {
      this._handleOnChange(_493, _494);
    },
    _handleOnChange: function(_495, _496) {
      this._set("value", _495);
      this.inherited(arguments);
    },
    undo: function() {
      this._setValueAttr(this._lastValueReported, false);
    },
    reset: function() {
      this._hasBeenBlurred = false;
      this._setValueAttr(this._resetValue, true);
    },
    _onKeyDown: function(e) {
      if (e.keyCode == dojo.keys.ESCAPE && !(e.ctrlKey || e.altKey || e.metaKey)) {
        var te;
        if (dojo.isIE) {
          e.preventDefault();
          te = document.createEventObject();
          te.keyCode = dojo.keys.ESCAPE;
          te.shiftKey = e.shiftKey;
          e.srcElement.fireEvent("onkeypress", te);
        }
      }
    },
    _layoutHackIE7: function() {
      if (dojo.isIE == 7) {
        var _497 = this.domNode;
        var _498 = _497.parentNode;
        var _499 = _497.firstChild || _497;
        var _49a = _499.style.filter;
        var _49b = this;
        while (_498 && _498.clientHeight == 0) {
          (function ping() {
            var _49c = _49b.connect(_498, "onscroll", function(e) {
              _49b.disconnect(_49c);
              _499.style.filter = (new Date()).getMilliseconds();
              setTimeout(function() {
                _499.style.filter = _49a;
              }, 0);
            });
          })();
          _498 = _498.parentNode;
        }
      }
    }
  });
}
if (!dojo._hasResource["dijit._HasDropDown"]) {
  dojo._hasResource["dijit._HasDropDown"] = true;
  dojo.provide("dijit._HasDropDown");
  dojo.declare("dijit._HasDropDown", null, {
    _buttonNode: null,
    _arrowWrapperNode: null,
    _popupStateNode: null,
    _aroundNode: null,
    dropDown: null,
    autoWidth: true,
    forceWidth: false,
    maxHeight: 0,
    dropDownPosition: ["below", "above"],
    _stopClickEvents: true,
    _onDropDownMouseDown: function(e) {
      if (this.disabled || this.readOnly) {
        return;
      }
      dojo.stopEvent(e);
      this._docHandler = this.connect(dojo.doc, "onmouseup", "_onDropDownMouseUp");
      this.toggleDropDown();
    },
    _onDropDownMouseUp: function(e) {
      if (e && this._docHandler) {
        this.disconnect(this._docHandler);
      }
      var _49d = this.dropDown,
        _49e = false;
      if (e && this._opened) {
        var c = dojo.position(this._buttonNode, true);
        if (!(e.pageX >= c.x && e.pageX <= c.x + c.w) || !(e.pageY >= c.y && e.pageY <= c.y + c.h)) {
          var t = e.target;
          while (t && !_49e) {
            if (dojo.hasClass(t, "dijitPopup")) {
              _49e = true;
            } else {
              t = t.parentNode;
            }
          }
          if (_49e) {
            t = e.target;
            if (_49d.onItemClick) {
              var _49f;
              while (t && !(_49f = dijit.byNode(t))) {
                t = t.parentNode;
              }
              if (_49f && _49f.onClick && _49f.getParent) {
                _49f.getParent().onItemClick(_49f, e);
              }
            }
            return;
          }
        }
      }
      if (this._opened && _49d.focus && _49d.autoFocus !== false) {
        window.setTimeout(dojo.hitch(_49d, "focus"), 1);
      }
    },
    _onDropDownClick: function(e) {
      if (this._stopClickEvents) {
        dojo.stopEvent(e);
      }
    },
    buildRendering: function() {
      this.inherited(arguments);
      this._buttonNode = this._buttonNode || this.focusNode || this.domNode;
      this._popupStateNode = this._popupStateNode || this.focusNode || this._buttonNode;
      var _4a0 = {
        "after": this.isLeftToRight() ? "Right" : "Left",
        "before": this.isLeftToRight() ? "Left" : "Right",
        "above": "Up",
        "below": "Down",
        "left": "Left",
        "right": "Right"
      } [this.dropDownPosition[0]] || this.dropDownPosition[0] || "Down";
      dojo.addClass(this._arrowWrapperNode || this._buttonNode, "dijit" + _4a0 + "ArrowButton");
    },
    postCreate: function() {
      this.inherited(arguments);
      this.connect(this._buttonNode, "onmousedown", "_onDropDownMouseDown");
      this.connect(this._buttonNode, "onclick", "_onDropDownClick");
      this.connect(this.focusNode, "onkeypress", "_onKey");
      this.connect(this.focusNode, "onkeyup", "_onKeyUp");
    },
    destroy: function() {
      if (this.dropDown) {
        if (!this.dropDown._destroyed) {
          this.dropDown.destroyRecursive();
        }
        delete this.dropDown;
      }
      this.inherited(arguments);
    },
    _onKey: function(e) {
      if (this.disabled || this.readOnly) {
        return;
      }
      var d = this.dropDown,
        _4a1 = e.target;
      if (d && this._opened && d.handleKey) {
        if (d.handleKey(e) === false) {
          dojo.stopEvent(e);
          return;
        }
      }
      if (d && this._opened && e.charOrCode == dojo.keys.ESCAPE) {
        this.closeDropDown();
        dojo.stopEvent(e);
      } else {
        if (!this._opened && (e.charOrCode == dojo.keys.DOWN_ARROW || ((e.charOrCode == dojo.keys.ENTER || e.charOrCode == " ") && ((_4a1.tagName || "").toLowerCase() !== "input" || (_4a1.type && _4a1.type.toLowerCase() !== "text"))))) {
          this._toggleOnKeyUp = true;
          dojo.stopEvent(e);
        }
      }
    },
    _onKeyUp: function() {
      if (this._toggleOnKeyUp) {
        delete this._toggleOnKeyUp;
        this.toggleDropDown();
        var d = this.dropDown;
        if (d && d.focus) {
          setTimeout(dojo.hitch(d, "focus"), 1);
        }
      }
    },
    _onBlur: function() {
      var _4a2 = dijit._curFocus && this.dropDown && dojo.isDescendant(dijit._curFocus, this.dropDown.domNode);
      this.closeDropDown(_4a2);
      this.inherited(arguments);
    },
    isLoaded: function() {
      return true;
    },
    loadDropDown: function(_4a3) {
      _4a3();
    },
    toggleDropDown: function() {
      if (this.disabled || this.readOnly) {
        return;
      }
      if (!this._opened) {
        if (!this.isLoaded()) {
          this.loadDropDown(dojo.hitch(this, "openDropDown"));
          return;
        } else {
          this.openDropDown();
        }
      } else {
        this.closeDropDown();
      }
    },
    openDropDown: function() {
      var _4a4 = this.dropDown,
        _4a5 = _4a4.domNode,
        _4a6 = this._aroundNode || this.domNode,
        self = this;
      if (!this._preparedNode) {
        this._preparedNode = true;
        if (_4a5.style.width) {
          this._explicitDDWidth = true;
        }
        if (_4a5.style.height) {
          this._explicitDDHeight = true;
        }
      }
      if (this.maxHeight || this.forceWidth || this.autoWidth) {
        var _4a7 = {
          display: "",
          visibility: "hidden"
        };
        if (!this._explicitDDWidth) {
          _4a7.width = "";
        }
        if (!this._explicitDDHeight) {
          _4a7.height = "";
        }
        dojo.style(_4a5, _4a7);
        var _4a8 = this.maxHeight;
        if (_4a8 == -1) {
          var _4a9 = dojo.window.getBox(),
            _4aa = dojo.position(_4a6, false);
          _4a8 = Math.floor(Math.max(_4aa.y, _4a9.h - (_4aa.y + _4aa.h)));
        }
        if (_4a4.startup && !_4a4._started) {
          _4a4.startup();
        }
        dijit.popup.moveOffScreen(_4a4);
        var mb = dojo._getMarginSize(_4a5);
        var _4ab = (_4a8 && mb.h > _4a8);
        dojo.style(_4a5, {
          overflowX: "hidden",
          overflowY: _4ab ? "auto" : "hidden"
        });
        if (_4ab) {
          mb.h = _4a8;
          if ("w" in mb) {
            mb.w += 16;
          }
        } else {
          delete mb.h;
        }
        if (this.forceWidth) {
          mb.w = _4a6.offsetWidth;
        } else {
          if (this.autoWidth) {
            mb.w = Math.max(mb.w, _4a6.offsetWidth);
          } else {
            delete mb.w;
          }
        }
        if (dojo.isFunction(_4a4.resize)) {
          _4a4.resize(mb);
        } else {
          dojo.marginBox(_4a5, mb);
        }
      }
      var _4ac = dijit.popup.open({
        parent: this,
        popup: _4a4,
        around: _4a6,
        orient: dijit.getPopupAroundAlignment((this.dropDownPosition && this.dropDownPosition.length) ? this.dropDownPosition : ["below"], this.isLeftToRight()),
        onExecute: function() {
          self.closeDropDown(true);
        },
        onCancel: function() {
          self.closeDropDown(true);
        },
        onClose: function() {
          dojo.attr(self._popupStateNode, "popupActive", false);
          dojo.removeClass(self._popupStateNode, "dijitHasDropDownOpen");
          self._opened = false;
        }
      });
      dojo.attr(this._popupStateNode, "popupActive", "true");
      dojo.addClass(self._popupStateNode, "dijitHasDropDownOpen");
      this._opened = true;
      return _4ac;
    },
    closeDropDown: function(_4ad) {
      if (this._opened) {
        if (_4ad) {
          this.focus();
        }
        dijit.popup.close(this.dropDown);
        this._opened = false;
      }
    }
  });
}
if (!dojo._hasResource["dijit.form.Button"]) {
  dojo._hasResource["dijit.form.Button"] = true;
  dojo.provide("dijit.form.Button");
  dojo.declare("dijit.form.Button", dijit.form._FormWidget, {
    label: "",
    showLabel: true,
    iconClass: "",
    type: "button",
    baseClass: "dijitButton",
    templateString: dojo.cache("dijit.form", "templates/Button.html", "<span class=\"dijit dijitReset dijitInline\"\n\t><span class=\"dijitReset dijitInline dijitButtonNode\"\n\t\tdojoAttachEvent=\"ondijitclick:_onButtonClick\"\n\t\t><span class=\"dijitReset dijitStretch dijitButtonContents\"\n\t\t\tdojoAttachPoint=\"titleNode,focusNode\"\n\t\t\trole=\"button\" aria-labelledby=\"${id}_label\"\n\t\t\t><span class=\"dijitReset dijitInline dijitIcon\" dojoAttachPoint=\"iconNode\"></span\n\t\t\t><span class=\"dijitReset dijitToggleButtonIconChar\">&#x25CF;</span\n\t\t\t><span class=\"dijitReset dijitInline dijitButtonText\"\n\t\t\t\tid=\"${id}_label\"\n\t\t\t\tdojoAttachPoint=\"containerNode\"\n\t\t\t></span\n\t\t></span\n\t></span\n\t><input ${!nameAttrSetting} type=\"${type}\" value=\"${value}\" class=\"dijitOffScreen\" tabIndex=\"-1\"\n\t\tdojoAttachPoint=\"valueNode\"\n/></span>\n"),
    attributeMap: dojo.delegate(dijit.form._FormWidget.prototype.attributeMap, {
      value: "valueNode"
    }),
    _onClick: function(e) {
      if (this.disabled) {
        return false;
      }
      this._clicked();
      return this.onClick(e);
    },
    _onButtonClick: function(e) {
      if (this._onClick(e) === false) {
        e.preventDefault();
      } else {
        if (this.type == "submit" && !(this.valueNode || this.focusNode).form) {
          for (var node = this.domNode; node.parentNode; node = node.parentNode) {
            var _4ae = dijit.byNode(node);
            if (_4ae && typeof _4ae._onSubmit == "function") {
              _4ae._onSubmit(e);
              break;
            }
          }
        } else {
          if (this.valueNode) {
            this.valueNode.click();
            e.preventDefault();
          }
        }
      }
    },
    buildRendering: function() {
      this.inherited(arguments);
      dojo.setSelectable(this.focusNode, false);
    },
    _fillContent: function(_4af) {
      if (_4af && (!this.params || !("label" in this.params))) {
        this.set("label", _4af.innerHTML);
      }
    },
    _setShowLabelAttr: function(val) {
      if (this.containerNode) {
        dojo.toggleClass(this.containerNode, "dijitDisplayNone", !val);
      }
      this._set("showLabel", val);
    },
    onClick: function(e) {
      return true;
    },
    _clicked: function(e) {},
    setLabel: function(_4b0) {
      dojo.deprecated("dijit.form.Button.setLabel() is deprecated.  Use set('label', ...) instead.", "", "2.0");
      this.set("label", _4b0);
    },
    _setLabelAttr: function(_4b1) {
      this._set("label", _4b1);
      this.containerNode.innerHTML = _4b1;
      if (this.showLabel == false && !this.params.title) {
        this.titleNode.title = dojo.trim(this.containerNode.innerText || this.containerNode.textContent || "");
      }
    },
    _setIconClassAttr: function(val) {
      var _4b2 = this.iconClass || "dijitNoIcon",
        _4b3 = val || "dijitNoIcon";
      dojo.replaceClass(this.iconNode, _4b3, _4b2);
      this._set("iconClass", val);
    }
  });
  dojo.declare("dijit.form.DropDownButton", [dijit.form.Button, dijit._Container, dijit._HasDropDown], {
    baseClass: "dijitDropDownButton",
    templateString: dojo.cache("dijit.form", "templates/DropDownButton.html", "<span class=\"dijit dijitReset dijitInline\"\n\t><span class='dijitReset dijitInline dijitButtonNode'\n\t\tdojoAttachEvent=\"ondijitclick:_onButtonClick\" dojoAttachPoint=\"_buttonNode\"\n\t\t><span class=\"dijitReset dijitStretch dijitButtonContents\"\n\t\t\tdojoAttachPoint=\"focusNode,titleNode,_arrowWrapperNode\"\n\t\t\trole=\"button\" aria-haspopup=\"true\" aria-labelledby=\"${id}_label\"\n\t\t\t><span class=\"dijitReset dijitInline dijitIcon\"\n\t\t\t\tdojoAttachPoint=\"iconNode\"\n\t\t\t></span\n\t\t\t><span class=\"dijitReset dijitInline dijitButtonText\"\n\t\t\t\tdojoAttachPoint=\"containerNode,_popupStateNode\"\n\t\t\t\tid=\"${id}_label\"\n\t\t\t></span\n\t\t\t><span class=\"dijitReset dijitInline dijitArrowButtonInner\"></span\n\t\t\t><span class=\"dijitReset dijitInline dijitArrowButtonChar\">&#9660;</span\n\t\t></span\n\t></span\n\t><input ${!nameAttrSetting} type=\"${type}\" value=\"${value}\" class=\"dijitOffScreen\" tabIndex=\"-1\"\n\t\tdojoAttachPoint=\"valueNode\"\n/></span>\n"),
    _fillContent: function() {
      if (this.srcNodeRef) {
        var _4b4 = dojo.query("*", this.srcNodeRef);
        dijit.form.DropDownButton.superclass._fillContent.call(this, _4b4[0]);
        this.dropDownContainer = this.srcNodeRef;
      }
    },
    startup: function() {
      if (this._started) {
        return;
      }
      if (!this.dropDown && this.dropDownContainer) {
        var _4b5 = dojo.query("[widgetId]", this.dropDownContainer)[0];
        this.dropDown = dijit.byNode(_4b5);
        delete this.dropDownContainer;
      }
      if (this.dropDown) {
        dijit.popup.hide(this.dropDown);
      }
      this.inherited(arguments);
    },
    isLoaded: function() {
      var _4b6 = this.dropDown;
      return (!!_4b6 && (!_4b6.href || _4b6.isLoaded));
    },
    loadDropDown: function() {
      var _4b7 = this.dropDown;
      if (!_4b7) {
        return;
      }
      if (!this.isLoaded()) {
        var _4b8 = dojo.connect(_4b7, "onLoad", this, function() {
          dojo.disconnect(_4b8);
          this.openDropDown();
        });
        _4b7.refresh();
      } else {
        this.openDropDown();
      }
    },
    isFocusable: function() {
      return this.inherited(arguments) && !this._mouseDown;
    }
  });
  dojo.declare("dijit.form.ComboButton", dijit.form.DropDownButton, {
    templateString: dojo.cache("dijit.form", "templates/ComboButton.html", "<table class=\"dijit dijitReset dijitInline dijitLeft\"\n\tcellspacing='0' cellpadding='0' role=\"presentation\"\n\t><tbody role=\"presentation\"><tr role=\"presentation\"\n\t\t><td class=\"dijitReset dijitStretch dijitButtonNode\" dojoAttachPoint=\"buttonNode\" dojoAttachEvent=\"ondijitclick:_onButtonClick,onkeypress:_onButtonKeyPress\"\n\t\t><div id=\"${id}_button\" class=\"dijitReset dijitButtonContents\"\n\t\t\tdojoAttachPoint=\"titleNode\"\n\t\t\trole=\"button\" aria-labelledby=\"${id}_label\"\n\t\t\t><div class=\"dijitReset dijitInline dijitIcon\" dojoAttachPoint=\"iconNode\" role=\"presentation\"></div\n\t\t\t><div class=\"dijitReset dijitInline dijitButtonText\" id=\"${id}_label\" dojoAttachPoint=\"containerNode\" role=\"presentation\"></div\n\t\t></div\n\t\t></td\n\t\t><td id=\"${id}_arrow\" class='dijitReset dijitRight dijitButtonNode dijitArrowButton'\n\t\t\tdojoAttachPoint=\"_popupStateNode,focusNode,_buttonNode\"\n\t\t\tdojoAttachEvent=\"onkeypress:_onArrowKeyPress\"\n\t\t\ttitle=\"${optionsTitle}\"\n\t\t\trole=\"button\" aria-haspopup=\"true\"\n\t\t\t><div class=\"dijitReset dijitArrowButtonInner\" role=\"presentation\"></div\n\t\t\t><div class=\"dijitReset dijitArrowButtonChar\" role=\"presentation\">&#9660;</div\n\t\t></td\n\t\t><td style=\"display:none !important;\"\n\t\t\t><input ${!nameAttrSetting} type=\"${type}\" value=\"${value}\" dojoAttachPoint=\"valueNode\"\n\t\t/></td></tr></tbody\n></table>\n"),
    attributeMap: dojo.mixin(dojo.clone(dijit.form.Button.prototype.attributeMap), {
      id: "",
      tabIndex: ["focusNode", "titleNode"],
      title: "titleNode"
    }),
    optionsTitle: "",
    baseClass: "dijitComboButton",
    cssStateNodes: {
      "buttonNode": "dijitButtonNode",
      "titleNode": "dijitButtonContents",
      "_popupStateNode": "dijitDownArrowButton"
    },
    _focusedNode: null,
    _onButtonKeyPress: function(evt) {
      if (evt.charOrCode == dojo.keys[this.isLeftToRight() ? "RIGHT_ARROW" : "LEFT_ARROW"]) {
        dijit.focus(this._popupStateNode);
        dojo.stopEvent(evt);
      }
    },
    _onArrowKeyPress: function(evt) {
      if (evt.charOrCode == dojo.keys[this.isLeftToRight() ? "LEFT_ARROW" : "RIGHT_ARROW"]) {
        dijit.focus(this.titleNode);
        dojo.stopEvent(evt);
      }
    },
    focus: function(_4b9) {
      if (!this.disabled) {
        dijit.focus(_4b9 == "start" ? this.titleNode : this._popupStateNode);
      }
    }
  });
  dojo.declare("dijit.form.ToggleButton", dijit.form.Button, {
    baseClass: "dijitToggleButton",
    checked: false,
    attributeMap: dojo.mixin(dojo.clone(dijit.form.Button.prototype.attributeMap), {
      checked: "focusNode"
    }),
    _clicked: function(evt) {
      this.set("checked", !this.checked);
    },
    _setCheckedAttr: function(_4ba, _4bb) {
      this._set("checked", _4ba);
      dojo.attr(this.focusNode || this.domNode, "checked", _4ba);
      dijit.setWaiState(this.focusNode || this.domNode, "pressed", _4ba);
      this._handleOnChange(_4ba, _4bb);
    },
    setChecked: function(_4bc) {
      dojo.deprecated("setChecked(" + _4bc + ") is deprecated. Use set('checked'," + _4bc + ") instead.", "", "2.0");
      this.set("checked", _4bc);
    },
    reset: function() {
      this._hasBeenBlurred = false;
      this.set("checked", this.params.checked || false);
    }
  });
}
if (!dojo._hasResource["dijit._KeyNavContainer"]) {
  dojo._hasResource["dijit._KeyNavContainer"] = true;
  dojo.provide("dijit._KeyNavContainer");
  dojo.declare("dijit._KeyNavContainer", dijit._Container, {
    tabIndex: "0",
    _keyNavCodes: {},
    connectKeyNavHandlers: function(_4bd, _4be) {
      var _4bf = (this._keyNavCodes = {});
      var prev = dojo.hitch(this, this.focusPrev);
      var next = dojo.hitch(this, this.focusNext);
      dojo.forEach(_4bd, function(code) {
        _4bf[code] = prev;
      });
      dojo.forEach(_4be, function(code) {
        _4bf[code] = next;
      });
      _4bf[dojo.keys.HOME] = dojo.hitch(this, "focusFirstChild");
      _4bf[dojo.keys.END] = dojo.hitch(this, "focusLastChild");
      this.connect(this.domNode, "onkeypress", "_onContainerKeypress");
      this.connect(this.domNode, "onfocus", "_onContainerFocus");
    },
    startupKeyNavChildren: function() {
      dojo.forEach(this.getChildren(), dojo.hitch(this, "_startupChild"));
    },
    addChild: function(_4c0, _4c1) {
      dijit._KeyNavContainer.superclass.addChild.apply(this, arguments);
      this._startupChild(_4c0);
    },
    focus: function() {
      this.focusFirstChild();
    },
    focusFirstChild: function() {
      var _4c2 = this._getFirstFocusableChild();
      if (_4c2) {
        this.focusChild(_4c2);
      }
    },
    focusLastChild: function() {
      var _4c3 = this._getLastFocusableChild();
      if (_4c3) {
        this.focusChild(_4c3);
      }
    },
    focusNext: function() {
      var _4c4 = this._getNextFocusableChild(this.focusedChild, 1);
      this.focusChild(_4c4);
    },
    focusPrev: function() {
      var _4c5 = this._getNextFocusableChild(this.focusedChild, -1);
      this.focusChild(_4c5, true);
    },
    focusChild: function(_4c6, last) {
      if (this.focusedChild && _4c6 !== this.focusedChild) {
        this._onChildBlur(this.focusedChild);
      }
      _4c6.set("tabIndex", this.tabIndex);
      _4c6.focus(last ? "end" : "start");
      this._set("focusedChild", _4c6);
    },
    _startupChild: function(_4c7) {
      _4c7.set("tabIndex", "-1");
      this.connect(_4c7, "_onFocus", function() {
        _4c7.set("tabIndex", this.tabIndex);
      });
      this.connect(_4c7, "_onBlur", function() {
        _4c7.set("tabIndex", "-1");
      });
    },
    _onContainerFocus: function(evt) {
      if (evt.target !== this.domNode || this.focusedChild) {
        return;
      }
      this.focusFirstChild();
      dojo.attr(this.domNode, "tabIndex", "-1");
    },
    _onBlur: function(evt) {
      if (this.tabIndex) {
        dojo.attr(this.domNode, "tabIndex", this.tabIndex);
      }
      this.inherited(arguments);
      this.focusedChild = null;
    },
    _onContainerKeypress: function(evt) {
      if (evt.ctrlKey || evt.altKey) {
        return;
      }
      var func = this._keyNavCodes[evt.charOrCode];
      if (func) {
        func();
        dojo.stopEvent(evt);
      }
    },
    _onChildBlur: function(_4c8) {},
    _getFirstFocusableChild: function() {
      return this._getNextFocusableChild(null, 1);
    },
    _getLastFocusableChild: function() {
      return this._getNextFocusableChild(null, -1);
    },
    _getNextFocusableChild: function(_4c9, dir) {
      if (_4c9) {
        _4c9 = this._getSiblingOfChild(_4c9, dir);
      }
      var _4ca = this.getChildren();
      for (var i = 0; i < _4ca.length; i++) {
        if (!_4c9) {
          _4c9 = _4ca[(dir > 0) ? 0 : (_4ca.length - 1)];
        }
        if (_4c9.isFocusable()) {
          return _4c9;
        }
        _4c9 = this._getSiblingOfChild(_4c9, dir);
      }
      return null;
    }
  });
}
if (!dojo._hasResource["dijit.MenuItem"]) {
  dojo._hasResource["dijit.MenuItem"] = true;
  dojo.provide("dijit.MenuItem");
  dojo.declare("dijit.MenuItem", [dijit._Widget, dijit._Templated, dijit._Contained, dijit._CssStateMixin], {
    templateString: dojo.cache("dijit", "templates/MenuItem.html", "<tr class=\"dijitReset dijitMenuItem\" dojoAttachPoint=\"focusNode\" role=\"menuitem\" tabIndex=\"-1\"\n\t\tdojoAttachEvent=\"onmouseenter:_onHover,onmouseleave:_onUnhover,ondijitclick:_onClick\">\n\t<td class=\"dijitReset dijitMenuItemIconCell\" role=\"presentation\">\n\t\t<img src=\"${_blankGif}\" alt=\"\" class=\"dijitIcon dijitMenuItemIcon\" dojoAttachPoint=\"iconNode\"/>\n\t</td>\n\t<td class=\"dijitReset dijitMenuItemLabel\" colspan=\"2\" dojoAttachPoint=\"containerNode\" role=\"presentation\"></td>\n\t<td class=\"dijitReset dijitMenuItemAccelKey\" style=\"display: none\" dojoAttachPoint=\"accelKeyNode\"></td>\n\t<td class=\"dijitReset dijitMenuArrowCell\" role=\"presentation\">\n\t\t<div dojoAttachPoint=\"arrowWrapper\" style=\"visibility: hidden\">\n\t\t\t<img src=\"${_blankGif}\" alt=\"\" class=\"dijitMenuExpand\"/>\n\t\t\t<span class=\"dijitMenuExpandA11y\">+</span>\n\t\t</div>\n\t</td>\n</tr>\n"),
    attributeMap: dojo.delegate(dijit._Widget.prototype.attributeMap, {
      label: {
        node: "containerNode",
        type: "innerHTML"
      },
      iconClass: {
        node: "iconNode",
        type: "class"
      }
    }),
    baseClass: "dijitMenuItem",
    label: "",
    iconClass: "",
    accelKey: "",
    disabled: false,
    _fillContent: function(_4cb) {
      if (_4cb && !("label" in this.params)) {
        this.set("label", _4cb.innerHTML);
      }
    },
    buildRendering: function() {
      this.inherited(arguments);
      var _4cc = this.id + "_text";
      dojo.attr(this.containerNode, "id", _4cc);
      if (this.accelKeyNode) {
        dojo.attr(this.accelKeyNode, "id", this.id + "_accel");
        _4cc += " " + this.id + "_accel";
      }
      dijit.setWaiState(this.domNode, "labelledby", _4cc);
      dojo.setSelectable(this.domNode, false);
    },
    _onHover: function() {
      this.getParent().onItemHover(this);
    },
    _onUnhover: function() {
      this.getParent().onItemUnhover(this);
      this._set("hovering", false);
    },
    _onClick: function(evt) {
      this.getParent().onItemClick(this, evt);
      dojo.stopEvent(evt);
    },
    onClick: function(evt) {},
    focus: function() {
      try {
        if (dojo.isIE == 8) {
          this.containerNode.focus();
        }
        dijit.focus(this.focusNode);
      } catch (e) {}
    },
    _onFocus: function() {
      this._setSelected(true);
      this.getParent()._onItemFocus(this);
      this.inherited(arguments);
    },
    _setSelected: function(_4cd) {
      dojo.toggleClass(this.domNode, "dijitMenuItemSelected", _4cd);
    },
    setLabel: function(_4ce) {
      dojo.deprecated("dijit.MenuItem.setLabel() is deprecated.  Use set('label', ...) instead.", "", "2.0");
      this.set("label", _4ce);
    },
    setDisabled: function(_4cf) {
      dojo.deprecated("dijit.Menu.setDisabled() is deprecated.  Use set('disabled', bool) instead.", "", "2.0");
      this.set("disabled", _4cf);
    },
    _setDisabledAttr: function(_4d0) {
      dijit.setWaiState(this.focusNode, "disabled", _4d0 ? "true" : "false");
      this._set("disabled", _4d0);
    },
    _setAccelKeyAttr: function(_4d1) {
      this.accelKeyNode.style.display = _4d1 ? "" : "none";
      this.accelKeyNode.innerHTML = _4d1;
      dojo.attr(this.containerNode, "colSpan", _4d1 ? "1" : "2");
      this._set("accelKey", _4d1);
    }
  });
}
if (!dojo._hasResource["dijit.PopupMenuItem"]) {
  dojo._hasResource["dijit.PopupMenuItem"] = true;
  dojo.provide("dijit.PopupMenuItem");
  dojo.declare("dijit.PopupMenuItem", dijit.MenuItem, {
    _fillContent: function() {
      if (this.srcNodeRef) {
        var _4d2 = dojo.query("*", this.srcNodeRef);
        dijit.PopupMenuItem.superclass._fillContent.call(this, _4d2[0]);
        this.dropDownContainer = this.srcNodeRef;
      }
    },
    startup: function() {
      if (this._started) {
        return;
      }
      this.inherited(arguments);
      if (!this.popup) {
        var node = dojo.query("[widgetId]", this.dropDownContainer)[0];
        this.popup = dijit.byNode(node);
      }
      dojo.body().appendChild(this.popup.domNode);
      this.popup.startup();
      this.popup.domNode.style.display = "none";
      if (this.arrowWrapper) {
        dojo.style(this.arrowWrapper, "visibility", "");
      }
      dijit.setWaiState(this.focusNode, "haspopup", "true");
    },
    destroyDescendants: function() {
      if (this.popup) {
        if (!this.popup._destroyed) {
          this.popup.destroyRecursive();
        }
        delete this.popup;
      }
      this.inherited(arguments);
    }
  });
}
if (!dojo._hasResource["dijit.CheckedMenuItem"]) {
  dojo._hasResource["dijit.CheckedMenuItem"] = true;
  dojo.provide("dijit.CheckedMenuItem");
  dojo.declare("dijit.CheckedMenuItem", dijit.MenuItem, {
    templateString: dojo.cache("dijit", "templates/CheckedMenuItem.html", "<tr class=\"dijitReset dijitMenuItem\" dojoAttachPoint=\"focusNode\" role=\"menuitemcheckbox\" tabIndex=\"-1\"\n\t\tdojoAttachEvent=\"onmouseenter:_onHover,onmouseleave:_onUnhover,ondijitclick:_onClick\">\n\t<td class=\"dijitReset dijitMenuItemIconCell\" role=\"presentation\">\n\t\t<img src=\"${_blankGif}\" alt=\"\" class=\"dijitMenuItemIcon dijitCheckedMenuItemIcon\" dojoAttachPoint=\"iconNode\"/>\n\t\t<span class=\"dijitCheckedMenuItemIconChar\">&#10003;</span>\n\t</td>\n\t<td class=\"dijitReset dijitMenuItemLabel\" colspan=\"2\" dojoAttachPoint=\"containerNode,labelNode\"></td>\n\t<td class=\"dijitReset dijitMenuItemAccelKey\" style=\"display: none\" dojoAttachPoint=\"accelKeyNode\"></td>\n\t<td class=\"dijitReset dijitMenuArrowCell\" role=\"presentation\">&nbsp;</td>\n</tr>\n"),
    checked: false,
    _setCheckedAttr: function(_4d3) {
      dojo.toggleClass(this.domNode, "dijitCheckedMenuItemChecked", _4d3);
      dijit.setWaiState(this.domNode, "checked", _4d3);
      this._set("checked", _4d3);
    },
    onChange: function(_4d4) {},
    _onClick: function(e) {
      if (!this.disabled) {
        this.set("checked", !this.checked);
        this.onChange(this.checked);
      }
      this.inherited(arguments);
    }
  });
}
if (!dojo._hasResource["dijit.MenuSeparator"]) {
  dojo._hasResource["dijit.MenuSeparator"] = true;
  dojo.provide("dijit.MenuSeparator");
  dojo.declare("dijit.MenuSeparator", [dijit._Widget, dijit._Templated, dijit._Contained], {
    templateString: dojo.cache("dijit", "templates/MenuSeparator.html", "<tr class=\"dijitMenuSeparator\">\n\t<td class=\"dijitMenuSeparatorIconCell\">\n\t\t<div class=\"dijitMenuSeparatorTop\"></div>\n\t\t<div class=\"dijitMenuSeparatorBottom\"></div>\n\t</td>\n\t<td colspan=\"3\" class=\"dijitMenuSeparatorLabelCell\">\n\t\t<div class=\"dijitMenuSeparatorTop dijitMenuSeparatorLabel\"></div>\n\t\t<div class=\"dijitMenuSeparatorBottom\"></div>\n\t</td>\n</tr>\n"),
    buildRendering: function() {
      this.inherited(arguments);
      dojo.setSelectable(this.domNode, false);
    },
    isFocusable: function() {
      return false;
    }
  });
}
if (!dojo._hasResource["dijit.Menu"]) {
  dojo._hasResource["dijit.Menu"] = true;
  dojo.provide("dijit.Menu");
  dojo.declare("dijit._MenuBase", [dijit._Widget, dijit._Templated, dijit._KeyNavContainer], {
    parentMenu: null,
    popupDelay: 500,
    startup: function() {
      if (this._started) {
        return;
      }
      dojo.forEach(this.getChildren(), function(_4d5) {
        _4d5.startup();
      });
      this.startupKeyNavChildren();
      this.inherited(arguments);
    },
    onExecute: function() {},
    onCancel: function(_4d6) {},
    _moveToPopup: function(evt) {
      if (this.focusedChild && this.focusedChild.popup && !this.focusedChild.disabled) {
        this.focusedChild._onClick(evt);
      } else {
        var _4d7 = this._getTopMenu();
        if (_4d7 && _4d7._isMenuBar) {
          _4d7.focusNext();
        }
      }
    },
    _onPopupHover: function(evt) {
      if (this.currentPopup && this.currentPopup._pendingClose_timer) {
        var _4d8 = this.currentPopup.parentMenu;
        if (_4d8.focusedChild) {
          _4d8.focusedChild._setSelected(false);
        }
        _4d8.focusedChild = this.currentPopup.from_item;
        _4d8.focusedChild._setSelected(true);
        this._stopPendingCloseTimer(this.currentPopup);
      }
    },
    onItemHover: function(item) {
      if (this.isActive) {
        this.focusChild(item);
        if (this.focusedChild.popup && !this.focusedChild.disabled && !this.hover_timer) {
          this.hover_timer = setTimeout(dojo.hitch(this, "_openPopup"), this.popupDelay);
        }
      }
      if (this.focusedChild) {
        this.focusChild(item);
      }
      this._hoveredChild = item;
    },
    _onChildBlur: function(item) {
      this._stopPopupTimer();
      item._setSelected(false);
      var _4d9 = item.popup;
      if (_4d9) {
        this._stopPendingCloseTimer(_4d9);
        _4d9._pendingClose_timer = setTimeout(function() {
          _4d9._pendingClose_timer = null;
          if (_4d9.parentMenu) {
            _4d9.parentMenu.currentPopup = null;
          }
          dijit.popup.close(_4d9);
        }, this.popupDelay);
      }
    },
    onItemUnhover: function(item) {
      if (this.isActive) {
        this._stopPopupTimer();
      }
      if (this._hoveredChild == item) {
        this._hoveredChild = null;
      }
    },
    _stopPopupTimer: function() {
      if (this.hover_timer) {
        clearTimeout(this.hover_timer);
        this.hover_timer = null;
      }
    },
    _stopPendingCloseTimer: function(_4da) {
      if (_4da._pendingClose_timer) {
        clearTimeout(_4da._pendingClose_timer);
        _4da._pendingClose_timer = null;
      }
    },
    _stopFocusTimer: function() {
      if (this._focus_timer) {
        clearTimeout(this._focus_timer);
        this._focus_timer = null;
      }
    },
    _getTopMenu: function() {
      for (var top = this; top.parentMenu; top = top.parentMenu) {}
      return top;
    },
    onItemClick: function(item, evt) {
      if (typeof this.isShowingNow == "undefined") {
        this._markActive();
      }
      this.focusChild(item);
      if (item.disabled) {
        return false;
      }
      if (item.popup) {
        this._openPopup();
      } else {
        this.onExecute();
        item.onClick(evt);
      }
    },
    _openPopup: function() {
      this._stopPopupTimer();
      var _4db = this.focusedChild;
      if (!_4db) {
        return;
      }
      var _4dc = _4db.popup;
      if (_4dc.isShowingNow) {
        return;
      }
      if (this.currentPopup) {
        this._stopPendingCloseTimer(this.currentPopup);
        dijit.popup.close(this.currentPopup);
      }
      _4dc.parentMenu = this;
      _4dc.from_item = _4db;
      var self = this;
      dijit.popup.open({
        parent: this,
        popup: _4dc,
        around: _4db.domNode,
        orient: this._orient || (this.isLeftToRight() ? {
          "TR": "TL",
          "TL": "TR",
          "BR": "BL",
          "BL": "BR"
        } : {
          "TL": "TR",
          "TR": "TL",
          "BL": "BR",
          "BR": "BL"
        }),
        onCancel: function() {
          self.focusChild(_4db);
          self._cleanUp();
          _4db._setSelected(true);
          self.focusedChild = _4db;
        },
        onExecute: dojo.hitch(this, "_cleanUp")
      });
      this.currentPopup = _4dc;
      _4dc.connect(_4dc.domNode, "onmouseenter", dojo.hitch(self, "_onPopupHover"));
      if (_4dc.focus) {
        _4dc._focus_timer = setTimeout(dojo.hitch(_4dc, function() {
          this._focus_timer = null;
          this.focus();
        }), 0);
      }
    },
    _markActive: function() {
      this.isActive = true;
      dojo.replaceClass(this.domNode, "dijitMenuActive", "dijitMenuPassive");
    },
    onOpen: function(e) {
      this.isShowingNow = true;
      this._markActive();
    },
    _markInactive: function() {
      this.isActive = false;
      dojo.replaceClass(this.domNode, "dijitMenuPassive", "dijitMenuActive");
    },
    onClose: function() {
      this._stopFocusTimer();
      this._markInactive();
      this.isShowingNow = false;
      this.parentMenu = null;
    },
    _closeChild: function() {
      this._stopPopupTimer();
      var _4dd = this.focusedChild && this.focusedChild.from_item;
      if (this.currentPopup) {
        if (dijit._curFocus && dojo.isDescendant(dijit._curFocus, this.currentPopup.domNode)) {
          this.focusedChild.focusNode.focus();
        }
        dijit.popup.close(this.currentPopup);
        this.currentPopup = null;
      }
      if (this.focusedChild) {
        this.focusedChild._setSelected(false);
        this.focusedChild._onUnhover();
        this.focusedChild = null;
      }
    },
    _onItemFocus: function(item) {
      if (this._hoveredChild && this._hoveredChild != item) {
        this._hoveredChild._onUnhover();
      }
    },
    _onBlur: function() {
      this._cleanUp();
      this.inherited(arguments);
    },
    _cleanUp: function() {
      this._closeChild();
      if (typeof this.isShowingNow == "undefined") {
        this._markInactive();
      }
    }
  });
  dojo.declare("dijit.Menu", dijit._MenuBase, {
    constructor: function() {
      this._bindings = [];
    },
    templateString: dojo.cache("dijit", "templates/Menu.html", "<table class=\"dijit dijitMenu dijitMenuPassive dijitReset dijitMenuTable\" role=\"menu\" tabIndex=\"${tabIndex}\" dojoAttachEvent=\"onkeypress:_onKeyPress\" cellspacing=\"0\">\n\t<tbody class=\"dijitReset\" dojoAttachPoint=\"containerNode\"></tbody>\n</table>\n"),
    baseClass: "dijitMenu",
    targetNodeIds: [],
    contextMenuForWindow: false,
    leftClickToOpen: false,
    refocus: true,
    postCreate: function() {
      if (this.contextMenuForWindow) {
        this.bindDomNode(dojo.body());
      } else {
        dojo.forEach(this.targetNodeIds, this.bindDomNode, this);
      }
      var k = dojo.keys,
        l = this.isLeftToRight();
      this._openSubMenuKey = l ? k.RIGHT_ARROW : k.LEFT_ARROW;
      this._closeSubMenuKey = l ? k.LEFT_ARROW : k.RIGHT_ARROW;
      this.connectKeyNavHandlers([k.UP_ARROW], [k.DOWN_ARROW]);
    },
    _onKeyPress: function(evt) {
      if (evt.ctrlKey || evt.altKey) {
        return;
      }
      switch (evt.charOrCode) {
        case this._openSubMenuKey:
          this._moveToPopup(evt);
          dojo.stopEvent(evt);
          break;
        case this._closeSubMenuKey:
          if (this.parentMenu) {
            if (this.parentMenu._isMenuBar) {
              this.parentMenu.focusPrev();
            } else {
              this.onCancel(false);
            }
          } else {
            dojo.stopEvent(evt);
          }
          break;
      }
    },
    _iframeContentWindow: function(_4de) {
      var win = dojo.window.get(this._iframeContentDocument(_4de)) || this._iframeContentDocument(_4de)["__parent__"] || (_4de.name && dojo.doc.frames[_4de.name]) || null;
      return win;
    },
    _iframeContentDocument: function(_4df) {
      var doc = _4df.contentDocument || (_4df.contentWindow && _4df.contentWindow.document) || (_4df.name && dojo.doc.frames[_4df.name] && dojo.doc.frames[_4df.name].document) || null;
      return doc;
    },
    bindDomNode: function(node) {
      node = dojo.byId(node);
      var cn;
      if (node.tagName.toLowerCase() == "iframe") {
        var _4e0 = node,
          win = this._iframeContentWindow(_4e0);
        cn = dojo.withGlobal(win, dojo.body);
      } else {
        cn = (node == dojo.body() ? dojo.doc.documentElement : node);
      }
      var _4e1 = {
        node: node,
        iframe: _4e0
      };
      dojo.attr(node, "_dijitMenu" + this.id, this._bindings.push(_4e1));
      var _4e2 = dojo.hitch(this, function(cn) {
        return [dojo.connect(cn, this.leftClickToOpen ? "onclick" : "oncontextmenu", this, function(evt) {
          dojo.stopEvent(evt);
          this._scheduleOpen(evt.target, _4e0, {
            x: evt.pageX,
            y: evt.pageY
          });
        }), dojo.connect(cn, "onkeydown", this, function(evt) {
          if (evt.shiftKey && evt.keyCode == dojo.keys.F10) {
            dojo.stopEvent(evt);
            this._scheduleOpen(evt.target, _4e0);
          }
        })];
      });
      _4e1.connects = cn ? _4e2(cn) : [];
      if (_4e0) {
        _4e1.onloadHandler = dojo.hitch(this, function() {
          var win = this._iframeContentWindow(_4e0);
          cn = dojo.withGlobal(win, dojo.body);
          _4e1.connects = _4e2(cn);
        });
        if (_4e0.addEventListener) {
          _4e0.addEventListener("load", _4e1.onloadHandler, false);
        } else {
          _4e0.attachEvent("onload", _4e1.onloadHandler);
        }
      }
    },
    unBindDomNode: function(_4e3) {
      var node;
      try {
        node = dojo.byId(_4e3);
      } catch (e) {
        return;
      }
      var _4e4 = "_dijitMenu" + this.id;
      if (node && dojo.hasAttr(node, _4e4)) {
        var bid = dojo.attr(node, _4e4) - 1,
          b = this._bindings[bid];
        dojo.forEach(b.connects, dojo.disconnect);
        var _4e5 = b.iframe;
        if (_4e5) {
          if (_4e5.removeEventListener) {
            _4e5.removeEventListener("load", b.onloadHandler, false);
          } else {
            _4e5.detachEvent("onload", b.onloadHandler);
          }
        }
        dojo.removeAttr(node, _4e4);
        delete this._bindings[bid];
      }
    },
    _scheduleOpen: function(_4e6, _4e7, _4e8) {
      if (!this._openTimer) {
        this._openTimer = setTimeout(dojo.hitch(this, function() {
          delete this._openTimer;
          this._openMyself({
            target: _4e6,
            iframe: _4e7,
            coords: _4e8
          });
        }), 1);
      }
    },
    _openMyself: function(args) {
      var _4e9 = args.target,
        _4ea = args.iframe,
        _4eb = args.coords;
      if (_4eb) {
        if (_4ea) {
          var od = _4e9.ownerDocument,
            ifc = dojo.position(_4ea, true),
            win = this._iframeContentWindow(_4ea),
            _4ec = dojo.withGlobal(win, "_docScroll", dojo);
          var cs = dojo.getComputedStyle(_4ea),
            tp = dojo._toPixelValue,
            left = (dojo.isIE && dojo.isQuirks ? 0 : tp(_4ea, cs.paddingLeft)) + (dojo.isIE && dojo.isQuirks ? tp(_4ea, cs.borderLeftWidth) : 0),
            top = (dojo.isIE && dojo.isQuirks ? 0 : tp(_4ea, cs.paddingTop)) + (dojo.isIE && dojo.isQuirks ? tp(_4ea, cs.borderTopWidth) : 0);
          _4eb.x += ifc.x + left - _4ec.x;
          _4eb.y += ifc.y + top - _4ec.y;
        }
      } else {
        _4eb = dojo.position(_4e9, true);
        _4eb.x += 10;
        _4eb.y += 10;
      }
      var self = this;
      var _4ed = dijit.getFocus(this);

      function _4ee() {
        if (self.refocus) {
          dijit.focus(_4ed);
        }
        dijit.popup.close(self);
      };
      dijit.popup.open({
        popup: this,
        x: _4eb.x,
        y: _4eb.y,
        onExecute: _4ee,
        onCancel: _4ee,
        orient: this.isLeftToRight() ? "L" : "R"
      });
      this.focus();
      this._onBlur = function() {
        this.inherited("_onBlur", arguments);
        dijit.popup.close(this);
      };
    },
    uninitialize: function() {
      dojo.forEach(this._bindings, function(b) {
        if (b) {
          this.unBindDomNode(b.node);
        }
      }, this);
      this.inherited(arguments);
    }
  });
}
if (!dojo._hasResource["ibmweb.w3.searchbar"]) {
  dojo._hasResource["ibmweb.w3.searchbar"] = true;
  dojo.provide("ibmweb.w3.searchbar");
  dojo.connect(ibmweb.dynnav, "dataCallback", function(data) {
    var _4ef = "searchbar-optionlabel",
      _4f0 = "searchbar-scopelabel",
      _4f1 = data.searchData.scope ? data.searchData.scope : "Choose search scope",
      _4f2 = data.searchData.sugg_search && data.searchData.nav_search ? data.searchData.sugg_search + " " + data.searchData.nav_search : "There are ${count} suggested search results.  Use down and up arrow keys to navigate through the results.",
      _4f3 = data.searchData.more.label ? data.searchData.more.label + "${count})" : "See more people (${count})",
      _4f4 = data.searchData.search_faces ? data.searchData.search_faces : "See more people (${count})",
      _4f5 = "//w3.ibm.com/connections/profiles/html/profileView.do?email=${email}",
      _4f6 = "//w3.ibm.com/newbp/profile.html?uid=${uid}";
    var _4f7 = null;
    var $ = dojo.query,
      w = window;
    var _4f8 = [{
      label: data.searchData.menuItem && data.searchData.menuItem.pages_label_w3 ? data.searchData.menuItem.pages_label_w3 : "Pages (w3)",
      description: data.searchData.pages_w3 ? data.searchData.pages_w3 : "Search Pages (w3)",
      url: ibmweb.config.search.w3SearchUrl,
      iconClass: "w3-icon",
      explainClick: data.searchData.enter_w3 ? data.searchData.enter_w3 : "Press Enter to search Pages (w3)",
      typeAheadEnabled: true,
      facesEnabled: true,
      topsearchEnabled: true
    }, {
      label: data.searchData.menuItem && data.searchData.menuItem.people_label ? data.searchData.menuItem.people_label : "People (Profiles)",
      description: data.searchData.people_prf ? data.searchData.people_prf : "Search People (Profiles)",
      url: ibmweb.config.search.profileSearchUrl,
      iconClass: "profiles-icon",
      explainClick: data.searchData.enter_prf ? data.searchData.enter_prf : "Press Enter to search People (Profiles)",
      typeAheadEnabled: true,
      facesEnabled: true,
      topsearchEnabled: false
    }, {
      label: data.searchData.menuItem && data.searchData.menuItem.pages_label_dotcom ? data.searchData.menuItem.pages_label_dotcom : "Pages (ibm.com)",
      description: data.searchData.pages_ibm ? data.searchData.pages_ibm : "Search Pages (ibm.com)",
      url: ibmweb.config.search.wwwSearchUrl,
      iconClass: "ibm-icon",
      explainClick: data.searchData.enter_dotcom ? data.searchData.enter_dotcom : "Press Enter to search Pages (ibm.com)",
      typeAheadEnabled: false,
      facesEnabled: false,
      topsearchEnabled: true,
      separator: true
    }, {
      label: data.searchData.menuItem && data.searchData.menuItem.connections_label ? data.searchData.menuItem.connections_label : "Connections",
      description: data.searchData.connections ? data.searchData.connections : "Search Connections",
      url: ibmweb.config.search.connectionSearchUrl,
      iconClass: "connections-icon",
      explainClick: data.searchData.enter_conn ? data.searchData.enter_conn : "Press Enter to search Connections",
      typeAheadEnabled: true,
      facesEnabled: true,
      topsearchEnabled: true
    }, {
      label: data.searchData.menuItem && data.searchData.menuItem.bp_label ? data.searchData.menuItem.bp_label : "Bluepages",
      description: data.searchData.bp ? data.searchData.bp : "Search Bluepages",
      url: ibmweb.config.search.bluepageSearchUrl,
      iconClass: "bluepages-icon",
      explainClick: data.searchData.enter_bp ? data.searchData.enter_bp : "Press Enter to search BluePages",
      typeAheadEnabled: true,
      facesEnabled: true,
      topsearchEnabled: true,
      separator: true
    }, {
      label: data.searchData.menuItem && data.searchData.menuItem.learn_label ? data.searchData.menuItem.learn_label : "IBM Learning",
      description: data.searchData.learn ? data.searchData.learn : "Search IBM Learning",
      url: ibmweb.config.search.learnSearchUrl,
      iconClass: "ibmlearning-icon",
      explainClick: data.searchData.enter_learn ? data.searchData.enter_learn : "Press Enter to search IBM Learning",
      typeAheadEnabled: false,
      facesEnabled: false,
      topsearchEnabled: true
    }, {
      label: data.searchData.menuItem && data.searchData.menuItem.qa_label ? data.searchData.menuItem.qa_label : "IBM Worldwide Q&A",
      description: data.searchData.qa ? data.searchData.qa : "Search IBM Worldwide Q&A",
      url: ibmweb.config.search.qaSearchUrl,
      iconClass: "ibmwwqa-icon",
      explainClick: data.searchData.enter_qa ? data.searchData.enter_qa : "Press Enter to search IBM Worldwide Q&A",
      typeAheadEnabled: false,
      facesEnabled: false,
      topsearchEnabled: true,
      separator: true
    }];
    var sb = window.ibmweb.w3.SearchBar = function(args, node) {
      dojo.mixin(this, args);
      if (!this.form || !(this.form = dojo.byId(this.form))) {
        throw new Error("SearchBar(args,node) was not called properly: args needs a \"form\" property that references the form node.");
      }
      if (!node || !(this.input = dojo.byId(node))) {
        throw new Error("SearchBar(args,node) was not called properly: node needs to reference a text input.");
      }
      _4f8 = ibmweb.config.search.before.concat(_4f8, ibmweb.config.search.after);
      this.createScopeButton();
      dojo.place(this.scope, this.input, "before");
      this.form.onsubmit = dojo.hitch(this, "submitForm");
      if (!("placeholder" in this.input)) {
        this.input.placeholderStateOn = true;
        dojo.connect(this.input, "onfocus", function(e) {
          if (e.target.placeholderStateOn) {
            e.target.value = "";
            e.target.placeholderStateOn = false;
          }
          if (dojo.byId("ibm-menu-links").className == "ibm-access") {
            dojo.publish("/ibm/dynnav/megamenu/expandMenu");
          }
        });
        dojo.connect(this.input, "onblur", dojo.hitch(this, function(e) {
          if (e.target.value.length == 0) {
            e.target.placeholderStateOn = true;
            e.target.value = this.placeholderText;
          }
        }));
      }
      w.onResponseFromFaces = dojo.hitch(this, function(data) {
        if (!data.topsearch.enabled) {
          data.topsearch.enabled = true;
        }
        var _4f9 = {
          key: "w3v17_search_transformation;hepatel@us.ibm.com",
          minQueryLength: 1,
          searchDelay: 200,
          xhrTimeout: 20000,
          topsearch: {
            enabled: true,
            host: "//topsearch.ciolab.ibm.com",
            maxResults: 4,
            headerLabel: "Web pages",
            iconUrl: ibmweb.config.imageUrl + "/w3/w3_search_pages_x24_icon.png",
            moreResultsUrl: "//w3.ibm.com/search/do/search?qt=${query}",
            moreResultsLabel: "See more w3 results",
            prefixDom: this.pagesTSExplanation,
            ariaLiveLabel: _4f2,
            onclick: function(link) {
              location.href = link.url;
            }
          },
          faces: {
            enabled: true,
            moreResultsUrl: "//faces.tap.ibm.com#${query}",
            headerLabel: "People",
            maxResults: 3,
            ariaLiveLabel: _4f2,
            prefixDom: this.facesExplanation,
            moreResultsLabel: _4f3,
            moreResultsLabel2: _4f4,
            largeImageSize: 24,
            smallImageSize: 24,
            onclick: dojo.hitch(this, function(_4fa) {
              if (_4f8[this.selectedIdx].url.indexOf("/newbp") === -1) {
                location.href = _4f5.replace("${email}", _4fa.email);
              } else {
                location.href = _4f6.replace("${uid}", _4fa.uid);
              }
            })
          }
        };
        if (dojo.indexOf(["http:", "https:"], location.protocol) == -1) {
          _4f9.faces.host = "//faces.tap.ibm.com";
          _4f9.faces.imageHost = "//images.tap.ibm.com";
        }
        for (feature in data) {
          if (feature in _4f9) {
            dojo.mixin(_4f9[feature], data[feature]);
          } else {
            _4f9[feature] = data[feature];
          }
        }
        _4f7 = FacesTypeAhead.init(this.input, _4f9);
        this.selectScope(0, true);
      });
      dojo.io.script.get({
        url: (dojo.indexOf(["http:", "https:"], location.protocol) == -1 ? "http:" : "") + "//faces.tap.ibm.com/j/w3.js"
      });
    };
    sb.prototype.submitForm = function(e) {
      dojo.stopEvent(dojo.fixEvent(e));
      if (typeof(_4f8[this.selectedIdx].onSubmit) == "function") {
        if (!_4f8[this.selectedIdx].onSubmit(this.input.value)) {
          return false;
        }
      }
      location.href = _4f8[this.selectedIdx].url.replace(/\$\{query\}/g, encodeURIComponent(this.input.value));
    };
    sb.prototype.createScopeButton = function() {
      var a = this.scope = document.createElement("a"),
        icon = this.icon = document.createElement("span"),
        _4fb = document.createElement("span"),
        _4fc = $(document.createElement("span")),
        _4fd = document.createElement("span"),
        _4fe = document.createElement("span");
      _4fd.innerHTML = "&#9660;";
      _4fd.className = "ibm-access";
      _4fe.className = "arrow";
      _4fb.id = _4ef;
      _4fb.style.position = "absolute";
      _4fb.style.visibility = "hidden";
      _4fb.style.textIndent = "-999em";
      _4fc.attr("id", _4f0).style({
        position: "absolute",
        visibility: "hidden",
        textIndent: "-999em"
      });
      _4fc.innerHTML = _4f1;
      $(a).attr("id", "ibm-scope-button").attr("href", "javascript:void(0)").attr("role", "button").attr("aria-haspopup", "true").attr("aria-owns", "ibm-scope-dropdown").attr("aria-labelledby", _4ef).attr("title", _4f1).append(icon).append(_4fc[0]).append(_4fb).append(_4fe).append(_4fd).addClass("scopeButton").onclick(dojo.hitch(this, "openMenu"));
      this.openMenu();
      this.closeMenu();
      dojo.attr(this.input, "aria-describedby", _4ef);
      dojo.connect(a, "onfocus", this, function(_4ff) {
        dojo.addClass(dojo.query("#ibm-search-module input#q")[0], "ibm-active");
      });
      dojo.connect(a, "onblur", this, function(_500) {
        if (!dojo.hasClass(a, "ibm-active")) {
          dojo.removeClass(dojo.query("#ibm-search-module input#q")[0], "ibm-active");
        }
      });
    };
    sb.prototype.openMenu = function(evt) {
      var m = this.getMenu();
      dijit.popup.open({
        popup: m,
        around: this.scope,
        orient: (dojo._isBodyLtr() ? {
          "BL": "TL",
          "BR": "TR",
          "TL": "BL",
          "TR": "BR"
        } : {
          "BR": "TR",
          "BL": "TL",
          "TR": "BR",
          "TL": "BL"
        }),
        onCancel: dojo.hitch(this, "closeMenu")
      });
      $("#ibm-search-module .scopeButton").addClass("ibm-active");
      m.focus();
      if (typeof this.selectedIdx == "number") {
        m.focusChild(this.menuItems[this.selectedIdx]);
      }
      var _501 = $("#ibm-scope-button_dropdown")[0];
      if (!!window.pageYOffset && window.pageYOffset != 0) {
        _501.style.top = (_501.style.top.substring(0, _501.style.top.indexOf("px")) - window.pageYOffset + 5) + "px";
      } else {
        if (!!document.documentElement.scrollTop && document.documentElement.scrollTop != 0) {
          _501.style.top = (_501.style.top.substring(0, _501.style.top.indexOf("px")) - document.documentElement.scrollTop + 5) + "px";
        } else {
          _501.style.top = (_501.style.top.substring(0, _501.style.top.indexOf("px")) - 0 + 5) + "px";
        }
      }
    };
    sb.prototype.closeMenu = function() {
      if (this.menu) {
        dijit.popup.close(this.menu);
      }
      dojo.removeClass(dojo.query("#ibm-search-module input#q")[0], "ibm-active");
      dijit.focus(this.scope);
      $("#ibm-search-module .scopeButton").removeClass("ibm-active");
    };
    sb.prototype.getMenu = function() {
      if (!this.menu) {
        var menu = this.menu = new dijit.Menu({
          style: "display: none;",
          id: "ibm-scope-dropdown"
        });
        var _502 = this.menuItems = new Array(_4f8.length);
        var o, that = this;
        for (var i = 0; i < _4f8.length; i++) {
          o = _4f8[i];
          _502[i] = new dijit.MenuItem({
            label: o.label,
            "class": "SearchBar",
            iconClass: o.iconClass || null,
            onClick: function(idx) {
              return function() {
                that.selectScope(idx);
              };
            }(i)
          });
          menu.addChild(_502[i]);
          if (o.separator) {
            menu.addChild(new dijit.MenuSeparator());
          }
        }
        dojo.connect(this.menu, "_onBlur", dojo.hitch(this, "closeMenu"));
      }
      return this.menu;
    };
    sb.prototype.selectScope = function(idx, _503) {
      this.selectedIdx = idx;
      this.icon.className = _4f8[idx].iconClass + " icon";
      if (!_503) {
        this.closeMenu();
      }
      $("#" + _4ef).html(_4f8[idx].label + " selected");
      this.changePlaceholderText(_4f8[idx].description);
      (_4f8[idx].facesEnabled) ? _4f7[0].enable("faces"): _4f7[0].disable("faces");
      (_4f8[idx].topsearchEnabled) ? _4f7[0].enable("topsearch"): _4f7[0].disable("topsearch");
      if (!!_4f7 && _4f7.length > 0) {
        if (_4f8[idx].typeAheadEnabled) {
          _4f7[0].enable();
        } else {
          _4f7[0].disable();
        }
      }
    };
    sb.prototype.changePlaceholderText = function(text) {
      this.placeholderText = text;
      if ("placeholder" in this.input) {
        this.input.placeholder = text;
      } else {
        if (this.input.placeholderStateOn) {
          this.input.value = text;
        }
      }
      this.input.setAttribute("aria-label", text);
    };
  });
}
if (!dojo._hasResource["ibmweb.dynnav.emailthispage"]) {
  dojo._hasResource["ibmweb.dynnav.emailthispage"] = true;
  dojo.provide("ibmweb.dynnav.emailthispage");
  dojo.declare("ibmweb.dynnav.emailthispage", [ibmweb.dynnav._module], {
    init: function() {
      return true;
    },
    onData: function(data) {
      var _504 = dojo.cookie("etpConf");
      if (!_504) {
        return false;
      }
      dojo["require"]("dijit.Dialog");
      dojo["require"]("ibmweb.dynnav.emailthispagewidget");
      dojo.addOnLoad(function() {
        var _505 = new dijit.Dialog();
        var etp = new ibmweb.dynnav.emailthispagewidget({
          confirmation: true,
          dialog_id: _505.id,
          etpConf: dojo.cookie("etpConf")
        });
        _505.attr("content", etp.domNode);
        _505.show();
      });
    },
    showForm: function() {
      if (dojo.exists("ibmweb.dynnav.emailthispagewidget") && dijit.byId("emailthispagew")) {
        var _506 = dijit.byId("emailthispagew");
        _506.destroy();
      }
      dojo["require"]("dijit.Dialog");
      dojo["require"]("ibmweb.dynnav.emailthispagewidget");
      dojo.addOnLoad(function() {
        var _507 = new dijit.Dialog();
        var etp = new ibmweb.dynnav.emailthispagewidget({
          dialog_id: _507.id
        });
        _507.attr("content", etp.domNode);
        etp.startup();
        _507.show();
      });
      return false;
    }
  });
}
if (!dojo._hasResource["ibmweb.dynnav.sbs"]) {
  dojo._hasResource["ibmweb.dynnav.sbs"] = true;
  dojo.provide("ibmweb.dynnav.sbs");
  dojo.declare("ibmweb.dynnav.sbs", ibmweb.dynnav._module, {
    init: function() {
      return ibmweb.dynnav.isServiceEnabled("sbs");
    },
    onData: function(_508) {
      if (ibmweb.dynnav.PMM.footerToolData.length == 0) {
        return false;
      }
      dojo.create("div", {
        id: "ibm-social-tools"
      }, "ibm-masthead", "last");
      ibmweb.sbs.register(dojo.byId("ibm-social-tools"));
      var ul = dojo.query("#ibm-social-tools ul");
      if (ul.length > 0) {
        ul = dojo.query("#ibm-social-tools ul")[0];
      }
      dojo.connect(ul.firstChild.firstChild, "onfocus", this, function(_509) {
        ul.className = "ibm-active";
      });
      dojo.query("#ibm-social-tools a").forEach(function(item) {
        dijit.setWaiRole(item, "button");
        item.tabIndex = -1;
        dojo.connect(item, "onfocus", this, function(_50a) {
          item.tabIndex = 0;
        });
      });
      ul.firstChild.firstChild.tabIndex = 0;
      dojo.connect(dojo.byId("ibm-social-tools"), "onkeypress", this, function(_50b) {
        if (_50b.shiftKey && _50b.charOrCode == dojo.keys.TAB) {
          ul.className = "";
          if (_50b.target != ul.firstChild.firstChild) {
            ul.firstChild.firstChild.focus();
            ul.firstChild.firstChild.tabIndex = -1;
          }
        }
        if (!_50b.shiftKey && _50b.charOrCode == dojo.keys.TAB) {
          ul.className = "";
        }
        if (_50b.keyCode == dojo.keys.DOWN_ARROW || _50b.keyCode == dojo.keys.RIGHT_ARROW) {
          dojo.stopEvent(_50b);
          var next = _50b.target.parentNode.nextSibling;
          if (next && next.firstChild && next.firstChild.nodeName == "A") {
            next.firstChild.focus();
            _50b.target.tabIndex = -1;
          } else {
            next = _50b.target.parentNode.parentNode.firstChild;
            if (next && next.firstChild && next.firstChild.nodeName == "A") {
              next.firstChild.focus();
              _50b.target.tabIndex = -1;
            }
          }
        }
        if (_50b.keyCode == dojo.keys.UP_ARROW || _50b.keyCode == dojo.keys.LEFT_ARROW) {
          dojo.stopEvent(_50b);
          var prev = _50b.target.parentNode.previousSibling;
          if (prev && prev.firstChild && prev.firstChild.nodeName == "A") {
            prev.firstChild.focus();
            _50b.target.tabIndex = -1;
          } else {
            prev = _50b.target.parentNode.parentNode.lastChild;
            if (prev && prev.firstChild && prev.firstChild.nodeName == "A") {
              prev.firstChild.focus();
              _50b.target.tabIndex = -1;
            }
          }
        }
        return false;
      });
      dojo.query("#ibm-social-tools").onmouseover(function(e) {
        dojo.stopEvent(e);
      });
      dojo.publish("/ibmweb/dynnav/sbs/finished");
    }
  });
}
if (!dojo._hasResource["ibmweb.dynnav.w3-greeting"]) {
  dojo._hasResource["ibmweb.dynnav.w3-greeting"] = true;
  dojo.provide("ibmweb.dynnav.w3-greeting");
  dojo.declare("ibmweb.dynnav.w3-greeting", ibmweb.dynnav._module, {
    init: function() {
      if (dojo.cookie("w3ibmProfile") && ibmweb.config.greeting.createSignOutEditProfile_FOR_TESTING) {
        var what = ibmweb.config.greeting.createSignOutEditProfile_FOR_TESTING,
          _50c = "<a id=\"portalLogoutLink\" href=\"#\" tabindex=\"-1\" style=\"padding:0 5px 1px 2px;\" role=\"button\" aria-label=\"Sign out\" aria-describedby=\"ibm-user-name\">Sign out</a>",
          _50d = "<a href=\"https://w3.ibm.com/workplace/myportal/profile\" role=\"button\" tabindex=\"-1\" aria-label=\"Edit Profile\" aria-describedby=\"ibm-user-name\">Edit Profile</a>",
          code = "";
        code += "<li role=\"presentation\">";
        if (what == "signOut" || what == "both") {
          code += _50c;
        }
        if (what == "both") {
          code += "-&nbsp;";
        }
        if (what == "editProfile" || what == "both") {
          code += _50d;
        }
        code += "</li>";
        dojo.create("ul", {
          role: "presentation",
          innerHTML: code
        }, dojo.query("#ibm-mast-options")[0]);
      }
      ibmweb.dynnav.isGreetingErrorThrew = false;
      return ibmweb.dynnav.isServiceEnabled("greeting");
    },
    onLoad: function() {
      if (!dojo.byId("ibm-sso")) {
        if (dojo.query("#ibm-mast-options ul").length == 1) {
          var _50e = dojo.query("#ibm-mast-options ul"),
            _50f = dojo.create("ul", {
              "role": "presentation"
            }, _50e[0], "after"),
            _510 = dojo.query("#portalLogoutLink");
          dojo.create("li", {
            id: "ibm-sso"
          }, _50f);
          if (_510.length == 1) {
            var _511 = _510.parent("li"),
              _512 = _511.parent("ul");
            _511.appendTo(_50f);
            dojo.destroy(_512[0]);
          }
        } else {
          if (dojo.query("#ibm-mast-options ul").length > 1) {
            var _50f = dojo.query("#ibm-mast-options ul:nth-child(2)");
            dojo.create("li", {
              id: "ibm-sso"
            }, _50f[0], "first");
          }
        }
      }
    },
    onData: function(_513) {
      this._loadGreeting(_513);
    },
    _loadGreeting: function(_514) {
      var _515 = dojo.cookie("w3ibmProfile");
      if (_515) {
        var hkey = _515.split("|")[0];
        var _516 = function(_517, _518) {
          dojo.empty("ibm-sso");
          var _519 = dojo.create("span", {
            className: "ibm-thumbnail"
          });
          var _51a = ibmweb.config.w3UserThumbnailUrl + _518;
          dojo.create("img", {
            src: _51a,
            alt: _517 + " " + "profile picture",
            width: 25,
            height: 25
          }, _519);
          dojo.place(_519, dojo.byId("ibm-sso"));
          var _51b = "Welcome back ";
          if (_514 && _514.ssoFormData && _514.ssoFormData.returnMsg && _514.ssoFormData.returnMsg.label) {
            _51b = _514.ssoFormData.returnMsg.label + " ";
          }
          var link = dojo.create("span", {
            id: "ibm-welcome-msg",
            innerHTML: _51b,
            className: "ibm-user"
          });
          var home = dojo.query("#ibm-home a");
          (home.length > 0) ? dojo.attr(home[0], "aria-describedby", "ibm-welcome-msg"): null;
          var _51c = dojo.create("span", {
            id: "ibm-user-name",
            innerHTML: _517
          });
          dojo.place(_51c, link, "last");
          dojo.place(link, _519, "after");
          ibmweb.config.megamenu._w3SwapDesign_welcomeWidth = dojo.coords(dojo.byId("ibm-welcome-msg"), false).w + 55;
        };
        var _51d = ibmweb.storage.getItem("w3UserName_" + hkey);
        var _51e = ibmweb.storage.getItem("w3UserEmail_" + hkey);
        if (_51d && _51e) {
          _516(_51d, _51e);
        } else {
          window["W3UserServiceCallback"] = function(data) {
            ibmweb.dynnav.__checkStringForUserService = true;
            if (ibmweb.dynnav.isGreetingErrorThrew || !data || !data.User) {
              return;
            }
            var _51f = data.User.NickName == "" ? data.User.CommonName : data.User.NickName;
            _516(_51f, data.User.Email);
            ibmweb.storage.setItem("w3UserName_" + hkey, _51f, 3600 * 8);
            ibmweb.storage.setItem("w3UserEmail_" + hkey, data.User.Email, 3600 * 8);
          };
          dojo.io.script.get({
            url: ibmweb.config.w3UserServiceUrl + hkey + "/callback/W3UserServiceCallback",
            checkString: "ibmweb.dynnav.__checkStringForUserService",
            timeout: ibmweb.config.w3UserServiceTimeout,
            error: function(_520, _521) {
              ibmweb.dynnav.isGreetingErrorThrew = true;
              var _522 = function(data) {
                dojo.empty("ibm-sso");
                var _523 = dojo.create("span", {
                  className: "ibm-thumbnail"
                });
                dojo.place(_523, dojo.byId("ibm-sso"));
                var _524 = "Sign in";
                if (data && data.ssoFormData && data.ssoFormData.signIn && data.ssoFormData.signIn.label) {
                  _524 = data.ssoFormData.signIn.label;
                }
                var _525 = dojo.create("a", {
                  innerHTML: _524,
                  href: ibmweb.config.w3logonUrl + ibmweb.util.getUrl(),
                  className: "ibm-sso-signin",
                  tabIndex: -1,
                  onfocus: dojo.hitch(this, function(_526) {
                    _526.target.tabIndex = (ibmweb.config.config == "w3") ? 3 : 0;
                    if (dojo.byId("ibm-menu-links").className == "ibm-access") {
                      dojo.publish("/ibm/dynnav/megamenu/expandMenu");
                    }
                  })
                });
                dojo.place(_525, dojo.byId("ibm-sso"), "last");
              };
              _522(_514);
              dojo.io.script.remove(_521.id);
            }
          });
        }
      } else {
        this._buildStaticSignInLinkAndPicture(_514);
      }
    },
    _buildStaticSignInLinkAndPicture: function(data) {
      dojo.empty("ibm-sso");
      var _527 = dojo.create("span", {
        className: "ibm-thumbnail"
      });
      dojo.place(_527, dojo.byId("ibm-sso"));
      var _528;
      if (ibmweb.dynnav.isServiceEnabled("signin")) {
        var _529 = "Sign in";
        if (data && data.ssoFormData && data.ssoFormData.signIn && data.ssoFormData.signIn.label) {
          _529 = data.ssoFormData.signIn.label;
        }
        _528 = dojo.create("a", {
          innerHTML: _529,
          href: ibmweb.config.w3logonUrl + ibmweb.util.getUrl(),
          className: "ibm-sso-signin",
          tabIndex: -1,
          onfocus: dojo.hitch(this, function(_52a) {
            _52a.target.tabIndex = (ibmweb.config.config == "w3") ? 3 : 0;
            if (dojo.byId("ibm-menu-links").className == "ibm-access") {
              dojo.publish("/ibm/dynnav/megamenu/expandMenu");
            }
          })
        });
      } else {
        var _52b = "Welcome";
        if (data && data.ssoFormData && data.ssoFormData.welcome && data.ssoFormData.welcome.label) {
          _52b = data.ssoFormData.welcome.label;
        }
        _528 = dojo.create("span", {
          id: "ibm-welcome-msg",
          innerHTML: _52b,
          className: "ibm-user"
        });
        var home = dojo.query("#ibm-home a");
        (home.length > 0) ? dojo.attr(home[0], "aria-describedby", "ibm-welcome-msg"): null;
      }
      dojo.place(_528, dojo.byId("ibm-sso"), "last");
    },
    onError: function() {
      this._buildStaticSignInLinkAndPicture(null);
    }
  });
}
if (!dojo._hasResource["ibmweb.dynnav.aria"]) {
  dojo._hasResource["ibmweb.dynnav.aria"] = true;
  dojo.provide("ibmweb.dynnav.aria");
  dojo.declare("ibmweb.dynnav.aria", [ibmweb.dynnav._module], {
    enabled: true,
    _lock: false,
    init: function() {
      return true;
    },
    onLoad: function() {
      dojo.attr(dojo.body(), "aria-busy", "true");
      if (!dojo.byId("ibm-com")) {
        dojo.body().id = "ibm-com";
      }!(dojo.query("*[role=\"main\"]").length > 0) ? dojo.query("#ibm-top > #ibm-leadspace-head, #ibm-top > #ibm-content-nav, #ibm-top > #ibm-pcon").wrapAll("<div id=\"ibm-access-cntr\"></div>"): null;
      if (dojo.byId("ibm-com").className.indexOf("dijit_a11y") != -1) {
        if (ibmweb.config.config == "www" && ibmweb.info.v17 && !document.getElementById("ibm-hcontrast-head") && !!dojo.byId("ibm-masthead")) {
          dojo.place("<img src=\"//www.ibm.com/i/v17/_hcontrast_head.png\" width=\"100%\" height=\"100%\" id=\"ibm-hcontrast-head\">", dojo.byId("ibm-masthead"), "first");
        }
        if (ibmweb.info.v17 && !document.getElementById("ibm-hcontrast-home") && !!dojo.byId("ibm-home")) {
          if (ibmweb.config.config == "www") {
            dojo.place("<img src=\"//www.ibm.com/i/v17/t/ibm-logo.png\" id=\"ibm-hcontrast-home\">", dojo.query("#ibm-home a")[0], "first");
          } else {
            dojo.place("<img src=\"//www.ibm.com/i/v17/w3/w3_logos_sprite.png\" id=\"ibm-hcontrast-home\">", dojo.query("#ibm-home a")[0], "first");
          }
        }
      }
      dojo.query("a[onclick*=\"ibmweb.overlay.show\"]").forEach(function(elem) {
        dojo.attr(elem, "role", "button");
      });
      dojo.subscribe("/ibmweb/form/initialized", function() {
        var _52c = dojo.query("input[type=radio], input[type=checkbox]");
        dojo.forEach(_52c, function(elem, i) {
          if (dojo.attr(elem, "aria-pressed") == "true") {
            dojo.attr(elem, "aria-checked", "true");
          } else {
            dojo.attr(elem, "aria-checked", "false");
          }
          dojo.removeAttr(elem, "aria-pressed");
        });
      });
      return true;
    },
    onData: function(_52d) {
      if (!_52d) {
        return;
      }
      var _52e = _52d.accessibilityData;
      if (dojo.byId("ibm-masthead")) {
        !(dojo.query("*[role=\"banner\"]").length > 0) ? dijit.setWaiRole("ibm-masthead", "banner"): null;
      }
      var _52f = dojo.query("#ibm-geo a");
      _52f = _52f.length > 0 ? dojo.query("#ibm-geo a")[0] : null;
      if (!!_52f) {
        dojo.query("#ibm-geo span.ibm-access").forEach(dojo.destroy);
        dijit.setWaiRole(_52f, "button");
        dijit.setWaiState(_52f, "label", ibmweb.dynnav.PMM.accessibilityData.locselector + " " + _52f.innerHTML + " " + ibmweb.dynnav.PMM.accessibilityData.linkseld);
      }
      if (!ibmweb.config.appmast.enabled) {
        var _530 = dojo.query("#ibm-masthead #ibm-geo a, #ibm-masthead #ibm-home a, #ibm-masthead #ibm-search-module a, #ibm-masthead #ibm-search-module input");
        _530.forEach(function(item) {
          dojo.connect(item, "onfocus", this, function(_531) {
            if (dojo.byId("ibm-menu-links").className == "ibm-access") {
              dojo.publish("/ibm/dynnav/megamenu/expandMenu");
            }
          });
        });
      }
      var home = dojo.query("#ibm-home a");
      if (home.length > 0) {
        var _532 = home.innerHTML.toString();
        _532 = _532.replace(/^\s+|\s+$/g, "");
        if (_532 == "" && _532.length == 0) {
          home.innerHTML = "IBM&reg;";
        }
        home[0].tabIndex = 0;
      }
      if (dojo.byId("ibm-mast-options")) {
        if (ibmweb.config.config == "w3") {
          var _533 = dojo.byId("ibm-mast-options");
          dijit.setWaiRole(_533, "toolbar");
          dojo.attr("ibm-mast-options", "aria-label", "Masthead Navigation");
          var _534 = dojo.query("#ibm-mast-options ul");
          _534.forEach(function(item) {
            dijit.setWaiRole(item, "presentation");
          });
        } else {
          var _534 = dojo.query("#ibm-mast-options ul");
          if (_534.length > 0) {
            _534 = _534[0];
            dijit.setWaiRole(_534, "toolbar");
            dojo.attr(_534, "aria-label", "Masthead Navigation");
          }
        }
        var _535 = dojo.query("#ibm-mast-options ul li");
        _535.forEach(function(item) {
          dijit.setWaiRole(item, "presentation");
        });
        var optA = dojo.query("#ibm-mast-options a");
        var home = dojo.query("#ibm-home a");
        optA.forEach(function(item) {
          dijit.setWaiRole(item, "button");
          item.tabIndex = -1;
          if (ibmweb.config.config == "w3") {
            if (home.length > 0 && item != home[0]) {
              dojo.attr(item, "aria-label", item.innerHTML);
              (item.className != "ibm-sso-signin") ? dojo.attr(item, "aria-describedby", "ibm-user-name"): null;
            }
          }
        });
        if (optA.length > 0) {
          optA[0].tabIndex = 0;
        }
        dojo.connect(dojo.byId("ibm-mast-options"), "onkeypress", this, function(_536) {
          var _537 = dojo.query("#ibm-mast-options a");
          if (!this._lock && (_536.keyCode == dojo.keys.RIGHT_ARROW || _536.keyCode == dojo.keys.DOWN_ARROW || _536.keyCode == dojo.keys.LEFT_ARROW || _536.keyCode == dojo.keys.UP_ARROW)) {
            if (_537.length > 0) {
              _537.forEach(function(item) {
                item.tabIndex = -1;
                dojo.connect(item, "onfocus", this, function(_538) {
                  _537.forEach(function(i) {
                    i.tabIndex = -1;
                  });
                  item.tabIndex = 0;
                });
              });
            }
            this._lock = true;
          }
          if (_536.keyCode == dojo.keys.RIGHT_ARROW || _536.keyCode == dojo.keys.DOWN_ARROW) {
            dojo.stopEvent(_536);
            var curr = _536.target;
            if (_537.length > 0) {
              for (var i = 0; i < _537.length; i++) {
                if ((curr == _537[i]) && (i < (_537.length - 1))) {
                  _537[i + 1].focus();
                  _536.target.tabIndex = -1;
                  break;
                } else {
                  if ((curr == _537[i]) && (i == (_537.length - 1))) {
                    _537[0].focus();
                    _536.target.tabIndex = -1;
                    break;
                  }
                }
              }
            }
          }
          if (_536.keyCode == dojo.keys.LEFT_ARROW || _536.keyCode == dojo.keys.UP_ARROW) {
            dojo.stopEvent(_536);
            var curr = _536.target;
            if (_537.length > 0) {
              for (var i = 0; i < _537.length; i++) {
                if ((curr == _537[i]) && (i > 0)) {
                  _537[i - 1].focus();
                  _536.target.tabIndex = -1;
                  break;
                } else {
                  if ((curr == _537[i]) && (i == 0)) {
                    _537[_537.length - 1].focus();
                    _536.target.tabIndex = -1;
                    break;
                  }
                }
              }
            }
          }
          return false;
        });
      }
      if (dojo.byId("ibm-universal-nav")) {
        dijit.setWaiRole("ibm-universal-nav", "navigation");
        dojo.attr("ibm-universal-nav", "aria-label", _52e.sitemap || "Site map");
      }
      if (dojo.byId("ibm-menu-links")) {
        dojo.attr("ibm-menu-links", "aria-label", _52e.sitemap || "Site map");
      }
      if (dojo.byId("ibm-search-module")) {
        dijit.setWaiRole("ibm-search-module", "search");
        dojo.attr("ibm-search-module", "aria-label", _52e.ibm_search || "IBM searchbar");
        var _539 = dojo.query("#ibm-search-module a, #ibm-search-module input");
        _539.forEach(function(item) {
          dojo.connect(item, "onfocus", this, function(_53a) {
            dojo.addClass(dojo.query("#ibm-search-module input#q")[0], "ibm-active");
          });
          dojo.connect(item, "onblur", this, function(_53b) {
            dojo.removeClass(dojo.query("#ibm-search-module input#q")[0], "ibm-active");
          });
        });
        if (ibmweb.config.config == "w3") {
          var _53c = dojo.query("#ibm-search-module .scopeButton");
          if (_53c.length > 0) {
            dojo.connect(_53c[0], "onkeypress", this, function(_53d) {
              if (_53d.shiftKey && _53d.charOrCode == dojo.keys.TAB) {
                dojo.stopEvent(_53d);
                var _53e = dojo.query("#ibm-menu-links a");
                if (_53e.length > 0) {
                  _53e.forEach(function(item) {
                    if (item.tabIndex == 0) {
                      item.focus();
                    }
                  });
                }
              }
            });
          }
        }
      }
      if (dojo.byId("ibm-social-tools") && dojo.style(dojo.byId("ibm-social-tools"), "display") != "none") {
        dijit.setWaiRole(dojo.byId("ibm-social-tools"), "navigation");
        dojo.attr(dojo.byId("ibm-social-tools"), "aria-label", _52e.tools || "Social tools");
        var _53f = dojo.query("#ibm-social-tools ul");
        if (_53f.length > 0) {
          dijit.setWaiRole(_53f[0], "toolbar");
        }
        dojo.attr(_53f[0], "aria-label", "Social tool list");
      }
      if (dojo.byId("ibm-navigation-trail") && dojo.style(dojo.byId("ibm-navigation-trail"), "display") != "none") {
        dijit.setWaiRole(dojo.byId("ibm-navigation-trail"), "directory");
        dojo.attr(dojo.byId("ibm-navigation-trail"), "aria-label", _52e.bcrumb || "Bread Crumbs");
      }
      if (dojo.byId("ibm-content-nav") && dojo.style(dojo.byId("ibm-content-nav"), "display") != "none") {
        (dojo.query("divibm-content-nav #ibm-primary-tabs.ibm-dyntabs").length > 0) ? dijit.setWaiRole(dojo.byId("ibm-content-nav"), "navigation"): null;
        var _540 = dojo.query("div#ibm-content-nav #ibm-primary-tabs");
        if (_540.length > 0) {
          if (dojo.style(_540[0], "display") != "none" && !(dojo.query("div#ibm-content-nav #ibm-primary-tabs.ibm-dyntabs").length > 0)) {
            dijit.setWaiRole(dojo.byId("ibm-primary-tabs"), "navigation");
          }
          var _541 = dojo.query("div#ibm-content-nav #ibm-secondary-tabs");
          if (_541.length > 0) {
            if (dojo.style(_541[0], "display") != "none") {
              if (dojo.query("div#ibm-content-nav #ibm-secondary-tabs.ibm-dyntabs").length > 0) {
                var _542 = dojo.query("div#ibm-content-nav #ibm-primary-tabs.ibm-dyntabs .ibm-tabs li.ibm-active a");
                if (_542.length > 0) {
                  dojo.attr(_541[0], "aria-label", _542[0].innerHTML);
                }
              } else {
                dijit.setWaiRole(_541[0], "navigation");
                var _542 = dojo.query("div#ibm-content-nav #ibm-primary-tabs .ibm-tabs li.ibm-active a");
                if (_542.length > 0) {
                  dojo.attr(_541[0], "aria-label", _542[0].innerHTML);
                  if (dojo.query("ul.ibm-tabs", _541[0])[0] && _542.length > 0) {
                    dojo.attr(dojo.query("ul.ibm-tabs", _541[0])[0], "aria-label", _542[0].innerHTML);
                  }
                }
              }
            }
          }
        }
      }
      var _543 = [];
      var dtl = dojo.query(".ibm-dyntabs .ibm-tabs");
      var tl = dojo.query(".ibm-tabs");
      if (dtl.length > 0) {
        tl.forEach(function(tle) {
          var flag = false;
          for (var i = 0; i < dtl.length; i++) {
            if (dtl[i] == tle) {
              flag = true;
              break;
            }
          }
          if (!flag) {
            _543.push(tle);
          }
        });
      } else {
        _543 = tl;
      }
      if (_543.length > 0) {
        for (var i = 0; i < _543.length; i++) {
          dijit.setWaiRole(_543[i], "tablist");
          if (ibmweb.config.config != "w3") {
            dojo.attr(_543[i].parentNode, "aria-label", dojo.attr(_543[i].parentNode, "aria-label") || "Tab Navigation");
            dijit.setWaiRole(_543[i].parentNode, dijit.getWaiRole(_543[i].parentNode) || "navigation");
          }
          dojo.query("li", _543[i]).forEach(function(item) {
            dijit.setWaiRole(item, "presentation");
          });
          var _544 = dojo.query("li span.ibm-access", _543[i])[0];
          if (typeof(_544) != "undefined") {
            _544.parentNode.removeChild(_544);
          }
          var _545 = dojo.query("a", _543[i]);
          _545.forEach(function(item) {
            dijit.setWaiRole(item, "tab");
            dojo.attr(item, "aria-selected", "false");
            dojo.attr(item, "tabindex", "-1");
            dojo.attr(item, "aria-label", item.innerHTML);
          });
          dojo.query(".ibm-tabs li.ibm-active a, .ibm-tabs li.ibm-highlight-tab a ").forEach(function(item) {
            dojo.attr(item, "aria-selected", "true");
            dojo.attr(item, "tabindex", "0");
          });
        }
      }
      var _546 = dojo.query(".ibm-tab-section h2.ibm-access")[0];
      if (typeof _546 !== "undefined") {
        _546.parentNode.removeChild(_546);
      }
      if (dojo.byId("ibm-navigation") && dojo.style(dojo.byId("ibm-navigation"), "display") != "none") {
        dijit.setWaiRole("ibm-navigation", "navigation");
        dojo.attr("ibm-navigation", "aria-label", "Primary");
        dojo.query("#ibm-navigation h2.ibm-access").style("display", "none");
      }
      if (dojo.byId("ibm-secondary-navigation") && dojo.style(dojo.byId("ibm-secondary-navigation"), "display") != "none") {
        dijit.setWaiRole("ibm-secondary-navigation", "navigation");
        dojo.attr("ibm-secondary-navigation", "aria-label", "Secondary");
      }(function() {
        if (dojo.byId("ibm-content-sidebar") && dojo.style(dojo.byId("ibm-content-sidebar"), "display") != "none") {
          var n = 0;
          (dojo.query("#ibm-content-sidebar #ibm-contact-module")[0]) ? n++ : null;
          (dojo.query("#ibm-content-sidebar #ibm-merchandising-module")[0]) ? n++ : null;
          if (dojo.query("#ibm-content-sidebar *").length > n) {
            dijit.setWaiRole("ibm-content-sidebar", "complementary");
          }
          dojo.attr("ibm-content-sidebar", "aria-label", _52e.sbmain || "Content sidebar");
        }
      })();
      (function() {
        if (dojo.byId("ibm-related-content") && dojo.style(dojo.byId("ibm-related-content"), "display") != "none") {
          var n = 0;
          (dojo.query("#ibm-related-content #ibm-merchandising-module")[0]) ? n++ : null;
          if (dojo.query("#ibm-related-content *").length > n) {
            dijit.setWaiRole("ibm-related-content", "complementary");
          }
          dojo.attr("ibm-related-content", "aria-label", _52e.relcnt || "Related Content");
        }
      })();
      if (dojo.isOpera) {
        dojo.query("#ibm-top a").forEach(function(i) {
          if (dojo.attr(i, "tabindex") == null) {
            i.tabIndex = 0;
          }
        });
      }
      if (dojo.byId("ibm-footer-module")) {
        dijit.setWaiRole("ibm-footer-module", "complementary");
      }
      if (dojo.byId("ibm-footer")) {
        !(dojo.query("*[role=\"contentinfo\"]").length > 0) ? dijit.setWaiRole("ibm-footer", "contentinfo"): null;
        dojo.attr("ibm-footer", "aria-label", _52e.footer || "Footer");
      }
      if (!(dojo.query("*[role=\"main\"]").length > 0)) {
        (dojo.query("#ibm-access-cntr").length > 0) ? dijit.setWaiRole("ibm-access-cntr", "main"): null;
      }
      var _547 = dojo.query(".ibm-form-elem-grp");
      var _548 = 0;
      if (_547.length > 0) {
        _547.forEach(function(_549) {
          var lbl = dojo.query(".ibm-form-grp-lbl", _549);
          dojo.query(".ibm-input-group", _549).forEach(function(_54a, i) {
            if (dojo.hasClass(_54a, "ibm-radio-group")) {
              dijit.setWaiRole(_54a, "radiogroup");
            } else {
              dijit.setWaiRole(_54a, "group");
            }
            if (lbl.length > i) {
              if (!!lbl[i].id || lbl[i].id != "") {
                dojo.attr(_54a, "aria-labelledby", lbl[i].id);
              } else {
                lbl[i].id = "ibm-grp-lbl_" + (_548++);
                dojo.attr(_54a, "aria-labelledby", lbl[i].id);
              }
            }
          });
        });
      }
      dojo.attr(dojo.byId("ibm-com"), "aria-busy", "false");
    }
  });
}
if (!dojo._hasResource["ibmweb.ribbon"]) {
  dojo._hasResource["ibmweb.ribbon"] = true;
  dojo.provide("ibmweb.ribbon");
  dojo.provide("ibmweb.ribbonSlide");
  dojo.provide("ibmweb.ribbonItem");
  dojo.provide("ibmweb.ribbonItemAbstract");
  dojo.provide("ibmweb.ribbonLeadspace");
  dojo.declare("ibmweb.ribbon", [dijit._Widget, dijit._Templated], {
    columns: 3,
    autoscroll: false,
    freeScroll: false,
    interval: 5000,
    leadNoHomeInterval: 15000,
    _interval: null,
    _intervalHandlers: {
      resize: null,
      onmouseleave: null,
      onmouseenter: null
    },
    rotationCount: 0,
    _rotationCount: 0,
    defaultDuration: 500,
    _isBeingAnimated: false,
    _isBegingAutoscroll: false,
    _isLeadSpace: false,
    _isLeadNoHome: false,
    _scrollableNodeAnimObj: null,
    templateString: "<div class='ibm-container-body' dojoAttachPoint='ribbonContainer'>" + "<a class='ibm-ribbon-prev' dojoAttachPoint='scrollLeftButton' role='button' href='#'></a>" + "<div class='ibm-ribbon-pane' dojoAttachPoint='scrollContainer'>" + "<div class='ibm-ribbon-section' dojoAttachPoint='scrollableNode' role='listbox'></div>" + "</div>" + "<a class='ibm-ribbon-next' dojoAttachPoint='scrollRightButton' role='button' href='#'></a>" + "<div class='ibm-ribbon-nav' dojoAttachPoint='navNode' role='toolbar'></div>" + "</div>",
    constructor: function(_54b) {
      this.slides = [];
      this.currentSlideIndex = 0;
      this.originalNode = null;
      this.duration = this.defaultDuration;
      this._durationCopy = this.duration;
      !!_54b && _54b.srcNodeRef && (this.originalNode = _54b.srcNodeRef);
    },
    postCreate: function() {
      var self = this;
      if (self.originalNode) {
        dojo.query(".ibm-columns", this.originalNode).forEach(function(_54c, _54d) {
          var _54e = null;
          if (_54c.id) {
            _54e = _54c.id;
          }
          dojo.query("> *", _54c).forEach(function(item, j) {
            self.addItem(new ibmweb.ribbonItemAbstract({
              srcNodeRef: item
            }), _54d, _54e);
          });
        });
        if (self.originalNode.id) {
          self.domNode.id = self.originalNode.id;
        }
      }
      var _54f = this.domNode.parentNode ? dojo.hasClass(this.domNode.parentNode, "ibm-no-rotate") : true;
      if (this._isLeadSpace && !this.checkHome() && !_54f) {
        this._isLeadNoHome = true;
        this.autoscroll = true;
        this.interval = this.leadNoHomeInterval;
      }
    },
    startup: function() {
      if (!dojo.hasClass(this.ribbonContainer.parentNode, "ibm-ribbon-fixed")) {
        var temp = dojo.coords(this.scrollableNode).h;
        var ob = dojo.query("object", this.id);
        if (ob.length > 0) {
          var max = 0;
          ob.forEach(function(i) {
            if (i.height) {
              if (i.height > max) {
                max = i.height;
              }
            }
          });
          if (max > temp) {
            temp = max;
          }
        }
        dojo.style(this.scrollContainer, "height", temp + "px");
      }
      dojo.style(this.scrollLeftButton, {
        marginTop: (dojo.coords(this.scrollContainer).h / 2) - 12 + "px"
      });
      dojo.style(this.scrollRightButton, {
        marginTop: "-" + ((dojo.coords(this.scrollContainer).h) / 2 + 12) + "px"
      });
      if (ibmweb.meta.cpi == "ilhe" && this._isLeadSpace && this._isLeadNoHome) {
        var pos = (dojo.window.getBox().w - 1050) / 2;
        dojo.style(this.scrollLeftButton, {
          left: "auto",
          right: (pos < 1 ? 1 : pos) + "px"
        });
        dojo.style(this.scrollRightButton, {
          right: "auto",
          left: (pos < 1 ? 1 : pos) + "px"
        });
      }
      dojo.addClass(this.scrollLeftButton, "ibm-disabled");
      if (this.slideCount() <= 1) {
        dojo.addClass(this.scrollRightButton, "ibm-disabled");
        dojo.query(this.navNode).orphan();
      } else {
        dojo.query(":first-child", this.navNode).addClass("ibm-active");
        dojo.query(":first-child", this.navNode)[0].tabIndex = 0;
        dojo.connect(this.scrollLeftButton, "onclick", dojo.hitch(this, this.slideLeft));
        dojo.connect(this.scrollRightButton, "onclick", dojo.hitch(this, this.slideRight));
      }
      if (this.ribbonContainer.parentNode.id != "ibm-promotion-module") {
        dojo.query("div[role=\"document\"] a, div[role=\"document\"] input, div[role=\"document\"]", this.scrollableNode).forEach(function(n) {
          n.tabIndex = -1;
        });
        var _550 = dojo.query("div[role=\"option\"]", this.scrollableNode);
        if (_550.length > 0) {
          dojo.query("div[role=\"document\"] a, div[role=\"document\"] input, div[role=\"document\"]", _550[0]).forEach(function(n) {
            n.tabIndex = 0;
          });
        }
      }
      ibmweb.queue.push(function() {
        return ibmweb.dynnav.isDataLoaded;
      }, dojo.hitch(this, function() {
        dojo.attr(this.scrollLeftButton, "title", ibmweb.dynnav.PMM.buttonData.prev);
        this.scrollLeftButton.innerHTML = ibmweb.dynnav.PMM.buttonData.prev;
        dojo.attr(this.scrollRightButton, "title", ibmweb.dynnav.PMM.buttonData.next);
        this.scrollRightButton.innerHTML = ibmweb.dynnav.PMM.buttonData.next;
      }));
      if (this.autoscroll) {
        if (!this._isLeadNoHome) {
          var _551 = this.slides[0].domNode,
            _552 = dojo.clone(_551);
          dojo.addClass(_552, "ibm-cloned");
          dojo.removeAttr(_552, "id");
          dojo.query("div", _552).removeAttr("id");
          dojo.place(_552, this.scrollableNode);
        }
        var self = this;
        this._interval = setInterval(function() {
          self.next();
        }, this.interval);
        if (!this.freeScroll) {
          this._intervalHandlers.onmouseenter = dojo.connect(this.domNode, "onmouseenter", dojo.hitch(this, function() {
            clearInterval(this._interval);
          }));
          this._intervalHandlers.onmouseleave = dojo.connect(this.domNode, "onmouseleave", dojo.hitch(this, function() {
            clearInterval(this._interval);
            this._interval = setInterval(function() {
              self.next();
            }, this.interval);
          }));
          this._intervalHandlers.resize = dojo.connect(window, "resize", dojo.hitch(this, function() {
            clearInterval(this._interval);
          }));
        }
      }
      if (ibmweb.info.iDevice) {
        var _553 = 0;
        this.domNode.ontouchstart = function(e) {
          _553 = e.touches[0].clientX;
        };
        this.domNode.ontouchmove = function(e) {
          e.preventDefault();
        };
        this.domNode.ontouchend = dojo.hitch(this, function(e) {
          swipeEnd = e.changedTouches[0].clientX;
          var diff = swipeEnd - _553;
          if (diff < 0) {
            this.slideRight();
          } else {
            if (diff > 0) {
              this.slideLeft();
            }
          }
        });
      }
      dojo.forEach(this.slides, function(w) {
        if (w && !w._started && w.startup) {
          w.startup();
        }
      });
      this.inherited(arguments);
    },
    addSlide: function(_554) {
      this.slides.push(_554);
      dojo.place(_554.domNode, this.scrollableNode);
      if (!this.scrollableNode.id) {
        this.scrollableNode.id = this.ribbonContainer.id + "_scrollable";
      }
      var _555 = dojo.create("a", {
        href: "#",
        innerHTML: "Show carousel " + this.slides.length,
        "role": "button",
        "aria-controls": this.scrollableNode.id,
        "IbmCarouselIndex": this.slides.length - 1
      }, this.navNode);
      _555.tabIndex = -1;
      dojo.connect(_555, "onclick", dojo.hitch(this, function(_556) {
        if (dojo.hasClass(_556.target, "ibm-ribbon-view")) {
          slideToIdx = dojo.attr(_556.target.parentNode, "IbmCarouselIndex") * 1;
        } else {
          slideToIdx = dojo.attr(_556.target, "IbmCarouselIndex") * 1;
        }
        this.slideTo(slideToIdx, _556);
      }));
      dojo.connect(_555, "keypress", dojo.hitch(this, function(_557) {
        if (_557.keyCode == dojo.keys.TAB && !_557.shiftKey) {
          _557.target.parentNode.lastChild.focus();
        }
        if (_557.keyCode == dojo.keys.TAB && _557.shiftKey) {
          _557.target.parentNode.firstChild.focus();
        }
        if (_557.keyCode == dojo.keys.DOWN_ARROW || _557.keyCode == dojo.keys.RIGHT_ARROW) {
          if (!!_557.target.nextSibling) {
            dojo.stopEvent(_557);
            _557.target.nextSibling.focus();
          } else {
            dojo.stopEvent(_557);
            _557.target.parentNode.firstChild.focus();
          }
        }
        if (_557.keyCode == dojo.keys.UP_ARROW || _557.keyCode == dojo.keys.LEFT_ARROW) {
          if (!!_557.target.previousSibling) {
            dojo.stopEvent(_557);
            _557.target.previousSibling.focus();
          } else {
            dojo.stopEvent(_557);
            _557.target.parentNode.lastChild.focus();
          }
        }
        if (_557.keyCode == dojo.keys.ENTER || _557.keyCode == dojo.keys.SPACE || _557.keyCode == 0) {
          slideToIdx = dojo.attr(_557.target, "IbmCarouselIndex") * 1;
          this.slideTo(slideToIdx, _557);
        }
      }));
      return _554;
    },
    _focusSlideContent: function(_558) {
      var _559 = dojo.query("div[role=\"document\"]", this.scrollableNode.childNodes[_558]);
      if (_559[0]) {
        _559[0].focus();
      }
    },
    addItem: function(item, _55a, _55b) {
      if (_55a == undefined || _55a < 0 || _55a >= this.slides.length) {
        if (_55a && _55a >= this.slides.length) {
          this.addSlide(new ibmweb.ribbonSlide({
            pid: _55b
          }));
        }
        if (!_55a && (this.slides.length == 0 || this.slides[this.slides.length - 1].getChildren().length == this.columns)) {
          this.addSlide(new ibmweb.ribbonSlide({
            pid: _55b
          }));
        }
        _55a = this.slides.length - 1;
      }
      this.slides[_55a].addChild(item);
      return item;
    },
    checkHome: function() {
      if (dojo.attr(this.ribbonContainer.parentNode, "id") == "ibm-leadspace-head" && dojo.query(".ibm-home-page").length > 0) {
        return 1;
      }
      return 0;
    },
    slideTo: function(_55c, _55d, _55e) {
      var that = this;
      _55d && dojo.stopEvent(_55d);
      _55c = parseInt(_55c, 10);
      if (_55c < 0 || _55c >= this.slides.length || _55c == this.currentSlideIndex) {
        if (_55d && !this.autoscroll) {
          this._focusSlideContent(this.currentSlideIndex);
        }
        return;
      }
      var _55f = dojo.coords(dojo.query("> *", this.scrollableNode)[0]).w;
      if (_55e && !this.checkHome() && !this._isLeadNoHome) {
        _55c = 0;
        dojo.animateProperty({
          node: this.scrollableNode,
          duration: this._durationCopy,
          properties: {
            left: _55f * (this.slideCount() - 1) * -1
          },
          onEnd: function() {
            if (_55d && !this.autoscroll) {
              that._focusSlideContent(_55c);
            }
          }
        }).play();
      }
      if (this.customSeekAnimation) {
        if (this._isBeingAnimated) {
          return;
        }
        this._isBeingAnimated = true;
        this.customSeekAnimation(_55f, _55c, _55e);
        if (_55e) {
          _55c = 0;
        }
      } else {
        if (!dojo._isBodyLtr()) {
          dojo.style(this.scrollableNode, {
            right: (_55f * _55c * -1) + "px"
          });
        } else {
          if (this._scrollableNodeAnimObj) {
            this._scrollableNodeAnimObj.stop();
          }
          this._scrollableNodeAnimObj = dojo.animateProperty({
            node: this.scrollableNode,
            duration: this.duration,
            properties: {
              left: _55f * _55c * -1
            },
            onEnd: function() {
              if (_55d && !this.autoscroll) {
                that._focusSlideContent(_55c);
              }
            }
          }).play();
        }
      }
      if (_55c == 0) {
        dojo.addClass(this.scrollLeftButton, "ibm-disabled");
      } else {
        dojo.removeClass(this.scrollLeftButton, "ibm-disabled");
      }
      if (_55c == this.slides.length - 1) {
        dojo.addClass(this.scrollRightButton, "ibm-disabled");
      } else {
        dojo.removeClass(this.scrollRightButton, "ibm-disabled");
      }
      if (this._isBegingAutoscroll && this._isLeadNoHome) {
        var _560 = ibmweb.ribbonLeadspace._widget.scrollLeftButton,
          _561 = ibmweb.ribbonLeadspace._widget.scrollRightButton;
        if (dojo.isIE < 7) {
          _560.style.display = "none";
          _561.style.display = "none";
        } else {
          dojo.forEach([_560, _561], function(item) {
            dojo.anim(item, {
              opacity: 0
            }, 250, null, function() {
              item.style.display = "none";
            });
          });
        }
      }
      var _562 = "div[role=\"document\"] a, div[role=\"document\"] input, div[role=\"document\"]";
      if (this.ribbonContainer.parentNode.id != "ibm-promotion-module") {
        dojo.query(_562, this.scrollableNode).forEach(function(n) {
          n.tabIndex = -1;
        });
      }
      this.currentSlideIndex = _55c;
      if (this.ribbonContainer.parentNode.id != "ibm-promotion-module") {
        dojo.query(_562, dojo.query("div[role=\"option\"]", this.scrollableNode)[_55c]).forEach(function(n) {
          n.tabIndex = 0;
        });
      }
      var _563 = dojo.query("a.ibm-active", this.navNode);
      if (_563.length > 0) {
        _563.removeClass("ibm-active");
        _563[0].tabIndex = -1;
      }
      dojo.query("a:nth-child(" + (_55c + 1) + ")", this.navNode).addClass("ibm-active");
      dojo.query("a:nth-child(" + (_55c + 1) + ")", this.navNode)[0].tabIndex = 0;
    },
    slideLeft: function(_564) {
      this.slideTo(this.currentSlideIndex - 1, _564);
    },
    slideRight: function(_565) {
      this.slideTo(this.currentSlideIndex + 1, _565);
    },
    slideFirst: function() {
      this.slideTo(0);
    },
    slideLast: function() {
      this.slideTo(this.slides.length - 1);
    },
    next: function() {
      if (this.checkHome()) {
        dojo.query(".ibm-cloned").attr("id", "ibm-lead-1");
        var _566 = dojo.query(".ibm-cloned div").attr("widgetId");
        dojo.query(".ibm-cloned div").attr("id", _566);
      }
      if (this.rotationCount > 0 && this.rotationCount == this._rotationCount) {
        this.autoscroll = false;
        this._isBegingAutoscroll = false;
        return false;
      }
      this._isBegingAutoscroll = true;
      if ((this.currentSlideIndex + 1) == this.slideCount()) {
        if (!this.checkHome()) {
          this.duration = 1;
        }
        this._rotationCount += 1;
        this.slides.length += 1;
        this.slideTo(this.slides.length - 1, null, true);
        this.slides.length -= 1;
        if (!this.checkHome()) {
          this.currentSlideIndex = -1;
        }
      } else {
        this.slideRight();
        if (!this.checkHome()) {
          this.duration = this._durationCopy;
        }
      }
    },
    placeAt: function(_567) {
      dojo.place(this.domNode, _567);
    },
    slideCount: function() {
      return this.slides.length;
    },
    setDuration: function(_568) {
      this.duration = _568;
      this._durationCopy = _568;
    },
    hideNavigationDots: function() {
      dojo.query(this.navNode).style("display", "none");
      dojo.query(this.navNode).orphan();
    },
    addRibbonCloseBtn: function() {
      var _569 = "Close";
      if (ibmweb.dynnav.isDataLoaded && ibmweb.config.config == "www") {
        _569 = PMM.buttonData.close;
      }
      if (dojo.byId("ibm-com").className.indexOf("dijit_a11y") != -1) {
        dojo.create("div", {
          className: "ibm-mm-close",
          innerHTML: "<span tabindex='0' aria-label='" + _569 + "' role='button' title='" + _569 + "'>&#9650;</span>"
        }, this.ribbonContainer, "last");
      } else {
        dojo.create("div", {
          className: "ibm-mm-close",
          innerHTML: "<span tabindex='0' aria-label='" + _569 + "' role='button' title='" + _569 + "'></span>"
        }, this.ribbonContainer, "last");
      }
    }
  });
  dojo.declare("ibmweb.ribbonSlide", [dijit._Widget, dijit._Templated, dijit._Container], {
    pid: null,
    templateString: "<div class='ibm-columns' dojoAttachPoint='containerNode' role='option'></div>",
    postCreate: function() {
      if (this.pid && this.pid !== "") {
        this.containerNode.id = this.pid;
      }
    }
  });
  dojo.declare("ibmweb.ribbonItemAbstract", [dijit._Widget], {
    constructor: function(_56a) {
      if (!_56a.srcNodeRef) {
        return;
      }
      if (!dojo.attr(_56a.srcNodeRef, "role")) {
        dojo.attr(_56a.srcNodeRef, "role", "document");
      }
      _56a.srcNodeRef.tabIndex = 0;
      if (!_56a.enableKeypress || _56a.enableKeypress == false) {
        dojo.connect(_56a.srcNodeRef, "keypress", this, function(e) {
          if (e.keyCode == dojo.keys.UP_ARROW || e.keyCode == dojo.keys.LEFT_ARROW || e.keyCode == dojo.keys.DOWN_ARROW || e.keyCode == dojo.keys.RIGHT_ARROW) {
            e && dojo.stopEvent(e);
          }
        });
      }
      this.srcNodeRef = dojo.clone(_56a.srcNodeRef);
    }
  });
  dojo.declare("ibmweb.ribbonItem", [dijit._Widget, dijit._Templated, dijit._Container], {
    columns: 3,
    templateString: "<div dojoAttachPoint='containerNode' role='option'></div>",
    attributeMap: {
      type: {
        node: "containerNode",
        type: "class"
      },
      content: {
        node: "containerNode",
        type: "innerHTML"
      }
    },
    postCreate: function() {
      switch (parseInt(this.columns)) {
        case 1:
          this.attr("class", "ibm-col-1-1");
          break;
        case 3:
          this.attr("class", "ibm-col-6-2");
          break;
        case 5:
          this.attr("class", "ibm-col-5-1");
          break;
        case 6:
          this.attr("class", "ibm-col-6-1");
          break;
        default:
          this.attr("class", "ibm-col-6-2");
          break;
      }
    }
  });
  ibmweb.ribbonLeadspace = {
    onWindowResize: function() {
      var temp = dojo.byId("ibm-leadspace-body");
      var _56b = null;
      if (temp) {
        var id = dojo.hasAttr(temp, "widgetid") ? dojo.attr(temp, "widgetid") : null;
        if (id) {
          _56b = dijit.byId(id);
        } else {
          return false;
        }
      }
      var _56c = dojo.window.getBox();
      dojo.query("#ibm-leadspace-head > div.ibm-container-body > div.ibm-ribbon-pane")[0].style.width = (_56c.w > 1030 ? _56c.w : 1030) + "px";
      dojo.query("#ibm-leadspace-head > div.ibm-container-body > div.ibm-ribbon-pane > div.ibm-ribbon-section > div").forEach(function(i) {
        var _56d = _56c.w + 30;
        if (_56d < 1030) {
          _56d = 1030;
        }
        i.style.width = _56d + "px";
      });
      dojo.query("#ibm-leadspace-head > div.ibm-container-body > div.ibm-ribbon-pane > div.ibm-ribbon-section > div > div").forEach(function(i) {
        var _56e = (_56c.w + 30 - dojo.coords(i).w) / 2;
        if ((_56c.w + 30) < 1030) {
          _56e = (1030 - dojo.coords(i).w) / 2;
        }
        dojo.style(i, "padding", "0 " + _56e + "px");
        i.style.height = "100%";
      });
      var _56f = dojo.query("#ibm-leadspace-head > div.ibm-container-body > div.ibm-ribbon-pane > div.ibm-ribbon-section")[0];
      if (!_56f.style.left || _56f.style.left == "0px") {} else {
        var dim = _56c.w;
        if (dim < 1000) {
          dim = 1000;
        }
        _56f.style.left = "-" + ((_56b.currentSlideIndex * dim) + (_56b.currentSlideIndex * 30)) + "px";
      }
      if (ibmweb.ribbonLeadspace._widget) {
        var pos = (_56c.w - 1050) / 2;
        if (ibmweb.meta.cpi == "ilhe") {
          ibmweb.ribbonLeadspace._widget.scrollLeftButton.style.right = (pos < 1 ? 1 : pos) + "px";
        } else {
          ibmweb.ribbonLeadspace._widget.scrollLeftButton.style.left = (pos < 1 ? 1 : pos) + "px";
        }
        if (dojo.isIE < 8) {
          if (ibmweb.meta.cpi == "ilhe") {
            ibmweb.ribbonLeadspace._widget.scrollRightButton.style.left = (pos < 1 ? 5 : pos) + "px";
          } else {
            ibmweb.ribbonLeadspace._widget.scrollRightButton.style.right = (pos < 1 ? 5 : pos) + "px";
          }
        } else {
          if (ibmweb.meta.cpi == "ilhe") {
            ibmweb.ribbonLeadspace._widget.scrollRightButton.style.left = (pos < 1 ? (-1) * (_56c.w - 988) : pos) + "px";
          } else {
            ibmweb.ribbonLeadspace._widget.scrollRightButton.style.right = (pos < 1 ? (-1) * (_56c.w - 988) : pos) + "px";
          }
        }
      }
    },
    arrowsToggle: {
      generic: function(x) {
        var _570 = ibmweb.ribbonLeadspace._widget.scrollLeftButton;
        var _571 = ibmweb.ribbonLeadspace._widget.scrollRightButton;
        switch (x) {
          case "show":
            if (dojo.isIE < 7) {
              _570.style.display = "block";
              _571.style.display = "block";
            } else {
              dojo.forEach([_570, _571], function(item) {
                dojo.style(item, {
                  opacity: "0",
                  display: "block"
                });
                dojo.anim(item, {
                  opacity: 1
                }, 300, null, function() {
                  if (ibmweb.ribbonLeadspace.arrowsToggle._stat != "show") {
                    ibmweb.ribbonLeadspace.arrowsToggle.generic("hide");
                  }
                });
              });
            }
            break;
          case "hide":
            if (dojo.isIE < 7) {
              _570.style.display = "none";
              _571.style.display = "none";
            } else {
              dojo.forEach([_570, _571], function(item) {
                dojo.anim(item, {
                  opacity: 0
                }, 250, null, function() {
                  item.style.display = "none";
                  if (ibmweb.ribbonLeadspace.arrowsToggle._stat != "hide") {
                    ibmweb.ribbonLeadspace.arrowsToggle.generic("show");
                  }
                });
              });
            }
            break;
        }
      },
      show: function() {
        this.ribbonLeadspace.arrowsToggle._stat = "show";
        this.ribbonLeadspace.arrowsToggle.generic("show");
      },
      hide: function() {
        this.ribbonLeadspace.arrowsToggle._stat = "hide";
        this.ribbonLeadspace.arrowsToggle.generic("hide");
      }
    },
    init: function() {
      var _572 = new ibmweb.ribbon({
        srcNodeRef: dojo.query("#ibm-leadspace-head.ibm-ribbon > div#ibm-leadspace-body.ibm-container-body")[0],
        _isLeadSpace: true,
        customSeekAnimation: function(_573, _574, _575) {
          if (_575 && this._isLeadNoHome) {
            _574 = 0;
            this.currentSlideIndex = -1;
          }
          switch (this.currentSlideIndex) {
            case 1:
              var _576 = dojo.query("#ibm-lead-2 div.ibm-col-1-1")[0];
              break;
            case 2:
              var _576 = dojo.query("#ibm-lead-3 div.ibm-col-1-1")[0];
              break;
            default:
              var _576 = dojo.query("#ibm-lead-1 div.ibm-col-1-1")[0];
          }
          var _577 = {
              left: {
                start: 0,
                end: -2000,
                unit: "px"
              }
            },
            _578 = {
              left: {
                start: -1000,
                end: 0,
                unit: "px"
              }
            };
          if (ibmweb.meta.cpi == "ilhe") {
            _577.right = _577.left;
            _577.left = undefined;
            _578.right = _578.left;
            _578.left = undefined;
          }
          var _579 = dojo.fx.chain([dojo.animateProperty({
              node: _576,
              properties: _577,
              duration: 950,
              easing: function(n) {
                return -1 * (Math.sqrt(1 - Math.pow(n, 2)) - 1);
              }
            }), dojo.animateProperty({
              node: _576,
              properties: _578,
              duration: 200
            })]),
            _57a = {
              left: {
                start: (ibmweb.meta.cpi == "ilhe" ? (this.scrollableNode.style.right || 0) : (this.scrollableNode.style.left || 0)),
                end: _573 * _574 * -1,
                nit: "px"
              }
            };
          if (ibmweb.meta.cpi == "ilhe") {
            _57a.right = _57a.left;
            _57a.left = undefined;
          }
          var _57b = dojo.animateProperty({
              node: this.scrollableNode,
              properties: _57a,
              duration: 2100,
              easing: function(n) {
                if (n == 0) {
                  return 0;
                }
                if (n == 1) {
                  return 1;
                }
                n = n * 2;
                if (n < 1) {
                  return Math.pow(2, 10 * (n - 1)) / 2;
                }--n;
                return (-1 * Math.pow(2, -10 * n) + 2) / 2;
              }
            }),
            _57c = dojo.fx.combine([_579, _57b]);
          if (ibmweb.meta.cpi == "ilhe" && dojo.isIE < 9) {
            dojo.style(this.scrollableNode, {
              right: _573 * _574 * -1
            });
            this._isBeingAnimated = false;
          } else {
            _57c.play();
          }
          dojo.connect(_57c, "onEnd", dojo.hitch(this, function() {
            this._isBeingAnimated = false;
            this._isBegingAutoscroll = false;
            if (_575 && this._isLeadNoHome) {
              this.currentSlideIndex = 0;
            }
          }));
        }
      });
      this._widget = _572;
      if (dojo.isIE < 8) {
        dojo.place(ibmweb.ribbonLeadspace._widget.scrollRightButton, ibmweb.ribbonLeadspace._widget.scrollLeftButton, "after");
        dojo.style(ibmweb.ribbonLeadspace._widget.scrollRightButton, {
          marginTop: "140px",
          zIndex: "1"
        });
      }
      dojo.connect(_572, "onMouseEnter", ibmweb, ibmweb.ribbonLeadspace.arrowsToggle.show);
      dojo.connect(_572, "onMouseLeave", ibmweb, ibmweb.ribbonLeadspace.arrowsToggle.hide);
      if (_572.slides.length > 1) {
        _572.startup();
        dojo.query("#ibm-leadspace-body div.ibm-ribbon-nav a").forEach(function(i, e) {
          var pos = e + 1;
          dojo.connect(i, "onclick", function() {
            ibmweb.util.statsHelper({
              "ibmEV": "track",
              "ibmEvAction": "ibmlink",
              "ibmEvLinktitle": "ls" + pos,
              "ibmEvSection": "ls" + pos
            });
            return true;
          });
          dojo.connect(i, "onmouseenter", function(e) {
            e.stopPropagation();
            ibmweb.util.statsHelper({
              "ibmEV": "track",
              "ibmEvAction": "ibmhover",
              "ibmEvLinktitle": "ls" + pos,
              "ibmEvSection": "ls" + pos
            });
            return false;
          });
        });
      }
      dojo.query("#ibm-lead-2, #ibm-lead-3").forEach(function(item) {
        item.style.display = "block";
      });
      if (_572.slides.length > 1) {
        dojo.query("#ibm-leadspace-head .ibm-ribbon-nav a").forEach(function(item, i) {
          var j = i + 1;
          var temp = dojo.query("#ibm-lead-" + j + " div.ibm-ribbon-view");
          if (temp.length == 1) {
            dojo.place(temp[0], item);
            var _57d = dojo.query("div.ibm-pagination-overlay", item)[0];
            dojo.connect(_57d, "onclick", function(e) {
              _572.slideTo(i, e);
            });
            dojo.connect(item, "onmouseenter", function(e) {
              if (_57d.style.display == "block") {
                return false;
              }
              if (dojo.isIE < 7) {
                dojo.style(_57d, {
                  display: "block"
                });
              } else {
                dojo.style(_57d, {
                  opacity: "0",
                  display: "block"
                });
                dojo.anim(_57d, {
                  opacity: 1
                }, 300);
              }
              var _57e = dojo.connect(item, "onmouseleave", function() {
                if (dojo.isIE < 7) {
                  _57d.style.display = "none";
                } else {
                  dojo.anim(_57d, {
                    opacity: 0
                  }, 200, null, function() {
                    _57d.style.display = "none";
                  });
                }
                dojo.disconnect(_57e);
              });
            });
          }
        });
      } else {
        dojo.query(_572.navNode).orphan();
      }
      if (dojo.isIE < 8) {
        dojo.byId("ibm-leadspace-body").style.position = "static";
      }
      dojo.connect(window, "resize", ibmweb.ribbonLeadspace.onWindowResize);
      ibmweb.ribbonLeadspace.onWindowResize();
      var _57f = dojo.query("#ibm-leadspace-head div.ibm-ribbon-nav a");
      dojo.connect(dojo.query("#ibm-leadspace-head div.ibm-ribbon-pane")[0], "onkeypress", dojo.hitch(this, function(evt) {
        if (evt.keyCode == dojo.keys.TAB && !evt.shiftKey) {
          if (_57f.length > 0) {
            dojo.query("#ibm-leadspace-head div.ibm-ribbon-nav a:first-child")[0].focus();
          } else {
            dojo.byId("ibm-promotion-module").focus();
          }
          dojo.stopEvent(evt);
        }
      }));
      if (dojo.isIE < 7) {
        dojo.query("#ibm-leadspace-body div.ibm-columns img:first-child").forEach(function(item) {
          var _580 = item.width;
          var _581 = item.height;
          var src = item.src;
          dojo.query(item).wrap("<span style=\"width:" + _580 + "px;height:" + _581 + "px;display:inline-block; filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + src + "');\"></span>");
          item.style.filter = "progid:DXImageTransform.Microsoft.Alpha(opacity=0)";
        });
      }
    }
  };
}
if (!dojo._hasResource["ibmweb.dynnav.megamenu"]) {
  dojo._hasResource["ibmweb.dynnav.megamenu"] = true;
  dojo.provide("ibmweb.dynnav.megamenu");
  dojo.declare("ibmweb.dynnav.megamenu", ibmweb.dynnav._module, {
    _currentMenu: null,
    _mouseover: false,
    _slideUpTimer: null,
    _slideUpDuration: 600,
    _keepexpanded: false,
    _mouseleaveHandlerEnabled: true,
    _expandTimer: null,
    _slideToTimer: null,
    _delayedExpandTimer: null,
    _isExpanded: true,
    _itemtitle: null,
    _lock: false,
    _beingAnimated: false,
    _doubleClickLock: null,
    _waitBeforeAssignPage: null,
    _unicaTagsW3MenuNames: [],
    _unicaTagsWWWMenuNames: [],
    _hasFocus: [],
    _isMinimizingOnMouseLeaveSemaphore: false,
    _newDataSource: {
      use: true,
      hidden: true,
      isBeingAnimated: false,
      isSubBeingAnimated: false,
      hasDynTitle: false,
      subTabs: [],
      subTabsContent: [],
      subTabsActItem: [],
      subTabsActItem_idx: [],
      subTabsHover: [],
      subTabMenu: null,
      curSubMenu: null
    },
    _w3SwapDesign: {
      secondSwitchEnabled: false,
      waitOnExist: null,
      waitOnMouseOver: null,
      stopClickForMoment: null,
      leavingItem: null,
      mouseOverOpenMethod: false,
      mouseOnMenuButton: false,
      currentLink: -1,
      profileLinks: null,
      newSso: null,
      portalLogOutLink: null,
      profileCustomLinks: null
    },
    _dimensions: {
      basicUniversalHeight: 50,
      basicMarginTop: 74
    },
    init: function() {
      if (dojo.isIE) {
        dojo.query(".lotusui30 .lotusBanner").style({
          position: "static"
        });
      }
      if (ibmweb.config.config == "w3") {
        this._dimensions.basicMarginTop = 90;
        this._w3SwapDesign.secondSwitchEnabled = !dojo.hasClass(dojo.query("#ibm-masthead")[0], "ibm-w3-masthead");
      }
      if (dojo.indexOf(ibmweb.config.megamenu.disabledLocales, ibmweb.meta.cpi) != -1) {
        ibmweb.config.megamenu.linksonly = true;
      }
      if (ibmweb.config.config == "w3") {
        if (this._w3SwapDesign.secondSwitchEnabled) {
          dojo.query("#ibm-masthead").addClass("ibm-w3-masthead");
        } else {
          if (!dojo.byId("ibm-profile-links")) {
            this._w3SwapDesign.profileLinks = dojo.create("div", {
              id: "ibm-profile-links"
            }, "ibm-universal-nav", "last");
          } else {
            this._w3SwapDesign.profileLinks = dojo.query("[id=\"ibm-profile-links\"]")[0];
          }
          if (!dojo.byId("ibm-sso")) {
            this._w3SwapDesign.newSso = dojo.create("div", {
              id: "ibm-sso",
              role: ""
            }, this._w3SwapDesign.profileLinks, "last");
            dijit.setWaiRole(this._w3SwapDesign.newSso, "presentation");
            if (dojo.query("#ibm-portal-logout-link").length == 1 || dojo.query("#ibm-profile-custom-links").length == 1) {
              dojo.addClass(this._w3SwapDesign.newSso, "ibm-profile-links-divider");
            }
          } else {
            this._w3SwapDesign.newSso = dojo.query("#ibm-sso")[0];
          }
        }
      }(function() {
        if (ibmweb.config.config === "w3") {
          var $st = dojo.query("#ibm-site-title");
          if ($st[0]) {
            if (dojo.query("> *", $st[0]).length === 0 && (dojo.trim($st[0].innerHTML).length === 0)) {
              $st[0].innerHTML = "<em>&nbsp;</em>";
            }
            if (dojo.query("> em", $st[0]).length === 1 && dojo.trim(dojo.query("> em", $st[0])[0].innerHTML).length === 0) {
              dojo.query("> em", $st[0])[0].innerHTML = "&nbsp;";
            }
          }
        }
      })();
      if (ibmweb.config.megamenu.disableUniversalNav) {
        this._dimensions.basicMarginTop = 42;
        this._dimensions.basicUniversalHeight = 0;
        dojo.query("#ibm-site-title").style("display", "none");
        dojo.query("#ibm-universal-nav").style("height", "0px");
        dojo.query("#ibm-top").style("marginTop", "42px");
      }
      return ibmweb.dynnav.isServiceEnabled("megamenu") && !ibmweb.config.appmast.enabled;
    },
    onLoad: function() {
      if (!dojo.byId("ibm-common-menu")) {
        var _582 = ibmweb.config.config == "www" ? "ibm-universal-nav" : "ibm-mast-options";
        dojo.create("div", {
          id: "ibm-common-menu"
        }, dojo.byId(_582), "after");
      }
      dojo.style("ibm-common-menu", "display", "none");
      if (ibmweb.config.config == "www") {
        var _583 = dojo.query("#ibm-unav-links");
      }
      if (ibmweb.config.config == "www" && ibmweb.config.megamenu.icons != "none") {
        if (ibmweb.config.megamenu.icons == "arrow") {
          if (!dojo.byId("ibm-arrow-logo")) {
            var _584 = dojo.create("span", {
              id: "ibm-arrow-logo",
              className: "ibm-masthead-indicator"
            });
            dojo.create("img", {
              src: ibmweb.config.imageUrl + "t/v17_min_mast_anim_que.gif"
            }, _584);
            dojo.place(_584, dojo.query("#ibm-universal-nav")[0], "first");
          }
        } else {
          if (!dojo.byId("ibm-search-logo")) {
            var _585 = dojo.create("li", {
              id: "ibm-search-logo",
              className: "ibm-masthead-indicator"
            });
            if (ibmweb.config.megamenu.icons == "text") {
              _585.innerHTML = "Search";
              dojo.addClass(_585, "ibm-text");
            }
            dojo.place(_585, _583[0], "last");
          }
          if (!dojo.byId("ibm-links-logo")) {
            var _586 = dojo.create("li", {
              id: "ibm-links-logo",
              className: "ibm-masthead-indicator"
            });
            if (ibmweb.config.megamenu.icons == "text") {
              _586.innerHTML = "Links";
              dojo.addClass(_586, "ibm-text");
            }
            dojo.place(_586, _583[0], "last");
          }
        }
        dojo.query(".ibm-masthead-indicator").addClass("ibm-access");
      }
      if (ibmweb.config.config == "www" && dojo.query("li", _583[0]).length == 1) {
        if (!dojo.hasAttr(_583, "role")) {
          dojo.attr(_583[0], "role", "");
          dijit.setWaiRole(_583[0], "presentation");
        }
      }
      if (ibmweb.config.config == "w3" && ibmweb.config.megamenu.icons != "none") {
        if (ibmweb.config.megamenu.icons == "arrow") {
          if (!dojo.byId("ibm-arrow-logo")) {
            var _584 = dojo.create("span", {
              id: "ibm-arrow-logo",
              className: "ibm-masthead-indicator"
            });
            dojo.create("img", {
              src: ibmweb.config.imageUrl + "t/w3_min_menu_open_icon.png"
            }, _584);
            dojo.place(_584, dojo.query("#ibm-universal-nav")[0], "first");
          }
        }
      }
      if (!ibmweb.config.megamenu.noScroll) {
        dojo.connect(window, "onscroll", this, "onVScrollCallback");
        dojo.connect(window, "onresize", this, "onVScrollCallback");
      } else {
        dojo.addClass("ibm-top", "ibm-no-scroll");
      }
      dojo.connect(window, "onscroll", this, "onHScrollCallback");
      dojo.connect(window, "onresize", this, function(_587) {
        dojo.stopEvent(_587);
        if (!window.pageXOffset && window.pageXOffset != 0) {
          dojo.style(dojo.byId("ibm-masthead"), "left", "-" + document.documentElement.scrollLeft + "px");
        } else {
          dojo.style(dojo.byId("ibm-masthead"), "left", "-" + window.pageXOffset + "px");
        }
      });
      dojo.connect(window, "blur", this, function(_588) {
        this.meta = this.shift = this.ctrl = this.cmmd = this.alt = false;
        dojo.publish("/ibm/dynnav/slideUp");
      });
      if (ibmweb.config.config == "w3" && ibmweb.config.megamenu.expandOnClick) {
        dojo.connect(dojo.byId("ibm-masthead"), "onclick", this, "onMouseEnter");
      } else {
        if (ibmweb.config.config == "w3" && ibmweb.config.megamenu.delay > 0) {
          dojo.connect(dojo.byId("ibm-masthead"), "onmouseenter", this, function(_589) {
            this._delayedExpandTimer = setTimeout(dojo.hitch(this, function() {
              this.onMouseEnter(_589);
            }), ibmweb.config.megamenu.delay);
          });
        } else {
          dojo.connect(dojo.byId("ibm-masthead"), "onmouseenter", this, "onMouseEnter");
        }
      }
      dojo.connect(dojo.byId("ibm-masthead"), "onmouseleave", this, "onMouseLeave");
      dojo.subscribe("/ibm/dynnav/slideUp", this, "_slideUp");
      dojo.subscribe("/ibm/dynnav/megamenu/slideUp", this, "_slideUp");
      dojo.subscribe("/ibm/dynnav/megamenu/slideDown", this, "_slideDown");
      dojo.subscribe("/ibm/dynnav/megamenu/expandMenu", this, "_expandMenu");
      if (ibmweb.config.megamenu.minimizeByDefault) {
        this._minimizeMenu(1);
      }
      dojo.publish("/ibmweb/dynnav/megamenu/finished");
    },
    onData: function(data) {
      var ul = dojo.byId("ibm-menu-links");
      dojo.empty(ul);
      dijit.setWaiRole("ibm-menu-links", "toolbar");
      var _58a = new ibmweb.ribbon();
      dijit.setWaiRole(_58a.scrollableNode, "region");
      dijit.setWaiState(_58a.scrollableNode, "label", data.accessibilityData.sitemap || "Site map");
      dojo.forEach(data.menuData, dojo.hitch(this, function(item, i) {
        var li = dojo.create("li", {}, ul);
        dijit.setWaiRole(li, "presentation");
        if (ibmweb.config.megamenu.minimizeByDefault) {
          dojo.addClass(li, "ibm-access");
        }
        var a = dojo.create("a", {
          href: item.url || "#",
          role: "",
          tabIndex: (ibmweb.info.OperaVersion != 11.11) ? "-1" : "0",
          innerHTML: item.title.htmlspecialchars()
        }, li);
        dijit.setWaiRole(a, "button");
        if (ibmweb.config.megamenu.linksonly) {
          return;
        }
        this._itemtitle = item.title.htmlspecialchars();
        if (ibmweb.config.config == "www") {
          this._unicaTagsWWWMenuNames.push(item.title.htmlspecialchars());
        }
        if (ibmweb.config.config == "w3") {
          this._unicaTagsW3MenuNames.push(item.title.htmlspecialchars());
        }
        if (item.child) {
          var div = this._buildMegaMenu(item.child, i);
          _58a.addItem(new ibmweb.ribbonItemAbstract({
            srcNodeRef: div,
            enableKeypress: true
          }), i);
          dijit.setWaiRole(div.parentNode, "dialog");
          dijit.setWaiState(div.parentNode, "labelledby", "ibm-rib-head-" + i);
          dojo.create("h2", {
            className: "ibm-access",
            id: "ibm-rib-head-" + i,
            innerHTML: item.title.htmlspecialchars()
          }, div.parentNode, "first");
          childAnk = dojo.query("a", _58a.navNode);
          if (childAnk.length > 0 && i < childAnk.length) {
            dojo.attr(childAnk[i], "title", item.title.htmlspecialchars());
            childAnk[i].innerHTML = item.title.htmlspecialchars();
          }
          dojo.connect(a, "onblur", this, function(_58b) {
            dojo.stopEvent(_58b);
            dojo.query("#ibm-menu-links li.ibm-inactive").removeClass("ibm-inactive");
            return false;
          });
          dojo.connect(a, "onfocus", this, function(_58c) {
            dojo.stopEvent(_58c);
            if (ibmweb.config.config === "www" && this._currentMenu != null) {
              dojo.removeClass(dojo.query("#ibm-menu-links a")[this._currentMenu], "ibm-active");
            }
            if (dojo.byId("ibm-menu-links").className == "ibm-access") {
              dojo.publish("/ibm/dynnav/megamenu/expandMenu");
            }
            var _58d = (ibmweb.config.config == "w3") ? dojo.query("#ibm-menu-links a[tabindex $='2']") : dojo.query("#ibm-menu-links a[tabindex $='0']");
            _58d.forEach(function(i) {
              i.tabIndex = "-1";
            });
            if (this._currentMenu != null) {
              _58a.setDuration(_58a.defaultDuration);
              this._currentMenu = i;
              dojo.query("a", _58a.scrollableNode).attr("tabIndex", "-1");
              _58a.slideTo(i);
              _58c.target.parentNode.className = "ibm-active";
              dojo.addClass(_58c.target, "ibm-active");
              var _58e = dojo.query("> div", _58a.scrollableNode),
                temp = dojo.query("a", _58e[i]);
              if (temp.length > 0) {
                temp.attr("tabIndex", (ibmweb.config.config == "w3") ? "2" : "0");
              }
            } else {
              dojo.query("#ibm-menu-links li.ibm-inactive").removeClass("ibm-inactive");
              _58c.target.parentNode.className = "ibm-inactive";
            }
            a.tabIndex = (ibmweb.config.config == "w3") ? 2 : 0;
            return false;
          });
          dojo.connect(a, "onkeypress", this, function(_58f) {
            if (_58f.shiftKey && _58f.charOrCode == dojo.keys.TAB) {
              this._slideUp();
            } else {
              if (_58f.keyCode == dojo.keys.TAB) {
                if (this._currentMenu != null) {
                  dojo.stopEvent(_58f);
                  var _590 = dojo.query("> div", _58a.scrollableNode),
                    temp = dojo.query("a", _590[i]);
                  if (temp.length > 0) {
                    temp[0].focus();
                  }
                  _58f.target.className = "ibm-active";
                } else {
                  this._slideUp();
                }
              }
            }
            if (_58f.keyCode == dojo.keys.LEFT_ARROW || _58f.keyCode == dojo.keys.UP_ARROW) {
              a.tabIndex = -1;
              var prev = _58f.target.parentNode.previousSibling;
              if (prev) {
                dojo.stopEvent(_58f);
                prev.firstChild.focus();
              } else {
                prev = _58f.target.parentNode.parentNode.lastChild;
                if (prev) {
                  dojo.stopEvent(_58f);
                  prev.firstChild.focus();
                }
              }
            }
            if (_58f.keyCode == dojo.keys.RIGHT_ARROW || (_58f.keyCode == dojo.keys.DOWN_ARROW && this._currentMenu == null)) {
              a.tabIndex = -1;
              var next = _58f.target.parentNode.nextSibling;
              if (next) {
                dojo.stopEvent(_58f);
                next.firstChild.focus();
              } else {
                next = _58f.target.parentNode.parentNode.firstChild;
                if (next) {
                  dojo.stopEvent(_58f);
                  next.firstChild.focus();
                }
              }
            }
            if (this._isExpanded && (_58f.keyCode == dojo.keys.ENTER || (_58f.keyCode == dojo.keys.SPACE || _58f.charOrCode == " "))) {
              dojo.stopEvent(_58f);
              if (this._currentMenu == null) {
                ibmweb.util.statsHelper({
                  "ibmEV": ibmweb.config.config + " Megamenu",
                  "ibmEvAction": "Expand menu",
                  "ibmEvName": ((ibmweb.config.config === "w3") ? this._unicaTagsW3MenuNames[i] : this._unicaTagsWWWMenuNames[i]),
                  "ibmEvSection": "Initial"
                });
                _58a.setDuration(1);
                dojo.publish("/ibm/dynnav/slideUp");
                dojo.publish("/ibm/dynnav/signin/slideUp", [_58f]);
                dojo.publish("/ibm/dynnav/megamenu/slideDown", [i]);
                _58a.slideTo(i);
                if (typeof this._newDataSource.subTabs[i] !== "undefined") {
                  dojo.query(this._newDataSource.subTabs[i][this._newDataSource.subTabsActItem_idx[i]]).children("a")[0].focus();
                } else {
                  var _590 = dojo.query("> div", _58a.scrollableNode),
                    temp = dojo.query("a", _590[i]);
                  if (temp.length > 0) {
                    temp.attr("tabIndex", "0");
                    this._slideToTimer = setTimeout(dojo.hitch(this, function() {
                      temp[0].focus();
                    }), _58a.defaultDuration + 60);
                  }
                }
              } else {
                ibmweb.util.statsHelper({
                  "ibmEV": ibmweb.config.config + " Megamenu",
                  "ibmEvAction": "Expand menu",
                  "ibmEvName": ((ibmweb.config.config === "w3") ? this._unicaTagsW3MenuNames[i] : this._unicaTagsWWWMenuNames[i]),
                  "ibmEvSection": "Secondary"
                });
                _58a.setDuration(_58a.defaultDuration);
                this._slideUp();
              }
            }
            if (_58f.keyCode == dojo.keys.DOWN_ARROW && this._currentMenu != null) {
              dojo.stopEvent(_58f);
              var _590 = dojo.query("> div", _58a.scrollableNode),
                temp = dojo.query("a", _590[i]);
              if (temp.length > 0) {
                temp[0].focus();
              }
              _58f.target.className = "ibm-active";
            }
            if (_58f.keyCode == dojo.keys.ESCAPE) {
              if (this._currentMenu != null) {
                dojo.stopEvent(_58f);
                this._slideUp();
              } else {
                var _591 = dojo.query("#q");
                if (_591.length > 0) {
                  _591[0].focus();
                }
              }
            }
            return false;
          });
          dojo.connect(a, "ondblclick", this, function(_592) {
            dojo.stopEvent(_592);
            if (!!this._doubleClickLock) {
              clearTimeout(this._doubleClickLock);
              this._doubleClickLock = null;
            }
            location.href = _592.target.href;
          });
          dojo.connect(a, "onclick", this, function(_593) {
            dojo.stopEvent(_593);
            if (!!this._doubleClickLock) {
              return;
            }
            this._doubleClickLock = setTimeout(dojo.hitch(this, function() {
              this._doubleClickLock = null;
              if (ibmweb.config.config === "w3" && this._w3SwapDesign.mouseOverOpenMethod == true) {
                return false;
              }
              _593.target.focus();
              if (this._currentMenu == null) {
                ibmweb.util.statsHelper({
                  "ibmEV": ((ibmweb.config.config === "www") ? "www" : "w3") + " Megamenu",
                  "ibmEvAction": "Expand menu",
                  "ibmEvName": (ibmweb.config.config === "www") ? this._unicaTagsWWWMenuNames[i] : this._unicaTagsW3MenuNames[i],
                  "ibmEvSection": "Initial"
                });
                dojo.addClass(_593.target.parentNode, "ibm-active");
                dojo.addClass(_593.target, "ibm-active");
                _58a.setDuration(1);
                dojo.publish("/ibm/dynnav/slideUp");
                dojo.publish("/ibm/dynnav/signin/slideUp", [_593]);
                dojo.publish("/ibm/dynnav/megamenu/slideDown", [i]);
                _58a.slideTo(i);
                this._hasFocus[i] == false;
                if (typeof this._newDataSource.subTabs[i] !== "undefined") {
                  dojo.query(this._newDataSource.subTabs[i][this._newDataSource.subTabsActItem_idx[i]]).children("a")[0].focus();
                } else {
                  var _594 = dojo.query("> div", _58a.scrollableNode),
                    temp = dojo.query("a", _594[i]);
                  if (temp.length > 0) {
                    temp.attr("tabIndex", "0");
                    if (ibmweb.config.config === "www") {
                      this._slideToTimer = setTimeout(dojo.hitch(this, function() {
                        temp[0].focus();
                      }), _58a.defaultDuration + 60);
                    }
                  }
                }
              } else {
                ibmweb.util.statsHelper({
                  "ibmEV": ((ibmweb.config.config === "www") ? "www" : "w3") + " Megamenu",
                  "ibmEvAction": "Expand menu",
                  "ibmEvName": (ibmweb.config.config === "www") ? this._unicaTagsWWWMenuNames[i] : this._unicaTagsW3MenuNames[i],
                  "ibmEvSection": "Secondary"
                });
                dojo.query("#ibm-menu-links a.ibm-active").removeClass("ibm-active");
                dojo.query("#ibm-menu-links li.ibm-active").removeClass("ibm-active");
                dojo.addClass(_593.target.parentNode, "ibm-active");
                dojo.addClass(_593.target, "ibm-active");
                if (this._currentMenu == i && this._hasFocus[i] == true) {
                  this._slideUp();
                } else {
                  if (this._newDataSource.use) {
                    var id = this._newDataSource.curSubMenu;
                    if (typeof this._newDataSource.subTabs[id] !== "undefined") {
                      dojo.forEach(this._newDataSource.subTabs[id], dojo.hitch(this, function(_595, _596) {
                        if (_596 === this._newDataSource.subTabsActItem_idx[id]) {
                          dojo.removeClass(_595, "ibm-active");
                        }
                        dojo.style(_595, "display", "none");
                      }));
                    }
                    id = i;
                    this._newDataSource.curSubMenu = i;
                    if (typeof this._newDataSource.subTabs[id] !== "undefined") {
                      dojo.forEach(this._newDataSource.subTabs[id], dojo.hitch(this, function(_597, _598) {
                        dojo.style(_597, "display", "block");
                        if (_598 === this._newDataSource.subTabsActItem_idx[id]) {
                          dojo.addClass(_597, "ibm-active");
                        }
                      }));
                    }
                  }
                  _58a.slideTo(i);
                }
              }
              for (var j = 0; j < this._hasFocus.length; j++) {
                this._hasFocus[j] = false;
              }
              this._hasFocus[i] = true;
              return false;
            }), 250);
          });
        }
      }));
      _58a.placeAt("ibm-common-menu");
      this._createSubTabs();
      _58a.hideNavigationDots();
      _58a.addRibbonCloseBtn();
      _58a.startup();
      dojo.connect(_58a.scrollableNode, "onkeypress", this, function(_599) {
        if (_599.keyCode == dojo.keys.ESCAPE) {
          dojo.stopEvent(_599);
          var k = this._newDataSource.curSubMenu;
          if (k !== null) {
            var l = this._newDataSource.subTabsActItem_idx[k];
            dojo.query("a", this._newDataSource.subTabs[k][l])[0].focus();
          } else {
            dojo.publish("/ibm/dynnav/megamenu/slideUp");
          }
        }
        if ((!_599.shiftKey && _599.keyCode == dojo.keys.TAB) || (_599.shiftKey && _599.keyCode == dojo.keys.TAB)) {
          dojo.stopEvent(_599);
          var _59a, _59b = false,
            _59c = false;
          (_599.target.parentNode.nodeName == "H3") ? _59a = _599.target.parentNode.parentNode: _59a = _599.target.parentNode.parentNode.parentNode;
          if (dojo.hasClass(_59a, "ibm-col-last")) {
            _59b = true;
          }
          if (dojo.hasClass(_59a, "ibm-menu-static")) {
            _59c = true;
          }
          if (!_599.shiftKey && _599.keyCode == dojo.keys.TAB) {
            if (_59b) {
              dojo.query("a", dojo.query(_59a.parentNode).siblings(".ibm-menu-static")[0])[0].focus();
            } else {
              (_59a.nextSibling && dojo.query("a", _59a.nextSibling)[0]) ? dojo.query("a", _59a.nextSibling)[0].focus(): dojo.query("div#ibm-common-menu span")[0].focus();
            }
          }
          if (_599.shiftKey && _599.keyCode == dojo.keys.TAB) {
            if (_59c) {
              var k = this._newDataSource.curSubMenu,
                l = this._newDataSource.subTabsActItem_idx[k];
              dojo.query(".ibm-col-last a", this._newDataSource.subTabsContent[k][l])[0].focus();
            } else {
              (_59a.previousSibling && dojo.query("a", _59a.previousSibling)[0]) ? dojo.query("a", _59a.previousSibling)[0].focus(): dojo.query("div#ibm-common-menu span")[0].focus();
            }
          }
        }
        if (_599.keyCode == dojo.keys.UP_ARROW || _599.keyCode == dojo.keys.LEFT_ARROW) {
          var _59d = dojo.query("> div", _58a.scrollableNode),
            _59e = dojo.query("a", _59d[this._currentMenu])[0],
            curr = dojo.query("a.ibm-active", _59d[this._currentMenu])[0];
          if (!!curr) {
            dojo.stopEvent(_599);
            var prev = curr.parentNode.previousSibling;
            if (prev == null) {
              if (curr.parentNode.parentNode.nodeName == "UL") {
                prev = curr.parentNode.parentNode.previousSibling;
              }
            }
            if (prev != null && prev.firstChild != null && prev.firstChild.nodeName == "A") {
              prev.firstChild.focus();
            }
          }
        }
        if (_599.keyCode == dojo.keys.DOWN_ARROW || _599.keyCode == dojo.keys.RIGHT_ARROW) {
          var _59d = dojo.query("> div", _58a.scrollableNode),
            curr = dojo.query("a.ibm-active", _59d[this._currentMenu])[0];
          if (!!curr) {
            dojo.stopEvent(_599);
            var next = curr.parentNode.nextSibling;
            if (next) {
              if (next.nodeName == "UL") {
                next = next.firstChild;
              }
              if (next && next.firstChild && next.firstChild.nodeName == "A") {
                next.firstChild.focus();
              }
            }
          }
        }
      });
      var span = dojo.query("div#ibm-common-menu div.ibm-mm-close span")[0];
      if (span) {
        dojo.connect(span, "onclick", this, function(_59f) {
          ibmweb.util.statsHelper({
            "ibmEV": ibmweb.config.config + " Megamenu",
            "ibmEvAction": "Arrow",
            "ibmEvName": "Close"
          });
          dojo.publish("/ibm/dynnav/megamenu/slideUp");
        });
        dojo.connect(span, "onkeypress", this, function(_5a0) {
          if (_5a0.keyCode == dojo.keys.ESCAPE || _5a0.keyCode == dojo.keys.ENTER || _5a0.keyCode == dojo.keys.SPACE || _5a0.charOrCode == " ") {
            dojo.stopEvent(_5a0);
            ibmweb.util.statsHelper({
              "ibmEV": ibmweb.config.config + " Megamenu",
              "ibmEvAction": "Arrow",
              "ibmEvName": "Close"
            });
            dojo.publish("/ibm/dynnav/megamenu/slideUp");
          }
          if (_5a0.keyCode == dojo.keys.TAB) {
            var _5a1 = dojo.query("> div", _58a.scrollableNode),
              _5a2 = dojo.query("a", _5a1[this._currentMenu]);
            if (!_5a0.shiftKey) {
              dojo.stopEvent(_5a0);
              _5a2[0].focus();
            }
            if (_5a0.shiftKey) {
              dojo.stopEvent(_5a0);
              var _5a3;
              if (_5a2[_5a2.length - 1].parentNode.nodeName == "H3") {
                _5a3 = _5a2[_5a2.length - 1].parentNode.parentNode;
              } else {
                _5a3 = _5a2[_5a2.length - 1].parentNode.parentNode.parentNode;
              }
              dojo.query("a", _5a3)[0].focus();
            }
          }
          if (_5a0.keyCode == dojo.keys.UP_ARROW || _5a0.keyCode == dojo.keys.RIGHT_ARROW || _5a0.keyCode == dojo.keys.DOWN_ARROW || _5a0.keyCode == dojo.keys.LEFT_ARROW) {
            dojo.stopEvent(_5a0);
          }
        });
      }
      var a = dojo.query("#ibm-menu-links a");
      if (a.length > 0) {
        dojo.attr(a[0], "tabindex", (ibmweb.config.config == "w3") ? "2" : "0");
      }
      if (ibmweb.config.config == "w3") {
        dojo.attr(dojo.query("#ibm-home a")[0], "tabindex", "1");
        var _5a4 = dojo.query("#ibm-sso"),
          _5a5 = _5a4.next("li[role=presentation]");
        dojo.forEach(_5a4.children("a"), function(_5a6) {
          dojo.attr(_5a6, "tabindex", "3");
        });
        dojo.forEach(_5a5.children("a"), function(_5a7) {
          dojo.attr(_5a7, "tabindex", "3");
        });
        if (this._w3SwapDesign.secondSwitchEnabled) {
          if (!ibmweb.config.megamenu.disableUniversalNav) {
            this._w3SwapDesign.profileLinks = dojo.create("div", {
              id: "ibm-profile-links"
            }, "ibm-universal-nav", "last");
            if (_5a4.length > 0) {
              this._w3SwapDesign.newSso = dojo.create("div", {
                id: "ibm-sso",
                className: "ibm-profile-links-divider",
                role: "",
                innerHTML: _5a4[0].innerHTML
              }, this._w3SwapDesign.profileLinks, "last");
              dijit.setWaiRole(this._w3SwapDesign.newSso, "presentation");
            }
            if (_5a5.length > 0) {
              this._w3SwapDesign.portalLogOutLink = dojo.create("div", {
                id: "ibm-portal-logout-link",
                className: "ibm-profile-links-divider ibm-profile-links-divider--transparent",
                role: "",
                innerHTML: _5a5[0].innerHTML
              }, this._w3SwapDesign.profileLinks, "last");
              dijit.setWaiRole(this._w3SwapDesign.portalLogOutLink, "presentation");
            }
            this._w3SwapDesign.profileCustomLinks = dojo.create("div", {
              className: "ibm-profile-custom-links"
            }, this._w3SwapDesign.profileLinks, "last");
          }
          if (_5a4.length > 0) {
            dojo.destroy(_5a4[0]);
          }
          if (_5a5.length > 0) {
            dojo.destroy(_5a5[0]);
          }
          var _5a8 = dojo.query("#ibm-mast-options ul");
          dojo.forEach(_5a8, dojo.hitch(this, function(_5a9) {
            if (dojo.query(_5a9).children().length == 0) {
              dojo.destroy(_5a9);
              return;
            }
            dojo.forEach(dojo.query("li:not([id=ibm-home])", _5a9), dojo.hitch(this, function(_5aa) {
              if (!ibmweb.config.megamenu.disableUniversalNav) {
                dojo.place(dojo.query("a", _5aa)[0], this._w3SwapDesign.profileCustomLinks, "last");
              }
              dojo.destroy(_5aa);
            }));
          }));
        } else {
          this._w3SwapDesign.profileLinks = dojo.query("#ibm-profile-links");
          this._w3SwapDesign.profileCustomLinks = dojo.query("#ibm-profile-links .ibm-profile-custom-links");
        }
      }
    },
    _subTabs: {
      _title: [],
      _div: [],
      _id: []
    },
    _createSubTabs: function() {
      var con, _5ab, _5ac, _5ad;
      if (this._subTabs._title.length == 0) {
        return;
      }
      var _5ae = dojo.query("#ibm-common-menu > div"),
        _5af = null,
        jump = dojo.hitch(this, function(e, _5b0) {
          var li = _5b0.parentNode,
            _5b1 = parseInt(/(?:subTab\-\d+\-)(\d+)/g.exec(dojo.attr(li, "className"))[1]),
            _5b2 = null,
            _5b3 = null;
          this._newDataSource.subTabsHover[this._newDataSource.curSubMenu][_5b1] = true;
          setTimeout(dojo.hitch(this, function() {
            if ((!this._newDataSource.subTabsHover[this._newDataSource.curSubMenu][_5b1]) || (dojo.hasClass(li, "ibm-active") || this._newDataSource.isSubBeingAnimated)) {
              return false;
            }
            this._newDataSource.isSubBeingAnimated = true;
            this._newDataSource.subTabsActItem[this._newDataSource.curSubMenu] = e.target.textContent;
            this._newDataSource.subTabsActItem_idx[this._newDataSource.curSubMenu] = _5b1;
            ibmweb.util.statsHelper({
              "ibmEV": ibmweb.config.config + " Megamenu",
              "ibmEvAction": "Mouse over",
              "ibmEvName": ((ibmweb.config.config === "www") ? this._unicaTagsWWWMenuNames[this._currentMenu] : this._unicaTagsW3MenuNames[this._currentMenu]) + " - " + this._newDataSource.subTabsActItem[this._newDataSource.curSubMenu],
              "ibmEvSection": "Subcategory Change"
            });
            dojo.query("li.ibm-active", _5ab).removeClass("ibm-active");
            dojo.addClass(li, "ibm-active");
            dojo.forEach(this._newDataSource.subTabs[this._newDataSource.curSubMenu], dojo.hitch(this, function(_5b4, i) {
              var _5b5 = dojo.query("a", _5b4)[0],
                _5b6 = this._newDataSource.subTabsContent[this._newDataSource.curSubMenu][i],
                disp = dojo.style(_5b6, "display");
              if (disp == "none" && i == _5b1) {
                _5b3 = _5b6;
              }
              if (disp == "block" && i != _5b1) {
                _5b2 = _5b6;
              }
            }));
            dojo.animateProperty({
              node: _5b2,
              properties: {
                opacity: {
                  start: 1,
                  end: 0
                }
              },
              duration: 180,
              onEnd: dojo.hitch(this, function() {
                dojo.style(_5b2, "display", "none");
                dojo.style(_5b3, {
                  opacity: 1,
                  display: "block"
                });
                this._newDataSource.isSubBeingAnimated = false;
              })
            }).play();
          }), 200);
        });
      if (_5ae.length == 1 && _5ae[0].id) {
        _5af = _5ae[0].id;
      } else {
        return false;
      }
      con = dojo.query(this._subTabs._div[0]).closest(".ibm-container-body");
      if (dojo.query(".ibm-menu-subtabs", con[0]).length == 0) {
        _5ab = dojo.create("div", {
          className: "ibm-menu-subtabs ibm-is-hidden"
        }, con[0], "first");
        _5ac = dojo.create("ul", {
          role: ""
        }, _5ab);
        dijit.setWaiRole(_5ac, "toolbar");
        dojo.attr(_5ac, "aria-label", "subtab menu list");
      }
      this._newDataSource.subTabMenu = _5ab;
      dojo.forEach(this._subTabs._title, dojo.hitch(this, function(_5b7, t) {
        if (typeof _5b7 === "undefined") {
          return;
        }
        var _5b8 = dojo.query("#ibmweb_ribbonSlide_" + this._subTabs._div[t].id.split("ibmweb_ribbonItemAbstract_")[1]);
        if (typeof this._newDataSource.subTabs[t] === "undefined") {
          this._newDataSource.subTabs[t] = new Array();
          this._newDataSource.subTabsContent[t] = new Array();
          this._newDataSource.subTabsHover[t] = new Array();
          dojo.forEach(_5b7, dojo.hitch(this, function(_5b9, x) {
            var _5ba = dojo.create("li", {
              className: "subTab-" + t + "-" + x,
              role: ""
            }, _5ac);
            dijit.setWaiRole(_5ba, "presentation");
            if (x == 0) {
              if (t == 0) {
                dojo.addClass(_5ba, "ibm-active");
              }
              this._newDataSource.subTabsActItem[t] = _5b9;
              this._newDataSource.subTabsActItem_idx[t] = 0;
            }
            var link = dojo.create("a", {
              href: "#",
              innerHTML: _5b9,
              role: ""
            }, _5ba);
            dijit.setWaiRole(link, "button");
            dojo.style(_5ba, "display", "none");
            this._newDataSource.subTabs[t][x] = _5ba;
            this._newDataSource.subTabsContent[t][x] = dojo.query(".ibm-menu-dynamic", _5b8[0])[x];
            this._newDataSource.subTabsHover[t][x] = false;
            dojo.connect(link, "onfocus", this, function(_5bb) {
              dojo.stopEvent(_5bb);
              jump(_5bb, link);
            });
            dojo.connect(link, "onclick", function(_5bc) {
              dojo.stopEvent(_5bc);
            });
            dojo.connect(link, "onmouseover", function(_5bd) {
              dojo.stopEvent(_5bd);
              link.focus();
            });
            dojo.connect(link, "onmouseout", this, function(_5be) {
              dojo.stopEvent(_5be);
              this._newDataSource.subTabsHover[t][x] = false;
            });
            dojo.connect(link, "onkeypress", this, function(_5bf) {
              if (_5bf.keyCode == dojo.keys.ESCAPE) {
                dojo.stopEvent(_5bf);
                dojo.publish("/ibm/dynnav/megamenu/slideUp");
              }
              if (_5bf.keyCode == dojo.keys.LEFT_ARROW || _5bf.keyCode == dojo.keys.UP_ARROW) {
                var prev = _5bf.target.parentNode.previousSibling;
                if (dojo.indexOf(this._newDataSource.subTabs[t], prev) >= 0) {
                  dojo.stopEvent(_5bf);
                  prev.firstChild.focus();
                } else {
                  dojo.stopEvent(_5bf);
                  var last = this._newDataSource.subTabs[t].length - 1;
                  dojo.query(this._newDataSource.subTabs[t][last]).children("a")[0].focus();
                }
              }
              if (_5bf.keyCode == dojo.keys.RIGHT_ARROW || _5bf.keyCode == dojo.keys.DOWN_ARROW) {
                var next = _5bf.target.parentNode.nextSibling;
                if (dojo.indexOf(this._newDataSource.subTabs[t], next) >= 0) {
                  dojo.stopEvent(_5bf);
                  next.firstChild.focus();
                } else {
                  dojo.stopEvent(_5bf);
                  dojo.query(this._newDataSource.subTabs[t][0]).children("a")[0].focus();
                }
              }
              if (_5bf.keyCode == dojo.keys.ENTER || (_5bf.keyCode == dojo.keys.SPACE || _5bf.charOrCode == " ")) {
                dojo.stopEvent(_5bf);
                var _5c0 = dojo.query(this._newDataSource.subTabsContent[t][x]).children()[0],
                  ul = dojo.query(_5c0).children()[1],
                  li = dojo.query(ul).children()[0],
                  a = dojo.query(li).children()[0];
                a.focus();
              }
            });
          }));
        }
      }));
      dojo.forEach(dojo.query(".ibm-menu-dynamic a", con[0]), dojo.hitch(this, function(link) {
        dojo.connect(link, "onclick", dojo.hitch(this, function(_5c1) {
          dojo.stopEvent(_5c1);
          ibmweb.util.statsHelper({
            "ibmEV": ibmweb.config.config + " Megamenu",
            "ibmEvAction": _5c1.target.href,
            "ibmEvName": ((ibmweb.config.config === "www") ? this._unicaTagsWWWMenuNames[this._currentMenu] : this._unicaTagsW3MenuNames[this._currentMenu]) + " - " + this._newDataSource.subTabsActItem[this._newDataSource.curSubMenu],
            "ibmEvSection": "Link Clicks"
          });
          this._waitBeforeAssignPage = setTimeout(dojo.hitch(this, function() {
            clearTimeout(this._waitBeforeAssignPage);
            location.assign(_5c1.target.href);
          }), 800);
        }));
        dojo.connect(link, "onfocus", function(_5c2) {
          _5c2.target.className = "ibm-active";
        });
        dojo.connect(link, "onblur", function(_5c3) {
          _5c3.target.className = "";
        });
        dojo.connect(link, "onkeypress", dojo.hitch(this, function(_5c4) {
          if (_5c4.keyCode == dojo.keys.ENTER) {
            dojo.stopEvent(_5c4);
            ibmweb.util.statsHelper({
              "ibmEV": ibmweb.config.config + " Megamenu",
              "ibmEvAction": _5c4.target.href,
              "ibmEvName": ((ibmweb.config.config === "www") ? this._unicaTagsWWWMenuNames[this._currentMenu] : this._unicaTagsW3MenuNames[this._currentMenu]) + " - " + this._newDataSource.subTabsActItem[this._newDataSource.curSubMenu],
              "ibmEvSection": "Link Clicks"
            });
            this._waitBeforeAssignPage = setTimeout(dojo.hitch(this, function() {
              clearTimeout(this._waitBeforeAssignPage);
              location.assign(_5c4.target.href);
            }), 800);
          }
        }));
      }));
      dojo.forEach(dojo.query(".ibm-menu-static a", con[0]), dojo.hitch(this, function(link) {
        dojo.connect(link, "onfocus", function(_5c5) {
          _5c5.target.className = "ibm-active";
        });
        dojo.connect(link, "onblur", function(_5c6) {
          _5c6.target.className = "";
        });
        dojo.connect(link, "onclick", dojo.hitch(this, function(_5c7) {
          dojo.stopEvent(_5c7);
          ibmweb.util.statsHelper({
            "ibmEV": ibmweb.config.config + " Megamenu",
            "ibmEvAction": _5c7.target.href,
            "ibmEvName": ((ibmweb.config.config === "www") ? this._unicaTagsWWWMenuNames[this._currentMenu] : this._unicaTagsW3MenuNames[this._currentMenu]) + " - persistentColumn",
            "ibmEvSection": "Link Clicks"
          });
          this._waitBeforeAssignPage = setTimeout(dojo.hitch(this, function() {
            clearTimeout(this._waitBeforeAssignPage);
            location.assign(_5c7.target.href);
          }), 800);
        }));
        dojo.connect(link, "onkeypress", dojo.hitch(this, function(_5c8) {
          if (_5c8.keyCode == dojo.keys.ENTER) {
            dojo.stopEvent(_5c8);
            ibmweb.util.statsHelper({
              "ibmEV": ibmweb.config.config + " Megamenu",
              "ibmEvAction": _5c8.target.href,
              "ibmEvName": ((ibmweb.config.config === "www") ? this._unicaTagsWWWMenuNames[this._currentMenu] : this._unicaTagsW3MenuNames[this._currentMenu]) + " - persistentColumn",
              "ibmEvSection": "Link Clicks"
            });
            this._waitBeforeAssignPage = setTimeout(dojo.hitch(this, function() {
              clearTimeout(this._waitBeforeAssignPage);
              location.assign(_5c8.target.href);
            }), 800);
          }
          if (!_5c8.shiftKey && _5c8.keyCode == dojo.keys.TAB) {
            dojo.stopEvent(_5c8);
          }
        }));
      }));
      dojo.connect(dijit.byId(_5af), "slideTo", dojo.hitch(this, function(i) {
        if (typeof this._newDataSource.subTabs[i] !== "undefined") {
          if (this._newDataSource.hidden) {
            if (this._newDataSource.isBeingAnimated) {
              return false;
            }
            this._newDataSource.isBeingAnimated = true;
            dojo.animateProperty({
              node: _5ab,
              properties: {
                opacity: {
                  start: 0,
                  end: 1
                },
                marginTop: {
                  start: -39,
                  end: 0
                },
                marginBottom: {
                  start: 20,
                  end: 0
                }
              },
              duration: 600,
              onBegin: function() {
                dojo.query("#ibm-masthead .ibm-mm-close")[0].style.marginTop = "0px";
              },
              onEnd: dojo.hitch(this, function() {
                this._newDataSource.hidden = false;
                this._newDataSource.isBeingAnimated = false;
                dojo.query("#ibm-masthead .ibm-mm-close")[0].style.marginTop = "-65px";
              })
            }).play();
          }
        } else {
          if (!this._newDataSource.hidden) {
            if (this._newDataSource.isBeingAnimated) {
              return false;
            }
            this._newDataSource.isBeingAnimated = true;
            dojo.animateProperty({
              node: _5ab,
              properties: {
                opacity: {
                  start: 1,
                  end: 0
                },
                marginTop: {
                  start: 0,
                  end: -39
                },
                marginBottom: {
                  start: 0,
                  end: 20
                }
              },
              duration: 600,
              onBegin: function() {
                dojo.query("#ibm-masthead .ibm-mm-close")[0].style.marginTop = "0px";
              },
              onEnd: dojo.hitch(this, function() {
                this._newDataSource.hidden = true;
                this._newDataSource.isBeingAnimated = false;
                dojo.query("#ibm-masthead .ibm-mm-close")[0].style.marginTop = "-45px";
              })
            }).play();
          }
        }
      }));
    },
    _buildMegaMenu: function(data, _5c9) {
      var id = ibmweb.util.generateId(),
        div = dojo.create("div", {
          className: "ibm-columns",
          id: id
        });
      this._subTabs._div[_5c9] = div;
      this._subTabs._id[_5c9] = id;
      if (this._newDataSource.use && data.length > 1) {
        dijit.setWaiRole(div, "group");
        dojo.attr(div, "aria-labelledby", "ibm-rib-head-" + _5c9);
        var _5ca = [],
          _5cb = 4,
          con, _5cc, _5cd, _5ce = dojo.create("div"),
          _5cf;
        dojo.forEach(data, dojo.hitch(this, function(_5d0, dIdx) {
          this._newDataSource.hasDynTitle = false;
          var dif = 1;
          if (dojo.filter(_5d0.child, function(item) {
              return item.persistentColumn;
            }).length == 1) {
            if (dIdx > 6) {
              return;
            }
            dif = 2;
          } else {
            if (dIdx > 5) {
              return;
            }
          }
          if (typeof this._subTabs._title[_5c9] == "undefined") {
            this._subTabs._title[_5c9] = new Array();
          }
          this._subTabs._title[_5c9][dIdx] = _5d0.title.htmlspecialchars();
          var _5d1 = -1,
            _5d2 = dojo.create("div", {
              className: "ibm-col-5-4 ibm-menu-dynamic"
            }, _5ce);
          if (dIdx > 0) {
            dojo.style(_5d2, "display", "none");
          }
          dojo.forEach(_5d0.child, dojo.hitch(this, function(_5d3, x) {
            if (_5d3.persistentColumn && _5d3.persistentColumn == "yes") {
              _5cf = dojo.create("div", {
                className: "ibm-col-5-1 ibm-menu-static"
              });
              var _5d4 = dojo.create("h3", {
                  innerHTML: _5d3.title.htmlspecialchars(),
                  id: "ibm-menu-related-links-title"
                }, _5cf),
                _5d5 = dojo.create("ul", {
                  role: "",
                  "aria-labelledby": "ibm-menu-related-links-title"
                }, _5cf);
              dijit.setWaiRole(_5d5, "menu");
              dojo.forEach(_5d3.child, function(_5d6) {
                var tLi = dojo.create("li", {
                    role: ""
                  }, _5d5),
                  tA = dojo.create("a", {
                    href: _5d6.url,
                    tabindex: -1,
                    role: "",
                    innerHTML: _5d6.title.htmlspecialchars()
                  }, tLi);
                dijit.setWaiRole(tLi, "presentation");
                dijit.setWaiRole(tA, "menuitem");
              });
            } else {
              _5d1++;
              if (_5d1 >= _5cb) {
                dojo.addClass(dojo.query(".ibm-col-4-1:last-child", _5d2)[0], "ibm-col-last");
                return;
              }
              var _5d7 = (x + dif == _5d0.child.length) ? " ibm-col-last" : "",
                _5d8 = dojo.create("div", {
                  className: "ibm-col-4-1" + _5d7,
                  role: ""
                }, _5d2),
                h3 = dojo.create("h3", null, _5d8),
                _5d9 = dojo.create("ul", {
                  role: "menu"
                }, _5d8);
              dijit.setWaiRole(_5d8, "menu");
              dojo.attr(_5d8, "aria-label", "menu list");
              dojo.attr(_5d9, "aria-label", "menu list");
              if (_5d3.title == "" || _5d3.title == " " || _5d3.title.length == 0) {
                dojo.destroy(h3);
                dojo.style(_5d9, "marginTop", "26px");
              } else {
                if (!this._newDataSource.hasDynTitle) {
                  this._newDataSource.hasDynTitle = true;
                }
                if (_5d3.url && _5d3.url.length > 0 && _5d3.url != " ") {
                  dojo.create("a", {
                    tabIndex: "-1",
                    href: _5d3.url,
                    innerHTML: _5d3.title.htmlspecialchars()
                  }, h3);
                  dojo.attr(h3, "role", "");
                  dijit.setWaiRole(h3, "menuitem");
                } else {
                  dojo.attr(h3, "innerHTML", _5d3.title.htmlspecialchars());
                }
              }
              dojo.forEach(_5d3.child, function(_5da, cIdx) {
                var _5db = dojo.create("li", {
                    role: ""
                  }, _5d9),
                  _5dc = dojo.create("a", {
                    href: _5da.url,
                    tabIndex: "-1",
                    role: "",
                    innerHTML: _5da.title.htmlspecialchars()
                  }, _5db);
                dijit.setWaiRole(_5db, "presentation");
                dijit.setWaiRole(_5dc, "menuitem");
              });
            }
          }));
          if (!this._newDataSource.hasDynTitle) {
            dojo.style(_5d2, "paddingTop", "0px");
          }
        }));
        if (typeof _5cf !== "undefined") {
          dojo.place(_5cf, _5ce);
        }
        div.innerHTML = _5ce.innerHTML;
      }
      if (!this._newDataSource.use || (this._newDataSource.use && data.length == 1)) {
        dijit.setWaiRole(div, "group");
        dojo.attr(div, "aria-labelledby", "ibm-rib-head-" + _5c9);
        var _5dd = null,
          _5de = dojo.hitch(this, function(_5df) {
            var col = dojo.create("div", {
              className: "ibm-col-6-1"
            }, div);
            dijit.setWaiRole(col, "menu");
            dojo.attr(col, "aria-label", "menu list");
            if (_5df.title && _5df.title.length > 0 && _5df.title != " ") {
              _5dd = _5df.title.htmlspecialchars();
              co = 1;
            }
            if (_5dd == null) {
              _5dd = this._itemtitle;
            }
            if (_5df.url) {
              var h3 = dojo.create("h3", {}, col),
                a = dojo.create("a", {
                  innerHTML: _5df.title.htmlspecialchars(),
                  href: _5df.url,
                  tabIndex: "-1"
                }, h3);
              dijit.setWaiRole(h3, "menuitem");
              dojo.connect(a, "onfocus", this, function(_5e0) {
                _5e0.target.className = "ibm-active";
              });
              dojo.connect(a, "onblur", this, function(_5e1) {
                _5e1.target.className = "";
              });
              dijit.setWaiState(col, "label", _5df.title.htmlspecialchars() + " menu");
            } else {
              if (_5df.title == " ") {
                co++;
                dojo.create("h3", {
                  innerHTML: _5dd + " menu list " + co,
                  className: "ibm-access"
                }, col);
                dijit.setWaiState(col, "label", _5dd + " menu list " + co);
              } else {
                dojo.create("h3", {
                  innerHTML: _5df.title.htmlspecialchars()
                }, col);
                dijit.setWaiState(col, "label", _5dd + " menu");
              }
            }
            var ul;
            if (_5df.title == " ") {
              ul = dojo.create("ul", _5df.extraClass ? {
                "className": "ibm-" + _5df.extraClass,
                style: {
                  marginTop: ((ibmweb.config.config == "www") ? 26 : 19) + "px"
                }
              } : {
                style: {
                  marginTop: ((ibmweb.config.config == "www") ? 26 : 19) + "px"
                }
              }, col);
            } else {
              ul = dojo.create("ul", _5df.extraClass ? {
                "className": "ibm-" + _5df.extraClass
              } : null, col);
            }
            dijit.setWaiRole(ul, "menu");
            dojo.attr(ul, "aria-label", _5dd + " menu list");
            var _5e2 = (_5df.child.length > ibmweb.config.megamenu.maxRows) ? ibmweb.config.megamenu.maxRows : _5df.child.length;
            for (var k = 0; k < _5e2; k++) {
              var _5e3 = _5df.child[k];
              if (_5e3.url != null && _5e3.url != "" && _5e3.title.htmlspecialchars().replace(/^\s+|\s+$/g, "") != "") {
                var li = dojo.create("li", {}, ul);
                dijit.setWaiRole(li, "presentation");
                var a = dojo.create("a", {
                  innerHTML: _5e3.title.htmlspecialchars(),
                  href: _5e3.url,
                  tabIndex: "-1"
                }, li);
                dijit.setWaiRole(a, "menuitem");
                dojo.connect(a, "onclick", dojo.hitch(this, function(_5e4) {
                  dojo.stopEvent(_5e4);
                  ibmweb.util.statsHelper({
                    "ibmEV": ibmweb.config.config + " Megamenu",
                    "ibmEvAction": _5e4.target.href,
                    "ibmEvName": ((ibmweb.config.config === "w3") ? this._unicaTagsW3MenuNames[this._currentMenu] : this._unicaTagsWWWMenuNames[this._currentMenu]),
                    "ibmEvSection": "Link Clicks"
                  });
                  this._waitBeforeAssignPage = setTimeout(dojo.hitch(this, function() {
                    clearTimeout(this._waitBeforeAssignPage);
                    location.assign(_5e4.target.href);
                  }), 800);
                }));
                dojo.connect(a, "onkeypress", dojo.hitch(this, function(_5e5) {
                  if (_5e5.keyCode == dojo.keys.ENTER) {
                    dojo.stopEvent(_5e5);
                    ibmweb.util.statsHelper({
                      "ibmEV": ibmweb.config.config + " Megamenu",
                      "ibmEvAction": _5e5.target.href,
                      "ibmEvName": ((ibmweb.config.config === "w3") ? this._unicaTagsW3MenuNames[this._currentMenu] : this._unicaTagsWWWMenuNames[this._currentMenu]),
                      "ibmEvSection": "Link Clicks"
                    });
                    this._waitBeforeAssignPage = setTimeout(dojo.hitch(this, function() {
                      clearTimeout(this._waitBeforeAssignPage);
                      location.assign(_5e5.target.href);
                    }), 800);
                  }
                }));
                dojo.connect(a, "onfocus", this, function(_5e6) {
                  _5e6.target.className = "ibm-active";
                });
                dojo.connect(a, "onblur", this, function(_5e7) {
                  _5e7.target.className = "";
                });
              }
            }
          }),
          max = (data.length > ibmweb.config.megamenu.maxColumns) ? ibmweb.config.megamenu.maxColumns : data.length;
        for (var co, i = 0; i < max; i++) {
          if (this._newDataSource.use) {
            var _5e8 = data[i].child;
            dojo.style(div, "paddingTop", "10px");
            dojo.forEach(_5e8, dojo.hitch(this, function(cd) {
              _5de(cd);
            }));
          } else {
            _5de(data[i]);
          }
        }
      }
      return div;
    },
    _slideDown: function(id) {
      if (!id && arguments[0]) {
        id = arguments[0];
      }
      if (this._animationInProgress) {
        return false;
      }
      this._animationInProgress = true;
      if (this._newDataSource.use && this._newDataSource.subTabMenu !== null) {
        if (typeof this._newDataSource.subTabs[id] === "undefined") {
          dojo.style(this._newDataSource.subTabMenu, {
            marginBottom: "20px",
            marginTop: "-39px",
            opacity: 0
          });
          this._newDataSource.hidden = true;
        } else {
          dojo.addClass(this._newDataSource.subTabs[id][this._newDataSource.subTabsActItem_idx[id]], "ibm-active");
        }
      }
      dojo.query("#ibm-common-menu").style({
        display: "block",
        height: "1px",
        overflow: "hidden"
      });
      dojo.anim("ibm-common-menu", {
        height: 292
      }, 200);
      if (!ibmweb.config.megamenu.noScroll) {
        if (true) {
          if ((ibmweb.info.isIE && ibmweb.info.ieVersion <= 6) || ibmweb.info.iDevice) {} else {
            dojo.anim("ibm-top", {
              marginTop: 292 + this._dimensions.basicMarginTop
            }, 200);
          }
        }
      }
      dojo.animateProperty({
        node: dojo.query("#ibm-common-menu .ibm-ribbon-pane")[0],
        duration: 200,
        properties: {
          height: {
            start: "1",
            end: "292"
          }
        },
        onBefore: function() {
          if (this._newDataSource.use) {
            dojo.query("#ibm-masthead .ibm-mm-close")[0].style.marginTop = "0px";
          }
        },
        onEnd: dojo.hitch(this, function() {
          this._animationInProgress = false;
          dojo.query("div#ibm-common-menu .ibm-ribbon-section a").attr("tabIndex", "-1");
          var temp = dojo.query("div#ibm-common-menu .ibm-ribbon-section > div");
          if (temp.length > 0) {
            temp = dojo.query("a", temp[id]);
          }
          if (temp.length != 0) {
            temp.attr("tabIndex", "0");
          }
          if (this._newDataSource.use) {
            dojo.query("#ibm-masthead .ibm-mm-close")[0].style.marginTop = (typeof this._newDataSource.subTabs[id] === "undefined") ? "-45px" : "-65px";
          }
        })
      }).play();
      dojo.query("#ibm-menu-links a")[id].className = "ibm-active";
      dojo.query("#ibm-menu-links li")[id].className = "ibm-active";
      dojo.query("#ibm-menu-links li.ibm-inactive").removeClass("ibm-inactive");
      this._currentMenu = id;
      if (this._newDataSource.use) {
        if (typeof this._newDataSource.subTabs[id] !== "undefined") {
          this._newDataSource.curSubMenu = id;
          dojo.forEach(this._newDataSource.subTabs[id], dojo.hitch(this, function(_5e9) {
            dojo.style(_5e9, "display", "block");
          }));
        }
      }
      if (!this._lock) {
        this._lock = true;
        dojo.query("#ibm-common-menu .ibm-col-6-1 ul").forEach(function(item) {
          var h = dojo.coords(item, true).h;
          if (item.parentNode.firstChild.className == "ibm-access") {
            for (; h > 272; h = dojo.coords(item, true).h) {
              dojo.destroy(item.lastChild);
            }
          } else {
            for (; h > 242; h = dojo.coords(item, true).h) {
              dojo.destroy(item.lastChild);
            }
          }
        });
      }
    },
    _slideUp: function(evt, _5ea) {
      if (this._currentMenu == null || this._animationInProgress) {
        return;
      }
      if (this._slideToTimer) {
        clearTimeout(this._slideToTimer);
        this._slideToTimer = null;
      }
      this._animationInProgress = true;
      var id = this._currentMenu;
      dojo.animateProperty({
        node: "ibm-common-menu",
        duration: 400,
        properties: {
          height: {
            start: "292",
            end: "1"
          }
        },
        onEnd: dojo.hitch(this, function() {
          dojo.query("#ibm-common-menu").style("display", "none");
          if (this._newDataSource.use && this._newDataSource.subTabMenu !== null) {
            dojo.style(this._newDataSource.subTabMenu, {
              marginBottom: "20px",
              marginTop: "-39px",
              opacity: 0
            });
            this._newDataSource.hidden = true;
          }
          if (ibmweb.config.config === "w3") {
            this._w3SwapDesign.currentLink = -1;
          }
          if (_5ea) {
            setTimeout(_5ea, 1);
          }
        })
      }).play();
      dojo.animateProperty({
        node: dojo.query("#ibm-common-menu .ibm-ribbon-pane")[0],
        duration: 400,
        properties: {
          height: {
            start: "292",
            end: "1"
          }
        },
        onEnd: dojo.hitch(this, function() {
          if (this._newDataSource.use) {
            if (typeof this._newDataSource.subTabs[id] !== "undefined") {
              dojo.forEach(this._newDataSource.subTabs[id], dojo.hitch(this, function(_5eb) {
                dojo.style(_5eb, "display", "none");
              }));
            }
            this._newDataSource.curSubMenu = null;
          }
          this._animationInProgress = false;
          if (ibmweb.config.megamenu.minimizeByDefault) {
            (evt && evt.target && evt.target.id === "ibm-masthead") ? this._minimizeMenu(): null;
          }
        })
      }).play();
      dojo.query("#ibm-common-menu a").attr("tabIndex", "-1");
      this._currentMenu = null;
      var _5ec = dojo.query("#ibm-menu-links a.ibm-active")[0];
      if (typeof _5ec !== "undefined") {
        _5ec.focus();
        _5ec.parentNode.className = "ibm-inactive";
        dojo.removeClass(_5ec, "ibm-active");
      }
      if (!((ibmweb.info.isIE && ibmweb.info.ieVersion <= 6) || ibmweb.info.iDevice)) {
        dojo.anim("ibm-top", {
          marginTop: (!ibmweb.config.megamenu.noScroll) ? (ibmweb.config.megamenu.minimizeByDefault && this._isMinimizingOnMouseLeaveSemaphore === true) ? 33 : this._dimensions.basicMarginTop : 0
        }, 370);
      }
    },
    onVScrollCallback: function(_5ed) {
      if (this._mouseover || this._keepexpanded || this._beingAnimated) {
        return;
      }
      var _5ee = true,
        _5ef = (this._isExpanded) ? this._dimensions.basicMarginTop : 36,
        _5f0 = (self.pageYOffset) ? self.pageYOffset : (document.documentElement && document.documentElement.scrollTop) ? document.documentElement.scrollTop : (document.body) ? document.body.scrollTop : 0;
      if ((this._isExpanded == false && _5f0 < _5ef) || (this._isExpanded == true && _5f0 <= _5ef && _5f0 >= 0)) {
        _5ee = false;
      }
      if (!ibmweb.config.megamenu.minimizeByDefault) {
        if (!window.pageYOffset && document.documentElement.scrollTop == 0 || (window.pageYOffset && window.pageYOffset == 0)) {
          if (dojo.byId("ibm-menu-links").className == "ibm-access") {
            this._expandMenu();
          }
        } else {
          if (dojo.byId("ibm-menu-links").className != "ibm-access" && _5ee) {
            this._minimizeMenu();
          }
        }
      } else {
        this._minimizeMenu();
      }
    },
    onHScrollCallback: function(_5f1) {
      dojo.stopEvent(_5f1);
      if (!window.pageXOffset && window.pageXOffset != 0) {
        dojo.style(dojo.byId("ibm-masthead"), "left", "-" + document.documentElement.scrollLeft + "px");
      } else {
        dojo.style(dojo.byId("ibm-masthead"), "left", "-" + window.pageXOffset + "px");
      }
    },
    isTypeAheadResultDisplay: function() {
      var _5f2 = false;
      if (dojo.query(".typeahead-results").some(function(node) {
          return node.style.display != "none";
        })) {
        _5f2 = true;
      }
      return _5f2;
    },
    isScopePopupDisplay: function() {
      var _5f3 = dojo.query("#ibm-scope-button_dropdown.dijitPopup");
      if (_5f3 && _5f3.length > 0 && _5f3[0].style.display != "none" && _5f3[0].style.visibility != "hidden") {
        return true;
      }
      return false;
    },
    _minimizeMenu: function(_5f4) {
      if (this.isTypeAheadResultDisplay() || this.isScopePopupDisplay()) {
        return;
      }
      _5f4 = _5f4 || 150;
      this._slideUp();
      if ((ibmweb.info.isIE && ibmweb.info.ieVersion <= 6) || ibmweb.info.iDevice) {
        return false;
      }
      var _5f5 = (ibmweb.config.config == "www" ? 30 : 0),
        _5f6 = (ibmweb.config.config == "www" ? 4 : 31);
      if (dojo.byId("ibm-universal-nav").style.height != (_5f5 + "px")) {
        dojo.animateProperty({
          node: "ibm-universal-nav",
          properties: {
            height: _5f5
          },
          beforeBegin: dojo.hitch(this, function() {
            this._beingAnimated = true;
          }),
          duration: _5f4
        }).play();
      }
      if (dojo.byId("ibm-mast-options").style.height != (_5f6 + "px")) {
        dojo.animateProperty({
          node: "ibm-mast-options",
          properties: {
            height: _5f6
          },
          beforeBegin: dojo.hitch(this, function() {
            if (ibmweb.config.config == "w3" && this._w3SwapDesign.profileLinks !== null) {
              dojo.style(this._w3SwapDesign.profileLinks, "display", "none");
            }
          }),
          duration: _5f4
        }).play();
      }
      dojo.animateProperty({
        node: "ibm-top",
        properties: {
          marginTop: (!ibmweb.config.megamenu.noScroll) ? 33 : 0
        },
        duration: _5f4,
        onEnd: dojo.hitch(this, function() {
          if (ibmweb.config.megamenu.icons != "none") {
            dojo.query(".ibm-masthead-indicator").style("display", "block").removeClass("ibm-access");
          }
          dojo.publish("/ibm/dynnav/megamenu/minimized");
          this._beingAnimated = false;
          this._isExpanded = false;
        })
      }).play();
      if (ibmweb.config.config == "www") {
        dojo.query("#ibm-mast-options li, #ibm-mast-options ul").addClass("ibm-access");
      }
      if (ibmweb.config.config == "w3") {
        dojo.query("#ibm-mast-options li").addClass("ibm-access");
        dojo.query("#ibm-menu-links li").addClass("ibm-access");
      }
      if (ibmweb.config.config == "www") {
        dojo.query("#ibm-menu-links, #ibm-search-module,#ibm-arrow-logo").addClass("ibm-access");
      }
      if (ibmweb.config.config == "w3") {
        if (this._w3SwapDesign.secondSwitchEnabled) {
          dojo.query("#ibm-menu-links, #ibm-search-module,#ibm-arrow-logo").addClass("ibm-access");
        } else {
          dojo.query("#ibm-menu-links,#ibm-arrow-logo").addClass("ibm-access");
          dojo.style(dojo.byId("ibm-search-module"), "display", "none");
        }
      }
      dojo.style("ibm-home", "display", "block");
      if (ibmweb.config.config == "w3") {
        dojo.query("#ibm-home").removeClass("ibm-access");
      }
      dojo.addClass("ibm-home", "ibm-sm-logo");
    },
    _expandMenu: function() {
      if ((ibmweb.info.isIE && ibmweb.info.ieVersion <= 6) || ibmweb.info.iDevice) {
        return false;
      }
      var _5f7 = (ibmweb.config.config == "www" ? 21 : 38);
      if (ibmweb.config.megamenu.icons != "none") {
        dojo.query(".ibm-masthead-indicator").style("display", "none").addClass("ibm-access");
      }
      dojo.animateProperty({
        node: "ibm-universal-nav",
        properties: {
          height: this._dimensions.basicUniversalHeight
        },
        beforeBegin: dojo.hitch(this, function() {
          this._beingAnimated = true;
        }),
        duration: 150
      }).play();
      dojo.animateProperty({
        node: "ibm-mast-options",
        properties: {
          height: _5f7
        },
        duration: 150,
        onEnd: dojo.hitch(this, function() {
          dojo.query("#ibm-mast-options li, #ibm-mast-options ul").removeClass("ibm-access");
          dojo.query("#ibm-menu-links li").removeClass("ibm-access");
          if (ibmweb.config.config == "www") {
            dojo.query("#ibm-menu-links, #ibm-search-module").removeClass("ibm-access");
          }
          if (ibmweb.config.config == "w3") {
            if (this._w3SwapDesign.secondSwitchEnabled) {
              dojo.query("#ibm-menu-links,#ibm-search-module").removeClass("ibm-access");
            } else {
              dojo.query("#ibm-menu-links").removeClass("ibm-access");
              dojo.style(dojo.byId("ibm-search-module"), "display", "block");
            }
            dojo.query("#ibm-menu-links li").removeClass("ibm-access");
            if (this._w3SwapDesign.profileLinks !== null) {
              dojo.style(this._w3SwapDesign.profileLinks, "display", "block");
            }
          }
          dojo.removeClass("ibm-home", "ibm-sm-logo");
          dojo.publish("/ibm/dynnav/megamenu/expanded");
        })
      }).play();
      dojo.animateProperty({
        node: "ibm-top",
        properties: {
          marginTop: (!ibmweb.config.megamenu.noScroll) ? this._dimensions.basicMarginTop : 0
        },
        duration: 150,
        onEnd: dojo.hitch(this, function() {
          this._beingAnimated = false;
          this._isExpanded = true;
        })
      }).play();
    },
    onMouseEnter: function(_5f8) {
      clearTimeout(this._slideUpTimer);
      this._expandTimer = setTimeout(dojo.hitch(this, function() {
        this._mouseleaveHandlerEnabled = true;
        this._mouseover = true;
        if (dojo.byId("ibm-menu-links").className == "ibm-access") {
          this._expandMenu();
        }
      }), 350);
    },
    onMouseLeave: function(_5f9) {
      if (this._delayedExpandTimer) {
        clearTimeout(this._delayedExpandTimer);
      }
      if (this._expandTimer) {
        clearTimeout(this._expandTimer);
        this._expandTimer = null;
      }
      if (!this._mouseleaveHandlerEnabled) {
        return;
      }
      this._slideUpTimer = setTimeout(dojo.hitch(this, function() {
        if (this._currentMenu !== null) {
          ibmweb.util.statsHelper({
            "ibmEV": ibmweb.config.config + " Megamenu",
            "ibmEvAction": "Mouse away",
            "ibmEvName": "Close"
          });
        }
        this._mouseover = false;
        this._isMinimizingOnMouseLeaveSemaphore = true;
        this._slideUp(_5f9);
        this._isMinimizingOnMouseLeaveSemaphore = false;
        (!ibmweb.config.megamenu.noScroll) ? this.onVScrollCallback(_5f9): null;
      }), this._slideUpDuration);
    },
    setKeepExpanded: function(_5fa) {
      this._keepexpanded = !!_5fa;
    },
    onError: function() {}
  });
}
if (!dojo._hasResource["ibmweb.dynnav.app-megamenu"]) {
  dojo._hasResource["ibmweb.dynnav.app-megamenu"] = true;
  dojo.provide("ibmweb.dynnav.app-megamenu");
  dojo.declare("ibmweb.dynnav.app-megamenu", ibmweb.dynnav._module, {
    internal: {
      _status: false,
      _isAnimated: false,
      _i: 0,
      _leaveInterval: null,
      _firstClick: true,
      _actionInterval: null,
      _actionBuffer: null,
      _isMoving: false,
      _isMovingInterval: null
    },
    init: function() {
      return ibmweb.config.appmast.enabled;
    },
    onLoad: function() {
      var _5fb = this;
      var _5fc = dojo.query("div.ibm-menu-subtabs");
      var _5fd = dojo.query("div.ibm-menu-subtabs a");
      var menu = dojo.byId("ibm-common-menu");
      var _5fe = dojo.query("#ibm-common-menu div.ibm-mm-close span");
      var _5ff = dojo.byId("ibm-masthead");
      var _600 = dojo.query("#ibm-common-menu div.ibm-container-body > div.ibm-columns");
      if (_5fc.length == 1 && _600.length == _5fd.length && menu && _5fe.length == 1) {
        dojo.query("li:first-child", _5fc[0]).addClass("ibm-active");
        var _601 = function(node, i, _602) {
          if (_5fb.internal._isAnimated) {
            return false;
          }
          if (!_5fb.internal._status) {
            _5fb.internal._status = true;
            _5fb.internal._isAnimated = true;
            _5fb.internal._i = i;
            dojo.addClass(node.parentNode, "ibm-active");
            dojo.addClass(_5fc[0], "ibm-active");
            dojo.style(menu, {
              display: "block"
            });
            _600.forEach(function(_603, _604) {
              dojo.style(_603, "display", _604 == i ? "block" : "none");
            });
            _605 = dojo.animateProperty({
              node: menu,
              duration: 600,
              properties: {
                height: {
                  start: 1,
                  end: 292
                }
              },
              onEnd: function() {
                _5fb.internal._isAnimated = false;
              }
            }).play();
          } else {
            if (dojo.hasClass(node.parentNode, "ibm-active")) {
              if (!_602) {
                dojo.publish("/ibm/app-megamenu/slideUp");
              }
              return false;
            }
            _5fb.internal._isAnimated = true;
            dojo.query("#ibm-masthead div.ibm-menu-subtabs li.ibm-active").removeClass("ibm-active");
            dojo.addClass(node.parentNode, "ibm-active");
            var _605 = dojo.animateProperty({
              node: _600[_5fb.internal._i],
              duration: 400,
              properties: {
                opacity: {
                  start: 1,
                  end: 0
                }
              },
              onEnd: function() {
                dojo.style(_600[_5fb.internal._i], {
                  opacity: "1",
                  display: "none"
                });
                dojo.style(_600[i], {
                  display: "block",
                  opacity: "0"
                });
                _605 = dojo.animateProperty({
                  node: _600[i],
                  duration: 300,
                  properties: {
                    opacity: {
                      start: 0,
                      end: 1
                    }
                  },
                  onEnd: function() {
                    _5fb.internal._i = i;
                    _5fb.internal._isAnimated = false;
                    if (_5fb.internal._actionBuffer instanceof Array && _5fb.internal._actionBuffer[0] !== node) {
                      _601(_5fb.internal._actionBuffer[0], _5fb.internal._actionBuffer[1], true);
                      _5fb.internal._actionBuffer = null;
                    }
                  }
                }).play();
              }
            });
            _605.play();
          }
        };
        _5fd.forEach(function(node, i) {
          node.onclick = dojo.hitch(this, function(e) {
            dojo.stopEvent(e);
            _601(node, i, false);
          });
          node.onmouseenter = dojo.hitch(this, function(e) {
            dojo.stopEvent(e);
            if (_5fb.internal._status) {
              _5fb.internal._actionBuffer = [node, i];
              window.clearInterval(_5fb.internal._actionInterval);
              _5fb.internal._actionInterval = setTimeout(function() {
                if (!_5fb.internal._isMoving && !_5fb.internal._isAnimated) {
                  _601(_5fb.internal._actionBuffer[0], _5fb.internal._actionBuffer[1], true);
                  _5fb.internal._actionBuffer = null;
                }
              }, 250);
            } else {
              if (_5fb.internal._firstClick) {
                if (i > 0) {
                  dojo.query("li:first-child", _5fc[0]).removeClass("ibm-active");
                }
                _5fb.internal._firstClick = false;
              }
              dojo.addClass(node.parentNode, "ibm-active");
            }
          });
          node.onmouseleave = dojo.hitch(this, function(e) {
            dojo.stopEvent(e);
            if (!_5fb.internal._status) {
              dojo.removeClass(node.parentNode, "ibm-active");
            }
          });
        });
        dojo.connect(dojo.doc.documentElement, "onmousemove", function(e) {
          window.clearInterval(_5fb.internal._isMovingInterval);
          _5fb.internal._isMoving = true;
          _5fb.internal._isMovingInterval = setTimeout(function() {
            window.clearInterval(_5fb.internal._isMovingInterval);
            _5fb.internal._isMoving = false;
          }, 50);
        });
        dojo.connect(_5fe[0], "onclick", function(e) {
          dojo.stopEvent(e);
          dojo.publish("/ibm/app-megamenu/slideUp");
        });
        dojo.subscribe("/ibm/app-megamenu/slideUp", function() {
          _5fb.internal._isAnimated = true;
          animation = dojo.animateProperty({
            node: menu,
            duration: 600,
            properties: {
              height: {
                start: 292,
                end: 1
              }
            },
            onEnd: function() {
              _5fb.internal._isAnimated = false;
              dojo.query("#ibm-masthead div.ibm-menu-subtabs li.ibm-active").removeClass("ibm-active");
              dojo.removeClass(_5fc[0], "ibm-active");
              _600.forEach(function(_606, _607) {
                dojo.style(_606, "display", "none");
              });
              dojo.style(menu, "display", "none");
              _5fb.internal._status = false;
            }
          }).play();
        });
        dojo.connect(_5ff, "onmouseleave", function(e) {
          if (_5fb.internal._status) {
            _5fb.internal._leaveInterval = setTimeout(function() {
              dojo.publish("/ibm/app-megamenu/slideUp");
            }, 800);
          }
        });
        dojo.connect(_5ff, "onmouseenter", function(e) {
          window.clearInterval(_5fb.internal._leaveInterval);
        });
        this.applyAria();
      }
    },
    onClick: function(e) {},
    onData: function(data) {
      var temp = dojo.byId("ibm-home");
      if (temp) {
        var ul = dojo.create("ul", {
          className: "ibm-logo-subnav"
        }, temp);
        for (var i = 0, j = data.menuData.length; i < j; i += 1) {
          dojo.create("li", {
            innerHTML: "<a href=\"" + data.menuData[i].url + "\">" + data.menuData[i].title + "</a>",
            className: i == 0 ? "ibm-first" : (i + 1 == j ? "ibm-last" : "")
          }, ul);
        }
        dojo.connect(ul, "onmouseenter", function(e) {
          dojo.addClass(temp, "ibm-active");
        });
        dojo.connect(ul, "onmouseleave", function(e) {
          dojo.removeClass(temp, "ibm-active");
        });
        dojo.connect(temp, "onmouseenter", function(e) {
          dojo.addClass(temp, "ibm-active");
        });
        dojo.connect(temp, "onmouseleave", function(e) {
          dojo.removeClass(temp, "ibm-active");
        });
      }
    },
    applyAria: function() {
      var _608 = dojo.query("#ibm-common-menu div.ibm-mm-close span");
      dojo.attr(_608[0], {
        "tabindex": "0",
        "aria-label": "Close",
        "role": "button"
      });
    },
    onError: function() {}
  });
}
if (!dojo._hasResource["ibmweb.dynnav._base-v17w3"]) {
  dojo._hasResource["ibmweb.dynnav._base-v17w3"] = true;
  dojo.provide("ibmweb.dynnav._base-v17w3");
}
if (!dojo._hasResource["ibmweb.controller"]) {
  dojo._hasResource["ibmweb.controller"] = true;
  dojo.provide("ibmweb.controller");
  (function() {
    var _609 = (function() {
      var self = "ibm_user_info";
      var data = null,
        _60a = false,
        _60b = [],
        _60c = typeof(ibmweb) !== "undefined" && typeof(ibmweb.config) !== "undefined" && typeof(ibmweb.config.w3UserServiceUrl) !== "undefined";

      function _60d(_60e, _60f) {
        _60b.push({
          success: _60e,
          fail: _60f
        });
        if (!_60a) {
          _60a = true;
          var _610 = dojo.cookie("w3ibmProfile");
          if (_610) {
            var hkey = _610.split("|")[0];
            dojo.io.script.get({
              url: ibmweb.config.w3UserServiceUrl + hkey + "/callback/" + self + ".setData",
              timeout: ibmweb.config.w3UserServiceTimeout,
              error: function(er1, er2) {
                _60c = false;
                if (console && console.warn) {}
                dojo.forEach(_60b, function(q) {
                  q.fail();
                });
              }
            });
          } else {
            _60c = false;
            dojo.forEach(_60b, function(q) {
              q.fail();
            });
          }
        }
      };
      var ret = function(_611, _612) {
        if (_60c && _611) {
          if (data === null) {
            _60d(_611, _612);
          } else {
            _611(data);
          }
        } else {
          _612();
        }
        return _60c;
      };
      ret.setData = function(_613) {
        data = _613;
        dojo.forEach(_60b, function(q) {
          q.success(data);
        });
      };
      return ret;
    })();
    if (ibmweb.meta.isLoaded) {
      ibmweb.dynnav.init();
    } else {
      dojo.connect(ibmweb.meta, "_init", function() {
        ibmweb.dynnav.init();
      });
    }
  })();
  dojo.addOnLoad(function() {
    var _614 = dojo.query(".v17, .nihilo, .soria, .tundra, .claro, .oneui, .lotusui, .ram"),
      node = dojo.body();
    if (!dojo.hasClass(node, "ibm-custom-theme")) {
      if (node.id && node.id == "ibm-com") {
        if (_614.length > 0) {
          var _615 = node.className.toString().match(/v17|nihilo|soria|tundra|claro|oneui|lotusui|ram/g),
            _616 = _614[0].className.toString().match(/v17|nihilo|soria|tundra|claro|oneui|lotusui|ram/g)[0];
          if (_615 == null) {
            dojo.addClass(node, _616);
          }
          if (_616 == "nihilo" || _616 == "soria" || _616 == "tundra" || _616 == "claro" || _616 == "oneui") {
            dojo.create("link", {
              rel: "stylesheet",
              type: "text/css",
              href: "//1.www.s81c.com/common/js/dojo/themes/" + _616 + ".css"
            }, dojo.query("head")[0], "last");
          }
        } else {
          dojo.addClass(node, "v17");
        }
      } else {
        if (node.id && node.id == "w3-ibm-com") {
          if (_614.length > 0) {
            var _615 = node.className.toString().match(/v17|nihilo|soria|tundra|claro|oneui|lotusui|ram/g),
              _616 = _614[0].className.toString().match(/v17|nihilo|soria|tundra|claro|oneui|lotusui|ram/g)[0];
            if (_615) {
              dojo.addClass(node, _616);
            }
            if (_616 == "nihilo" || _616 == "soria" || _616 == "tundra" || _616 == "claro" || _616 == "oneui") {
              dojo.query("head").append("<link rel=\"stylesheet\" type=\"text/css\" href=\"//1.www.s81c.com/common/js/dojo/themes/" + _616 + ".css\">");
            }
          } else {
            dojo.addClass(node, "v8");
          }
        }
      }
    }
  });
  dojo.addOnLoad(function() {
    if (!ibmweb.info.v17) {
      return;
    }
    if (dojo.query("table.ibm-alternating").length != 0 || dojo.query("table.ibm-alternating-col").length != 0 || dojo.query("table.ibm-sortable-table").length != 0) {
      dojo["require"]("ibmweb.table");
    }
    if (dojo.query("table.ibm-table-scroll")[0]) {
      dojo["require"]("ibmweb.table-scroll");
    }
  });
  dojo.addOnLoad(function() {
    if (!ibmweb.info.v17) {
      return;
    }
    if (dojo.query("div.ibm-common-overlay").length > 0) {
      dojo["require"]("ibmweb.overlay");
      dojo.addOnLoad(function() {
        ibmweb.overlay.init();
      });
    }
  });
  dojo.addOnLoad(function() {
    if (!ibmweb.info.v17) {
      return;
    }
    if (dojo.query(".ibm-leaving-prompt").length > 0) {
      dojo["require"]("ibmweb.leaving");
      dojo.addOnLoad(function() {
        ibmweb.leaving.init();
      });
    }
  });
  dojo.addOnLoad(function() {
    if (!ibmweb.info.v17) {
      return;
    }
    if (dojo.query(".ibm-tooltip, .ibm-tooltip-html").length > 0) {
      dojo["require"]("ibmweb.tooltip");
      dojo.addOnLoad(function() {
        ibmweb.tooltip.init();
      });
    }
  });
  dojo.addOnLoad(function() {
    var _617 = dojo.query(".ibm-ribbon .ibm-container-body");
    if (_617.length > 0) {
      dojo["require"]("ibmweb.ribbon");
      dojo.addOnLoad(function() {
        dojo.forEach(_617, function(item) {
          dojo.query(item).parent().removeClass("ibm-ribbon-hidden");
          if (dojo.hasAttr(item, "id") && item.id == "ibm-leadspace-body") {
            ibmweb.ribbonLeadspace.init();
          } else {
            var s = new ibmweb.ribbon({
              id: dojo.hasAttr(item, "id") ? item.id : null,
              autoscroll: dojo.hasClass(item, "ibm-autoscroll") ? true : false,
              srcNodeRef: item
            });
            s.startup();
          }
        });
      });
    }
  });
  dojo.addOnLoad(function() {
    var _618 = dojo.query(".ibm-scroll");
    if (_618.length > 0) {
      dojo["require"]("ibmweb.scroll");
      dojo.addOnLoad(function() {
        dojo.forEach(_618, function(item) {
          new ibmweb.scroll({
            element: item
          });
        });
      });
    }
  });
  dojo.addOnLoad(function() {
    var _619 = dojo.query("#ibm-primary-tabs"),
      _61a = dojo.query("#ibm-secondary-tabs");
    if (_619.length == 1 && _61a.length == 1) {
      dojo["require"]("ibmweb.subtabs");
      dojo.addOnLoad(function() {
        ibmweb.subtabs.init();
      });
    }
  });
  dojo.addOnLoad(function() {
    var tabs = dojo.query("div.ibm-dyntabs");
    if (tabs.length > 0) {
      dojo["require"]("ibmweb.dyntabs");
      dojo.addOnLoad(function() {
        ibmweb.dyntabs.init();
      });
    }
  });
  dojo.addOnLoad(function() {});
  dojo.addOnLoad(function() {
    dojo.query("p.ibm-back-to-top a").onclick(function(e) {
      if (dojo.attr(this, "href") != "#ibm-content") {
        return true;
      }
      dojo.stopEvent(e);
      window.scrollTo(0, 1);
    });
    dojo.query(".ibm-tab-section.ibm-text a.ibm-anchor-down-em-link").onclick(function(e) {
      var hash = e.target.href.split("#");
      if (hash.length != 2) {
        return;
      }
      hash = hash[1];
      var elem = dojo.byId(hash);
      if (!elem) {
        return;
      }
      window.scrollTo(0, dojo.marginBox(elem).t - dojo.position(dojo.byId("ibm-masthead")).h);
      dojo.stopEvent(e);
    });
    if (dojo.version.major <= 1 && dojo.version.minor > 3) {
      var _61b = dojo.query("a[name]");
      _61b.forEach(function(a) {
        if (a.className === "" && a.href === "") {
          var ap = dojo.create("div", {
            className: "ibm-anchor-pointer"
          }, a, "before");
          if (dojo.isIE < 8) {
            dojo.create("a", {
              id: a.name
            }, ap);
          } else {
            dojo.create("a", {
              name: a.name
            }, ap);
          }
          dojo.query(a).orphan();
        }
      });
      if (window.location.hash !== "") {
        window.location.hash += "";
      }
    }
  });
  dojo.addOnLoad(function() {
    dojo.query("ul.ibm-portrait-module-list li h3 a, ul.ibm-portrait-module-list li h2 a").forEach(function(item) {
      var _61c = dojo.query(dojo.query(item).parents(".ibm-portrait-module-list")).at(0);
      if (!dojo.hasClass(_61c[0], "ibm-no-link")) {
        var _61d = dojo.query(item).parents("li").at(0).children("img").at(0);
        if (dojo.hasClass(_61c[0], "ibm-new-window")) {
          _61d.onclick(function(e) {
            dojo.stopEvent(e);
            window.open(item.href);
          });
          _61d.style("cursor", "pointer");
        } else {
          if (_61d.parent("a")[0]) {
            return;
          }
          var _61e = dojo.query(dojo.clone(item)).attr({
              tabindex: "-1",
              role: "presentation"
            }),
            _61f = dojo.hasClass(_61e[0], "ibm-media"),
            _620 = dojo.hasClass(_61e[0], "ibm-leaving-prompt");
          _61e[0].innerHTML = "";
          _61e[0].className = "";
          if (_61f) {
            _61e.addClass("ibm-media");
          }
          if (_620) {
            _61e.addClass("ibm-leaving-prompt");
          }
          _61d.wrap(_61e[0]);
        }
      }
    });
  });
  dojo.addOnLoad(function() {
    var tmp = dojo.query(".ibm-twitter-module");
    if (tmp.length > 0) {
      dojo["require"]("ibmweb.twitter");
      dojo.addOnLoad(function() {
        dojo.forEach(tmp, function(item) {
          var w = new ibmweb.twitter.Widget({
            targetNode: item
          });
          w.placeAt(item);
          w.startup();
        });
      });
    }
  });
  dojo.addOnLoad(function() {
    var tmp = dojo.query(".ibm-rss-module");
    if (tmp.length > 0) {
      dojo["require"]("ibmweb.rssmodule");
      dojo.addOnLoad(function() {
        dojo.forEach(tmp, function(item) {
          var w = new ibmweb.rssmodule.Widget({
            targetNode: item
          });
          w.placeAt(item);
          w.startup();
        });
      });
    }
  });
  dojo.addOnLoad(function() {
    var _621 = dojo.query(".ibm-img-contactbox");
    if (_621.length > 0) {
      dojo["require"]("ibmweb.www.contactmodule");
      dojo.addOnLoad(function() {
        _621.forEach(function(box) {
          ibmweb.www.contactmodule.init(box);
        });
      });
    }
  });
  dojo.addOnLoad(function() {
    if (dojo.query("#ibm-top").attr("class") == "ibm-popup") {
      dojo.query("html,body").style("background", "none");
    }
  });
  dojo.addOnLoad(function() {
    if (ibmweb.config.backtocountry.enabled) {
      dojo["require"]("ibmweb.backtocountry");
      dojo.addOnLoad(function() {
        ibmweb.backtocountry.init();
      });
    }
  });
  dojo.addOnLoad(function() {
    ibmweb.stepindicator.init();
  });
  dojo.addOnLoad(function() {
    var _622 = dojo.query("[class*='ibm-play-button-']");
    if (_622.length > 0) {
      dojo.forEach(_622, function(item) {
        var elm = dojo.query(item),
          con = elm.closest(".ibm-video-container"),
          col = elm.closest("[class*='ibm-col-']"),
          _623 = elm.closest(".ibm-ribbon"),
          rQ = dojo.query(_623),
          _624 = (_623.length == 1),
          _625 = elm.closest(".ibm-video"),
          fB = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\"/i/v17/video-play-button-medium.png\",sizingMethod=\"crop\")",
          fBh = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\"/i/v17/video-play-button-medium-hover.png\",sizingMethod=\"crop\")",
          fBd = 90,
          _626 = true;
        if (col.length == 0) {
          return false;
        }
        if (dojo.hasClass(col[0], "ibm-col-6-1") || dojo.hasClass(col[0], "ibm-col-5-1")) {
          fB = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\"/i/v17/video-play-button-small.png\",sizingMethod=\"crop\")";
          fBh = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src=\"/i/v17/video-play-button-small-hover.png\",sizingMethod=\"crop\")";
          fBd = 50;
        }
        if (_624) {
          dojo.style(con[0], {
            marginTop: "0px"
          });
          dojo.style(col[0], {
            overflow: "hidden"
          });
          if (!dojo.hasClass(rQ[0], "ibm-alternate")) {
            dojo.addOnLoad(function() {
              var cols = dojo.query(".ibm-columns", rQ[0]);
              cols.forEach(function(c) {
                dojo.style(c, "margin", "0px");
              });
            });
          }
        }
        var i, i_n = elm.next("img"),
          i_p = elm.prev("img");
        if (i_n.length == 1) {
          i = i_n;
        } else {
          if (i_p.length == 1) {
            i = i_p;
          } else {
            _626 = false;
          }
        }
        if (dojo.isIE == 6) {
          if (_626) {
            dojo.removeAttr(i[0], "alt");
            dojo.style(item, {
              height: i[0].height + "px",
              top: (i[0].height - fBd) / 2 + "px",
              left: (i[0].width - fBd) / 2 + "px",
              backgroundImage: "none",
              cursor: "pointer",
              filter: fB
            });
          } else {
            if (con.length == 1) {
              dojo.style(item, {
                height: con[0].height + "px",
                top: (con[0].height - fBd) / 2 + "px",
                left: (con[0].width - fBd) / 2 + "px",
                backgroundImage: "none",
                cursor: "pointer"
              });
            }
          }
          if (_625.length == 1) {
            dojo.connect(_625[0], "onmouseover", function() {
              dojo.style(item, {
                filter: fBh
              });
            });
            dojo.connect(_625[0], "onmouseout", function() {
              dojo.style(item, {
                filter: fB
              });
            });
          }
        } else {
          dojo.addOnLoad(function() {
            var h = (_626) ? i[0].height : 0;
            if (h > 0) {
              con.style("height", h + "px");
            }
          });
        }
      });
    }
  });
  dojo.addOnLoad(function() {
    var _627 = dojo.query("ul.ibm-tabs")[0];
    var _628 = dojo.query("ul.ibm-tabs li a");
    if (_627) {
      dojo.connect(_627, "onfocus", this, function(e) {});
      dojo.connect(_627, "onkeypress", this, function(e) {
        if (e.keyCode == dojo.keys.RIGHT_ARROW) {
          dojo.stopEvent(e);
          var curr = e.target;
          for (var i = 0; i < _628.length; i++) {
            if ((curr == _628[i]) && (i < (_628.length - 1))) {
              _628[i + 1].focus();
              break;
            } else {
              if ((curr == _628[i]) && (i == (_628.length - 1))) {
                _628[0].focus();
                break;
              }
            }
          }
        }
        if (e.keyCode == dojo.keys.LEFT_ARROW) {
          dojo.stopEvent(e);
          var curr = e.target;
          for (var i = 0; i < _628.length; i++) {
            if ((curr == _628[i]) && (i > 0)) {
              _628[i - 1].focus();
              break;
            } else {
              if ((curr == _628[i]) && (i == 0)) {
                _628[_628.length - 1].focus();
                break;
              }
            }
          }
        }
        if (e.keyCode == 32) {
          dojo.stopEvent(e);
          window.location.href = dojo.attr(e.target, "href");
        }
      });
    }
  });
}
if (!dojo._hasResource["ibmweb.w3.notification"]) {
  dojo._hasResource["ibmweb.w3.notification"] = true;
  dojo.provide("ibmweb.w3.notification");
  ibmweb.w3.notification = {};
  ibmweb.w3.notification.start = false;
  ibmweb.w3.notification.init = function() {
    ibmweb.w3.notification.start = true;
    var _629 = countHighPriority = countNormalPriority = 0;
    var data = {
      status0: [],
      status1: []
    };
    var _62a = [];
    var _62b;
    var _62c;
    var _62d = true;
    var _62e = false;
    var _62f = false;
    var hkey;
    var _630;
    var _631 = 1920000;
    var _632 = null;
    var _633 = 30;
    var _634 = typeof dojo.isIE != "undefined" ? 3600 : 20000;
    var _635 = false;
    var _636 = {};

    function _637() {
      if (ibmweb.config.usedSettings === "development") {
        _636 = {
          loadAlerts: "//jade.webmaster.ibm.com/gateway/ea/getalerts/?cb=165:ibmweb.w3.notification.callback&lc=en&cc=us&format=json",
          loadAlertsCookie: "//jade.webmaster.ibm.com/gateway/ea/getalerts/?cb=165:ibmweb.w3.notification.nextCall&lc=en&cc=us&format=json",
          updateAlerts: "//jade.webmaster.ibm.com/gateway/ea/updatestatus/?cb=165:ibmweb.w3.notification.uploadData&lc=en&cc=us&alert",
          ibmalertsconfigcb: "//www.ibm.com/common/alerts/alerts_config.json"
        };
      } else {
        if (ibmweb.config.usedSettings === "wwwpreview") {
          _636 = {
            loadAlerts: "//stage.the.ibm.com/ibmweb/swwif/ea/getalerts/?cb=165:ibmweb.w3.notification.callback&lc=en&cc=us&format=json",
            loadAlertsCookie: "//stage.the.ibm.com/ibmweb/swwif/ea/getalerts/?cb=165:ibmweb.w3.notification.nextCall&lc=en&cc=us&format=json",
            updateAlerts: "//stage.the.ibm.com/ibmweb/swwif/ea/updatestatus/?cb=165:ibmweb.w3.notification.uploadData&lc=en&cc=us&alert",
            ibmalertsconfigcb: "//www.ibm.com/common/alerts/alerts_config.json"
          };
        } else {
          _636 = {
            loadAlerts: "//w3.the.ibm.com/ibmweb/swwif/ea/getalerts/?cb=165:ibmweb.w3.notification.callback&lc=en&cc=us&format=json",
            loadAlertsCookie: "//w3.the.ibm.com/ibmweb/swwif/ea/getalerts/?cb=165:ibmweb.w3.notification.nextCall&lc=en&cc=us&format=json",
            updateAlerts: "//w3.the.ibm.com/ibmweb/swwif/ea/updatestatus/?cb=165:ibmweb.w3.notification.uploadData&lc=en&cc=us&alert",
            ibmalertsconfigcb: "//www.ibm.com/common/alerts/alerts_config.json"
          };
        }
      }
    };

    function _638() {
      var _639 = dojo.cookie("w3ibmProfile");
      if (typeof _639 !== "undefined") {
        w3profile = _639.split("|");
        hkey = w3profile[0];
        _630 = w3profile[2];
        var _63a = ibmweb.storage.getItem("w3UserName_" + hkey);
        _62b = ibmweb.storage.getItem("w3UserEmail_" + hkey);
        var _63b = dojo.fromJson(ibmweb.storage.getItem("w3User"));
        if (_63b) {
          _63a = _63b.commonName;
          _62b = _63b.email;
        }
        if (_63a && _62b) {
          _637();
          alertConfigCookie = dojo.fromJson(window.localStorage.alert_config);
          if (typeof alertConfigCookie !== "undefined") {
            timeStart = alertConfigCookie.timeStamp;
            timeEnd = new Date().getTime();
            var _63c = timeEnd - timeStart;
            var _63d = Math.round(_63c / 60 / 60 / 1000);
            if (_63d < 24) {
              _63e(alertConfigCookie);
            } else {
              _63f();
            }
          } else {
            _63f();
          }
          _632 = setTimeout(_638, _631);
        } else {
          setTimeout(_638, 100);
        }
      }
    };

    function _63f() {
      dojo.io.script.get({
        url: _636.ibmalertsconfigcb,
        load: function(_640) {
          return _640;
        }
      });
    };

    function _63e(_641) {
      var _642 = [];
      var _643 = [];
      _642 = _641["supported-intranetids"];
      var flag = false;
      var _644 = location.href,
        _645 = /w3b.ibm.com\//.test(_644);
      if (_645) {
        _641.allusers = "0";
      }
      if (_641.global == "1") {
        flag = true;
      } else {
        if (_641.allusers == "1" && dojo.indexOf(_641["supported-countries"], _630) != -1) {
          flag = true;
        } else {
          if (dojo.indexOf(_641["supported-countries"], _630) != -1 && dojo.indexOf(_641["supported-letters"], _62b.charAt(0)) != -1) {
            flag = true;
          }
        }
      }
      _646(flag, _642);
    };

    function _646(flag, _647) {
      if (flag == true || dojo.indexOf(_647, _62b) != -1) {
        if (_62d) {
          _648();
          _62d = false;
        }
        _649(_62b);
      }
    };

    function _648() {
      var _64a = dojo.create("div", {
        className: "ibm-background-dim"
      });
      var _64b = dojo.create("div", {
        className: "ibm-w3-notifications ibm-w3-notifications--full"
      });
      dojo.create("div", {
        className: "ibm-w3-notifications--close",
        innerHTML: "close"
      }, _64b);
      var _64c = dojo.create("div", {
        className: "ibm-w3-notifications--inner"
      }, _64b);
      var _64d = dojo.create("div", {
        className: "ibm-w3-notifications--head"
      }, _64c);
      var _64e = dojo.create("h3", {
        className: "ibm-w3-notifications--heading",
        innerHTML: "Notifications"
      }, _64d);
      dojo.create("span", {
        id: "msgInformation"
      }, _64d);
      var _64f = dojo.create("div", {
        className: "ibm-w3-notifications--views"
      }, _64d);
      dojo.create("p", {
        innerHTML: "view"
      }, _64f);
      dojo.create("a", {
        innerHTML: "Unread",
        href: "#",
        className: "ibm-w3-notifications--view-link ibm-active",
        id: "unreadAlerts"
      }, _64f);
      dojo.create("a", {
        innerHTML: "All",
        href: "#",
        className: "ibm-w3-notifications--view-link",
        id: "allAlerts"
      }, _64f);
      var _650 = dojo.create("div", {
        className: "ibm-w3-notifications--body ibm-scroll notify"
      }, _64c);
      var _651 = dojo.create("ul", {
        id: "ibm-w3-notifications-result-full",
        className: "ibm-w3-notifications--notificaction-list ibm-alternate"
      }, _650);
      var _650 = dojo.create("div", {
        className: "ibm-w3-notifications--body ibm-scroll notify"
      }, _64c);
      var _651 = dojo.create("ul", {
        id: "ibm-w3-notifications-All-normal-priority",
        className: "ibm-w3-notifications--notificaction-list ibm-alternate"
      }, _650);
      var _652 = dojo.query("#ibm-related-content");
      if (_652.length != 0) {
        if (dojo.query("#ibm-related-content").style("display") != "none") {
          dijit.setWaiRole("ibm-related-content", "complementary");
        }
        _652.append(_64a).append(_64b);
      } else {
        dojo.query("body").append(_64a).append(_64b);
      }
      dojo.query(".notify").style({
        "display": "none",
        "height": "190px"
      });
      dojo.query(".notify:nth-child(2)").style("display", "block");
      dojo.query(".ibm-w3-notifications.ibm-w3-notifications--full").style("top", "37px");
      _653();
    };

    function _653() {
      var _654 = {
        id: "closeNotify",
        onClick: function(evt) {
          dojo.query(".ibm-background-dim,.ibm-w3-notifications").style("display", "none");
        }
      };
      dojo.query(".ibm-w3-notifications--close").connect("onclick", _654.onClick);
      var _655 = {
        id: "viewLink",
        onClick: function(evt) {
          dojo.stopEvent(evt);
          dojo.query(".ibm-w3-notifications--view-link").removeClass("ibm-active").query(evt.currentTarget).addClass("ibm-active");
          text = dojo.query(evt.currentTarget).attr("id")[0];
          dojo.query(".notify").style("display", "none");
          switch (text) {
            case "unreadAlerts":
              dojo.query(".notify:nth-child(2)").style("display", "block");
              Unread = dojo.query("#ibm-w3-notifications-result-full");
              children = Unread.children();
              if (children.length == 0) {
                dojo.create("li", {
                  innerHTML: "No Active Messages",
                  style: {
                    color: "red",
                    textAlign: "center",
                    fontWeight: "bold"
                  }
                }, Unread[0]);
              }
              break;
            case "allAlerts":
              dojo.query(".notify:nth-child(3)").style("display", "block");
              _6ab();
              break;
          }
        }
      };
      dojo.query(".ibm-w3-notifications--view-link").connect("onclick", _655.onClick);
      dojo.connect(dojo.doc, "onkeypress", function(e) {
        var key = e.keyCode || e.charCode;
        var k = dojo.keys;
        if (key == k.ESCAPE && dojo.query(".ibm-background-dim").style("display")[0] == "block") {
          dojo.query(".ibm-background-dim,.ibm-w3-notifications").style("display", "none");
        }
      });
    };

    function _656() {
      return dojo.create("span", {
        className: "ibm-access",
        innerHTML: "notifications"
      });
    };

    function _649() {
      if (_635 == true) {
        _635 = false;
        return false;
      }
      var _657 = dojo.cookie("unreadMessage");
      if (typeof _657 === "undefined") {
        _62f = true;
        dojo.io.script.get({
          url: _636.loadAlerts + "&time=" + new Date().getTime().toString(),
          timeout: _634
        }).then(function(_658) {
          return _658;
        }, function(_659) {
          _6b0();
        });
      } else {
        _69a();
        txtLength = _657.split("|")[0];
        if (txtLength > 0) {
          dojo.query("#ibm-notification").text(txtLength);
        } else {
          dojo.query("#ibm-w3-notifications-icon").empty().append(_656());
        }
        var _65a = {
          id: "notificationDetail",
          onClick: function(evt) {
            dojo.stopEvent(evt);
            _65b();
          }
        };
        dojo.query("#ibm-w3-notifications-icon").connect("onclick", _65a.onClick);
      }
    };

    function _65c(_65d, _65e, _65f) {
      var _660 = {},
        _661 = "",
        _662 = "",
        _663 = ["img"],
        _664 = [],
        _665 = 0,
        _666 = _661,
        _667 = "(\\w+\\s*=\\s*\"[^\"]*\"\\s*)*",
        _668 = "\\s*\\/?\\s*",
        _669 = "\\s*\\/\\s*",
        _66a = new RegExp("<\\/?\\w+\\s*" + _667 + _669 + ">"),
        _66b = new RegExp("<\\/?\\w+\\s*" + _667 + _668 + ">"),
        _66c = new RegExp("<img\\s*" + _667 + _668 + ">"),
        _66d = true,
        _66e, _66f, tail, tag, _670;

      function _671(_672) {
        var _673 = _66c.exec(_672),
          _66f, len;
        if (!_673) {
          return _672;
        }
        _66f = _673.index;
        len = _673[0].length;
        return _672.substring(0, _66f) + _672.substring(_66f + len);
      };

      function _674(tags) {
        var html = "";
        dojo.forEach(tags.reverse(), function(tag, _675) {
          if (-1 === dojo.indexOf(_663, tag)) {
            html += "</" + tag + ">";
          }
        });
        return html;
      };

      function _676(_677) {
        var tail = _677.indexOf(" ");
        if (-1 === tail) {
          tail = _677.indexOf(">");
          if (-1 === tail) {
            throw new Error("HTML tag is not well-formed : " + _677);
          }
        }
        return _677.substring(1, tail);
      };
      _65f = _65f || _660;
      _65f.ellipsis = _65f.ellipsis || _662;
      _65f.truncateLastWord = (_65f.truncateLastWord === undefined) ? true : _65f.truncateLastWord;
      while (_66d) {
        _66d = _66b.exec(_65d);
        if (!_66d) {
          if (_665 < _65e) {
            _666 += _65d.substring(0, _65e - _665);
          }
          break;
        }
        _66e = _66d[0];
        _66f = _66d.index;
        if (_665 + _66f > _65e) {
          _666 += (_65d.substring(0, _65e - _665));
          break;
        } else {
          _665 += _66f;
          _666 += _65d.substring(0, _66f);
        }
        if ("/" === _66e[1]) {
          _664.pop();
        } else {
          _670 = _66a.exec(_66e);
          if (!_670) {
            tag = _676(_66e);
            _664.push(tag);
          }
        }
        if (_670) {
          _666 += _670[0];
        } else {
          _666 += _66e;
        }
        _65d = _65d.substring(_66f + _66e.length);
      }
      if (_65d.length > _65e && _65f.ellipsis) {
        if (_65f.truncateLastWord) {
          _666 += _65f.ellipsis;
        } else {
          _666 = _666.replace(/ \w*$/, _65f.ellipsis);
        }
      }
      _666 += _674(_664);
      if (!_65f.keepImageTag) {
        _666 = _671(_666);
      }
      return _666;
    };
    var _678 = function(text, _679, _67a, _67b) {
      if (!_679 || text.length < _679) {
        return text;
      }
      var tags = [],
        _67c = 0,
        _67d = 0;
      for (var i = 0; i < text.length; i++) {
        var _67e = text.charAt(i);
        if (_67e === "<") {
          var tail = text.indexOf(">", i);
          if (tail < 0) {
            return text;
          }
          var _67f = text.substring(i + 1, tail);
          var tag = {
            "name": "",
            "closing": false
          };
          if (_67f.charAt(0) === "/") {
            tag.closing = true;
            _67f = _67f.substring(1);
          }
          tag.name = _67f.match(/(\w)+/)[0];
          if (tag.closing) {
            var _680 = tags.pop();
            if (!_680 || _680.name !== tag.name) {
              return text;
            }
          }
          i = tail;
        } else {
          if (_67e === "&" && text.substring(i).match(/^(\S)+;/)) {
            i = text.indexOf(";", i);
          } else {
            if (_67c === _679) {
              _67d = i;
              break;
            }
            _67c++;
          }
        }
      }
      if (_67d || _67b) {
        if (_67d) {
          text = text.substring(0, _67d) + (_67a || "");
        }
        for (var i = tags.length - 1; i >= 0; i--) {
          text += "</" + tags[i].name + ">";
        }
      }
      return text;
    };
    var _681 = function(text, _682, _683, _684) {
      if (!_682 || text.length < _682) {
        return text;
      }
      var _685 = {
        "tags": [],
        "count": 0,
        "position": 0,
        "lastWordChar": 0
      };
      var _686;
      var _687 = function(_688) {
        return {
          "position": _688.position,
          "count": _688.count,
          "lastWordChar": _688.lastWordChar,
          "tags": _688.tags.slice()
        };
      };
      var _689 = function(_68a, _68b) {
        if ((_68b.length !== 1 && _68b !== "&nbsp") || /^\w$/.test(_68b)) {
          _68a.lastWordChar = _68a.position;
          if (_68a.position === text.length - 1) {
            _68a.position++;
            _686 = _687(_68a);
            return true;
          }
        } else {
          if (_68a.lastWordChar == _68a.position - 1) {
            _686 = _687(_68a);
            return true;
          }
        }
        return false;
      };
      for (var i = 0; i < text.length; i++) {
        var _68c = text.charAt(i);
        _685.position = i;
        if (_68c === "<") {
          var tail = text.indexOf(">", i);
          if (tail < 0) {
            return text;
          }
          var _68d = text.substring(i + 1, tail);
          var tag = {
            "name": "",
            "closing": false
          };
          if (_68d.charAt(0) === "/") {
            tag.closing = true;
            _68d = _68d.substring(1);
          }
          tag.name = _68d.match(/(\w)+/)[0];
          if (tag.closing) {
            var _68e = _685.tags.pop();
            if (!_68e || _68e.name !== tag.name) {
              return text;
            }
          }
          i = tail;
        } else {
          if (_68c === "&" && text.substring(i).test(/^(\S)+;/)) {
            var _68f = text.indexOf(";", i);
            i = _68f;
            _685.count++;
          } else {
            if (_685.count === _682) {
              break;
            }
            _685.count++;
          }
        }
      }
      if ((_685.count === _682 && _685.position < text.length) || _684) {
        _685 = _686 || _685;
        if (_685.position) {
          text = text.substring(0, _685.position) + (_683 || "");
        }
        for (var i = _685.tags.length - 1; i >= 0; i--) {
          text += "</" + _685.tags[i].name + ">";
        }
      }
      return text;
    };

    function _690(_691, _692, _693, _694) {
      text = _65c(_691, _692);
      var _695 = /(<([^>]+)>)/ig;
      temp1 = _691.replace(_695, "");
      temp2 = text.replace(_695, "");
      _693 = _693 || "&hellip;";
      more = " <a  id=" + _694 + " class=\"ibm-w3-notifications--moreless\" href=\"#\">[more]</a>";
      preFix = "<span>";
      postFix = "</span>";
      return temp1.length > temp2.length ? preFix + text + _693 + postFix + more : preFix + _691.substr(0, 1024) + postFix;
    };

    function _696() {
      dojo.destroy("main_notification");
      var _697 = dojo.create("div", {
        ariaLabel: "Notifications",
        role: "toolbar",
        id: "main_notification"
      });
      var id = "";
      var _698 = location.href,
        _699 = /w3b.ibm.com\//.test(_698);
      if (_699) {
        id = "ibm-w3b-notifications-broken-icon";
      } else {
        id = "ibm-w3-notifications-broken-icon";
      }
      div_notification = dojo.create("a", {
        id: id,
        title: "Notifications",
        href: "#",
        role: "button"
      }, _697);
      dojo.create("span", {
        className: "ibm-access",
        innerHTML: "notifications"
      }, div_notification);
      dojo.query("#ibm-search-module").before(_697);
    };

    function _69a() {
      dojo.destroy("main_notification");
      var _69b = dojo.create("div", {
        ariaLabel: "Notifications",
        role: "toolbar",
        id: "main_notification"
      });
      div_notification = dojo.create("a", {
        id: "ibm-w3-notifications-icon",
        title: "Notifications",
        href: "#",
        role: "button"
      }, _69b);
      dojo.create("span", {
        id: "ibm-notification"
      }, div_notification);
      dojo.create("span", {
        className: "ibm-access",
        innerHTML: "notifications"
      }, div_notification);
      dojo.query("#ibm-search-module").before(_69b);
      _69c();
    };

    function _69c() {
      var _69d = function(e) {
        dojo.stopEvent(e);
        var text = dojo.query(e.currentTarget).attr("id")[0];
        dojo.query(".notify").style("display", "none");
        dojo.query(".ibm-w3-notifications--view-link").removeClass("ibm-active").query(e.currentTarget).addClass("ibm-active");
        switch (text) {
          case "unreadAlerts":
            dojo.query(".notify:nth-child(2)").style("display", "block");
            Unread = dojo.query("#ibm-w3-notifications-result-full");
            children = Unread.children();
            if (children.length == 0) {
              dojo.create("li", {
                innerHTML: "No Active Messages",
                style: {
                  color: "red",
                  textAlign: "center",
                  fontWeight: "bold"
                }
              }, Unread[0]);
            }
            break;
          case "allAlerts":
            dojo.query(".notify:nth-child(3)").style("display", "block");
            _6ab();
            break;
        }
      };
      var _69e = dojo.query(".ibm-w3-notifications--views a");
      _69e.connect("onfocus", _69d);
      _69e.connect("onkeypress", function(e) {
        if (e.keyCode == dojo.keys.ENTER || e.keyCode == 0 || e.keyCode == " " || e.keyCode == dojo.keys.SPACE) {
          _69d(e);
        }
        if (e.keyCode == dojo.keys.RIGHT_ARROW || e.keyCode == dojo.keys.DOWN_ARROW) {
          dojo.stopEvent(e);
          var curr = e.target;
          for (var i = 0; i < _69e.length; i++) {
            if ((curr == _69e[i]) && (i < (_69e.length - 1))) {
              _69e[i + 1].focus();
              break;
            } else {
              if ((curr == _69e[i]) && (i == (_69e.length - 1))) {
                _69e[0].focus();
                break;
              }
            }
          }
        }
        if (e.keyCode == dojo.keys.LEFT_ARROW || e.keyCode == dojo.keys.UP_ARROW) {
          dojo.stopEvent(e);
          var curr = e.target;
          for (var i = 0; i < _69e.length; i++) {
            if ((curr == _69e[i]) && (i > 0)) {
              _69e[i - 1].focus();
              break;
            } else {
              if ((curr == _69e[i]) && (i == 0)) {
                _69e[_69e.length - 1].focus();
                break;
              }
            }
          }
        }
      });
    };

    function _69f(obj) {
      _62c = obj.results.alerts;
      list = dojo.byId("ibm-w3-notifications-result-full");
      normalList = dojo.byId("ibm-w3-notifications-All-normal-priority");
      dojo.empty("ibm-w3-notifications-result-full");
      var j = 0;
      if (_62c[0] == "novalue-No Alerts found for the current user Country" || _62c[0] == "novalue-Inranet id(mail id) not found") {
        dojo.create("li", {
          innerHTML: "No Active Messages",
          style: {
            color: "red",
            textAlign: "center",
            fontWeight: "bold"
          }
        }, normalList);
      } else {
        dojo.forEach(_62c, function(_6a0, i) {
          if (_6a0.status == 0) {
            if (_6a0.alertMsg != null) {
              _62a[j++] = _6a0.id;
              var msg = _690(_6a0.alertMsg, 145, "&hellip;", "unRead_" + i);
            } else {
              msg = "";
            }
            if (_6a0.detailLink) {
              dojo.create("li", {
                innerHTML: msg + " <a href=" + _6a0.detailLink + " target='_blank'>details</a>",
                className: "ibm-information-link"
              }, list);
            } else {
              dojo.create("li", {
                innerHTML: msg,
                className: "ibm-information-link"
              }, list);
            }
          }
        });
      }
      dojo.query("#ibm-w3-notifications-result-full").query("a:not(.ibm-w3-notifications--moreless)").attr("target", "_blank");
      msgCount = 0;
      if (typeof dojo.cookie("unreadMessage") != "undefined") {
        msgCount = parseInt(dojo.cookie("unreadMessage").split("|")[0]);
      }
      if (msgCount != 0 && msgCount != j) {
        strMsg = "";
        if (msgCount > j) {
          cnt = msgCount - j;
          strMsg = cnt + " alerts have been removed as they just expired";
        } else {
          cnt = j - msgCount;
          strMsg = cnt + " new alerts have been added";
        }
        dojo.animateProperty({
          node: dojo.byId("msgInformation"),
          delay: 3000,
          onEnd: function() {
            dojo.query("#msgInformation").html(strMsg);
          }
        }).play();
      }
      var _6a1 = j;
      if (_62f) {
        var date = new Date();
        var time = date.getTime();
        date.setTime(time + (_633 * 60 * 1000));
        dojo.cookie("unreadMessage", j + "|" + time, {
          expires: date,
          domain: ".ibm.com",
          path: "/"
        });
      }
      if (_6a1 == 0) {
        dojo.query("#ibm-w3-notifications-icon").empty().append(_656());
      } else {
        dojo.query("#ibm-notification").text(_6a1);
      }
      var _6a2 = {
        id: "notificationDetail",
        onClick: function(evt) {
          dojo.stopEvent(evt);
          _65b();
        }
      };
      dojo.query("#ibm-w3-notifications-icon").connect("onclick", _6a2.onClick);
      _6a3("#ibm-w3-notifications-result-full");
    };

    function _6a3(id) {
      var _6a4 = {
        id: "showMoreLink",
        onClick: function(evt) {
          dojo.stopEvent(evt);
          linkId = parseInt(dojo.query(evt.currentTarget).attr("id")[0].split("_")[1]);
          var _6a5 = dojo.query(evt.currentTarget).parents("li").children("span");
          var _6a6 = "&hellip;";
          if (_6a5.next(".ibm-w3-notifications--moreless").text() === "[more]") {
            str = _62c[linkId].alertMsg;
            _6a5.html(str).next(".ibm-w3-notifications--moreless").text("[less]");
          } else {
            str = _65c(_62c[linkId].alertMsg, 145) + _6a6;
            _6a5.html(str).next(".ibm-w3-notifications--moreless").text("[more]");
          }
          dojo.query(id).query("a:not(.ibm-w3-notifications--moreless)").attr("target", "_blank");
        }
      };
      dojo.query(id + " .ibm-w3-notifications--moreless").connect("onclick", _6a4.onClick);
    };

    function _65b() {
      var _6a7 = dojo.coords(dojo.byId("ibm-w3-notifications-icon"));
      var _6a8 = (dojo.query("html").position()[0].w - (_6a7.x + _6a7.w)) - 45;
      dojo.query(".ibm-w3-notifications").style("right", _6a8 + "px");
      dojo.query("#ibm-w3-notifications-result-full .ibm-spinner-large").remove();
      countMsg = dojo.cookie("unreadMessage");
      if (typeof _62c === "undefined" || typeof countMsg === "undefined") {
        dojo.query(".ibm-background-dim,.ibm-w3-notifications").style("display", "block");
        var _6a9 = dojo.create("a", {
          className: "ibm-spinner-large",
          href: "#",
          innerHTML: "&nbsp;"
        });
        dojo.query("#ibm-w3-notifications-result-full").append(_6a9);
        _6aa();
      } else {
        countMsg = countMsg.split("|");
        var date = new Date();
        var time = parseFloat(countMsg[1]);
        date.setTime(time + (_633 * 60 * 1000));
        dojo.cookie("unreadMessage", "0|" + countMsg[1], {
          expires: date,
          domain: ".ibm.com",
          path: "/"
        });
        if (dojo.query("#ibm-w3-notifications-result-full").children().length == 0 || _62e == true) {
          dojo.query(".ibm-background-dim,.ibm-w3-notifications").style("display", "block");
          dijit.focus(dojo.byId("allAlerts"));
          dojo.query("#ibm-w3-notifications-result-full").empty();
          dojo.query(".notify").style("display", "none");
          dojo.query(".notify:nth-child(3)").style("display", "block");
          dojo.query(".ibm-w3-notifications--view-link").removeClass("ibm-active");
          dojo.query("#allAlerts").addClass("ibm-active");
          _6ab();
        } else {
          dojo.query(".ibm-background-dim,.ibm-w3-notifications").style("display", "block");
          dojo.query("#ibm-w3-notifications-icon").empty().append(_656());
          dijit.focus(dojo.byId("unreadAlerts"));
          _62e = true;
        }
        _6ac();
      }
    };

    function _6ac() {
      str = _62a.slice(",");
      if (str.length > 0) {
        _62a = [];
        getUrl = _636.updateAlerts + "&alertIds=" + str + "&format=json";
        dojo.io.script.get({
          url: getUrl + "&time=" + new Date().getTime().toString(),
          timeout: _634
        }).then(function(_6ad) {
          return _6ad;
        });
      }
    };

    function _6aa() {
      dojo.io.script.get({
        url: _636.loadAlertsCookie + "&time=" + new Date().getTime().toString(),
        timeout: _634
      }).then(function(_6ae) {
        return _6ae;
      }, function(_6af) {
        _6b0();
      });
    };

    function _6b1(obj) {
      var _6b2 = false;
      dojo.forEach(obj.results.alerts, function(_6b3, i) {
        if (_6b3.status == 0) {
          _6b2 = true;
        }
      });
      if (_6b2) {
        _69f(obj);
        dojo.query("#ibm-w3-notifications-icon").empty().append(_656());
        dijit.focus(dojo.byId("unreadAlerts"));
        _62e = true;
      } else {
        _62c = obj.results.alerts;
        _6ab();
        dojo.query("#ibm-w3-notifications-icon").empty().append(_656());
        dojo.query(".ibm-background-dim,.ibm-w3-notifications").style("display", "block");
        dijit.focus(dojo.byId("allAlerts"));
        dojo.query("#ibm-w3-notifications-result-full").empty();
        dojo.query(".notify").style("display", "none");
        dojo.query(".notify:nth-child(3)").style("display", "block");
        dojo.query(".ibm-w3-notifications--view-link").removeClass("ibm-active");
        dojo.query("#allAlerts").addClass("ibm-active");
      }
    };

    function _6ab() {
      normalList = dojo.byId("ibm-w3-notifications-All-normal-priority");
      dojo.empty("ibm-w3-notifications-All-normal-priority");
      if (_62c[0] == "novalue-No Alerts found for the current user Country" || _62c[0] == "novalue-Inranet id(mail id) not found") {
        dojo.create("li", {
          innerHTML: "No Active Messages",
          style: {
            color: "red",
            textAlign: "center",
            fontWeight: "bold"
          }
        }, normalList);
      } else {
        dojo.forEach(_62c, function(_6b4, i) {
          if (_6b4.alertMsg != null) {
            var msg = _690(_6b4.alertMsg, 145, "&hellip;", "normalPriority_" + i);
          } else {
            msg = "";
          }
          if (_6b4.detailLink) {
            dojo.create("li", {
              innerHTML: msg + " <a href=" + _6b4.detailLink + " target='_blank'>details</a>",
              className: "ibm-information-link"
            }, normalList);
          } else {
            dojo.create("li", {
              innerHTML: msg,
              className: "ibm-information-link"
            }, normalList);
          }
        });
      }
      dojo.query("#ibm-w3-notifications-All-normal-priority").query("a:not(.ibm-w3-notifications--moreless)").attr("target", "_blank");
      _6a3("#ibm-w3-notifications-All-normal-priority");
    };

    function _6b0() {
      if (_631 == 1920000) {
        clearTimeout(_632);
        _631 = 600000;
        date = "Thu, 01 Jan 1970 00:00:01 GMT";
        dojo.cookie("unreadMessage", null, {
          expires: date,
          domain: ".ibm.com",
          path: "/"
        });
        _696();
        dojo.query("#ibm-w3-notifications-result-full .ibm-spinner-large").remove();
        list = dojo.byId("ibm-w3-notifications-result-full");
        dojo.empty("ibm-w3-notifications-result-full");
        dojo.create("li", {
          innerHTML: "Service unavailable. Please try again later",
          style: {
            color: "red",
            textAlign: "center",
            fontWeight: "bold"
          }
        }, list);
        _635 = true;
        _638();
      }
    };

    function _6b5(_6b6) {
      if (typeof _6b6.results.alerts == "undefined" || _6b6.length == 0) {
        _6b0();
      } else {
        _631 = 1920000;
        _69a();
        _69f(_6b6);
      }
    };
    ibmweb.w3.notification.callback = _6b5;

    function _6b7(_6b8) {};
    ibmweb.w3.notification.uploadData = _6b7;

    function _6b9(_6ba) {
      if (typeof _6ba.results.alerts == "undefined" || _6ba.length == 0) {
        _6b0();
      } else {
        _631 = 1920000;
        dojo.query("#ibm-w3-notifications-result-full .ibm-spinner-large").remove();
        _6b1(_6ba);
        var date = new Date();
        var time = date.getTime();
        date.setTime(time + (_633 * 60 * 1000));
        dojo.cookie("unreadMessage", 0 + "|" + time, {
          expires: date,
          domain: ".ibm.com",
          path: "/"
        });
        countMsg = dojo.cookie("unreadMessage").split("|");
      }
    };
    ibmweb.w3.notification.nextCall = _6b9;

    function _6bb(obj) {
      if (obj.lentgh != 0) {
        var _6bc = obj.alertsconfig;
        _6bc.timeStamp = new Date().getTime();
        window.localStorage.alert_config = dojo.toJson(_6bc);
        _63e(_6bc);
      }
    };
    ibmweb.w3.notification.ibmalertsconfigcb = _6bb;
    if (ibmweb.config.config == "w3") {
      if (!(ibmweb.config.get("enterprisealert") && ibmweb.config.get("enterprisealert").enabled === false)) {
        _638();
      }
    }
  };
  var w3Notification_url = location.href,
    w3Notification_isODWpreview = /w3.boulder.ibm.com\/workplace\//.test(w3Notification_url),
    w3Notification_isODWlive = /w3.ibm.com\/workplace\//.test(w3Notification_url),
    w3Notification_isw3Alpha2 = /w3alpha-2.toronto.ca.ibm.com\/profiles/.test(w3Notification_url),
    w3Notification_isConnection = /w3-connections.ibm.com/.test(w3Notification_url),
    w3Notification_isPP = /\/services\/practitionerportal/.test(w3Notification_url);
  dojo.addOnLoad(function() {
    if (!ibmweb.w3.notification.start && !w3Notification_isw3Alpha2 && !w3Notification_isConnection && !w3Notification_isPP) {
      ibmweb.w3.notification.init();
    }
  });
  setTimeout(function() {
    if (!ibmweb.w3.notification.start && !w3Notification_isw3Alpha2 && !w3Notification_isConnection && !w3Notification_isPP) {
      ibmweb.w3.notification.init();
    }
  }, 130000);
}
if (!dojo._hasResource["ibmweb.w3.bluecard"]) {
  dojo._hasResource["ibmweb.w3.bluecard"] = true;
  dojo.provide("ibmweb.w3.bluecard");
  var bluecardPanel = dojo.declare("ibmweb.w3.bluecard.panel", [dijit._Widget, dijit._Templated], {
    templateString: "<div class=\"ibm-bluecard-popup\" role=\"dialog\"><div class=\"ibm-bluecard-main\" role=\"document\" ><div class=\"ibm-vcard\" id=\"cardDiv\"><div dojoAttachPoint=\"loading\"></div><div dojoAttachPoint=\"infocard\"><div style=\"position: absolute;display: inline;line-height: 0;max-height: 0px;font-size: 0px;outline: 0px none;\" role=\"alert\"> Press escape to dismiss this business card. </div><div style=\"line-height:0;max-height:0;font-size:0;outline:0;\" tabindex=\"0\"></div><table cellspacing=\"0\" class=\"ibm-bluecard-Container\" id=\"cardTable\"><tbody><tr id=\"ibm-vcard-header\"><td><table width=\"100%\" cellspacing=\"0\" aria-label=\"application links\" role=\"list\"><tbody><tr class=\"ibm-vcard-headerlink1\"><td class=\"ibm-vcard-firsttext\" role=\"listitem\" style=\"width: 24%;\"><a dojoAttachPoint=\"profilelink\" class=\"url\"> Profile </a></td><td role=\"listitem\" style=\"width: 24%;\"><a dojoAttachPoint=\"communitylink\" class=\"url\"> Communities </a></td><td role=\"listitem\" style=\"width: 24%;\"><a dojoAttachPoint=\"bloglink\" class=\"url\"> Blogs </a></td><td role=\"listitem\" style=\"width: 24%;\"><a dojoAttachPoint=\"forumlink\" class=\"url\"> Forums </a></td></tr><tr class=\"ibm-vcard-headerlink1 ibm-vcard-headerlink2\"><td class=\"ibm-vcard-firsttext\" role=\"listitem\"><a dojoAttachPoint=\"usermailto\" class=\"email\"> Send Email </a></td><td role=\"listitem\"><a dojoAttachPoint=\"bookmarklink\" class=\"url\"> Bookmarks </a></td><td role=\"listitem\"><a dojoAttachPoint=\"activitylink\" class=\"url\"> Activities </a></td><td role=\"listitem\"></td></tr></tbody></table></td></tr><tr id=\"ibm-vcard-body\"><td colspan=\"6\" class=\"ibm-vcard-bodydetails\"><div class=\"ibm-vcard-righticon\"><a role=\"button\" href=\"javascript:;\" title=\"Show Less\" class=\"ibm-minimize-link\" dojoAttachEvent=\"onclick:_hideLinkPanels\"><span class=\"ibm-vcard-alttext\"> Show less </span></a></div><div class=\"ibm-vcard-photo photo\" dojoAttachPoint=\"userphoto\"></div><div class=\"ibm-vcard-perinfo\"><h2><span dojoAttachPoint=\"username\"></span></h2><p class=\"ibm-vcard-perinfo-bio\" dojoAttachPoint=\"userbio\"></p><div class=\"adr\"><span dojoAttachPoint=\"userlocation\"></span> ,&nbsp;<span dojoAttachPoint=\"userstate\"></span> &nbsp;<span dojoAttachPoint=\"usercountry\"></span><p><span dojoAttachPoint=\"userbuilding\"></span><span dojoAttachPoint=\"userfloor\"></span><span dojoAttachPoint=\"useroffice\"></span></p></div><p class=\"tel\" dojoAttachPoint=\"userofficephone\"></p><p><a dojoAttachPoint=\"usermailtoemail\"></a></p></div></td></tr></tbody></table><div tabindex=\"0\" style=\"line-height:0px;max-height: 0px;font-size:0px;outline:0;\"> &nbsp;</div></div></div></div></div>",
    links: {
      profile: "https://w3-connections.ibm.com/profiles/html/simpleSearch.do?searchFor=@@@email@@@&searchBy=email",
      community: "https://w3-connections.ibm.com/communities/service/html/allcommunities?email=@@@email@@@",
      blog: "https://w3-connections.ibm.com/blogs/roller-ui/allblogs?email=@@@email@@@",
      forum: "https://w3-connections.ibm.com/forums/html/search?email=@@@email@@@",
      wiki: "https://w3-connections.ibm.com/wikis/home/search?email=@@@email@@@",
      files: "https://w3-connections.ibm.com/files/app/person/@@@email@@@",
      bookmark: "https://w3-connections.ibm.com/dogear/html?email=@@@email@@@",
      activities: "https://w3-connections.ibm.com/activities/service/html/mainpage#dashboard,myactivities,email=@@@email@@@"
    },
    userinfo: null,
    postCreate: function() {
      var bcp = this;
      ibmweb.queue.push(function() {
        if (bcp.userinfo != null) {
          bcp.loading.innerHTML = "";
          bcp.infocard.style.display = "";
          return true;
        } else {
          bcp.loading.innerHTML = "<p class=\"ibm-spinner-small\" href=\"#\">&nbsp;</p><p style=\"text-align: center;font-size: 12px;\">Loading...</p> ";
          bcp.infocard.style.display = "none";
          return false;
        }
      }, function() {
        if (typeof bcp.userinfo.status != "undefined" && bcp.userinfo.status == "error") {
          bcp.loading.innerHTML = "<p>&nbsp;</p><p style=\"text-align: center;font-size: 14px;\">Unable to fetch data. Try after sometime.</p><p>&nbsp;</p>";
          bcp.infocard.style.display = "none";
        } else {
          bcp.profilelink.href = bcp.links.profile.replace("@@@email@@@", bcp.userinfo.email);
          bcp.communitylink.href = bcp.links.community.replace("@@@email@@@", bcp.userinfo.email);
          bcp.bloglink.href = bcp.links.blog.replace("@@@email@@@", bcp.userinfo.email);
          bcp.forumlink.href = bcp.links.forum.replace("@@@email@@@", bcp.userinfo.email);
          bcp.bookmarklink.href = bcp.links.bookmark.replace("@@@email@@@", bcp.userinfo.email);
          bcp.activitylink.href = bcp.links.activities.replace("@@@email@@@", bcp.userinfo.email);
          bcp.userphoto.innerHTML = "<img width=\"59\" height=\"59\" title=\"" + bcp.userinfo.name + "\" src=\"https://w3-connectionsapi.ibm.com/profiles/photo.do?email=" + bcp.userinfo.email + "\">";
          bcp.username.innerHTML = (typeof bcp.userinfo.name != "undefined") ? bcp.userinfo.name : "";
          bcp.userbio.innerHTML = (typeof bcp.userinfo.bio != "undefined") ? bcp.userinfo.bio : "";
          bcp.userlocation.innerHTML = (typeof bcp.userinfo.location != "undefined") ? bcp.userinfo.location : "";
          bcp.userbuilding.innerHTML = (typeof bcp.userinfo.building != "undefined") ? "Building:" + bcp.userinfo.building + "&nbsp;|&nbsp;" : "";
          bcp.userfloor.innerHTML = (typeof bcp.userinfo.floor != "undefined") ? "Floor:" + bcp.userinfo.floor + "&nbsp;|&nbsp;" : "";
          bcp.useroffice.innerHTML = (typeof bcp.userinfo.office != "undefined") ? "Office:" + bcp.userinfo.office : "";
          bcp.userofficephone.innerHTML = (typeof bcp.userinfo["office-phone"] != "undefined") ? bcp.userinfo["office-phone"] : "";
          bcp.usermailtoemail.innerHTML = (typeof bcp.userinfo.email != "undefined") ? bcp.userinfo.email : "";
          bcp.usermailtoemail.href = (typeof bcp.userinfo.email != "undefined") ? "mailto:" + bcp.userinfo.email : "#";
          bcp.usermailto.href = (typeof bcp.userinfo.email != "undefined") ? "mailto:" + bcp.userinfo.email : "#";
          bcp.userinfo = null;
        }
        bcp.profilelink.focus();
      });
      this.inherited(arguments);
    },
    strartup: function() {},
    constructor: function(_6bd) {
      this.userinfo = _6bd;
    },
    _hideLinkPanels: function(e) {
      if (e.currentTarget.className == "ibm-minimize-link") {
        dojo.byId("ibm-vcard-header").style.display = "none";
        e.currentTarget.className = "ibm-maximize-link";
        e.currentTarget.title = "Show more";
      } else {
        dojo.byId("ibm-vcard-header").style.display = "";
        e.currentTarget.className = "ibm-minimize-link";
        e.currentTarget.title = "Show less";
      }
    }
  });
  ibmweb.w3.bluecard = (function() {
    var lang = "",
      _6be = {
        loaded: false
      },
      _6bf = "",
      _6c0 = null;
    var _6c1 = false;

    function _6c2() {
      dojo.query(".ibm-blue-card").forEach(function(node, _6c3) {
        var _6c4 = dojo.create("span", {
          className: "vcard"
        });
        var _6c5 = "";
        var _6c6 = "";
        if (dojo.query(".email", node).length > 0) {
          var _6c7 = "Internet address";
          _6c5 = dojo.query(".email", node)[0].innerHTML;
        }
        if (dojo.query(".name", node).length > 0) {
          _6c6 = dojo.query(".name", node)[0].innerHTML;
        }
        if (_6c6 != "" && _6c5 != "") {
          var _6c8 = dojo.create("a", {
            innerHTML: _6c6,
            "aria-label": _6c6 + ". Click here or press control-enter to view the business card",
            href: "https://w3-connections.ibm.com/profiles/html/profileView.do?email=" + _6c5,
            target: "_blank",
            className: "fn url"
          }, _6c4);
          var _6c9 = dojo.create("span", {
            innerHTML: _6c5,
            className: "email",
            style: {
              display: "none"
            }
          }, _6c4);
          dojo.place(_6c4, node, "replace");
          dojo.connect(_6c8, "onfocus", _6ca);
          dojo.connect(_6c8, "mouseover", _6ca);
          dojo.connect(_6c8, "mouseout", _6cb);
        } else {
          _6c4.innerHTML = "";
          dojo.place(_6c4, node, "replace");
        }
      });
      var _6cc = dojo.byId("semtagmenu");
      if (_6cc == null) {
        var _6cc = dojo.create("span", {
          id: "semtagmenu",
          className: "ibm-bluecard",
          role: "tooltip",
          style: "position: absolute; display: none;"
        });
        dojo.connect(_6cc, "mouseover", _6cd);
        dojo.connect(_6cc, "mouseout", _6cb);
        dojo.place(_6cc, dojo.query("body")[0], "first");
      }
    };

    function _6cd() {
      var _6ce = dojo.byId("semtagmenu");
      dojo.style(_6ce, "display", "");
    };

    function _6cb(e) {
      var _6cf = dojo.byId("semtagmenu");
      dojo.style(_6cf, "display", "none");
      _6c0 = null;
    };

    function _6ca(e) {
      _6c0 = null;
      var _6d0 = dojo.byId("semtagmenu");
      var _6d1 = dojo.query("#semtagmenuBox", _6d0);
      if (dojo.query(".ibm-bluecard-popup", _6d0).length > 0) {
        dojo.destroy(dojo.query(".ibm-bluecard-popup", _6d0)[0]);
      }
      if (_6d1.length <= 0) {
        _6d1 = dojo.create("a", {
          id: "semtagmenuBox",
          className: "ibm-bluecard-text",
          style: "z-index:902; opacity: 1; top: 0;",
          innerHTML: "Click here or press control-enter to view the business card",
          title: "Click here or press control-enter to view the business card"
        }, _6d0);
      }
      _6bf = dojo.query(".email", e.currentTarget.parentElement)[0].innerHTML;
      dojo.connect(_6d1, "onclick", _6d2);
      dojo.connect(document.body, "onkeydown", function(e) {
        if (e.keyCode == 13 && e.ctrlKey) {
          if (_6d1.length > 0) {
            _6d1 = _6d1[0];
          }
          if (navigator.userAgent.indexOf("Safari") > 0) {
            var evt = document.createEvent("HTMLEvents");
            evt.initEvent("click", true, true);
            _6d1.dispatchEvent(evt);
          } else {
            _6d1.click();
          }
          e.preventDefault();
        } else {
          if (e.keyCode == 13) {
            e.preventDefault();
          }
        }
      });
      var _6d3 = dojo.coords(e.currentTarget);
      var pos = dojo.position(e.currentTarget);
      var _6d4 = document.documentElement.scrollTop != 0 ? document.documentElement.scrollTop : document.body.scrollTop;
      dojo.style(_6d0, "top", ((pos.y + _6d4 + pos.h)) + "px");
      dojo.style(_6d0, "left", (pos.x - 10) + "px");
      dojo.style(_6d0, "display", "");
      _6c1 = true;
    };

    function _6d2(e) {
      var _6d5 = dojo.byId("semtagmenu");
      dojo.destroy(dojo.byId("semtagmenuBox"));
      _6c1 = false;
      _6d6(_6bf);
      dojo.place(bluecardPanel(_6c0).domNode, _6d5);
      ibmweb.queue.push(function() {
        if (_6c0 != null) {
          return true;
        } else {
          return false;
        }
      }, function() {
        dojo.query(".ibm-bluecard-popup", _6d5).forEach(function(node, _6d7) {
          dojo.destroy(node);
        });
        dojo.place(bluecardPanel(_6c0).domNode, _6d5);
      });
    };

    function _6d6() {
      dojo.io.script.get({
        url: "//faces.tap.ibm.com/api/find/?q=" + _6bf + "&limit=1&callback=ibmweb.w3.bluecard.userinfo",
        timeout: 10000,
        error: function() {
          _6c0 = {
            "status": "error"
          };
        }
      });
    };

    function _6d8(data) {
      if (typeof data.directoryInfo == "undefined") {
        var _6d9 = ["email", "name", "bio", "location", "building", "floor", "office", "office-phone"];
        var _6da = {};
        for (var ind in _6d9) {
          _6da[_6d9[ind]] = data[0][_6d9[ind]];
        }
      } else {
        var _6d9 = {
          "email": "MailAddress",
          "name": "resolvedName",
          "location": "Address",
          "office-phone": "telephonenumber"
        };
        var _6da = {};
        var _6db = data;
        for (var ind in _6d9) {
          if (typeof data[_6d9[ind]] == "undefined") {
            data = data["directoryInfo"];
          }
          _6da[ind] = data[_6d9[ind]];
          data = _6db;
        }
      }
      _6c0 = _6da;
    };

    function _6dc() {
      _6c2();
    };
    return {
      onLoad: _6c2,
      initialize: _6dc,
      userinfo: _6d8
    };
  })();
  dojo.ready(function() {
    ibmweb.w3.bluecard.onLoad();
  });
}
if (!dojo._hasResource["ibmweb.w3.controller"]) {
  dojo._hasResource["ibmweb.w3.controller"] = true;
  dojo.provide("ibmweb.w3.controller");
  dojo.ready(function() {
    dojo.addClass(dojo.body(), "ibm-prps-js-on");
    if (dojo.hasClass(dojo.query("#ibm-top")[0], "ibm-application")) {
      dojo.addClass(dojo.body(), "ibm-prps-w3-app");
    } else {
      dojo.addClass(dojo.body(), "ibm-prps-w3");
      if (dojo.query("#ibm-masthead #ibm-universal-nav #ibm-site-title")[0]) {
        dojo.addClass(dojo.body(), "ibm-prps-with-site-title");
      }
    }
  });
  var url = location.href,
    isODWpreview = /w3.boulder.ibm.com\/workplace\//.test(url),
    isODWlive = /w3.ibm.com\/workplace\//.test(url),
    isw3Alpha2 = /w3alpha-2.toronto.ca.ibm.com\/profiles/.test(url),
    isConnection = /w3-connections.ibm.com/.test(url),
    isPP = /\/services\/practitionerportal/.test(url);
  if (!isODWlive && !isw3Alpha2 && !isConnection) {}
}