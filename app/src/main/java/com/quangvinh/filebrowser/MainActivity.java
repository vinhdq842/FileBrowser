package com.quangvinh.filebrowser;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;


/**
 * @author ServantOfEvil
 */

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private TextView path;
    private File[] listFiles;
    private String currentFolder;
    private MyAdapter adapter;
    private boolean accessible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        } else accessible = true;

        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        path = findViewById(R.id.textViewPath);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changePath(listFiles[position].getAbsolutePath());
            }
        });

       if (accessible) changePath(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    accessible = true;
                    changePath(Environment.getExternalStorageDirectory().getAbsolutePath());
                }
                break;
        }
    }

    private Item[] generateItems(File[] files) {
        Item[] items = new Item[files.length];
        for (int i = 0; i < files.length; i++) {
            items[i] = new Item(files[i].getName(), files[i].lastModified(), files[i].length(), files[i].isDirectory());
        }

        return items;
    }

    private void changePath(String path) {
        File file;
        if (path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
            file = Environment.getExternalStorageDirectory();
        } else file = new File(path);
        if (file.isDirectory()) {
            listFiles = file.listFiles();
            adapter = new MyAdapter(this, R.layout.entry_list, generateItems(listFiles));
            listView.setAdapter(adapter);
            setPath(path);
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] b = new byte[fileInputStream.available()];
                fileInputStream.read(b);
                fileInputStream.close();
                Intent intent = new Intent(this, Viewer.class);
                intent.putExtra("data", b);
                startActivity(intent);
            } catch (IOException ioe) {

            }
        }
    }

    private void setPath(String cur) {
        currentFolder = cur;
        path.setText(currentFolder);
    }

    @Override
    public void onBackPressed() {
        if (!accessible) {
            finish();
            return;
        }
        if (currentFolder.equals(Environment.getExternalStorageDirectory().getAbsolutePath()))
            finish();
        else {
            int index = currentFolder.lastIndexOf('/');
            currentFolder = currentFolder.substring(0, index);
            changePath(currentFolder);
        }
    }

}


class MyAdapter extends ArrayAdapter<Item> {

    private int resource;

    MyAdapter(Context context, int resource, Item[] items) {
        super(context, resource, items);
        this.resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        convertView = inflater.inflate(resource, parent, false);

        ImageView imageViewIcon = convertView.findViewById(R.id.icon);
        TextView textViewTitle = convertView.findViewById(R.id.title);
        TextView textViewContent = convertView.findViewById(R.id.lastMod);
        TextView textViewSize = convertView.findViewById(R.id.size);

        imageViewIcon.setImageResource(getItem(position).isDir() ? R.drawable.folder : R.drawable.file);
        textViewTitle.setText(getItem(position).getLabel());
        textViewContent.setText(getItem(position).getLastModified());
        textViewSize.setText(String.format("%.2f", getItem(position).getSize()) + "kB");

        return convertView;
    }

}

class Item {

    private String label;
    private long lastModified;
    private float size;
    private boolean isDir;

    Item(String label, long lastModified, float size, boolean isDir) {
        this.label = label;
        this.lastModified = lastModified;
        this.size = size;
        this.isDir = isDir;
    }

    boolean isDir() {
        return isDir;
    }


    String getLabel() {
        return label;
    }


    String getLastModified() {
        Calendar c;
        (c = Calendar.getInstance()).setTimeInMillis(lastModified);
        return "" + form(c.get(Calendar.DAY_OF_MONTH)) + "/" + form(c.get(Calendar.MONTH) + 1) + "/" + form(c.get(Calendar.YEAR)) + " " + form(c.get(Calendar.HOUR_OF_DAY)) + ":" + form(c.get(Calendar.MINUTE)) + ":" + form(c.get(Calendar.SECOND));
    }

    private String form(int s) {
        if (s < 10) return "0" + s;
        return s + "";
    }

    float getSize() {
        return size / 1024;
    }


}