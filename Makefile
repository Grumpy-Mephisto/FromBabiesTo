.SILENT: clean
.PHONY: default compile run clean

JAVAC=$(shell which javac)
JAVA=$(shell which java)

JFLAGS=-g

SOURCES=$(wildcard *.java)

CLASSES=$(SOURCES:.java=.class)

default: compile

compile: clean $(CLASSES)

%.class: %.java
	$(JAVAC) $(JFLAGS) $<

run: compile
	$(JAVA) Main

clean:
	$(RM) *.class

all: clean compile run
