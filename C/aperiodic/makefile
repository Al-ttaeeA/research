CC = gcc
CFLAGS = -Wall -Wextra -O2 -std=c99
TARGETS = aperiodic_coollex aperiodic_bellringer

all: $(TARGETS)

aperiodic_coollex: aperiodic_coollex.c
	$(CC) $(CFLAGS) -o $@ $<

aperiodic_bellringer: aperiodic_bellringer.c
	$(CC) $(CFLAGS) -o $@ $<

clean:
	del /Q aperiodic_coollex.exe aperiodic_bellringer.exe 2>nul

.PHONY: all clean
