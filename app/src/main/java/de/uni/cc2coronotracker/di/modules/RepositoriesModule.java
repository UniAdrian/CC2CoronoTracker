package de.uni.cc2coronotracker.di.modules;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import de.uni.cc2coronotracker.data.api.RKIApiInterface;
import de.uni.cc2coronotracker.data.db.AppDatabase;
import de.uni.cc2coronotracker.data.repositories.AppRepository;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;
import de.uni.cc2coronotracker.data.repositories.ExposureRepository;
import de.uni.cc2coronotracker.data.repositories.StatisticsRepository;
import de.uni.cc2coronotracker.data.repositories.providers.LocationProvider;
import de.uni.cc2coronotracker.data.repositories.providers.ReadOnlySettingsProvider;
import de.uni.cc2coronotracker.helper.ContextMediator;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@Module
@InstallIn(SingletonComponent.class)
public class RepositoriesModule {

    @Provides
    @Singleton
    public ContactRepository bindContactRepository(@ApplicationContext Context ctx, Executor executor, AppDatabase db) {
        return new ContactRepository(ctx, executor, db.getContactDao());
    }

    @Provides
    @Singleton
    public AppRepository bindAppRepository(@ApplicationContext Context ctx, Executor executor, SharedPreferences prefs) {
        return new AppRepository(ctx, executor, prefs);
    }

    @Provides
    @Singleton
    public ExposureRepository bindExposureRepository(@ApplicationContext Context ctx, Executor executor, AppDatabase db) {
        return new ExposureRepository(ctx, executor, db.getExposureDao());
    }

    @Provides
    @Singleton
    public StatisticsRepository bindStatisticsRepository(@ApplicationContext Context ctx, Executor executor, AppDatabase db)  {
        return new StatisticsRepository(ctx, executor, db.getStatisticsDao());
    }

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context appContext)  {
        return Room.databaseBuilder(appContext, AppDatabase.class, "cc2-db").build();
    }

    @Provides
    @Singleton
    public LocationProvider provideLocation(@ApplicationContext Context appContext, ContextMediator mediator)  {
        return new LocationProvider(appContext, mediator);
    }

    @Provides
    @Singleton
    public ReadOnlySettingsProvider provideReadOnlySettings(@ApplicationContext Context appContext, SharedPreferences preferences)  {
        return new ReadOnlySettingsProvider(appContext, preferences);
    }


    @Provides
    @Singleton
    public RKIApiInterface provideRKIApiInterface()  {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://services7.arcgis.com/mOBPykOjAyBO2ZKk/arcgis/rest/services/RKI_Landkreisdaten/FeatureServer/0/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        return retrofit.create(RKIApiInterface.class);
    }
}