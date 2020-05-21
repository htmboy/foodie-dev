package com.imooc.pojo.vo;

import java.util.Date;

/**
 * 用于展示商品搜索列表结果的VO
 */
public class SearchItemsVO {

    private String itemId;
    private String itemName;
    private int sellCounts;
    private String imgUrl;
    private int price; // 涉及到金额的场景下，使用分为单位。用元为单位会有小数点，会有四舍五入

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getSellCounts() {
        return sellCounts;
    }

    public void setSellCounts(int sellCounts) {
        this.sellCounts = sellCounts;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
