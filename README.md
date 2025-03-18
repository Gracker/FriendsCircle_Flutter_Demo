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
输入: 原生视频/相机帧 / Input: Native video/camera frames
→ SurfaceTexture (Android)
→ 纹理注册表(ID映射) / Texture Registry (ID mapping)
→ Flutter引擎 / Flutter Engine
→ GPU渲染 / GPU Rendering
输出: 显示帧 / Output: Displayed frame

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

## 资源生成 / Resource Generation

应用使用代码生成所有资源，无需外部图片文件：
- 用户头像：随机生成的彩色头像
- 背景图片：基于负载类型生成的渐变背景
- 朋友圈图片：随机生成的颜色块和图案

## 开发者信息 / Developer Information

本项目为Flutter性能测试演示应用，专为Android平台性能和功耗测试设计。
