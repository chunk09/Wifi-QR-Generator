package kr.dlrkdbs.wifiqrgenerator.func;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Map;

public class Define {
    // volatile 키워드 사용
    private static volatile Define singletonObject;

    public ArrayList<Map<String, String>> contacts;
    public Bitmap qr;

    private Define() {
        contacts = null;
    }

    public static Define getInstance() {
        if (singletonObject == null) {
            // if 문 진입 시에만 Singleton 클래스에 대한 동기화 작업 수행
            synchronized (Define.class) {
                if (singletonObject == null) {
                    singletonObject = new Define();
                }
            }
        }

        return singletonObject;
    }
}
