package br.com.zup.edu.test

import br.com.zup.edu.CarroRequest
import br.com.zup.edu.CarrosGrpcServiceGrpc
import br.com.zup.edu.carros.Carro
import br.com.zup.edu.carros.CarrosRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.nio.channels.Channel
import javax.inject.Singleton

@MicronautTest(transactional = false) //teste de integração
internal class CarrosEndpointTest(
    val repository: CarrosRepository,
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setup(){
        repository.deleteAll()
    }

    /**
     * 1. happy path - ok
     * 2. quando ja existe carro com a placa - ok
     * 3. quando os dados de entrada são invalidos - ok
     */

    @Test
    fun `deve adicionar um novo carro`() {
        // Cenário - limpando
//        repository.deleteAll() não preciso mais

        // Ação precisa de um cliente GRPC - exercitamos o endpoint
        val response = grpcClient.adicionar(
            CarroRequest.newBuilder()
                .setModelo("Gol")
                .setPlaca("HPX-1234")
                .build()
        )

        // Validação
        with(response) {
            assertNotNull(id)
            assertTrue(repository.existsById(id)) // efeito colateral esta gravado o registro
        }
    }

    @Test
    fun `nao deve adicionar novo carro quando carro com placa ja existente`(){
        // Cenário
//        repository.deleteAll() não preciso mais
        val existente = repository.save(Carro(modelo = "Palio", placa = "OIP-9876"))

        // Ação - Consumir um endpoint com placa repetida esperando erro
        val error = assertThrows<StatusRuntimeException>{
//            grpcClient.adicionar(CarroRequest.newBuilder().build())
            grpcClient.adicionar(CarroRequest.newBuilder()
                .setModelo("Ferrari")
                .setPlaca(existente.placa)
                .build())
        }


        // Validação desse caminho alternativo
        with(error){
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("carro com placa existente", status.description)
            // TODO: verificar as violações da bean validations
        }

    }

    @Test
    fun `nao deve adicionar novo carro quando dados de entrada forem invalidos`(){
        // Cenário
//        repository.deleteAll() não preciso mais

        // Ação - Consumir um endpoint com placa repetida esperando erro
        val error = assertThrows<StatusRuntimeException>{
            grpcClient.adicionar(CarroRequest.newBuilder()
                .setModelo("")
                .setPlaca("")
                .build())
        }


        // Validação desse caminho alternativo
        with(error){
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("dados de entrada inválidos", status.description)
        }


    }

    // Fabrica GRPC
    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }

    }

}