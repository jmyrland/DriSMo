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

package com.drismo.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import com.drismo.R;
import com.drismo.facebook.ShareOnFacebook;
import com.drismo.gui.quickaction.ActionItem;
import com.drismo.gui.quickaction.QuickAction;
import com.drismo.gui.quickaction.QuickActionAdapter;
import com.drismo.model.Config;
import com.drismo.model.Trip;

import java.io.*;
import java.util.*;

/**
 * This class gets the timestamp/filename data for all stored trips, and lists them in a <code>ListView</code>.
 * It also supplies a quick action menu for the user to view, rename or delete each trip.
 * @see com.drismo.gui.quickaction.QuickAction
 * @see com.drismo.gui.quickaction.QuickActionAdapter
 */
public class ViewArchive extends Activity {
    private String drismoDir;
    private String[] prevTrips, tripDurations, tripDates;
    private QuickActionAdapter adapter;
    private final int PLEASE_WAIT_SPINNER = 1;

    /**
     * Sort trip name array descending by when the files has been created.
     * @param sortedTrips An array of all trip files.
     */
    private void sortTripsByDate(File[] sortedTrips) {
        String[] sortedStrings = new String[prevTrips.length];

        for(int i = 0; i < prevTrips.length; i++)
            sortedStrings[i] = sortedTrips[i].getName();

        prevTrips = sortedStrings;                  // Storing the result
    }

    /**
     * Open and read all trip files, to calculate trip duration based on
     * number of lines multiplied by the update frequency.
     * @return Array of strings, displaying readable duration in hours, mins and secs.
     */
    public String[] addTripDuration() {
        tripDurations = new String[prevTrips.length];
        String buff;
        int updateSec, updateCount;
        int totSec, minutes, hours;
        int i = 0;

        for(String filename : prevTrips) {
            updateCount = 0;

            try {
                openFileInput(filename);
                BufferedReader in = new BufferedReader(new InputStreamReader(openFileInput(filename)));

                buff = in.readLine();
                if(buff != null){
                    updateSec = Integer.parseInt(buff.split(":")[1])/1000;  // Getting update frequency in seconds
                    buff = in.readLine();

                    while (!((buff = in.readLine()) == null || buff.startsWith("#") )){
                        updateCount++;                                      // Count all lines of data in the file
                    }
                    in.close();

                    if(updateCount > 1) {
                        totSec   = updateSec * updateCount;                 // Total duration in seconds
                        minutes  = totSec / 60;
                        hours    = minutes / 60;                            // Split seconds in hours, mins and secs.
                        minutes %= 60;
                        totSec  %= 60;

                        tripDurations[i++] = ((hours > 0)   ? hours   + "h " : "") +
                                             ((minutes > 0) ? minutes + "m " : "") + totSec + "s";
                    }
                    else tripDurations[i++] = null;                         // File has no content.
                }
            } catch (FileNotFoundException e) {
//                Log.d("Reading", "filenotfound");
            } catch (IOException e) {
//                Log.d("Reading", "IOExeption");
            }
        }
        return tripDurations;
    }

    /**
     * Delete all trip files, while progressbar runs in the foreground.
     */
    private class DeleteAllTrips extends AsyncTask<Void, Void, Boolean>{
        ProgressDialog deleteProgressDialog;

        /**
         * Create the progressbar before starting the delete-process.
         */
        protected void onPreExecute() {
            deleteProgressDialog = new ProgressDialog(ViewArchive.this);
            deleteProgressDialog.setCancelable(false);
            deleteProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            deleteProgressDialog.setMax(prevTrips.length);
            deleteProgressDialog.setMessage(getString(R.string.deletingTrips)+"...");
            deleteProgressDialog.show();
        }

        /**
         * Loop through all trip-files, and delete them.
         * @param params Void
         * @return True if ALL files were deleted. False otherwise.
         */
        @Override
        protected Boolean doInBackground(Void... params) {
             int deleteCount = 0;
             for(String trip : prevTrips) {
                 File file = new File(drismoDir+trip);
                 if(file.delete()) deleteCount++;
                 deleteProgressDialog.setProgress(deleteCount+1);     // Update the progressbar
             }
             return (deleteCount == prevTrips.length);
        }

        /**
         * If all files successfully was deleted, the activity will restart, to show an empty archive.<br />
         * If something went wrong, the user will be notified by a Toast,
         * before the activity restarts and shows the remaining files.
         * @param deletedAll True if ALL files has been deleted. False otherwise.
         */
        @Override
        protected void onPostExecute(Boolean deletedAll) {
            deleteProgressDialog.dismiss();                         // Close ProgressDialog
            if(!deletedAll)
                Toast.makeText(ViewArchive.this, getString(R.string.unableToDeleteAll),
                Toast.LENGTH_LONG).show();

            restartActivity(getIntent());
        }
    }

    /**
     * Delete a trip file, while a ProgressDialog (spinner) runs in the foreground.
     */
    private class DeleteOneTrip extends AsyncTask<String, Void, Boolean>{
//        int updatedTripNr = -1;
        String filename;

        /**
         * Prepare a spinner for this task
         */
        protected void onPreExecute() {
            showDialog(PLEASE_WAIT_SPINNER);
        }

        /**
         * Delete a file from the application directory
         * @param fn Name of the file to delete.
         * @return True/false result of whether or not the file has been deleted.
         */
        @Override
        protected Boolean doInBackground(String... fn) {
            filename = fn[0];
            File file = new File(drismoDir+filename);

            if(file.delete()) {
/*                for(int i = 0; i < prevTrips.length; i++) {
                    if(prevTrips[i].equals(filename)) updatedTripNr = i;
                }
*/                return true;
            }
            else return false;
        }

        /**
         * Close the ProgressDialog, report success of failure to the user via Toast and
         * restart the activity to show the archive with updated content.
         * @param deleted True if the file was deleted. False otherwise.
         */
        @Override
        protected void onPostExecute(Boolean deleted) {
            if(deleted) {
                 Toast.makeText(ViewArchive.this, filename+" "+getString(R.string.beenDeleted),
                 Toast.LENGTH_SHORT).show();
            }
            else Toast.makeText(ViewArchive.this, getString(R.string.unableToDelete)+" "+filename,
                 Toast.LENGTH_SHORT).show();

            dismissDialog(PLEASE_WAIT_SPINNER);
            restartActivity(getIntent());
        }
    }

    /**
     * Rename a trip file, while a ProgressDialog (spinner) runs in the foreground.
     */
    private class RenameTrip extends AsyncTask<String[], Void, Boolean>{
        String[] filenames;
        String oldFilename, newFilename;
        int updatedTripNr = -1;

        /**
         * Prepare a spinner for this task
         */
        protected void onPreExecute() {
            showDialog(PLEASE_WAIT_SPINNER);
        }

        /**
         * Rename a file from the application directory
         * @param param A string array with both the old filename and what to rename it to.
         * @return Result of the rename action.
         */
        @Override
        protected Boolean doInBackground(String[]... param) {
            filenames = param[0];           // Extracting filenames from the the parameter String-array
            oldFilename = filenames[0];
            newFilename = filenames[1];
                                            // Get and rename file from the application directory
            File file = new File(drismoDir+oldFilename);

            for(int i = 0; i < prevTrips.length; i++) {
                if(prevTrips[i].equals(oldFilename)) updatedTripNr = i;
            }
                                            // If file is successfully renamed, update the filename in prevTrips.
            if(file.renameTo(new File(drismoDir+newFilename))) {
                List<String> list = new ArrayList<String>(Arrays.asList(prevTrips));
                list.set(updatedTripNr, newFilename);
                prevTrips = list.toArray(prevTrips);

                return true;
            }
            else return false;
        }

        /**
         * Close the ProgressDialog, report success of failure to the user via Toast and
         * restart the activity to show the archive with updated content.
         * @param renamed True if the file was renamed. False otherwise.
         */
        @Override
        protected void onPostExecute(Boolean renamed) {
            if(!renamed)
                Toast.makeText(ViewArchive.this, getString(R.string.renameFailed),
                Toast.LENGTH_SHORT).show();
            else {
                adapter.setData(prevTrips);           // Add updated data to the adapter.
                adapter.notifyDataSetChanged();       // Refresh the list data.

                Toast.makeText(ViewArchive.this, getString(R.string.renameSuccess),
                Toast.LENGTH_SHORT).show();
            }
            dismissDialog(PLEASE_WAIT_SPINNER);
        }
    }

    /**
     * Load the trip files. Get metadata and sort the files before populating the archive list.
     */
    private class PopulateArchive extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog loadingDialog;

        protected void onPreExecute() {
            drismoDir = (getApplicationContext().getFilesDir()).toString()+"/";

            try {
                File[] files = getApplicationContext().getFilesDir().listFiles();

                                    // Sort the array of trip files, by the last modified-metadata.
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        if      (f1.lastModified() > f2.lastModified()) return -1;
                        else if (f1.lastModified() < f2.lastModified()) return  1;
                        else    return 0;
                    }
                });

                FilenameFilter filter = new FilenameFilter() {
                    public boolean accept(File file, String s) {
                        return (s.endsWith(".csv"));
                    }
                };                          // Get only the csv-files from the directory.

                prevTrips = getApplicationContext().getFilesDir().list(filter);
                sortTripsByDate(files);
            }
            catch(Exception e) { e.printStackTrace(); }

                                            // Show a progress bar while loading trip files
            loadingDialog = new ProgressDialog(ViewArchive.this);
            loadingDialog.setCancelable(false);
            loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            loadingDialog.setMax(prevTrips.length);
            loadingDialog.setMessage(getString(R.string.loadingTrips));
            loadingDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            synchronized(ViewArchive.this){
            if(prevTrips.length < 1) {      // 0 trips in the directory
                TextView tv = (TextView) findViewById(R.id.noTripData);

                Typeface font = Typeface.createFromAsset(getAssets(), "eras-demi.ttf");
                tv.setTypeface(font);
                try{
                    tv.setVisibility(View.VISIBLE);     // Display the text for no trip data available.
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            else {
                tripDates = new String[prevTrips.length];

                for(int i = 0; i < prevTrips.length; i++) {
                    if(!prevTrips[i].endsWith(".csv"))
                        prevTrips[i] +=".csv";                  // Adding file-extension if it doesn't exist

                    try {
                        File file = new File(drismoDir+prevTrips[i]);
                        Date lastModDate = new Date(file.lastModified());
                        tripDates[i] = lastModDate.toString();  // Storing metadata / last modified timestamp

                        loadingDialog.setProgress(i);           // Update progressbar

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean done) {
                                                            // Getting the layouts for single list items,
            ListView archiveList = (ListView) findViewById(R.id.archiveList);  // and the whole ListView.

            adapter = new QuickActionAdapter(getApplicationContext());
            adapter.setData(prevTrips);                     // Populate the archive list.
            adapter.setTimestamp(tripDates);                // Adding file timestamp to the ListView elements.
            adapter.setDuration(addTripDuration());         // Calculate and add trip duration for each trip file.

            for(int i = 0; i < prevTrips.length; i++) {
                if(tripDurations[i] == null)                    // Delete files with no content.
                    new DeleteOneTrip().execute(prevTrips[i]);
            }

            archiveList.setAdapter(adapter);

            final ActionItem viewAction = new ActionItem();     // Quick action to view a trip

            viewAction.setTitle(getString(R.string.view));
            viewAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_qa_graph));

            final ActionItem renameAction = new ActionItem();   // Quick action to rename a trip

            renameAction.setTitle(getString(R.string.rename));
            renameAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_qa_pencil));

            final ActionItem delAction = new ActionItem();      // Quick action to delete a trip

            delAction.setTitle(getString(R.string.delete));
            delAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_qa_delete));

            final ActionItem facebookAction = new ActionItem(); // Quick action to share score on facebook

            facebookAction.setTitle(getString(R.string.share));
            facebookAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_qa_facebook));

            final ActionItem exportAction = new ActionItem();   // Quick action to upload a trip

            exportAction.setTitle(getString(R.string.export));
            exportAction.setIcon(getResources().getDrawable(R.drawable.ic_menu_qa_upload));

            // On normal click, just go directly yo the ViewTrip activity.
            archiveList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(ViewArchive.this, ViewTrip.class);
                    intent.putExtra("File", prevTrips[position]);
                    startActivity(intent);
                }
            });

            // On long click, prepare the quick action menu.
            archiveList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    final String fileName = prevTrips[position];
                    final QuickAction mQuickAction = new QuickAction(view);
                    final ImageView drismoD = (ImageView) view.findViewById(R.id.drismoD);

                    // Highlight icon on the ListView element that the quick action menu points to
                    drismoD.setImageResource(R.drawable.img_archive_d_highlighted);

                    viewAction.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent intent = new Intent(ViewArchive.this, ViewTrip.class);
                            intent.putExtra("File", fileName);
                            startActivity(intent);
                            mQuickAction.dismiss();
                        }
                    });

                    renameAction.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            final EditText fileNameInput = new EditText(ViewArchive.this);

                                               // File name without extension in input field.
                            fileNameInput.setText(fileName.substring(0, fileName.length()-4));

                            renameDialog(fileName);

                            mQuickAction.dismiss();
                        }
                    });

                    delAction.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                           new AlertDialog.Builder(ViewArchive.this)
                              .setMessage(getString(R.string.delTripData)+"\n\n"+fileName)
                              .setTitle(R.string.delete)
                              .setCancelable(false)
                              .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton){
                                     new DeleteOneTrip().execute(fileName);
                                 }})
                              .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton){
                                 }})
                              .show();

                            mQuickAction.dismiss();
                        }
                    });

                    // Build and share a trip summary on Facebook
                    facebookAction.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                Trip thisTrip = new Trip(fileName, ViewArchive.this);

                                Intent myIntent = new Intent(ViewArchive.this, ShareOnFacebook.class);
                                myIntent.putExtra("facebookMessage", thisTrip.getTripSummary());
                                myIntent.putExtra("shareImage", false);

                                try{
                                    startActivity(myIntent);            // Start activity to share the summary on Facebook.
                                }catch(Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(ViewArchive.this, "Failed to post to facebook.", Toast.LENGTH_LONG).show();
                                }

                            }
                            catch(IOException e) {
                                e.printStackTrace();
                            }
                            finally {
                                mQuickAction.dismiss();
                            }
                        }
                    });

                            // Export/send the trip file, via a user selected external app.
                    exportAction.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent exportIntent = new Intent(android.content.Intent.ACTION_SEND);
                            exportIntent.setType("text/csv");
                            exportIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + drismoDir + fileName));
                            startActivity( Intent.createChooser(exportIntent, "Export file..") );

                            mQuickAction.dismiss();
                        }
                    });

                    mQuickAction.addActionItem(viewAction);
                    mQuickAction.addActionItem(renameAction);       // Populate the quick action menu
                    mQuickAction.addActionItem(delAction);
                    mQuickAction.addActionItem(facebookAction);
                    mQuickAction.addActionItem(exportAction);

                    mQuickAction.setAnimStyle(QuickAction.ANIM_AUTO);

                    mQuickAction.setOnDismissListener(new PopupWindow.OnDismissListener() {
                        public void onDismiss() {
                            drismoD.setImageResource(R.drawable.img_archive_d);
                        }                                          // Remove highlight icon when menu closes.
                    });
                    mQuickAction.show();
                    return true;
                }
            });

            loadingDialog.dismiss();
        }
    }

    /**
     * Create a <code>ListView</code> from the archive with all previous trips.
     * The list will be displayed as clickable titles with timestamp.
     * @param savedInstanceState Not beeing used, as <code>onSaveInstanceState(Bundle)</code> is not implemented.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}
        setContentView(R.layout.archive);

        if(getResources().getConfiguration().orientation == 2)      // On horizontal view,
            findViewById(R.id.archiveScreen).                       //  change to the landscape background.
            setBackgroundDrawable(ViewArchive.this.getResources().getDrawable(R.drawable.bg_ls));

        new PopulateArchive().execute();
    }

    /**
     * Prepare and show a <code>ProgressDialog</code> with a spinner.
     * @param id Determines what text to display on the dialog.
     * @return The <code>ProgressDialog</code> instance.
     */
	@Override
	protected Dialog onCreateDialog(int id) {
        ProgressDialog mProgressDialog;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.loading));
        mProgressDialog.setCancelable(false);

		switch (id) {
			case PLEASE_WAIT_SPINNER: mProgressDialog.setMessage(getString(R.string.pleaseWait));
			break;
			default: return null;
		}

        mProgressDialog.show();
        return mProgressDialog;
	}

    /**
     * Creating the menu for this activity from <code>archive_menu.xml</code>.
     * @param menu The menu to place the items into.
     * @return True to display the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.archive_menu, menu);
        return true;
    }

    /**
     * If user confirmes the action, run <code>AsyncTask</code> to delete all trip files.
     * @param item  The menu item that was selected.
     * @return True, to end menu processing when the <code>AsyncTask</code> has been executed.
     * @see com.drismo.gui.ViewArchive.DeleteAllTrips
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.deleteAll:
            new AlertDialog.Builder(ViewArchive.this)
              .setMessage(getString(R.string.delAllTripData))
              .setTitle(R.string.deleteAll)
              .setCancelable(false)
              .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton){
                    new DeleteAllTrips().execute();
                 }})
              .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int whichButton){
                     // On cancel, do nothing.
                 }
                 })
              .show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Finish the current activity, and startMonitoring a new instance immediately.
     * This will refresh the list when changes has been done to one or more files.
     * @param intent Intent describing what activity to startMonitoring.
     */
    public void restartActivity(Intent intent) {
        finish();
        startActivity(intent);
    }

    /**
     * Recursive dialog to rename a file.
     * @param oldFile Current file name of the file to be renamed.
     */
    public void renameDialog(final String oldFile) {
        final EditText fileNameInput = new EditText(ViewArchive.this);
        fileNameInput.setText(oldFile.substring(0, oldFile.length()-4));   // Display old filename without the extension.

        new AlertDialog.Builder(ViewArchive.this)
            .setTitle(getString(R.string.rename))
            .setMessage(getString(R.string.newFileName))
            .setView(fileNameInput)
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String newFileName = fileNameInput.getText().toString();
                    if(!newFileName.endsWith(".csv")) newFileName +=".csv"; // Adding extension if not present.

                    File file = new File(drismoDir+newFileName);

                    if(file.exists()) {
                        Toast.makeText(ViewArchive.this, getString(R.string.fileNameInUse),
                        Toast.LENGTH_SHORT).show();            // If filename is already in use, call this function
                        renameDialog(oldFile);                 //  recursively, until a valid name is typed,
                    }                                          //  or the user cancel the alertdialog.
                    else {
                        String fileNames[] = { oldFile, newFileName };
                        new RenameTrip().execute(fileNames);   // Running rename in AsyncTask with ProgressDialog.
                    }
                }
            }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                                                               // On cancel, do nothing.
                }
            }).show();
    }

}

