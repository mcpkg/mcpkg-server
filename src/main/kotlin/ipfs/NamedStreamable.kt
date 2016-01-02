package org.ipfs.api;

/**
 * Copyright © 2015 Ian Preston
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.*;
import java.util.*;

public interface NamedStreamable {
    fun getInputStream(): InputStream;

    fun getName(): Optional<String>;

    fun getContents(): ByteArray {
        val inS = getInputStream();
        val bout = ByteArrayOutputStream();
        val tmp = ByteArray(4096);
        var r: Int;
        var i: Int;
        while(true) {
          r = inS.read(tmp)
          if(r < 0) { break; }
          bout.write(tmp, 0, r);
        }
        return bout.toByteArray();
    }

    class FileWrapper(source: File): NamedStreamable {
        private val source = source;

        public override fun getInputStream(): InputStream {
            return FileInputStream(source);
        }

        public override fun getName(): Optional<String> {
            return Optional.of(source.getName());
        }
    }

    class ByteArrayWrapper(name: Optional<String>,
                           bytes: ByteArray): NamedStreamable {
        private val name = name;
        private val bytes = bytes;

        constructor(bytes: ByteArray) :
            this(Optional.empty(), bytes) {}
            
        constructor(name: String, bytes: ByteArray) :
            this(Optional.empty(), bytes) {}

        public override fun getInputStream(): InputStream {
            return ByteArrayInputStream(bytes);
        }

        public override fun getName(): Optional<String> {
            return name;
        }
    }
}
