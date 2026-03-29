package com.cryptopulse.trader.config;

public class AWSConfig {
    // Production AWS Configuration
    public static final String API_BASE_URL = "https://hs28uxr9j6.execute-api.ap-south-1.amazonaws.com/prod";
    public static final String AWS_REGION = "ap-south-1";
    
    // API Endpoints
    public static final String AUTH_ENDPOINT = API_BASE_URL + "/auth";
    public static final String TRADES_ENDPOINT = API_BASE_URL + "/trades";
    public static final String SUBSCRIPTIONS_ENDPOINT = API_BASE_URL + "/subscriptions";
    public static final String PORTFOLIO_ENDPOINT = API_BASE_URL + "/portfolio";
    public static final String MARKET_DATA_ENDPOINT = API_BASE_URL + "/market-data";
    
    // Test Configuration
    public static final String TEST_EMAIL = "shrikananu@gmail.com";
    public static final boolean ENABLE_FREE_TRIAL = true;
    public static final int FREE_TRIAL_HOURS = 24;
}
