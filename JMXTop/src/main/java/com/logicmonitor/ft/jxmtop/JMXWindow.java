package com.logicmonitor.ft.jxmtop;

import jcurses.system.InputChar;
import jcurses.system.Toolkit;
import jcurses.widgets.Window;


public class JMXWindow extends Window {
    private int height = 0;
    private static int pageNum = 0;
    public static int code = 0;
    public JMXWindow(int x, int y, int width, int height, boolean border, String title) {
        super(x, y, width, height, border, title);
        this.height = Math.min(border ? height - 2 : height, Toolkit.getScreenHeight());
    }

    protected void handleInput(InputChar inp) {
        super.handleInput(inp);
        if (inp.equals(new InputChar(InputChar.KEY_DOWN))) {
            pageNum++;
        }
        else if (inp.equals(new InputChar(InputChar.KEY_UP))) {
            pageNum--;
            if (pageNum < 0) pageNum = 0;
        }
        code = inp.getCode();
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
}
