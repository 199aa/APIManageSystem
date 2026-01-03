package com.api.service;

import com.api.mapper.RateLimitMapper;
import com.api.model.RateLimit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RateLimitService {

    @Autowired
    private RateLimitMapper rateLimitMapper;

    public List<RateLimit> getAll() {
        return rateLimitMapper.selectAll();
    }

    public RateLimit getById(Long id) {
        return rateLimitMapper.selectById(id);
    }

    @Transactional
    public void save(RateLimit rateLimit) {
        if (rateLimit.getId() == null) {
            rateLimitMapper.insert(rateLimit);
        } else {
            rateLimitMapper.update(rateLimit);
        }
    }

    @Transactional
    public void delete(Long id) {
        rateLimitMapper.deleteById(id);
    }

    @Transactional
    public void updateStatus(Long id, Integer status) {
        rateLimitMapper.updateStatus(id, status);
    }
}
