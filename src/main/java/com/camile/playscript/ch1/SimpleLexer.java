package com.camile.playscript.ch1;

import com.camile.playscript.Token;
import com.camile.playscript.TokenType;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 简单的词法解析器
 * */
public class SimpleLexer {
    //下面几个变量是在解析过程中用到的临时变量,如果要优化的话，可以塞到方法里隐藏起来
    //临时保存token的文本
    private StringBuffer tokenText = null;
    //保存解析出来的Token
    private List<Token> tokens = null;
    //当前正在解析的Token
    private SimpleToken token = null;

    //是否是字母
    private boolean isAlpha(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    //是否是数字
    private boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    //是否是空白字符
    private boolean isBlank(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    /**
     * 有限状态机进入初始状态。
     * 这个初始状态其实并不做停留，它马上进入其他状态。
     * 开始解析的时候，进入初始状态；某个Token解析完毕，也进入初始状态，在这里把Token记下来，然后建立一个新的Token。
     *
     * @param ch
     * @return
     */
    private DfaState initToken(char ch) {
        if (tokenText.length() > 0) {
            token.setText(tokenText.toString());
            tokens.add(token);

            tokenText = new StringBuffer();
            token = new SimpleToken();
        }

        DfaState newState;
        /*
         * 状态迁移
         * */
        if (isAlpha(ch)) {              //第一个字符是字母
            if (ch == 'i') {
                newState = DfaState.Id_int1;
            } else {
                newState = DfaState.Id; //进入Id状态
            }
            token.setType(TokenType.Identifier);
            tokenText.append(ch);
        } else if (isDigit(ch)) {       //第一个字符是数字
            newState = DfaState.IntLiteral;
            token.setType(TokenType.IntLiteral);
            tokenText.append(ch);
        } else if (ch == '>') {         //第一个字符是>
            newState = DfaState.GT;
            token.setType(TokenType.GT);
            tokenText.append(ch);
        } else if (ch == '+') {
            newState = DfaState.Plus;
            token.setType(TokenType.Plus);
            tokenText.append(ch);
        } else if (ch == '-') {
            newState = DfaState.Minus;
            token.setType(TokenType.Minus);
            tokenText.append(ch);
        } else if (ch == '*') {
            newState = DfaState.Star;
            token.setType(TokenType.Star);
            tokenText.append(ch);
        } else if (ch == '/') {
            newState = DfaState.Slash;
            token.setType(TokenType.Slash);
            tokenText.append(ch);
        } else if (ch == ';') {
            newState = DfaState.SemiColon;
            token.setType(TokenType.SemiColon);
            tokenText.append(ch);
        } else if (ch == '(') {
            newState = DfaState.LeftParen;
            token.setType(TokenType.LeftParen);
            tokenText.append(ch);
        } else if (ch == ')') {
            newState = DfaState.RightParen;
            token.setType(TokenType.RightParen);
            tokenText.append(ch);
        } else if (ch == '=') {
            newState = DfaState.Assignment;
            token.setType(TokenType.Assignment);
            tokenText.append(ch);
        } else {
            // skip all unknown patterns
            newState = DfaState.Initial;
        }
        return newState;
    }

    /**
     * 解析字符串，形成Token。
     * 这是一个有限状态自动机，在不同的状态中迁移。
     *
     * @param code
     * @return
     */
    public SimpleTokenReader tokenize(String code) {
        tokens = new ArrayList<>();
        CharArrayReader reader = new CharArrayReader(code.toCharArray());
        tokenText = new StringBuffer();
        token = new SimpleToken();
        int ich = 0;
        char ch = 0;
        DfaState state = DfaState.Initial;
        try {
            while ((ich = reader.read()) != -1) {
                ch = (char) ich;
                switch (state) {
                    case Initial:
                        //重新确定后续状态
                        state = initToken(ch);
                        break;
                    case Id:
                        if (isAlpha(ch) || isDigit(ch)) {
                            //保持标识符状态
                            tokenText.append(ch);
                        } else {
                            //退出标识符状态，并保存Token
                            state = initToken(ch);
                        }
                        break;
                    case GT:
                        if (ch == '=') {
                            //转换成GE
                            token.setType(TokenType.GE);
                            state = DfaState.GE;
                            tokenText.append(ch);
                        } else {
                            //退出GT状态，并保存Token
                            state = initToken(ch);
                        }
                        break;
                    case GE:
                    case Assignment:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case SemiColon:
                    case LeftParen:
                    case RightParen:
                        //退出当前状态，并保存Token
                        state = initToken(ch);
                        break;
                    case IntLiteral:
                        if (isDigit(ch)) {
                            //继续保持在数字字面量状态
                            tokenText.append(ch);
                        } else {
                            //退出当前状态，并保存Token
                            state = initToken(ch);
                        }
                        break;
                    case Id_int1:
                        if (ch == 'n') {
                            state = DfaState.Id_int2;
                            tokenText.append(ch);
                        } else if (isDigit(ch) || isAlpha(ch)) {
                            //切换回Id状态
                            state = DfaState.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        if (ch == 't') {
                            state = DfaState.Id_int3;
                            tokenText.append(ch);
                        } else if (isDigit(ch) || isAlpha(ch)) {
                            //切换回id状态
                            state = DfaState.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        if (isBlank(ch)) {
                            token.setType(TokenType.Int);
                            state = initToken(ch);
                        } else {
                            //切换回Id状态
                            state = DfaState.Id;
                            tokenText.append(ch);
                        }
                        break;
                    default:

                }

            }
            // 把最后一个token送进去
            if (tokenText.length() > 0) {
                initToken(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SimpleTokenReader(tokens);
    }

    /**
     * 打印所有的Token
     *
     * @param tokenReader
     */
    public void dump(SimpleTokenReader tokenReader) {
        System.out.println("Dump token and type ...");
        Token token;
        while ((token = tokenReader.read()) != null) {
            System.out.println(token.getText() + "\t\t" + token.getType());
        }
    }
}
