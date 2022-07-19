/// Refund object.
class ZettleRefundRequest {
  ZettleRefundRequest({
    this.refundAmount,
    this.taxAmount,
    required this.reference,
    this.receiptNumber,
  });

  /// Total refund amount.
  double? refundAmount;

  /// Total refund amount.
  double? taxAmount;

  /// An identifier associated with the transaction that can be used to retrieve details related to the transaction.
  String reference;

  /// An identifier associated with the transaction that can be used to retrieve details related to the transaction.
  String? receiptNumber;

  Map<String, dynamic> toMap() => {
        'refundAmount': refundAmount,
        'taxAmount': taxAmount,
        'reference': reference,
        'receiptNumber': receiptNumber
      };
}
