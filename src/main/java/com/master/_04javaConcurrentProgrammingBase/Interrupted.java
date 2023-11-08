package com.master._04javaConcurrentProgrammingBase;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: Interrupted
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 线程中断，观察标识位
 * @Datetime: 2023/11/8 23:23
 * @author: ColorXJH
 */
public class Interrupted {
    public static void main(String[] args) throws InterruptedException {
        //不停的尝试睡眠
        Thread sleepThread=new Thread(new SleepRunner(),"sleepThread");
        sleepThread.setDaemon(true);
        //不停的运行
        Thread busyThread=new Thread(new BusyRunner(),"busyThread");
        busyThread.setDaemon(true);
        sleepThread.start();
        busyThread.start();
        //休眠5秒这种，让上面两个线程充分运行
        TimeUnit.SECONDS.sleep(5);
        sleepThread.interrupt();
        busyThread.interrupt();
        System.out.println("sleepThread interrupted is :"+sleepThread.isInterrupted());//sleepThread interrupted is :false
        System.out.println("busyThread interrupted is :"+busyThread.isInterrupted());//busyThread interrupted is :true
        //防止sleepThread busyThread立刻退出
        SleepUtils.second(2);


        //从结果可以看出，抛出InterruptedException的线程SleepThread，其中断标识位被清除了，
        //而一直忙碌运作的线程BusyThread，中断标识位没有被清除。

    }

    static class SleepRunner implements Runnable{

        @Override
        public void run() {
            while (true){
                //中断后抛异常，自动清理中断标记
                SleepUtils.second(10);
            }

        }
    }
    static class BusyRunner implements Runnable{

        @Override
        public void run() {
//            while (true){
//
//            }
            //没被interrupt之前一致返回false,当外界调用线程的interrupt()方法，这里检测到打断，返回true,并清楚标志位
            while (!Thread.interrupted()){
                System.out.println("没有被中断");

            }
            //因为清楚了标准位置，这里依然显示未被打断 false
            System.out.println("清除标志位之后---"+Thread.currentThread().isInterrupted());
        }
    }
}
