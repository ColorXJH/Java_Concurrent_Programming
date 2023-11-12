package com.master._04javaConcurrentProgrammingBase;

import java.sql.Connection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: ConnectionPoolTest
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 模拟客户端ConnectionRunner获取、使用、最后释放连接的过程，当它使用时连接将会增加获取到连接的数量，反之，将会增加未获取到连接的数量
 * @Datetime: 2023/11/12 11:42
 * @author: ColorXJH
 */
public class ConnectionPoolTest {
    static ConnectionPool pool=new ConnectionPool(10);
    //保证所有的ConnectionRunner能够同时开始
    static CountDownLatch start=new CountDownLatch(1);
    //main线程将会等待所有的ConnectionRunner结束后才继续执行
    static CountDownLatch end;

    public static void main(String[] args) throws InterruptedException {
        //线程数量，可以修改线程数量进行观察 (10个线程，每个线程获取20次，一共获取200次)
        int threadCount=50;
        end=new CountDownLatch(threadCount);
        int count=20;
        AtomicInteger got=new AtomicInteger();
        AtomicInteger notGot=new AtomicInteger();
        for(int i=0;i<threadCount;i++){
            Thread thread=new Thread(new ConnectionRunner(count,got,notGot),"ConnectionRunnerThread"+i);
            thread.start();
        }
        //等待所有的thread可以同时执行
        start.countDown();
        //等待所有的线程执行完之后才让main线程执行
        end.await();
        System.out.println("total invoke: "+(threadCount*count));
        System.out.println("got connection: "+got);
        System.out.println("not got connection: "+notGot);
    }



    static class ConnectionRunner implements Runnable{
        int count;
        AtomicInteger got;
        AtomicInteger notGot;
        public ConnectionRunner(int count,AtomicInteger got,AtomicInteger notGot){
            this.count=count;
            this.got=got;
            this.notGot=notGot;
        }
        @Override
        public void run() {
            try {
                start.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            while (count>0){
                //从线程池中获取连接，如果1000ms内无法获取到，将会返回null,
                //分别统计获取到和未能获取到的数量
                try {
                    Connection connection= pool.fetchConnection(1000);
                    if(connection!=null){
                        try {
                            connection.createStatement();
                            connection.commit();
                        } finally {
                            pool.releaseConnection(connection);
                            got.incrementAndGet();
                        }
                    }else{
                        notGot.incrementAndGet();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }finally {
                    count--;
                }

            }
            end.countDown();
        }
    }
}


/**
 * 在资源一定的情况下（连接池中的10个连接），随着客户端
 * 线程的逐步增加，客户端出现超时无法获取连接的比率不断升高。虽然客户端线程在这种超
 * 时获取的模式下会出现连接无法获取的情况，但是它能够保证客户端线程不会一直挂在连接
 * 获取的操作上，而是“按时”返回，并告知客户端连接获取出现问题，是系统的一种自我保护机
 * 制。数据库连接池的设计也可以复用到其他的资源获取的场景，针对昂贵资源（比如数据库连
 * 接）的获取都应该加以超时限制。
 *
 */