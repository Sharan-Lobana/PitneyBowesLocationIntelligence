//package com.example.ianno.plotter;
//
//import android.os.Bundle;
//import android.support.design.widget.FloatingActionButton;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.Toolbar;
//import android.view.View;
//
//import android.os.Bundle;
//import android.app.Activity;
//import android.content.Intent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.google.zxing.integration.android.IntentIntegrator;
//import com.google.zxing.integration.android.IntentResult;
//
//public class barcodenGPS extends AppCompatActivity implements OnClickListener {
//    private Button scanBtn;
//    private TextView formatTxt, contentTxt;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_barcoden_gps);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//
//    }
//    public void onClick(View v){
////respond to clicks
//        scanBtn = (Button)findViewById(R.id.scan_button);
//        formatTxt = (TextView)findViewById(R.id.scan_format);
//        contentTxt = (TextView)findViewById(R.id.scan_content);
//        scanBtn.setOnClickListener(this);
//        if(v.getId()==R.id.scan_button){
////scan
//            IntentIntegrator scanIntegrator = new IntentIntegrator(this);
//            scanIntegrator.initiateScan();
//        }
//    }
//    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
////retrieve scan result
//        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//        if (scanningResult != null) {
//            String scanContent = scanningResult.getContents();
//            String scanFormat = scanningResult.getFormatName();
//            formatTxt.setText("FORMAT: " + scanFormat);
//            contentTxt.setText("CONTENT: " + scanContent);
////we have a result
//        }
//        else{
//            Toast toast = Toast.makeText(getApplicationContext(),
//                    "No scan data received!", Toast.LENGTH_SHORT);
//            toast.show();
//        }
//    }
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
//
