SHELL := bash

SOURCE_FILES = src/main/kotlin/Main.kt \
               src/test/kotlin/MainTest.kt \
               src/main/kotlin/FileWrapper.kt

TEST_FILES = test-data/ae2.json \
             test-data/mekanism.json \
             test-data/blood-magic.json

MVN = mvn

MVN_FLAGS = -T 8

RUN_MVN = $(MVN) $(MVN_FLAGS)

TEST_TMP = /tmp/packages     

TEST_ARGS = "$$PWD/pom.xml"

all: build

build: $(SOURCE_FILES)
	$(RUN_MVN) "compile"
	$(RUN_MVN) "test-compile"

test-setup: $(TEST_FILES)
	-[ -e $(TEST_TMP) ] && rm -rf $(TEST_TMP)
	mkdir -pv $(TEST_TMP)
	cp -vt $(TEST_TMP) $(TEST_FILES)

test: build test-setup
	$(RUN_MVN) "exec:java" -Dexec.args=$(TEST_ARGS)
