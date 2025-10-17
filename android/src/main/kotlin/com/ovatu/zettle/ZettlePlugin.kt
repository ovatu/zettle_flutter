package com.ovatu.zettle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.lifecycle.ProcessLifecycleOwner
import com.zettle.sdk.ZettleSDK
import com.zettle.sdk.ZettleSDKLifecycle
import com.zettle.sdk.config
// Import the CardReaderFeature from the correct package
import com.zettle.sdk.feature.cardreader.ui.CardReaderFeature
import com.zettle.sdk.feature.cardreader.payment.PayPalReaderTippingStyle
import com.zettle.sdk.feature.cardreader.payment.TippingConfiguration
import com.zettle.sdk.feature.cardreader.payment.TransactionReference
import com.zettle.sdk.feature.cardreader.payment.ZettleReaderTippingStyle
import com.zettle.sdk.feature.cardreader.ui.CardReaderAction
import com.zettle.sdk.feature.cardreader.ui.payment.CardPaymentResult
import com.zettle.sdk.feature.cardreader.ui.refunds.RefundResult
import com.zettle.sdk.features.charge
import com.zettle.sdk.features.refund
import com.zettle.sdk.features.retrieve
import com.zettle.sdk.features.show
import com.zettle.sdk.ui.ZettleResult
import com.zettle.sdk.ui.zettleResult
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*
import kotlin.math.abs

/** ZettlePlugin */
class ZettlePlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private val tag = "_ZettlePlugin"

  // The MethodChannel that will handle communication between Flutter and native Android
  private lateinit var channel : MethodChannel
  private lateinit var context: Context
  private lateinit var activity: ComponentActivity

  // Activity result launchers
  private lateinit var paymentLauncher: ActivityResultLauncher<Intent>
  private lateinit var refundLauncher: ActivityResultLauncher<Intent>
  private lateinit var settingsLauncher: ActivityResultLauncher<Intent>
  private lateinit var tippingSettingsLauncher: ActivityResultLauncher<Intent>

  private data class PendingOperation(val result: Result, val methodName: String)

  // Pending method results
  private var pendingOperation: PendingOperation? = null

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(tag, "onAttachedToEngine")
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "zettle")
    channel.setMethodCallHandler(this)
    context = flutterPluginBinding.applicationContext
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    Log.d(tag, "onDetachedFromEngine")
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    Log.d(tag, "onAttachedToActivity")
    activity = binding.activity as ComponentActivity

    // Register activity result launchers
    registerActivityResultLaunchers()
  }

  private fun registerActivityResultLaunchers() {
    paymentLauncher = activity.registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
      if (activityResult.resultCode != Activity.RESULT_OK || activityResult.data == null) {
        sendResult(false, mapOf("status" to "canceled"))
        return@registerForActivityResult
      }

      val result = activityResult.data?.zettleResult()
      handlePaymentResult(result)
    }

    refundLauncher = activity.registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
      if (activityResult.resultCode != Activity.RESULT_OK || activityResult.data == null) {
        sendResult(false, mapOf("status" to "canceled"))
        return@registerForActivityResult
      }

      val result = activityResult.data?.zettleResult()
      handleRefundResult(result)
    }

    settingsLauncher = activity.registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { _ ->
      // Settings don't return specific results, just report success
      sendResult(true, mapOf("status" to "completed"))
    }

    tippingSettingsLauncher = activity.registerForActivityResult(
      ActivityResultContracts.StartActivityForResult()
    ) { _ ->
      // Settings don't return specific results, just report success
      sendResult(true, mapOf("status" to "completed"))
    }
  }

  override fun onDetachedFromActivityForConfigChanges() {
    Log.d(tag, "onDetachedFromActivityForConfigChanges")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    Log.d(tag, "onReattachedToActivityForConfigChanges")
    activity = binding.activity as ComponentActivity
    registerActivityResultLaunchers()
  }

  override fun onDetachedFromActivity() {
    Log.d(tag, "onDetachedFromActivity")
    // Stop the SDK when detached from activity
    try {
      ZettleSDK.instance?.stop()
    } catch (e: Exception) {
      Log.e(tag, "Error stopping Zettle SDK", e)
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    Log.d(tag, "onMethodCall: ${call.method}")

    // Store the pending operation for async operations
    pendingOperation = PendingOperation(result, call.method)

    when (call.method) {
      "init" -> init(call.arguments as Map<*, *>, result)
      "requestPayment" -> requestPayment(call.arguments as Map<*, *>)
      "requestRefund" -> requestRefund(call.arguments as Map<*, *>)
      "showSettings" -> showSettings()
      "showTippingSettings" -> showTippingSettings()
      "retrieveTransaction" -> retrieveTransaction(call.arguments as Map<*, *>)
      else -> result.notImplemented()
    }
  }

  private fun init(@NonNull args: Map<*, *>, result: Result) {
    try {
      val clientID = args["androidClientId"] as String
      val redirectUrl = args["redirect"] as String
      val isDevMode = args["devMode"] as? Boolean ?: false

      // Create the SDK configuration
      val config = config(context) {
        this.isDevMode = isDevMode
        auth {
          this.clientId = clientID
          this.redirectUrl = redirectUrl
        }
        logging {
          allowWhileRoaming = false
        }
        // Add the features
        addFeature(CardReaderFeature)
      }

      // Configure the SDK
      ZettleSDK.configure(config)

      // Add lifecycle observer
      ProcessLifecycleOwner.get().lifecycle.addObserver(ZettleSDKLifecycle())

      // Return success immediately since init is synchronous
      result.success(mapOf(
        "status" to true,
        "message" to mapOf("initialized" to true),
        "methodName" to "init"
      ))
    } catch (e: Exception) {
      Log.e(tag, "Error initializing Zettle SDK", e)
      result.success(mapOf(
        "status" to false,
        "message" to mapOf("initialized" to false, "errors" to e.message),
        "methodName" to "init"
      ))
    }
  }

  private fun requestPayment(@NonNull args: Map<*, *>) {
    Log.d(tag, "requestPayment: $args")

    val internalUniqueTraceId = args["reference"] as String
    val reference = TransactionReference.Builder(internalUniqueTraceId)
      .build()

    val amount = (((args["amount"] as Double) * 100).toInt()).toLong()
    val enableTipping = args["enableTipping"] as? Boolean ?: true
    val enableInstalments = args["enableInstalments"] as? Boolean ?: false

    // Configure tipping based on the enableTipping parameter
    val tippingConfiguration = if (enableTipping) {
      TippingConfiguration(
        ZettleReaderTippingStyle.Default,
        PayPalReaderTippingStyle.SDKConfigured
      )
    } else {
      TippingConfiguration(
        ZettleReaderTippingStyle.None,
        PayPalReaderTippingStyle.None
      )
    }

    val intent = CardReaderAction.Payment(
      reference = reference,
      amount = amount,
      tippingConfiguration = tippingConfiguration,
      enableInstallments = enableInstalments
    ).charge(activity)

    // Launch using the activity result launcher
    paymentLauncher.launch(intent)
  }

  private fun requestRefund(@NonNull args: Map<*, *>) {
    Log.d(tag, "requestRefund: $args")

    val paymentReferenceId = args["reference"] as String
    val refundReference = TransactionReference.Builder(UUID.randomUUID().toString())
      .put("REFUND_EXTRA_INFO", "Started from Flutter")
      .build()

    val refundAmount = if (args["refundAmount"] != null) {
      (((args["refundAmount"] as Double) * 100).toInt()).toLong()
    } else {
      0L
    }

    val intent = CardReaderAction.Refund(
      amount = refundAmount,
      paymentReferenceId = paymentReferenceId,
      refundReference = refundReference
    ).refund(activity)

    // Launch using the activity result launcher
    refundLauncher.launch(intent)
  }

  private fun showSettings() {
    val intent = CardReaderAction.Settings.show(activity)
    settingsLauncher.launch(intent)
  }

  private fun showTippingSettings() {
    val intent = CardReaderAction.TippingSettings().show(activity)
    tippingSettingsLauncher.launch(intent)
  }

  private fun retrieveTransaction(@NonNull args: Map<*, *>) {
    Log.d(tag, "retrieveTransaction: $args")

    val referenceId = args["reference"] as String

    CardReaderAction.Transaction(referenceId).retrieve { result ->
      when (result) {
        is ZettleResult.Completed<*> -> {
          val transaction = CardReaderAction.fromRetrieveTransactionResult(result)
          val payload = transaction.payload

          sendResult(true, mapOf(
            "status" to "completed",
            "amount" to payload.amount,
            "referenceId" to payload.referenceId
            // Add other fields you need from the payload
          ))
        }
        is ZettleResult.Cancelled -> {
          sendResult(false, mapOf("status" to "canceled"))
        }
        is ZettleResult.Failed -> {
          sendResult(false, mapOf(
            "status" to "failed",
            "reason" to result.reason.toString()
          ))
        }
      }
    }
  }

  private fun handlePaymentResult(result: ZettleResult?) {
    when (result) {
      is ZettleResult.Completed<*> -> {
        val payment: CardPaymentResult.Completed = CardReaderAction.fromPaymentResult(result)
        val payload = payment.payload

        // Create a map with all possible fields
        val resultMap = mutableMapOf<String, Any?>(
          "status" to "completed",
          "amount" to (payload.amount / 100.0),
          "gratuityAmount" to (payload.gratuityAmount?.let { it / 100.0 }),
          "cardType" to payload.cardType,
          "cardPaymentEntryMode" to payload.cardPaymentEntryMode?.toString(),
          "cardholderVerificationMethod" to payload.cardholderVerificationMethod?.toString(),
          "maskedPan" to payload.maskedPan,
          "reference" to payload.reference?.id
        )

        // Add all optional fields that might have changed in the SDK
        payload.transactionId?.let { resultMap["transactionId"] = it }
        payload.tsi?.let { resultMap["tsi"] = it }
        payload.tvr?.let { resultMap["tvr"] = it }
        payload.applicationIdentifier?.let { resultMap["applicationIdentifier"] = it }
        payload.cardIssuingBank?.let { resultMap["cardIssuingBank"] = it }
        payload.panHash?.let { resultMap["panHash"] = it }
        payload.applicationName?.let { resultMap["applicationName"] = it }
        payload.authorizationCode?.let { resultMap["authorizationCode"] = it }
        payload.installmentAmount?.let { resultMap["installmentAmount"] = it / 100.0 }
        payload.nrOfInstallments?.let { resultMap["nrOfInstallments"] = it }
        payload.mxFiid?.let { resultMap["mxFiid"] = it }
        payload.mxCardType?.let { resultMap["mxCardType"] = it }

        sendResult(true, resultMap)
      }
      is ZettleResult.Cancelled -> {
        sendResult(false, mapOf("status" to "canceled"))
      }
      is ZettleResult.Failed -> {
        sendResult(false, mapOf(
          "status" to "failed",
          "reason" to result.reason.toString()
        ))
      }
      else -> {
        sendResult(false, mapOf(
          "status" to "unknown",
          "errors" to "Unknown result type"
        ))
      }
    }
  }

  private fun handleRefundResult(result: ZettleResult?) {
    when (result) {
      is ZettleResult.Completed<*> -> {
        val refund: RefundResult.Completed = CardReaderAction.fromRefundResult(result)
        val payload = refund.payload

        sendResult(true, mapOf(
          "status" to "completed",
          "originalAmount" to (payload.originalAmount / 100.0), // Convert cents back to dollars
          "refundedAmount" to (payload.refundedAmount / 100.0), // Convert cents back to dollars
          "cardType" to payload.cardType,
          "maskedPan" to payload.maskedPan,
          // Add other fields as needed
        ))
      }
      is ZettleResult.Cancelled -> {
        sendResult(false, mapOf("status" to "canceled"))
      }
      is ZettleResult.Failed -> {
        sendResult(false, mapOf(
          "status" to "failed",
          "reason" to result.reason.toString()
        ))
      }
      else -> {
        sendResult(false, mapOf(
          "status" to "unknown",
          "errors" to "Unknown result type"
        ))
      }
    }
  }

  // Helper method to send results back to Flutter
  private fun sendResult(status: Boolean, message: Map<String, Any?>) {
    val operation = pendingOperation
    if (operation != null) {
      pendingOperation = null
      operation.result.success(mapOf(
        "status" to status,
        "message" to message,
        "methodName" to operation.methodName
      ))
      Log.d(tag, "Result sent for ${operation.methodName}: $status")
    } else {
      Log.e(tag, "No pending operation to send result to")
    }
  }

  // Helper extension function to handle Parcelable extras in a version-compatible way
  inline fun <reified T : android.os.Parcelable> Intent.parcelableExtra(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
  }
}