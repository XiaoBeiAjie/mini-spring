package org.example.sub;

import org.example.Autowired;
import org.example.Component;
import org.example.PostConstruct;

@Component
public class Cat {

    @Autowired
    Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("Cat创建了一个属性" + dog);
    }
}
