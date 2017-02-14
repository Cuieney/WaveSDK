package com.phl.wavesdk;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.feetsdk.android.FeetSdk;
import com.feetsdk.android.feetsdk.ui.FwController;

public class MainActivity extends AppCompatActivity {

    public Button start;
    public FwController feetUiController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start = ((Button) findViewById(R.id.start));
        feetUiController = FeetSdk.getFeetUiController();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (feetUiController != null) {
                    feetUiController.show(MainActivity.this);
                }
            }
        });

    }
}
