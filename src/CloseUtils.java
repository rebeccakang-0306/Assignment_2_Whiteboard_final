

import java.io.Closeable;

/**
 * 名字:工具类
 * 作用:关闭各种资源
 * @author Himit_ZH
 *
 */
public class CloseUtils {
    /**
     * 释放资源
     */
    public static void close(Closeable... targets ) {
        for(Closeable target:targets) {
            try {
                if(null!=target) {
                    target.close();
                }
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
