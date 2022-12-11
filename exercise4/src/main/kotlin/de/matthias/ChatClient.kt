package de.matthias

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.delay

class ChatClient(
    host: String,
    port: Int
) {
    private val chanel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext()
        .build()
    private val stub: ChatGrpc.ChatBlockingStub = ChatGrpc.newBlockingStub(chanel)
    private var session: String = ""
    private var username: String = ""

    suspend fun login(username: String) {
        println("[$username] Logging in ...")
        val data = stub.login(LoginRequest.newBuilder().setUsername(username).build())
        if(data.status == StatusCode.FAILED) {
            println("[$username] Failed to login")
            return
        }
        session = data.sessionID
        this.username = username

        println("[$username] Status: ${data.status}")

        val chatData = stub.chatStream(SubscribeMessage.newBuilder().setSessionID(session).build())

        while (!chanel.isShutdown) {
            if(chatData.hasNext()) {
                println("[$username] ${chatData.next().payload}")
            } else {
                delay(10)
            }
        }
    }

    fun logout() {
        println("[$username] Logging out ...")
        stub.logout(LogoutRequest.newBuilder().setSessionID(session).setUsername(username).build())
        chanel.shutdown()
    }

    fun listUsers() {
        val data = stub.listUsers(GetUsersMessage.newBuilder().setSessionID(session).build())
        println("[$username] User Data:")
        while (data.hasNext()) {
            println(data.next().username)
        }
    }

    fun sendMsg(msg: String) {
        stub.sendMessage(ClientMessage.newBuilder().setSessionID(session).setPayload(msg).build())
    }
}