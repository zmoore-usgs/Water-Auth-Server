package gov.usgs.wma.mlrauthserver.model;

public class WaterAuthUserDetails {
    private String officeState;

    public WaterAuthUserDetails() {

    }

    public WaterAuthUserDetails(String officeState) {
        this.officeState = officeState;
    }

    public String getOfficeState() {
        return officeState;
    }

    public void setOfficeState(String officeState) {
        this.officeState = officeState;
    }
}