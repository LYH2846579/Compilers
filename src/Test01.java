import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @author LYHstart
 * @create 2021-11-02 14:31
 */
public class Test01
{
    @Test   //测试字符串
    public void test1()
    {
       String s = "int main() \n" +     //\n的读取
                  "{          \n" +
                  "int 1 = 0; \n" +
                  "return 0;  \n" +
                  "} \n";

        System.out.println(s);
    }

    @Test   //测试输入
    public void test2() throws IOException
    {
        FileInputStream fis = new FileInputStream("F:\\Java\\Compilers\\src\\Hello.txt");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[5];
        while((read = fis.read(buffer)) != -1)
        {
            baos.write(buffer,0,read);
        }

        System.out.println(baos.toString());

        String s = baos.toString();
        String[] split = s.split(" ");
        for(String s1:split)
        {
            byte[] bytes = s1.getBytes();
            for(byte b:bytes)
                System.out.println((char)b);
        }
    }

    @Test   //针对数据结构双向链表的一些测试
    public void test3()
    {
        //实现的时候仅仅需要维护一个下标index即可实现循环链表
        LinkedList<Integer> list = new LinkedList<>();

        list.offerLast(1);
        list.offerLast(2);
        list.offerLast(3);
        list.offerLast(4);
        list.offerLast(5);

        Integer integer = list.get(4);
        System.out.println(integer);
    }

    @Test      //针对word类的一些测试
    public void test4()
    {
        Word word = new Word("Integer","a","12");
        System.out.println(word.toString());
    }

    @Test   //针对小数转化问题的测试
    public void test5()
    {
        Float f = new Float(".1");
        System.out.println(f);
    }

    @Test   //针对反射的一些测试 -> 还是使用if作为判断ba
    public void test6()
    {
        try
        {

            Class<?> string = Class.forName("int");
            try
            {
                Object o = string.newInstance();
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }

        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

}
