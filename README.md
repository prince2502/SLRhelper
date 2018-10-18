# SLRhelper
Generate First-Follow sets, LR(0) automaton, SLR Table for a given LR1 grammar

The grammar has to be in following format
  
  1. Every production must contain '::' to split between the head and body.
  2. Every symbol(terminal or non terminal) in bosy must be separated by space.
   
example.

S :: A $ 

A :: e B 

B :: e B D

B :: a C

C :: a C

C :: r

D :: e B D

D :: r

The output will give you text files for the following
  
  1. All terminals
  2. All Non terminals
  3. The first sets
  4. The follow sets
  5. The text representation of LR(0) automaton for the grammar
  6. The visual representation of LR(0) automaton, in .dot file format. Use graphviz or any other software to open it.
  7. All the state kernals
  
If provided with a valid text(source) for the grammer, it will

  1. Test if the code follows the grammar.
  2. Print a parse tree for it.
  

## Input

Give path to grammar file as arg 0, and and path to source file as arg 1
  
