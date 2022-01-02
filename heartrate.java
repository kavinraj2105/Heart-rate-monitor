package com.kavinraj.cse535individualassignment1;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class heartrate extends Service {

    private Bundle b = new Bundle();
    private String root = Environment.getExternalStorageDirectory().getPath();
    private int w = 9;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.gc();
        Toast.makeText(this, "Processing the video file", Toast.LENGTH_LONG).show();
        Log.i("log", "Heart Rate service began");

        //Multithreading for frame extracting from 9 5-second windows of heart rate video
        HeartRateWindowSplitting runnable = new HeartRateWindowSplitting();
        Thread thread = new Thread(runnable);
        thread.start();

        return START_STICKY;
    }


    public class HeartRateWindowSplitting implements Runnable {
        @Override
        public void run() {

            ExecutorService executor = Executors.newFixedThreadPool(6);
            List<FrameExtractor> taskList = new ArrayList<>();

            //Only 45 seconds of video processed as 9 5-second windows, even if video length exceeds 45 seconds
            for (int i = 0; i < w; i++) {
                FrameExtractor frameExtractor = new FrameExtractor(i * 5);
                taskList.add(frameExtractor);
            }

            List<Future<ArrayList<Integer>>> resultList = null;
            try {
                resultList = executor.invokeAll(taskList);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            executor.shutdown();
            System.gc();

            for (int i = 0; i < resultList.size(); i++) {

                Future<ArrayList<Integer>> future = resultList.get(i);
                try {
                    b.putIntegerArrayList("heartData" + i, future.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    e.getCause();
                }
            }
            //Service stopped once frames are extracted
            stopSelf();
        }
    }


    public class FrameExtractor implements Callable<ArrayList<Integer>> {
        private int startTime;

        FrameExtractor(int startTime){
            this.startTime = startTime;
        }


        @RequiresApi(api = Build.VERSION_CODES.P)
        private ArrayList<Integer> getFrames(){
            Bitmap bitmap = null;

            try {
                String path = root + "/heart_rate.mp4";
                ArrayList<Integer> avgColorArr = new ArrayList<>();
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(path);
                AndroidFrameConverter converterToBitMap = new AndroidFrameConverter();
                grabber.start();
                grabber.setTimestamp(startTime*1000000);
                double frameRate = grabber.getFrameRate();

                grabberframe(avgColorArr, grabber, converterToBitMap, frameRate);

                return avgColorArr;

            } catch(Exception e) {
                Log.e("FrameError",e.toString());
                System.out.println(e.toString());
            }
            return null;
        }

        private void grabberframe(ArrayList<Integer> avgColorArr, FFmpegFrameGrabber grabber, AndroidFrameConverter converterToBitMap, double frameRate) throws FrameGrabber.Exception {
            Bitmap bitmap;
            for (int i = 0; i < 5* frameRate;) {
                Frame frame = grabber.grabFrame();
                if (frame == null) {
                    break;
                }
                if (frame.image == null) {
                    continue;
                }
                i++;
                Log.i("log", "Processing frame " + i);
                System.gc();


                bitmap = converterToBitMap.convert(frame);
                int avgColor = getAverageColor(bitmap);

                avgColorArr.add(avgColor);
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public ArrayList<Integer> call() {

            ArrayList<Integer> rednessData = new ArrayList<>();
            try {
                rednessData = getFrames();

            } catch (Exception e) {
                e.printStackTrace();
            }
            return rednessData;
        }
    }


    private int getAverageColor(Bitmap bitmap){

        long redBucket = 0;
        long pixelCount = 0;


        for (int y = 0; y < bitmap.getHeight(); y+=5) {
            for (int x = 0; x < bitmap.getWidth(); x+=5) {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                redBucket += Color.red(c);
            }
        }

        return (int)(redBucket / pixelCount);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i("log", "Heart Service stopping");
                Intent intent = new Intent("broadcastingHeartData");
                intent.putExtras(b);
                LocalBroadcastManager.getInstance(heartrate.this).sendBroadcast(intent);
                b.clear();
                System.gc();
            }
        });

        thread.start();
    }


}
