import java.util.LinkedList;

/**
 * @author LYHstart
 * @create 2021-11-09 17:08
 *
 * 用以存储状态的类
 * 其内部要保存这个状态所对应的所有项 -> 如何存储? 以字符串链表形式? -> 试一试
 */
public class State
{
    //状态名
    private String name;
    //项存储链表
    private LinkedList<String> str;
    //未处理的变量链表
    private LinkedList<String> list1;
    //已经处理的变量链表
    private LinkedList<String> list2;

    public State() {
    }
    public State(String name) {
        this.name = name;
        this.str = new LinkedList<>();
        this.list1 = new LinkedList<>();
        this.list2 = new LinkedList<>();
    }
    public State(String name, LinkedList<String> str, LinkedList<String> list1, LinkedList<String> list2) {
        this.name = name;
        this.str = str;
        this.list1 = list1;
        this.list2 = list2;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public LinkedList<String> getStr() {
        return str;
    }
    public void setStr(LinkedList<String> str) {
        this.str = str;
    }
    public LinkedList<String> getList1() {
        return list1;
    }
    public void setList1(LinkedList<String> list1) {
        this.list1 = list1;
    }
    public LinkedList<String> getList2() {
        return list2;
    }
    public void setList2(LinkedList<String> list2) {
        this.list2 = list2;
    }

    @Override
    public String toString() {
        return "State{" +
                "name='" + name + '\'' +
                ", str=" + str +
                ", list1=" + list1 +
                ", list2=" + list2 +
                '}';
    }
}

