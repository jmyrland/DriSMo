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

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.facebook.ShareOnFacebook;
import com.drismo.model.Config;
import com.drismo.model.Trip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Handles displaying of a trip and shows the different info in different views.
 */
public class ViewTrip extends TabActivity {

    public static final String EXTRA_FILENAME = "EXTRA_FILENAME";

    private static int showThresholds;
    private Trip trip;
    private GraphView graphView;

    private TabHost tabHost;

    /**
     * Sets up the whole layout/view.
     * @param savedInstanceState savedInstance
     */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){}

        final String filename = getIntent().getStringExtra(EXTRA_FILENAME);     //gets the name of the file to display

        tabHost = getTabHost();

        /**
         * The following 9 lines of code is a workaround to get a spinner while loading a trip on Android v2.1.
         *   The app crashes if the tabHost is empty when returning from onCreate.
         */
        TabHost.TabSpec empty = tabHost.newTabSpec(getString(R.string.graph)).setIndicator(getString(R.string.graph)).setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                View blackView = new View(ViewTrip.this);
                blackView.setBackgroundColor(Color.BLACK);
                return blackView;
            }}
        );
        tabHost.addTab(empty);
        getTabWidget().getChildAt(0).getLayoutParams().height =1;



        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminateDrawable(getResources().getDrawable(R.anim.loading));
        dialog.setCancelable(false);
        dialog.setMessage("Loading.. please wait..");
        dialog.show();

        final Handler handler = new Handler() {
            public final static int POPULATE_ACTIVITY = 0;
            public void handleMessage(Message msg){
                if(msg.what == POPULATE_ACTIVITY)
                    populateActivity();
            }
        };

        /**
         * Reads trip while displaying a loader. Populates the activity when done reading the trip file.
         */
        new Thread(){
            public void run(){
                synchronized (this){
                    try{
                        readTripFile(filename);
                        dialog.dismiss();
                        handler.sendEmptyMessage(0);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    /**
     * Fires the app rater on start:
     */
    public void onStart(){
        super.onStart();
        AppRater.app_launched(this);
    }

    /**
     * Reads one trip, based of the given file name.
     * @param filename File to read.
     */
    public void readTripFile(String filename){
        try {
            trip = new Trip(filename, getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Populates the activity's tabs & views.
     */
    public void populateActivity(){

        tabHost.clearAllTabs();

        showThresholds = 0;

        TabHost.TabSpec graphSpec, mapSpec, infoSpec;       // TabSpecs for each tab
                                                                //the score graph
        graphView = new GraphView(
                ViewTrip.this, trip.getScoreArray() , getString(R.string.trip) +" ", trip.getColorArray()
        );

        graphSpec = tabHost.newTabSpec(getString(R.string.graph)).setIndicator(getString(R.string.graph)).setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return(graphView);
            }} );

        Intent intent = new Intent(getApplicationContext(), BasicMap.class);
        intent.putExtra("lat", trip.getLatitudeArray());
        intent.putExtra("long", trip.getLongitudeArray());
        intent.putExtra("colors", trip.getShortColorArray());

        mapSpec = tabHost.newTabSpec(getString(R.string.map))
                .setIndicator(getString(R.string.map)).setContent(intent);

        intent = new Intent(getApplicationContext(), ViewExtraInfo.class);
        intent.putExtra("speed", trip.getSpeedArray());
        intent.putExtra("score", trip.getScoreArray());
        intent.putExtra("ms", trip.getRefreshRate());
        intent.putExtra("tripM", trip.getTripLengthInMeters());
        intent.putExtra("tripKM", trip.getTripLengthInKM());
        intent.putExtra("fromStreet", trip.getStartStreet());
        intent.putExtra("fromCity", trip.getStartCity());
        intent.putExtra("toStreet", trip.getDestinationStreet());
        intent.putExtra("toCity", trip.getDestinationCity());

        infoSpec = tabHost.newTabSpec(getString(R.string.shortInfo))
                .setIndicator(getString(R.string.shortInfo)).setContent(intent);

        tabHost.addTab(graphSpec);      // add the scoregraph to the tabHost
        tabHost.addTab(mapSpec);
        tabHost.addTab(infoSpec);

        getTabWidget().getChildAt(0).getLayoutParams().height =50;
        getTabWidget().getChildAt(1).getLayoutParams().height =50;
        getTabWidget().getChildAt(2).getLayoutParams().height =50;

        if(!trip.isGpsCordsAvailable() ){
            getTabWidget().getChildAt(1).setFocusable(false);
            getTabWidget().getChildAt(1).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), getString(R.string.noGpsTrip), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Creating the menu for this activity from trip_graph_menu.xml.
     * @param menu The menu to place the items into.
     * @return True to display the menu if we're in graph view. False in other tabs.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        String tabTag = getTabHost().getCurrentTabTag();

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trip_graph_menu, menu);

        if(tabTag.equals(getString(R.string.map)))
            menu.removeItem(R.id.graphThresholds);

        return (tabTag.equals(getString(R.string.graph)) || tabTag.equals(getString(R.string.map)));
    }

    /**
     * Set the menu for this activity, when the user press the device menu button.
     * @param item The selected menu item.
     * @return True, to end menu processing after the invalidation is done.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item selection
        switch (item.getItemId()) {
            case R.id.graphThresholds:
                getTabHost().invalidate();    // Invalidate the TabHost, to redraw and add/remove the threshold-lines.

                if(showThresholds == 1) {     // Swapping between show hide, since both use the same menu button:
                    item.setTitle(getString(R.string.showThresholds));
                    item.setIcon(R.drawable.ic_menu_graph_add_thresholds);
                    showThresholds = 2;
                }
                else {
                    item.setTitle(getString(R.string.hideThresholds));
                    item.setIcon(R.drawable.ic_menu_graph_remove_thresholds);
                    showThresholds = 1;
                }
                return true;
            case R.id.postFacebook:
                String tabTag = getTabHost().getCurrentTabTag();

                Intent myIntent = new Intent(ViewTrip.this, ShareOnFacebook.class);

                myIntent.putExtra("shareTripImage", true);
                myIntent.putExtra("facebookMessage", trip.getTripSummary());

                if(tabTag.equals(getString(R.string.graph))){
                    myIntent.putExtra("tripImage", saveGraph(graphView).toByteArray());
                }
                else{
                   myIntent.putExtra("tripImage", saveGraph(getTabWidget().getChildAt(1).getRootView()).toByteArray());
                }

                try{
                    startActivity(myIntent);            // Start activity to share the summary on Facebook.
                }catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(ViewTrip.this, "Failed to post to facebook.", Toast.LENGTH_LONG).show();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Get the value that determines if thresholds should be displayed in the graph or not.
     * @return Returns 1 if thresholds should be shown.
     */
    public static int showThresholds() {
        return showThresholds;
    }

    public static ByteArrayOutputStream saveGraph(View v){

        synchronized (v){
            v.setDrawingCacheEnabled(true);
            //v.invalidate();
            Bitmap b = v.getDrawingCache(true);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            v.setDrawingCacheEnabled(false);

            return stream;
        }
    }

}