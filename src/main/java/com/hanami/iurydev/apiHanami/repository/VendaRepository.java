package com.hanami.iurydev.apiHanami.repository;


import com.hanami.iurydev.apiHanami.entity.Venda;
import com.hanami.iurydev.apiHanami.entity.enums.Regiao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    boolean existsByIdTransacao(String idTransacao);

    Optional<Venda> findByIdTransacao(String idTransacao);

    List<Venda> findByDataVendaBetween(LocalDate dataInicio, LocalDate dataFim);

    List<Venda> findByLogistica_Regiao(Regiao regiao);

    List<Venda> findByCliente_Estado(String estado);
}
