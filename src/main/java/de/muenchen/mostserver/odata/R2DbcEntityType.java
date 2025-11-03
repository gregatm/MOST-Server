package de.muenchen.mostserver.odata;

import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;

public class R2DbcEntityType extends CsdlEntityType {

    private Class<?> typeClass;

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public void setTypeClass(Class<?> typeClass) {
        this.typeClass = typeClass;
    }
}
