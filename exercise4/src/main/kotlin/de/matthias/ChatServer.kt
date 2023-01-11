package de.matthias

import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.LinkedBlockingQueue
import kotlin.collections.ArrayList

class ChatServer {
    companion object ChatService : ChatGrpc.ChatImplBase() {
        //tuple of session id and username
        private val users = ConcurrentHashMap<String, String>()

        //tuple of session id and message payload
        private val messages = LinkedBlockingQueue<Pair<String, String>>()

        //tuple of session id and message offset
        private val messageSubscriber = ConcurrentHashMap<String, Int>()

        override fun login(request: LoginRequest, responseObserver: StreamObserver<LoginResponse>) {
            val loginResponse = LoginResponse.newBuilder()
            val user = request.username

            if (users.contains(user)) {
                loginResponse.status = StatusCode.FAILED
            } else {
                val session = Random().nextInt().toString()
                users[session] = user
                loginResponse.status = StatusCode.OK
                loginResponse.sessionID = session
            }
            responseObserver.onNext(loginResponse.build())
            responseObserver.onCompleted()
        }

        override fun logout(request: LogoutRequest, responseObserver: StreamObserver<LogoutResponse>) {
            val logoutBuilder = LogoutResponse.newBuilder()

            val username = request.username
            val sessionId = request.sessionID

            if (users.containsKey(sessionId) && users[sessionId] == username) {
                users.remove(sessionId)
                logoutBuilder.status = StatusCode.OK
                messageSubscriber.remove(sessionId)
            } else {
                logoutBuilder.status = StatusCode.FAILED
            }
            responseObserver.onNext(logoutBuilder.build())
            responseObserver.onCompleted()
        }

        override fun chatStream(request: SubscribeMessage, responseObserver: StreamObserver<ChatMessage>) {
            val sessionId = request.sessionID
            if (!users.containsKey(sessionId)) {
                responseObserver.onCompleted()
                return
            }
            if (!messageSubscriber.contains(sessionId)) {
                //set the offset to the current message
                messageSubscriber[sessionId] = messages.size
            }

            while (messageSubscriber.containsKey(sessionId)) {
                val offset = messageSubscriber[sessionId] ?: break
                if (messages.size > offset) {
                    val builder = ChatMessage.newBuilder()
                    //take messages as arrayList and look get the value at [offset]
                    val messageWithSession = ArrayList<Pair<String, String>>(messages)[offset]
                    //format the string to 'user: message'
                    builder.payload = "${users[messageWithSession.first]}: ${messageWithSession.second}"
                    //send the msg
                    responseObserver.onNext(builder.build())
                    //increment the offset for messages
                    messageSubscriber[sessionId] = offset + 1
                }
            }
            responseObserver.onCompleted()
        }

        override fun sendMessage(request: ClientMessage, responseObserver: StreamObserver<Empty>) {
            val sessionId = request.sessionID
            if (!users.containsKey(sessionId)) {
                responseObserver.onCompleted()
                return
            }
            messages.add(sessionId to request.payload)
            responseObserver.onNext(Empty.newBuilder().setStatus(StatusCode.OK).build())
            responseObserver.onCompleted()
        }

        override fun listUsers(request: GetUsersMessage, responseObserver: StreamObserver<UserInfoMessage>) {
            val sessionId = request.sessionID
            if (!users.containsKey(sessionId)) {
                responseObserver.onCompleted()
                return
            }
            users.forEach { (_, username) ->
                val builder = UserInfoMessage.newBuilder()
                builder.username = username
                responseObserver.onNext(builder.build())
            }
            responseObserver.onCompleted()
        }
    }
}

//coroutine scope for clients, so the can busy wait for msg's
suspend fun main() = coroutineScope {
    try {
        val server = ServerBuilder.forPort(5555)
            .addService(ChatServer.ChatService)
            .build()
            .start()


        val clientA = ChatClient("localhost", 5555)
        val clientB = ChatClient("localhost", 5555)

        //launch them, so they can block each other on the thread while waiting
        launch { clientA.login("UserA") }
        launch { clientB.login("UserB") }

        delay(1000)     //wait a sec till login complete

        clientA.listUsers()

        clientA.sendMsg("Hello client b")
        clientB.sendMsg("Hello client a")

        delay(1000)     //wait a sec till msg's came through

        clientA.logout()
        clientB.logout()

        //we could cancel the coroutines, but we don't stop the server either

        // Add a hook to shut the server down if the program is terminated
        Runtime.getRuntime().addShutdownHook(Thread { server.shutdown() })

        // Wait for the server to terminate
        server.awaitTermination()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}