package com.example.database.service.impl;


import com.example.database.datasource.DataSourceType;
import com.example.database.datasource.TargetDataSource;
import com.example.database.entity.AttachmentUploadingRecord;
import com.example.database.repository.AttachmentUploadingRecordRepository;
import com.example.database.service.AttachmentUploadingRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/***
 * 文件名称: AttachmentUploadingRecordServiceImpl.java
 * 文件描述: 附件 service 实现
 * 公 司: 积微物联
 * 内容摘要:
 * 其他说明:
 * 完成日期:2018年08月27日
 * 修改记录:
 * @version 1.0
 * @author ljy
 */
@Slf4j
@Service
public class AttachmentUploadingRecordServiceImpl implements AttachmentUploadingRecordService {

    @Autowired
    private AttachmentUploadingRecordRepository attachmentUploadingRecordRepository;

    @Transactional
    @Override
    public Long insert(AttachmentUploadingRecord record) {
        Long fileId = 0L;
        try {
            AttachmentUploadingRecord save = attachmentUploadingRecordRepository.save(record);
            fileId = save.getId();
        } catch (Exception e) {
            log.error("上传文件数据入库出现异常.");
            e.printStackTrace();
        }
        return fileId;

    }

    @Transactional
    @TargetDataSource(dataSource = DataSourceType.SLAVE)
    @Override
    public Long update(AttachmentUploadingRecord record) {
        Long fileId = 0L;
        try {
            AttachmentUploadingRecord save = attachmentUploadingRecordRepository.save(record);
            fileId = save.getId();
        } catch (Exception e) {
            log.error("上传文件数据入库出现异常.");
            e.printStackTrace();
        }
        return fileId;
    }


    @Transactional
    @Override
    public int deleteById(Long id) {
        List<Long> ids = new LinkedList<>();
        ids.add(id);
        List<AttachmentUploadingRecord> records = this.findAllById(ids);
        int result =  attachmentUploadingRecordRepository.deleteAllById(id);
        return result;
    }

    //@Transactional(rollbackFor = Exception.class)
    @TargetDataSource(dataSource = DataSourceType.SLAVE)
    @Override
    public int delete(Long id) {
        try {
             attachmentUploadingRecordRepository.deleteById(id);
             this.findByAttachmentType((byte) 1);
            AttachmentUploadingRecord record = new AttachmentUploadingRecord();
            Random random = new Random(1000);
            int number = random.nextInt();
            record.setAttachmentName("20180828170511301217357554666142018082817051130121735755466614201808281705113012173575546661420180828170511301217357554666142018082817051130121735755466614201808281705113012173575546661420180828170511301217357554666142018082817051130121735755466614201808281705113012173575546661420180828170511301217357554666142018082817051130121735755466614201808281705113012173575546661420180828170511301217357554666142018082817051130121735755466614201808281705113012173575546661420180828170511301217357554666142018082817051130121735755466614201808281705113012173575546661420180828170511301217357554666142018082817051130121735755466614201808281705113012173575546661420180828170511301217357554666142018082817051130121735755466614" + number + ".docx");
            record.setAttachmentPostfix(".docx");
            record.setAttachmentOriginalName("关于提报半年工作总结的通知-" + number + ".docx");
            this.update(record);
            return  1;
        } catch (Exception e){
            e.printStackTrace();
            log.error("删除出现异常.");
            return 0;
        }
    }

    @TargetDataSource(dataSource = DataSourceType.SLAVE)
    @Override
    public List<AttachmentUploadingRecord> findAllById(List<Long> ids) {
        return attachmentUploadingRecordRepository.findAllById(ids);
    }

    @Override
    public List<AttachmentUploadingRecord> findByAttachmentType(Byte attachmentType) {
        return attachmentUploadingRecordRepository.findByAttachmentType((byte)1);
    }


}
