package com.example.cs360lillybryan;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements TextWatcher {
    TextView textGreeting;
    EditText nameText;
    Button buttonSayHello;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textGreeting = findViewById(R.id.textGreeting);
        nameText = findViewById(R.id.editNameText);
        buttonSayHello = findViewById(R.id.buttonSayHello);
        nameText.addTextChangedListener(this);
        buttonSayHello.setEnabled(false);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        buttonSayHello.setEnabled(!nameText.getText().toString().matches(""));
    }

    public void SayHello(View view) {
        if(!nameText.getText().toString().isEmpty()) {
            textGreeting.setText("Hello " + nameText.getText().toString());
        }
        else {
            textGreeting.setText("You must enter a name");
        }
    }
}
