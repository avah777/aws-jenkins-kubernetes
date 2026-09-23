package com.devops.app;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
@RestController
public class HelloController {
    @GetMapping("/")
    public String home() {
        return "Hello from Jenkins + Docker + Kubernetes + AWS V2.0 webhook!";
    }
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "application", "aws-jenkins-kubernetes"
        );
    }
}
