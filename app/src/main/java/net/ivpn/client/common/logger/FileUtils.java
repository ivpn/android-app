package net.ivpn.client.common.logger;

/*
 IVPN Android app
 https://github.com/ivpn/android-app

 Created by Oleksandr Mykhailenko.
 Copyright (c) 2020 Privatus Limited.

 This file is part of the IVPN Android app.

 The IVPN Android app is free software: you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by the Free
 Software Foundation, either version 3 of the License, or (at your option) any later version.

 The IVPN Android app is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details.

 You should have received a copy of the GNU General Public License
 along with the IVPN Android app. If not, see <https://www.gnu.org/licenses/>.
*/

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.core.content.FileProvider;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.LoggerContext;

public class FileUtils {

    private static final String RESULT_LOG_FILE_NAME = "android_ivpn_client_logs.txt";
    private static final String LOG_PATH_PROPERTY = "LOG_PATH";
    private static final String OLD_LOG_PATH_PROPERTY = "LOG_PATH_OLD";
    private static final String ACTIVE_LOG_FILE_NAME = "log.log";

    public static Uri createLogFileUri(Context context) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        List<File> fileList = new ArrayList<>();
        String logFilePath = loggerContext.getProperty(LOG_PATH_PROPERTY);
        Log.d("FileUtils", "createLogFileUri: logFilePath = " + logFilePath);
        File logFile = new File(logFilePath + "/" + ACTIVE_LOG_FILE_NAME);

        File oldLogFile = getOldLogFile(loggerContext);
        if (oldLogFile != null) {
            fileList.add(oldLogFile);
        }
        fileList.add(logFile);

        File commonFile = FileUtils.createFinalLogFile(logFilePath);
        FileUtils.mergeFiles(commonFile, fileList);

        return FileProvider.getUriForFile(context, context.getPackageName(), commonFile);
    }

    public static void clearAllLogs() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            String logFilePath = loggerContext.getProperty(LOG_PATH_PROPERTY);
            File logFile = new File(logFilePath + "/" + ACTIVE_LOG_FILE_NAME);
            clearLogFile(logFile);

            File oldLogFile = getOldLogFile(loggerContext);
            clearLogFile(oldLogFile);

            File commonFile = FileUtils.createFinalLogFile(logFilePath);
            clearLogFile(commonFile);
        } catch (Exception ignored) {
            Log.e("FileUtils", "clearAllLogs: exception = " + ignored);
        }
    }

    private static void clearLogFile(File file) {
        Log.d("FileUtils", "clearLogFile: file = " + (file == null ? null : file.getName()));
        if (file == null) {
            return;
        }
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.close();
        } catch (FileNotFoundException e) {
            Log.e("FileUtils", "clearLogFile: e = " + e);
            e.printStackTrace();
        }
    }

    private static File createFinalLogFile(String path) {
        return new File(path, RESULT_LOG_FILE_NAME);
    }

    private static File getOldLogFile(LoggerContext loggerContext) {
        String oldLogFilePath = loggerContext.getProperty(OLD_LOG_PATH_PROPERTY);
        File oldLogDir = new File(oldLogFilePath);
        File[] files = oldLogDir.listFiles();
        File oldLogFile = null;

        if (files != null && files.length > 0) {
            oldLogFile = files[0];
        }
        return oldLogFile;
    }

    private static void mergeFiles(final File outFile, final List<File> files) {
        try {
            OutputStream out = new FileOutputStream(outFile, false);
            byte[] buf = new byte[256];
            for (File file : files) {
                InputStream in = new FileInputStream(file);
                int b = 0;
                while ((b = in.read(buf)) >= 0) {
                    out.write(buf, 0, b);
                    out.flush();
                }
            }
            out.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}