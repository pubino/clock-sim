const scope = /** @type {import("./protocol").WorkerScope} */ (/** @type {unknown} */ (self));

async function initialize() {
  const vendor = new URL("./vendor/", scope.location.href).href;
  scope.importScripts(new URL("pyodide.js", vendor).href);
  const [python, response] = await Promise.all([
    scope.loadPyodide({ indexURL: vendor }), fetch(new URL("simulation.py", scope.location.href)),
  ]);
  if (!response.ok) throw new Error("Clock model unavailable.");
  python.FS.writeFile("/home/pyodide/simulation.py", await response.text());
  const face = python.runPython("from simulation import load_face, sample\nload_face()");
  if (typeof face !== "string") throw new Error("Clock face unavailable.");
  const sample = /** @type {(h: number, m: number, s: number) => string} */ (python.runPython("sample"));
  scope.onmessage = ({ data }) => {
    try {
      const hands = JSON.parse(sample(data.hour, data.minute, data.second));
      scope.postMessage({ type: "sample", timestamp: data.timestamp, hands });
    } catch {
      scope.postMessage({ type: "fatal" });
    }
  };
  scope.postMessage({ type: "ready", face: JSON.parse(face) });
}

initialize().catch(() => scope.postMessage({ type: "fatal" }));
