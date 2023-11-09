package com.master._04javaConcurrentProgrammingBase;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: Shutdown
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 线程的终止
 * @Datetime: 2023/11/9 21:06
 * @author: ColorXJH
 */
public class Shutdown {
    public static void main(String[] args) throws InterruptedException {
        Runner runner=new Runner();
        Thread countThread=new Thread(runner,"Count Thread");
        countThread.start();
        //睡眠一秒，main线程对CountThread线程进行中断，使CountThread线程能够感知到中断而结束
        TimeUnit.SECONDS.sleep(1);
        countThread.interrupt();
        Runner two=new Runner();
        countThread=new Thread(two,"Count Thread");
        countThread.start();
        //睡眠1秒，main线程对two进行取消，使CountThread能够感知on为false而结束
        TimeUnit.SECONDS.sleep(1);
        two.cancel();

        //在上方方法中，main线程通过中断操作interrupt()和自定义的cancel()方法（设置修改可见性参数的布尔值）都可以终止，
        //这种通过标识位或者中断操作的方式都能够使线程在终止的时候有机会去清理资源，而不是武断的将线程停止，因此上面的做法更安全
        //相比于suspend(),stop()这样的过时方法，设置表示位与布尔方法更优
    }

    private static class Runner implements Runnable{
        private long i;
        private volatile boolean on=true;
        @Override
        public void run() {
            while(on&&!Thread.currentThread().isInterrupted()){
                i++;
            }
            System.out.println("count i="+i);
        }
        public void cancel(){
            on=false;
        }
    }
}
