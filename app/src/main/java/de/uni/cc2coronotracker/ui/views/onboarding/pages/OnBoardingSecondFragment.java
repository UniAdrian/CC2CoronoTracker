package de.uni.cc2coronotracker.ui.views.onboarding.pages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.uni.cc2coronotracker.R;

public class OnBoardingSecondFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_on_boarding_second, container, false);
        final ViewPager2 viewPager = getActivity() != null ? getActivity().findViewById(R.id.viewpager) : null;
        v.findViewById(R.id.next_btn).setOnClickListener(v1 -> {
            if(viewPager != null) {
                viewPager.setCurrentItem(2);
            }
        });
        return v;
    }

}