package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Activity for the main screen of the application that is shown upon initial application launch.
 */
public class MainActivity extends AppCompatActivity {

    // var used to pass Genius API song info between activities
    public static final String GENIUS_API_SONG_KEY = "genius.api.song";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup the custom toolbar
        Toolbar toolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info_icon:
                showInfoDialog(); // show info dialog when info icon on toolbar clicked
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
     * Start SongLibrary activity
     * @param view
     */
    public void goToSelectSongActivity(View view) {
        Intent intent = new Intent(this, SongLibraryActivity.class);
        startActivity(intent);
    }

    /**
     * Builds and shows an AlertDialog to user that gives information about app
     */
    private void showInfoDialog() {
        // get the text from the resource bundle
        String title = this.getResources().getString(R.string.how_to_use_title_text);
        String body = this.getResources().getString(R.string.how_to_use_body_text);
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

}