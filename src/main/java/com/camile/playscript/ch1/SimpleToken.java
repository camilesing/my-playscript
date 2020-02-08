package com.camile.playscript.ch1;

import com.camile.playscript.Token;
import com.camile.playscript.TokenType;

public class SimpleToken implements Token {
    //Token类型
    private TokenType type;

    //文本值
    private String text;

    public void setType(TokenType type) {
        this.type = type;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public TokenType getType() {
        return type;
    }

    @Override
    public String getText() {
        return text;
    }
}
