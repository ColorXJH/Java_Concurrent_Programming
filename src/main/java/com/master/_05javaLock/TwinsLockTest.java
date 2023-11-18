package com.master._05javaLock;

/**
 * @ClassName: TwinsLockTest
 * @Package: com.master._05javaLock
 * @Description:
 * @Datetime: 2023/11/18 22:02
 * @author: ColorXJH
 */

import com.master._04javaConcurrentProgrammingBase.SleepUtils;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

/**
 * 验证TwinsLock是否能按照预期工作。在测试用例中，定义了工作者
 * 线程Worker，该线程在执行过程中获取锁，当获取锁之后使当前线程睡眠1秒（并不释放锁），
 * 随后打印当前线程名称，最后再次睡眠1秒并释放锁
 */
public class TwinsLockTest {
    static CountDownLatch start=new CountDownLatch(1);
    @Test
    public void test(){
        final Lock lock=new TwinsLock();
        class Worker extends Thread{
            @Override
            public void run() {
                try {
                    start.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                while (true){
                    lock.lock();
                    try {
                        SleepUtils.second(1);
                        System.out.println(Thread.currentThread().getName());
                        SleepUtils.second(1);
                    } finally {
                        lock.unlock();
                    }

                }
            }
        }
        // 启动10个线程
        for (int i = 0; i < 10; i++) {
            Worker w=new Worker();
            w.setName("Thread "+i);
            w.setDaemon(true);
            w.start();
        }
        //确保10个线程同时执行
        start.countDown();
        // 每隔1秒换行
        for (int i = 0; i < 10; i++) {
            SleepUtils.second(1);
            System.out.println();
        }
    }
}
