# Cordova NFC Plugin

A Cordova plugin for reading and writing NFC tags on Android and iOS.

## Installation

```bash
cordova plugin add cordova-plugin-nfc
```

## Usage

### Start Reading NFC Tags

```javascript
window.NFC.startReading(
    function(payload) {
        console.log("NFC Tag Read:", payload);
    },
    function(error) {
        console.error("NFC Reading Error:", error);
    }
);
```

### Stop Reading NFC Tags

```javascript
window.NFC.stopReading(
    function() {
        console.log("NFC Reading Stopped");
    },
    function(error) {
        console.error("Error Stopping NFC:", error);
    }
);
```

### Write to NFC Tag

```javascript
window.NFC.writeTag(
    "Your data here",
    function() {
        console.log("NFC Tag Written Successfully");
    },
    function(error) {
        console.error("NFC Writing Error:", error);
    }
);
```

## Platform Support

- Android
- iOS (with CoreNFC support)

## Permissions

### Android
- NFC permission is automatically added to AndroidManifest.xml

### iOS
- Requires CoreNFC framework
- Needs NSMicrophoneUsageDescription in Info.plist

## Limitations

- iOS NFC write functionality is not yet fully implemented
- Requires devices with NFC hardware support

## License

MIT
