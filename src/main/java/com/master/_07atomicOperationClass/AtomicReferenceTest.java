package com.master._07atomicOperationClass;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @ClassName: AtomicReferenceTest
 * @Package: com.master._07atomicOperationClass
 * @Description: 原子更新引用类型示例
 * @Datetime: 2023/11/23 20:56
 * @author: ColorXJH
 */
public class AtomicReferenceTest {
    static class User{
        private String name;
        private int old;
        public User(String name,int old){
            this.name=name;
            this.old=old;
        }

        public String getName() {
            return name;
        }

        public int getOld() {
            return old;
        }
    }



    public static AtomicReference<User> af=new AtomicReference<>();

    public static void main(String[] args) {
        User user=new User("ColorXJH",30);
        af.set(user);
        User updateUser=new User("KCY",32);
        af.compareAndSet(user,updateUser);
        System.out.println(af.get().getOld());
        System.out.println(af.get().getName());
    }
}

/**
 * 代码中首先构建一个user对象，然后把user对象设置进AtomicReferenc中，最后调用
 * compareAndSet方法进行原子更新操作，实现原理同AtomicInteger里的compareAndSet方法
 *
 */