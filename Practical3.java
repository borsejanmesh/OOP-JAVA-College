interface Flyable {
    void move();
}

interface Swimmable {
    void move();
}

class Duck implements Flyable, Swimmable {
    @Override
    public void move() {
        System.out.println("The duck moves through the air or water.");
    }

    public void quack() {
        System.out.println("Quack!");
    }
}

public class Practical3 {
    public static void main(String[] args) {
        Duck duck = new Duck();

        // Polymorphism: the Duck object is used through either interface.
        Flyable flyingDuck = duck;
        Swimmable swimmingDuck = duck;

        flyingDuck.move();
        swimmingDuck.move();

        duck.quack();
    }
}