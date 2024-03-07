package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-03-06 11:18
 */
public class SafeInitialization {
    private int data;

    public SafeInitialization(int data) {
        this.data = data;
        initialize(); // 在构造函数中调用初始化方法
    }

    private void initialize() {
        // 对象初始化操作
        // 在这里完成所有必要的初始化工作
        // 这样确保对象已完全初始化后再启动线程
        Thread thread = new Thread(new MyRunnable());
        thread.start(); // 在初始化完成后启动线程
    }

    private class MyRunnable implements Runnable {
        @Override
        public void run() {
            // 这里是线程执行的代码逻辑
            // 可以安全地访问对象的状态和数据
            System.out.println("Thread running with data: " + data);
        }
    }

    public static void main(String[] args) {
        SafeInitialization obj = new SafeInitialization(10); // 创建对象并完成初始化
        // 现在线程已经在初始化完成后启动，可以安全地访问对象的状态
    }
}
