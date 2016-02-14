vertx.setPeriodic(1000, { id ->
  vertx.eventBus().send("ping2", "ping1") { reply -> 
    if (reply.succeeded()) 
      println("Reply from ping2: ${reply.result().body()}")
  }
})

vertx.eventBus().consumer("ping1") { message -> 
  message.reply("${message.body()} from ping1")
}
