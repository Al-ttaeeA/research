CC = gcc
CFLAGS = -Wall -Wextra -O2 -std=c99
TARGETS = NAS validate_k

all: $(TARGETS)

NAS: NAS.c
	$(CC) $(CFLAGS) -o $@ $< -lm

validate_k: validate_k.c
	$(CC) $(CFLAGS) -o $@ $< -lm

clean:
	del /Q NAS.exe validate_k.exe 2>nul

.PHONY: all clean
