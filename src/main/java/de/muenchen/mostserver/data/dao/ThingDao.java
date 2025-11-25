package de.muenchen.mostserver.data.dao;

import de.muenchen.mostserver.odata.EdmEntityAsType;
import de.muenchen.mostserver.odata.EdmEntityEntityExclude;
import de.muenchen.mostserver.odata.EdmEntityProvider;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;
import java.util.UUID;

@Table("THING")
@jakarta.persistence.Table(name = "Thing")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "Thing")
@EdmEntityProvider(namespace = "Odata.MOSTServer", type = "Thing")
public class ThingDao {

    @Id
    private UUID id;

    @EdmEntityEntityExclude
    @Column(name = "acl_id")
    private long aclId;

    private String name;

    @OneToMany
    @EdmEntityAsType(value = DatastreamDao.class, isCollection = true)
    private List<DatastreamDao> datastreams;

    @ManyToOne
    private ProjectDao project;

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

    public ProjectDao getProject() {
        return project;
    }

    public void setProject(ProjectDao project) {
        this.project = project;
    }
}
