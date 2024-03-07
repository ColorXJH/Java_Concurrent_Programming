package com.master.PART2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-03-06 11:16
 */
public class UnsafeInitialization {

    private int data;

    public UnsafeInitialization() {
        Thread thread = new Thread(new MyRunnable());
        thread.start(); // 在构造函数中启动线程
        // 线程启动后，构造函数可能尚未完成
        // 由于线程可能在对象完全初始化之前运行，存在对象状态不稳定的风险
    }

    public void setData(int value) {
        this.data = value;
    }

    public int getData() {
        return this.data;
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            // 线程执行时尝试访问对象的状态
            int value = getData(); // 访问对象的状态
            System.out.println("Thread running with data: " + value);
        }
    }

    public static void main(String[] args) {
        UnsafeInitialization obj = new UnsafeInitialization();
        obj.setData(42); // 在构造函数之后修改对象的状态
        List list=new ArrayList<>();
        Object ref=list;
        ref=null;
        System.out.println(ref);
        System.out.println(list);
        list=null;
        System.out.println(list);

    }
}
