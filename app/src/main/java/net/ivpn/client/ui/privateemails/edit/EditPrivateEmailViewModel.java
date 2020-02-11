package net.ivpn.client.ui.privateemails.edit;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.util.Log;

import com.todtenkopf.mvvm.ViewModelBase;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.prefs.ServersRepository;
import net.ivpn.client.common.prefs.Settings;
import net.ivpn.client.common.prefs.UserPreference;
import net.ivpn.client.rest.HttpClientFactory;
import net.ivpn.client.rest.RequestListener;
import net.ivpn.client.rest.Responses;
import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.rest.data.privateemails.RemovePrivateEmailRequestBody;
import net.ivpn.client.rest.data.privateemails.RemovePrivateEmailResponse;
import net.ivpn.client.rest.data.privateemails.UpdatePrivateEmailRequestBody;
import net.ivpn.client.rest.data.privateemails.UpdatePrivateEmailResponse;
import net.ivpn.client.rest.requests.common.Request;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;

import java.net.UnknownHostException;

import javax.inject.Inject;

public class EditPrivateEmailViewModel extends ViewModelBase {

    private static final String TAG = EditPrivateEmailViewModel.class.getSimpleName();

    public final ObservableBoolean updatingData = new ObservableBoolean();
    public final ObservableField<Email> email = new ObservableField<>();
    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableBoolean editable = new ObservableBoolean();

    CommandVM editCommand = new CommandVM() {
        @Override
        public void execute() {
            setAction(PrivateEmailAction.GENERATE);
            refreshCommands();
        }

        @Override
        public void refresh() {
            isEnabled(action.equals(PrivateEmailAction.EDIT));
        }
    };

    CommandVM removeCommand = new CommandVM() {
        @Override
        public void execute() {
            navigator.tryRemoveEmail();
        }

        @Override
        public void refresh() {
            isEnabled(action.equals(PrivateEmailAction.EDIT));
        }
    };

    CommandVM doneCommand = new CommandVM() {
        @Override
        public void execute() {
            updatePrivateEmail();
        }

        @Override
        public void refresh() {
            isEnabled(action.equals(PrivateEmailAction.GENERATE));
        }
    };

    private EditPrivateEmailNavigator navigator;
    private PrivateEmailAction action;
    private UserPreference userPreference;

    private Request<RemovePrivateEmailResponse> removeEmailRequest;
    private Request<UpdatePrivateEmailResponse> updateEmailRequest;

    @Inject
    EditPrivateEmailViewModel(Settings settings, HttpClientFactory clientFactory,
                              UserPreference userPreference, ServersRepository serversRepository) {
        this.userPreference = userPreference;
        removeEmailRequest = new Request<>(settings, clientFactory, serversRepository, Request.Duration.SHORT);
        updateEmailRequest = new Request<>(settings, clientFactory, serversRepository, Request.Duration.SHORT);
    }

    public void setEmail(Email email) {
        this.email.set(email);
    }

    public void setNavigator(EditPrivateEmailNavigator navigator) {
        this.navigator = navigator;
    }

    public void setAction(PrivateEmailAction action) {
        this.action = action;
        if (action.equals(PrivateEmailAction.EDIT)) {
            title.set(IVPNApplication.getApplication().getString(R.string.private_emails_edit_title));
            editable.set(false);
        } else {
            title.set(IVPNApplication.getApplication().getString(R.string.private_emails_generate_title));
            editable.set(true);
        }

        refreshCommands();
    }

    void removePrivateEmail() {
        updatingData.set(true);
        removePrivateEmail(email.get(), new RequestListener<RemovePrivateEmailResponse>() {
            @Override
            public void onSuccess(RemovePrivateEmailResponse response) {
                Log.d(TAG, "onSuccess: ");
                updatingData.set(false);
                if (response.getStatus() == Responses.SUCCESS) {
                    navigator.toEmailsList();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                updatingData.set(false);

                if (throwable instanceof UnknownHostException) {
                    navigator.openErrorDialogue(Dialogs.CONNECTION_ERROR);
                }
            }

            @Override
            public void onError(String string) {
                updatingData.set(false);
            }
        });
    }

    private void updatePrivateEmail() {
        updatingData.set(true);
        updatePrivateEmail(email.get().getEmail(), email.get().getNote(), new RequestListener<UpdatePrivateEmailResponse>() {
            @Override
            public void onSuccess(UpdatePrivateEmailResponse response) {
                Log.d(TAG, "onSuccess: ");
                updatingData.set(false);
                if (response.getStatus() == Responses.SUCCESS) {
                    navigator.toEmailsList();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                updatingData.set(false);

                if (throwable instanceof UnknownHostException) {
                    navigator.openErrorDialogue(Dialogs.CONNECTION_ERROR);
                }
            }

            @Override
            public void onError(String string) {
                updatingData.set(false);
            }
        });
    }

    private void removePrivateEmail(Email email, RequestListener listener) {
        String token = getToken();
        RemovePrivateEmailRequestBody requestBody;
        if (token != null && !token.isEmpty()) {
            requestBody = new RemovePrivateEmailRequestBody(token, email.getEmail());
            removeEmailRequest.start(api -> api.removePrivateEmail(requestBody), listener);
        }
    }

    private void updatePrivateEmail(String email, String note, RequestListener listener) {
        String token = getToken();
        UpdatePrivateEmailRequestBody requestBody;
        if (token != null && !token.isEmpty()) {
            requestBody = new UpdatePrivateEmailRequestBody(token, email, note);
            updateEmailRequest.start(api -> api.updatePrivateEmail(requestBody), listener);
        }
    }

    private String getToken() {
        return userPreference.getSessionToken();
    }
}