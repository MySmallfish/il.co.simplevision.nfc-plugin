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
    },
    onNfcRead: function(tagData) {
        var event = new CustomEvent('nfcread', { detail: tagData });
        document.dispatchEvent(event);
        console.log('NFC Tag Read:', tagData);
    }
};

// Start NFC reading when device is ready
document.addEventListener('deviceready', function() {
    console.log('Starting NFC reading...');
    NFC.startReading(
        function(tagData) {
            NFC.onNfcRead(tagData);
        },
        function(error) {
            console.error('NFC reading error:', error);
        }
    );
}, false);

module.exports = NFC;
