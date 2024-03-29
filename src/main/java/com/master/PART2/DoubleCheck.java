package com.master.PART2;

/**
 * @ClassName: DoubleCheck
 * @Package: com.master.PART2
 * @Description:双重检查
 * @Datetime: 2024/3/29 20:23
 * @author: ColorXJH
 */
public class DoubleCheck {
    //在对一个非同步成员变量进行访问时，如果调用者发现读到了非法成员变量的时候，可以采取相应的措施，这样的措施之一就是在同步的环境下重新访问这个成员
    //变量，判断一下他的最新值，然后采取恰当的行动，这就是双重检查的思想精髓：测试-》然后-》测试-》然后-》设置
        //应用：有条件的放宽初始化检查的同步操作：如果遇到一个没有初始化的值，访问方法需要一把锁来同步检查一下初始化工作是否必须，而不是仅仅读取陈旧的值
        //并在同步锁下执行初始化操作，以防止多次初始化：如下
    int notInitializedValue;//默认的0是非法值
    void animate(){
        if(notInitializedValue==0){//非同步检查
            synchronized (this){
                if(notInitializedValue==0){
                    notInitializedValue=100;
                    System.out.println("这才进入到真正的初始化");
                }
            }
        }
        //接下来使用这个初始化后的值 notInitializedValue，执行到这里表示一定被初始化过了
    }
}

//注意：1:一般来说对数组或对象使用双重检查是不明智的，对于没有同步的读操作来说，引用的可见性不能保证从引用访问的非volatile对象的可见性
            //双重检查锁定是一种常见的多线程编程技术，用于在并发环境下延迟初始化对象，并保证在多线程环境下对象只被初始化一次。
            //在双重检查锁定中，程序先检查对象是否已经被初始化，如果没有初始化，则对对象进行加锁，并再次检查对象是否已经被初始化。这种方式可以在大多数情况下工作正常，但是在某些情况下会存在问题。
            //其中一个问题就是可见性问题。当一个线程对对象进行初始化后，其他线程可能无法立即看到对象的变化，因为缓存和寄存器的值可能与主内存中的值不一致。如果其他线程在初始化完成之前访问了这个对象，就可能会得到一个未完全初始化的对象，从而导致不可预期的行为。
            //另外，双重检查锁定还存在指令重排序的问题。即使对象已经被初始化，但是在对象的引用被返回之前，可能会被其他线程访问到，并且在构造函数执行完毕之前，这个对象可能处于一个不一致的状态。
            //因此，针对数组或对象的双重检查锁定不是一个安全的做法。如果确实需要延迟初始化对象，并且需要保证多线程安全，可以考虑使用更安全的并发编程技术，比如使用线程安全的容器类、volatile关键字、synchronized关键字或者使用并发工具类中提供的线程安全机制。
     //2:只是用一个标量成员变量来指示所有必须被初始化的成员变量是很难的，顺序化重新排序会导致其他成员变量在还没有初始化之前，标量本身就已经被设置为初始化了
            //假设有一个类，其中包含一个布尔类型的成员变量 initialized 用来指示其他成员变量是否已经初始化。在某些情况下，编译器或者运行时可能会对代码进行优化，导致初始化标志 initialized 被设置为 true，但是其他成员变量还没有被正确初始化。
            //这种情况下，即使标志 initialized 已经被设置为 true，其他成员变量仍然可能处于未初始化的状态。这就会导致程序的不一致性和不确定性，因为其他线程可能在使用这些未初始化的成员变量时发生错误。
            //因此，仅仅依靠一个标量成员变量来指示所有必须被初始化的成员变量是不可靠的，因为顺序化重新排序可能会导致标量成员变量在其他成员变量初始化之前就被设置为初始化了。
    //以上两个问题的补救方法是使用锁或者及时加载不适用懒加载，使用尽早的初始化机制，或是完全同步的检查



class ServerWithStateUpdate{
    protected double state;
    protected final Helper helper=new Helper();
    protected  Helper helper2=new Helper();
    public synchronized void service(){
        state=getState();
        helper.operation();//如果这个操作需要很长时间,那么像调用getState这样的synchronized操作就会一直被阻塞，等待这个方法可用（等待获取锁）
        //如果上述的operation操作会影响或者改变state,则必须同步，如果不影响则可以不同步，如service2方法
    }

    public void service2(){
        updateState();
        helper.operation();
    }

    public synchronized double getState(){
        return 3;
    }
    public synchronized void updateState(){
        state= 3;
    }
    //非同步的发送消息也成为开放调用，适合在并行和事件驱动的系统中作为部件
    //即使helper的成员变量是可变的，也可以使用开放调用
    synchronized void setHelper(Helper h){
        helper2=h;
    }
    public void service3(){
        Helper h;
        synchronized (this){
            state=3;
            h=helper2;
        }
        h.operation();
    }
    //对上述描述：
       //1:JAVA内存模型
        //在Java中，对象引用和对象本身是分开存储的。
        //当你创建一个对象时，实际上在堆内存中分配了一块内存空间，并返回该对象的引用。
        //这个引用存储在栈内存中，而对象本身存储在堆内存中。
        //因此，对象的引用传递是值传递，传递的是引用的副本，而不是对象本身。
      //2:线程同步问题
        //当调用 setHelper(Helper h) 方法时，将会在堆内存中创建一个 Helper 对象，并将对象的引用存储在 helper2 中。这个操作发生在堆内存中。
        //在 service3() 方法中，首先声明了一个 Helper 类型的变量 h。然后进入同步块，在同步块中，state 被设置为 3，同时将 helper2 的值赋给了 h。由于同步块中获取了对象锁，因此可以确保在获取 helper2 的值时，对于当前线程是可见的。这个操作也发生在堆内存中。
        //当调用 h.operation() 时，会使用 h 中存储的对象引用来调用 operation() 方法。即使在其他线程中修改了 helper2 的值，也不会影响 h 中存储的对象引用，因为引用传递的是值的副本，h 持有的是在同步块中获取的 helper2 的值的副本，而不是 helper2 本身。因此，即使 helper2 的值发生了变化，h 引用的对象不会受到影响。
    //综上所述，即使在其他线程中修改了 helper2 的值，在当前线程中获取 h 引用之后，h 引用指向的对象不会受到影响，因为引用传递的是值的副本，而不是对象本身
    public synchronized void synchronizedService3(){
        service3();
    }
    //上述代码展示了使用开放调用的弱点，synchronizedService3方法内调用service3(),会导致整个service3执行过程中锁都会被占用，包括对h.operation()调用的时候，因为进入service3方法前已经获取了对象锁，这违背了开放调用的目的（非同步调用）

    //但是通过不变引用连接成的数据结构通常适用于这种操作，例如有一个LinkedCell类，类的每个单元cell,都包含对他后续单元的引用，并且对于这个类来说，我们要求在创建时就固定了对后续单元的引用，
    //有关后续单元的方法或者方法的一部分不需要同步，这样做可以加快遍历的速度，下面以递归展示（迭代也可以），如下LinkedCell类
}

class Helper{
    public void operation(){};
}


class LinkedCell{
    protected int value;
    protected final LinkedCell next;
    //这种不变连接构成的数据是可以开放调用（非同步调用、部分同步调用）
    public LinkedCell(int v,LinkedCell cell){
        value=v;
        next=cell;
    }

    public synchronized int getValue(){
        return value;
    }

    public synchronized void setValue(int v){
        value=v;
    }

    public int sum(){
        return (next==null)?getValue():getValue()+ next.sum();
    }

    public boolean includes(int x){
        return (getValue()==x)?true:(next==null)?false: next.includes(x);
    }

    //再次强调，当一个同步方法调用非同步方法时，对象还处在被锁状态，在下面的代码中，适用sum方法时，仍然处于同步状态
    synchronized int invokeUnSyncMethodSum(){//bad idea  不要将同步锁传递给非同步方法

        return value+nextSum();//sync still on call
    }
    int nextSum(){
        return (next==null)?0:next.sum();
    }

    //上述代码的注释指出了一个不良的编程实践。在 invokeUnSyncMethodSum 方法中使用了 synchronized 关键字修饰，使得该方法成为了同步方法。
    //在该方法内部调用了 nextSum 方法，而 nextSum 方法并没有使用 synchronized 关键字修饰，因此它是一个非同步方法。
    //虽然 invokeUnSyncMethodSum 方法内部调用了一个非同步方法，但调用的过程中仍然持有对象的锁，因为调用链仍然处于同步方法的调用上下文中。
    //这种情况可能导致在执行 nextSum 方法时出现阻塞，因为即使 nextSum 方法不是同步方法，但它仍然会被调用时的同步锁所限制。
    //这种编程方式通常被认为是不好的实践，因为它可能会导致性能问题和不必要的线程阻塞。最好的做法是在需要同步的地方使用 synchronized 关键字，而不是将同步锁传递给非同步方法。

    //深化理解：
        //nextSum 方法本身并没有锁，因此其他线程在没有同步块或同步方法限制的情况下，是可以访问它的。
        //但是，需要注意的是，尽管 nextSum 方法本身没有锁，但在被同步方法调用的上下文中，调用链仍然受到同步方法的影响。
        //也就是说，即使 nextSum 方法本身没有锁，但在 invokeUnSyncMethodSum 方法中调用它时，线程仍然需要获得对象的锁才能执行该调用。
        //这种情况下，虽然 nextSum 方法本身不会获得锁，但是调用链上的同步锁会影响到整个调用过程，可能会导致其他线程在获取对象锁之前被阻塞。
            //(比如此时你去访问getValue方法，就会被阻塞，因为整个上下文期间都获得了锁，其他的同步方法无法执行，虽然nextSum没有锁，但是整个调用过程上下文锁没有释放前其他同步方法都不能获取到锁)
        //所以，你的理解是正确的，nextSum 方法本身不会获得锁，但是在调用链上的同步锁会对整个调用过程产生影响。
}