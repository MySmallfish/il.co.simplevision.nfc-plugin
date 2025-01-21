var exec = require("cordova/exec");

var NFC = {
    startReading: function(success, error) {
        console.log('NFC: Attempting to start reading');
        exec(
            function(result) {
                console.log('NFC: Reading started successfully', result);
                if (success) success(result);
            }, 
            function(err) {
                console.error('NFC: Failed to start reading', err);
                if (error) error(err);
            }, 
            "NfcPlugin", 
            "startReading", 
            []
        );
    },
    stopReading: function(success, error) {
        console.log('NFC: Attempting to stop reading');
        exec(
            function(result) {
                console.log('NFC: Reading stopped successfully', result);
                if (success) success(result);
            }, 
            function(err) {
                console.error('NFC: Failed to stop reading', err);
                if (error) error(err);
            }, 
            "NfcPlugin", 
            "stopReading", 
            []
        );
    },
    writeTag: function(data, success, error) {
        console.log('NFC: Attempting to write tag', data);
        exec(
            function(result) {
                console.log('NFC: Tag write successful', result);
                if (success) success(result);
            }, 
            function(err) {
                console.error('NFC: Failed to write tag', err);
                if (error) error(err);
            }, 
            "NfcPlugin", 
            "writeTag", 
            [data]
        );
    },
    onNfcRead: function(tagData) {
        console.log('NFC: Tag read event triggered', tagData);
        var event = new CustomEvent('nfcread', { detail: tagData });
        document.dispatchEvent(event);
    }
};

// Start NFC reading when device is ready
document.addEventListener('deviceready', function() {
    console.log('NFC: Device ready, starting NFC reading...');
    NFC.startReading(
        function(tagData) {
            console.log('NFC: Successfully started reading', tagData);
            NFC.onNfcRead(tagData);
        },
        function(error) {
            console.error('NFC: Error starting reading', error);
        }
    );
}, false);

module.exports = NFC;
