package com.master.PART2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-02-23 14:44
 */
public class LockTest {


//基于锁的最安全的（但不一定时最好的）并发面向对象设计策略是，
    //1：所有的方法都是同步的
    //2：没有公共成员变量，或者而其他的封装问题
    //3：所有的方法都是有限的，没有无线循环或者而是无休止的递归，所以最终都会释放锁
    //4：所有成员变量在构造函数已经初始化为一个稳定的状态
    //5：在一个方法开始和结束的时候，对象的状态都应该稳定一致，或者而遵守不变性，即使出现了异常情况也该如此

//遍历：遍历就是对集合中的每一个元素执行一些相应的操作或者逐个使用，因为集合操作的元素可能无限多，所以把集合中的每个方法都设置为synchronized是没有意义的
    //对于这个设计问题一般有三种解决方法：1：聚集操作，2索引化遍历 3：版本化迭代遍历

    //1. 聚集操作 (Aggregation Operation):
    //聚集操作是指对集合中的元素执行某些操作，例如计算总和、平均值等。
    //Java 8 中的并行流和串行流提供了一种方便的方式来执行聚集操作，并且会自动处理线程安全性。
    public void aggregation(){
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        // 使用并行流求和
        int sum = numbers.parallelStream().mapToInt(Integer::intValue).sum();
        System.out.println("Sum: " + sum);
    }
    //2. 索引化遍历 (Index-based Traversal):
    //索引化遍历是指通过索引来访问集合中的元素。在索引化遍历过程中，如果没有修改集合的结构，则是线程安全的。
    //但是如果在遍历期间修改了集合的结构（例如添加或删除元素），可能会导致 ConcurrentModificationException 异常。
    //下面是一个简单的示例：
    public void indexBasedTraversal(){
        List<String> list = new ArrayList<>();
        list.add("apple");
        list.add("banana");
        list.add("orange");

        // 索引化遍历
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i));
        }

    }
    //3. 版本化迭代遍历 (Versioned Iterative Traversal):
    //版本化迭代遍历是指使用迭代器来遍历集合。迭代器内部维护了一个版本号，用于检测在迭代过程中是否有结构性修改。
    //如果在迭代期间修改了集合的结构，则会抛出 ConcurrentModificationException 异常
    //下面是一个示例：
    public void versionedIterativeTraversal(){
        List<String> list = new ArrayList<>();
        list.add("apple");
        list.add("banana");
        list.add("orange");

        // 使用迭代器遍历
        Iterator<String> iterator = list.iterator();
        while (iterator.hasNext()) {
            String item = iterator.next();
            System.out.println(item);
        }

    }
}
