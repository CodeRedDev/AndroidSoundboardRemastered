package de.codereddev.howtoandroidsoundboard;

public class SoundObject {


    /*
     * SoundObject is an object that stores all kindof information you
     * need for a sound button like a name and the soundID/itemId.
     * The itemId will be the resource id for a raw .mp3
     * file that is stored in the raw folder in the projects res folder.
     */
    private String itemName;
    private Integer itemId;

    /**
     * Creates an object that holds references to the resources that define a sound (button).
     * @param itemName Name of the sound.
     * @param itemId Resource id of the sound file.
     */
    public SoundObject(String itemName, Integer itemId) {

        this.itemName = itemName;
        this.itemId = itemId;
    }

    public String getItemName() {

        return itemName;
    }

    public Integer getItemId() {

        return itemId;
    }
}
