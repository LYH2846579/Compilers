/**
 * @author LYHstart
 * @create 2021-11-17 19:28
 *
 * 为了实现语义处理的文法符号类
 */
public class Symbol
{
    //用以表示语法变量的符号值 -> 对应原来的String
    private String name;
    //存储自底向上规约的符号 -> F → d 则有F.code = d.name --> 对应于Token的name属性
    private String code;
    //一个可能用到的value
    private String value;  //使用value记录规约符号的三地址代码?
    //记录两个跳转地址
    private String trueAdd;
    private String falseAdd;
    //存储跳转语句!
    private String key;

    //用于控制流语句的信息
    private int tagIndex;


    public Symbol() {
    }
    public Symbol(String name) {
        this.name = name;
    }
    public Symbol(String name, String value, String code) {
        this.name = name;
        this.value = value;
        this.code = code;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getTrueAdd() {
        return trueAdd;
    }
    public void setTrueAdd(String trueAdd) {
        this.trueAdd = trueAdd;
    }
    public String getFalseAdd() {
        return falseAdd;
    }
    public void setFalseAdd(String falseAdd) {
        this.falseAdd = falseAdd;
    }
    public int getTagIndex() {
        return tagIndex;
    }
    public void setTagIndex(int tagIndex) {
        this.tagIndex = tagIndex;
    }
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
}
