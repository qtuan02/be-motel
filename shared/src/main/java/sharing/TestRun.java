package sharing;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestRun {
    private static final Logger log = LoggerFactory.getLogger(TestRun.class);

    @PostConstruct
    public void init() {
        log.info("====================================================");
        log.info("SHARED MODULE IS ACTIVE AND SCANNING SUCCESSFULLY!");
        log.info("====================================================");
    }
}
