package com.imooc.pojo.vo;

import java.util.List;

/**
 * 二级分类VO
 * view
 * PO：persistent object 持久对象
 * POJO ：plain ordinary java object 无规则简单java对象
 * BO：business object 业务对象
 * VO：value object 值对象 / view object 表现层对象
 */
public class CategoryVO {
    /**
     * f.id as id,
     * f.name as name,
     * f.type as type,
     * f.father_id as fatherId,
     */
    private Integer id;
    private String name;
    private String type;
    private Integer fatherId;

    // 三级分类vo List
    private List<SubCategoryVO> subCatList;

    public List<SubCategoryVO> getSubCatList() {
        return subCatList;
    }

    public void setSubCatList(List<SubCategoryVO> subCatList) {
        this.subCatList = subCatList;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getFatherId() {
        return fatherId;
    }

    public void setFatherId(Integer fatherId) {
        this.fatherId = fatherId;
    }
}
