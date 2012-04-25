package drismo.test.logic;

import android.test.AndroidTestCase;
import com.drismo.logic.QualityRater;

/**
 * @author fredrik@kvitvik.no
 */
public class QualityRaterTest extends AndroidTestCase {

    QualityRater qualityRater;
    float score;

    @Override
    protected void setUp() throws Exception {
        qualityRater = new QualityRater();
    }

    public void testCalculateDeltaScore() {
        score = qualityRater.calculateDeltaScore(132, 234);
        assertEquals(30888F, score);
        score = qualityRater.calculateDeltaScore(888, 545);
        assertEquals(483960F, score);
    }
}
