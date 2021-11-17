import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LYHstart
 * @create 2021-11-09 15:23
 *
 * 针对给定的文法产生式进行解析
 * 首先生成状态转换图 ->　输入校验
 *
 * 不妨就将语义处理及中间代码生成就在语法分析中给做了!
 *
 * -> 定义在AnalysisInput之中
 */
public class ParseGrammar
{
    @Test   //内部测试
    public void test() throws IOException
    {
        //文法经过处理之后，形成了一个三级链表
        this.parse();
        //接下来要通过查表的方式来构建状态转换图
        //生成状态转换表
        this.stateTransition();
        this.firstCollection();
        this.followCollection();
        this.createActionGoToTable();
    }


    public ParseGrammar() {
    }

    public HashMap<String, HashMap<String, LinkedList<String>>> getSynVar() {
        return synVar;
    }
    public void setSynVar(HashMap<String, HashMap<String, LinkedList<String>>> synVar) {
        this.synVar = synVar;
    }

    public HashMap<State, HashMap<String, State>> getActGoToTable() {
        return actGoToTable;
    }
    public HashMap<String, State> getItList() {
        return itList;
    }
    public HashMap<String, HashSet<String>> getFollowMap() {
        return followMap;
    }

    //存储语法变量的Map
    HashMap<String, HashMap<String,LinkedList<String>>> synVar = new HashMap<>();
    //状态存储链表
    LinkedList<State> stateList = new LinkedList<>();
    //构建一个HashMap进行状态与项的对应
    HashMap<String,State> itemState = new HashMap<>();
    //创建一个状态转换存储结构，用于存储在读入或者规约出某个符号之后的转换状态
    //如何存储状态转换?     使用如下数据结构存储
    HashMap<State,HashMap<String,State>> stateHashMap = new HashMap<>();
    //创建一个未推导的状态存储链表
    LinkedList<State> undisposedStateList = new LinkedList<>();
    //设置一个状态记录符
    static int index = 0;
    //创建一个可规约项记录表 -> 用于记录当输入达到某种情况之下，可以进行规约的状态
    HashMap<String,State> specificationList = new HashMap<>();
    //终结符号存储链表
    LinkedList<String> symList = new LinkedList<>();

    //存储语法变量的first集
    HashMap<String,HashSet<String>> firstlist = new HashMap<>();
    //由于first集求解的特殊性，这里不妨采取一种回填的方式进行处理  -> 存储语法变量及待回填的first集
    HashMap<String,LinkedList<String>> backfillMap = new HashMap<>();
    //存储backfillMap中list为0等待回填其他first集的元素的链表
    LinkedList<String> backfillList = new LinkedList<>();

    //存储语法变量的follow集
    HashMap<String,HashSet<String>> followMap = new HashMap<>();
    //存储推导过程之中得到的语法变量   -> 没啥大用处的校验结构
    HashSet<String> tempSet = new HashSet<>();
    //存储推导过程之中得到的语法变量
    LinkedList<String> tempList = new LinkedList<>();
    //follow集求解解决后更新的问题
    HashMap<String,LinkedList<String>> updateMap = new HashMap<>();
    //保存已经被更新的follow集
    LinkedList<String> updateList = new LinkedList<>();

    //生成Action表和GoTo表
    HashMap<State,HashMap<String,State>> actGoToTable = new HashMap<>();
    //经过统一规格之后的itemState表
    HashMap<String,State> itList = new HashMap<>();


    //处理文法
    public void parse()
    {
        String string = null;
        try
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
            string = baos.toString();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

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
            boolean b = synVar.containsKey(s1);

            //辅助优化变量 -> 两种情况存在大量重复的代码，故进行优化
            HashMap<String,LinkedList<String>> map = null;
            if(!b)
            {
                //倘若不存在该元素，则创建元素及其生成链表
                //创建生成链表
                 map = new HashMap<>();
                //将该元素加其中
                synVar.put(s1,map);

            }
            else
            {
                //倘若有该元素存在，则将产生式加入其生成链表之中
                map = synVar.get(s1);
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

        //记录终极符号
        synVar.forEach((key,value) -> {
            value.forEach((key1,value1) -> {
                value1.forEach(s -> {
                    if(!synVar.containsKey(s))
                    {
                        //判断是否已经加入其中
                        if(!symList.contains(s))
                        {
                            symList.add(s);
                        }
                    }
                });
            });
        });

        //加入终止符号
        symList.add("$");

        //System.out.println();
    }

    //构建状态转换图
    public void stateTransition()
    {
        //设置一个状态记录符
        //static int index = 0;   -> 直接搞成静态变量

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

        //System.out.println();
        //System.out.println();
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
            //退出判断

            //1、取出首状态来处理
            State state1 = this.undisposedStateList.pollFirst();
            //2、创建一个存储分析结果的队列 -> 采用 HashMap<String,Linkedlist<String>>的格式进程存储
            HashMap<String,LinkedList<String>> tempList = new HashMap<>();

            //3、首先针对state中的Str中的每一个字符串进行分析
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

                //注意空格的影响!
                strValue = strValue.replaceAll(" ", "");

                //这里是否可以使用正则表达式的方式快速实现匹配?   可以!查看Test01中的test8
                Matcher matcher = Pattern.compile("\\..").matcher(strValue);
                if(matcher.find())
                {
                    String group = matcher.group(0);
                    //用于暂存替代.之后的字符串
                    String repStr;
                    repStr = group.replaceAll("\\.","");

                    //针对于关键字的匹配分析  -> 当修改文法之后，这一部分需要进行修改
                    //采取编码方式优化处理
                    //针对id、while、if等等均采取单字符编码处理!




                    //在该状态的templist中寻找是否存在该key值的元素  --> 一定注意格式
                    LinkedList<String> linkedList = tempList.get("."+repStr);
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
                    //交换位置 -> 需要判断是否为空格!!!
                    if(chars[tag+1] == ' ')
                    {
                        chars[tag] = chars[tag+2];
                        chars[tag+2] = '.';
                    }
                    else
                    {
                        chars[tag] = chars[tag+1];
                        chars[tag+1] = '.';
                    }
                    //接下来将char[]转换为String   -> 生成一个新的字符串
                    String newStr = new String(chars);
                    //将其加入到序列之中
                    linkedList.offerLast(newStr);
                }
            });

            //System.out.println();
            //System.out.println();

            //对Str中的字符串分析结束之后得到templist中的各项
            //接下来要对每一项进行分析，整理成为state的格式并将其加入到undisposedStateList之中
            //在生成state格式之前，要先在itemState之中查询其所含项的任意一项  -> 校验是否已经有该项存在
            //若不存在该项 -> 创建一个新的状态state将原所含项包含到Str之中
            //判断.之后的元素是否属于语法变量 -> 通过查询synVar来实现
            //倘若是语法变量，则将以该语法变量生成的产生式全部加入到新生成的状态之中

            //首先对templist进行遍历
            tempList.forEach((key,value)->{
                //记录一下输入的字符
                String inputStr = key.replaceAll("\\.","");
                //首先查询是否有对应的项存在 -> (已经属于某一个项集闭包)
                String s = value.get(0);    //s是否存在为0的情况?
                State state2 = itemState.get(s);
                //倘若不存在该状态
                if(state2 == null)
                {
                    //创建一个新的状态
                    index++;
                    state2 = new State("I"+index);
                    //将value中的每一项加入其中
                    for(String s1:value)
                    {
                        state2.getStr().offerLast(s1);
                        //为了正则匹配的正确性，首先将空格进行剔除
                        s1 = s1.replaceAll(" ", "");

                        //同时判断是否要进行等价状态的加入 -> 分析.后的元素是否在synVar中出现
                        //1.对s1进行匹配
                        Matcher matcher = Pattern.compile("\\..").matcher(s1);
                        boolean matches = matcher.find();
                        //倘若匹配成功 -> 意味着.之后还存在着元素
                        //判断该元素是否为语法变量 -> 倘若是语法变量就将其等价状态加入该状态之中
                        if(matches)
                        {
                            String group = matcher.group(0);
                            //将.去除  -> 注意一定是\\.
                            group = group.replaceAll("\\.","");
                            //在synVar中查询是否有该记录存在
                            //HashMap<String, LinkedList<String>> map = synVar.get(group);
                            boolean b = synVar.containsKey(group);
                            //倘若查询到map -> 则为语法变量 -> 需要将等价状态进行合并
                            if(b)       //比map == mull 要好很多
                            {
                                //将该值加入到list1之中等待后续处理
                                state2.getList1().offerLast(group);
                            }
                            //倘若map == null  -> 为终结符号 -> 不会有等等价状态
                        }
                        else{
                            //倘若没有match成功 -> 意味着.之后将不再有元素出现
                            //就将其直接加入到可以规约的链表之中 -> 并且跳转状态为state1
                            this.specificationList.put(s1,state1);
                            //但是也要将其加入到该状态之中!
                            //state2.getStr().offerLast(s1); -> 一上来不久加进去了?
                        }
                    }
                    //遍历完成之后，扫描state2的未处理序列是否为空 -> 这里和I0的处理方案相似
                    while(true) //这里需要将.给加上
                    {
                        //倘若待处理的元素为零，则退出循环
                        if(state2.getList1().size() == 0)
                            break;

                        //取出第一个元素
                        String s1 = state2.getList1().pollFirst();
                        //记录在list2之中
                        state2.getList2().offerLast(s1);
                        //从synVar中进行分析
                        HashMap<String, LinkedList<String>> map = synVar.get(s1);
                        //将等价状态加入到Str之中，并且分析首元素是否在已经处理过的队列之中，倘若没有
                        //还要将其加入到list1之中
                        for(Map.Entry<String,LinkedList<String>> entry:map.entrySet())
                        {
                            //将s1和key值合并之后加入到state2的Str之中
                            state2.getStr().offerLast(s1 +" → ."+ entry.getKey());
                            //并分析value的首元素是否是语法变量且未被分析过
                            String s2 = entry.getValue().get(0);    //获取首元素
                            //判断是否为s1
                            if(!s2.equals(s1))
                            {
                                boolean flag = false;
                                //判断是否被分析过
                                for(String s3:state2.getList2())
                                {
                                    if(s3.equals(s2))
                                        flag = true;
                                }
                                //倘若没有被分析过
                                if(!flag)
                                {
                                    //在synVar中查询
                                    boolean b = synVar.containsKey(s2);
                                    if(b)   //倘若存在b
                                    {
                                        //将s2加入到state2.list1之中
                                        state2.getList1().offerLast(s2);
                                    }
                                }
                            }
                        }
                    }

                    //此时一个状态应当分析完毕 -> 将该状态加入到状态列表之中
                    this.stateList.offerLast(state2);
                    //并将该状态的所有项加入到itemList之中
                    for(String s4:state2.getStr())
                    {
                        this.itemState.put(s4,state2);
                    }

                    //System.out.println();
                    //System.out.println();
                    //System.out.println();

                    //将这个新的状态加入到待处理队列之中  -> 而不是什么都加
                    undisposedStateList.offerLast(state2);
                }
                //倘若存在该状态，记录一下转换就好了
                else
                {
                    //此时state2不为Null -> 说明之前已经创建过包含该项的状态
                    //因此取itemState中进行查找
                    //state2 = this.itemState.get(s);
                    //设置转换图
                    //这一部分相同，不妨将其与if中的语句进行合并
                }

                //接下来记录状态转换
                HashMap<String, State> state1Map = this.stateHashMap.get(state1);
                if(state1Map == null)
                {
                    //倘若为null，就创建一个新的加入进去
                    state1Map = new HashMap<>();
                    this.stateHashMap.put(state1,state1Map);
                }
                //接下来将状态转换写入
                State state3 = state1Map.get(inputStr);
                if(state3 == null)
                {
                    //倘若未记载这个状态
                    state1Map.put(inputStr,state2);
                }

                //将这个新的状态加入到待处理队列之中 -> 只有新创建的才会加进去
                //undisposedStateList.offerLast(state2);
            });


            //System.out.println();
            //System.out.println();
        }

        //现在已经可以正常生成11个状态!  ->  可是存在格式上一些丑陋的地方
        //不妨对所有状态将空格进行抽出   ->  统一规格

        //foreach并不会改变原有值!
//        stateList.forEach(state1 -> {
//            state1.getStr().forEach(s -> {
//                s.replaceAll(" ","");
//            });
//        });

        //格式优化
        for (int i = 0; i < stateList.size(); i++)
        {
            //获取该状态
            State state1 = stateList.get(i);
            //创建一个新的项集闭包链表
            LinkedList<String> newStr = new LinkedList<>();
            LinkedList<String> str = state1.getStr();
            //状态处理
            str.forEach(s -> {
                newStr.offerLast(s.replaceAll(" ",""));
            });

            //替代原有项集闭包
            state1.setStr(newStr);
        }

        //System.out.println();
        //System.out.println();
        //System.out.println();
    }

    //求解语法变量的first集
    public void firstCollection()
    {
        //为了防止在查询回填表时存在的空指针问题，不妨一开始就设置好
        synVar.forEach((key,value) -> {
            //针对于每一个key进行都创建一个对应的待填充链表
            backfillMap.put(key,new LinkedList<>());
        });

        //需要根据文法来生成
        synVar.forEach((key,value) -> {

            //查询是否存在该语法变量对应的first集
            boolean b = firstlist.containsKey(key);
            if(b)
            {
                //倘若存在该记录 -> 啥也不做
            }
            else
            {
                //倘若不存在该记录
                HashSet<String> set = new HashSet<>();
                value.forEach((key1,value1) -> {
                    //获取内部，分析其首元素
                    String s = value1.get(0);
                    //由于foreach并不会改变原有值 -> 因此使用pollfirst()也可!!! ※
                    //看test11中的测试！！！

                    //左递归解决 ->　放在遍历完成之后-> 就晚了!

                    if(!s.equals(key))
                    {
                        //首先分析该元素是否为语法变量
                        boolean b1 = synVar.containsKey(s);
                        //若是语法变量 -> 需要判断是否是自己
                        if(b1)
                        {
                            //就将其first集加入分析的元素的first集之中
                            //首先判断该元素的first集合是否已经分析出来了 -> 在firstlist中查询
                            boolean b2 = firstlist.containsKey(s);
                            if(b2)
                            {
                                //若存在该条记录 -> 查询该记录是否有待填充的记录
                                LinkedList<String> linkedList = backfillMap.get(s);
                                if(linkedList.size() == 0)
                                {
                                    //倘若无待填充的记录
                                    //就将该语法变量的first集中的所有元素加入到当前语法变量的first集中
                                    HashSet<String> strings = firstlist.get(s);
                                    //将每一条记录加入其中        -> 会出现重复异常吗?    ->测试一下
                                    strings.forEach(s1 -> {
                                        set.add(s1);
                                    });
                                }
                                else
                                {
                                    //倘若这条记录之中还有等待被其他语法变量的first集填充
                                    // -> 就将该语法变量的first集记录在当前语法变量的回填列表之中
                                    LinkedList<String> linkedList1 = backfillMap.get(key);
                                    linkedList1.add(s);
                                    //等待自底向上的回填
                                }
                            }
                            else
                            {
                                //倘若这是first集中还没有关于该语法变量
                                //修改backfilllist
                                LinkedList<String> linkedList = backfillMap.get(key);
                                linkedList.add(s);
                            }
                        }
                        else
                        {
                            //倘若不是语法变量 -> 直接加入到其first集中即可
                            set.add(s);
                        }
                    }
                });
                //遍历完成之后，将新生成的first集加入到firstlist之中
                firstlist.put(key,set);

                //此时需要增加一个回填操作
                //回填操作放在最后执行也可!
                LinkedList<String> linkedList = backfillMap.get(key);
                if(linkedList.size() == 0)
                {
                    //倘若无待回填的first集 -> 就将其加入到其他的first集之中
                    //遍历backfillList
                    backfillMap.forEach((key1,value1) -> {
                        if(value1.contains(key))
                        {
                            HashSet<String> strings = firstlist.get(key1);
                            firstlist.get(key).forEach(s -> {
                                strings.add(s);
                            });
                            //同时去除记录
                            value1.remove(key);
                            //这里增加一个判断  -> 倘若这时候为0了
                            if(value1.size() == 0)
                            {
                                backfillList.add(key1);
                            }
                        }


                    });
                }
            }
        });
        //处理左递归问题 -->　处理的晚了！
        //backfillMap.forEach((key,value) -> {
        //    value.remove(key);
        //});

        //等待遍历完毕synVar之后 -> 所有的first即已经生成
        //但是backfill中仍然有等待回填的记录 -> 消除掉所有的记录
        //不妨设置一个填充链表，当待填充的记录达到语法变量的个数的时候停止循环
        while(true)
        {
            //倘若待回填链表中的元素个数为0 -> 退出循环
            if(backfillList.size() == 0)
                break;

            //取出一个在backfillList中不存在的语法变量进行回填
            String s = backfillList.pollFirst();
            //遍历backfillMap
            backfillMap.forEach((key1,value1) -> {
                if(value1.contains(s))
                {
                    HashSet<String> strings = firstlist.get(key1);
                    firstlist.get(s).forEach(s2 -> {
                        strings.add(s2);
                    });
                }
                if(value1.contains(s))  //代码重复提示好烦!
                {
                    //同时去除记录
                    value1.remove(s);
                    //这里增加一个判断  -> 倘若这时候为0了
                    if(value1.size() == 0)
                    {
                        backfillList.add(key1);
                    }
                }
            });
        }

        //System.out.println();
        //System.out.println();
        //System.out.println();
    }

    //推导follow集
    public void followCollection()
    {
        //将文法的开始符号加入到tempSet之中 -> 有点画蛇添足了
        HashSet<String> set = new HashSet<>();
        //将$加入
        set.add("$");
        //处理文法开始符号的产生式
        HashMap<String, LinkedList<String>> map = synVar.get("P");
        //遍历所有产生式 ->　观察是否存在其他的包含有文法开始符号的产生式
        //好吧，这种情况在此文法结构之中并不存在

        //直接将其加入到follow集合队列之中
        followMap.put("P",set);


        //遍历其产生式
        map.forEach((key,value) -> {
            //在句子中进行分析
            LinkedList<String> temp = new LinkedList<>();

            //分析其中的语法变量
            value.forEach((s -> {

                if(synVar.containsKey(s))
                {
                    //判断是否重复
                    if(!tempSet.contains(s))
                    {
                        tempSet.add(s);
                        tempList.add(s);
                        temp.add(s);
                    }
                }
                //取出temp中的元素分析 -> 针对于每个句子进行分析
                while (temp.size() != 0)
                {
                    String s1 = temp.pollFirst();
                    //从表达式中获取s1所在的位置
                    //1、判断是否位于尾部
                    Matcher matcher = Pattern.compile(s1+".").matcher(key);
                    if(matcher.find())
                    {
                        //发现了该语法变量 -> 根据语法分析得知这种情况肯定不存在!
                        //...
                    }
                    else
                    {
                        //创建一个follow集   -> 文法开始符号仅仅可能产生S -> 且S的follow集仅仅由P决定
                        //String group = matcher.group(0);
                        //group = group.replaceAll(,"");
                        String group = s1;
                        //创建一个新的语法变量单元
                        HashSet<String> set1 = new HashSet<>();
                        followMap.get("P").forEach(s2 -> {
                            set1.add(s2);
                        });
                        //并将其加入followMap之中
                        followMap.put(group,set1);

                        //载入关联列表之中 -> 这里肯定未创建过 -> 直接创建了
                        LinkedList<String> list = new LinkedList<>();
                        list.add(s1);
                        updateMap.put("P",list);
                    }
                }
            }));
        });

        //接下来分析语法变量
        while(true)
        {
            //若无待分析的语法变量
            if(tempList.size() == 0)
            {
                break;
            }

            //分析语法变量
            String s = tempList.pollFirst();                //E
            //接下来分析s的产生式
            HashMap<String, LinkedList<String>> map1 = synVar.get(s);



            //遍历map1获得语法变量
            map1.forEach((key,value) -> {       //key:E + T、T
                value.forEach(s1 -> {           //value 即为将key拆分
                    //在句子中进行分析
                    LinkedList<String> temp = new LinkedList<>();
                    //统计出现的所有语法变量
                    if(synVar.containsKey(s1))
                    {
                        //判断是否重复
                        if(!tempSet.contains(s1))
                        {
                            tempList.add(s1);
                            tempSet.add(s1);
                        }
                        //在任何情况之下都要加入到list之中
                        temp.add(s1);
                    }
                    while(temp.size() != 0)
                    {
                        //设置一个查询标识位 ->用于记录使用正则表达式是否匹配到元素
                        boolean flag = false;

                        String s2 = temp.pollFirst();
                        //查询是否位于表达式的中间          //注意空格的影响!
                        Matcher matcher = Pattern.compile(s2 + " .").matcher(key);
                        while(matcher.find())
                        {
                            flag = true;
                            //由于可能存在不止一个，因此采取while循环
                            String group = matcher.group(0);
                            //取出这个终结符号来
                            group = group.replaceAll(s2+" ","");
                            //这时候group即为跟在语法变量之后的符号
                            // -> 由于语法的特性导致必定为终结符号 -> 故这里就不做语法变量的判断
                            //判断该元素是否已经在followMap中存在记录
                            HashSet<String> strings = followMap.get(s2);
                            if(strings != null)
                            {
                                //倘若已经存在该记录
                                strings.add(group);

                                //此时需要标记为更新了! -> 如果没有此条记录的话
                                if(!updateList.contains(s2))
                                    updateList.add(s2);
                                //等待进一步的处理
                            }
                            else
                            {
                                //添加该记录
                                strings = new HashSet<>();
                                strings.add(group);
                                //一定看清楚加入的变量是啥!!!       尤其是s、s1、s2等
                                followMap.put(s2,strings);
                            }
                        }
                        //若没找到 -> 一定位于产生式的最末端
                        if(!flag)
                        {
                            //这就需要判断是否出现过该元素，且判断是否为s本身!
                            //分析s2
                            if(s2.equals(s))
                            {
                                //相等了就不加了
                            }
                            else
                            {
                                HashSet<String> strings = followMap.get(s);
                                HashSet<String> strings1;
                                //判断末尾元素的follow集是否存在
                                if(followMap.containsKey(s2))
                                {
                                    strings1 = followMap.get(s2);
                                }
                                else
                                {
                                    strings1 = new HashSet<>();
                                    followMap.put(s2,strings1);
                                }
                                //添加元素
                                strings.forEach(s3 -> {
                                    strings1.add(s3);
                                });

                                //在关联队列中记录
                                boolean b = updateMap.containsKey(s);
                                LinkedList<String> linkedList;
                                if(b)
                                {
                                    linkedList = updateMap.get(s);
                                }
                                else
                                {
                                    linkedList = new LinkedList<>();
                                    updateMap.put(s,linkedList);
                                }
                                //若不包含 -> 添加
                                if(!linkedList.contains(s2))
                                    linkedList.add(s2);
                            }
                        }
                    }
                });
            });
        }

        //System.out.println();
        //System.out.println();

        //结束之后要处理更新的follow集!
        while(true)
        {
            if(updateList.size() == 0)
                break;
            else
            {
                String s = updateList.pollFirst();
                LinkedList<String> linkedList = updateMap.get(s);
                //解决空指针异常!
                if(linkedList != null)
                {
                    linkedList.forEach(s1 -> {
                        HashSet<String> strings = followMap.get(s1);
                        HashSet<String> strings1 = followMap.get(s);
                        strings1.forEach(s2 -> {
                            strings.add(s2);
                        });
                        //将s1加入到updateFollow之中 -> 如果没有的话
                        if(!updateList.contains(s1))
                            updateList.add(s1);
                    });
                }
            }
        }

        //System.out.println();
        //System.out.println();
        //System.out.println();

    }

    //生成Action表和GoTo表
    public void createActionGoToTable()
    {
        //获取状态数量 ->　开辟多大的链表     //list无需开辟!

        /*
        //将对应的状态加入到对应的位置上即可
        int index = 0;
        while(actGoToTable.size() != stateList.size())
        {
            for (int i = 0; i < stateList.size(); i++)
            {
                State state = stateList.get(i);
                if(state.getName().equals("I"+index))
                {
                    //存储state信息 -> 这也是为何修改ArrayList为HashMap的原因
                    actGoToTable.put(state,new HashMap<>());
                }
            }
            index++;
        }
        */

        //由于引入HashMap之后将原有的线性结构打破 -> 因此无规律插入即可!
        stateList.forEach(state-> {
            actGoToTable.put(state,new HashMap<>());
        });

        //遍历状态转换记录表，设置Action表
        stateHashMap.forEach((key,value) -> {
            //获取到key在actGoToTable中的位置
            HashMap<String, State> map = actGoToTable.get(key);
            //接下来遍历value -> 生成转换表
            value.forEach((key1,value1) -> {
                map.put(key1,value1);
            });
        });

        //遍历规约记录表，生成GoTo表  --> 这里需要查询一下key所属的状态
        //使用ItemState进行查询

        //注意，itemState和specificationList中规格不一的问题!
        //这里采取将itemState中空格删除的方法统一规格!
        //itemState.forEach((key,value) -> {
        //    key = key.replaceAll(" ","");         //不可以使用foreach
        //});

//        for (int i = 0; i < itemState.size(); i++)
//        {
//            Set<Map.Entry<String, State>> entries = itemState.entrySet();
//            itemState.remove(state);
//
//        }

        //与其替换处理，不如重新生成一个得了!
        itemState.forEach((key,value) -> {
            itList.put(key.replaceAll(" ",""),value);
        });

        //可规约的项所属的状态一定唯一!
        specificationList.forEach((key,value) -> {
            State state = itList.get(key);
            HashMap<String, State> map = actGoToTable.get(state);
            map.put(key,value);
        });


        //System.out.println();
        //System.out.println();
        //System.out.println();

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

/*
P → E
E → E + T
E → T
T → T * F
T → F
F → ( E )
F → d
 */

/*
P → S
S → d
S → E
S → C
C → E
E → T
T → F
F → d
F → ( E )
F → t
 */


/*
稍作修改S → d = E; S
P → S
S → d = E ;
S → d = E ; S
S → f ( C ) S
S → f ( C ) S e S
S → w ( C ) S
S → S ; S
C → E > E
C → E < E
C → E & E
E → E + T
E → E – T
E → T
T → F
T → T * F
T → T / F
F → ( E )
F → d
F → g
 */


