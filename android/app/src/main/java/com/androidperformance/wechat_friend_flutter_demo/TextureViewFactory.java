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
 * 管理TextureView的生命周期和与Flutter的通信
 * 
 * 正确的架构:
 * 1. Flutter引擎生成内容并绘制到Surface
 * 2. 我们创建一个TextureView来显示自定义内容
 * 3. TextureView独立于Flutter的渲染流程，但我们在Flutter通知渲染完成时更新UI
 */
class FlutterTextureView implements PlatformView, MethodCallHandler, TextureView.SurfaceTextureListener {
    private static final String TAG = "FlutterTextureView";
    private final Context context;
    private final int viewId;
    private final MethodChannel methodChannel;
    private final int loadType;
    
    // Flutter纹理相关
    private final TextureRegistry.SurfaceTextureEntry textureEntry;
    private final SurfaceTexture flutterSurfaceTexture;
    private final Long textureId;
    private Surface flutterSurface;
    
    // 原生Android视图
    private final FrameLayout rootView;
    private final TextureView textureView;
    
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
        
        Log.d(TAG, "创建FlutterTextureView: viewId=" + viewId + ", loadType=" + loadType);
        
        // 创建方法通道，用于与Flutter通信
        methodChannel = new MethodChannel(messenger, "com.androidperformance.wechat_friend_flutter_demo/texture_view_" + viewId);
        methodChannel.setMethodCallHandler(this);
        
        // 获取主线程Handler
        mainHandler = new Handler(Looper.getMainLooper());
        
        // 从Flutter引擎获取SurfaceTexture入口
        textureEntry = textureRegistry.createSurfaceTexture();
        textureId = textureEntry.id();
        flutterSurfaceTexture = textureEntry.surfaceTexture();
        
        // 设置初始大小
        flutterSurfaceTexture.setDefaultBufferSize(1, 1);
        
        // 创建Surface，Flutter将渲染到这个Surface
        flutterSurface = new Surface(flutterSurfaceTexture);
        
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
        
        Log.d(TAG, "已创建FlutterTextureView和TextureView, textureId=" + textureId);
        
        // 通知Flutter已创建纹理
        mainHandler.post(() -> {
            methodChannel.invokeMethod("textureReady", textureId);
        });
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
            // 释放Flutter相关资源
            if (flutterSurface != null) {
                flutterSurface.release();
                flutterSurface = null;
            }
            
            textureEntry.release();
            
            // 移除调试覆盖层
            if (debugOverlay != null && rootView != null) {
                rootView.removeView(debugOverlay);
                debugOverlay = null;
            }
            
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
                Log.d(TAG, "getTextureId: 返回textureId=" + textureId);
                result.success(textureId);
                break;
                
            case "updateTexture":
                // 确保TextureView可用，且Flutter的SurfaceTexture已准备就绪
                if (isReady && !isDestroyed.get()) {
                    try {
                        // Flutter引擎已经在flutterSurfaceTexture上渲染了内容
                        // TextureView展示自己的SurfaceTexture内容，与Flutter的渲染解耦
                        // 但我们需要通知Flutter帧已更新，以保持同步
                        
                        result.success(true);
                        
                        // 通知Flutter帧已更新
                        mainHandler.post(() -> {
                            methodChannel.invokeMethod("textureFrameAvailable", null);
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "updateTexture失败", e);
                        result.error("TEXTURE_ERROR", e.getMessage(), null);
                    }
                } else {
                    Log.d(TAG, "updateTexture: 纹理未就绪或已销毁");
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
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        Log.d(TAG, "onSurfaceTextureAvailable: width=" + width + ", height=" + height);
        
        try {
            // 设置Flutter SurfaceTexture的大小
            flutterSurfaceTexture.setDefaultBufferSize(width, height);
            
            // 添加调试覆盖层
            if (showDebugInfo) {
                addDebugOverlay();
            }
            
            isReady = true;
            
            // 通知Flutter原生TextureView已准备就绪
            mainHandler.post(() -> {
                methodChannel.invokeMethod("surfaceTextureReady", null);
            });
        } catch (Exception e) {
            Log.e(TAG, "设置SurfaceTexture失败", e);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        Log.d(TAG, "onSurfaceTextureDestroyed");
        isReady = false;
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: width=" + width + ", height=" + height);
        
        try {
            // 更新Flutter SurfaceTexture的大小
            flutterSurfaceTexture.setDefaultBufferSize(width, height);
            
            // 通知Flutter纹理大小已改变
            mainHandler.post(() -> {
                methodChannel.invokeMethod("textureSizeChanged", null);
            });
        } catch (Exception e) {
            Log.e(TAG, "更新SurfaceTexture大小失败", e);
        }
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
        // 当TextureView内容更新时触发
        // 由于内容由Flutter控制，此处不需要特殊处理
    }
} 