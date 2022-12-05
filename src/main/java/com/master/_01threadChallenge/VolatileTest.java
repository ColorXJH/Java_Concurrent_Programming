package com.master._01threadChallenge;

import java.util.concurrent.LinkedTransferQueue;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 如何优化volatile
 * @date 2022/12/5 16:05
 */
public class VolatileTest {
    public static void main(String[] args) throws InterruptedException {
        MyThread changeThread=new MyThread();
        new Thread(changeThread).start();
        while(true){
            boolean mainflag=changeThread.isFlag();
            if(mainflag){
                System.out.println("mainflagChanged======"+mainflag);
                break;
            }
        }
        System.out.println("main thread end");
        //Doug lea优化volatile--查看源码
        LinkedTransferQueue<MyThread>queue=new LinkedTransferQueue<>();
    }
}
