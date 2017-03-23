package se.mdh.tools.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.springframework.stereotype.Service;
import se.mdh.tools.dto.DependencyResponse;
import se.mdh.tools.rest.RestClient;

/**
 * A class that ties together the controller and the rest client.
 */
@Service
public class AnalyzerService {

  RestClient restClient;
  public AnalyzerService(RestClient restClient) {
    this.restClient = restClient;
  }

  /**
   * Get a map representation for the dependencies.
   * @return All the dependencies represented as a Map
   * @throws IOException
   */
  public DependencyResponse getDependencies(List<String> jobnameFilter) throws IOException {
    ZipInputStream zis = restClient.getDependenciesInputStream();
    return getDependencies(zis, jobnameFilter);
  }

  private DependencyResponse getDependencies(ZipInputStream zipIn, List<String> jobnameFilter) throws IOException {
    DependencyResponse dependencyResponse = new DependencyResponse();
    ZipEntry entry;
    Map<String, Map<String, List<String>>> mainMap = new TreeMap<>();
    while((entry = zipIn.getNextEntry()) != null) {
      String entryName = entry.getName();

      if(!entryName.startsWith("META")) {
        String resourceName = entryName.substring(0, entryName.indexOf("-dependencyList.txt"));
        if(isAccepted(jobnameFilter, resourceName)) {
          dependencyResponse.addJobIncluded(resourceName);
          Scanner sc = new Scanner(zipIn);
          sc.nextLine();
          sc.nextLine();
          while (sc.hasNextLine()) {
            String dependencyString = sc.nextLine();

            if(!"".equals(dependencyString) && !"none".equals(dependencyString.trim())) {
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
    }
    dependencyResponse.setDependencyMap(mainMap);

    return dependencyResponse;
  }

  private boolean isAccepted(List<String> jobnameFilter, String resourceName) {
    if(jobnameFilter.isEmpty() || jobnameFilter.contains(resourceName)) {
      return true;
    }

    for(String filterString : jobnameFilter) {
      Pattern p = Pattern.compile(filterString, Pattern.CASE_INSENSITIVE);
      Matcher m = p.matcher(resourceName);

      if (m.matches()) {
        return true;
      }
    }
    return false;
  }
}
