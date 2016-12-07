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

    document.addEventListener("deviceready", function() {
        // do async check to check plugin is loaded
        cordova.exec(function() {
            console.log("plugin load detected");
            pluginLoaded = true;
        }, function(err) {
            console.log("plugin load failure detected");
            console.log(err);
            pluginLoaded = false;
        }, "TSAClient", "availability", [{}]);
    }, false);

    TSAClient.prototype.getDevice = function() {
        return "Android";
    }

    TSAClient.prototype.initKey = function(params, successCallback, errorCallback) {
        if (!checkPlugin()) {
            errorCallback({
                status: "error",
                error: errors.PLUGIN_NOT_LOADED
            });
            return;
        }
        cordova.exec(
            function(res) {
                if (res.status == "ok") {
                    res.key = createKeyFromHexSeed(res.key);
                } else if (res.status == "error") {
                    res.cause = res.error;
                    if (res.error == 7) {
                        res.error = errors.TOO_MANY_TRIES;
                    } else if (res.error == -314) {
                        res.error = errors.KEY_NOT_FOUND;
                    } else {
                        res.error = errors.FINGERPRINT_NOT_AVAILABLE;
                    }
                }
                successCallback(res);
            },
            errorCallback,
            "TSAClient", // Java Class
            "initkey", // action
            [ // Array of arguments to pass to the Java class
                params
            ]
        );
    };

    TSAClient = new TSAClient();
    window.TSAClient = TSAClient;
})();
module.exports = TSAClient;