package storagemanager;

/**
 * Created by ananya on 26/10/15.
 * Taken from https://github.com/felixr/java-spatialindex
 */

public class InvalidPageException extends RuntimeException {
    public InvalidPageException(int id)
    {
        super("" + id);
    }
}