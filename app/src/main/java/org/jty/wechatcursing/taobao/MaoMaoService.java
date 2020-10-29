package org.jty.wechatcursing.taobao;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ScrollView;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.Utils;
import com.hjq.xtoast.XToast;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;

import org.jty.wechatcursing.MainActivity;
import org.jty.wechatcursing.R;
import org.jty.wechatcursing.service.IDKey;
import org.jty.wechatcursing.service.ServiceUtils;
import org.jty.wechatcursing.utils.AccessibilityHelper;
import org.jty.wechatcursing.utils.RxTimerUtil;

/**
 * 淘宝集猫猫活动
 * 自动领取猫币
 * 非安卓原生控件 需要点击WebView
 */
public class MaoMaoService extends AccessibilityService {
    private final String TAG = "MaoMaoService";
    public static String APP_PACKAGE = "com.taobao.taobao";
    /**
     * 辅助功能是否启动
     */
    public static boolean isStart = false;

    private long firstTime = 0;
    private long secondTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //时间回调太快 2s检测一次
        secondTime = System.currentTimeMillis();
        if (secondTime - firstTime < 1000) {
            return;
        }
        firstTime = secondTime;
        showLog(TAG + ":" + accessibilityEvent.getPackageName() + "\n" + accessibilityEvent.getClassName());
        AccessibilityHelper.mService = this;
        if (isListHome()) {
            //点击我的淘宝
            boolean tf = checkMyTaobao();
            if (tf) {
                //点击养猫分20亿
                checkMao20yi();
            }
        } else {
            showLog("不在首页..");
            if (isMaoMaotHome()) {
                //正在集猫猫的首页
                selectMaoMaoMuen();
            }
        }
    }

    /**
     * 选择我的淘宝
     */
    private boolean checkMyTaobao() {
        boolean tf = false;
        AccessibilityNodeInfo myTaobao = AccessibilityHelper.findNodeInfosByText("我的淘宝");
        if (myTaobao != null) {
            //点击
            addToasMsg("正在点击[我的淘宝]中..");
            tf = myTaobao.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        if (tf) {
            addToasMsg("点击[我的淘宝]成功");
        } else {
            addToasMsg("请回到淘宝首页,将会自动点击!");
        }
        return tf;
    }

    /**
     * 选择养猫分20亿
     */
    private boolean checkMao20yi() {
        boolean tf = false;
        AccessibilityNodeInfo myTaobao = AccessibilityHelper.findNodeInfosByText("养猫分20亿");

        if (myTaobao != null) {
            //点击
            addToasMsg("正在点击[养猫分20亿中]中..");
            tf = myTaobao.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        if (tf) {
            addToasMsg("点击[养猫分20亿中]成功");
        } else {
            addToasMsg("请回到淘宝首页,将会自动点击!");
        }
        return tf;
    }

    /**
     * 点击领猫币的大致位置
     */
    private AccessibilityNodeInfo mHmoeNodeInfoWebView = null;

    private void findWebViewNode(AccessibilityNodeInfo rootView) {
        for (int i = 0; i < rootView.getChildCount(); i++) {
            AccessibilityNodeInfo child = rootView.getChild(i);
            showLog("child=" + child.toString());
            if ("android.widget.Button" == child.getClassName() && child.getText().toString().contains("我的喵币")) {
                mHmoeNodeInfoWebView = child;
                addToasMsg("找到我的喵币");
                return;
            }
            if (child.getChildCount() > 0) {
                findWebViewNode(child);
            }
        }
    }

    private boolean selectMaoMaoMuen() {
        boolean tf = false;
        addToasMsg("正在点击[领猫币]中..");
        AccessibilityNodeInfo rootView = AccessibilityHelper.findNodeInfosById("com.taobao.taobao:id/decor_content_parent");
        findWebViewNode(rootView);

        if (mHmoeNodeInfoWebView != null && mHmoeNodeInfoWebView.getChildCount() > 0) {
            mHmoeNodeInfoWebView.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            addToasMsg("点击[我的喵币]成功");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dispatchGestureClick(1080, 2159);
        }
        return tf;
    }

    /**
     * 开始倒计时的监听
     */
    private int mNumbers = 10;//10s的倒计时
    private boolean startISok = false;//定时器是否启动
    private boolean ischeck = false;//锁

    private void startTimer() {
        startISok = true;
        RxTimerUtil.interval(1000, new RxTimerUtil.IRxNext() {
            @SuppressLint("NewApi")
            @Override
            public void doNext(long number) {
                if (mrMode) {
                    return;
                }
                addToasMsg(mNumbers + "s后之后将开启祖安模式---请注意,可按返回键取消~");
                --mNumbers;
                if (mNumbers == 0) {
                    if (MainActivity.mode == 0) {
                        addToasMsg("祖安优雅模式已开启（" + (MainActivity.times / 1000) + "s一次)");
                    } else {
                        addToasMsg("祖安火力全开模式已开启（" + (MainActivity.times / 1000) + "s一次)");
                    }
                    stopTimer();
                    //开启骂人模式
                    startMRMode();
                }
            }
        });
    }

    /**
     * 停止监听
     */
    private void stopTimer() {
        RxTimerUtil.cancel();
        ischeck = false;
        startISok = false;
    }

    private int mrNumber = 0;
    private boolean mrMode = false;//默认模式是否开启

    @SuppressLint("NewApi")
    private void startMRMode() {
        mrMode = true;//已开启
        RxTimerUtil.interval(MainActivity.times, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                if (mrMode) {
                    mrNumber++;
                    addToasMsg("正在获取第" + mrNumber + "条祖安语录");

                }
            }
        });
    }

    /**
     * 停止骂人模式
     */
    private void stopMRTimer() {
        RxTimerUtil.cancel();
        mrMode = false;
        addToasMsg("一共怼了对方:" + mrNumber + "次!");
        mrNumber = 0;
        OkGo.getInstance().cancelTag(this);
    }

    /**
     * 立即发送移动的手势
     * 注意7.0以上的手机才有此方法，请确保运行在7.0手机上
     *
     * @param path  移动路径
     * @param mills 持续总时间
     */
    @RequiresApi(24)
    public void dispatchGestureMove(Path path, long mills) {
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                (path, 0, mills)).build(), null, null);
    }

    /**
     * 点击指定位置
     * 注意7.0以上的手机才有此方法，请确保运行在7.0手机上
     */
    @RequiresApi(24)
    public void dispatchGestureClick(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription
                (path, 0, 100)).build(), null, null);
    }

    /**
     * 即将关闭
     * 服务中断的时候 会回调此方法
     */
    @Override
    public void onInterrupt() {
        isStart = false;
    }

    /**
     * 启动回调
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        isStart = true;
        showLog("服务启动");
        addToasMsg("祖安语录服务启动成功");
        addToasMsg("--请进入微信,选择好友/群 详情界面--'");
        //配置监听内容 --->动态配置
        AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        serviceInfo.packageNames = new String[]{APP_PACKAGE, "com.android.systemui"};// 监控的app
        serviceInfo.notificationTimeout = 100;
        //设置可以监控webview
        serviceInfo.flags = serviceInfo.flags | AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;
        final AccessibilityServiceInfo info = getServiceInfo();
        //获取到webview的内容
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY;

        AccessibilityHelper.mService = this;
        setServiceInfo(serviceInfo);
    }

    /**
     * 销毁回调
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        isStart = false;
        showLog("服务关闭");
        closeXToast();
    }

    private void showLog(String log) {
        Log.e(TAG, log);
    }

    private XToast mXtoast;
    public static boolean isShow = false;

    public void intXToast() {
        if (mXtoast == null) {
            isShow = true;
            mXtoast = new XToast(Utils.getApp())
                    .setView(R.layout.toast)
                    .setGravity(Gravity.CENTER)
                    .setDuration(5000 * 1000)
                    .setAnimStyle(android.R.style.Animation_Translucent)
                    .show();
        }

    }

    public void closeXToast() {
        if (mXtoast != null) {
            mXtoast.cancel();
        }
    }

    private StringBuffer stringBuffer = new StringBuffer();

    private void addToasMsg(String msg) {
        if (mXtoast == null) {
            intXToast();
            stringBuffer.append("========已开启祖安模式========\n");
        }
        stringBuffer.append(msg);
        stringBuffer.append("\n");

        mXtoast.setText(R.id.tv_toast, stringBuffer.toString());
        ((ScrollView) mXtoast.findViewById(R.id.scrollView)).smoothScrollBy(0, 500);
    }
    //--------------------------------判断------------------------

    /**
     * 判断是否在集猫猫的首页
     *
     * @return
     */
    private int isshow = 0;

    public boolean isListHome() {
        if (isshow == 0) {
            addToasMsg("集猫猫自动浏览启动成功");
            isshow = 1;
        }
        return isViewExist("android:id/tabs");
    }

    private int isMaomaoshow = 0;

    public boolean isMaoMaotHome() {
        if (isMaomaoshow == 0) {
            addToasMsg("正在集猫猫首页，点击领喵币");
            isMaomaoshow = 1;
        }
        return isViewExist("com.taobao.taobao:id/decor_content_parent");
    }

    /**
     * 判断是否在聊天详情界面
     *
     * @return
     */
    public boolean isDetailsHome() {
        return isViewExist(IDKey.HOME_DETAILS_ID);
    }

    /**
     * 判断当前页面view在不在
     *
     * @param packageAndId
     * @return
     */
    public boolean isViewExist(String packageAndId) {
        AccessibilityNodeInfo nodeInfo = this.getRootInActiveWindow();
        if (nodeInfo == null) {
            return false;
        }
        AccessibilityNodeInfo targetNode = ServiceUtils.findNodeInfoByViewId(nodeInfo, packageAndId);
        nodeInfo.recycle();
        if (targetNode != null) {
            return true;
        }
        return false;
    }

    /**
     * 将内容复制到剪切板
     */
    public void setEtText(String s) {
        AccessibilityNodeInfo etinfos = AccessibilityHelper.findNodeInfosById(IDKey.ETXT_ID);
        if (etinfos != null) {
            AccessibilityHelper.performSetText(etinfos, s);
        } else {
            addToasMsg("获取文本框失败,请退出重试!");
            addToasMsg("祖安骂人模式已关闭");
            stopMRTimer();
        }
    }

    /**
     * 点击发送按钮
     */
    public void sendMsg() {
        AccessibilityNodeInfo send = AccessibilityHelper.findNodeInfosById(IDKey.SEND_MSG);
        if (send != null) {
            AccessibilityHelper.performClick(send);
        } else {
            addToasMsg("获取文本框失败,请退出重试!");
            addToasMsg("祖安骂人模式已关闭");
            stopMRTimer();
        }
    }
}
