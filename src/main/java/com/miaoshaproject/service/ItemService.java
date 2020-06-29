package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.mode.ItemMode;

import java.util.List;

public interface ItemService {

    //创建商品
    ItemMode createItem(ItemMode itemMode) throws BusinessException;

    //商品列表浏览
    List<ItemMode> listItem();

    //商品详情浏览
    ItemMode getItemById(Integer id);

    //库存扣减
    boolean decreaseStock(Integer itemId,Integer amount) throws BusinessException;

    //商品销量增加
    void increaseSales(Integer itemId,Integer amount) throws BusinessException;

}
