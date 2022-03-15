/******************************************************************************
 * Copyright 2022 TypeFox GmbH
 * This program and the accompanying materials are made available under the
 * terms of the MIT License, which is available in the project root.
 ******************************************************************************/
package io.typefox.elkserver;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public final class JsonObjects {

    private JsonObjects() {}

    public static class Error {
        public String message;
        public String name;
        public String stack;

        public Error(Exception exception) {
            this.message = exception.getMessage();
            this.name = exception.getClass().getSimpleName();
            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            this.stack = sw.toString();
        }
    }

    public static class Dimension {
        public double width;
        public double height;

        public Dimension(double width, double height) {
            this.width = width;
            this.height = height;
        }
    }

    public static class Point {
        public double x;
        public double y;
        
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    public interface LayoutElement {
    }

    public static class ShapeLayoutElement implements LayoutElement {
        public Point position;
        public Dimension size;

        public ShapeLayoutElement(Point position, Dimension size) {
            this.position = position;
            this.size = size;
        }
    }

    public static class EdgeLayoutElement implements LayoutElement {
        public List<Point> route;

        public EdgeLayoutElement(List<Point> route) {
            this.route = route;
        }
    }

}