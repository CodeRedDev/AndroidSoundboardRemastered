package de.codereddev.howtoandroidsoundboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;

public class FavoriteActivity
    extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<SoundObject>> {

    // Define a tag that is used to log any kind of error or comment
    private static final String LOG_TAG = FavoriteActivity.class.getSimpleName();

    // Declare a mToolbar to use instead of the system standard mToolbar
    private Toolbar mToolbar;

    // Declare an ArrayList that you fill with SoundObjects
    private ArrayList<SoundObject> mSoundList = new ArrayList<>();

    // Declare a RecyclerView and its components
    // You can assign the RecyclerView.Adapter right away
    private RecyclerView mRecyclerView;
    private SoundboardRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        // Assign mToolbar to the Toolbar item declared in activity_favorite.xml
        mToolbar = (Toolbar) findViewById(R.id.favorite_toolbar);

        // Set mToolbar as new action bar
        setSupportActionBar(mToolbar);

        // Assign SoundView to the RecyclerView item declared in activity_soundboard.xml
        mRecyclerView = (RecyclerView) findViewById(R.id.favoriteRecyclerView);

        // Define the RecyclerView.LayoutManager to have 3 columns
        mLayoutManager = new GridLayoutManager(this, 3);

        // Set the RecyclerView.LayoutManager
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Initialize recycler adapter
        mRecyclerAdapter = new SoundboardRecyclerAdapter(this, mSoundList);

        // Set the RecyclerView.Adapter
        mRecyclerView.setAdapter(mRecyclerAdapter);

        // Calls a method that adds data from a database to the soundList
        getSupportLoaderManager().initLoader(R.id.favorites_soundlist_loader_id, null, this);
    }

    // Create/Inflate options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the layout
        getMenuInflater().inflate(R.menu.toolbar_menu_fav, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // Handle 'onClicks' in the options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Finish activity on click
        if (item.getItemId() == R.id.action_favorite_hide) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventHandlerClass.releaseMediaPlayer();
    }

    /**
     * Refreshes the displayed list by restarting the loader that holds references to the list.
     */
    public void refreshSoundList() {

        getSupportLoaderManager().restartLoader(R.id.favorites_soundlist_loader_id, null, this);
    }

    // Gets the entries from the database and loads the data into the RecyclerView.

    @NonNull
    @Override
    public Loader<ArrayList<SoundObject>> onCreateLoader(int id, @Nullable Bundle args) {
        return new SoundListLoader(getApplicationContext()) {

            @Override
            public ArrayList<SoundObject> loadInBackground() {
                return DatabaseHandler.getInstance(FavoriteActivity.this).getFavorites();
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<SoundObject>> loader, ArrayList<SoundObject> data) {

        mRecyclerAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<SoundObject>> loader) {
        mRecyclerAdapter.swapData(new ArrayList<SoundObject>());
    }
}
