package com.xeine.repository;

import com.xeine.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByCompanyCompanyIdAndIsActiveTrue(Long companyId);

    Optional<Category> findByCategoryCodeAndCompanyCompanyId(String categoryCode, Long companyId);

    @Query("SELECT c FROM Category c WHERE c.company.companyId = :companyId AND c.categoryName LIKE %:name%")
    List<Category> findByCompanyAndCategoryNameContaining(@Param("companyId") Long companyId,
                                                          @Param("name") String name);
}