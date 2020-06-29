package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.mode.ItemMode;
import com.miaoshaproject.service.mode.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService{

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    private ItemDO convertItemDOfromItemModel(ItemMode itemMode){
        if (itemMode == null){
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemMode,itemDO);
        itemDO.setPrice(itemMode.getPrice().doubleValue());


        return itemDO;

    }

    private ItemStockDO convertItemStockDOFromItemModel(ItemMode itemMode){
        if (itemMode == null){
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemMode.getId());
        itemStockDO.setStock(itemMode.getStock());
        return itemStockDO;
    }

    @Override
    @Transactional
    public ItemMode createItem(ItemMode itemMode) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemMode);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrorMsg());
        }
        //转化itemModel ->dataobject
        ItemDO itemDO = this.convertItemDOfromItemModel(itemMode);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemMode.setId(itemDO.getId());

        ItemStockDO itemStockDO = this.convertItemStockDOFromItemModel(itemMode);

        itemStockDOMapper.insertSelective(itemStockDO);

        //返回创建完成的对象

        return this.getItemById(itemMode.getId());
    }

    @Override
    public List<ItemMode> listItem() {
        List<ItemDO> listDOItem= itemDOMapper.listItem();

        List<ItemMode> itemModeList= listDOItem.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemMode itemMode = this.convertModeFromDataObject(itemDO,itemStockDO);
            return itemMode;
        }).collect(Collectors.toList());
        return itemModeList;
    }

    @Override
    public ItemMode getItemById(Integer id) {
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null){
            return null;
        }
        //操作获得库存数量
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());



        //将dataobject->model
        ItemMode itemMode = convertModeFromDataObject(itemDO,itemStockDO);

        //获取活动商品信息
        PromoModel promoModel = promoService.getPromoByItemId(itemMode.getId());
        if (promoModel != null && promoModel.getStatus().intValue() != 3){
            itemMode.setPromoModel(promoModel);
        }

        return itemMode;
    }

    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) throws BusinessException {
        int affectedRow = itemStockDOMapper.decreaseStock(itemId,amount);
        if (affectedRow >0){
            //更新库存成功
            return true;
        }else {
            //更新库存失败
            return false;
        }

    }

    @Override
    @Transactional
    public void increaseSales(Integer itemId, Integer amount) throws BusinessException {
        itemDOMapper.increaseSales(itemId,amount);
    }

    private ItemMode convertModeFromDataObject(ItemDO itemDO,ItemStockDO itemStockDO){
        ItemMode itemMode = new ItemMode();
        BeanUtils.copyProperties(itemDO,itemMode);
        itemMode.setPrice(new BigDecimal(itemDO.getPrice()));
        itemMode.setStock(itemStockDO.getStock());
        return itemMode;
    }

}
