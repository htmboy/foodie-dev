package com.imooc.service.impl;

import com.imooc.service.ItemsESService;
import com.imooc.utils.PagedGridResult;
import org.springframework.stereotype.Service;

@Service
public class ItemsESServiceImpl implements ItemsESService {
    @Override
    public PagedGridResult searchItems(String keywords, String sort, Integer page, Integer pageSize) {
        return null;
    }
}
