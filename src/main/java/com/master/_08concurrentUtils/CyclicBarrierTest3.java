package com.master._08concurrentUtils;

import com.master._04javaConcurrentProgrammingBase.SleepUtils;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @ClassName: CyclicBarrierTest3
 * @Package: com.master._08concurrentUtils
 * @Description: 循环屏障使用案例
 * @Datetime: 2023/11/25 16:28
 * @author: ColorXJH
 */
public class CyclicBarrierTest3 {
    static CyclicBarrier barrier=new CyclicBarrier(2);

    public static void main(String[] args) {//执行顺序如下：
        Thread thread=new Thread(() -> {
            try {
                barrier.await();//2
                System.out.println("又开始执行了---");
            } catch (InterruptedException e) {
                System.out.println("被打断了----");//7
                throw new RuntimeException(e);
            } catch (BrokenBarrierException e) {
                throw new RuntimeException(e);
            }
        });
        thread.setName("ColorXJH");
        thread.start();//1
        SleepUtils.second(1);
        thread.interrupt();//3
        System.out.println("阻塞的线程数量"+barrier.getNumberWaiting());//4
        try {
            System.out.println("先开始执行---");//5
            barrier.await();//6
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            System.out.println(barrier.isBroken());
            throw new RuntimeException(e);//8
        }

    }
}
