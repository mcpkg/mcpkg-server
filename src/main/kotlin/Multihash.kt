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

public class Multihash {
    public enum Type {
        sha1(0x11, 20),
        sha2_256(0x12, 32),
        sha2_512(0x13, 64),
        sha3(0x14, 64),
        blake2b(0x40, 64),
        blake2s(0x41, 32);

        public int index, length;

        Type(int index, int length) {
            this.index = index;
            this.length = length;
        }

        private static Map<Integer, Type> lookup = new TreeMap<>();
        static {
            for (Type t: Type.values())
                lookup.put(t.index, t);
        }

        public static Type lookup(int t) {
            if (!lookup.containsKey(t))
                throw new IllegalStateException("Unknown Multihash type: "+t);
            return lookup.get(t);
        }
    }

    public final Type type;
    public final byte[] hash;

    public Multihash(Type type, byte[] hash) {
        if (hash.length > 127)
            throw new IllegalStateException("Unsupported hash size: "+hash.length);
        if (hash.length != type.length)
            throw new IllegalStateException("Incorrect hash length: " + hash.length + " != "+type.length);
        this.type = type;
        this.hash = hash;
    }

    public Multihash(byte[] multihash) {
        this(Type.lookup(multihash[0] & 0xff), Arrays.copyOfRange(multihash, 2, multihash.length));
    }

    public byte[] toBytes() {
        byte[] res = new byte[hash.length+2];
        res[0] = (byte)type.index;
        res[1] = (byte)hash.length;
        System.arraycopy(hash, 0, res, 2, hash.length);
        return res;
    }

    @Override
    public String toString() {
        return toBase58();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Multihash))
            return false;
        return type == ((Multihash) o).type && Arrays.equals(hash, ((Multihash) o).hash);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(hash) ^ type.hashCode();
    }

    public String toHex() {
        StringBuilder res = new StringBuilder();
        for (byte b: toBytes())
            res.append(String.format("%x", b&0xff));
        return res.toString();
    }

    public String toBase58() {
        return Base58.encode(toBytes());
    }

    public static Multihash fromHex(String hex) {
        if (hex.length() % 2 != 0)
            throw new IllegalStateException("Uneven number of hex digits!");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        for (int i=0; i < hex.length()-1; i+= 2)
            bout.write(Integer.valueOf(hex.substring(i, i+2), 16));
        return new Multihash(bout.toByteArray());
    }

    public static Multihash fromBase58(String base58) {
        return new Multihash(Base58.decode(base58));
    }
}
