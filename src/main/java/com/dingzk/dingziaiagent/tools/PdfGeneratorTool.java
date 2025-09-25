package com.dingzk.dingziaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.dingzk.dingziaiagent.constants.FileConstants;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.html.CssAppliers;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.TagProcessorFactory;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipeline;
import com.itextpdf.tool.xml.pipeline.html.HtmlPipelineContext;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.ast.Node;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import com.vladsch.flexmark.parser.Parser;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PdfGeneratorTool {

    @Tool(description = "This is a tool which can used to generate pdf document using given content")
    public String executePdfGeneration(@ToolParam(description = "The pdf file content") String content,
                                       @ToolParam(description = "The file name of generated pdf document") String fileName) {
        File dir = new File(FileConstants.FILE_BASE_DIR, "pdf");

        try {
            FileUtil.mkdir(dir);
            String filePath = dir + "/" + fileName;

            convertMdToPdf(content, filePath);
            return "Pdf generated success!";
        } catch (Exception e) {
            return "Error occurred when generating pdf file: " + e.getMessage();
        }
    }

    private void convertMdToPdf(String mdContent, String pdfFilePath) throws Exception {

        // 2. 使用 flexmark-java 将 Markdown 转换为 HTML
        Parser parser = Parser.builder().build();
        Node document = parser.parse(mdContent);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        String htmlContent = renderer.render(document);

        String fontPath = "fonts/SourceHanSansSC-Regular.otf"; // 字体在 classpath 中的路径
        String fontCss = String.format(
                "<style>\n" +
                        "   @font-face {\n" +
                        "       font-family: 'ChineseFont';\n" + // 定义一个字体名称
                        "       src: url('%s');\n" + // 字体文件的路径
                        "   }\n" +
                        "   body {\n" +
                        "       font-family: 'ChineseFont', sans-serif;\n" + // 为 body 应用此字体
                        "       font-size: 12px;\n" +
                        "   }\n" +
                        "   h1 {\n" +
                        "       font-family: 'ChineseFont', sans-serif;\n" +// 为标题也应用此字体
                        "       font-size: 24px;\n" +
                        "       line-height: 48px;\n" +
                        "   }\n" +
                        "   h2 {\n" +
                        "       font-family: 'ChineseFont', sans-serif;\n" + // 为标题也应用此字体
                        "       font-size: 20px;\n" +
                        "       line-height: 40px;\n" +
                        "   }\n" +
                        "   h3 {\n" +
                        "       font-family: 'ChineseFont', sans-serif;\n" + // 为标题也应用此字体
                        "       font-size: 16px;\n" +
                        "       line-height: 32px;\n" +
                        "   }\n" +
                        "   p, li {\n" +
                        "       font-family: 'ChineseFont', sans-serif;\n" + // p, li
                        "   }\n" +
                        "</style>\n",
                fontPath
        );
        String htmlWithFont = fontCss + htmlContent;

        Document pdfDocument = new Document();
        PdfWriter writer = PdfWriter.getInstance(pdfDocument, new FileOutputStream(pdfFilePath));
        pdfDocument.open();

        // 4. 创建自定义的 FontProvider
        // XMLWorkerFontProvider 是 XMLWorker 用来查找字体的关键类
        XMLWorkerFontProvider fontProvider = new MyXMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);

        // 5. 创建 CSS 解析器和应用器
        StyleAttrCSSResolver cssResolver = new StyleAttrCSSResolver();
        CssFile cssFile = XMLWorkerHelper.getCSS(new ByteArrayInputStream(fontCss.getBytes(StandardCharsets.UTF_8)));
        cssResolver.addCss(cssFile);

        // 6. 创建 CSS 应用器，并将自定义 FontProvider 注入
        CssAppliers cssAppliers = new CssAppliersImpl(fontProvider);

        // 7. 创建 HTML 标签处理器
        TagProcessorFactory tagProcessorFactory = Tags.getHtmlTagProcessorFactory();

        // PDF 内容处理器 (这是 Pipeline 的终点)
        // 它负责将解析后的 HTML 事件转换为 PDF 元素
        PdfWriterPipeline pdf = new PdfWriterPipeline(pdfDocument, writer);

        // 6. **关键**: 构建 Pipeline 链
        // 流程: HTML/CSS 解析 -> 标签处理 -> PDF 生成
        HtmlPipelineContext htmlContext = new HtmlPipelineContext(cssAppliers);
        htmlContext.setTagFactory(tagProcessorFactory);
        HtmlPipeline htmlPipeline = new HtmlPipeline(htmlContext, pdf);
        CssResolverPipeline cssPipeline = new CssResolverPipeline(cssResolver, htmlPipeline);

        // 7. 使用 Pipeline 创建 XMLWorker
        XMLWorker worker = new XMLWorker(cssPipeline, true);

        // 8. 创建 XMLParser，并将 XMLWorker 设置为其监听器
        XMLParser xmlParser = new XMLParser(worker);

        // 使用 XMLWorkerHelper 解析 HTML 并写入 PDF
        try (ByteArrayInputStream bais = new ByteArrayInputStream(htmlWithFont.getBytes(StandardCharsets.UTF_8));
             InputStreamReader isr = new InputStreamReader(bais, StandardCharsets.UTF_8)) {
            xmlParser.parse(isr);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            pdfDocument.close();
            writer.close();
        }
    }

    /**
     * 自定义 XMLWorkerFontProvider，用于 iTextPDF 的 XMLWorker 查找和加载字体
     * 必须继承 XMLWorkerFontProvider
     */
    public static class MyXMLWorkerFontProvider extends XMLWorkerFontProvider {

        public MyXMLWorkerFontProvider(String path) {
            super(path);
        }

        @Override
        public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, BaseColor color) {
            // 如果 HTML/CSS 中指定的字体名为 "ChineseFont"，则加载我们的自定义字体
            if ("ChineseFont".equals(fontname)) {
                try {
                    // 从 classpath 加载字体文件
                    // 注意：这里的路径是相对于 resources 目录的
                    String fontPath = "fonts/SourceHanSansSC-Regular.otf";
                    BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    return new Font(bf, size, style, color);
                } catch (Exception e) {
                    // 如果加载失败，返回一个默认字体，防止程序崩溃
                    System.err.println("警告：自定义字体加载失败，将使用默认字体。" + e.getMessage());
                    return super.getFont(fontname, encoding, embedded, size, style, color);
                }
            }
            // 对于其他字体请求，使用默认逻辑
            return super.getFont(fontname, encoding, embedded, size, style, color);
        }
    }
}
