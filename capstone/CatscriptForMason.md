# Catscript Guide
This document should be used to create a guide for catscript, to satisfy capstone requirement 4

## Introduction
Catscript is a simple scripting language.  Here is an example:
~~~
var x = "foo"
print(x)
~~~
## Features
A CatScript program is statically typed and begins with a program statement, which contains either a statement or function declaration.
Its type system is outlined below.
~~~
- int - a 32 bit integer
- string - a java-style string
- bool - a boolean value
- list - a list of value with the type 'x'
- null - the null type
- object - any type of value
~~~
### Statements
Statements are the individual commands or instructions that make up the program.

#### For loops
For loops are control flow statements that allow you to iterate through a sequence of elements, like lists. 
They repeatedly execute a block of code until there are no more elements to iterate through.
~~~
var list = [0, 1, 2]

for( i in lst ) {
    print(i)
}
~~~
#### If statements
If statements are control flow statements that allow you to conditionally execute certain blocks of code.
It allows the program to make decisions and execute different branches of code based on what conditions have or have not been met.
~~~
var x = 10
~~~
~~~
if(x == 10){
    print("true")
}
~~~
~~~
if(x == 10){
    print("true")
}
else {
    print("false")
}
~~~
~~~
if(x == 10){
    print("true")
}
else if(x < 10){
    print("less than")
}
~~~
~~~
if(x == 10){
    print("true")
}
else if(x < 10){
    print("less than")
}
else {
    print("greater than")
}
~~~
#### Print statement
Print statements allow you to display certain output on a screen. You may encase any single value expression within a print statement.
~~~
print("Hello, world!")
~~~
#### Variable statement
Variable statements are declarations used to create a storage location in memory that holds data.
CatScript does not require variables to be declared with types, but you may choose to do so anyway.
~~~
var intExample = 1
var bool : boolExample = true
~~~
#### Function call statement
Function call statements invoke the execution of a function. They may or may not return a value.
~~~
foo()
~~~
May return:
~~~
Hello, world!
~~~
#### Assignment statement
Assignment statements are used to assign a value to a variable. This allows you to store and manipulate data within the program.
~~~
x = 0
~~~
#### Function declaration
Function declaration statements are used to define new functions. 
This is where you can also define its input parameters, if necessary, and the body of code to be executed when it's called.
~~~
function bar(x : int, y : int) {
    var answer = x + y
    return(answer)
}
~~~
#### Function body statement
Function body statements define the body of the function. This is the block of code that gets executed when the function is called.
This is the body of the bar() function in the function declaration example:
~~~
var answer = x + y
    return(answer)
~~~
#### Parameter list
Parameter lists are part of function declarations that list the parameters that a function accepts, along with their types.
A parameter list may be as long or as short as you like.
These are the parameters of the bar() function in the function declaration example:
~~~
x : int, y : int
~~~
#### Parameters
Parameters consist of an identifier and a type expression to define the inputs a function takes.
You may name the identifier whatever you want, but the type expression needs to be one in the CatScript type system.
This is the first parameter of the bar() function in the function declaration example:
~~~
x : int
~~~
#### Return statement
Return statements are used to return a value within a function. This will terminate the execution of the function.
This is the return statement of the bar() function in the function declaration example:
~~~
return(answer)
~~~
### Expressions
Expressions are combinations of values, variables, operators, and function calls that are evaluated to produce a single result.
#### Equality expression
Equality expressions evaluate whether two values are equal or not. You may use equality operators in order to produce a boolean return value.
They may be used in if statements.
- Equal (==)
- Not equal (!=)
~~~
if(x == 10){}
~~~
#### Comparison expression
Comparison expressions are used to compare two values. You may use comparison operators to determine the relationship between operands and produce a boolean value.
They may be used in if statements.
- Less than (<)
- Less than or equal to (<=)
- Greater than (>)
- Greater than or equal to (>=)
~~~
if(x >= 10){}
~~~
#### Additive expression
Additive expressions involve the addition or subtraction between operands. You may add or subtract values from one another and get a value in return depending on the operation.
~~~
1 + 1
~~~
#### Factor expression
Factor expressions involve the multiplication or division between operands. You may multiply or divide values and get a value in return depending on the operation.
~~~
2 * 2
~~~
#### Unary expression
Unary expressions involve only one operand or value and an operator. These operate on a single operand like "not" or "-".
~~~
not true
~~~
#### Primary expression
Primary expressions refer to the simplest form of an expression. It represents a single value or operand without any additional operators or nested expressions.
~~~
false
~~~
#### List literal
List literals specify a collection of elements when defining or initializing a list. The lists may store a items of the same data type.
~~~
[1, 2, 3]
~~~
#### Function call
Function calls are expressions that invoke the execution of a function. 
This is where you may pass in predefined arguments in order to manipulate data, print something, or get a return value.
~~~
var x = 10
var y = 11
bar(x, y)
~~~
#### Argument list
Argument lists are part of function calls that list the arguments that a function accepts.
An argument list size may only be the amount of parameters defined in the function.
These are the arguments of the bar() function in the function call example:
~~~
x, y
~~~
#### Type expression
Type expressions are used to specify or represent data types. They allow you to define the data type of variable, expression, or value.
~~~
int
~~~
## CatScript Grammar
~~~ebnf
catscript_program = { program_statement };

program_statement = statement |
                    function_declaration;

statement = for_statement |
            if_statement |
            print_statement |
            variable_statement |
            assignment_statement |
            function_call_statement;

for_statement = 'for', '(', IDENTIFIER, 'in', expression ')', 
                '{', { statement }, '}';

if_statement = 'if', '(', expression, ')', '{', 
                    { statement }, 
               '}' [ 'else', ( if_statement | '{', { statement }, '}' ) ];

print_statement = 'print', '(', expression, ')'

variable_statement = 'var', IDENTIFIER, 
     [':', type_expression, ] '=', expression;

function_call_statement = function_call;

assignment_statement = IDENTIFIER, '=', expression;

function_declaration = 'function', IDENTIFIER, '(', parameter_list, ')' + 
                       [ ':' + type_expression ], '{',  { function_body_statement },  '}';

function_body_statement = statement |
                          return_statement;

parameter_list = [ parameter, {',' parameter } ];

parameter = IDENTIFIER [ , ':', type_expression ];

return_statement = 'return' [, expression];

expression = equality_expression;

equality_expression = comparison_expression { ("!=" | "==") comparison_expression };

comparison_expression = additive_expression { (">" | ">=" | "<" | "<=" ) additive_expression };

additive_expression = factor_expression { ("+" | "-" ) factor_expression };

factor_expression = unary_expression { ("/" | "*" ) unary_expression };

unary_expression = ( "not" | "-" ) unary_expression | primary_expression;

primary_expression = IDENTIFIER | STRING | INTEGER | "true" | "false" | "null"| 
                     list_literal | function_call | "(", expression, ")"

list_literal = '[', expression,  { ',', expression } ']'; 

function_call = IDENTIFIER, '(', argument_list , ')'

argument_list = [ expression , { ',' , expression } ]

type_expression = 'int' | 'string' | 'bool' | 'object' | 'list' [, '<' , type_expression, '>']
~~~