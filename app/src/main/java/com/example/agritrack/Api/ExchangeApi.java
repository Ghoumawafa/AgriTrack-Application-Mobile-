package com.example.agritrack.Api;

import com.example.agritrack.Models.ExchangeRateResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface ExchangeApi {
    @GET("latest?base=EUR")
    Call<ExchangeRateResponse> getEuroToTnd();
}

