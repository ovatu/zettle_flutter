enum ZettlePluginPaymentStatus { completed, canceled, failed }

/// Payment response object.
///
/// Contains all the transaction informations.
/// Some fields are available for Android only.
class ZettlePluginPaymentResponse {
  ZettlePluginPaymentResponse(
      {required this.status,
      this.amount,
      this.gratuityAmount,
      this.cardType,
      this.cardPaymentEntryMode,
      this.cardholderVerificationMethod,
      this.tsi,
      this.tvr,
      this.applicationIdentifier,
      this.cardIssuingBank,
      this.maskedPan,
      this.panHash,
      this.applicationName,
      this.authorizationCode,
      this.installmentAmount,
      this.nrOfInstallments,
      this.mxFiid,
      this.mxCardType,
      this.reference});

  ZettlePluginPaymentResponse.fromMap(Map<dynamic, dynamic> response) {
    switch (response['status']) {
      case "completed":
        status = ZettlePluginPaymentStatus.completed;
        break;
      case "canceled":
        status = ZettlePluginPaymentStatus.canceled;
        break;
      case "failed":
      default:
        status = ZettlePluginPaymentStatus.failed;
    }

    amount = response['amount'];
    gratuityAmount = response['gratuityAmount'];
    cardType = response['cardType'];
    cardPaymentEntryMode = response['cardPaymentEntryMode'];
    cardholderVerificationMethod = response['cardholderVerificationMethod'];
    tsi = response['tsi'];
    tvr = response['tvr'];
    applicationIdentifier = response['applicationIdentifier'];
    cardIssuingBank = response['cardIssuingBank'];
    maskedPan = response['maskedPan'];
    panHash = response['panHash'];
    applicationName = response['applicationName'];
    authorizationCode = response['authorizationCode'];
    installmentAmount = response['installmentAmount'];
    nrOfInstallments = response['nrOfInstallments'];
    mxFiid = response['mxFiid'];
    mxCardType = response['mxCardType'];
    reference = response['reference'];
  }

  late ZettlePluginPaymentStatus status;
  late int? amount;
  late int? gratuityAmount;
  late String? cardType;
  late String? cardPaymentEntryMode;
  late String? cardholderVerificationMethod;
  late String? tsi;
  late String? tvr;
  late String? applicationIdentifier;
  late String? cardIssuingBank;
  late String? maskedPan;
  late String? panHash;
  late String? applicationName;
  late String? authorizationCode;
  late String? installmentAmount;
  late String? nrOfInstallments;
  late String? mxFiid;
  late String? mxCardType;
  late String? reference;

  @override
  String toString() {
    return 'status: $status, amount: $amount, gratuityAmount: $gratuityAmount, cardType: $cardType, cardPaymentEntryMode: $cardPaymentEntryMode';
  }
}
