package com.master.PART4;

import java.time.LocalDateTime;

/**
 * @ClassName: ThreadTest
 * @Package: com.master.PART4
 * @Description:
 * @Datetime: 2024/4/11 22:02
 * @author: ColorXJH
 */
public class ThreadTest {
    public static void main(String[] args) {
        System.out.println("主线程：开始执行程序了！！！当前时间："+ LocalDateTime.now().toString());
        Thread t1=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    System.out.println("新线程：我睡了5秒，当前时间："+ LocalDateTime.now().toString());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t1.start();
        try {
            t1.join(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("主线程：2秒钟已达到，不管了，我先跑路了！当前时间："+ LocalDateTime.now().toString());
    }
}

