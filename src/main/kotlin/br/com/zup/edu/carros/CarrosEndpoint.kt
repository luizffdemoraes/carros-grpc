package br.com.zup.edu.carros

import br.com.zup.edu.CarroRequest
import br.com.zup.edu.CarroResponse
import br.com.zup.edu.CarrosGrpcServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.ConstraintViolationException

@Singleton
class CarrosEndpoint(@Inject val repository: CarrosRepository) : CarrosGrpcServiceGrpc.CarrosGrpcServiceImplBase() {

    override fun adicionar(request: CarroRequest, responseObserver: StreamObserver<CarroResponse>) {
        if (repository.existsByPlaca(request.placa)) {
            responseObserver.onError(
                Status.ALREADY_EXISTS
                    .withDescription("carro com placa existente")
                    .asRuntimeException()
            )
            return
        }

        val carro = Carro(request.modelo, request.placa)

        try {
            repository.save(carro)
        } catch (e: ConstraintViolationException) {
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("dados de entrada inválidos")
                    .asRuntimeException()
            )
            return
        }

        responseObserver.onNext(
            CarroResponse.newBuilder()
                .setId(carro.id!!)
                .build()
        )
        responseObserver.onCompleted()

    }


}