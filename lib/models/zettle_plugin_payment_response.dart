enum ZettlePluginPaymentStatus { completed, canceled, failed }

/// Payment response object.
///
/// Contains all the transaction informations.
/// Some fields are available for Android only.
class ZettlePluginPaymentResponse {
  ZettlePluginPaymentResponse(
      {required this.status,
      this.transactionId,
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

    transactionId = response['transactionId']?.toString();
    amount = double.tryParse(response['amount']?.toString() ?? '');
    gratuityAmount = double.tryParse(response['gratuityAmount'];
    cardType = response['cardType']?.toString();
    cardPaymentEntryMode = response['cardPaymentEntryMode']?.toString();
    cardholderVerificationMethod = response['cardholderVerificationMethod']?.toString();
    tsi = response['tsi']?.toString();
    tvr = response['tvr']?.toString();
    applicationIdentifier = response['applicationIdentifier']?.toString();
    cardIssuingBank = response['cardIssuingBank']?.toString();
    maskedPan = response['maskedPan']?.toString();
    panHash = response['panHash']?.toString();
    applicationName = response['applicationName']?.toString();
    authorizationCode = response['authorizationCode']?.toString();
    installmentAmount = double.tryParse(response['installmentAmount']?.toString() ?? '');
    nrOfInstallments = int.tryParse(response['nrOfInstallments']?.toString() ?? '');
    mxFiid = response['mxFiid']?.toString();
    mxCardType = response['mxCardType']?.toString();
    reference = response['reference']?.toString();
  }

  late ZettlePluginPaymentStatus status;
  late String? transactionId;
  late double? amount;
  late double? gratuityAmount;
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
  late double? installmentAmount;
  late int? nrOfInstallments;
  late String? mxFiid;
  late String? mxCardType;
  late String? reference;

  @override
  String toString() {
    return 'status: $status, amount: $amount, gratuityAmount: $gratuityAmount, cardType: $cardType, cardPaymentEntryMode: $cardPaymentEntryMode';
  }
}
