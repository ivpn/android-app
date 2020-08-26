package net.ivpn.client.v2.qr

import android.view.View
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.CompoundBarcodeView
import net.ivpn.client.R

class QRActivity: CaptureActivity() {

    override fun initializeContent(): CompoundBarcodeView {
        setContentView(R.layout.fragment_qr_reader)
        return (findViewById<View>(R.id.scanner) as CompoundBarcodeView)
    }
}