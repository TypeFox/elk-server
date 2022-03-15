/******************************************************************************
 * Copyright 2022 TypeFox GmbH
 * This program and the accompanying materials are made available under the
 * terms of the MIT License, which is available in the project root.
 ******************************************************************************/
package io.typefox.elkserver;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkNode;

public class SocketElkServer extends AbstractElkServer {

    protected final Logger logger = Logger.getLogger(SocketElkServer.class.getName());

    @Override
    protected void run() throws Exception {
        var executorService = Executors.newCachedThreadPool();
        var address = new InetSocketAddress("localhost", 5008);
        try (
            var serverSocket = AsynchronousServerSocketChannel.open().bind(address)
        ) {
            logger.info("ELK Server listening to " + address);
            while (true) {
                var socketChannel = serverSocket.accept().get();
                logger.info("Accepted new connection.");
                executorService.submit(() -> {
                    try {
                        handle(socketChannel);
                    } catch (Exception exc) {
                        logger.severe(exc.getMessage());
                        exc.printStackTrace();
                    }
                });
            }
        } finally {
            executorService.shutdown();
        }
    }

    protected void handle(AsynchronousSocketChannel socketChannel) throws IOException {
        var input = Channels.newInputStream(socketChannel);
        var writer = new OutputStreamWriter(Channels.newOutputStream(socketChannel), Charset.forName("UTF-8"));
        var engine = new RecursiveGraphLayoutEngine();
        var gson = new Gson();

        ElkNode graph;
        while ((graph = readGraph(input)) != null) {
            logger.log(Level.FINE, "Received input graph.");
            try {
                engine.layout(graph, new BasicProgressMonitor());
                logger.log(Level.FINE, "Computed layout");

                writeLayoutData(graph, writer, gson);
                writer.flush();
                logger.log(Level.FINE, "Sent JSON data.");
            } catch (Exception exc) {
                gson.toJson(new JsonObjects.Error(exc), writer);
            }
        }
    }

}