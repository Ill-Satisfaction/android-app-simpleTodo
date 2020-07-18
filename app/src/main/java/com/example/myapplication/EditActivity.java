package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;

public class EditActivity extends AppCompatActivity {

    EditText etItem;
    String tempText;
    RadioButton[] rb;
    Switch importSwitch;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        etItem = findViewById(R.id.etItem);
        tempText = getIntent().getStringExtra(MainActivity.KEY_ITEM_TEXT);
        rb = new RadioButton[]{findViewById(R.id.typeTaskEdit), findViewById(R.id.typeEventEdit), findViewById(R.id.typeNoteEdit)};
        importSwitch = (Switch) findViewById(R.id.switchImportEdit);
        btnSave = findViewById(R.id.btnSave);

        getSupportActionBar().setTitle("Edit Item");

        etItem.setText(tempText.substring(2));
        // toggle correct radiobutton and switch
        if(tempText.length() > 2) {
            if (tempText.charAt(0) == '!') importSwitch.setChecked(true);
            switch (tempText.charAt(1)) {
                case '.':
                    rb[0].setChecked(true);
                    break;
                case '0' :
                    rb[1].setChecked(true);
                    break;
                case '-' :
                    rb[2].setChecked(true);
                    break;
                default :
                    tempText = "-" + tempText;
                    rb[2].setChecked(true);
            }
        }

        btnSave.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                // create an intent, will contain results
                Intent intent = new Intent();
                // update tempText w/ updated text
                String prefix = "";
                tempText = etItem.getText().toString();
                prefix = (importSwitch.isChecked()) ? "!" : "?";
                if (rb[0].isChecked()) prefix += ".";
                if (rb[1].isChecked()) prefix += "0";
                if (rb[2].isChecked()) prefix += "-";
                tempText = prefix + tempText;

                // pass results
                intent.putExtra(MainActivity.KEY_ITEM_TEXT, tempText);
                intent.putExtra(MainActivity.KEY_ITEM_POSITION, getIntent().getExtras().getInt(MainActivity.KEY_ITEM_POSITION));
                // set the result of the intent
                setResult(RESULT_OK, intent);
                // finish activity -> close screen and go back
                finish();
            }
        });
    }
}