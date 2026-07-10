package code.adagedo.proxialertengine.repositories;

import code.adagedo.proxialertengine.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLatitudeBetweenAndLongitudeBetween(
            Double minLat, Double maxLat,
            Double minLon, Double maxLon
    );
}
