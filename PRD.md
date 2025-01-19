Creating a Cordova NFC plugin that supports both Android and iOS requires bridging native code (Java/Kotlin for Android and Swift/Objective-C for iOS) with JavaScript. Below is a structured guide following your C.R.A.F.T. approach.

📌 Cordova NFC Plugin – Full Implementation
📂 1. Project Structure
plaintext
Copy
Edit
cordova-plugin-nfc/
│── package.json
│── plugin.xml
│── src/
│   ├── android/
│   │   ├── NfcPlugin.java
│   ├── ios/
│   │   ├── NfcPlugin.swift
│── www/
│   ├── nfc.js
│── example/
│   ├── config.xml
│   ├── platforms/
│   ├── plugins/
│   ├── www/
🔧 2. plugin.xml (Plugin Configuration)
This file defines the plugin’s metadata, platform dependencies, and native code integration.

xml
Copy
Edit
<?xml version="1.0" encoding="UTF-8"?>
<plugin id="cordova-plugin-nfc" version="1.0.0" xmlns="http://apache.org/cordova/ns/plugins/1.0">
    <name>NFC Plugin</name>
    <description>Cordova plugin to read/write NFC tags</description>
    <license>MIT</license>
    <keywords>cordova, nfc</keywords>
    
    <js-module src="www/nfc.js" name="NFC">
        <clobbers target="window.NFC"/>
    </js-module>

    <!-- Android -->
    <platform name="android">
        <config-file target="AndroidManifest.xml" parent="/*">
            <uses-permission android:name="android.permission.NFC"/>
            <uses-feature android:name="android.hardware.nfc" android:required="true"/>
        </config-file>
        <source-file src="src/android/NfcPlugin.java" target-dir="src/com/example/nfc"/>
    </platform>

    <!-- iOS -->
    <platform name="ios">
        <framework src="CoreNFC.framework" weak="true"/>
        <config-file parent="NSMicrophoneUsageDescription" target="*-Info.plist">
            <string>App requires NFC access to read tags</string>
        </config-file>
        <source-file src="src/ios/NfcPlugin.swift" />
    </platform>
</plugin>
🤖 3. Android Implementation (NfcPlugin.java)
This class handles NFC initialization, reading, and writing.

java
Copy
Edit
package com.example.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefRecord;
import android.nfc.NdefMessage;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

public class NfcPlugin extends CordovaPlugin {
    private static final String ACTION_START = "startReading";
    private static final String ACTION_STOP = "stopReading";
    private static final String ACTION_WRITE = "writeTag";

    private NfcAdapter nfcAdapter;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;

        if (ACTION_START.equals(action)) {
            startNfc();
            return true;
        } else if (ACTION_STOP.equals(action)) {
            stopNfc();
            return true;
        } else if (ACTION_WRITE.equals(action)) {
            try {
                String data = args.getString(0);
                writeTag(data);
                return true;
            } catch (JSONException e) {
                callbackContext.error("Invalid data format");
            }
        }
        return false;
    }

    private void startNfc() {
        Activity activity = cordova.getActivity();
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        if (nfcAdapter == null) {
            callbackContext.error("NFC not supported");
            return;
        }

        Intent intent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);

        callbackContext.success("NFC listening started");
    }

    private void stopNfc() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(cordova.getActivity());
            callbackContext.success("NFC listening stopped");
        }
    }

    private void writeTag(String data) {
        Tag tag = getTag(); 
        if (tag == null) {
            callbackContext.error("No tag detected");
            return;
        }

        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            try {
                ndef.connect();
                NdefRecord record = NdefRecord.createTextRecord("en", data);
                NdefMessage message = new NdefMessage(record);
                ndef.writeNdefMessage(message);
                ndef.close();
                callbackContext.success("Write successful");
            } catch (Exception e) {
                callbackContext.error("Write failed: " + e.getMessage());
            }
        }
    }
}
🍏 4. iOS Implementation (NfcPlugin.swift)
swift
Copy
Edit
import Foundation
import CoreNFC
import Cordova

@objc(NfcPlugin)
class NfcPlugin: CDVPlugin, NFCNDEFReaderSessionDelegate {
    var session: NFCNDEFReaderSession?

    @objc(startReading:)
    func startReading(command: CDVInvokedUrlCommand) {
        session = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: true)
        session?.begin()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "NFC Reader started")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        if let record = messages.first?.records.first {
            let payload = String(data: record.payload, encoding: .utf8) ?? ""
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: payload)
            self.commandDelegate.send(pluginResult, callbackId: "nfc-read")
        }
    }
}
📜 5. JavaScript Interface (nfc.js)
javascript
Copy
Edit
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
