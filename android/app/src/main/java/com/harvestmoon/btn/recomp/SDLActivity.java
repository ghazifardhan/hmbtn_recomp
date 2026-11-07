package com.harvestmoon.btn.recomp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class SDLActivity extends org.libsdl.app.SDLActivity {
    private static final String TAG = "HMRecomp";

    private TouchControllerView mTouchControllerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applySavedOrientation();
        extractAssetsIfNeeded();
        super.onCreate(savedInstanceState);
        setupTouchControls();
    }

    public void applyOrientation(String choice) {
        runOnUiThread(() -> {
            if (TouchControllerView.ORIENTATION_PORTRAIT.equals(choice)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else if (TouchControllerView.ORIENTATION_AUTO.equals(choice)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        });
    }

    @Override
    public void setOrientationBis(int w, int h, boolean resizable, String hint) {
        SharedPreferences prefs = getSharedPreferences(TouchControllerView.PREFS_NAME, Context.MODE_PRIVATE);
        String orient = prefs.getString(TouchControllerView.KEY_ORIENTATION, TouchControllerView.ORIENTATION_LANDSCAPE);
        applyOrientation(orient);
    }

    private void applySavedOrientation() {
        SharedPreferences prefs = getSharedPreferences(TouchControllerView.PREFS_NAME, Context.MODE_PRIVATE);
        String orient = prefs.getString(TouchControllerView.KEY_ORIENTATION, TouchControllerView.ORIENTATION_LANDSCAPE);
        applyOrientation(orient);
    }

    private void setupTouchControls() {
        if (mLayout != null) {
            mTouchControllerView = new TouchControllerView(this);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            );
            mLayout.addView(mTouchControllerView, lp);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityCreated = false;
        mSDLMainFinished = false;
    }


    private void extractAssetsIfNeeded() {
        File filesDir = getFilesDir();
        if (filesDir == null) {
            return;
        }

        String[] assetsToExtract = {
            "game.toml",
            "Harvest Moon - Back to Nature.cue",
            "Harvest Moon - Back to Nature.bin",
            "bios/openbios.bin"
        };

        for (String assetName : assetsToExtract) {
            File outFile = new File(filesDir, assetName);
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try {
                // If it's not game.toml, don't re-copy if it already exists and has data
                if (!assetName.equals("game.toml") && outFile.exists() && outFile.length() > 0) {
                    Log.i(TAG, "Asset already extracted: " + assetName + " (" + outFile.length() + " bytes)");
                    continue;
                }

                Log.i(TAG, "Extracting asset: " + assetName + " -> " + outFile.getAbsolutePath());
                try (InputStream in = getAssets().open(assetName);
                     OutputStream out = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[65536];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                }
                Log.i(TAG, "Extracted successfully: " + assetName + " (" + outFile.length() + " bytes)");
            } catch (Exception e) {
                Log.e(TAG, "Failed to extract asset: " + assetName, e);
            }
        }
    }

    @Override
    protected String[] getLibraries() {
        return new String[] {
            "SDL3",
            "main"
        };
    }
}
