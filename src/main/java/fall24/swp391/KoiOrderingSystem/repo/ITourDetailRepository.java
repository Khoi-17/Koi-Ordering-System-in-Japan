package fall24.swp391.KoiOrderingSystem.repo;

import fall24.swp391.KoiOrderingSystem.pojo.TourDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface ITourDetailRepository extends JpaRepository<TourDetail, Long> {

    @Query(name = "select * from tour_detail where farm_id = ?1", nativeQuery = true)
    Set<TourDetail> findByFarm_Id(Long farmID);

    @Query(name = "select * from tour_detail where tour_id = ?1", nativeQuery = true)
    Set<TourDetail> findByTour_Id(Long tourID);
}
