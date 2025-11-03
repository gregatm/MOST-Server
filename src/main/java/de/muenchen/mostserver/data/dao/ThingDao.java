package de.muenchen.mostserver.data.dao;

import de.muenchen.mostserver.odata.EdmEntityAsType;
import de.muenchen.mostserver.odata.EdmEntityEntityExclude;
import de.muenchen.mostserver.odata.EdmEntityProvider;
import jakarta.persistence.OneToMany;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table("THING")
@jakarta.persistence.Table(name = "Thing")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EdmEntityProvider(namespace = "Odata.MOSTServer", type = "Thing")
public class ThingDao implements IDao{

    @Id
    private UUID id;

    @EdmEntityEntityExclude
    private long aclId;

    private String name;

    @OneToMany
    @EdmEntityAsType(value = DatastreamDao.class, isCollection = true)
    private List<DatastreamDao> datastreams;

    public ThingDao(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getAclId() {
        return aclId;
    }

    public void setAclId(long aclId) {
        this.aclId = aclId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DatastreamDao> getDatastreams() {
        return datastreams;
    }

    public void setDatastreams(List<DatastreamDao> datastreams) {
        this.datastreams = datastreams;
    }
}
