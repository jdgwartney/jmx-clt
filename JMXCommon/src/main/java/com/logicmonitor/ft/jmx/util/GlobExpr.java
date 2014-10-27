package com.logicmonitor.ft.jmx.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * {@code GlobExpr} represent a globbing expression follwoing
 * the specification
 * <p/>
 * 1. ? matches any one character
 * 2. * matches any number of characters
 * 3. [abc] matches any character in the set a, b, or c
 * 4. [^abc] matches any character not in the set a, b, or c
 * 5. [a-z] matches any character in the range a to z, inclusive.
 * 6. support the following escapes
 * \w matches any alphanumeric character or underscore
 * \s matchs a space or tab
 * \S matches a printable non-whitespace
 * \d matches a decimal digit
 * \\ matches '\'
 * \| matches '|'
 * 7. globexpr1|globexpr2|globexpr3
 * 8. negative glob expression !globeexpr1|globexpr2|globexpr3
 * <p/>
 * Note
 * GlobExpr doesn't support escape between '[' and ']'. For example, "[\w\d]"
 * won't be processed correctly.
 * GlobExpr support more than one ranges. For example, [a-cd-e] is invalid
 */
public class GlobExpr {
    /**
     * A globbing expression is split into multiple simple globbing expressions.
     * "ab|a*d|[0-9]?kc" will be split into three simple globbing expressions -
     * "ab", "a*d", and "[0-9]?kx", for example.
     * <p/>
     * Each simple globbing expression will be represented by a list of tokens such
     * as "[0-9]", "?", "k", and "x", each of which, except '*' will match one character.
     */
    final private String _expr;
    final private ArrayList<SimpleGlobExpr> _simpleExprs = new ArrayList<SimpleGlobExpr>();
    private boolean _negative = false;

    /**
     * Globbing expression, full match version
     * @param expr The globbing expression
     * @throws NullPointerException     if {@code expr} is null
     * @throws IllegalArgumentException if {@code expr} is not valid globbing expression
     */
    public GlobExpr(String expr) {
        this(expr, true, true); // full match
    }

    /**
     * Globbing expression, partial match version
     * @param expr The globbing expression
     * @param from_head Match from the header
     * @param to_tail Match to the tail
     * @throws NullPointerException     if {@code expr} is null
     * @throws IllegalArgumentException if {@code expr} is not valid globbing expression
     */
    public GlobExpr(String expr, boolean from_head, boolean to_tail) {
        if (expr == null) {
            throw new NullPointerException();
        }

        _expr = StringUtil.strip(expr);

        ArrayList<Token> tokens = new ArrayList<Token>();
        for (Token token : _tokenize()) {
            if (token.code == Token.CODE_OR) {
                if (tokens.size() == 0) {
                    throw new IllegalArgumentException("Empty simple globbing expression");
                }
                _adjustSimpleExpr(tokens, from_head, to_tail);
                _simpleExprs.add(new SimpleGlobExpr(tokens.toArray(new Token[0])));
                tokens.clear();
            }
            else if (token.code == Token.CODE_NEGATIVE) {
                _negative = true;
            }
            else {
                tokens.add(token);
            }
        }

        if (tokens.size() > 0) {
            _adjustSimpleExpr(tokens, from_head, to_tail);
            _simpleExprs.add(new SimpleGlobExpr(tokens.toArray(new Token[0])));
        }
        else {
            throw new IllegalArgumentException("Empty simple globbing expression");
        }
    }

    public String matchAndCapture(String s) {
        if (_negative || _simpleExprs.size() > 1) {
            throw new IllegalArgumentException("Don't support capture for negative or composed glob expression");
        }
        return _simpleExprs.get(0).testAndCapture(s);
    }


    public boolean isNegative() {
        return _negative;
    }


    /**
     * add the prefix and suffix "*" for partial match
     *
     */
    private void _adjustSimpleExpr(ArrayList<Token> tokens, boolean from_head, boolean to_tail) {
        if (tokens.size() > 0) {
            if(!from_head && tokens.get(0).code != Token.CODE_STAR) {
                Token add_token = new Token(Token.CODE_STAR);
                add_token._do_capture = false;
                tokens.add(0, add_token);
            }
            if(!to_tail && tokens.get(tokens.size() - 1).code != Token.CODE_STAR) {
                Token add_token = new Token(Token.CODE_STAR);
                add_token._do_capture = false;
                tokens.add(add_token);
            }
        }
    }
    /**
     * Split a string into a list of tokens
     *
     * @return a list of tokens
     * @throws IllegalArgumentException if {@code _expr} has invalid characters.
     */
    private Token[] _tokenize() {
        ArrayList<Token> tokens = new ArrayList<Token>();

        int i = 0;
        while (i < _expr.length()) {
            char ch = _expr.charAt(i);
            if (ch == '\\') {
                i++;
                if (i == _expr.length()) {
                    throw new IllegalArgumentException("Invalid escape character");
                }
                tokens.add(new Token("\\" + _expr.charAt(i)));
                i++;
            }
            else if (ch == '[') {
                StringBuilder tmp = new StringBuilder("[");
                int j = i + 1;
                while (j < _expr.length()) {
                    char ch1 = _expr.charAt(j);
                    tmp.append(ch1);
                    if (ch1 == ']') {
                        tokens.add(new Token(tmp.toString()));
                        i = j + 1;
                        break;
                    }
                    j++;
                }
                if (tmp.charAt(tmp.length() - 1) != ']') { // user doesn't enter ']'
                    throw new IllegalArgumentException("No close ]");
                }
            }
            else if (ch == '!') {
                if (i == 0) {
                    tokens.add(new Token(Token.CODE_NEGATIVE));
                }
                else {
                    tokens.add(new Token(ch + ""));
                }

                i++;
            }
            else {
                tokens.add(new Token(ch + ""));
                i++;
            }
        }

        return tokens.toArray(new Token[0]);
    }


    /**
     * Check if the given string matches the globbing expression
     *
     * @param s the string
     * @return true if matches, false otherwise
     * @throws NullPointerException if s is null
     */
    public boolean test(String s) {
        boolean r = _test(s);
        return _negative ? !r : r;
    }

    private boolean _test(String s) {
        if (s == null) {
            throw new NullPointerException();
        }

        for (SimpleGlobExpr sge : _simpleExprs) {
            if (sge.test(s)) {
                return true;
            }
        }

        return false;
    }

    // ------------------------------------------------------------------------

    private static class Token {
        /**
         * Definitions for code
         */
        static final public int CODE_OR = 2; // '|'
        static final public int CODE_SPACE = 3; // SPACE or TAB
        static final public int CODE_QUESTION = 4; // '?'
        static final public int CODE_STAR = 5; // '*'
        static final public int CODE_CHAR = 6; // a specific character, check 'ch' for value
        static final public int CODE_ALPHANUMERIC = 7; // any alphanumeric letter and underscore
        static final public int CODE_NONSPACE = 8;
        static final public int CODE_DIGIT = 9;
        static final public int CODE_MULTICHOICE = 10; // [abc], [^abc], or [1-9]
        static final public int CODE_NEGATIVE = 11;

        int code;
        char ch;
        MultiChoiceToken _multiChoiceToken;
        boolean  _do_capture; // capture the string for 'CODE_STAR' in default.

        public Token(int code) {
            this.code = code;
            _do_capture = false;
            if(code == CODE_STAR) {
                _do_capture = true;
            }
        }

        public Token(String s) {
            _do_capture = false;
            if (s.length() == 1) {
                if (s.equals("*")) {
                    code = CODE_STAR;
                    _do_capture = true;
                }
                else if (s.equals("|")) {
                    code = CODE_OR;
                }
                else if (s.equals("?")) {
                    code = CODE_QUESTION;
                }
                else {
                    code = CODE_CHAR;
                    ch = s.charAt(0);
                }
            }
            else if (s.length() == 2) { // it must be '\'
                char ch0 = s.charAt(0);
                char ch1 = s.charAt(1);
                if (ch0 != '\\') {
                    throw new IllegalArgumentException("Invalid token " + s);
                }

                switch (ch1) {
                    case 'w':
                        code = CODE_ALPHANUMERIC;
                        break;
                    case 's':
                        code = CODE_SPACE;
                        break;
                    case 'S':
                        code = CODE_NONSPACE;
                        break;
                    case 'd':
                        code = CODE_DIGIT;
                        break;
                    case '\\':
                        code = CODE_CHAR;
                        ch = '\\';
                        break;
                    case '|':
                        code = CODE_CHAR;
                        ch = '|';
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid token " + s);
                }
            }
            else { // must be "[...]"
                if (s.charAt(0) != '[' ||
                        s.length() < 3 ||
                        s.charAt(s.length() - 1) != ']') {
                    throw new IllegalArgumentException("Invalid token " + s);
                }

                _multiChoiceToken = new MultiChoiceToken(s.substring(1, s.length() - 1));
                code = CODE_MULTICHOICE;
            }
        }

        /**
         * Check to see if c matches this token
         *
         * @param c the character
         * @return true if matches, false otherwise
         * @throws IllegalStateException if code is CODE_OR
         */
        public boolean matches(char c) {
            switch (code) {
                case CODE_OR:
                    throw new IllegalStateException("| is illegal");
                case CODE_SPACE:
                    return c == ' ' || ch == '\t';
                case CODE_QUESTION:
                    return true;
                case CODE_STAR:
                    return true;
                case CODE_CHAR:
                    return c == ch;
                case CODE_ALPHANUMERIC:
                    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || (c >= '0' && c <= '9');
                case CODE_NONSPACE:
                    return c != ' ' && c != '\t';
                case CODE_DIGIT:
                    return c >= '0' && c <= '9';
                case CODE_MULTICHOICE:
                    return _multiChoiceToken.matches(c);
                default:
                    throw new IllegalStateException("Invalid code " + code);
            }
        }
    }

    /**
     * A MultiChoiceToken represents a token such as [abc], [^abc], or [1-9]
     */
    static class MultiChoiceToken {
        boolean negative = false;
        boolean range = false;
        char bch;
        char ech;
        Set<Character> chs = new HashSet<Character>();

        public MultiChoiceToken(String s) {
            negative = (s.charAt(0) == '^');
            if (negative) {
                s = s.substring(1);
            }

            if (s.length() == 0) {
                throw new IllegalArgumentException("Invalid token " + s);
            }

            int pos = s.indexOf('-');
            if (pos == 1 && s.length() == 3) {
                range = true;
                bch = s.charAt(0);
                ech = s.charAt(2);
            }
            else {
                for (int i = 0; i < s.length(); i++) {
                    chs.add(s.charAt(i));
                }
            }
        }

        public boolean matches(char c) {
            boolean included = false;
            if (range) {
                included = (c >= bch && c <= ech);
            }
            else {
                included = chs.contains(c);
            }

            return negative ? !included : included;
        }
    }

    // ------------------------------------------------------------------------

    /**
     * {code SimpleGlobExpr} is a globbing expression w/o '|'.
     */
    private static class SimpleGlobExpr {
        final private Token[] _tokens;

        public SimpleGlobExpr(Token[] tokens) {
            _tokens = tokens;
        }

        /**
         * Check if the given string matches the globbing expression
         *
         * @param s the string
         * @return true if matches, false otherwise
         * @throws NullPointerException if s is null
         */
        public boolean test(String s) {
            if (s == null) {
                throw new NullPointerException();
            }

            return _test(_tokens, 0, s, 0, null);
        }

        public String testAndCapture(String s) {
            StringBuilder buf = new StringBuilder();
            if (_test(_tokens, 0, s, 0, buf)) {
                return buf.toString();
            }
            return null;
        }

        private boolean _test(Token[] tokens, int tk_p, String text, int str_p, StringBuilder buf) {
            for (; tk_p < tokens.length && str_p <= text.length(); str_p++, tk_p++) {
                Token token = tokens[tk_p];

                if (token.code != Token.CODE_STAR) {
                    if (text.length() <= str_p || !token.matches(text.charAt(str_p))) {
                        return false;
                    }
                } else {
                    if (tk_p + 1 == tokens.length) {
                        if (buf != null && token._do_capture) {
                            buf.insert(0, text.substring(str_p));
                        }
                        return true;
                    }

                    for (int k = 0; str_p + k < text.length(); k++) {
                        if (_test(tokens, tk_p + 1, text, str_p + k, buf)) {
                            if (buf != null && token._do_capture) {
                                buf.insert(0, text.substring(str_p, str_p + k));
                            }
                            return true;
                        }
                    }
                }
            } // end-for

            return tk_p == tokens.length && str_p == text.length();
        }
    }
}
