package zhubaoseller.sunnsoft.com.mythreadstudy.com.zejian.handlerlooper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import zhubaoseller.sunnsoft.com.mythreadstudy.R;

/**
 * Created by zejian
 * Time 16/9/3.
 * Description:
 */
public class IntentServiceActivity extends Activity implements MyIntentService.UpdateUI{
    /**
     * 图片地址集合
     */
    private String url[] = {
            "http://img.blog.csdn.net/20160903083245762",
            "http://img.blog.csdn.net/20160903083252184",
            "http://img.blog.csdn.net/20160903083257871",
            "http://img.blog.csdn.net/20160903083257871",
            "http://img.blog.csdn.net/20160903083311972",
            "http://img.blog.csdn.net/20160903083319668",
            "http://img.blog.csdn.net/20160903083326871"
    };

    private static ImageView imageView;
    private static final Handler mUIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            imageView.setImageBitmap((Bitmap) msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intent_service);
        imageView = (ImageView) findViewById(R.id.image);

        Intent intent = new Intent(this,MyIntentService.class);
        for (int i=0;i<7;i++) {

            intent.putExtra(MyIntentService.DOWNLOAD_URL,url[i]);
            intent.putExtra(MyIntentService.INDEX_FLAG,i);
            startService(intent);
        }
        MyIntentService.setUpdateUI(this);
    }


    @Override
    public void updateUI(Message message) {
        mUIHandler.sendMessageDelayed(message,message.what * 1000);
    }
}
