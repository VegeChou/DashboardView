# 自定义View-表盘面板

<img src="http://vegechou.github.io/img/dashboard-view.png" width = "216" height = "384" alt="DashboardView" align=center />

### 介绍

表盘面板，可以用于展示某项指标信息的百分比

可以自定义表盘、指针颜色，动画时间，背景色

### 使用方法

在 Layout 中引用 

```
<com.android.ui.DashboardView>
```

### 已知 BUG

Layout 文件中设置这些属性目前无法生效，需要在代码中 `findViewById` 后通过代码设置

### 样例参考

SENSORO [配置工具](http://www.sensoro.com/zh/developer#config)

### 说明

Demo 中附源码和详细注释


