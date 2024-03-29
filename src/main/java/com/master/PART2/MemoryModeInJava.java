package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: java内存模型
 * @date 2024-03-05 15:13
 */
public class MemoryModeInJava {
}

final class SetCheck{
    private int a=0;
    private int b=0;
    void set(){
        a=1;
        b=-1;
    }
    //在串行化语言里 该方法永远不会返回false,即使编译器，运行系统及硬件可以用任何掉哟给代码，情况还是如此
    boolean check(){
        return ((b==0)||(b==-1&&a==1));
    }
    //以下为可能执行的路线
        //编译器可以重新安排语句的执行顺序，这样b就可以在a前面赋值，如果方法是内嵌的，方法还可以把其他语句重新排列
        //处理器可以改变这些语句的机器指令的执行顺序，甚至同时执行这些语句
        //存储系统（由于被缓存控制单元控制）也可以重新安排对应存储单元的写操作顺序，这些写操作可能和其他计算和存储操作同时发生
        //编译器、处理器和/或存储系统都可以把这两条语句的机器指令交叉执行，例如在一台32位机器上，可能先写b的高位，然后再写a,最后写b的低位
        //编译器、处理器和/或存储系统都可以使对应于变量的存储单元一直保留着原来的值，以某种方式维护者相应的值
            //(例如再cpu的寄存器中)，以保证代码正常运行，直到一个check调用才更新
            //通过管道超标量cpu,多层缓存，装载/存储平衡，和过程间寄存器分配策略等等，计算机执行速度都得到了迅速发展

    //java的编程模型围绕着以下三个相关的问题
        //原子性：指令必须有不可分割的特性，为了建模的目的，规则只需要阐述对代表成员变量的存储单元的简单读写操作，这里的成员变量可以是实例对象和静态变量
            //但是不包括方法中的局部变量
        //可见性：在什么情况下一个线程的效果对另一个线程是可见的，这里的效果是指写入成员变量的值对于这个成员变量的读出操作是可见的
        //顺序化：在什么情况下对一个线程来说操作可以是无序的，主要的顺序化问题围绕着和读写有关的赋值语句的顺序

    //程序员需要做的额外约束
        //对象必须对所有依赖于自己的线程维护不变约束，而不是只对修改该对象某个状态的线程保持不变约束

    //原子性
        //存取和更新除了long/double之外的任何类型的成员变量所对应的存储单元，他们都是原子的，其中包括引用其他对象的成员变量，另外原子性还能扩展到
            //volatile long/double类型（尽管非volatile的long/double类型不能保证原子性，但是他们在某些实现中也可以具有这种特性）

        //当在表达式中使用非long/double类型时，原子性保证程序员得到的数据或者是初始值或者是某个线程修改后的值，绝不是两个或多个线程同时修改而产生的混乱字节
            //但是，就像下面将要看到的，原子性本身并不能保证程序获得的值是线程最近修改的值，由于这个原因，原子性在本质上对并发程序的设计没有多大影响
    //可见性
        //只有在下面的情况中，线程对数据的修改对于另一个线程而言才确保是可见的
            //1：写线程释放了同步锁，而读线程获得了该同步锁
                //释放的时候强制的把线程所使用的工作存储单元的值刷新到主存，获得锁的时候要装载（或者重新装载）可访问成员变量的值，锁只为同步方法或者同步块
                //中操作提供独占，而它对存储的影响却包括了执行操作的线程使用的所有成员变量
            //2：如果一个成员变量被声明为volatile,那么在写线程做下一步存储操作之前，写入成员变量的数据在主存中刷新
            //3：如果一个线程访问一个对象的成员变量，那么线程读到的是初始值或者是被某个线程修改过的值
                //1:对还没有完全创建好的对象进行引用很不好-》不要再构造函数中创建线程调用该对象实例
                //2:多个线程对共同使用的对象引用需要设置访问可见性-》volatile成员变量或者synchronized所有方法
            //4：当一个线程结束的时候，所有的写入数据都将被刷新到主存中
        //存储模型保证：如果上述这些操作一定会发生，那么一个线程对成员变量的更新最终对另一个线程是可见的
        //但是“最终”可能是一段很长的时间，很难期盼一个不使用同步的线程和另一个能对成员变量的值一直保持同步，尤其要注意的是
        //如果成员变量不是volatile或者不是通过同步访问的，千万不要企图通过循环来等待另一个线程的写入操作
    public   void add(Test test){
        synchronized (test){
            test.x++;
        }

    }

    class Test{
        public int x=0;
    }
}
//volatile
    //在原子性 可见性 排序性方面，把一个数据声明为volatile几乎等价于使用一个小的完全同步类来保护通过get/set方法操作的成员变量
    //一个数据声明为volatile与同步的区别，只是没有使用锁，对于复合读写操作，其不具备原子性
    //排序性和可见性只能影响volatile成员变量本身，引用类型的volatile成员变量不能保证这个成员变量引用的非volatile数据的可见性
        //数组类型的volatile变量不能保证数组元素的可见性
    //没有使用锁，性能不会比同步慢，如果频繁使用volatile成员变量
    //如果仅仅需要保证在多线程之间正确的访问成员变量的值，就可以只需要声明变量为volatile,如下情况：
        //1：不需要和其他成员变量之间遵循不变约束
        //2：不需要根据当前值重写成员变量
        //3：没有线程会使用正常语法写入非法值
        //4：读线程的行为不依赖于任何非volatile成员变量的值