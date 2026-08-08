package bgu.spl.mics.application.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * LiDarDataBase is a singleton class responsible for managing LiDAR data.
 * It provides access to cloud point data and other relevant information for tracked objects.
 */
public class LiDarDataBase {
    private List<StampedCloudPoints> cloudPoints;
    private static LiDarDataBase liDarDataBase;
    private LiDarDataBase(List<StampedCloudPoints> stampedCloudPoints){
        cloudPoints=stampedCloudPoints;
    }
    /**
     * Returns the singleton instance of LiDarDataBase.
     *
     //* @param filePath The path to the LiDAR data file.
     * @return The singleton instance of LiDarDataBase.
     */
    public static LiDarDataBase getInstance(List<StampedCloudPoints> stampedCloudPoints) {
        if(liDarDataBase==null)
            liDarDataBase=new LiDarDataBase(stampedCloudPoints);
        return liDarDataBase;
    }
    public List<CloudPoint> search(int time,String id){
        List<CloudPoint> output=new ArrayList<>();
        boolean found=false;
        for(int i=0;(i<cloudPoints.size())&&(!found);i=i+1){
            if((cloudPoints.get(i)).getTime()>time)
                break;
            if(((cloudPoints.get(i)).getTime()==time)&&((cloudPoints.get(i)).getId().equals("ERROR")))
                return null;
            if((((cloudPoints.get(i)).getId()).equals(id))&&(time==(cloudPoints.get(i)).getTime())){
                for(List<Double> list:(cloudPoints.get(i)).getCloudPoint()){
                    CloudPoint point=new CloudPoint(list.get(0),list.get(1));
                    output.add(point);

                }
            }

        }
        return output;
    }
    public int  getSize(){
        return cloudPoints.size();
    }
}
