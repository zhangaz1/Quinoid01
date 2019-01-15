package com.example.my32ndapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
//import android.widget.ListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TwActivity extends AppCompatActivity {
    public static final String LOG_TAG = "32XND";
    public static final String LAUNCH_PAGE = "PagerView Launch Page Position" ;
    public static final String EXTRA_MESSAGE = "com.example.my32ndapplication.MESSAGE";
    final int REQUEST_FILE_OPEN = 2 ;
    final int NNF_FILEPICKER = 3 ;
    private ArrayList<TwFile> mTwFiles ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TwManager.get(this).loadTwFilesFromPreferences();
        mTwFiles = TwManager.get(this).getTwFiles() ;

        ListView listView = findViewById(R.id.listview) ;
        ArrayAdapter<TwFile> adapter =
                new ArrayAdapter<TwFile>(this, android.R.layout.simple_list_item_1, mTwFiles);

        listView.setAdapter(adapter);

        // Create a message handling object as an anonymous class.
        AdapterView.OnItemClickListener mMessageClickedHandler = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                TwFile twFile =  mTwFiles.get(0) ;
                Intent intent = new Intent(TwActivity.this, TwPagerActivity.class) ;
                //intent.putExtra(TwFragment.TW_FILE_NAME, twFile.getTitle());
                intent.putExtra(LAUNCH_PAGE,position) ;
                startActivity(intent);
                // Do something in response to the click
            }
        };
       listView.setOnItemClickListener(mMessageClickedHandler);

       // Handle long presses
        AdapterView.OnItemLongClickListener mItemLongClickListener = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                TwManager.get(TwActivity.this).deleteTwFile(i);
                ListView listView = findViewById(R.id.listview) ;
                ((ArrayAdapter<TwFile>) listView.getAdapter()).notifyDataSetChanged();
                return true;
            }
        } ;
       listView.setOnItemLongClickListener(mItemLongClickListener);


    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void selectFile(View view) {

        /*
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        //intent.setType("text/html");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        // Only the system receives the ACTION_OPEN_DOCUMENT, so no need to test.
        startActivityForResult(intent, REQUEST_FILE_OPEN);
        //selectFile();
        */

        // This always works
        Intent i = new Intent(this, FilePickerActivity.class);
        // This works if you defined the intent filter
        // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

        // Set these depending on your use case. These are the defaults.
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);

        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

        startActivityForResult(i, NNF_FILEPICKER);


    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // This code may be obsolete if the file-picker approach works.
        // But may be good backup as various file-pickers come and go
        if (requestCode == REQUEST_FILE_OPEN && resultCode == RESULT_OK) {
            Uri uriFile = data.getData();
            Toast.makeText(this,uriFile.toString(),Toast.LENGTH_SHORT).show();
            //EditText editText = findViewById(R.id.editText) ;
            //editText.setText(uriFile.toString());
            TwManager.get(this).addTwFile(new TwFile(uriFile.toString()));
            // Launch something 2019-01-10 We will need this code in the
            // listview listener, maybe
            ListView listView = findViewById(R.id.listview) ;
            ((ArrayAdapter<TwFile>) listView.getAdapter()).notifyDataSetChanged();

            /* THIS IS CODE WE USE TO LAUNCH FRAGMENT WITH WEBVIEW -- IT WORKS.
            Intent intent = new Intent(TwActivity.this, TwFragmentActivity.class) ;
            intent.putExtra(TwFragment.TW_FILE_NAME, uriFile.toString());
            startActivity(intent);
            /*

            /*WebView webView = (WebView) findViewById(R.id.webview);
            webView.addJavascriptInterface(new WebAppInterface(this),"twi");
            WebSettings webSettings = webView.getSettings() ;
            webSettings.setJavaScriptEnabled(true);
            webView.loadUrl(uriFile.toString());*/
        }

            if (requestCode == NNF_FILEPICKER && resultCode == RESULT_OK) {
                // Use the provided utility method to parse the result
                List<Uri> uriFiles = Utils.getSelectedFilesFromResult(data);
                for (Uri uriFile : uriFiles) {
                    //File file = Utils.getFileForUri(uriFile);
                    String path = uriFile.getPath() ;
                    int rootpos = path.indexOf("/root/") ;
                    if( rootpos != -1 ) {
                        path = path.substring(6);
                    }
                    path = "file:///" + path ;
                    TwManager.get(this).addTwFile(new TwFile(path));
                    // Launch something 2019-01-10 We will need this code in the
                    // listview listener, maybe
                    ListView listView = findViewById(R.id.listview);
                    ((ArrayAdapter<TwFile>) listView.getAdapter()).notifyDataSetChanged();
                }
            }


    }

    public void makeToast(String tm) {
        Toast.makeText(this, tm, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        TwManager.get(this).saveTwFilesToPreferences();
    }

    public String getPublicAlbumStorageDir(String albumName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), albumName);
        if (!file.mkdirs()) {
            Log.d(LOG_TAG, "Directory not created");
        }

        return file.getPath() ;
    }


    public void sendLaunchMessage(View view) {
        // TODO: Make this either launch from list press, or do a check that mTwFiles is populated.
       /* TwFile twFile =  mTwFiles.get(0) ;
        Intent intent = new Intent(TwActivity.this, TwPagerActivity.class) ;
        intent.putExtra(TwFragment.TW_FILE_NAME, twFile.getTitle());
        startActivity(intent);*/
    }
}
