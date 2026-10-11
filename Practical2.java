class Animal {
    void eat() {
        System.out.println("The animal is eating.");
    }

    void sleep() {
        System.out.println("The animal is sleeping.");
    }
}

class Dog extends Animal {
    void bark() {
        System.out.println("The dog is barking.");
    }
}

public class Practical2 {
    public static void main(String[] args) {
        Dog dog = new Dog();

        // Calls methods inherited from Animal
        dog.eat();
        dog.sleep();

        // Calls a method defined in Dog
        dog.bark();
    }
}