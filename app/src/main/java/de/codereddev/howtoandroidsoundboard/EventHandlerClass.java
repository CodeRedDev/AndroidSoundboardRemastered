package de.codereddev.howtoandroidsoundboard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EventHandlerClass {

    // Define a tag that is used to log any kind of error or comment
    private static final String LOG_TAG = EventHandlerClass.class.getSimpleName();

    // Declare a MediaPlayer to be used by the app
    private static MediaPlayer mp;

    private static final int PERMISSIONS_REQUEST_WRITE_STORAGE = 0;

    /**
     * Creates and starta a MediaPlayer instance to play a sound.
     *
     * @param context Context of the current activity.
     * @param soundId Resource id to play.
     */
    public static void startMediaPlayer(Context context, Integer soundId) {

        try {

            // Check if the sound id was set correctly
            if (soundId != null) {

                // Check if the MediaPlayer maybe is in use
                // If so the MediaPlayer will be reset
                if (mp != null) {
                    mp.reset();
                }

                // Create and start the MediaPlayer on the given sound id
                mp = MediaPlayer.create(context, soundId);
                mp.start();
            }
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "MediaPlayer is in an invalid state for start: " + e.getMessage());
        }
    }

    /**
     * Releases all data and resources from the MediaPlayer.
     */
    public static void releaseMediaPlayer() {

        if (mp != null) {

            mp.release();
            mp = null;
        }
    }

    /**
     * Checks if the WRITE_EXTERNAL_STORAGE permission is granted.
     *
     * @param context Context needed to check permissions.
     * @return True if the permission is granted.
     */
    private static boolean storagePermissionGranted(Context context) {

        return (ContextCompat
            .checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Checks if the WRITE_SETTINGS permission is granted.
     *
     * @param context Context needed to check permissions.
     * @return True if the permission is granted. Also returns true if runtime permissions are not supported.
     */
    private static boolean settingsPermissionGranted(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            return Settings.System.canWrite(context);
        }
        return true;
    }

    /**
     * Manages the popup window when longclicking on a sound.
     *
     * @param view        View that should be used as an anchor.
     * @param soundObject SoundObject that has been clicked.
     */
    public static void popupManager(View view, final SoundObject soundObject) {

        // Context to use for the functions
        final Context context = view.getContext();

        // Declare PopupMenu and assign it to the design created in longclick.xml
        PopupMenu popup = new PopupMenu(context, view);

        // Identify the current activity and inflate the right popup menu
        if (context instanceof FavoriteActivity) {
            popup.getMenuInflater().inflate(R.menu.favo_longclick, popup.getMenu());
        } else {
            popup.getMenuInflater().inflate(R.menu.longclick, popup.getMenu());
        }

        // Handle user clicks on the popupmenu
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                // Check if the user wants to share a sound or set a sound as system audio
                if (item.getItemId() == R.id.action_send || item.getItemId() == R.id.action_ringtone) {

                    // Check if the WRITE_EXTERNAL_STORAGE permission is granted. If not just return.
                    if (!storagePermissionGranted(context)) {

                        Toast.makeText(context, R.string.perm_write_storage_error, Toast.LENGTH_SHORT)
                             .show();
                        return true;
                    }

                    // Define a filename on the given information from the SoundObject AND add the .mp3 tag
                    final String fileName = soundObject.getItemName() + ".mp3";

                    // Get the path to the users external storage
                    File storage = Environment.getExternalStorageDirectory();

                    /*
                     * Define the directory path to the soundboard apps folder.
                     * Change my_soundboard to whatever you want as your folder but keep the slash.
                     * When changing the path be sure to also
                     * modify the path in filepaths.xml (res/xml/filepaths.xml).
                     */
                    File directory = new File(storage.getAbsolutePath() + "/my_soundboard/");

                    /*
                     * Creates the directory if it doesn't exist
                     * mkdirs() gives back a boolean.
                     * You can use it to do some processes as well but we don't really need it.
                     */
                    directory.mkdirs();

                    // Finally define the file by giving over the directory and the filename
                    final File file = new File(directory, fileName);

                    // Define an InputStream that will read your sound-raw.mp3 file into a buffer
                    InputStream in = null;
                    OutputStream out = null;

                    try {

                        in = context.getResources().openRawResource(soundObject.getItemId());

                        // Log the name of the sound that is being saved
                        Log.i(LOG_TAG, "Saving sound " + soundObject.getItemName());


                        /*
                         * Define an FileOutputStream that will write the buffer data
                         * into the sound.mp3 on the external storage
                         */
                        out = new FileOutputStream(file);
                        // Define a buffer of 1kb (you can make it a bit bigger but 1kb will be adequate)
                        byte[] buffer = new byte[1024];

                        int len;
                        /*
                         * Write the data to the sound.mp3 file while reading it from the sound-raw.mp3.
                         * If (int) InputStream.read() returns -1 stream is at the end of file.
                         */
                        while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                            out.write(buffer, 0, len);
                        }

                    } catch (FileNotFoundException e) {

                        Log.e(LOG_TAG, "Failed to find file: " + e.getMessage());

                    } catch (IOException e) {

                        // Log error if process failed
                        Log.e(LOG_TAG, "Failed to save file: " + e.getMessage());
                    } finally {

                        // Close all data streams
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to close InputStream: " + e.getMessage());
                        }

                        try {
                            if (out != null) {
                                out.close();
                            }
                        } catch (IOException e) {
                            Log.e(LOG_TAG, "Failed to close OutputStream: " + e.getMessage());
                        }
                    }

                    // Send a sound via WhatsApp or the like
                    if (item.getItemId() == R.id.action_send) {

                        try {

                            /*
                             * Check if the users device Android version is 5.1 or higher
                             * If it is you'll have to use FileProvider to get the sharing function to work
                             * For more information about FileProvider and Content URI see:
                             * https://developer.android.com/training/secure-file-sharing
                             */
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                                String authority = context.getPackageName() + ".fileprovider";

                                Uri contentUri = FileProvider.getUriForFile(context, authority, file);

                                final Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                                // Define the intent to be of type audio/mp3
                                intent.setType("audio/mp3");
                                // Start a new chooser dialog where the user chooses an app to share the sound
                                context.startActivity(
                                    Intent.createChooser(intent,
                                                         context.getResources()
                                                                .getString(R.string.share_sound_title)));
                            } else {

                                final Intent intent = new Intent(Intent.ACTION_SEND);

                                // Uri refers to a name or location
                                // .parse() analyzes a given uri string and creates a Uri from it

                                // Define a "link" (Uri) to the saved file
                                Uri fileUri = Uri.parse(file.getAbsolutePath());
                                intent.putExtra(Intent.EXTRA_STREAM, fileUri);
                                // Define the intent to be of type audio/mp3
                                intent.setType("audio/mp3");
                                // Start a new chooser dialog where the user chooses an app to share the sound
                                context.startActivity(
                                    Intent.createChooser(intent,
                                                         context.getResources()
                                                                .getString(R.string.share_sound_title)));
                            }

                        } catch (IllegalArgumentException | NullPointerException e) {

                            // Log error if process failed
                            Log.e(LOG_TAG, "Failed to share sound: " + e.getMessage());
                        }
                    }

                    // Save as ringtone, alarm or notification
                    if (item.getItemId() == R.id.action_ringtone) {

                        // Check if the WRITE_SETTINGS permission is granted. If not just return.
                        if (!settingsPermissionGranted(context)) {

                            Toast.makeText(context, R.string.perm_write_settings_error, Toast.LENGTH_SHORT)
                                 .show();
                            return true;
                        }

                        /*
                         * Create a little popup like dialog that gives
                         * the user the choice between the 3 types.
                         * THEME_HOLO_LIGHT was deprecated in API 23
                         * but to support older APIs you should use it.
                         */
                        AlertDialog.Builder builder =
                            new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);

                        builder.setTitle("Save as...");
                        builder.setItems(new CharSequence[]{"Ringtone", "Notification", "Alarm"},
                                         new DialogInterface.OnClickListener() {

                                             @Override
                                             public void onClick(DialogInterface dialog, int which) {

                                                 switch (which) {

                                                     // Ringtone
                                                     case 0:
                                                         changeSystemAudio(context, fileName, file, 1);
                                                         break;
                                                     // Notification
                                                     case 1:
                                                         changeSystemAudio(context, fileName, file, 2);
                                                         break;
                                                     // Alarmton
                                                     case 2:
                                                         changeSystemAudio(context, fileName, file, 3);
                                                         break;
                                                     default:
                                                 }
                                             }
                                         });
                        builder.create();
                        builder.show();
                    }

                }

                // Add sound to favorites / Remove sound from favorites
                if (item.getItemId() == R.id.action_favorite) {

                    DatabaseHandler databaseHandler = DatabaseHandler
                        .getInstance(context.getApplicationContext());

                    // Identify the current activity
                    if (context instanceof FavoriteActivity) {
                        databaseHandler.removeFavorite(context, soundObject);
                    } else {
                        databaseHandler.addFavorite(soundObject);
                    }
                }

                return true;
            }
        });

        popup.show();
    }

    /**
     * Saves a sound as a system audio file.
     *
     * @param context  Context of the current activity.
     * @param fileName Name that is used to save the file.
     * @param file     File to save as system audio.
     * @param action   Defines as what kind of system audio the file should be saved.
     */
    private static void changeSystemAudio(Context context, String fileName, File file, int action) {

        try {

            // Put all informations about the audio into ContentValues
            ContentValues values = new ContentValues();

            // DATA stores the path to the file on disk
            values.put(MediaStore.MediaColumns.DATA, file.getAbsolutePath());
            // TITLE stores... guess what? Right, the title. GENIUS
            values.put(MediaStore.MediaColumns.TITLE, fileName);
            // MIME_TYPE stores the type of the data send via the MediaProvider
            values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");

            switch (action) {

                // Ringtone
                case 1:
                    values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                    break;
                // Notification
                case 2:
                    values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                    break;
                // Alarm
                case 3:
                    values.put(MediaStore.Audio.Media.IS_ALARM, true);
                    break;
                default:
            }

            // Define the uri to save as system tone
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(file.getAbsolutePath());
            // Delete the existing row in the MediaStore database
            context.getContentResolver()
                   .delete(uri, MediaStore.MediaColumns.DATA + "=\"" + file.getAbsolutePath() + "\"", null);
            // Insert the new row with the ContentValues
            Uri finalUri = context.getContentResolver().insert(uri, values);

            // Finally set the audio as one of the system audio types
            switch (action) {

                // Ringtone
                case 1:
                    RingtoneManager
                        .setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE, finalUri);
                    break;
                // Notification
                case 2:
                    RingtoneManager
                        .setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION, finalUri);
                    break;
                // Alarm
                case 3:
                    RingtoneManager
                        .setActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM, finalUri);
                    break;
                default:
            }

        } catch (IllegalArgumentException | NullPointerException e) {

            // Log error if process failed
            Log.e(LOG_TAG, "Failed to save as system tone: " + e.getMessage());
        }
    }
}
