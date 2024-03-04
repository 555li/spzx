package com.atguigu.spzx.manager.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.spzx.common.exception.GuiguException;
import com.atguigu.spzx.manager.listener.ExcelListener;
import com.atguigu.spzx.manager.mapper.CategoryMapper;
import com.atguigu.spzx.manager.service.CategoryService;
import com.atguigu.spzx.model.entity.product.Category;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.atguigu.spzx.model.vo.product.CategoryExcelVo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    //导入
    @Override
    public void importData(MultipartFile file)  {
        //TODO 监听器
        ExcelListener<CategoryExcelVo> excelListener=new ExcelListener(categoryMapper);
        try {
            EasyExcel.read(file.getInputStream(), CategoryExcelVo.class, excelListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }
    }

    //导出
    @Override
    public void exportData(HttpServletResponse response) {
        try {
            //1.设置响应头信息和其他信息
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            //这里URLEncoder.encode可以防止中文乱码，当然和easyexcel没有关系
            String fileName= URLEncoder.encode("分类数据", "UTF-8");
            //设置响应头信息
            response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");
            //2.调用mapper方法查询所有分类，返回List集合
            List<Category> categoryList=categoryMapper.findAll();
            List<CategoryExcelVo> categoryExcelVoList=new ArrayList<>();
            for (Category category:categoryList) {
                CategoryExcelVo categoryExcelVo=new CategoryExcelVo();
                BeanUtils.copyProperties(category,categoryExcelVo);
                categoryExcelVoList.add(categoryExcelVo);
            }
            //3.调用EasyExcel的write方法完成写操作
            EasyExcel.write(response.getOutputStream(), CategoryExcelVo.class).sheet("分类数据").doWrite(categoryExcelVoList);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GuiguException(ResultCodeEnum.DATA_ERROR);
        }


    }

    //分类列表，每次查询一层数据
    @Override
    public List<Category> findCategoryList(Long id) {
        //1 根据id条件值进行查询，返回List集合
        //Select * from category where parent_id=id
        List<Category> categoryList=categoryMapper.selectCategoryByParentId(id);
        System.out.println("categoryList="+categoryList);
        //2 遍历返回List集合
        //判断每个分类是否有下一层分类，如果有设置hasChildren=true
        if(!CollectionUtils.isEmpty(categoryList)){
            categoryList.forEach(category -> {
                //判断每个分类是否有下一层分类
                //Select count(*) from category Where parent_id=id
                int count=categoryMapper.selectCountByParentId(category.getId());
                if(count>0){
                    category.setHasChildren(true);
                }
                else category.setHasChildren(false);
            });
        }
        return categoryList;
    }
}
