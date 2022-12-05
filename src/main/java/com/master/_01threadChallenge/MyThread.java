package com.master._01threadChallenge;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2022/12/5 16:42
 */
public class MyThread implements Runnable{
    volatile boolean  flag=false;
    public boolean isFlag() {
        return flag;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("subThread change flag to:"+flag);
        flag=true;
    }
}
