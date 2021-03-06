package com.example.xb.domain.vo;

import com.example.xb.domain.product.Brand;
import com.example.xb.domain.file.FileRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BrandVo extends Brand {

    private List<FileRecord> fileRecordList;
}
