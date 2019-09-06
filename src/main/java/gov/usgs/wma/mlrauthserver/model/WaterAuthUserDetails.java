package gov.usgs.wma.mlrauthserver.model;

import java.io.Serializable;

public class WaterAuthUserDetails implements Serializable {

	private static final long serialVersionUID = 1L;
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