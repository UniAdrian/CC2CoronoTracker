package de.uni.cc2coronotracker.ui.views.splash;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import de.uni.cc2coronotracker.R;

public class SplashFragment extends Fragment {

    private static final String TAG = "SplashFragment";
    private static final int SPLASH_SCREEN_SHOWING_TIME = 2000;
    private static final String SP_TAG = "on_boarding_sp";
    private static final String SP_KEY = "on_boarding_status";

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View v = inflater.inflate(R.layout.fragment_splash, container, false);
        navigateToDesiredPageAfterMentionedTime(v);
        return v;
    }

    private void navigateToDesiredPageAfterMentionedTime(View view) {
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> {
                    try {
                        boolean isAppLaunchedBefore = isAppLaunchedBefore();
                        Log.d(TAG, "navigateToDesiredPageAfterMentionedTime: isAppLaunchedBefore? " + isAppLaunchedBefore);
                        int action = isAppLaunchedBefore ? R.id.action_splashFragment_to_startScreen : R.id.action_splashFragment_to_onBoardingFragment;
                        //int action = isAppLaunchedBefore ? R.id.action_splashFragment_to_onBoardingFragment : R.id.action_splashFragment_to_onBoardingFragment;
                        Navigation.findNavController(view).navigate(action);
                    } catch (Exception e) {
                        Log.e(TAG, "Stupid 'splash screen' implementation taken from StackOverflow doesn't work. Thanks, Mehedi.", e);
                    }
                }, SPLASH_SCREEN_SHOWING_TIME
        );
    }

    private boolean isAppLaunchedBefore() {
        if(getActivity() != null) {
            SharedPreferences sp = getActivity().getSharedPreferences(SP_TAG, Context.MODE_PRIVATE);
            boolean retValue = sp.getBoolean(SP_KEY, false);
            sp.edit().putBoolean(SP_KEY, true).apply();
            return retValue;
        }
        return false;
    }
}