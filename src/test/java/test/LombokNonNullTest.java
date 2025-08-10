package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zank.mods.kube_packages.impl.dependency.ImmutableMetaData;

/**
 * @author ZZZank
 */
public class LombokNonNullTest {

    @Test
    public void test() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            new ImmutableMetaData(null, null, null, null, null, null, null);
        });
    }
}
