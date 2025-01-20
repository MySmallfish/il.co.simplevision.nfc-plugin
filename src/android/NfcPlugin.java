package com.simplevision.nfc;

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
import java.util.Arrays;

public class NfcPlugin extends CordovaPlugin {
    private static final String TAG = "NfcPlugin";
    private static final String ACTION_START = "startReading";
    private static final String ACTION_STOP = "stopReading";
    private static final String ACTION_WRITE = "writeTag";

    private NfcAdapter nfcAdapter;
    private CallbackContext callbackContext;
    private PendingIntent pendingIntent;

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
            case ACTION_START:
                startNfc();
                return true;
            case ACTION_STOP:
                stopNfc();
                return true;
            case ACTION_WRITE:
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
        try {
            Intent intent = new Intent(cordova.getActivity(), cordova.getActivity().getClass())
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(
                    cordova.getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            nfcAdapter.enableForegroundDispatch(cordova.getActivity(), pendingIntent, null, null);
            callbackContext.success("NFC scanning started");
        } catch (Exception e) {
            callbackContext.error("Error starting NFC: " + e.getMessage());
        }
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
            try {
                JSONObject result = new JSONObject();
                result.put("id", bytesToHex(tag.getId()));
                result.put("tech", Arrays.toString(tag.getTechList()));

                // Try to read NDEF message if available
                if (Arrays.asList(tag.getTechList()).contains("android.nfc.tech.Ndef")) {
                    Ndef ndef = Ndef.get(tag);
                    if (ndef != null) {
                        ndef.connect();
                        NdefMessage ndefMessage = ndef.getNdefMessage();
                        if (ndefMessage != null && ndefMessage.getRecords().length > 0) {
                            NdefRecord record = ndefMessage.getRecords()[0];
                            result.put("payload", new String(record.getPayload(), Charset.forName("UTF-8")));
                        }
                        ndef.close();
                    }
                }

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            } catch (Exception e) {
                callbackContext.error("Error reading NFC tag: " + e.getMessage());
            }
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

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }

    private Tag getTag() {
        Activity activity = cordova.getActivity();
        Intent intent = activity.getIntent();
        return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    }
}
