package storagemanager;

/**
 * Created by ananya on 26/10/15.
 * Not taking the code from https://github.com/felixr/java-spatialindex
 * The disk storage manager in the library supports Random Page Access.
 * Need Sequential Page Access for nodes belonging to the same level of the spatial index
 */

public class DiskStorageManager implements IStorageManager {
    @Override
    public void flush() {

    }

    @Override
    public byte[] loadByteArray(int id) {
        return new byte[0];
    }

    @Override
    public int storeByteArray(int id, byte[] data) {
        return 0;
    }

    @Override
    public void deleteByteArray(int id) {

    }
}