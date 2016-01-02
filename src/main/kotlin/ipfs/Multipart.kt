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

private val LINE_FEED = "\r\n";


public class Multipart(requestURL: String, charset: String) {
    private val httpConn: HttpURLConnection;
    private val out: OutputStream;
    private val writer: PrintWriter;
    
    private val boundary = createBoundary();
    private val charset  = charset; 

    init {
        val url = URL(requestURL);
        httpConn = url.openConnection() as HttpURLConnection;
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true);
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type",
                                    "multipart/form-data; boundary=$boundary");
        httpConn.setRequestProperty("User-Agent",
                                    "Java IPFS Client");
        out = httpConn.getOutputStream();
        writer = PrintWriter(OutputStreamWriter(out, charset), true);
    }

    companion object Static {
        val allowed
          = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
          
        public fun createBoundary(): String {
            val r = Random();
            val b = StringBuilder();
            var i = 0;
            while(i < 32) {
                i++;
                b.append(allowed.charAt(r.nextInt(allowed.length())));
                i++;
            }
            return b.toString();
        }
    }

    public fun addFormField(name: String, value: String) {
        writer.append("--" + boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"$name\"");
        writer.append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=$charset")
        writer.append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value);
        writer.append(LINE_FEED);
        writer.flush();
    }

    public fun addFilePart(fieldName: String, uploadFile: NamedStreamable) {
        val fileName = uploadFile.getName();
        writer.append("--" + boundary);
        writer.append(LINE_FEED);
        writer.append("Content-Disposition: ");
        writer.append("file; ")
        writer.append("name=\"$fieldName\"; ");
        if(fileName.isPresent()) {
            writer.append("filename=\"${fileName.get()}\"");
        }
        writer.append(LINE_FEED);
        writer.append("Content-Type: application/octet-stream").append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        val inputStream = uploadFile.getInputStream();
        val buffer = ByteArray(4096);
        var r: Int;
        while(true) {
            r = inputStream.read(buffer);
            if(r == -1) { break; }
            out.write(buffer, 0, r);
        }
        out.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    public fun addHeaderField(name: String, value: String) {
        writer.append("${name}: ${value}")
        writer.append(LINE_FEED);
        writer.flush();
    }

    public fun finish(): String {
        val b = StringBuilder();

        writer.append("--${boundary}--");
        writer.append(LINE_FEED);
        writer.close();

        val status = httpConn.getResponseCode();
        if(status == HttpURLConnection.HTTP_OK) {
            val inS = httpConn.getInputStream();
            val reader = BufferedReader(InputStreamReader(inS));
            var line: String;
            while(true) {
                line = reader.readLine()
                if(line == null) { break; }
                b.append(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw IOException("Server returned status: $status");
        }

        return b.toString();
    }
}
