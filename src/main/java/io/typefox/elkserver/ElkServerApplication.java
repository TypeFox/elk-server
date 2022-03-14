/******************************************************************************
 * Copyright 2022 TypeFox GmbH
 * This program and the accompanying materials are made available under the
 * terms of the MIT License, which is available in the project root.
 ******************************************************************************/
package io.typefox.elkserver;

import java.util.Arrays;

public class ElkServerApplication {

    public static void main(String[] args) {
        var argsList = Arrays.asList(args);
        var stdio = argsList.contains("--stdio");
        var socket = argsList.contains("--socket");
        if (!stdio && !socket) {
            System.err.println("The server must be started with either --stdio or --socket argument.");
            System.exit(1);
        }
        if (stdio && socket) {
            System.err.println("The --stdio and --socket arguments cannot be used at the same time.");
            System.exit(1);
        }
        if (stdio) {
            new StdioElkServer().start();
        }
        if (socket) {
            new SocketElkServer().start();
        }
    }
    
}
