package storagemanager;

/**
 * Created by ananya on 26/10/15.
 * Modified from from https://github.com/felixr/java-spatialindex
 */

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class DiskStorageManager implements IStorageManager {
    private RandomAccessFile m_dataFile = null;
    private RandomAccessFile m_indexFile = null;
    private int m_pageSize = 0;
    private int m_nextPage = -1;
    private TreeSet<Integer> m_emptyPages = new TreeSet<Integer>();
    private Map<Integer,Entry> m_pageIndex = new HashMap<Integer,Entry>();
    private byte[] m_buffer = null;

    public DiskStorageManager(String fileName, int pageSize, boolean overwrite)
            throws SecurityException, NullPointerException, IOException, IllegalArgumentException {
        Object var;

        // Open/Create flag.
        boolean bOverwrite = false;
        var = overwrite;

        bOverwrite = ((Boolean) var);

        // storage filename.
        var = fileName;

        if (var != null) {
            File indexFile = new File((String) var + ".idx");
            File dataFile = new File((String) var + ".dat");

            // check if files exist.
            if (!bOverwrite  && (! indexFile.exists() || ! dataFile.exists())) bOverwrite = true;

            if (bOverwrite) {
                if (indexFile.exists()) indexFile.delete();
                if (dataFile.exists()) dataFile.delete();

                boolean b = indexFile.createNewFile();
                if (!b) throw new IOException("Index file cannot be opened.");

                b = dataFile.createNewFile();
                if (!b) throw new IOException("Data file cannot be opened.");
            }

            m_indexFile = new RandomAccessFile(indexFile, "rw");
            m_dataFile = new RandomAccessFile(dataFile, "rw");
        } else {
            throw new IllegalArgumentException("Property FileName was not specified.");
        }

        // find page size.
        if (bOverwrite) {
            var = pageSize;

            if (! (var instanceof Integer)) throw new IllegalArgumentException("Property PageSize must be an Integer");
            m_pageSize = ((Integer) var);
            m_nextPage = 0;

        } else {
            try {
                m_pageSize = m_indexFile.readInt();
            } catch (EOFException ex) {
                throw new IllegalStateException("Failed reading pageSize.");
            }

            try {
                m_nextPage = m_indexFile.readInt();
            } catch (EOFException ex) {
                throw new IllegalStateException("Failed reading nextPage.");
            }
        }

        // create buffer.
        m_buffer = new byte[m_pageSize];

        if (!bOverwrite) {
            int count, id, page;

            // load empty pages in memory.
            try {
                count = m_indexFile.readInt();

                for (int cCount = 0; cCount < count; cCount++) {
                    page = m_indexFile.readInt();
                    m_emptyPages.add(page);
                }

                // load index table in memory.
                count = m_indexFile.readInt();

                for (int cCount = 0; cCount < count; cCount++) {
                    Entry e = new Entry();

                    id = m_indexFile.readInt();
                    e.m_length = m_indexFile.readInt();

                    int count2 = m_indexFile.readInt();

                    for (int cCount2 = 0; cCount2 < count2; cCount2++) {
                        page = m_indexFile.readInt();
                        e.m_pages.add(page);
                    }
                    m_pageIndex.put(id, e);
                }
            } catch (EOFException ex) {
                throw new IllegalStateException("Corrupted index file.");
            }
        }
    }

    public void flush() {
        try {
            m_indexFile.seek(0l);

            m_indexFile.writeInt(m_pageSize);
            m_indexFile.writeInt(m_nextPage);

            int id, page;
            int count = m_emptyPages.size();

            m_indexFile.writeInt(count);

            Iterator it = m_emptyPages.iterator();
            while (it.hasNext()) {
                page = ((Integer) it.next());
                m_indexFile.writeInt(page);
            }

            count = m_pageIndex.size();
            m_indexFile.writeInt(count);

            it = m_pageIndex.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry me = (Map.Entry) it.next();
                id = ((Integer) me.getKey());
                m_indexFile.writeInt(id);

                Entry entry = (Entry) me.getValue();
                count = entry.m_length;
                m_indexFile.writeInt(count);

                count = entry.m_pages.size();
                m_indexFile.writeInt(count);

                for (int cIndex = 0; cIndex < count; cIndex++) {
                    page = entry.m_pages.get(cIndex);
                    m_indexFile.writeInt(page);
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Corrupted index file.");
        }
    }

    public byte[] loadByteArray(final int id) {
        Entry entry = m_pageIndex.get(id);
        if (entry == null) throw new InvalidPageException(id);

        int cNext = 0;
        int cTotal = entry.m_pages.size();

        byte[] data = new byte[entry.m_length];
        int cIndex = 0;
        int cLen;
        int cRem = entry.m_length;

        do {
            try {
                m_dataFile.seek(entry.m_pages.get(cNext) * m_pageSize);
                int bytesread = m_dataFile.read(m_buffer);
                if (bytesread != m_pageSize) throw new IllegalStateException("Corrupted data file.");
            } catch (IOException ex) {
                throw new IllegalStateException("Corrupted data file.");
            }

            cLen = (cRem > m_pageSize) ? m_pageSize : cRem;
            System.arraycopy(m_buffer, 0, data, cIndex, cLen);

            cIndex += cLen;
            cRem -= cLen;
            cNext++;
        }
        while (cNext < cTotal);

        return data;
    }

    public int storeByteArray(final int id, final byte[] data) {
        if (id == NewPage) {
            Entry e = new Entry();
            e.m_length = data.length;

            int cIndex = 0;
            int cPage;
            int cRem = data.length;
            int cLen;

            while (cRem > 0) {
                if (! m_emptyPages.isEmpty()) {
                    Integer i = m_emptyPages.first();
                    m_emptyPages.remove(i);
                    cPage = i;
                }
                else {
                    cPage = m_nextPage;
                    m_nextPage++;
                }

                cLen = (cRem > m_pageSize) ? m_pageSize : cRem;
                System.arraycopy(data, cIndex, m_buffer, 0, cLen);

                try {
                    m_dataFile.seek(cPage * m_pageSize);
                    m_dataFile.write(m_buffer);
                }
                catch (IOException ex) {
                    throw new IllegalStateException("Corrupted data file.");
                }

                cIndex += cLen;
                cRem -= cLen;
                e.m_pages.add(cPage);
            }

            Integer i = e.m_pages.get(0);
            m_pageIndex.put(i, e);

            return i;
        } else {
            // find the entry.
            Entry oldEntry = m_pageIndex.get(id);
            if (oldEntry == null) throw new InvalidPageException(id);

            m_pageIndex.remove(id);

            Entry e = new Entry();
            e.m_length = data.length;

            int cIndex = 0;
            int cPage;
            int cRem = data.length;
            int cLen, cNext = 0;

            while (cRem > 0) {
                if (cNext < oldEntry.m_pages.size()) {
                    cPage = oldEntry.m_pages.get(cNext);
                    cNext++;
                } else if (! m_emptyPages.isEmpty()) {
                    Integer i = m_emptyPages.first();
                    m_emptyPages.remove(i);
                    cPage = i;
                } else {
                    cPage = m_nextPage;
                    m_nextPage++;
                }

                cLen = (cRem > m_pageSize) ? m_pageSize : cRem;
                System.arraycopy(data, cIndex, m_buffer, 0, cLen);

                try {
                    m_dataFile.seek(cPage * m_pageSize);
                    m_dataFile.write(m_buffer);
                } catch (IOException ex) {
                    throw new IllegalStateException("Corrupted data file.");
                }

                cIndex += cLen;
                cRem -= cLen;
                e.m_pages.add(cPage);
            }

            while (cNext < oldEntry.m_pages.size()) {
                m_emptyPages.add(oldEntry.m_pages.get(cNext));
                cNext++;
            }

            Integer i = e.m_pages.get(0);
            m_pageIndex.put(i, e);

            return i;
        }
    }

    public void deleteByteArray(final int id) {
        // find the entry.
        Entry e = m_pageIndex.get(id);
        if (e == null) throw new InvalidPageException(id);

        m_pageIndex.remove(id);

        for (int cIndex = 0; cIndex < e.m_pages.size(); cIndex++) {
            m_emptyPages.add(e.m_pages.get(cIndex));
        }
    }

    public void close() {
        flush();
    }

    class Entry {
        int m_length = 0;
        List<Integer> m_pages = new ArrayList<Integer>();
    }
}