package kong.qingwei.kqwhcidemo;

/**
 * Created by kqw on 2016/8/12.
 * 灵云配置信息
 */
public final class ConfigUtil {

    /**
     * 灵云APP_KEY
     */
    public static final String APP_KEY = "3d5d5466";

    /**
     * 开发者密钥
     */
    public static final String DEVELOPER_KEY = "eca643ff7b3c758745d7cf516e808d34";

    /**
     * 灵云云服务的接口地址
     */
    public static final String CLOUD_URL = "test.api.hcicloud.com:8888";

    /**
     * 需要运行的灵云能力
     */
    // 离线语音合成
    public static final String CAP_KEY_TTS_LOCAL = "tts.local.synth";
    // 云端语音合成
    public static final String CAP_KEY_TTS_CLOUD = "tts.cloud.wangjing";

    // 云端语义识别
    public static final String CAP_KEY_NUL_CLOUD = "nlu.cloud";

    // 云端自由说
    public static final String CAP_KEY_ASR_CLOUD_FREETALK = "asr.cloud.freetalk";
    // 离线自由说
    public static final String CAP_KEY_ASR_LOCAL_FREETALK = "asr.local.freetalk";
    // 云端语音识别+语义
    public static final String CAP_KEY_ASR_CLOUD_DIALOG = "asr.cloud.dialog";
    // 离线命令词
    public static final String CAP_KEY_ASR_LOCAL_GRAMMAR = "asr.local.grammar.v4";
    // 在线命令词
    public static final String CAP_KEY_ASR_CLOUD_GRAMMAR = "asr.cloud.grammar";
}
