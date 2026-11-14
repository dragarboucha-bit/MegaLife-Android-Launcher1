package com.megalife.samp.game;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.megalife.samp.R;
import com.megalife.samp.config.ServerConfig;
import com.megalife.samp.storage.GameFileManager;

import java.io.File;

/**
 * Native GTA San Andreas + SA-MP Client Engine Activity (ALYN-SA-MP-FIX Architecture).
 * Loads libmain.so / libsamp.so and initializes the locked connection to mega-life.top:7777.
 */
public class GTASA extends AppCompatActivity implements SurfaceHolder.Callback {

    private static boolean nativeLibraryLoaded = false;

    static {
        try {
            System.loadLibrary("samp");
            nativeLibraryLoaded = true;
        } catch (UnsatisfiedLinkError e) {
            nativeLibraryLoaded = false;
        }
    }

    private SurfaceView gameSurfaceView;
    private TextView tvHudConnectionInfo;
    private String playerNickname = "MegaLife_Player";

    // JNI hooks into C++ SA-MP engine (main.cpp)
    public native void nativeInitSampEngine(String gameRootDir, String playerNickname, String lockedHost, int lockedPort);
    public native String nativeGetLockedEndpoint();
    public native void nativeOnSurfaceCreated();
    public native void nativeOnSurfaceDestroyed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_gtasa);

        gameSurfaceView = findViewById(R.id.gameSurfaceView);
        tvHudConnectionInfo = findViewById(R.id.tvHudConnectionInfo);

        if (getIntent() != null && getIntent().hasExtra("PLAYER_NICKNAME")) {
            playerNickname = getIntent().getStringExtra("PLAYER_NICKNAME");
        }

        // Enforce locked settings.ini prior to engine boot
        GameFileManager.writeLockedSampSettings(this, playerNickname);

        gameSurfaceView.getHolder().addCallback(this);

        File rootDir = GameFileManager.getGameRootDir(this);
        String rootPath = rootDir != null ? rootDir.getAbsolutePath() : "/sdcard/Android/data/com.megalife.samp/files";

        if (nativeLibraryLoaded) {
            nativeInitSampEngine(rootPath, playerNickname, ServerConfig.SERVER_HOST, ServerConfig.SERVER_PORT);
            String endpoint = nativeGetLockedEndpoint();
            tvHudConnectionInfo.setText("Connected: " + endpoint + " as " + playerNickname);
        } else {
            tvHudConnectionInfo.setText("Target: " + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT + " (" + playerNickname + ")");
            attemptExternalSampBridgeIfInstalled();
        }
    }

    /**
     * If external SA-MP runtime core is also installed on the device, bridge samp:// intent seamlessly
     * while keeping our locked settings.ini active.
     */
    private void attemptExternalSampBridgeIfInstalled() {
        try {
            Uri sampUri = Uri.parse("samp://" + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT);
            Intent intent = new Intent(Intent.ACTION_VIEW, sampUri);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this,
                        "Mega Life Client Initialized: " + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT,
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (nativeLibraryLoaded) {
            nativeOnSurfaceCreated();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (nativeLibraryLoaded) {
            nativeOnSurfaceDestroyed();
        }
    }
}
