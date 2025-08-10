package org.example;


import net.minecraftforge.fml.loading.FMLPaths;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/// see `enable_junit` and `junit_version` in gradle.properties
public class ExampleTest {

    @Test
    public void test() {
        Assertions.assertEquals(1, 1);
        System.out.println(FMLPaths.FMLCONFIG.get().relativize(FMLPaths.GAMEDIR.get()));
    }
}
