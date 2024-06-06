package com.master.PART4;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 并行分解
 * @date 2024-06-05 16:19
 */
public class ParallelDecomposition {
    //着重三个基于任务的设计系列：分支/合并fork/join  计算树computation tree 关卡barrier

    //分支/合并 分而治之计数的并行版本，伪代码如下
    /**
     * class Solver{
     *     Result solve(Param problem){
     *         if(problem.size<basicSize)
     *           return directlySolve(problem)
     *         else{
     *             Result l,r;
     *             in-parallel{
     *                 l=solve(leftHalf(problem));
     *                 r=solve(rightHalf(problem));
     *             }
     *             return combine(l,r);
     *         }
     *     }
     * }
     */
    abstract class FJTask implements Runnable{
        abstract boolean isDone();
        abstract void cancel();
        abstract void fork();
        abstract void start();
        abstract void yield();
        abstract void join();
        abstract void invoke(FJTask task);
        abstract void coInvoke(FJTask a,FJTask b);
        abstract void conInvoke(FJTask[] tasks);
        abstract void reset();
    }

    //具体方法如下
        //1：使用下面的属性创建一个任务类
            //1：保存参数和结果字段，他们中的大多数应该被严格的控制在一个任务的内部，永远不要从其他任务访问这些字段，这可以消除在使用他们的过程中对同步的需求
                //但是在典型的情况下，其他任务会访问它的结果变量，这些变量要么被声明为volatile,要么只能通过synchronized访问
            //2：一个初始化参数变量的构造函数
            //3：一个运行通过改写方法代码而得到的run方法
        //2：替换递归：
            //1：创建子任务对象
            //2：派生fork每一个子任务，并让他们并行运行
            //3：主任务连接join每一个子任务
            //4：通过访问每一个子任务的结果变量组合最后的结果
        //3：用阈值检查替代或扩展原来的判断是否为基本操作的检擦
        //4：用一个创建相关任务的方法替代原来的方法，等它运行结束后返回结果
    //斐波那契
    /**
     * int setFib(int n){
     *     if(n<=1) return n;
     *     else
     *       return setFib(n-1)+setFib(n-2)
     * }
     */
    //上述斐波那契可以转换为一个如下的任务类
    class Fib extends FJTask{
        final int sequenceThreshold=13;
        volatile int number;
        Fib(int n){
            number=n;
        }
        int getAnswer(){
            if(!isDone())throw new IllegalStateException("not yet computed");
            return number;
        }
        int setFib(int n){
            if(n<=1) return n;
            else return setFib(n-1)+setFib(n-2);
        }
        @Override
        public void run() {
            int n=number;
            if(n<=sequenceThreshold) number=setFib(n);
            else{
                Fib f1=new Fib(n-1);
                Fib f2=new Fib(n-2);
                coInvoke(f1,f2);
                number=f1.number+f2.number;
            }
        }

        /*void init(){
            try{
                int groupSize=2;
                int num=3;
                //......
            }catch (InterruptedException ie){

            }
        }*/
        @Override
        boolean isDone() {
            return false;
        }

        @Override
        void cancel() {

        }

        @Override
        void fork() {

        }

        @Override
        void start() {

        }

        @Override
        void yield() {

        }

        @Override
        void join() {

        }

        @Override
        void invoke(FJTask task) {

        }

        @Override
        void coInvoke(FJTask a, FJTask b) {

        }

        @Override
        void conInvoke(FJTask[] tasks) {

        }

        @Override
        void reset() {

        }
    }
}
