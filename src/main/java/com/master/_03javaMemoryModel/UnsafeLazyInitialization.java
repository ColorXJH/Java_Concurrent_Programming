package com.master._03javaMemoryModel;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 不安全的懒加载初始化（双检查策略不安全）
 * @date 2022/12/8 11:44
 */
public class UnsafeLazyInitialization {
    private static Instance instance;
    //线程不安全的方法
    //假设A线程执行代码1的同时，B线程执行代码2。此时，线
    //程A可能会看到instance引用的对象还没有完成初始化
    public static Instance getUnsafeInstance(){
        if(instance==null){//1：A线程执行
            instance=new Instance();//2:B线程执行（可能开辟了内存，但是还没来得及初始化就返回了）
        }
        return instance;
    }
    //synchronized存在欣能开销-在不被多个线程频繁调用时，可以提供满意性能
    public synchronized  static Instance getSafeInstance(){
        if(instance==null){
            instance=new Instance();
        }
        return  instance;
    }

    //双检查-还是存在安全隐患
    //在线程执行到第4行，代码读取到instance不为null时，instance引用的对象有可能还没有完成初始化
    public static Instance doubleCheckLocking(){
        if(instance==null){//4 第一次检查
            synchronized (UnsafeLazyInitialization.class){//5 加锁
                if(instance==null){//6 第二次检查
                    instance=new Instance();//7 问题的根源出在这里
                }
            }
        }
        return instance;
    }
    //instance=new Instance();这一步用伪代码可以表示为：
    //memory=allocate();1:分配对象的内存空间
    //ctorInstance(memory);2:初始化对象
    //instance=memory;3:设置instance指向刚分配好的内存地址
        //问题的根源就是上面3行伪代码中的2和3之间，可能会被重排序，导致分配了内存地址却还未被初始化就返回了

    //以下为较好的安全方案（禁止重排序）
    private volatile static Instance instance2;
    public Instance safeDoubleCheckLocking(){
        if(instance2==null){
            synchronized (UnsafeLazyInitialization.class){
                if(instance2==null){
                    //当声明对象的引用为volatile后,3行伪代码中的2和3之间的重排序，在多线程环境中将会被禁止
                    //需要JDK5或更高版本，jdk5使用新的jsr-133内存模型规范，增强的volatile语义
                    instance2=new Instance();//instance2为volatile,现在没问题了
                }
            }
        }
        return instance2;
    }

    //基于类初始化的解决方案
    //JVM在类的初始化阶段（即在Class被加载后，且被线程使用之前），会执行类的初始化。在
    //执行类的初始化期间，JVM会去获取一个锁。这个锁可以同步多个线程对同一个类的初始化
    //基于这个特性，可以实现另一种线程安全的延迟初始化方案（这个方案被称之为Initialization On Demand Holder idiom）
    private static class InstanceHolder{
        public static Instance instance3=new Instance();
    }
    public static Instance getInstance3(){
        return InstanceHolder.instance3;//这里将导致InstanceHolder类被初始化
    }
}  //这个方案的实质是：允许3行伪代码中的2和3重排序，但不允许非构造线程“看到”这个重排序

//通过对比基于volatile的双重检查锁定的方案和基于类初始化的方案，我们会发现基于类
//初始化的方案的实现代码更简洁。但基于volatile的双重检查锁定的方案有一个额外的优势：
//除了可以对静态字段实现延迟初始化外，还可以对实例字段实现延迟初始化

//字段延迟初始化降低了初始化类或创建实例的开销，但增加了访问被延迟初始化的字段
//的开销。在大多数时候，正常的初始化要优于延迟初始化。如果确实需要对实例字段使用线程
//安全的延迟初始化，请使用上面介绍的基于volatile的延迟初始化的方案；如果确实需要对静
//态字段使用线程安全的延迟初始化，请使用上面介绍的基于类初始化的方案