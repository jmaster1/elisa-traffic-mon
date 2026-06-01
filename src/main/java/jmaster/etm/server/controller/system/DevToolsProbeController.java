package jmaster.etm.server.controller.system;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DevToolsProbeController {

    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    ResponseEntity<Void> chromeDevToolsProbe() {
        return ResponseEntity.notFound().build();
    }
}
