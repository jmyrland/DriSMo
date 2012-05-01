package com.drismo.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.task.callback.TaskCompleteCallback;
import com.drismo.utils.FileController;

import java.io.File;

public class DeleteOneTask extends AsyncTask<String, Void, Boolean> {
    private Context context;
    private FileController controller;
    private TaskCompleteCallback callback;
    private ProgressDialog progress;

    private String fileName;

    public DeleteOneTask(Context context, FileController controller, TaskCompleteCallback callback) {
        this.context = context;
        this.controller = controller;
        this.callback = callback;
    }

    protected void onPreExecute() {
        setProgressDialog();
        progress.show();
    }

    private void setProgressDialog() {
        progress = new ProgressDialog(context);
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminateDrawable(context.getResources().getDrawable(R.anim.loading));
        progress.setCancelable(false);
        progress.setMessage(context.getString(R.string.pleaseWait));
    }

    @Override
    protected Boolean doInBackground(String... fileNames) {
        fileName = fileNames[0];
        File file = controller.getFile(fileName);
        return file.delete();
    }

    @Override
    protected void onPostExecute(Boolean deleted) {
        if(deleted)
            Toast.makeText(context, fileName + " " + context.getString(R.string.beenDeleted), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, context.getString(R.string.unableToDelete)+" "+ fileName, Toast.LENGTH_SHORT).show();

        callback.onComplete(deleted);
        progress.dismiss();
    }
}

