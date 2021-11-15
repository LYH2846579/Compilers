import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @author LYHstart
 * @create 2021-11-14 21:15
 *
 * //语法分析总控程序 -> 读取用户的输入信息，进行规约
 */
public class AnalysisInput
{
    private LexicalAnalyzer analyzer;
    private ParseGrammar grammar;

    //存储当前状态的栈
    private Stack<State> stateStack = new Stack<>();
    //存储符号的栈
    private Stack<String> symStack = new Stack<>();

    //维护一个字符串用于记录符号栈中对应的字符序列
    private String symStr = new String();


    public AnalysisInput() {
    }
    public AnalysisInput(LexicalAnalyzer analyzer, ParseGrammar grammar) {
        this.analyzer = analyzer;
        this.grammar = grammar;
    }

    //进入输入分析阶段
    public void inputAnalysis()
    {
        //词法分析预处理
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.analysis();
        analyzer.createTable();
        //编码
        analyzer.analysisDeal();

        //语法分析预处理
        ParseGrammar parseGrammar = new ParseGrammar();
        parseGrammar.parse();
        parseGrammar.stateTransition();
        parseGrammar.firstCollection();
        parseGrammar.followCollection();
        parseGrammar.createActionGoToTable();

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
                    if(symStack.peek().equals("P"))
                    {
                        System.out.println("Accept");
                        //退出循环
                        break;
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
                //将其加入到符号栈中
                symStack.push(word.getValue());
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
                    temp += symStack.get(j);
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
                        for (int i = 0; i < bytes.length; i++)
                        {
                            //弹出对应的符号
                            symStack.pop();
                            //同时弹出对应的状态
                            stateStack.pop();
                        }
                        //将被规约的符号加入
                        symStack.push(s1);
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
                            for(String s:symStack)
                            {
                                symStr += s;
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

                        symStack.push(word1.getValue());
                        symStr += word1.getValue();
                        //移入之后状态发生变化 -> 不加入? --> 还是得加入
                        State peek1 = stateStack.peek();
                        HashMap<String, State> map = actGoToTable.get(peek1);
                        //读取字符之后转换为state2
                        State state2 = map.get(word1.getValue());
                        ///将state2压栈
                        stateStack.push(state2);
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

                    symStack.push(word1.getValue());
                    symStr += word1.getValue();
                    //移入之后状态发生变化 -> 不加入? --> 还是得加入
                    State peek1 = stateStack.peek();
                    HashMap<String, State> map = actGoToTable.get(peek1);
                    //读取字符之后转换为state2
                    State state2 = map.get(word1.getValue());
                    ///将state2压栈
                    stateStack.push(state2);
                }
            }

        }
    }
}
