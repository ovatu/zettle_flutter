import 'dart:async';

import 'package:flutter/services.dart';

import 'models/zettle_payment_request.dart';
import 'models/zettle_refund_request.dart';
import 'models/zettle_plugin_payment_response.dart';
import 'models/zettle_plugin_refund_response.dart';
import 'models/zettle_plugin_response.dart';

export 'models/zettle_payment_request.dart';
export 'models/zettle_plugin_payment_response.dart';
export 'models/zettle_refund_request.dart';
export 'models/zettle_plugin_refund_response.dart';
export 'models/zettle_plugin_response.dart';

class Zettle {
  static const MethodChannel _channel = MethodChannel('zettle');

  static bool _isInitialized = false;

  static void _throwIfNotInitialized() {
    if (!_isInitialized) {
      throw Exception(
          'Zettle SDK is not initialized. You should call Zettle.init(iosClientId, androidClientId, redirectUrl)');
    }
  }

  /// Initializes Zettle SDK with your [affiliateKey].
  ///
  /// Must be called before anything else
  static Future<ZettlePluginResponse> init(
      String iosClientId, String androidClientId, String redirectUrl) async {
    if (_isInitialized) {
      throw Exception(
          'Zettle SDK is already initialized. You should only call Zettle.init() once');
    }

    final method = await _channel.invokeMethod('init', {
      'iosClientId': iosClientId,
      'androidClientId': androidClientId,
      'redirect': redirectUrl
    });
    final response = ZettlePluginResponse.fromMap(method);
    if (response.status) {
      _isInitialized = true;
    }
    return response;
  }

  /// Starts a payment process with [paymentRequest].
  static Future<ZettlePluginPaymentResponse> requestPayment(
      ZettlePaymentRequest paymentRequest) async {
    _throwIfNotInitialized();
    final request = paymentRequest.toMap();
    final method = await _channel.invokeMethod('requestPayment', request);
    final response = ZettlePluginResponse.fromMap(method);
    return ZettlePluginPaymentResponse.fromMap(response.message!);
  }

  /// Starts a refund process with [refundRequest].
  static Future<ZettlePluginRefundResponse> requestRefund(
      ZettleRefundRequest refundRequest) async {
    _throwIfNotInitialized();
    final request = refundRequest.toMap();
    final method = await _channel.invokeMethod('requestRefund', request);
    final response = ZettlePluginResponse.fromMap(method);
    return ZettlePluginRefundResponse.fromMap(response.message!);
  }

  /// Shows the settings screen.
  static void showSettings() {
    _throwIfNotInitialized();
    _channel.invokeMethod('showSettings');
  }
}
