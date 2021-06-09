package net.ivpn.client.v2.subscription.monthly;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ivpn.client.R;
import net.ivpn.client.databinding.FragmentMonthlySubscriptionBinding;
import net.ivpn.client.v2.subscription.SubscriptionActivity;
import net.ivpn.client.v2.subscription.SubscriptionViewModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class MonthlySubscriptionFragment extends Fragment {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlySubscriptionFragment.class);

    private FragmentMonthlySubscriptionBinding binding;
    @Inject
    SubscriptionViewModel viewmodel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((SubscriptionActivity) context).activityComponent.inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_monthly_subscription, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init() {
        binding.setViewmodel(viewmodel);
    }
}