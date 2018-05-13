package de.codereddev.howtoandroidsoundboard;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SoundboardRecyclerAdapter
    extends RecyclerView.Adapter<SoundboardRecyclerAdapter.SoundboardViewHolder> {

    // Declare an ArrayList that will contain all SoundObjects
    private ArrayList<SoundObject> soundObjects;

    // DatabaseHandler to handle database requests
    private DatabaseHandler databaseHandler;

    /**
     * Creates a new RecyclerAdapter that demands all needed informations for the RecyclerView.
     *
     * @param soundObjects Main content provider.
     */
    public SoundboardRecyclerAdapter(Context context, ArrayList<SoundObject> soundObjects) {

        // Hand over all data to the private ArrayList
        this.soundObjects = soundObjects;
        databaseHandler = DatabaseHandler.getInstance(context.getApplicationContext());
    }

    // Initialises each RecyclerView item
    @NonNull
    @Override
    public SoundboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Set the default design for a element in the RecyclerView that is based on sound_item.xml
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sound_item, null);

        // Returns a new ViewHolder for each RecyclerView item
        return new SoundboardViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SoundboardViewHolder holder, int position) {

        // Get a SoundObject from the ArrayList that also contains
        // Simplifies the set processes
        final SoundObject soundObject = soundObjects.get(position);

        // Set the name of each sound button that is represented by the soundObject
        holder.itemTextView.setText(soundObject.getItemName());

        // Handle actions when the user simply clicks on a sound button
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Context context = v.getContext();
                // Calls a method that plays the sound
                // Should be handled in an extra thread
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        EventHandlerClass.startMediaPlayer(context, soundObject.getItemId());
                    }
                }).start();
            }
        });

        // Handle actions when the user presses a sound button
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                // Calls a method that creates a PopupMenu where the user can choose between several actions
                EventHandlerClass.popupManager(v, soundObject);
                return true;
            }
        });
    }

    // Tells the RecyclerView how many items are accessible to be displayed
    // Should return the size of the given content provider (here: SoundObject ArrayList)
    @Override
    public int getItemCount() {
        return soundObjects.size();
    }

    /**
     * An object that holds all accessible areas that are declared in XML by you.
     */
    class SoundboardViewHolder extends RecyclerView.ViewHolder {

        // TextView to display the name of a sound button
        private TextView itemTextView;

        SoundboardViewHolder(View itemView) {
            super(itemView);

            // Assign itemTextView to the TextView item declared in sound_item.xml
            itemTextView = itemView.findViewById(R.id.textViewItem);
        }
    }

    /**
     * Exchanges the SoundObject list that should be displayed.
     *
     * @param data New SoundObject list.
     */
    public void swapData(ArrayList<SoundObject> data) {

        this.soundObjects = data;
        notifyDataSetChanged();
    }

    /**
     * Queries the soundlist for sound objects that begin with the given string.
     * The query process is running on an extra thread.
     *
     * @param soundName Name to query for.
     */
    public void queryData(final String soundName) {

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {

                final ArrayList<SoundObject> queryList =
                    databaseHandler.getSoundCollectionFromQuery(soundName);

                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        swapData(queryList);
                    }
                });
            }
        }).start();
    }
}