.PHONY: default clean plugin post tree browse base test run runbase runtest all

# The plugin's source file and JAR to create
# PLUGIN=OptimizedNumeric.scala
PLUGIN=KindProjector.scala
PLUGJAR=kind-projector.jar
PLUGXML=scalac-plugin.xml

# The phase to run before inspecting the AST
## PHASE=parser
PHASE=typer
PHASE2=kind-projector

# The test's source file, and class name
# TEST=Test.scala
# TESTBASE=Test
TEST=test.scala
TESTBASE=Test

# The classpath to use when building/running the test
## CP=lib/numeric_2.9.1-0.1.jar
CP=kind-projector.jar

# Some help output
default:
	@echo "targets: clean | plugin | post | tree | base | test | run"

# Remove classfiles and the plugin's jar file
clean:
	rm -rf classes
	find . -name '*.class' -exec rm -f {} \;
	rm -f $(PLUGJAR)

# Build the plugin
plugin:
	mkdir -p classes
	scalac -cp $(CP) -d classes $(PLUGIN)
	cp $(PLUGXML) classes
	jar cf $(PLUGJAR) -C classes .

# Various targets for inspecting the AST:
#   post shows the AST as source code
#   tree prints the actual AST
#   browse runs a graphical AST browser
post:
	scalac -cp $(CP) -Xprint:$(PHASE) -Ystop-after:$(PHASE) $(TEST) | tee POST

tree:
	scalac -cp $(CP) -Xprint:$(PHASE) -Ystop-after:$(PHASE) -Yshow-trees $(TEST) | tee TREE

browse:
	scalac -cp $(CP) -Ybrowse:$(PHASE) -Ystop-after:$(PHASE) -Yshow-trees $(TEST)

postplug:
	scalac -cp $(CP) -Xplugin:$(PLUGJAR) -Xprint:$(PHASE2) -Ystop-after:$(PHASE2) $(TEST) | tee POST

# Compile the test without the plugin (the base case)
base:
	scalac -cp $(CP) $(TEST)

# Compile the test with the plugin (the test case)
test:
	scalac -cp $(CP) -Xplugin:$(PLUGJAR) $(TEST)

# Run the test
run:
	scala -cp $(CP) $(TESTBASE)

# Compile and run the base case
runbase: base run

# Compile and run the test case
runtest: test run

# Do a clean build of the plugin and test case, then run the test
all: clean plugin test run
