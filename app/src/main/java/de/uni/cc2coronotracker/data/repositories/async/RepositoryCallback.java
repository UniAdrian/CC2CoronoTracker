package de.uni.cc2coronotracker.data.repositories.async;

public interface RepositoryCallback<T> {
    void onComplete(Result<T> result);
}