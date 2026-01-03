package com.api.util;

import java.security.MessageDigest;

public class MD5Util {

  public static String encrypt(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      md.update(text.getBytes());
      byte[] bytes = md.digest();

      StringBuilder sb = new StringBuilder();
      for (byte b : bytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException("MD5加密失败", e);
    }
  }
}
