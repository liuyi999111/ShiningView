package com.compilesense.liuyi.shiningview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    ShiningLayout container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        container = (ShiningLayout) findViewById(R.id.container);
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                container.setShininessType(ShiningLayout.ShininessType.spotlight);
                container.setDirectionOfMovement(ShiningLayout.DirectionOfMovement.left2right);
                container.setDuration(2000);
                container.setShininessSize(0.1f);
                container.start();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        container.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        container.cancel();
    }
}
