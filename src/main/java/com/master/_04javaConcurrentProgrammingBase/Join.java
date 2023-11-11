package com.master._04javaConcurrentProgrammingBase;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: Join
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description:
 * @Datetime: 2023/11/11 18:49
 * @author: ColorXJH
 */
public class Join {
    public static void main(String[] args) throws InterruptedException {
        Thread previous=Thread.currentThread();
        for (int i = 0; i < 10; i++) {
            //每个线程都拥有前一个线程的引用，需要等待前一个线程终止，才能从等待中返回
            Thread thread=new Thread(new Domino(previous),String.valueOf(i));
            thread.start();
            previous=thread;
        }
        TimeUnit.SECONDS.sleep(5);
        System.out.println(Thread.currentThread().getName()+" terminate.");
        /**
         * 每个线程的终止是前驱线程的终止，每个线程等待前驱线程终止后，才从join()方法返回，这里涉及到了等待通知（等待前驱线程结束，接收前驱线程结束通知）
         * join()源码部分如下
         *  public final synchronized void join() throws InterruptedException{
         *      //条件不满足，继续等待
         *      while(isAlive()){
         *          wait(0);
         *      }
         *      //条件符合，方法返回
         *  }
         * 当线程终止时，会调用线程自身的notifyAll()方法，会通知所有等待在该线程对象上的线程
         * 等待/通知经典范式一致，即加锁、循环和处理逻辑3个步
         */
    }

    static class Domino implements Runnable{
        private Thread thread;
        public Domino(Thread thread){
            this.thread=thread;
        }
        /**
         * 当调用Thread.join(Long long)方法时，如果指定的线程还未执行完毕，且等待时间已经到了，那么当前线程会被唤醒并继续执行。
         * 此时，指定的线程仍然在继续执行，不会因为当前线程被唤醒而停止执行。
         *
         * 需要注意的是，Thread.join(Long long)方法只是让当前线程等待指定的线程执行一段时间，而不是强制停止指定的线程。
         * 如果需要强制停止指定的线程，可以使用Thread.interrupt()方法来中断指定的线程。
         */
        @Override
        public void run() {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println(Thread.currentThread().getName()+" terminate.");
        }
    }
}
