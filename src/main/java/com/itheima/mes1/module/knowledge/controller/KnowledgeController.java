package com.itheima.mes1.module.knowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.common.PageResult;
import com.itheima.mes1.common.Result;
import com.itheima.mes1.common.annotation.RequirePermission;
import com.itheima.mes1.module.knowledge.entity.KbChunk;
import com.itheima.mes1.module.knowledge.entity.KbDocument;
import com.itheima.mes1.module.knowledge.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Tag(name = "知识库")
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeService knowledgeService;

    @RequirePermission("knowledge:doc:list")
    @GetMapping
    public Result<PageResult<KbDocument>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Page<KbDocument> result = knowledgeService.page(page, pageSize, category, keyword);
        return Result.ok(new PageResult<>(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @RequirePermission("knowledge:doc:upload")
    @Operation(summary = "上传文档")
    @PostMapping("/upload")
    public Result<KbDocument> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(defaultValue = "other") String category) throws IOException {
        return Result.ok(knowledgeService.upload(file, category));
    }

    @RequirePermission("knowledge:doc:list")
    @Operation(summary = "查看切片")
    @GetMapping("/{id}/chunks")
    public Result<List<KbChunk>> chunks(@PathVariable Long id) {
        return Result.ok(knowledgeService.getChunks(id));
    }

    @RequirePermission("knowledge:doc:delete")
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.ok();
    }
}
