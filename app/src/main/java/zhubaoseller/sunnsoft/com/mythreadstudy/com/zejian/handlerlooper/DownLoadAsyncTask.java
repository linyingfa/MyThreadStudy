package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper.util.LogUtils;

/**
 * 我们在使用AsyncTask时还必须遵守一些规则，以避免不必要的麻烦。
 * <p>
 * (1) AsyncTask的实例必须在主线程（UI线程）中创建 ，execute方法也必须在主线程中调用
 * (2) 不要在程序中直接的调用onPreExecute(), onPostExecute(Result)，doInBackground(Params…), onProgressUpdate(Progress…)这几个方法
 * (3) 不能在doInBackground(Params… params)中更新UI
 * (5) 一个AsyncTask对象只能被执行一次，也就是execute方法只能调用一次，否则多次调用时将会抛出异常, 除非执行线程池
 */
public class DownLoadAsyncTask extends AsyncTask<String, Integer, String> {

    private PowerManager.WakeLock mWakeLock;
    private int ValueProgress = 100;
    private Context context;


    public DownLoadAsyncTask(Context context) {
        this.context = context;
    }



    //todo 该方法在主线程中执行，将在execute(Params… params)被调用后执行              1
    //todo ，一般用来做一些UI的准备工作，如在界面上显示一个进度条。
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();
        //Display progressBar
//        progressBar.setVisibility(View.VISIBLE);
    }


    //todo  2 线程池执行， 子线程                                                        2
    @Override
    protected String doInBackground(String... params) {
        //todo 抽象方法，必须实现，该方法在线程池中执行，用于执行异步任务，
        //todo 将在onPreExecute方法执行后执行。其参数是一个可变类型，表示异步任务的输入参数，
        //todo 在该方法中还可通过publishProgress(Progress… values)来更新实时的任务进度，
        //todo 而publishProgress方法则会调用onProgressUpdate方法。
        //todo 此外doInBackground方法会将计算的返回结果传递给onPostExecute方法。
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(params[1]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
            }
            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();
            // download the file
            input = connection.getInputStream();
            //create output
            output = new FileOutputStream(getSDCardDir());
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) // only if total length is known
                    publishProgress((int) (total * 100 / fileLength));
                //
                Thread.sleep(100);
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
            if (connection != null)
                connection.disconnect();
        }

        return null;
    }


    //TODO  最后doInBackground方法执行后完后，onPostExecute方法将被执行。
    @Override
    protected void onPostExecute(String values) {
        //TODO  在主线程中执行，在doInBackground 执行完成后，onPostExecute 方法将被UI线程调用，
        //TODO  doInBackground 方法的返回值将作为此方法的参数传递到UI线程中，并执行一些UI相关的操作，如更新UI视图。
        super.onPostExecute(values);
        mWakeLock.release();
        if (values != null)
            LogUtils.e("Download error: " + values);
        else {
            Toast.makeText(context, "File downloaded", Toast.LENGTH_SHORT).show();
        }
    }


    //TODO 如果调用了publishProgress方法，那么onProgressUpdate方法将会被执行，
    @Override
    protected void onProgressUpdate(Integer... values) {
        //TODO 在主线程中执行，该方法在publishProgress(Progress… values)方法被调用后执行，一般用于更新UI进度，如更新进度条的当前进度。
        super.onProgressUpdate(values);
//        progressBar.setmProgress(values[0]);
        //update progressBar
        if (updateUI != null) {
            updateUI.UpdateProgressBar(values[0]);
        }
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        //TODO 在主线程中执行，当异步任务被取消时,该方法将被调用,要注意的是这个时onPostExecute将不会被执行
    }

    /**
     * get SD card path
     *
     * @return
     */
    public File getSDCardDir() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // 创建一个文件夹对象，赋值为外部存储器的目录
            String dirName = Environment.getExternalStorageDirectory() + "/MyDownload/";
            File f = new File(dirName);
            if (!f.exists()) {
                f.mkdir();
            }
            File downloadFile = new File(f, "new.jpg");
            return downloadFile;
        } else {
            LogUtils.e("NO SD Card!");
            return null;

        }

    }

    public UpdateUI updateUI;


    public interface UpdateUI {
        void UpdateProgressBar(Integer values);
    }

    public void setUpdateUIInterface(UpdateUI updateUI) {
        this.updateUI = updateUI;
    }

}