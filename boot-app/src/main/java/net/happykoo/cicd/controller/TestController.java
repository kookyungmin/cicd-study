package net.happykoo.cicd.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class TestController {

  @Value("${spring.application.name}")
  private String applicationName;

  @GetMapping
  public ResponseEntity<String> test() {
    return ResponseEntity.ok(String.format("%s status: OK", applicationName));
  }
}
