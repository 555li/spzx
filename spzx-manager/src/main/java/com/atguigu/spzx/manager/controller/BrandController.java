package com.atguigu.spzx.manager.controller;

import com.atguigu.spzx.model.entity.product.Brand;
import com.atguigu.spzx.model.vo.common.Result;
import com.atguigu.spzx.model.vo.common.ResultCodeEnum;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.atguigu.spzx.manager.service.BrandService;

import java.util.List;

@RestController
@RequestMapping("admin/product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;
    //查询所有品牌
    @GetMapping("findAll")
    public Result findAll(){
        List<Brand> brands=brandService.findAll();
        return Result.build(brands,ResultCodeEnum.SUCCESS);
    }

    //列表
    @GetMapping("/{page}/{limit}")
    public Result list(@PathVariable("page") Integer page, @PathVariable("limit") Integer limit){
        PageInfo<Brand> pageInfo=brandService.findByPage(page,limit);
        return Result.build(pageInfo, ResultCodeEnum.SUCCESS);
    }

    //添加
    @PostMapping("/save")
    public Result save(@RequestBody Brand brand){
        brandService.save(brand);
        return Result.build(null,ResultCodeEnum.SUCCESS);
    }
    //修改
    @PutMapping("/updateById")
    public Result updateById(@RequestBody Brand brand){
        brandService.updateById(brand);
        return Result.build(null,ResultCodeEnum.SUCCESS);
    }
    //删除
    @DeleteMapping("/deleteById/{id}")
    public Result deleteById(@PathVariable("id") Long id){

        brandService.deleteById(id);
        return Result.build(null,ResultCodeEnum.SUCCESS);
    }
}
