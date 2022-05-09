import Flutter
import UIKit
import iZettleSDK

public class SwiftZettlePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "zettle", binaryMessenger: registrar.messenger())
    let instance = SwiftZettlePlugin()
      registrar.addMethodCallDelegate(instance, channel: channel)
      registrar.addApplicationDelegate(instance)
  }

    private func topController() -> UIViewController {
        return UIApplication.shared.keyWindow!.rootViewController!
    }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      let pluginResponse = ZettlePluginResponse(methodName: call.method)

      switch (call.method) {
        case "init":
          let initResult = _init(call.arguments as! [String:Any])
          pluginResponse.status = initResult
          pluginResponse.message = ["result": initResult]
          result(pluginResponse.toDict())

        case "requestPayment":
          _requestPayment(call.arguments as! [String:Any]) { success, message in
              pluginResponse.status = success
              pluginResponse.message = message
              result(pluginResponse.toDict())
          }
      case "requestRefund":
        _requestRefund(call.arguments as! [String:Any]) { success, message in
            pluginResponse.status = success
            pluginResponse.message = message
            result(pluginResponse.toDict())
        }
      case "showSettings":
        _showSettings()
          
            pluginResponse.status = true
          pluginResponse.message = [:]
            result(pluginResponse.toDict())
        default:
          pluginResponse.status = false
          pluginResponse.message = ["result": "Method not implemented"]
          result(pluginResponse.toDict())
      }
    }
    
    func _init(_ options: [String:Any]) -> Bool {
        let clientID = options["iosClientId"] as! String
        let callbackURL = options["redirect"] as! String
        
        print(clientID)
        print(callbackURL)

        do {
            let authenticationProvider = try iZettleSDKAuthorization(
                clientID: clientID,
                callbackURL: URL(string: callbackURL)!)

            iZettleSDK.shared().start(with: authenticationProvider)

            return true
        } catch {
            return false
        }
    }

    func _requestPayment(_ payment: [String:Any], completion: @escaping ((Bool, [String:Any?]) -> Void)) {
        let enableTipping = (payment["enableTipping"] as? Bool) ?? true
        let reference = payment["reference"] as! String
        let amount = NSDecimalNumber(decimal: Decimal(sign: .plus, exponent: -2, significand: Decimal(payment["amount"] as! Int)))
        
        iZettleSDK.shared().charge(amount: amount, enableTipping: enableTipping, reference: reference, presentFrom: topController()) { payment, error in
            
            if (error != nil) {
                completion(false, [
                    "status": "failed",
                ])
            } else if (payment == nil) {
                completion(false, [
                    "status": "canceled",
                ])
            } else {
                completion(true, [
                    "status": "completed",
                    "amount": payment!.amount,
                    "gratuityAmount": payment!.gratuityAmount,
                    "cardType": payment!.cardBrand,
                    "cardPaymentEntryMode": payment!.entryMode,
                    "cardholderVerificationMethod": nil,
                    "tsi": payment!.tsi,
                    "tvr": payment!.tvr,
                    "applicationIdentifier": payment!.aid,
                    "cardIssuingBank": nil,
                    "maskedPan": payment!.obfuscatedPan,
                    "panHash": payment!.panHash,
                    "applicationName": payment!.applicationName,
                    "authorizationCode": payment!.authorizationCode,
                    "installmentAmount": payment!.installmentAmount,
                    "nrOfInstallments": payment!.numberOfInstallments,
                    "mxFiid": payment!.mxFIID,
                    "mxCardType": payment!.mxCardType,
                    "reference": payment!.referenceNumber,
                ])
            }
        }
    }
    
    func _requestRefund(_ refund: [String:Any], completion: @escaping ((Bool, [String:Any?]) -> Void)) {
        let reference = refund["reference"] as! String
        let receiptNumber = refund["receiptNumber"] as? String
        let refundAmount = NSDecimalNumber(decimal: Decimal(sign: .plus, exponent: -2, significand: Decimal(refund["refundAmount"] as! Int)))

        iZettleSDK.shared().refund(amount: refundAmount, ofPayment: reference, withRefundReference: receiptNumber, presentFrom: topController()) { payment, error in
            if (error != nil) {
                completion(false, [
                    "status": "failed",
                ])
            } else if (payment == nil) {
                completion(false, [
                    "status": "canceled",
                ])
            } else {
                completion(true, [
                    "status": "completed",
                ])
            }

        }
    }

    func _showSettings() {
        iZettleSDK.shared().presentSettings(from: topController())
    }
}

public class ZettlePluginResponseWrapper: NSObject {
    var response: ZettlePluginResponse!
    var result: FlutterResult!

    init(result: @escaping FlutterResult) {
        self.result = result
    }

    
    func flutterResult() {
        result(response.toDict())
    }
}

public class ZettlePluginResponse: NSObject {
    var methodName: String
    var status: Bool = false
    var message: [String:Any?]?

    init(methodName: String) {
        self.methodName = methodName
    }


    func toDict() -> [String:Any?] {
        return [
            "status": status,
            "message": message,
            "methodName": methodName
        ]
    }
}
