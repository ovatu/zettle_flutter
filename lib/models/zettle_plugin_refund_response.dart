import 'dart:io';

enum ZettlePluginRefundStatus { completed, canceled, failed }

/// Refund response object.
///
/// Contains all the transaction informations.
/// Some fields are available for Android only.
class ZettlePluginRefundResponse {
  ZettlePluginRefundResponse(
      {required this.status,
      this.originalAmount,
      this.refundedAmount,
      this.cardType,
      this.maskedPan,
      this.cardPaymentUUID});

  ZettlePluginRefundResponse.fromMap(Map<dynamic, dynamic> response) {
    switch (response['status']) {
      case "completed":
        status = ZettlePluginRefundStatus.completed;
        break;
      case "canceled":
        status = ZettlePluginRefundStatus.canceled;
        break;
      case "failed":
      default:
        status = ZettlePluginRefundStatus.failed;
    }

    originalAmount = response['originalAmount'];
    refundedAmount = response['refundedAmount'];
    cardType = response['cardType'];
    maskedPan = response['maskedPan'];
    cardPaymentUUID = response['cardPaymentUUID'];
  }

  /// Transaction's outcome
  late ZettlePluginRefundStatus status;

  late int? originalAmount;
  late int? refundedAmount;
  late String? cardType;
  late String? maskedPan;
  late String? cardPaymentUUID;

  @override
  String toString() {
    return 'Success: $status, originalAmount: $originalAmount, refundedAmount: $refundedAmount, cardType: $cardType, cardType: $maskedPan, cardType: $cardPaymentUUID';
  }
}
