package net.ivpn.client.ui.billing;

public interface BillingNavigator {

    void onSuccessBilling();

    void onCredentialsError();

    void onPurchaseAlreadyDone();

    void createPurchaseErrorDialog(String errorCode, String errorMessage);

    void onAccountCreated();

    void onAddFundsFinish();
}
