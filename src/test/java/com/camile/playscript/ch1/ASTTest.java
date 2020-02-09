package com.camile.playscript.ch1;


import com.camile.playscript.TokenReader;
import com.camile.playscript.ch2.SimpleASTNode;
import com.camile.playscript.ch2.SimpleCalculator;
import org.junit.jupiter.api.Test;

public class ASTTest {

    @Test
    public void test() {
        SimpleCalculator calculator = new SimpleCalculator();

        //测试变量声明语句的解析
        String script = "int a = b+3;";
        System.out.println("解析变量声明语句: " + script);
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(script);
        try {
            SimpleASTNode node = calculator.intDeclare(tokens);
            calculator.dumpAST(node,"");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        //测试表达式
        script = "2+3*5";
        System.out.println("\n计算: " + script + "，看上去一切正常。");
        calculator.evaluate(script);

        //测试语法错误
        script = "2+";
        System.out.println("\n: " + script + "，应该有语法错误。");
        calculator.evaluate(script);

        script = "2+3+4";
        System.out.println("\n计算: " + script + "，结合性出现错误。");
        calculator.evaluate(script);
    }
}
