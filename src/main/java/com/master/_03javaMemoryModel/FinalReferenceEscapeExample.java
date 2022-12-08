package com.master._03javaMemoryModel;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: final引用不能从构造函数中逸出
 * @date 2022/12/8 10:43
 */

/**
写final域的重排序规则可以确保：在引用变量为任意线程可见之前，该
        引用变量指向的对象的final域已经在构造函数中被正确初始化过了。其实，要得到这个效果，
        还需要一个保证：在构造函数内部，不能让这个被构造对象的引用为其他线程所见，也就是对
        象引用不能在构造函数中“逸出”
 */
public class FinalReferenceEscapeExample {
    final int i;
    static FinalReferenceEscapeExample obj;
    public FinalReferenceEscapeExample(){
        i=1;//1 写final域
        obj=this;//2 this引用在此“溢出”
    }
    public static void writer(){
        obj=new FinalReferenceEscapeExample();
    }

    public static void reader(){
        if(obj!=null){//3
            int temp=obj.i; //4
        }
    }
}
//假设一个线程A执行writer()方法，另一个线程B执行reader()方法。这里的操作2使得对象
//还未完成构造前就为线程B可见。即使这里的操作2是构造函数的最后一步，且在程序中操作2
//排在操作1后面，执行read()方法的线程仍然可能无法看到final域被初始化后的值，因为这里的
//操作1和操作2之间可能被重排序

//在构造函数返回前，被构造对象的引用不能为其他线程所见，因为此
//时的final域可能还没有被初始化。在构造函数返回后，任意线程都将保证能看到final域正确初
//始化之后的值