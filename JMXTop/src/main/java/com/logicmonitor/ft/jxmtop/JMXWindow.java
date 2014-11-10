package com.logicmonitor.ft.jxmtop;

import jcurses.system.InputChar;
import jcurses.system.Toolkit;
import jcurses.widgets.Window;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JMXWindow extends Window {
    public final static int KEY_CTRL_C = 3;
    public final static int KEY_LOW_Q = 113;
    public final static int KEY_UP_Q = 81;
    private int height = 0;
    private static int pageNum = 0;
    private List<InputChar> closingChars = new ArrayList<InputChar>();

    public JMXWindow(int x, int y, int width, int height, boolean border, String title) {
        super(x, y, width, height, border, title);
        this.height = Math.min(border ? height - 2 : height, Toolkit.getScreenHeight());
    }

    protected void handleInput(InputChar inp) {
        super.handleInput(inp);
        for (InputChar ch : closingChars) {
            if (inp.equals(ch)) {
                tryToClose();
            }
        }
        if (inp.equals(new InputChar(InputChar.KEY_DOWN))) {
            pageNum++;
        }
        else if (inp.equals(new InputChar(InputChar.KEY_UP))) {
            pageNum--;
            if (pageNum < 0) pageNum = 0;
        }
    }

    public int getHeight() {
        return height;
    }

    public static int getPageNum() {
        return pageNum;
    }

    public static void setPageNum(int pageNum) {
        JMXWindow.pageNum = pageNum;
    }

    public void addClosingChar(InputChar ch) {
        closingChars.add(ch);
    }
}
