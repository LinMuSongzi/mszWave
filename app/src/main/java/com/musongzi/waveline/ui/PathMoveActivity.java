package com.musongzi.waveline.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.musongzi.waveline.R;
import com.musongzi.waveline.databinding.ActivityPathMoveBinding;

public class PathMoveActivity extends AppCompatActivity {


    private static final String TAG = "PathMoveActivityTag";
    ActivityPathMoveBinding dataBinding;

    WaveLineView.WaveCallBack waveCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_path_move);

        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_path_move);

        initWave();
//        getLifecycle().addObserver(new DefaultLifecycleObserver() {
//            @Override
//            public void onResume(@NonNull LifecycleOwner owner) {
//                owner.getLifecycle().removeObserver(this);
//                new Thread() {
//                    @Override
//                    public void run() {
//
//                        while (true) {
//                            if (myValue != null) {
//
//                                if (myIndex == myValue.length || myIndex == -1) {
//                                    myIndex = 0;
//                                }
//                                myValue[myIndex] = (int) (Math.random() * 120);
//                                Log.i(TAG, "run: myValue[myIndex] = " + myValue[myIndex] + " , index  = " + myIndex);
//                                dataBinding.idLineview.changeMusicDB(myValue[myIndex], true);
//                                myIndex++;
//                                dataBinding.idLineview.drawPathLocationReal();
//                                try {
//                                    Thread.sleep(250);
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
//                            }
//                        }
//
//                    }
//                }.start();
//            }
//        });
    }

    int myIndex = -1;

    int[] myValue;

    private void initWave() {
//        dataBinding.idLineview.setAutomaticInvalidate(false);
        dataBinding.idLineview.setWaveLineColor(Color.WHITE);
        dataBinding.idLineview.setInnerBgColor(Color.parseColor("#22ffffff"));
        dataBinding.idLineview.setCatchValueListener(new WaveLineView.CatchValueListener() {
            @Override
            public int catchValue() {
                return (int) (Math.random() * 100);
            }
        });
//        dataBinding.idLineview.setWaveCallBack(new WaveLineView.WaveCallBack() {
//            @Override
//            public void onFirstDraw(int size) {
//                myValue = new int[size];
//            }
//
//            @Override
//            public void changeValuesAndInvalidate(int value) {
//            }
//
//            @Override
//            public Integer onValueChange(Integer value, Integer lastValues, int index, int size) {
//                Log.i(TAG, "onValueChange: value = " + value);
//                if (myIndex == -1) {
//                    return WaveLineView.EMPTY_VALUE;
//                }
//
//                if (myIndex == size) {
//                    myIndex = 0;
//                }
//
//                if (index > myIndex) {
//                    return WaveLineView.EMPTY_VALUE;
//                } else if (index < myIndex) {
//                    return lastValues;
//                } else {
//                    return myValue[index];
//                }
//
//
//            }
//        });

    }
}