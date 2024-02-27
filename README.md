1. kms local docker 실행 
```docker-compose up -d```
2. spring boot local 프로파일 실행
3. local api test 
```curl -X GET http://localhost:8080/aws-kms-test/test1234```


AWS Cli 를 통해 키 생성 가능 

``` aws --endpoint-url=http://localhost:58080 kms create-key | jq ```
```{
  "KeyMetadata": {
    "AWSAccountId": "555555555555",
    "KeyId": "8ea8a8e0-c0bc-4ae7-93aa-767ac15cde11",
    "Arn": "arn:aws:kms:ap-northeast-2:555555555555:key/8ea8a8e0-c0bc-4ae7-93aa-767ac15cde11",
    "CreationDate": "2024-02-28T08:41:32+09:00",
    "Enabled": true,
    "KeyUsage": "ENCRYPT_DECRYPT",
    "KeyState": "Enabled",
    "Origin": "AWS_KMS",
    "KeyManager": "CUSTOMER",
    "CustomerMasterKeySpec": "SYMMETRIC_DEFAULT",
    "KeySpec": "SYMMETRIC_DEFAULT",
    "EncryptionAlgorithms": [
      "SYMMETRIC_DEFAULT"
    ]
  }
}
```

생성된 키 확인
```aws --endpoint-url=http://localhost:58080 kms list-keys | jq```
