package com.androidperformance.wechat_friend_flutter_demo;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;

public class MainActivity extends FlutterActivity {
    
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        
        // 注册TextureViewFactory，处理Flutter到Android的TextureView渲染
        flutterEngine
            .getPlatformViewsController()
            .getRegistry()
            .registerViewFactory(
                "com.androidperformance.wechat_friend_flutter_demo/texture_view", 
                new TextureViewFactory(flutterEngine.getDartExecutor().getBinaryMessenger(), flutterEngine.getRenderer())
            );
    }
}
