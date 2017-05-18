package com.github.recyclerview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tvSingle = (TextView) findViewById(R.id.tv_single);
        TextView tvMultiple = (TextView) findViewById(R.id.tv_multiple);

        tvSingle.setOnClickListener(this);
        tvMultiple.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.tv_single:
                intent.setClass(this, SimpleActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_multiple:
                intent.setClass(this, MultipleActivity.class);
                startActivity(intent);
                break;
        }
    }
}
