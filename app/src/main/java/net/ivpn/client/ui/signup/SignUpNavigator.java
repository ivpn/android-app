package net.ivpn.client.ui.signup;

public interface SignUpNavigator {
    void onSignUp();

    void onEmailFormatError();

    void onError(String errorCode, String errorMessage);
}
