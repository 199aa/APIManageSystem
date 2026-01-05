package com.api.service;

import com.api.mapper.BlacklistWhitelistMapper;
import com.api.model.BlacklistWhitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlacklistWhitelistService {

    @Autowired
    private BlacklistWhitelistMapper blacklistWhitelistMapper;

    public List<String> getBlacklist() {
        List<BlacklistWhitelist> list = blacklistWhitelistMapper.selectByType("blacklist");
        return list.stream().map(BlacklistWhitelist::getIpAddress).collect(Collectors.toList());
    }

    public List<String> getWhitelist() {
        List<BlacklistWhitelist> list = blacklistWhitelistMapper.selectByType("whitelist");
        return list.stream().map(BlacklistWhitelist::getIpAddress).collect(Collectors.toList());
    }

    @Transactional
    public void saveBlacklist(List<String> ips) {
        // 删除旧的黑名单
        blacklistWhitelistMapper.deleteByType("blacklist");

        // 批量插入新的黑名单
        if (ips != null && !ips.isEmpty()) {
            List<BlacklistWhitelist> list = new ArrayList<>();
            for (String ip : ips) {
                if (ip != null && !ip.trim().isEmpty()) {
                    BlacklistWhitelist item = new BlacklistWhitelist();
                    item.setIpAddress(ip.trim());
                    item.setListType("blacklist");
                    list.add(item);
                }
            }
            if (!list.isEmpty()) {
                blacklistWhitelistMapper.batchInsert(list);
            }
        }
    }

    @Transactional
    public void saveWhitelist(List<String> ips) {
        // 删除旧的白名单
        blacklistWhitelistMapper.deleteByType("whitelist");

        // 批量插入新的白名单
        if (ips != null && !ips.isEmpty()) {
            List<BlacklistWhitelist> list = new ArrayList<>();
            for (String ip : ips) {
                if (ip != null && !ip.trim().isEmpty()) {
                    BlacklistWhitelist item = new BlacklistWhitelist();
                    item.setIpAddress(ip.trim());
                    item.setListType("whitelist");
                    list.add(item);
                }
            }
            if (!list.isEmpty()) {
                blacklistWhitelistMapper.batchInsert(list);
            }
        }
    }
}
