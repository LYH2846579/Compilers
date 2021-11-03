import java.util.Objects;

/**
 * @author LYHstart
 * @create 2021-11-03 20:05
 *
 * 该类作为符号表中的内容，使用HashMap作为符号表的搭建结构，便于查找
 */
public class TableNode
{
    private Integer i;
    private Float f;
    private String name;
    private Word word;

    public TableNode() {
    }
    public TableNode(Integer i, Float f, String name,Word word) {
        this.i = i;
        this.f = f;
        this.name = name;
        this.word = word;
    }

    public Integer getI() {
        return i;
    }
    public void setI(Integer i) {
        this.i = i;
    }
    public Float getF() {
        return f;
    }
    public void setF(Float f) {
        this.f = f;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Word getWord() {
        return word;
    }
    public void setWord(Word word) {
        this.word = word;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableNode tableNode = (TableNode) o;
        return Objects.equals(i, tableNode.i) &&
                Objects.equals(f, tableNode.f) &&
                Objects.equals(name, tableNode.name) &&
                Objects.equals(word, tableNode.word);
    }
    @Override
    public int hashCode() {
        return Objects.hash(i, f, name, word);
    }

    @Override
    public String toString() {
        return "TableNode{" +
                "i=" + i +
                ", f=" + f +
                ", name='" + name + '\'' +
                ", word=" + word +
                '}';
    }
}
