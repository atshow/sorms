package org.smallframework.spring.path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;
import sf.tools.ArrayUtils;
import sf.tools.StringUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * spring 类扫描
 * @see org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
 */
public class SpringClassPathScanning {

    private static Logger log = LoggerFactory.getLogger(SpringClassPathScanning.class);

    protected static final String DEFAULT_RESOURCE_PATTERN = "**/*.class";

    protected static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    /**
     * metadataReaderFactory spring中用来读取resource为class的工具
     */
    protected static MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

    /**
     * 根据扫描包的配置
     * 加载需要检查的方法
     */
    public static Resource[] getClassResources(String scanPackages) {
        String[] scanPackageArr = scanPackages.split(",");
        return getClassResources(scanPackageArr);
    }


    public static Resource[] getClassResources(String... scanPackageArr) {
        return getClassResources(null, scanPackageArr);
    }

    /**
     * @param rootClasspath  lass path根路径，如果不指定那么就在所有ClassPath下寻找
     * @param scanPackageArr
     * @return
     */
    public static Resource[] getClassResources(URL rootClasspath, String... scanPackageArr) {
        ResourcePatternResolver rpr = resourcePatternResolver;
        if (rootClasspath != null) {
            rpr = new PathMatchingResourcePatternResolver(new URLClassLoader(new URL[]{rootClasspath}));
        }
        if (ArrayUtils.isNotEmpty(scanPackageArr)) {
            for (String basePackage : scanPackageArr) {
                if (StringUtils.isBlank(basePackage)) {
                    continue;
                }
                String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                        ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage)) + "/" + DEFAULT_RESOURCE_PATTERN;
                Resource[] resources = null;
                try {
                    resources = rpr.getResources(packageSearchPath);
                } catch (Exception e) {
                    log.error("初始化SensitiveWordInterceptor失败", e);
                }
                return resources;
            }
        } else {
            String packageSearchPath = "classpath:" + DEFAULT_RESOURCE_PATTERN;
            Resource[] resources = null;
            try {
                resources = rpr.getResources(packageSearchPath);
            } catch (Exception e) {
                log.error("初始化SensitiveWordInterceptor失败", e);
            }
            return resources;
        }
        return null;
    }


    /**
     * 根据扫描包的配置
     * 加载需要检查的方法
     */
    public static List<ClassMetadata> getClassMetadatas(String scanPackages) {
        String[] scanPackageArr = scanPackages.split(",");
        return getClassMetadatas(scanPackageArr);
    }

    public static List<ClassMetadata> getClassMetadatas(String... scanPackageArr) {
        Resource[] resources = getClassResources(scanPackageArr);
        List<ClassMetadata> list = new ArrayList<>();
        if (resources != null) {
            for (Resource resource : resources) {
                //检查resource，这里的resource都是class
                list.add(getClassMetadata(resource));
            }
        }
        return list;
    }

    /**
     * 加载资源，判断里面的方法
     * @param resource 这里的资源就是一个Class
     * @return
     */
    public static ClassMetadata getClassMetadata(Resource resource) {
        ClassMetadata className = null;
        try {
            if (resource.isReadable()) {
                MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
                if (metadataReader != null) {
                    className = metadataReader.getClassMetadata();
                }
            }
        } catch (Exception e) {
            log.error("获取类失败", e);
        }
        return className;
    }
}

