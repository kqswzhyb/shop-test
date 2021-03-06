package com.example.xb.controller;

import com.example.xb.domain.file.FileRecord;
import com.example.xb.domain.page.DataDomain;
import com.example.xb.domain.product.*;
import com.example.xb.domain.result.AjaxResult;
import com.example.xb.domain.result.ResultInfo;
import com.example.xb.domain.vo.ProductParameterVo;
import com.example.xb.domain.vo.ProductVo;
import com.example.xb.domain.vo.ProductgVo;
import com.example.xb.service.*;
import com.example.xb.utils.JwtUtil;
import com.example.xb.utils.UUIDUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;
import springfox.documentation.annotations.ApiIgnore;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1/product")
@Api(tags = "产品管理")
public class ProductController extends BaseController{
    @Autowired
    private AttrBaseService attrBaseService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductDesService productDesService;
    @Autowired
    private ProductParameterService productParameterService;
    @Autowired
    private FileRecordService fileRecordService;
    @Autowired
    private ProductgService productgService;
    @Autowired
    private ParameterService parameterService;
    @Autowired
    private ProductgStockService productgStockService;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/list")
    @ApiOperation(value = "分页获取产品信息", notes = "分页获取产品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", defaultValue = "10")
    }
    )
    public AjaxResult list(@ApiIgnore() Product product, String current, String pageSize) {
        DataDomain dd = new DataDomain(current, pageSize);
        ResultInfo resultInfo = startPage(dd);
        List<ProductVo> list = productService.productList(product);
        dd.setRecords(list);
        PageInfo pageInfo = new PageInfo(list);
        dd.setTotal(pageInfo.getTotal());

        return new AjaxResult(resultInfo, "1".equals(resultInfo.getCode()) ? null : dd);
    }

    /**
     * 创建新产品
     *
     * @return
     */
    @PostMapping("/save")
    @ApiOperation(value = "创建新产品基础", notes = "创建新产品基础")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult save(@RequestBody ProductVo productVo) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(productVo.getName())) {
            resultInfo.error("产品名为空");
            return new AjaxResult(resultInfo, null);
        }
        if (StringUtils.isEmptyOrWhitespace(productVo.getProductCode())) {
            resultInfo.error("产品编码为空");
            return new AjaxResult(resultInfo, null);
        }
        if (StringUtils.isEmptyOrWhitespace(productVo.getBrandId())) {
            resultInfo.error("产品品牌id为空");
            return new AjaxResult(resultInfo, null);
        }
        if (StringUtils.isEmptyOrWhitespace(productVo.getProductUnit())) {
            resultInfo.error("产品单位为空");
            return new AjaxResult(resultInfo, null);
        }
        String Uid= UUIDUtil.NewUUID();

        List<FileRecord> fileList = productVo.getFileRecordList();
        int j = 1;
        if(fileList.size()!=0) {
            for(FileRecord child:fileList) {
                child.setFileId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setRecordId(Uid);
            }
            j = fileRecordService.bathSaveFile(fileList);
        }

        List<ProductDes> desList = productVo.getProductDesList();
        int k = 1;
        if(desList.size()!=0) {
            for(ProductDes child:desList) {
                child.setDesId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setProductId(Uid);
            }
            k = productDesService.batchSaveProductDes(desList);
        }

        List<ProductParameterVo> parameterList = productVo.getProductParameterVoList();
        List<ProductParameter> ppList= new ArrayList<>();
        int l = 1;
        if(parameterList.size()!=0) {
            for(ProductParameterVo child:parameterList) {
                ProductParameter productParameter = new ProductParameter();
                productParameter.setContent(child.getContent());
                productParameter.setProductId(Uid);
                productParameter.setParameterId(child.getParameterId());
                ppList.add(productParameter);
            }
            l = productParameterService.batchSaveProductParameter(ppList);
        }

        List<Productg> productgList = productVo.getProductgList();
        int n = 1;
        if(productgList.size()!=0) {
            for(Productg child:productgList) {
                child.setProductgId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setProductId(Uid);
            }
            n = productgService.batchSaveProductg(productgList);
        }

        productVo.setProductId(Uid);
        productVo.setCreateBy(jwtUtil.getJwtUserId());
        productVo.setStatus("0");
        productVo.setSaleStatus("0");

        List<AttrBase> attrBaseList = productVo.getAttrBaseList();
        int m = 1;
        if(attrBaseList.size()!=0) {
            for(AttrBase child:attrBaseList) {
                child.setAttrId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setProductId(Uid);
            }
            m = attrBaseService.batchSaveAttrBase(attrBaseList);
        }

        int i = productService.saveProduct(productVo);
        if (i == 1&& j!=0 && k!=0&&l!=0&&m!=0&&n!=0) {
            resultInfo.success("创建成功");
        } else {
            resultInfo.error("创建失败");
        }
        return new AjaxResult(resultInfo, null);
    }

    /**
     * 更新产品
     *
     * @return
     */
    @PutMapping("/update")
    @ApiOperation(value = "更新产品基础", notes = "更新产品基础")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult update(@RequestBody ProductVo productVo) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(productVo.getName())) {
            resultInfo.error("产品名为空");
            return new AjaxResult(resultInfo, null);
        }
        if (StringUtils.isEmptyOrWhitespace(productVo.getProductCode())) {
            resultInfo.error("产品编码为空");
            return new AjaxResult(resultInfo, null);
        }
        if (StringUtils.isEmptyOrWhitespace(productVo.getBrandId())) {
            resultInfo.error("产品品牌id为空");
            return new AjaxResult(resultInfo, null);
        }
        if (StringUtils.isEmptyOrWhitespace(productVo.getProductUnit())) {
            resultInfo.error("产品单位为空");
            return new AjaxResult(resultInfo, null);
        }
        String Uid= productVo.getProductId();

        fileRecordService.deleteFileById(Uid);
        productDesService.deleteProductDesById(Uid);
        productParameterService.deleteProductParameterById(Uid);
        productgService.deleteProductgById(Uid);
        attrBaseService.deleteAttrBaseById(Uid);

        List<FileRecord> fileList = productVo.getFileRecordList();
        int j = 1;
        if(fileList.size()!=0) {
            for(FileRecord child:fileList) {
                child.setFileId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setRecordId(Uid);
            }
            j = fileRecordService.bathSaveFile(fileList);
        }

        List<ProductDes> desList = productVo.getProductDesList();
        int k = 1;
        if(desList.size()!=0) {
            for(ProductDes child:desList) {
                child.setDesId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setProductId(Uid);
            }
            k = productDesService.batchSaveProductDes(desList);
        }

        List<ProductParameterVo> parameterList = productVo.getProductParameterVoList();
        List<ProductParameter> ppList= new ArrayList<>();
        int l = 1;
        if(parameterList.size()!=0) {
            for(ProductParameterVo child:parameterList) {
                ProductParameter productParameter = new ProductParameter();
                productParameter.setContent(child.getContent());
                productParameter.setProductId(Uid);
                productParameter.setParameterId(child.getParameterId());
                ppList.add(productParameter);
            }
            l = productParameterService.batchSaveProductParameter(ppList);
        }

        List<Productg> productgList = productVo.getProductgList();
        int n = 1;
        if(productgList.size()!=0) {
            for(Productg child:productgList) {
                child.setProductgId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setProductId(Uid);
            }
            n = productgService.batchSaveProductg(productgList);
        }

        productVo.setProductId(Uid);
        productVo.setCreateBy(jwtUtil.getJwtUserId());
        productVo.setStatus("0");
        productVo.setSaleStatus("0");

        List<AttrBase> attrBaseList = productVo.getAttrBaseList();
        int m = 1;
        if(attrBaseList.size()!=0) {
            for(AttrBase child:attrBaseList) {
                child.setAttrId(UUIDUtil.NewUUID());
                child.setCreateBy(jwtUtil.getJwtUserId());
                child.setStatus("0");
                child.setProductId(Uid);
            }
            m = attrBaseService.batchSaveAttrBase(attrBaseList);
        }

        int i = productService.updateProduct(productVo);
        if (i == 1&& j!=0 && k!=0&&l!=0&&m!=0&&n!=0) {
            resultInfo.success("更新成功");
        } else {
            resultInfo.error("更新失败");
        }
        return new AjaxResult(resultInfo, null);
    }

    /**
     * 根据id删除产品
     *
     * @return
     */
    @DeleteMapping("/delete")
    @ApiOperation(value = "根据id删除产品", notes = "根据id删除产品")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult delete(String productId) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(productId)) {
            resultInfo.error("productId不能为空");
            return new AjaxResult(resultInfo, null);
        }
        int i = productService.deleteProductById(productId);
        fileRecordService.deleteFileById(productId);
        productDesService.deleteProductDesById(productId);
        productParameterService.deleteProductParameterById(productId);
        productgService.deleteProductgById(productId);
        attrBaseService.deleteAttrBaseById(productId);
        if (i == 1) {
            resultInfo.success("删除成功");
        } else {
            resultInfo.error("该productId不存在，无法删除");
        }
        return new AjaxResult(resultInfo, null);
    }

    /**
     * 获取产品参数列表
     *
     * @return
     */
    @GetMapping("/parameterList")
    @ApiOperation(value = "获取产品参数列表", notes = "获取产品参数列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", defaultValue = "10")
    }
    )
    public AjaxResult parameterList(@ApiIgnore() Parameter parameter, String current, String pageSize) {
        DataDomain dd = new DataDomain(current, pageSize);
        ResultInfo resultInfo = startPage(dd);
        List<Parameter> list = parameterService.parameterList(parameter);
        dd.setRecords(list);
        PageInfo pageInfo = new PageInfo(list);
        dd.setTotal(pageInfo.getTotal());

        return new AjaxResult(resultInfo, "1".equals(resultInfo.getCode()) ? null : dd);
    }

    /**
     * 创建新参数
     *
     * @return
     */
    @PostMapping("/saveParameter")
    @ApiOperation(value = "创建新参数", notes = "创建新参数")
    public AjaxResult saveParameter(@RequestBody Parameter parameter) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(parameter.getName())) {
            resultInfo.error("参数名为空");
            return new AjaxResult(resultInfo, null);
        }
        parameter.setParameterId(UUIDUtil.NewUUID());
        parameter.setCreateBy(jwtUtil.getJwtUserId());
        parameter.setStatus("0");
        int i = parameterService.saveParameter(parameter);
        if (i == 1) {
            resultInfo.success("创建成功");
        } else {
            resultInfo.error("创建失败");
        }
        return new AjaxResult(resultInfo, null);
    }

    /**
     * 更新参数
     *
     * @return
     */
    @PutMapping("/updateParameter")
    @ApiOperation(value = "更新参数", notes = "更新参数")
    public AjaxResult updateParameter(@RequestBody Parameter parameter) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(parameter.getParameterId())) {
            resultInfo.error("parameterId为空");
            return new AjaxResult(resultInfo, null);
        }
        int i = parameterService.updateParameter(parameter);
        if (i == 1) {
            resultInfo.success("更新成功");
        } else {
            resultInfo.error("更新失败");
        }
        return new AjaxResult(resultInfo, null);
    }

    /**
     * 根据id删除参数
     *
     * @return
     */
    @DeleteMapping("/deleteParameter")
    @ApiOperation(value = "根据id删除参数", notes = "根据id删除参数")
    public AjaxResult deleteParameter(String parameterId) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(parameterId)) {
            resultInfo.error("parameterId不能为空");
            return new AjaxResult(resultInfo, null);
        }
        int i = parameterService.deleteParameterById(parameterId);
        if (i == 1) {
            resultInfo.success("删除成功");
        } else {
            resultInfo.error("该roleId不存在，无法删除");
        }
        return new AjaxResult(resultInfo, null);
    }

    /**
     * 获取产品组员列表
     *
     * @return
     */
    @GetMapping("/productgList")
    @ApiOperation(value = "获取产品组员列表", notes = "获取产品组员列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页", defaultValue = "1"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", defaultValue = "10")
    }
    )
    public AjaxResult productgList( String productId, String current, String pageSize) {
        DataDomain dd = new DataDomain(current, pageSize);
        ResultInfo resultInfo = startPage(dd);
        List<ProductgVo> list = productgService.productgListAll(productId);
        dd.setRecords(list);
        PageInfo pageInfo = new PageInfo(list);
        dd.setTotal(pageInfo.getTotal());

        return new AjaxResult(resultInfo, "1".equals(resultInfo.getCode()) ? null : dd);
    }

    /**
     * 更新产品库存
     *
     * @return
     */
    @PutMapping("/updateProductStock")
    @ApiOperation(value = "更新产品库存", notes = "更新产品库存")
    public AjaxResult updateProductg(@RequestBody ProductgStock productgStock) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(productgStock.getProductgId())) {
            resultInfo.error("ProductgId为空");
            return new AjaxResult(resultInfo, null);
        }
        int i = productgStockService.updateStock(productgStock);
        if (i == 1) {
            resultInfo.success("更新成功");
        } else {
            resultInfo.error("更新失败");
        }
        return new AjaxResult(resultInfo, null);
    }

    /**
     * 更新产品上架状态
     *
     * @return
     */
    @PutMapping("/updateSaleStatus")
    @ApiOperation(value = "更新产品上架状态", notes = "更新产品上架状态")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult update(@RequestBody Product product) {
        ResultInfo resultInfo = new ResultInfo();
        if (StringUtils.isEmptyOrWhitespace(product.getProductId())) {
            resultInfo.error("产品id为空");
            return new AjaxResult(resultInfo, null);
        }

        int i = productService.updateProduct(product);
        if (i == 1) {
            resultInfo.success("更新成功");
        } else {
            resultInfo.error("更新失败");
        }
        return new AjaxResult(resultInfo, null);
    }
}
