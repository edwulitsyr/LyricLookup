package com.example.project2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Activity for holding a list of songs from the user's phone storage
 */
public class SongLibraryActivity extends AppCompatActivity implements AsyncResponseIF {

    // view containing list of songs from external storage
    private ListView mySongsListView;

    // list of songs found in external storage
    public ArrayList<UserStorageSong> songsList = new ArrayList<>();
    public ArrayList<String> songsDisplayNameList = new ArrayList<>();

    // variables for progress spinner dialog
    ProgressDialog spinnerDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_library);

        // check for permission to get files from storage
        boolean hasPermission = ActivityCompat.checkSelfPermission(SongLibraryActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!hasPermission) { // if permission not granted, ask again
            ActivityCompat.requestPermissions(SongLibraryActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }

        // populate the songs list and sort alphabetically
        mySongsListView = findViewById(R.id.song_library_listview);
        getSongsFromStorage(this);
        Collections.sort(songsDisplayNameList, String.CASE_INSENSITIVE_ORDER);
        Collections.sort(songsList, new Comparator<UserStorageSong>() {
            public int compare(UserStorageSong s1, UserStorageSong s2) {
                // compare two instance of `Score` and return `int` as result.
                return s1.getDisplayName().compareTo(s2.getDisplayName());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // close the spinner dialog if it was previously open
        if (spinnerDialog != null) {
            spinnerDialog.dismiss();
        }

        // set the songs list view
        ArrayAdapter adapter = new ArrayAdapter(SongLibraryActivity.this, android.R.layout.simple_list_item_1, songsDisplayNameList);
        mySongsListView.setAdapter(adapter);
        mySongsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // show dialog while looking up lyrics
                spinnerDialog = new ProgressDialog(SongLibraryActivity.this);
                spinnerDialog.setMessage("Please Wait");
                spinnerDialog.setCancelable(false);
                spinnerDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                spinnerDialog.show();

                // create async task to look up lyrics
                UserStorageSong userStorageSong = songsList.get(i);
                SongInfoFinderTask asyncSongInfoFinderTask = new SongInfoFinderTask();
                asyncSongInfoFinderTask.responseDelegate = SongLibraryActivity.this;
                asyncSongInfoFinderTask.execute(new String[] { userStorageSong.getArtist(), userStorageSong.getSongName() });
            }
        });
    }

    @Override
    public void processFinish(String output) {
        // dismiss the spinner dialog after lyrics have been found
        handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                spinnerDialog.dismiss();
            };
        };

        if (output != null && !output.isEmpty()) {
            //Log.d("songLibActFinish", output);
            GeniusAPISong apiSong = new GeniusAPISong(output);
            Log.i("songLibAct", "Generated API song: " + apiSong.toString());
            Intent intent = new Intent(this, SongInfoActivity.class);
            intent.putExtra(MainActivity.GENIUS_API_SONG_KEY, apiSong);
            startActivity(intent);
        } else {
            Log.e("songLibAct", "Could not fetch song lyrics");
            showSongNotFoundDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // go through granted permissions and determine if required permission was granted
        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                boolean permissionGranted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                // if permission not granted, show dialog and redirect to MainActivity
                if (!permissionGranted) {
                    showRedirectToMainActivityDialog();
                }
                break; // once required permission is evaluated, can stop evaluating loop
            }
        }
    }

    /**
     * Builds and shows an AlertDialog to user that prompts them to go back to MainActivity screen
     */
    private void showRedirectToMainActivityDialog() {
        // get strings
        String title = this.getResources().getString(R.string.permission_not_granted_title_text);
        String body = this.getResources().getString(R.string.permission_not_granted_body_text);
        String buttonText = this.getResources().getString(R.string.go_back_text);

        // build and show alert dialog
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SongLibraryActivity.this);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(body);
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // when user clicks on button of dialog, redirect to main page to try the other activity
                Intent intent = new Intent(SongLibraryActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        alertBuilder.show();
    }

    /**
     * Builds and shows an AlertDialog to user that prompts them to go back to MainActivity screen
     */
    private void showSongNotFoundDialog() {
        // get the strings
        String title = this.getResources().getString(R.string.could_not_find_title_text);
        String body = this.getResources().getString(R.string.could_not_find_body_text);
        String okText = this.getResources().getString(R.string.ok_text);

        // set up and show the alert dialog
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(SongLibraryActivity.this);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(body);
        alertBuilder.setCancelable(true);
        alertBuilder.setPositiveButton(okText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // close the spinner dialog after user dismisses the not found alert
                if (spinnerDialog != null) {
                    spinnerDialog.dismiss();
                }
            }
        });
        alertBuilder.show();
    }

    /**
     * Get list of songs from external storage
     */
    private void getSongsFromStorage(Context context) {
        // get songs from external storage
        Uri externalContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION
        };
        Cursor cursor = this.managedQuery(externalContentUri, projection, selection,null, null);
        while(cursor.moveToNext()){
            // get properties of songs found in external storage
            String id = cursor.getString(0).trim();
            String artist = cursor.getString(1).trim();
            String songName = cursor.getString(2).trim();
            songName = (songName.substring(0, songName.indexOf("via"))).trim(); // needed to remove from test files titles
            String filePath = cursor.getString(3).trim();
            String fileName = cursor.getString(4).trim();
            String duration = cursor.getString(5).trim();

            // construct a song object to represent a song found in the user's external storage
            UserStorageSong userStorageSong = new UserStorageSong(songName, artist);

            // add the song to the lists
            songsList.add(userStorageSong);
            songsDisplayNameList.add(userStorageSong.getDisplayName());
        }
    }
}