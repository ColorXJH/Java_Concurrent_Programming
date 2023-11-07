package com.master._04javaConcurrentProgrammingBase;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: SleepUtils
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description:
 * @Datetime: 2023/11/7 23:03
 * @author: ColorXJH
 */
public class SleepUtils {
    public static final void second(long seconds){
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
