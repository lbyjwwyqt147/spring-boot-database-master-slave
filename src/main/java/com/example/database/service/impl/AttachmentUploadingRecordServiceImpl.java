package com.example.database.service.impl;


import com.example.database.datasource.DataSourceType;
import com.example.database.datasource.TargetDataSource;
import com.example.database.entity.AttachmentUploadingRecord;
import com.example.database.repository.AttachmentUploadingRecordRepository;
import com.example.database.service.AttachmentUploadingRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    @Autowired
    private ApplicationContext applicationContext;

   // @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
   // @TargetDataSource(dataSource = DataSourceType.masterDataSource)
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
        log.info("新增数据返回id:" + fileId);
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

    @Transactional(rollbackFor = Exception.class)
    @TargetDataSource(dataSource = DataSourceType.SLAVE)
    @Override
    public int delete(Long id) throws Exception {
        try {
            attachmentUploadingRecordRepository.deleteById(id);
            AttachmentUploadingRecord record = new AttachmentUploadingRecord();
            Random random = new Random(1000);
            int number = random.nextInt();
            record.setAttachmentName("20180828170511301217614" + number + ".docx");
            record.setAttachmentPostfix(".docx");
            record.setAttachmentOriginalName("关于提报半年工作总结的通知-" + number + ".docx");
            //this.insert(record); // 内嵌方法 这种方式直接调用无法触发AOP 切换数据源

            //使用下面方式才能在内嵌方法中触发AOP 切换数据源
            AttachmentUploadingRecordService serviceTemp = applicationContext.getBean(AttachmentUploadingRecordServiceImpl.class);
            serviceTemp.insert(record); //正确的切换到 insert（）上配置的数据源


           // int i = 1 / 0;

            return  1;
        } catch (Exception e){
            e.printStackTrace();
            log.error("删除出现异常.");
            throw new Exception("抛异常了");

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
