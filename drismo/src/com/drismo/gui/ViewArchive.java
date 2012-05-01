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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.drismo.R;
import com.drismo.adapter.ArchiveListAdapter;
import com.drismo.facebook.ShareOnFacebook;
import com.drismo.gui.quickaction.QuickActionMenu;
import com.drismo.model.Config;
import com.drismo.model.Trip;
import com.drismo.task.DeleteAllTask;
import com.drismo.task.DeleteOneTask;
import com.drismo.task.RenameTask;
import com.drismo.task.callback.TaskCompleteCallback;
import com.drismo.utils.FileComparator;
import com.drismo.utils.FileController;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ViewArchive extends Activity implements TaskCompleteCallback {

    private ListView archiveList;
    private ArchiveListAdapter archiveAdapter;
    private QuickActionMenu quickActionMenu;

    private DeleteOneTask deleteOneTask;
    private DeleteAllTask deleteAllTask;
    private FileController fileController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.archive);

        createFileController();
        createFileOperationTasks();
        setupArchiveList();

        try {
            Config.setConfigLocale(getBaseContext(), Config.getLanguageCode());
        } catch(Exception e){
            e.printStackTrace();
        }
        setBackgroundFromInitialOrientation();
    }

    private void setBackgroundFromInitialOrientation() {
        if(getResources().getConfiguration().orientation == 2)
            findViewById(R.id.archiveLayout)
                .setBackgroundDrawable(ViewArchive.this.getResources().getDrawable(R.drawable.bg_ls));
    }

    private void createFileController() {
        fileController = new FileController(this, getApplicationContext().getFilesDir());
    }

    private void createFileOperationTasks() {
        deleteOneTask = new DeleteOneTask(this, fileController, this);
        deleteAllTask = new DeleteAllTask(this, fileController, this);
    }

    private void setupArchiveList() {
        archiveAdapter = new ArchiveListAdapter(this, fileController);
        archiveList = (ListView) findViewById(R.id.archiveList);
        archiveList.setAdapter(archiveAdapter);
        archiveAdapter.sort(new FileComparator(fileController));

        quickActionMenu = new QuickActionMenu(this);
        setListItemListeners();
        showEmptyLayoutIfNoTrips();
    }

    private void setListItemListeners() {
        archiveList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ViewArchive.this, ViewTrip.class);
                intent.putExtra(ViewTrip.EXTRA_FILENAME, archiveAdapter.getItem(position));
                startActivity(intent);
            }
        });
        archiveList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                buildQuickActionMenu(archiveAdapter.getItem(position));
                quickActionMenu.showQuickActionsForItem(view);
                return true;
            }
        });
    }

    private void buildQuickActionMenu(String fileName) {
        quickActionMenu.setDeleteListener(createDeleteListener(fileName));
        quickActionMenu.setExportListener(createExportListener(fileName));
        quickActionMenu.setFacebookListener(createFacebookListener(fileName));
        quickActionMenu.setRenameListener(createRenameListener(fileName));
        quickActionMenu.setViewListener(createViewListener(fileName));
    }

    private void showEmptyLayoutIfNoTrips() {
        if(fileController.getCount() < 1) {
            TextView noTripsView = (TextView) findViewById(R.id.archiveTextNoTripData);
            Typeface font = Typeface.createFromAsset(getAssets(), "eras-demi.ttf");
            noTripsView.setTypeface(font);
            noTripsView.setVisibility(View.VISIBLE);
        }
    }

    private View.OnClickListener createExportListener(final String fileName) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + fileController.getDirectoryString() + fileName));
                startActivity( Intent.createChooser(intent, "Export file..") );
                quickActionMenu.dismiss();
            }
        };
    }

    private View.OnClickListener createFacebookListener(final String fileName) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Trip trip = new Trip(fileName, ViewArchive.this);
                    Intent intent = new Intent(ViewArchive.this, ShareOnFacebook.class);
                    intent.putExtra("facebookMessage", trip.getTripSummary());
                    intent.putExtra("shareImage", false);

                    try {
                        startActivity(intent);
                    } catch(Exception e){
                        Toast.makeText(ViewArchive.this, "Failed to post to facebook.", Toast.LENGTH_LONG).show();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                } finally {
                    quickActionMenu.dismiss();
                }
            }
        };
    }

    private View.OnClickListener createDeleteListener(final String fileName) {
        return new View.OnClickListener() {
            public void onClick(View v) {
               new AlertDialog.Builder(ViewArchive.this)
                  .setMessage(getString(R.string.delTripData)+"\n\n"+fileName)
                  .setTitle(R.string.delete)
                  .setCancelable(false)
                  .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton){
                         deleteOneTask.execute(fileName);
                     }})
                  .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton){
                     }})
                  .show();
               quickActionMenu.dismiss();
            }
        };
    }

    private View.OnClickListener createRenameListener(final String fileName) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                EditText fileNameInput = new EditText(ViewArchive.this);
                fileNameInput.setText(fileName.substring(0, fileName.length()-FileController.FILE_EXTENSION.length()));
                fileNameInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
                fileNameInput.setSelection(fileNameInput.getText().length());
                fileNameInput.setLines(1);
                showRenameDialog(fileName);
                quickActionMenu.dismiss();
            }
        };
    }

    private View.OnClickListener createViewListener(final String fileName) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ViewArchive.this, ViewTrip.class);
                intent.putExtra(ViewTrip.EXTRA_FILENAME, fileName);
                startActivity(intent);
                quickActionMenu.dismiss();
            }
        };
    }

    private String[] listToArray(List<String> prevTrips) {
        String[] trips = new String[prevTrips.size()];

        for(int i = 0; i < prevTrips.size(); i++) {
            trips[i] = prevTrips.get(i);
        }
        return trips;
    }

    public void showRenameDialog(final String oldFileName) {
        final EditText fileNameInput = new EditText(this);
        fileNameInput.setText(oldFileName.substring(0, oldFileName.length()-FileController.FILE_EXTENSION.length()));

        new AlertDialog.Builder(ViewArchive.this)
            .setTitle(getString(R.string.rename))
            .setMessage(getString(R.string.newFileName))
            .setView(fileNameInput)
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    tryRenameFile(fileNameInput, oldFileName);
                }
            })
            .setNegativeButton(getString(R.string.cancel), null)
            .show();
    }

    private void tryRenameFile(EditText fileNameInput, String oldFile) {
        String newFileName = fileNameInput.getText().toString();
        if(!newFileName.endsWith(FileController.FILE_EXTENSION))
            newFileName += FileController.FILE_EXTENSION;

        File file = fileController.getFile(newFileName);

        if(file.exists()) {
            Toast.makeText(this, getString(R.string.fileNameInUse), Toast.LENGTH_SHORT).show();
            showRenameDialog(oldFile);  // If filename is already in use, call this function recursively,
        }                               // until a valid name is typed, or the user cancel the alertdialog.
        else {
            String fileNames[] = { oldFile, newFileName };
            new RenameTask(this, fileController, this).execute(fileNames);
        }
    }

    @Override
    public void onComplete(boolean success) {
        if(!success) return;

        archiveAdapter.clear();
        archiveAdapter.addTrips(fileController.listFiles());
        archiveAdapter.sort(new FileComparator(fileController));

        showEmptyLayoutIfNoTrips();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.archive_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll:
                new AlertDialog.Builder(ViewArchive.this)
                        .setMessage(getString(R.string.delAllTripData))
                        .setTitle(R.string.deleteAll)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteAllTask.execute(listToArray(fileController.listFiles()));
                            }
                        })
                        .setNegativeButton(getString(R.string.no), null)
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

