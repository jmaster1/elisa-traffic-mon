package com.geolog.server.controller.admin;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.nio.charset.StandardCharsets;

@Controller
public class DocsController {
    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    @GetMapping("/admin/docs/{name}")
    public String renderMarkdown(@PathVariable String name, Model model
    ) throws Exception {
        String path = "docs/" + name + ".md";
        ClassPathResource resource = new ClassPathResource(path);
        String markdown = resource.getContentAsString(StandardCharsets.UTF_8);
        String html = renderer.render(parser.parse(markdown));
        model.addAttribute("title", name);
        model.addAttribute("content", html);

        return "admin/docs";
    }
}
