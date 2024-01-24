package com.master.PART1;

import java.awt.*;
import java.util.Random;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-01-23 12:28
 */
public class Particle {
    //一些规则来尽量避免死锁问题
    //1：永远只是在更新对象的成员变量时加锁
    //2：永远只是在访问有可能被更新对象的成员变量时才加锁
    //3：永远不要在调用其他对象的方法时加锁
    int x;
    int y;
    //final确保这个成员变量的引用不会被改变，这样一来就不会被我们的加锁规则所影响
    //许多并发程序中都大量的使用了final关键字，以帮助减少同步需要的设计意图
    final Random rnd=new Random();

    public Particle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public synchronized void move(){
        //在调用其他对象的方法时释放锁 这里似乎没有遵循这个规则，他调用了rnd.nextInt方法，
        //这么做是有原因的，每一个Particle都包含自身的rnd对象，它属于Particle对象的一部分，
        //不应该被看成时规则中描述的其他对象
        x+=rnd.nextInt(10)-5;
        y+= rnd.nextInt(20)-10;
    }

    public void draw(Graphics g){
        int lx,ly;
        synchronized (this){
            lx=x;
            ly=y;
        }
        //在调用其他对象的方法时释放锁
        g.drawRect(lx,ly,10,10);
    }
}
