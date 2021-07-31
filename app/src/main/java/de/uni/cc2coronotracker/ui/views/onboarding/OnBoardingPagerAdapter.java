package de.uni.cc2coronotracker.ui.views.onboarding;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class OnBoardingPagerAdapter extends FragmentStateAdapter {

    private final ArrayList<Fragment> fragmentList;

    public OnBoardingPagerAdapter(
            @NonNull FragmentManager fragmentManager,
            @NonNull Lifecycle lifecycle,
            ArrayList<Fragment> fragments
    ) {
        super(fragmentManager, lifecycle);
        this.fragmentList = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }
}
