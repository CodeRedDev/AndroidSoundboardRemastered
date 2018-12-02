package de.codereddev.howtoandroidsoundboard;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;

/**
 * An asynchronous loader that holds an ArrayList of SoundObjects.
 */
public class SoundListLoader extends AsyncTaskLoader<ArrayList<SoundObject>> {

    public SoundListLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This method should be overridden to implement the right database function to get the ArrayList from.
     *
     * @return An ArrayList of SoundObjects
     */
    @Override
    public ArrayList<SoundObject> loadInBackground() {
        return new ArrayList<SoundObject>();
    }

    @Override
    public void deliverResult(ArrayList<SoundObject> data) {
        super.deliverResult(data);
    }
}
