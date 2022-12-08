package com.master._03javaMemoryModel;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:  重排序对多线程执行效果的影响
 * @date 2022/12/7 8:23
 */
public class RecorderExample {
    int count=0;
    int a =0;
    boolean flag=false;
    public void writer() throws InterruptedException {
        Thread.sleep(100);
        a=1;
        flag=true;
    }
    public void reader(){
        if(flag){
            System.out.println(++count);
            System.out.println("enter");
            int i=a*a;
            System.out.println(i);
        }
    }
}
