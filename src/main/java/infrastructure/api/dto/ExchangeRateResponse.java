package infrastructure.api.dto;

import com.google.gson.annotations.SerializedName;

public class ExchangeRateResponse {
    @SerializedName("result")
    private String result;
    
    @SerializedName("base_code")
    private String baseCode;
    
    @SerializedName("target_code")
    private String targetCode;
    
    @SerializedName("conversion_rate")
    private double conversionRate;
    
    @SerializedName("time_last_update_utc")
    private String timeLastUpdate;

    // Getters
    public String getResult() { return result; }
    public String getBaseCode() { return baseCode; }
    public String getTargetCode() { return targetCode; }
    public double getConversionRate() { return conversionRate; }
    public String getTimeLastUpdate() { return timeLastUpdate; }
}
