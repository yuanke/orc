/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hive.ql.io.orc;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

class SerializationUtils {

  static void writeVuint(OutputStream output, int value) throws IOException {
    while (true) {
      if ((value & ~0x7f) == 0) {
        output.write((byte) value);
        return;
      } else {
        output.write((byte) (value | 0x80));
        value >>>= 7;
      }
    }
  }

  static void writeVsint(OutputStream output, int value) throws IOException {
    writeVuint(output, (value << 1) ^ (value >> 31));
  }

  static void writeVulong(OutputStream output, long value) throws IOException {
    while (true) {
      if ((value & ~0x7f) == 0) {
        output.write((byte) value);
        return;
      } else {
        output.write((byte) (0x80 | (value & 0x7f)));
        value >>>= 7;
      }
    }
  }

  static void writeVslong(OutputStream output, long value) throws IOException {
    writeVulong(output, (value << 1) ^ (value >> 63));
  }

  static int readVuint(InputStream in) throws IOException {
    int result = 0;
    int b;
    int offset = 0;
    do {
      b = in.read();
      if (b == -1) {
        throw new EOFException("Reading Vuint past EOF");
      }
      result |= (0x7f & b) << offset;
      offset += 7;
    } while (b >= 0x80);
    return result;
  }

  static int readVsint(InputStream in) throws IOException {
    int result = readVuint(in);
    return (result >>> 1) ^ -(result & 1);
  }

  static long readVulong(InputStream in) throws IOException {
    long result = 0;
    int b;
    int offset = 0;
    do {
      b = in.read();
      if (b == -1) {
        throw new EOFException("Reading Vulong past EOF");
      }
      result |= (0x7f & b) << offset;
      offset += 7;
    } while (b >= 0x80);
    return result;
  }

  static long readVslong(InputStream in) throws IOException {
    long result = readVulong(in);
    return (result >>> 1) ^ -(result & 1);
  }

  static float readFloat(InputStream in) throws IOException {
    int ser = in.read() | (in.read() << 8) | (in.read() << 16) |
      (in.read() << 24);
    return Float.intBitsToFloat(ser);
  }

  static void writeFloat(OutputStream output, float value) throws IOException {
    int ser = Float.floatToIntBits(value);
    output.write(ser & 0xff);
    output.write((ser >> 8) & 0xff);
    output.write((ser >> 16) & 0xff);
    output.write((ser >> 24) & 0xff);
  }

  static void writeDouble(OutputStream output, double value) throws IOException {
    long ser = Double.doubleToLongBits(value);
    output.write(((int) ser) & 0xff);
    output.write(((int) (ser >> 16)) & 0xff);
    output.write(((int) (ser >> 24)) & 0xff);
    output.write(((int) (ser >> 32)) & 0xff);
    output.write(((int) (ser >> 40)) & 0xff);
    output.write(((int) (ser >> 48)) & 0xff);
    output.write(((int) (ser >> 56)) & 0xff);
  }
}