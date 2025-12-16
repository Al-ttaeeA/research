# Research Collection
This repository is a collection of algorithms and programs that I'm writing to help with my research about De Bruijn Sequences and concatenation algorithms that produce them.

# List of current programs:
## 1. RepFinder.java
A program that generates all the CCR based cycles and representatives of n-length k-ary alphabet strings

To use the program simply change the values of the fields "n" and "k" and then run the program

The program prints all the representatives first in lex order, then in colex order 

All the cycles are stored in 3 maps, one of which is the initial map, the other two are the same but sorted in lex or colex order. A list of cycles for each representative can be found using the representative as key

## 2. DBChecker.java
A simple program that checks if a given string is a DB sequence of the given n and k values

To use it simply put the string, expected n and k values and run the program

## 3. CCR1Periodic, CCR2Periodic, and CCR3Periodic
3 Classes that each check if there are periodic parents when the parents of each rep is found using the respective parent rule

Each program when run checks for all periodic parents upto n=8 and k=6, though these values can be changed by the user if navigated to the checker() function and the 2 nested for-loops
