package com.master._03javaMemoryModel;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: final域的重排序规则
 * @date 2022/12/8 9:11
 */
public class FinalExample {
    int i;//普通变量
    final int j;//final变量
    static FinalExample obj;//引用类型
    public FinalExample(){//构造函数
        i=1;//写普通域
        j=2;//写final域
    }
    public static void reader(){//读线程B执行
        FinalExample object=obj;//读对象引用
        int a=object.i;//读普通域
        int b=object.j;//读final域
    }
    public static void writer(){//写线程A执行
        obj=new FinalExample();//对象引用
    }
}
