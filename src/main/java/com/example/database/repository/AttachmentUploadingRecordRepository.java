package com.example.database.repository;


import com.example.database.datasource.DataSourceType;
import com.example.database.datasource.TargetDataSource;
import com.example.database.entity.AttachmentUploadingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/***
 * 文件名称: AttachmentUploadingRecordRepository.java
 * 文件描述: 附件  Repository
 * 公 司: 积微物联
 * 内容摘要:
 * 其他说明:
 * 完成日期:2018年08月27日
 * 修改记录:
 * @version 1.0
 * @author ljy
 */
public interface AttachmentUploadingRecordRepository extends JpaRepository<AttachmentUploadingRecord, Long>, JpaSpecificationExecutor<AttachmentUploadingRecord> {

    /**
     * 批量删除
     * @param ids
     * @return
     */
    int deleteByIdIn(List<Long> ids);

    /**
     * 单条删除
     * @param id
     * @return
     */
    int deleteAllById(Long id);

    /**
     *
     * @param attachmentType
     * @return
     */
    //使用指定数据源
    @TargetDataSource(dataSource = DataSourceType.MASTER)
    List<AttachmentUploadingRecord> findByAttachmentType(Byte attachmentType);


}
