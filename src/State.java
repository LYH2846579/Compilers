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
    private LinkedList<String> UndealValue;

    public State() {
    }
    public State(String name) {
        this.name = name;
        this.str = new LinkedList<>();
    }
    public State(String name, LinkedList<String> str) {
        this.name = name;
        this.str = str;
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
}
