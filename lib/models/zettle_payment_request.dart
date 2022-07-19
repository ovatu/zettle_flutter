/// Payment object.
class ZettlePaymentRequest {
  ZettlePaymentRequest(
      {required this.amount,
      required this.reference,
      this.enableLogin = true,
      this.enableTipping = true,
      this.enableInstalments = true});

  /// Total payment amount.
  double amount;

  /// An identifier associated with the transaction that can be used to retrieve details related to the transaction.
  String reference;

  /// An identifier associated with the transaction that can be used to retrieve details related to the transaction.
  bool enableLogin;

  /// An identifier associated with the transaction that can be used to retrieve details related to the transaction.
  bool enableTipping;

  /// An identifier associated with the transaction that can be used to retrieve details related to the transaction.
  bool enableInstalments;

  Map<String, dynamic> toMap() => {
        'amount': amount,
        'reference': reference,
        'enableLogin': enableLogin,
        'enableTipping': enableTipping,
        'enableInstalments': enableInstalments
      };
}
