package sf.accelerator.asm.commons;


import org.springframework.asm.FieldVisitor;
import sf.tools.utils.Assert;

public abstract class FieldExtCallback {

    public FieldExtCallback(FieldVisitor v) {
        this.visitor = v;
        Assert.notNull(v, "");
    }

    FieldVisitor visitor;

    public abstract void onFieldRead(FieldExtDef info);
}
