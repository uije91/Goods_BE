package com.unity.goods.global.service;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.unity.goods.infra.service.S3Service;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
class S3ServiceTest {

  @Autowired
  private S3Service s3Service;

  @Test
  public void imageUploadTest() {
    // given
    String originalFileName = "test.jpg";
    MultipartFile file = new MockMultipartFile("file", originalFileName, "image/jpeg",
        "test data".getBytes());

    // when
    String uploadedFileUrl = s3Service.uploadFile(file, originalFileName);

    try {
      String fileNameFromUrl = uploadedFileUrl.substring(uploadedFileUrl.lastIndexOf('/') + 1);
      // then
      assertEquals(originalFileName, fileNameFromUrl);
    } catch (Exception e) {
      fail("URL processing failed");
    }
  }

  @Test
  public void imageDeleteTest() {
    // given
    String fileHeader = "fortestseowon@gmail.com";
    MultipartFile file = new MockMultipartFile("file", "originalFileName",
        "image/jpeg", "test data".getBytes());

    // when
    String uploadedFileUrl = s3Service.uploadFile(file, fileHeader);
    s3Service.deleteFile(uploadedFileUrl);

  }
}