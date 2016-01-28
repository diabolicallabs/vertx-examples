package com.diabolicallabs.test;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class MongoTestBase {

  private static MongodExecutable mongoDb;

  @BeforeClass
  public static void startMongo() throws Exception {

    IMongodConfig config = new MongodConfigBuilder().
      version(Version.Main.PRODUCTION).
      net(new Net(27018, Network.localhostIsIPv6())).
      build();

    mongoDb = MongodStarter.getDefaultInstance().prepare(config);
    mongoDb.start();
  }

  @AfterClass
  public static void stopMongo() {

    if (mongoDb != null) {
      mongoDb.stop();
    }
  }

}
