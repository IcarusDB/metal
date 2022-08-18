package org.metal.backend;

import org.junit.Test;

public class BackendManagerTest {
    @Test
    public void case0(){
        BackendManager.getBackendBuilder().get()
                .conf("master", "local[*]")
                .conf("appName", "test")
                .build()
                .start();
    }
}
