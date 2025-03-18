# 微信朋友圈性能测试Flutter演示

这是一个用于测试Android平台Performance和Power的Flutter Demo App，模拟了微信朋友圈的滑动场景，并提供了两种渲染模式（SurfaceView和TextureView），每种模式提供三种不同负载级别的测试界面。

# WeChat Moments Performance Testing Flutter Demo

This is a Flutter Demo App for testing Android platform Performance and Power, simulating the scrolling scenario of WeChat Moments and providing test interfaces with two rendering modes (SurfaceView and TextureView), each offering three different load levels.

## 开发环境要求 / Development Environment Requirements

- Flutter SDK: 3.10.0 或以上 (推荐使用 3.29.1)
- Dart SDK: 3.0.0 或以上
- Android Studio: 最新版本
- Xcode: 最新版本 (仅用于iOS开发)

## 项目特点 / Project Features

- **一致性测试环境 / Consistent Test Environment**：每次启动应用时，界面、UI和逻辑都保持一致，确保测试结果的可比性。
- **两种渲染模式 / Two Rendering Modes**：提供SurfaceView和TextureView两种渲染模式，可以比较不同渲染方式在相同负载下的性能表现。
- **三种负载级别 / Three Load Levels**：每种渲染模式下提供轻、中、重三种不同的负载级别，模拟不同使用场景。
- **精确控制 / Precise Control**：通过不同算法模拟不同级别的CPU和GPU负载，帮助您精确测试设备性能。
- **微信风格UI / WeChat Style UI**：UI设计参考微信朋友圈，提供真实的用户体验测试环境。

## 渲染模式比较 / Rendering Mode Comparison

### SurfaceView模式 / SurfaceView Mode
- 使用Flutter的标准渲染管线
- 适用于大多数场景
- 通常有更好的性能表现

### TextureView模式 / TextureView Mode
- 使用Android原生TextureView渲染
- Flutter通过TextureLayer从原生层采样
- 采用SurfaceTexture进行硬件加速渲染
- 在GPU内存中使用零复制纹理采样
- 适用于需要与其他UI元素混合的场景

## TextureView实现架构 / TextureView Architecture

### 架构组件 / Architecture Components
1. Flutter引擎层 / Flutter Engine Layer
2. 平台(Android)层 / Platform (Android) Layer 
3. 纹理注册系统 / Texture Registry System
4. GPU渲染管线 / GPU Rendering Pipeline

### 数据流 / Data Flow
输入: Flutter渲染帧 / Input: Flutter rendering frames
→ SurfaceTexture (Android)
→ 纹理注册表(ID映射) / Texture Registry (ID mapping)
→ TextureView (Native Android)
→ 屏幕显示 / Screen Display

### 详细架构说明 / Architecture Details
1. **Flutter层组件**:
   - `NativeTextureView`: Flutter小部件，包装原生TextureView
   - `TextureViewController`: 管理与原生层的通信
   - `Texture`小部件: 显示从原生层获取的纹理内容

2. **Native层组件**:
   - `TextureViewFactory`: 创建和管理原生TextureView实例
   - `FlutterTextureView`: 实现PlatformView接口，管理TextureView的生命周期
   - 纹理调试覆盖层: 提供实时性能监控信息

3. **通信机制**:
   - Method Channel: Flutter与Native层之间的双向通信
   - Texture Registry: 管理纹理ID和SurfaceTexture的映射
   - 事件回调: 处理纹理创建、更新和销毁事件

## 使用方法 / Usage

1. 从主界面选择渲染模式（SurfaceView或TextureView）
2. 选择负载级别（轻、中、重）进行测试
3. 观察滚动性能和电池消耗情况
4. 比较不同渲染模式和负载级别下的性能差异

## 主要功能 / Main Features

- 模拟微信朋友圈界面，包括头部视差效果
- 支持图片、文字、点赞和评论展示
- 随机生成用户数据，但保持一致性
- 在滚动时模拟不同级别的CPU/GPU负载
- 提供两种不同的渲染实现方式
- 原生TextureView支持，实现更真实的性能测试场景
- 调试信息覆盖层，实时显示渲染状态和性能数据

## 资源生成 / Resource Generation

应用使用代码生成所有资源，无需外部图片文件：
- 用户头像：随机生成的彩色头像
- 背景图片：基于负载类型生成的渐变背景
- 朋友圈图片：随机生成的颜色块和图案

## 开发者信息 / Developer Information

本项目为Flutter性能测试演示应用，专为Android平台性能和功耗测试设计。

## 最近更新 / Recent Updates

### 2024-12-12: TextureView渲染模式实现
- 添加原生TextureView支持，实现Native与Flutter集成渲染
- 新增TextureViewFactory类，实现PlatformView与Flutter的集成
- 创建NativeTextureView和TextureViewController组件，提供Flutter侧API
- 优化TextureView架构，将Flutter渲染内容与Android视图层分离
- 添加调试信息显示，便于监控性能和渲染状态
- 解决SurfaceTexture附加到多个上下文的问题
