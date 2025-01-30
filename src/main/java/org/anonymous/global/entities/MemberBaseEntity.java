package org.anonymous.global.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class MemberBaseEntity extends BaseEntity {
    @CreatedBy
    @Column(length = 65, updatable = false)
    private String createdBy; // 작성자

    @LastModifiedBy
    @Column(length = 65, insertable = false)
    private String modifiedBy; // 수정한자
}
