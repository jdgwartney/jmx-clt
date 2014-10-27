package com.logicmonitor.ft.jmx.util;

import java.io.*;

public class FileUtil {
    static public boolean exists(String path) {
        File f = new File(path);
        return f.exists();
    }

    static public void renameFile(String newName, String oldName) throws IOException {
        if (File.separatorChar == '\\') {
            copyFile(oldName, newName);
            File oldF = new File(oldName);
            oldF.delete();
        }
        else {
            File oldF = new File(oldName);
            File newF = new File(newName);
            if (newF.exists()) {
                newF.delete();
            }
            if (!oldF.renameTo(newF)) {
                throw new IOException(String.format("Unable to rename %s to %s", oldName, newName));
            }
        }
    }

    static public void copyFile(String src, String dst) throws IOException {
        FileOutputStream fos = null;
        FileInputStream fis = null;

        try {
            fos = new FileOutputStream(dst);
            fis = new FileInputStream(src);
            byte[] buf = new byte[4096];

            int nread = fis.read(buf);
            while (nread > 0) {
                fos.write(buf, 0, nread);
                nread = fis.read(buf);
            }
        }
        catch (IOException e) {
            File f = new File(dst);
            if (f.exists()) {
                f.delete();
            }

            throw e;
        }
        finally {
            if (fis != null) {
                try {
                    fis.close();
                }
                catch (Exception e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (Exception e) {
                }
            }
        }
    }

    static public String readFileToString(String fileName) {
        File file = new File(fileName);
        FileReader fileReader = null;
        String ret = null;
        try {
            fileReader = new FileReader(fileName);
            int size = (int) file.length();
            char[] buf = new char[size];
            fileReader.read(buf, 0, size);
            ret = new String(buf);
            fileReader.close();
        }
        catch (IOException e) {
            /* LogMsg.error("Cannot read content from file", String.format(
                             "file=%s, error=%s", fileName, e.getMessage()
                             ));*/
        }
        return ret;
    }

    static public void deleteDirectory(File dir) {
        File[] children = dir.listFiles();

        if(children != null) {
            for (File f : children) {
                if (!f.isDirectory()) {
                    f.delete();
                }
                else {
                    deleteDirectory(f);
                }
            }
        }


        dir.delete();
    }


    static public void deleteDirectory(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        deleteDirectory(file);
    }


    static public void renameDirectory(String newDir, String oldDir) {
        File ofile = new File(oldDir);
        File nfile = new File(newDir);
        ofile.renameTo(nfile);
    }


    static public void createDirectory(String path) throws IOException {
        File f = new File(path);
        f.mkdir();
    }


    static public boolean deleteFile(String path) {
        File f = new File(path);
        return f.delete();
    }


    static public void createFile(String path, long size) throws IOException {
        RandomAccessFile file = new RandomAccessFile(path, "rw");
        try {
            file.setLength(size);
        }
        finally {
            file.close();
        }
    }
}
