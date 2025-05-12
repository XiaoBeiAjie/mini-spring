package org.example;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author a-j1e
 * @data 2025/5/9 13:03
 * @description
 */
@SuppressWarnings({
        "all",           // 忽略所有警告
        "unused",        // 未使用的变量
        "unchecked",     // 未检查的转换
        "rawtypes",      // 使用原生类型
        "deprecation",   // 使用已废弃的API
        "serial"         // 序列化相关
})
public class ApplicationContext {

    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    private Map<String, BeanDefinition> beanDefinitions = new HashMap<>();
    private Map<String, Object> ioc = new HashMap<>();
    private Map<String, Object> loadingIoc = new HashMap<>();
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public void initContext(String packageName) throws Exception {
        scanPackage(packageName)
                .stream().filter(this::scanCreate)
                .forEach(this::wrapper);
        initBeanPostProcessor();
        beanDefinitions.values().stream()
                .forEach(this::createBean);
    }

    private List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        URL resource = this.getClass()
                .getClassLoader()
                .getResource(packageName.replace(".", File.separator));
        Path path = Path.of(Objects.requireNonNull(resource).getFile());
        Files.walkFileTree(path,
                new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        Path absolutePath = file.toAbsolutePath();
                        if (absolutePath.toString().endsWith(".class")) {
                            String fileName = absolutePath.toString()
                                    .replace(File.separator, ".");
                            String className = fileName.substring(fileName.indexOf(packageName)
                                    , fileName.length() - ".class".length());
                            try {
                                classList.add(Class.forName(className));
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

        return classList;
    }

    protected Boolean scanCreate(Class<?> type) {
        return type.isAnnotationPresent(Component.class);
    }

    protected BeanDefinition wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitions.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("Duplicate bean name: " + beanDefinition.getName());
        }
        beanDefinitions.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    private void initBeanPostProcessor() {
        beanDefinitions.values().stream()
                .filter(db -> BeanPostProcessor.class.isAssignableFrom(db.getBeanType()))
                .map(this::createBean)
                .map(bean -> (BeanPostProcessor) bean)
                .forEach(beanPostProcessors::add);
    }

    public Object getBean(String beanName) {
        if (beanName == null) {
            return null;
        }
        Object bean = this.ioc.get(beanName);
        if (bean != null)
            return bean;
        if (this.beanDefinitions.containsKey(beanName)) {
            return createBean(beanDefinitions.get(beanName));
        }
        return null;
    }

    protected Object createBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.getName();
        if (ioc.containsKey(beanName)) {
            return ioc.get(beanName);
        }
        if (loadingIoc.containsKey(beanName)) {
            return loadingIoc.get(beanName);
        }
        return doCreateBean(beanDefinition);
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            // 实例化
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            // 自动注入
            autowiredBean(bean, beanDefinition);
            // postConstruct
            bean = initializeBean(beanDefinition, bean);
            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return bean;
    }

    private Object initializeBean(BeanDefinition beanDefinition, Object bean) throws IllegalAccessException, InvocationTargetException {
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.beforeInitializeBean(bean, beanDefinition.getName());
        }
        Method method = beanDefinition.getPostContructMethod();
        if (method != null) {
            method.invoke(bean);
        }
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.afterInitializeBean(bean, beanDefinition.getName());
        }
        return bean;
    }

    public <T> T getBean(Class<T> type) {
        String beanName = this.beanDefinitions.values().stream()
                .filter(beanDefinition -> type.equals(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .findFirst().orElse(null);
        return (T) getBean(beanName);
    }

    public <T> List<T> getBeans(Class<T> type) {
        return this.beanDefinitions.values().stream()
                .filter(beanDefinition -> type.equals(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean -> (T) bean)
                .collect(Collectors.toList());
    }



    private void autowiredBean(Object bean, BeanDefinition beanDefinition) {
        beanDefinition.getAutowiredFields().forEach((field) -> {
            field.setAccessible(true);
            try {
                field.set(bean, getBean(field.getType()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
