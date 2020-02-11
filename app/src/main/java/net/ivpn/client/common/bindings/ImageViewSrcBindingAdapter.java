package net.ivpn.client.common.bindings;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

import net.ivpn.client.ui.split.data.ApplicationItem;
import net.ivpn.client.rest.data.model.Server;

public class ImageViewSrcBindingAdapter {
    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, String imageUri) {
        if (imageUri == null) {
            view.setImageURI(null);
        } else {
            view.setImageURI(Uri.parse(imageUri));
        }
    }

    @BindingAdapter("android:src")
    public static void setImageUri(ImageView view, Uri imageUri) {
        view.setImageURI(imageUri);
    }

    @BindingAdapter("android:src")
    public static void setImageDrawable(ImageView view, Drawable drawable) {
        view.setImageDrawable(drawable);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, Server server) {
        if (server == null) return;

        Context context = imageView.getContext();
        String countryCode = server.getCountryCode();
        if (countryCode.equalsIgnoreCase("uk")) {
            countryCode = "gb";
        }
        String path = "flag" + File.separator
                + countryCode.toLowerCase() + ".png";
        Drawable drawable;
        try {
            drawable = Drawable.createFromStream(context.getAssets().open(path), null);
            imageView.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, ApplicationItem info) {
        if (info == null) return;

        imageView.setImageDrawable(info.getIcon());
    }

    @BindingAdapter("android:src")
    public static void setImageResource(ImageView imageView, String countryCode) {
        if (countryCode == null) {
            return;
        }

        Context context = imageView.getContext();
        if (countryCode.equalsIgnoreCase("uk")) {
            countryCode = "gb";
        }
        String path = "flag" + File.separator
                + countryCode.toLowerCase() + ".png";
        Drawable drawable;
        try {
            drawable = Drawable.createFromStream(context.getAssets().open(path), null);
            imageView.setImageDrawable(drawable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}