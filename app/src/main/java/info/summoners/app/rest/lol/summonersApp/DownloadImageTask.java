package info.summoners.app.rest.lol.summonersApp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;

// DownloadImageTask.java

// Asynchronous Task used to thread the download of a picture.

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView bmImage;
    private boolean autoSetOnPostExecute;
    private boolean transition;
    private Context context;
    // Indicates if the image will be automatically placed
    // in the received ImageView (onPostExecute asynchronously)
    // or if it shouldn't (just download it - don't do
    // anything asynchronous after that!).

    // Constructor.
    public DownloadImageTask(Context context, ImageView bmImage, boolean autoSetOnPostExecute, boolean transition) {
        this.bmImage = bmImage;
        this.autoSetOnPostExecute = autoSetOnPostExecute;
        this.transition=transition;
        this.context=context;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap image = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            image = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            LogSystem.appendLog("Code 01 - DownloadImageTask: " + e.toString(), "SummonersErrors");
            // (A null may be returned).
        }
        return image;
    }

    protected void onPostExecute(Bitmap result) {
        if (autoSetOnPostExecute) {
            if (transition) {
                Drawable backgrounds[] = new Drawable[2];
                Drawable icono = new BitmapDrawable(result);
                backgrounds[0] = context.getResources().getDrawable(R.drawable.empty);;
                backgrounds[1] = icono;

                TransitionDrawable crossfader = new TransitionDrawable(backgrounds);
                bmImage.setImageDrawable(crossfader);
                crossfader.startTransition(2000);
            } else {
                bmImage.setImageBitmap(result);
            }

        }
    }
}