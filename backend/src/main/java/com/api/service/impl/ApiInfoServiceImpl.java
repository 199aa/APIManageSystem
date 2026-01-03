package com.api.service.impl;

import com.api.mapper.ApiInfoMapper;
import com.api.model.ApiInfo;
import com.api.service.ApiInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ApiInfoServiceImpl implements ApiInfoService {

  @Autowired
  private ApiInfoMapper apiInfoMapper;

  @Override
  public List<ApiInfo> getApiList(String name, String method) {
    return apiInfoMapper.selectList(name, method);
  }

  @Override
  public ApiInfo getApiById(Long id) {
    return apiInfoMapper.selectById(id);
  }

  @Override
  public ApiInfo createApi(ApiInfo apiInfo) {
    apiInfo.setStatus(1);
    apiInfoMapper.insert(apiInfo);
    return apiInfo;
  }

  @Override
  public ApiInfo updateApi(Long id, ApiInfo apiInfo) {
    ApiInfo existApi = apiInfoMapper.selectById(id);
    if (existApi == null) {
      throw new RuntimeException("API不存在");
    }

    apiInfo.setId(id);
    apiInfoMapper.update(apiInfo);
    return apiInfoMapper.selectById(id);
  }

  @Override
  public boolean deleteApi(Long id) {
    return apiInfoMapper.deleteById(id) > 0;
  }
}
