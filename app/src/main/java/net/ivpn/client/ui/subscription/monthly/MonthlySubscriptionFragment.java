package net.ivpn.client.ui.subscription.monthly;

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
import net.ivpn.client.ui.subscription.SubscriptionActivity;
import net.ivpn.client.ui.subscription.SubscriptionViewModel;

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