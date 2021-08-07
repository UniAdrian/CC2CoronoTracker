package de.uni.cc2coronotracker.di.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import de.uni.cc2coronotracker.data.repositories.providers.ResourceProvider;
import de.uni.cc2coronotracker.helper.ContextMediator;

@Module
@InstallIn(SingletonComponent.class)
public class ApplicationModule {

    @Singleton
    @Provides
    public Context provideContext(Application application) {
        return application.getApplicationContext();
    }

    @Provides
    @Singleton
    public Executor provideExecutor() {
        return Executors.newCachedThreadPool();
    }

    @Provides
    @Singleton
    public ContextMediator provideContextMediator() {
        return new ContextMediator();
    }

    @Provides
    @Singleton
    public SharedPreferences providePreferences(@ApplicationContext Context appCtx) {
        return PreferenceManager.getDefaultSharedPreferences(appCtx);
    }

    @Provides
    @Singleton
    public ResourceProvider provideResources(@ApplicationContext Context appCtx) {
        return new ResourceProvider(appCtx);
    }
}