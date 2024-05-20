package com.master.PART3;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName: JoinOperation
 * @Package: com.master.PART3
 * @Description: 协同操作
 * @Datetime: 2024/5/18 19:45
 * @author: ColorXJH
 */
public class JoinOperation {
    //协同操作时这样一种原子的受保护方法：它包含的条件与操作是和多个不同的且相互独立的参与对象相关的，他们可以抽象的被描述为一个包含两个或者两个以上对象的原子方法
    /**
     * void joinAction(A a,B b){
     *     when(canPerformAction(a,b)){
     *         performAction(a,b)
     *     }
     * }
     */
    //通用解决方案：当参与者处于，或者可能出去，适合执行协同操作的状态时，他们彼此之间会互相通知，并且直到操作执行完，他们才会改变自己的状态
        //考虑如下例子：当支票账户的余额低于某个值时，服务会自动将钱从储蓄账户转移到支票账户，但是这只能发生在支票账户没有透支的情况下，操作的伪代码如下：
    /**
     * void autoTransfer(BankAccount checking,BankAccount saving,long threshold,long maxTransfer){
     *     when (checking.balance<threshold&&saving.balance>=0){
     *         long amount=saving.balance();
     *         if(amount>maxTransfer){amount=maxTransfer}
     *         saving.withdraw(amount);
     *         checking.deposit(amount);
     *     }
     * }
     * 解决方案依赖于下面这个类
     */
    class BankAccount{
        protected long balance=0;
        public synchronized long getBalance(){
            return balance;
        }
        public synchronized void despot(long amount)throws Exception{
            if(balance+amount<0){
                throw new Exception();
            }else{
                balance+=amount;
            }
        }

        public void withdraw(long amount) throws Exception{
            despot(-amount);
        }
    }

    //解决潜在的对称问题死锁的非定时让步：untimed back-off
    class TSBoolean{
        private boolean value=false;
        public synchronized boolean testAndSet(){
            boolean oldValue=value;
            value=true;
            return oldValue;
        }
        public synchronized void clear(){
            value=false;
        }
    }

    class ATCheckingAccount extends BankAccount{
        protected ATSavingAccount savings;
        protected long threshold;
        protected TSBoolean transferInProgress=new TSBoolean();
        public ATCheckingAccount(long t){
            threshold=t;
        }
        synchronized void initSavings(ATSavingAccount s){
            savings=s;
        }
        protected boolean shouldTry(){
            return balance<threshold;
        }
        void tryTransfer(){
            if(!transferInProgress.testAndSet()){
                try{
                    synchronized (this){
                        if(shouldTry()){balance+=savings.transferOut();}
                    }
                }finally {
                    transferInProgress.clear();
                }
            }
        }
        public synchronized void despot(long amount) throws Exception{
            if(balance+amount<0)throw new Exception();
            else{
                balance+=amount;
                tryTransfer();
            }
        }

    }
    class ATSavingAccount extends BankAccount{
        protected ATCheckingAccount checking;
        protected long maxTransfer;
        public ATSavingAccount(long max){
            maxTransfer=max;
        }
        synchronized void initChecking(ATCheckingAccount c){
            checking=c;
        }
        synchronized long transferOut(){
            long amount=balance;
            if(amount>maxTransfer)amount=maxTransfer;
            if(amount>=0)balance-=amount;
            return amount;
        }
        public synchronized void despot(long amount)throws Exception{
            if(balance+amount<0){throw new Exception();}
            else{
                balance+=amount;
                checking.tryTransfer();
            }
        }
    }

    //分离观察者
        //解决有关完全协同操作的设计和实现问题的最佳方法就是：不坚持要求跨越多个独立对象的操作为原子性操作，完全原子化很少是必要的，并可能导致其他后续设计问题，并使类难以使用或复用
        //参考设计模式中的观察者模式：Observer
        //一下为一个简单的实现：Subject仅仅用一个双精度数来代表其模拟的状态，使用CopyOnWriteArrayList类来维护Observer的列表，这就避免了在遍历观察者的时候锁定操作，同时也满足了设计的目的
        //为了简化示例，这里的Observer被定义为一个具体的类而不是接口，，且只能处理一个subject
    class Subject{
        protected double val=0.0;
        protected final CopyOnWriteArrayList observers=new CopyOnWriteArrayList();
        public synchronized double getVal(){
            return val;
        }
        public synchronized void setVal(double d){
            this.val=d;
        }
        public void attach(Observer o){
            observers.add(o);
        }
        public void detach(Observer o){
            observers.remove(o);
        }
        public void changeValue(double newState){
            setVal(newState);
            for(Iterator it=observers.iterator(); it.hasNext();){
                ((Observer)(it.next())).changed(this);
            }
        }
    }

    class Observer{
        protected double cacheState;
        protected final Subject subject;
        public Observer(Subject subject){
            this.subject=subject;
            cacheState=subject.getVal();
            display();
        }
        public synchronized void changed(Subject subject){
            if(subject!=this.subject){return;}
            double oldState=cacheState;
            cacheState=this.subject.getVal();
            if(oldState!=cacheState){
                display();
            }
        }
        protected  void display(){
            System.out.println(cacheState);
        }
    }





























}
