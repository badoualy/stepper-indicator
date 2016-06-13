package com.badoualy.stepperindicator.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.badoualy.stepperindicator.StepperIndicator;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        StepperIndicator indicator = (StepperIndicator) findViewById(R.id.stepper_indicator);

        indicator.setStepCount(4);
        indicator.setCurrentStep(3);

        // Or
        indicator.setViewPager(pager);
    }
}
