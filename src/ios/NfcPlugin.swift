import Foundation
import CoreNFC
import WebKit

@objc(NfcPlugin)
class NfcPlugin: CDVPlugin, NFCNDEFReaderSessionDelegate {
    var session: NFCNDEFReaderSession?
    var readCallbackId: String?

    @objc(startReading:)
    func startReading(command: CDVInvokedUrlCommand) {
        readCallbackId = command.callbackId
        session = NFCNDEFReaderSession(delegate: self, queue: nil, invalidateAfterFirstRead: false)
        session?.begin()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "NFC Reader started")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(stopReading:)
    func stopReading(command: CDVInvokedUrlCommand) {
        session?.invalidate()
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "NFC Reader stopped")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    @objc(writeTag:)
    func writeTag(command: CDVInvokedUrlCommand) {
        guard let data = command.arguments[0] as? String else {
            let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Invalid data format")
            self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
            return
        }

        // iOS NFC write functionality requires additional implementation
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Write functionality not yet implemented")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    }

    func readerSession(_ session: NFCNDEFReaderSession, didDetectNDEFs messages: [NFCNDEFMessage]) {
        guard let record = messages.first?.records.first else { return }
        
        let payload = String(data: record.payload, encoding: .utf8) ?? ""
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: payload)
        pluginResult?.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: readCallbackId)
    }

    func readerSession(_ session: NFCNDEFReaderSession, didInvalidateWithError error: Error) {
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error.localizedDescription)
        self.commandDelegate.send(pluginResult, callbackId: readCallbackId)
    }
}
