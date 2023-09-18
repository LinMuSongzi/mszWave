package com.musongzi.waveline.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.os.Bundle;

import com.musongzi.waveline.R;
import com.musongzi.waveline.databinding.ActivityPathMoveBinding;
import com.musongzi.waveline.databinding.ActivityProcessWaveBinding;
import com.musongzi.waveline.ui.business.ICanvasTask;
import com.musongzi.waveline.ui.business.ProcessWaveTask;

public class ProcessWaveActivity extends AppCompatActivity {


    ActivityProcessWaveBinding dataBinding;

    int postion = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_process_wave);
        ProcessWaveTask processWaveTask = dataBinding.idLineview.getCanvasTask();
//        processWaveTask.setNumber(10);
        processWaveTask.setInnerBgColor(Color.parseColor("#22ffffff"));
        processWaveTask.setPrepareListener(new ICanvasTask.IPrepare() {
            @Override
            public void prepareNow(@Nullable Object para) {
                postion = 0;
                new Thread() {
                    @Override
                    public void run() {
                        while (postion < processWaveTask.getMaxSize() / 2) {
                            processWaveTask.changeSingleValue((int) ((Math.random() * 0.1f + 0.4f) * dataBinding.idLineview.getHeight()), postion);
                            postion++;
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }.start();
            }
        });
    }
}