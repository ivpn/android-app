package net.ivpn.client.ui.privateemails.edit;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.todtenkopf.mvvm.MenuCommandBindings;
import com.todtenkopf.mvvm.ViewModelActivity;
import com.todtenkopf.mvvm.ViewModelBase;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.SnackbarUtil;
import net.ivpn.client.databinding.ActivityEditPrivateEmailBinding;
import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class EditPrivateEmailActivity extends ViewModelActivity implements EditPrivateEmailNavigator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditPrivateEmailActivity.class);
    public static final String EMAIL = "EMAIL";
    public static final String ACTION_TYPE = "ACTION_TYPE";

    private Email email = null;
    private PrivateEmailAction action;
    private ActivityEditPrivateEmailBinding binding;
    @Inject
    EditPrivateEmailViewModel viewModel;

    @Nullable
    @Override
    protected ViewModelBase createViewModel() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getParcelable(EMAIL);
            action = (PrivateEmailAction) bundle.getSerializable(ACTION_TYPE);
        }
        viewModel.setNavigator(this);
        viewModel.setEmail(email);
        viewModel.setAction(action);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_private_email);
        binding.setViewmodel(viewModel);
        binding.contentLayout.setViewmodel(viewModel);

        addMenuBinding(R.id.action_delete, viewModel.removeCommand, MenuCommandBindings.EnableBinding.Visible);
        addMenuBinding(R.id.action_edit, viewModel.editCommand, MenuCommandBindings.EnableBinding.Visible);
        addMenuBinding(R.id.action_done, viewModel.doneCommand, MenuCommandBindings.EnableBinding.Visible);

        return viewModel;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        initToolbar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_private_email, menu);
        return true;
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.private_emails_generate_title);
    }

    @Override
    public void toEmailsList() {
        finish();
    }

    @Override
    public void tryRemoveEmail() {
        DialogBuilder.createOptionDialog(this, Dialogs.REMOVE_EMAIL,
                (dialogInterface, i) -> viewModel.removePrivateEmail());
    }

    @Override
    public void openErrorDialogue(Dialogs dialogs) {
        DialogBuilder.createNotificationDialog(this, dialogs);
    }

    public void copyToClipBoard(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("email", email.getEmail());
        clipboard.setPrimaryClip(clip);
        SnackbarUtil.show(binding.coordinator, R.string.private_emails_email_copied, R.string.action_settings, null);
    }
}
