package com.example.blogapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blogapi.dao.mapper.DocumentMapper;
import com.example.blogapi.dao.pojo.Document;
import com.example.blogapi.service.DocumentService;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.DocDownLoadVo;
import com.example.blogapi.vo.DocListVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.UserVo;
import com.example.blogapi.vo.params.DocUploadParam;
import com.example.blogapi.vo.params.PageParams;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class DocumentServiceImpl implements DocumentService {
    public static final  String FIFLE_PREFIX = "D:\\demo\\blog\\blog-api\\uploadfile\\";
    @Resource
    private DocumentMapper documentMapper;
    @Resource
    private SysUserService sysUserService;

    @Override
    public Result getAllDocuments(PageParams params) {
        Page<Document> page = new Page<>(params.getPage(), params.getPageSize());
        LambdaQueryWrapper wrapper = new LambdaQueryWrapper<>();
        Page selectPage = documentMapper.selectPage(page, wrapper);
        Integer count = documentMapper.selectCount(wrapper);
        DocListVo docListVo = new DocListVo(selectPage.getRecords(), params.getPage(), params.getPageSize(), count);
        List<Long> idList = docListVo.getDocuments().stream().map(Document::getPublisherId).distinct().collect(Collectors.toList());
        List<UserVo> userVoByIds = sysUserService.findUserVoByIds(idList);
        docListVo.getDocuments().stream().forEach(document -> {
            for (UserVo userVo : userVoByIds) {
                if (userVo.getId().equals(String.valueOf(document.getPublisherId()))) {
                    document.setPublisher(userVo.getNickname());
                    return;
                }
            }
        });

        return Result.success(docListVo);
    }

    @Override
    public Result uploadDocument(DocUploadParam uploadParam) {
        MultipartFile multipartFile = uploadParam.getMultipartFile();
        String filename = UUID.randomUUID().toString() + multipartFile.getOriginalFilename();
        File targetFile=new File(FIFLE_PREFIX+filename);
        try {
            multipartFile.transferTo(targetFile);
            Document doc = Document.builder().docUri(filename).ownerType(uploadParam.getOwnerType())
                    .publisherId(UserThreadLocal.get().getId())
                    .updateTime(System.currentTimeMillis()).docTitle(multipartFile.getOriginalFilename()).build();
            int insert = documentMapper.insert(doc);
            return  Result.success(insert);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Result downloadDocument(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if(document == null){
            return Result.fail(20001, "Document not found");
        }
        if(document.getOwnerType() == 1&&
                !document.getPublisherId().equals(UserThreadLocal.get().getId()) ) {
            return Result.fail(20001, "Document not found");
        }
        File f = new File(FIFLE_PREFIX+document.getDocUri());
        ByteBuffer byteBuffer = null;
        try(FileInputStream fs = new FileInputStream(f); FileChannel channel = fs.getChannel()){
            byteBuffer = ByteBuffer.allocate((int)channel.size());
            while((channel.read(byteBuffer)) > 0);
        } catch (IOException e ) {
            throw new RuntimeException(e);
        }
        DocDownLoadVo docDownLoadVo = new DocDownLoadVo();
        docDownLoadVo.setDocTitle(document.getDocTitle());
        docDownLoadVo.setContent(byteBuffer.array());
        return Result.success(docDownLoadVo);
    }

    @Override
    public Result updateDocument(DocUploadParam uploadParam) {
        Document oldDoc = documentMapper.selectById(uploadParam.getDocId());
        if(Objects.isNull(oldDoc) ||oldDoc.getOwnerType()==1&&!oldDoc.getPublisherId().equals(UserThreadLocal.get().getId())){
            return Result.fail(20001,"????????????");
        }
        MultipartFile multipartFile = uploadParam.getMultipartFile();
        // ??????????????????????????????????????????
        Document.DocumentBuilder documentBuilder = Document.builder().id(String.valueOf(uploadParam.getDocId()))
                .ownerType(uploadParam.getOwnerType())
                .publisherId(UserThreadLocal.get().getId())
                .updateTime(System.currentTimeMillis()).docTitle(multipartFile.getOriginalFilename());

        try {
            if(multipartFile!=null){
                String filename = UUID.randomUUID().toString() + multipartFile.getOriginalFilename();
                File targetFile=new File(FIFLE_PREFIX+filename);
                multipartFile.transferTo(targetFile);
                File oldDocFile = new File(FIFLE_PREFIX+oldDoc.getDocUri());
                oldDocFile.delete();
                documentBuilder.docUri(filename);
            }
            int insert = documentMapper.updateById(documentBuilder.build());
            return  Result.success(insert);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Result deleteDocument(Long documentId) {
        Document oldDoc = documentMapper.selectById(documentId);
        if(Objects.isNull(oldDoc) ||oldDoc.getOwnerType()==1&&!oldDoc.getPublisherId().equals(UserThreadLocal.get().getId())){
            return Result.fail(20001,"????????????");
        }
        int i = documentMapper.deleteById(documentId);
        File oldDocFile = new File(FIFLE_PREFIX+oldDoc.getDocUri());
        oldDocFile.delete();
        return  Result.success(i);
    }
}
