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
import java.net.*;
import java.util.*;

public class Protocol {
    public static int LENGTH_PREFIXED_VAR_SIZE = -1;

    enum Type {
        IP4(4, 32, "ip4"),
        TCP(6, 16, "tcp"),
        UDP(17, 16, "udp"),
        DCCP(33, 16, "dccp"),
        IP6(41, 128, "ip6"),
        SCTP(132, 16, "sctp"),
        UTP(301, 0, "utp"),
        UDT(302, 0, "udt"),
        IPFS(421, LENGTH_PREFIXED_VAR_SIZE, "ipfs"),
        HTTPS(443, 0, "https"),
        HTTP(480, 0, "http");

        public final int code, size;
        public final String name;
        private final byte[] encoded;

        Type(int code, int size, String name) {
            this.code = code;
            this.size = size;
            this.name = name;
            this.encoded = encode(code);
        }

        static byte[] encode(int code) {
            byte[] varint = new byte[(32 - Integer.numberOfLeadingZeros(code)+6)/7];
            putUvarint(varint, code);
            return varint;
        }
    }

    public final Type type;

    public Protocol(Type type) {
        this.type = type;
    }

    public void appendCode(OutputStream out) throws IOException {
        out.write(type.encoded);
    }

    public int size() {
        return type.size;
    }

    public String name() {
        return type.name;
    }

    public int code() {
        return type.code;
    }

    @Override
    public String toString() {
        return name();
    }

    public byte[] addressToBytes(String addr) {
        try {
            switch (type) {
                case IP4:
                    return Inet4Address.getByName(addr).getAddress();
                case IP6:
                    return Inet6Address.getByName(addr).getAddress();
                case TCP:
                case UDP:
                case DCCP:
                case SCTP:
                    int x = Integer.parseInt(addr);
                    if (x > 65536)
                        throw new IllegalStateException("Failed to parse "+type.name+" address "+addr + " (> 65536");
                    return new byte[]{(byte)(x >>8), (byte)x};
                case IPFS:
                    Multihash hash = Multihash.fromBase58(addr);
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    byte[] hashBytes = hash.toBytes();
                    byte[] varint = new byte[(32 - Integer.numberOfLeadingZeros(hashBytes.length)+6)/7];
                    putUvarint(varint, hashBytes.length);
                    bout.write(varint);
                    bout.write(hashBytes);
                    return bout.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        throw new IllegalStateException("Failed to parse address: "+addr);
    }

    public String readAddress(InputStream in) throws IOException {
        int sizeForAddress = sizeForAddress(in);
        byte[] buf;
        switch (type) {
            case IP4:
                buf = new byte[sizeForAddress];
                in.read(buf);
                return Inet4Address.getByAddress(buf).toString().substring(1);
            case IP6:
                buf = new byte[sizeForAddress];
                in.read(buf);
                return Inet6Address.getByAddress(buf).toString().substring(1);
            case TCP:
            case UDP:
            case DCCP:
            case SCTP:
                return Integer.toString((in.read() << 8) | (in.read()));
            case IPFS:
                buf = new byte[sizeForAddress];
                in.read(buf);
                return new Multihash(buf).toBase58();
        }
        throw new IllegalStateException("Unimplemented protocl type: "+type.name);
    }

    public int sizeForAddress(InputStream in) throws IOException {
        if (type.size > 0)
            return type.size/8;
        if (type.size == 0)
            return 0;
        return (int)readVarint(in);
    }

    static int putUvarint(byte[] buf, long x) {
        int i = 0;
        while (x >= 0x80) {
            buf[i] = (byte)(x | 0x80);
            x >>= 7;
            i++;
        }
        buf[i] = (byte)x;
        return i + 1;
    }

    static long readVarint(InputStream in) throws IOException {
        long x = 0;
        int s=0;
        for (int i=0; i < 10; i++) {
            int b = in.read();
            if (b == -1)
                throw new EOFException();
            if (b < 0x80) {
                if (i > 9 || i == 9 && b > 1) {
                    throw new IllegalStateException("Overflow reading varint" +(-(i + 1)));
                }
                return x | (((long)b) << s);
            }
            x |= ((long)b & 0x7f) << s;
            s += 7;
        }
        throw new IllegalStateException("Varint too long!");
    }

    private static Map<String, Protocol> byName = new HashMap<>();
    private static Map<Integer, Protocol> byCode = new HashMap<>();

    static {
        for (Protocol.Type t: Protocol.Type.values()) {
            Protocol p = new Protocol(t);
            byName.put(p.name(), p);
            byCode.put(p.code(), p);
        }

    }

    public static Protocol get(String name) {
        if (byName.containsKey(name))
            return byName.get(name);
        throw new IllegalStateException("No protocol with name: "+name);
    }

    public static Protocol get(int code) {
        if (byCode.containsKey(code))
            return byCode.get(code);
        throw new IllegalStateException("No protocol with code: "+code);
    }
}
