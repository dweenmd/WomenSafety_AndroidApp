package com.example.womensafety;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterNumberActivity extends AppCompatActivity {

    TextInputEditText number1, number2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_number);

        number1 = findViewById(R.id.numberEdit1);
        number2 = findViewById(R.id.numberEdit2);
    }

    public void saveNumber(View view) {
        String num1 = number1.getText().toString().trim();
        String num2 = number2.getText().toString().trim();

        if (isValidNumber(num1) && isValidNumber(num2)) {
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", MODE_PRIVATE);
            SharedPreferences.Editor myEdit = sharedPreferences.edit();
            myEdit.putString("ENUM1", num1);
            myEdit.putString("ENUM2", num2);
            myEdit.apply();

            Toast.makeText(this, "Numbers Saved!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Enter Valid 11-Digit Numbers!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidNumber(String number) {
        return number.length() == 11 && number.matches("[0-9]+");
    }
}
