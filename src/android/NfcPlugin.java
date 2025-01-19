package com.simplevision.nfc;

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
