package storagemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by ananya on 30/10/15.
 * Taken from https://github.com/felixr/java-spatialindex
 * Added a memory storage manager.
 */

public class MemoryStorageManager implements IStorageManager {
    private List<Entry> m_buffer = new ArrayList<Entry>();
    private Stack<Integer> m_emptyPages = new Stack<Integer>();

    public void flush() {}

    public byte[] loadByteArray(final int id) {
        Entry e = null;

        try {
            e = m_buffer.get(id);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidPageException(id);
        }

        byte[] ret = new byte[e.m_pData.length];
        System.arraycopy(e.m_pData, 0, ret, 0, e.m_pData.length);
        return ret;
    }

    public int storeByteArray(final int id, final byte[] data) {
        int ret = id;
        Entry e = new Entry(data);

        if (id == NewPage) {
            if (m_emptyPages.empty()) {
                m_buffer.add(e);
                ret = m_buffer.size() - 1;
            } else {
                ret = m_emptyPages.pop();
                m_buffer.set(ret, e);
            }
        } else {
            if (id < 0 || id >= m_buffer.size()) throw new InvalidPageException(id);
            m_buffer.set(id, e);
        }

        return ret;
    }

    public void deleteByteArray(final int id) {
        Entry e = null;
        try {
            e = m_buffer.get(id);
        } catch (IndexOutOfBoundsException ex) {
            throw new InvalidPageException(id);
        }

        m_buffer.set(id, null);
        m_emptyPages.push(id);
    }

    class Entry {
        byte[] m_pData;

        Entry(final byte[] d) {
            m_pData = new byte[d.length];
            System.arraycopy(d, 0, m_pData, 0, d.length);
        }
    }
}
