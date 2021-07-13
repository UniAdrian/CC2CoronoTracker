package de.uni.cc2coronotracker.ui.views;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.data.viewmodel.PreferencesViewModel;
import de.uni.cc2coronotracker.helper.CallWithContextRequest;
import de.uni.cc2coronotracker.helper.ContextMediator;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    public ContextMediator ctxMediator;

    private PreferencesViewModel preferencesViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencing the view model here is actually enough already to assure the UUID is assigned on startup.
        preferencesViewModel = new ViewModelProvider(this).get(PreferencesViewModel.class);

        ctxMediator.getRequests().observe(this, event -> {
            CallWithContextRequest contentIfNotHandled = event.getContentIfNotHandled();
            if (contentIfNotHandled != null) {
                contentIfNotHandled.run(this);
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onStart() {
        super.onStart();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = findViewById(R.id.app_toolbar_top);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);

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

    public boolean gotoPreferences(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (navController.getCurrentDestination() != null && navController.getCurrentDestination().getId() != R.id.preferencesFragment) {
            navController.navigate(R.id.action_global_preferences);
        }
        return true;
    }
}