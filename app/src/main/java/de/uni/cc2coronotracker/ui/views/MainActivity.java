package de.uni.cc2coronotracker.ui.views;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.repositories.providers.LocationProvider;
import de.uni.cc2coronotracker.data.viewmodel.PreferencesViewModel;
import de.uni.cc2coronotracker.helper.CallWithContextRequest;
import de.uni.cc2coronotracker.helper.ContextMediator;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "CC2MainActivity";

    /**
     * Identifies a single location availability request. Mainly used by the LocationProvider.
     */
    public static final int LOCATION_AVAILABILITY_REQUEST = 1337;

    @Inject
    public ContextMediator ctxMediator;

    @Inject
    public LocationProvider locationProvider;

    private PreferencesViewModel preferencesViewModel;
    AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupThemeMode();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencing the view model here is actually enough already to assure the UUID is assigned on startup.
        preferencesViewModel = new ViewModelProvider(this).get(PreferencesViewModel.class);
    }

    /**
     * Checks user preferences for night mode settings. Follows the preferences if they exist,
     * defaults to {@code isNightDefaultMode} otherwise.
     */
    private void setupThemeMode() {
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        // Default to night.
        if (isNightMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }


    /**
     * Returns true or false according to the current nightmode settings or (if indeterminate) the
     * boolean resource {@code isNightDefaultMode}
     * @return
     */
    private boolean isNightMode() {
        int nightModeFlags =
            getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;

            case Configuration.UI_MODE_NIGHT_NO:
                return false;

            default:
                return getResources().getBoolean(R.bool.isNightDefaultMode);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onStart() {
        super.onStart();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // Create and integrate our custom toolbar with appcompat and the navigation system.
        Toolbar toolbar = findViewById(R.id.app_toolbar_top);
        setSupportActionBar(toolbar);

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        // Sets the app to fullscreen+no-decorations for the onboarding and splash screens.
        navController.addOnDestinationChangedListener((controller, d, arguments) -> {
            boolean condition = d.getId() == R.id.splashFragment || d.getId() == R.id.onBoardingFragment;
            toolbar.setVisibility(condition ? View.GONE : View.VISIBLE);

            switch (d.getId()) {
                case R.id.splashFragment:
                case R.id.onBoardingFragment:
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    );
                    break;
                default:
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    );
                    break;
            }
        });
    }


    /**
     * Called when the app returns from pause.
     * Restarts the ctxMediator observer.
     */
    @Override
    public void onResume() {
        super.onResume();
        ctxMediator.getRequests().observe(this, event -> {
            CallWithContextRequest contentIfNotHandled = event.getContentIfNotHandled();
            if (contentIfNotHandled != null) {
                contentIfNotHandled.run(this);
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    /**
     * Inflates the actionbar default menu. Can be overwritten or extended by fragments as needed.
     * @param menu The menu to be inflated to
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_main_menu, menu);
        return true;
    }


    /**
     * Simple navigation to the preferences page using sage args via the action bar
     * @param item Not used, default signature
     * @return true - consumes the event
     */
    public boolean gotoPreferences(@SuppressWarnings("unused") MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.preferencesFragment) {
            navController.navigate(R.id.action_global_preferences);
        }
        return true;
    }


    /**
     * Receives certain activityResults and delegates them to the appropriate shared ViewModels
     * @param requestCode The request code of the activity, used to identify the receiver of the result
     * @param resultCode Activity Result, Usually SUCCESS or ABORTED
     * @param data The actual intent data passed to us from the sending activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check for the integer request code originally supplied to startResolutionForResult().
        if (requestCode == LOCATION_AVAILABILITY_REQUEST) {
            locationProvider.onSettingsResult(resultCode, data);
        }

        Log.d(TAG, "Code: " + requestCode + ". Result: " + resultCode);
    }
}