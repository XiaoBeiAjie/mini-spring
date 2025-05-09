package org.example;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

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

    private Map<String, Object> ioc = new HashMap<>();

    public Object getBean(String beanName) {
        return null;
    }

    public <T> T getBean(Class<T> Type) {
        return null;
    }

    public <T> List<T> getBeans(Class<T> type) {
        return null;
    }

    public void initContext(String packageName) throws Exception {
        scanPackage(packageName)
                .stream().filter(this::scanCreate)
                .map(this::wrapper)
                .forEach(this::createBean);
        ApplicationContext.class.getClassLoader().getResource("");
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
        return new BeanDefinition(type);
    }

    protected void createBean(BeanDefinition beanDefinition) {
        String beanName = beanDefinition.getName();
        if (ioc.containsKey(beanName)) {
            return;
        }
        doCreateBean(beanDefinition);
    }

    private void doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ioc.put(beanDefinition.getName(), bean);
    }

}
