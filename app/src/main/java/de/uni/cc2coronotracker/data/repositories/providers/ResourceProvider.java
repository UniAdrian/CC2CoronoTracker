package de.uni.cc2coronotracker.data.repositories.providers;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

import androidx.annotation.ArrayRes;
import androidx.annotation.StringRes;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * A simple wrapper that allows ViewModels to retrieve app resources without itself holding context.
 */
public class ResourceProvider {

    private final Context context;
    public final Resources res;

    @Inject
    public ResourceProvider(@ApplicationContext Context context) {
        this.context = context;
        res = context.getResources();
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

    /**
     * Returns a string resource to the caller.
     * @param strId The strings resource id
     * @param args The string arguments as a var-arg
     * @return The (potentially interpreted) string
     */
    public String getString(@StringRes int strId, Object... args) {
        return res.getString(strId, args);
    }
}
