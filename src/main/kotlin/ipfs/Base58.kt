package org.ipfs.api;

/**
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.math.BigInteger;

/**
 * A custom form of base58 is used to encode BitCoin addresses.
 *
 * Note that this is not the same base58 as used by Flickr,
 * which you may see reference to around the internet.
 *
 * <p>Satoshi says: why base-58 instead of standard base-64 encoding?<p>
 *
 * <ul>
 * <li>Don't want 0OIl characters that look the same in some fonts and
 *     could be used to create visually identical looking account numbers.</li>
 * <li>A string with non-alphanumeric characters is not as easily accepted
 *     as an account number.</li>
 * <li>E-mail usually won't line-break without punctuation to break at.</li>
 * <li>Double-clicking and other forms of selection tend to select the whole
 *     number as one word if it's all alphanumeric.</li>
 * </ul>
 */

public class Base58 {
    private val ALPHABET
      = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private val BASE = BigInteger.valueOf(58);

    public fun encode(input: ByteArray): String {
        // TODO: This could be a lot more efficient.
        var bi = BigInteger(1, input);
        val s = StringBuffer();
        while(bi.compareTo(BASE) >= 0) {
            val mod = bi.mod(BASE);
            s.insert(0, ALPHABET.charAt(mod.intValue()));
            bi = bi.subtract(mod).divide(BASE);
        }
        s.insert(0, ALPHABET.charAt(bi.intValue()));
        // Convert leading zeros too.
        for(anInput in input) {
            if((anInput as Int) == 0) {
                s.insert(0, ALPHABET.charAt(0));
            } else {
                break;
            }
        }
        return s.toString();
    }

    public fun decode(input: String): ByteArray {
        val bytes = decodeToBigInteger(input).toByteArray();

        // We may have got one more byte than we wanted, if the high bit of the
        // next-to-last byte was not zero. This is because BigIntegers are
        // represented with twos-complement notation, thus if the high bit of
        // the last byte happens to be 1 another 8 zero bits will be added to
        // ensure the number parses as positive. Detect that case here and chop
        // it off.
        val stripSignByte =    bytes.size        >  1
                            && (bytes[0] as Int) == 0
                            && (bytes[1] as Int) <  0;

        // Count the leading zeros, if any.
        var leading = 0;
        var i = 0;
        while(input.charAt(i) == ALPHABET.charAt(0)) {
            leading++;
            i++;
        }

        // Now cut/pad correctly.
        // Java 6 has a convenience for this, but Android can't use it.
        val strip = if(stripSignByte) 1 else 0;
        val tmp = ByteArray(bytes.size - strip + leading);
        System.arraycopy(bytes, strip, tmp, leading, tmp.size - leading);

        return tmp;
    }

    public fun decodeToBigInteger(input: String): BigInteger {
        var bi = BigInteger.valueOf(0);
        // Work backwards through the string.
        var i = input.length - 1;
        while(i >= 0) {
            val alphaIndex = ALPHABET.indexOf(input.charAt(i)) as Long;
            if(alphaIndex == -1L) {
                val errMsg = "Illegal character ${input.charAt(i)} at ${i}";
                throw IllegalStateException(errMsg);
            }
            val multiplier = BASE.pow(input.length - 1 - i);
            bi = bi.add(BigInteger.valueOf(alphaIndex).multiply(multiplier));
            i--;
        }
        return bi;
    }
}
