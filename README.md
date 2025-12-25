# De Bruijn Research Collection

This repository contains a collection of programs and algorithms developed to support my research under Professor Joseph Sawada into **De Bruijn sequences**, **concatenation-based constructions**, and **periodicity properties of cyclic representatives**.

The focus of this work is on understanding how equivalence classes of k-ary strings behave under various parent rules and how these structures relate to the generation and verification of De Bruijn sequences.

## Research Focus
- De Bruijn sequences over k-ary alphabets
- Concatenation algorithms for DB sequence construction
- CCR-based cycle representations
- Periodicity of parent structures under different rules

This repository serves as an experimental and verification toolkit rather than a polished application.

## Programs Included

### 1. `RepFinder.java`
Generates all **CCR-based cycles and representatives** for n-length strings over a k-ary alphabet.

- Representatives are printed in both **lexicographic** and **colexicographic** order
- Cycles are stored in three maps:
  - Original generation order
  - Lex-sorted
  - Colex-sorted
- Each representative maps to its corresponding cycle structure

Parameters `n` and `k` can be modified directly in the source before execution.

### 2. `DBChecker.java`
Verifies whether a given string is a valid **De Bruijn sequence** for specified values of `n` and `k`.

This tool is primarily used to validate candidate sequences produced by experimental constructions.

### 3. `CCR1Periodic`, `CCR2Periodic`, `CCR3Periodic`
Three programs that test for **periodic parents** under different CCR parent rules.

- Each class implements a distinct parent rule
- By default, checks all cases up to `n = 8` and `k = 6`
- These bounds can be adjusted in the `checker()` method

The results are used to explore structural differences between parent rules and their impact on periodicity.

### 4. `CCR1Construction.java`, `CCR2Construction.java`, `CCR3Construction.java`
Three programs that construct a sequence using the concatenation tree framework and the CCR1, CCR2, and CCR3 parent rules respectively, then it checks if the sequence is actually a DB sequence or not

This construction algorithm is the culmination of the first step of this research project, as it applies the concatenation tree framework on CCR functions.

## What This Repository Is (and Is Not)
- ✔ A research and experimentation toolkit
- ✔ Focused on theoretical computer science and discrete mathematics
- ✘ Not a finished library
- ✘ Not optimized for performance or UI

## What I Learned
- Practical exploration of equivalence classes in combinatorics on words
- Implementing abstract mathematical rules as executable algorithms
- Using computational experiments to support theoretical reasoning
- Managing correctness and verification in research-driven code

## License
This repository is released under the GNU General Public License (GPL).
