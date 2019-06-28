package net.balqisstudio.goeksdriver.network;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

/**
 * Created by Balqis Studio on 27/11/2017.
 */

public class AsyncTaskHelperNoLoad extends AsyncTask<Void, Void, String> {

    public ProgressDialog dialog;
    public Activity activity;
    public boolean isDialogShow;

    OnAsyncTaskListener mCallback;

    public interface OnAsyncTaskListener {
        void onAsyncTaskDoInBackground(AsyncTaskHelperNoLoad asyncTask);

        void onAsyncTaskProgressUpdate(AsyncTaskHelperNoLoad asyncTask);

        void onAsyncTaskPostExecute(AsyncTaskHelperNoLoad asyncTask);

        void onAsyncTaskPreExecute(AsyncTaskHelperNoLoad asyncTask);
    }

    public void setAsyncTaskListener(OnAsyncTaskListener listener) {
        try {
            mCallback = listener;
        } catch (ClassCastException e) {
            throw new ClassCastException(this.toString() + "Did not implement OnMGAsyncTaskListener");
        }
    }

    public AsyncTaskHelperNoLoad(Activity activity, boolean isDialogShow) {
        this.activity = activity;
        this.isDialogShow = isDialogShow;
    }

    @Override
    protected void onPreExecute() {
        // Things to be done before execution of long running operation. For example showing ProgessDialog
        if (isDialogShow) {
//            dialog = ProgressDialog.show(activity, "", "Loading...", true);
        }
        mCallback.onAsyncTaskPreExecute(this);
    }

    @Override
    protected String doInBackground(Void... params) {
        mCallback.onAsyncTaskDoInBackground(this);
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        // execution of result of Long time consuming operation. parse json data
        if (isDialogShow) {
//            dialog.dismiss();
        }
        mCallback.onAsyncTaskPostExecute(this);
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        // Things to be done while execution of long running operation is in progress. For example updating ProgessDialog
        mCallback.onAsyncTaskProgressUpdate(this);
    }
}
