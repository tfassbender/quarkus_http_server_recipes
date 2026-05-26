package net.tfassbender.markdown;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class RecipesMarkdownFormatter {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public RecipesMarkdownFormatter() {
        parser = Parser.builder()
                .extensions(List.of(TablesExtension.create()))
                .build();
        renderer = HtmlRenderer.builder()
                .extensions(List.of(TablesExtension.create()))
                .build();
    }

    public String toHtml(String markdown) {
        return renderer.render(parser.parse(markdown));
    }
}