SHELL := bash

SOURCE_FILES = src/main/kotlin/mcpkg/Main.kt \
               src/main/kotlin/mcpkg/FileWrapper.kt \
               src/main/kotlin/mcpkg/DatabaseInterface.kt \
               src/test/kotlin/mcpkg/MainTest.kt \
               src/main/kotlin/ipfs/IPFS.kt \
               src/main/kotlin/ipfs/Base58.kt \
               src/main/kotlin/ipfs/JSONParser.kt \
               src/main/kotlin/ipfs/MerkleNode.kt \
               src/main/kotlin/ipfs/MultiAddress.kt \
               src/main/kotlin/ipfs/Multihash.kt \
               src/main/kotlin/ipfs/Multipart.kt \
               src/main/kotlin/ipfs/NamedStreamable.kt \
               src/main/kotlin/ipfs/Protocol.kt

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
