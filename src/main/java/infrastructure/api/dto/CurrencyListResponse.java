package infrastructure.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CurrencyListResponse {
    @SerializedName("result")
    private String result;
    
    @SerializedName("supported_codes")
    private List<List<String>> supportedCodes;

    public String getResult() { return result; }
    public List<List<String>> getSupportedCodes() { return supportedCodes; }
}
