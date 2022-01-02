package com.kavinraj.cse535individualassignment1;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import java.lang.*;


import androidx.appcompat.app.AppCompatActivity;

public class symptompage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    RatingBar rb;
    Button upload_signs;
    String r,selected_text;
    databaseclass myDb;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symptom_selfmonitoring_page);
        this.setTitle("Diashield");
        Spinner spin = (Spinner) findViewById(R.id.spinner2);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(symptompage.this,
                android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.symptoms));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin.setAdapter(adapter);
        spin.setOnItemSelectedListener(this);
        myDb = new databaseclass(this);
        rb = findViewById(R.id.ratingBar);
        upload_signs = findViewById(R.id.usbutton);
        upload_signs.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                r = String.valueOf(rb.getRating());
                Toast.makeText(getApplicationContext(), selected_text + " " + r + "  Star", Toast.LENGTH_SHORT).show();
                Float f = Float.parseFloat(r);
                switch (selected_text)
                {
                    case "Nausea":
                        myDb.addData(1, 0, 0, f, 0, 0, 0, 0, 0, 0, 0, 0, 0, true);
                        break;
                    case "Headache":
                        myDb.addData(1, 0, 0, 0, f, 0, 0, 0, 0, 0, 0, 0, 0, true);
                        break;
                    case "Diarrhea":
                        myDb.addData(1, 0, 0, 0, 0, f, 0, 0, 0, 0, 0, 0, 0, true);
                        break;
                    case "Soar throat":

                        myDb.addData(1, 0, 0, 0, 0, 0, f, 0, 0, 0, 0, 0, 0, true);
                        break;
                    case "Fever":
                        myDb.addData(1, 0, 0, 0, 0, 0, 0, f, 0, 0, 0, 0, 0, true);
                        break;
                    case "Muscle Ache":
                        myDb.addData(1, 0, 0, 0, 0, 0, 0, 0, f, 0, 0, 0, 0, true);
                        break;
                    case "Loss of smell or taste":
                        myDb.addData(1, 0, 0, 0, 0, 0, 0, 0, 0, f, 0, 0, 0, true);
                        break;
                    case "cough":
                        myDb.addData(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, f, 0, 0, true);
                        break;
                    case "Shortness of breath":
                        myDb.addData(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, f, 0, true);
                        break;
                    case "Feeling tired":
                        myDb.addData(1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, f, true);
                        break;

                    default:
                        Toast.makeText(getApplicationContext(),"Invalid symptom", Toast.LENGTH_LONG).show();
                }


            }


        });


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        selected_text = adapterView.getItemAtPosition(i).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}
