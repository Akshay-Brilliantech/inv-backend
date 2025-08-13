package com.xeine.repository;


import com.xeine.models.Subcategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    List<Subcategory> findByCategoryCategoryIdAndIsActiveTrue(Long categoryId);

    List<Subcategory> findByCompanyCompanyIdAndIsActiveTrue(Long companyId);

    Optional<Subcategory> findBySubcategoryCodeAndCompanyCompanyId(String subcategoryCode, Long companyId);

    @Query("SELECT s FROM Subcategory s WHERE s.category.categoryId = :categoryId AND s.subcategoryName LIKE %:name%")
    List<Subcategory> findByCategoryAndSubcategoryNameContaining(@Param("categoryId") Long categoryId,
                                                                 @Param("name") String name);
}
