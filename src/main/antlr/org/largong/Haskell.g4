grammar Haskell;

@header {
    package org.largong;
}

prog: NEWLINE* (NEWLINE* function_declaration)* EOF;

function_declaration: name (function_header | function_body) NEWLINE;

function_header: '::' function_header_args;
function_header_args: (type '->')* type;

function_body: function_body_args function_body_condition? '=' expression;
function_body_args: arg*;

function_body_condition: '|' expression;

type: NAME | '[' type ']';
name: NAME;
value: CONST | number | STRING | CHAR | BOOL;
number: NUMBER;
expression: ('(' expression ')' | name | value)+ | if_statement | arith | logic;
arg: name | value;

arith:    arith ('*'|'/'|'%') arith
        | arith ('+'|'-') arith
        | '(' expression ')'
        | (name | number)
        ;

logic:    logic '&&' logic
        | logic '||' logic
        | logic_expression logic_compare_operator logic_expression
        | logic_expression
        ;

logic_compare_operator: '<' | '>' | '<=' | '>=' | '==';
logic_expression: '(' expression ')' | name | value | arith;

if_statement: 'if' expression 'then' expression 'else' expression;

/*
    Определим токены для парсинга выражения
    Хотим тьюринг полный язык
    Несколько типов: Int, Bool, Char, [<type>]
    Хотим паттерн матчинг по значениям:
        pattern :: Int -> Bool
        pattern 0 = true
        pattern x | x > 0 = true <- нужно добавить ещё if
        pattern _ = false
    В языке возможна рекурсия
    И if-statement:
        if <expr> then <expr> else <expr>
    expr тут может быть любым, без скобок
    Также должны быть возможны suffix operations
*/

fragment BS: '\\';
fragment QUAT: '\'';
fragment DQUAT: '"';
fragment HQUAT: '`';
fragment SPEC_SYMBOLS: (BS 'n' | BS 'r' | BS 't' | BS BS);
fragment NATURAL: [1-9];
fragment DIGIT: [0-9];
fragment LETTER: [a-zA-Z_];
fragment LETTER_OR_DIGIT: (LETTER | DIGIT);

COMMENT: '--' .*? NEWLINE -> channel(HIDDEN);
CHAR: QUAT (BS QUAT | SPEC_SYMBOLS | ~['\\]) QUAT;
STRING: DQUAT (BS DQUAT | SPEC_SYMBOLS | ~["\\])* DQUAT;
ANY_NAME: HQUAT ~[`]+ HQUAT;
CONST: 'undefined' | 'null';
BOOL: 'true' | 'false';
NUMBER: (NATURAL DIGIT* | '0') ('.'DIGIT*)?;
NAME: LETTER LETTER_OR_DIGIT*;
WHITESPACE: (' ' | '\t') -> skip;
NEWLINE: [\r\n]+;
