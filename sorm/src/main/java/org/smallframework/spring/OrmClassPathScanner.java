package org.smallframework.spring;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

public class OrmClassPathScanner extends ClassPathBeanDefinitionScanner {
    /**
     * 这用于指定SqlManagerFactoryBean的名称，利用Spring Bean机制自动创建
     */
    String sqlManagerFactoryBeanName;

    String suffix;

    public OrmClassPathScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public OrmClassPathScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public OrmClassPathScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public OrmClassPathScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment, ResourceLoader resourceLoader) {
        super(registry, useDefaultFilters, environment, resourceLoader);
    }

    public void registerFilters() {
        addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                    throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                // 这里设置包含条件
                return className.endsWith(suffix);
            }
        });
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
                    throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        // 如果指定的基础包路径中不存在任何类对象，则提示
        if (beanDefinitions.isEmpty()) {
            logger.warn("BeetlSql没有在 '" + Arrays.toString(basePackages) + "' 包中找到任何Mapper，请检查配置");
        } else {
            processBeanDefinitions(beanDefinitions);
        }
        return beanDefinitions;
    }

    /**
     * 重新父类的判断是否能够实例化的组件
     * @param beanDefinition
     * @return
     */
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        // 原方法这里不是判断是否为接口和是不依赖
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }

    /**
     * 对扫描到的含有BeetlSqlFactoryBean的Bean描述信息进行遍历
     * @param beanDefinitions
     */
    void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder holder : beanDefinitions) {
            logger.debug(holder.toString());
            definition = (GenericBeanDefinition) holder.getBeanDefinition();
            String mapperClassName = definition.getBeanClassName();
            // 必须在这里加入泛型限定，要不然在spring下会有循环引用的问题
            definition.getConstructorArgumentValues().addGenericArgumentValue(mapperClassName);
            definition.getPropertyValues().add("mapperInterface", mapperClassName);
            // 根据工厂的名称创建出SqlManager
            definition.getPropertyValues().add("sqlManager", new RuntimeBeanReference(this.sqlManagerFactoryBeanName));
//            definition.setBeanClass(BeetlSqlFactoryBean.class);
            // 设置Mapper按照接口组装
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            if (logger.isDebugEnabled()) {
                logger.debug("已开启BeetSql自动按照类型注入 '" + holder.getBeanName() + "'.");
            }
        }
    }
}
