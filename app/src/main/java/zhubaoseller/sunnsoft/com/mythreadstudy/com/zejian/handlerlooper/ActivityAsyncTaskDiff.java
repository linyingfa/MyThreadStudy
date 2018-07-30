package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import java.text.SimpleDateFormat;
import java.util.Date;

import zhubaoseller.sunnsoft.com.mythreadstudy.R;
import zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper.util.LogUtils;

/**
 * Created by zejian
 * Time 16/9/5.
 * Description:
 */
public class ActivityAsyncTaskDiff extends Activity {
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_async_diff);
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                /**
                 * AsyncTask在不同android版本的下的差异
                 这里我们主要区分一下android3.0前后版本的差异，
                 在android 3.0之前，AsyncTask处理任务时默认采用的是线程池里并行处理任务的方式，
                 而在android 3.0之后 ，为了避免AsyncTask处理任务时所带来的并发错误，AsyncTask则采用了单线程串行执行任务。
                 但是这并不意味着android 3.0之后只能执行串行任务，我们仍然可以采用AsyncTask的executeOnExecutor方法来并行执行任务。
                 接下来，编写一个案例，分别在android 2.3.3 和 android 6.0上执行，然后打印输出日志。代码如下：
                 */

                //TODO 串行任务
                new AysnTaskDiff("AysnTaskDiff-1").execute("");
//                new AysnTaskDiff("AysnTaskDiff-2").execute("");
//                new AysnTaskDiff("AysnTaskDiff-3").execute("");
//                new AysnTaskDiff("AysnTaskDiff-4").execute("");
//                new AysnTaskDiff("AysnTaskDiff-5").execute("");


                //TODO 并行执行任务
                new AysnTaskDiff("AysnTaskDiff-5").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            }
        });
    }

    private static class AysnTaskDiff extends AsyncTask<String, Integer, String> {
        private String name;

        public AysnTaskDiff(String name) {
            super();
            this.name = name;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Thread.sleep(2000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return name;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            LogUtils.e(s + " execute 执行完成时间:" + df.format(new Date()));
        }
    }

}
