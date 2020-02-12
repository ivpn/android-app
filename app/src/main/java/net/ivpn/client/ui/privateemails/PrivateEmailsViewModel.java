package net.ivpn.client.ui.privateemails;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableList;

import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.rest.data.privateemails.GenerateEmailRequestBody;
import net.ivpn.client.rest.data.privateemails.GenerateEmailResponse;
import net.ivpn.client.rest.data.privateemails.PrivateEmailsListRequestBody;
import net.ivpn.client.rest.data.privateemails.PrivateEmailsListResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.ui.dialog.Dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.UnknownHostException;

import javax.inject.Inject;

public class PrivateEmailsViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateEmailsViewModel.class);

    public final ObservableBoolean dataLoading = new ObservableBoolean();
    public final ObservableBoolean updatingData = new ObservableBoolean();
    public final ObservableList<Email> emails = new ObservableArrayList<>();
    private final ObservableBoolean isNewForPrivateEmails = new ObservableBoolean();

    private PrivateEmailsNavigator navigator;

    private Request<PrivateEmailsListResponse> emailListRequest;
    private Request<GenerateEmailResponse> generateEmailRequest;
    private Settings settings;
    private UserPreference userPreference;

    @Inject
    PrivateEmailsViewModel(Settings settings, UserPreference userPreference,
                           HttpClientFactory httpClientFactory, ServersRepository serversRepository) {
        this.settings = settings;
        this.userPreference = userPreference;
        emailListRequest = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.SHORT);
        generateEmailRequest = new Request<>(settings, httpClientFactory, serversRepository, Request.Duration.SHORT);
    }

    public void setNavigator(PrivateEmailsNavigator navigator) {
        this.navigator = navigator;
    }

    public void start() {
        dataLoading.set(true);
        isNewForPrivateEmails.set(isNewForPrivateEmails());
        if (isNewForPrivateEmails.get()) {
            navigator.openNewFeatureDialog(getPrivateEmailActionListener());
        }
        loadEmails(new RequestListener<PrivateEmailsListResponse>() {
            @Override
            public void onSuccess(PrivateEmailsListResponse response) {
                LOGGER.info("Successfully loaded private emails list, response = " + response);
                dataLoading.set(false);
                if (response.getStatus() == Responses.SUCCESS) {
                    PrivateEmailsViewModel.this.emails.clear();
                    PrivateEmailsViewModel.this.emails.addAll(response.getEmails());
                }
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("ERROR while loading private emails: " + throwable);
                dataLoading.set(false);

                if (throwable instanceof UnknownHostException) {
                    navigator.openErrorDialogue(Dialogs.CONNECTION_ERROR);
                }
            }

            @Override
            public void onError(String error) {
                LOGGER.error("ERROR while loading private emails: " + error);
                dataLoading.set(false);
            }
        });
    }


    void generatePrivateEmail() {
        updatingData.set(true);
        generatePrivateEmail(new RequestListener<GenerateEmailResponse>() {
            @Override
            public void onSuccess(GenerateEmailResponse response) {
                LOGGER.info("Successfully generated private email, response = " + response);
                updatingData.set(false);
                if (response.getStatus() == Responses.SUCCESS) {
                    navigator.onEmailAdded(new Email(response.getGenerated(), null));
                }
            }

            @Override
            public void onError(Throwable throwable) {
                LOGGER.error("ERROR while generating private email: " + throwable);
                updatingData.set(false);

                if (throwable instanceof UnknownHostException) {
                    navigator.openErrorDialogue(Dialogs.CONNECTION_ERROR);
                }
            }

            @Override
            public void onError(String error) {
                LOGGER.error("ERROR while generating private email: " + error);
                updatingData.set(false);
            }
        });
    }

    private void loadEmails(RequestListener listener) {
        String token = getToken();
        PrivateEmailsListRequestBody requestBody;

        if (token != null && !token.isEmpty()) {
            requestBody = new PrivateEmailsListRequestBody(token);
            emailListRequest.start(api -> api.getPrivateEmails(requestBody), listener);
        }
    }

    private void generatePrivateEmail(RequestListener listener) {
        String token = getToken();
        GenerateEmailRequestBody requestBody;
        if (token != null && !token.isEmpty()) {
            requestBody = new GenerateEmailRequestBody(token);
            generateEmailRequest.start(api -> api.generatePrivateEmail(requestBody), listener);
        }
    }

    private void setIsNewForPrivateEmails() {
        settings.setIsNewForPrivateEmails(false);
    }

    private boolean isNewForPrivateEmails() {
        return settings.isNewForPrivateEmails();
    }

    private String getToken() {
        return userPreference.getSessionToken();
    }

    private PrivateEmailActionListener getPrivateEmailActionListener() {
        return this::setIsNewForPrivateEmails;
    }
}