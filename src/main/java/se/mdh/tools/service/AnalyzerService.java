package se.mdh.tools.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.mdh.tools.rest.RestClient;

/**
 * A class that ties together the controller and the rest client.
 */
@Service
public class AnalyzerService {
  @Value("${analyzer.jenkins.jobnames}")
  String[] jobnames;

  RestClient restClient;
  public AnalyzerService(RestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * Get a map representation for the dependencies.
   * @return All the dependencies represented as a Map
   * @throws IOException
   */
  public Map<String, Map<String, List<String>>> getDependencies() throws IOException {
    ZipInputStream zis = restClient.getDependenciesInputStream();
    return getStringMapMap(zis);
  }

  private Map<String, Map<String, List<String>>> getStringMapMap(ZipInputStream zipIn) throws IOException {
    ZipEntry entry;
    Map<String, Map<String, List<String>>> mainMap = new TreeMap<>();
    while((entry = zipIn.getNextEntry()) != null) {
      String entryName = entry.getName();

      if(!entryName.startsWith("META")) {
        String resourceName = entryName.substring(0, entryName.indexOf("-dependencyList.txt"));
        Scanner sc = new Scanner(zipIn);
        sc.nextLine();
        sc.nextLine();
        while (sc.hasNextLine()) {
          String dependencyString = sc.nextLine();

          if(!"".equals(dependencyString)) {
            String[] split = dependencyString.split(":");
            String version = split[split.length - 2];
            String groupId = split[0];
            String artifactId = split[1];

            String mainKey = groupId + ":" + artifactId;
            if(mainMap.containsKey(mainKey)) {
              Map<String, List<String>> secondaryMap = mainMap.get(mainKey);
              if(secondaryMap.containsKey(version)) {
                List<String> resourceNames = secondaryMap.get(version);
                if(!resourceNames.contains(resourceName)) {
                  resourceNames.add(resourceName);
                }
              }
              else {
                List<String> resourceNames = new ArrayList<>();
                resourceNames.add(resourceName);
                secondaryMap.put(version, resourceNames);
              }
            }
            else {
              Map<String, List<String>> secondaryMap = new TreeMap<>();
              List<String> resourceNames = new ArrayList<>();
              resourceNames.add(resourceName);
              secondaryMap.put(version, resourceNames);
              mainMap.put(mainKey, secondaryMap);
            }
          }
        }
      }
    }
    return mainMap;
  }
}
