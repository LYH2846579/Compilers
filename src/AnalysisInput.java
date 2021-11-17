import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LYHstart
 * @create 2021-11-14 21:15
 *
 * //语法分析总控程序 -> 读取用户的输入信息，进行规约
 */
public class AnalysisInput
{
    //一些私有属性 -> 保证在语法分析之后还可以被语义处理及中间代码生成所调用
    private LexicalAnalyzer analyzer;
    private ParseGrammar parseGrammar;

    //存储当前状态的栈
    private Stack<State> stateStack = new Stack<>();
    //存储符号的栈 -> 将栈结构改了!  --> 预计全场爆红  --> 由Symbol类对象代替原来的String
    private Stack<Symbol> symStack = new Stack<>();
    //用于存储语义处理过程之中出现的三地址代码
    private ArrayList<String> addressCodeList = new ArrayList<>();
    //用于存储已经扫描过的Token序列
    private Stack<Word> wordStack = new Stack<>();
    //用以产生序号的变量
    private int label = 0;
    //用于产生行号的变量
    private int tag = 0;


    //维护一个字符串用于记录符号栈中对应的字符序列
    private String symStr = new String();

    //Error标识
    private int ErrorTag = -1;

    //备份一份输入Token序列供给语义处理使用
    private LinkedList<Word> tempList = new LinkedList<>();


    public AnalysisInput() {
    }
    public AnalysisInput(LexicalAnalyzer analyzer, ParseGrammar parseGrammar) {
        this.analyzer = analyzer;
        this.parseGrammar = parseGrammar;
    }

    //进入输入分析阶段
    public boolean inputAnalysis()
    {
        try
        {
            //词法分析预处理
            analyzer = new LexicalAnalyzer();
            analyzer.analysis();
            analyzer.createTable();
            //编码
            analyzer.analysisDeal();

            //语法分析预处理
            parseGrammar = new ParseGrammar();
            parseGrammar.parse();
            parseGrammar.stateTransition();
            parseGrammar.firstCollection();
            parseGrammar.followCollection();
            parseGrammar.createActionGoToTable();

            //为备份的链表赋值 -> 便于语义处理
            analyzer.getAnalysis().forEach(word -> {
                tempList.offerLast(word);
            });

            //获取Token表
            LinkedList<Word> analysis = analyzer.getAnalysis();
            //获取符号表
            HashMap<String, TableNode> table = analyzer.getTable();
            //遍历Token队列，对多字节变量进行编码 -> 对多字节Type进行编码
            //letter -> l      digit -> g     OP -> o       BinOP  -> b
            //KeyWord -> k
            //如果是letter或者digit就进行编码转换，否则就  -> 已经在词法分析阶段完成，分析value值可得

            //获取actGoToTable
            HashMap<State, HashMap<String, State>> actGoToTable = parseGrammar.getActGoToTable();
            //获取followMap
            HashMap<String, HashSet<String>> followMap = parseGrammar.getFollowMap();


            //1、首先将文法的I0状态压入栈中  //statelist中第一个即为I0
            stateStack.push(parseGrammar.stateList.get(0));
            //接下来进入分析阶段
            while(true)
            {
                //判断是否为$
                if(analysis.get(0).getValue().equals("$"))
                {
                    //判断是否接受
                    if(stateStack.peek().getName().equals("I0"))
                    {
                        //判断符号栈 ->　是否规约出来文法的开始符号
                        if(symStack.peek().getName().equals("P"))
                        {
                            System.out.println("Accept!");
                            //退出循环
                            //break;
                            this.ErrorTag = 0;
                            return true;
                        }
    //                    else
    //                        System.out.println("syntax error!");
                    }
    //                else
    //                    System.out.println("syntax error!");
                }

                //取出当前符号栈中的状态
                State peek = stateStack.peek();

                //判断当前的符号栈中的内容
                //若当前符号栈为空 -> 移入一个元素
                if(symStack.size() == 0)
                {
                    //从输入序列中取出首元素
                    Word word = analysis.pollFirst();

                    //创建一个新的Symbol类对象
                    Symbol symbol = new Symbol(word.getValue());

                    //这里需要判断一下word的类型 -> Info.txt
                    //倘若为字符类型或者为数字类型
                    if(word.getType().equals("letter") || word.getType().equals("digit"))
                    {
                        //需将其值加入到对应的code属性之中
                        symbol.setCode(word.getName());
                    }

                    //将取出的word压入栈中
                    wordStack.push(word);


                    //将其加入到符号栈中
                    symStack.push(symbol);


                    //同时修改字符串
                    symStr += word.getValue();

                    //将状态压入栈中
                    HashMap<String, State> map = actGoToTable.get(peek);
                    State state = map.get(word.getValue());
                    //将状态压入栈
                    stateStack.push(state);
                }

                //此时符号栈中一定有元素存在
                //分析字符串
                // -> 移入规约的时候字符串对应的也要发生改变!


                /*//根据当前的状态进行分析是否有对应的项
                HashMap<String, State> actGoTo = actGoToTable.get(peek);
                State state = actGoTo.get(symStr);
                if(state == null)
                {
                    //若为空，不存在转换状态!
                    System.out.println("syntax Error!");
                }
                else
                {

                }*/

                State state = stateStack.peek();
                HashMap<String, State> actGoTo = actGoToTable.get(state);


                //获得输入该字符串之后转换的状态
                //接下来将转换之后的状态压入栈中
                //stateStack.push(state);
                //分析当前状态中对应的字符串是否可以进行规约
                //根据当前字符串的状态查询对应转换之后状态的str中是否含有可规约项

                //可规约项的扫描应当从符号栈顶依次向下扫描!
                //维护一个index进行加
                //从栈顶一直加到Index
                int index = symStack.size()-1;

                //String temp = symStr+".";
                String temp = "";
                String find = null;

                //在对应状态的str中查找
                //state.getStr().forEach(s -> {
                //});   //foreach有一些条件限制
                for (int i = 0; i < symStack.size(); i++)
                {
                    //temp每一回合都要重置!
                    temp = "";
                    //取出对应的字符串
                    for (int j = index; j < symStack.size(); j++)
                    {
                        temp += symStack.get(j).getName();
                    }
                    temp += ".";

                    LinkedList<String> str = state.getStr();
                    for(String s:str)
                    {
                        //首先将字符串截断
                        String[] split = s.split("→");
                        //取出第二个元素来判断split[0]
                        String s1 = split[1];
                        //判断是否相等
                        if(s1.equals(temp))
                        {
                            //发现了可规约项目!
                            find = s;
                            break;
                        }
                    }

                    //判断是否发现了可规约项
                    if(find != null)
                        break;

                    //修改Index值
                    index--;
                }



                //遍历完成之后，若有可规约项 -> find != null
                if(find != null)
                {
                    //若存在可规约项 -> 在对应状态之中查询该规约字符串!
                    //peek = stateStack.peek();
                    //actGoTo = actGoToTable.get(peek);
                    //查询规约项
                    State state1 = actGoTo.get(find);
                    //得到了转换之后的状态
                    //这里由于存在移入规约冲突，因此需要考察一下follow集
                    //判断下一个输入的元素是否在当前规约的元素的follow集中
                    //取出待规约的元素 -> 若存在与其follow集之中 -> 进行规约
                    String[] split = find.split("→");
                    //取出规约之后的语法变量
                    String var = split[0];
                    //查询下一个待输入的元素
                    Word word = analysis.get(0);

                    //暂时跳过';'
                    /*if(word.getValue().equals(";"))
                        analysis.pollFirst();
                    word = analysis.get(0);*/

                    //判断是否为$
                    //if(!word.getValue().equals("$"))
                    {
                        //是否存在
                        boolean flag = false;
                        //查询其follow集
                        HashSet<String> strings = followMap.get(var);
                        for(String s:strings)
                        {
                            //针对于赋值语句的特殊处理 -> 赋值语句与if-else的隔离
                            /*if(find.equals("S→d=E;.") && word.getValue().equals("d"))
                            {
                                flag = true;        //要让他规约!
                                break;
                            }*/

                            if(s.equals(word.getValue()))
                            {
                                flag = true;

                                //针对于if - else的特殊处理 ->  ※
                                if(find.equals("S→f(C)S.") && word.getValue().equals("e"))
                                {
                                    flag = false;
                                    break;
                                }

                                break;
                            }

                        }
                        //遍历之后分析flag的情况
                        if(flag)
                        {
                            //倘若位于其follow集之中 -> 执行规约
                            //弹栈 -> 取出首元素
                            stateStack.pop();   //原状态弹出
                            //stateStack.push(state1);
                            //分析在state1状态得到var时的转换
                            //actGoTo = actGoToTable.get(state1);
                            //State state2 = actGoTo.get(var);
                            //将state2加入
                            //将state1加入!
                            stateStack.push(state1);
                            //删除符号栈中的内容 ->　将规约之后的内容加入其中
                            //针对于发现的表达式(项)进行分析
                            String[] split1 = find.split("→");
                            //取出被规约之后的符号 -> 等待被加入到符号栈之中
                            String s1 = split1[0];
                            //取出之后的规约表达式 -> 删除其对应的符号栈中的元素
                            String s2 = split1[1];
                            //当然要出去.的影响
                            s2 = s2.replaceAll("\\.","");
                            byte[] bytes = s2.getBytes();

                            //生成要规约为的符号
                            Symbol symbol = new Symbol(s1);

                            //在规约状态下，应当生成三地址代码!
                            this.createThreeAddressCode(find,symbol,wordStack,symStack);




                            for (int i = 0; i < bytes.length; i++)
                            {
                                //弹出对应的符号
                                symStack.pop();
                                //同时弹出对应的状态
                                stateStack.pop();
                            }
                            //将被规约的符号加入  -> 压入Symbol类对象
                            symStack.push(symbol);


                            //分析从该状态加入s1到达的状态!
                            state = stateStack.peek();
                            actGoTo = actGoToTable.get(state);
                            //判断一下s1是否为文法的开始符号
                            if(!s1.equals("P"))
                            {
                                State state2 = actGoTo.get(s1);
                                //将state2压入栈
                                stateStack.push(state2);
                                //修该对应的symStr
                                symStr = "";
                                for(Symbol s:symStack)
                                {
                                    symStr += s.getName();
                                }
                                //接下来进入下一轮分析
                            }
                        }
                        else
                        {
                            //若不位于follow集之中 -> 继续进行移入
                            //并不进行规约
                            Word word1 = analysis.pollFirst();

                            //暂时跳过';'
                            /*if(word.getValue().equals(";"))
                            {
                                analysis.pollFirst();
                                word1 = analysis.get(0);
                            }*/



                            //要不然就在这个地方加入一个单独处理赋值语句的算法
                            if(find.equals("S→d=E;."))
                            {
                                //将赋值语句加入三地址代码存储链表
                                addressCodeList.add("L"+ tag++ +": "+symStack.get(symStack.size()-4).getCode()+" = "+ symStack.get(symStack.size()-2).getCode());
                            }


                            //创建对象
                            Symbol symbol = new Symbol(word1.getValue());
                            if(word1.getType().equals("letter") || word1.getType().equals("digit"))
                                symbol.setCode(word1.getName());

                            //压入对象
                            symStack.push(symbol);
                            symStr += word1.getValue();
                            //移入之后状态发生变化 -> 不加入? --> 还是得加入
                            State peek1 = stateStack.peek();
                            HashMap<String, State> map = actGoToTable.get(peek1);
                            //读取字符之后转换为state2
                            State state2 = map.get(word1.getValue());
                            ///将state2压栈
                            stateStack.push(state2);


                            //将取出的word压入栈中
                            wordStack.push(word1);
                        }
                    }
                    //else
                    {
                        //若下一个字符为$
                        //只能进行规约 -> 针对于目前的符号栈中的内容执行规约
                        //But!  这个条件的前提是针对目前的状态有可规约项
                        //find == null 意味着无可规约项!
                        //执行规约!
                        //发现这个判断下一个字符是否为$的情况没啥作用!
                    }
                }
                else
                {
                    //find为Null -> 只能继续移入
                    Word word1 = analysis.pollFirst();
                    if(!word1.getValue().equals("$"))   //默认$不会出现在符号栈中
                    {
                        //暂时跳过';'
                        /*if(word1.getValue().equals(";"))
                        {
                            analysis.pollFirst();
                            word1 = analysis.get(0);
                        }*/

                        //创建并压入对象
                        Symbol symbol = new Symbol(word1.getValue());
                        if(word1.getType().equals("letter") || word1.getType().equals("digit"))
                            symbol.setCode(word1.getName());

                        symStack.push(symbol);
                        symStr += word1.getValue();
                        //移入之后状态发生变化 -> 不加入? --> 还是得加入
                        State peek1 = stateStack.peek();
                        HashMap<String, State> map = actGoToTable.get(peek1);
                        //读取字符之后转换为state2
                        State state2 = map.get(word1.getValue());
                        ///将state2压栈
                        stateStack.push(state2);

                        //将取出的word压入栈中
                        wordStack.push(word1);
                    }
                }

            }
        } catch (Exception e)
        {
            //e.printStackTrace();
            System.out.println("Syntax Errors!");
        }
        {
            if(this.ErrorTag == 0)
                return true;
            else
                return false;
        }
    }


    //加入一个三地址代码生成方法 -> 将表达式和符号栈,还必须有要规约为的符号! 传入进去
    //即将表达式与Token符号栈传入
    public void createThreeAddressCode(String exp,Symbol symbol,Stack<Word> wordStack,Stack<Symbol> symStack)
    {
        String temp = "";
        String value1 = "";
        String value2 = "";
        String group = "";
        String group1 = "";
        //首选分析表达式的类型 ->　直接上switch-case可否?
        switch (exp)
        {
            case "F→d.":
                //若为F -> d
                symbol.setCode(wordStack.peek().getName());
                break;
            case "F→g.":
                //若为F -> g
                symbol.setCode(wordStack.peek().getName());
                break;
            case "T→F.":
                //若为T -> F
                symbol.setCode(symStack.peek().getCode());
                break;
            case "E→T.":
                //若为E -> T
                symbol.setCode(symStack.peek().getCode());
                break;
            case "E→E+T.":
                //若为E -> E + T
                symbol.setCode("T"+label++);
                //此时初始位置定位: 需判断E和T之中是否含有value属性
                temp = "L"+ tag++ +": "+symbol.getCode()+" = "+symStack.get(symStack.size()-3).getCode()+" + "+symStack.peek().getCode();
                //判断是否存在更前的位置信息
                value1 = symStack.get(symStack.size() - 3).getValue();
                value2 = symStack.peek().getValue();
                if(value1 != null && value2 != null)
                {
                    //若两者都不为null -> 判断谁的位置更低
                    Matcher matcher1 = Pattern.compile("L.").matcher(value1);
                    if(matcher1.find())
                        group = matcher1.group(0);
                    Matcher matcher2 = Pattern.compile("L.").matcher(value2);
                    if(matcher2.find())
                        group1 = matcher2.group(0);
                    int i = Integer.parseInt(group);
                    int i1 = Integer.parseInt(group1);
                    //判断谁更低 -> 取最小者
                    if(i < i1)
                    {
                        symbol.setValue(value1);
                    }
                    else
                        symbol.setValue(value2);
                }
                else if(value1 == null && value2 == null)   //若都为空
                {
                    symbol.setValue(temp);      //直接设置为此条
                }
                else
                {
                    //若只有一个为空   -> 判断一下
                    if(value1 == null)
                        symbol.setValue(value2);
                    else
                        symbol.setValue(value1);
                }
                addressCodeList.add(temp);
                break;
            case "E→E-T.":
                //若为E -> E + T
                symbol.setCode("T"+label++);
                //此时初始位置定位: 需判断E和T之中是否含有value属性
                temp = "L"+ tag++ +": "+symbol.getCode()+" = "+symStack.get(symStack.size()-3).getCode()+" - "+symStack.peek().getCode();
                //判断是否存在更前的位置信息
                value1 = symStack.get(symStack.size() - 3).getValue();
                value2 = symStack.peek().getValue();
                if(value1 != null && value2 != null)
                {
                    //若两者都不为null -> 判断谁的位置更低
                    Matcher matcher1 = Pattern.compile("L.").matcher(value1);
                    if(matcher1.find())
                        group = matcher1.group(0);
                    Matcher matcher2 = Pattern.compile("L.").matcher(value2);
                    if(matcher2.find())
                        group1 = matcher2.group(0);

                    int j;  //别给我重复提醒!!!!!


                    int i = Integer.parseInt(group);
                    int i1 = Integer.parseInt(group1);
                    //判断谁更低 -> 取最小者
                    if(i < i1)
                    {
                        symbol.setValue(value1);
                    }
                    else
                        symbol.setValue(value2);
                }
                else if(value1 == null && value2 == null)   //若都为空
                {
                    symbol.setValue(temp);      //直接设置为此条
                }
                else
                {
                    //若只有一个为空   -> 判断一下
                    if(value1 == null)
                        symbol.setValue(value2);
                    else
                        symbol.setValue(value1);
                }
                addressCodeList.add(temp);
                break;
            case "T→T*F.":
                //若为T -> T * F
                symbol.setCode("T"+label++);
                //记录开始地点
                temp = "L"+ tag++ +": "+symbol.getCode()+" = "+symStack.get(symStack.size()-3).getCode()+" * "+symStack.peek().getCode();
                symbol.setValue(temp);
                addressCodeList.add(temp);
                break;
            case "T→T/F.":
                //若为T -> T / F
                symbol.setCode("T"+label++);
                //记录开始地点
                temp = "L"+ tag++ +": "+symbol.getCode()+" = "+symStack.get(symStack.size()-3).getCode()+" * "+symStack.peek().getCode();
                symbol.setValue(temp);
                addressCodeList.add(temp);
                break;
            case "S→d=E;.":
                temp = "L"+ tag++ +": "+symStack.get(symStack.size()-4).getCode()+" = "+ symStack.get(symStack.size()-2).getCode();
                addressCodeList.add(temp);
                //规约的时候也要判断
                if(symStack.get(symStack.size()-2).getValue() != null)
                    symbol.setValue(symStack.get(symStack.size()-2).getValue());
                else
                    symbol.setValue(temp);
                break;
            case "C→E>E.":
                symbol.setCode("T"+label++);
                addressCodeList.add("L"+ tag++ +": "+symbol.getCode()+" = "+symStack.get(symStack.size()-3).getCode()+" > "+symStack.peek().getCode());
                //布尔表达式需要记录一下该值
                //占位 -> 等待回填
                addressCodeList.add("L"+ tag++ +": ");
                //记录tag
                symbol.setTagIndex(tag-1);      //减一才是空位置!

                break;
            case "C→E<E.":
                symbol.setCode("T"+label++);
                addressCodeList.add("L"+ tag++ +": "+symbol.getCode()+" = "+symStack.get(symStack.size()-3).getCode()+" < "+symStack.peek().getCode());
                //布尔表达式需要记录一下该值
                //占位 -> 等待回填
                addressCodeList.add("L"+ tag++ +": ");
                //记录tag
                symbol.setTagIndex(tag-1);
                break;
            case "C→E&E.":
                symbol.setCode("T"+label++);
                addressCodeList.add("L"+ tag++ +": "+symbol.getCode()+" = "+symStack.get(symStack.size()-3).getCode()+" == "+symStack.peek().getCode());
                //布尔表达式需要记录一下该值
                //占位 -> 等待回填
                addressCodeList.add("L"+ tag++ +": ");
                //记录tag
                symbol.setTagIndex(tag-1);
                break;
            case "S→f(C)S.":
                //针对于if语句的规约
                //首先创建一个跳转到下下条语句的跳转语句
                addressCodeList.add("L"+ tag++ +": "+"goto "+(tag+2));

                //从S中获取其首代码地址  -> 可能为多位数!   //L10: a = c
                Matcher matcher = Pattern.compile("L\\d{1,4}").matcher(symStack.peek().getValue());
                if(matcher.find())
                {
                    group = matcher.group(0);
                }
                String temp1 = "L"+ tag++ +": "+ "if(" + symStack.get(symStack.size()-3).getCode()+") goto "+ group;
                symbol.setValue(symStack.peek().getValue());
                addressCodeList.add(temp1);

                //接下来对原来空位置进行回填
                //首先找到原来的位置
                int tagIndex = symStack.get(symStack.size() - 3).getTagIndex();
                //从三地址代码序列中寻找合适的语句
                int index = 0;
                for (int i = 0; i < addressCodeList.size(); i++)
                {
                    String s = addressCodeList.get(i);
                    Matcher matcher1 = Pattern.compile("L" + tagIndex + ": ").matcher(s);
                    if(matcher1.find())
                    {
                        //应当进行替换！
                        addressCodeList.remove(s);
                        index = i;
                        break;
                    }
                }
                //将原句子替换
                addressCodeList.add(index,"L"+tagIndex+": goto "+"L"+(tag-1));


                break;
            //symbol.setName(symStack.get(symStack.size()-3).getName());
            default:
                break;
        }

    }










    //进行语义处理及中间代码生成 -> 还是在语法分析的过程之中将语义处理加入其中
    public void semantic_Analysis()
    {
        //获取输入的Token队列
        System.out.println(tempList.size());
        //获取符号表 -> 此时声明语句及符号表都已经建立起来
        HashMap<String, TableNode> table = analyzer.getTable();



    }


    //getter & setter
    public ArrayList<String> getAddressCodeList() {
        return addressCodeList;
    }

    public void setAddressCodeList(ArrayList<String> addressCodeList) {
        this.addressCodeList = addressCodeList;
    }
}
