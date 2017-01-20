/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.commons.compress.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import static org.apache.commons.compress.utils.ByteUtils.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ByteUtilsTest {

    @Test
    public void fromLittleEndianFromArrayOneArg() {
        byte[] b = new byte[] { 2, 3, 4 };
        assertEquals(2 + 3 * 256 + 4 * 256 * 256, fromLittleEndian(b));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromLittleEndianFromArrayOneArgThrowsForLengthTooBig() {
        fromLittleEndian(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 });
    }

    @Test
    public void fromLittleEndianFromArray() {
        byte[] b = new byte[] { 1, 2, 3, 4, 5 };
        assertEquals(2 + 3 * 256 + 4 * 256 * 256, fromLittleEndian(b, 1, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromLittleEndianFromArrayThrowsForLengthTooBig() {
        fromLittleEndian(new byte[0], 0, 9);
    }

    @Test
    public void fromLittleEndianFromStream() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] { 2, 3, 4, 5 });
        assertEquals(2 + 3 * 256 + 4 * 256 * 256, fromLittleEndian(bin, 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromLittleEndianFromStreamThrowsForLengthTooBig() throws IOException {
        fromLittleEndian(new ByteArrayInputStream(new byte[0]), 9);
    }

    @Test(expected = IOException.class)
    public void fromLittleEndianFromStreamThrowsForPrematureEnd() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] { 2, 3 });
        fromLittleEndian(bin, 3);
    }

    @Test
    public void fromLittleEndianFromSupplier() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] { 2, 3, 4, 5 });
        assertEquals(2 + 3 * 256 + 4 * 256 * 256, fromLittleEndian(new InputStreamByteSupplier(bin), 3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromLittleEndianFromSupplierThrowsForLengthTooBig() throws IOException {
        fromLittleEndian(new InputStreamByteSupplier(new ByteArrayInputStream(new byte[0])), 9);
    }

    @Test(expected = IOException.class)
    public void fromLittleEndianFromSupplierThrowsForPrematureEnd() throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(new byte[] { 2, 3 });
        fromLittleEndian(new InputStreamByteSupplier(bin), 3);
    }

    @Test
    public void toLittleEndianToStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        toLittleEndian(bos, 2 + 3 * 256 + 4 * 256 * 256, 3);
        bos.close();
        assertArrayEquals(new byte[] { 2, 3, 4 }, bos.toByteArray());
    }

    @Test
    public void toLittleEndianToConsumer() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        toLittleEndian(new OutputStreamByteConsumer(bos), 2 + 3 * 256 + 4 * 256 * 256, 3);
        bos.close();
        assertArrayEquals(new byte[] { 2, 3, 4 }, bos.toByteArray());
    }
}
