package com.master.PART2;

import java.util.Random;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 完全同步的单例类的创建，推迟实例的创建，直到通过instance方法第一次调用这个类
 * @date 2024-02-26 14:50
 */
public class LazySingletonCounter {
    private final long initial;
    private long count;
    private LazySingletonCounter(){
        initial=Math.abs(new Random().nextLong()/2);
        count=initial;
    }

    private static LazySingletonCounter s=null;
    private static final Object classLock=LazySingletonCounter.class;
    public static LazySingletonCounter instance(){
        synchronized (classLock){
            if(s==null){
                s=new LazySingletonCounter();
            }
            return s;
        }
    }

    public long next(){
        synchronized (classLock){
            return count++;
        }
    }

    public void reset(){
        synchronized (classLock){
            count=initial;
        }
    }

}
