package net.ivpn.client.ui.privateemails;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.SnackbarUtil;
import net.ivpn.client.common.utils.LogUtil;
import net.ivpn.client.databinding.ActivityPrivateEmailsBinding;
import net.ivpn.client.rest.data.privateemails.Email;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.privateemails.edit.EditPrivateEmailActivity;
import net.ivpn.client.ui.privateemails.edit.PrivateEmailAction;

import javax.inject.Inject;

public class PrivateEmailsActivity extends AppCompatActivity implements PrivateEmailsNavigator{

    @Inject
    PrivateEmailsViewModel viewModel;
    private ActivityPrivateEmailsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        init();
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
            return false;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() == null) return;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.private_emails_title);
    }

    private void init() {
        viewModel.setNavigator(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_private_emails);
        binding.setViewmodel(viewModel);
        binding.contentLayout.setViewmodel(viewModel);

        PrivateEmailsRecyclerViewAdapter adapter = new PrivateEmailsRecyclerViewAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.contentLayout.recyclerView.setLayoutManager(layoutManager);
        binding.contentLayout.recyclerView.setAdapter(adapter);
        binding.contentLayout.recyclerView.setEmptyView(findViewById(R.id.empty_view));

        binding.fab.setOnClickListener(view -> viewModel.generatePrivateEmail());
    }

    @Override
    public void copyToClipboardEmail(Email email) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("email", email.getEmail());
        clipboard.setPrimaryClip(clip);
        SnackbarUtil.show(binding.coordinator, R.string.private_emails_email_copied, R.string.action_settings, null);
    }

    @Override
    public void editEmail(Email email) {
        Intent intent = new Intent(this, EditPrivateEmailActivity.class);
        intent.putExtra(EditPrivateEmailActivity.EMAIL, email);
        intent.putExtra(EditPrivateEmailActivity.ACTION_TYPE, PrivateEmailAction.EDIT);
        startActivity(intent);
    }

    @Override
    public void onEmailAdded(Email email) {
        Intent intent = new Intent(this, EditPrivateEmailActivity.class);
        intent.putExtra(EditPrivateEmailActivity.EMAIL, email);
        intent.putExtra(EditPrivateEmailActivity.ACTION_TYPE, PrivateEmailAction.GENERATE);
        startActivity(intent);
    }

    @Override
    public void openErrorDialogue(Dialogs dialog) {
        DialogBuilder.createNotificationDialog(this, dialog);
    }

    @Override
    public void openNewFeatureDialog(PrivateEmailActionListener listener) {
        DialogBuilder.createPrivateEmailNewFeatureDialog(this, listener);
    }
}