package com.androidperformance.wechat_friend_flutter_demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.widget.FrameLayout;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.StandardMessageCodec;
import io.flutter.plugin.platform.PlatformView;
import io.flutter.plugin.platform.PlatformViewFactory;
import io.flutter.view.TextureRegistry;

/**
 * Android原生TextureView工厂类
 * 用于创建和管理TextureView实例，并与Flutter部分通信
 */
public class TextureViewFactory extends PlatformViewFactory {
    private static final String TAG = "TextureViewFactory";
    private final BinaryMessenger messenger;
    private final TextureRegistry textureRegistry;

    public TextureViewFactory(BinaryMessenger messenger, TextureRegistry textureRegistry) {
        super(StandardMessageCodec.INSTANCE);
        this.messenger = messenger;
        this.textureRegistry = textureRegistry;
        Log.d(TAG, "创建TextureViewFactory实例");
    }

    @Override
    public PlatformView create(Context context, int viewId, @Nullable Object args) {
        Map<String, Object> params = (Map<String, Object>) args;
        int loadType = (int) params.get("loadType");
        Log.d(TAG, "创建PlatformView: viewId=" + viewId + ", loadType=" + loadType);
        return new FlutterTextureView(context, messenger, viewId, loadType, textureRegistry);
    }
}

/**
 * Flutter平台视图的TextureView实现
 * 真正使用TextureView作为渲染内容载体，内容由Flutter引擎提供
 */
class FlutterTextureView implements PlatformView, MethodCallHandler, TextureView.SurfaceTextureListener {
    private static final String TAG = "FlutterTextureView";
    private final Context context;
    private final int viewId;
    private final MethodChannel methodChannel;
    private final int loadType;
    private final TextureRegistry textureRegistry;
    
    // 原生Android视图
    private final FrameLayout rootView;
    private final TextureView textureView;
    
    // 纹理相关
    private SurfaceTexture surfaceTexture;
    private Surface surface;
    private TextureRegistry.SurfaceTextureEntry textureEntry;
    private long flutterTextureId = -1;
    
    // 调试信息
    private final Paint debugPaint;
    private boolean showDebugInfo = true;
    private View debugOverlay;
    
    // 线程控制
    private final Handler mainHandler;
    private boolean isReady = false;
    private final AtomicBoolean isDestroyed = new AtomicBoolean(false);

    FlutterTextureView(Context context, BinaryMessenger messenger, int viewId, int loadType, TextureRegistry textureRegistry) {
        this.context = context;
        this.viewId = viewId;
        this.loadType = loadType;
        this.textureRegistry = textureRegistry;
        
        Log.d(TAG, "创建FlutterTextureView: viewId=" + viewId + ", loadType=" + loadType);
        
        // 创建方法通道，用于与Flutter通信
        methodChannel = new MethodChannel(messenger, "com.androidperformance.wechat_friend_flutter_demo/texture_view_" + viewId);
        methodChannel.setMethodCallHandler(this);
        
        // 获取主线程Handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 初始化调试画笔
        debugPaint = new Paint();
        debugPaint.setColor(Color.RED);
        debugPaint.setTextSize(30);
        debugPaint.setAntiAlias(true);
        
        // 创建根视图布局
        rootView = new FrameLayout(context);
        
        // 创建TextureView
        textureView = new TextureView(context);
        
        // 设置SurfaceTextureListener以监听TextureView的生命周期
        textureView.setSurfaceTextureListener(this);
        
        // 添加TextureView到根视图
        rootView.addView(textureView, 
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.MATCH_PARENT));
        
        Log.d(TAG, "已创建FlutterTextureView和TextureView");
    }

    /**
     * 添加调试信息覆盖层
     */
    private void addDebugOverlay() {
        if (!showDebugInfo || rootView == null) {
            return;
        }
        
        // 检查是否已存在调试视图
        if (debugOverlay != null) {
            return;
        }
        
        // 创建一个透明视图用于显示调试信息
        debugOverlay = new View(context) {
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                
                // 绘制调试信息
                String info = "TextureView ID: " + viewId + 
                             " TextureID: " + flutterTextureId +
                             " Load: " + loadType + 
                             " Size: " + getWidth() + "x" + getHeight();
                canvas.drawText(info, 20, 60, debugPaint);
                
                // 定期刷新
                postInvalidateDelayed(1000);
            }
        };
        
        // 设置为透明背景
        debugOverlay.setBackgroundColor(Color.TRANSPARENT);
        
        // 使用与TextureView相同的布局参数
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        
        // 添加到根容器
        rootView.addView(debugOverlay, params);
        
        // 触发首次绘制
        debugOverlay.invalidate();
        
        Log.d(TAG, "已添加调试信息覆盖层");
    }

    @Override
    public View getView() {
        return rootView;
    }

    @Override
    public void dispose() {
        Log.d(TAG, "dispose: 清理资源");
        isDestroyed.set(true);
        
        mainHandler.post(() -> {
            // 移除调试覆盖层
            if (debugOverlay != null && rootView != null) {
                rootView.removeView(debugOverlay);
                debugOverlay = null;
            }
            
            // 释放Flutter纹理资源
            if (textureEntry != null) {
                textureEntry.release();
                textureEntry = null;
            }
            
            // 释放Surface
            if (surface != null) {
                surface.release();
                surface = null;
            }
            
            // 不要释放SurfaceTexture，它会随TextureView自动释放
            
            methodChannel.setMethodCallHandler(null);
            Log.d(TAG, "资源已清理完成");
        });
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (isDestroyed.get()) {
            result.error("DISPOSED", "TextureView已被销毁", null);
            return;
        }
        
        switch (call.method) {
            case "getTextureId":
                Log.d(TAG, "getTextureId: 返回" + flutterTextureId);
                result.success(flutterTextureId);
                break;
                
            case "updateTexture":
                // 请求更新纹理，如果需要进行额外操作可以在这里处理
                if (isReady && !isDestroyed.get()) {
                    if (surface != null) {
                        // 这里可以添加额外渲染逻辑，如果需要在Native层绘制额外内容
                        // Canvas canvas = surface.lockCanvas(null);
                        // ... 渲染操作 ...
                        // surface.unlockCanvasAndPost(canvas);
                    }
                    result.success(true);
                } else {
                    Log.d(TAG, "updateTexture: TextureView未就绪或已销毁");
                    result.success(false);
                }
                break;
                
            default:
                result.notImplemented();
                break;
        }
    }

    // TextureView.SurfaceTextureListener实现
    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: width=" + width + ", height=" + height);
        
        try {
            // 保存TextureView的SurfaceTexture
            this.surfaceTexture = surface;
            
            // 将SurfaceTexture注册到Flutter引擎
            textureEntry = textureRegistry.createSurfaceTexture();
            flutterTextureId = textureEntry.id();
            
            // 获取Flutter分配的SurfaceTexture，并将其设置给TextureView
            SurfaceTexture flutterSurfaceTexture = textureEntry.surfaceTexture();
            flutterSurfaceTexture.setDefaultBufferSize(width, height);
            
            // 创建Surface用于渲染
            this.surface = new Surface(flutterSurfaceTexture);
            
            // 添加调试覆盖层
            if (showDebugInfo) {
                addDebugOverlay();
            }
            
            isReady = true;
            
            // 通知Flutter纹理已准备就绪，并传递纹理ID
            mainHandler.post(() -> {
                methodChannel.invokeMethod("textureReady", flutterTextureId);
            });
            
            Log.d(TAG, "TextureView准备完成，Flutter纹理ID: " + flutterTextureId);
        } catch (Exception e) {
            Log.e(TAG, "设置SurfaceTexture失败", e);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        if (!isDestroyed.get()) {
            // 自动标记为已销毁
            isDestroyed.set(true);
        }
        return true; // 返回true让TextureView自行处理清理工作
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: width=" + width + ", height=" + height);
        if (textureEntry != null) {
            try {
                // 更新Flutter纹理的大小
                SurfaceTexture flutterSurfaceTexture = textureEntry.surfaceTexture();
                flutterSurfaceTexture.setDefaultBufferSize(width, height);
                
                // 通知Flutter纹理大小改变
                mainHandler.post(() -> {
                    methodChannel.invokeMethod("textureSizeChanged", 
                            new int[]{width, height});
                });
            } catch (Exception e) {
                Log.e(TAG, "更新纹理大小失败", e);
            }
        }
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        // 这个回调会非常频繁，不要在这里打印日志
        if (!isDestroyed.get()) {
            // 通知Flutter新的帧可用
            mainHandler.post(() -> {
                // 不使用invokeMethod，减少消息传递开销
                // methodChannel.invokeMethod("textureFrameAvailable", null);
            });
        }
    }
} 