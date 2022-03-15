/******************************************************************************
 * Copyright 2022 TypeFox GmbH
 * This program and the accompanying materials are made available under the
 * terms of the MIT License, which is available in the project root.
 ******************************************************************************/
package io.typefox.elkserver;

import com.google.gson.Gson;

import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkNode;

/**
 * ELK layout server connected via System.in / System.out
 */
public class StdioElkServer extends AbstractElkServer {

    @Override
    protected void run() throws Exception {
        var engine = new RecursiveGraphLayoutEngine();
        var gson = new Gson();

        ElkNode graph;
        while ((graph = readGraph(System.in)) != null) {
            try {
                engine.layout(graph, new BasicProgressMonitor());
                writeLayoutData(graph, System.out, gson);
            } catch (Exception exc) {
                gson.toJson(new JsonObjects.Error(exc), System.out);
            }
        }
    }

}