import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/foundation.dart';

/// TextureView控制器，用于管理与原生TextureView的通信
class TextureViewController {
  /// 方法通道，用于调用原生方法
  MethodChannel? _channel;
  
  /// 纹理ID，由原生层生成
  int? _textureId;
  
  /// 获取纹理ID
  int? get textureId => _textureId;
  
  /// 是否已准备就绪
  bool _isReady = false;
  
  /// 是否已准备就绪
  bool get isReady => _isReady;
  
  /// 初始化方法通道
  void initMethodChannel(int viewId) {
    _channel = MethodChannel('com.androidperformance.wechat_friend_flutter_demo/texture_view_$viewId');
    _setupMethodCallHandler();
    debugPrint('TextureViewController: 初始化方法通道 viewId=$viewId');
  }
  
  /// 设置方法调用处理器
  void _setupMethodCallHandler() {
    _channel?.setMethodCallHandler((call) async {
      switch (call.method) {
        case 'textureReady':
          // 原生层已经创建了新的Texture
          _textureId = call.arguments as int;
          _isReady = true;
          debugPrint('TextureViewController: 纹理已就绪，ID=$_textureId');
          break;
        case 'surfaceTextureReady':
          // 原生的SurfaceTexture已准备就绪
          _isReady = true;
          debugPrint('TextureViewController: 原生SurfaceTexture已就绪');
          break;
        case 'textureSizeChanged':
          // 纹理大小已改变
          debugPrint('TextureViewController: 纹理大小已改变');
          break;
        case 'textureFrameAvailable':
          // 新的帧可用
          // 此处不打印日志，避免日志过多
          break;
      }
      return null;
    });
  }
  
  /// 请求更新纹理
  Future<bool> updateTexture() async {
    if (_channel == null) {
      debugPrint('TextureViewController: 无法更新纹理 - 通道未初始化');
      return false;
    }
    
    if (!_isReady) {
      return false;
    }
    
    try {
      final result = await _channel!.invokeMethod<bool>('updateTexture');
      return result ?? false;
    } on PlatformException catch (e) {
      if (e.code == 'TEXTURE_ERROR') {
        debugPrint('TextureViewController: 纹理更新错误: ${e.message}');
      } else {
        debugPrint('TextureViewController: 更新纹理平台异常: ${e.code} - ${e.message}');
      }
      return false;
    } catch (e) {
      debugPrint('TextureViewController: 更新纹理一般异常: $e');
      return false;
    }
  }
  
  /// 获取纹理ID（如果尚未获取）
  Future<int?> getTextureId() async {
    if (_textureId != null) {
      return _textureId;
    }
    
    if (_channel == null) {
      debugPrint('TextureViewController: 无法获取纹理ID - 通道未初始化');
      return null;
    }
    
    try {
      final id = await _channel!.invokeMethod<int>('getTextureId');
      _textureId = id;
      debugPrint('TextureViewController: 获取到纹理ID=$id');
      return id;
    } catch (e) {
      debugPrint('TextureViewController: 获取纹理ID失败: $e');
      return null;
    }
  }
  
  /// 释放资源
  void dispose() {
    debugPrint('TextureViewController: 释放资源');
    _channel?.setMethodCallHandler(null);
    _channel = null;
    _textureId = null;
    _isReady = false;
  }
} 