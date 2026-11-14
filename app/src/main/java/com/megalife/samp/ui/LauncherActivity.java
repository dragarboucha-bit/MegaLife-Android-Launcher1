package com.megalife.samp.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.megalife.samp.R;
import com.megalife.samp.config.ServerConfig;
import com.megalife.samp.game.GTASA;
import com.megalife.samp.network.SampQueryClient;
import com.megalife.samp.storage.GameFileManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Official Mega Life Roleplay Android Launcher Activity.
 * Exclusively queries and launches connections to mega-life.top:7777.
 */
public class LauncherActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MegaLifeLauncherPrefs";
    private static final String KEY_NICKNAME = "player_nickname";
    private static final String KEY_CDN_URL = "custom_cdn_url";
    private static final long STATUS_REFRESH_INTERVAL_MS = 10000L;

    private View statusDot;
    private TextView tvServerStatusBadge;
    private TextView tvServerHostname;
    private TextView tvServerGamemode;
    private TextView tvPlayersCount;
    private TextView tvPing;
    private ProgressBar pbSlotsBar;
    private ImageButton btnRefreshStatus;

    private EditText etNickname;
    private TextView tvFilesStatus;
    private TextView btnConfigureCacheUrl;
    private ProgressBar pbDownloadProgress;
    private Button btnPlay;
    private LinearLayout btnDiscord;

    private SharedPreferences prefs;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isDownloading = false;

    private final Runnable autoRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshServerStatus();
            mainHandler.postDelayed(this, STATUS_REFRESH_INTERVAL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        bindViews();
        loadSavedPreferences();
        setupAnimations();
        updateFileStatusUI();

        btnRefreshStatus.setOnClickListener(v -> {
            Toast.makeText(this, "Refreshing Mega Life Server status...", Toast.LENGTH_SHORT).show();
            refreshServerStatus();
        });

        btnConfigureCacheUrl.setOnClickListener(v -> showCdnConfigurationDialog());

        btnPlay.setOnClickListener(v -> handlePlayButtonClick());

        btnDiscord.setOnClickListener(v -> openDiscordInvite());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainHandler.removeCallbacks(autoRefreshRunnable);
        mainHandler.post(autoRefreshRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainHandler.removeCallbacks(autoRefreshRunnable);
    }

    private void bindViews() {
        statusDot = findViewById(R.id.statusDot);
        tvServerStatusBadge = findViewById(R.id.tvServerStatusBadge);
        tvServerHostname = findViewById(R.id.tvServerHostname);
        tvServerGamemode = findViewById(R.id.tvServerGamemode);
        tvPlayersCount = findViewById(R.id.tvPlayersCount);
        tvPing = findViewById(R.id.tvPing);
        pbSlotsBar = findViewById(R.id.pbSlotsBar);
        btnRefreshStatus = findViewById(R.id.btnRefreshStatus);

        etNickname = findViewById(R.id.etNickname);
        tvFilesStatus = findViewById(R.id.tvFilesStatus);
        btnConfigureCacheUrl = findViewById(R.id.btnConfigureCacheUrl);
        pbDownloadProgress = findViewById(R.id.pbDownloadProgress);
        btnPlay = findViewById(R.id.btnPlay);
        btnDiscord = findViewById(R.id.btnDiscord);
    }

    private void loadSavedPreferences() {
        String savedNick = prefs.getString(KEY_NICKNAME, "");
        if (!savedNick.isEmpty()) {
            etNickname.setText(savedNick);
        }
    }

    private void setupAnimations() {
        AlphaAnimation pulse = new AlphaAnimation(0.4f, 1.0f);
        pulse.setDuration(900);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        statusDot.startAnimation(pulse);
    }

    private void updateFileStatusUI() {
        boolean installed = GameFileManager.areBaseGameFilesInstalled(this);
        if (installed) {
            tvFilesStatus.setText("✓ GTA SA & Mega Life Client Ready");
            tvFilesStatus.setTextColor(getResources().getColor(R.color.status_online));
        } else {
            tvFilesStatus.setText("⚡ Ready to Connect (Tap CDN Settings for Auto-Cache/TXD)");
            tvFilesStatus.setTextColor(getResources().getColor(R.color.text_gray));
        }
    }

    private void refreshServerStatus() {
        tvServerStatusBadge.setText("QUERYING MEGA-LIFE.TOP:7777...");
        executor.execute(() -> {
            SampQueryClient.ServerStatus status = SampQueryClient.queryMegaLifeServer(3500);
            mainHandler.post(() -> renderServerStatus(status));
        });
    }

    private void renderServerStatus(SampQueryClient.ServerStatus status) {
        if (status.online) {
            statusDot.setBackgroundResource(R.drawable.dot_online);
            tvServerStatusBadge.setText("ONLINE • LIVE");
            tvServerStatusBadge.setTextColor(getResources().getColor(R.color.status_online));
            tvServerHostname.setText(status.hostname);
            tvServerGamemode.setText(status.gamemode + " • " + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT);
            tvPlayersCount.setText(status.players + " / " + status.maxPlayers);
            tvPing.setText(status.pingMs + " ms");

            int pct = status.maxPlayers > 0 ? (status.players * 100) / status.maxPlayers : 0;
            pbSlotsBar.setProgress(Math.min(100, Math.max(2, pct)));
        } else {
            statusDot.setBackgroundResource(R.drawable.dot_offline);
            tvServerStatusBadge.setText("OFFLINE / UNREACHABLE");
            tvServerStatusBadge.setTextColor(getResources().getColor(R.color.status_offline));
            tvServerHostname.setText(ServerConfig.SERVER_NAME);
            tvServerGamemode.setText("Endpoint: " + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT + " (Waiting for response)");
            tvPlayersCount.setText("0 / 500");
            tvPing.setText("-- ms");
            pbSlotsBar.setProgress(0);
        }
    }

    private void handlePlayButtonClick() {
        if (isDownloading) {
            Toast.makeText(this, "Please wait for asset download to finish.", Toast.LENGTH_SHORT).show();
            return;
        }

        String nick = etNickname.getText().toString().trim();
        if (nick.length() < 3 || !nick.contains("_")) {
            etNickname.setError("Use Roleplay format: Firstname_Lastname");
            Toast.makeText(this, "Please enter a valid Roleplay name (e.g., Houssem_Jdidi)", Toast.LENGTH_LONG).show();
            return;
        }

        // Save player nickname and lock settings.ini to mega-life.top:7777
        prefs.edit().putString(KEY_NICKNAME, nick).apply();
        GameFileManager.writeLockedSampSettings(this, nick);

        // Launch the integrated SA-MP Native Game Engine Activity directly
        Toast.makeText(this, "Launching Mega Life Roleplay (" + ServerConfig.SERVER_HOST + ":" + ServerConfig.SERVER_PORT + ")...", Toast.LENGTH_SHORT).show();
        Intent gameIntent = new Intent(this, GTASA.class);
        gameIntent.putExtra("PLAYER_NICKNAME", nick);
        gameIntent.putExtra("SERVER_HOST", ServerConfig.SERVER_HOST);
        gameIntent.putExtra("SERVER_PORT", ServerConfig.SERVER_PORT);
        startActivity(gameIntent);
    }

    private void showCdnConfigurationDialog() {
        String currentUrl = prefs.getString(KEY_CDN_URL, ServerConfig.DEFAULT_GAME_CACHE_ZIP_URL);
        final EditText input = new EditText(this);
        input.setText(currentUrl);
        input.setSelection(currentUrl.length());
        input.setPadding(40, 30, 40, 30);

        new AlertDialog.Builder(this)
                .setTitle("Mega Life Auto-Installer & TXD/DFF Sync")
                .setMessage("Enter a direct ZIP download link (GTA SA cache or custom TXD/DFF mod pack) to download and extract directly into the Mega Life game folder:")
                .setView(input)
                .setPositiveButton("Download & Install Now", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    if (!url.isEmpty()) {
                        prefs.edit().putString(KEY_CDN_URL, url).apply();
                        startAssetDownload(url);
                    }
                })
                .setNeutralButton("Save URL Only", (dialog, which) -> {
                    String url = input.getText().toString().trim();
                    prefs.edit().putString(KEY_CDN_URL, url).apply();
                    Toast.makeText(this, "CDN URL saved.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startAssetDownload(String zipUrl) {
        isDownloading = true;
        btnPlay.setEnabled(false);
        pbDownloadProgress.setVisibility(View.VISIBLE);
        pbDownloadProgress.setProgress(0);

        executor.execute(() -> GameFileManager.downloadAndExtractZip(this, zipUrl, new GameFileManager.DownloadCallback() {
            @Override
            public void onProgress(int percent, String statusText) {
                mainHandler.post(() -> {
                    pbDownloadProgress.setProgress(percent);
                    tvFilesStatus.setText(statusText);
                });
            }

            @Override
            public void onCompleted() {
                mainHandler.post(() -> {
                    isDownloading = false;
                    btnPlay.setEnabled(true);
                    pbDownloadProgress.setVisibility(View.GONE);
                    updateFileStatusUI();
                    Toast.makeText(LauncherActivity.this, "Mega Life game files & TXD/DFF assets installed!", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String errorMessage) {
                mainHandler.post(() -> {
                    isDownloading = false;
                    btnPlay.setEnabled(true);
                    pbDownloadProgress.setVisibility(View.GONE);
                    tvFilesStatus.setText(errorMessage);
                    tvFilesStatus.setTextColor(getResources().getColor(R.color.status_offline));
                });
            }
        }));
    }

    private void openDiscordInvite() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ServerConfig.DISCORD_INVITE_URL));
            startActivity(browserIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Discord link: " + ServerConfig.DISCORD_INVITE_URL, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
