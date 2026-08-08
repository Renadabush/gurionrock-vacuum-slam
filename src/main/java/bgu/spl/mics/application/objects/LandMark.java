package bgu.spl.mics.application.objects;

import java.util.List;

/**
 * Represents a landmark in the environment map.
 * Landmarks are identified and updated by the FusionSlam service.
 */
public class LandMark {
    private String id;
    private String Description;
    private List<CloudPoint> Coordinates;
    public LandMark(String id,String Description,List<CloudPoint> coordinates){
        this.id=id;
        this.Description=Description;
        this.Coordinates=coordinates;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCoordinates(List<CloudPoint> coordinates) {
        Coordinates = coordinates;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDescription() {
        return Description;
    }

    public List<CloudPoint> getCoordinates() {
        return Coordinates;
    }
    public String getId() {
        return id;
    }
    public String toString() {
        return "LandMark{id='" + id + "', description='" + Description + "', coordinates=";
    }
}
