package com.atguigu.spzx.manager.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import com.atguigu.spzx.manager.mapper.CategoryMapper;
import com.atguigu.spzx.model.vo.product.CategoryExcelVo;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

//监听器
public class ExcelListener<T>  implements ReadListener<T> {
    private static final int BATCH_COUNT = 100;
    //缓存的数据
    private List<T> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    //构造传递mapper,操作数据库
    private CategoryMapper categoryMapper;
    public ExcelListener(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }
    //从第二行开始读取，把每行读取内容封装到t对象里面
    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        //把每行数据对象t放到cacheDataList集合中去
        cachedDataList.add(t);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            //清空缓存
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }


    }

    //保存的方法
    private void saveData() {
        categoryMapper.batchInsert((List<CategoryExcelVo>)cachedDataList);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        //保存数据
        saveData();
    }
}
