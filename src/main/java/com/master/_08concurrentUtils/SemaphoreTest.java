package com.master._08concurrentUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @ClassName: SemaphoreTest
 * @Package: com.master._08concurrentUtils
 * @Description: 线程信号量控制并发
 * @Datetime: 2023/11/25 17:11
 * @author: ColorXJH
 */
public class SemaphoreTest {
    private static final int THREAD_COUNT=30;
    private static ExecutorService threadPool=Executors.newFixedThreadPool(THREAD_COUNT);
    private static Semaphore semaphore=new Semaphore(10);

    public static void main(String[] args) {
        for (int i = 0; i < THREAD_COUNT; i++) {
            threadPool.execute(()->{
                try {
                    semaphore.acquire();
                    System.out.println("save data--");
                    semaphore.release();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        threadPool.shutdown();
    }
}

/**
 * 在代码中，虽然有30个线程在执行，但是只允许10个并发执行。Semaphore的构造方法
 * Semaphore（int permits）接受一个整型的数字，表示可用的许可证数量。Semaphore（10）表示允
 * 许10个线程获取许可证，也就是最大并发数是10。Semaphore的用法也很简单，首先线程使用
 * Semaphore的acquire()方法获取一个许可证，使用完之后调用release()方法归还许可证。还可以
 * 用tryAcquire()方法尝试获取许可证。
 *
 */