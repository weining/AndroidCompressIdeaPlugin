## Brief Plugin

此项目是百度知道安卓NA团队为了压缩项目图片体积减少包大小所制作的idea plugin, Android studio也可以使用。

此项目引用了开源项目libimagequant

之后有机会有可能增加更多功能。

## Getting started
如果有插件开发环境，直接打开项目即可。如果需要直接使用，直接安装本地插件Brief Plugin.zip，然后再拷贝libimagequant.jnilib文件到本机。（Windows同学可以自行生成libimagequant.dll文件，[地址](https://github.com/ImageOptim/libimagequant)）

## 如何使用
安装后会在Help之后多出一栏Brief Plugin选项，选择Compress Png Image，第一次会需要你选择jnilib文件的位置，然后选择需要压缩的图片路径即可。现在是会递归遍历文件夹，找出其中所有的src/main/res下的.png文件进行处理，处理后会直接覆盖原路径图片，压缩率因图片不同而不同，拿知道安卓端举例，1400+张图片压缩后整个压缩包减小了3M+

有不同需求的同学可以直接用源码修改，源码非常简单。

## Support
直接Hi 知识搜索部 薛维宁

