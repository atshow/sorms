package sf.accelerator.asm.commons;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Attribute;
import org.springframework.asm.FieldVisitor;
import org.springframework.asm.Opcodes;
import sf.tools.ASMUtils;
import sf.tools.utils.Assert;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class FieldExtDef extends FieldVisitor {
    private boolean end = false;
    private Map<String, AnnotationDef> annotations = new HashMap<String, AnnotationDef>();
    private Map<String, Attribute> attrs = new LinkedHashMap<String, Attribute>();
    private FieldExtCallback call;

    public FieldExtDef(FieldExtCallback call) {
        super(Opcodes.ASM5);
        this.call = call;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        AnnotationDef ann = new AnnotationDef(desc);
        ann.visible = visible;
        annotations.put(desc, ann);
        return ann;
    }

    @Override
    public void visitAttribute(Attribute attr) {
        attrs.put(attr.type, attr);
    }

    @Override
    public void visitEnd() {
        end = true;
        if (call != null) {
            call.onFieldRead(this);
            if (call.visitor != null) {
                this.accept(call.visitor);
            }
        }
    }

    public boolean isEnd() {
        return end;
    }

    public void accept(FieldVisitor to) {
        Assert.isTrue(end, "");
        for (Attribute attr : attrs.values()) {
            to.visitAttribute(attr);
        }
        for (Map.Entry<String, AnnotationDef> e : annotations.entrySet()) {
            String desc = e.getKey();
            boolean visible = e.getValue().visible;
            AnnotationVisitor too = to.visitAnnotation(desc, visible);
            e.getValue().inject(too);
        }
        to.visitEnd();
    }

    public AnnotationDef getAnnotation(String desc) {
        return annotations.get(desc);
    }

    public AnnotationDef getAnnotation(Class<?> class1) {
        return annotations.get(ASMUtils.getDesc(class1));
    }

    public Attribute getAttribute(String type) {
        return attrs.get(type);
    }

}
