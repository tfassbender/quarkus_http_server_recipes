package net.tfassbender.markdown;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RecipesMarkdownFormatter {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public RecipesMarkdownFormatter() {
        MutableDataSet options = new MutableDataSet()
                .set(Parser.EXTENSIONS, List.of(TablesExtension.create()))
                // Recipes are plain markdown; raw HTML (e.g. <script> or onerror attributes) is rendered as text
                // so the frontend can safely insert the result via innerHTML. javascript: links are suppressed by
                // flexmark's default SUPPRESSED_LINKS setting.
                .set(HtmlRenderer.ESCAPE_HTML, true);
        parser = Parser.builder(options).build();
        renderer = HtmlRenderer.builder(options).build();
    }

    public String toHtml(String markdown) {
        return renderer.render(parser.parse(markdown));
    }
}
