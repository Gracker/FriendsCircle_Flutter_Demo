import 'package:flutter/material.dart';
import '../utils/asset_generator.dart';

/// 朋友圈九宫格图片组件 - 模仿微信朋友圈的九宫格布局
class PostImageGrid extends StatelessWidget {
  /// 图片URL列表
  final List<String>? imageUrls;
  
  /// 帖子ID (用于生成模拟图片)
  final String postId;
  
  /// 图片间距
  final double spacing;
  
  /// 构造函数
  const PostImageGrid({
    Key? key,
    this.imageUrls,
    required this.postId,
    this.spacing = 6.0,
  }) : super(key: key);
  
  @override
  Widget build(BuildContext context) {
    // 如果没有图片，返回空组件
    if (imageUrls == null || imageUrls!.isEmpty) {
      return const SizedBox.shrink();
    }
    
    // 确保图片URL列表不超过9张
    final List<String> displayUrls = imageUrls!.length > 9 
      ? imageUrls!.sublist(0, 9) 
      : imageUrls!;
    
    // 计算父容器可用宽度
    final double availableWidth = MediaQuery.of(context).size.width - 80; // 减去头像和边距
    
    // 确定网格布局参数
    final GridParameters params = _calculateGridParameters(displayUrls.length, availableWidth);
    
    // 单个图片的展示处理
    if (displayUrls.length == 1) {
      return _buildSingleImage(context, params);
    }
    
    // 构建九宫格
    return Container(
      width: params.gridWidth,
      height: params.gridHeight,
      child: _buildGrid(displayUrls, params),
    );
  }
  
  /// 计算网格布局参数
  GridParameters _calculateGridParameters(int count, double availableWidth) {
    int columns;
    int rows;
    
    // 确定列数
    if (count <= 1) {
      columns = 1;
    } else if (count <= 4) {
      columns = 2;
    } else {
      columns = 3;
    }
    
    // 确定行数 
    rows = (count / columns).ceil();
    
    // 计算每个图片的尺寸和网格总尺寸
    final double itemWidth = (availableWidth - (columns - 1) * spacing) / columns;
    final double itemHeight = itemWidth; // 保持正方形
    
    final double gridWidth = columns * itemWidth + (columns - 1) * spacing;
    final double gridHeight = rows * itemHeight + (rows - 1) * spacing;
    
    return GridParameters(
      columns: columns,
      rows: rows,
      itemWidth: itemWidth,
      itemHeight: itemHeight,
      gridWidth: gridWidth,
      gridHeight: gridHeight,
    );
  }
  
  /// 构建单张图片视图
  Widget _buildSingleImage(BuildContext context, GridParameters params) {
    // 单张图片的特殊处理
    final double maxWidth = params.itemWidth * 2 + spacing; // 最大宽度为两个格子
    final double maxHeight = params.itemHeight * 2; // 最大高度为两个格子
    
    return ClipRRect(
      borderRadius: BorderRadius.circular(4.0),
      child: Container(
        constraints: BoxConstraints(
          maxWidth: maxWidth,
          maxHeight: maxHeight,
        ),
        child: AssetGenerator().generateImageWidget(
          "$postId-0", 
          width: maxWidth,
          height: params.itemHeight * 1.3, // 高宽比约为1.3:1
        ),
      ),
    );
  }
  
  /// 构建网格布局
  Widget _buildGrid(List<String> urls, GridParameters params) {
    return GridView.builder(
      shrinkWrap: true,
      physics: NeverScrollableScrollPhysics(),
      gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: params.columns,
        mainAxisSpacing: spacing,
        crossAxisSpacing: spacing,
        childAspectRatio: 1.0, // 正方形
      ),
      itemCount: urls.length,
      itemBuilder: (context, index) {
        return _buildGridItem(
          index,
          params.itemWidth,
          params.itemHeight,
        );
      },
    );
  }
  
  /// 构建单个网格项
  Widget _buildGridItem(int index, double width, double height) {
    String imageId = "$postId-$index"; // 确保每个图片ID唯一且可重复
    
    return ClipRRect(
      borderRadius: BorderRadius.circular(4.0),
      child: AssetGenerator().generateImageWidget(
        imageId, 
        width: width,
        height: height,
      ),
    );
  }
}

/// 网格布局参数类
class GridParameters {
  final int columns;
  final int rows;
  final double itemWidth;
  final double itemHeight;
  final double gridWidth;
  final double gridHeight;
  
  GridParameters({
    required this.columns,
    required this.rows,
    required this.itemWidth,
    required this.itemHeight,
    required this.gridWidth,
    required this.gridHeight,
  });
} 