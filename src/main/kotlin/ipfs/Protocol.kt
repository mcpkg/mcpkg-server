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

val LENGTH_PREFIXED_VAR_SIZE = -1;

enum class ProtocolType(val code: Int, val size: Int, val pname: String) {
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

    val encoded = encode(code);
}

fun encode(code: Int): ByteArray {
    val leading = Integer.numberOfLeadingZeros(code);
    val varint = ByteArray((32 - leading + 6) / 7);
    putUvarint(varint, code as Long);
    return varint;
}

fun putUvarint(buf: ByteArray, x: Long): Int {
    var i: Int = 0;
    var r: Long = x;
    while(r >= 0x80) {
        buf[i] = (r or 0x80) as Byte;
        r = r shr 7;
        i = i + 1;
    }
    buf[i] = r as Byte;
    return i + 1;
}

fun readVarint(inS: InputStream): Long {
    var x: Long = 0;
    var s: Int = 0;
    var i: Int = 0;
    while(i < 10) {
        val b = inS.read();
        if(b == -1) { throw EOFException(); }
        if(b < 0x80) {
            if(i > 9 || i == 9 && b > 1) {
                val errMsg = "Overflow reading varint ${-(i + 1)}";
                throw IllegalStateException(errMsg);
            }
            return (x or ((b as Long) shl s));
        }
        x = x or ((b as Long) and 0x7f) shl s;
        s = s + 7;
        i = i + 1;
    }
    throw IllegalStateException("Varint too long!");
}

public class Protocol(givenType: ProtocolType) {
    public val type = givenType;

    public fun appendCode(out: OutputStream) {
        out.write(type.encoded);
    }

    public fun getProtoSize(): Int {
        return type.size;
    }

    public fun getProtoName(): String {
        return type.pname;
    }

    public fun getProtoCode(): Int {
        return type.code;
    }

    public override fun toString(): String {
        return getProtoName();
    }

    public fun addressToBytes(addr: String): ByteArray {
        try {
            when(type) {
                ProtocolType.IP4 -> {
                    return Inet4Address.getByName(addr).getAddress();
                };
                ProtocolType.IP6 -> {
                    return Inet6Address.getByName(addr).getAddress();
                };
                ProtocolType.TCP -> {};
                ProtocolType.UDP -> {};
                ProtocolType.DCCP -> {};
                ProtocolType.SCTP -> {
                    val x = Integer.parseInt(addr);
                    if(x > 65536) {
                        val errMsg = "Failed to parse ${type.name} address: " +
                                     "${addr} > 65536";
                        throw IllegalStateException(errMsg);
                    }
                    return byteArrayOf((x shr 8) as Byte, x as Byte);
                };
                ProtocolType.IPFS -> {
                    val hash = Multihash.fromBase58(addr);
                    val bout = ByteArrayOutputStream();
                    val hashBytes = hash.toBytes();
                    val hbLen = hashBytes.size;
                    val leading = Integer.numberOfLeadingZeros(hbLen);
                    val varint = ByteArray((32 - leading + 6) / 7);
                    putUvarint(varint, hbLen as Long);
                    bout.write(varint);
                    bout.write(hashBytes);
                    return bout.toByteArray();
                }
            };
        } catch(e: IOException) {
            throw RuntimeException(e);
        }
        throw IllegalStateException("Failed to parse address: ${addr}");
    }

    public fun readAddress(inS: InputStream): String {
        val sizeForAddress = sizeForAddress(inS);
        var buf: ByteArray;
        when(type) {
            ProtocolType.IP4 -> {
                buf = ByteArray(sizeForAddress);
                inS.read(buf);
                return Inet4Address.getByAddress(buf).toString().substring(1);
            };
            ProtocolType.IP6 -> {
                buf = ByteArray(sizeForAddress);
                inS.read(buf);
                return Inet6Address.getByAddress(buf).toString().substring(1);
            };
            ProtocolType.TCP -> {};
            ProtocolType.UDP -> {};
            ProtocolType.DCCP -> {};
            ProtocolType.SCTP -> {
                return Integer.toString((inS.read() shl 8) or (inS.read()));
            };
            ProtocolType.IPFS -> {
                buf = ByteArray(sizeForAddress);
                inS.read(buf);
                return Multihash(buf).toBase58();
            };
        }
        val errMsg = "Unimplemented protocol type: ${type.name}";
        throw IllegalStateException(errMsg);
    }

    public fun sizeForAddress(inS: InputStream): Int {
        if(type.size > 0) { return type.size/8; }
        if(type.size == 0) { return 0; }
        return (readVarint(inS) as Int);
    }

    private val byName = initializeByName();
    private val byCode = initializeByCode();

    private fun initializeByName(): HashMap<String, Protocol> {
        val res = HashMap<String, Protocol>();
        for(t in ProtocolType.values()) {
            val p = Protocol(t);
            res.put(p.getProtoName(), p);
        }
        return res;
    }

    private fun initializeByCode(): HashMap<Int, Protocol> {
        val res = HashMap<Int, Protocol>();
        for(t in ProtocolType.values()) {
            val p = Protocol(t);
            res.put(p.getProtoCode(), p);
        }
        return res;
    }

    public fun get(name: String): Protocol {
        if(byName.containsKey(name)) { return ((byName.get(name))!!); }
        throw IllegalStateException("No protocol with name: ${name}");
    }

    public fun get(code: Int): Protocol {
        if(byCode.containsKey(code)) { return ((byCode?.get(code))!!); }
        throw IllegalStateException("No protocol with code: ${code}");
    }
}
