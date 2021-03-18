package io.tackle.controls.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.tackle.commons.annotations.Filterable;
import io.tackle.commons.entities.AbstractEntity;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PostPersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "stakeholder")
@SQLDelete(sql = "UPDATE stakeholder SET deleted = true WHERE id = ?", check = ResultCheckStyle.COUNT)
@Where(clause = "deleted = false")
public class Stakeholder extends AbstractEntity {
    @Filterable
    public String displayName;
    @ManyToOne
    @Filterable
    public JobFunction jobFunction;
    @Filterable
    public String email;
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    @JsonBackReference("businessServicesReference")
    public List<BusinessService> businessServices = new ArrayList<>();

    @ManyToMany(mappedBy="stakeholders", fetch = FetchType.LAZY)
    @JsonBackReference("stakeholderGroupsReference")
    @Filterable(filterName = "stakeholderGroups.name")
    public Set<StakeholderGroup> stakeholderGroups = new HashSet<>();

    @PreRemove
    private void preRemove() {
        businessServices.forEach(businessService -> businessService.owner = null);
        stakeholderGroups.forEach(stakeholderGroup -> stakeholderGroup.stakeholders.remove(this));
    }

    @PreUpdate
    private void preUpdate() {
        stakeholderGroups.forEach(stakeholderGroup -> stakeholderGroup.stakeholders.add(this));
    }

    @PostPersist
    private void postPersist() {
        stakeholderGroups.forEach(stakeholderGroup -> {
            StakeholderGroup stakeholderGroupFromDb = StakeholderGroup.findById(stakeholderGroup.id);
            if (stakeholderGroupFromDb != null) stakeholderGroupFromDb.stakeholders.add(this);
        });
    }
}
