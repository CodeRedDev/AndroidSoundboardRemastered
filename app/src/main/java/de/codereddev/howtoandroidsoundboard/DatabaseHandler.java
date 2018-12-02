package de.codereddev.howtoandroidsoundboard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import de.codereddev.howtoandroidsoundboard.DatabaseSchema.FavoritesTable;
import de.codereddev.howtoandroidsoundboard.DatabaseSchema.MainTable;


/**
 * Handles database queries.
 * Is designed as a Singleton to only use one instance over the whole app.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // Define a tag that is used to log any kind of error or comment
    private static final String LOG_TAG = DatabaseHandler.class.getSimpleName();

    // Define a private variable that holds the database instance
    private static DatabaseHandler instance = null;

    // Define a context that's used for several tasks.
    private Context context;

    // Define a database name and version
    private static final String DATABASE_NAME = "soundboard.db";
    private static final int DATABASE_VERSION = 1;

    // Define the SQL statements to create both tables
    private static final String SQL_CREATE_MAIN_TABLE = "CREATE TABLE IF NOT EXISTS "
            + MainTable.TABLE_NAME + "("
            + MainTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MainTable.NAME + " TEXT, "
            + MainTable.RESOURCE_ID + " INTEGER unique);";

    /*
     * The sound resource id in FAVORITES_TABLE is not unique because we have to set it again on
     * every app update because every resource id changes if you add new resources
     */
    private static final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS "
            + FavoritesTable.TABLE_NAME + "("
            + FavoritesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FavoritesTable.NAME + " TEXT, "
            + FavoritesTable.RESOURCE_ID + " INTEGER);";

    /**
     * Creates the database.
     *
     * @param context Context to create the database.
     */
    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(LOG_TAG, "Database successfully initialised: " + getDatabaseName());

        this.context = context;
    }

    /**
     * Returns an instance of the database or if necessary creates it.
     *
     * @param context Context to create the database if necessary.
     * @return The current instance of the database.
     */
    public static DatabaseHandler getInstance(Context context) {

        if (instance == null) {
            return new DatabaseHandler(context.getApplicationContext());
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {
            // Execute the creation statements
            db.execSQL(SQL_CREATE_MAIN_TABLE);
            db.execSQL(SQL_CREATE_FAVORITES_TABLE);

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to create tables: " + e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        /*
         * We are using the app version instead of the database version
         * to upgrade the database, so this method is unnecessary.
         * If it gets called somehow it should only delete the main table
         * that will be refilled again in the SoundboardActivity.
         */
        db.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);
        onCreate(db);
    }

    // Defining the sound buttons

    /**
     * Creates the predefined collection of sounds and inserts them into the database.
     */
    public void createSoundCollection() {

        // Get all entries of the name StringArray(soundNames) declared in strings.xml
        String[] nameList = context.getResources().getStringArray(R.array.soundNames);

        // Declare your resource ids in the right order
        Integer[] soundIDs = {R.raw.audio01, R.raw.audio02, R.raw.audio03};

        // Define and fill a list with SoundObjects
        ArrayList<SoundObject> soundItems = new ArrayList<>();

        for (int i = 0; i < soundIDs.length; i++) {
            soundItems.add(new SoundObject(nameList[i], soundIDs[i]));
        }

        /*
         * Call putIntoMain() for each SoundObject in soundItems to fill
         * the MAIN_TABLE with all necessary information.
         */
        for (SoundObject i : soundItems) {
            putIntoMain(i);
        }
    }

    // Check if the sound id allready exists in the selected table

    /**
     * Checks if the favorites table contains a SoundObject.
     *
     * @param database    Readable database instance.
     * @param soundObject SoundObject to check for.
     * @return True if the SoundObject is already part of the table.
     */
    private boolean verification(SQLiteDatabase database, SoundObject soundObject) {

        int count = -1;
        Cursor cursor = null;

        try {

            // Get all rows from the selected table that contain the given sound id
            cursor = database.query(FavoritesTable.TABLE_NAME, new String[]{FavoritesTable.NAME},
                                    FavoritesTable.RESOURCE_ID + "=?",
                                    new String[]{soundObject.getItemId().toString()},
                                    null, null, null);

            // If the entry with the given sound id exists get the rows _id as count value
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            // close the cursor after the verification and if it is filled with something
            if (cursor != null) {
                cursor.close();
            }
        }

        // Return true if sound exists in the selected table
        return (count > 0);
    }

    // Add sounds to MAIN_TABLE

    /**
     * Inserts a SoundObject into the MAIN table.
     *
     * @param soundObject SoundObject to insert.
     */
    private void putIntoMain(SoundObject soundObject) {

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        try {

            // Put the information into a ContentValues object
            ContentValues contentValues = new ContentValues();

            contentValues.put(MainTable.NAME, soundObject.getItemName());
            contentValues.put(MainTable.RESOURCE_ID, soundObject.getItemId());

            // Insert the SoundObject into the MAIN_TABLE
            database.insertOrThrow(MainTable.TABLE_NAME, null, contentValues);

        } catch (SQLException e) {
            Log.e(LOG_TAG, "(MAIN) Failed to insert sound: " + e.getMessage());
        }
    }

    /**
     * Wraps the content of MAIN table into an ArrayList.
     *
     * @return An ArrayList that holds all SoundObjects contained in the MAIN table.
     */
    public ArrayList<SoundObject> getSoundCollection() {

        // Get a readable instance of the database
        SQLiteDatabase database = this.getReadableDatabase();

        ArrayList<SoundObject> soundObjects = new ArrayList<>();

        Cursor cursor = null;

        try {
            // Get a cursor filled with all information from the MAIN_TABLE
            cursor = database.query(MainTable.TABLE_NAME,
                                    new String[]{MainTable.NAME, MainTable.RESOURCE_ID}, null, null,
                                    null, null, MainTable.NAME);

            // Check if the cursor is empty or failed to convert the data
            if (cursor.getCount() != 0) {

                // Add each item of MAIN_TABLE to soundObjects
                while (cursor.moveToNext()) {

                    String name = cursor.getString(cursor.getColumnIndex(MainTable.NAME));
                    Integer resId = cursor.getInt(cursor.getColumnIndex(MainTable.RESOURCE_ID));

                    soundObjects.add(new SoundObject(name, resId));
                }

            } else {

                Log.d(LOG_TAG, "Failed to convert data");
            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return soundObjects;
    }

    /**
     * Searches for database entries which begin with the given string.
     *
     * @param queryString Query to search for.
     * @return An ArrayList that holds all SoundObjects that begin with the given string.
     */
    public ArrayList<SoundObject> getSoundCollectionFromQuery(String queryString) {

        SQLiteDatabase database = this.getReadableDatabase();

        ArrayList<SoundObject> soundObjects = new ArrayList<>();

        Cursor cursor = null;

        try {
            // Get a cursor filled with all information from the MAIN_TABLE
            cursor = database.query(MainTable.TABLE_NAME,
                                    new String[]{MainTable.NAME, MainTable.RESOURCE_ID},
                                    MainTable.NAME + " LIKE ?",
                                    new String[]{queryString.toLowerCase() + "%"},
                                    null,
                                    null,
                                    MainTable.NAME);

            // Check if the cursor is empty or failed to convert the data
            if (cursor.getCount() != 0) {

                // Add each item of MAIN_TABLE to soundObjects
                while (cursor.moveToNext()) {

                    String name = cursor.getString(cursor.getColumnIndex(MainTable.NAME));
                    Integer resId = cursor
                            .getInt(cursor.getColumnIndex(MainTable.RESOURCE_ID));

                    soundObjects.add(new SoundObject(name, resId));
                }

            } else {

                Log.d(LOG_TAG, "Failed to convert data");
            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return soundObjects;
    }

    /**
     * Inserts a SoundObject into the MAIN table.
     *
     * @param soundObject SoundObject to insert.
     */
    public void addFavorite(SoundObject soundObject) {

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        /*
         * Check if the soundId allready exists in the table
         * then add it to the table if it does not exist.
         */
        if (!verification(database, soundObject)) {

            try {

                // Put the information into a ContentValues object
                ContentValues contentValues = new ContentValues();

                contentValues.put(FavoritesTable.NAME, soundObject.getItemName());
                contentValues.put(FavoritesTable.RESOURCE_ID, soundObject.getItemId());

                // Insert the SoundObject into the FAVORITES_TABLE
                database.insertOrThrow(FavoritesTable.TABLE_NAME, null, contentValues);

            } catch (SQLException e) {
                Log.e(LOG_TAG, "(FAVORITES) Failed to insert sound: " + e.getMessage());
            }
        }
    }

    /**
     * Removes a SoundObject from the FAVORITES_TABLE and restarts the Activity.
     *
     * @param context     Context of the Activity.
     * @param soundObject SoundObject to be removed.
     */
    public void removeFavorite(Context context, SoundObject soundObject) {

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        // Remove entry from database table
        // Only refresh the list if something was deleted
        if (database.delete(FavoritesTable.TABLE_NAME, FavoritesTable.RESOURCE_ID + "=?",
                            new String[]{Integer.toString(soundObject.getItemId())}) != 0) {

            if (context instanceof FavoriteActivity) {
                ((FavoriteActivity) context).refreshSoundList();
            }
        }
    }

    /**
     * Wraps the content of Favorites table into an ArrayList.
     *
     * @return An ArrayList that holds all SoundObjects contained in the FAVORITES table.
     */
    public ArrayList<SoundObject> getFavorites() {

        // Get a readable instance of the database
        SQLiteDatabase database = this.getReadableDatabase();

        ArrayList<SoundObject> soundObjects = new ArrayList<>();

        Cursor cursor = null;

        try {
            // Get a cursor filled with all information from the FAVORITES_TABLE
            cursor = database.query(FavoritesTable.TABLE_NAME,
                                    new String[]{FavoritesTable.NAME, FavoritesTable.RESOURCE_ID},
                                    null,
                                    null,
                                    null,
                                    null,
                                    FavoritesTable.NAME);

            // Check if the cursor is empty or failed to convert the data
            if (cursor.getCount() != 0) {

                // Add each item of FAVORITES_TABLE to soundObjects
                while (cursor.moveToNext()) {

                    String name = cursor.getString(cursor.getColumnIndex(FavoritesTable.NAME));
                    Integer resId = cursor
                            .getInt(cursor.getColumnIndex(FavoritesTable.RESOURCE_ID));

                    soundObjects.add(new SoundObject(name, resId));
                }

            } else {

                Log.d(LOG_TAG, "Failed to convert data");
            }

        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } finally {

            if (cursor != null) {
                cursor.close();
            }
        }

        return soundObjects;
    }

    /**
     * When adding sounds to the soundboard and updating the app the resource ids might change.
     * This method will update the resource ids in the FAVORITES_TABLE.
     */
    public void updateFavorites() {

        // Get a writable instance of the database
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor favoriteContent = null;
        Cursor updateEntry = null;

        try {

            // Get all data from the FAVORITES_TABLE
            favoriteContent = database.query(FavoritesTable.TABLE_NAME,
                                             new String[]{FavoritesTable.NAME,
                                                          FavoritesTable.RESOURCE_ID},
                                             null,
                                             null,
                                             null,
                                             null,
                                             null);

            // Check if the cursor is empty or failed to convert the data
            if (favoriteContent.getCount() == 0) {

                Log.d(LOG_TAG, "Cursor is empty or failed to convert data");
                favoriteContent.close();
                return;
            }


            while (favoriteContent.moveToNext()) {

                // Set a String that will contain the name of the current sound
                String entryName = favoriteContent
                        .getString(favoriteContent.getColumnIndex(FavoritesTable.NAME));

                // Get the entry of MAIN_TABLE where the name of the current favorite sound appears
                updateEntry = database.rawQuery(
                        "SELECT * FROM " + MainTable.TABLE_NAME + " WHERE "
                                + MainTable.NAME + " = '" + entryName + "'",
                        null);

                // You can log the name of the sound that is in the update order right now for debug reasons
                //Log.d(LOG_TAG, "Currently working on: " + entryName);

                // Check if the cursor is empty or failed to convert the data
                if (updateEntry.getCount() == 0) {

                    Log.d(LOG_TAG, "Cursor is empty or failed to convert data");
                    updateEntry.close();
                    return;
                }

                // Move to the cursors first position (should only have 1 position)
                updateEntry.moveToFirst();

                // Check if the resource ids match and update the favorite resource id if necessary
                if (favoriteContent.getInt(favoriteContent.getColumnIndex(FavoritesTable.RESOURCE_ID))
                        != updateEntry.getInt(updateEntry.getColumnIndex(MainTable.RESOURCE_ID))) {

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FavoritesTable.RESOURCE_ID,
                                      updateEntry.getInt(updateEntry.getColumnIndex(MainTable.RESOURCE_ID)));

                    database.update(FavoritesTable.TABLE_NAME,
                                    contentValues,
                                    FavoritesTable.NAME + "=?",
                                    new String[]{entryName});

                    // You can log the name of the sound that has been updated for debug reasons
                    //Log.d(LOG_TAG, "Updated sound: " + entryName);
                }
                // You can log the name of the sound if it is allready up to date for debug reasons
                //else {
                //
                //    Log.d(LOG_TAG, "Allready up to date: " + entryName);
                //}
            }
        } catch (NullPointerException e) {
            Log.e(LOG_TAG, "Cursor is a NullPointer: " + e.getMessage());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to update favorites: " + e.getMessage());
        } finally {

            if (favoriteContent != null) {
                favoriteContent.close();
            }

            if (updateEntry != null) {
                updateEntry.close();
            }
        }
    }

    /**
     * Gets called when app is updated and recreates the MAIN_TABLE.
     */
    public void appUpdate() {

        try {

            SQLiteDatabase database = this.getWritableDatabase();

            database.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);

            database.execSQL(SQL_CREATE_MAIN_TABLE);


        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to update the main table on app update: " + e.getMessage());
        }
    }

}
