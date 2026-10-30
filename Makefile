JAVAC ?= javac
JAVA ?= java
OUT_DIR ?= out

SOURCES := $(wildcard *.java)

.PHONY: all build test run clean

all: test

build:
	mkdir -p $(OUT_DIR)
	$(JAVAC) -d $(OUT_DIR) $(SOURCES)

test: build
	$(JAVA) -cp $(OUT_DIR) simulator.TestRunner

run: build
	$(JAVA) -cp $(OUT_DIR) simulator.GUI

clean:
	rm -rf $(OUT_DIR)
	find . -maxdepth 1 -name '*.class' -delete
