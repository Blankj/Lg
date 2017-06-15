# Lg

## Introduce

该日志是对mars-xlg的封装，具有mars-xlog的高性能写入，多功能及美观的优点。

## How to use

```

```


## Functions

* 可设置Log开启和关闭
* 可设置是否输出到控制台(Logcat)
* 可设置Log全局Tag
* 全局Tag为空时Tag为当前类名
* 可设置Log是否显示头部信息
* Log头部含有当前线程名
* Log头部含有当前类及行号和函数名，支持点击跳转
* 可设置Log是否写入文件
* 可设置Log写入文件目录
* 可设置Log是否显示边框
* 可设置Log控制台过滤器
* 可设置Log文件过滤器
* 支持控制台长字符串的输出
* 支持多参数输出
* 支持单独写入文件
* 支持JSON串的输出
* 支持XML串的输出


* ### API of Lg

```
config           : 获取配置
setLogSwitch     : 设置log总开关
setConsoleSwitch : 设置log控制台开关
setGlobalTag     : 设置log全局tag
setLogHeadSwitch : 设置log头部信息开关
setLog2FileSwitch: 设置log文件开关
setDir           : 设置log文件存储目录
setBorderSwitch  : 设置log边框开关
setConsoleFilter : 设置log控制台过滤器
setFileFilter    : 设置log文件过滤器
setsFilePrefix   : 设置log文件前缀名
setIsSecondWrap  : 设置log是否二次封装
v                : Verbose日志
d                : Debug日志
i                : Info日志
w                : Warn日志
e                : Error日志
a                : Assert日志
file             : log到文件
json             : log字符串之json
xml              : log字符串之xml
flush            : 将缓冲输出到文件中
destroy          : 程序退出时需调用
```


## Proguard
```
#Xlog
-keep class com.tencent.mars.** { *; }
-keepclassmembers class com.tencent.mars.** { *; }
-dontwarn com.tencent.mars.**
```


## Introduce

LogUtils包含了我们开发人员大部分对日志的操作，因为在写入文件是即时写入，所以效率会很高，但是存在内存的波动，甚至会造成卡顿。

这一点xlog无疑是做得最好的，利用mmap对文件进行操作从而规避了这一问题，同时，它还支持压缩、加密日志以及对过时日志进行清理的功能（默认10天），所以毫无疑问地选择了它。

现在已经集成到了`Lg`中，采用`Lg`的命名考虑到开发人员更简短方便调用，我们只需要在程序入口处初始化即可，参看如下代码。

``` java
/**
 * 在程序退出时一定要调用{@link Lg#destroy()}来将缓冲输出到文件
 * <br>如果程序中要强行立马输出到文件，调用{@link Lg#flush()}即可
 */
public static void initLg() {
    Lg.config(appContext)
            .setLogSwitch(BuildConfig.DEBUG)// 设置log总开关，包括输出到控制台和文件，默认开
            .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
            .setGlobalTag(null)// 设置log全局标签，默认为空
            // 当全局标签不为空时，我们输出的log全部为该tag，
            // 为空时，如果传入的tag为空那就显示类名，否则显示tag
            .setLogHeadSwitch(true)// 设置log头信息开关，默认为开
            .setLog2FileSwitch(false)// 打印log时是否存到文件的开关，默认关
            .setDir("")// 当自定义路径为空时，写入应用的/cache/log/目录中
            .setBorderSwitch(true)// 输出日志是否带边框开关，默认开
            .setConsoleFilter(Lg.V)// log的控制台过滤器，和logcat过滤器同理，默认Verbose
            .setFileFilter(Lg.V)// log文件过滤器，和logcat过滤器同理，默认Verbose
            .setsFilePrefix("XCCommon")// 设置log文件前缀，默认为"ECARX"，格式为"prefix_date.xlog"，例如"ECARX_20170516.xlog"
            .setIsSecondWrap(true);// 设置log是否二次封装，默认为false，如果开发人员需要在自己封装的Log中调用的话，请设置为true
    Lg.d(Lg.getConfig());
}
```

如果想要了解更多，参看Demo之LgActivity。


## TODO

由于XLog的JNI层输出文件部分信息无用且格式较乱，导致体验不是很好，所以后续我会修改JNI部分来达到日志信息的有效性及可观性，默认生成的日志是经过压缩过的，所以还需要解压，请运行`/mars/mars/log/crypt/decode_mars_log_file.py`文件来解压日志文件
