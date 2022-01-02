package com.kavinraj.cse535individualassignment1;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.*;
import java.util.ArrayList;

import static java.lang.Math.abs;

public class MainActivity extends AppCompatActivity {
    Button buttonsymptom;
    databaseclass myDb;
    Camera cam;
    FrameLayout frameLayout;
    Button recordbt;
    surfacecamm surfacecamm;
    Button heartratebutton;
    Button respratebutton;
    TextView heartratetv;
    TextView respratetv;
    Button buttonuploadsigns;
    private Uri U;
    private int w = 9;
    long start;
    private boolean upload_Signs_tapped = false;
    private static final int v = 101;

    private boolean live_heart_rate_process = false;
    private boolean live_resp_rate_process = false;


    private MediaRecorder m;
    private boolean isRec = false;
    public float h;
    public float r;
    public float a = 0;


    private String rootPath = Environment.getExternalStorageDirectory().getPath();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setTitle("Diashield");

        handlePermissions(this);
        myDb = new databaseclass(this);
        buttonsymptom = (Button) findViewById(R.id.symptoms);
        heartratebutton = (Button) findViewById(R.id.heart);
        respratebutton = (Button) findViewById(R.id.lungs);
        buttonuploadsigns = (Button) findViewById(R.id.uploadsigns);
        buttonuploadsigns.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDb.addData(1, h, r,0,0,0,0,0,0,0,0,0,0, false);
            }
        });
        buttonsymptom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(MainActivity.this, symptompage.class);
                startActivity(intent1);
            }
        });
        frameLayout = (FrameLayout) findViewById(R.id.cameraid);
        heartratetv = (TextView) findViewById(R.id.showhr);
        respratetv = (TextView) findViewById(R.id.showrr);


        cam = Camera.open();
        surfacecamm = new surfacecamm(this, cam);
        frameLayout.addView(surfacecamm);
        Button captureButton = (Button) findViewById(R.id.recordbutton);


        if (!hasCamera()) {
            recordbt.setEnabled(false);
        }


        handlePermissions(MainActivity.this);



        heartratebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                File videoFile = new File(rootPath + "/heart_rate.mp4");
                U = Uri.fromFile(videoFile);


                livebreatherate(videoFile);
            }
        });


        respratebutton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                if (live_resp_rate_process == true) {
                    Toast.makeText(MainActivity.this, "Please wait for some time!",
                            Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Place the phone on your chest ", Toast.LENGTH_LONG).show();
                    live_resp_rate_process = true;
                    respratetv.setText("Calculating...");
                    Intent accelIntent = new Intent(MainActivity.this, respiratory_rate.class);
                    startService(accelIntent);
                }
            }
        });
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (live_heart_rate_process == true) {
                            Toast.makeText(MainActivity.this, "Please wait for some time!",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            if (surfacecamerarecorder()) {


                                m.start();
                                System.out.println("Recording !!!!!");
                                Toast.makeText(getApplicationContext(), "Recording Started", Toast.LENGTH_SHORT).show();



                                Log.i("Camera", "Stop");

                            } else {

                                releaseMediaRecorder();

                            }

                        }
                    }
                }
        );
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                recievefunc(intent);

            }

        }, new IntentFilter("broadcastingAccelData"));



        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle b = intent.getExtras();
                float heartRate = 0;
                int fail = 0;

                for (int i = 0; i < w; i++) {

                    ArrayList<Integer> heartData = null;
                    heartData = b.getIntegerArrayList("heartData" + i);


                    ArrayList<Integer> denoisedRedness = denoise(heartData, 5);


                    float zeroCrossings = peakFinding(denoisedRedness);
                    heartRate += zeroCrossings / 2;
                    Log.i("log", "heart rate for " + i + ": " + zeroCrossings / 2);







                }

                heartRate = (heartRate * 12) / w;
                Log.i("log", "Final heart rate: " + heartRate);
                heartratetv.setText(heartRate + "");
                h = heartRate;

                live_heart_rate_process = false;
                Toast.makeText(MainActivity.this, "Heart rate calculated!", Toast.LENGTH_SHORT).show();
                System.gc();
                b.clear();

            }
        }, new IntentFilter("broadcastingHeartData"));


    }

    private void recievefunc(Intent intent) {
        Bundle b = intent.getExtras();
        BreathingRateDetector runnable = new BreathingRateDetector(b.getIntegerArrayList("accelValuesX"));

        Thread thread = new Thread(runnable);
        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        respratetv.setText(runnable.breathingRate + "");
        r = runnable.breathingRate;
        System.out.println(r + "hello");

        Toast.makeText(MainActivity.this, "Respiratory rate calculated!", Toast.LENGTH_SHORT).show();
        live_resp_rate_process = false;
        b.clear();
        System.gc();
    }

    private void livebreatherate(File videoFile) {
        if (live_heart_rate_process == true) {
            Toast.makeText(MainActivity.this, "Please wait for some time!",
                    Toast.LENGTH_SHORT).show();
        } else if (videoFile.exists()) {
            live_heart_rate_process = true;
            heartratetv.setText("Calculating...");

            heartint();

        } else {
            Toast.makeText(MainActivity.this, "Please record the video  first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void heartint() {
        start = System.currentTimeMillis();
        System.gc();
        Intent heartIntent = new Intent(MainActivity.this, heartrate.class);
        startService(heartIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        upload_Signs_tapped = false;
    }


    public class BreathingRateDetector implements Runnable {

        public float breathingRate;
        ArrayList<Integer> accelValuesX;

        BreathingRateDetector(ArrayList<Integer> accelValuesX) {
            this.accelValuesX = accelValuesX;
        }

        @Override
        public void run() {




            ArrayList<Integer> accelValuesXDenoised = denoise(accelValuesX, 10);




            int zeroCrossings = peakFinding(accelValuesXDenoised);
            breathingRate = (zeroCrossings * 60) / 90;
            Log.i("log", "Respiratory rate" + breathingRate);
        }

    }


    public ArrayList<Integer> denoise(ArrayList<Integer> data, int filter) {

        ArrayList<Integer> movingAvgArr = new ArrayList<>();
        arylst(data, filter, movingAvgArr);

        return movingAvgArr;

    }

    private void arylst(ArrayList<Integer> data, int filter, ArrayList<Integer> movingAvgArr) {
        int movingAvg = 0;

        for (int i = 0; i < data.size(); i++) {
            movingAvg += data.get(i);
            if (i + 1 < filter) {
                continue;
            }
            movingAvgArr.add((movingAvg) / filter);
            movingAvg -= data.get(i + 1 - filter);
        }
    }


    public int peakFinding(ArrayList<Integer> data) {

        int diff, prev, slope = 0, zeroCrossings = 0;
        int j = 0;
        prev = data.get(0);


        while (slope == 0 && j + 1 < data.size()) {
            diff = data.get(j + 1) - data.get(j);
            if (diff != 0) {
                slope = diff / abs(diff);
            }
            j++;
        }


        zeroCrossings = pkfnd(data, prev, slope, zeroCrossings);

        return zeroCrossings;
    }

    private int pkfnd(ArrayList<Integer> data, int prev, int slope, int zeroCrossings) {
        int diff;
        for (int i = 1; i < data.size(); i++) {

            diff = data.get(i) - prev;
            prev = data.get(i);

            if (diff == 0) continue;

            int currSlope = diff / abs(diff);

            if (currSlope == -1 * slope) {
                slope *= -1;
                zeroCrossings++;
            }
        }
        return zeroCrossings;
    }


    public static void handlePermissions(Activity activity) {

        int storagePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int REQUEST_EXTERNAL_STORAGE = 1;

        String[] PERMISSIONS = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA

        };

        if (storagePermission != PackageManager.PERMISSION_GRANTED) {
            Log.i("log", "Read/Write Permissions needed!");
        }

        ActivityCompat.requestPermissions(
                activity,
                PERMISSIONS,
                REQUEST_EXTERNAL_STORAGE
        );

        Log.i("log", "Permissions Granted!");

    }

    private boolean hasCamera() {

        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            return false;
        }
    }


    public void startRecording() {

        File mediaFile = new File(rootPath + "/heart_rate.mp4");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 45);

        U = Uri.fromFile(mediaFile);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, U);
        startActivityForResult(intent, v);
    }


    private boolean surfacecamerarecorder() {

        cam = Camera.open();
        Camera.Parameters p = cam.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam.setParameters(p);
        m = new MediaRecorder();


        cam.unlock();
        m.setCamera(cam);


        m.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        m.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        m.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

        File mediaFile = new File(rootPath + "/heart_rate.mp4");

        m.setMaxDuration(45000); // 45 seconds
        m.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    mr.stop();
                    Camera.Parameters p = cam.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    cam.setParameters(p);
                    Toast.makeText(getApplicationContext(), "Recording completed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        m.setOutputFile(mediaFile.toString());


        m.setPreviewDisplay(surfacecamm.getHolder().getSurface());


        try {
            m.prepare();
        } catch (IllegalStateException e) {
            Log.d("Error", "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d("Error", "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (m != null) {
            m.reset();
            m.release();
            m = null;
            cam.lock();
        }
    }

    private void releaseCamera() {
        if (cam != null) {
            cam.release();
            cam = null;
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        boolean deleteFile = false;
        super.onActivityResult(requestCode, resultCode, data);
        resultextract(requestCode, resultCode, data, deleteFile);
    }

    private void resultextract(int requestCode, int resultCode, Intent data, boolean deleteFile) {
        if (requestCode == v) {
            if (resultCode == RESULT_OK) {

                MediaMetadataRetriever videoRetriever = new MediaMetadataRetriever();
                FileInputStream input = null;
                try {
                    input = new FileInputStream(U.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                try {
                    videoRetriever.setDataSource(input.getFD());
                } catch (IOException e) {
                    e.printStackTrace();
                }


                String timeString = videoRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long time = Long.parseLong(timeString) / 1000;


                if (time < 45) {

                    Toast.makeText(this,
                            "Please record for at least 45 seconds! ", Toast.LENGTH_SHORT).show();
                    deleteFile = true;
                } else {
                    Toast.makeText(this, "Video has been saved to:\n" +
                            data.getData(), Toast.LENGTH_SHORT).show();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Video recording cancelled.",
                        Toast.LENGTH_SHORT).show();
                deleteFile = true;
            } else {
                Toast.makeText(this, "Failed to record video",
                        Toast.LENGTH_SHORT).show();
            }

            if (deleteFile) {
                File fdelete = new File(U.getPath());

                if (fdelete.exists()) {
                    if (fdelete.delete()) {
                        System.out.println("Recording deleted");
                    }
                }
            }
            U = null;
        }
    }

}