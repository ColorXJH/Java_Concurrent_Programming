package com.master._08concurrentUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @ClassName: CyclicBarrierTest
 * @Package: com.master._08concurrentUtils
 * @Description: 循环屏障 拦截线程 使其同时运行
 * @Datetime: 2023/11/25 12:08
 * @author: ColorXJH
 */
public class CyclicBarrierTest {
    static CyclicBarrier barrier=new CyclicBarrier(2);

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(1);
            }
        }).start();
        try {
            barrier.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        System.out.println(2);
    }
}

/**
 * 因为主线程和子线程的调度是由CPU决定的，两个线程都有可能先执行，所以会产生两种
 * 输出， 1 2。  2 1
 *
 */

/**
 * 如果把new CyclicBarrier(2)修改成new CyclicBarrier(3)，则主线程和子线程会永远等待，
 * 因为没有第三个线程执行await方法，即没有第三个线程到达屏障，所以之前到达屏障的两个
 * 线程都不会继续执行
 *
 */