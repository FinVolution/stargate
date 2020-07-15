package com.ppdai.stargate.po;

import com.ppdai.stargate.po.converter.EnvUrl;
import com.ppdai.stargate.po.converter.ListToJsonStringConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Data
@Cacheable(false)
@EqualsAndHashCode(callSuper = false)
@Table(name = "application", catalog = "")
public class ApplicationEntity extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Basic
    @Column(name = "description", nullable = true, length = 128)
    private String description;

    @Basic
    @Column(name = "owner", nullable = true, length = 512)
    private String owner;

    @Basic
    @Column(name = "department", nullable = true, length = 64)
    private String department;

    @Basic
    @Column(name = "department_code", nullable = true, length = 64)
    private String departmentCode;

    @Basic
    @Column(name = "cmdb_app_id", nullable = false, length = 64)
    private String cmdbAppId;

    @Column(name = "enable_ha", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean enableHa;

    @Column(name="env_urls")
    @Convert(converter = ListToJsonStringConverter.class)
    private List<EnvUrl> envUrls;
}
