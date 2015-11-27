SHELL := bash

SOURCE_FILES = src/main/kotlin/Main.kt \
               src/test/kotlin/MainTest.kt \
               src/main/kotlin/FileWrapper.kt

TEST_FILES = test-data/ae2.json \
             test-data/mekanism.json \
             test-data/blood-magic.json

TEST_TMP = /tmp/packages     

TEST_ARGS = ""

build: $(SOURCE_FILES)
	mvn compile

test-setup: $(TEST_FILES)
	-[ -e $(TEST_TMP) ] && rm -rf $(TEST_TMP)
	mkdir -pv $(TEST_TMP)
	cp -vt $(TEST_TMP) $(TEST_FILES)

test: build test-setup
	mvn exec:java -Dexec.args=$(TEST_ARGS)
