package com.example.database.controller;


import com.example.database.entity.AttachmentUploadingRecord;
import com.example.database.service.AttachmentUploadingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;


/***
 * 文件名称: UploadingRecordController.java
 * 文件描述: 文件管理 Controller
 * 公 司: 积微物联
 * 内容摘要:
 * 其他说明:
 * 完成日期:2018年08月28日
 * 修改记录:
 * @version 1.0
 * @author ljy
 */
@RestController
public class UploadingRecordController  {
    @Autowired
    private AttachmentUploadingRecordService attachmentUploadingRecordService;

    /**
     * 保存
     * @return
     */
    @PostMapping(value = "/save")
    public Long save(){
        AttachmentUploadingRecord record = new AttachmentUploadingRecord();
        Random random = new Random(1000);
        int number = random.nextInt();
        record.setAttachmentName("2018082817051130121735755466614" + number + ".docx");
        record.setAttachmentPostfix(".docx");
        record.setAttachmentOriginalName("关于提报半年工作总结的通知-" + number + ".docx");
        return attachmentUploadingRecordService.insert(record);
    }

    /**
     * 保存
     * @return
     */
    @PostMapping(value = "/update")
    public Long update(){
        AttachmentUploadingRecord record = new AttachmentUploadingRecord();
        Random random = new Random(1000);
        int number = random.nextInt();
        record.setAttachmentName("2018082817051130121735755466614" + number + ".docx");
        record.setAttachmentPostfix(".docx");
        record.setAttachmentOriginalName("关于提报半年工作总结的通知-" + number + ".docx");
        return attachmentUploadingRecordService.update(record);
    }


    /**
     *
     * @return
     */
    @GetMapping(value = "/attachmentType")
    public List<AttachmentUploadingRecord> findByAttachmentType(){
        return attachmentUploadingRecordService.findByAttachmentType(null);
    }

    /**
     * 单条删除数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/delete/{id}")
    public int singleDelete(@PathVariable(value = "id") Long id) {
        return attachmentUploadingRecordService.deleteById(id);

    }

    /**
     * 单条删除数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/deletes/{id}")
    public int delete(@PathVariable(value = "id") Long id) {
        return attachmentUploadingRecordService.delete(id);

    }


    /**
     * 根据　一组　id　获取信息
     * @param id  多个id 用 , 隔开
     * @return
     */

    @GetMapping(value = "/details/{id}")
    public List<AttachmentUploadingRecord> findAllById(@PathVariable(value = "id") String id){
       String[] idsArray =  id.split(",");
        List<Long> ids =  new LinkedList<>();
        for (String string : idsArray) {
            ids.add(Long.valueOf(string));
        }
        return attachmentUploadingRecordService.findAllById(ids);
    }


}
