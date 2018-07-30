package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


import zhubaoseller.sunnsoft.com.mythreadstudy.R;
import zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper.util.LogUtils;

public class MainActivity extends AppCompatActivity {

    public static final int MSG_FINISH = 0X001;
    //创建一个Handler的匿名内部类
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FINISH:
                    LogUtils.e("handler所在的线程id是-->" + Thread.currentThread().getName());
                    break;
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //启动耗时操作
        consumeTimeThread(findViewById(R.id.tv));
//        handler.post()
    }

    //启动一个耗时线程
    public void consumeTimeThread(View view) {
        new Thread() {
            public void run() {
                try {
                    LogUtils.e("耗时子线程的Name是--->" + Thread.currentThread().getName());
                    //在子线程运行
                    Thread.sleep(2000);
                    //完成后，发送下载完成消息
                    handler.sendEmptyMessage(MSG_FINISH);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    class childThread extends Thread{
        public Handler mHandler;

        @Override
        public void run() {
            //子线程中必须先创建Looper
            Looper.prepare();

            mHandler =new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //处理消息
                }
            };
            //启动looper循环
            Looper.loop();
        }
    }
}
