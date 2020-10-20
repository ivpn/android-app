package net.ivpn.client.common.bindings;

/*
 IVPN Android app
 https://github.com/ivpn/android-app
 <p>
 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.
 <p>
 This file is part of the IVPN Android app.
 <p>
 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.
 <p>
 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.
 <p>
 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/


import android.content.Context;
import androidx.databinding.BindingAdapter;

import android.graphics.Bitmap;
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

    @BindingAdapter("bitmap")
    public static void setImageBitmap(ImageView imageView, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }

        imageView.setImageBitmap(bitmap);
    }
}