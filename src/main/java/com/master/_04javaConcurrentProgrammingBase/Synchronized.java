package com.master._04javaConcurrentProgrammingBase;

/**
 * @ClassName: Synchronized
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 同步方法和同步代码块 在Synchronized.class同级目录使用javap -v Synchronized.class 生成文件信息查看synchronized的实现细节
 * @Datetime: 2023/11/11 14:12
 * @author: ColorXJH
 */
public class Synchronized {
    public static void main(String[] args) {
        //对Synchronized Class对象进行加锁
        synchronized (Synchronized.class){

        }
        //静态同步方法 对Synchronized Class对象进行加锁
        m();
    }
    public static synchronized void m(){

    }
}

/**
 * class信息中，对于同步块的实现使用了monitorenter和monitorexit指令，而同步方法则
 * 是依靠方法修饰符上的ACC_SYNCHRONIZED来完成的。无论采用哪种方式，其本质是对一
 * 个对象的监视器（monitor）进行获取，而这个获取过程是排他的，也就是同一时刻只能有一个
 * 线程获取到由synchronized所保护对象的监视器
 *
 */

/**
 * 任何一个对象都有自己的监视器，当这个对象由同步块或者这个对象的同步方法调用时，执行方法的线程必须先获取到这个对象的监视器才能进入同步块块或者同步方法
 * 而没有获取到监视器(执行该方法)的线程将会被阻塞在同步块或者同步方法的入口处，进入blocked状态，过程如下：
 *          ---》Monitor.enter          -->Monitor监视器                  --》Monitor.Enter成功     ---》对象Object    --->Monitor.Exit-->
 *                                                                       --》Monitor.Enter失败
 *     重新试图进入Monitor监视器《——Monitor.Exit后通知，出队列<--同步队列SynchronizedQueue《--|
 * 从上面的流程可以看到：任意线程对Object(Object由Synchronized保护)的访问，首先要获得Object的监视器，如果获取失败，线程进入同步队列，线程状态变为Blocked
 * 当访问Object的前驱(获得了锁的线程)释放了锁，则该释操作唤醒在同步队列中的线程，使其重新尝试对监视器的获取
 */