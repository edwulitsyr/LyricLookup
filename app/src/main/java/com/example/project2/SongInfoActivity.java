package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;

/**
 * Activity for the screen containing information about the selected song, which includes the lyrics.
 */
public class SongInfoActivity extends AppCompatActivity {

    // The song the info is being shown for
    GeniusAPISong song = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_info);

        // setup the custom toolbar
        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        // get the song passed from the intent
        Intent intent = getIntent();
        if (intent != null) {
            song = (GeniusAPISong) intent.getSerializableExtra(MainActivity.GENIUS_API_SONG_KEY);
            Log.i("songInfoAct", "Got song: " + song.toString());
        }

        // check that there is a song available
        if (song == null) {
            Log.d("songInfoAct", "Song could not be defined");
            // redirect to songselect activity
            return;
        }

        // set the activity text
        setText();

        // set the activity image
        String imgUrl = song.getAlbumImageUrl();
        ImageView albumImgView = findViewById(R.id.album_imgview);
        new ImageDownloaderTask(albumImgView).execute(imgUrl);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info_icon:
                showInfoDialog();
                break;
            default:
                // do nothing
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // setup the menu for the toolbar
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Builds and shows an AlertDialog to user that gives information about app
     */
    private void showInfoDialog() {
        // get the text from the resource bundle
        String title = this.getResources().getString(R.string.song_info_title_text);
        String body = this.getResources().getString(R.string.song_info_body_text);
        String okText = this.getResources().getString(R.string.ok_text);

        // set up and show the info alert
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(body);
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // do nothing
            }
        });
        alertBuilder.show();
    }

    private void setText() {
        TextView songTitleTxtView = findViewById(R.id.song_title_txtview);
        TextView artistTxtView = findViewById(R.id.song_artist_txtview);
        TextView albumNameTxtView = findViewById(R.id.album_name_txtview);
        TextView releaseDateTxtView = findViewById(R.id.release_date_txtview);
        TextView lyricsTxtView = findViewById(R.id.lyrics_textview);

        songTitleTxtView.setText(song.getSongName());
        artistTxtView.setText(song.getArtistName());
        albumNameTxtView.setText(song.getAlbumName());
        releaseDateTxtView.setText("Release Date: " + song.getReleaseDate());
        lyricsTxtView.setText(song.getLyrics());

        // allow vertical scrolling of lyrics
        lyricsTxtView.setMovementMethod(new ScrollingMovementMethod());
    }

    /**
     * Task used to get an image from internet
     */
    private class ImageDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imgView;

        public ImageDownloaderTask(ImageView bmImage) {
            this.imgView = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String imageUrl = urls[0];
            Bitmap img = null;
            try {
                InputStream in = new URL(imageUrl).openStream();
                img = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("imgDownloader", "Exception getting image from internet");
            }
            return img;
        }

        protected void onPostExecute(Bitmap result) {
            imgView.setImageBitmap(result);
        }
    }
}