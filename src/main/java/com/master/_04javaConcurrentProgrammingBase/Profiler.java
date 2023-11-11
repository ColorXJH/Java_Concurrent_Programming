package com.master._04javaConcurrentProgrammingBase;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: Profiler
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 构建了一个常用的Profiler类，它具有begin()和end()两个方法，而end()方法返回从begin()方法调用开始到end()方法被调用时的时间差，单位是毫秒
 * @Datetime: 2023/11/11 20:25
 * @author: ColorXJH
 */
public class Profiler {
    //第一次get()方法调用时会进行初始化（如果set方法没有调用），每个线程会调用一次
    private static final ThreadLocal<Long>TIME_THREADLOCAL=new ThreadLocal<Long>(){
        @Override
        protected Long initialValue() {
            return System.currentTimeMillis();
        }
    };

    public static final void begin(){
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }
    public static final long end(){
        return System.currentTimeMillis()-TIME_THREADLOCAL.get();
    }
    public static void main(String[] args) throws InterruptedException {
        Profiler.begin();
        TimeUnit.SECONDS.sleep(5);
        System.out.println("Cost: "+Profiler.end()+" mills");

    }
}

/**
 * Profiler可以被复用在方法调用耗时统计的功能上，在方法的入口前执行begin()方法，在
 * 方法调用后执行end()方法，好处是两个方法的调用不用在一个方法或者类中，比如在AOP（面
 * 向方面编程）中，可以在方法调用前的切入点执行begin()方法，而在方法调用后的切入点执行
 * end()方法，这样依旧可以获得方法的执行耗时。
 */