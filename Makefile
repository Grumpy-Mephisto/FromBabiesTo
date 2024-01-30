# Define a variable for compiler
JAVAC=$(shell which javac)

# Define a variable for Java
JAVA=$(shell which java)

# Define a variable for compilation flags
JFLAGS=-g

# Define .java files
SOURCES=MainFrame.java AnimationPanel.java Main.java

# Define .class files
CLASSES=$(SOURCES:.java=.class)

# The default action
default: compile

# This target entry builds the .class files
compile: clean $(CLASSES)

%.class: %.java
	$(JAVAC) $(JFLAGS) $<

# This entry runs the program
run: compile
	$(JAVA) Main

# This entry cleans up the directory
clean:
	$(RM) *.class

all: clean compile run

.SILENT: clean

# Define the phony targets
.PHONY: default compile run clean
