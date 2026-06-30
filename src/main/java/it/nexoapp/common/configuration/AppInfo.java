package it.nexoapp.common.configuration;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithName;
import it.nexoapp.common.constant.Environment;
import org.slf4j.event.Level;

@StaticInitSafe
@ConfigMapping(prefix = "app")
public interface AppInfo {

    String code();
    String name();
    String description();
    Environment environment();
    String hostname();
    @WithName("log.level")
    Level logLevel();

}