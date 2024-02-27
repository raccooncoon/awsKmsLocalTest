package org.example.localkmstest;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class AwsKmsController {

  private final AwsKmsService awsKmsService;

  @GetMapping("/aws-kms-test/{text}")
  public ResponseEntity<Object> test(@PathVariable String text) {

    log.info("text = {}", text);

    String encryptText = awsKmsService.encrypt(text);

    String decryptText = awsKmsService.decrypt(encryptText);

    Map<String, String> response = Map.of(
        "encryptText", encryptText,
        "decryptText", decryptText
    );

    return ResponseEntity.ok(response);
  }
}
