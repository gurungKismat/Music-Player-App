package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Adapter adapter;
    private static final String TAG = "MyTag";
    private String[] items;
    private ArrayList<File> mySongs;
    private String[] songPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initRecyclerView();
        runTimePermission();

    }


    // initializing recylcer view
    private void initRecyclerView() {
        Log.d(TAG,"Initializing recycler view");

        recyclerView = findViewById(R.id.recycler_view);

        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    // setting adapter
    private void setUpAdapter() {
        Log.d(TAG,"Setting Adapter");
        adapter = new Adapter(this,items,songPath);
        recyclerView.setAdapter(adapter);
    }

    private void runTimePermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        displaySong();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private ArrayList<File> findSong(File file) {
        ArrayList<File> arrayList = new ArrayList<>();
        File[] fileList = file.listFiles();

        if(fileList != null) {
            for (File singleFile : fileList) {
                if (singleFile.isDirectory() && !singleFile.isHidden()) {
                    arrayList.addAll(findSong(singleFile));
                } else {
                    if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) {
                        arrayList.add(singleFile);
                    }
                }
            }
        }
        return arrayList;
    }

    private void displaySong() {
        File file = Environment.getExternalStorageDirectory();
        mySongs = findSong(file);
        items = new String[mySongs.size()];
        songPath = new String[mySongs.size()];

        for (int i = 0; i<items.length; i++) {
            items[i] = mySongs.get(i).getName().toString().replace(".mp3", "").replace(".wav", "");
            songPath[i] = mySongs.get(i).toString();
//            Log.d(TAG,"SONG PATH "+songPath[i]);
        }

        setUpAdapter();
        }


}