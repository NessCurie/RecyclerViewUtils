package com.github.recyclerview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvSingle = findViewById(R.id.tv_single);
        TextView tvMultiple = findViewById(R.id.tv_multiple);
        TextView tvHfrefreshQuicksidebar = findViewById(R.id.tv_hfrefresh_quicksidebar);

        tvSingle.setOnClickListener(this);
        tvMultiple.setOnClickListener(this);
        tvHfrefreshQuicksidebar.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_single:
                intent.setClass(this, SimpleActivity.class);
                break;
            case R.id.tv_multiple:
                intent.setClass(this, MultipleActivity.class);
                break;
            case R.id.tv_hfrefresh_quicksidebar:
                intent.setClass(this, HfRefreshQuickSidebarActivity.class);
                break;
        }
        startActivity(intent);
    }
}
