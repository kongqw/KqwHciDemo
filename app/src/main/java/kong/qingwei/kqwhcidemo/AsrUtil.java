package kong.qingwei.kqwhcidemo;

import android.app.Activity;
import android.util.Log;

import com.sinovoice.hcicloudsdk.android.asr.recorder.ASRRecorder;
import com.sinovoice.hcicloudsdk.common.asr.AsrConfig;
import com.sinovoice.hcicloudsdk.common.asr.AsrGrammarId;
import com.sinovoice.hcicloudsdk.common.asr.AsrInitParam;
import com.sinovoice.hcicloudsdk.common.asr.AsrRecogResult;
import com.sinovoice.hcicloudsdk.recorder.ASRCommonRecorder;
import com.sinovoice.hcicloudsdk.recorder.ASRRecorderListener;
import com.sinovoice.hcicloudsdk.recorder.RecorderEvent;

import java.io.IOException;
import java.io.InputStream;


/**
 * Created by kqw on 2016/8/15.
 * 语音识别类
 */
public class AsrUtil {

    private static final String TAG = "AsrUtil";
    private Activity mActivity;
    private ASRRecorder mAsrRecorder;
    private AsrConfig asrConfig;
    private OnAsrRecogListener mOnAsrRecogListener;
    private String mGrammar = null;
    private String mCapKey = ConfigUtil.CAP_KEY_ASR_CLOUD_GRAMMAR;

    public AsrUtil(Activity activity) {
        mActivity = activity;
        initAsr();
        initGrammar(mCapKey);
    }

    /**
     * 初始化语音识别
     */
    private void initAsr() {
        Log.i(TAG, "initAsr: ");
        // 初始化录音机
        mAsrRecorder = new ASRRecorder();

        // 配置初始化参数
        AsrInitParam asrInitParam = new AsrInitParam();
        String dataPath = mActivity.getFilesDir().getPath().replace("files", "lib");
        asrInitParam.addParam(AsrInitParam.PARAM_KEY_INIT_CAP_KEYS, mCapKey);
        asrInitParam.addParam(AsrInitParam.PARAM_KEY_DATA_PATH, dataPath);
        asrInitParam.addParam(AsrInitParam.PARAM_KEY_FILE_FLAG, AsrInitParam.VALUE_OF_PARAM_FILE_FLAG_ANDROID_SO);
        Log.v(TAG, "init parameters:" + asrInitParam.getStringConfig());

        // 设置初始化参数
        mAsrRecorder.init(asrInitParam.getStringConfig(), new ASRResultProcess());

        // 配置识别参数
        asrConfig = new AsrConfig();
        // PARAM_KEY_CAP_KEY 设置使用的能力
        asrConfig.addParam(AsrConfig.SessionConfig.PARAM_KEY_CAP_KEY, mCapKey);
        // PARAM_KEY_AUDIO_FORMAT 音频格式根据不同的能力使用不用的音频格式
        asrConfig.addParam(AsrConfig.AudioConfig.PARAM_KEY_AUDIO_FORMAT, AsrConfig.AudioConfig.VALUE_OF_PARAM_AUDIO_FORMAT_PCM_16K16BIT);
        // PARAM_KEY_ENCODE 音频编码压缩格式，使用OPUS可以有效减小数据流量
        asrConfig.addParam(AsrConfig.AudioConfig.PARAM_KEY_ENCODE, AsrConfig.AudioConfig.VALUE_OF_PARAM_ENCODE_SPEEX);
        // 其他配置，此处可以全部选取缺省值

        asrConfig.addParam("intention", "weather");
    }

    /**
     * 初始化语法
     *
     * @param capKey CapKey
     */
    public void initGrammar(String capKey) {
        // 语法相关的配置,若使用自由说能力可以不必配置该项
        if (capKey.contains("local.grammar")) {
            mGrammar = loadGrammar("stock_10001.gram");
            // 加载本地语法获取语法ID
            AsrGrammarId id = new AsrGrammarId();
            ASRCommonRecorder.loadGrammar("capkey=" + capKey + ",grammarType=jsgf", mGrammar, id);
            Log.d(TAG, "grammarid=" + id);
            // PARAM_KEY_GRAMMAR_TYPE 语法类型，使用自由说能力时，忽略以下此参数
            asrConfig.addParam(AsrConfig.GrammarConfig.PARAM_KEY_GRAMMAR_TYPE, AsrConfig.GrammarConfig.VALUE_OF_PARAM_GRAMMAR_TYPE_ID);
            asrConfig.addParam(AsrConfig.GrammarConfig.PARAM_KEY_GRAMMAR_ID, "" + id.getGrammarId());
        } else if (capKey.contains("cloud.grammar")) {
            mGrammar = loadGrammar("stock_10001.gram");
            // PARAM_KEY_GRAMMAR_TYPE 语法类型，使用自由说能力时，忽略以下此参数
            asrConfig.addParam(AsrConfig.GrammarConfig.PARAM_KEY_GRAMMAR_TYPE, AsrConfig.GrammarConfig.VALUE_OF_PARAM_GRAMMAR_TYPE_JSGF);
        }
    }

    /**
     * 开始语音识别
     */
    public void start(OnAsrRecogListener listener) {
        mOnAsrRecogListener = listener;
        if (mAsrRecorder.getRecorderState() == ASRRecorder.RECORDER_STATE_IDLE) {
            asrConfig.addParam(AsrConfig.SessionConfig.PARAM_KEY_REALTIME, "no");
            mAsrRecorder.start(asrConfig.getStringConfig(), mGrammar);
        } else {
            Log.i(TAG, "start: 录音机未处于空闲状态，请稍等");
        }
    }

    private class ASRResultProcess implements ASRRecorderListener {
        @Override
        public void onRecorderEventError(RecorderEvent event, int errorCode) {
            Log.i(TAG, "onRecorderEventError: errorCode = " + errorCode);
            if (null != mOnAsrRecogListener) {
                mOnAsrRecogListener.onError(errorCode);
            }
        }

        @Override
        public void onRecorderEventRecogFinsh(RecorderEvent recorderEvent, final AsrRecogResult arg1) {
            if (recorderEvent == RecorderEvent.RECORDER_EVENT_RECOGNIZE_COMPLETE) {
                Log.i(TAG, "onRecorderEventRecogFinsh: 识别结束");
            }
            if (null != mOnAsrRecogListener) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mOnAsrRecogListener.onAsrRecogResult(arg1);
                    }
                });
            }
        }

        @Override
        public void onRecorderEventStateChange(RecorderEvent recorderEvent) {
            if (recorderEvent == RecorderEvent.RECORDER_EVENT_BEGIN_RECORD) {
                Log.i(TAG, "onRecorderEventStateChange: 开始录音");
            } else if (recorderEvent == RecorderEvent.RECORDER_EVENT_BEGIN_RECOGNIZE) {
                Log.i(TAG, "onRecorderEventStateChange: 开始识别");
            } else if (recorderEvent == RecorderEvent.RECORDER_EVENT_NO_VOICE_INPUT) {
                Log.i(TAG, "onRecorderEventStateChange: 无音频输入");
            } else {
                Log.i(TAG, "onRecorderEventStateChange: recorderEvent = " + recorderEvent);
            }
        }

        @Override
        public void onRecorderRecording(byte[] volumedata, int volume) {
            if (null != mOnAsrRecogListener) {
                mOnAsrRecogListener.onVolume(volume);
            }
        }

        @Override
        public void onRecorderEventRecogProcess(RecorderEvent recorderEvent, AsrRecogResult arg1) {
            if (recorderEvent == RecorderEvent.RECORDER_EVENT_RECOGNIZE_PROCESS) {
                Log.i(TAG, "onRecorderEventRecogProcess: 识别中间反馈");
            }
            if (arg1 != null) {
                if (arg1.getRecogItemList().size() > 0) {
                    Log.i(TAG, "onRecorderEventRecogProcess: 识别中间结果结果为：" + arg1.getRecogItemList().get(0).getRecogResult());
                } else {
                    Log.i(TAG, "onRecorderEventRecogProcess: 未能正确识别,请重新输入");
                }
            }
        }
    }

    /**
     * 读取语法
     *
     * @param fileName 文件名
     * @return 语法
     */
    private String loadGrammar(String fileName) {
        String grammar = "";
        InputStream is = null;
        try {
            is = mActivity.getAssets().open(fileName);
            byte[] data = new byte[is.available()];
            is.read(data);
            grammar = new String(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return grammar;
    }

    /**
     * 语音识别的回调接口
     */
    public interface OnAsrRecogListener {
        // 识别结果
        void onAsrRecogResult(AsrRecogResult asrRecogResult);

        // 识别错误码
        void onError(int errorCode);

        // 录音音量
        void onVolume(int volume);
    }
}
