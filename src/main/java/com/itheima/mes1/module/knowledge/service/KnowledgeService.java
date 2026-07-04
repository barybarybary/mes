package com.itheima.mes1.module.knowledge.service;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mes1.module.knowledge.entity.KbChunk;
import com.itheima.mes1.module.knowledge.entity.KbDocument;
import com.itheima.mes1.module.knowledge.mapper.KbChunkMapper;
import com.itheima.mes1.module.knowledge.mapper.KbDocumentMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class KnowledgeService {

    @Autowired
    private KbDocumentMapper documentMapper;
    @Autowired
    private KbChunkMapper chunkMapper;

    public Page<KbDocument> page(int page, int pageSize, String category, String keyword) {
        LambdaQueryWrapper<KbDocument> w = new LambdaQueryWrapper<KbDocument>()
                .eq(StrUtil.isNotBlank(category), KbDocument::getCategory, category)
                .like(StrUtil.isNotBlank(keyword), KbDocument::getTitle, keyword)
                .orderByDesc(KbDocument::getCreateTime);
        Page<KbDocument> result = documentMapper.selectPage(new Page<>(page, pageSize), w);
        // MP 3.5.9 removed PaginationInnerInterceptor, manually set total
        result.setTotal(documentMapper.selectCount(w));
        return result;
    }

    @Transactional
    public KbDocument upload(MultipartFile file, String category) throws IOException {
        KbDocument doc = new KbDocument();
        doc.setTitle(file.getOriginalFilename());
        doc.setCategory(category);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(getFileExtension(file.getOriginalFilename()));
        doc.setFileSize(file.getSize());
        doc.setStatus(1); // 待处理

        // 读取文本内容
        String content = readContent(file);
        doc.setContent(content);
        documentMapper.insert(doc);

        // 切片
        if (StrUtil.isNotBlank(content)) {
            int count = chunkDocument(doc.getId(), content);
            doc.setChunkCount(count);
            doc.setStatus(3); // 已完成
            documentMapper.updateById(doc);
        }

        return doc;
    }

    public List<KbChunk> getChunks(Long documentId) {
        return chunkMapper.selectByDocumentId(documentId);
    }

    @Transactional
    public void delete(Long id) {
        chunkMapper.deleteByDocumentId(id);
        documentMapper.deleteById(id);
    }

    private int chunkDocument(Long documentId, String content) {
        // 简单按段落+500字切片
        int chunkSize = 500;
        int overlap = 50;
        List<String> chunks = new ArrayList<>();
        int i = 0;
        while (i < content.length()) {
            int end = Math.min(i + chunkSize, content.length());
            chunks.add(content.substring(i, end));
            i += (chunkSize - overlap);
        }

        for (int j = 0; j < chunks.size(); j++) {
            KbChunk chunk = new KbChunk();
            chunk.setDocumentId(documentId);
            chunk.setChunkIndex(j);
            chunk.setContent(chunks.get(j));
            chunk.setTokenCount(chunks.get(j).length());
            chunk.setCreateTime(LocalDateTime.now());
            chunkMapper.insert(chunk);
        }

        KbDocument doc = documentMapper.selectById(documentId);
        if (doc != null) {
            doc.setChunkCount(chunks.size());
            documentMapper.updateById(doc);
        }
        return chunks.size();
    }

    private String readContent(MultipartFile file) throws IOException {
        String ext = getFileExtension(file.getOriginalFilename()).toLowerCase();
        try (InputStream in = file.getInputStream()) {
            return switch (ext) {
                case "txt", "md", "csv", "json", "xml", "sql" ->
                    IoUtil.read(in, StandardCharsets.UTF_8);
                case "pdf" -> {
                    try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                        yield new PDFTextStripper().getText(doc);
                    }
                }
                case "docx" -> {
                    try (XWPFDocument doc = new XWPFDocument(in)) {
                        StringBuilder sb = new StringBuilder();
                        doc.getParagraphs().forEach(p -> sb.append(p.getText()).append("\n"));
                        yield sb.toString();
                    }
                }
                case "xlsx" -> {
                    try (XSSFWorkbook wb = new XSSFWorkbook(in)) {
                        StringBuilder sb = new StringBuilder();
                        wb.forEach(sheet -> {
                            sb.append("\n=== ").append(sheet.getSheetName()).append(" ===\n");
                            sheet.forEach(row -> {
                                row.forEach(cell -> sb.append(cell.toString()).append("\t"));
                                sb.append("\n");
                            });
                        });
                        yield sb.toString();
                    }
                }
                default -> new String(in.readAllBytes(), StandardCharsets.UTF_8);
            };
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "";
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
