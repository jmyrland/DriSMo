package com.drismo.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.task.callback.TaskCompleteCallback;
import com.drismo.utils.FileController;

import java.io.File;

public class DeleteAllTask extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private FileController controller;
    private TaskCompleteCallback callback;
    private ProgressDialog progress;

    public DeleteAllTask(Context context, FileController controller, TaskCompleteCallback callback) {
        this.context = context;
        this.controller = controller;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        setProgressDialog(controller.getCount());
        progress.show();
    }

    @Override
    protected Boolean doInBackground(String... fileNames) {
        int deleteCount = 0;

        for(String fileName : fileNames) {
            File file = controller.getFile(fileName);
            if(file.delete()) deleteCount++;
            progress.setProgress(deleteCount + 1);
        }
        return (deleteCount == fileNames.length);
    }

    private void setProgressDialog(int total) {
        progress = new ProgressDialog(context);
        progress.setCancelable(false);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(total);
        progress.setMessage(context.getString(R.string.deletingTrips)+"...");
    }

    @Override
    protected void onPostExecute(Boolean deletedAll) {
        if(!deletedAll)
            Toast.makeText(context, context.getString(R.string.unableToDeleteAll), Toast.LENGTH_LONG).show();

        callback.onComplete(deletedAll);
        progress.dismiss();
    }
}