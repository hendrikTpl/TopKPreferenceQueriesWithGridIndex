package quadtreeindex;

/**
 * Created by ananya on 30/10/15.
 * Modified from https://github.com/felixr/java-spatialindex
 *
 * This spatial index maps 2D space onto a grid index.
 * Each grid is divided into 4 children in the subsequent levels.
 *
 * The quad tree divides all of space including empty space. Nodes mapping empty space
 * do not have any summary of spatial objects and hence do not occupy a lot of space on the disk.
 * Multiple empty nodes must be compressed together onto a single disk page for efficient storage of the index.
 *   ____ ____
 *  |    |    |
 *  |____|____|
 *  |    |    |
 *  |____|____|
 *
 *  This spatial index is designed to map the spatial location of an object along with its textual description
 *  and non-textual rating. A set of interfaces, common to all spatial indices, are used to define functions
 *  associated with this spatial index. Parameters passed to these functions have been modified to store and operate
 *  on 2D points.
 *
 * This index supports all the common query types for a spatial index, such as containment, intersection, pointLocation
 * and nearest neighbour queries.
 */

public interface ISpatialIndex {
    public void flush() throws IllegalStateException;
    public void insertData();
    public void deleteData();
    public void containmentQuery();
    public void intersectionQuery();
    public void pointLocationQuery();
    public void nearestNeighbourQuery();
    public boolean isIndexValid();
    public IStatistics getIndexStatistics();
}
