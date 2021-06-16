package br.com.zup.edu.carros

import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository

@Repository
interface CarrosRepository: JpaRepository<Carro, Long> {
    fun existsByPlaca(placa:String) : Boolean
}