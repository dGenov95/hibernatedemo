package app;

import javax.persistence.Persistence;

public class Application {

    public static void main(String[] args) {
        new Engine(Persistence.createEntityManagerFactory("soft_uni").createEntityManager())
                .run();
    }
}
