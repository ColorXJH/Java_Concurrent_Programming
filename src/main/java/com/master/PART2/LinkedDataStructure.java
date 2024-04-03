package com.master.PART2;

/**
 * @author ColorXJH
 * @version 1.0
 * @description: 链表数据结构
 * @date 2024-04-02 17:22
 */
public class LinkedDataStructure {
}

//作为链表数据结构入口点的对象，类分解策略可以通过下面的方法使得对该对象的访问竞争达到最小：
    //在入口类的完全同步化（会限制并发数量）
    //链表中所有节点完全同步化（这种方式效率低，会有活跃性问题）
        //折中的方法：所有锁分解技术的主要目的都是把不同的锁和不同的方法联系起来，但是在链表数据结构中
        //通常会导致对数据结构和算法本身的进一步调整，下面的类有一些通用的技巧
            //LinkedQueue可以作为普通的无界先进先出的队列FIFO,他对put和poll操作分别维持同步，putLock的锁保证一个时候只能进行一个put操作
                //pollLock保证一个时刻只能poll一个元素，在这种应用中通常有一个头节点，使得put和poll可以独立进行
                //每一个poll之后，以前的第一个节点变成了新的头节点，put、poll同时对一个空队列或者即将为空的队列操作时，对于被访问的节点本身
                //需要加锁防止冲突，因为在这种情况下，head/last指向同一个头节点
                    //LinkedQueue    a（头节点）--》第一个节点--》下一个节点--》。。。。。  头节点poll,尾节点put
class LinkedQueue{
    //初始化创建头尾指针都不指向任何地方
    protected Node head=new Node(null);
    protected Node last=head;
    protected final Object pollLock=new Object();
    protected final Object putLock=new Object();
    public void put(Object x ){
        Node node=new Node(x);
        synchronized (putLock){
            synchronized (last){
                last.next=node;//添加一个新的节点
                last=node;//将新的节点标记为尾节点
            }
        }
    }

    public Object poll(){
        synchronized (pollLock){
            synchronized (head){
                Object x=null;
                Node first=head.next;//队列中第一个实际数据节点
                if(first!=null){
                    x=first.object;
                    head=first;//删除前一个数据节点，将head的next节点重新定义为head节点
                }
                return x;
            }
        }
    }

    static class Node{
        Object object;
        Node next=null;
        Node(Object x){object=x;}
    }
}
//只读适配器
    //有的情况下，对象不能泄露它的任何其他成员属性的标识，这一点对于任何存区访问或者属性检查的方法来说，都不能返回原始对象的引用

    //1：一个代替的方法是返回原始对象的拷贝，对象实现clone方法的时候可以返回x.clone();
    //2:很多情况下拷贝昂贵或者拷贝本身没有意义（例如存储本身不应该被拷贝的文件，线程或者其他资源的引用），
        //通过创建和返回x对象的适配器来选择性的适当允许一些遗漏，适配器一般只提供给外部不受干扰的相关操作，一般是只读操作
            //1：定义一个基本的interface,用来描述一些不变性的功能
            //2：作为可选功能，可以定义一个子类接口，来支持在通常可变的实现类中用于更新的方法，
            //3：定有一个只读的适配器，该适配器只传递接口中定义的操作，为了增加安全性可以把不变类声明为final
                    //使用final成员变量表明，当你认为拥有一个不变对象的时候，你确实就拥有了一个（它不会是一个支持可变操作的子类）

class InsufficientFunds extends Exception{

}
interface Account{
    long balance();
}
interface UpdatableAccount extends Account{
    void credit(long amount) throws InsufficientFunds;
    void debit(long amount)throws InsufficientFunds;

}
class UpdatableAccountImpl implements UpdatableAccount{
    private long currentBalance;

    public UpdatableAccountImpl(long currentBalance) {
        this.currentBalance = currentBalance;
    }

    @Override
    public synchronized long balance() {
        return currentBalance;
    }

    @Override
    public synchronized void credit(long amount) throws InsufficientFunds {
        if(amount>=0||currentBalance>=-amount){
            currentBalance+=amount;
        }
        else{
            throw new InsufficientFunds();
        }
    }

    @Override
    public synchronized void debit(long amount) throws InsufficientFunds {
        credit(-amount);
    }
}

final class ImmutableAccount implements Account{
    private Account delegate;

    public ImmutableAccount(long initBalance) {
        delegate = new UpdatableAccountImpl(initBalance);
    }
    ImmutableAccount(Account delegate){
        this.delegate=delegate;
    }

    @Override
    public long balance() {
        return delegate.balance();
    }
}
class AccountRecorder{
    public void recordBalance(Account a){
        System.out.println(a.balance());
    }
}

class AccountHolder{
    private UpdatableAccount acct=new UpdatableAccountImpl(0);
    private AccountRecorder recorder;

    public AccountHolder(AccountRecorder recorder) {
        this.recorder = recorder;
    }
    public synchronized void acceptMoney(long amount){
        try {
            acct.credit(amount);
            //通过将 UpdatableAccount 接口的实现类包装在 ImmutableAccount 中，
            //并且只提供只读的 balance() 方法，确保了外部类在调用 recordBalance 方法时只能观察到账户余额，而不能修改它。
            //这样，即使有恶意的继承类试图在 recordBalance 方法中对账户进行非法操作，也无法成功，
            //因为它们只能调用到 ImmutableAccount 提供的方法，而无法修改账户的状态。
            //这种设计方式符合面向对象编程的封装性原则，即隐藏对象的内部细节，并只暴露必要的接口供外部使用。
            //通过适配器模式和不可变对象的概念，可以有效地保护对象的状态，并防止外部类对其进行非法操作。
            recorder.recordBalance(new ImmutableAccount(acct));
        } catch (InsufficientFunds e) {
            System.out.println("cannot accept negative amount.");
        }

    }

}

class EvilAccountRecorder extends AccountRecorder{
    private long element;

    //继承AccountRecorder，重写该方法，扩展了行为
    @Override
    public void recordBalance(Account a) {
        super.recordBalance(a);
        //对传入的Account 对象进行一些额外的处理，而不影响到AccountHolder类
        if(a instanceof UpdatableAccount){
            UpdatableAccount updatableAccount=(UpdatableAccount) a;
            try {
                updatableAccount.debit(10);
                element+=10;
            } catch (InsufficientFunds e) {

            }
        }
    }
}
//EvilAccountRecorder 类试图在 recordBalance 方法中对传入的 Account 对象进行非预期的修改。
//具体来说，它尝试在记录账户余额时调用 debit 方法，这可能导致账户余额被减少，
//而原本的目的是仅仅记录余额而已。这种行为可能破坏了原有程序的逻辑，违反了面向对象编程的封装性原则，
//因为它修改了传入对象的状态而不是仅仅观察它。


//上述的适配器方法：recorder.recordBalance(new ImmutableAccount(acct));
    //在java.util.Collection框架结构中使用了这种方法的变体，由于主Collection接口允许可变方法抛出
    //UnsupportedOperationExceptions异常，所以这里没有声明单独的接口，对于所有试图进行数据更新的操作，
    //匿名只读适配器都会抛出异常，如下面的使用示例：
        //List list=new ArrayList();
        //untrustedObject.use(Collections.unmodifiableList(list))
























