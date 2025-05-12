package org.example;

import org.example.sub.Cat;
import org.example.sub.Dog;

public class Main {
    public static void main(String[] args) throws Exception {
        ApplicationContext ioc = new ApplicationContext("org.example");
//        Cat cat = (Cat) ioc.getBean("Cat");
//        System.out.println(cat);
//        Dog dog = (Dog) ioc.getBean("myDog");
//        System.out.println(dog);
    }
}
