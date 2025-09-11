package com.xypay.xypay.repository;

import com.xypay.xypay.domain.MenuConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuConfigurationRepository extends JpaRepository<MenuConfiguration, Long> {
    Optional<MenuConfiguration> findByMenuCode(String menuCode);
    List<MenuConfiguration> findByParentMenuCode(String parentMenuCode);
    List<MenuConfiguration> findByMenuLevel(Integer menuLevel);
    List<MenuConfiguration> findByIsVisibleAndIsEnabled(Boolean isVisible, Boolean isEnabled);
    List<MenuConfiguration> findByMenuTypeOrderByDisplayOrder(String menuType);
    List<MenuConfiguration> findAllByOrderByDisplayOrder();
}
