package org.jty.wechatcursing;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import org.jty.wechatcursing.service.ZuAnService;

import java.util.List;

/**
 * 主界面
 */
@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv_wza_status;
    private TextView tv_yf_status;
    private Button bt_wza_per;
    private Button bt_yf_per;
    private Button bt_start_app;

    private RadioGroup rgMode;
    private RadioGroup timeMode;

    public static int mode = 0;
    public static long times = 2000;

    private int isYF = 0;
    private int isWZA = 0;
    //保存屏幕的高度和宽度
    public static int width = 0, height = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initViewClick();
        setData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    private void initView() {
        tv_wza_status = findViewById(R.id.tv_wza_status);
        tv_yf_status = findViewById(R.id.tv_yf_status);

        bt_wza_per = findViewById(R.id.bt_wza_per);
        bt_yf_per = findViewById(R.id.bt_yf_per);
        bt_start_app = findViewById(R.id.bt_start_app);

        rgMode = findViewById(R.id.rg_mode);
        timeMode = findViewById(R.id.rg_time);
    }

    private void initViewClick() {
        bt_wza_per.setOnClickListener(this);
        bt_yf_per.setOnClickListener(this);
        bt_start_app.setOnClickListener(this);

        rgMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_mode1://模式1
                        mode = 0;
                        break;
                    case R.id.rb_mode2://模式2
                        mode = 1;
                        break;
                    default:
                        break;
                }
            }
        });
        timeMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_time2://2s
                        times = 2000;
                        break;
                    case R.id.rb_time4://4s
                        times = 4000;
                        break;
                    case R.id.rb_time8://8s
                        times = 8000;
                        break;
                    case R.id.rb_time10://10s
                        times = 10000;
                        break;
                    default:
                        break;
                }
            }
        });
    }


    private void setData() {
        height = ScreenUtils.getScreenHeight();
        width = ScreenUtils.getScreenWidth();

        tv_wza_status.setText("权限状态：未开启");
        tv_yf_status.setText("权限状态：未开启");
    }

    private void checkPermissions() {
        //1.无障碍权限
        if (ZuAnService.isStart) {
            isWZA = 1;
            tv_wza_status.setText("已开启");
            tv_wza_status.setTextColor(getResources().getColor(R.color.green));

            bt_wza_per.setText("已开启无障碍服务");
            bt_wza_per.setEnabled(false);
            bt_wza_per.setBackgroundColor(getResources().getColor(R.color.gray));
        } else {
            isWZA = 0;
            tv_wza_status.setText("未开启");
            tv_wza_status.setTextColor(getResources().getColor(R.color.gray_d2d3d5));

            bt_wza_per.setText("开启无障碍服务");
            bt_wza_per.setEnabled(true);
            bt_wza_per.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        //2.悬浮窗权限
        if (Settings.canDrawOverlays(this)) {
            isYF = 1;
            tv_yf_status.setText("已开启");
            tv_yf_status.setTextColor(getResources().getColor(R.color.green));

            bt_yf_per.setText("已开启悬浮窗权限");
            bt_yf_per.setEnabled(false);
            bt_yf_per.setBackgroundColor(getResources().getColor(R.color.gray));
        } else {
            isYF = 0;
            tv_yf_status.setText("未开启");
            tv_yf_status.setTextColor(getResources().getColor(R.color.gray_d2d3d5));

            bt_yf_per.setText("开启悬浮窗权限");
            bt_yf_per.setEnabled(true);
            bt_yf_per.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        //3.启动科普中国
        if (isYF == 1 && isWZA == 1) {
            bt_start_app.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            bt_start_app.setBackgroundColor(getResources().getColor(R.color.gray_d2d3d5));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_wza_per:
                wzaPermissions();
                break;
            case R.id.bt_yf_per:
                yfPermissions();
                break;
            case R.id.bt_start_app:
                if (isYF == 0) {
                    ToastUtils.showShort("未开启悬浮窗权限设置!");
                    return;
                }
                if (isWZA == 0) {
                    ToastUtils.showShort("未开启无障碍服务设置!");
                    return;
                }
                startAPP();
                break;
            default:
                break;
        }
    }

    private void startAPP() {
        PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(ZuAnService.APP_PACKAGE);
        if (intent == null) {
            ToastUtils.showShort("应用未安装");
        } else {
            startActivity(intent);
            moveTaskToBack(true);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 启动无障碍设置
     */
    private void wzaPermissions() {
        try {
            this.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } catch (Exception e) {
            this.startActivity(new Intent(Settings.ACTION_SETTINGS));
            e.printStackTrace();
        }
    }

    /**
     * 开启悬浮窗设置
     */
    private void yfPermissions() {
        XXPermissions.with(this)
                // 可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .constantRequest()
                // 支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.SYSTEM_ALERT_WINDOW)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean isAll) {
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                    }
                });
    }

}