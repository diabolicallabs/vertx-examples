vertx.setPeriodic(1000, { id ->
  vertx.eventBus().send("ping1", "ping2") { reply -> 
    if (reply.succeeded()) 
      println("Reply from ping1: ${reply.result().body()}")
  }
})

vertx.eventBus().consumer("ping2") { message -> 
  message.reply("${message.body()} from ping2")
}

