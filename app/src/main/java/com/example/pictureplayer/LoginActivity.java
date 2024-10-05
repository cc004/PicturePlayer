package com.example.pictureplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        TextView textView = findViewById(R.id.textView);
        SharedPreferences pref = this.getPreferences(Context.MODE_PRIVATE);

        textView.setText(pref.getString("addr", ""));

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String addr = textView.getText().toString();
                SharedPreferences.Editor editor = pref.edit();
                editor.putString("addr", addr);
                editor.apply();

                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, MainActivity.class);
                intent.putExtra("addr", addr);
                startActivity(intent);
            }
        });
    }
}
