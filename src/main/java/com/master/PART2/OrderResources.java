package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 顺序化资源
 * 顺序化资源的思想是把每一个嵌套的synchronized方法或者块中使用的对象和数字标签（或者而其他可以排序的数据类型）关联起来
 * 如果同步操作是根据对象标签的最小最先原则（least-first）那么下面的现象就永远不会发生，一个线程拥有了x的同步锁正在等待yd的同步锁
 * 另一个线程拥有了y的同步锁，正在等待x的同步锁，他们都会以同样的顺序得到锁，从而避免发生死锁，更普遍一些，在开发设计中，为了打破对称
 * 或者强行设置优先次序，都可以使用顺序化资源的方式
 * @date 2024-03-05 11:03
 */
public class OrderResources {
    private long value;
    /**
     * 有些情况下我们会对使用的一系列锁指定特殊的顺序化规则，有的时候程序员也可以按照常规的标签实现锁的顺序化
     * 例如：System.identityHashCode的返回值，即使类本身已经覆盖了hashCode方法，这个方法在默认情况下还是直接调用hashCode方法
     * 为了进一步确保安全，程序员应该覆盖hashCode方法，或者在任何使用顺序化资源的类中使用其他的标签方法来保证唯一性
     * 例如为每一个对象分配一个串行数
      */

    //作为更深层次的检查方法，别名检查可以应用在使用了嵌套同步的方法中，用以处理绑定同一对象的两个或者多个引用情况，
    //在swapValue中可以检查Cell是否在和自己进行交换，严格来说这种检查方式在这里是可选的，同步锁访问是每线程而不是每调用的
    //对于已经获得其锁的对象再进行同步操作是可行的，但是这种别名检测方法可以进一步预防后续功能，效率的下降和基于同步的复杂性
    //一般在对两个或者多个对象同步之前使用这个方法，除非两个对象是独立、毫不相关的类型（因为两个不相关的对象的引用无论如何也不会是同一类型对象，所以无需检查）

    //如下，及使用了顺序化资源，又使用了别名检查
    public void swapValue(OrderResources others ){
        if(others==this){
            return ;//alias check
        }
        else if(System.identityHashCode(this)<System.identityHashCode(others)){
            this.swapValue(others);
        }else{
            others.swapValue(this);
        }
    }

    //作为提高效率的小小改进，我们可以进一步简化上述代码，其方法是：首先得到必要的锁，然后直接访问成员变量，这样可以避免在已经获得锁得情况下
    //再调用自己的synchronized方法，但是当成员变量的访问属性发生变化时，需要做相应的diamagnetic改动

    protected synchronized void doSwapValue(OrderResources other){
        //注意this的锁是通过上方synchronized方法限定得到的，other的锁却是显式的
        synchronized (other){
            long t=value;
            value=other.value;
            other.value=t;
        }
    }
    //获得锁的顺序问题无疑在使用嵌套同步的方法中非常突出，当一个同步方法获得一个对象的锁之后，
    //再调用另外一个对象的同步方法的时候，就会产生问题，但是再级联式调用的情况下，使用顺序化资源并没有什么优势
    //总的来说，对象不确定接下来哪个对象会被调用，也不知道他们是否需要同步，这就是为什么再开放系统中
    //当同步调用不释放锁时，死锁如此难以解决的原因之一

}
