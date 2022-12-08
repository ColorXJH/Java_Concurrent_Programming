package com.master._03javaMemoryModel;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: final域为引用类型
 * @date 2022/12/8 9:46
 */
public class FinalReferenceExample {
    final int[] intArray; //final是引用类型
    static FinalReferenceExample obj;
    public FinalReferenceExample(){//构造函数
        intArray=new int[1];//1
        intArray[0]=1;//2
    }
    //1和3不能重排序，2和3也不能重排序
    public static void writeOne(){//写线程A
        obj=new FinalReferenceExample();//3
    }
    public static void writeTwo(){//写线程B
        obj.intArray[0]=2;//4
    }
    public static void reader(){//读线程C
        if(obj!=null){//5
            int temp=obj.intArray[0];//6
            System.out.println(temp);
            System.out.println("okkk---");
        }
    }
    //对上面的示例程序，假设首先线程A执行writerOne()方法，执行完后线程B执行
    //writerTwo()方法，执行完后线程C执行reader()方法

    //JMM可以确保读线程C至少能看到写线程A在构造函数中对final引用对象的成员域的写
    //入。即C至少能看到数组下标0的值为1。而写线程B对数组元素的写入，读线程C可能看得到，
    //也可能看不到。JMM不保证线程B的写入对读线程C可见，因为写线程B和读线程C之间存在数
    //据竞争，此时的执行结果不可预知。

}

