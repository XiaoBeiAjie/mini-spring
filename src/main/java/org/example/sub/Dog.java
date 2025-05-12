package org.example.sub;

import org.example.Autowired;
import org.example.Component;
import org.example.PostConstruct;

@Component(name = "myDog")
public class Dog {
    @Autowired
    private Cat cat;

    @PostConstruct
    public void init() {
        System.out.println("Dog创建了一个属性" + cat);
    }
}
