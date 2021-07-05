package de.uni.cc2coronotracker.ui.views;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import de.uni.cc2coronotracker.R;
import de.uni.cc2coronotracker.helper.CallWithContextRequest;
import de.uni.cc2coronotracker.helper.ContextMediator;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    public ContextMediator ctxMediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctxMediator.getRequests().observe(this, event -> {
            CallWithContextRequest contentIfNotHandled = event.getContentIfNotHandled();
            if (contentIfNotHandled != null) {
                contentIfNotHandled.run(this);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        Toolbar toolbar = findViewById(R.id.app_toolbar_top);
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
    }

    public boolean gotoPreferences(MenuItem item) {
        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.action_global_preferences);
        return true;
    }
}