package com.example.akaszuba.flashlight;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        log = (TextView)findViewById(R.id.log);
        initialize();
        flashlightEnabled= false;
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(camera != null){
            camera.release();
            camera = null;
        }
    }

    TextView log;
    Camera camera;
    boolean flashlightEnabled;

    private void appendLog(String txt){
        log.append("\r\n");
        log.append(txt);
    }

    public void onButtonClicked(View view) {
        appendLog("Button clicked, flashlight enabled:" + flashlightEnabled);
        if (camera != null) {
            try {
                if (flashlightEnabled) {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                    flashlightEnabled = false;
                } else {
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    camera.startPreview();
                    flashlightEnabled = true;
                }
            } catch (Exception e) {
                if (camera != null) {
                    camera.release();
                    camera = null;
                }
            }
        } else {
            appendLog("Error Camera not initialized.");
        }

    }

    private void initialize(){
        appendLog("Initialization...");
        int cameraId = getCameraId();

        if(cameraId == -1){
            appendLog("No flashlight available.");
        }else{
            camera = Camera.open(cameraId);
        }
    }

    private int getCameraId() {
        appendLog("Getting camera id...");
        int cameraToBeUsed = -1;

        boolean hasCamera = this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        appendLog("Has camera:" + hasCamera);
        int numberOfCams = Camera.getNumberOfCameras();
        appendLog("Number of cameras:" + numberOfCams);

        if (hasCamera && numberOfCams > 0) {
            for (int i = 0; i < numberOfCams; i++) {
                appendLog("Camera #" + i);
                Camera currentCamera = null;
                try {
                    currentCamera = Camera.open(i);

                    Camera.Parameters parameters = currentCamera.getParameters();
                    List<String> supportedFlashModes = parameters.getSupportedFlashModes();

                    if (supportedFlashModes != null){
                        for (String s : supportedFlashModes) {
                            appendLog("Flash Mode:" + s);
                        }

                        if(supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)){
                            cameraToBeUsed = i;
                        }
                    }

                } catch (Exception e) {
                    appendLog(e.toString());
                } finally {
                    if (currentCamera != null) {
                        currentCamera.release();
                        currentCamera = null;
                    }
                }
            }
        } else {
            Toast.makeText(this, "Your device has no camera.", Toast.LENGTH_LONG);
        }

        appendLog("Camera to be used:" + cameraToBeUsed);
        return cameraToBeUsed;
    }
}
