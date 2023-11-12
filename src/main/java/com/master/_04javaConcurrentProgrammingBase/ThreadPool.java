package com.master._04javaConcurrentProgrammingBase;

/**
 * @ClassName: ThreadPool
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 自定义简单的线程池接口
 * @Datetime: 2023/11/12 12:59
 * @author: ColorXJH
 */
public interface ThreadPool<Job extends Runnable> {
    //执行一个job,这个job需要实现Runnable
    void execute(Job job);
    //关闭线程池
    void shutdown();
    //增加工作者线程
    void addWorkers(int num);
    //减少工作者线程
    void removeWorkers(int num);
    //得到正在等待执行的任务数量
    int getJobSize();
}

/**
 * 客户端可以通过execute(Job)方法将Job提交入线程池执行，而客户端自身不用等待Job的
 * 执行完成。除了execute(Job)方法以外，线程池接口提供了增大/减少工作者线程以及关闭线程
 * 池的方法。这里工作者线程代表着一个重复执行Job的线程，而每个由客户端提交的Job都将进
 * 入到一个工作队列中等待工作者线程的处理。
 *
 */