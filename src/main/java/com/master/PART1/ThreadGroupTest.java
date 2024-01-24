package com.master.PART1;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 线程组测试
 * @date 2024-01-24 9:12
 */
public class ThreadGroupTest {
    public static void main(String[] args) {
        ThreadGroup myThreadGroup=new ThreadGroup("myThreadGroup");
        Thread thread1=new Thread(myThreadGroup,new MyRunnable());
        Thread thread2=new Thread(myThreadGroup,new MyRunnable());
        thread1.start();
        thread2.start();
        //列出线程组中的线程
        System.out.println("active thread in "+myThreadGroup.getName()+": "+myThreadGroup.activeCount());
        myThreadGroup.list();
    }

    static class MyRunnable implements Runnable{

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName()+" MyRunnable.run");
        }
    }
}
