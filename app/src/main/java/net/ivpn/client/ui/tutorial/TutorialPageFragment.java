package net.ivpn.client.ui.tutorial;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.ivpn.client.R;
import net.ivpn.client.databinding.FragmentTutorialPageBinding;

public class TutorialPageFragment extends Fragment {

    private int position;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentTutorialPageBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_tutorial_page, container, false);
        View view = binding.getRoot();
        position = getArguments().getInt(TutorialPages.PAGE_POSITION);
        TutorialPages page = TutorialPages.values()[position];
        binding.setPage(page.getTutorialPageContent());

        return view;
    }
}
