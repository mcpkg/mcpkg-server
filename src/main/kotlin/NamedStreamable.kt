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

public interface NamedStreamable
{
    InputStream getInputStream() throws IOException;

    Optional<String> getName();

    default byte[] getContents() throws IOException {
        InputStream in = getInputStream();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] tmp = new byte[4096];
        int r;
        while ((r=in.read(tmp))>= 0)
            bout.write(tmp, 0, r);
        return bout.toByteArray();
    }

    class FileWrapper implements NamedStreamable {
        private final File source;

        public FileWrapper(File source) {
            this.source = source;
        }

        public InputStream getInputStream() throws IOException {
            return new FileInputStream(source);
        }

        public Optional<String> getName() {
            return Optional.of(source.getName());
        }
    }

    class ByteArrayWrapper implements NamedStreamable {
        private final Optional<String> name;
        private final byte[] data;

        public ByteArrayWrapper(byte[] data) {
            this(Optional.empty(), data);
        }

        public ByteArrayWrapper(String name, byte[] data) {
            this(Optional.of(name), data);
        }

        public ByteArrayWrapper(Optional<String> name, byte[] data) {
            this.name = name;
            this.data = data;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public Optional<String> getName() {
            return name;
        }
    }
}
