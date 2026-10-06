package mikkelmattern;

import jakarta.persistence.EntityManagerFactory;
import mikkelmattern.config.HibernateConfig;
import mikkelmattern.dao.UserDaoImpl;
import mikkelmattern.entities.User;

public class Main {
    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = HibernateConfig.getEntityManagerFactory();
        UserDaoImpl userDaoImpl = new UserDaoImpl(entityManagerFactory);

        User user = User.builder()
                .username("MikkelMattern")
                .email("Mini0011@stud.ek.dk")
                .password("test123")
                .build();

        userDaoImpl.save(user);
    }
}
