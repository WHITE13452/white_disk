package com.whitedisk.white_disk.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qiwenshare.common.anno.MyLog;
import com.qiwenshare.common.exception.QiwenException;
import com.qiwenshare.common.result.RestResult;
import com.qiwenshare.common.util.security.JwtUser;
import com.qiwenshare.common.util.security.SessionUtil;
import com.whitedisk.white_disk.component.FileDealComp;
import com.whitedisk.white_disk.dto.file.*;
import com.whitedisk.white_disk.entity.FileEntity;
import com.whitedisk.white_disk.entity.UserFileEntity;
import com.whitedisk.white_disk.service.api.IFileService;
import com.whitedisk.white_disk.service.api.IUserFileService;
import com.whitedisk.white_disk.utils.TreeNode;
import com.whitedisk.white_disk.utils.WhiteFile;
import com.whitedisk.white_disk.vo.file.FileDetailVO;
import com.whitedisk.white_disk.vo.file.FileListVO;
import com.whitedisk.white_disk.vo.file.SearchFileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Session;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author white
 * 文件基本操作
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private IFileService fileService;
    @Autowired
    private IUserFileService userFileService;

    @Autowired
    private FileDealComp fileDealComp;

    private static final String CURRENT_MODULE="文件接口";

    @Operation(summary = "创建文件", description = "文件的创建", tags = {"file"})
    @MyLog(operation = "创建文件", module = CURRENT_MODULE)
    @PostMapping("/createFile")
    @ResponseBody
    public RestResult<Object> createFile(@Valid @RequestBody CreateFileDTO createFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        String message =fileService.createFile(createFileDTO,sessionUser);
        return RestResult.success().message(message);
    }

    @Operation(summary = "创建文件夹", description = "文件夹（目录）的创建", tags = {"file"})
    @MyLog(operation = "创建文件夹", module = CURRENT_MODULE)
    @PostMapping("/createFold")
    @ResponseBody
    public RestResult<String> createFold(@Valid @RequestBody CreateFoldDTO createFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        Boolean flag = fileService.createFold(createFileDTO, sessionUser);
        if (!flag){
            return RestResult.fail().message("同名文件已存在");
        }
        return RestResult.success();
    }

    @Operation(summary = "文件搜索", description = "文件搜索", tags = {"file"})
    @MyLog(operation = "搜索文件", module = CURRENT_MODULE)
    @GetMapping("/search")
    @ResponseBody
    public RestResult<List<SearchFileVO>> searchFile(SearchFileDTO searchFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        List<SearchFileVO> searchFileVOList= fileService.searchFile(searchFileDTO,sessionUser);
        return RestResult.success().data(searchFileVOList);
    }
    @Operation(summary = "重命名文件", description = "重命名文件", tags = {"file"})
    @MyLog(operation = "重命名文件", module = CURRENT_MODULE)
    @PostMapping("/renamefile")
    @ResponseBody
    public RestResult<String> renameFile(@RequestBody RenameFileDTO renameFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        Boolean flag=fileService.renameFile(renameFileDTO,sessionUser);
        if(!flag){
            return RestResult.fail().message("同名文件已存在");
        }
        return RestResult.success();
    }
    @Operation(summary = "文件列表", description = "用于展示用户所拥有的文件", tags = {"file"})
    @MyLog(operation = "文件列表", module = CURRENT_MODULE)
    @GetMapping("/getfilelist")
    @ResponseBody
    public RestResult<FileListVO> getFileList(
            @Parameter(description = "文件类型", required = true) String fileType,
            @Parameter(description = "文件路径", required = true) String filePath,
            @Parameter(description = "当前页", required = true) long currentPage,
            @Parameter(description = "页面数量", required = true) long pageCount){
        JwtUser sessionUser = SessionUtil.getSession();
        if("0".equals(fileType)){
            IPage<FileListVO> fileList = userFileService.userFileList(null,filePath,currentPage,pageCount);
            return RestResult.success().dataList(fileList.getRecords(), fileList.getTotal());
        }else {
            IPage<FileListVO> fileList = userFileService.getFileByFileType(Integer.valueOf(fileType), currentPage, pageCount, sessionUser.getUserId());
            return RestResult.success().dataList(fileList.getRecords(), fileList.getTotal());
        }
    }

    @Operation(summary = "批量删除文件", description = "批量删除文件", tags = {"file"})
    @MyLog(operation = "批量删除文件", module = CURRENT_MODULE)
    @PostMapping("/batchdeletefile")
    @ResponseBody
    public RestResult<String> deleteBatchOfFile(@RequestBody BatchDeleteFileDTO batchDeleteFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        String[] userFileIds = batchDeleteFileDTO.getUserFileIds().split(",");
        for (String userFileId : userFileIds) {
            userFileService.deleteUserFile(userFileId,sessionUser);
            fileDealComp.deleteESByUserFileId(userFileId);
        }
        return RestResult.success().message("删除成功");
    }

    @Operation(summary = "删除文件", description = "删除文件", tags = {"file"})
    @MyLog(operation = "删除文件", module = CURRENT_MODULE)
    @PostMapping("/deletefile")
    @ResponseBody
    public RestResult<String> deleteFile(@RequestBody DeleteFileDTO deleteFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        userFileService.deleteUserFile(deleteFileDTO.getUserFileId(), sessionUser);
        fileDealComp.deleteESByUserFileId(deleteFileDTO.getUserFileId());
        return RestResult.success();
    }


    @Operation(summary = "解压文件", description = "解压文件", tags = {"file"})
    @MyLog(operation = "解压文件", module = CURRENT_MODULE)
    @PostMapping("/unzipfile")
    @ResponseBody
    public RestResult<String> unzipFile(@RequestBody UnzipFileDTO unzipFileDTO){
        try{
            fileService.unzipFile(unzipFileDTO.getUserFileId(), unzipFileDTO.getUnzipMode(), unzipFileDTO.getFilePath());
        }catch (QiwenException e){
            return RestResult.fail().message(e.getMessage());
        }
        return RestResult.success();
    }

    @Operation(summary = "文件复制", description = "复制文件或者目录", tags = {"file"})
    @MyLog(operation = "文件复制", module = CURRENT_MODULE)
    @PostMapping("/copyfile")
    @ResponseBody
    public RestResult<String> copyFile(CopyFileDTO copyFileDTO){
        JwtUser sessionUser = SessionUtil.getSession();
        if(!fileService.copyFile(copyFileDTO,sessionUser)){
            return RestResult.fail().message("原路径与目标路径冲突，不能复制");
        }
        return RestResult.success();
    }

    @Operation(summary = "文件移动", description = "可以移动文件或者目录", tags = {"file"})
    @PostMapping("/movefile")
    @MyLog(operation = "文件移动", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> moveFile(@RequestBody MoveFileDTO moveFileDTO){
        JwtUser jwtUser = SessionUtil.getSession();
        UserFileEntity userFile = userFileService.getById(moveFileDTO.getUserFileId());
        String oldPath = userFile.getFilePath();
        String newPath = moveFileDTO.getFilePath();
        String fileName = userFile.getFileName();
        String extendName = userFile.getExtendName();
        //是文件夹
        if (StringUtil.isEmpty(extendName)) {
            WhiteFile whiteFile = new WhiteFile(oldPath, fileName, true);
            if (newPath.startsWith(whiteFile.getPath() + WhiteFile.separator) || newPath.equals(whiteFile.getPath())) {
                return RestResult.fail().message("换个路径吧，别在一个文件夹里转悠");
            }
        }
        userFileService.updateFilepathByUserFileId(moveFileDTO.getUserFileId(), newPath, jwtUser.getUserId());
        fileDealComp.deleteRepeatSubDirFile(newPath, jwtUser.getUserId());
        return RestResult.success();
    }

    @Operation(summary = "批量移动文件", description = "可以同时选择移动多个文件或者目录", tags = {"file"})
    @PostMapping("/batchmovefile")
    @MyLog(operation = "批量移动文件", module = CURRENT_MODULE)
    @ResponseBody
    public RestResult<String> batchMoveFile(@RequestBody BatchMoveFileDTO batchMoveFileDTO){
        JwtUser jwtUser = SessionUtil.getSession();

        String newFilePath = batchMoveFileDTO.getFilePath();

        //拿到userFileId数组
        String userFileIds = batchMoveFileDTO.getUserFileIds();
        String[] userFileIdArr = userFileIds.split(",");
        for (String userFileId : userFileIdArr) {
            UserFileEntity userFile = userFileService.getById(userFileId);
            //if its a dir
            if (StringUtil.isEmpty(userFile.getExtendName())) {
                WhiteFile whiteFile = new WhiteFile(userFile.getFilePath(), userFile.getFileName(), true);
                if (newFilePath.startsWith(whiteFile.getPath() + WhiteFile.separator) || newFilePath.equals(whiteFile.getPath())) {
                    return RestResult.fail().message("换个路径吧，别在一个文件夹里转悠");
                }
            }
            userFileService.updateFilepathByUserFileId(userFile.getUserFileId(), newFilePath, jwtUser.getUserId());
        }
        return RestResult.success().data("批量移动文件成功");
    }

    @Operation(summary = "获取文件树", description = "文件移动的时候需要用到该接口，用来展示目录树", tags = {"file"})
    @MyLog(operation = "获取文件树", module = CURRENT_MODULE)
    @GetMapping(value = "/getfiletree")
    @ResponseBody
    public RestResult<TreeNode> getFileTree(){
        RestResult<TreeNode> result = new RestResult<>();
        JwtUser jwtUser = SessionUtil.getSession();
        //找到文件夹
        List<UserFileEntity> userFileList = userFileService.selectFilePathTreeByUserId(jwtUser.getUserId());
        TreeNode treeNode = new TreeNode();
        treeNode.setLabel(WhiteFile.separator);
        treeNode.setId(0L);
        long id = 1;
        for (int i = 0; i < userFileList.size(); i++) {
            UserFileEntity userFile = userFileList.get(i);
            WhiteFile whiteFile = new WhiteFile(userFile.getFilePath(), userFile.getFileName(), false);
            String filePath = whiteFile.getPath();

            Queue<String> queue = new LinkedList<>();

            String[] strArr = filePath.split(WhiteFile.separator);
            for (int j = 0; j < strArr.length; j++) {
                if (!"".equals(strArr[j]) && strArr[j] != null) {
                    queue.add(strArr[j]);
                }
            }
            if (queue.size() == 0) {
                continue;
            }
            treeNode = fileDealComp.insertTreeNode(treeNode, id++, WhiteFile.separator, queue);
        }
        List<TreeNode> treeNodeList = treeNode.getChildren();
        Collections.sort(treeNodeList, (o1, o2) -> {
            long i = o1.getId() - o2.getId();
            return (int) i;
        });
        result.setSuccess(true);
        result.setData(treeNode);
        return result;
    }

    @Operation(summary = "更新文件", description = "更新简单的文本文件", tags = {"file"})
    @MyLog(operation = "更新文件", module = CURRENT_MODULE)
    @PostMapping(value = "/update")
    @ResponseBody
    public RestResult<String> updateFile(@RequestBody UpdateFileDTO updateFileDTO) {
        UserFileEntity userFile = userFileService.getById(updateFileDTO.getUserFileId());
        FileEntity fileEntity = fileService.getById(userFile.getFileId());
        Long filePointCount = fileService.getFilePointCount(fileEntity.getFileId());
        String fileUrl = fileEntity.getFileUrl();
        if (filePointCount > 1){
            fileUrl = fileDealComp.copyFile(fileEntity, userFile);
        }
        String content = updateFileDTO.getFileContent();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(content.getBytes());
        try {
            int fileSize = byteArrayInputStream.available();
            fileDealComp.saveFileInputStream(fileEntity.getStorageType(), fileUrl, byteArrayInputStream);
            String md5Str = fileDealComp.getIdentifierByFile(fileUrl, fileEntity.getStorageType());
            fileService.updateFileDetail(userFile.getUserFileId(), md5Str, fileSize);

        } catch (IOException e) {
            throw new QiwenException(999999, "修改文件异常");
        } finally {
            try {
                byteArrayInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return RestResult.success().message("修改文件成功");
    }

    @Operation(summary = "查询文件详情", description = "查询文件详情", tags = {"file"})
    @GetMapping(value = "/detail")
    @ResponseBody
    public RestResult<FileDetailVO> queryFileDetail(
            @Parameter(description = "用户文件Id", required = true) String userFileId){
        FileDetailVO vo = fileService.getFileDetail(userFileId);
        return RestResult.success().data(vo);
    }
}
