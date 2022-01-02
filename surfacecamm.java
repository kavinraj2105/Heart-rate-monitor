package com.kavinraj.cse535individualassignment1;

import android.content.Context;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.io.IOException;

public class surfacecamm extends SurfaceView implements SurfaceHolder.Callback {
    Camera cam;
    SurfaceHolder hold;
    public surfacecamm(Context context, Camera cam) {
        super(context);
        this.cam = cam;
        hold = getHolder();
        hold.addCallback(this);
    }



    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {



    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder)
    {
        cam = Camera.open();
        Camera.Parameters para = cam.getParameters();
        orientation(holder, para);

    }

    private void orientation(SurfaceHolder holder, Camera.Parameters para) {
        if(this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            para.set("Orientation","Portrait");
            cam.setDisplayOrientation(90);
            para.setRotation(90);
        }
        else
        {
            para.set("Orientation","Landscape");
            cam.setDisplayOrientation(0);
            para.setRotation(0);
        }
        cam.setParameters(para);

        try {
            cam.setPreviewDisplay(holder);
            cam.startPreview();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
