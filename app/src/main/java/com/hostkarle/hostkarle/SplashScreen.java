package com.hostkarle.hostkarle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class SplashScreen extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    sleep(5000);
                    Intent in = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(in);
                    SplashScreen.this.finishAffinity();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }
}
