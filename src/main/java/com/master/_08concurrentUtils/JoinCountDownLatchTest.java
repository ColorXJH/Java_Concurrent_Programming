package com.master._08concurrentUtils;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 测试CountDownLatch的一种方法
 * @date 2023-11-24 9:42
 */
public class JoinCountDownLatchTest {
    public static void main(String[] args) throws InterruptedException {
        Thread parser1=new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("开始解析excel1");
            }
        });
        Thread parser2=new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("开始解析excel2");
            }
        });
        parser1.start();
        parser2.start();
        parser1.join();
        parser2.join();
        System.out.println("两个excel解析完毕");
    }
}

/**
 * oin用于让当前执行线程等待join线程执行结束。其实现原理是不停检查join线程是否存
 * 活，如果join线程存活则让当前线程永远等待。其中，wait（0）表示永远等待下去，代码片段如
 * 下。
 * while (isAlive()) {
 * wait(0);
 * }
 * 直到join线程中止后，线程的this.notifyAll()方法会被调用，调用notifyAll()方法是在JVM里
 * 实现的，所以在JDK里看不到，大家可以查看JVM源码。
 * 在JDK 1.5之后的并发包中提供的CountDownLatch也可以实现join的功能，并且比join的功
 * 能更多,见CountDownLatchTest.java
 *
 */