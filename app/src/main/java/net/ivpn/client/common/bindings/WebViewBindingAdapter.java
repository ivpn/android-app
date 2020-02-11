package net.ivpn.client.common.bindings;

import android.databinding.BindingAdapter;
import android.webkit.WebView;

public class WebViewBindingAdapter {

    @BindingAdapter("app:url")
    public static void setWebViewUrl(WebView view, String url) {
        if (view != null) {
            view.loadUrl(url);
        }
    }
}
