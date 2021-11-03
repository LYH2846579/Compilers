import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author LYHstart
 * @create 2021-11-02 15:24
 *
 * 实现词法分析 -> 要求操作符之间要有空格
 */
public class LexicalAnalyzer
{
    @Test
    public void test() throws IOException
    {
        LinkedList<Word> analysis = analysis();
        IdentityHashMap<String, TableNode> table = createTable(analysis);
        analysis.forEach(word -> {
            System.out.println(word);
        });
        //for(Word word:analysis)
        //    System.out.println(word);
        Set<Map.Entry<String, TableNode>> entries = table.entrySet();
        System.out.println("===================================");
        //输出符号表 -> 上迭代器
        Iterator<Map.Entry<String, TableNode>> iterator = entries.iterator();
        while(iterator.hasNext())
        {
            Map.Entry<String, TableNode> next = iterator.next();
            String name = next.getKey();
            TableNode value = next.getValue();
            System.out.println(name+" "+value);
        }
    }


    //实现词法分析
    public LinkedList<Word> analysis() throws IOException
    {
        LinkedList<Word> list = new LinkedList<>();

        //辅助变量
        int flag = 1;

        //载入数据
        /**/
        FileInputStream fis = new FileInputStream("F:\\Java\\Compilers\\src\\Hello.txt");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[5];
        while((read = fis.read(buffer)) != -1)
        {
            baos.write(buffer,0,read);
        }


        //接下来将数据分片 -> 得到String类型的数组
        String string = baos.toString();

        //处理字符串\n\r   ->  !     //难道正则无法处理\n\r的选项??? -> 使用返回值接收!!
        string = string.replaceAll("\n|\r"," ");

        System.out.println(string);


        //String string = "int abc = 100;";
        String[] split = string.split(" ");

        //设置一下辅助变量 -> 解决多位问题
        String temp = "";               //用于暂时储备多位变量中的前几位
        String type = "";               //用于存储多位变量的类型

        //接下来对数组中的每一个元素进行分析
        for(String s:split)
        {

            //当然可以首先对元素进行正则匹配
            Word word = regString(s);
            if(word != null)
            {
                //判断前位元素
                if(temp.length() != 0)
                {
                    Word wordTemp = null;
                    //这里需要考虑变量值的问题
                    if(type == "digit")         //倘若为数字
                    {
                        wordTemp = new Word(type,temp,type);
                    }
                    else if(type == "letter")
                        wordTemp = new Word(type,temp,null);

                    //初始化
                    temp = "";
                    type = "";

                    list.offerLast(wordTemp);
                }


                list.offerLast(word);
                flag = 0;   //允许多位定义变量
            }
            else
            {
                //倘若不是关键字，接下来进行分析
                byte[] bytes = s.getBytes();
                for(Byte b:bytes)
                {
                    //必须将byte转换为String进行匹配
                    char c = (char)Integer.parseInt(b.toString());
                    String byStr = "" + c;
                    /*
                    if(b.equals(";"))
                    {
                        Word word = new Word("OP",";",null);
                        list.offerLast(word);
                        flag = 1;       //不允许多位定义变量
                    }

                    //考虑的一下，这里使用switch-case语句可能会更好
                    switch (b)
                    {
                        case ';':
                            Word word = new Word("OP",";",null);
                            list.offerLast(word);
                            flag = 1;   //接下来不允许多位定义变量及多位赋值情况
                            break;

                    }
                    */
                    //后来觉得使用正则还是更容易
                    if(byStr.matches(";"))
                    {
                        //必须判断temp中是否存有字符
                        if(temp.length() != 0)
                        {
                            Word wordTemp = null;
                            //这里需要考虑变量值的问题
                            if(type == "digit")         //倘若为数字
                            {
                                wordTemp = new Word(type,temp,type);
                            }
                            else if(type == "letter")
                            {
                                wordTemp = new Word(type,temp,null);
                            }

                            //初始化
                            temp = "";
                            type = "";

                            list.offerLast(wordTemp);
                            flag = 1;
                        }

                        Word word1 = new Word("OP",";",null);
                        list.offerLast(word1);
                        flag = 1;   //接下来不允许多位定义变量及多位赋值情况
                    }
                    else if(byStr.matches("[0-9]"))         //倘若为数字
                    {
                        //倘若前面变量为关键字等 -> 允许多位赋值
                        if(flag == 0 && temp.length()==0)   //此时还未给temp赋值
                        {
                            //存储到temp中
                            temp += byStr;
                            //将属性定义下来
                            type = "digit";         //属性为数字
                        }
                        else if(flag == 0 && temp.length() != 0)    //这里保证变量中可以出现数字
                        {                                           //同时保证数字不可以出现在变量开头
                            //存储到temp中
                            temp += byStr;
                        }
                        //若flag = 1  --> 不允许连续定义 -> 直接加入list
                        else if(flag == 1)
                        {
                            //为单个字符
                            Word word1 = new Word("digit",byStr,byStr);
                            list.offerLast(word1);
                        }

                    }
                    else if(byStr.matches("[a-zA-Z_]"))     //倘若属于字符序列
                    {
                        //一下处理与数字类似
                        if(flag == 0 && temp.length()==0)
                        {
                            temp += byStr;
                            type = "letter";
                        }
                        else if(flag == 0 && temp.length() != 0)
                        {
                            temp += byStr;
                        }
                        else if(flag == 1)
                        {
                            //为单个字符
                            Word word1 = new Word("letter",byStr,byStr);
                            list.offerLast(word1);
                        }
                    }
                    else if(byStr.matches("\\."))   //处理小数问题
                    {
                        if(temp.length() == 0)
                        {
                            temp += "0.";
                            type = "digit";
                        }
                        else if(temp.length() != 0 && type.equals("digit"))
                        {
                            temp += ".";
                        }
                        else
                            throw new RuntimeException("输的啥???");
                    }

                }
            }


        }
        return list;
    }

    //针对关键字的正则匹配
    public Word regString(String s)
    {
        //匹配关键字
        String regStr = "if|else|while|int|float";
        boolean matches = s.matches(regStr);
        if(matches)
        {
            Word word = new Word("KeyWord",s,null);
            return word;
        }

        //匹配操作符 [+|*|/|>|<|=|(|)|'|\-]
        regStr = "[+|*|/|>|<|=|(|)|'|\\-]";   //单符号字符
        matches = s.matches(regStr);
        if(matches)
        {
            Word word = new Word("OP",s,null);
            return word;
        }

        //匹配二元操作符
        regStr = "==|>=|<=|!=";
        s.matches(regStr);
        if(matches)
        {
            Word word = new Word("BinOP",s,null);
            return word;
        }

        return null;
    }

    //创建符号表 -> 这里其实采用的是自底向上的分析方式来创建符号表  "KL:" -> Node
    public IdentityHashMap<String,TableNode> createTable(LinkedList<Word> list)
    {
        //创建符号表 -> 不允许重复定义
        IdentityHashMap<String,TableNode> ihashMap = new IdentityHashMap<>();
        //创建一个删除列表
        //LinkedList<Integer> deleteList = new LinkedList<>();
        //创建一个删除栈
        Stack<Integer> stack = new Stack<>();

        int index = 0;      //作为下标对list进行扫描
        int flag = 0;
        for(Word word:list)
        {
            //跳跃
            if(flag > 0)
            {
                flag--;
                continue;
            }

            //倘若类型为int或者float
            if(word.getName().equals("int")|word.getName().equals("float"))
            {
                //创建两个临时变量存储下两个元素
                Word word1 = list.get(index+1);
                Word word2 = list.get(index+2);
                //再次判断
                if(word1.getType().equals("letter") && word2.getName().equals(";"))
                {
                    //倘若符合定义条件
                    if(word.getName().equals("int"))
                    {
                        TableNode tableNode = new TableNode(0,null,word1.getName(),word);
                        //加入符号表 -> 以变量名作为key
                        ihashMap.put(word1.getName(),tableNode);

                        //将这三个元素从原来的列表中剔除 -> 需要修改!!!
                        //list.remove(index);
                        //list.remove(index+1);
                        //list.remove(index+2);

                        //加入删除列表
                        //deleteList.offerLast(index-1);        //删除链表已被淘汰
                        //加入删除栈
                        stack.push(index-1);
                        //修改Index
                        index += 3;
                        //设置跳跃值
                        flag = 2;
                    }
                    else if(word.getName().equals("float"))
                    {
                        TableNode tableNode = new TableNode(null,0.0f,word1.getName(),word);
                        //加入符号表 -> 以变量名作为key
                        ihashMap.put(word1.getName(),tableNode);

                        //deleteList.offerLast(index-1);
                        stack.push(index-1);
                        index += 3;
                        flag = 2;
                    }
                    else
                    {
                        //倘若不满足以上的情况 -> 继续向后扫描
                        //理论上这里不存在其他情况!
                        index++;
                    }
                }
                else
                {
                    //倘若不满足以上的情况 -> 继续向后扫描
                    //这里需要处理只给出一个关键字 或者 没有; 或者关键字后面不是letter属性变量的情况 --> 错误处理
                    index++;
                }
            }
            else
            {
                //倘若不满足以上的情况 -> 继续向后扫描
                index++;
            }
        }

        //全部扫描完毕之后,删除对应的位置
        /*
        删除元素的时候发现大问题了！删除之后list中的index值也随之改变，无法正确的删除对应位置的信息!
            -> 逆序删除是否可以很好的解决这个问题?
                -> 还考虑链表逆序干啥? 直接上栈!

        deleteList.forEach(integer -> {
            list.remove(integer+3);
            list.remove(integer+2);
            list.remove(integer+1);                       //为何integer这个位置的元素无法删除??? -> 修改一下list中的值试一试

        });
        */

        while(!stack.empty())
        {
            Integer pop = stack.pop();
            list.remove(pop+3);
            list.remove(pop+2);
            list.remove(pop+1);
        }



        return ihashMap;
    }
}
