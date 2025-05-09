package org.example;

import java.util.List;

/**
 * @author
 * @data 2025/5/9 13:03
 * @description
 */
public class ApplicationContext {

    public ApplicationContext(String packageName) {
        initContext(packageName);
    }

    public Object getBean(String beanName) {
        return null;
    }

    public <T> T getBean(Class<T> Type) {
        return null;
    }

    public <T> List<T> getBeans(Class<T> type) {
        return null;
    }

    public void initContext(String packageName) {
        ApplicationContext.class.getClassLoader().getResource("packageName");
    }
}
