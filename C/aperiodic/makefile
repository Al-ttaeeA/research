CC = gcc
CFLAGS = -Wall -Wextra -O2 -std=c99
TARGET = aperiodic_coollex

all: $(TARGET)

$(TARGET): aperiodic_coollex.c
	$(CC) $(CFLAGS) -o $(TARGET) aperiodic_coollex.c

clean:
	del /Q $(TARGET).exe 2>nul

.PHONY: all clean
