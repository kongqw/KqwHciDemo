package kong.qingwei.kqwhcidemo;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.sinovoice.hcicloudsdk.common.asr.AsrRecogItem;
import com.sinovoice.hcicloudsdk.common.asr.AsrRecogResult;
import com.sinovoice.hcicloudsdk.common.nlu.NluRecogResult;
import com.sinovoice.hcicloudsdk.common.nlu.NluRecogResultItem;
import com.sinovoice.hcicloudsdk.player.TTSCommonPlayer;
import com.sinovoice.hcicloudsdk.player.TTSPlayerListener;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements TTSPlayerListener {

    private static final String TAG = "MainActivity";
    private EditText mEditText;
    private EditText mEditText2;
    private boolean isInitPlayer;
    private TtsUtil mTtsUtil;
    private HciUtil mInitTts;
    private NluUtil mNluUtil;
    private AsrUtil mAsrUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mEditText = (EditText) findViewById(R.id.edit_text);
        mEditText2 = (EditText) findViewById(R.id.edit_text2);

        // 灵云语音工具类
        mInitTts = new HciUtil(this);
        // 初始化灵云语音
        boolean isInitHci = mInitTts.initHci();
        if (isInitHci) { // 初始化成功
            // 语音合成能力工具类
            mTtsUtil = new TtsUtil(this);
            // 初始化语音合成
            isInitPlayer = mTtsUtil.initPlayer(this);

            // 语义理解
            mNluUtil = new NluUtil(this);
            boolean isInitNul = mNluUtil.initNul();
            if (isInitNul) {
                Toast.makeText(this, "语义理解 初始化成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "语义理解 初始化失败", Toast.LENGTH_SHORT).show();
            }

            // 语音识别
            mAsrUtil = new AsrUtil(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTtsUtil != null) {
            mTtsUtil.release();
        }
        if (null != mInitTts) {
            mInitTts.hciRelease();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 开始合成
     *
     * @param view v
     */
    public void synth(View view) {
        if (!isInitPlayer) {
            Toast.makeText(this, "初始化失败", Toast.LENGTH_SHORT).show();
            return;
        }
        String text = mEditText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "合成内容为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mTtsUtil.synth(text);
    }

    /**
     * 语义理解
     *
     * @param view view
     */
    public void recog(View view) {
        final String text = mEditText2.getText().toString();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(this, "理解句子内容为空", Toast.LENGTH_SHORT).show();
            return;
        }
        mNluUtil.recog(text, new NluUtil.OnNluRecogListener() {
            @Override
            public void onNluResult(NluRecogResult nluRecogResult) {
                StringBuilder stringBuffer = new StringBuilder();
                ArrayList<NluRecogResultItem> nluRecogResultItems = nluRecogResult.getRecogResultItemList();
                for (NluRecogResultItem nluRecogResultItem : nluRecogResultItems) {
                    String result = nluRecogResultItem.getResult();
                    stringBuffer.append(result).append("\n");
                    Log.i(TAG, "onNluResult: " + result);
                }
                showDialog(text, stringBuffer.toString());
            }

            @Override
            public void onError(int errorCode) {
                Log.i(TAG, "onError: errorCode = " + errorCode);
            }
        });
    }

    /**
     * 语音识别（语音转文字）
     *
     * @param view view
     */
    public void asr(View view) {
        mAsrUtil.start(new AsrUtil.OnAsrRecogListener() {
            @Override
            public void onAsrRecogResult(AsrRecogResult asrRecogResult) {
                StringBuilder stringBuffer = new StringBuilder();
                ArrayList<AsrRecogItem> asrRecogItemArrayList = asrRecogResult.getRecogItemList();
                for (AsrRecogItem asrRecogItem : asrRecogItemArrayList) {
                    String result = asrRecogItem.getRecogResult();
                    Log.i(TAG, "onAsrRecogResult: " + result);
                    stringBuffer.append(result).append("\n");
                }
                showDialog("识别结果", stringBuffer.toString());
            }

            @Override
            public void onError(int errorCode) {
                Log.i(TAG, "onError: " + errorCode);
            }

            @Override
            public void onVolume(int volume) {
                Log.i(TAG, "onVolume: " + volume);
            }
        });
    }

    // 语音合成状态的回调
    @Override
    public void onPlayerEventStateChange(TTSCommonPlayer.PlayerEvent playerEvent) {
        Log.i(TAG, "onStateChange " + playerEvent.name());
    }

    // 合成进度回调
    @Override
    public void onPlayerEventProgressChange(TTSCommonPlayer.PlayerEvent playerEvent, int start, int end) {
        Log.i(TAG, "onProcessChange " + playerEvent.name() + " from " + start + " to " + end);
    }

    // 错误回调
    @Override
    public void onPlayerEventPlayerError(TTSCommonPlayer.PlayerEvent playerEvent, int errorCode) {
        Log.i(TAG, "onError " + playerEvent.name() + " code: " + errorCode);
    }
}
