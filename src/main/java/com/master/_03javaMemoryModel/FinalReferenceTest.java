package com.master._03javaMemoryModel;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2022/12/8 10:08
 */
public class FinalReferenceTest {
    public static void main(String[] args) {
        Thread t1=new Thread(new Runnable() {
            @Override
            public void run() {
                FinalReferenceExample.writeOne();
            }
        });
        t1.start();
        Thread t2=new Thread(new Runnable() {
            @Override
            public void run() {
                FinalReferenceExample.writeTwo();
            }
        });
        t2.start();
        Thread t3=new Thread(new Runnable() {
            @Override
            public void run() {
                FinalReferenceExample.reader();
            }
        });
        t3.start();

    }
}
