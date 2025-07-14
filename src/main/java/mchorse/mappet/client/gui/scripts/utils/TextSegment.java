package mchorse.mappet.client.gui.scripts.utils;

import mchorse.mappet.client.gui.scripts.style.SyntaxHighlighter;

public class TextSegment
{
    public SyntaxHighlighter.TOKENS token;
    public String text;
    public int color;
    public int width;

    public TextSegment(SyntaxHighlighter.TOKENS token, String text, int color, int width)
    {
        this.token = token;
        this.text = text;
        this.color = color;
        this.width = width;
    }

    public boolean is(SyntaxHighlighter.TOKENS token){
        return this.token == token;
    }
}