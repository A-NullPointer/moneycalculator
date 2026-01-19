package infrastructure.config;

public class ApiConfig {
    private static final String API_KEY = "aeb1cd5ef6081142040d717f";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    
    public static String getApiUrl() {
        return BASE_URL + API_KEY + "/";
    }
    
    public static String getCodesUrl() {
        return getApiUrl() + "codes";
    }
    
    public static String getPairUrl(String from, String to) {
        return getApiUrl() + "pair/" + from + "/" + to;
    }
}
