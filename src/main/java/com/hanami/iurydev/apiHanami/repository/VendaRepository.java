package com.hanami.iurydev.apiHanami.repository;

import com.hanami.iurydev.apiHanami.entity.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    boolean existsByIdTransacao(String idTransacao);

    List<Venda> findByProcessadoSucessoTrue();

    List<Venda> findByProcessadoSucessoTrueAndCliente_Estado(String estado);

    List<Venda> findByDataVendaBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT v.idTransacao FROM Venda v WHERE v.idTransacao IN :ids")
    List<String> findExistingIds(@Param("ids") List<String> ids);
}
