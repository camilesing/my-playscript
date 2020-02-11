package com.camile.playscript.ch2;


import com.camile.playscript.ASTNode;
import com.camile.playscript.ASTNodeType;
import com.camile.playscript.Token;
import com.camile.playscript.TokenReader;
import com.camile.playscript.TokenType;
import com.camile.playscript.ch1.SimpleLexer;

/**
 * 实现一个计算器，但计算的结合性是有问题的。因为它使用了下面的语法规则：
 * <p>
 * additive -> multiplicative | multiplicative + additive
 * multiplicative -> primary | primary * multiplicative
 * <p>
 * 递归项在右边，会自然的对应右结合。我们真正需要的是左结合。
 *
 * 上下文无关文法之巴克斯范式表达法：
 *
 * add ::= mul | add + mul  add可以用mul or add + mul来标示。下面同理
 * mul ::= pri | mul * pri
 * pri ::= Id | Num | (add)
 *
 * 当遇到Id Num这种不可展开的文法时。我们称之为终结符
 */
public class SimpleCalculator {
    /**
     * 执行脚本，并打印输出AST和求值过程。
     *
     * @param script 脚本
     */
    public void evaluate(String script) {
        try {
            ASTNode tree = parse(script);

            dumpAST(tree, "");
            evaluate(tree, "");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 解析脚本，并返回根节点
     *
     * @param code 代码
     * @return ASTNode
     * @throws Exception
     */
    public ASTNode parse(String code) throws Exception {
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(code);
        ASTNode rootNode = prog(tokens);
        return rootNode;
    }

    /**
     * 对某个AST节点求值，并打印求值过程。
     *
     * @param node   抽象语法树节点
     * @param indent 打印输出时的缩进量，用tab控制
     * @return
     */
    private int evaluate(ASTNode node, String indent) {
        int result = 0;
        System.out.println(indent + "Calculating: " + node.getType());
        switch (node.getType()) {
            case Programm:
                for (ASTNode child : node.getChildren()) {
                    result = evaluate(child, indent + "\t");
                }
                break;
            case Additive:
                ASTNode child1 = node.getChildren().get(0);
                int value1 = evaluate(child1, indent + "\t");
                ASTNode child2 = node.getChildren().get(1);
                int value2 = evaluate(child2, indent + "\t");
                if ("+".equals(node.getText())) {
                    result = value1 + value2;
                } else {
                    result = value1 - value2;
                }
                break;
            case Multiplicative:
                child1 = node.getChildren().get(0);
                value1 = evaluate(child1, indent + "\t");
                child2 = node.getChildren().get(1);
                value2 = evaluate(child2, indent + "\t");
                if ("*".equals(node.getText())) {
                    result = value1 * value2;
                } else {
                    result = value1 / value2;
                }
                break;
            case IntLiteral:
                result = Integer.parseInt(node.getText());
                break;
            default:
        }
        System.out.println(indent + "Result: " + result);
        return result;
    }

    /**
     * 语法解析：根节点
     *
     * @return
     * @throws Exception
     */
    private SimpleASTNode prog(TokenReader tokens) throws Exception {
        SimpleASTNode node = new SimpleASTNode(ASTNodeType.Programm, "Calculator");
        SimpleASTNode child = additive(tokens);
        if (child != null) {
            node.addChild(child);
        }
        return node;
    }

    /**
     * 整型变量声明语句，如：
     * int a;
     * int b = 2*3;
     *
     * @return
     * @throws Exception
     */
    public SimpleASTNode intDeclare(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        //预读
        Token token = tokens.peek();
        if (token != null && token.getType() == TokenType.Int) {
            //匹配Int
            tokens.read();
            //匹配标识符
            if (tokens.peek().getType() == TokenType.Identifier) {
                //消耗掉标识符
                token = tokens.read();
                //创建当前节点，并把变量名记到AST节点的文本值中，这里新建一个变量子节点也是可以的
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
                //预读
                token = tokens.peek();
                if (token != null && token.getType() == TokenType.Assignment) {
                    //消耗掉等号
                    tokens.read();
                    //匹配一个表达式
                    SimpleASTNode child = additive(tokens);
                    if (child == null) {
                        throw new Exception("invalide variable initialization, expecting an expression");
                    } else {
                        node.addChild(child);
                    }
                }
            } else {
                throw new Exception("variable name expected");
            }

            token = tokens.peek();
            if (token != null && token.getType() == TokenType.SemiColon) {
                tokens.read();
            } else {
                throw new Exception("invalid statement, expecting semicolon");
            }
        }
        return node;
    }

    /**
     * 语法解析：加法表达式
     *
     * @return
     * @throws Exception
     */

    private SimpleASTNode additive(TokenReader tokens) throws Exception {
        //add -> mul (+ mul)*
        //即一个add文法可以转换成一个mul文法跟着出现0次+的 (+ mul)
        SimpleASTNode child1 = multiplicative(tokens);
        SimpleASTNode node = child1;
        if (child1 != null) {
            while (true) {
                Token token = tokens.peek();
                //是否有(+ mul)出现
                if (token == null || (token.getType() != TokenType.Plus && token.getType() != TokenType.Minus)) {
                    break;
                }
                //读出加号
                token = tokens.read();
                //计算下级节点
                SimpleASTNode child2 = multiplicative(tokens);
                node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                //注意，新节点在顶层，保证正确的结合性
                node.addChild(child1);
                node.addChild(child2);
                child1 = node;
            }
        }
        return node;
    }

    /**
     * 语法解析：乘法表达式
     *
     * @return SimpleASTNode 最简单的语法树
     * @throws Exception
     */
    private SimpleASTNode multiplicative(TokenReader tokens) throws Exception {
        SimpleASTNode child1 = primary(tokens);
        SimpleASTNode node = child1;

        Token token = tokens.peek();
        if (child1 != null && token != null) {
            if (token.getType() == TokenType.Star || token.getType() == TokenType.Slash) {
                token = tokens.read();
                SimpleASTNode child2 = primary(tokens);
                if (child2 != null) {
                    node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                } else {
                    throw new Exception("invalid multiplicative expression, expecting the right part.");
                }
            }
        }
        return node;
    }

    /**
     * 语法解析：基础表达式
     *
     * @return
     * @throws Exception
     */
    private SimpleASTNode primary(TokenReader tokens) throws Exception {
        SimpleASTNode node = null;
        Token token = tokens.peek();
        if (token != null) {
            if (token.getType() == TokenType.IntLiteral) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
            } else if (token.getType() == TokenType.Identifier) {
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());
            } else if (token.getType() == TokenType.LeftParen) {
                tokens.read();
                node = additive(tokens);
                if (node != null) {
                    token = tokens.peek();
                    if (token != null && token.getType() == TokenType.RightParen) {
                        tokens.read();
                    } else {
                        throw new Exception("expecting right parenthesis");
                    }
                } else {
                    throw new Exception("expecting an additive expression inside parenthesis");
                }
            }
        }
        //这个方法也做了AST的简化，就是不用构造一个primary节点，直接返回子节点。因为它只有一个子节点。
        return node;
    }


    /**
     * 打印输出AST的树状结构
     *
     * @param node   语法树节点
     * @param indent 缩进字符，由tab组成，每一级多一个tab
     */
    public void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + "\t");
        }
    }
}
