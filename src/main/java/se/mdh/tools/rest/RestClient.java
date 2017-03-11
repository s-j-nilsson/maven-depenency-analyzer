package se.mdh.tools.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.ZipInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthorizationInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by johan on 2017-02-19.
 * Hanterar alla anrop mot andra system.
 */
@Component
public class RestClient {
  private static final Log log = LogFactory.getLog(RestClient.class);

  @Value("${jenkins.username}")
  String username;

  @Value("${jenkins.password}")
  String password;

  @Value("${analyzer.data.resource}")
  String dataResource;

  /**
   * Get the Jenkins depenencies resource
   * @return
   * @throws IOException
   */
  public ZipInputStream getDependenciesInputStream() throws IOException {

    RestTemplate restTemplate = new RestTemplate();

    restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(username, password));

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
    HttpEntity<String> entity = new HttpEntity<>("parameters", httpHeaders);

    ResponseEntity<byte[]> responseEntity = restTemplate.exchange(dataResource, HttpMethod.GET, entity, byte[].class);

    ZipInputStream zipIn = new ZipInputStream(new ByteArrayInputStream(responseEntity.getBody()));
    return zipIn;
  }
}
