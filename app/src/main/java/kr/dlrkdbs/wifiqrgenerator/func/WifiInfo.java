package kr.dlrkdbs.wifiqrgenerator.func;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Map;

public class WifiInfo {
    private Context context;
    private WifiManager wifiManager;
    private android.net.wifi.WifiInfo wifiInfo;

    public WifiInfo(Context context) {
        this.context = context;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
    }

    public String getSSID() {
        String ssid = null;

        if (wifiInfo.getNetworkId() != -1) {
            ssid = wifiInfo.getSSID();
            // 가져온 SSID가 큰따옴표("")로 둘러싸여 있을 수 있으므로 제거
            ssid = ssid.replace("\"", "");
        }

        return ssid;
    }

    public String getPassword() {
        String password = null;
        if (wifiInfo.getNetworkId() != -1) { // WiFi에 연결된 경우에만 비밀번호 가져오기
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("Wifi", "onCreate: Wifi 권한 오류");
                return null;
            }
            List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
            System.out.println("configuredNetworks size : " + configuredNetworks.size());
            if (configuredNetworks != null) {
                System.out.println("널 아님 !");
                for (WifiConfiguration config : configuredNetworks) {
                    System.out.println("config: " + config);
                    if (config.networkId == wifiInfo.getNetworkId()) {
                        password = config.preSharedKey;

                        System.out.println("preSharedKey: " + config.preSharedKey);
                        break;
                    }
                }
            }
        }

        return password;
    }
}
