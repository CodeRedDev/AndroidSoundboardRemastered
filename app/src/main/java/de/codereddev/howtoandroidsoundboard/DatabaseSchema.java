package de.codereddev.howtoandroidsoundboard;

import android.provider.BaseColumns;

/**
 * Defines the schema/structure of the database.
 */
public abstract class DatabaseSchema {

    /*
    * The following classes describe your database table structure.
    * They have to implement the interface BaseColumns to include the base column _ID
    */

    /**
     * Class to hold the main table structure.
     * This table contains all sounds of the soundboard.
     */
    public abstract static class MainTable implements BaseColumns {

        public static final String TABLE_NAME = "main_table";
        public static final String NAME = "name";
        public static final String RESOURCE_ID = "resourceID";
    }

    /**
     * Class to hold the favorites table structure.
     * This table contains all sounds that were set as favorites by the user.
     */
    public abstract static class FavoritesTable implements BaseColumns {

        public static final String TABLE_NAME = "favorites_table";
        public static final String NAME = "name";
        public static final String RESOURCE_ID = "resourceID";
    }
}
