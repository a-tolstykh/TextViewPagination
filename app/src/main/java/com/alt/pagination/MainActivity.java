package com.alt.pagination;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private PaginationController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView mText = (TextView) findViewById(R.id.text);
        mController = new PaginationController(mText);

        findViewById(R.id.previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.previous();
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.next();
            }
        });

        // Simulate obtaining text
        onTextLoaded(getString(R.string.lorem_ipsum));
    }

    void onTextLoaded(String text) {
        // start displaying loading here
        mController.onTextLoaded(text, new PaginationController.OnInitializedListener() {
            @Override
            public void onInitialized() {
                // stop displaying loading here
                // enable buttons
            }
        });
    }
}
