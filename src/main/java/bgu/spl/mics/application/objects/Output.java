package bgu.spl.mics.application.objects;

import java.util.List;
import java.util.Map;

public class Output {
    private List<LandMark> landMarks;
    private StatisticalFolder statisticalFolder;
    public Output(List<LandMark> landMarks, StatisticalFolder statisticalFolder){
        this.landMarks=landMarks;
        this.statisticalFolder=statisticalFolder;
    }

    public List<LandMark> getLandMarks() {
        return landMarks;
    }

    public StatisticalFolder getStatisticalFolder() {
        return statisticalFolder;
    }
}
