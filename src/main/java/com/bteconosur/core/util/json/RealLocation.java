package com.bteconosur.core.util.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RealLocation {

    public double lat;
    
    public double lon;

    @JsonProperty("display_name")
    public String displayName;

}
