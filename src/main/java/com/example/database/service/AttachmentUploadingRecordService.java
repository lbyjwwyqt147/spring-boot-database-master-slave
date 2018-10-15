package com.example.database.service;


import com.example.database.entity.AttachmentUploadingRecord;

import java.util.List;

/***
 * 文件名称: AttachmentUploadingRecordService.java
 * 文件描述: 附件 service
 * 公 司: 积微物联
 * 内容摘要:
 * 其他说明:
 * 完成日期:2018年08月27日
 * 修改记录:
 * @version 1.0
 * @author ljy
 */
public interface AttachmentUploadingRecordService {

    /**
     * 新增纪录
     * @param record
     * @return
     */
    Long insert(AttachmentUploadingRecord record);

    /**
     * 新增纪录
     * @param record
     * @return
     */
    Long update(AttachmentUploadingRecord record);

    /**
     * 单条删除
     * @param id
     * @return
     */
    int deleteById(Long id);

    /**
     * 删除
     * @param id
     * @return
     */
    int delete(Long id) throws Exception;

    /**
     * 根据ID 获取数据
     * @param ids
     * @return
     */
    List<AttachmentUploadingRecord> findAllById(List<Long> ids);


    /**
     * 根据文件类型获取数据
     * @param attachmentType
     * @return
     */
    List<AttachmentUploadingRecord> findByAttachmentType(Byte attachmentType);
}
