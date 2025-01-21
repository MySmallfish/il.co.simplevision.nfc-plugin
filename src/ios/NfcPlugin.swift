import Foundation
import CoreNFC
import WebKit

@objc(NfcPlugin)
class NfcPlugin: CDVPlugin, NFCNDEFReaderSessionDelegate {
    var session: NFCNDEFReaderSession?
    var readCallbackId: String?

    @objc(startReading:)
    func startReading(command: CDVInvokedUrlCommand) {
        // Check NFC availability
        guard NFCNDEFReaderSession.readingAvailable else {
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR, 
                messageAs: "NFC reading is not available on this device"
            )
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        readCallbackId = command.callbackId
        
        do {
            session = try NFCNDEFReaderSession(
                delegate: self, 
                queue: nil, 
                invalidateAfterFirstRead: false
            )
            session?.begin()
            
            // Keep the callback for future NFC events
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_OK, 
                messageAs: "NFC scanning started"
            )
            pluginResult?.setKeepCallbackAs(true)
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        } catch {
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR, 
                messageAs: "Failed to start NFC session: \(error.localizedDescription)"
            )
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
        }
    }

    @objc(stopReading:)
    func stopReading(command: CDVInvokedUrlCommand) {
        session?.invalidate()
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK, 
            messageAs: "NFC scanning stopped"
        )
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(writeTag:)
    func writeTag(command: CDVInvokedUrlCommand) {
        guard let data = command.arguments[0] as? String else {
            let pluginResult = CDVPluginResult(
                status: CDVCommandStatus_ERROR, 
                messageAs: "Invalid data format"
            )
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        // iOS NFC write functionality requires additional implementation
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR, 
            messageAs: "Write functionality not yet implemented"
        )
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        guard let record = messages.first?.records.first else { 
            print("NFC: No records found in the tag")
            return 
        }
        
        let payload = String(data: record.payload, encoding: .utf8) ?? ""
        print("NFC: Detected payload - \(payload)")
        
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_OK, 
            messageAs: payload
        )
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: readCallbackId)
    }

    func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        print("NFC: Session invalidated with error - \(error.localizedDescription)")
        
        let pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR, 
            messageAs: error.localizedDescription
        )
        self.commandDelegate.send(pluginResult, callbackId: readCallbackId)
    }

    func readerSessionDidBecomeActive(_ session: NFCNDEFReaderSession) {
        print("NFC: Reader session became active")
    }
}
