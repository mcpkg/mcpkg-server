SHELL := bash

SOURCE_FILES = src/main/kotlin/Main.kt \
               src/main/kotlin/FileWrapper.kt \
               src/main/kotlin/DatabaseInterface.kt \
               src/test/kotlin/MainTest.kt \
               src/main/java/org/ipfs/api/IPFS.java \
               src/main/java/org/ipfs/api/Base58.java \
               src/main/java/org/ipfs/api/JSONParser.java \
               src/main/java/org/ipfs/api/MerkleNode.java \
               src/main/java/org/ipfs/api/MultiAddress.java \
               src/main/java/org/ipfs/api/Multihash.java \
               src/main/java/org/ipfs/api/Multipart.java \
               src/main/java/org/ipfs/api/NamedStreamable.java \
               src/main/java/org/ipfs/api/Protocol.java

MVN = mvn

MVN_FLAGS = -T 8

RUN_MVN = $(MVN) $(MVN_FLAGS)

TEST_ARGS = ""

all: build doc

doc: $(SOURCE_FILES)
	$(RUN_MVN) "pre-site"

build: $(SOURCE_FILES)
	$(RUN_MVN) "compile"
	$(RUN_MVN) "test-compile"

test: build test-setup
	cd test-data; $(RUN_MVN) "exec:java" -Dexec.args=$(TEST_ARGS)
