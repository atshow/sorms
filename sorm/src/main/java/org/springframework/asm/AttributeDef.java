package org.springframework.asm;

import org.springframework.asm.Attribute;

public class AttributeDef extends Attribute {

    public AttributeDef(String type) {
        super(type);
    }

    public AttributeDef(final String type, byte[] value) {
        super(type);
        this.value = value;
    }
}
