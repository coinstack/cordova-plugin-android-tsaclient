(function() {
    function TSAClient() {}

    var ua = navigator.userAgent;
    var checker = {
        iphone: ua.match(/(iPhone|iPod|iPad)/),
        blackberry: ua.match(/BlackBerry/),
        android: ua.match(/Android/)
    };

    var errors = {
        TOO_MANY_TRIES: {
            code: -101,
            message: "Fingerprint is disabled due to too many tries",
        },
        KEY_NOT_FOUND: {
            code: -102,
            message: "Fingerprint key is not initialized",
        },
        FINGERPRINT_NOT_AVAILABLE: {
            code: -103,
            message: "Fingerprint capability is not accessible",
        },
        PLUGIN_NOT_LOADED: {
            code: -104,
            message: "Fingerprint plugin is not loaded",
        }
    }

    TSAClient.prototype.errors = errors;

    var status = {

    }

    TSAClient.prototype.status = status;

    var statePrefix = "blockfingerprintkey_state_"
    TSAClient.prototype.getState = function(key) {
        if (!key) {
            key = "";
        }
        return localStorage.getItem(statePrefix + key);
    }

    TSAClient.prototype.setState = function(key, state) {
        if (!key) {
            key = "";
        }
        return localStorage.setItem(statePrefix + key, state);
    }


    var pluginLoaded = false;

    function checkPlugin() {
        return pluginLoaded;
    }

    TSAClient.prototype.checkPlugin = checkPlugin;


    TSAClient.prototype.getDevice = function() {
        return "Android";
    }

    TSAClient.prototype.stampDocument = function(params, successCallback, errorCallback) {
        cordova.exec(
            successCallback,
            errorCallback,
            "TSAClient", // Java Class
            "stampDocument", // action
            [ // Array of arguments to pass to the Java class
                params
            ]
        );
    };

    TSAClient = new TSAClient();
    window.TSAClient = TSAClient;
})();
module.exports = TSAClient;