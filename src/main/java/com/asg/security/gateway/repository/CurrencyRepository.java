package com.asg.security.gateway.repository;

import com.asg.security.gateway.entity.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Currency findByCurrencyPoid(Long currencyPoid);
}
