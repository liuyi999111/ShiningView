package com.compilesense.liuyi.shiningviewsample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.compilesense.liuyi.shiningview.ShiningLayout;

public class MainActivity extends AppCompatActivity {
    ShiningLayout container;
    ShiningLayout.ShininessType shininessType = ShiningLayout.ShininessType.linearity;
    Button normal,spotlight;
    ImageView imageView;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                container.setShininessType(shininessType);
                container.setDirectionOfMovement(ShiningLayout.DirectionOfMovement.left2right);
                container.setDuration(2000);
                container.setShininessSize(0.1f);
                container.start();
            }
        });
    }

    void initView(){
        imageView = (ImageView) findViewById(R.id.img_j);
        imageView.setVisibility(View.GONE);
        textView = (TextView) findViewById(R.id.tv_s);
        container = (ShiningLayout) findViewById(R.id.container);
        normal = (Button) findViewById(R.id.bt_normal);
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shininessType == ShiningLayout.ShininessType.spotlight){
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.VISIBLE);

                    shininessType = ShiningLayout.ShininessType.linearity;
                    container.setShininessSize(0.1f);
                    container.setShininessType(shininessType);
                }
            }
        });
        spotlight = (Button) findViewById(R.id.bt_spotlight);
        spotlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shininessType == ShiningLayout.ShininessType.linearity){
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.GONE);

                    shininessType = ShiningLayout.ShininessType.spotlight;
                    container.setShininessSize(0.3f);
                    container.setShininessType(shininessType);
                }
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
