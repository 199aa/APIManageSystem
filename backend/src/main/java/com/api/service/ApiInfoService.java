package com.api.service;

import com.api.model.ApiInfo;
import java.util.List;

public interface ApiInfoService {

  List<ApiInfo> getApiList(String name, String method);

  ApiInfo getApiById(Long id);

  ApiInfo createApi(ApiInfo apiInfo);

  ApiInfo updateApi(Long id, ApiInfo apiInfo);

  boolean deleteApi(Long id);
}
