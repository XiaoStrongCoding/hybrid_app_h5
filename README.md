# hybrid_app_h5

# 使用
1. cd到web文件夹， npm i -g http-server, 后http-server运行服务，后copy ip对应的服务地址 到app文件夹下的MainActivity.java下，跟换webView.loadUrl地址
2. 用Android studio打开app文件夹

# 原理
1. 运用urlschema, 约定schema为jsBridge://{方法名}?{参数}
2. h5、app两端提供对外API

#框架
urlSchema --- JsBridge
API --- DsBridge
