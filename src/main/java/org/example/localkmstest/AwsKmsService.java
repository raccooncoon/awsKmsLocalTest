package org.example.localkmstest;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CommitmentPolicy;
import com.amazonaws.encryptionsdk.CryptoAlgorithm;
import com.amazonaws.encryptionsdk.CryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.CryptoMaterialsCache;
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache;
import com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.util.Base64;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AwsKmsService {

  @Value("${kms.keyarn}")
  private String kmsKeyArn;

  @Value("${kms.endpoint.url:#{null}}") // local 설정에 만 사용
  private String kmsEndpointUrl;
  private CryptoMaterialsManager cachingCmm;
  private AwsCrypto awsCrypto;

  @PostConstruct
  public void init() {
    System.out.println("KMS Key ARN for Encryption: " + kmsKeyArn);
    System.out.println("KMS Endpoint URL: " + kmsEndpointUrl);

    // 1. Instantiate the SDK
    this.awsCrypto = AwsCrypto.builder()
        .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
        .withEncryptionAlgorithm(CryptoAlgorithm.ALG_AES_256_GCM_HKDF_SHA512_COMMIT_KEY) // algorithmus
        .build();

    // 2. Instantiate an AWS KMS master key provider in strict mode using buildStrict().
    KmsMasterKeyProvider keyProvider = (
        StringUtils.isBlank(kmsEndpointUrl) ?
            KmsMasterKeyProvider.builder().withDefaultRegion("ap-northeast-2").buildStrict(kmsKeyArn) :
            KmsMasterKeyProvider.builder()
                .withCustomClientFactory((region) ->
                    AWSKMSClientBuilder
                        .standard()
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(kmsEndpointUrl, region))
                        .withCredentials(new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials("accessKey", "secretKey"))) // 임의 값 넣어줌
                        .build()
                ).buildStrict(this.kmsKeyArn));

    // 3. Create a key material cache.
    CryptoMaterialsCache cache = new LocalCryptoMaterialsCache(100);

    // 4. Create a caching CMM.
    this.cachingCmm = CachingCryptoMaterialsManager
        .newBuilder()
        .withMasterKeyProvider(keyProvider)
        .withCache(cache)
        .withMaxAge(3600, TimeUnit.SECONDS)
        .build();
  }

  public String encrypt(String plainText) {

    byte[] plainBytes = plainText.getBytes(StandardCharsets.UTF_8);
    byte[] result = awsCrypto.encryptData(cachingCmm, plainBytes).getResult();
    String encryptText = Base64.encodeAsString(result);
    log.info("Encrypted Text: {}", encryptText);
    return encryptText;
  }

  public String decrypt(String encryptText) {

    byte[] encryptBytes = Base64.decode(encryptText);
    byte[] decryptedBytes = awsCrypto.decryptData(cachingCmm, encryptBytes).getResult();
    String decryptText = new String(decryptedBytes, StandardCharsets.UTF_8);
    log.info("Decrypted Text: {}", decryptText);
    return decryptText;
  }
}
