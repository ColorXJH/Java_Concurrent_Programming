package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 子类控制父类的静态数据
 * @date 2024-02-23 9:08
 */
public class Sync {
}

class Parent{
    protected static int staticData;
    //静态同步块
    static{
        synchronized (Parent.class){
            staticData=0;//初始化静态数据
        }
    }
}
class Child extends Parent{
    public static void accessData(){
        //这里如果改成getClass(),如果有一个Child类的实例调用了accessData方法，他会使用Child类的类对象作为锁对象
        //但是如果在其他地方也有另一个子类AnotherChild，并且也调用了相同的方法，那么这两个子类实例会锁住不同的类对象
        //getClass()表示的是方法实际对象的类对象，实际调用方法可能存在父子类关系，或者兄弟关系
        synchronized (Parent.class){
            //访问和修改父类的静态数据
            int data=Parent.staticData;
            //执行其他操作。。。
        }
    }
}
//JVM在类装载和初始化的时候为Class类自动申请和释放锁，除非程序员自己编写了一个ClassLoader类，在静态初始化过程中有多个锁，
//否则这种内部机制不会影响作用于Class对象的普通方法和块的同步机制，除此之外，jvm再没有什么行为会使用到程序员创建和使用的锁
//尽管如此，当程序员创建java.*的子类时还是要格外小心这些类中锁原则

