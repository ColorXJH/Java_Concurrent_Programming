package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description:
 * @date 2024-02-26 14:06
 */
public class VisitorPattern {
    //在设计模式一书中，访问者模式（visitor）扩展了迭代遍历的含义，支持客户对按任意方式互相连接的对象集执行操作
//这是可以使用某种树或图的形式来组织节点，访问者模式也支持每个节点的多态操作

//访问者和其他一些具有广义的、遍历概念的实现策略和考虑与最简单的迭代变量的实现思路相似，或可以简化为这样的思路，
//例如，程序员可以首先创建一个具有节点的列表，然后使用任何一种上面提到的遍历技术来遍历该列表，然而这里的锁只能锁住列表
//而不能锁住节点本身，通常这是最好的方法，但是程序员要想确保在遍历过程中，所有的节点都被锁住，则应该考虑容器锁，和限制的方式
//相反，如果遍历通过每个节点都支持nextNode来实现，并且程序员不想同时占用所有节点的所有锁，那么在处理下一个节点之前，每一个节点的锁都要被释放


    //访问者模式客户端代码
    public static void main(String[] args) {
        Element elementA=new ConcreteElementA();
        Element elementB=new ConcreteElementB();
        Visitor visitor=new ConcreteVisitor();
        elementA.accept(visitor);
        elementB.accept(visitor);
        //visitor.visit((ConcreteElementA)elementA);
        //visitor.visit((ConcreteElementB)elementB);
    }


}

//定义元素接口
interface Element{
    void accept(Visitor visitor);
}
//具体元素a
class ConcreteElementA implements Element{

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    public void operationA(){
        System.out.println("ConcreteElementA.operationA");
    }
}
//具体元素b
class ConcreteElementB implements Element{

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
    public void operationB(){
        System.out.println("ConcreteElementB.operationB");
    }
}
//定义访问者接口
interface Visitor{
    void visit(ConcreteElementA elementA);
    void visit(ConcreteElementB elementB);
}
//具体访问者类
class ConcreteVisitor implements Visitor{

    @Override
    public void visit(ConcreteElementA elementA) {
        elementA.operationA();
    }

    @Override
    public void visit(ConcreteElementB elementB) {
        elementB.operationB();
    }
}