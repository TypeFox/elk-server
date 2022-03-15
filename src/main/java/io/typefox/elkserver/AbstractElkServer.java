/******************************************************************************
 * Copyright 2022 TypeFox GmbH
 * This program and the accompanying materials are made available under the
 * terms of the MIT License, which is available in the project root.
 ******************************************************************************/
package io.typefox.elkserver;

import static io.typefox.elkserver.JsonObjects.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

import org.eclipse.elk.alg.force.options.ForceMetaDataProvider;
import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.elk.alg.mrtree.options.MrTreeMetaDataProvider;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkShape;
import org.eclipse.elk.graph.json.ElkGraphJson;

public abstract class AbstractElkServer {

    private static final int STRING_BUILDER_CAPACITY = 1 << 18;

    public void start() {
        initialize();
        try {
            run();
        } catch (Throwable thr) {
            thr.printStackTrace();
            System.exit(1);
        }
    }

    protected void initialize() {
        LayoutMetaDataService.getInstance().registerLayoutMetaDataProviders(
            new LayeredMetaDataProvider(),
            new ForceMetaDataProvider(),
            new MrTreeMetaDataProvider()
        );
    }

    protected abstract void run() throws Exception;

    protected ElkNode readGraph(InputStream input) throws IOException {
        var objLevel = 0;
        var strBuilder = new StringBuilder(STRING_BUILDER_CAPACITY);
        do {
            var ch = input.read();
            if (ch < 0) {
                return null;
            } else if (ch == '{') {
                objLevel++;
            } else if (ch == '}') {
                objLevel--;
            }
            strBuilder.append((char) ch);
        } while (objLevel > 0);
        return ElkGraphJson.forGraph(strBuilder.toString()).toElk();
    }

    protected void writeLayoutData(ElkNode graph, Appendable output, Gson gson) {
        var data = new LinkedHashMap<String, LayoutElement>();
        computeLayoutData(graph, data);
        gson.toJson(data, output);
    }

    protected void computeLayoutData(ElkNode node, Map<String, LayoutElement> data) {
        if (node.getIdentifier() != null) {
            data.put(node.getIdentifier(), getShapeLayout(node));
        }
        for (var label : node.getLabels()) {
            if (label.getIdentifier() != null) {
                data.put(label.getIdentifier(), getShapeLayout(label));
            }
        }
        for (var port : node.getPorts()) {
            if (port.getIdentifier() != null) {
                data.put(port.getIdentifier(), getShapeLayout(port));
            }
            for (var label : port.getLabels()) {
                if (label.getIdentifier() != null) {
                    data.put(label.getIdentifier(), getShapeLayout(label));
                }
            }
        }
        for (var edge : node.getContainedEdges()) {
            if (edge.getIdentifier() != null) {
                data.put(edge.getIdentifier(), getEdgeLayout(edge));
            }
        }
        for (var child : node.getChildren()) {
            computeLayoutData(child, data);
        }
    }

    protected ShapeLayoutElement getShapeLayout(ElkShape shape) {
        var position = new Point(shape.getX(), shape.getY());
        var size = new Dimension(shape.getWidth(), shape.getHeight());
        return new ShapeLayoutElement(position, size);
    }

    protected EdgeLayoutElement getEdgeLayout(ElkEdge edge) {
        if (edge.getSections().isEmpty()) {
            return new EdgeLayoutElement(Collections.emptyList());
        }
        var section = edge.getSections().get(0);
        var routingPoints = new ArrayList<Point>();
        var p1 = new Point(section.getStartX(), section.getStartY());
        routingPoints.add(p1);
        for (var bendPoint : section.getBendPoints()) {
            var p2 = new Point(bendPoint.getX(), bendPoint.getY());
            routingPoints.add(p2);
        }
        var p3 = new Point(section.getEndX(), section.getEndY());
        routingPoints.add(p3);
        return new EdgeLayoutElement(routingPoints);
    }

}