package com.logicmonitor.ft.jmx.jmxman;


/**
 * Created by jianyuan on 8/28/2014.
 */

import com.logicmonitor.ft.jmx.util.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileHelper {

    public static String loadFileIntoString(String filePath) {
        return FileUtil.readFileToString(filePath);
    }

    public static boolean saveStringIntoFile(String filePath, String contents) {
        File file = new File(filePath);

        try {
            new FileOutputStream(file).write(contents.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists())
            return true;
        return false;
    }
}
