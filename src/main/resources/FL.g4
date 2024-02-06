grammar FL;

prog: NEWLINE* (NEWLINE* function_declaration)* EOF;

function_declaration: name (function_header | function_body) NEWLINE;

function_header: '::' function_header_args;
function_header_args: (type '->')* type;

function_body: function_body_args ('|' logic)? '=' expression;
function_body_args: arg*;

type: NAME | '[' type ']';
name: ANY_NAME | NAME;
value: CONST | NUMBER | STRING | CHAR | BOOL | name;
expression: ('(' expression ')' | value)+ | if_statement | arith | logic;
arg: name | value;

arith:    arith ('*'|'/'|'%') arith
        | arith ('+'|'-') arith
        | '(' expression ')'
        | (name | NUMBER)
        ;
val: '(' arith ')' | (name | NUMBER);

logic:    logic '&&' logic
        | logic '||' logic
        | logic_expression logic_compare_operator logic_expression
        | logic_expression
        ;

logic_compare_operator: '<' | '>' | '<=' | '>=' | '==';
logic_expression: '(' expression ')' | value | arith;

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

COMMENT: ('/*' .*? '*/' | '//' .*? NEWLINE) -> channel(HIDDEN);
CHAR: '\'' (~['] | '\\\'' | '\\n' | '\\r' | '\\t') '\'';
STRING: '"' (~["] | '\\"')* '"';
ANY_NAME: '`' ~[`]+ '`';
CONST: 'undefined' | 'null';
BOOL: 'true' | 'false';
NUMBER: [1-9][0-9]*('.'[0-9]*)? | [0];
NAME: [a-zA-Z_][a-zA-Z0-9_]*;
WHITESPACE: (' ' | '\t') -> skip;
NEWLINE: [\r\n]+;
