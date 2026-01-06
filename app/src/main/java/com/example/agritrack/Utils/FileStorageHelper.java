package com.example.agritrack.Utils;

import android.content.Context;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileStorageHelper {

    private Context context;

    public FileStorageHelper(Context context) {
        this.context = context;
    }

    public void logActivity(String activity) {
        String timestamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        ).format(new Date());

        String logEntry = timestamp + " - " + activity + "\n";

        try (FileOutputStream fos =
                     context.openFileOutput("activity_log.txt", Context.MODE_APPEND)) {
            fos.write(logEntry.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
