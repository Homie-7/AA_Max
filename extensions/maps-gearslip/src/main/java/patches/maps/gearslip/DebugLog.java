package patches.maps.gearslip;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Diagnostic trace of the car session, written to logcat and to Download/AA_Max on the phone.
 * It records host geometry, gestures, templates and view ids. It never records search text,
 * destinations, intents' data or the vehicle's position.
 */
final class DebugLog {
    private static final String TAG = "MapsNativeCar";
    private static final int MAX_LINES = 20000;
    private static Handler handler;
    private static OutputStream out;
    private static int lines;
    private static SimpleDateFormat clock;
    private DebugLog() {}

    static synchronized void init(Context context) {
        if (handler != null || context == null) return;
        Context app = context.getApplicationContext() == null ? context : context.getApplicationContext();
        HandlerThread thread = new HandlerThread("MapsCarLog");
        thread.start();
        handler = new Handler(thread.getLooper());
        handler.post(() -> open(app));
    }

    private static void open(Context context) {
        try {
            clock = new SimpleDateFormat("HH:mm:ss.SSS", Locale.ROOT);
            String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ROOT).format(new Date());
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, "aa-max-log-" + stamp + ".txt");
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/AA_Max");
            Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new IllegalStateException("Downloads refused the log file");
            out = context.getContentResolver().openOutputStream(uri, "w");
            write(System.currentTimeMillis(), "device " + Build.MANUFACTURER + " " + Build.MODEL + " sdk " + Build.VERSION.SDK_INT
                    + " maps " + version(context, context.getPackageName())
                    + " androidauto " + version(context, "com.google.android.projection.gearhead"));
        } catch (Exception | Error error) {
            out = null;
            Log.w(TAG, "Diagnostic log file unavailable", error);
        }
    }

    private static String version(Context context, String name) {
        try { return context.getPackageManager().getPackageInfo(name, 0).versionName; }
        catch (Exception missing) { return "unknown"; }
    }

    static void log(String line) {
        Log.i(TAG, line);
        Handler target = handler;
        if (target == null) return;
        long now = System.currentTimeMillis();
        target.post(() -> write(now, line));
    }

    static void log(String line, Throwable error) { log(line + "\n" + Log.getStackTraceString(error)); }

    private static void write(long time, String line) {
        if (out == null || lines >= MAX_LINES) return;
        try {
            lines++;
            out.write((clock.format(new Date(time)) + " " + line + "\n").getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception error) {
            out = null;
            Log.w(TAG, "Diagnostic log file closed", error);
        }
    }
}
