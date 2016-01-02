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

public enum class MultihashType(index: Int, length: Int) {
    sha1(0x11, 20),
    sha2_256(0x12, 32),
    sha2_512(0x13, 64),
    sha3(0x14, 64),
    blake2b(0x40, 64),
    blake2s(0x41, 32);

    public val index = index;
    public val length = length;


    companion object Static {
        private val lookupM = TreeMap<Int, MultihashType>();

        init {
            for(t in MultihashType.values()) {
                lookupM.put(t.index, t);
            }
        }

        public fun lookup(t: Int): MultihashType {
            if(!lookupM.containsKey(t)) {
                throw IllegalStateException("Unknown Multihash type: $t");
            }
            return (lookupM.get(t))!!;
        }
    }
}


public class Multihash(type: MultihashType, hash: ByteArray) {
    public val type = type;
    public val hash = hash;

    init {
        val hl = hash.size;
        val tl = type.length;
        if(hl > 127) {
            throw IllegalStateException("Unsupported hash size: $hl");
        }
        if(hl != tl) {
            throw IllegalStateException("Incorrect hash length: $hl != $tl");
        }
    }

    companion object Static {
        public fun fromBase58(base58: String): Multihash {
            return Multihash(Base58().decode(base58));
        }
    }

    constructor(multihash: ByteArray) :
        this(MultihashType.lookup((multihash[0] as Int) and 0xff),
             Arrays.copyOfRange(multihash, 2, multihash.size)) {}

    public fun toBytes(): ByteArray {
        val res = ByteArray(hash.size + 2);
        res[0] = type.index as Byte;
        res[1] = hash.size as Byte;
        System.arraycopy(hash, 0, res, 2, hash.size);
        return res;
    }

    public override fun toString(): String {
        return toBase58();
    }

    public fun equals(o: Any): Boolean {
        if(o is Multihash) {
            return (type == o.type && Arrays.equals(hash, o.hash));
        } else {
            return false;
        }
    }

    public override fun hashCode(): Int {
        return Arrays.hashCode(hash) xor type.hashCode();
    }

    public fun toHex(): String {
        val res = StringBuilder();
        for(b in toBytes()) {
            res.append("%x".format((b as Int) and 0xff));
        }
        return res.toString();
    }

    public fun toBase58(): String {
        return Base58().encode(toBytes());
    }

    public fun fromHex(hex: String): Multihash {
        if(hex.length() % 2 != 0) {
            throw IllegalStateException("Uneven number of hex digits!");
        }
        val bout = ByteArrayOutputStream();
        var i = 0;
        while(i < hex.length() - 1) {
            bout.write(Integer.valueOf(hex.substring(i, i + 2), 16));
            i += 2;
        }
        return Multihash(bout.toByteArray());
    }

}
