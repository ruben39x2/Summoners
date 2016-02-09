package info.summoners.app.rest.lol.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

// LoLDataParser.java

// A class representing the in-game data for the activity "showInGameInfo". This is basically
// the class that gathers all the info necessary to show the activity correctly.

// For example, when the activity changes from "portrait view" to "landscape view", it's
// destroyed and created again; but an object of LoLInGameData is transferred so other requests
// to the server are not necessary.

public class LoLInGameData implements Serializable {
    private List<String> team100names;
    private List<String> team100leagues;
    transient private List<Bitmap> team100champImages;
    private List<String> team200names;
    private List<String> team200leagues;
    transient private List<Bitmap> team200champImages;

    public LoLInGameData() {
        this.team100names = new LinkedList<>();
        this.team100champImages = new LinkedList<>();
        this.team100leagues = new LinkedList<>();
        this.team200names = new LinkedList<>();
        this.team200champImages = new LinkedList<>();
        this.team200leagues = new LinkedList<>();
    }

    public List<String> getTeam100names() {
        return team100names;
    }

    public void addTeam100name(String team100name) {
        this.team100names.add(team100name);
    }

    public List<String> getTeam100leagues() {
        return team100leagues;
    }

    public void addTeam100League(String team100League) {
        this.team100leagues.add(team100League);
    }

    public List<Bitmap> getTeam100champImages() {
        return team100champImages;
    }

    public void addTeam100champImage(Bitmap team100champImage) {
        this.team100champImages.add(team100champImage);
    }

    public List<String> getTeam200names() {
        return team200names;
    }

    public void addTeam200name(String team200name) {
        this.team200names.add(team200name);
    }

    public List<String> getTeam200leagues() {
        return team200leagues;
    }

    public void addTeam200League(String team200League) {
        this.team200leagues.add(team200League);
    }

    public List<Bitmap> getTeam200champImages() {
        return team200champImages;
    }

    public void addTeam200champImage(Bitmap team200champImage) {
        this.team200champImages.add(team200champImage);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Necessary to change the way that Android writes our object (remember this object implements
    // the interface Parcelable) to send it.
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // This will serialize all fields that were not marked with 'transient'
        // (Java's default behaviour).
        oos.defaultWriteObject();
        // Now, manually serialize all transient fields that you want to be serialized.
        for (Bitmap bmp : this.team100champImages)
            if (bmp != null) {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                boolean success = bmp.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                if (success) {
                    oos.writeObject(byteStream.toByteArray());
                }
            }
        for (Bitmap bmp : this.team200champImages)
            if (bmp != null) {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                boolean success = bmp.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                if (success) {
                    oos.writeObject(byteStream.toByteArray());
                }
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // Deserializing now - in the SAME ORDER!
        // All non-transient fields
        ois.defaultReadObject();
        // All other fields (the Bitmaps).
        for (int i = 0; i < 6; i++) {
            byte[] image = (byte[]) ois.readObject();
            if (image != null && image.length > 0) {
                this.team100champImages.set(i, BitmapFactory.decodeByteArray(image, 0, image.length));
            }
        }

        for (int i = 0; i < 6; i++) {
            byte[] image = (byte[]) ois.readObject();
            if (image != null && image.length > 0) {
                this.team200champImages.set(i, BitmapFactory.decodeByteArray(image, 0, image.length));
            }
        }
    }

}
