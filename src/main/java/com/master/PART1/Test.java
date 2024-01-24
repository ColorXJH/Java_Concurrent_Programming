package com.master.PART1;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-01-23 16:59
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    System.out.println("2"+Thread.currentThread().isInterrupted());
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
        System.out.println("1"+t.isInterrupted());
        Thread.sleep(1000);
        t.interrupt();
        Thread.sleep(2000);
        System.out.println("3"+t.isInterrupted());
    }
}
