package com.master._03javaMemoryModel;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2022/12/7 8:41
 */
public class RecorderTest {
    public static void main(String[] args) {
        RecorderExample example=new RecorderExample();
        Thread t=new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    example.writer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();

            Thread ts=new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        example.reader();
                    }
                }
            });
            ts.start();
        System.out.println("main thread end");
    }
}
