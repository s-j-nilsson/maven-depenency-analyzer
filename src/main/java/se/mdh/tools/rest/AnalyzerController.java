package se.mdh.tools.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.mdh.tools.dto.DependencyResponse;
import se.mdh.tools.service.AnalyzerService;

/**
 * The controller class for the analyzer.
 */
@Controller
@RequestMapping("/analyzer")
public class AnalyzerController {
  private static final Log log = LogFactory.getLog(AnalyzerController.class);

  private AnalyzerService analyzerService;

  public AnalyzerController(AnalyzerService analyzerService) {
    this.analyzerService = analyzerService;
  }

  @RequestMapping("")
  public String printDependenciesTable(Model model, @RequestParam("searchfilter") Optional<String> searchfilter) throws IOException, URISyntaxException {
    if(searchfilter.isPresent()) {
      populateModel(model, searchfilter.get());
    } else {
      populateModel(model, "");
    }
    return "table";
  }

  private void populateModel(Model model, String searchfilter) throws IOException {
    long start = System.currentTimeMillis();
    List<String> filteredList = new ArrayList<>();

    if(isFiltered(searchfilter)) {
      filteredList = Arrays.asList(searchfilter.split("\\s*,\\s*"));
    }

    DependencyResponse dependencyResponse = analyzerService.getDependencies(filteredList);

    long stop = System.currentTimeMillis();
    log.info("Getting files from Jenkins took " + (stop - start) + " ms");

    model.addAttribute("dependencies", dependencyResponse.getDependencyMap());
    model.addAttribute("isFiltered", isFiltered(searchfilter));
    model.addAttribute("filter", searchfilter);
    model.addAttribute("missingFilter", getJobNamesWithoutDependenciesExposed(dependencyResponse, searchfilter));
  }

  /**
   * Filter on specific Jenkins job names?
   * @return
   */
  public boolean isFiltered(String searchfilter) {
    boolean filtered = false;

    if(!StringUtils.isEmpty(searchfilter)) {
      filtered = true;
    }
    return filtered;
  }

  private String getJobNamesWithoutDependenciesExposed(DependencyResponse dependencyResponse, String searchfilter) {
    Map<String, Boolean> matchingMap = new HashMap<>();

    List<String> jobsIncluded = dependencyResponse.getJobsIncluded();
    List<String> jobNameFilterList = Arrays.asList(searchfilter.split("\\s*,\\s*"));

    for(String jobNameFilter : jobNameFilterList) {
      matchingMap.put(jobNameFilter, Boolean.FALSE);
    }

    for(String jobIncluded : jobsIncluded) {
      for(String key : matchingMap.keySet()) {
        Pattern p = Pattern.compile(key, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(jobIncluded);
        if (m.matches()) {
          matchingMap.put(key, Boolean.TRUE);
        }
      }
    }

    List<String> jobNamesWithoutDependenciesExposedList = matchingMap.keySet().stream()
        .filter(k -> !matchingMap.get(k))
        .collect(Collectors.toList());

    return String.join(", ", jobNamesWithoutDependenciesExposedList);
  }
}
