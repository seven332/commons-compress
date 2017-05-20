/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.utils;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.Charset;
import java.util.Arrays;

import static org.apache.commons.compress.utils.CharsetNames.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MemoryStoreTest {

    private final byte[] testData = "Some data".getBytes(Charset.forName(UTF_8));

    @Test
    public void shouldReadContentsProperly() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
        //when
        int readCount = c.read(readBuffer);
        //then
        assertEquals(testData.length, readCount);
        assertArrayEquals(testData, readBuffer.array());
        assertEquals(testData.length, c.tell());
        c.close();
    }

    @Test
    public void shouldReadContentsWhenBiggerBufferSupplied() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(testData.length + 1);
        //when
        int readCount = c.read(readBuffer);
        //then
        assertEquals(testData.length, readCount);
        assertArrayEquals(testData, Arrays.copyOf(readBuffer.array(), testData.length));
        assertEquals(testData.length, c.tell());
        c.close();
    }

    @Test
    public void shouldReadDataFromSetPosition() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(4);
        //when
        c.seek(5L);
        int readCount = c.read(readBuffer);
        //then
        assertEquals(4L, readCount);
        assertEquals("data", new String(readBuffer.array(), Charset.forName(UTF_8)));
        assertEquals(testData.length, c.tell());
        c.close();
    }

    @Test
    public void shouldSignalEOFWhenPositionAtTheEnd() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        ByteBuffer readBuffer = ByteBuffer.allocate(testData.length);
        //when
        c.seek(testData.length + 1);
        int readCount = c.read(readBuffer);
        //then
        assertEquals(0L, readBuffer.position());
        assertEquals(-1, readCount);
        c.close();
    }

    @Test(expected = ClosedChannelException.class)
    public void shouldThrowExceptionOnReadingClosedChannel() throws IOException {
        //given
        MemoryStore c = new MemoryStore();
        //when
        c.close();
        c.read(ByteBuffer.allocate(1));
    }

    @Test
    public void shouldWriteDataProperly() throws IOException {
        //given
        MemoryStore c = new MemoryStore();
        ByteBuffer inData = ByteBuffer.wrap(testData);
        //when
        int writeCount = c.write(inData);
        //then
        assertEquals(testData.length, writeCount);
        assertArrayEquals(testData, Arrays.copyOf(c.array(), (int) c.size()));
        assertEquals(testData.length, c.tell());
        c.close();
    }

    @Test
    public void shouldWriteDataProperlyAfterPositionSet() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        ByteBuffer inData = ByteBuffer.wrap(testData);
        ByteBuffer expectedData = ByteBuffer.allocate(testData.length + 5).put(testData, 0, 5).put(testData);
        //when
        c.seek(5L);
        int writeCount = c.write(inData);

        //then
        assertEquals(testData.length, writeCount);
        assertArrayEquals(expectedData.array(), Arrays.copyOf(c.array(), (int) c.size()));
        assertEquals(testData.length + 5, c.tell());
        c.close();
    }


    @Test(expected = ClosedChannelException.class)
    public void shouldThrowExceptionOnWritingToClosedChannel() throws IOException {
        //given
        MemoryStore c = new MemoryStore();
        //when
        c.close();
        c.write(ByteBuffer.allocate(1));
    }

    @Test
    public void shouldTruncateContentsProperly() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        //when
        c.truncate(4);
        //then
        byte[] bytes = Arrays.copyOf(c.array(), (int) c.size());
        assertEquals("Some", new String(bytes, Charset.forName(UTF_8)));
        c.close();
    }

    @Test
    public void shouldSetProperPositionOnTruncate() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        //when
        c.seek(testData.length);
        c.truncate(4L);
        //then
        assertEquals(4L, c.tell());
        assertEquals(4L, c.size());
        c.close();
    }

    @Test
    public void shouldSetProperPosition() throws IOException {
        //given
        MemoryStore c = new MemoryStore(testData);
        //when
        c.seek(4L);
        long posAtFour = c.tell();
        c.seek(testData.length);
        long posAtTheEnd = c.tell();
        c.seek(testData.length + 1L);
        long posPastTheEnd = c.tell();
        //then
        assertEquals(4L, posAtFour);
        assertEquals(c.size(), posAtTheEnd);
        assertEquals(posPastTheEnd, posPastTheEnd);
        c.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenSettingIncorrectPosition() throws IOException {
        //given
        MemoryStore c = new MemoryStore();
        //when
        c.seek(Integer.MAX_VALUE + 1L);
        c.close();
    }

    @Test(expected = ClosedChannelException.class)
    public void shouldThrowExceptionWhenSettingPositionOnClosedChannel() throws IOException {
        //given
        MemoryStore c = new MemoryStore();
        //when
        c.close();
        c.seek(1L);
    }

}
