package blackshoe.estheteuserservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sangeun")
public class TestController {
    @GetMapping("/shinhan_security_intern_cicd_test")
    String test() {
        return "ICT 기획/운영부 CI/CD 테스트";
    }
}
