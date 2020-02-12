package com.camile.playscript.ch1;


import com.camile.playscript.ASTNode;
import com.camile.playscript.ch3.SimpleParser;
import org.junit.jupiter.api.Test;

public class ParserTest {

    @Test
    public void test() {
        SimpleParser parser = new SimpleParser();
        String script;
        ASTNode tree;

        try {
            script = "int age = 45+2; age= 20; age+10*2;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {

            System.out.println(e.getMessage());
        }

        //测试异常语法
        try {
            script = "2+3+;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        //测试异常语法
        try {
            script = "2+3*;";
            System.out.println("解析："+script);
            tree = parser.parse(script);
            parser.dumpAST(tree, "");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}
