package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import zhubaoseller.sunnsoft.com.mythreadstudy.R;

/**
 * Created by zejian on 16/3/6.
 */
public class HandlerActivity extends Activity {

    //创建一个2M大小的int数组
    int[] datas = new int[1024 * 1024 * 2];
    private final Handler mLeakyHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // ...
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_leak);
        mLeakyHandler.postDelayed(new Runnable() {
            @Override
            public void run() { /* ... */ }
        }, 1000 * 60 * 10);
        // Go back to the previous Activity.


}

//    Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    };

    /**
     * 创建静态内部类
     */
//    private static class MyHandler extends Handler{
//        //持有弱引用HandlerActivity,GC回收时会被回收掉.
//        private final WeakReference<HandlerActivity> mActivty;
//
//        public MyHandler(HandlerActivity activity){
//            mActivty =new WeakReference<HandlerActivity>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            HandlerActivity activity=mActivty.get();
//            super.handleMessage(msg);
//            if(activity!=null){
//                //执行业务逻辑
//            }
//        }
//    }

    private static final Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            //执行我们的业务逻辑
        }
    };

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_handler_leak);
//        MyHandler myHandler=new MyHandler(this);
//        //解决了内存泄漏,延迟5分钟后发送
//        myHandler.postDelayed(myRunnable, 1000 * 60 * 5);
//    }
}
