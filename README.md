# ELK Server

This project wraps the [Eclipse Layout Kernel (ELK)](https://www.eclipse.org/elk/) in a standalone Java application. It can be connected to a client via a process pipe (standard I/O) or a socket (useful for debugging).

## Usage

Start the application with the `--stdio` argument to connect via a process pipe, or with the `--socket` argument to connect via a socket.

### Input

The input of the server is an [ELK graph in JSON format](https://www.eclipse.org/elk/documentation/tooldevelopers/graphdatastructure/jsonformat.html). The server responds to an input graph as soon as the full JSON data has been received (detected with the closing `'}'` character).

### Output

The output is a JSON object mapping element identifiers to their layout information. The following TypeScript definitions specify the output format:

```typescript
interface LayoutData {
    [id: string]: LayoutElement
}

type LayoutElement = ShapeLayoutElement | EdgeLayoutElement

interface ShapeLayoutElement {
    position: Point
    size: Dimension
}

interface EdgeLayoutElement {
    route: Point[]
}

interface Point {
    x: number
    y: number
}

interface Dimension {
    width: number
    height: number
}
```

In case the request cannot be fulfilled due to a Java exception, the server responds with an error description with the following format:

```typescript
interface Error {
    message: string
    name: string
    stack: string
}
```

## Client Implementation

A client for `elk-server` is available for Sprotty in the [sprotty-elk](https://www.npmjs.com/package/sprotty-elk) package. See the examples in the [Sprotty repository](https://github.com/eclipse/sprotty) for more details.
