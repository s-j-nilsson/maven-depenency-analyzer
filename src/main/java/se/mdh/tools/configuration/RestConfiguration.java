package se.mdh.tools.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by johan on 2017-03-12.
 */
@Configuration
public class RestConfiguration {

  @Value("${analyzer.connection.timeout.ms}")
  int timeout;

  @Bean
  RestTemplate myRestTemplate() {
    RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());
    return restTemplate;
  }

  private ClientHttpRequestFactory getClientHttpRequestFactory() {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
        = new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setConnectTimeout(timeout);
    return clientHttpRequestFactory;
  }

}
