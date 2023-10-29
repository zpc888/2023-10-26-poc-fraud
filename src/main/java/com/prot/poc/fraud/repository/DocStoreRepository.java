package com.prot.poc.fraud.repository;

import com.prot.poc.fraud.entity.DocStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * @Author: <a href="mailto: pengcheng.zhou@gmail.com">PengCheng Zhou</a>
 * @Created: 2023-10-27T23:11 Friday
 */
public interface DocStoreRepository extends JpaRepository<DocStore, Long> {
    @Query("SELECT d FROM DocStore d WHERE d.sourceNumber = :sourceNumber and d.vendorName = :vendorName")
    Optional<DocStore> findDocByVendorInfo(String sourceNumber, String vendorName);
}
