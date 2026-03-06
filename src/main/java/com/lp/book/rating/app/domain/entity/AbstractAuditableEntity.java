package com.lp.book.rating.app.domain.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Auditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Optional;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class AbstractAuditableEntity<ID extends Serializable> implements Auditable<String, ID, LocalDateTime> {

    @Version
    @JsonIgnore
    @Column(name = "rec_version", nullable = false)
    protected Integer version;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    protected String createdBy;

    @CreatedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "created_ts", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "modified_by")
    protected String lastModifiedBy;

    @LastModifiedDate
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "modified_ts")
    private LocalDateTime lastModifiedDate;

    @Override
    @Transient
    @JsonIgnore
    public boolean isNew() { return version == null; }

    @NonNull
    public Optional<String> getCreatedBy() { return Optional.ofNullable(createdBy); }

    @NonNull
    public Optional<LocalDateTime> getCreatedDate() { return Optional.ofNullable(createdDate); }

    @NonNull
    public Optional<String> getLastModifiedBy() { return Optional.ofNullable(lastModifiedBy); }

    @NonNull
    public Optional<LocalDateTime> getLastModifiedDate() { return Optional.ofNullable(lastModifiedDate); }

    public Integer getVersion() { return Optional.ofNullable(version).orElse(0); }

}
