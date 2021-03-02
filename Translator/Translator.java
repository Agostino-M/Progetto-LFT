package Translator;

import java.io.*;
import Lexer.*;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count = 0;

    public Translator(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() {
        look = lex.lexical_scan(pbr);
        System.out.println("token = " + look);
    }

    void error(String s) {
        throw new Error("near line " + Lexer.line + ": " + s + " token: " + look.tag);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF)
                move();
        } else
            error("Syntax Error: instead of " + t + " found");
    }

    public void prog() {
        switch (look.tag) {
            // <prog> → <statlist> EOF
            case '=', Tag.PRINT, Tag.READ, Tag.COND, Tag.WHILE, '{': {
                statlist();
                match(Tag.EOF);
                try {
                    code.toJasmin();
                } catch (java.io.IOException e) {
                    System.out.println("IO error\n");
                }
                break;
            }
            default:
                error("Prog");
        }
    }

    private void statlist() {
        switch (look.tag) {
            // <statlist> → <stat> <statlistp>
            case '=', Tag.PRINT, Tag.READ, Tag.COND, Tag.WHILE, '{':
                stat();
                statlistp();
                break;

            default:
                error("Statlist");
        }
    }

    private void statlistp() {
        switch (look.tag) {
            // <statlistp> → ; <stat> <statlistp>
            case ';':
                match(';');
                stat();
                statlistp();
                break;

            // <statlistp> → ε
            case Tag.EOF, '}':
                break;

            default:
                error("Statlistp");
        }
    }

    public void stat() {
        switch (look.tag) {
            // <stat> → = ID <expr>
            case '=': {
                match('=');
                if (look.tag == Tag.ID) { // controllo se ID esiste nella symbol table
                    int id_addr = st.lookupAddress(((Word) look).lexeme); // lo cerco nella ST
                    if (id_addr == -1) { // non esiste
                        id_addr = count;
                        st.insert(((Word) look).lexeme, count++); // lo creo e inserisco nella ST
                    }
                    match(Tag.ID);
                    expr();
                    code.emit(OpCode.istore, id_addr);
                } else // non ho trovato un tag.ID
                    error("Error in grammar (stat) after = with " + look);
                break;
            }

            // <stat> → print(<exprlist>)
            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');
                exprlist(1); // indico che sto facendo una stampa
                match(')');
                break;

            // <stat> → read(ID)
            case Tag.READ:
                match(Tag.READ);
                match('(');
                if (look.tag == Tag.ID) {
                    int id_addr = st.lookupAddress(((Word) look).lexeme);
                    if (id_addr == -1) {
                        id_addr = count;
                        st.insert(((Word) look).lexeme, count++);
                    }
                    match(Tag.ID);
                    match(')');
                    code.emit(OpCode.invokestatic, 0);
                    code.emit(OpCode.istore, id_addr);
                } else
                    error("Error in grammar (stat) after read( with " + look);
                break;

            // <stat> → cond <whenlist> else <stat>
            case Tag.COND:
                int stat_next = code.newLabel(); // etichetta dello stat successivo al cond
                match(Tag.COND);
                whenlist(stat_next);
                match(Tag.ELSE);
                stat();
                code.emitLabel(stat_next);
                break;

            // <stat> → while(<bexpr>) <stat>
            case Tag.WHILE: {
                match(Tag.WHILE);
                match('(');
                int bexpr_true = code.newLabel();
                int bexpr_false = code.newLabel();
                int stat1_next = code.newLabel();
                code.emitLabel(stat1_next);
                bexpr(bexpr_true, bexpr_false);
                match(')');
                code.emitLabel(bexpr_true);
                stat();
                code.emit(OpCode.GOto, stat1_next);
                code.emitLabel(bexpr_false);
                break;
            }

            // <stat> → {<statlist>}
            case '{':
                match('{');
                statlist();
                match('}');
                break;

            default:
                error("Stat");
        }
    }

    private void whenlist(int whenlist_next) {
        switch (look.tag) {
            // <whenlist> → <whenitem> <whenlistp>
            case Tag.WHEN:
                whenitem(whenlist_next);
                whenlistp(whenlist_next);
                break;

            default:
                error("Whenlist");
        }
    }

    private void whenlistp(int whenlistp_next) {
        switch (look.tag) {
            // <whenlistp> → <whenitem> <whenlistp>
            case Tag.WHEN:
                whenitem(whenlistp_next);
                whenlistp(whenlistp_next);
                break;

            // <whenlistp> → ε
            case Tag.ELSE:
                break;

            default:
                error("whenlist error");
        }
    }

    private void whenitem(int whenitem_next) {
        switch (look.tag) {
            // <whenitem> → when(<bexpr>) do <stat>
            case Tag.WHEN:
                match(Tag.WHEN);
                match('(');

                int bexpr_true = code.newLabel();
                int bexpr_false = code.newLabel();
                bexpr(bexpr_true, bexpr_false);

                match(')');
                match(Tag.DO);

                code.emitLabel(bexpr_true);
                stat();
                code.emit(OpCode.GOto, whenitem_next);// salto else
                code.emitLabel(bexpr_false);
                break;

            default:
                error("Whenitem");
        }
    }

    private void bexpr(int bexpr_true, int bexpr_next) {
        switch (look.tag) {
            // <bexpr> → RELOP <expr> <expr>
            case Tag.RELOP:
                String relop = ((Word) look).lexeme;
                match(Tag.RELOP);
                expr();
                expr();

                switch (relop) {
                    case ">":
                        code.emit(OpCode.if_icmpgt, bexpr_true);
                        break;
                    case "<":
                        code.emit(OpCode.if_icmplt, bexpr_true);
                        break;
                    case "==":
                        code.emit(OpCode.if_icmpeq, bexpr_true);
                        break;
                    case ">=":
                        code.emit(OpCode.if_icmpge, bexpr_true);
                        break;
                    case "<=":
                        code.emit(OpCode.if_icmple, bexpr_true);
                        break;
                    case "<>":
                        code.emit(OpCode.if_icmpne, bexpr_true);
                        break;
                }
                code.emit(OpCode.GOto, bexpr_next);
                break;

            default:
                error("Bexpr");
        }
    }

    private void expr() {
        switch (look.tag) {
            // <expr> → + <exprlist>
            case '+':
                match('+');
                match('(');
                exprlist(2); // indico che sto facendo una somma
                match(')');
                break;

            // <expr> → * <exprlist>
            case '*':
                match('*');
                match('(');
                exprlist(3); // indico che sto facendo una moltiplicazione
                match(')');
                break;

            // <expr> → - <expr> <expr>
            case '-':
                match('-');
                expr();
                expr();
                code.emit(OpCode.isub);
                break;

            // <expr> → / <expr> <expr>
            case '/':
                match('/');
                expr();
                expr();
                code.emit(OpCode.idiv);
                break;

            // <expr> → NUM
            case Tag.NUM: {
                code.emit(OpCode.ldc, ((NumberTok) look).numLexeme);
                match(Tag.NUM);
                break;
            }

            // <expr> → ID
            case Tag.ID:
                code.emit(OpCode.iload, st.lookupAddress(((Word) look).lexeme));
                match(Tag.ID);
                break;

            default:
                error("Expr");
        }
    }

    private void exprlist(int operation) {
        switch (look.tag) {
            // <exprlist> → <expr> <exprlistp>
            case '+', '-', '/', '*', Tag.NUM, Tag.ID:
                expr();

                if (operation == 1)
                    code.emit(OpCode.invokestatic, 1);

                exprlistp(operation);
                break;

            default:
                error("Exprlist");
        }
    }

    private void exprlistp(int operation) {
        switch (look.tag) {
            // <exprlistp> → <expr> <exprlistp>
            case '+', '-', '/', '*', Tag.NUM, Tag.ID:
                expr();

                if (operation == 1)
                    code.emit(OpCode.invokestatic, 1);
                else if (operation == 2)
                    code.emit(OpCode.iadd);
                else if (operation == 3)
                    code.emit(OpCode.imul);

                exprlistp(operation);
                break;

            // <exprlistp> → ε
            case ')':
                break;

            default:
                error("Exprlistp");
        }
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "Input.lft";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}