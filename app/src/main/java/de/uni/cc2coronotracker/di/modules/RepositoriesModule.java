package de.uni.cc2coronotracker.di.modules;

import android.content.Context;

import androidx.room.Room;

import java.util.concurrent.Executor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import de.uni.cc2coronotracker.data.db.AppDatabase;
import de.uni.cc2coronotracker.data.repositories.ContactRepository;


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
    public AppDatabase provideAppDatabase(@ApplicationContext Context appContext)  {
        // TODO: REMOVE FALLBACKTODESTRUCTIVEMIGRATION. This is only for test purposes and has no place in production.
        return Room.databaseBuilder(appContext, AppDatabase.class, "cc2-db").fallbackToDestructiveMigration().build();
    }
}