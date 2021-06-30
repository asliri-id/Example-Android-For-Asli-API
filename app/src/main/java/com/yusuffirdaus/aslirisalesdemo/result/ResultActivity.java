package com.yusuffirdaus.aslirisalesdemo.result;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yusuffirdaus.aslirisalesdemo.R;

public class ResultActivity extends AppCompatActivity {

    private Button bDone;
    private String address;
    private Boolean pob, dob, nama;
    private Double selfie;
    private ImageView ivResultName, ivResultDOB, ivResultPOB;
    private TextView tvSelfie, tvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        bDone = findViewById(R.id.bDone);
        bDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        nama = intent.getBooleanExtra("nama", false);
        address = intent.getStringExtra("address");
        pob = intent.getBooleanExtra("pob", false);
        dob = intent.getBooleanExtra("dob", false);
        selfie = intent.getDoubleExtra("selfie", 0);

        ivResultName = findViewById(R.id.iv_name);
        ivResultPOB = findViewById(R.id.iv_pob);
        ivResultDOB = findViewById(R.id.iv_bod);
        tvAddress = findViewById(R.id.tvAddress);
        tvSelfie = findViewById(R.id.tvScore);


        if (nama == true) {
            ivResultName.setImageResource(R.drawable.ic_baseline_check_green);
        } else {
            ivResultName.setImageResource(R.drawable.ic_baseline_cancel_24);
        }

        if (pob == true) {
            ivResultPOB.setImageResource(R.drawable.ic_baseline_check_green);
        } else {
            ivResultPOB.setImageResource(R.drawable.ic_baseline_cancel_24);
        }
        if (dob == true) {
            ivResultDOB.setImageResource(R.drawable.ic_baseline_check_green);
        } else {
            ivResultDOB.setImageResource(R.drawable.ic_baseline_cancel_24);
        }
        tvSelfie.setText(selfie.toString());
        tvAddress.setText(address);

    }

}