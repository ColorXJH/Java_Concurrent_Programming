package com.master._04javaConcurrentProgrammingBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName: DefaultThreadPool
 * @Package: com.master._04javaConcurrentProgrammingBase
 * @Description: 自定义线程接口的默认实现
 * @Datetime: 2023/11/12 13:23
 * @author: ColorXJH
 */
public class DefaultThreadPool<Job extends Runnable>implements ThreadPool<Job> {
    //线程池最大限制数
    private static final int MAX_WORKER_NUMBERS=10;
    //线程池默认的数量
    private static final int DEFAULT_WORKER_NUMBERS=5;
    //线程池最小数量
    private static final int MIN_WORKER_NUMBERS=1;
    //这是一个工作列表，会向里面插入工作
    private final LinkedList<Job>jobs=new LinkedList<Job>();
    //工作者列表
    private final List<Worker> workers= Collections.synchronizedList(new ArrayList<Worker>());
    //工作者线程的数量
    private int workerNum=DEFAULT_WORKER_NUMBERS;
    //线程编号生成
    private AtomicLong threadNum=new AtomicLong();

    public DefaultThreadPool(){
        initializeWorkers(DEFAULT_WORKER_NUMBERS);
    }
    public DefaultThreadPool(int num){
        //三目运算符，工作者线程的数量限制在max-min之间
        workerNum=num>MAX_WORKER_NUMBERS?MAX_WORKER_NUMBERS:num<MIN_WORKER_NUMBERS?MIN_WORKER_NUMBERS:num;
        initializeWorkers(workerNum);
    }
    @Override
    public void execute(Job job) {
        if(job!=null){
            //添加一个工作，然后进行通知
            synchronized (jobs){
                jobs.addLast(job);
                //对工作队列jobs调用了其notify()方法，而不是notifyAll()方法，因为能够确定有工作者线程被唤醒
                //使用notify()方法将会比notifyAll()方法获得更小的开销（避免将等待队列中的线程全部移动到阻塞队列中）
                jobs.notifyAll();
            }
        }
    }

    @Override
    public void shutdown() {
        for(Worker worker:workers){
            worker.shutdown();
        }
    }

    @Override
    public void addWorkers(int num) {
        synchronized (jobs){
            //限制新增的Worker数量不能超过最大值
            if(num+this.workerNum>MAX_WORKER_NUMBERS){
                num=MAX_WORKER_NUMBERS-this.workerNum;
            }
            initializeWorkers(num);
            this.workerNum+=num;
        }
    }

    @Override
    public void removeWorkers(int num) {
        synchronized (jobs){
            if(num>=this.workerNum){
                throw new IllegalArgumentException("beyond workNum");
            }
            //按照给定数量停止Worker
            int count=0;
            while (count<num){
                Worker worker=workers.get(count);
                //如果包含
                if(workers.remove(worker)){
                    worker.shutdown();
                    count++;
                }
            }
            this.workerNum-=count;
        }
    }

    @Override
    public int getJobSize() {
        return jobs.size();
    }

    //初始化线程工作
    private void initializeWorkers(int workerNum){
        for (int i = 0; i < workerNum; i++) {
            Worker worker=new Worker();
            workers.add(worker);
            Thread thread=new Thread(worker,"ThreadPool-Worker-"+threadNum.incrementAndGet());
            thread.start();
        }
    }

    //工作者，负责消费任务
    class Worker implements Runnable{
        //是否工作
        private volatile boolean running=true;
        @Override
        public void run() {
            while (running){
                Job job=null;
                synchronized (jobs){
                    //如果工作者列表是空的，那么就wait
                    while (jobs.isEmpty()){
                        try {
                            jobs.wait();
                        } catch (InterruptedException e) {
                            //感知到外部对WorkerThread的中断操作，返回
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                    //去除一个Job
                    job=jobs.removeFirst();
                }
                if(job!=null){
                    try {
                        job.run();
                    } catch (Exception e) {
                        // 忽略Job执行中的Exception
                        //throw new RuntimeException(e);
                    }
                }
            }
        }

        public void shutdown(){
            running=false;
        }
    }

}
