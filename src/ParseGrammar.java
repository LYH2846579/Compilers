import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author LYHstart
 * @create 2021-11-09 15:23
 *
 * 针对给定的文法产生式进行解析
 */
public class ParseGrammar
{
    //存储语法变量的Map
    HashMap<String, HashMap<String,LinkedList<String>>> synVar = new HashMap<>();
    //状态存储链表
    LinkedList<State> stateList = new LinkedList<>();

    @Test
    public void test() throws IOException
    {
        //文法经过处理之后，形成了一个三级链表
        this.parse(synVar);
        //接下来要通过查表的方式来构建状态转换图


    }

    //处理文法
    public void parse(HashMap<String, HashMap<String,LinkedList<String>>> hashMap) throws IOException
    {
        //从文件中读取数据
        FileInputStream fis = new FileInputStream("F:\\Java\\Compilers\\src\\grammar");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int read = 0;
        byte[] buffer = new byte[5];
        while((read = fis.read(buffer)) != -1)
        {
            baos.write(buffer,0,read);
        }
        //导出字符串
        String string = baos.toString();

        //以"\r\n"为界限进行划分
        String[] split = string.split("\r\n");
        //接下来针对split的每一个单元，如S → id = E; 进行解析
        for(String s:split)
        {
            //首先以 " → " 为界限进行划分
            String[] split1 = s.split(" → ");
            //取出首元素
            String s1 = split1[0];
            //在HashMap中查询是否有该元素存在
            boolean b = hashMap.containsKey(s1);

            //辅助优化变量 -> 两种情况存在大量重复的代码，故进行优化
            HashMap<String,LinkedList<String>> map = null;
            if(!b)
            {
                //倘若不存在该元素，则创建元素及其生成链表
                //创建生成链表
                 map = new HashMap<>();
                //将该元素加其中
                hashMap.put(s1,map);

            }
            else
            {
                //倘若有该元素存在，则将产生式加入其生成链表之中
                map = hashMap.get(s1);
            }

            //接下来对产生式进行分析
            String s2 = split1[1];
            //对产生式进行分析
            String[] split2 = s2.split(" ");
            //创建一个字符单元存储链表
            LinkedList<String> list = new LinkedList<>();
            //填充list
            for(String s3:split2)
            {
                list.offerLast(s3);
            }
            //将原式与字符单元存储链表作为一个Entry加入map中
            map.put(s2,list);
        }
    }

    //构建状态转换图
    public void stateTransition()
    {
        //首先创建一个初始状态
        State state = new State("I0");
        //将文法的开始符号加入到未扫描的队列中
        state.getStr().offerLast("P");
        //接下来
    }
}



/*
P → S
S → id = E;
S → if ( C )  S
S → if ( C )  S   else   S
S → while ( C )  S
S → S ; S
C → E > E
C → E < E
C → E == E
E → E + T
E → E – T
E → T
T → F
T → T * F
T → T / F
F → ( E )
F → id
F → digits
 */
