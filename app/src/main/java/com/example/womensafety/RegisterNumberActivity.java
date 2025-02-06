package com.example.womensafety;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.womensafety.R;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterNumberActivity extends AppCompatActivity {

    TextInputEditText number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);
        number = findViewById(R.id.numberEdit);
    }

    public void saveNumber(View view) {
        String numberString = number.getText().toString();

        // Validate the number to be exactly 11 digits
        if (numberString.length() == 11 && numberString.matches("[0-9]+")) {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("ENUM", numberString);
            myEdit.apply();
            RegisterNumberActivity.this.finish();
        } else {
            Toast.makeText(this, "Enter Valid 11-Digit Number!", Toast.LENGTH_SHORT).show();
        }
    }

}