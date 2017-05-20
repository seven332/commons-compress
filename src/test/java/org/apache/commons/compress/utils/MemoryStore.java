/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.compress.utils;

/*
 * Created by Hippo on 1/13/2017.
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.Buffer;
import okio.Store;
import okio.Timeout;

public class MemoryStore implements Store {

  private static final int NAIVE_RESIZE_LIMIT = Integer.MAX_VALUE >> 1;

  private byte[] data;
  private final AtomicBoolean closed = new AtomicBoolean();
  private int position, size;

  public MemoryStore(byte[] data) {
    this.data = data;
    size = data.length;
  }

  public MemoryStore() {
    this(new byte[0]);
  }

  public MemoryStore(int size) {
    this(new byte[size]);
  }

  @Override
  public void seek(long position) throws IOException {
    ensureOpen();
    if (position < 0L || position > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("Position has to be in range 0.. " + Integer.MAX_VALUE);
    }
    this.position = (int) position;
  }

  @Override
  public long tell() throws IOException {
    return position;
  }

  @Override
  public long size() throws IOException {
    return size;
  }

  public void truncate(long newSize) {
    if (size > newSize) {
      size = (int) newSize;
    }
    repositionIfNecessary();
  }

  @Override
  public long read(Buffer sink, long byteCount) throws IOException {
    ensureOpen();
    repositionIfNecessary();
    int wanted = (int) byteCount;
    int possible = size - position;
    if (possible <= 0) {
      return -1;
    }
    if (wanted > possible) {
      wanted = possible;
    }
    sink.write(data, position, wanted);
    position += wanted;
    return wanted;
  }

  public int read(ByteBuffer buf) throws IOException {
    ensureOpen();
    repositionIfNecessary();
    int wanted = buf.remaining();
    int possible = size - position;
    if (possible <= 0) {
      return -1;
    }
    if (wanted > possible) {
      wanted = possible;
    }
    buf.put(data, position, wanted);
    position += wanted;
    return wanted;
  }

  @Override
  public void write(Buffer source, long byteCount) throws IOException {
    ensureOpen();
    int wanted = (int) byteCount;
    int possibleWithoutResize = size - position;
    if (wanted > possibleWithoutResize) {
      int newSize = position + wanted;
      if (newSize < 0) { // overflow
        resize(Integer.MAX_VALUE);
        wanted = Integer.MAX_VALUE - position;
      } else {
        resize(newSize);
      }
    }

    for (int remain = wanted; remain != 0;) {
      int read = source.read(data, position, remain);
      remain -= read;
      position += read;
      if (size < position) {
        size = position;
      }
    }

    if (wanted != byteCount) throw new IOException();
  }

  public int write(ByteBuffer b) throws IOException {
    ensureOpen();
    int wanted = b.remaining();
    int possibleWithoutResize = size - position;
    if (wanted > possibleWithoutResize) {
      int newSize = position + wanted;
      if (newSize < 0) { // overflow
        resize(Integer.MAX_VALUE);
        wanted = Integer.MAX_VALUE - position;
      } else {
        resize(newSize);
      }
    }
    b.get(data, position, wanted);
    position += wanted;
    if (size < position) {
      size = position;
    }
    return wanted;
  }

  @Override
  public void flush() throws IOException {}

  @Override
  public Timeout timeout() {
    return null;
  }

  @Override
  public void close() {
    closed.set(true);
  }

  private void resize(int newLength) {
    int len = data.length;
    if (len <= 0) {
      len = 1;
    }
    if (newLength < NAIVE_RESIZE_LIMIT) {
      while (len < newLength) {
        len <<= 1;
      }
    } else { // avoid overflow
      len = newLength;
    }
    data = Arrays.copyOf(data, len);
  }

  private boolean isOpen() {
    return !closed.get();
  }

  private void ensureOpen() throws ClosedChannelException {
    if (!isOpen()) {
      throw new ClosedChannelException();
    }
  }

  private void repositionIfNecessary() {
    if (position > size) {
      position = size;
    }
  }

  public byte[] array() {
    return data;
  }
}
