package code.adagedo.proxialertengine.repositories;

import code.adagedo.proxialertengine.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByLatitudeBetweenAndLongitudeBetween(
            BigDecimal minLat, BigDecimal maxLat,
            BigDecimal minLon, BigDecimal maxLon
    );
}
