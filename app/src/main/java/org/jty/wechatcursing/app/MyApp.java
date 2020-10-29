package org.jty.wechatcursing.app;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import com.blankj.utilcode.util.PermissionUtils;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

/**
 * @Author: jiangtuanyuan
 * @Date: 2020/9/27 15:31
 * @DESC: 自动启动服务
 */
public class MyApp extends Application {
    /**
     * app包名
     */
    private String app_package = "org.jty.wechatcursing";
    /**
     * 辅助服务的绝对路径
     */
    private String Accessibility_class_path = "org.jty.wechatcursing.service.ZuAnService";

    @Override
    public void onCreate() {
        super.onCreate();
        iniAccessibilityService();
    }

    private void iniAccessibilityService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionUtils.isGrantedWriteSettings()) {
                //有了权限，具体的动作
                Settings.Secure.putString(getContentResolver(),
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        app_package + "/" + Accessibility_class_path);
                Settings.Secure.putInt(getContentResolver(),
                        Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            } else {
                //open app setting os ui
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}
