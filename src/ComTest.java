import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author LYHstart
 * @create 2021-11-18 12:37
 *
 * 整体测试方法
 */
public class ComTest
{
    //自动读取Input.txt中已经写入程序的方法
    public static void main(String[] args)
    {
        System.out.println("读入的字符串如下:");
        AnalysisInput analysisInput = new AnalysisInput();
        boolean r = analysisInput.inputAnalysis();
        System.out.println("语法分析结果如下:");
        if(r)
        {
            //进行语义处理...  --> 整成三地址代码的形式!
            //analysisInput.semantic_Analysis();

            System.out.println("Accept!");
            System.out.println("=======================");

            //打印生成的三地址代码
            analysisInput.getAddressCodeList().forEach(s -> {
                System.out.println(s);
            });
            System.out.println("========================");
            System.out.println("符号表内容如下:");

            analysisInput.getAnalyzer().printTable();
        }
        else
        {
            System.out.println("Syntax Errors!");
            System.out.println("=======================");
            System.exit(0);
        }
    }


    //手动输入程序的调用
    @Test
    public void test() throws IOException
    {
        LexicalAnalyzer analyzer = new LexicalAnalyzer();
        analyzer.inputWindow();

        System.out.println("读入的字符串如下:");
        AnalysisInput analysisInput = new AnalysisInput();
        boolean r = analysisInput.inputAnalysis();
        System.out.println("语法分析结果如下:");
        if(r)
        {
            //进行语义处理...  --> 整成三地址代码的形式!
            //analysisInput.semantic_Analysis();

            System.out.println("Accept!");
            System.out.println("========================");

            //打印生成的三地址代码
            analysisInput.getAddressCodeList().forEach(s2 -> {
                System.out.println(s2);
            });
            System.out.println("=========================");
            System.out.println("符号表内容如下:");

            analysisInput.getAnalyzer().printTable();
        }
        else
        {
            System.out.println("Syntax Errors!");
            System.out.println("========================");
            System.exit(0);
        }
    }
}
