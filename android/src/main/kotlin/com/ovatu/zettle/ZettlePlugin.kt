package com.ovatu.zettle

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat.startActivityForResult
import com.izettle.android.commons.state.StateObserver
import com.izettle.payments.android.payment.TransactionReference
import com.izettle.payments.android.payment.refunds.RefundFailureReason
import com.izettle.payments.android.sdk.IZettleSDK
import com.izettle.payments.android.sdk.User
import com.izettle.payments.android.ui.payment.CardPaymentActivity
import com.izettle.payments.android.ui.payment.CardPaymentResult
import com.izettle.payments.android.ui.payment.FailureReason
import com.izettle.payments.android.ui.readers.CardReadersActivity
import com.izettle.payments.android.ui.refunds.RefundResult
import com.izettle.payments.android.ui.refunds.RefundsActivity

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import java.util.*

/** ZettlePlugin */
class ZettlePlugin: FlutterPlugin, MethodCallHandler, ActivityAware, PluginRegistry.ActivityResultListener {
  private val tag = "_ZettlePlugin"

  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var activity: Activity

  private var operations: MutableMap<String, ZettlePluginResponseWrapper> = mutableMapOf()
  private lateinit var currentOperation: ZettlePluginResponseWrapper

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(tag, "onAttachedToEngine")
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "zettle")
    channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(tag, "onDetachedFromEngine")
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    Log.d(tag, "onAttachedToActivity")
    activity = binding.activity
    binding.addActivityResultListener(this)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    Log.d(tag, "onDetachedFromActivityForConfigChanges")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    Log.d(tag, "onReattachedToActivityForConfigChanges")
  }

  override fun onDetachedFromActivity() {
    Log.d(tag, "onDetachedFromActivity")
    IZettleSDK.stop()
    //IZettleSDK.user.state.removeObserver(authObserver)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    Log.d(tag, "onMethodCall: ${call.method}")
    if (!operations.containsKey(call.method)) {
      operations[call.method] = ZettlePluginResponseWrapper(result)
    }

    val pluginResponseWrapper = operations[call.method]!!
    pluginResponseWrapper.methodResult = result
    pluginResponseWrapper.response = ZettlePluginResponse(call.method)

    currentOperation = pluginResponseWrapper

    when (call.method) {
      "init" -> init(call.arguments as Map<*, *>).flutterResult()
      //"loggedIn" -> loggedIn().flutterResult()
      //"info" -> info().flutterResult()
      //"login" -> login()
      //"loginWithToken" -> loginWithToken(call.arguments as String)
      "requestPayment" -> requestPayment(call.arguments as Map<*, *>)
      "requestRefund" -> requestRefund(call.arguments as Map<*, *>)
      "showSettings" -> showSettings().flutterResult()
      else -> result.notImplemented()
    }
  }

  var loggedIn = false
  var info: User.Info? = null

  fun loginChange() {
    var currentOp: ZettlePluginResponseWrapper? = null
    if (operations["login"] != null) {
      currentOp = operations["login"]
    } else if (operations["loginWithToken"] != null) {
      currentOp = operations["loginWithToken"]
    }

    if (currentOp != null) {
      currentOp.response.status = loggedIn
      currentOp.response.message = mutableMapOf(
              "loggedIn" to loggedIn
      )

      currentOp.flutterResult()
      operations.remove(currentOp.response.methodName)
    }
  }

  /*private val authObserver = object : StateObserver<User.AuthState> {
    override fun onNext(state: User.AuthState) {
      Log.d(tag, "authObserver.onNext: $state")

      channel.invokeMethod("userStateDidChange", null)

      when (state) {
        is User.AuthState.LoggedIn -> { // User authorized
          loggedIn = true
          info = state.info
          loginChange()
        }
        is User.AuthState.LoggedOut -> { // There is no authorized use
          loggedIn = false
          info = null
          loginChange()
        }
        else -> {}
      }
    }
  }*/

  private fun init(@NonNull args: Map<*, *>): ZettlePluginResponseWrapper {
    val currentOp = operations["init"]!!
    try {
      val clientID = args["androidClientId"] as String
      val redirect = args["redirect"] as String

      IZettleSDK.init(activity, clientID, redirect)
      IZettleSDK.start()
      //IZettleSDK.user.state.addObserver(authObserver)

      currentOp.response.message = mutableMapOf("initialized" to true)
      currentOp.response.status = true
    } catch (e: Exception) {
      currentOp.response.message = mutableMapOf("initialized" to false, "errors" to e.message)
      currentOp.response.status = false
    }
    return currentOp
  }

  /*private fun loggedIn(): ZettlePluginResponseWrapper {
    val currentOp = operations["loggedIn"]!!

    try {
      currentOp.response.status = loggedIn
      currentOp.response.message = mutableMapOf("loggedIn" to loggedIn)
    } catch (e: Exception) {
      currentOp.response.message = mutableMapOf("errors" to e.message)
      currentOp.response.status = false
    }
    return currentOp
  }*/

  /*private fun info(): ZettlePluginResponseWrapper {
    val currentOp = operations["info"]!!
    try {
      currentOp.response.status = true
      currentOp.response.message = mutableMapOf(
              "organizationId" to info?.organizationId,
              "country" to info?.country.toString(),
              "publicName" to info?.publicName,
              "userId" to info?.userId,
              "imageUrl" to info?.imageUrl?.small,
              "timeZone" to info?.timeZone.toString(),
              "currency" to info?.currency.toString()
      )
    } catch (e: Exception) {
      currentOp.response.message = mutableMapOf("errors" to e.message)
      currentOp.response.status = false
    }
    return currentOp
  }*/

  /*private fun login() {
    Log.d(tag, "login")
    IZettleSDK.user.login(activity)
  }

  private fun loginWithToken(@NonNull token: String) {
    Log.d(tag, "loginWithToken")
    IZettleSDK.user.login(token)
  }*/

  private fun requestPayment(@NonNull args: Map<*, *>) {
    Log.d(tag, "requestPayment: $args")

    val internalUniqueTraceId = args["reference"] as String
    val reference = TransactionReference.Builder(internalUniqueTraceId).build()

    val intent = CardPaymentActivity.IntentBuilder(activity)
            .amount((args["amount"] as Int).toLong())
            .reference(reference)
            .enableLogin(args["enableLogin"] as Boolean? ?: true)
            .enableTipping(args["enableTipping"] as Boolean? ?: true)
            .enableInstalments(args["enableInstalments"] as Boolean? ?: false)
            .build()

    startActivityForResult(activity, intent, ZettleTask.REQUEST_PAYMENT.code, null)
  }

  private fun requestRefund(@NonNull args: Map<*, *>) {
    Log.d(tag, "requestRefund: $args")

    val internalUniqueTraceId = args["reference"] as String
    val reference = TransactionReference.Builder(internalUniqueTraceId).build()

    val intentBuilder = RefundsActivity.IntentBuilder(activity)
            .reference(reference)

    if (args["refundAmount"] != null) {
      intentBuilder.refundAmount((args["refundAmount"] as Int).toLong())
    }
    if (args["taxAmount"] != null) {
      intentBuilder.taxAmount((args["taxAmount"] as Int).toLong())
    }
    if (args["receiptNumber"] != null) {
      intentBuilder.receiptNumber(args["receiptNumber"] as String)
    }

    val intent = intentBuilder.build()

    // Start activity with the intent
    startActivityForResult(activity, intent, ZettleTask.REQUEST_REFUND.code, null)
  }

  private fun showSettings(): ZettlePluginResponseWrapper {
    val currentOp = operations["showSettings"]!!

    val intent = CardReadersActivity.newIntent(activity)
    startActivityForResult(activity, intent, ZettleTask.SETTINGS.code, null)

    return currentOp
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
    Log.d(tag, "onActivityResult - RequestCode: $requestCode - Result Code: $resultCode")

    Log.d(tag, "onActivityResult - operations: $operations")

    val currentOp: ZettlePluginResponseWrapper? = when (ZettleTask.valueOf(requestCode)) {
      ZettleTask.REQUEST_PAYMENT -> operations["requestPayment"]
      ZettleTask.REQUEST_REFUND -> operations["requestRefund"]
      else -> currentOperation
    }

    Log.d(tag, "onActivityResult - cuurent op: $currentOp")

    if (currentOp == null) {
      return false
    }

    if (data != null && data.extras != null) {
      val result = resultCode == Activity.RESULT_OK

      currentOp.response.status = result

      when (ZettleTask.valueOf(requestCode)) {
        ZettleTask.REQUEST_PAYMENT -> {

          when (val paymentResult: CardPaymentResult? =
                  data.getParcelableExtra(CardPaymentActivity.RESULT_EXTRA_PAYLOAD)) {
            is CardPaymentResult.Completed -> {
              currentOp.response.status = true
              currentOp.response.message = mutableMapOf(
                      "status" to "completed",
                      "amount" to paymentResult.payload.amount,
                      "gratuityAmount" to paymentResult.payload.gratuityAmount,
                      "cardType" to paymentResult.payload.cardType,
                      "cardPaymentEntryMode" to paymentResult.payload.cardPaymentEntryMode,
                      "cardholderVerificationMethod" to paymentResult.payload.cardholderVerificationMethod,
                      "tsi" to paymentResult.payload.tsi,
                      "tvr" to paymentResult.payload.tvr,
                      "applicationIdentifier" to paymentResult.payload.applicationIdentifier,
                      "cardIssuingBank" to paymentResult.payload.cardIssuingBank,
                      "maskedPan" to paymentResult.payload.maskedPan,
                      "panHash" to paymentResult.payload.panHash,
                      "applicationName" to paymentResult.payload.applicationName,
                      "authorizationCode" to paymentResult.payload.authorizationCode,
                      "installmentAmount" to paymentResult.payload.installmentAmount,
                      "nrOfInstallments" to paymentResult.payload.nrOfInstallments,
                      "mxFiid" to paymentResult.payload.mxFiid,
                      "mxCardType" to paymentResult.payload.mxCardType,
                      "panHash" to paymentResult.payload.panHash,
                      "reference" to paymentResult.payload.reference?.id,
              )
            }
            is CardPaymentResult.Canceled -> {
              currentOp.response.status = false
              currentOp.response.message = mutableMapOf(
                      "status" to "canceled"
              )
            }
            is CardPaymentResult.Failed -> {
              currentOp.response.message = mutableMapOf(
                      "status" to "failed",
              )
            }
          }

          currentOp.flutterResult()
          operations.remove(currentOp.response.methodName)
        }
        ZettleTask.REQUEST_REFUND -> {

          when (val paymentResult: RefundResult? =
                  data.getParcelableExtra(CardPaymentActivity.RESULT_EXTRA_PAYLOAD)) {
            is RefundResult.Completed -> {
              currentOp.response.status = true
              currentOp.response.message = mutableMapOf(
                      "status" to "completed",
                      "originalAmount" to paymentResult.payload.originalAmount,
                      "refundedAmount" to paymentResult.payload.refundedAmount,
                      "cardType" to paymentResult.payload.cardType,
                      "maskedPan" to paymentResult.payload.maskedPan,
                      "cardPaymentUUID" to paymentResult.payload.cardPaymentUUID,
              )
            }
            is RefundResult.Canceled -> {
              currentOp.response.status = false
              currentOp.response.message = mutableMapOf(
                      "status" to "canceled"
              )
            }
            is RefundResult.Failed -> {
              currentOp.response.status = false
              currentOp.response.message = mutableMapOf(
                      "status" to "failed",
              )
            }
          }


          currentOp.flutterResult()
          operations.remove(currentOp.response.methodName)
        }
        else -> {
          currentOp.response.message = mutableMapOf("errors" to "Intent Data and/or Extras are null or empty")
          currentOp.response.status = false
          currentOp.flutterResult()
          operations.remove(currentOp.response.methodName)
        }
      }
    } else {
      currentOp.response.message = mutableMapOf("errors" to "Intent Data and/or Extras are null or empty")
      currentOp.response.status = false
      currentOp.flutterResult()
      operations.remove(currentOp.response.methodName)
    }
    return currentOp.response.status
  }
}


enum class ZettleTask(val code: Int) {
  REQUEST_PAYMENT(1), REQUEST_REFUND(2), SETTINGS(3);

  companion object {
    fun valueOf(value: Int) = values().find { it.code == value }
  }
}

class ZettlePluginResponseWrapper(@NonNull var methodResult: Result) {
  lateinit var response: ZettlePluginResponse
  fun flutterResult() {
    methodResult.success(response.toMap())
  }
}

class ZettlePluginResponse(@NonNull var methodName: String) {
  var status: Boolean = false
  lateinit var message: MutableMap<String, Any?>
  fun toMap(): Map<String, Any?> {
    return mapOf("status" to status, "message" to message, "methodName" to methodName)
  }
}
