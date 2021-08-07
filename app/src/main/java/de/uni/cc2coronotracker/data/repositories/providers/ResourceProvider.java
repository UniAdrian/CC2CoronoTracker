package de.uni.cc2coronotracker.data.repositories.providers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import androidx.annotation.ArrayRes;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A simple wrapper that allows ViewModels to retrieve app resources without itself holding context.
 */
public class ResourceProvider {

    private Context context;

    @Inject
    public ResourceProvider(@ApplicationContext Context context) {
        this.context = context;
    }

    /**
     * Given an array resource returns the array interpreted as colors.
     * @param resourceName The array res.
     * @return The array of color integers.
     */
    public int[] getColors(@ArrayRes int resourceName) {
        Resources res = context.getResources();
        TypedArray colors = res.obtainTypedArray(resourceName);

        int length = colors.length();
        int[] result = new int[length];
        for (int i=0; i<length; ++i) {
            result[i] = colors.getColor(i, 0);
        }

        return result;
    }
}
