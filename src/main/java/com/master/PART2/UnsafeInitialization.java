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

    //决定数据陈旧与否，或者是否需要声明为volatile类型十分依赖在使用环境中的遍历策略的选择，如果用索引循环来遍历
    //那么使用陈旧的size值是不能接收的,例如，某个客户获得的size值是0，这样他会跳过循环，即使此时List中有很多元素也是如此
    //注意，如果一旦运行循环，在执行第一个同步的get之后，便会刷新size,作为下一次调用size()的值，所以客户需要准备好处理
    //在索引检查和元素访问之间size改变的情况，所以这种遍历方法是有问题的。
    public void unsafeOperation(){
        List<String>list=new ArrayList<>();
        for(int i=0;i<list.size();++i){
            System.out.println(list.get(i));
        }
    }
    //但是如果使用聚合或者迭代变量，因为他们都执行内部的同步操作，所以程序员可以使用非同步的size()方法和非volatile的size
    //并且声明这个方法这是大致估计了当前元素的个数，对于这种用户：他们获得的数据只要是上次同步的线程读或写之后的最新数据就可以了
    //聚合器：forEach遍历 for (String item : list)  迭代器：Iterator<String> iterator = list.iterator();
}
