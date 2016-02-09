package info.summoners.app.rest.lol.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// LoLRecentGamesData.java

// Java object containing all the necessary info to fill the List of a summoner's recent games.
// Similar to "LoLInGameData".

public class LoLRecentGamesData implements Serializable {
    private String[] gameMode = null;
    private String[] KDA = null;
    private String[] createDate = null;
    private String[] win = null;
    private int longitud = 0;
    transient private Bitmap icons[] = null;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Constructor.
    public LoLRecentGamesData(int numMatches) {
        this.gameMode = new String[numMatches];
        this.KDA = new String[numMatches];
        this.createDate = new String[numMatches];
        this.win = new String[numMatches];
        this.icons = new Bitmap[numMatches];
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Setters.
    public void setGameMode(String gameMode, int i) {
        this.gameMode[i] = gameMode;
    }

    public void setKDA(String KDA, int i) {
        this.KDA[i] = KDA;
    }

    public void setCreateDate(String createDate, int i) {
        this.createDate[i] = createDate;
    }

    public void setWin(String win, int i) {
        this.win[i] = win;
    }

    public void setIcon(Bitmap icon, int i) {
        this.longitud++;
        this.icons[i] = icon;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Getters
    public String[] getGameMode() {
        return (this.gameMode);
    }

    public String[] getKDA() {
        return (this.KDA);
    }

    public String[] getCreateDate() {
        return (this.createDate);
    }

    public String[] getWin() {
        return (this.win);
    }

    public Bitmap[] getIcon() {
        return (this.icons);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    // Necessary to change the way that Android writes our object (remember this object implements
    // the interface Parcelable) to send it.
    private void writeObject(ObjectOutputStream oos) throws IOException {
        // This will serialize all fields that were not marked with 'transient'
        // (Java's default behaviour).
        oos.defaultWriteObject();
        // Now, manually serialize all transient fields that you want to be serialized.
        for (int i = 0; i < this.longitud; i++)
            if (icons[i] != null) {
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                boolean success = icons[i].compress(Bitmap.CompressFormat.PNG, 100, byteStream);
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
        for (int i = 0; i < this.longitud; i++) {
            byte[] image = (byte[]) ois.readObject();
            if (image != null && image.length > 0) {
                this.icons[i] = BitmapFactory.decodeByteArray(image, 0, image.length);
            }
        }
    }
}
