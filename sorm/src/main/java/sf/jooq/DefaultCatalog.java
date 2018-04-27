package sf.jooq;

import org.jooq.Name;
import org.jooq.impl.CatalogImpl;

public class DefaultCatalog extends CatalogImpl {
    public DefaultCatalog(Name name) {
        super(name);
    }

    public DefaultCatalog(String name) {
        super(name);
    }
}
