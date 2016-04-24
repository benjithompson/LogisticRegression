import org.apache.commons.math3.linear.RealMatrix;

/**
 * Created by Smaddady on 4/23/2016.
 */
public class LogisticRegression {
    Dataset data;
    RealMatrix trainData;
    RealMatrix devData;

    public LogisticRegression(Dataset data) {
        this.data = data;
    }

}
