package com.drismo.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;
import com.drismo.R;
import com.drismo.task.callback.TaskCompleteCallback;
import com.drismo.utils.FileController;

public class RenameTask extends AsyncTask<String[], Void, Boolean> {
    private Context context;
    private FileController controller;
    private TaskCompleteCallback callback;
    private ProgressDialog progress;

    public RenameTask(Context context, FileController controller, TaskCompleteCallback callback) {
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
    protected Boolean doInBackground(String[]... param) {
        String[] fileNames = param[0];
        return controller.rename(fileNames[0], fileNames[1]);
    }

    @Override
    protected void onPostExecute(Boolean renamed) {
        String toastMessage = (renamed ? context.getString(R.string.renameSuccess)
                                       : context.getString(R.string.renameFailed));
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
        callback.onComplete(renamed);
        progress.dismiss();
    }
}