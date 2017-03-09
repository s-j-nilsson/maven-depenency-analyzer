package se.mdh.tools.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import se.mdh.tools.dto.DependencyResponse;
import se.mdh.tools.service.AnalyzerService;

/**
 * The controller class for the analyzer.
 */
@Controller
@RequestMapping("/analyzer")
public class AnalyzerController {
  private static final Log log = LogFactory.getLog(AnalyzerController.class);

  @Value("${analyzer.jenkins.jobnames.filter:}")
  String jobnameFilter;

  private AnalyzerService analyzerService;

  public AnalyzerController(AnalyzerService analyzerService) {
    this.analyzerService = analyzerService;
  }

  @RequestMapping("/standard")
  public String printDependencies(Model model) throws IOException, URISyntaxException {
    populateModel(model);
    return "standard";
  }
  @RequestMapping("")
  public String printDependenciesTable(Model model) throws IOException, URISyntaxException {
    populateModel(model);
    return "table";
  }

  private void populateModel(Model model) throws IOException {
    long start = System.currentTimeMillis();
    List<String> filteredList = new ArrayList<>();

    if(isFiltered()) {
      filteredList = Arrays.asList(jobnameFilter.split("\\s*,\\s*"));
    }

    DependencyResponse dependencyResponse = analyzerService.getDependencies(filteredList);

    long stop = System.currentTimeMillis();
    log.info("Getting files from Jenkins took " + (stop - start) + " ms");

    model.addAttribute("dependencies", dependencyResponse.getDependencyMap());
    model.addAttribute("isFiltered", isFiltered());
    model.addAttribute("filter", jobnameFilter);
    model.addAttribute("missingFilter", getJobNamesWithoutDependenciesExposed(dependencyResponse));
  }

  /**
   * Filter on specific Jenkins job names?
   * @return
   */
  public boolean isFiltered() {
    boolean filtered = false;

    if(!StringUtils.isEmpty(jobnameFilter)) {
      filtered = true;
    }
    return filtered;
  }

  private String getJobNamesWithoutDependenciesExposed(DependencyResponse dependencyResponse) {
    List<String> jobNamesWithoutDependenciesExposed = new ArrayList();
    List<String> jobsIncluded = dependencyResponse.getJobsIncluded();
    List<String> jobNameFilterList = Arrays.asList(jobnameFilter.split("\\s*,\\s*"));
    for(String jobName : jobNameFilterList) {
      if(!jobsIncluded.contains(jobName)) {
        jobNamesWithoutDependenciesExposed.add(jobName);
      }
    }
    return String.join(", ", jobNamesWithoutDependenciesExposed);
  }
}
