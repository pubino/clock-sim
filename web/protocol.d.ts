export type Point = [x: number, y: number];
export type Line = [Point, Point];
export interface Face { hours: Line[]; minutes: Line[]; rings: [number, number]; half_width: number }
export interface Hands { second: Line; minute: [Point, Point, Point]; hour: [Point, Point, Point] }
export interface Request { hour: number; minute: number; second: number; timestamp: number }
export type Reply =
  | { type: "ready"; face: Face }
  | { type: "sample"; timestamp: number; hands: Hands }
  | { type: "fatal" };
export interface WorkerScope {
  importScripts(url: string): void;
  location: Location;
  loadPyodide(options: { indexURL: string }): Promise<{
    FS: { writeFile(path: string, contents: string): void };
    runPython(code: string): unknown;
  }>;
  onmessage: (event: MessageEvent<Request>) => void;
  postMessage(message: Reply): void;
}
