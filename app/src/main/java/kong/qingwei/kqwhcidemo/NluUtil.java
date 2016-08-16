package kong.qingwei.kqwhcidemo;

import android.app.Activity;
import android.util.Log;

import com.sinovoice.hcicloudsdk.api.nlu.HciCloudNlu;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.Session;
import com.sinovoice.hcicloudsdk.common.nlu.NluConfig;
import com.sinovoice.hcicloudsdk.common.nlu.NluInitParam;
import com.sinovoice.hcicloudsdk.common.nlu.NluRecogResult;

/**
 * Created by kqw on 2016/8/12.
 * 语义理解类
 */
public class NluUtil {

    private static final String TAG = "NluUtil";
    private Activity mActivity;

    public NluUtil(Activity activity) {
        mActivity = activity;
    }

    public boolean initNul() {
        //构造Asr初始化的帮助类的实例
        NluInitParam initParam = new NluInitParam();
        // 获取App应用中的lib的路径,放置能力所需资源文件。如果使用/data/data/packagename/lib目录,需要添加android_so的标记
        String dataPath = mActivity.getFilesDir().getAbsolutePath().replace("files", "lib");
        initParam.addParam(NluInitParam.PARAM_KEY_DATA_PATH, dataPath);
        initParam.addParam(NluInitParam.PARAM_KEY_FILE_FLAG, NluInitParam.VALUE_OF_PARAM_FILE_FLAG_ANDROID_SO);
        initParam.addParam(NluInitParam.PARAM_KEY_INIT_CAP_KEYS, ConfigUtil.CAP_KEY_NUL_CLOUD);
        int errCode = HciCloudNlu.hciNluInit(initParam.getStringConfig());
        Log.i(TAG, "initAsr: errCode = " + errCode);
        return errCode == HciErrorCode.HCI_ERR_NONE || errCode == HciErrorCode.HCI_ERR_NLU_ALREADY_INIT;
    }

    public void recog(String text, OnNluRecogListener onNluRecogListener) {
        // 初始化配置参数
        NluConfig nluConfig = initConfig();

        // 创建会话
        Session session = new Session();
        // 开始会话
        int errCode = HciCloudNlu.hciNluSessionStart(nluConfig.getStringConfig(), session);
        if (errCode == HciErrorCode.HCI_ERR_NONE) {
            // 开始翻译
            // 调用HciCloudMt.hciMtTrans() 方法进行合成
            NluRecogResult nluResult = new NluRecogResult();
            errCode = HciCloudNlu.hciNluRecog(session, text, nluConfig.getStringConfig(), nluResult);
            if (errCode == HciErrorCode.HCI_ERR_NONE) {
                onNluRecogListener.onNluResult(nluResult);
            } else {
                onNluRecogListener.onError(errCode);
            }

            // 结束会话
            errCode = HciCloudNlu.hciNluSessionStop(session);
            if (errCode != HciErrorCode.HCI_ERR_NONE) {
                onNluRecogListener.onError(errCode);
            }
        } else {
            onNluRecogListener.onError(errCode);
        }
    }

    private NluConfig initConfig() {
        NluConfig nluConfig = new NluConfig();
        nluConfig.addParam(NluConfig.SessionConfig.PARAM_KEY_CAP_KEY, ConfigUtil.CAP_KEY_NUL_CLOUD);
        nluConfig.addParam("intention", "weather");
        return nluConfig;
    }

    public interface OnNluRecogListener {
        void onNluResult(NluRecogResult nluRecogResult);

        void onError(int errorCode);
    }
}
