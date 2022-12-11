package uulm.in.vs.gvs;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private final static ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();

    public static class ChatService extends ChatGrpc.ChatImplBase {
        @Override
        public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {

            var loginResponse = LoginResponse.newBuilder();
            String user = request.getUsername();

            if(users.contains(user)) {
                loginResponse.setStatus(StatusCode.FAILED);
                responseObserver.notify();
            } else {
                String session = new Random(Long.parseLong(user)).toString();
                users.put(user, session);

                loginResponse.setStatus(StatusCode.OK);
                loginResponse.setSessionID(session);

                responseObserver.notify();
            }
        }

        @Override
        public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
            // TODO
        }

        // TODO
    }

    public static void main(String[] args) {
        try {
            // Create and start the server
            Server server = ServerBuilder.forPort(5555)
                    .addService(new ChatService())
                    .build()
                    .start();

            // Add a hook to shut the server down if the program is terminated
            Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));

            // Wait for the server to terminate
            server.awaitTermination();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
