package com.unity.goods.global.service;

import static com.unity.goods.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.unity.goods.global.exception.CustomException;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

  @Value("${cloud.aws.s3.bucket}")
  private String bucket;

  @Value("${cloud.aws.s3.baseUrl}")
  private String BASE_URL;

  private final AmazonS3 amazonS3;

  public String uploadFile(MultipartFile multipartFile, String fileHeader) {

    String fileName = createFileName(fileHeader, multipartFile);

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(multipartFile.getSize());
    metadata.setContentType(multipartFile.getContentType());

    try{
      amazonS3.putObject(
          new PutObjectRequest(bucket, fileName, multipartFile.getInputStream(), metadata));
      log.info("[uploadFile] : aws S3 파일 업로드 완료");
    } catch (IOException e){
      throw new CustomException(INTERNAL_SERVER_ERROR);
    }
    return URLDecoder.decode(amazonS3.getUrl(bucket, fileName).toString(), StandardCharsets.UTF_8);
  }

  private String createFileName(String fileHeader, MultipartFile multipartFile) {
    return fileHeader + "/" + multipartFile.getOriginalFilename();
  }

  public void deleteFile(String uploadedFileName) {
    // 저장된 url : BASE_URL + email + 원본 이름
    // 삭제 이름은 email + 원본이어야함
    String key = uploadedFileName.replace(BASE_URL, "");

    try {
      amazonS3.deleteObject(bucket, key);
      log.info("[deleteFile] : aws S3 파일 삭제 완료");
    } catch (AmazonServiceException e){
      throw new AmazonServiceException(e.getErrorMessage());
    }
  }
}
