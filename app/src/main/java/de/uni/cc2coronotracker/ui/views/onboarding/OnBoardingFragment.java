package de.uni.cc2coronotracker.ui.views.onboarding;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.ui.views.onboarding.pages.OnBoardingFirstFragment;
import de.uni.cc2coronotracker.ui.views.onboarding.pages.OnBoardingSecondFragment;
import de.uni.cc2coronotracker.ui.views.onboarding.pages.OnBoardingThirdFragment;

public class OnBoardingFragment extends Fragment {

    private static final String TAG = "OnBoardingFragment";

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_on_boarding, container, false);
        if(getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            ArrayList<Fragment> fragments = getOnBoardingFragments();
            OnBoardingPagerAdapter adapter = new OnBoardingPagerAdapter(fragmentManager, getLifecycle(), fragments);
            ViewPager2 viewPager = view.findViewById(R.id.viewpager);
            viewPager.setAdapter(adapter);
        } else {
            Log.e(TAG, "Activity is null, can't initialize view pager");
        }
        return view;
    }

    public ArrayList<Fragment> getOnBoardingFragments() {
        ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new OnBoardingFirstFragment());
        fragmentList.add(new OnBoardingSecondFragment());
        fragmentList.add(new OnBoardingThirdFragment());
        return fragmentList;
    }
}