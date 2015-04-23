JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class: 
	$(JC) $(JFLAGS) $*.java

CLASSES = HW1.java Node.java Rule.java Variable.java

default: classes

classes: $(CLASSES:.java=.class)

clean: 
	$(RM) *.class