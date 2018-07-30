package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.Message;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper.util.LogUtils;

/**
 * Created by zejian
 * Time 16/9/3.
 * Description:
 */
public  class MyIntentService extends IntentService {
    public static final String DOWNLOAD_URL="download_url";
    public static final String INDEX_FLAG="index_flag";
    public static UpdateUI updateUI;


    public static void setUpdateUI(UpdateUI updateUIInterface){
        updateUI=updateUIInterface;
    }

    public MyIntentService(){
        super("MyIntentService");
    }

    /**
     * 实现异步任务的方法
     * @param intent Activity传递过来的Intent,数据封装在intent中
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        //在子线程中进行网络请求
        Bitmap bitmap=downloadUrlBitmap(intent.getStringExtra(DOWNLOAD_URL));
        Message msg1 = new Message();
        msg1.what = intent.getIntExtra(INDEX_FLAG,0);
        msg1.obj =bitmap;
        //通知主线程去更新UI
        if(updateUI!=null){
            updateUI.updateUI(msg1);
        }
        //mUIHandler.sendMessageDelayed(msg1,1000);

        LogUtils.e("onHandleIntent");
    }
    //----------------------重写一下方法仅为测试------------------------------------------
    @Override
    public void onCreate() {
        LogUtils.e("onCreate");
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        LogUtils.e("onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.e("onStartCommand");
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        LogUtils.e("onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.e("onBind");
        return super.onBind(intent);
    }


    public interface UpdateUI{
        void updateUI(Message message);
    }


    private Bitmap downloadUrlBitmap(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;
        Bitmap bitmap=null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(), 8 * 1024);
            bitmap= BitmapFactory.decodeStream(in);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

}
