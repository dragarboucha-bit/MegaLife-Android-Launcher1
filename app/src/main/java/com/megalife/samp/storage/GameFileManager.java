package com.megalife.samp.storage;

import android.content.Context;
import com.megalife.samp.config.ServerConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Manages GTA San Andreas game cache files, custom TXD/DFF server packages,
 * and enforces the locked settings.ini pointing exclusively to mega-life.top:7777.
 */
public class GameFileManager {

    public interface DownloadCallback {
        void onProgress(int percent, String statusText);
        void onCompleted();
        void onError(String errorMessage);
    }

    public static File getGameRootDir(Context context) {
        File dir = context.getExternalFilesDir(null);
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static boolean areBaseGameFilesInstalled(Context context) {
        File root = getGameRootDir(context);
        if (root == null) return false;
        File texdb = new File(root, "texdb");
        File samp = new File(root, "SAMP");
        return texdb.exists() && samp.exists();
    }

    /**
     * Initializes default SAMP folders and writes the locked settings.ini
     * so the native C++ client connects strictly to mega-life.top:7777.
     */
    public static void writeLockedSampSettings(Context context, String playerName) {
        try {
            File root = getGameRootDir(context);
            File sampDir = new File(root, "SAMP");
            if (!sampDir.exists()) {
                sampDir.mkdirs();
            }
            File texdbDir = new File(root, "texdb");
            if (!texdbDir.exists()) {
                texdbDir.mkdirs();
            }

            File settingsIni = new File(sampDir, "settings.ini");
            FileWriter writer = new FileWriter(settingsIni, false);
            writer.write("[client]
");
            writer.write("name = " + playerName + "
");
            writer.write("host = " + ServerConfig.SERVER_HOST + "
");
            writer.write("port = " + ServerConfig.SERVER_PORT + "
");
            writer.write("password = 
");
            writer.write("version = 0.3.7

");
            writer.write("[gui]
");
            writer.write("Font = Arial.ttf
");
            writer.write("FontSize = 30.0
");
            writer.write("FontOutline = 2
");
            writer.write("ChatMaxLines = 12
");
            writer.write("fps = 60
");
            writer.write("timestamp = 1
");
            writer.write("androidkeyboard = 0
");
            writer.write("voice = 1
");
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downloadAndExtractZip(Context context, String zipUrl, DownloadCallback callback) {
        File root = getGameRootDir(context);
        File tempZip = new File(root, "megalife_package_temp.zip");
        try {
            callback.onProgress(0, "Connecting to Mega Life CDN...");
            URL url = new URL(zipUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                callback.onError("CDN returned HTTP " + responseCode + ". Check CDN URL in Settings.");
                return;
            }

            int fileLength = conn.getContentLength();
            InputStream input = new BufferedInputStream(conn.getInputStream());
            FileOutputStream output = new FileOutputStream(tempZip);

            byte[] buffer = new byte[8192];
            long total = 0;
            int count;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (fileLength > 0) {
                    int pct = (int) ((total * 100L) / fileLength);
                    callback.onProgress(pct, "Downloading Mega Life Assets (" + pct + "%)...");
                }
                output.write(buffer, 0, count);
            }
            output.flush();
            output.close();
            input.close();

            callback.onProgress(100, "Extracting GTA SA & TXD/DFF Archives...");
            extractZipSafe(tempZip, root);
            if (tempZip.exists()) {
                tempZip.delete();
            }

            callback.onCompleted();
        } catch (Exception e) {
            if (tempZip.exists()) {
                tempZip.delete();
            }
            callback.onError("Download error: " + e.getMessage());
        }
    }

    private static void extractZipSafe(File zipFile, File targetRoot) throws Exception {
        String canonicalRoot = targetRoot.getCanonicalPath();
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(targetRoot, entry.getName());
                if (!outFile.getCanonicalPath().startsWith(canonicalRoot)) {
                    throw new SecurityException("Zip Slip path traversal blocked.");
                }
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null && !parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        }
    }
}
