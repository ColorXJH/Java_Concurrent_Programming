package com.master._04javaConcurrentProgrammingBase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: WaitNotify
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 等待通知模式 java对象内置的wait/notify模式
 * @Datetime: 2023/11/11 14:58
 * @author: ColorXJH
 */
public class WaitNotify {
    static boolean flag=true;
    static Object lock=new Object();

    public static void main(String[] args) throws InterruptedException {
        Thread waitThread=new Thread(new Wait(),"wait-thread");
        waitThread.start();
        TimeUnit.SECONDS.sleep(1);
        Thread notifyThread=new Thread(new Notify(),"notify-thread");
        notifyThread.start();
    }
    static class Wait implements Runnable{
        /**
         * 在synchronized代码块或方法中，对共享变量的修改会立即被写入主内存，并且其他线程在获取锁之后会从主内存中读取最新的值。
         * 因此，如果一个线程修改了某个变量的值，其他线程在获取锁之后就一定能看到最新的值
         */
        @Override
        public void run() {
            //加锁，拥有lock对象的Monitor
            synchronized (lock){
                //当条件不满足的时候 继续等待 同时释放lock对象的锁
                while (flag){
                    try {
                        System.out.println(Thread.currentThread()+" flag is true wait @ "+new SimpleDateFormat("HH:mm:ss").format(new Date()));
                        lock.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                //满足条件时，完成工作
                System.out.println(Thread.currentThread()+" flag is false @ "+new SimpleDateFormat("HH:mm:ss").format(new Date()));
            }
        }
    }

    static class Notify implements Runnable{

        @Override
        public void run() {
            //加锁 拥有lock对象的Monitor
            synchronized (lock){
                //获取lock锁，然后进行通知，通知时不会释放lock的锁
                //直到当前线程释放了lock的锁之后，Wait thread才能从wait()方法中返回
                System.out.println(Thread.currentThread()+" hold lock. notify @"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
                lock.notifyAll();
                flag=false;
                SleepUtils.second(5);
            }
            //再次加锁
            synchronized (lock){
                System.out.println(Thread.currentThread()+" hold lock again,sleep @"+new SimpleDateFormat("HH:mm:ss").format(new Date()));
                SleepUtils.second(5);
            }
        }
    }
}

//结果如下
/**
 * Thread[wait-thread,5,main] flag is true wait @ 16:36:19
 * Thread[notify-thread,5,main] hold lock. notify @16:36:20
 * Thread[notify-thread,5,main] hold lock again,sleep @16:36:25
 * Thread[wait-thread,5,main] flag is false @ 16:36:30
 * 注意 上述第3行和第4行输出的顺序可能会互换，上述；例子主要说明了wait\notify\notifyAll调用时需要注意的细节如下：
 * 1：使用wait\notify\notifyAll时需要先对调用对象加锁
 * 2：调用wait()方法之后，线程状态由running编程waiting,并将当前线程放到对象的等待队列
 * 3：notify、notifyAll方法调用后，等待线程依旧不会从wait()返回，需要调用notify或者notifyAll的线程释放锁之后，等待线程才有机会从wait()返回
 * 4：notify方法将等待队列中的一个等待线程从等待队列移到同步队列中，而notifyAll方法将等待队列中的所有线程移到同步队列中，被移动的线程状态由waiting变为blocked状态
 * 5：从wait()方法返回的前提是获得了调用对象的锁，
 * 从上述细节可以看到，等待、通知机制 依赖于同步机制，其目的就是确保等待线程从wait()方法返回时能够感知到线程对变量做出的改变
 */