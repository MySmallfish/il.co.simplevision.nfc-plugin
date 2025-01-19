var exec = require("cordova/exec");

var NFC = {
    startReading: function(success, error) {
        exec(success, error, "NfcPlugin", "startReading", []);
    },
    stopReading: function(success, error) {
        exec(success, error, "NfcPlugin", "stopReading", []);
    },
    writeTag: function(data, success, error) {
        exec(success, error, "NfcPlugin", "writeTag", [data]);
    }
};

module.exports = NFC;
