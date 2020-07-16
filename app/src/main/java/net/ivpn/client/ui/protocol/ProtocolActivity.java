package net.ivpn.client.ui.protocol;

import android.content.ClipboardManager;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import net.ivpn.client.IVPNApplication;
import net.ivpn.client.R;
import net.ivpn.client.common.SnackbarUtil;
import net.ivpn.client.common.utils.ToastUtil;
import net.ivpn.client.databinding.ActivityProtocolBinding;
import net.ivpn.client.ui.dialog.DialogBuilder;
import net.ivpn.client.ui.dialog.Dialogs;
import net.ivpn.client.ui.protocol.dialog.WireGuardDetailsDialogListener;
import net.ivpn.client.ui.protocol.port.Port;
import net.ivpn.client.ui.protocol.port.PortAdapter;
import net.ivpn.client.vpn.Protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class ProtocolActivity extends AppCompatActivity implements ProtocolNavigator, WireGuardDetailsDialogListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolActivity.class);
    private ActivityProtocolBinding binding;
    @Inject
    ProtocolViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        IVPNApplication.getApplication().appComponent.provideActivityComponent().create().inject(this);
        super.onCreate(savedInstanceState);
        LOGGER.info("onCreate");
        init();
        initToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        if (getSupportActionBar() == null) {
            return;
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.protocol_action_bar_title);
    }

    private void init() {
        viewModel.setNavigator(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_protocol);
        binding.contentLayout.setViewmodel(viewModel);
        PortAdapter openVpnPortAdapter = new PortAdapter(this, R.layout.port_item, Port.valuesFor(Protocol.OPENVPN));
        binding.contentLayout.protocolSettings.openvpnSpinner.setAdapter(openVpnPortAdapter);
        PortAdapter wgVpnPortAdapter = new PortAdapter(this, R.layout.port_item, Port.valuesFor(Protocol.WIREGUARD));
        binding.contentLayout.protocolSettings.wgSpinner.setAdapter(wgVpnPortAdapter);
    }

    public void wgDetails(View view) {
        DialogBuilder.createWireGuardDetailsDialog(this, viewModel.getWireGuardInfo(), this);
    }

    @Override
    public void notifyUser(int msgId, int actionId, View.OnClickListener listener) {
        SnackbarUtil.show(binding.coordinator, msgId, actionId, listener);
    }

    @Override
    public void openDialogueError(Dialogs dialog) {
        DialogBuilder.createNotificationDialog(this, dialog);
    }

    @Override
    public void openCustomDialogueError(String title, String message) {
        DialogBuilder.createFullCustomNotificationDialog(this, title, message);
    }

    @Override
    public void reGenerateKeys() {
        LOGGER.info("Regenerating WireGuard keys");
        viewModel.reGenerateKeys();
    }

    @Override
    public void copyPublicKeyToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        viewModel.copyWgKeyToClipboard(clipboard);
        ToastUtil.toast(R.string.protocol_wg_public_key_copied);
    }

    @Override
    public void copyIpAddressToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        viewModel.copyWgIpToClipboard(clipboard);
        ToastUtil.toast(R.string.protocol_wg_ip_address_copied);
    }
}