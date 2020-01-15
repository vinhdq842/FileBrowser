package com.quangvinh.filebrowser;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class Viewer extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewer);


        String ct;
        TextView content = findViewById(R.id.content);
        ct = new String(getIntent().getByteArrayExtra("data"));
        content.setText(ct);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}