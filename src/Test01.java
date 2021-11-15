import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.temporal.ValueRange;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    @Test       //测试 ->
    public void test7()
    {
        char c = '→';               //8594
        System.out.println(c);

        c = (char)(26);
        System.out.println(c);

        String s = "→";             //8594
        System.out.println(s);
    }

    @Test   //使用正则表达式解决输入、状态分析问题
    public void test8()
    {
        String s = "E → .E+T";
        String[] split = s.split(" → ");
        String s1 = split[1];
        String regStr = "\\..";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(s1);
        if(matcher.find())
        {
            String group = matcher.group(0);
            String s2 = group.replaceAll("\\.", "");
            System.out.println(s2);
        }
    }

    @Test   //对等价项合并及可规约状态的判断分析
    public void test9()
    {
        String s1 = "E → .E+T";
        String s2 = "E → E.";

        Matcher matcher = Pattern.compile("\\..").matcher(s2);
        boolean b = matcher.find();
        if(b)
        {
            System.out.println(matcher.group(0));
        }
        else
            System.out.println("此项可以进行规约!");
    }

    @Test       //测试HashSet     -> 不会因为重复加入而产生异常
    public void test10()
    {
        HashSet<String> set = new HashSet<>();

        set.add("P");
        set.add("P");
        set.add("P");

        set.forEach(s -> {
            System.out.println(s);
        });
    }

    @Test       //测试针对于HashMap的foreach
    public void test11()
    {
        HashMap<String,LinkedList<String>> map = new HashMap<>();
        map.put("P",new LinkedList<>());
        map.put("T",new LinkedList<>());
        map.put("F",new LinkedList<>());

        map.forEach((key,value) -> {
            value.add("N");
            System.out.println(value.size());
        });

        map.forEach((key,value) -> {
            System.out.println(value.get(0));
            value.remove("N");
            System.out.println(value.size());
        });

    }

    @Test   //针对于LinkedList的一些性能测试
    public void test12()
    {
        LinkedList<String> list = new LinkedList<>();
        list.add("Q");
        if (list.contains("Q"))
        {
            System.out.println(true);
        }
    }


    @Test   //测试HashMap使用foreach修改的情况   -> 不会改变!
    public void test13()
    {
        HashMap<String,String> map = new HashMap<>();
        map.put("F -> E","");
        map.put("F ->E","");

        map.forEach((s1,s2) -> {
            s1.replaceAll(" ","");
            s2.replaceAll(" ","");
        });

        map.forEach((key,value) -> {
            System.out.println(key+value);
        });
    }

    @Test   //针对词法分析器、语法分析器的测试
    public void test14()
    {
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.analysis();
        analyzer.createTable();

        ParseGrammar parseGrammar = new ParseGrammar();
        parseGrammar.parse();
        parseGrammar.stateTransition();
        parseGrammar.firstCollection();
        parseGrammar.followCollection();
        parseGrammar.createActionGoToTable();
    }

    @Test
    public void test15()
    {
        Stack<String> stack = new Stack<>();
        stack.push("P");
        stack.push("F");
        stack.push("G");

        String temp = "";
        for(String s:stack)
        {
            temp += s;
        }

        temp += stack.get(0);


        //顺序输出
        System.out.println(temp);

    }

    @Test   //词法分析调试
    public void test16()
    {
        AnalysisInput analysisInput = new AnalysisInput();
        analysisInput.inputAnalysis();
    }

}
