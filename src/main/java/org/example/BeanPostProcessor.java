package org.example;

public interface BeanPostProcessor {
    default Object beforeInitializeBean(Object bean, String beanName) {
        return bean;
    }
    default Object afterInitializeBean(Object bean, String beanName) {
        return bean;
    }
}
