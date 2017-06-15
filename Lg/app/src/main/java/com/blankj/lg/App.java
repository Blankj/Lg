package com.blankj.lg;

import android.app.Application;

import com.blankj.log.Lg;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2017/06/15
 *     desc  :
 * </pre>
 */
public class App extends Application {

    private static App appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;

        initLg();
    }

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
                .setIsSecondWrap(false);// 设置log是否二次封装，默认为false，如果开发人员需要在自己封装的Log中调用的话，请设置为true
        Lg.d(Lg.getConfig());
    }
}
