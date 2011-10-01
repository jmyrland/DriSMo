/*
 * The basis of DriSMo was developed as a bachelor project in 2011,
 * by three students at Gjøvik University College (Fredrik Kvitvik,
 * Fredrik Hørtvedt and Jørn André Myrland). For documentation on DriSMo,
 * view the JavaDoc provided with the source code.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.drismo.logic;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.drismo.model.Config;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class for making/updating log file. Logs the quality when monitoring, and location if specified.
 * @see com.drismo.logic.MonitorController
 */
public class TripLogger extends Thread implements QualityListener, NewLocationListener {

    private int update_ms;
    private OutputStreamWriter osw;
    private long mStartTime;
    private String logFileName;
    private Context context;
    private boolean isLogging;

    private volatile ArrayList<Integer> scoreList = new ArrayList<Integer>();

    private Location currentLocation;

    /**
     * The constructor
     * @param con the application context. This is used when we make/write a log file.
     */
    public TripLogger(Context con){
        context = con;
        update_ms =0;
        logFileName = "not";
        osw = null;
    }

    public void run() {
        synchronized (this){
            while(isLogging && update_ms != 0){  //While we havn't stopped the logging
                Log.d("Fil", "updating logfile..");
                updateFile();                               //update the file, then sleeps.
                try {
                    wait(update_ms);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Called when we want to stop logging.
     */
    @Override
     public synchronized void interrupt() {
         Log.d("Fil", "stopping file");
         if(isLogging){
             update_ms = 0;
             closeLogFile();
             isLogging = false;
         }

         super.interrupt();
     }

    /**
     * Start the logging
     */
    @Override
    public synchronized void start(){
        Log.d("FIL", "starting logging");
        update_ms = Config.powerSaverOn() ? 6000 : 2000;
        newLogFile();
        isLogging = true;

        super.start();
    }

    /**
     * makes a new log file.
     */
    public void newLogFile(){
        int fileNameCount = 1;
        String fileNameEnding;
        FileOutputStream fos;

        try {
            currentLocation = new Location("");
            mStartTime = System.currentTimeMillis();

            String[] filenames = context.fileList();
            Arrays.sort(filenames);
            logFileName ="drismo-";
            fileNameEnding = "0001";
                            // creating filename in format drismo-<first-available-number>.dtf
            for(String filname : filenames){
                if(filname.equals(logFileName+fileNameEnding+".csv")) fileNameCount++;

                if(fileNameCount < 10)          fileNameEnding = "000" + fileNameCount;
                else if(fileNameCount < 100)    fileNameEnding = "00"  + fileNameCount;
                else if(fileNameCount < 1000)   fileNameEnding = "0"   + fileNameCount;
                else fileNameEnding = Integer.toString(fileNameCount);
            }

            logFileName += fileNameEnding+".csv";
            fos = context.openFileOutput(logFileName, Context.MODE_WORLD_READABLE); // makes a new file, in applications local storage place

            osw = new OutputStreamWriter(fos) ;

            osw.write("#Time,Score,Lat,Long,Speed,:"+ update_ms +"\n");
            osw.flush();

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }catch (NullPointerException e) {
//            Toast.makeText(getApplicationContext(), "FAILED! (to create logfile)", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    /**
     * closes the log file, so the application can startMonitoring a new one.
     */
    public void closeLogFile(){
        try {
            osw.flush();
            osw.close();
            osw = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the log file with a new line of info.
     */
    private void updateFile(){
        synchronized (this) {
            if(osw != null && !logFileName.equals("not"))         //if there is a log file
                try {
                    final long start = mStartTime;
                    long millis = System.currentTimeMillis() - start;    //milliseconds from starting the logger

                    //writes the milliseconds from startMonitoring, and the avg. score since last update + location info.
                    String output = millis + "," + calculateAverageScore() + "," + currentLocation.getLatitude() + "," + currentLocation.getLongitude() +","+ currentLocation.getSpeed()+"\n";

                    osw.write(output);
                    scoreList.clear();

//                    Log.d("Log", output);

                    osw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Calculates the avg. score
     * @see TripLogger#updateFile()
     * @return the avg. score since last write to the log file.
     */
    private int calculateAverageScore(){
        if(scoreList.size() > 0){
            int avgSum = 0;

            for(int score : scoreList)
                avgSum += score;

            return avgSum / scoreList.size();
        }

        return 0;
    }

    /**
     * This method is called when the score changes.
     * adds the new score to the scorelist.
     * @param newScore the new score
     */
    public synchronized void onQualityUpdate(int newScore) {
        scoreList.add(newScore);
    }

    /**
     * This method is called when there is a location update.
     * @param loc the new location.
     */
    public synchronized void onNewLocation(Location loc) {
        currentLocation = loc;
    }

    /**
     * Get the file name for this log.
     * @return the file name.
     */
    public String getFileName() {
        return logFileName;
    }
}