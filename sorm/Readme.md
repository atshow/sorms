# 简单ORM框架(用户使用文档以后有时间补上) #
* 此框架适合喜欢JPA注解,但讨厌hibernate和spring data jpa效率低下的.同时又不想失去sql灵活性的.
* 使用jpa注解,但不完全实现jpa规范,单表增,删,改比较方便,同时对级联也做了支持,但不实现延迟加载功能,必须手动调用,才能加载级联对象(此处主要降低jpa实现复杂度).
* jpa支持注解如下:
  @Column,@Table,@Entity,@Id,@OneToOne,@OneToMany,@ManyToMany,@ManyToOne.@JoinColumn,@JoinTable
  支持将未定义的对象装入Map中.
* 支持使用模板写sql,主要使用jetbrick-template实现.
* 支持tinysql用法.
* 支持mybatis的resultMap,但无需编写xml,只需使用@Column注解和数据库字段映射即可,对于一条sql语句对应一个主类带子类对象,使用@OneToOne注解标记即可实现主类,子类的组装.
* 此框架为整合性框架,感谢jfinal,Nutz,mybatis,jetbrick-orm,ef-orm