package com.abhijitvalluri.android.fitnotifications.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Abhijit Valluri on 6/14/2017.
 */

public class DebugLog {

    private static DebugLog sDebugLog;
    private static final String LOG_FILENAME = "fitNotificationsLog.txt";

    public static final int STATUS_UNINITIALIZED = -1;
    public static final int STATUS_LOG_OPENED = 1;
    public static final int STATUS_WRITE_OK = 2;
    public static final int STATUS_IO_EXCEPTION = -2;


    private File mLogFile;
    private FileOutputStream mLog;
    private int mFileStatus;
    private int mWriteStatus;
    private DateFormat mDateFormat;
    private boolean mEnabled;


    public static DebugLog get(Context context) {
        if (sDebugLog == null) {
            sDebugLog = new DebugLog(context);
        }
        return sDebugLog;
    }

    private DebugLog(Context context) {
        mLogFile = new File(context.getExternalFilesDir(null), LOG_FILENAME);
        mFileStatus = STATUS_UNINITIALIZED;
        mWriteStatus = STATUS_UNINITIALIZED;
        mDateFormat  = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    }

    public int getFileStatus() {
        return mFileStatus;
    }

    public int getWriteStatus() {
        return mWriteStatus;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public int enable() {
        mEnabled = true;
        return init();
    }

    public int disable() {
        mEnabled = false;
        return deInit();
    }

    private int init() {
        if (mFileStatus == STATUS_LOG_OPENED) {
            return mFileStatus;
        }

        boolean isNewLogFile;
        try {
            isNewLogFile = mLogFile.createNewFile();
        } catch (IOException e) {
            Log.e(LOG_FILENAME, "Unable to create Fit Notifications Log: " + e.getMessage());
            mFileStatus = STATUS_IO_EXCEPTION;
            return mFileStatus;
        }

        try {
            mLog = new FileOutputStream(mLogFile, false);
            Date date = new Date();
            String fileOpenStatus = mDateFormat.format(date) + ": " +
                    (isNewLogFile ? "New log file created.\n" : "Existing log file opened.\n");
            mLog.write(fileOpenStatus.getBytes());
            mFileStatus = STATUS_LOG_OPENED;
            mWriteStatus = STATUS_WRITE_OK;
        } catch (FileNotFoundException e) {
            // Not going to happen as you literally created it above. But still...
            Log.e(LOG_FILENAME, "Log file not found: " + e.getMessage());
        } catch (IOException e) {
            Log.e(LOG_FILENAME, "Unable to write to Fit Notifications Log: " + e.getMessage());
            mWriteStatus = STATUS_IO_EXCEPTION;
        }
        return mWriteStatus;
    }

    public int writeLog(String string) {
        if (mFileStatus == STATUS_LOG_OPENED) {
            try {
                if (mLogFile.length() >= Math.pow(10, 7)) {
                    // Reset log file.
                    deInit();
                    init();
                }
            } catch (SecurityException e) {
                Log.e(LOG_FILENAME, "Unable to reset Fit Notifications Log: " + e.getMessage());
                mWriteStatus = STATUS_IO_EXCEPTION;
                return mWriteStatus;
            }

            try {
                Date date = new Date();
                string = mDateFormat.format(date) + ": " + string;
                mLog.write(string.getBytes());
                mLog.write('\n');
                mWriteStatus = STATUS_WRITE_OK;
            } catch (IOException e) {
                Log.e(LOG_FILENAME, "Unable to write to Fit Notifications Log: " + e.getMessage());
                mWriteStatus = STATUS_IO_EXCEPTION;
            }

            return mWriteStatus;
        }

        return mFileStatus;
    }

    private int deInit() {
        if (mLog != null) {
            try {
                Date date = new Date();
                String fileOpenStatus = mDateFormat.format(date) + ": Closing log.\n";
                mLog.write(fileOpenStatus.getBytes());
                mLog.close();
                mFileStatus = STATUS_UNINITIALIZED;
                mWriteStatus = STATUS_UNINITIALIZED;
            } catch (IOException e) {
                Log.e(LOG_FILENAME, "Error closing Fit Notifications Log: " + e.getMessage());
                mWriteStatus = STATUS_IO_EXCEPTION;
                mFileStatus = STATUS_IO_EXCEPTION;
            }
        }

        return mFileStatus;
    }

    public Intent emailLogIntent(Context context, String logcat) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("application/octet-stream");

        String subject = "Fit Notification Logs";
        ArrayList<Uri> attachments = new ArrayList<>();
        attachments.add(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", mLogFile));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "android@abhijitvalluri.com" });
        intent.putExtra(Intent.EXTRA_TEXT, logcat);
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);

        return intent;
    }
}
