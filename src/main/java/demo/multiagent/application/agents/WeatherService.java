package demo.multiagent.application.agents;

import akka.javasdk.http.HttpClient;
import akka.javasdk.http.HttpClientProvider;
import dev.langchain4j.agent.tool.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class WeatherService {

  private final Logger logger = LoggerFactory.getLogger(WeatherService.class);
  private final String WEATHER_API_KEY = "WEATHER_API_KEY";

  private HttpClient httpClient;

  public WeatherService(HttpClientProvider httpClientProvider) {
    if (System.getenv(WEATHER_API_KEY) != null && !System.getenv(WEATHER_API_KEY).isEmpty()) {
      this.httpClient = httpClientProvider.httpClientFor("https://api.weatherapi.com");
    }
  }

  @Tool("Returns the weather forecast for a given city.")
  public String getWeather(String location) throws UnsupportedEncodingException {

    logger.info("Getting weather forecast for city {}", location);

    if (httpClient == null) {
      logger.warn("Weather API Key not set, using a fake weather forecast");
      return "It's always sunny today at " + location + ".";
    } else {

      var encodedLocation = java.net.URLEncoder.encode(location, StandardCharsets.UTF_8);

      var apiKey = System.getenv(WEATHER_API_KEY);
      String url = String.format("/v1/current.json?&q=%s&aqi=no&key=%s", encodedLocation, apiKey);

      return httpClient.GET(url).invoke().body().toString();
    }
  }
}
