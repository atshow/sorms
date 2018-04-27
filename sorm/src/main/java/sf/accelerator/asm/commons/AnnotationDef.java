package sf.accelerator.asm.commons;

import org.springframework.asm.AnnotationVisitor;
import org.springframework.asm.Opcodes;
import sf.tools.utils.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class AnnotationDef extends AnnotationVisitor {
    private boolean end = false;

    public AnnotationDef(String desc) {
        super(Opcodes.ASM5);
        this.desc = desc;
    }

    protected boolean visible;
    protected String desc;
    protected Map<String, Object> attrs = new HashMap<String, Object>();
    protected List<String[]> enums = new ArrayList<String[]>();
    protected Map<String, AnnotationDef> annotations = new HashMap<String, AnnotationDef>();
    protected Map<String, AnnotationDef> arrays = new HashMap<String, AnnotationDef>();

    @Override
    public void visit(String name, Object value) {
        attrs.put(name, value);
    }

    @Override
    public void visitEnum(String name, String desc, String value) {
        this.enums.add(new String[]{name, desc, value});
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationDef ann = new AnnotationDef(desc);
        annotations.put(name, ann);
        return ann;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        AnnotationDef ann = new AnnotationDef(name);
        arrays.put(name, ann);
        return ann;
    }

    @Override
    public void visitEnd() {
        end = true;
    }

    public void inject(AnnotationVisitor to) {
        Assert.isTrue(end, "");
        for (Entry<String, Object> e : attrs.entrySet()) {
            to.visit(e.getKey(), e.getValue());
        }
        for (String[] enu : enums) {
            to.visitEnum(enu[0], enu[1], enu[2]);
        }
        for (Entry<String, AnnotationDef> e : annotations.entrySet()) {
            AnnotationVisitor too = to.visitAnnotation(e.getKey(), e.getValue().desc);
            e.getValue().inject(too);
        }
        for (Entry<String, AnnotationDef> e : arrays.entrySet()) {
            AnnotationVisitor too = to.visitArray(e.getKey());
            e.getValue().inject(too);
        }
        to.visitEnd();
    }

    public boolean isEnd() {
        return end;
    }
}

