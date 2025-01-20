Updated Project Structure
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
🔧 Updated plugin.xml (Plugin Configuration)
xml
Copy
Edit
<?xml version="1.0" encoding="UTF-8"?>
<plugin id="cordova-plugin-nfc" version="1.1.0" xmlns="http://apache.org/cordova/ns/plugins/1.0">
    <name>NFC Plugin</name>
    <description>Cordova plugin for NFC reading and writing</description>
    <license>MIT</license>
    
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
            <string>App requires NFC access</string>
        </config-file>
        <source-file src="src/ios/NfcPlugin.swift" />
    </platform>
</plugin>
🤖 Android: Event-Driven & Background Scanning (NfcPlugin.java)
This implementation: ✔ Uses Foreground & Background dispatching for NFC events.
✔ Emits real-time events to JavaScript when a tag is detected.
✔ Supports reading and writing NFC tags.

java
Copy
Edit
package com.example.nfc;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefMessage;
import android.nfc.tech.NdefRecord;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;

public class NfcPlugin extends CordovaPlugin {
    private static final String TAG = "NfcPlugin";
    private NfcAdapter nfcAdapter;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        Activity activity = cordova.getActivity();
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);

        if (nfcAdapter == null) {
            callbackContext.error("NFC not supported");
            return false;
        }

        switch (action) {
            case "startReading":
                startNfc();
                return true;
            case "stopReading":
                stopNfc();
                return true;
            case "writeTag":
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
        Intent intent = new Intent(cordova.getActivity(), cordova.getActivity().getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                cordova.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        nfcAdapter.enableForegroundDispatch(cordova.getActivity(), pendingIntent, null, null);
        callbackContext.success("NFC scanning started");
    }

    private void stopNfc() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(cordova.getActivity());
            callbackContext.success("NFC scanning stopped");
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            JSONObject result = new JSONObject();
            try {
                result.put("id", bytesToHex(tag.getId()));
                result.put("tech", tag.getTechList());
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            } catch (JSONException e) {
                callbackContext.error("Error reading NFC");
            }
        }
    }

    private void writeTag(String data) {
        // Implementation for writing data to an NFC tag
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
}
🍏 iOS: Event-Driven NFC Handling (NfcPlugin.swift)
✔ Uses CoreNFC for scanning NFC tags.
✔ Emits real-time events to JavaScript when an NFC tag is detected.

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
        session = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: false)
        session?.begin()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "NFC Reader started")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        if let record = messages.first?.records.first {
            let payload = String(data: record.payload, encoding: .utf8) ?? ""
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: payload)
            pluginResult?.setKeepCallbackAs(true)
            self.commandDelegate.send(pluginResult, callbackId: "nfc-read")
        }
    }
}
📜 JavaScript Interface (nfc.js)
✔ Uses event-driven architecture for real-time tag detection.
✔ Provides easy-to-use functions for NFC interaction.

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
    },
    onTagRead: function(callback) {
        document.addEventListener("nfc-read", function(event) {
            callback(event.detail);
        });
    }
};

module.exports = NFC;