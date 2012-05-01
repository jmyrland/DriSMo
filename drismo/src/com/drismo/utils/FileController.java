package com.drismo.utils;

import android.content.Context;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FileController {
    public static final String FILE_EXTENSION = ".csv";

    private Context context;
    private File directory;
    private FilenameFilter filter;

    public FileController(Context context, File directory) {
        this.context = context;
        this.directory = directory;
        setFileNameFilter();
    }

    private void setFileNameFilter() {
        filter = new FilenameFilter() {
            public boolean accept(File file, String s) {
                return (s.endsWith(FILE_EXTENSION));
            }
        };
    }

    public String getDirectoryString() {
        return directory.toString()+"/";
    }

    public int getCount() {
        return listFiles().size();
    }

    public List<String> listFiles() {
        return new ArrayList<String>(Arrays.asList(directory.list(filter)));
    }

    public boolean rename(String from, String to) {
        File file = new File(getDirectoryString()+from);
        return file.renameTo(new File(getDirectoryString()+to));
    }

    public String getTimestamp(String fileName) {
        File file = new File(getDirectoryString()+fileName);
        Date lastModDate = new Date(file.lastModified());
        return lastModDate.toString();
    }

    public File getFile(String fileName) {
        return new File(getDirectoryString()+fileName);
    }

    public int getSecondDurationFromTripFile(String fileName) {
        String buffer;
        int updateFrequency;
        int totalSeconds = 0;
        int updateCount = 0;

        try {
            context.openFileInput(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(context.openFileInput(fileName)));

            buffer = in.readLine();
            if(buffer != null){
                updateFrequency = Integer.parseInt(buffer.split(":")[1])/1000;

                while (!((buffer = in.readLine()) == null || buffer.startsWith("#") )){
                    updateCount++;      // Count all lines of data in the file
                }
                in.close();

                if(updateCount > 1) totalSeconds = updateFrequency * updateCount;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalSeconds;
    }
}
