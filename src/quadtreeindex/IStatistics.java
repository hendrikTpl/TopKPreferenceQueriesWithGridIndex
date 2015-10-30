package quadtreeindex;

/**
 * Created by ananya on 30/10/15.
 */

public interface IStatistics {
    public long getNumberOfDataObjects();
    public long getNumberOfFeatureObjects();
    public int getNumberOfTypesOfFeatureObjects();
    public int getNumberOfLevels();
    public long getNumberOfNodes();
}
