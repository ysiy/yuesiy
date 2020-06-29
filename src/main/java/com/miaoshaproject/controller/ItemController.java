package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewobject.ItemVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.mode.ItemMode;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/item")
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*")
public class ItemController extends BaseController{

    @Autowired
    private ItemService itemService;

    //创建商品的controller
    @RequestMapping(value="/create",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title") String title,
                                       @RequestParam(name = "description") String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock") Integer stock,
                                       @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {

        //封装service请求用来创建商品
        ItemMode itemMode = new ItemMode();
        itemMode.setTitle(title);
        itemMode.setDescription(description);
        itemMode.setPrice(price);
        itemMode.setStock(stock);
        itemMode.setImgUrl(imgUrl);

        ItemMode itemModeForReturn = itemService.createItem(itemMode);
        ItemVO itemVO = convertVOfromModel(itemModeForReturn);

        return CommonReturnType.create(itemVO);
    }


    //商品详情页浏览
    @RequestMapping(value="/get",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id){
        ItemMode itemMode = itemService.getItemById(id);
        ItemVO itemVO = convertVOfromModel(itemMode);
        return CommonReturnType.create(itemVO);
    }

    private ItemVO convertVOfromModel(ItemMode itemMode){
        if (itemMode == null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemMode,itemVO);
        if (itemMode.getPromoModel() != null){
            //有正在进行或即将进行的秒杀活动
            itemVO.setPromoStatus(itemMode.getPromoModel().getStatus());
            itemVO.setPromoId(itemMode.getPromoModel().getId());
            itemVO.setStartDate(itemMode.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
            itemVO.setPromoPrice(itemMode.getPromoModel().getPromoItemPrice());
        }else {
            itemVO.setPromoStatus(0);

        }
        return itemVO;
    }


    //商品列表页面浏览
    @RequestMapping(value="/list",method = {RequestMethod.GET})
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemMode> itemModeList = itemService.listItem();

        //使用stream api 将list内的itemMode转化为ITEMVO
        List<ItemVO> itemVOList = itemModeList.stream().map(itemMode -> {
            ItemVO itemVO = this.convertVOfromModel(itemMode);
            return itemVO;
        }).collect(Collectors.toList());
        return CommonReturnType.create(itemVOList);
    }
}
