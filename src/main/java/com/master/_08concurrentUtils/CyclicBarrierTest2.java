package com.master._08concurrentUtils;

import jdk.internal.org.objectweb.asm.tree.MultiANewArrayInsnNode;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @ClassName: CyclicBarrierTest2
 * @Package: com.master._08concurrentUtils
 * @Description: 循环屏障有线执行特定行为
 * @Datetime: 2023/11/25 12:23
 * @author: ColorXJH
 */
public class CyclicBarrierTest2 {

    static class A implements Runnable{

        @Override
        public void run() {
            System.out.println("首先要做的事情---");
        }
    }

    static CyclicBarrier barrier=new CyclicBarrier(2,new A());

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
                System.out.println("1");
            }
        }).start();

        try {
            barrier.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
        System.out.println("2");
    }

}
