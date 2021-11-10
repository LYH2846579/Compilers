import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    //构建一个HashMap进行状态与项的对应
    HashMap<String,State> itemState = new HashMap<>();
    //创建一个状态转换存储结构，用于存储在读入或者规约出某个符号之后的转换状态
    //如何存储状态转换?     使用如下数据结构存储
    HashMap<Map.Entry<State,String>,State> stateHashMap = new HashMap<>();
    //创建一个未推导的状态存储链表
    LinkedList<State> undisposedStateList = new LinkedList<>();


    @Test
    public void test() throws IOException
    {
        //文法经过处理之后，形成了一个三级链表
        this.parse(synVar);
        //接下来要通过查表的方式来构建状态转换图
        //生成状态转换表
        this.stateTransition();

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
            //list.offerFirst(".");     //->将I0单独摘出来处理  -> 其他状态合并处理
            //填充list
            for(String s3:split2)
            {
                list.offerLast(s3);
            }
            //将原式与字符单元存储链表作为一个Entry加入map中
            map.put(s2,list);
        }

        System.out.println();
    }

    //构建状态转换图
    public void stateTransition()
    {
        //设置一个状态记录符
        int index = 0;
        //首先创建一个初始状态
        State state = new State("I"+index);
        //将文法的开始符号加入到未处理的队列中
        state.getList1().offerLast("P");
        //接下来扫描未处理的链表       -> 针对于状态I0
        while(true)
        {
            //判断链表长度是否为0 -> 为0就退出循环
            if(state.getList1().size() == 0)
                break;
            //若不为零 -> 根据文法推导
            //1.取出第一个符号
            String s = state.getList1().pollFirst();
            //将该符号加入到已经处理的队列之中
            state.getList2().offerLast(s);
            //从synVar中查询该符号
            HashMap<String, LinkedList<String>> map = this.synVar.get(s);
            //判断map是否为空
            if(map != null)
            {
                //遍历map
                map.forEach((key,value) ->{
                    //首先将key加入Str   -> 加入项集闭包中
                    state.getStr().offerFirst(s + " → ." +key);
                    //分析产生式的首元素 -> 将.元素略过 --> 获取第一个语法变量 get(1)
                    //后期将.在首状态I0中生成，因此这里取首元素即可
                    String s1 = value.get(0);
                    //设置一个标识    -> 存储是否扫描过该变量
                    boolean flag = true;
                    //判断是否为s -> 若为s就无需加入未处理队列，若已经加入则无需加入
                    if(!s1.equals(s))   //不为s的时候在已经处理的list中查询
                    {
                        //遍历已经处理的list
                        for(String s2:state.getList2())
                        {
                            //倘若发现已经存在该元素
                            if(s2.equals(s1))
                            {
                                flag = false;
                                break;
                            }
                        }

                        //扫描之后若仍为true -> 将新扫描到的语法变量加入到list1中
                        if(flag)
                        {
                            state.getList1().offerLast(s1);
                        }
                        //若为flase -> 则表明已经扫描过该元素，故不再加入扫描队列之中
                    }
                });
            }
        }

        System.out.println();
        System.out.println();
        //首先将首状态加入到statelist之中
        this.stateList.offerLast(state);
        //接下来将state加入到未处理的链表之中
        this.undisposedStateList.offerLast(state);
        //接下来将首状态的所有项集闭包加入到itemState之中
        state.getStr().forEach(s ->{
            //记录每个状态的项
            itemState.put(s,state);
        });

        //接下来要对其他的状态进行处理 -> 此时仅仅改变.的位置
        //倘若待处理的队列不为空 -> 继续处理   -> 注意以下使用state1作为状态标识符
        while(this.undisposedStateList.size() != 0)
        {
            //1、取出首状态来处理
            State state1 = this.undisposedStateList.pollFirst();
            //2、创建一个存储分析结果的队列 -> 采用 HashMap<String,Linkedlist<String>>的格式进程存储
            HashMap<String,LinkedList<String>> tempList = new HashMap<>();

            //首先针对state中的Str中的每一个字符串进行分析
            state1.getStr().forEach(s -> {
                //首先将字符串依照" → "进行划分
                String[] split = s.split(" → ");
                //获取左半部分的key
                String strKey = split[0];
                //获取右半部分的value
                String strValue = split[1];
                //对value进行解析
                byte[] bytes = strValue.getBytes();
                //对bytes进行遍历
                //...

                //这里是否可以使用正则表达式的方式快速实现匹配?   可以!查看Test01中的test8
                Matcher matcher = Pattern.compile("\\..").matcher(strValue);
                if(matcher.find())
                {
                    String group = matcher.group(0);
                    group = group.replaceAll("\\.","");
                    //在该状态的templist中寻找是否存在该key值的元素
                    LinkedList<String> linkedList = tempList.get(group);
                    if(linkedList != null)
                    {
                        //将两个状态进行合并
                    }
                    else
                    {
                        //倘若不存在该元素所对应的list
                        LinkedList<String> linkedList1 = new LinkedList<>();
                        //将其加入到map中
                        tempList.put(group,linkedList1);
                        //修改linkedlist -> 以便于合并状态
                        linkedList = linkedList1;
                    }

                    //倘若存在该元素 -> 交换.和后面元素的位置，重新生成字符串
                    char[] chars = s.toCharArray();
                    int tag = 0;
                    for (int i = 0; i < chars.length; i++)
                    {
                        if(chars[i] == '.')
                        {
                            //倘若等于.  -> 就交换位置
                            tag = i;
                        }
                    }
                    //交换位置
                    chars[tag] = chars[tag+1];
                    chars[tag+1] = '.';
                    //接下来将char[]转换为String   -> 生成一个新的字符串
                    String newStr = new String(chars);
                    //将其加入到序列之中
                    linkedList.offerLast(newStr);
                }
            });

            System.out.println();
            System.out.println();
        }

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
