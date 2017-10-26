package am.ik.servicebroker.mysql.servicebroker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/v2/catalog")
public class CatalogController {
    private final Object catalog;

    public CatalogController(@Value("${service-broker.catalog:classpath:catalog.yml}") Resource catalog) throws IOException {
        Yaml yaml = new Yaml();
        try (InputStream stream = catalog.getInputStream()) {
            this.catalog = yaml.load(stream);
        }
    }

    @GetMapping
    public Object catalog() {
        return this.catalog;
    }
}
