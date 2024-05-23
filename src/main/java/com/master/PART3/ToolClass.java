package com.master.PART3;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 工具类的实现
 * @date 2024-05-20 17:23
 */
public class ToolClass {
    //如何在普通的接口上添加获得-释放（acquire-release）协议，随后通过例子演示了如何使用协同操作的技巧来将类分割
    //以获得必要的并发控制，并在随后将其合并以提交性能，最后讨论如何将等待的线程隔离以管理通知

    //1：获得-释放 协议
    interface Sync{
        void acquire()throws InterruptedException;
        void release();
        boolean attempt(long times)throws InterruptedException;
    }

    class Semaphore implements Sync{
        protected long permits;//current available permits
        public Semaphore(long permits){
            this.permits=permits;
        }

        @Override
        public void acquire() throws InterruptedException {
            if(Thread.interrupted()){
                throw new InterruptedException();
            }
            synchronized (this){
                try {
                    while (permits<=0)wait();
                    permits--;
                }catch (InterruptedException ie){
                    notify();
                    throw ie;
                }
            }
        }

        @Override
        public synchronized void release() {
            permits++;
            notify();
        }

        @Override
        public boolean attempt(long times) throws InterruptedException {
            if(Thread.interrupted())throw new InterruptedException();
            synchronized (this){
                if(permits>0){
                    permits--;
                    return true;
                }
                else if(times<=0){
                    return false;
                }else{
                    try {
                        long startTime=System.currentTimeMillis();
                        long waitTime=times;
                        for(;;){
                            wait(waitTime);
                            if(permits>0){
                                permits--;
                                return true;
                            }
                            else{
                                long now=System.currentTimeMillis();
                                //是否是等待后立刻唤醒并重新获得锁，如果是0或者负数说明等待至少整个时间段，如果大于0说明提前抢到锁，
                                //但是没有许可证，接着等待剩余的时间，如果一直没到就返回false
                                waitTime=times-(now-startTime);
                                if(waitTime<=0) return false;
                            }
                        }
                    }catch (InterruptedException ie){
                        notify();
                        throw ie;
                    }
                }
            }
        }
    }
    //委托操作
    //将不同的等待条件委托给不同的辅助对象，适用于纯等待与通知机制
        //1：由于辅助类必须访问公共的状态，所以无法完全把每个辅助类按自包含方式隔离起来，在辅助类之间，对公共状态的不同访问必须被正确的同步
        //2：每个可能影响其他辅助类的受保护条件的辅助类，必须提供对这种影响的有效通知，以避免活跃性问题
        //3：和wait机制有关的辅助方法的同步，必须注意避免出现嵌套的监视器问题，

    /**
     * void doM()throws InterruptedException{
     *     for(;;){
     *         synchronized(this){
     *             synchronized(rep){
     *                 boolean canAct=inRightState(rep);
     *                 if(canAct){
     *                     update(rep);
     *                     break;
     *                 }
     *             }
     *             wait();
     *         }
     *     }
     *     host.signalChange();
     * }
     *
     */

    public static void main(String[] args) {
        System.out.println((System.currentTimeMillis()-1716438485000l)/1000/60);
    }
    //有界缓冲区
        //将辅助对象分为分别执行put和take的，对执行put操作的辅助类，计数器的值从数组的容量开始，对于执行take操作的辅助类
        //计数器的值是从0开始的，exchange只有在存储槽的数量大于0的时候才可以执行
}
