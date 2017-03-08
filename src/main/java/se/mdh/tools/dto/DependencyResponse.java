package se.mdh.tools.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by johan on 2017-03-08.
 */
public class DependencyResponse {
  private Map<String, Map<String, List<String>>> dependencyMap;
  private List<String> jobsIncluded = new ArrayList<>();

  public Map<String, Map<String, List<String>>> getDependencyMap() {
    return dependencyMap;
  }

  public void setDependencyMap(Map<String, Map<String, List<String>>> dependencyMap) {
    this.dependencyMap = dependencyMap;
  }

  public List<String> getJobsIncluded() {
    return jobsIncluded;
  }

  public void addJobIncluded(String jobName) {
    jobsIncluded.add(jobName);
  }
}
