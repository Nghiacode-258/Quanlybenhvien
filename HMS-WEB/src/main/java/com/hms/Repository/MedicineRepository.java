package com.hms.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.Entity.Medicine;

public interface MedicineRepository extends JpaRepository<Medicine, String>{
    
}
