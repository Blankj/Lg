package com.blankj.log;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.IntDef;

import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Formatter;
import java.util.concurrent.ExecutorService;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * <pre>
 *     author: Blankj
 *     blog  : http://blankj.com
 *     time  : 2017/06/15
 *     desc  : XLog相关工具类
 * </pre>
 */
public final class Lg {

    public static final int V = Log.LEVEL_VERBOSE;
    public static final int D = Log.LEVEL_DEBUG;
    public static final int I = Log.LEVEL_INFO;
    public static final int W = Log.LEVEL_WARNING;
    public static final int E = Log.LEVEL_ERROR;
    public static final int A = Log.LEVEL_FATAL;

    @IntDef({V, D, I, W, E})
    @Retention(RetentionPolicy.SOURCE)
    private @interface TYPE {
    }

    private static final char[] T = new char[]{'V', 'D', 'I', 'W', 'E', 'A'};

    private static final int FILE = 0x10;
    private static final int JSON = 0x20;
    private static final int XML  = 0x30;
    private static ExecutorService executor;
    private static String          defaultDir;// log默认存储目录
    private static String          dir;       // log存储目录

    private static boolean sLogSwitch         = true;   // log总开关，默认开
    private static boolean sLog2ConsoleSwitch = true;   // logcat是否打印，默认打印
    private static String  sGlobalTag         = null;   // log标签
    private static boolean sTagIsSpace        = true;   // log标签是否为空白
    private static boolean sLogHeadSwitch     = true;   // log头部开关，默认开
    private static boolean sLog2FileSwitch    = false;  // log写入文件开关，默认关
    private static boolean sLogBorderSwitch   = true;   // log边框开关，默认开
    private static int     sConsoleFilter     = V;      // log控制台过滤器
    private static int     sFileFilter        = V;      // log文件过滤器
    private static String  sFilePrefix        = "ECARX";// log文件前缀
    private static boolean isSecondWrap       = false;  // log是否二次封装

    private static final String FILE_SEP      = System.getProperty("file.separator");
    private static final String LINE_SEP      = System.getProperty("line.separator");
    private static final String TOP_BORDER    = "╔═══════════════════════════════════════════════════════════════════════════════════════════════════";
    private static final String LEFT_BORDER   = "║ ";
    private static final String BOTTOM_BORDER = "╚═══════════════════════════════════════════════════════════════════════════════════════════════════";
    private static final int    MAX_LEN       = 4000;

    private static final String NULL_TIPS = "Log with null object.";
    private static final String NULL      = "null";
    private static final String ARGS      = "args";

    private volatile static Lg sLg;

    private Lg(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                && context.getExternalCacheDir() != null)
            defaultDir = context.getExternalCacheDir() + FILE_SEP + "log" + FILE_SEP;
        else {
            defaultDir = context.getCacheDir() + FILE_SEP + "log" + FILE_SEP;
        }

        // https://github.com/Tencent/mars/wiki/Mars-Android-%E6%8E%A5%E5%8F%A3%E8%AF%A6%E7%BB%86%E8%AF%B4%E6%98%8E
        System.loadLibrary("stlport_shared");
        System.loadLibrary("marsxlog");
        Xlog.setConsoleLogOpen(false);
        Log.setLogImp(new Xlog());
    }

    public static Lg config(Context context) {
        if (sLg == null) {
            synchronized (Lg.class) {
                if (sLg == null) {
                    sLg = new Lg(context);
                }
            }
        }
        return sLg;
    }

    public Lg setLogSwitch(boolean logSwitch) {
        Lg.sLogSwitch = logSwitch;
        return this;
    }

    public Lg setConsoleSwitch(boolean consoleSwitch) {
        Lg.sLog2ConsoleSwitch = consoleSwitch;
        return this;
    }

    public Lg setGlobalTag(final String tag) {
        if (isSpace(tag)) {
            Lg.sGlobalTag = "";
            sTagIsSpace = true;
        } else {
            Lg.sGlobalTag = tag;
            sTagIsSpace = false;
        }
        return this;
    }

    public Lg setLogHeadSwitch(boolean logHeadSwitch) {
        Lg.sLogHeadSwitch = logHeadSwitch;
        return this;
    }

    public Lg setLog2FileSwitch(boolean log2FileSwitch) {
        Lg.sLog2FileSwitch = log2FileSwitch;
        return this;
    }

    public Lg setDir(final String dir) {
        if (isSpace(dir)) {
            Lg.dir = null;
        } else {
            Lg.dir = dir.endsWith(FILE_SEP) ? dir : dir + FILE_SEP;
        }
        Log.appenderClose();
        Xlog.appenderOpen(sFileFilter,
                Xlog.AppednerModeAsync,
                Lg.dir,
                Lg.dir == null ? defaultDir : Lg.dir,
                sFilePrefix);
        return this;
    }

    public Lg setDir(final File dir) {
        Lg.dir = dir == null ? null : dir.getAbsolutePath() + FILE_SEP;
        return this;
    }

    public Lg setBorderSwitch(boolean borderSwitch) {
        Lg.sLogBorderSwitch = borderSwitch;
        return this;
    }

    public Lg setConsoleFilter(@TYPE int consoleFilter) {
        Lg.sConsoleFilter = consoleFilter;
        return this;
    }

    public Lg setFileFilter(@TYPE int fileFilter) {
        Lg.sFileFilter = fileFilter;
        return this;
    }

    public Lg setsFilePrefix(String filePrefix) {
        if (!isSpace(filePrefix)) Lg.sFilePrefix = filePrefix;
        Log.appenderClose();
        Xlog.appenderOpen(sFileFilter,
                Xlog.AppednerModeAsync,
                Lg.dir,
                Lg.dir == null ? defaultDir : Lg.dir,
                sFilePrefix);
        return this;
    }

    public Lg setIsSecondWrap(boolean isSecondWrap) {
        Lg.isSecondWrap = isSecondWrap;
        return this;
    }

    public static String getConfig() {
        return "switch: " + sLogSwitch
                + LINE_SEP + "console: " + sLog2ConsoleSwitch
                + LINE_SEP + "tag: " + (sTagIsSpace ? "null" : sGlobalTag)
                + LINE_SEP + "head: " + sLogHeadSwitch
                + LINE_SEP + "file: " + sLog2FileSwitch
                + LINE_SEP + "dir: " + (dir == null ? defaultDir : dir)
                + LINE_SEP + "border: " + sLogBorderSwitch
                + LINE_SEP + "consoleFilter: " + T[sConsoleFilter - V]
                + LINE_SEP + "fileFilter: " + T[sFileFilter - V]
                + LINE_SEP + "prefix: " + sFilePrefix
                + LINE_SEP + "secondWrap: " + isSecondWrap;
    }

    public static void v(Object contents) {
        log(V, sGlobalTag, contents);
    }

    public static void v(String tag, Object... contents) {
        log(V, tag, contents);
    }

    public static void d(Object contents) {
        log(D, sGlobalTag, contents);
    }

    public static void d(String tag, Object... contents) {
        log(D, tag, contents);
    }

    public static void i(Object contents) {
        log(I, sGlobalTag, contents);
    }

    public static void i(String tag, Object... contents) {
        log(I, tag, contents);
    }

    public static void w(Object contents) {
        log(W, sGlobalTag, contents);
    }

    public static void w(String tag, Object... contents) {
        log(W, tag, contents);
    }

    public static void e(Object contents) {
        log(E, sGlobalTag, contents);
    }

    public static void e(String tag, Object... contents) {
        log(E, tag, contents);
    }

    public static void a(Object contents) {
        log(A, sGlobalTag, contents);
    }

    public static void a(String tag, Object... contents) {
        log(A, tag, contents);
    }

    public static void file(Object contents) {
        log(FILE | D, sGlobalTag, contents);
    }

    public static void file(@TYPE int type, Object contents) {
        log(FILE | type, sGlobalTag, contents);
    }

    public static void file(String tag, Object contents) {
        log(FILE | D, tag, contents);
    }

    public static void file(@TYPE int type, String tag, Object contents) {
        log(FILE | type, tag, contents);
    }

    public static void json(String contents) {
        log(JSON | D, sGlobalTag, contents);
    }

    public static void json(@TYPE int type, String contents) {
        log(JSON | type, sGlobalTag, contents);
    }

    public static void json(String tag, String contents) {
        log(JSON | D, tag, contents);
    }

    public static void json(@TYPE int type, String tag, String contents) {
        log(JSON | type, tag, contents);
    }

    public static void xml(String contents) {
        log(XML | D, sGlobalTag, contents);
    }

    public static void xml(@TYPE int type, String contents) {
        log(XML | type, sGlobalTag, contents);
    }

    public static void xml(String tag, String contents) {
        log(XML | D, tag, contents);
    }

    public static void xml(@TYPE int type, String tag, String contents) {
        log(XML | type, tag, contents);
    }

    public static void flush() {
        Log.appenderFlush(false);
    }

    public static void destroy() {
        Log.appenderClose();
    }

    private static void log(final int type, String tag, final Object... contents) {
        if (!sLogSwitch || (!sLog2ConsoleSwitch && !sLog2FileSwitch)) return;
        int type_low = type & 0x0f, type_high = type & 0xf0;
        if (type_low < sConsoleFilter && type_low < sFileFilter) return;
        final String[] tagAndHead = processTagAndHead(tag);
        String body = processBody(type_high, contents);
        if (sLog2ConsoleSwitch && type_low >= sConsoleFilter) {
            print2Console(type_low, tagAndHead[0], tagAndHead[1] + body);
        }
        if (sLog2FileSwitch || type_high == FILE) {
            if (type_low >= sFileFilter) print2File(type_low, tagAndHead[0], tagAndHead[2] + body);
        }
    }

    private static String[] processTagAndHead(String tag) {
        if (!sTagIsSpace && !sLogHeadSwitch) {
            tag = sGlobalTag;
        } else {
            StackTraceElement targetElement = new Throwable().getStackTrace()[isSecondWrap ? 4 : 3];
            String className = targetElement.getClassName();
            String[] classNameInfo = className.split("\\.");
            if (classNameInfo.length > 0) {
                className = classNameInfo[classNameInfo.length - 1];
            }
            if (className.contains("$")) {
                className = className.split("\\$")[0];
            }
            if (sTagIsSpace) {
                tag = isSpace(tag) ? className : tag;
            }
            if (sLogHeadSwitch) {
                String head = new Formatter()
                        .format("%s, %s(%s.java:%d)",
                                Thread.currentThread().getName(),
                                targetElement.getMethodName(),
                                className,
                                targetElement.getLineNumber())
                        .toString();
                return new String[]{tag, head + LINE_SEP, " [" + head + "]: "};
            }
        }
        return new String[]{tag, "", ": "};
    }

    private static String processBody(int type, Object... contents) {
        String body = NULL_TIPS;
        if (contents != null) {
            if (contents.length == 1) {
                Object object = contents[0];
                body = object == null ? NULL : object.toString();
                if (type == JSON) {
                    body = formatJson(body);
                } else if (type == XML) {
                    body = formatXml(body);
                }
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0, len = contents.length; i < len; ++i) {
                    Object content = contents[i];
                    sb.append(ARGS)
                            .append("[")
                            .append(i)
                            .append("]")
                            .append(" = ")
                            .append(content == null ? NULL : content.toString())
                            .append(LINE_SEP);
                }
                body = sb.toString();
            }
        }
        return body;
    }

    private static String formatJson(String json) {
        try {
            if (json.startsWith("{")) {
                json = new JSONObject(json).toString(4);
            } else if (json.startsWith("[")) {
                json = new JSONArray(json).toString(4);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static String formatXml(String xml) {
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlInput, xmlOutput);
            xml = xmlOutput.getWriter().toString().replaceFirst(">", ">" + LINE_SEP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    private static void print2Console(final int type, String tag, String msg) {
        if (sLogBorderSwitch) {
            print(type, tag, TOP_BORDER);
            msg = addLeftBorder(msg);
        }
        int len = msg.length();
        int countOfSub = len / MAX_LEN;
        if (countOfSub > 0) {
            print(type, tag, msg.substring(0, MAX_LEN));
            String sub;
            int index = MAX_LEN;
            for (int i = 1; i < countOfSub; i++) {
                sub = msg.substring(index, index + MAX_LEN);
                print(type, tag, sLogBorderSwitch ? LEFT_BORDER + sub : sub);
                index += MAX_LEN;
            }
            sub = msg.substring(index, len);
            print(type, tag, sLogBorderSwitch ? LEFT_BORDER + sub : sub);
        } else {
            print(type, tag, msg);
        }
        if (sLogBorderSwitch) print(type, tag, BOTTOM_BORDER);
    }

    private static void print(final int type, final String tag, final String msg) {
        switch (type) {
            case V:
                android.util.Log.v(tag, msg);
                break;
            case D:
                android.util.Log.d(tag, msg);
                break;
            case I:
                android.util.Log.i(tag, msg);
                break;
            case W:
                android.util.Log.w(tag, msg);
                break;
            case E:
                android.util.Log.e(tag, msg);
                break;
            case A:
                android.util.Log.wtf(tag, msg);
                break;
        }
    }

    private static String addLeftBorder(String msg) {
        if (!sLogBorderSwitch) return msg;
        StringBuilder sb = new StringBuilder();
        String[] lines = msg.split(LINE_SEP);
        for (String line : lines) {
            sb.append(LEFT_BORDER).append(line).append(LINE_SEP);
        }
        return sb.toString();
    }

    private static boolean isSpace(String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static void print2File(final int type, final String tag, final String msg) {
        switch (type) {
            case V:
                Log.v(tag, msg);
                break;
            case D:
                Log.d(tag, msg);
                break;
            case I:
                Log.i(tag, msg);
                break;
            case W:
                Log.w(tag, msg);
                break;
            case E:
                Log.e(tag, msg);
                break;
            case A:
                Log.f(tag, msg);
                break;
        }
    }
}
