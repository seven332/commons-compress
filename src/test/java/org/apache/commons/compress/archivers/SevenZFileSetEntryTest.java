package org.apache.commons.compress.archivers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.compress.BaseTestCase;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SevenZFileSetEntryTest extends BaseTestCase {

    private SevenZFile file;
    private List<SevenZArchiveEntry> entries;

    @Before
    public void before() throws IOException {
        file = new SevenZFile(getFile("COMPRESS-348.7z"));
        entries = new ArrayList<>();
        for (SevenZArchiveEntry entry : file.getEntries()) {
            entries.add(entry);
        }
    }

    @After
    public void after() throws IOException {
        file.close();
    }

    @Test
    public void testEntries() {
        assertEquals(5, entries.size());
        assertEquals("1.txt", entries.get(0).getName());
        assertEquals("3.txt", entries.get(1).getName());
        assertEquals("4.txt", entries.get(2).getName());
        assertEquals("2.txt", entries.get(3).getName());
        assertEquals("5.txt", entries.get(4).getName());
    }

    @Test
    public void testFromStartToFirstEntry() throws IOException {
        byte[] bytes = new byte[3];
        file.setEntry(entries.get(0));
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("foo".getBytes(), bytes);
    }

    @Test
    public void testFromFirstEntryToFirstEntry() throws IOException {
        byte[] bytes = new byte[3];
        file.setEntry(entries.get(0));
        file.setEntry(entries.get(0));
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("foo".getBytes(), bytes);
    }

    @Test
    public void testFromFirstEntryMidToFirstEntry() throws IOException {
        byte[] bytes = new byte[3];
        file.setEntry(entries.get(0));
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("foo".getBytes(), bytes);
        file.setEntry(entries.get(0));
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("foo".getBytes(), bytes);
    }

    @Test
    public void testFromSecondEntryToFirstEntry() throws IOException {
        byte[] bytes = new byte[3];
        file.getNextEntry();
        file.getNextEntry();
        file.setEntry(entries.get(0));
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("foo".getBytes(), bytes);
    }

    @Test
    public void testFromSecondEntryMidToFirstEntry() throws IOException {
        byte[] bytes = new byte[3];
        file.getNextEntry();
        file.getNextEntry();
        assertEquals(3, readFully(file, bytes));
        file.setEntry(entries.get(0));
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("foo".getBytes(), bytes);
    }

    @Test
    public void testNextEntryAfterSetEntry() throws IOException {
        byte[] bytes = new byte[3];
        file.setEntry(entries.get(0));
        assertEquals(3, readFully(file, bytes));
        file.getNextEntry();
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("bar".getBytes(), bytes);
    }

    @Test
    public void testToDifferentFolder() throws IOException {
        byte[] bytes = new byte[3];
        file.getNextEntry();
        file.setEntry(entries.get(2));
        assertEquals(3, readFully(file, bytes));
        assertArrayEquals("baz".getBytes(), bytes);
    }

    @Test
    public void testToInvalidFolder() throws IOException {
        byte[] bytes = new byte[3];
        file.getNextEntry();
        file.setEntry(entries.get(3));
        assertEquals(-1, readFully(file, bytes));
        file.setEntry(entries.get(4));
        assertEquals(-1, readFully(file, bytes));
    }

    private static int readFully(SevenZFile file, byte[] bytes) throws IOException {
        return readFully(file, bytes, bytes.length);
    }

    private static int readFully(SevenZFile file, byte[] bytes, int count) throws IOException {
        if (count <= 0) {
            return 0;
        }

        int read = 0;
        do {
            int l = file.read(bytes, read, count - read);
            if (l < 0)
                break;
            read += l;
        } while (read < count);

        if (read == 0) {
            return -1;
        } else {
            return read;
        }
    }
}
