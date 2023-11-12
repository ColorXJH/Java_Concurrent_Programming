package com.master._04javaConcurrentProgrammingBase;

import java.util.concurrent.TimeUnit;

/**
 * @ClassName: ThreadPoolClient
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description:
 * @Datetime: 2023/11/12 13:55
 * @author: ColorXJH
 */
public class ThreadPoolClient {
    public static void main(String[] args) throws InterruptedException {
        DefaultThreadPool2 pool=new DefaultThreadPool2(10);
        for (int i=0;i<100;i++){
            int finalI = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("我被执行了----:  "+ finalI);
                }
            });
        }
        Thread.sleep(5000);
        System.out.println( pool.getJobSize()+"  <===");

        //为什么需要后续任务才能是前方任务中断？
        /*for (int i=0;i<10;i++){
            int finalI = i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("我被执行了222222----:  "+ finalI);
                }
            });
        }*/

    }
}


/**
 * 从线程池的实现可以看到，当客户端调用execute(Job)方法时，会不断地向任务列表jobs中
 * 添加Job，而每个工作者线程会不断地从jobs上取出一个Job进行执行，当jobs为空时，工作者线
 * 程进入等待状态。
 *
 * 添加一个Job后，对工作队列jobs调用了其notify()方法，而不是notifyAll()方法，因为能够
 * 确定有工作者线程被唤醒，这时使用notify()方法将会比notifyAll()方法获得更小的开销（避免
 * 将等待队列中的线程全部移动到阻塞队列中）。
 *
 * 可以看到，线程池的本质就是使用了一个线程安全的工作队列连接工作者线程和客户端
 * 线程，客户端线程将任务放入工作队列后便返回，而工作者线程则不断地从工作队列上取出
 * 工作并执行。当工作队列为空时，所有的工作者线程均等待在工作队列上，当有客户端提交了
 * 一个任务之后会通知任意一个工作者线程，随着大量的任务被提交，更多的工作者线程会被
 * 唤醒。
 *
 */