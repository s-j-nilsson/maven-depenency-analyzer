package se.mdh.tools.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.mdh.tools.service.AnalyzerService;

/**
 * Created by johan on 2017-03-05.
 */
@Controller
@RequestMapping("/analyzer")
public class AnalyzerController {
  private static final Log log = LogFactory.getLog(AnalyzerController.class);


  private AnalyzerService analyzerService;

  public AnalyzerController(AnalyzerService analyzerService) {
    this.analyzerService = analyzerService;
  }

  @RequestMapping("/standard")
  public String printDependencies(Model model) throws IOException, URISyntaxException {
    long start = System.currentTimeMillis();
    Map<String, Map<String, List<String>>> dependencies = analyzerService.getDependencies();
    long stop = System.currentTimeMillis();
    log.info("Getting files from Jenkins took " + (stop - start) + " ms");

    model.addAttribute("dependencies", dependencies);
    return "standard";
  }
}
