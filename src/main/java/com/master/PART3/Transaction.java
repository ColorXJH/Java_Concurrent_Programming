package com.master.PART3;

import java.awt.*;
import java.beans.VetoableChangeListener;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-05-20 8:50
 */
public class Transaction {
    //事务参与者：一般的默认形式如下
    /**
     * ReturnType aMethod(Transaction t,ArgType args)throws ...
     */
    //接口
    class Failure extends Exception{}
    interface Transactor{
        public boolean join(TransactionNew transactionNew);
        boolean canCommit(TransactionNew transactionNew);
        void commit(TransactionNew transactionNew);//update state
        void abort(TransactionNew transactionNew);//rollback state
    }
    class TransactionNew{
        //增加任何你想增加的东西在这里
    }

    //实现：事务中的参与者必须同时支持事务参与者接口和描述其基本操作的接口
    interface TransBankAccount extends Transactor{
        long balance(TransactionNew transactionNew)throws Failure;
        void deposit(TransactionNew transactionNew,long amount)throws Failure;
        void withdraw(TransactionNew transactionNew,long amount)throws Failure;
    }

    class SimpleTransBankAccount implements TransBankAccount{
        protected long balance=0;
        protected long workingBalance=0;
        protected TransactionNew currentTx=null;
        @Override
        public synchronized boolean join(TransactionNew transactionNew) {
            if(transactionNew!=currentTx)return false;
            currentTx=transactionNew;
            workingBalance=balance;
            return true;
        }

        @Override
        public boolean canCommit(TransactionNew transactionNew) {
            return transactionNew==currentTx;
        }

        @Override
        public void commit(TransactionNew transactionNew) {
            if(transactionNew!=currentTx){return;}
            balance=workingBalance;
            currentTx=null;
        }

        @Override
        public void abort(TransactionNew transactionNew) {
            if(transactionNew==currentTx)currentTx=null;
        }

        @Override
        public synchronized long balance(TransactionNew transactionNew) throws Failure {
            if(transactionNew!=currentTx){throw new Failure();}
            return workingBalance;
        }

        @Override
        public synchronized void deposit(TransactionNew transactionNew, long amount) throws Failure {
            if(transactionNew!=currentTx)throw new Failure();
            if(workingBalance<-amount)throw new Failure();
            workingBalance+=amount;
        }

        @Override
        public synchronized void withdraw(TransactionNew transactionNew, long amount) throws Failure {
                deposit(transactionNew,-amount);
        }
    }

    //可否决改变
    class ColoredThing{
        protected Color myColor=Color.red;
        protected boolean changePending;
    }
}
