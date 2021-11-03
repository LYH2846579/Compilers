import java.util.Objects;

/**
 * @author LYHstart
 * @create 2021-11-02 15:06
 *
 * 该类作为存储输入的字符类
 */
public class Word
{                            //(种属性,变量名,变量值)        a -> (Integer,a,0)
    //定义一些分析所用到的属性 -> 构建三元式 --> 这里对变量本身的值也进行了存储
    private String Type;
    private String name;
    private String value;

    public Word() {
    }
    public Word(String type, String name, String value) {
        Type = type;
        this.name = name;
        this.value = value;
    }

    public String getType() {
        return Type;
    }
    public void setType(String type) {
        Type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Word word = (Word) o;
        return Objects.equals(Type, word.Type) &&
                Objects.equals(name, word.name) &&
                Objects.equals(value, word.value);
    }
    @Override
    public int hashCode() {
        return Objects.hash(Type, name, value);
    }

    @Override
    public String toString() {
        return "Word{" +
                "Type='" + Type + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
