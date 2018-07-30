package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import zhubaoseller.sunnsoft.com.mythreadstudy.R;
import zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper.util.LoadProgressBarWithNum;
import zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper.util.LogUtils;


/**
 * Created by zejian
 * Time 16/9/4.
 * Description:AsynTaskActivity
 */
public class AsynTaskActivity extends Activity implements DownLoadAsyncTask.UpdateUI {
    private static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 0x11;

    private static String DOWNLOAD_FILE_ZIP_URL = "http://dl.download.csdn.net/down11/20160119/6d4b7b1d239e030e1178532b1ba6f42f.zip?response-content-disposition=attachment%3Bfilename%3D%22emotionkeyboard.zip%22&OSSAccessKeyId=9q6nvzoJGowBj4q1&Expires=1473037024&Signature=FrIlo0hY%2Fg1bOXLkEeLsYIMyZlw%3D";
    private static String DOWNLOAD_FILE_JPG_URL = "http://img2.3lian.com/2014/f6/173/d/51.jpg";
    private static String JPG_2 = "https://img2.rrcimg.com/o_1cjj64lpj516202505256631971136915.jpg?imageView/4/w/600/h/400";
    private LoadProgressBarWithNum progressBar;
    private Button downloadBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        progressBar = findViewById(R.id.progressbar);
        downloadBtn = findViewById(R.id.downloadBtn);
        //create DownLoadAsyncTask
        final DownLoadAsyncTask downLoadAsyncTask = new DownLoadAsyncTask(AsynTaskActivity.this);
        //set Interface
        downLoadAsyncTask.setUpdateUIInterface(this);
        //start download
        downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //execute
                //该方法是一个final方法，参数类型是可变类型，实际上这里传递的参数和doInBackground(Params…params)方法中的参数是一样的，
                //该方法最终返回一个AsyncTask的实例对象，可以使用该对象进行其他操作，比如结束线程之类的。启动范例如下：
                //new DownLoadAsyncTask().execute(url1,url2,url3);
                downLoadAsyncTask.execute(DOWNLOAD_FILE_JPG_URL, JPG_2);
            }
        });

        //android 6.0 权限申请
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //android 6.0 API 必须申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                LogUtils.e("Permission Granted");
            } else {
                // Permission Denied
                LogUtils.e("Permission Denied");
            }
        }
    }

    /**
     * update progressBar
     *
     * @param values
     */
    @Override
    public void UpdateProgressBar(Integer values) {
        progressBar.setmProgress(values);
    }
}
