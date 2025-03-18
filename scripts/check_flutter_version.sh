#!/bin/bash

REQUIRED_FLUTTER_VERSION="3.10.0"
CURRENT_FLUTTER_VERSION=$(flutter --version | grep -o "Flutter [0-9]\.[0-9]\+\.[0-9]\+" | cut -d ' ' -f 2)

if [ "$CURRENT_FLUTTER_VERSION" != "$REQUIRED_FLUTTER_VERSION" ]; then
  echo "错误: 项目需要Flutter $REQUIRED_FLUTTER_VERSION版本，但您当前使用的是 $CURRENT_FLUTTER_VERSION"
  echo "请运行以下命令切换Flutter版本:"
  echo "  flutter version $REQUIRED_FLUTTER_VERSION"
  exit 1
else
  echo "Flutter版本检查通过: $CURRENT_FLUTTER_VERSION"
fi 