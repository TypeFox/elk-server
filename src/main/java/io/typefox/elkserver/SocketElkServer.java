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
            int nextClientNo = 0;
            while (true) {
                var socketChannel = serverSocket.accept().get();
                var clientNo = nextClientNo++;
                logger.info("[" + clientNo + "] Accepted new connection.");
                executorService.submit(() -> {
                    try {
                        handle(socketChannel, clientNo);
                        logger.info("[" + clientNo + "] Closed connection.");
                    } catch (Exception exc) {
                        logger.severe("[" + clientNo + "]" + exc.getMessage());
                        exc.printStackTrace();
                    }
                });
            }
        } finally {
            executorService.shutdown();
        }
    }

    protected void handle(AsynchronousSocketChannel socketChannel, int clientNo) throws IOException {
        var input = Channels.newInputStream(socketChannel);
        var writer = new OutputStreamWriter(Channels.newOutputStream(socketChannel), Charset.forName("UTF-8"));
        var engine = new RecursiveGraphLayoutEngine();
        var gson = new Gson();

        ElkNode graph;
        while ((graph = readGraph(input)) != null) {
            logger.info("[" + clientNo + "] Received input graph.");
            try {
                engine.layout(graph, new BasicProgressMonitor());
                logger.info("[" + clientNo + "] Computed layout.");

                writeLayoutData(graph, writer, gson);
                writer.flush();
                logger.info("[" + clientNo + "] Sent JSON data.");
            } catch (Exception exc) {
                gson.toJson(new JsonObjects.Error(exc), writer);
            }
        }
    }

}