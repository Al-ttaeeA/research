# Research Collection
This repository is a collection of algorithms and programs that I'm writing to help with my research about De Bruijn Sequences and concatenation algorithms that produce them.

# List of current programs:
## 1. RepFinder.java
A program that generates all the CCR based cycles and representatives of n-length k-ary alphabet strings

To use the program simply change the values of the fields "n" and "k" and then run the program

The program prints all the representatives first in lex order, then in colex order 

All the cycles are stored in 3 maps, one of which is the initial map, the other two are the same but sorted in lex or colex order. A list of cycles for each representative can be found using the representative as key
