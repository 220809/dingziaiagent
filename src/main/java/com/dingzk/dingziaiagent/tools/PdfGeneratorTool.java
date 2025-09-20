package com.dingzk.dingziaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.dingzk.dingziaiagent.constants.FileConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

public class PdfGeneratorTool {

    @Tool(description = "This is a tool which can used to generate pdf document using given content")
    public String executePdfGeneration(@ToolParam(description = "The pdf file content") String content,
                                       @ToolParam(description = "The file name of generated pdf document") String fileName) {
        File dir = new File(FileConstants.FILE_BASE_DIR, "pdf");

        try {
            FileUtil.mkdir(dir);
            String filePath = dir + "/" + fileName;

            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
            document.setFont(font);

            // 添加内容
            Paragraph contentParagraph = new Paragraph(content)
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(15);
            document.add(contentParagraph);

            document.close();
            return "Pdf generated success!";
        } catch (Exception e) {
            return "Error occurred when generating pdf file: " + e.getMessage();
        }
    }
}
