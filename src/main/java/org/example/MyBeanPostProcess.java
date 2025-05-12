package org.example;
@Component
public class MyBeanPostProcess implements BeanPostProcessor {
    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        System.out.println("初始化完成" + beanName);
        return bean;
    }
}
