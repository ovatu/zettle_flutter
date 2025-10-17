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

    originalAmount = double.tryParse(response['originalAmount']?.toString() ?? '');
    refundedAmount = double.tryParse(response['refundedAmount']?.toString() ?? '');
    cardType = response['cardType']?.toString();
    maskedPan = response['maskedPan']?.toString();
    cardPaymentUUID = response['cardPaymentUUID']?.toString();
  }

  /// Transaction's outcome
  late ZettlePluginRefundStatus status;

  late double? originalAmount;
  late double? refundedAmount;
  late String? cardType;
  late String? maskedPan;
  late String? cardPaymentUUID;

  @override
  String toString() {
    return 'Success: $status, originalAmount: $originalAmount, refundedAmount: $refundedAmount, cardType: $cardType, cardType: $maskedPan, cardType: $cardPaymentUUID';
  }
}
