package io.grpc.example.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class HelloWorldServer {

    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Collection<HelloReply> helloReplyCollection;

    private int port = 42420;
    private Server server;

    private void start() throws Exception {
        logger.info("Starting the grpc server");

        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .build()
                .start();

        logger.info("Server started. Listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** JVM is shutting down. Turning off grpc server as well ***");
            HelloWorldServer.this.stop();
            System.err.println("*** shutdown complete ***");
        }));
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }


    public static void main(String[] args) throws Exception {
        logger.info("Server startup. Args = " + Arrays.toString(args));
        final HelloWorldServer helloWorldServer = new HelloWorldServer();

        helloWorldServer.start();
        helloWorldServer.blockUntilShutdown();
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    private class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            HelloReply response = HelloReply.newBuilder().setMessage("Hello " + request.getName()).build();
            logger.info("Say Hello called for " + request.getName());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }

        @Override
        public void streamHelloReply(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
            List<HelloReply> responses = new LinkedList<>();
            for (int i = 1 ; i <= 10 ; i++){
                HelloReply response = HelloReply.newBuilder().setMessage("Hello " + request.getName() + i).build();
                responses.add(response);
            }
            for (HelloReply response : responses) {
                responseObserver.onNext(response);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logger.info(response.getMessage());
            }
            responseObserver.onCompleted();
        }
    }
}

