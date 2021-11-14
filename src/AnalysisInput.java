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
                    //判断符号栈
                    if(stateStack.peek().getName().equals("P"))
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
            }

            //此时符号栈中一定有元素存在
            //分析字符串


            //根据当前的状态进行分析是否有对应的项
            HashMap<String, State> actGoTo = actGoToTable.get(peek);
            State state = actGoTo.get(symStr);
            if(state == null)
            {
                //若为空，不存在转换状态!
                System.out.println("syntax Error!");
            }
            else
            {

            }

        }


    }
}
