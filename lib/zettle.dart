import 'dart:async';

import 'package:flutter/services.dart';

import 'models/zettle_payment_request.dart';
import 'models/zettle_refund_request.dart';
import 'models/zettle_plugin_payment_response.dart';
import 'models/zettle_plugin_refund_response.dart';
import 'models/zettle_plugin_info_response.dart';
import 'models/zettle_plugin_response.dart';

export 'models/zettle_payment_request.dart';
export 'models/zettle_plugin_payment_response.dart';
export 'models/zettle_refund_request.dart';
export 'models/zettle_plugin_refund_response.dart';
export 'models/zettle_plugin_info_response.dart';
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

  /*static Future<void> _throwIfNotLoggedIn() async {
    final isLogged = await isLoggedIn;
    if (isLogged == null || !isLogged) {
      throw Exception('Not logged in. You must login before.');
    }
  }*/

  /// Initializes Zettle SDK with your [affiliateKey].
  ///
  /// Must be called before anything else
  static Future<ZettlePluginResponse> init(
      String iosClientId, String androidClientId, String redirectUrl) async {
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

  /*
  /// Shows Zettle login dialog.
  ///
  /// Should be called after [init].
  static Future<ZettlePluginResponse> login() async {
    _throwIfNotInitialized();
    final method = await _channel.invokeMethod('login');
    return ZettlePluginResponse.fromMap(method);
  }

  /// Uses Transparent authentication to login to Zettle SDK with supplied token.
  ///
  /// Should be called after [init].
  static Future<ZettlePluginResponse> loginWithToken(String token) async {
    _throwIfNotInitialized();
    final method = await _channel.invokeMethod('loginWithToken', token);
    return ZettlePluginResponse.fromMap(method);
  }

  /// Returns whether merchant is already logged in.
  static Future<bool?> get isLoggedIn async {
    _throwIfNotInitialized();
    final method = await _channel.invokeMethod('loggedIn');
    return ZettlePluginResponse.fromMap(method).status;
  }

  /// Returns the current merchant.
  static Future<ZettlePluginInfoResponse> get info async {
    _throwIfNotInitialized();
    await _throwIfNotLoggedIn();
    final method = await _channel.invokeMethod('info');
    final response = ZettlePluginResponse.fromMap(method);
    return ZettlePluginInfoResponse.fromMap(response.message!);
  }
  */

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
