package net.ivpn.core.v2.connect.createSession;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2023 IVPN Limited.

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
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.ivpn.core.R;
import net.ivpn.core.databinding.BottomSheetBinding;
import net.ivpn.core.databinding.BottomSheetDmProBinding;
import net.ivpn.core.databinding.BottomSheetLegacyStandardBinding;
import net.ivpn.core.databinding.BottomSheetProBinding;
import net.ivpn.core.databinding.BottomSheetDmStandardBinding;
import net.ivpn.core.databinding.BottomSheetStandardBinding;
import net.ivpn.core.rest.data.session.SessionErrorResponse;
import net.ivpn.core.v2.login.LoginFragment;
import net.ivpn.core.common.billing.addfunds.Plan;

import java.util.Objects;

public class CreateSessionFragment extends BottomSheetDialogFragment {

    private CreateSessionNavigator navigator;

    private final SessionErrorResponse error;

    public CreateSessionFragment(SessionErrorResponse error) {
        this.error = error;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof LoginFragment) {
            navigator = (CreateSessionNavigator) getParentFragment();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Plan plan = Plan.Companion.getPlanByProductName(Objects.requireNonNull(error.getData()).getCurrentPlan());
        boolean deviceManagement = Objects.requireNonNull(error.getData()).getDeviceManagement();
        boolean isAccountNewStyle = Objects.requireNonNull(error.getData()).getPaymentMethod().equals("prepaid");

        // Device Management enabled, Pro plan
        if (deviceManagement && plan.equals(Plan.PRO) && isAccountNewStyle) {
            return getDmProBinding(inflater, container);
        }

        // Device Management disabled, Pro plan
        if (!deviceManagement && plan.equals(Plan.PRO) && isAccountNewStyle) {
            return getProBinding(inflater, container);
        }

        // Device Management enabled, Standard plan
        if (deviceManagement && plan.equals(Plan.STANDARD) && isAccountNewStyle) {
            return getDmStandardBinding(inflater, container);
        }

        // Device Management disabled, Standard plan
        if (!deviceManagement && plan.equals(Plan.STANDARD) && isAccountNewStyle) {
            return getStandardBinding(inflater, container);
        }

        // Legacy account, Standard plan
        if (plan.equals(Plan.STANDARD)) {
            return getLegacyStandardBinding(inflater, container);
        }

        // Legacy account, Pro plan
        return getDefaultBinding(inflater, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commit();
        } catch (IllegalStateException e) {
            Log.d("CreateSessionFragment", "show exception: " + e);
        }
    }

    private View getDefaultBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        BottomSheetBinding binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet, container, false);
        binding.forceLogout.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.onForceLogout();
            }
        });
        binding.tryAgain.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.tryAgain();
            }
        });
        binding.close.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.cancel();
            }
        });
        return binding.getRoot();
    }

    private View getLegacyStandardBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        BottomSheetLegacyStandardBinding binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_legacy_standard, container, false);
        String upgradeToUrl = Objects.requireNonNull(error.getData()).getUpgradeToUrl();
        binding.forceLogout.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.onForceLogout();
            }
        });
        binding.tryAgain.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.tryAgain();
            }
        });
        binding.upgradePlan.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.upgradePlan(upgradeToUrl);
            }
        });
        binding.close.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.cancel();
            }
        });
        return binding.getRoot();
    }

    private View getDmProBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        BottomSheetDmProBinding binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_dm_pro, container, false);
        String deviceManagementUrl = Objects.requireNonNull(error.getData()).getDeviceManagementUrl();
        binding.forceLogout.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.onForceLogout();
            }
        });
        binding.enableDeviceManagement.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.enableDeviceManagement(deviceManagementUrl);
            }
        });
        binding.tryAgain.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.tryAgain();
            }
        });
        binding.close.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.cancel();
            }
        });
        return binding.getRoot();
    }

    private View getProBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        BottomSheetProBinding binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_pro, container, false);
        String deviceManagementUrl = Objects.requireNonNull(error.getData()).getDeviceManagementUrl();
        binding.forceLogout.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.onForceLogout();
            }
        });
        binding.enableDeviceManagement.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.enableDeviceManagement(deviceManagementUrl);
            }
        });
        binding.tryAgain.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.tryAgain();
            }
        });
        binding.close.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.cancel();
            }
        });
        return binding.getRoot();
    }

    private View getDmStandardBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        BottomSheetDmStandardBinding binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_dm_standard, container, false);
        String deviceManagementUrl = Objects.requireNonNull(error.getData()).getDeviceManagementUrl();
        String upgradeToUrl = Objects.requireNonNull(error.getData()).getUpgradeToUrl();
        binding.forceLogout.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.onForceLogout();
            }
        });
        binding.enableDeviceManagement.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.enableDeviceManagement(deviceManagementUrl);
            }
        });
        binding.tryAgain.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.tryAgain();
            }
        });
        binding.upgradePlan.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.upgradePlan(upgradeToUrl);
            }
        });
        binding.close.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.cancel();
            }
        });
        return binding.getRoot();
    }

    private View getStandardBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        BottomSheetStandardBinding binding = DataBindingUtil.inflate(inflater, R.layout.bottom_sheet_standard, container, false);
        String deviceManagementUrl = Objects.requireNonNull(error.getData()).getDeviceManagementUrl();
        String upgradeToUrl = Objects.requireNonNull(error.getData()).getUpgradeToUrl();
        binding.forceLogout.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.onForceLogout();
            }
        });
        binding.enableDeviceManagement.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.enableDeviceManagement(deviceManagementUrl);
            }
        });
        binding.tryAgain.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.tryAgain();
            }
        });
        binding.upgradePlan.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.upgradePlan(upgradeToUrl);
            }
        });
        binding.close.setOnClickListener(view -> {
            if (navigator != null) {
                navigator.cancel();
            }
        });
        return binding.getRoot();
    }

    private void init() {
    }
}